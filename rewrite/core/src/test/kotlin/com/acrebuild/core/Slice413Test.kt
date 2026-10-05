package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 413 — the player's helper methods re-read from the raw bytes (`g.javap.txt`).
 *
 * - `g.ar()` @384-393 / `g.aq()` @356-365 (the standing / mounted interact action): the ax58 lever
 *   arm is `g.i(i.S + 1)` — the PLAYER's S plus one (the reach / throw anim just picked), not the
 *   lever's own S. That is out of the lever clip's range for every picked anim, so `i(int)` ignores
 *   it: a knife throw never flips a lever. The port read `g.S + 1` and flipped it.
 * - `g.ax()` @224-250: the slope pull is `aR == 17 || aR == 16 → ah = -ag >> 1` (the port tested 17
 *   only).
 * - `g.d(Z)` @88-281 (`Entity.land`): the landing anim first, then `if (I == 4) h(1)` (an equip-4
 *   player is handed his sword back on touchdown), then the fall damage — which reads `S` / `Q`
 *   after the switch. The port had no `h(1)` and ran the damage before the anim.
 *
 * Verified equal, no change: `g.au()` (the wheel / pole / cart orbit, every arm and sign), `g.as()`
 * (the lunge arc), `g.l()`, `g.aw()`, `g.ay()` (the combo step's ledge guard and `aN` clamp), `g.aB()`,
 * `g.ao()`, `g.i(i)`, `g.at()`.
 */
class Slice413Test {
    private fun lever(w: Level0World, s: Int): Entity =
        Entity(58, w.clips[20]).apply { aw = 88888; setAnim(s) }

    // ---------------------------------------------------------------- ar() / aq()

    @Test fun `ar - a lever hit by the throw gets the PLAYER's S plus one, not its own`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val lv = lever(w, 0)
        // the lever sits above the player, almost on his axis: rise:run > 1024 → no reach anim is
        // picked and the player's S stays 5 (so the bytes' `i(S + 1)` is i(6))
        p.setPositionPx(300, 200); p.setAnim(5); p.refreshBoxes()
        lv.setPositionPx(301, 100); lv.refreshBoxes()
        p.g = lv
        p.K = 4
        p.interactAction(w, w.pad)
        assertEquals(6, lv.S, "g.i(i.S + 1) = i(5 + 1); the lever's own S + 1 would be 1")
    }

    @Test fun `ar - an out-of-range player anim leaves the lever alone`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val lv = lever(w, 0)
        p.setPositionPx(300, 200); p.setAnim(0); p.refreshBoxes()
        lv.setPositionPx(360, 200); lv.refreshBoxes()           // beside the player → a 299-302 reach anim
        p.g = lv
        p.K = 4
        p.interactAction(w, w.pad)
        assertTrue(p.S in 299..302, "reach anim S${p.S}")
        assertEquals(0, lv.S, "i(S + 1) is not a lever anim: ignored")
    }

    @Test fun `aq - the mounted action also uses the player's S plus one`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val lv = lever(w, 0)
        p.setPositionPx(300, 200); p.setAnim(5); p.refreshBoxes()
        lv.setPositionPx(360, 200); lv.refreshBoxes()           // beside the player → a 304-306 mounted reach anim
        p.g = lv
        p.mountedInteractAction(w, w.pad)
        assertTrue(p.S in 304..306, "mounted reach anim S${p.S}")
        assertEquals(0, lv.S, "i(S + 1) is not a lever anim: ignored")
    }

    @Test fun `ar - the S2 lever still fires i(3)`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val lv = lever(w, 2)
        p.setPositionPx(300, 200); p.setAnim(5); p.refreshBoxes()
        lv.setPositionPx(301, 100); lv.refreshBoxes()
        p.g = lv
        p.K = 4
        p.interactAction(w, w.pad)
        assertEquals(3, lv.S)
    }

    // ---------------------------------------------------------------- ax() slope pull

    @Test fun `ax - cell 16 pulls like cell 17 (aR == 17 or aR == 16)`() {
        for (r in intArrayOf(16, 17)) {
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 100); p.refreshBoxes()
            p.aZ = true; p.av = true; p.aR = r; p.aA = 0; p.S = 0
            w.pad.commit(Pad.M_LEFT)
            w.playerFsm.l(p, w.pad)
            assertEquals(1280, p.ah, "aR=$r: ah = (-ag) >> 1 with ag = -2560")
        }
        for (r in intArrayOf(14, 15)) {                        // the down-slopes pull the other way
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 100); p.refreshBoxes()
            p.aZ = true; p.av = true; p.aR = r; p.aA = 0; p.S = 0
            w.pad.commit(Pad.M_LEFT)
            w.playerFsm.l(p, w.pad)
            assertEquals(0, p.ah, "aR=$r: ah = ag >> 1 is negative → clamped to 0")
        }
    }

    // ---------------------------------------------------------------- d(Z) landing

    @Test fun `land - an equip-4 player is handed the sword back on touchdown`() {
        for (variant in booleanArrayOf(false, true)) {
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 100); p.refreshBoxes()
            p.gJ = 15; p.gI = 4; p.S = 43
            p.land(w, variant)
            assertEquals(1, p.gI, "platformVariant=$variant: @211 `if (I == 4) h(1)`")
        }
    }

    @Test fun `land - other equips are left alone`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        p.gJ = 15; p.gI = 2; p.S = 43
        p.land(w, false)
        assertEquals(2, p.gI)
    }

    // ---------------------------------------------------------------- a(0) = g.a(int)

    /** `finish` the current clip so `animFinished()` (`r()`) holds. */
    private fun finish(p: Entity) {
        p.jumpToLastFrame()
        p.U = maxOf(0, p.clip!!.frameDuration(p.S, p.T) - 1)
    }

    @Test fun `a0 - the fall entry re-centres al on the box centre of the last a(Z) pass`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.setAnim(377); p.refreshBoxes()
        p.collideSides(w, false)                       // i.a(Z) tail: t/u = the S377 box centre
        val al0 = p.al; val uc0 = p.uc
        p.enterFall(0, w)
        val c43 = (p.W[1] + p.W[3]) shr 1              // the S43 box at the pre-shift anchor
        assertEquals(43, p.S)
        assertTrue(uc0 != c43, "the two centres differ, so the masked entry moves the anchor")
        assertEquals(al0 + (uc0 - c43) + 10, p.al, "g.a(int): a(43, 32) then al += 10")
        assertEquals(0, p.ah)
        assertEquals(1536, p.aj)
    }

    @Test fun `a0 - S377 recovery drops into S43 through the re-centre`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.setAnim(377); p.refreshBoxes()
        finish(p)
        p.refreshBoxes()                                 // the finished frame's box: what the e() head's a(Z) pass sees
        val al0 = p.al
        val c377 = (p.W[1] + p.W[3]) shr 1
        val q = Entity(0, p.clip); q.setPositionPx(300, al0); q.setAnim(43); q.refreshBoxes()
        val c43 = (q.W[1] + q.W[3]) shr 1
        assertTrue(c377 != c43)
        w.playerFsm.tick(p, w.pad)
        assertEquals(43, p.S, "@4309 r() → a(0)")
        assertEquals(al0 + (c377 - c43) + 10, p.al, "S377 centre - S43 centre, then the +10 of g.a(int)")
    }

    @Test fun `a0 - the scroll holder ceiling drops an air-action player through the re-centre`() {
        for (s in intArrayOf(23, 25, 242)) {
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 200); p.setAnim(s); p.refreshBoxes()
            p.collideSides(w, false)
            val al0 = p.al; val uc0 = p.uc
            p.ah = -256
            p.Y[1] = p.W[1]
            val holder = Entity(37, null).apply { Z[3] = 1; Z[0] = 4; X[1] = p.W[1] }
            w.kAh = holder
            try { w.scrollWallClamp(p) } finally { w.kAh = null }
            assertEquals(43, p.S, "S$s: i.f(i) top bound → k.aS.a(0)")
            val c43 = (p.W[1] + p.W[3]) shr 1
            assertTrue(uc0 != c43, "S$s: the centres differ")
            assertEquals(al0 + (uc0 - c43) + 10, p.al, "S$s")
        }
    }

    @Test fun `a0 - the air wall resolve ceiling hit drops through the re-centre`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        var cx = -1; var cy = -1
        search@ for (y in 4 until w.kBq - 4) for (x in 4 until w.kBp - 4) {
            if (w.collisionCell(x, y) >= 20) { cx = x; cy = y; break@search }
        }
        assertTrue(cx >= 0, "level 0 has a solid cell")
        p.setAnim(23); p.setPositionPx(cx * 20 + 10, 300); p.refreshBoxes()
        val off1 = p.W[1] - p.al                         // W[1] of the box probed 20 px higher
        p.setPositionPx(cx * 20 + 10, cy * 20 + 10 - off1 + 20); p.refreshBoxes()
        p.collideSides(w, false)
        val al0 = p.al; val uc0 = p.uc
        p.airWallResolve(w)                              // av(): the top row of the raised box is solid
        assertTrue(p.aO >= 20, "the probe saw the ceiling cell")
        assertEquals(43, p.S, "@36 a(0)")
        val c43 = (p.W[1] + p.W[3]) shr 1
        assertTrue(uc0 != c43, "the centres differ")
        assertEquals(al0 + (uc0 - c43) + 10, p.al)
    }

    // ---------------------------------------------------------------- c(Z): raw k.g, not i.e

    @Test fun `c(Z) - the grapple-climb facing cell is the raw k_g read, S37 has no pass-through there`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        var checked = 0
        search@ for (cy in 6..70) for (ak in 60..3000 step 7) {
            p.setPositionPx(ak, cy * 20 + 10); p.setAnim(37); p.av = true; p.refreshBoxes()
            val cx = (p.W[0] / 20) - 1
            if (cx < 0 || w.collisionCell(cx, p.al / 20) != 20) continue
            // `g.c(Z)` @40: `k.g(cx, al/20) < 12` — a solid 20 cell is NOT open. `i.e()` would read
            // it as 0 for S37 (the vault pass-through) and call it open.
            assertEquals(false, p.facingCellOpen(w), "solid wall cell at ($cx, ${p.al / 20})")
            checked++
            if (checked >= 3) break@search
        }
        assertTrue(checked > 0, "level 0 has a wall to face")
        // and an open cell is open
        p.setPositionPx(300, 100); p.setAnim(37); p.av = true; p.refreshBoxes()
        val cx = (p.W[0] / 20) - 1
        if (w.collisionCell(cx, p.al / 20) < 12) assertEquals(true, p.facingCellOpen(w))
    }

    // ---------------------------------------------------------------- g.h(I)Z vs i.h(I)V

    // `k.aS.h(n)` in the bytes is `invokevirtual g.h:(I)Z` — the player's equip request (`I = n`,
    // `k.at = 1`) — at aW() @847, bC() @1014, aE() @129 and aJ() @2037. The claim-script bind
    // `i.h(I)V` is a different, private method that only takes `k.s(..)` script indexes.

    private fun ax13Rope(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(13, null)
        e.setPositionPx(x, y)
        val f = mutableListOf(13, 0, x, y, 3, 0, 0)          // r8[4] = 3 → the aG4 rope of the shipped levels
        for (i in 7..15) f += 0
        w.npcFsm.initAx13(e, f)
        e.Z[1] = 10
        w.npcs.add(e)
        return e
    }

    @Test fun `h - the ax13 rope grab requests equip 1, it does not bind claim script 1`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        var grabbed = false
        for (dy in 10..130) {
            w.npcs.clear()
            p.setPositionPx(400, 300); p.setAnim(18); p.refreshBoxes()
            p.bM = null; p.aA = p.aA and 64.inv()
            p.gI = 4; p.gJ = 1 or 4; p.ca = -1
            val e = ax13Rope(w, p.ak, p.al - dy)
            e.bN = 6
            w.npcFsm.tickAx13(e, w, p)
            if (p.bM === e) {
                grabbed = true
                assertEquals(1, p.gI, "aW() @847: g.h(1) → I = 1")
                assertEquals(-1, p.ca, "...and no claim script is bound on the player")
                assertEquals(326, p.S)
                break
            }
        }
        assertTrue(grabbed, "some rope offset catches the player")
    }

    @Test fun `h - the ax69 armed kill requests equip 1, it does not bind claim script 1`() {
        val w = world(); w.npcs.clear()
        val e = Entity(69, w.clips[38])
        val rec = mutableListOf(69, 152, 100, 200, 1, 0, 0, -1)
        while (rec.size < 22) rec += 0
        e.setPositionPx(100, 200)
        w.npcFsm.initAx69(e, rec.toList(), w)
        w.npcs.add(e)
        e.setAnim(7)
        val p = w.player
        p.af = e
        p.setPositionPx(100, 163)
        p.gI = 4; p.gJ = 1; p.ca = -1
        val victim = Entity(11, w.clips[7]); victim.aw = 7
        victim.setPositionPx(160, 190); victim.av = false; victim.refreshBoxes()
        w.npcs.add(victim)
        w.paint(e, victim)
        w.pad.commit(65568)
        w.npcFsm.tickAx69(e, w, p)
        assertEquals(244, p.S, "the kill ran")
        assertEquals(1, p.gI, "bC() @1014: g.h(1) → I = 1")
        assertEquals(-1, p.ca, "no claim script is bound on the player")
    }

    @Test fun `land - the fall damage is a real landing's only`() {
        fun drained(s: Int, q: Int, variant: Boolean): Int {
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 100); p.refreshBoxes()
            p.S = s; p.Q = q; p.gt = 0; p.x1 = 30; p.gy = -10000   // a fall of hundreds of cells
            p.land(w, variant)
            return 30 - p.x1
        }
        assertEquals(30, drained(43, 0, false), "plain landing: op 21 drains (clamped at 0)")
        assertEquals(30, drained(150, 0, false), "S150 landing: also a real landing")
        assertEquals(0, drained(16, 0, false), "S16 door-exit arm: no damage")
        assertEquals(0, drained(43, 16, false), "Q16: no damage")
        assertEquals(0, drained(43, 0, true), "platform variant: no damage")
    }
}
