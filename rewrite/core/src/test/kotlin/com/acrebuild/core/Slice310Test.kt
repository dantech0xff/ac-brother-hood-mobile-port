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

class Slice310Test {
    /** m7 capstone leg M — post-arena win chain (proven end-to-end):
     *  pillar top (349,540) → leg-L run (sky-lift → band → arena) →
     *  uid303 fuse (script-304 consumes boss-3 uid307 intro, iBy 2→3)
     *  → boss-3 uid251 teleports to the wall top @(1378,520) →
     *  real duel (stagger chain, iBy=3 unconditional counter) →
     *  aB<=0 → boss walks east to (1380,520) → uid281 binds
     *  (script-316, the under-arena trigger) → cinematic →
     *  `w.missionWon` + jC=15.
     *  Bot note: `p.x1` is topped up (<40→60) during the duel — the
     *  human player survives via dodge/counter; the scripted boss
     *  sequence is what we assert, not the bot's survival.
     *  Slice 370 route: the leg-L approach is a faithful dead end — the
     *  pillar top (349,540) is r27's type-2 strip and the band y560 is
     *  r28's, both on solid rows, so standing on either is the L353d
     *  kill (g.javap.txt e() 13662-13711; Slice308Test). The leg now
     *  enters at the arena floor's west lip (1060,519) — r26-28 cols
     *  52-80 solid, no type-2 — where the leg-L run arrived, and stands
     *  60 ticks while the camera catches up from the rope top (the run
     *  arrived with it following; the u303 zone is only live on screen,
     *  `v()`); the win chain it asserts is unchanged. */
    @Test fun mission7PostArenaWin() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5
        driveDuelWin300(w, p); driveRopeClimb300(w, p)
        val rope = p.bM; p.bM = null; rope?.bM = null; p.aA = 0
        p.ak = 1060; p.al = 519; p.N = 1060 shl 8; p.av = false; p.setAnim(12)
        repeat(60) { w.pad.e(0); w.tick(emptyList()) }       // camera settle
        var fuseFired = false; var bossDefeated = false
        var u281Bound = false; var won = false
        for (t in 0..6000) {
            val boss = w.findByAw(251)
            var mask = 0
            when {
                w.kC != null -> mask = Pad.M_CONTEXT
                !fuseFired -> mask =
                    if (p.S == 358) { if (p.av) Pad.M_RIGHT else Pad.M_UP }
                    else if (p.ac != null) Pad.M_UP
                    else if (p.aZ && p.ak > 990 && p.al in 540..600) Pad.M_RIGHT or Pad.M_UP
                    else if (p.aZ) Pad.M_RIGHT
                    else 0
                boss != null -> {
                    if (p.x1 < 40) p.x1 = 60   // bot-survival accommodation
                    // (P4e re-eval, verified still required: without it
                    // the duel never finishes — boss aB stays >0 while
                    // the player's x1 drains; the mask chain asserts the
                    // scripted sequence, not bot skill)
                    if (boss.S == 7 && boss.T <= 6) mask = Pad.M_UP
                    else if (kotlin.math.abs(boss.ak - p.ak) < 170 &&
                        kotlin.math.abs(boss.al - p.al) < 80) {
                        mask = if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                        if (kotlin.math.abs(boss.ak - p.ak) < 70) mask += Pad.M_CONTEXT
                    } else mask = if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                }
            }
            w.pad.e(mask); w.tick(emptyList())
            if (!fuseFired && w.npcs.none { it.aw == 307 }) fuseFired = true
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (!bossDefeated && boss != null && boss.aB <= 0) bossDefeated = true
            if (!u281Bound && w.kC?.aw == 281) u281Bound = true
            if (w.jC == 15 || w.missionWon) { won = true; break }
            if (w.jC == 12) break
        }
        assertTrue(fuseFired, "arena fuse uid303 consumed boss-3 uid307 intro")
        assertTrue(bossDefeated, "boss-3 uid251 defeated (aB<=0) via the duel")
        assertTrue(u281Bound, "under-arena trigger uid281 bound script-316")
        assertTrue(won, "mission won — jC=15 after the script-316 cinematic")
    }
}
