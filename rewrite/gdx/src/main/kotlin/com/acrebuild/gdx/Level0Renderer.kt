package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.Entity
import com.acrebuild.core.ScriptPrompt
import com.acrebuild.core.FontClip
import com.acrebuild.core.UiAnimObject
import com.acrebuild.core.Trig
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
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
    private lateinit var white: TextureRegion
    private lateinit var font: BitmapFont
    // All module pixmaps live in one atlas: SpriteBatch draws of regions on
    // the same page cost no texture switch; before this, every module was a
    // standalone Texture and each draw flushed + re-uploaded the batch (~0.2
    // fps on a software GL emulator). `white` is a 1x1 atlas pixel so HUD
    // fills stay on the same texture.
    private lateinit var packer: PixmapPacker
    private lateinit var atlas: TextureAtlas
    private val drawScratch = TextureRegion()
    /** `bW`/`y` = pack-1 entries 1/3 (k.java:3966-3967) — the game's two
     *  bitmap fonts. Glyph ids index each clip's OBJECT space (shared
     *  charmap `j.f(2)` = pack-1 entry-2). `l()` → palette variant. */
    private lateinit var fontW: com.acrebuild.core.FontClip
    private lateinit var fontY: com.acrebuild.core.FontClip

    // (module index, palette slot) -> TextureRegion, per pack id.
    // palette-00 is canonical (clip.moduleNames); palette-NN siblings are
    // resolved lazily by filename substitution (b.aH slot, b.java:2436).
    private val clipModules = HashMap<Int, Array<TextureRegion?>>()
    private val clipPalettes = HashMap<Int, HashMap<Int, TextureRegion>>()
    private val clipCanonical = HashMap<Int, Int>()
    private val paletteTextures = HashSet<Texture>()
    private val clipDims = HashMap<Int, Array<Pair<Int, Int>>>()
    private var clips: Map<Int, Clip> = emptyMap()

    fun create(world: Level0World) {
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, Level0World.VIEW_W, Level0World.VIEW_H, false)
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        batch = SpriteBatch()
        font = BitmapFont()
        clips = world.clips
        val charmap = com.acrebuild.core.FontClip.loadCharmap(
            Gdx.files.internal("fonts/charmap.bin").readBytes())
        fontW = com.acrebuild.core.FontClip(clips[91]!!, charmap, 4)
        fontY = com.acrebuild.core.FontClip(clips[92]!!, charmap, 4)
        packer = PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 2, true)
        Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(1f, 1f, 1f, 1f); fill()
            packer.pack("white", this); dispose()
        }
        // dedupe: aliased pack ids (clips[12]===clips[94]) share the same
        // Clip object — pack its pixmaps once under the first key.
        val canonical = HashMap<Clip, Int>()
        for ((packId, clip) in clips) {
            val dims = Array(clip.moduleNames.size) { clip.moduleWidth(it) to clip.moduleHeight(it) }
            // positive keys are clip packs (clips/clipN/), negative keys are
            // negated tileset ids (level0/tilesetN/) — compute, don't map:
            // every new clip slice used to crash here when the when() lagged.
            val base = if (packId >= 0) "clips/clip$packId/modules"
                       else "level0/tileset-${-packId}/modules"
            val canon = canonical.getOrPut(clip) { packId }
            if (canon == packId) {
                for (i in clip.moduleNames.indices) {
                    // aU==2 non-pixel modules are empty-name slots in the blob.
                    if (clip.moduleNames[i].isEmpty()) continue
                    val file = Gdx.files.internal("$base/${clip.moduleNames[i]}")
                    if (!file.exists()) continue
                    val pm = Pixmap(file)
                    packer.pack("$packId/$i", pm)
                    pm.dispose()
                }
            }
            clipCanonical[packId] = canon
            clipDims[packId] = dims
        }
        atlas = packer.generateTextureAtlas(Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest, false)
        white = atlas.findRegion("white")!!
        for ((packId, clip) in clips) {
            val regs = arrayOfNulls<TextureRegion>(clip.moduleNames.size)
            val canon = clipCanonical[packId]!!
            for (i in clip.moduleNames.indices) {
                regs[i] = atlas.findRegion("$canon/$i")
            }
            clipModules[packId] = regs
        }
    }

    /** `y.a(cd, str, x, y, align)` / `bW.a(...)` — real glyph text:
     *  glyph ids index the font clip's OBJECT space (composite draw
     *  via the placement pool). `pack` 91 = bW (title), 92 = y (body). */
    private fun drawText(str: String, x: Int, y: Int, align: Int,
                         palette: Int = -1, pack: Int = 92) {
        val f = if (pack == 91) fontW else fontY
        if (palette >= 0) f.l(palette)
        f.draw(str, x, y, align) { g, gx, gy, pal ->
            drawObject(pack, g, gx, gy, 0, 0, pal)
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
        val base = (clipModules[pack] ?: clipModules[-pack])
            ?.getOrNull(m) ?: return null
        if (palette <= 0) return base
        val pal = clipPalettes.getOrPut(pack) { HashMap() }
        return pal.getOrPut(m or (palette shl 16)) {
            // clips map mixes positive entity keys + negative tileset
            // keys — accept both conventions at the lookup.
            val clip = clips[pack] ?: clips[-pack] ?: return base
            val dir = if (pack >= 0) "clips/clip$pack/modules"
                      else "level0/tileset-${-pack}/modules"
            val variant = clip.moduleNames[m]
                .replace("-palette-00-", "-palette-%02d-".format(palette))
            val fh = Gdx.files.internal("$dir/$variant")
            if (!fh.exists()) return@getOrPut base
            // palette variants are rare: pack them standalone so the hot
            // atlas path is never invalidated by a late variant upload.
            val t = Texture(fh)
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            paletteTextures += t
            TextureRegion(t)
        }
    }

    private fun drawModule(pack: Int, m: Int, x: Int, y: Int, transform: Int, palette: Int = 0) {
        val src = moduleRegion(pack, m, palette) ?: return
        val (w, h) = (clipDims[pack] ?: clipDims[-pack])!![m]
        val t = transform and 7
        // shared scratch: setRegion resets the uv box to `src`, flips then
        // mutate only this instance — draw() samples the values immediately.
        val region = drawScratch
        region.setRegion(src)
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
        val clip = clips[pack] ?: clips[-pack] ?: return
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

    /** `b.a(g, anim, frame, x, y, flags, 0, 0)` (b.java:907-913): frame →
     *  `av` OBJECT index → the 6-arg object draw (:915) — composite
     *  placements via drawObject (falls back to drawModule when the
     *  object has no placements, so single-module frames are unchanged). */
    private fun drawFrame(pack: Int, anim: Int, frame: Int, x: Int, y: Int,
                          flags: Int, palette: Int = 0) {
        val clip = clips[pack] ?: clips[-pack] ?: return
        if (anim < 0 || anim >= clip.animCount() ||
            frame < 0 || frame >= clip.frameCount(anim)) return
        val fd = clip.frameDraw(anim, frame, flags)
        drawObject(pack, fd.module and 0x3FFF, x - fd.dx, y - fd.dy,
                   fd.transform, 0, palette)
    }

    /** `a.b(j.f)` + `a.c()` (a.java:99-114) — one script-prompt card:
     *  tick the anim by the frame ms, then draw `d.a(g, e, f, a, b, c,
     *  0,0)` — anim e frame f at (a,b), flags c. Palette slot `k` stays
     *  -1 in every op we ported (no producer), so palette 0. */
    private fun drawPrompt(pr: ScriptPrompt, ms: Int) {
        pr.anim.tick(ms)
        drawFrame(pr.clipIdx, pr.anim.e, pr.anim.currentFrame,
                  pr.anim.a, pr.anim.b, pr.anim.c)
    }

    // -- b(x,y,w,z2,z3) menu panel (k.java:5903-6150, proven) --------------
    private var menuFj: UiAnimObject? = null          // k.fJ (a.java inst)
    private var menuFk: UiAnimObject? = null          // k.fK
    private var nDl: UiAnimObject? = null             // k.dl — N() icon
    private var pauseFl: UiAnimObject? = null         // k.fL — pause icon
    private var edgeCQ = -1                           // k.cQ — corner width
    private var edgeCR = -1                           // k.cR — tile width
    private var nTipDj = 0                            // k.dj — tip type pos
    private var nTipDk = 0                            // k.dk — tip hold

    /** `eW[]` (k.java:299, proven) — per-mission tip index for `N()`. */
    private val tipEW = intArrayOf(2, 2, 1, 1, 2, 0, 3, 2, 2)

    /** `fP[]` (k.java:344, proven) — mission poster-boundary table for
     *  `ag()`'s `A[4]` anim pick (`i+4`). */
    private val posterFP = intArrayOf(0, 2, 5, 7)
    private val ROPE_COL = -3584205                    // k.b rope-line (k.java:2961)
    private val BAR_DEAD_S = intArrayOf(24, 21, 0, 139, 133, 134, 145, 135, 106, 107)
    private var menuEz = 0                            // k.ez fit-scroll

    /** `i(int,int)` (k.java:414-429, proven): A[4] anim-12 top strip
     *  + `j.h(MIN_VALUE); j.d` 400×68 half-dark fill + u∈{8,9,10}
     *  `y.a(d(0,9),200,220,3)` blink hint (`j.g%10<5`). */
    private fun dialogPanel(world: Level0World, i: Int, bP: Int) {
        drawFrame(98, 12, 0, 0, bP, 0)                // A[4].a(cd,12,0,0,i2)
        fillAr(0, bP, 400, 68, Int.MIN_VALUE)         // j.h(MIN_VALUE);j.d
        // blink tail (k.java:418-429): u8 never blinks; every other u
        // blinks `d(0,9)` while `j.g%10 < 5`.
        if (world.dlgU != 8 && world.jG % 10L < 5L) {
            drawText(world.d0(9) ?: "", 200, 220, 3, pack = 92)
        }
    }

    /** `a(bVar,i,str,x,y,w,align,limit)` (k.java:4114-4128, proven):
     *  wrap `str` at `w`, draw ≤8 lines from `8*(cY-1)`, `bT` char cap.
     *  The `bL`-EZIO rename arm (:4121-4124) — bL==0 on level 0. */
    private fun dialogText(world: Level0World, str: String, x: Int, y: Int,
                           w: Int, align: Int, limit: Int) {
        if (str.isEmpty()) return
        val u = fontY.wrap(str, w)
        fontY.l(0)
        fontY.drawWrapped(str, u, x, y, 8 * (world.kCY - 1), 8, align, limit)
        { g, gx, gy, pal -> drawObject(92, g, gx, gy, 0, 0, pal) }
    }

    /** `N()` load screen (k.java:3472-3513, proven positions) —
     *  **unreachable-labeled**: only the menu-flow `l(9)` picks it
     *  (k.java:1303,3934,6273); our flow boots straight to jC8 and
     *  fail→retry restores `j.c` without re-showing it.
     *  Black fill; lazy A[5] → `dl` UiAnimObject(80,-40) arm(0,-1);
     *  `j.g>=165` → dm=165 + `dl.a(dl.a()-3)` freeze-frame + `d(0,9)`
     *  blink; else `dm=j.g` + `bW.l(0)` `d(0,24)`; bar `j.a(dm<<?/165
     *  *300)` at (50,205) color 7644855; `j.g>1` → tip typewriter
     *  `a(bW,d(0,51+eW[aj]))` + `d(1,0)` mission title wrap. */
    private fun loadScreen(world: Level0World) {
        val dl = nDl ?: UiAnimObject(clips[99], 80, -40)
            .also { it.arm(0, -1); nDl = it }
        fillAr(0, 0, 400, 240, -16777216)            // setColor(0);j.b
        val dm: Int
        if (world.jG >= 165L) {
            dm = 165
            dl.tick(62)
            dl.seek(dl.len() - 3)                  // dl.a(dl.a()-3)
            drawFrame(99, dl.e, dl.currentFrame, dl.a, dl.b, dl.c)
            if (world.jG % 10L < 5L) {
                drawText(world.d0(9) ?: "", 200, 220, 17, pack = 91)
            }
        } else {
            dl.tick(62)
            drawFrame(99, dl.e, dl.currentFrame, dl.a, dl.b, dl.c)
            dm = world.jG.toInt()
            fontW.l(0)
            drawText(world.d0(24) ?: "", 395, 230, 40, pack = 91)
        }
        // j.b(j.a,50,205, j.a(((dm<<8)/165)*300), 10) — fixed-round w
        val w = ((((dm shl 8) / 165) * 300) + 128) shr 8
        fillAr(50, 205, w, 10, 7644855)
        if (world.jG > 1L) {
            tipTypewriter(world.d0(51 + tipEW[world.kAj]) ?: "")
            // y.a(str,null) → b.d measured width; x = max(20,(400-b.d)>>1)
            val title = world.levelString(1, 0) ?: return
            val x = ((400 - fontY.measure(title)[0]) shr 1).coerceAtLeast(20)
            // a(y,2,str,x,135,360,240,0,20) — 9-arg drops i5/i6 → 20,-1
            dialogText(world, title, x, 135, 360, 20, -1)
        }
    }

    /** `i.a(int,int,int,int,boolean)` (i.java:20129-20166, proven) —
     *  the ax35 eagle-view minimap composite, drawn at the `k.aQ`
     *  blit site (198-w, 5) as a clipped direct draw (our compositor
     *  skips the offscreen copy — same visible result). Parts:
     *  eu stamp grid (`k.a(g,x,y,cell)` :4504, `bt` = eu cols);
     *  `k.a()` (:4417) ep front-layer rect + `k.b()` (:4453) er
     *  bottom-layer rect at (cx*20-x, cy*20-y+i7); enemy blips —
     *  ax∈{11,73,35,79} `(P&128)==0` gated by `i.a(i,i2,i+i3,i2+i4,Y)`
     *  rect-overlap (:520); `drawRect(0,0,h-1,w-1)` border — the
     *  verbatim h/w swap. `z2` (`Z[4]==0`) is a dead param in the
     *  composite body (verbatim). `i7 = -(h-240)` bottom-anchored
     *  shift, verbatim. */
    private fun minimap(world: Level0World, r: IntArray) {
        val x = r[0]; val y = r[1]; val w = r[2]; val h = r[3]
        val ox = 198 - w; val oy = 5
        val i7 = -(h - 240)
        clipScissor(ox, oy, w, h)
        fillAr(ox, oy, w, h, -16777216)
        // eu stamp grid (:20136-20141): (w/20+1)×(h/20+1) cells
        val eu = world.level.layers.firstOrNull { it.id == 2 }
        if (eu != null) {
            for (i8 in 0 until h / 20 + 1) {
                for (i9 in 0 until w / 20 + 1) {
                    if (i9 >= eu.cols || i8 >= eu.rows) continue
                    val cell = eu.cell(i9, i8)
                    if (cell < 0 || cell == 255) continue
                    drawTileCell(eu.tilesetClip, cell, ox + i9 * 20,
                                 oy + i8 * 20 + i7, eu.flag(i9, i8))
                }
            }
        }
        minimapLayer(world, 1, x, y, w, h, ox, oy, i7)  // k.a() ep rect
        minimapLayer(world, 3, x, y, w, h, ox, oy, i7)  // k.b() er rect
        // enemy blips (:20145-20158): ax∈{11,73,35,79} in-rect
        for (e in world.npcs) {
            if (e == null) continue
            if (e.ax != 11 && e.ax != 73 && e.ax != 35 && e.ax != 79) continue
            if ((e.P and 128) != 0) continue
            if (!minimapOverlap(x, y, x + w, y + h, e.Y)) continue
            val pack = e.clip?.let { clipPackOf(it) } ?: continue
            val bx = ox + e.ak - x; val by = oy + e.al - y + i7
            val pal = if (e.ax == 79) e.Z.getOrElse(1) { 0 } else e.palette
            if (e.U >= 0) drawFrame(pack, e.S, e.T, bx, by, e.P and 7, pal)
            else if (e.S >= 0) drawObject(pack, e.S, bx, by, e.P and 7, 0, pal)
            else if (e.T >= 0) drawObject(pack, e.T, bx, by, e.P and 7, 0, pal)
        }
        clipScissor(0, 0, 400, 240)
        // graphics.drawRect(0,0,i4-1,i3-1) — verbatim h/w arg swap
        outlineAr(ox, oy, h - 1, w - 1, -256)
    }

    /** `k.a()`/`k.b()` rect draw (k.java:4417-4449/4453-4498, proven):
     *  world-rect (x,y,w,h) → cell range (x/20..(x+w-1)/20) ×
     *  (y/20..(y+h-1)/20), each non-empty cell stamped at
     *  (cx*20-x, cy*20-y+i7). The `i2<0 → i2-=20` negative-round
     *  quirk is verbatim. bh3's `dL` arm is flying-only (dead here). */
    private fun minimapLayer(world: Level0World, id: Int, x: Int, y: Int,
                             w: Int, h: Int, ox: Int, oy: Int, i7: Int) {
        val layer = world.level.layers.firstOrNull { it.id == id } ?: return
        var yy = y
        if (yy < 0) yy -= 20                                  // i2<0 quirk
        val c0 = x / 20; val r0 = yy / 20
        val c1 = (x + w - 1) / 20; val r1 = (y + h - 1) / 20
        var dx = c0 * 20 - x
        for (cx in c0..c1) {
            var dy = r0 * 20 - y
            for (cy in r0..r1) {
                val cell = layer.cell(cx, cy)
                if (cell >= 0 && cell != 255)
                    drawTileCell(layer.tilesetClip, cell, ox + dx,
                                 oy + dy + i7, layer.flag(cx, cy))
                dy += 20
            }
            dx += 20
        }
    }

    /** `i.a(int,int,int,int,int[])` (i.java:520-537, proven): minimap
     *  blip's rect-vs-Y-bounds overlap — disjoint → false; degenerate
     *  query → false; `Y[0]==Y[2]` → `Y[1] != Y[3]`; else true. */
    private fun minimapOverlap(x0: Int, y0: Int, x1: Int, y1: Int,
                               r: IntArray): Boolean {
        if (x0 > r[2] || x1 < r[0] || y0 > r[3] || y1 < r[1]) return false
        if (x0 == x1 && y0 == y1) return false
        if (r[0] == r[2]) return r[1] != r[3]
        return true
    }

    /** `a(bVar,str)` tip typewriter (k.java:3450-3469, proven): types
     *  `dj` chars; inserts `\\2`/palette-2 around the newest char;
     *  after full string `dk=15` frame hold then `dj=0` restart.
     *  `bVar.f=true` bold — our drawText maps `\\0`.. codes via the
     *  font's own escape pass. */
    private fun tipTypewriter(str: String) {
        if (str.isEmpty()) return
        if (nTipDk <= 0) {
            if (nTipDj < str.length) {
                // verbatim: \2<new char>\0 bracket inside the FULL string
                // (untyped tail still draws — moving-highlight cursor)
                val shown = "\\0" + str.substring(0, nTipDj) +
                            "\\2" + str[nTipDj] + "\\0" +
                            str.substring(nTipDj + 1)
                drawText(shown, 200, 40, 17, pack = 91)
                nTipDj++
                return
            }
            nTipDj = 0
            nTipDk = 15
        }
        nTipDk--
        drawText("\\0" + str, 200, 40, 17, pack = 91)
    }

    /** jc18 title-screen arm (k.java:1146-1157, proven) —
     *  **unreachable-labeled** (boot-flow only): `A[1].a(cd,1,0,0,0)` +
     *  `A[0].a(cd,0,0,0,0)` + `A[1].a(cd,2,0,0,0)`; `!cS` → `d(0,9)`
     *  blink at (200,205) `j.g%10>5`. */
    /** `R()` draw side (k.java:4004-4099, proven): every case fills
     *  black first (`cd.setColor(0); j.b`), then `bX` (clip 0 = pack-3
     *  entry-0 logo bank) anim 0 for cu≤2, anim 1 + `d(0,63)` for
     *  cu==3, `d(0,65)` copyright text for cu≥4. `b.a` ticks the logo
     *  one frame per call — `(jG-kDu) % frameCount` reproduces it. */
    private fun bootScreen(world: Level0World) {
        fillAr(0, 0, 400, 240, -16777216)              // 0xFF000000
        when (world.kCu) {
            in 0..2 -> drawFrame(0, 0, bootFrame(world, 0), 200, 120, 0)
            3 -> {
                drawFrame(0, 1, bootFrame(world, 1), 200, 120, 0)
                world.d0(63)?.let {
                    dialogText(world, it, 10, 140, 380, 3, Int.MAX_VALUE) }
            }
            else -> world.d0(65)?.let {
                dialogText(world, it, 10, 120, 380, 3, Int.MAX_VALUE) }
        }
    }

    /** `b.a` per-call anim tick → frame = elapsed ticks mod count. */
    private fun bootFrame(world: Level0World, anim: Int): Int {
        val clip = clips[0] ?: return 0
        val n = clip.frameCount(anim)
        return if (n <= 0) 0 else ((world.jG - world.kDu) % n).toInt()
    }

    /** jc20 overlay (k.java:1280-1298, proven): `z[39]` anim-1 icon at
     *  (eY,80) for ALL `cu>=2`; text at eZ inside the (85,120) clip for
     *  `cu<=4`; cu5 draws the scroll panel's text at `fd` inside the
     *  same clip; `z[39]` anim-10 spinner at (300,80) for `cu>=4`. */
    private fun storyScreen(world: Level0World) {
        if (world.kCu < 2) return
        val icon = clips[39]
        if (icon != null) {
            val n = icon.frameCount(1)
            drawFrame(39, 1, if (n <= 0) 0 else (world.jG % n).toInt(),
                      0, world.kEY, 80)
        }
        clipScissor(0, 85, 400, 120)
        if (world.kCu <= 4) {
            // :1290-1296 — `y.a(str,null)` measure, tall text slides
            // eZ up (`eZ = 85-(b.e-120)`), then draws at eZ; the write-
            // back is verbatim so `fd = eZ` at cu4→5 inherits it.
            val h = world.footerFont?.linesHeight(
                world.storyText().count { it == '\n' } + 1) ?: 0
            if (h > 120) world.kEz = 85 - (h - 120)
            drawText(world.storyText(), 5, world.kEz, 0)
        } else {
            drawText(world.storyText(), 5, world.kFd, 0)
        }
        clipScissor(0, 0, 400, 240)
        if (icon != null && world.kCu >= 4) {
            val n = icon.frameCount(10)
            drawFrame(39, 10, if (n <= 0) 0 else (world.jG % n).toInt(),
                      300, 80, 0)
        }
    }

    /** jc24 credits (k.java:1326-1386, proven): dz<120 letterbox iris;
     *  black field + `d(0,28)` title slide (dw 1-10 → y 220→120, 11-20
     *  hold); dw>=21 the wrapped credits at fd clipped (0,33,400,205);
     *  dw>=160 gray ramp `i11=dw-160` (alpha+rgb channels) white-out. */
    private fun creditsScreen(world: Level0World) {
        if (world.kDz < 120) {
            fillAr(0, 0, 400, world.kDz, -0x1000000)
            fillAr(0, 240 - world.kDz, 400, world.kDz, -0x1000000)
            return
        }
        val fade = if (world.kDw >= 160) {
            (world.kDw - 160).coerceAtMost(255)
        } else 0
        fillAr(0, 0, 400, 240,
               if (world.kDw >= 160)
                   (fade shl 24) or (fade shl 16) or (fade shl 8) or fade
               else -0x1000000)
        val title = world.d0(28) ?: ""
        if (world.kDw in 1..10) {
            drawText(title, 200, 120 + (100 * (10 - world.kDw)) / 10, 3, pack = 91)
        } else if (world.kDw in 11..20) {
            drawText(title, 200, 120, 3, pack = 91)
        }
        if (world.kDw >= 21) {
            clipScissor(0, 33, 400, 205)
            drawText(world.kDy ?: "", 200, world.kFd, 3, pack = 91)
            clipScissor(0, 0, 400, 240)
        }
    }

    private fun titleScreen(world: Level0World) {
        drawFrame(97, 1, 0, 0, 0, 0)                 // A[1] anim 1
        drawFrame(96, 0, 0, 0, 0, 0)                 // A[0] frame 0
        drawFrame(97, 2, 0, 0, 0, 0)                 // A[1] anim 2
        if (world.jG % 10L > 5L) {
            drawText(world.d0(9) ?: "", 200, 205, 3)
        }
    }

    /** `j.h(argb); j.d(g,x,y,w,h)` — translucent rect fill, verbatim ints. */
    private fun fillAr(x: Int, y: Int, w: Int, h: Int, argb: Int) {
        batch.setColor(((argb ushr 16) and 255) / 255f,
                       ((argb ushr 8) and 255) / 255f,
                       (argb and 255) / 255f,
                       ((argb ushr 24) and 255) / 255f)
        batch.draw(white, x.toFloat(),
                   (Level0World.VIEW_H - y - h).toFloat(),
                   w.toFloat(), h.toFloat())
        batch.setColor(1f, 1f, 1f, 1f)
    }

    /** `j.c(g,x,y,w,h)` (j.java, proven) — 1px hollow rect outline. */
    private fun outlineAr(x: Int, y: Int, w: Int, h: Int, argb: Int) {
        fillAr(x, y, w, 1, argb); fillAr(x, y + h - 1, w, 1, argb)
        fillAr(x, y + 1, 1, h - 2, argb); fillAr(x + w - 1, y + 1, 1, h - 2, argb)
    }

    /** `j.a(g,x0,y0,x1,y1)` (j.java drawLine, proven) — 1px line via a
     *  rotated `white` quad (screen-space y-down → rotate by −dy). */
    private fun drawLine(x0: Int, y0: Int, x1: Int, y1: Int, argb: Int) {
        val dx = x1 - x0; val dy = y1 - y0
        if (dx == 0 && dy == 0) { fillAr(x0, y0, 1, 1, argb); return }
        val len = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val rot = Math.toDegrees(Math.atan2(-dy.toDouble(), dx.toDouble())).toFloat()
        batch.setColor(((argb ushr 16) and 255) / 255f,
                       ((argb ushr 8) and 255) / 255f,
                       (argb and 255) / 255f,
                       ((argb ushr 24) and 255) / 255f)
        batch.draw(white, x0.toFloat(), (Level0World.VIEW_H - y0).toFloat(),
                   0f, 0.5f, len, 1f, 1f, 1f, rot)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    /** `g.a(i2,i3,i4,i5,i6)` (g.java:4723-4733, proven): the dotted
     *  rope — `k.h(len)/6` clip61 dots stepped 6px along `+angle` from
     *  `(i4,i5)`. (`i.bg` in the call is the Graphics, so anim0/frame0
     *  and no palette.) Caller passes screen coords. */
    private fun ropeDots(x2: Int, y2: Int, x4: Int, y4: Int, ang: Int) {
        if (clips[61] == null) return
        var seg = Trig.khypot(x4 - x2, y4 - y2) / 6
        var d = 0
        while (seg > 0) {
            seg--
            d += 6
            drawFrame(61, 0, 0,
                      x4 + ((d * Trig.sin(ang)) shr 8),
                      y4 - ((d * Trig.sin(Trig.N - ang)) shr 8), 0)
        }
    }

    /**
     * `k.b(z2)` per-entity overlay tail (k.java:2931-2990, proven):
     * gated by `C==null || !C.ab() || !C.cd[2] || P&512 || ax==0` (a
     *  claim-script in its marker-quiet state suppresses all bars), then
     *  `(P&32)==0 && (P&128)==0`:
     *  - `bl>0` && ax!={73,11} → mash/charge bar (white 42×5 @ ak,al-70,
     *    red fill `bl*40/10`);
     *  - ax11 alive-anim + `h()`, or ax73 `h()`, or ax17 `h()` && S!=69
     *    → HP bar (41×5 @ ak-20,al-80; green >half else red/white blink;
     *    fill `aB*40/i33`, halved to `*20` when Z0∈{1,2} or ax73;
     *    `i33 = i.bu[k.au]` soldiers / `i.bv[k.au]` civilians);
     *  - ax10 S==32 → rope-volume lines (W mid-height; two toward the
     *    claimed player when `aS.ac==self`, else the W span);
     *  - ax0 → claim `cd[9]` hints (`a(cg,ch)` link rope or `cf` dotted
     *    line) else `S∈272-277|293|298` → `aS.i()` own grapple rope.
     */
    private fun drawOverlayTail(w: Level0World, e: Entity, camX: Int, camY: Int) {
        val c = w.kC
        if (!(c == null || !c.claimAb() || !c.cd[2] ||
              (e.P and 512) != 0 || e.ax == 0)) return
        if ((e.P and 32) != 0 || (e.P and 128) != 0) return
        if (e.bl > 0) {
            if (e.ax != 73 && e.ax != 11) {
                outlineAr(e.ak - camX, e.al - camY - 70, 42, 5, -1)
                fillAr(e.ak - camX + 1, e.al - camY - 70,
                       (e.bl * 40) / 10, 4, -65536)
            }
        } else if ((e.ax == 11 && e.S !in BAR_DEAD_S && w.showsHpBar(e)) ||
                   (e.ax == 73 && w.showsHpBar(e)) ||
                   (e.ax == 17 && w.showsHpBar(e) && e.S != 69)) {
            val ex = e.ak - 20 - camX
            val ey = e.al - camY - 80
            outlineAr(ex, ey, 41, 5, -1)
            val i33 = if (e.ax == 17) Entity.NPC_HP_BV[w.kAu]
                      else Entity.WEAPON_DMG[w.kAu]
            val col = if (e.aB > (i33 shr 1)) 65280
                      else if ((w.jG and 1L) == 0L) -65536 else -1
            val fw = if (e.Z[0] == 2 || e.Z[0] == 1 || e.ax == 73)
                     (e.aB * 20) / i33 else (e.aB * 40) / i33
            fillAr(ex + 1, ey, fw, 4, col)
        } else if (e.ax == 10 && e.S == 32) {
            val my = (e.W[1] + e.W[3]) shr 1
            val p = w.player
            if (p.ac == e) {
                drawLine(e.W[0] - camX, my - camY, p.ak - camX, p.al - camY, ROPE_COL)
                drawLine(e.W[2] - camX, my - camY, p.ak - camX, p.al - camY, ROPE_COL)
                drawLine(e.W[0] - camX, my + 1 - camY, p.ak - camX, p.al + 1 - camY, ROPE_COL)
                drawLine(e.W[2] - camX, my + 1 - camY, p.ak - camX, p.al + 1 - camY, ROPE_COL)
            } else {
                drawLine(e.W[0] - camX, my - camY, e.W[2] - camX, my - camY, ROPE_COL)
                drawLine(e.W[0] - camX, my + 1 - camY, e.W[2] - camX, my + 1 - camY, ROPE_COL)
            }
        } else if (e.ax == 0) {
            if (c != null && c.claimAb() && c.cd[9]) {
                if (c.cg != null && c.ch != null) {
                    val g0 = c.cg!!; val g1 = c.ch!!
                    val dx = g1.X[0] - g0.X[0]; val dy = g1.X[1] - g0.X[1]
                    ropeDots(g0.X[0] - camX, g0.X[1] - camY,
                             g1.X[0] - camX, g1.X[1] - camY,
                             Trig.atan2(dy, -dx))
                } else if (c.cf != null) {
                    val cf = c.cf!!
                    ropeDots(cf[0] - camX, cf[1] - camY,
                             cf[2] - camX, cf[3] - camY, cf[4])
                }
            } else if (e.S in 272..277 || e.S == 293 || e.S == 298) {
                ropeDots(e.cJ - camX, e.cK - camY,
                         e.cH - camX, e.cI - camY, e.cy)
            }
        }
    }

    /** `j.a(g,x,y,w,h,true)` — GL scissor in FBO space (Y-flip). */
    private fun clipScissor(x: Int, y: Int, w: Int, h: Int) {
        batch.flush()
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glScissor(x, Level0World.VIEW_H - y - h, w, h)
    }
    private fun clipReset() {
        batch.flush()
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
    }

    /** `a(i,i2,i3,z2,z3)` (k.java:5872, proven) — ornamental band:
     *  cap frame at x, fill repeated to x+w, cap mirrored (flags=1).
     *  Frame pick: z3? (z2?16,17:14,15) : (z2?12,13:10,11). */
    private fun panelEdge(x: Int, y: Int, w: Int, z2: Boolean, z3: Boolean) {
        val clip = clips[93] ?: return
        val (cap, fill) = if (z3) {
            if (z2) 16 to 17 else 14 to 15
        } else if (z2) 12 to 13 else 10 to 11
        val fM = clip.moduleWidth(
            clip.frameDraw(cap, 0, 0).module and 0x3FFF)
        val fN = clip.moduleWidth(
            clip.frameDraw(fill, 0, 0).module and 0x3FFF)
        if (fM <= 0 || fN <= 0) return
        drawFrame(93, cap, 0, x, y, 0)
        var i7 = x + fM
        do {
            drawFrame(93, fill, 0, i7, y, 0)
            i7 += fN
        } while (i7 + fN < x + w)
        drawFrame(93, cap, 0, x + w, y, 1)     // flags=1 — mirrored end cap
    }

    /** `a(str, z2, i)` (k.java:6335, proven) — unpressed rows truncate
     *  with "..."; pressed rows scroll `ez` (-w .. textW); else `ez=0`. */
    private fun fitText(str0: String, zD: Boolean, w: Int): String {
        var str = str0
        var i2 = fontW.measure(str).first()
        if (!zD) {
            var length = str.length - 3
            while (length > 0 && i2 > w) {
                length--
                str = str.substring(0, length) + "..."
                i2 = fontW.measure(str).first()
            }
        } else if (i2 > w) {
            menuEz += 2
            if (menuEz > i2) menuEz = -w
        } else {
            menuEz = 0
        }
        return str
    }

    /** `a(i,i2,i3,z2)` (k.java:2242, proven) — the soft-key pill:
     *  frames `z2?41,42:43,44`, cached `cQ`/`cR` frame-0 module widths,
     *  cap + do-while fill + inner `i5` + mirrored cap. */
    private var pillCQ = -1
    private var pillCR = -1
    private fun softPill(x: Int, yBottom: Int, w: Int, pressed: Boolean) {
        val clip = clips[93] ?: return
        val i4 = if (pressed) 41 else 43
        val i5 = if (pressed) 42 else 44
        if (pillCQ == -1) {
            val fd = clip.frameDraw(i4, 0, 0)
            pillCQ = clip.moduleWidth(fd.module and 0x3FFF)
        }
        if (pillCR == -1) {
            val fd = clip.frameDraw(i5, 0, 0)
            pillCR = clip.moduleWidth(fd.module and 0x3FFF)
        }
        drawFrame(93, i4, 0, x, yBottom, 0)
        var i7 = x + pillCQ
        do {
            drawFrame(93, i5, 0, i7, yBottom, 0)
            i7 += pillCR
        } while (i7 + pillCR < x + w)
        drawFrame(93, i5, 0, (x + w) - pillCQ - pillCR, yBottom, 0)
        drawFrame(93, i4, 0, x + w, yBottom, 1)
    }

    /** `a(str,str2)` (k.java:2270, proven) — the footer soft-key strip:
     *  left pill at (5,235) skipped on jc21/8, `y` text when str==d(0,16);
     *  right pill at (395-cf,235), `y` text when str2==d(0,18) else the
     *  A[2] arrow `zD?29:24`. Hit-test lives in world.footerQ. */
    private fun footer(world: Level0World, left: String?, right: String?) {
        if (left != null && left != "" && world.jC != 21 && world.jC != 8) {
            val ce = world.footerLeftDim(left)
            world.kCe = ce
            softPill(5, 235, ce,
                     world.pointerMoveIn(-5, 198, ce + 20, 47))
            if (left == world.d0(16)) {
                fontY.l(0)
                drawText(left, 5 + (ce shr 1), 222, 3, pack = 92)
            }
        }
        if (!right.isNullOrEmpty()) {
            val cf = world.footerRightDim(right)
            world.kCf = cf
            val zD = world.pointerMoveIn(395 - cf - 10, 198, cf + 20, 47)
            softPill(395 - cf, 235, cf, zD)
            if (right == world.d0(18)) {
                fontY.l(0)
                drawText(right, 395 - (cf shr 1), 222, 3, pack = 92)
            } else {
                drawFrame(93, if (zD) 29 else 24, 0,
                          395 - (cf shr 1), 222, 0)
            }
        }
    }

    /** `b(i,i2,i3,z2,z3)` (k.java:5903-6150, proven) — the menu panel +
     *  row renderer. The `c()→bw` tap hook is the world's `menuRowAt`;
     *  the j.c==2 side soft-buttons are unported (jC==2 unreachable). */
    private fun menuPanel(world: Level0World, x: Int, y: Int, w: Int,
                          z2: Boolean, z3: Boolean) {
        val clipA2 = clips[93]
        if (menuFj == null && clipA2 != null) {
            menuFj = UiAnimObject(clipA2); menuFj!!.arm(18, -1)
        }
        if (menuFk == null && clipA2 != null) {
            menuFk = UiAnimObject(clipA2); menuFk!!.arm(21, 1)
        }
        val frameMs = (Gdx.graphics.deltaTime * 1000f).toInt()
        var i9 = y + 10
        val i10 = world.menuRowCount()
        val i11 = if (z3) 40 else 0
        if (z2) {
            fillAr(x, y, w, i10 * 33 + 20 + i11, -856756498)
            fillAr(x - 2, y - 2, 2, i10 * 33 + 24 + i11, -2013265920)
            fillAr(x + w, y - 2, 2, i10 * 33 + 24 + i11, -2013265920)
            fillAr(x, y - 2, 95, 2, -2013265920)
            fillAr(x, y + i10 * 33 + 20 + i11, 95, 2, -2013265920)
            fillAr(x + 108, y - 2, w - 108, 2, -2013265920)
            fillAr(x + 108, y + i10 * 33 + 20 + i11, w - 108, 2, -2013265920)
        }
        fillAr(x, y, w, 10, 805306368)
        if (z2) fillAr(x + 95, y - 2, 13, 2, -2013265920)
        if (z3) { fillAr(x, i9, w, 40, 805306368); i9 += 40 }
        val i12 = i9
        var i = x
        for (i13 in 0 until i10) {
            val i4 = world.menuI4(i13)
            val i5 = world.menuI5()
            if (i13 == 1 && world.jC == 2) i9 += 13
            val zD = world.pointerMoveIn(i, i9, w, i4)
            if (zD) {
                fillAr(i, i9, w, i4, 1879048192)
                panelEdge(i + ((w - i5) shr 1), i9 + (i4 shr 1), i5,
                          false, i13 == 0 && world.jC == 2)
                val icon = if (world.jC == 30) i13 + 5
                           else if (i13 == 0 && world.jC == 2) 9 else 5
                drawFrame(93, icon, 0, i + 40, i9 + (i4 shr 1), 0)
                fontW.l(0)
            } else {
                val fj = menuFj
                if (fj != null) {
                    if (i13 == 0 && world.jC == 2) {
                        if (fj.e != 19) fj.arm(19, -1)
                    } else if (fj.e != 18) fj.arm(18, -1)
                    fj.a = (i + w) - ((w - i5) shr 1); fj.b = i9
                    fj.tick(frameMs)
                }
                if (world.kFI > 0) {
                    fillAr(i, i9, w, i4, 1879048192)
                    clipScissor(0, i9 + ((i4 - world.kFI) shr 1),
                                400, world.kFI)
                    world.kFI += world.kFH; world.kFH += 8
                    if (world.kFI >= i4) world.kFI = 0
                }
                fj?.let { drawFrame(93, it.e, it.currentFrame, it.a, it.b, it.c) }
                clipReset()
                fillAr(i, i9, (w + i5) shr 1, i4, -16777216)
                panelEdge(i + ((w - i5) shr 1) - 2, i9 + (i4 shr 1),
                          i5 + 4, true, i13 == 0 && world.jC == 2)
                val icon = if (world.jC == 30) i13
                           else if (i13 == 0 && world.jC == 2) 4 else 0
                drawFrame(93, icon, 0, i + 40, i9 + (i4 shr 1), 0)
                val fk = menuFk
                if (fk != null) {
                    if (world.menuFkArm >= 0) {          // af() `fK.a(21,1)`
                        fk.arm(world.menuFkArm, 1); world.menuFkArm = -1
                    }
                    fk.tick(frameMs)
                    if (fk.stopped()) fk.arm(20, -1)
                    fk.a = i; fk.b = i9 + (i4 shr 1)
                    drawFrame(93, fk.e, fk.currentFrame, fk.a, fk.b, fk.c)
                    fontW.l(1)
                }
            }
            val (strD, pal) = world.menuRowText(i13)
            val strA = fitText(strD, zD, i5 - 50)
            val i15 = if (world.jC == 19) -3 else 0
            val i14 = world.menuI14(i, w)
            if (zD) {
                drawText(strA, i14, i9 + (i4 shr 1) + i15,
                         3, palette = pal, pack = 91)
            } else {
                world.menuRowSub(i13)?.let {
                    fontY.l(1)
                    drawText(it, i14 - menuEz,
                             i9 + (i4 shr 1) + 10 + i15, 3)
                }
                clipScissor(i14 - (i5 shr 1) + 25, i9, i5 - 50, 240)
                drawText(strA, i14 - menuEz, i9 + (i4 shr 1) + i15,
                         3, palette = pal, pack = 91)
                clipReset()
            }
            if ((world.kBv != 4 && world.jC != 14) || world.jC == 19) {
                var i16 = i10 / 2
                if (i10 % 2 == 0) i16--
                if (i13 == i16 && i13 < i10 - 1) {
                    fillAr(i, i9 + i4, w, 10, 805306368)
                    i = 206
                    i9 = i12 - (i4 + 3)
                    fillAr(206, i9 + i4 + 3 - 10, w, 10, 805306368)
                }
            }
            i9 += i4 + 3
        }
    }

    /** `b.java:915` composite-sprite draw for one tile cell. */
    private fun drawTileCell(pack: Int, cell: Int, x: Int, y: Int, dX: Int) {
        if (cell == 255) return
        val clip = clips[pack] ?: clips[-pack] ?: return
        if (cell >= clip.objPlaceStart.size) return
        // tile cells sit on a 20px grid: +20 anchor compensation on the
        // mirrored axes (k.java:4476-4490)
        val anchorX = x + if (dX and 1 != 0) 20 else 0
        val anchorY = y + if (dX and 2 != 0) 20 else 0
        drawObject(pack, cell, anchorX, anchorY, dX)
    }

    /** `b.e(b.d(anim,0))` (k.java:2254-2257, proven): the pixel width
     *  of anim's frame-0 composite object — `d(i,j)` = the frame's
     *  `r8[5]`-style object ref (module | flags<<2), `e()` = bounds w.
     *  Cached into `edgeCQ`/`edgeCR` exactly like `cQ`/`cR` (-1 = unset). */
    private fun animObjWidth(pack: Int, anim: Int): Int {
        val clip = clips[pack] ?: return 0
        if (anim < 0 || anim >= clip.animCount() ||
            clip.frameCount(anim) == 0) return 0
        val fi = clip.animFrameStart[anim]
        val obj = clip.frameModule[fi] or ((clip.frameFlags[fi] and 0xC0) shl 2)
        val q = obj * 4
        return if (q + 3 < clip.bounds.size) clip.bounds[q + 2] else 0
    }

    /** `a(int,int,int,boolean)` (k.java:2242-2268, proven) — the
     *  repeating edge-strip: left corner `i4` at x, `i5` tiles forward
     *  while the next tile fits inside `x+w`, a last `i5` right-aligned
     *  at `x+w-cQ-cR`, then `i4` again at `x+w` mirrored (flags=1).
     *  `z2` selects the pair {41,42} pressed / {43,44} idle on `A[2]`. */
    private fun edgeStrip(x: Int, y: Int, w: Int, pressed: Boolean) {
        val i4 = if (pressed) 41 else 43
        val i5 = if (pressed) 42 else 44
        if (edgeCQ == -1) edgeCQ = animObjWidth(93, i4)
        if (edgeCR == -1) edgeCR = animObjWidth(93, i5)
        drawFrame(93, i4, 0, x, y, 0)
        var i7 = x + edgeCQ
        do {
            drawFrame(93, i5, 0, i7, y, 0)
            i7 += edgeCR
        } while (i7 + edgeCR < x + w)
        drawFrame(93, i5, 0, x + w - edgeCQ - edgeCR, y, 0)
        drawFrame(93, i4, 0, x + w, y, 1)
    }

    /** `G()` draw surface (:2412-2460) — help/instructions scroller:
     *  chevrons, `a(y,1,cV[bw],200,iK,261,240,0,3)` wrapped viewport
     *  (8-line window, `i3 = 8*(cY-1)` start line), page counter.
     *  `cy==14` (help opened from the pause menu) → framed variant:
     *  `b(true)` + `j.h` panel (57,10,285,eF+10) + black title bar
     *  (57,18,285,18) + `j.h(-2013265920)` 4 borders + `bW` title
     *  `d(0,6)` at (200,20,17). `bw==1` → page arts: `z[11]` anim17 at
     *  (200, iK2+20) and `z[54]` anim0 palette-1 at
     *  (200, iK2+y.k(3)+cW) — the cy==14/else arms are byte-identical
     *  in the original (verbatim quirk). */
    private fun helpScreen(world: Level0World) {
        if (world.kCy == 14) {                        // cy==14 framed arm
            fillAr(57, 10, 285, world.kEf + 10, -856756498)   // j.d panel
            fillAr(57, 18, 285, 18, -16777216)                // title bar
            // j.h(-2013265920) — the verbatim 4 border rects
            fillAr(57, 8, 285, 2, -2013265920)
            fillAr(57, world.kEf + 20, 285, 2, -2013265920)
            fillAr(55, 8, 2, world.kEf + 14, -2013265920)
            fillAr(342, 8, 2, world.kEf + 14, -2013265920)
            fontW.l(0)
            world.d0(6)?.let { drawText(it, 200, 20, 17, pack = 91) }
        }
        val iK = world.menuGIK()
        val lf = if (world.pointerMoveIn(45, iK - 15, 50, 30)) 40 else 36  // d()
        val rf = if (world.pointerMoveIn(305, iK - 15, 50, 30)) 39 else 35 // d()
        drawFrame(93, lf, 0, 70, iK, 0)
        drawFrame(93, rf, 0, 330, iK, 0)
        val page = world.kCV[world.kBw] ?: return
        val u = world.helpWrap(page)
        fontY.l(1)
        fontY.drawWrapped(page, u, 200, iK, 8 * (world.kCY - 1), 8, 3)
        { g, gx, gy, pal -> drawObject(92, g, gx, gy, 0, 0, pal) }
        if (world.kBw == 1) {                         // page-1 art pair
            val iA = u[0]                             // wrapped line count
            val iK2 = iK - ((world.footerFont?.linesHeight(iA) ?: 0) / 2)
            drawFrame(11, 17, 0, 200, iK2 + 20, 0)    // z[11].a(cd,17,0,…)
            // z[54].a(cd,0,0,200, iK2+y.k(3)+cW) — h(0,1) palette-1;
            // cy==14/else arms byte-identical (verbatim quirk)
            drawFrame(54, 0, 0, 200,
                      iK2 + (world.footerFont?.linesHeight(3) ?: 0) +
                      world.kCW, 0, 1)
        }
        var i = 0
        for (i2 in 0 until world.kBw) i += world.kCX[i2]
        fontY.l(0)
        drawText("${i + world.kCY}/${world.kCZ}", 200, 220, 33)
    }

    /** `case 6` draw surface (k.java:844-853, proven): `d(0,7)` "ABOUT"
     *  title on `bW.l(1)` + `b(y,1,d(0,77),200,50,390,155,0,1)` — the
     *  scrollable credits roll clipped to (0,50,400,155). */
    private fun aboutScreen(world: Level0World) {
        fontW.l(1)
        world.d0(7)?.let { drawText(it, 200, 24, 3, pack = 91) }
        clipScissor(0, 50, 400, 105)
        fontY.l(1)
        drawText(world.d0(77) ?: "", 200, world.kFd, 3, pack = 91)
        clipScissor(0, 0, 400, 240)
    }

    /** `F()` draw surface (:2338-2371) — `a(30,d(0,5))` title bar,
     *  subtitle, chevrons, 8 score rows + TOTAL. */
    private fun scoreScreen(world: Level0World) {
        drawFrame(95, 1, 0, 200, 30, 0)               // `a(30,str)` A[3] pieces
        drawFrame(95, 2, 0, 120, 30, 0)
        fillAr(87, 39, 228, 183, -14274509)           // `j.b(87,i+9,228,183)`
        fontW.l(0)
        world.d0(5)?.let { drawText(it, 200, 30, 3, pack = 91) }
        world.d0(35 + world.kCU)?.let { drawText(it, 200, 55, 3, pack = 91) }
        val lf = if (world.pointerMoveIn(110, 15, 50, 80)) 40 else 36      // d()
        val rf = if (world.pointerMoveIn(240, 15, 50, 80)) 39 else 35      // d()
        drawFrame(93, lf, 0, 160, 55, 0)
        drawFrame(93, rf, 0, 240, 55, 0)
        for (i in 0 until 8) {
            val y = 75 + i * 14
            drawText("${world.d0(10)} ${world.kBw + i + 1}", 107, y, 20, pack = 91)
            val s = world.scoreAt(81 + (world.kCU shl 4) + ((world.kBw + i) shl 1))
            drawText(if (s > 0) s.toString() else "-", 293, y, 24, pack = 91)
        }
        var tot = 0
        for (i2 in 0 until 8) tot += world.scoreAt(81 + (world.kCU shl 4) + (i2 shl 1))
        world.d0(23)?.let { drawText(it, 107, 197, 20, pack = 91) }
        drawText(if (tot > 0) tot.toString() else "-", 293, 197, 24, pack = 91)
    }

    fun render(world: Level0World) {
        fbo.begin()
        ScreenUtils.clear(0.07f, 0.07f, 0.09f, 1f)
        batch.projectionMatrix.setToOrtho2D(
            0f, 0f, Level0World.VIEW_W.toFloat(), Level0World.VIEW_H.toFloat())
        batch.begin()

        val camX = world.camX
        val camY = world.camY
        // `eu` backdrop (k.java:2680-2771 composite + :4372-4409 `h()`
        // painter + :4504-4519 `a()` cell, proven): painted into the
        // 420×260 `dJ` offscreen buffer — cell (cx,cy) → buffer anchor
        // (cx*20, cy*20-20) — then `d()` blits the 400×240 view
        // toroidally from (i12,i13) = (euX%420, euY%260).
        // euX = camX*(euCols<21 ? 0 : euCols-21)/(lvlCols-21),
        // euY = camY*(euRows-13)/(lvlRows-13) — level-0: eu=21×13 ⇒
        // euX=euY=0, a fully static backdrop. `h()`'s aR/dT vertical-
        // parallax arm is bh3-flying-only (aR=-1 → i5=i6, dead here);
        // `ef[aj]` z[58] drift overlay static-init all-false (dead arm).
        val eu = world.level.layers.firstOrNull { it.id == 2 }
        if (eu != null) {
            val euX = camX * (if (eu.cols < 21) 0 else eu.cols - 21) /
                      (world.level.cols - 21)
            val euY = camY * (eu.rows - 13) / (world.level.rows - 13)
            val sx = euX % 420; val sy = euY % 260
            for (cy in 0 until eu.rows) {
                for (cx in 0 until eu.cols) {
                    val cell = eu.cell(cx, cy)
                    if (cell < 0 || cell == 255) continue
                    val flag = eu.flag(cx, cy)
                    var wx = (cx * 20 - sx) % 420; if (wx < 0) wx += 420
                    var wy = (cy * 20 - 20 - sy) % 260; if (wy < 0) wy += 260
                    // toroidal blit: a cell crossing a wrap edge splits
                    for (dx in intArrayOf(wx, wx - 420)) {
                        for (dy in intArrayOf(wy, wy - 260)) {
                            if (dx < 400 && dx > -20 && dy < 240 && dy > -20)
                                drawTileCell(eu.tilesetClip, cell, dx, dy, flag)
                        }
                    }
                }
            }
        }
        // draw order (k.java:2800-2819): eu → ep → er (bh4/bh3) → entities
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

        // k.b(z2) entity draw pass (k.java:2904-2934, proven): iterate the
        // `bd[]` sorted list; `ad` child draws BEFORE the parent except
        // ax76/ax29 (after + `ad.s()`); `E` held entity + `ab` overlay.
        world.buildDrawList()
        for (i32 in 0 until world.drawCount) {
            val e = world.drawList[i32]!!
            if (e.ad != null && e.ax != 76 && e.ax != 29) drawEntity(world, e.ad!!, camX, camY)
            if (e.ax == 21 && e.S == 1 && e.ad != null) {
                if (world.kC == null || world.dlgU != 9) e.ad!!.P = e.ad!!.P and 64.inv()
                e.ad!!.advanceAnim()
            }
            drawEntity(world, e, camX, camY)
            if (e.ad != null && (e.ax == 76 || e.ax == 29)) {
                drawEntity(world, e.ad!!, camX, camY); e.ad!!.advanceAnim()
            }
            // ag()→ah() ghost-trail draw — `a` card producer unported
            // (i.java:19605); `z2==0` bubble tick arm lives in the sim.
            val kE = world.kE
            if (e.ax == 0 && kE != null && (kE.P and 128) == 0 &&
                (world.jC == 8 || (world.jC == 21 && world.dlgU == 8))) {
                drawEntity(world, kE, camX, camY); kE.advanceAnim()
            }
            if ((e.ax != 11 && e.ax != 17) || e.aB > 0) world.drawPassBubble(e)
            val ab = e.ab
            if (ab != null && (ab.P and 128) == 0 && ab.inPlayV(world))
                drawEntity(world, ab, camX, camY)
            drawOverlayTail(world, e, camX, camY)
        }

        // k.b(z2) tail (k.java:3081-3083, proven): `bJ>0 && de` →
        // scissor + full-screen fill `df` (the damage flash; sim side
        // ticks `bJ--` + recomputes the ARGB ramp — k.java:2522-2526).
        if (world.kBJ > 0 && world.kDe) {
            val df = world.kDf
            batch.setColor(((df ushr 16) and 0xFF) / 255f,
                           ((df ushr 8) and 0xFF) / 255f,
                           (df and 0xFF) / 255f,
                           ((df ushr 24) and 0xFF) / 255f)
            batch.draw(white, 0f, 0f, Level0World.VIEW_W.toFloat(),
                       Level0World.VIEW_H.toFloat())
            batch.setColor(1f, 1f, 1f, 1f)
        }

        // c(z2) top bar (k.java:4178-4186, proven): `ax==0→ax=30` +
        // `g.f(ax)` (applied in hudStep), z[12] anim2 emblem at (2,30)
        // frame `(ax/15)-1` = the meter tier, `A[4]` card anim `8+bL`
        // at (22,30), then the sync bar, then z[12] anim6 overlay.
        val tierFrame = world.kAx / 15 - 1
        drawFrame(12, 2, tierFrame, 2, 30, 0)
        drawFrame(98, 8 + world.kBL, 0, 22, 30, 0)

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
        drawFrame(12, 6, tierFrame, 2, 30, 0)     // k.java:4185 overlay emblem

        // i.bA[] script-prompt cards (k.java:3085-3117, proven): while a
        // claim-script entity (`kC`) is active (`ab()`), its cb/cc state
        // selects — `cb[1] ∈ {0,1,2}` → the single YES/NO card at
        // (200,160); else `cc != null` → the choice-list fan
        // (cc[0]==3 → 200±50, cc[0]==2 → 200±50, else 200; y=160). Each
        // card ticks `b(j.f)` then `c()` draws anim e frame f at (a,b)
        // flags c — palette slot k when set. The trailing
        // `cd[8] && cb[3]>0` arm pulses bW palette 3 while counting down.
        // `j.f` = the fixed 62ms tick delta for card ticks.
        val cEnt2 = world.kC
        if (cEnt2 != null && cEnt2.claimActive()) {
            val cb = cEnt2.cb
            val cc = cEnt2.cc
            if (cb != null && (cb[1] == 0 || cb[1] == 1 || cb[1] == 2 ||
                cc != null)) {
                if (cb[1] == 0 || cb[1] == 1 || cb[1] == 2) {
                    Entity.scriptPrompts[0]?.let { pr ->
                        pr.a = 200; pr.b = 160
                        drawPrompt(pr, 62)
                    }
                } else if (cc != null) {
                    for (i54 in 0 until cc[0]) {
                        val pr = Entity.scriptPrompts[i54] ?: continue
                        pr.a = when {
                            cc[0] == 3 -> 200 + 50 * (i54 - 1)
                            cc[0] == 2 -> 200 + 50 * (if (i54 == 1) 1 else -1)
                            else -> 200
                        }
                        pr.b = 160
                        drawPrompt(pr, 62)
                    }
                }
            }
            if (cEnt2.cd[8] && cEnt2.cb != null && cEnt2.cb!![3] > 0) {
                fontW.l(3); cEnt2.cb!![3]--
            }
        }

        if (world.bh3) {
            // bh3 arm (k.java:4187-4245, proven)
            // i.bT && B!=null → boss HP column: black 6x100 at (389,60)
            // + red fill (100*aB)/bU climbing from the bottom (1px stub
            // at y159 when the fill rounds to 0 but aB>0).
            val b = world.kB
            if (world.iBT && b != null && world.iBU > 0) {
                fillAr(389, 60, 6, 100, -16777216)
                val h = (100 * b.aB) / world.iBU
                if (b.aB <= 0 || h != 0)
                    fillAr(389, 160 - h, 6, h, -65536)      // 0xFFFF0000
                else
                    fillAr(389, 159, 6, 1, -65536)
            }
            clips[54]?.let { c ->
                drawFrame(54, 0, ((world.jG % c.frameCount(0))).toInt(),
                          300, 8, 0)
            }
            drawText("${world.kAp[4]}/${world.kAq}", 312, 5, 20)
            // aE alert meter (k.java:4208-4244): aH countdown slides the
            // icon column out over 30 frames (i3 = 30-aH), aF trickles
            // +3/tick into aE (i4 = min(aE,100)); aE<25 → animated anim5
            // else anim3; anim4 marker rides the fill height.
            if (world.kAE > 0) {
                val i3 = world.alertSlide; val i4 = world.alertFill
                clips[12]?.let { c ->
                    if (world.kAE < 25)
                        drawFrame(12, 5, ((world.jG % c.frameCount(5))).toInt(),
                                  15 - i3, 165, 0)
                    else
                        drawFrame(12, 3, 0, 15 - i3, 165, 0)
                }
                drawFrame(12, 4, 0, 10 - i3,
                          49 + (116 * (100 - i4)) / 100, 0)
            }
        } else {
            // !bh3 score HUD (k.java:4247-4263, proven): `az` clamped in
            // hudStep; `n/d` progress toward the next dE threshold (or raw
            // remainder at the top tier) at (200,-1) align 17, plus the
            // z[12] anim7 icon that bobbles 1px every 3 frames.
            world.hudScoreText()?.let { score ->
                drawText(score, 200, -1, 17)
                val tw = fontY.measure(score).first()
                drawFrame(12, 7, 0, 200 - (tw shr 1) - 10,
                          13 + ((world.jG / 3) % 2).toInt(), 0)
            }
        }

        // weapon corner (k.java:4273-4287, proven): armed gate in
        // world; `at==1→0` consumed in hudStep; d(355,197,30,26) press →
        // anim22 else anim8 at (370,210), plus `dn[p(gI)]` weapon icon.
        if (world.weaponCornerArmed()) {
            drawFrame(12, if (world.weaponCornerPressed()) 22 else 8,
                      0, 370, 210, 0)
            drawFrame(12, world.weaponIconAnim(), 0, 370, 210, 0)
        }

        // aJ stopwatch (k.java:4289-4326, proven): slide-in label +
        // mm:ss:cc of `i8` at (aK,40/60); aj==7 swaps d(0,78) for d(0,122).
        if (world.kAJ >= 1) {
            val lbl = if (world.kAj == 7) world.d0(122) else world.d0(78)
            lbl?.let { drawText(it, world.kAK, 40, 0) }
            drawText(world.stopwatchText(), world.kAK, 60, 0)
        }

        // aB/aC center banner (k.java:4327-4335, proven): 400x40 black
        // bar at (0,200) + centered `aB` text; TTL steps in hudStep.
        world.kAB?.let {
            if (world.kAC != 0) {
                fillAr(0, 200, 400, 40, -16777216)
                drawText(it, 200, 202, 17)
            }
        }

        // aO/aP timed line (k.java:4337-4343, proven)
        world.kAP?.let { drawText(it, 200, 23, 17) }

        // L157 goal-milestone blit (k.java:3321-3327, proven): while
        // the ax9 goal sits in the camera band, `z[9]` anim38 blinks
        // every other j.f at (360,120). world.goalTicker = j.f%2 gate.
        if (world.goalTicker) drawFrame(9, 38, 0, 360, 120, 0)

        // k.aD capture/fuse bar (k.java:3133-3139, proven): under the
        // C-claim gate `(C!=null && (!C.cd[6] || !C.ab())) || C==null`,
        // when the HUD-bar entity sits in S6 with Z[1]>0 → z[12] f18
        // outline at (110,215), then f19 fill clipped to
        // (125,0,120*(Z[1]-Z[2])/Z[1],240) — progress fill = elapsed
        // share of the Z[1] total.
        val cEnt = world.kC
        val cGate = cEnt == null || !cEnt.cd[6] || !cEnt.claimActive()
        val barEnt = world.kAD
        if (cGate && !world.gG() && barEnt != null && barEnt.Z != null &&
            barEnt.S == 6 && barEnt.Z[1] > 0) {
            drawFrame(12, 18, 0, 110, 215, 0)
            clipScissor(125, 0, (120 * (barEnt.Z[1] - barEnt.Z[2])) /
                        barEnt.Z[1], 240)
            drawFrame(12, 19, 0, 110, 215, 0)
            clipReset()
        }

        // g.g overhead icon (k.java:4344-4350, proven): while the player
        // rides the grab-QTE states with a focus entity, z[10] anim 41
        // (S303) or 29 (S295) frame `aS.K` sits at (aS.L-O, aS.M-P).
        val pg = world.player
        if (pg.g != null && (pg.S == 303 || pg.S == 295)) {
            drawFrame(10, if (pg.S == 295) 29 else 41, pg.K,
                      pg.gQL - world.camX, pg.gQM - world.camY, 0)
        }

        // pause button (k.java:1040-1056, proven — the jc8/21 tail):
        // `J()` = !(jc==12||jc==13) — inside the case-8/21 arm so the
        // button draws under both play and dialog-overlay states.
        // `fL = new a(A[2],377,19)`; `d(354,0,46,37)` hold → pressed
        // edge + `fL.a(30,1)` else idle edge + `fL.a(25,-1)`; then
        // `fL.b(j.f)` tick + `fL.c()` draw. The `c(354,0,46,37)` →
        // `E(262144)` → `v(262144)` → `l(14)` press chain is already
        // wired in `consume`.
        if (world.jC != 12 && world.jC != 13) {
            val fl = pauseFl ?: UiAnimObject(clips[93], 377, 19)
                             .also { pauseFl = it }
            val held = world.pointerMoveIn(354, 0, 46, 37)
            edgeStrip(359, 32, 36, held)
            fl.arm(if (held) 30 else 25, if (held) 1 else -1)
            fl.tick(62)                                       // fL.b(j.f)
            drawFrame(93, fl.e, fl.currentFrame, fl.a, fl.b, fl.c)
        }

        // z[74] touch-controls overlay (k.java:3142-3161, proven):
        // `k()` + jc∉{14,5} + !(jc21,u9) + claim-gate → D-pad object at
        // (cn,134) with pressed-sector art `i55`, plus the two radial
        // action buttons (270,165)=9/10 and (320,110)=11/12 when !bh3.
        if (world.touchPadVisible()) {
            val cn = world.padCn
            val iC = if (world.padPressed()) world.padZone() else -1
            drawObject(74, world.padZoneFrame(iC), cn, 134, 0)
            if (!world.bh3) {
                drawObject(74, if (world.padButton(270, 165)) 10 else 9,
                           270, 165, 0)
                drawObject(74, if (world.padButton(320, 110)) 12 else 11,
                           320, 110, 0)
            }
        }

        // claim footer (k.java:3164, proven): `C!=null && (C.ab()||u==9)
        // && C.cd[2]` → `a("", d(0,18))` — right-pill "context" softkey.
        val kC = world.kC
        if (kC != null && (kC.claimActive() || world.dlgU == 9) && kC.cd[2]) {
            footer(world, "", world.d0(18))
        }

        // -- b(z2) overlay tail (k.java:3166-3253) -------------------------

        // `k.aQ` blit (k.java:3140-3141, proven site / inferred body):
        // `drawImage(aQ, 198 - aQ.getWidth(), 5)` — the vol-paint/debug
        // surface. `volPaintRect` records the painted rect; the composite
        // fills it (i.a(IIIIZ) — the ax35 eagle-view window).
        world.volPaintRect?.let { r -> minimap(world, r) }

        // `an`/`ao` fades (k.java:3166-3188 + `aa()` :5715-5736, proven):
        // stripe letterbox — `an` grows `fn` stripes (top `fm*fn`, bottom
        // mirrored), the finishing frame is one solid black fill; `ao`
        // shrinks to the `120-((fl-fn)*fm)` / `120-((fl-fn-1)*fm)` bars.
        if (world.kAn) {
            val h = world.kFn * world.kFm
            fillAr(0, 0, 400, h, -16777216)
            fillAr(0, 240 - h, 400, h, -16777216)
        }
        if (world.fadeSolidFrame) {
            fillAr(0, 0, 400, 240, -16777216)
            world.fadeSolidFrame = false
        }
        if (world.kAo && world.kFn >= 0) {
            val h1 = 120 - ((world.kFl - world.kFn) * world.kFm)
            val h2 = 120 - ((world.kFl - world.kFn - 1) * world.kFm)
            fillAr(0, 0, 400, h1, -16777216)
            fillAr(0, 120 + ((world.kFl - world.kFn - 1) * world.kFm),
                   400, h2, -16777216)
        }

        // `i.bh` damage vignette (k.java:3190-3202, proven): red edges
        // alpha `(255*fs)/100` while the hit-lock holds in play.
        if (world.iBh > 0 && world.jC == 8) {
            val argb = (((255 * world.kFs) / 100) shl 24) or 0xff0000
            fillAr(5, 0, 390, 5, argb); fillAr(5, 235, 390, 5, argb)
            fillAr(0, 0, 5, 240, argb); fillAr(395, 0, 5, 240, argb)
        }

        // `av`/`aw`/`dz` cinematic letterbox (k.java:3203-3218, proven):
        // black bars of height dz, top + mirrored bottom.
        if (world.jC != 14 && world.kDz > 0) {
            fillAr(0, 0, 400, world.kDz, -16777216)
            fillAr(0, 240 - world.kDz, 400, world.kDz, -16777216)
        }

        // `i.bJ` flicker line (k.java:3239, inferred): `y.l(0)` +
        // `y.a(cd, null, wrap(y,null,320), 200,50, 0,4,17,-1)` — a null-
        // string wrapped draw; no visible glyph body. Early-return on the
        // zeroing frame (`tailSkipFrame`) skips the aU bar.

        // `aU` grab-QTE meter (k.java:3241-3253, proven): white outline
        // (120,215,125,11) + fill `(125*aU.aB)/800 - 1` px — red when
        // `aB>300 || j.g%3==0` else amber 0xFFBF00.
        val aU = world.kAU
        if (aU != null && (aU.P and 32) == 0 && world.iBy > 0 &&
            !world.tailSkipFrame) {
            outlineAr(120, 215, 125, 11, -1)
            val fw = (125 * aU.aB) / 800
            fillAr(121, 215, fw - 1, 10,
                   if (aU.aB > 300 || world.jG % 3L == 0L) -65536
                   else -16512)                                  // 0xFFBF00
        }

        // k.l(21) modal dialog — case-21 u==9 arm (k.java:899-1017,
        // proven): `bN[v]` picks icon/portrait + panel side, `bO`
        // overrides the side, `i(0,bP)` panel, then `a(y,0,bM[v],…,bT)`
        // typewriter text. The world suspends behind it (i.java:20190);
        // the press tail lives in the tick.
        if (world.dialogModal) {
            // `j.a(cd,0,0,400,240,true)` dim behind the modal (:869).
            fillAr(0, 0, 400, 240, -16777216)
            if (world.dlgU == 7)                              // (:870-873)
                fillAr(0, 0, 400, 240, -16777216)
            when (world.dlgU) {
                0, 4, 5, 7 -> {                               // (:878-892)
                    val bP = if (world.dlgU == 4 || world.dlgU == 5) 60 else 240
                    dialogPanel(world, 0, bP)
                    val ty = if (world.dlgU == 4 || world.dlgU == 5)
                        bP + 120 else bP + 30
                    dialogText(world, world.dlgBM[0] ?: "",
                               200, ty, 400, 3, world.dlgBT)
                }
                else -> {                                     // u∈{1,2,3,6,8,9,10}
                    val v = world.dlgV
                    val page = world.dlgBM.getOrNull(v) ?: ""
                    var i3 = -1
                    var bP: Int
                    if (world.dlgU == 6) {                    // (:913-914)
                        bP = 137
                    } else if (world.dlgBN[v] == 1) {         // (:908-909)
                        bP = 137; i3 = 1
                    } else if (world.dlgBN[v] in 2..10) {     // (:910-912)
                        bP = 50; i3 = world.dlgBN[v]
                    } else if (world.player.al - world.kP < 120) {  // (:921)
                        bP = 137
                    } else {
                        bP = 50
                    }
                    if (world.dlgU == 9) {                    // (:926-932)
                        if (world.bO == 0) bP = 50
                        else if (world.bO == 1) bP = 137
                    }
                    dialogPanel(world, 0, bP)                 // i(0,bP) (:414-429)
                    val bT = if (world.dlgSuppressed()) world.dlgBT  // gate (:946)
                             else world.dlgTypeTick(page.length)     // (:947-955)
                    if (world.dlgU == 6 || i3 == -1) {        // (:934-935)
                        dialogText(world, page, 200, bP + 34, 380, 3, bT)
                    } else {
                        if (i3 == 1) {
                            drawFrame(98, 4 + world.kBL, 0, 378, (bP + 68) - 4, 0)
                        } else {                              // z[39] icon (:940)
                            drawFrame(39, i3, 0, 355, (bP + 68) - 2, 0)
                        }
                        dialogText(world, page, 10, bP + 4, 300, 20, bT)
                    }
                }
            }
            if (world.kFS >= 0)                               // fS tip (:1037)
                drawText(world.tipStr, 390, 40, 10, pack = 92)
        }

        // menu screens — k.L462/Q() (k.java:1108-1138, :6218-6227,
        // :5903-6150, proven): `b(x,y,w,z2,z3)` panel + `bW` prompt/title +
        // `a(str,str2)` footer soft-keys for the ae()/jc14/19/29 states.
        if (world.jC == 4) scoreScreen(world)
        if (world.jC == 5) helpScreen(world)
        if (world.jC == 6) aboutScreen(world)
        if (world.panelVisible) {
            val pr = world.menuPanelRect()
            menuPanel(world, pr[0], pr[1], pr[2],
                      world.menuPanelZ2(), world.menuPanelZ3())
            if (world.jC == 23 || world.jC == 28) {
                // ae() `bW.a(cd,d(0,eC),a(bW,str,200),200,80,...)` (:6221)
                // — centered bW title; the eC==121 arm draws at y=120.
                world.d0(world.kEc)?.let { t ->
                    fontW.l(1)
                    val cx = 200 - fontW.measure(t).first() / 2
                    drawText(t, cx, if (world.kEc == 121) 120 else 80, 3)
                }
            }
            if (world.menuVisible) {
                world.menuPrompt()?.let { t ->
                    drawText(t, 200, pr[1] + 26, 3, palette = 1, pack = 91)
                }
            }
            val fl = world.menuFooter()
            if (fl.first != null || fl.second != null) {
                footer(world, fl.first, fl.second)
            }
        }

        // stats screen — k.L466 (k.java:1788, proven): `d(0,bx)` text +
        // `j.g%6` "TOUCH THE SCREEN" blink at (200,173) on `y`.
        if (world.statsVisible) {
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 93f, 40f, 214f, 150f)
            batch.setColor(1f, 1f, 1f, 1f)
            world.statsText()?.let { t -> drawText(t, 200, 90, 3) }
            if (world.jG % 6L < 3L) {
                val t = world.d0(9) ?: "TOUCH THE SCREEN"
                drawText(t, 200, 173, 3)
            }
        }

        // N() load screen (k.java:3472-3513) — unreachable-labeled.
        if (world.jC == 9) loadScreen(world)

        // jc0 boot R() (k.java:3949-4100): black + logo + loading text.
        if (world.jC == 0) bootScreen(world)

        // jc18 title screen (k.java:1146-1157) — unreachable-labeled.
        if (world.jC == 18) titleScreen(world)

        // jc20 story-typewriter draw tail (k.java:1278-1298, proven):
        // `f(false)` world behind; cu>=2 → `z[39].a(cd,1,0,eY,80)` slide
        // icon + `y.a(cd,str,5,eZ,0)` text (cu2 = grown fa, cu>=3 = fb);
        // cu 4/5 also `z[39].a(cd,10,0,300,80)` spinner. footerQ() arms
        // the NEXT/SKIP footer in world.menuFooter().
        if (world.jC == 20) storyScreen(world)

        // jc24 ending credits (k.java:1326-1386, proven)
        if (world.jC == 24) creditsScreen(world)

        // jc1 hard-mode unlock toast (k.java:800-811, proven): `f(false)`
        // world behind; `a(y,0,d(0,99),200,120,220,240,0,3)` early-returns
        // on the dx end-tail (fd=-1 post-l() → re-fires — the toast text
        // never draws, verbatim quirk); `y.l(1)` + `d(0,9)` blink at
        // (200,220,3) while `j.g%10<5` — unreachable in practice since
        // j.c==1 self-skips to l(25) in one tick, kept verbatim.
        if (world.jC == 1 && world.hintBlink) {
            drawText(world.d0(9) ?: "", 200, 220, 3, pack = 92)
        }

        // M() win-stats screen (k.java:3280-3445, proven positions):
        // `a(i2,d(0,60))` title ribbon + `bW.a` rows — labels x=95
        // (align 20), values right-aligned x=305 (align 24), rows
        // 55+20i; total row y=175; `a(d(0,16),str2)` bottom hint.
        if (world.jC == 15) {
            val H = Level0World.VIEW_H
            batch.setColor(0f, 0f, 0f, 0.8f)
            batch.draw(white, 0f, 0f, 400f, 240f)
            // `a(i2,str)` title-bar proc (k.java:2328-2336, proven):
            // A[3] anim1 centered + anim2 left-cap + j.b(87,i+9,228,183)
            // dark panel + bW title (matches scoreScreen's use).
            val ty = world.statsTitleY
            drawFrame(95, 1, 0, 200, ty, 0)
            drawFrame(95, 2, 0, 120, ty, 0)
            fillAr(87, ty + 9, 228, 183, -14274509)
            fontW.l(0)
            world.d0(60)?.let { t -> drawText(t, 200, ty, 3, pack = 91) }
            // row labels (x=95, align 20) + values right-aligned x=305 (24)
            for (i3 in 0..4) {
                val v = world.statsRowText[i3]
                if (v.isEmpty()) continue
                world.d0(38 + i3)?.let { t -> drawText(t, 95, 55 + i3 * 20, 20) }
                drawText(v, 305, 55 + i3 * 20, 24)
            }
            // total row (y=175, one-shot after jG>10)
            if (world.statsScoreVisible) {
                world.d0(43)?.let { t -> drawText(t, 95, 175, 20) }
                drawText(world.fmtJ(world.statsScore), 305, 175, 24)
            }
            // `a(d(0,16),str2)` hint — NEXT ▸ typewriter (inferred box)
            if (world.statsTypeNext >= 0) {
                val t = (world.d0(16) ?: "NEXT") + " " +
                        world.typewriterText
                drawText(t, 200, 222, 3)
            }
        }

        // ag() mission poster card (k.java:6358-6386, proven):
        // `i(0,120)` dialog panel + `A[4].a(cd, i+4, 0, 200, 119)` where
        // i = index of aj+1 in fP={0,2,5,7} (else 4) + brief
        // a(y,0,d(0,110),200,150,380,240,0,3) + `y.l(1)` + `j.g%10<5`
        // d(0,9) blink at (200,220).
        if (world.jC == 10) {
            val H = Level0World.VIEW_H
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 0f, 0f, 400f, 240f)
            dialogPanel(world, 0, 120)               // i(0,120)
            // i = 1; while (i<4 && aj+1 != fP[i]) i++  → 4 for level 0
            var pi = 1
            while (pi < 4 && world.kAj + 1 != posterFP[pi]) pi++
            drawFrame(98, pi + 4, 0, 200, 119, 0)    // A[4] poster frame
            if (world.posterBrief.isNotEmpty()) {
                // a(y,0,…,200,150,380,240,0,3) — 9-arg drops i5/i6 →
                // effective a(y,0,str,200,150,380,align=3,limit=-1)
                dialogText(world, world.posterBrief, 200, 150, 380, 3, -1)
            }
            fontY.l(1)                                // y.l(1) after draw
                                                    // (blink draws pal-1)
            if (world.hintBlink) {
                world.d0(9)?.let { t -> drawText(t, 200, 220, 3) }
            }
        }

        // ah() medal/unlock viewer (k.java:6392-6490, proven positions):
        // title d(0,113) at (210,43); panel (114,59,172,155); rows
        // (114,70+45i,172,40) labels at (164, 70+45i+21); j.g<10 →
        // black fade (10-j.g)*25 alpha; blink hint (200,220).
        if (world.jC == 22) {
            val H = Level0World.VIEW_H
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 0f, 0f, 400f, 240f)
            if (world.medalTitle.isNotEmpty()) {
                drawText(world.medalTitle, 210, 43, 17, palette = 1, pack = 91)
            }
            batch.setColor(0.08f, 0.07f, 0.1f, 0.95f)
            batch.draw(white, 114f, (H - 59 - 155).toFloat(), 172f, 155f)
            for (i in 0 until world.medalRowCount) {
                val ry = (H - 70 - i * 45 - 40).toFloat()
                batch.setColor(0.16f, 0.14f, 0.2f, 1f)
                batch.draw(white, 114f, ry, 172f, 40f)
                // medal icon placeholder — z[73] frame circle
                val icon = world.medalRowIcon[i]
                if (icon >= 0) {
                    if (world.medalRowDim[i])
                        batch.setColor(0.3f, 0.3f, 0.35f, 1f)
                    else
                        batch.setColor(0.85f, 0.7f, 0.25f, 1f)
                    batch.draw(white, 122f, ry + 12f, 16f, 16f)
                }
                val t = world.medalRowText[i]
                if (t.isNotEmpty()) drawText(t, 164, 91 + i * 45, 6)
            }
            batch.setColor(1f, 1f, 1f, 1f)
            if (world.screenFadeAlpha > 0) {
                batch.setColor(0f, 0f, 0f,
                               (world.screenFadeAlpha / 255f).coerceIn(0f,1f))
                batch.draw(white, 0f, 0f, 400f, 240f)
                batch.setColor(1f, 1f, 1f, 1f)
            }
            if (world.hintBlink && !world.hintBack) {
                world.d0(9)?.let { t -> drawText(t, 200, 220, 3) }
            }
            if (world.hintBack) {
                world.d0(17)?.let { t -> drawText(t, 390, 222, 24) }
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
    /** `k.bo[bL]` (k.java:274, proven) — per-area clip-0
     *  (palette, remapTable) pairs for ax0 entities. */
    private val boArt = arrayOf(
        intArrayOf(0, -1), intArrayOf(3, 1), intArrayOf(5, 2), intArrayOf(6, 3))

    private fun drawEntity(world: Level0World, e: Entity, camX: Int, camY: Int) {
        // `aU()` (i.java:3047 → :8989-9143, proven): ax10 early-outs of
        // the blit path entirely — before the clip checks (zones carry
        // no drawable clip). S34 draws its rail line(s); S31's prompt
        // row is dormant on level 0 (no records); other S draw nothing.
        if (e.ax == 10) {
            if (e.S == 34) drawRailLine(world, e, camX, camY)
            return
        }
        val clip = e.clip ?: return
        if (e.S < 0 || e.S >= clip.animCount()) return
        val pack = clipPackOf(clip) ?: return

        // i.a(Graphics) art-select preamble (i.java:3040-3180, proven):
        // `aa.l(i)` palette select + `aa.a(i)` az-remap select + `aa.g`
        // palette alpha, chosen per ax before the (P&128)==0 blit.
        var palette = e.palette
        var alpha = 255
        val last = e.T >= clip.frameCount(e.S) - 1
        // `i.a(Graphics)` ax13 arm (i.java:3050-3052 → :13391, proven):
        // draws the rope segments (object Z[7] of clip61) BEFORE the
        // standard blit; the ax13 entity skips the art-select `when`
        // entirely (it's the `else` branch in the original).
        if (e.ax == 13) drawRopeSegments(e, pack, camX, camY)
        when {
            e.ax == 45 -> palette = e.Z.getOrElse(0) { 0 }
            e.ax == 30 || e.ax == 32 -> {
                palette = 0
                if (e.cGCount > 0) {
                    e.cGCount--
                    if (e.cGCount % 2 != 0) palette = 1
                }
            }
            e.ax == 11 -> palette =
                if (e.Z.getOrElse(0) { 0 } == 1 || e.Z.getOrElse(0) { 0 } == 2) 1 else 0
            e.ax == 47 || e.ax == 17 || e.ax == 73 -> palette = 0
            e.ax == 68 -> palette = if (e.af != null && e.af!!.ax == 30) 1 else 0
            (e.ax == 0 || e.ax == 9 || e.ax == 4 ||
             (e.ax == 67 && e.Z.getOrElse(0) { 0 } == 11)) && !world.bh3 -> {
                if (world.iCe) palette = 1
                else if (e.ax == 9) {
                    if (e.S == 21 || e.S == 22) e.ad = null
                    palette = when (world.kAj) {
                        2 -> 5; 3 -> 6; 5 -> 7; 6 -> 4; else -> 0
                    }
                } else palette = 0
                if (e.ax == 9 && e.Z.getOrElse(2) { 0 } == 47) {
                    // candle entity (i.java:3079-3115): S4/S5 need k.bK
                    if (e.S == 4 || e.S == 5) {
                        if (!world.kBK) return
                        palette = 5
                    }
                    if (world.iCe && e.S == 4 && last) { world.removeEntity(e); return }
                    if (world.iCe && e.S == 2 && last) e.P = e.P or 64
                    if (e.S == 5) {
                        e.ak = camX; e.al = camY
                        if (last) e.P = e.P or 64
                        if (e.aC > 0 && (e.P and 64) != 0) {
                            alpha = (e.aC * 255) / 10
                            e.aC--
                            if (e.aC <= 0) { world.removeEntity(e); return }
                        }
                    }
                } else if (e.ax == 0 && world.kBL in boArt.indices) {
                    palette = boArt[world.kBL][0]
                    e.remapTable = boArt[world.kBL][1]
                }
            }
            e.ax == 43 -> {
                // vision-split (i.java:3132-3143): draw only OUTSIDE cv's
                // x-span — left of W[0] when ak <= mid, right of W[2] else.
                val cv = world.cv
                if (cv != null) {
                    if (e.ak <= (cv.W[0] + cv.W[2]) shr 1) {
                        clipScissor(0, 0, cv.W[0] - camX, 240)
                    } else {
                        clipScissor(cv.W[2] - camX, 0, 400 - (cv.W[2] - camX), 240)
                    }
                }
            }
            e.ax == 79 -> { e.remapTable = e.Z.getOrElse(1) { 0 }
                            palette = e.Z.getOrElse(0) { 0 } }
            e.ax == 46 -> e.remapTable =
                if (e.Z.getOrElse(6) { 0 } == 0) e.Z.getOrElse(7) { 0 } else -1
            e.ax == 29 -> {
                if (world.iBy == 2 || world.iBy == 3) {
                    e.remapTable = 0
                    // `aa.j[0] = j[1 or 4]` palette-row blink every 3rd
                    // j.g → our -palette-NN slots (inferred mapping).
                    if (e.S != 28 && e.S != 20 && e.S != 4 && e.S != 24 &&
                        e.S != 25 && e.S != 26 && e.S != 22) {
                        palette = if (world.jG % 3L == 0L) 1 else 4
                    }
                } else e.remapTable = -1
            }
            e.ax == 61 -> palette = 0
            e.ax == 74 -> if (e.S == 3 || e.S == 4 || e.S == 5) palette = 7
        }

        val fd = clip.frameDraw(e.S, e.T, e.drawFlags())
        val obj = clip.remap(e.remapTable, fd.module)      // az[aA][i11]
        if (alpha != 255) batch.setColor(1f, 1f, 1f, alpha / 255f)
        drawObject(pack, obj, e.ak - camX - fd.dx, e.al - camY - fd.dy, fd.transform,
                   palette = palette)
        if (alpha != 255) batch.setColor(1f, 1f, 1f, 1f)
        if (e.ax == 43 && world.cv != null) clipReset()
    }

    /** `i.a(Graphics)` ax13 rope draw (i.java:13391-13411, proven):
     *  anchor object `Z[7]` at `(N>>8, O>>8)`, then `i4` middle
     *  segments (`Z[1]-1`, or `bN-1` when `aG==4`) stepped 12px along
     *  the pendulum angle `bP>>8` (angle-256), plus the end segment.
     *  `iB = 3072·j.b(n-θ)>>8` = 12px·cos θ; `iB2 = 3072·j.b(θ)>>8` =
     *  12px·sin θ. */
    private fun drawRopeSegments(e: Entity, pack: Int, camX: Int, camY: Int) {
        if (e.Z.size <= 7) return
        val ax = (e.N shr 8) - camX
        val ay = (e.O shr 8) - camY
        val th = e.bP shr 8
        val sx = (3072 * Trig.sin(Trig.N - th)) shr 8   // iB  = 12px·cos θ
        val sy = (3072 * Trig.sin(th)) shr 8            // iB2 = 12px·sin θ
        val segs = if (e.aG == 4) e.bN - 1 else e.Z[1] - 1
        drawObject(pack, e.Z[7], ax, ay, e.P, palette = e.palette)  // anchor
        var fx = ax shl 8
        var fy = (ay shl 8) + 3072                                // i7 = i6+3072
        for (s in 1..segs) {
            drawObject(pack, e.Z[7], fx shr 8, fy shr 8, 0, palette = e.palette)
            fx += sx; fy += sy
        }
        drawObject(pack, e.Z[7], fx shr 8, fy shr 8, 0, palette = e.palette)
    }

    /** `aU()` case-34 draw (i.java:9106-9143, proven): rail line(s) in
     *  ROPE_COL — single span when the player isn't over the rail's
     *  x-range, else two segments split at the player's screen x. */
    private fun drawRailLine(world: Level0World, e: Entity, camX: Int, camY: Int) {
        val wa = e.W
        val fwd = e.Z[0] == 0
        val p = world.player
        val free = p.af == null || p.af === e
        val near = p.S != 9 &&
            ((p.af === e && p.ak > wa[0] && p.ak < wa[2]) ||
             Entity.overlapI(p.W, wa))
        var i10 = -1; var i3 = wa[1] - camY; var i11 = 0
        if (near) {
            if (p.ak >= wa[0] && p.ak <= wa[2]) i10 = p.ak - camX
            val slope = ((wa[3] - wa[1]) shl 8) / (wa[2] - wa[0])
            val ry = wa[1] + (if (fwd)
                (slope * (p.ak - wa[0])) shr 8
            else
                (slope * (wa[2] - p.ak)) shr 8)
            i11 = ry - camY
        }
        val i9 = wa[3] - camY
        val xa = (if (fwd) wa[0] else wa[2]) - camX
        val xb = (if (fwd) wa[2] else wa[0]) - camX
        if (i10 == -1) {
            drawLine(xa, i3, xb, i9, ROPE_COL)
            drawLine(xa + 1, i3 + 1, xb + 1, i9 + 1, ROPE_COL)
        } else {
            drawLine(xa, i3, i10, i11, ROPE_COL)
            drawLine(i10, i11, xb, i9, ROPE_COL)
            drawLine(xa + 1, i3 + 1, i10 + 1, i11 + 1, ROPE_COL)
            drawLine(i10 + 1, i11 + 1, xb + 1, i9 + 1, ROPE_COL)
        }
    }

    private fun clipPackOf(clip: Clip): Int? =
        clips.entries.firstOrNull { it.value === clip }?.key

    fun dispose() {
        fbo.dispose(); batch.dispose()
        if (::font.isInitialized) font.dispose()
        if (::atlas.isInitialized) atlas.dispose()
        if (::packer.isInitialized) packer.dispose()
        paletteTextures.forEach { it.dispose() }
    }
}
