package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.Entity
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
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
    private lateinit var white: Texture
    private lateinit var font: BitmapFont

    // (module index, palette slot) -> TextureRegion, per pack id.
    // palette-00 is canonical (clip.moduleNames); palette-NN siblings are
    // resolved lazily by filename substitution (b.aH slot, b.java:2436).
    private val clipModules = HashMap<Int, Array<TextureRegion?>>()
    private val clipPalettes = HashMap<Int, HashMap<Int, TextureRegion>>()
    private val clipDims = HashMap<Int, Array<Pair<Int, Int>>>()
    private var clips: Map<Int, Clip> = emptyMap()

    fun create(world: Level0World) {
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, Level0World.VIEW_W, Level0World.VIEW_H, false)
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        batch = SpriteBatch()
        font = BitmapFont()
        Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(1f, 1f, 1f, 1f); fill()
            white = Texture(this); dispose()
        }
        clips = world.clips
        for ((packId, clip) in clips) {
            val regs = arrayOfNulls<TextureRegion>(clip.moduleNames.size)
            val dims = Array(clip.moduleNames.size) { clip.moduleWidth(it) to clip.moduleHeight(it) }
            // positive keys are clip packs (clips/clipN/), negative keys are
            // negated tileset ids (level0/tilesetN/) — compute, don't map:
            // every new clip slice used to crash here when the when() lagged.
            val base = if (packId >= 0) "clips/clip$packId/modules"
                       else "level0/tileset-${-packId}/modules"
            for (i in clip.moduleNames.indices) {
                // aU==2 non-pixel modules are empty-name slots in the blob.
                if (clip.moduleNames[i].isEmpty()) continue
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
    private fun moduleRegion(pack: Int, m: Int, palette: Int): TextureRegion? {
        val base = clipModules[pack]?.getOrNull(m) ?: return null
        if (palette <= 0) return base
        val pal = clipPalettes.getOrPut(pack) { HashMap() }
        return pal.getOrPut(m or (palette shl 16)) {
            val clip = clips[-pack] ?: return base   // tilesets use neg keys
            val dir = when (pack) {
                else -> "level0/tileset-${-pack}/modules"    // negated keys
            }
            val variant = clip.moduleNames[m]
                .replace("-palette-00-", "-palette-%02d-".format(palette))
            val fh = Gdx.files.internal("$dir/$variant")
            if (!fh.exists()) return@getOrPut base
            val t = Texture(fh)
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            TextureRegion(t)
        }
    }

    private fun drawModule(pack: Int, m: Int, x: Int, y: Int, transform: Int, palette: Int = 0) {
        val src = moduleRegion(pack, m, palette) ?: return
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
    private fun drawObject(pack: Int, obj: Int, x: Int, y: Int, flags: Int, depth: Int = 0, palette: Int = 0) {
        val clip = clips[-pack] ?: return        // tilesets use neg keys
        if (obj < 0 || obj >= clip.objPlaceStart.size || depth > 4) return
        val count = clip.objPlaceCount[obj]
        if (count == 0) {
            drawModule(pack, obj, x, y, flags, palette)
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
                drawModule(pack, m, x + dx, y + dy, tf and 15, palette)
            } else {
                // aq bit 0x10: target is another composite object, not a module
                drawObject(pack, m, x + dx, y + dy, tf and 15, depth + 1, palette)
            }
        }
    }

    /** `b.java:915` composite-sprite draw for one tile cell. */
    private fun drawTileCell(pack: Int, cell: Int, x: Int, y: Int, dX: Int) {
        if (cell == 255) return
        val clip = clips[-pack] ?: return        // tilesets use neg keys
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

        // HUD sync meter — k.java:5388 (proven): j.a clip (43,6,x1*11/15,20)
        // reveals z[12] bar art; sprite undecoded → filled rect (inferred
        // color) + thin track. FBO is y-up: y6-top bar → VIEW_H-6-20.
        val mw = (world.player.x1 * 11) / 15
        batch.setColor(0.1f, 0.1f, 0.1f, 0.8f)
        batch.draw(white, 43f, (Level0World.VIEW_H - 26).toFloat(), 66f, 20f)
        batch.setColor(0.9f, 0.85f, 0.4f, 1f)
        batch.draw(white, 43f, (Level0World.VIEW_H - 26).toFloat(),
                   mw.toFloat(), 20f)
        batch.setColor(1f, 1f, 1f, 1f)

        // k.l(21) modal dialog — the original suspends the sim behind a
        // drawn dialog box (i.java:20190-20240, j.d text panel); port draws
        // a bottom dialog box so the freeze is visible (panel `inferred`,
        // text glyphs unported). dismiss = press edge.
        if (world.dialogModal) {
            batch.setColor(0f, 0f, 0f, 0.8f)
            batch.draw(white, 10f, 6f, 380f, 60f)
            batch.setColor(0.85f, 0.8f, 0.5f, 1f)
            batch.draw(white, 12f, 8f, 376f, 2f)
            batch.draw(white, 12f, 62f, 376f, 2f)
            batch.setColor(0.85f, 0.8f, 0.5f, 1f)
            batch.draw(white, 190f, 20f, 20f, 20f)      // ▼ hint marker
            batch.setColor(1f, 1f, 1f, 1f)
        }

        // menu screens — k.L462 (k.java:1775, proven): frozen world +
        // `b(93,67,214,true,true)` panel, `eB` title, `eC` prompt, eA[bv]
        // rows with `bw` cursor. Glyph stand-in: BitmapFont (inferred —
        // the original's `bW`/`y` bitmap-font clips are unported). Layout
        // `inferred` (rows ~36px from y≈130 in world y-down space).
        if (world.menuVisible) {
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 93f, 40f, 214f, 150f)
            batch.setColor(0.8f, 0.15f, 0.15f, 1f)
            batch.draw(white, 95f, 42f, 210f, 2f)
            batch.draw(white, 95f, 186f, 210f, 2f)
            batch.setColor(1f, 1f, 1f, 1f)
            font.setColor(1f, 1f, 1f, 1f)
            world.menuTitle()?.let { t ->
                font.draw(batch, t, 200f - t.length * 3.5f,
                          Level0World.VIEW_H - 76f)
            }
            world.menuPrompt()?.let { t ->
                font.setColor(0.9f, 0.85f, 0.5f, 1f)
                font.draw(batch, t, 200f - t.length * 3.5f,
                          Level0World.VIEW_H - 98f)
                font.setColor(1f, 1f, 1f, 1f)
            }
            for ((i, row) in world.menuRows().withIndex()) {
                val (text, sel) = row
                if (sel) {
                    batch.setColor(0.85f, 0.8f, 0.5f, 0.35f)
                    batch.draw(white, 100f,
                               (Level0World.VIEW_H - 130 - i * 36 - 14).toFloat(),
                               200f, 20f)
                    batch.setColor(1f, 1f, 1f, 1f)
                }
                font.draw(batch, text, 200f - text.length * 3.5f,
                          Level0World.VIEW_H - 130 - i * 36f)
            }
        }

        // stats screen — k.L466 (k.java:1788, proven): `d(0,bx)` text +
        // `j.g%6` "TOUCH THE SCREEN" blink at (200,173).
        if (world.statsVisible) {
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 93f, 40f, 214f, 150f)
            batch.setColor(1f, 1f, 1f, 1f)
            font.setColor(0.9f, 0.85f, 0.5f, 1f)
            world.statsText()?.let { t ->
                font.draw(batch, t, 200f - t.length * 3.5f,
                          Level0World.VIEW_H - 90f)
            }
            font.setColor(1f, 1f, 1f, 1f)
            if (world.jG % 6L < 3L) {
                val t = world.d0(9) ?: "TOUCH THE SCREEN"
                font.draw(batch, t, 200f - t.length * 3.5f,
                          Level0World.VIEW_H - 173f)
            }
        }

        // M() win-stats screen (k.java:3280-3445, proven positions):
        // `a(i2,d(0,60))` title ribbon + `bW.a` rows — labels x=95
        // (align 20), values right-aligned x=305 (align 24), rows
        // 55+20i; total row y=175; `a(d(0,16),str2)` bottom hint.
        // Ribbon/panel sprites (A[3], fJ/fK corners) are unported —
        // procedural stand-ins, `inferred` styling.
        if (world.jC == 15) {
            val H = Level0World.VIEW_H
            batch.setColor(0f, 0f, 0f, 0.8f)
            batch.draw(white, 0f, 0f, 400f, 240f)
            // title ribbon — `a(i2,str)`: color box + centered text
            // (A[3] clip sprites 1/2 unported → gold bar stand-in)
            val ty = world.statsTitleY
            batch.setColor(0.8f, 0.15f, 0.15f, 0.9f)
            batch.draw(white, 87f, (H - ty - 12).toFloat(), 226f, 20f)
            batch.setColor(1f, 1f, 1f, 1f)
            world.d0(60)?.let { t ->
                font.draw(batch, t, 200f - t.length * 3.5f,
                          (H - ty).toFloat())
            }
            // row labels + right-aligned values
            font.setColor(1f, 1f, 1f, 1f)
            for (i3 in 0..4) {
                val v = world.statsRowText[i3]
                if (v.isEmpty()) continue
                world.d0(38 + i3)?.let { t ->
                    font.draw(batch, t, 95f, (H - 55 - i3 * 20).toFloat())
                }
                font.draw(batch, v, 305f - v.length * 7f,
                          (H - 55 - i3 * 20).toFloat())
            }
            // total row (y=175, one-shot after jG>10)
            if (world.statsScoreVisible) {
                world.d0(43)?.let { t ->
                    font.draw(batch, t, 95f, (H - 175).toFloat())
                }
                val t = world.fmtJ(world.statsScore)
                font.draw(batch, t, 305f - t.length * 7f,
                          (H - 175).toFloat())
            }
            // `a(d(0,16),str2)` hint — NEXT ▸ typewriter (inferred box)
            if (world.statsTypeNext >= 0) {
                val t = (world.d0(16) ?: "NEXT") + " " +
                        world.typewriterText
                font.setColor(0.9f, 0.85f, 0.5f, 1f)
                font.draw(batch, t, 390f - t.length * 7f,
                          (H - 222).toFloat())
                font.setColor(1f, 1f, 1f, 1f)
            }
        }

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
        drawObject(pack, fd.module, e.ak - camX - fd.dx, e.al - camY - fd.dy, fd.transform,
                   palette = e.palette)
    }

    private fun clipPackOf(clip: Clip): Int? =
        clips.entries.firstOrNull { it.value === clip }?.key

    fun dispose() {
        fbo.dispose(); batch.dispose()
        if (::font.isInitialized) font.dispose()
        if (::white.isInitialized) white.dispose()
        clipModules.values.forEach { arr ->
            arr.filterNotNull().forEach { it.texture.dispose() }
        }
    }
}
