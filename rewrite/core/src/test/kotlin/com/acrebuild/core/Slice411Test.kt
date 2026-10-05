package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 411 — the flying player tick `g.n()` (`g.javap.txt` 2.5 KB), re-read from the raw bytes.
 *
 * - @1022-1025 (S1/9/12/27 recovery): `i(4); goto 2262` — straight to the friction block. The
 *   port also wrote `av = 0` (not in the bytes) and ran the glide tail (steering input) on the
 *   recovery tick.
 * - @1035-1040 (S3): `r() → i(4); goto 2262`; only the unfinished S3 takes `av = 0` and the glide
 *   tail @1043-1048.
 * - `k.B()` (the mission BGM start) is not called from `n()` — its callers are `k.l(int)`,
 *   `k.Q()` and `k.a(boolean)`; the port re-requested the track on every flight tick.
 * - `g.c(i)` (the mount lunge, `g.javap.txt` `c(Li;)V` @138-160): the anim pick is
 *   `invokestatic g.b:(I)Z` — the AIR / HANG / CLIMB set (the same overload the hit intake's head
 *   tests, slice 410) → S292; S298 keeps its anim; any other (grounded) state picks the 272-275
 *   slope arc. The port tested the no-arg attack set, so an airborne press took a slope arc whose
 *   hand box lifts the player ~47 px, and a grounded combo state took S292.
 *
 * Everything else in `n()` (head meter/conveyor arms, the S2/S24, S20-S23, S25-S29 arms, the glide
 * tail's steering/chase blocks, `aC()`, `e(boolean)`, the friction block and the `ad` copy) was
 * diffed against the bytes and matches.
 */
class Slice411Test {
    private fun flying(): Level0World {
        val w = world(aj = 1)
        settleIntro(w)
        assertTrue(w.bh3, "mission 1 is a flying (bh==3) mission")
        return w
    }

    /** Run S's anim to its last frame so `r()` is true. */
    private fun finish(p: Entity, s: Int) {
        p.setAnim(s)
        var guard = 0
        while (!p.animFinished() && guard++ < 400) p.advanceAnim()
        assertTrue(p.animFinished(), "S$s anim finished")
    }

    @Test fun `S3 finishing tick is i(4) and the friction block only - no glide tail`() {
        val w = flying(); val p = w.player
        finish(p, 3)
        p.av = true; p.ag = 0
        w.pad.e(Pad.M_LEFT)
        w.playerFsm.tickBody(p, w.pad)
        assertEquals(4, p.S)
        assertEquals(0, p.ag, "the steering arm (ag -= 768) did not run on the finishing tick")
        assertTrue(p.av, "@1040 goto 2262 skips `av = 0`")
    }

    @Test fun `S1 S9 S12 S27 recovery is i(4) with the iBB release and no av write or steering`() {
        for (s in intArrayOf(1, 9, 12, 27)) {
            val w = flying(); val p = w.player
            finish(p, s)
            p.av = true; p.ag = 0
            w.iBB = true; w.iBG = 5; p.az = 0
            w.kAw = 0
            w.pad.e(Pad.M_LEFT)
            w.playerFsm.tickBody(p, w.pad)
            assertEquals(4, p.S, "S$s → i(4)")
            assertFalse(w.iBB, "S$s: bB = 0")
            assertEquals(-1, w.iBG, "S$s: bG = -1")
            assertEquals(202, p.az, "S$s: k.aS.az = 202")
            assertEquals(0, w.kAw, "aC(): k.aw 20 → 0")
            assertEquals(0, p.ag, "S$s: no steering on the recovery tick")
            assertTrue(p.av, "S$s: no `av = 0`")
        }
    }

    @Test fun `an unfinished S3 faces forward and steers through the glide tail`() {
        val w = flying(); val p = w.player
        p.setAnim(3)
        assertFalse(p.animFinished())
        p.av = true; p.ag = 0
        w.pad.e(Pad.M_LEFT)
        w.playerFsm.tickBody(p, w.pad)
        assertEquals(3, p.S, "S3 is excluded from the bank-anim switch (z4 = false)")
        assertFalse(p.av, "@1043 av = 0")
        assertTrue(p.ag < 0, "u(4112) steered left")
    }

    @Test fun `flight ticks do not re-request the mission music`() {
        val w = flying()
        w.sfxLog.clear()
        repeat(6) { w.tick(emptyList()) }
        val track = w.kEE[w.kAj]
        assertFalse(9 in w.sfxLog, "no per-tick k.B()")
        assertFalse(track != -1 && track in w.sfxLog, "no per-tick k.B() (track $track)")
    }

    // ---------------------------------------------------------------- g.c(i) lunge anim pick

    private val AIR = intArrayOf(18, 19, 20, 22, 23, 24, 25, 35, 36, 43, 150, 157, 165, 233, 242, 243,
        263, 264, 265, 266)
    private val ATTACK = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115, 183, 184, 216, 217, 286, 287)

    private fun wheelAt(x: Int, y: Int): Entity = Entity(72, null).apply {
        ak = x; al = y; Z[0] = 2
        W[0] = x - 15; W[1] = y - 15; W[2] = x + 15; W[3] = y + 15
    }

    /** The player's box centre after `refreshBoxes()` for state [s] — the lunge's start anchor. */
    private fun stand(w: Level0World, s: Int): IntArray {
        val p = w.player
        p.setPositionPx(300, 100); p.S = s; p.T = 0; p.refreshBoxes()
        return intArrayOf((p.W[0] + p.W[2]) shr 1, (p.W[1] + p.W[3]) shr 1)
    }

    @Test fun `lunge - every air hang and climb state takes S292 and keeps the press point`() {
        for (s in AIR) {
            val w = world(); w.npcs.clear()
            val c = stand(w, s)
            val wheel = wheelAt(c[0] + 90, c[1] - 120)
            val p = w.player
            val x0 = p.ak; val y0 = p.al
            w.sfxLog.clear()
            p.grabLunge(wheel, w)
            assertEquals(292, p.S, "S$s: g.b(int) → i(292)")
            assertTrue(30 in w.sfxLog, "S$s: k.A(30)")
            assertSame(wheel, p.F)
            // S292 has no hand rect — X = the press point, so the swing radius is the plain
            // distance from the press point to the wheel centre
            assertEquals(Trig.khypot(wheel.ak - p.ak, ((wheel.W[1] + wheel.W[3]) shr 1) - p.al), p.cB,
                "S$s: cB = k.h(target - hand)")
            Entity.at = wheel
            try {
                var guard = 0
                while (p.S == 292 && guard++ < 20) p.lungeTick(w)
            } finally { Entity.at = null }
            assertEquals(277, p.S, "S$s: arc end → the mount-on anim")
            assertEquals(x0, p.ak, "S$s: the press point is kept")
            assertEquals(y0, p.al)
        }
    }

    @Test fun `lunge - a grounded player takes the slope arc, combo states included`() {
        for (s in intArrayOf(0, 1, 7, 11, 12, 26, 79) + ATTACK) {
            val w = world(); w.npcs.clear()
            val c = stand(w, s)
            val wheel = wheelAt(c[0] + 100, c[1] - 50)          // rise:run = 128 → 273
            val p = w.player
            p.grabLunge(wheel, w)
            assertEquals(273, p.S, "S$s is not in g.b(int)'s set → the slope pick")
        }
    }

    @Test fun `lunge - the slope pick thresholds are 64 256 and 1024`() {
        val cases = listOf(          // (dx, dy) → anim
            100 to 10 to 272, 100 to 25 to 272, 100 to 26 to 273, 100 to 100 to 273,
            100 to 101 to 274, 100 to 400 to 274, 100 to 401 to 275, 100 to 800 to 275)
        for ((dd, anim) in cases) {
            val (dx, dy) = dd
            val w = world(); w.npcs.clear()
            val c = stand(w, 0)
            val p = w.player
            p.grabLunge(wheelAt(c[0] + dx, c[1] - dy), w)
            assertEquals(anim, p.S, "dx=$dx dy=$dy")
        }
        // degenerate: no run → 275 (steepest); no rise → 272 (flattest)
        run {
            val w = world(); w.npcs.clear()
            val c = stand(w, 0)
            w.player.grabLunge(wheelAt(c[0], c[1] - 60), w)
            assertEquals(275, w.player.S, "r03 == 0")
        }
        run {
            val w = world(); w.npcs.clear()
            val c = stand(w, 0)
            w.player.grabLunge(wheelAt(c[0] + 60, c[1]), w)
            assertEquals(272, w.player.S, "r04 == 0")
        }
    }

    @Test fun `lunge - S298 keeps its anim`() {
        val w = world(); w.npcs.clear()
        val c = stand(w, 298)
        val p = w.player
        w.sfxLog.clear()
        p.grabLunge(wheelAt(c[0] + 100, c[1] - 50), w)
        assertEquals(298, p.S)
        assertTrue(30 in w.sfxLog)
    }

    @Test fun `lunge - the grounded slope arc lifts the player by the hand box, the aerial one does not`() {
        val w = world(); w.npcs.clear()
        val c = stand(w, 0)
        val wheel = wheelAt(c[0] + 100, c[1] - 50)
        val p = w.player
        val y0 = p.al
        p.grabLunge(wheel, w)
        Entity.at = wheel
        try {
            var guard = 0
            while (p.S in 272..275 && guard++ < 20) p.lungeTick(w)
        } finally { Entity.at = null }
        assertEquals(277, p.S)
        assertTrue(p.al < y0, "the hand rect sits above the feet: the arc lands the player higher (al ${p.al} < $y0)")
    }
}
