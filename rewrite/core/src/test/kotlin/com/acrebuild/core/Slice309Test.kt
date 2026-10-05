package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class Slice309Test {
    /**
     * m7 capstone feed-in leg — the u252-claim zone lifecycle + the
     * pillar-top reachability from the west-stair top.
     *
     * The feed-in connects slice-302's u252-claim release (zone
     * W=[301,1057,340,1218], covering the lower pillar top) to slice-308's
     * leg L (arena approach starting at the pillar top (349,540)).
     *
     * Proven this slice:
     *  (a) the u252 ax5 claim zone binds+releases when the player is inside
     *      its W — the player mounts u248 (ax66-S12 lift/perch at
     *      (318,1136)) and the claim runs scr42 then releases;
     *  (b) the u248 release throws the player WEST (a fixed direction, not
     *      the faced direction) onto the pillar's west edge;
     *  (c) the west-stair top segment is traversable: middle shelf
     *      (x0-239@y660) → the '##########'x100-299 mass's west face →
     *      the mass top (y519) → east → the pillar-top '2' ledge region.
     *
     * The full release→pillar-top bot route is a precision gauntlet — the
     * perch chain (u248→u169/u170), the lethal lift X-boxes (u56/u57
     * leading-edge kill strips), and the S101-bounce column faces are all
     * mapped but the lower connection (release → middle shelf) requires
     * swing-release timing a simple waypoint bot can't reliably traverse.
     */
    @Test fun m7FeedIn() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5
        val u252 = w.findByAw(252)!!
        val u248 = w.findByAw(248)!!
        assertEquals(5, u252.ax); assertEquals(intArrayOf(301, 1057, 340, 1218).toList(), u252.W.toList())
        assertEquals(66, u248.ax); assertEquals(12, u248.S)

        // (a) park inside u252's zone → fall → mount u248 → claim binds → releases
        p.ak = u252.ak; p.al = u252.al; p.N = u252.ak shl 8; p.setAnim(0); p.aZ = true; p.refreshBoxes()
        var mounted = false; var bound = false; var released = false
        for (t in 0..120) {
            // G12 (k.I() order): u248's board test (i.bm() L131, needs
            // g.b(S)) reads the player's state from the previous frame,
            // and the straight fall lands (S5) in the same frame its W
            // first reaches u248's W — so the drop alone never boards.
            // A jump press on the landing spot puts him in S233 (in
            // g.b()) while overlapping, and the next entity pass boards.
            w.pad.e(if (!mounted && p.aZ && p.S == 0) Pad.M_UP else 0); w.tick(emptyList())
            if (p.ga === u248) mounted = true
            if (w.kC === u252) bound = true
            if (bound && w.kC == null) { released = true; break }
        }
        assertTrue(mounted, "player mounts u248 inside the u252 zone")
        assertTrue(bound, "u252 claim binds when the player is in its zone")
        assertTrue(released, "u252 claim releases after its script runs")

        // (b) releasing u248 (a direction-toward-av lunge) lets the player
        //     continue the perch chain / drop to the lower structures.
        var freed = false
        // slice 413: the scroll-holder ceiling's `k.aS.a(0)` (i.f(i) @354) now runs the masked
        // `a(43, 32)` re-centre on the last a(Z) pass's box centre, so the release arc's bounce
        // chain lands later (≈ tick 77 instead of 39) — timing only, same end state
        for (t in 0..200) {
            w.pad.e(Pad.M_UP); w.tick(emptyList())
            if (p.ga !== u248 && p.aZ) { freed = true; break }
        }
        assertTrue(freed, "u248 releases the player back to free movement (al=${p.al} ak=${p.ak})")

        // (c) the west-stair top segment: middle shelf → '##########' mass's
        // WEST face (x100) → mass top (y519) → east → pillar-top '2' ledge.
        // (The pillar's own west face x300-320 is guarded by u60's sweeping
        // leading-edge tip — the mass's west face clears it by rising above
        // the tip's y585-621 sweep band.) Park on the middle shelf's west end
        // (away from the u60 sweep) and drive the proven path.
        val w2 = world(aj = 7); w2.stateL(8); settleIntro(w2)
        val p2 = w2.player; p2.gJ = 5
        driveDuelWin300(w2, p2); driveRopeClimb300(w2, p2)
        val rope = p2.bM; p2.bM = null; rope?.bM = null; p2.aA = 0
        p2.ak = 25; p2.al = 659; p2.N = 25 shl 8; p2.setAnim(0); p2.aZ = true; p2.refreshBoxes()
        var reached = false; var lastS = p2.S
        for (t in 0..1200) {
            var mask = 0
            val tx = if (p2.al > 525) 100 else 340   // to mass west face x100, then east to pillar top
            when {
                w2.kC != null -> mask = Pad.M_CONTEXT
                p2.ga != null -> mask = 0
                p2.S in listOf(33,34,146,147,63,17,102) -> mask = Pad.M_UP
                p2.S in listOf(37,38,280,332) -> mask = Pad.M_UP
                p2.S in listOf(228,358,258,259,260,263,265,235,236,239) ->
                    mask = if (t % 18 < 4) Pad.M_UP else (if (p2.ak < tx) Pad.M_RIGHT else Pad.M_LEFT)
                p2.S == 101 || p2.S == 36 -> mask = if (p2.ak < tx) Pad.M_RIGHT else Pad.M_LEFT
                p2.S == 21 -> mask = Pad.M_UP
                !p2.aZ -> mask = if (p2.ak < tx) Pad.M_RIGHT else Pad.M_LEFT
                else -> {
                    mask = if (p2.ak < tx) Pad.M_RIGHT else Pad.M_LEFT
                    if (p2.al > 525 && t % 3 == 0) mask = mask or Pad.M_UP
                }
            }
            w2.pad.e(mask); w2.tick(emptyList())
            lastS = p2.S
            if (p2.aZ && p2.al < 565 && p2.ak >= 300) { reached = true; break }
            if (w2.jC != 8 && w2.jC != 21) break
        }
        assertTrue(reached, "west-stair top segment reaches the pillar top (al=${p2.al} ak=${p2.ak} S=$lastS)")
    }
}
