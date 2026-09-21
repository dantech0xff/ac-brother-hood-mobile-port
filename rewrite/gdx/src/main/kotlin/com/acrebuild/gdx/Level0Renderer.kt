package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.Entity
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.ScreenUtils

/**
 * render-gdx adapter for the slice-1 world. Draws the pack-6 tile layers,
 * NPC entities and the player into the 400x240 logical framebuffer, then
 * integer-scales with nearest filtering (same contract as PixelRenderer).
 *
 * World space is y-down (J2ME): the FBO ortho is y-up, so every draw
 * converts `fboY = VIEW_H - worldY - spriteH`.
 *
 * Tile draw math ported from `k.java:4460-4540`: cell value = composite
 * object index into the layer's tileset clip; 2-bit flag `dX` mirrors on
 * bit0 / flips on bit1 and shifts the anchor by +20 on the affected axis;
 * placements then follow `b.java:915`.
 */
class Level0Renderer {

    @Volatile var scale: Int = 1; private set
    @Volatile var offsetX: Int = 0; private set
    @Volatile var offsetY: Int = 0; private set

    private lateinit var fbo: FrameBuffer
    private lateinit var batch: SpriteBatch

    // module index -> TextureRegion, per pack id
    private val clipModules = HashMap<Int, Array<TextureRegion?>>()
    private val clipDims = HashMap<Int, Array<Pair<Int, Int>>>()
    private var clips: Map<Int, Clip> = emptyMap()

