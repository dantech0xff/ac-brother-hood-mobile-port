package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.Entity
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
        val charmap = com.acrebuild.core.FontClip.loadCharmap(
            Gdx.files.internal("fonts/charmap.bin").readBytes())
        fontW = com.acrebuild.core.FontClip(clips[91]!!, charmap, 4)
        fontY = com.acrebuild.core.FontClip(clips[92]!!, charmap, 4)
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
            val t = Texture(fh)
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            TextureRegion(t)
        }
    }

    private fun drawModule(pack: Int, m: Int, x: Int, y: Int, transform: Int, palette: Int = 0) {
        val src = moduleRegion(pack, m, palette) ?: return
        val (w, h) = (clipDims[pack] ?: clipDims[-pack])!![m]
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

    /** `b.a(g, anim, frame, x, y, flags, 0, 0)` — single frame draw
     *  (the `a`-object/`A[2]` path, b.java:915). */
    private fun drawFrame(pack: Int, anim: Int, frame: Int, x: Int, y: Int,
                          flags: Int, palette: Int = 0) {
        val clip = clips[pack] ?: clips[-pack] ?: return
        if (anim < 0 || anim >= clip.animCount() ||
            frame < 0 || frame >= clip.frameCount(anim)) return
        val fd = clip.frameDraw(anim, frame, flags)
        drawModule(pack, fd.module and 0x3FFF, x - fd.dx, y - fd.dy,
                   fd.transform, palette)
    }

    // -- b(x,y,w,z2,z3) menu panel (k.java:5903-6150, proven) --------------
    private var menuFj: UiAnimObject? = null          // k.fJ (a.java inst)
    private var menuFk: UiAnimObject? = null          // k.fK
    private val ROPE_COL = -3584205                    // k.b rope-line (k.java:2961)
    private val BAR_DEAD_S = intArrayOf(24, 21, 0, 139, 133, 134, 145, 135, 106, 107)
    private var menuEz = 0                            // k.ez fit-scroll

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
                   0f, 0.5f, len, 1f, 1f, 1f, rot,
                   0, 0, white.width, white.height, false, false)
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

    /** `G()` draw surface (:2412-2460) — help/instructions scroller:
     *  chevrons, `a(y,1,cV[bw],200,iK,261,240,0,3)` wrapped viewport
     *  (8-line window, `i3 = 8*(cY-1)` start line), page counter.
     *  `z[11]`/`z[54]` page arts not converted — skipped (`inferred`). */
    private fun helpScreen(world: Level0World) {
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
        var i = 0
        for (i2 in 0 until world.kBw) i += world.kCX[i2]
        fontY.l(0)
        drawText("${i + world.kCY}/${world.kCZ}", 200, 220, 33)
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

        // k.b(z2) entity draw pass (k.java:2904-2934, proven): iterate the
        // `bd[]` sorted list; `ad` child draws BEFORE the parent except
        // ax76/ax29 (after + `ad.s()`); `E` held entity + `ab` overlay.
        world.buildDrawList()
        for (i32 in 0 until world.drawCount) {
            val e = world.drawList[i32]!!
            if (e.ad != null && e.ax != 76 && e.ax != 29) drawEntity(e.ad!!, camX, camY)
            if (e.ax == 21 && e.S == 1 && e.ad != null) {
                if (world.kC == null || world.subU != 9) e.ad!!.P = e.ad!!.P and 64.inv()
                e.ad!!.advanceAnim()
            }
            drawEntity(e, camX, camY)
            if (e.ad != null && (e.ax == 76 || e.ax == 29)) {
                drawEntity(e.ad!!, camX, camY); e.ad!!.advanceAnim()
            }
            // ag()→ah() ghost-trail draw — `a` card producer unported
            // (i.java:19605); `z2==0` bubble tick arm lives in the sim.
            val kE = world.kE
            if (e.ax == 0 && kE != null && (kE.P and 128) == 0 &&
                (world.jC == 8 || (world.jC == 21 && world.subU == 8))) {
                drawEntity(kE, camX, camY); kE.advanceAnim()
            }
            if ((e.ax != 11 && e.ax != 17) || e.aB > 0) world.drawPassBubble(e)
            val ab = e.ab
            if (ab != null && (ab.P and 128) == 0 && ab.inPlayV(world))
                drawEntity(ab, camX, camY)
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
        drawFrame(4, 8 + world.kBL, 0, 22, 30, 0)

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
        if (kC != null && (kC.claimActive() || world.subU == 9) && kC.cd[2]) {
            footer(world, "", world.d0(18))
        }

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

        // menu screens — k.L462/Q() (k.java:1108-1138, :6218-6227,
        // :5903-6150, proven): `b(x,y,w,z2,z3)` panel + `bW` prompt/title +
        // `a(str,str2)` footer soft-keys for the ae()/jc14/19/29 states.
        if (world.jC == 4) scoreScreen(world)
        if (world.jC == 5) helpScreen(world)
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
            world.d0(60)?.let { t -> drawText(t, 200, ty, 1) }
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

        // ag() mission poster card (k.java:6358, proven positions):
        // `i(0,120)` card overlay (frame12 + fill — procedural stand-in),
        // `A[4]` frame i+4 at (200,119), brief a(y,0,d(0,110),200,150,
        // 380,240,0,3), `j.g%10<5` → d(0,9) blink at (200,220).
        if (world.jC == 10) {
            val H = Level0World.VIEW_H
            batch.setColor(0f, 0f, 0f, 0.85f)
            batch.draw(white, 0f, 0f, 400f, 240f)
            // card frame (A[4] clip unported → dark plate stand-in)
            batch.setColor(0.12f, 0.1f, 0.16f, 1f)
            batch.draw(white, 10f, (H - 200).toFloat(), 380f, 190f)
            batch.setColor(0.8f, 0.15f, 0.15f, 0.9f)
            batch.draw(white, 10f, (H - 30).toFloat(), 380f, 4f)
            batch.setColor(1f, 1f, 1f, 1f)
            if (world.posterBrief.isNotEmpty()) {
                drawText(world.posterBrief, 200, 150, 3)
            }
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
