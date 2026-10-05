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

class Slice178Test {

    /** Force `j.c=8` each tick — level1's intro u9 dialog otherwise pops
     *  on tick 1 and the play arm (which holds the bh3 entity pass)
     *  early-returns for the rest of the dialog. Also pins the player at
     *  camera center: the bh3 autoscroll camera otherwise outruns the
     *  (wingless test) player and `k.l(12)` fails the mission. */
    private fun tickPlay(w: Level0World, n: Int = 1) {
        repeat(n) {
            w.stateL(8)
            w.player.al = w.camY + 120; w.player.ah = 0
            w.tick(emptyList())
        }
    }

    @Test fun `flying pack allocates the dL stamp grid`() {
        // `G(2)`/`U()` (k.java:4359-4365, proven): `bh==3 → dL=int[21][13]`.
        val w = world(aj = 1)
        assertNotNull(w.level.flyingGrid, "bh3 must allocate dL")
        assertEquals(21 * 13, w.level.flyingGrid!!.size)
        assertNull(world().level.flyingGrid, "level0 stays grounded")
    }

    @Test fun `first ticks fill the stamp window`() {
        val w = world(aj = 1)
        tickPlay(w, 2)
        val dl = w.level.flyingGrid!!
        assertTrue(dl.any { it != 0 }, "dL should stamp view cells")
        // `aR<0 → i5=i6` identity rows: slot (cx%21, cy%13) caches
        // `cx + cy*etCols` for the parallax-view window.
        val px = w.parallaxX / 20; val py = w.parallaxY / 20
        assertEquals(px + py * w.level.etCols, dl[(px % 21) * 13 + (py % 13)],
            "top-left stamp slot = linear et index")
    }

    @Test fun `collisionCell resolves through the stamp grid`() {
        val w = world(aj = 1)
        tickPlay(w, 2)
        val et = w.level.layers.first { it.id == 0 }
        val dl = w.level.flyingGrid!!
        // pick a cell inside the current stamp window so `dL` holds a
        // live et index (wrap-band slots can point past et — `g()` then
        // returns 0 via its own `i4>=len` guard).
        val cx = w.parallaxX / 20 + 1; val cy = w.parallaxY / 20 + 1
        val stamped = dl[(cx % 21) * 13 + (cy % 13)]
        val expected = if (stamped < 0 || stamped >= et.cells.size) 0
                       else et.cells[stamped].let { if (it == 255) 0 else it }
        assertEquals(expected, w.level.collisionCell(cx, cy),
            "g() bh3 = et[dL[x%21][y%13]]")
        assertEquals(0, w.level.collisionCell(0, -5),
            "bh3: above the world is air, not solid")
        assertEquals(20, w.level.collisionCell(-1, 0), "x<0 = solid")
        assertEquals(20, w.level.collisionCell(0, et.rows), "cy>=rows = solid")
    }

    @Test fun `grounded levels keep direct et indexing`() {
        val w = world()
        val et = w.level.layers.first { it.id == 0 }
        assertNull(w.level.flyingGrid)
        val cy = et.rows - 2; val cx = 1
        val direct = et.cells[cy * et.cols + cx].let { if (it == 255) 0 else it }
        assertEquals(direct, w.level.collisionCell(cx, cy))
        assertEquals(20, w.level.collisionCell(0, -1),
            "grounded: above world = solid sentinel")
    }

    @Test fun `parallax tracks the camera one to one on level1`() {
        val w = world(aj = 1)
        tickPlay(w, 3)
        // `i2 = O*(bt-21)/(bp-21)`, `i3 = P*(bu-13)/(bq-13)` — level1's
        // et is 44x600 → both fractions are 1:1.
        assertEquals(w.camX, w.parallaxX)
        assertEquals(w.camY, w.parallaxY)
    }

    @Test fun `au scores camera distance in screen units`() {
        val w = world(aj = 1)
        val e = w.npcs.first()
        e.ak = w.camX + 200; e.al = w.camY + 120
        e.recomputeAu(w.camX, w.camY) { i -> w.kBk(i) }
        assertEquals(0, e.au, "center of view = distance 0")
        e.ak += 800
        e.recomputeAu(w.camX, w.camY) { i -> w.kBk(i) }
        assertTrue(e.au >= 2, "two x-screens out should be au>=2")
    }

    @Test fun `on-screen copy member arms the group then consumes`() {
        // `k.I()` (k.java:2536-2550, proven): an eligible entity with
        // `au<1 && ay>0` adopts `ak=ay`, members get `dR=aG` + `u()`, then
        // `ay==ak → -1` marks them consumed so they tick this frame.
        val w = world(aj = 1)
        val e = w.npcs.first()
        val e2 = w.npcs.drop(1).first()
        e.ak = w.camX + 200; e.al = w.camY + 120; e.P = 0; e.ay = 5
        e2.ak = w.camX + 260; e2.al = w.camY + 120; e2.P = 0; e2.ay = 5
        e2.aG = 9
        tickPlay(w)
        assertEquals(-1, e.ay, "first member consumed")
        assertEquals(-1, e2.ay, "second member consumed")
        assertEquals(9, w.kDR, "member aG lands on dR")
    }

    @Test fun `dU world shift applies on a quiet tick`() {
        // k.java:2560-2570 (proven): `i2==0 && dU!=0` → `cB+=dU*400`,
        // `P=cB`, `aS.al+=i5; aS.b(true)`, `dT+=dU*20`, `dU=dR=ak` reset.
        val w = world(aj = 1)
        tickPlay(w, 2)
        w.npcs.clear()                                  // nothing to consume
        // …and no running claim: the intro claim's u9 dialog is switched
        // (auto-dismissed) on the frame that opens it (slice 380), so the
        // claim runs again and would suspend the player (I() L108)
        w.kC = null
        // slice 180: flightTick's z3 tail holds ah at kY — integrate drifts
        // al by kY>>8 = -7/tick. Seat the latch + pin ah=kY so the drift is
        // exact (O's sub-256 residue would otherwise alternate -3/-4).
        w.iBi = true
        w.kDU = 2
        w.stateL(8)
        w.player.ah = w.kY
        val al0 = w.player.al; val dT0 = w.kDT
        w.tick(emptyList())
        assertEquals(al0 + 800 - 7, w.player.al, "player teleports dU*400 (+flight drift)")
        assertEquals(dT0 + 40, w.kDT, "copy boundary shifts dU*20")
        assertEquals(0, w.kDU)
        assertEquals(-1, w.kDR)
        assertEquals(0, w.kAk)
    }
}
