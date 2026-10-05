package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 409 — the ax43 grapple hook `bw()` (`i.javap.txt` @0-534) and its offer `o(i)`.
 *
 * - `bw()` @0-11: `if (ab()) aa();` and then the S switch — **no return** after the claim step.
 * - `bw()` @367-492: the rider gets `aS.i(295)` unless `S ∈ {295, 304, 305, 306, 307}`, and the
 *   pin to the hook's box centre runs for every S (the port: `< 304` / `< 308`).
 * - `o(i)` @L185: `g.g() != 0` is the player's DEATH check (`x[1] <= 0`, g.javap g() 0-11), not
 *   `g.b()` (the attack test) — an attacking player is offered the hook, a dead one is not.
 * - `bM()` (ax9) @0-200: the ride re-pin is the ELSE of the link scan; S4/S5 are bare returns.
 */
class Slice409Test {
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

    private fun scripted(vararg blocks: ByteArray): Level0World {
        val base = world()
        val tables = ScriptTables(
            eH = intArrayOf(7),
            by = arrayOf(arrayOf(*blocks)),
            bz = arrayOf(IntArray(blocks.size) { i ->
                val t = blocks[i][0].toInt() and 0xFF
                if (t == 2 || t == 3) 6 else 4
            }),
        )
        return Level0World(base.level, base.clips, DeterministicRandom(1L),
            scripts = tables).also { it.npcs.clear() }
    }

    /** ax43 on clip 31 at (400,400); `z2 = 1` → the auto-proximity offer (`Z[2] > 0`). */
    private fun hook(w: Level0World, s: Int, z2: Int = 1): Entity {
        val e = Entity(43, w.clips[31]); e.setPositionPx(400, 400)
        val f = listOf(43, 115, 400, 400, 0, s, 1, 0, -1, 9, z2)
        w.npcFsm.initAx43(e, f, w)
        e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    private fun playerAt(w: Level0World, s: Int) {
        val p = w.player
        p.setPositionPx(400, 400); p.refreshBoxes()
        p.S = s; p.ga = null; p.x1 = maxOf(p.x1, 1)
        w.iFlag = true
    }

    // ------------------------------------------------------------------ o(i): g.g() is death

    @Test fun `an attacking player is offered the hook (g_g is the death check, not g_b)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = hook(w, 7)
        playerAt(w, 67); p.gI = 1
        assertTrue(w.playerAttacking(), "fixture: the player is mid-swing")
        w.npcFsm.tickAx43(e, w, p)
        assertSame(e, p.ga, "o(i) @L185 only bails on g.g() (dead)")
        assertEquals(1, e.S)
        assertSame(e, w.kAe)
    }

    @Test fun `a dead player is not offered the hook`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = hook(w, 7)
        playerAt(w, 0); p.x1 = 0
        w.npcFsm.tickAx43(e, w, p)
        assertNull(p.ga)
        assertEquals(7, e.S)
    }

    @Test fun `an idle player is offered the hook`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = hook(w, 7)
        playerAt(w, 0)
        w.npcFsm.tickAx43(e, w, p)
        assertSame(e, p.ga)
        assertEquals(1, e.S)
    }

    // ------------------------------------------------------------------ bw(): rider anim + pin

    private fun rider(s: Int): Triple<Int, Int, Int> {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = hook(w, 1)
        playerAt(w, s); p.ga = e; w.kAe = e
        e.T = 0; e.U = 0
        w.npcFsm.tickAx43(e, w, p)
        val cx = (e.X[0] + e.X[2]) shr 1
        return Triple(p.S, p.ak, cx)
    }

    @Test fun `the rider is put back into the hang pose from any state but 295 and 304-307 (367-423)`() {
        assertEquals(295, rider(0).first, "S0 < 304")
        assertEquals(295, rider(320).first, "S320 >= 308: the port left it alone")
        assertEquals(295, rider(308).first)
        assertEquals(295, rider(295).first, "already there")
        for (s in 304..307) assertEquals(s, rider(s).first, "S$s keeps its anim")
    }

    @Test fun `the rider is pinned to the hook's box centre in every state (424-492)`() {
        for (s in intArrayOf(0, 295, 305, 307, 308, 320)) {
            val (_, ak, cx) = rider(s)
            assertEquals(cx, ak, "S$s")
        }
    }

    // ------------------------------------------------------------------ bw(): claim step has no return

    @Test fun `a hook with a running claim still runs its own state arm (no return after aa)`() {
        val w = scripted(block(0, 0, group(10, byteArrayOf(101) + u16le(5))))
        val p = w.player
        val e = hook(w, 7)
        e.bindScript(0, w); e.scriptKeyStep(0, w)
        assertTrue(e.claimActive(), "fixture: the claim script is running")
        playerAt(w, 0)
        p.setPositionPx(-5000, -5000); p.refreshBoxes()          // out of reach: no re-offer
        p.ga = e; w.kAe = e
        w.npcFsm.tickAx43(e, w, p)
        assertNull(p.ga, "S7 arm: g.a == this → g.a = null")
        assertSame(p, w.kAe, "S7 arm: k.ae = aS")
    }

    // ------------------------------------------------------------------ ax9 bM()

