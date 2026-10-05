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

class Slice69Test {
    private fun ax69At(w: Level0World, x: Int, y: Int, vararg f: Int): Entity {
        val e = Entity(69, w.clips[38])
        val rec = mutableListOf(69, 152, x, y)
        rec += f.toList()
        while (rec.size < 22) rec += 0
        e.setPositionPx(x, y)
        w.npcFsm.initAx69(e, rec.toList(), w)
        w.npcs.add(e)
        return e
    }

    private fun ax11At(w: Level0World, x: Int, y: Int, aw: Int): Entity {
        val e = Entity(11, w.clips[7])
        e.aw = aw
        e.setPositionPx(x, y)
        e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    /** clip38 W = (-38,-75,76,76) rel anchor → arithmetic-shift floor at
     *  (100,200): x∈[61,137], y∈[124,200], mid-x=99, mid-y=162. */
    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    @Test fun `init — Z0=f4 flavor, Z1=f7 link, S=f5, az=0 aA=0`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, 102)
        assertEquals(1, e.Z[0], "Z[0] = r8[4] zone flavor")
        assertEquals(102, e.Z[1], "Z[1] = r8[7] link uid")
        assertEquals(0, e.S, "S = r8[5] init anim")
        assertEquals(0, e.az); assertEquals(0, e.aA)
    }

    @Test fun `S0 Z0=0 — gB player inside zone hangs S250, e S7, af=link`() {
        val w = world()
        // uid 9999 — free of every shipped record (level-0 carries a
        // checkpoint at aw=102 which now spawns after the clipless gate
        // removal; k.q resolves record entities before test fixtures).
        val victim = ax11At(w, 160, 190, 9999)
        val e = ax69At(w, 100, 200, 0, 0, 0, 9999)
        w.player.setPositionPx(100, 163)
        w.player.setAnim(19)                              // gB member
        w.player.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(250, w.player.S, "player hangs")
        assertEquals(7, e.S, "zone arms")
        assertSame(victim, e.af, "af = k.q(Z[1]) link")
        assertSame(e, w.player.af, "player bound to zone")
        assertEquals(99, w.player.ak); assertEquals(162, w.player.al)
        assertEquals(0, w.player.ag)
    }

    @Test fun `S0 Z0=0 — non-gB player does not bind`() {
        val w = world()
        val e = ax69At(w, 100, 200, 0, 0, 0, -1)
        w.player.setPositionPx(100, 163)
        w.player.setAnim(0)                               // not in gB set
        w.player.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertNotEquals(250, w.player.S)
        assertEquals(0, e.S)
    }

    @Test fun `S0 Z0=1 — the zone catches a player whose feet are still above its mid-line`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        w.player.setAnim(19)
        // @693 `if_icmpge 801` (raw bytes, slice 412): `aS.W[3] >= mid → return`. Feet at / below
        // the mid-line → no bind (the port had this the other way round)
        w.player.setPositionPx(100, 195)
        w.player.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(0, e.S, "feet below mid: no bind")
        // feet above the mid-line — he is dropping into the zone from above → perch anim S1
        w.player.setPositionPx(100, 130)
        w.player.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(1, e.S, "feet above mid: perch anim S1")
        assertSame(e, w.player.af)
    }

    @Test fun `S1 runs the armed S7 body in the same tick (bytes @802 falls into @815)`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        e.setAnim(1)                                      // perch anim, not finished
        assertEquals(1, e.S)
        w.player.setPositionPx(300, 300)
        w.pad.commit(8256)                                // k.u(12368): RIGHT held
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(6, e.S, "Z0=1 + held pad → i(6) straight from S1")
    }

    @Test fun `preamble — ax11 victim right of zone within 40px binds + marker`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        e.setAnim(7)
        w.player.af = e                                   // bound player
        w.player.setPositionPx(100, 163)
        val victim = ax11At(w, 160, 190, 7)
        victim.av = false                                 // facing right = away
        victim.refreshBoxes()
        w.paint(e, victim)
        w.npcFsm.tickAx69(e, w, w.player)
        assertSame(victim, e.af, "victim bound")
        assertNotNull(e.ae, "hand marker spawned")
        assertEquals(152, e.ae!!.ak); assertEquals(115, e.ae!!.al)
    }

    @Test fun `preamble — victim facing the player is skipped`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        e.setAnim(7)
        w.player.af = e
        w.player.setPositionPx(100, 163)
        val victim = ax11At(w, 160, 190, 7)
        victim.av = true                                  // facing left = toward player
        victim.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertNull(e.af, "facing victim rejected (!g(aS))")
    }

    @Test fun `preamble keep-alive — victim drifting past 40px releases af`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        e.setAnim(7)
        w.player.af = e
        w.player.setPositionPx(100, 163)
        val victim = ax11At(w, 160, 190, 7)
        victim.av = false; victim.refreshBoxes()
        w.paint(e, victim)
        w.npcFsm.tickAx69(e, w, w.player)
        assertSame(victim, e.af)
        victim.setPositionPx(250, 190); victim.refreshBoxes()   // >40px out
        w.npcFsm.tickAx69(e, w, w.player)
        assertNull(e.af, "out-of-reach victim dropped (L39)")
    }

    @Test fun `S7 kill arm — eligible victim + v(65568) → leap, death anim`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)            // Z0=1 auto-eligible
        e.setAnim(7)
        w.player.af = e
        w.player.setPositionPx(100, 163)
        val victim = ax11At(w, 160, 190, 7)
        victim.av = false; victim.refreshBoxes()
        w.paint(e, victim)
        w.pad.commit(65568)                               // v() edge
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(244, w.player.S, "aS.i(244) leap")
        assertTrue(w.player.P and 64 != 0, "P|=64")
        assertEquals(162, w.player.al); assertEquals(137, w.player.ak,
            "player snaps to zone right-center")
        assertEquals(117, victim.S, "af.i(117) death anim")
        assertEquals(-1, victim.az)
        assertEquals(2, e.S, "i(2) kill windup")
    }

    @Test fun `S7 kill arm — Z0=0 requires prompt eligibility`() {
        val w = world()
        val e = ax69At(w, 100, 200, 0, 0, 0, -1)            // Z0=0 prompt mode
        e.setAnim(7)
        w.player.af = e
        w.player.setPositionPx(100, 163)
        // victim inside 40px reach but FACING the player → not eligible
        val victim = ax11At(w, 160, 190, 7)
        victim.av = true; victim.refreshBoxes()
        e.af = victim                                     // bound by link
        w.pad.commit(65568)
        w.npcFsm.tickAx69(e, w, w.player)
        assertNotEquals(244, w.player.S, "facing victim blocks the kill")
    }

    @Test fun `S2 censored tail — kBK=false → S10 + player recentered`() {
        val w = world()
        val e = ax69At(w, 100, 200, 0, 0, 0, -1)
        e.setAnim(2); finish(e)
        w.player.setPositionPx(90, 163)
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(10, e.S, "censored chain 2→10")
        assertEquals(99, w.player.ak, "player recentered on zone")
    }

    @Test fun `S4 kill-landing — p S244 → S12 + settle + af release`() {
        val w = world()
        val e = ax69At(w, 100, 200, 0, 0, 0, -1)            // Z0=0
        e.setAnim(4); e.aA = 0; finish(e)
        w.player.setAnim(244); w.player.P = w.player.P or 64
        w.player.af = e
        w.player.setPositionPx(100, 163)
        w.player.refreshBoxes()
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(12, e.S, "censored landing 4→12")
        assertEquals(0, w.player.S, "aS.i(0)")
        assertTrue(w.player.P and 64 == 0, "P&=-65")
        assertNull(w.player.af)
    }

    @Test fun `S4 aA==1 mission advance — bw=-1 bx=57 screenL(13)`() {
        val w = world()
        val e = ax69At(w, 100, 200, 0, 0, 0, -1)
        e.setAnim(4); e.aA = 1; finish(e)
        w.player.af = e
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(-1, w.kBw, "k.bw = -1")
        assertEquals(57, w.kBx, "k.bx = 57")
    }

    @Test fun `S6 carry-drift — u(8256) drifts both, w(12368) release → S7`() {
        val w = world()
        val e = ax69At(w, 100, 200, 1, 0, 0, -1)
        e.setAnim(6)
        w.player.af = e
        w.player.setPositionPx(100, 163)
        w.pad.commit(8256)                                // RIGHT held
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(1536, e.ag); assertEquals(1536, w.player.ag)
        w.pad.commit(0)                                   // released → eM edge
        w.npcFsm.tickAx69(e, w, w.player)
        assertEquals(7, e.S, "release → i(7) re-arm")
        assertEquals(0, e.ag); assertEquals(0, w.player.ag)
    }
}
