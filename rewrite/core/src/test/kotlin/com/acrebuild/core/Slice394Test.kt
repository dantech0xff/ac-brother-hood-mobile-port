package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 394 — the claim-script VM `i.aa()` and its op decoder `i.a(I[BIII)I`
 * re-read from the bytecode (i.javap `aa()` @0-3010, `a(I[BIII)I` @0-3335).
 * Everything else in the two methods (the 4-byte move/camera ops, op13, the
 * anim/mask ops, the lerp and its carry/follower scan, the group-consume
 * rule, the arg-op sub-switches, ops 101-107/109-112/114, `bI`/`bJ`/`h`/`k`/
 * `b`/`O`) matched; the shipped scripts' op13 groups, op-length table and
 * `eI` were checked against the VM's consumption as well.
 *
 * Fixed:
 * - **op37 arg 1** (`k.l(15)`): `if (k.aj != 7) skip; k.o(0)` — the extra
 *   `ap[0]` tick belongs to the FINAL mission; the port had the gate inverted.
 * - **op22/32** anim 139 on an ax11: `k.e(0, aw)` counts THIS entity's uid
 *   (the script holder, `aload_0 getfield aw` @884), not the target's.
 * - **op100 sub 0** (38 shipped `(0, arg)` ops): `arg != 0 → az = arg` (@256
 *   joins sub 5 at @748). The port ran the sub-1 arg switch, so every z-order
 *   op did nothing (and `(0, 1)` / `(0, 5)` did the wrong thing).
 *   **sub 4** is `k.ab = true` only.
 * - **op108 / op113** (the QTE prompts): ACCEPT = the armed key, or (touch
 *   mode) a press INSIDE the 70x70 prompt rect; REJECT = mounted `k.v(1020)`
 *   or, touch mode, `k.j()` — a press anywhere else in the play area. The
 *   port had `k.j()` in the accept set: every tap passed.
 */
class Slice394Test {
    private fun u16le(v: Int) =
        byteArrayOf((v and 0xFF).toByte(), ((v shr 8) and 0xFF).toByte())

    private fun group(key: Int, vararg ops: ByteArray): ByteArray {
        val g = ByteArray(3 + ops.sumOf { it.size })
        var p = 0
        u16le(key).copyInto(g, p); p += 2
        g[p++] = ops.size.toByte()
        for (o in ops) { o.copyInto(g, p); p += o.size }
        return g
    }

    private fun block(type: Int, uid: Int, vararg groups: ByteArray): ByteArray {
        val hdr = if (type == 2 || type == 3) 6 else 4
        val b = ByteArray(hdr + groups.sumOf { it.size } + 2)
        var p = 0
        b[p++] = type.toByte(); b[p++] = 0
        if (type == 2 || type == 3) { u16le(uid).copyInto(b, p); p += 2 }
        u16le(groups.size).copyInto(b, p); p += 2
        for (g in groups) { g.copyInto(b, p); p += g.size }
        u16le(hdr).copyInto(b, p)
        return b
    }

    private fun scripted(aj: Int = 0, vararg blocks: ByteArray): Level0World {
        val base = world(aj = aj)
        val tables = ScriptTables(
            eH = intArrayOf(7),
            by = arrayOf(arrayOf(*blocks)),
            bz = arrayOf(IntArray(blocks.size) { i ->
                val t = blocks[i][0].toInt() and 0xFF
                if (t == 2 || t == 3) 6 else 4
            }),
        )
        return Level0World(base.level, base.clips, DeterministicRandom(1L),
            scripts = tables, aj = aj).also { it.npcs.clear() }
    }

    private fun claimer(w: Level0World, aw: Int = 700): Entity {
        val e = Entity(5, null)
        e.aw = aw
        w.npcs.add(e)
        e.bindScript(0, w)
        e.scriptKeyStep(0, w)
        return e
    }

