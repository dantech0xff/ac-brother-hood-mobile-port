package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Regression verdict for the m2 "respawn die-loop" report (tester saw
 *  die→YES→instant re-fail). Proves the respawn mechanism itself is sound
 *  on m2 geometry: camera snaps via C()→m(ad), the meter restores from
 *  the aY() snapshot, the checkpoint write-pos sits ≤60px above a floor,
 *  and 80 settle ticks never re-arm k.l(12) even with a live alert
 *  soldier stamped adjacent (the bf[] image restores live S — faithful
 *  to d(z2) image restore, k.java:4604/5974). */
class ReproDieLoop {
    private fun armCp(w: Level0World, cp: Level0World.Checkpoint) {
        w.player.setPositionPx(cp.ak, cp.al + 5)
        w.npcs.firstOrNull { it.ax == 2 && it.aw == cp.aw }?.let { it.P = it.P or 16 }
    }

    private fun tap(w: Level0World, x: Int, y: Int) =
        listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y),
               InputQueue.Event(1, InputQueue.Type.UP, x, y))

    @Test fun `m2 roof-checkpoint respawn does not insta-refail`() {
        val w = world(aj = 2)
        settleIntro(w)
        // every m2 checkpoint write spot has a floor ≤80px below — a
        // respawn drop can never accrue lethal op21 fall damage.
        for ((i, cp) in w.checkpoints.withIndex()) {
            var y = cp.al + 5; var depth = 0
            while (depth < 600 && w.level.collisionAtPx(cp.ak, y) == 0) {
                y += 20; depth += 20
            }
            assertTrue(depth < 600,
                "cp$i @(${cp.ak},${cp.al}) has no floor within 600px below")
        }
        // arm the roof-route checkpoint (aw458 @ (4172,760)) with an alert
        // soldier stamped adjacent — the harshest respawn case.
        val cp = w.checkpoints.first { it.aw == 458 }
        val foe = w.npcs.filter { it.ax == 11 }
            .minByOrNull { kotlin.math.abs(it.al - cp.al) + kotlin.math.abs(it.ak - cp.ak) }!!
        foe.P = foe.P or 16
        foe.setPositionPx(cp.ak + 40, cp.al + 5)
        foe.setAnim(4)
        armCp(w, cp)
        repeat(3) { w.tick(emptyList()) }
        val snap = w.checkpointSnap
        assertNotNull(snap, "checkpoint must arm")
        assertEquals(cp.ak, snap.ak)
        assertTrue(snap.x1 > 0, "snapshot carries a live meter")
        // die beside the foe (op18 melee hits → x1 0 → k.l(12))
        while (w.player.x1 > 0) {
            w.player.applyHit(18, 0, foe, w)
            w.player.gt = 0; w.iBh = 0
        }
        tickUntilFailed(w)                    // S50 plays out first (slice 379)
        assertTrue(w.failed, "KO must reach the fail screen")
        // YES → f(false) reload → checkpoint restore
        w.tick(tap(w, 200, 130))
        assertEquals(8, w.jC, "reload returns to play")
        assertEquals(snap.ak, w.player.ak, "respawn at the snapshot pos")
        assertEquals(w.kAx, w.player.x1, "a(true) refills the meter: g.e(ax) (k.java:5225)")
        assertTrue(w.player.al <= w.camY + 240,
            "camera snapped under the respawn — OOB arm cannot fire")
        // 80 settle ticks: the respawned player lands and stays alive —
        // no instant re-fail from OOB, meter, or the stamped foe.
        repeat(80) {
            w.tick(emptyList())
            assertFalse(w.failed, "tick $it re-failed: al=${w.player.al} camY=${w.camY} x1=${w.player.x1}")
        }
    }
}
