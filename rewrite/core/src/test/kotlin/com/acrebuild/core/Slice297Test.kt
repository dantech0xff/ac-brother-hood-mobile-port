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

class Slice297Test {

    /** m7 leg A — ax37 scroll-holder survives a level reload (zombie
     *  regression). The player spawns at (850,1920) inside uid41's zone
     *  [810,1641,1890,1961] → it claims k.ah → bound writes arm
     *  R/S/T/U. `resetLevel(false)` rebuilds `scrollTriggers` with fresh
     *  instances; a stale `scrollHolder` (pre-fix) then blocked every
     *  claim via the mode-1 lock → `k.ah==null` → `k.m()`'s `k.n()`
     *  zeroed the bounds every tick → the arena clamp teleported the
     *  boss to ~-7. `kN()` now releases `scrollHolder` with `k.ah`. */
    @Test fun mission7ScrollHolderReload() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        repeat(30) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()) }
        assertTrue(w.boundMinX != 0 || w.boundMaxX != 0,
            "bounds armed pre-reload: [${w.boundMinX},${w.boundMaxX}]")
        // resetLevel(false) → a(false) → loadMission → loadPackI:
        // the fail-retry path — rebuilds `scrollTriggers` with fresh
        // instances (the zombie trigger).
        w.resetLevel(false)
        settleIntro(w)
        // record spawn (579,1740) → walk back inside uid41 (≥830).
        repeat(80) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()) }
        assertTrue(w.boundMinX != 0 || w.boundMaxX != 0,
            "bounds armed post-reload (zombie holder leaves them 0): " +
            "[${w.boundMinX},${w.boundMaxX}] p@(${w.player.ak},${w.player.al})")
    }

    /** m7 leg B — the ax29 duel runs without the arena-clamp teleport
     *  artifact: uid251@(1324,1920) engages (iBy=1 native, kAU armed),
     *  takes real damage, and `ak` never drops below -50 — the clamp on
     *  `W[2] >= r13=0` (unset bounds) used to slam it to ~-7 every
     *  chase cycle. Death/reload mid-fight must not re-zombie the
     *  holder either (assert bounds re-arm after respawn). */
    @Test fun mission7BossArenaClamp() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var sawEngage = false; var sawBossHit = false
        var sawBoundsAfterDeath = false
        var respawns = 0
        for (t in 0..3000) {
            val boss = w.npcs.firstOrNull { it.ax == 29 }
            val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 29) && it.aB > 0 && it.S != 139 &&
                    kotlin.math.abs(it.ak - p.ak) < 130 && kotlin.math.abs(it.al - p.al) < 90
            }
            var mask = chaseMask297(p, w)
            if (foe != null && kotlin.math.abs(foe.ak - p.ak) < 70)
                mask = if (foe.ak < p.ak) Pad.M_LEFT + Pad.M_CONTEXT else Pad.M_RIGHT + Pad.M_CONTEXT
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (boss != null) {
                assertTrue(boss.ak > -50,
                    "boss teleported via unset bounds: ak=${boss.ak} at t=$t")
                if (boss.S !in intArrayOf(0)) sawEngage = true
                if (boss.aB < 800) sawBossHit = true
            }
            if (w.jC == 15) break
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                respawns++
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                // post-respawn: settle the intro replay, then walk back
                // into uid41's zone and prove the holder re-arms (the
                // zombie fix's whole point).
                settleIntro(w)
                repeat(80) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()) }
                if (w.boundMinX != 0 || w.boundMaxX != 0) sawBoundsAfterDeath = true
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(sawEngage, "boss never left S0 — duel never started")
        assertTrue(sawBossHit, "boss took no damage — duel never engaged")
        if (respawns > 0) assertTrue(sawBoundsAfterDeath,
            "bounds never re-armed after reload — holder zombied")
    }

    /** m7 leg C — the iBy=1 duel phase-win: stagger the boss to
     *  `aB<=300` → it retreats S13 → the ax10-S55 zone uid306 anchors it
     *  → S25→S26 (block) — press CONTEXT (65568) while overlapping →
     *  `k.q(Z[1]=280)` binds ax5 uid280 → script 305 sets `by=2` → aP()
     *  returns early → the boss goes dormant and the climb route opens.
     *  (iBy=1 cannot die by hits — the heal cycle is the design; the
     *  phase win is the S26 counter-press, proven i.java:8540-8564 +
     *  op10 `by=i21, aU.aB=300` i.java:18287.) */
    @Test fun mission7BossPhaseWin() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var sawGrabQte = false; var sawHeal = false; var sawPhaseWin = false
        var minBossAb = 800
        for (t in 0..12000) {
            val boss = w.npcs.firstOrNull { it.ax == 29 }
            if (w.iBy == 2) { sawPhaseWin = true; break }
            if (boss != null) {
                if (boss.S == 7) sawGrabQte = true
                if (boss.S == 25 || boss.S == 26 || boss.S == 27) sawHeal = true
                if (boss.aB < minBossAb) minBossAb = boss.aB
            }
            var mask = chaseMask297(p, w)
            if (boss != null && boss.aB > 0 && boss.S != 139 &&
                !(boss.S == 7 && boss.T <= 6)) {
                if (boss.S == 26) {
                    // S26 block/counter window — walk INTO the boss and
                    // press CONTEXT; the ax5 bind needs e.W ∩ p.W/X.
                    mask = (if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT) + Pad.M_CONTEXT
                } else if (w.kC != null && w.kC!!.aw == 280 &&
                    (boss.S == 23 || boss.S == 25 || boss.S == 27)) {
                    // Script-305 QTE: prompt-1 = CONTEXT (steps 5-20),
                    // prompt-2 = LEFT (steps 58-81) — hold LEFT+CONTEXT
                    // through the S23 heal-outro window.
                    mask = Pad.M_LEFT + Pad.M_CONTEXT
                } else if (kotlin.math.abs(boss.ak - p.ak) < 170 &&
                    kotlin.math.abs(boss.al - p.al) < 60) {
                    mask = if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                    if (kotlin.math.abs(boss.ak - p.ak) < 70) mask += Pad.M_CONTEXT
                }
                bossDodge297(p, w, boss)?.let { mask = it }       // slice 410
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (w.jC == 15) break
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(sawGrabQte, "boss never entered the S7 grab-QTE")
        assertTrue(sawHeal,
            "boss never entered the S25/26/27 heal chain — aB floor $minBossAb")
        assertTrue(sawPhaseWin, "by never advanced to 2 — aB floor $minBossAb")
    }
}