    private fun op37(sub: Int) = byteArrayOf(37) + u16le(sub)
    private fun op22(anim: Int) = byteArrayOf(22) + u16le(anim)
    private fun op100(uid: Int, sub: Int, arg: Int) =
        byteArrayOf(100) + u16le(uid) + u16le(sub) + u16le(arg)
    private fun op107(mask: Int) = byteArrayOf(107) + u16le(mask)
    private fun op108(pass: Int, fail: Int) =
        byteArrayOf(108) + u16le(pass) + u16le(fail)
    private fun op112(a: Int, b: Int, c: Int) =
        byteArrayOf(112) + u16le(a) + u16le(b) + u16le(c)
    private fun op113(pass: Int, fail: Int) =
        byteArrayOf(113) + u16le(pass) + u16le(fail)

    // ------------------------------------------------------------ op37 arg 1
    @Test fun `op37 arg1 completes the mission and ticks ap0 on the final mission only`() {
        for (aj in 0..7) {
            val w = scripted(aj, block(0, 0, group(0, op37(1))))
            val e = claimer(w)
            val before = w.kAp[0]
            e.runClaimScript(w)
            assertTrue(w.jC != 8, "aj$aj k.l(15) left the play state (jC=${w.jC})")
            assertEquals(if (aj == 7) before + 1 else before, w.kAp[0],
                "aj$aj: `if (k.aj != 7) skip; k.o(0)` (@1419-1428)")
        }
    }

    // ------------------------------------------------------------ op22
    @Test fun `op22 anim 139 on an ax11 counts the script holder's uid not the target's`() {
        val w = scripted(0, block(2, 900, group(0, op22(139))))
        val e = claimer(w, aw = 0)                       // holder uid 0: k.e gate (uid > 0) fails
        val t = Entity(11, null).apply { aw = 900; setPositionPx(0, 0) }
        w.npcs.add(t)
        val a0 = w.kAp[0]; val a3 = w.kAp[3]
        e.runClaimScript(w)
        assertEquals(139, t.S)
        assertEquals(a0, w.kAp[0], "k.e(0, this.aw = 0) counts nothing")
        assertEquals(a3 + 1, w.kAp[3], "k.o(3) still ticks")
        val w2 = scripted(0, block(2, 900, group(0, op22(139))))
        val e2 = claimer(w2, aw = 700)
        w2.npcs.add(Entity(11, null).apply { aw = 900; setPositionPx(0, 0) })
        val b0 = w2.kAp[0]
        e2.runClaimScript(w2)
        assertEquals(b0 + 1, w2.kAp[0], "holder uid 700 > 0 counts")
    }

    // ------------------------------------------------------------ op100
    @Test fun `op100 sub0 is a z-order store and sub4 only arms k_ab`() {
        val w = scripted(0, block(0, 0,
            group(0, op100(900, 0, 99)), group(1, op100(900, 0, 0)),
            group(2, op100(900, 0, 65531)), group(3, op100(900, 4, 77))))
        val e = claimer(w)
        val t = Entity(11, null).apply { aw = 900; az = 11; P = 16 }
        w.npcs.add(t)
        e.runClaimScript(w)
        assertEquals(99, t.az, "(0, 99): az = arg"); assertEquals(16, t.P, "no arg-switch effect")
        e.runClaimScript(w)
        assertEquals(99, t.az, "(0, 0): untouched")
        e.runClaimScript(w)
        assertEquals(-5, t.az, "(0, 65531): the signed short")
        e.runClaimScript(w)
        assertTrue(w.kAb, "sub4: k.ab = true")
        assertEquals(-5, t.az, "…and nothing else")
    }

    // ------------------------------------------------------------ op108
    private fun armedPrompt(w: Level0World, e: Entity) {
        e.runClaimScript(w)                              // group 0: op107 arms cb[0]
        val pr = Entity.scriptPrompts[0]!!
        pr.a = 100; pr.b = 100                           // prompt anchor
    }

    @Test fun `op108 mounted - the armed key accepts and any other key rejects`() {
        for (reject in listOf(false, true)) {
            val w = scripted(0, block(0, 0,
                group(0, op107(32)), group(10, op108(7, 0))))
            w.cm = 1
            val e = claimer(w)
            armedPrompt(w, e)
            w.pad.edge = if (reject) 4 else 65568        // 4 ∈ 1020, 65568 = cb[0]
            e.runClaimScript(w)
            assertEquals(if (reject) 2 else 1, e.cb!![1], "reject=$reject")
        }
    }

