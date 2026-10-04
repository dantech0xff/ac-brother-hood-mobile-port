package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 349 — player S18 and S371 arms (g.e() dispatch).
 *
 * The g.e() switch (bytecode g.javap.txt:2537-2555, :2890, :2924; proven):
 *  - 18/19/23/36 → offset 7396 = L1ce4: `cv = 1`, then the L1ce8 air tail
 *    (`cp/ct/cw`, `aj = 1536`, the T==1 launch, wall-grab, land, apex). The
 *    tail compares S only against 15/19/20/22/23/24/25/36/215, never 18, so
 *    S18 runs the generic air path. The port dispatched 19/36 (and 23) there
 *    but had no arm for 18: the S17 wall-kick's flight fell into the default
 *    arm — no gravity, no air flags, no cv wall-grab — until its anim end.
 *  - 371 → 2223: `goto 13629` (L353d), the bare shared tail, like
 *    59/65/164/211/297. Without an arm the default arm flung the player out
 *    of the Cesare grab hold when its anim ended, and ax61's S12 overlay then
 *    took its "player broke the grab" branch — the grab QTE could not be lost.
 */
class Slice349Test {
    private fun resetStatics() {
        Entity.gq = false; Entity.gf = null; Entity.gE = false
        Entity.grabLatch = false
    }

    private fun kicker(clip: Clip?, s: Int): Entity {
        val p = Entity(0, clip)
        p.setPositionPx(200, 100); p.av = false
        p.setAnim(s); p.refreshBoxes()
        return p
    }

    @Test fun `S18 runs the L1ce4 air arm — cv, air flags, gravity`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = kicker(world().clips[0], 18)
        p.ag = 2048; p.ah = -5120; p.aj = 0
        fsm.tick(p, Pad())
        assertTrue(p.cv, "L1ce4: cv = 1")
        assertTrue(p.cp && p.ct && p.cw, "L1ce8: cp/ct/cw")
        assertEquals(1536, p.aj, "L1ce8: aj = 1536")
        assertEquals(18, p.S, "mid-anim: still in the kick flight")
    }

    @Test fun `S17 wall-kick hands its flight to the S18 air arm`() {
        resetStatics()
        val clip0 = world().clips[0]!!
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = kicker(clip0, 17)
        p.T = clip0.frameCount(17) - 1
        p.U = clip0.frameDuration(17, p.T) - 1
        fsm.tick(p, Pad())
        assertEquals(18, p.S, "S17 r() → i(18)")
        assertEquals(-5120, p.ah, "kick launch")
        fsm.tick(p, Pad())
        assertTrue(p.cv, "S18's first tick runs the L1ce4 arm")
        assertEquals(1536, p.aj)
    }

    @Test fun `S371 holds through its anim end — no default-arm fling`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = kicker(null, 0)                       // null clip → r() true
        p.S = 371
        repeat(3) { fsm.tick(p, Pad()) }
        assertEquals(371, p.S, "bare L353d tail — the grab hold persists")
    }

    @Test fun `Cesare grab — a held S371 reaches the S12 QTE lose path`() {
        resetStatics()
        val w = world(); w.npcs.clear()
        val boss = Entity(29, w.clips[7])
        boss.aw = 60; boss.aB = 500
        boss.setPositionPx(300, 150); boss.refreshBoxes()
        w.npcs.add(boss); w.kAU = boss
        val p = w.player
        p.setPositionPx(300, 200); p.setAnim(371); p.refreshBoxes()
        val pc = p.clip!!                             // hold anim already over:
        p.T = pc.frameCount(371) - 1                  // r() is true from tick 1
        p.U = pc.frameDuration(371, p.T) - 1
        val e = Entity(61, w.clips[0])                // clip0 S12 runs 12 frames
        e.setPositionPx(300, 150); e.refreshBoxes()
        w.npcs.add(e); e.setAnim(12)
        w.pad.commit(0)
        var t = 0
        while (e.S == 12 && t++ < 200) {
            w.playerFsm.tick(p, w.pad)
            e.advanceAnim()
            w.npcFsm.tickAx61(e, w, p)
        }
        assertEquals(13, e.S, "S12 ran out with the player still held → i(13)")
        assertEquals(374, p.S, "aS.i(374) — thrown")
        assertEquals(6, w.kBj, "k.bJ latch")
    }
}