    private fun ax9Linked(w: Level0World, s: Int): Pair<Entity, Entity> {
        val crate = Entity(51, w.clips[7])
        crate.aw = 777; crate.setPositionPx(200, 200)
        crate.ag = 2560; crate.refreshBoxes()
        w.npcs.add(crate)
        val e = Entity(9, w.clips[47]); e.setPositionPx(200, 200)
        val f = mutableListOf(9, 0, 200, 200, 0, s, 0, 777, 0)
        for (i in 9..15) f += 0
        w.npcFsm.initAx9(e, f, w)
        w.npcs.add(e)
        return e to crate
    }

    @Test fun `ax9 link tick sets s and al only - the az re-pin is the else of the link scan`() {
        val w = world(); w.npcs.clear()
        val (e, crate) = ax9Linked(w, 0)
        val az0 = e.az
        w.npcFsm.tickAx9(e, w, w.player)
        assertSame(crate, e.s)
        assertEquals(az0, e.az, "@0-136: the link branch jumps to the switch, skipping @139-199")
        w.npcFsm.tickAx9(e, w, w.player)
        assertEquals(crate.az + 1, e.az, "next tick: s != null → az = s.az + 1")
    }

    @Test fun `ax9 S4 and S5 are bare returns even with the player on the zone`() {
        val w = world(); w.npcs.clear()
        for (s in intArrayOf(4, 5)) {
            val e = Entity(9, w.clips[47]); e.setPositionPx(400, 400)
            val f = mutableListOf(9, 0, 400, 400, 0, s, 0, -1, 0)
            for (i in 9..15) f += 0
            w.npcFsm.initAx9(e, f, w)
            w.npcs.add(e)
            val p = w.player
            p.setPositionPx(400, 400); p.refreshBoxes()
            e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10; e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
            w.kAB = null; w.pad.commit(16388)
            val s0 = p.S
            w.npcFsm.tickAx9(e, w, p)
            assertNull(w.kAB, "S$s: no hint banner")
            assertEquals(s0, p.S, "S$s: no rise")
        }
    }

    // ------------------------------------------------------------------ ax40 bx()

    @Test fun `ax40 with equal (unresolved) endpoints is never past the far end (663-728)`() {
        val w = world(); w.npcs.clear()
        val e = Entity(40, w.clips[45]); e.aw = 935
        e.setPositionPx(500, 300)
        w.npcFsm.initAx40(e, listOf(40, 935, 500, 300, 0, 0, 0, 0, -1, -1, -1))
        w.npcs.add(e)
        assertEquals(e.Z[2], e.Z[3], "fixture: Z2 == Z3 (no anchors)")
        w.player.setPositionPx(-5000, -5000); w.player.refreshBoxes()
        w.npcFsm.tickAx40(e, w, w.player)
        assertEquals(0, e.ah, "no departure fall")
    }

    @Test fun `ax40 past the far end of a left-to-right run starts the departure fall`() {
        val w = world(); w.npcs.clear()
        val e = Entity(40, w.clips[45]); e.aw = 935
        e.setPositionPx(900, 300)
        w.npcFsm.initAx40(e, listOf(40, 935, 900, 300, 0, 0, 0, 0, -1, -1, -1))
        e.Z[2] = 100; e.Z[3] = 800                         // Z2 < Z3 and ak (900) > Z3
        w.npcs.add(e)
        w.player.setPositionPx(-5000, -5000); w.player.refreshBoxes()
        w.npcFsm.tickAx40(e, w, w.player)
        assertTrue(e.ah != 0 || e.S == 1, "ah=512 kick (then gravity) or the wall reset")
    }

    // ------------------------------------------------------------------ j.a / j.b Bezier

    @Test fun `Trig_bezier is the textbook quadratic over idiv 65536 (j_javap a and b)`() {
        val z = Trig.bezier(0, 0, 200, 100, 400, 0, 0)
        assertEquals(0, z[0]); assertEquals(0, z[1])                 // t = 0 → the start
        val e = Trig.bezier(0, 0, 200, 100, 400, 0, 256)
        assertEquals(400, e[0]); assertEquals(0, e[1])               // t = i → the end
        val m = Trig.bezier(0, 0, 200, 100, 400, 0, 128)
        assertEquals((0 + 2 * 200 + 400) / 4, m[0]); assertEquals((0 + 2 * 100 + 0) / 4, m[1])
        // idiv truncates toward zero (a shift would floor): -3·om·t·2 / 65536 → toward 0
        val n = Trig.bezier(0, 0, -1, 0, 0, 0, 128)
        assertEquals(0, n[0], "2·(-1)·128·128 / 65536 = -0.5 → 0, not -1")
    }

    @Test fun `ax74 wisp flight starts on its start point and ends on its target`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = Entity(74, w.clips[54]); e.setPositionPx(0, 0)
        w.npcFsm.initAx74(e, listOf(74, 0, 0, 0, 0, 3, 0, 0), w)
        w.npcs.add(e)
        e.setAnim(3)
        e.Z[0] = 100; e.Z[1] = 200; e.Z[2] = 300; e.Z[3] = 100; e.Z[4] = 200; e.Z[5] = 50
        e.Z[6] = 0; e.Z[7] = 4
        w.npcFsm.tickAx74(e, w, p)
        assertEquals(100 + w.kO, e.ak); assertEquals(200 + w.kP, e.al)
    }
}
