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

private fun op101(v: Int) = byteArrayOf(101) + u16le(v)

private fun op102(id: Int, flag: Int) = byteArrayOf(102) + u16le(id) + u16le(flag)

private fun op104(v: Int) = byteArrayOf(104) + u16le(v)

private fun op109(x0: Int, y0: Int, x1: Int, y1: Int) =
    byteArrayOf(109) + u16le(x0) + u16le(y0) + u16le(x1) + u16le(y1)

private fun op110(a: Int, b: Int) = byteArrayOf(110) + u16le(a) + u16le(b)

private fun op111(s: Int, x: Int, y: Int, f: Int, az: Int) =
    byteArrayOf(111) + u16le(s) + u16le(x) + u16le(y) +
        byteArrayOf(f.toByte()) + u16le(az)

private fun op112(a: Int, b: Int, c: Int) =
    byteArrayOf(112) + u16le(a) + u16le(b) + u16le(c)

private fun op113(pass: Int, fail: Int) =
    byteArrayOf(113) + u16le(pass) + u16le(fail)

private fun op114(str: Int, cd: Int) =
    byteArrayOf(114) + u16le(str) + u16le(cd)

class Slice43cTest {

    // -- prologue (i.java:20060): only 108/113 run bodies before key ---

    @Test fun `non-108 ops are consumed but only fire at step==key`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(10, op101(5))))
        val e = claimer(w)
        e.runClaimScript(w)                    // r02=0: consumed, not fired
        assertEquals(-1, e.cP, "op101 must not write cP before its key")
        repeat(9) { e.runClaimScript(w) }      // r02=1..9
        assertEquals(-1, e.cP)
        e.runClaimScript(w)                    // r02=10 = key: fires
        assertEquals(5, e.cP)
    }

    // -- op100 (L12): uid/sub dispatch + the r04 arg-switch -------------

    @Test fun `op100 sub1 arg-switch 0 arms P32+P128 and drops ae link`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op100(900, 1, 0))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900; P = 16 }
        t.ae = Entity(11, null)
        w.npcs.add(t)
        e.runClaimScript(w)
        assertTrue(t.P and 32 != 0 && t.P and 128 != 0, "r04=0 → P|=32|128")
        assertEquals(0, t.P and 16, "r04=0 → P&=~16")
        assertNull(t.ae, "r04=0 → G() drops ae")
    }

    @Test fun `op100 sub0 skips when arg is 0, sets az = arg otherwise (@256 joins @748)`() {
        // slice 394: the sub-0 case is `arg == 0 → return; else az = arg` —
        // the same store as sub 5. (The port ran the sub-1 arg switch here;
        // the 38 shipped `(0, arg)` ops are z-order values 50-300 / -5 / -1.)
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op100(0, 0, 0)), scriptGroup(1, op100(0, 0, 5))))
        val e = claimer(w)
        val before = e.P; e.az = 33
        e.runClaimScript(w)                    // sub0 arg0 → no-op
        assertEquals(before, e.P); assertEquals(33, e.az)
        e.runClaimScript(w)                    // sub0 arg5 → az = 5
        assertEquals(5, e.az, "sub0 arg5 → az = 5")
        assertEquals(before, e.P, "and NOT the arg-5 P^1024 toggle")
    }

    @Test fun `op100 sub2 removes the uid entity`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op100(900, 2, 0))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900 }
        w.npcs.add(t)
        e.runClaimScript(w)
        assertTrue(t in w.pendingRemove, "sub2 → k.c(r05)")
    }

    @Test fun `op100 sub4 az-flag and sub5 az-write`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op100(900, 4, 77)), scriptGroup(1, op100(0, 5, 55))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900 }
        w.npcs.add(t)
        e.runClaimScript(w)
        assertTrue(w.kAb, "sub4 → k.ab = true")
        assertEquals(0, t.az, "sub4 is `k.ab = true` ONLY (@741 returns before @748)")
        e.runClaimScript(w)
        assertEquals(55, e.az, "sub5 uid0 → this.az = r04")
    }

    @Test fun `op100 arg4 clears P512 and zeroes velocity`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op100(900, 1, 4))))
        val e = claimer(w)
        val t = Entity(11, null).apply {
            aw = 900; P = 512 + 32; ah = 100; ag = 50; ai = 25; aj = 10
        }
        w.npcs.add(t)
        e.runClaimScript(w)
        assertEquals(0, t.P and 512, "r04=4 → P&=~512")
        assertEquals(0, t.ah); assertEquals(0, t.ag)
    }

    // -- op101/op102/op104 ---------------------------------------------

    @Test fun `op101 stores cP which releaseClaim rebinds`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op101(7), opAnim(9))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(7, e.cP)
        w.kC = e                                    // cP handoff needs kC
        e.releaseClaim(w)
        assertEquals(-1, e.cP, "cP consumed by the bI tail")
        assertEquals(0, e.ca, "h/k(k.s(7)) → script idx 0")
        assertEquals(0, e.scriptStep, "k() reset the step")
    }

    @Test fun `op102 plays sfx either way — kz and kA share the body`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op102(33, 0), op102(44, 1))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(listOf(33, 44), w.sfxLog)
    }

    @Test fun `op104 writes the screen-transition statics`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op104(-5)), scriptGroup(1, op104(3))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(5, w.kCO); assertEquals(false, w.kCP)
        e.runClaimScript(w)
        assertEquals(3, w.kCO); assertEquals(true, w.kCP)
    }

    // -- op105 dialog (L76) ---------------------------------------------

    @Test fun `op105 arms the dialog modal and halts the script`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op105(3, 42, 1))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertTrue(e.cd[0], "cd[0]=true halt")
        assertTrue(w.dialogModal, "k.b accept → k.l(21)")
        assertEquals(1, w.bO); assertEquals(3, w.bN0); assertEquals(42, w.dialogLine)
        // case-21 u==9 semantics (bytecode 3466-3632 — the jadx
        // linearization hides it): a press edge while typing only
        // forces `bT=-1` (reveal); a press once revealed falls into
        // the SHARED advance arm `x=48; D(v+1)` (3604-3629) reached
        // via 3568-3581 (`v(65568) && u!=8 → 3604`). With v+1==w the
        // terminal `v==w && u==9 → C.Z(); l(8)` fires the same tick.
        w.autoDismissDialog = false
        w.kC = e
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertTrue(w.dialogModal, "press while typing reveals, not dismisses")
        assertEquals(-1, w.dlgBT)
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertFalse(w.dialogModal,
            "revealed press → D(v+1): v 0→1=w → C.Z(); l(8) (:3604/:985)")
        assertEquals(1, w.dlgV)
        assertFalse(e.cd[0], "Z() resumed the claim")
        // the same press does not leak a gameplay edge
        assertEquals(0, w.pad.edge)
    }

    // -- op106 (L84) ----------------------------------------------------

    @Test fun `op106 writes the dialog-box cQ on cT`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op106(0, 11, 22, 33, 3))))
        val e = claimer(w)
        e.runClaimScript(w)
        val q = e.cT!!.cQ!!
        assertEquals(11, q[5]); assertEquals(22, q[6]); assertEquals(-1, q[2])
        assertEquals(33, q[3]); assertEquals(1, q[8], "r016=3 → q[8]=3-2")
        assertEquals(-1, q[7], "r012=0 → cQ[7] keeps its -1 init")
        assertEquals(1, q[9], "r016>1 → q[9]=1")
    }

    // -- op107 (L106) ---------------------------------------------------

    @Test fun `op107 normalizes the key mask and spawns the prompt`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op107(32))))
        w.cm = 0                                             // unmounted arm
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(65568, e.cb!![0], "raw 32 → pad mask 65568")
        assertEquals(5, e.cb!![2], "bit position of 32")
        assertEquals(0, e.cb!![1])
        val pr = Entity.scriptPrompts[0]!!
        assertEquals(74, pr.clipIdx, "unmounted → touch-art clip74")
        assertEquals(0, pr.e)   // setState(0,-1) writes the frame slot
    }

    @Test fun `op107 mounted mode uses key-art clip9 + ct frames`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op107(4))))
        w.cm = 1
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(16388, e.cb!![0]); assertEquals(2, e.cb!![2])
        val pr = Entity.scriptPrompts[0]!!
        assertEquals(9, pr.clipIdx)
        assertEquals(Entity.CT[2], pr.e, "ct[cb[2]] prompt frame")
        w.cm = 0
    }

    // -- op108 single-button QTE (L133) ---------------------------------

    @Test fun `op108 press sets cb1 and the key branches to pass`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op107(32)),
            scriptGroup(10, op108(7, 0))))
        val e = claimer(w)
        e.runClaimScript(w)                     // op107 arms cb[0]=65568
        w.pad.edge = 65568
        repeat(10) { e.runClaimScript(w) }      // polls r02=1..10
        // step10 = key: the recorded press (cb[1]==1) && pass-uid>0 →
        // h/k(k.s(7)) rebind + return -1 (cb re-alloc'd back to [1]=-1)
        assertEquals(0, e.ca, "pass branch rebinds script idx 0 (uid7)")
        assertEquals(0, e.scriptStep)
        assertEquals(-1, e.cb!![1], "k() re-alloc'd cb after the branch")
    }

    @Test fun `op108 at key with no press branches to fail`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op107(32)),
            scriptGroup(5, op108(0, 7))))
        val e = claimer(w)
        repeat(5) { e.runClaimScript(w) }       // polls: cb[1] stays 0
        e.runClaimScript(w)                     // step5 = key: fail branch
        // fail uid → h/k(k.s(7)) rebind; k() re-alloc'd cb ([1]=-1)
        assertEquals(-1, e.cb!![1])
        assertEquals(0, e.scriptStep, "h/k rebind ran on the fail uid")
    }

    // -- op109/op110 carrier track (L184/L196) ---------------------------

    @Test fun `op109 fills cf and arms cd9 only at the key`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(5, op109(0, 0, 10, 5))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertNull(e.cf, "op109's body only runs at step==key")
        assertFalse(e.cd[9])
        repeat(4) { e.runClaimScript(w) }       // steps 1..4 still not key
        assertNull(e.cf)
        e.runClaimScript(w)                     // step5 = key: fills + arms
        assertEquals(0, e.cf!![0]); assertEquals(0, e.cf!![1])
        assertEquals(10, e.cf!![2]); assertEquals(5, e.cf!![3])
        assertTrue(e.cf!![4] != 0, "cf[4] = atan2 track angle")
        assertTrue(e.cd[9])
    }

    @Test fun `op110 binds the cg-ch pair and arms cd9 at key`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(3, op110(900, 901))))
        val e = claimer(w)
        val a = Entity(11, null).apply { aw = 900 }
        val b = Entity(11, null).apply { aw = 901 }
        w.npcs.add(a); w.npcs.add(b)
        e.runClaimScript(w)
        assertNull(e.cg, "op110's body only runs at step==key")
        assertFalse(e.cd[9])
        repeat(2) { e.runClaimScript(w) }
        assertNull(e.cg)
        e.runClaimScript(w)                     // step3 = key
        assertSame(a, e.cg); assertSame(b, e.ch)
        assertTrue(e.cd[9])
    }

    // -- op111 (L203) ----------------------------------------------------

    @Test fun `op111 spawns the ax8 param projectile only when kBK`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op111(7, 300, 200, 1, 99))))
        val e = claimer(w)
        e.runClaimScript(w)                     // kBK=false → nothing
        assertTrue(w.pendingInsert.isEmpty())
        w.kBK = true
        val e2 = claimer(w)
        e2.runClaimScript(w)
        assertEquals(1, w.pendingInsert.size)
        val p = w.pendingInsert[0]
        assertEquals(8, p.ax)
        assertTrue(p.P and 512 != 0, "param spawn P|=512")
        assertEquals(300, p.ak); assertEquals(200, p.al); assertEquals(99, p.az)
        assertTrue(p.av, "facing u8>0 → av")
    }

    // -- op112/op113 sequential choices (L209/L238) ----------------------

    @Test fun `op112 filters choices 0-9 and arms prompts + cc`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op112(2, 5, 99))))
        val e = claimer(w)
        e.runClaimScript(w)
        val c = e.cc!!
        assertEquals(2, c[0], "99 filtered out")
        assertEquals(2, c[1]); assertEquals(5, c[2])
        assertEquals(0, c[4])
        assertNotNull(Entity.scriptPrompts[0]); assertNotNull(Entity.scriptPrompts[1])
        assertNull(Entity.scriptPrompts[2])
    }

    @Test fun `op113 walks choices in order then branches at key`() {
        val w = scriptedWorld(scriptBlock(0, 0,
            scriptGroup(0, op112(1, 2, 10)),   // 10 out of range → 2 choices
            scriptGroup(10, op113(7, 0))))
        val e = claimer(w)
        e.runClaimScript(w)                     // op112: cc=[2][1,2]
        w.pad.edge = 1 shl 1                    // 1<<cc[1] = first choice
        e.runClaimScript(w)
        assertEquals(1, e.cc!![4])
        w.pad.edge = 1 shl 2                    // 1<<cc[2] = second choice
        e.runClaimScript(w)
        assertEquals(2, e.cc!![4], "both choices pressed in order")
        w.pad.edge = 0
        repeat(8) { e.runClaimScript(w) }       // steps to the key
        // step10 = key: cc[4]==cc[0] → pass uid7 → rebind + cc=null
        assertNull(e.cc)
        assertEquals(0, e.scriptStep)
    }

    // -- op114 (L299) ----------------------------------------------------

    @Test fun `op114 writes the HUD objective line and countdown`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op114(9, 300))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertEquals(300, w.kAO)
        // kAP = levelString(1+k.aj, 9) — empty table → null, write still ran
        assertNull(w.kAP)
    }
}
