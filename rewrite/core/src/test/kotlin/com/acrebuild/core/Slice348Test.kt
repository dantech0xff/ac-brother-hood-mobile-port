package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 348 — `applyHit` op 40, the clip-27 prop bump.
 *
 * Original L96 (simple/i.java:4733-4758; bytecode i.javap.txt:18662-18697,
 * offset 799; structured :3615-3624, proven):
 * `S==3 → return; !g.a() → return; !o() → return;` then
 * `g.b = r13; aB = 3; i(3); av = r13.ak < ak; ag = av ? 512 : -512`.
 * `g.a()` = `g.a(g.g)` drains the player when it passes, so a player in an
 * `o()`-false state (S2, S20..29) still takes the damage but gets no bump.
 *
 * The only caller is ax67 `bB()` for `k.bk[Z[0]]==27` props in S19/21/23/
 * 32/35/38 (structured/i.java:16426-16441): `aS.ah = 768 + k.Y` then
 * `aS.a(40,0,0,this)` and `i(S+1)`. The port had no op-40 arm, so the
 * 32 armed props of the flight missions (m1 21, m4 11) only pushed the
 * glider down — no damage, no S3 bump, no horizontal push.
 */
class Slice348Test {
    /** A clip-27 prop (`k.bk[1]==27`) built through the real ax67 init. */
    private fun prop(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = Entity(67, w.clips[27])
        e.setPositionPx(x, y)
        val f = mutableListOf(67, 900 + w.npcs.size, x, y, 0, s, 0, 1, 0)
        while (f.size < 12) f += 0
        w.npcFsm.initDecor(e, f)
        w.npcs.add(e)
        return e
    }

    private fun ready(w: Level0World, s: Int = 0): Entity {
        val p = w.player
        p.setPositionPx(200, 300); p.setAnim(s)
        p.x1 = 30; p.gt = 0; w.iBh = 0
        return p
    }

    @Test fun `op40 — all gates pass → link, aB 3, S3, facing and push away`() {
        val w = world()
        val p = ready(w)
        val s = prop(w, 260, 300, 19)                 // source to the right
        p.applyHit(40, 0, s, w)
        assertEquals(3, p.S, "i(3)")
        assertEquals(3, p.aB, "aB = 3")
        assertSame(s, w.playerLinkB, "g.b = r13")
        assertFalse(p.av, "r13.ak >= ak → av = false")
        assertEquals(-512, p.ag, "av false → ag = -512")
        assertTrue(p.x1 < 30, "g.a() drained the player")
        assertEquals(10, p.gt, "x1 > 0 → t = 10")

        val w2 = world()
        val p2 = ready(w2)
        val s2 = prop(w2, 140, 300, 19)               // source to the left
        p2.applyHit(40, 0, s2, w2)
        assertTrue(p2.av, "r13.ak < ak → av = true")
        assertEquals(512, p2.ag)
    }

    @Test fun `op40 — S3 and an invulnerable player exit before anything`() {
        val w = world()
        val p = ready(w, 3)
        p.applyHit(40, 0, prop(w, 260, 300, 19), w)
        assertEquals(30, p.x1, "S==3 → return before g.a()")
        assertNull(w.playerLinkB)

        val w2 = world()
        val p2 = ready(w2)
        p2.gt = 5                                     // h(): invulnerable
        p2.applyHit(40, 0, prop(w2, 260, 300, 19), w2)
        assertEquals(30, p2.x1, "g.a() false → no drain")
        assertEquals(0, p2.S, "no i(3)")
        assertNull(w2.playerLinkB)
    }

    @Test fun `op40 — o() false still drains but gets no bump`() {
        val w = world()
        val p = ready(w, 22)                          // S20..29 → o() false
        p.ag = 0
        p.applyHit(40, 0, prop(w, 260, 300, 19), w)
        assertTrue(p.x1 < 30, "g.a() ran first and drained")
        assertEquals(22, p.S, "o() false → no i(3)")
        assertEquals(0, p.ag, "no push")
        assertNull(w.playerLinkB, "no g.b link")
    }

    @Test fun `bB() clip-27 prop — overlap bumps the player through op40`() {
        val w = world()
        val p = ready(w)
        val e = prop(w, 200, 300, 19)
        assertEquals(27, NpcFsm.decorClip(e.Z[0]))
        p.refreshBoxes()
        assertTrue(Entity.overlapI(e.W, p.W), "fixture: player body on the prop")
        w.npcFsm.tickDecor(e, p)
        assertEquals(3, p.S, "op40 → i(3)")
        assertEquals(768 + w.kY, p.ah, "aS.ah = 768 + k.Y before op40")
        assertEquals(20, e.S, "i(S+1)")
        assertTrue(p.x1 < 30, "the bump costs health")
    }

    @Test fun `flight packs carry the 32 armed clip-27 props`() {
        val armed = intArrayOf(19, 21, 23, 32, 35, 38)
        fun count(aj: Int) = world(aj = aj).npcs.count {
            it.ax == 67 && NpcFsm.decorClip(it.Z[0]) == 27 && it.S in armed
        }
        assertEquals(21, count(1), "m1 (pack-7)")
        assertEquals(11, count(4), "m4 (pack-10)")
    }
}