    @Test fun `op108 touch mode - a press inside the prompt accepts, anywhere else in the play area rejects`() {
        for (inside in listOf(true, false)) {
            val w = scripted(0, block(0, 0,
                group(0, op107(32)), group(10, op108(7, 0))))
            w.cm = 0
            val e = claimer(w)
            armedPrompt(w, e)
            if (inside) { w.lastTouchX = 110; w.lastTouchY = 90 }       // within (65..135, 65..135)
            else { w.lastTouchX = 300; w.lastTouchY = 150 }             // play area, outside the rect
            e.runClaimScript(w)
            assertEquals(if (inside) 1 else 2, e.cb!![1], "inside=$inside")
        }
    }

    @Test fun `op108 touch mode - a press on a soft key (outside k_j) neither accepts nor rejects`() {
        val w = scripted(0, block(0, 0,
            group(0, op107(32)), group(10, op108(7, 0))))
        w.cm = 0
        val e = claimer(w)
        armedPrompt(w, e)
        w.lastTouchX = 10; w.lastTouchY = 230            // bottom-left soft-key corner
        e.runClaimScript(w)
        assertEquals(0, e.cb!![1], "k.j() false there")
    }

    @Test fun `op108 decides once - a later reject does not overwrite an accept`() {
        val w = scripted(0, block(0, 0,
            group(0, op107(32)), group(10, op108(7, 0))))
        w.cm = 1
        val e = claimer(w)
        armedPrompt(w, e)
        w.pad.edge = 65568
        e.runClaimScript(w)
        assertEquals(1, e.cb!![1])
        w.pad.edge = 4
        e.runClaimScript(w)
        assertEquals(1, e.cb!![1], "@1794-1800: cb[1] != 0 → no reject")
    }

    // ------------------------------------------------------------ op113
    @Test fun `op113 touch mode - a press outside the current prompt rejects the whole sequence`() {
        val w = scripted(0, block(0, 0,
            group(0, op112(1, 2, 10)), group(10, op113(7, 0))))
        w.cm = 0
        val e = claimer(w)
        e.runClaimScript(w)                              // op112: cc = [2][1, 2]
        val pr = Entity.scriptPrompts[0]!!
        pr.a = 100; pr.b = 100
        w.lastTouchX = 300; w.lastTouchY = 150
        e.runClaimScript(w)
        assertEquals(-1, e.cc!![4], "k.j() → cc[4] = -1 (@3099)")
    }

    @Test fun `op113 touch mode - a press inside the current prompt advances it`() {
        val w = scripted(0, block(0, 0,
            group(0, op112(1, 2, 10)), group(10, op113(7, 0))))
        w.cm = 0
        val e = claimer(w)
        e.runClaimScript(w)
        val pr = Entity.scriptPrompts[0]!!
        pr.a = 100; pr.b = 100
        w.lastTouchX = 110; w.lastTouchY = 90
        e.runClaimScript(w)
        assertEquals(1, e.cc!![4])
    }

    @Test fun `op113 mounted - a wrong key rejects`() {
        val w = scripted(0, block(0, 0,
            group(0, op112(1, 2, 10)), group(10, op113(7, 0))))
        w.cm = 1
        val e = claimer(w)
        e.runClaimScript(w)
        w.pad.edge = 4                                   // ∈ 1020, not 1 << cc[1] = 2
        e.runClaimScript(w)
        assertEquals(-1, e.cc!![4])
    }

    @Test fun `no pad no touch no decision`() {
        val w = scripted(0, block(0, 0,
            group(0, op107(32)), group(10, op108(7, 0))))
        w.cm = 0
        val e = claimer(w)
        armedPrompt(w, e)
        e.runClaimScript(w)
        assertFalse(e.cb!![1] != 0, "nothing pressed")
    }
}
