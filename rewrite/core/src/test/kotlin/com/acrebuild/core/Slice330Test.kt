package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 330 — m2 claim-script 575 (eH index 19), the script-346 QTE
 * **fail** path for the posted-perch trio.
 *
 * Chain proven by scripts.bin byte decode (level2):
 *  - Watcher aw345 `aG=346` binds script 346 when the player crosses
 *    `W=[3662,154,3728,358]` on the rooftop approach.
 *  - Script 346 block0: op107 arms a one-button prompt (mask 32→65568)
 *    at key=105; op108 at key=121 branches pass=0 (continue) /
 *    fail=uid 552. Pressing context in the window = pass.
 *  - op112 at key=324 arms a 3-choice prompt (masks 64/1024/1024);
 *    op113 at key=341 resolves: all pressed → pass-uid 0 (continue);
 *    `cc[4]<cc[0]` → **fail → `bindScript(k.s(575))` = index 19**.
 *  - Script 575 = the reset choreography: type-2 blocks lerp aw312/313
 *    back toward perch coords with `op22` anim 152 (posted) and `op24`
 *    flag writes; aw86/aw304 repositioned too.
 *
 * Verified live: binding, pass-arm, fail-arm, the 575 descent lerps.
 */
class Slice330Test {

    private fun driveTo(w: Level0World, cond: () -> Boolean, maxT: Int = 6000): Int {
        var t = 0
        while (t++ < maxT && !cond()) {
            if (w.jC == 21) {                       // op105 dialogs — accept
                w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                w.tick(emptyList()); continue
            }
            w.tick(emptyList())
        }
        return t
    }

    @Test
    fun `watcher aw345 binds script 346 on rooftop approach`() {
        val w = world(aj = 2)
        val w345 = w.npcs.first { it.aw == 345 }
        assertEquals(346, w345.aG)
        // enter the watcher zone (rooftop row y~256)
        w.player.setPositionPx(3695, 256)
        driveTo(w, { w345.claimActive() }, 300)
        assertTrue(w345.claimActive(), "aw345 should hold the claim")
        assertTrue(w345.ca >= 0, "script 346 bound")
    }

    @Test
    fun `passed prompt + failed 3-choice binds script 575 (index 19)`() {
        val w = world(aj = 2)
        val w345 = w.npcs.first { it.aw == 345 }
        w.player.setPositionPx(3695, 256)

        var t = 0
        while (t++ < 6000 && w345.ca != 19) {
            if (w.jC == 21) {                     // op105 dialogs — accept
                w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                w.tick(emptyList()); continue
            }
            // the op107/108 one-button prompt (~steps 105-121) — press
            // context so the script passes (fail → uid 552 instead)
            if (w345.ca == w.kSIndex(346) && w345.scriptStep in 100..120)
                w.pad.e(Pad.M_CONTEXT)
            else w.pad.releaseFlush()            // release held bit outside window
            w.tick(emptyList())
        }
        assertEquals(19, w345.ca,
            "failed 3-choice at key=341 must bind script-uid 575 (index 19), got ca=${w345.ca}")
    }

    @Test
    fun `script 575 lerps the trio back to posted-perch state`() {
        val w = world(aj = 2)
        val w345 = w.npcs.first { it.aw == 345 }
        val s312 = w.npcs.first { it.aw == 312 }
        val s313 = w.npcs.first { it.aw == 313 }
        val start312 = s312.ak to s312.al
        val start313 = s313.ak to s313.al
        w.player.setPositionPx(3695, 256)

        var t = 0
        while (t++ < 6000 && w345.ca != 19) {
            if (w.jC == 21) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush(); w.tick(emptyList()); continue }
            if (w345.ca == w.kSIndex(346) && w345.scriptStep in 100..120)
                w.pad.e(Pad.M_CONTEXT)
            else w.pad.releaseFlush()
            w.tick(emptyList())
        }
        assertEquals(19, w345.ca)

        // run 575's blocks: op21 lerps + op22 anim-152 + op24 flag writes
        repeat(400) { w.tick(emptyList()) }
        assertEquals(152, s312.S, "aw312 returns to posted-perch anim")
        assertEquals(152, s313.S, "aw313 returns to posted-perch anim")
        // lerp targets decoded from block payloads: aw312 → ~(4041,797),
        // aw313 → ~(4073,800) — near their original perches.
        assertTrue(s312.ak in 3900..4100 && s312.al in 760..830,
            "aw312 lerped toward (4041,797): got (${s312.ak},${s312.al})")
        assertTrue(s313.ak in 3950..4150 && s313.al in 760..830,
            "aw313 lerped toward (4073,800): got (${s313.ak},${s313.al})")
    }

    @Test
    fun `failing the op108 prompt binds script 552 instead`() {
        val w = world(aj = 2)
        val w345 = w.npcs.first { it.aw == 345 }
        w.player.setPositionPx(3695, 256)
        // never press — op108 at key=121 fails → bind uid 552
        var t = 0
        while (t++ < 3000 && w345.ca != w.kSIndex(552) && w345.ca != 19) {
            if (w.jC == 21) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush(); w.tick(emptyList()); continue }
            w.tick(emptyList())
        }
        assertEquals(w.kSIndex(552), w345.ca,
            "unpressed one-button prompt should take the fail branch to uid 552, got ca=${w345.ca}")
        assertNotEquals(19, w345.ca)
    }
}