    fun create(world: Level0World) {
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, Level0World.VIEW_W, Level0World.VIEW_H, false)
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        batch = SpriteBatch()
        clips = world.clips
        for ((packId, clip) in clips) {
            val regs = arrayOfNulls<TextureRegion>(clip.moduleNames.size)
            val dims = Array(clip.moduleNames.size) { clip.moduleWidth(it) to clip.moduleHeight(it) }
            val base = when (packId) {
                0 -> "clips/clip0/modules"
                7 -> "clips/clip7/modules"
                else -> "level0/tileset-$packId/modules"
            }
            for (i in clip.moduleNames.indices) {
                val t = Texture(Gdx.files.internal("$base/${clip.moduleNames[i]}"))
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
                regs[i] = TextureRegion(t)
            }
            clipModules[packId] = regs
            clipDims[packId] = dims
        }
    }

    /**
     * Draw module `m` of clip `pack` with J2ME `Sprite.TRANS_*` `transform`
     * (`b` uses `aQ[i & 7]`). J2ME draws the *transformed* image's top-left
     * at (x, y); rot90/270 swap the box to h×w.
     * Constants: 0 none, 1 MIRROR_ROT180, 2 MIRROR, 3 ROT180,
     * 4 MIRROR_ROT270, 5 ROT90, 6 ROT270, 7 MIRROR_ROT90.
     * FBO space is y-up vs J2ME y-down: screen-CW rotations are CCW here.
     */
    private fun drawModule(pack: Int, m: Int, x: Int, y: Int, transform: Int) {
        val src = clipModules[pack]?.getOrNull(m) ?: return
        val (w, h) = clipDims[pack]!![m]
        val t = transform and 7
        val region = TextureRegion(src)
        var rot = 0f
        var dw = w; var dh = h
        when (t) {
            1 -> region.flip(false, true)               // MIRROR_ROT180 = V flip
            2 -> region.flip(true, false)               // MIRROR = H flip
            3 -> region.flip(true, true)                // ROT180
            4 -> { region.flip(true, false); rot = 270f; dw = h; dh = w }
            5 -> { rot = 90f; dw = h; dh = w }          // ROT90 (screen CW)
            6 -> { rot = -90f; dw = h; dh = w }         // ROT270
            7 -> { region.flip(true, false); rot = 90f; dw = h; dh = w }
        }
        val fy = Level0World.VIEW_H - y - dh
        if (rot == 0f) {
            batch.draw(region, x.toFloat(), fy.toFloat(), dw.toFloat(), dh.toFloat())
        } else {
            // rotate the un-rotated w×h quad about the destination box center
            val cx = x + dw / 2f
            val cy = fy + dh / 2f
            batch.draw(region, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f,
                       w.toFloat(), h.toFloat(), 1f, 1f, rot)
        }
    }

    /**
     * Recursive object draw — `b.java:915` composite path. Objects index a
     * shared space: `ai[obj]==0` -> plain module `obj`; otherwise `ai[obj]`
     * placements (module or nested object when `aq & 16`). `flags` is the
     * caller's flip word (2-bit on tiles, `P & 7` on entities).
     */
    private fun drawObject(pack: Int, obj: Int, x: Int, y: Int, flags: Int, depth: Int = 0) {
        val clip = clips[pack] ?: return
        if (obj < 0 || obj >= clip.objPlaceStart.size || depth > 4) return
        val count = clip.objPlaceCount[obj]
        if (count == 0) {
            drawModule(pack, obj, x, y, flags)
            return
        }
        for ((m0, pf, off) in clip.placements(obj)) {
            // target object index: ap | ((aq & 0xC0) << 2) (b.java:920)
            val m = m0 or ((pf and 0xC0) shl 2)
            var mw = if (m < clip.moduleW.size) clip.moduleWidth(m) else 0
            var mh = if (m < clip.moduleW.size) clip.moduleHeight(m) else 0
            val tf = flags xor pf
            if (tf and 4 != 0) { val tmp = mw; mw = mh; mh = tmp }
            val dx = if (flags and 1 != 0) -(off.first + mw) else off.first
            val dy = if (flags and 2 != 0) -(off.second + mh) else off.second
            if (pf and 16 == 0) {
                drawModule(pack, m, x + dx, y + dy, tf and 15)
            } else {
                // aq bit 0x10: target is another composite object, not a module
                drawObject(pack, m, x + dx, y + dy, tf and 15, depth + 1)
            }
        }
    }

    /** `b.java:915` composite-sprite draw for one tile cell. */
    private fun drawTileCell(pack: Int, cell: Int, x: Int, y: Int, dX: Int) {
        if (cell == 255) return
        val clip = clips[pack] ?: return
        if (cell >= clip.objPlaceStart.size) return
        // tile cells sit on a 20px grid: +20 anchor compensation on the
        // mirrored axes (k.java:4476-4490)
        val anchorX = x + if (dX and 1 != 0) 20 else 0
        val anchorY = y + if (dX and 2 != 0) 20 else 0
        drawObject(pack, cell, anchorX, anchorY, dX)
    }

    fun render(world: Level0World) {
        fbo.begin()
        ScreenUtils.clear(0.07f, 0.07f, 0.09f, 1f)
        batch.projectionMatrix.setToOrtho2D(
            0f, 0f, Level0World.VIEW_W.toFloat(), Level0World.VIEW_H.toFloat())
        batch.begin()

        val camX = world.camX
        val camY = world.camY
        // draw order: eu backdrop (skipped v1), ep, er — same as original
        for (layer in world.level.layers) {
            if (layer.id == 0 || layer.id == 2) continue
            val pack = layer.tilesetClip
            val c0 = (camX / 20).coerceAtLeast(0)
            val c1 = ((camX + Level0World.VIEW_W) / 20).coerceAtMost(layer.cols - 1)
            val r0 = (camY / 20).coerceAtLeast(0)
            val r1 = ((camY + Level0World.VIEW_H) / 20).coerceAtMost(layer.rows - 1)
            for (cy in r0..r1) {
                for (cx in c0..c1) {
                    val cell = layer.cell(cx, cy)
                    if (cell < 0 || cell == 255) continue
                    drawTileCell(pack, cell, cx * 20 - camX, cy * 20 - camY,
                                 layer.flag(cx, cy))
                }
            }
        }

        for (e in world.npcs) drawEntity(e, camX, camY)
        drawEntity(world.player, camX, camY)

        batch.end()
        fbo.end()

        // letterbox blit (same as PixelRenderer)
        val sw = Gdx.graphics.width; val sh = Gdx.graphics.height
        var sc = minOf(sw / Level0World.VIEW_W, sh / Level0World.VIEW_H)
        if (sc < 1) sc = 1
        val dw = Level0World.VIEW_W * sc; val dh = Level0World.VIEW_H * sc
        scale = sc; offsetX = (sw - dw) / 2; offsetY = (sh - dh) / 2
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, sw.toFloat(), sh.toFloat())
        batch.begin()
        batch.draw(fbo.colorBufferTexture,
                   offsetX.toFloat(), offsetY.toFloat(), dw.toFloat(), dh.toFloat(),
                   0, 0, Level0World.VIEW_W, Level0World.VIEW_H, false, true)
        batch.end()
    }

    /** `b.java:907` 8-arg path: draw the frame's module at anchor - offset. */
    private fun drawEntity(e: Entity, camX: Int, camY: Int) {
        val clip = e.clip ?: return
        if (e.S < 0 || e.S >= clip.animCount()) return
        val pack = clipPackOf(clip) ?: return
        val fd = clip.frameDraw(e.S, e.T, e.drawFlags())
        // screenX = ak - camX - dx ; screenY = al - camY - dy (b.java:907
        // i3-i10 / i4-i11); the object resolves through the composite path.
        drawObject(pack, fd.module, e.ak - camX - fd.dx, e.al - camY - fd.dy, fd.transform)
    }

    private fun clipPackOf(clip: Clip): Int? =
        clips.entries.firstOrNull { it.value === clip }?.key

    fun dispose() {
        fbo.dispose(); batch.dispose()
        clipModules.values.forEach { arr ->
            arr.filterNotNull().forEach { it.texture.dispose() }
        }
    }
}
