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

class Slice96Test {
    @Test fun `hudStep lazy ax init and meter cap`() {
        val w = world(); w.npcs.clear()
        w.kAx = 0; w.player.x1 = 90
        w.tick(emptyList())
        assertEquals(30, w.kAx, "ax==0 → ax=30 (k.java:4177)")
        assertEquals(30, w.player.x1, "g.f(ax) clamps x1 to ax (:4180)")
    }

    @Test fun `hudStep az clamp under score arm gate`() {
        val w = world(); w.npcs.clear()
        w.kAz = -5
        w.tick(emptyList())
        assertEquals(0, w.kAz)
        w.kAz = 40000
        w.tick(emptyList())
        assertEquals(32767, w.kAz)
    }

    @Test fun `stopwatch slide-in then run then out`() {
        val w = world(); w.npcs.clear()
        w.kAJ = 1; w.kAK = 0; w.kAL = 65; w.kAM = 0
        repeat(9) { w.tick(emptyList()) }         // aK>80 needs 9 (80==80 is not >)
        assertEquals(80, w.kAK, "aK +=10 → capped at 80")
        assertEquals(2, w.kAJ, "aJ 1 → 2")
        assertEquals(0, w.kAM, "aM reset on entry")
        // running: i8 = aL*1000 - aM (aM +=62/tick via ax42 arm absent →
        // stays 0 in the test world; kTimerMs tracks aL*1000)
        w.kAM = 3000
        w.tick(emptyList())
        assertEquals(62000, w.kTimerMs)
        w.kAJ = 3; w.kAM = 0
        repeat(7) { w.tick(emptyList()) }         // aK<-40 needs 7 (-40==-40 is not <)
        assertEquals(0, w.kAJ, "aJ 3 → 0 at aK<-40")
    }

    @Test fun `stopwatchText formats mm-ss-cc`() {
        val w = world()
        w.kTimerMs = 75420                 // 1m 15s 420ms
        assertEquals("1:15:42", w.stopwatchText())
    }

    @Test fun `banner aC counts down then clears aB`() {
        val w = world(); w.npcs.clear()
        w.kAB = "OBJ"; w.kAC = 3
        w.tick(emptyList()); w.tick(emptyList())
        assertEquals("OBJ", w.kAB)
        w.tick(emptyList())
        assertNull(w.kAB, "aC==0 → aB=null (k.java:4334)")
        assertEquals(0, w.kAC)
    }

    @Test fun `aP timed line clears on expiry`() {
        val w = world(); w.npcs.clear()
        w.kAP = "MSG"; w.kAO = 10        // <62 → negative next tick
        w.tick(emptyList())
        assertNull(w.kAP, "aO<0 → aP=null (k.java:4338)")
        val w2 = world(); w2.npcs.clear()
        w2.kAP = "MSG"; w2.kAO = 200
        w2.tick(emptyList())
        assertEquals("MSG", w2.kAP)
    }

    @Test fun `weaponCorner gates`() {
        val w = world(); w.npcs.clear()
        w.cm = 0
        // play state: jc8 = in-game → armed
        w.stateL(8)
        w.player.aZ = true
        assertTrue(w.weaponCornerArmed())
        // raw bytes @1092-1098 (slice 416): the gate is `g.o()Z` — grounded or aboard a vehicle — so
        // an airborne player (aZ false, nothing under him) disarms
        w.player.aZ = false; w.player.standingOn = null
        assertFalse(w.weaponCornerArmed())
        w.player.aZ = true
        // menu jc (not 8 / 21-8) disarms
        w.stateL(0)
        assertFalse(w.weaponCornerArmed())
    }

    @Test fun `weaponCornerPressed rect`() {
        val w = world()
        w.lastMoveX = 370; w.lastMoveY = 210
        assertTrue(w.weaponCornerPressed())
        w.lastMoveX = 300
        assertFalse(w.weaponCornerPressed())
    }

    @Test fun `k_at cycle lock consumed inside armed gate`() {
        val w = world(); w.npcs.clear()
        w.stateL(8); w.actionLock = 1
        w.player.aZ = true
        w.tick(emptyList())
        assertEquals(0, w.actionLock, "at==1 → 0 (k.java:4277, raw bytes @1159-1167): the lock `g.ao()` sets")
    }

    @Test fun `hudScoreText tiers`() {
        val w = world(); w.npcs.clear()
        w.kAz = 250
        assertEquals("50/200", w.hudScoreText(), "tier 2 → (az-dE[t])/(dE[t+1]-dE[t])")
        w.kAz = 850
        assertEquals("50", w.hudScoreText(), "top tier → remainder")
        w.kAj = 8
        assertNull(w.hudScoreText(), "aj>=8 gate")
    }

    @Test fun `weaponIconAnim dn lookup`() {
        val w = world()
        w.player.gI = 1
        assertEquals(10, w.weaponIconAnim())
        w.player.gI = 4
        assertEquals(9, w.weaponIconAnim(), "dn[2] for bit 2")
        w.player.gI = 8
        assertEquals(11, w.weaponIconAnim())
    }
}
