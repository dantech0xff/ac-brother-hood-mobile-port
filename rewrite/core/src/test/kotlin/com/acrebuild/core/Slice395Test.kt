package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 395 — the ax29 boss `aP()` and its attack picker `aQ()` (i.javap
 * `aP()` @0-3897, `aQ()` @0-832) re-read from the raw bytecode.
 *
 * `aQ()` — four defects:
 * - the by3 idle-window pick (`ci[2] >= 160` → table[3] = S17, the finisher)
 *   did not stop: the distance bands that followed overwrote it, so the
 *   finisher never fired;
 * - `ci[3] = 0` belongs to the ELSE path only (@264-272): the by1 first-time
 *   arm (S33 + `e(17,…)` + `cm`) leaves it at ≥ 80 so the very next pick is
 *   the S7 grab;
 * - the mid band (60 < |Δx| ≤ 100) is `if (ci[1]>=48 && aS.aZ) {S5} else if
 *   (ci[0]>=32) {S15}` — the port ran both tests and let S15 win;
 * - the by3 remap is `5→40, 8→39, 10→37, 14→35` (the port had `5→40, 6→39`:
 *   the by3 far band played the inert S10 and froze the boss), and picks
 *   15/16/17 spawn the aura `e(4/5/6, …)` and play sfx 33/33/31 (14/35: 31) —
 *   the port dropped both.
 *
 * `aP()` — S4's forward lunge was backwards (`ag = av ? -2560 : 2560`); S14/35
 * scan the floor under an airborne player in a LOOP (the port stepped once);
 * S6 does not zero ah/ag (S15/16 do); S9/37's `r()` test is independent of the
 * `|Δx| < 100` test; the arena clamp ends `a(1,0); return` (the S-switch is
 * skipped that tick).
 */
class Slice395Test {
    private data class Rig(val w: Level0World, val e: Entity, val p: Entity)

