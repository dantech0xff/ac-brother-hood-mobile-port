package com.acrebuild.core

/**
 * Port of the original `b` sprite-clip runtime (ACPK pack emitted by
 * `tools/convert_slice1.py`). Semantics ported 1:1 from `b.java`:
 *
 * - anims index the frame pool via `h[anim]` (frameStart); `au[anim]` frames.
 * - each frame: module index `av | ((i & 0xC0) << 2)`, tick duration `aw`,
 *   signed offsets `ax`/`ay`, transform `i & 15` (J2ME Sprite.TRANS_*).
 * - `ao`/`am_or_an` object space: drawable objects (modules + composite
 *   sprites) own rect spans; rect 0 = W hitbox, rect 1 = X attackbox,
 *   queried per frame's module with facing flips (`b.java:849-905`).
 * - `ai`/`aj` + `ap_aq_ar_as` = composite placement pool used by the 7-arg
 *   draw (`b.java:915`) for props (ropes, chunk sprites).
 */
class Clip private constructor(
    val moduleNames: List<String>,
    val moduleW: IntArray,
    val moduleH: IntArray,
    val animFrameStart: IntArray,
    val animFrameCount: IntArray,
    val frameModule: IntArray,
    val frameDuration: IntArray,
    val frameDx: IntArray,
    val frameDy: IntArray,
    val frameFlags: IntArray,
    val objRectStart: IntArray,
    val objRectCount: IntArray,
    val objPlaceStart: IntArray,
    val objPlaceCount: IntArray,
    val rects: IntArray,        // x,y,w,h quads (am_or_an — W/X rects)
    val bounds: IntArray,       // x,y,w,h quads (ak_or_al — Y bounds)
    val placements: IntArray,   // module,flags,x,y quads
    val remapTables: List<IntArray> = emptyList(), // az[] — aA object remap
) {

    /** `aa.a()` — anim count. */
    fun animCount(): Int = animFrameCount.size

    /** `aa.b(anim)` — frames in anim. */
    fun frameCount(anim: Int): Int = animFrameCount[anim]

    /** `aa.a(anim, frame)` — per-frame tick duration. */
    fun frameDuration(anim: Int, frame: Int): Int =
        frameDuration[animFrameStart[anim] + frame]

    fun frameIndex(anim: Int, frame: Int): Int = animFrameStart[anim] + frame

    /** `av | ((i & 0xC0) << 2)` — the frame's drawable-object index. */
    fun frameModuleIndex(anim: Int, frame: Int): Int {
        val fi = frameIndex(anim, frame)
        return frameModule[fi] or ((frameFlags[fi] and 0xC0) shl 2)
    }

    fun moduleWidth(module: Int): Int = moduleW[module]
    fun moduleHeight(module: Int): Int = moduleH[module]

    /**
     * `aa.a(anim, frame, which, out[4], flags)` — rect query (`b.java:884`).
     * `which` 0 = W hitbox, 1 = X attackbox; `flags & 1` mirrors x,
     * `flags & 2` mirrors y. `out` receives [x, y, w, h] relative to the
     * entity anchor, or zeros when absent.
     */
    fun rect(anim: Int, frame: Int, which: Int, flags: Int, out: IntArray) {
        val fi = frameIndex(anim, frame)
        val obj = frameModule[fi] or ((frameFlags[fi] and 0xC0) shl 2)
        val start = objRectStart[obj]
        val count = objRectCount[obj]
        if (which >= count) {
            out[0] = 0; out[1] = 0; out[2] = 0; out[3] = 0
            return
        }
        val ri = (start + which) * 4
        var x = rects[ri]; var y = rects[ri + 1]
        val w = rects[ri + 2]; val h = rects[ri + 3]
        val fl = flags xor (frameFlags[fi] and 15)
        if (fl and 1 != 0) x = -x - w
        if (fl and 2 != 0) y = -y - h
        out[0] = x; out[1] = y; out[2] = w; out[3] = h
    }

    /**
     * `aa.g/h/e/f(obj)` — per-object bounds quad (b.java:868-895; the
     * `ak`/`al` array read by t()'s Y fill). `out` = [x,y,w,h].
     */
    fun objectBounds(obj: Int, out: IntArray) {
        val q = obj * 4
        if (q + 3 >= bounds.size) { out.fill(0); return }
        out[0] = bounds[q]; out[1] = bounds[q + 1]
        out[2] = bounds[q + 2]; out[3] = bounds[q + 3]
    }

    /** Draw descriptor for one frame (`b.java:907`): module + anchor offset. */
    fun frameDraw(anim: Int, frame: Int, flags: Int): FrameDraw {
        val fi = frameIndex(anim, frame)
        // (i5 & 1) ? x + ax : x - ax ; same for y — sign flips when mirrored.
        // b.java:907 — draw pos = anchor - (cam + dx); with flags&1 the
        // stored dx is subtracted the other way, i.e. draw pos flips sign.
        // Returned dx is already sign-folded: screenX = anchorX - camX - dx.
        val flipX = flags and 1 != 0
        val flipY = flags and 2 != 0
        val dx = if (flipX) -frameDx[fi] else frameDx[fi]
        val dy = if (flipY) -frameDy[fi] else frameDy[fi]
        return FrameDraw(frameModule[fi] or ((frameFlags[fi] and 0xC0) shl 2),
                         dx, dy, (flags xor (frameFlags[fi] and 15)) and 15)
    }

    /** `az[aA][i]` (b.java:926/1057, proven): when an `aa.a(table)`
     *  remap slot is armed, drawable-object index `i` resolves through
     *  table `aA`. Tables are sparse (key,value) overlays on an identity
     *  `short[ab]`; `aa.a(n)` on a clip with no table n is the identity
     *  no-op (b.java:714-720 fills `az[i]` with 0..ab-1 first). */
    fun remap(table: Int, obj: Int): Int {
        if (table < 0 || table >= remapTables.size) return obj
        val t = remapTables[table]
        return if (obj in t.indices) t[obj] else obj
    }

    class FrameDraw(val module: Int, val dx: Int, val dy: Int, val transform: Int)

    /** Composite sprite draw list (`b.java:915`): one entry per placement. */
    fun placements(obj: Int): List<Triple<Int, Int, Pair<Int, Int>>> {
        val start = objPlaceStart[obj]
        val count = objPlaceCount[obj]
        val out = ArrayList<Triple<Int, Int, Pair<Int, Int>>>(count)
        for (k in 0 until count) {
            val pi = (start + k) * 4
            out += Triple(placements[pi], placements[pi + 1],
                          placements[pi + 2] to placements[pi + 3])
        }
        return out
    }

    companion object {
        fun load(data: ByteArray): Clip {
            val r = PackReader(data)
            r.magic("ACPK")
            require(r.u8() == 1) { "ACPK version" }
            val moduleCount = r.u16()
            val names = ArrayList<String>(moduleCount)
            val mw = IntArray(moduleCount); val mh = IntArray(moduleCount)
            for (i in 0 until moduleCount) {
                mw[i] = r.u16(); mh[i] = r.u16()
                names.add(String(r.bytes(r.u8()), Charsets.US_ASCII))
            }
            val animCount = r.u16()
            val aStart = IntArray(animCount); val aCount = IntArray(animCount)
            for (i in 0 until animCount) { aStart[i] = r.u16(); aCount[i] = r.u16() }
            val frameCount = r.u32().toInt()
            val fMod = IntArray(frameCount); val fDur = IntArray(frameCount)
            val fDx = IntArray(frameCount); val fDy = IntArray(frameCount)
            val fFl = IntArray(frameCount)
            for (i in 0 until frameCount) {
                fMod[i] = r.u16(); fDur[i] = r.u8()
                fDx[i] = r.i16(); fDy[i] = r.i16(); fFl[i] = r.u8()
            }
            val objCount = r.u16()
            val oRs = IntArray(objCount); val oRc = IntArray(objCount)
            val oPs = IntArray(objCount); val oPc = IntArray(objCount)
            for (i in 0 until objCount) {
                oRs[i] = r.u16(); oRc[i] = r.u16(); oPs[i] = r.u16(); oPc[i] = r.u16()
            }
            val rectCount = r.u32().toInt()
            val rects = IntArray(rectCount * 4)
            for (i in rects.indices) rects[i] = r.i16()
            val boundsCount = r.u32().toInt()
            val bounds = IntArray(boundsCount * 4)
            for (i in bounds.indices) bounds[i] = r.i16()
            val placeCount = r.u32().toInt()
            val places = IntArray(placeCount * 4)
            for (i in 0 until placeCount) {
                places[i * 4] = r.u16(); places[i * 4 + 1] = r.u8()
                places[i * 4 + 2] = r.i16(); places[i * 4 + 3] = r.i16()
            }
            val remaps = ArrayList<IntArray>()
            if (r.pos < data.size) {
                // ACPK tail (v1 extension): `u8 tableCount`, then per table
                // `u16 pairCount` + (u16 src, u16 dst) sparse overrides on
                // an identity [0..objectCount) table — az[ab] indexes the
                // OBJECT space (b.java:926/1057), not module space.
                val tableCount = r.u8()
                repeat(tableCount) {
                    val t = IntArray(oRs.size) { it }
                    val pairs = r.u16()
                    repeat(pairs) { t[r.u16()] = r.u16() }
                    remaps += t
                }
            }
            require(r.pos == data.size) { "trailing bytes in ACPK" }
            return Clip(names, mw, mh, aStart, aCount, fMod, fDur, fDx, fDy,
                        fFl, oRs, oRc, oPs, oPc, rects, bounds, places, remaps)
        }
    }
}
