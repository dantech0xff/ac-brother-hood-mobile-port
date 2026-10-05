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

internal fun u16le(v: Int) =
    byteArrayOf((v and 0xFF).toByte(), ((v shr 8) and 0xFF).toByte())

private fun opMove(x: Int, y: Int) = byteArrayOf(21) + u16le(x) + u16le(y)

private fun opSub1(sub: Int) = byteArrayOf(37) + u16le(sub)

private fun opSub2(sub: Int, r2: Int) = byteArrayOf(38) + u16le(sub) + u16le(r2)

class Slice43bTest {


    @Test fun `op21 lerps the target once per step until the key`() {
        val w = scriptedWorld(
            scriptBlock(2, 900, scriptGroup(10, opMove(300, 200))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900; setPositionPx(100, 100) }
        w.npcs.add(t)
        e.runClaimScript(w)                    // r02=0: arm r20=10
        assertEquals(118, t.ak, "delta=(300-100)/(10-0+1)=18")
        assertEquals(109, t.al, "delta=(200-100)/11=9")
        // key10 not consumed while step < 10
        repeat(9) { e.runClaimScript(w) }      // r02=1..9
        assertTrue(e.scriptStep > 0, "group still open mid-lerp")
        e.runClaimScript(w)                    // r02=10: consume + release
        assertEquals(300, t.ak); assertEquals(200, t.al)
        assertEquals(-2, e.scriptStep, "all-done → releaseClaim cK=-2")
    }

    @Test fun `group key ahead of the step is decoded but not consumed`() {
        val w = scriptedWorld(scriptBlock(2, 900,
            scriptGroup(0, opAnim(7)), scriptGroup(50, opAnim(9))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900; setPositionPx(0, 0) }
        w.npcs.add(t)
        e.runClaimScript(w)                    // r02=0: group0 fires + consumed
        assertEquals(7, t.S)
        e.runClaimScript(w)                    // r02=1: key50 group decodes…
        assertEquals(7, t.S, "…but its op22 does not fire (key<=step gate)")
        val pc = e.scriptOps!![0]
        assertTrue(pc > 6, "PC advanced past group0 to the key50 group")
        repeat(48) { e.runClaimScript(w) }     // r02=2..49: still gated
        assertEquals(7, t.S)
        e.runClaimScript(w)                    // r02=50: fires + consumed
        assertEquals(9, t.S)
    }

    @Test fun `op22 ax11 anim-139 bumps the kill stat and plays the anim`() {
        val w = scriptedWorld(scriptBlock(2, 900,
            scriptGroup(0, opAnim(139))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900; setPositionPx(0, 0) }
        w.npcs.add(t)
        val a0 = w.kAp[0]; val a3 = w.kAp[3]
        e.runClaimScript(w)
        assertEquals(139, t.S)
        assertEquals(a0 + 1, w.kAp[0], "k.e(0,aw>0) → ap[0]++")
        assertEquals(a3 + 1, w.kAp[3], "k.o(3) → ap[3]++")
    }

    @Test fun `op13 focuses kAe on the indexed type-2 block uid`() {
        // blk0 type1 holds op13 → blk index 1; blk1 type2 uid=936.
        // blk1's group sits at key50 so the claim stays open — otherwise
        // releaseClaim's ax5 arm rewrites k.ae back to the player.
        val w = scriptedWorld(
            scriptBlock(1, 0, scriptGroup(0, byteArrayOf(13, 1, 0, 0, 0, 0))),
            scriptBlock(2, 936, scriptGroup(50, opAnim(50))))
        val e = claimer(w)
        val t = Entity(23, null).apply { aw = 936; setPositionPx(10, 10) }
        w.npcs.add(t)
        w.kZ = true; w.kAa = false
        e.runClaimScript(w)
        assertTrue(w.kAe === t, "k.ae = k.q(block1.uid)")
        assertFalse(w.kZ); assertTrue(w.kAa, "k.Z=false, k.aa=true")
    }

    @Test fun `arg-op 37 sub-switch arms cd2 and toggles iZ`() {
        // key50 tail keeps the claim open — bI() clears cd[2] on release.
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0,
                opSub1(0),                         // cd[2]=true
                opSub1(25),                        // i.z=true
                opSub1(24)),                       // i.z=false
            scriptGroup(50, opSub1(13))))          // pending: iCe=true later
        val e = claimer(w)
        e.runClaimScript(w)
        assertTrue(e.cd[2])
        assertFalse(w.iZ, "24 clears i.z after 25 set it")
        repeat(49) { e.runClaimScript(w) }
        e.runClaimScript(w)                        // r02=50: tail fires
        assertTrue(w.iCe)
    }

    @Test fun `arg-op 38 sub-switch writes iBQ iBD and player P`() {
        val w = scriptedWorld(scriptBlock(0, 0, scriptGroup(0,
            opSub2(6, 77),                         // i.bQ = 77
            opSub2(4, 1),                          // i.bD = true
            opSub2(0, 0x40))))                     // aS.P |= 0x40
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(77, w.iBQ)
        assertTrue(w.iBD)
        assertTrue(w.player.P and 0x40 != 0)
    }

    @Test fun `arg-op 34 fires only when the lead uid is absent`() {
        // [34][uid][sub]: uid present → skip entirely; absent → fire.
        val w = scriptedWorld(scriptBlock(0, 0, scriptGroup(0,
            byteArrayOf(34) + u16le(901) + u16le(14),   // absent → iCe=false
            byteArrayOf(34) + u16le(900) + u16le(13)))) // present → would set iCe
        val e = claimer(w)
        w.npcs.add(Entity(11, null).apply { aw = 900 })
        e.runClaimScript(w)
        assertFalse(w.iCe, "uid-900 op34 must NOT fire (entity exists)")
    }

    @Test fun `L341 tail zeros player velocity while the claim steps`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(50, opSub1(0))))
        val e = claimer(w)
        w.kC = e                                     // k.C == this
        w.player.ag = 100; w.player.ah = -2560
        w.player.ai = 1; w.player.aj = 2
        e.runClaimScript(w)                          // scriptStep → 1 > 0
        assertEquals(0, w.player.ag); assertEquals(0, w.player.ah)
        assertEquals(0, w.player.ai); assertEquals(0, w.player.aj)
    }

    @Test fun `cd0 early-returns and skip-input latches cd1 + sfx23`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, opSub1(0))))
        val e = claimer(w)
        e.cd[0] = true
        e.runClaimScript(w)
        assertFalse(e.cd[2], "cd[0] → the VM does not run")
        e.cd[0] = false; e.cd[2] = true
        w.pad.commit(0x20000)                          // k.v(131072) edge
        e.runClaimScript(w)
        assertTrue(e.cd[1], "skip latch arms fast-forward")
        assertTrue(23 in w.sfxLog, "k.A(23) sfx on skip")
    }

    @Test fun `self-targeting op25 on a claimPositionType block-0`() {
        // ax11 claims: block-0 type-2 + r13==0 → r14 = the claimer, and
        // the op25 latch adds cM/cN (bind-time position) to the target.
        val w = scriptedWorld(scriptBlock(2, 900,
            scriptGroup(0, byteArrayOf(25) + u16le(30) + u16le(40))))
        val e = Entity(11, null).apply { aw = 700; setPositionPx(100, 50) }
        w.npcs.add(e)
        e.bindScript(0, w)
        e.scriptKeyStep(0, w)
        e.runClaimScript(w)
        // r18 = 30+cM(100)=130, r19 = 40+cN(50)=90; r20=0 → instant move
        assertEquals(130, e.ak); assertEquals(90, e.al)
    }
}