    private fun rig(by: Int, dx: Int = 0, pS: Int = 0, aZ: Boolean = true): Rig {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 400); p.refreshBoxes(); p.setAnim(pS)
        p.aZ = aZ
        w.kM(2)                                    // camera on the player
        val e = Entity(29, w.clips[52]); e.aw = 60; e.aB = 600
        e.setPositionPx(300 + dx, 400); e.refreshBoxes()
        w.npcs.add(e)
        w.iBy = by; w.iCi = IntArray(5); w.iCm = false; w.iCj = true
        return Rig(w, e, p)
    }

    private fun pick(r: Rig) = r.w.npcFsm.bossPick(r.e)

    // ------------------------------------------------------------ aQ: by0 / by3 window
    @Test fun `by0 always picks table 7 - S14 and sfx 31`() {
        val r = rig(0, dx = 150)
        pick(r)
        assertEquals(14, r.e.S)
        assertTrue(31 in r.w.sfxLog)
        assertEquals(0, r.e.ag)
    }

    @Test fun `by3 idle window picks the S17 finisher and the bands cannot overwrite it`() {
        val r = rig(3, dx = 150)
        assertTrue(r.e.inPlayV(r.w), "fixture: v()")
        r.w.iCi!![2] = 160; r.w.iCi!![1] = 48; r.w.iCi!![0] = 32   // tempt the bands
        pick(r)
        assertEquals(17, r.e.S, "table[3] = 17")
        assertEquals(0, r.w.iCi!![2], "ci[2] = 0")
        assertEquals(6, r.w.iCk?.S, "e(6,…) aura for a 17 pick")
        assertTrue(31 in r.w.sfxLog, "k.A(31) for 17")
        assertEquals(48, r.w.iCi!![1], "the bands did not run")
    }

    @Test fun `by3 idle window needs an idle player`() {
        val r = rig(3, dx = 150, pS = 8)
        r.w.iCi!![2] = 160
        pick(r)
        assertTrue(r.e.S != 17, "player S8 is not in the idle set")
        assertEquals(160, r.w.iCi!![2])
    }

    @Test fun `the by3 window, the finisher arm and the far window all need v() - an off-screen boss just stalks`() {
        for (by in intArrayOf(1, 3)) {
            val r = rig(by, dx = 1500)
            assertFalse(r.e.inPlayV(r.w), "fixture: the boss is off camera")
            r.w.iCi!![2] = 160; r.w.iCi!![3] = 80; r.w.iCi!![1] = 48
            r.e.aB = 400
            pick(r)
            assertEquals(if (by == 3) 37 else 9, r.e.S, "by$by: far band without a window")
            assertEquals(160, r.w.iCi!![2], "by$by ci2")
            assertEquals(80, r.w.iCi!![3], "by$by ci3")
            assertEquals(48, r.w.iCi!![1], "by$by ci1")
        }
    }

    // ------------------------------------------------------------ aQ: finisher arm
    @Test fun `by1 first finisher arm picks S33 and leaves ci3 for the grab that follows`() {
        val r = rig(1, dx = 150)
        r.e.aB = 400; r.w.iCi!![3] = 80
        pick(r)
        assertEquals(33, r.e.S, "table[9]")
        assertTrue(r.w.iCm, "cm = true")
        assertEquals(80, r.w.iCi!![3], "@257-261: ci[3] is NOT reset here")
        assertEquals(17, r.w.iCk?.S, "e(17,…) aura")
        pick(r)                                    // second pick: cm now set → the else path
        assertEquals(14, r.e.S, "table[7]")
        assertEquals(0, r.w.iCi!![3], "@264-272")
    }

    @Test fun `by3 finisher arm is index 7 with the by3 remap - S35`() {
        val r = rig(3, dx = 150)
        r.e.aB = 400; r.w.iCi!![3] = 80
        pick(r)
        assertEquals(35, r.e.S, "table[7] = 14 → by3 remap 35")
        assertEquals(0, r.w.iCi!![3])
        assertTrue(31 in r.w.sfxLog, "k.A(31) for the remapped 35 too")
    }

    @Test fun `the finisher arm needs aB at or under 500 and v()`() {
        val r = rig(1, dx = 150)
        r.e.aB = 501; r.w.iCi!![3] = 80
        pick(r)
        assertEquals(80, r.w.iCi!![3], "aB > 500 → no finisher arm")
        assertEquals(9, r.e.S, "…so the far band picks the stalk, not the S33 finisher")
        assertFalse(r.w.iCm, "cm stays clear")
    }

    // ------------------------------------------------------------ aQ: bands
    @Test fun `far band - idle window picks S7 and resets, otherwise S9 (by3 S37)`() {
        val a = rig(1, dx = 150); a.w.iCi!![1] = 48
        pick(a)
        assertEquals(7, a.e.S); assertEquals(0, a.w.iCi!![1]); assertFalse(a.w.iCj)
        val b = rig(1, dx = 150); b.w.iCi!![1] = 47
        pick(b)
        assertEquals(9, b.e.S, "table[4]")
        val c = rig(3, dx = 150)
        pick(c)
        assertEquals(37, c.e.S, "by3 far: index 8 → 10 → remap 37 (the port played the inert S10)")
        val d = rig(1, dx = 150, pS = 8); d.w.iCi!![1] = 48
        pick(d)
        assertEquals(9, d.e.S, "a non-idle player: no S7")
    }

    @Test fun `mid band - S5 or else S15, never both`() {
        val a = rig(1, dx = 80, aZ = true)
        a.w.iCi!![1] = 48; a.w.iCi!![0] = 32
        pick(a)
        assertEquals(5, a.e.S, "table[6]")
        assertEquals(32, a.w.iCi!![0], "S15's ci[0] arm did not run")
        assertNotNull(a.e.cU, "a(true,0) trail armed")
        val a3 = rig(3, dx = 80, aZ = true)
        a3.w.iCi!![1] = 48
        pick(a3)
        assertEquals(40, a3.e.S, "by3 remap 5 → 40")
        val b = rig(1, dx = 80, aZ = false)
        b.w.iCi!![1] = 48; b.w.iCi!![0] = 32
        pick(b)
        assertEquals(15, b.e.S, "airborne player: the S15 arm")
        assertEquals(0, b.w.iCi!![0])
        assertEquals(4, b.w.iCk?.S, "e(4,…) for 15")
        assertTrue(33 in b.w.sfxLog)
        val c = rig(1, dx = 80)
        c.e.setAnim(8)
        pick(c)
        assertEquals(0, c.e.S, "neither: idx -1 → i(0)")
    }

    @Test fun `near band - S16 with aura and sfx 33, else S8 (by3 S39)`() {
        val a = rig(1, dx = 40); a.w.iCi!![0] = 32
        pick(a)
        assertEquals(16, a.e.S); assertEquals(0, a.w.iCi!![0])
        assertEquals(5, a.w.iCk?.S); assertTrue(33 in a.w.sfxLog)
        val b = rig(1, dx = 40)
        pick(b)
        assertEquals(8, b.e.S, "table[5]")
        val c = rig(3, dx = 40)
        pick(c)
        assertEquals(39, c.e.S, "by3 remap 8 → 39")
    }

    @Test fun `a valid pick clears ci4`() {
        val r = rig(1, dx = 40); r.w.iCi!![4] = 9
        pick(r)
        assertEquals(0, r.w.iCi!![4])
    }

    // ------------------------------------------------------------ aP arms
    @Test fun `S4 lunges forward - negative ag when facing left`() {
        for (av in listOf(false, true)) {
            val r = rig(1, dx = 400)
            r.e.setAnim(4); r.e.av = av
            r.w.npcFsm.tickBoss(r.e, r.p, Pad())
            assertEquals(if (av) -2560 else 2560, r.e.ag, "av=$av")
        }
    }

    @Test fun `S4 strike turns into S6 with the e(2) aura when it touches the player`() {
        val r = rig(1, dx = 0)
        r.e.setAnim(4)
        r.w.npcFsm.tickBoss(r.e, r.p, Pad())
        assertEquals(6, r.e.S, "touch → i(6)")
        assertEquals(0, r.e.ag, "the lunge stops")
        assertEquals(2, r.w.iCk?.S, "e(2, ak, al, 300)")
    }

    @Test fun `S14 scans down to the floor under an airborne player`() {
        val r = rig(1, dx = 200, aZ = false)
        r.p.setPositionPx(300, 100); r.p.refreshBoxes(); r.p.aZ = false
        r.e.setAnim(14)
        r.e.X[0] = 10; r.e.X[1] = 10; r.e.X[2] = 40; r.e.X[3] = 40
        var y = 100
        while (r.w.collisionCell(300 / 20, y / 20).let { it < 12 && it != 5 && it != 3 }) y += 10
        assertTrue(y > 200, "fixture: the floor is well below")
        r.w.npcFsm.tickBoss(r.e, r.p, Pad())
        val fx = r.w.pendingInsert.filter { it.ax == 61 && it.Z[9] != 0 }
        assertTrue(fx.isNotEmpty(), "path fx queued")
        assertEquals(y, fx.first().Z[9], "target y = the first standable cell row")
    }

    @Test fun `S6 keeps its ah and ag while S15 zeroes them`() {
        for (s in intArrayOf(6, 15)) {
            val r = rig(1, dx = 400)
            r.e.setAnim(s); r.e.ag = 77; r.e.ah = 55
            r.w.npcFsm.tickBoss(r.e, r.p, Pad())
            if (s == 6) { assertEquals(77, r.e.ag, "S6"); assertEquals(55, r.e.ah) }
            else { assertEquals(0, r.e.ag, "S15"); assertEquals(0, r.e.ah) }
        }
    }

    @Test fun `S9 re-tests r() after the close-in i0`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 400); p.refreshBoxes(); p.setAnim(0); w.kM(2)
        val e = Entity(29, null); e.aw = 60; e.aB = 600
        e.setPositionPx(340, 400)                   // |dx| 40 < 100
        e.setAnim(9)
        w.npcs.add(e)
        w.iBy = 1; w.iCi = IntArray(5)
        w.npcFsm.tickBoss(e, p, Pad())
        assertTrue(e.S != 0 && e.S != 9,
            "i(0) then the independent r() test (clip-less → finished) runs the picker: S=${e.S}")
    }

    @Test fun `the arena clamp ends a(1,0) and returns - the S switch does not run`() {
        val r = rig(1, dx = 0)
        r.w.boundMinX = r.e.W[0] + 5; r.w.boundMaxX = 100000
        r.e.setAnim(8); r.e.av = false
        r.e.refreshBoxes()
        r.w.npcFsm.tickBoss(r.e, r.p, Pad())
        assertEquals(2, r.e.S, "clamp → i(2)")
        assertEquals(0, r.e.ag, "S2's lunge arm (±5120) must not have run this tick")
        assertNotNull(r.e.cU, "a(1,0)")
    }
}
