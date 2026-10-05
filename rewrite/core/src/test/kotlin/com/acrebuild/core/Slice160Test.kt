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

class Slice160Test {

    @Test fun `spawnFloatie inserts ax24 clip40 Si at pos i16745`() {
        val w = world()
        val e = w.npcs.first()
        e.spawnFloatie(w, 8, 123, 456)
        assertEquals(1, w.pendingInsert.size)
        val aK = w.pendingInsert[0]
        assertEquals(24, aK.ax); assertEquals(8, aK.S)
        assertEquals(123, aK.ak); assertEquals(456, aK.al)
        assertEquals(123 shl 8, aK.N); assertEquals(456 shl 8, aK.O)
        assertEquals(0, aK.ag); assertEquals(0, aK.ah)
        assertEquals(0, aK.ai); assertEquals(0, aK.aj)
        assertFalse(aK.av); assertEquals(201, aK.az)
    }

    @Test fun `bk27 springboard child link spawns d8 floatie`() {
        val w = world()
        // ax67 kind-1 springboard — decorClip(1)=bk[1]=27, clip27 loaded
        val pad = Entity(67, w.clipFor(27))
        pad.Z[0] = 1
        pad.setAnim(19)
        pad.ak = w.player.ak + 200; pad.al = w.player.al
        pad.refreshBoxes()
        // linked child: r0 (any ax) whose ad is ax68 inside pad.W
        val ad = Entity(68, null)
        ad.ak = (pad.W[0] + pad.W[2]) / 2
        ad.al = (pad.W[1] + pad.W[3]) / 2
        pad.W.copyInto(ad.W)
        val r0 = Entity(30, null); r0.ad = ad
        w.npcs += r0
        w.paint(r0, pad)
        w.npcFsm.tickDecor(pad, w.player)
        assertEquals(20, pad.S)                          // S+1 armed
        assertEquals(2, ad.S)                            // ad.i(2)
        assertEquals(10, r0.S)                           // r0.i(10)
        val fl = w.pendingInsert.filter { it.ax == 24 }
        assertEquals(1, fl.size)
        assertEquals(8, fl[0].S)
        assertEquals(ad.ak, fl[0].ak); assertEquals(ad.al, fl[0].al)
    }

    @Test fun `ba trail arm spawns d9 floatie only at S22 i13661`() {
        val w = world()
        val e = Entity(24, null)
        e.S = 22; e.aG = 1; e.aC = -1          // countdown elapsed → arm
        e.ak = w.player.ak + 40; e.al = w.player.al
        intArrayOf(e.ak - 20, e.al - 20, e.ak + 20, e.al + 20).copyInto(e.W)
        w.camRect.copyInto(e.Y)                      // in-play overlap gate
        // S22 in the in-flight gate + aG==1 + aC hits <0 → a(9,false) +
        // d(9,ak,al) + retire flags
        w.npcFsm.tickAx24(e, w, w.player)
        val fl = w.pendingInsert.filter { it.ax == 24 && it.az == 201 }
        assertEquals(1, fl.size)
        assertEquals(9, fl[0].S)
        assertEquals(w.player.ak + 40, fl[0].ak)
        assertEquals(w.player.al, fl[0].al)
        assertTrue((e.P and 128) != 0); assertNull(e.af)
        // S!=22 → no floatie
        w.pendingInsert.clear()
        e.S = 23; e.aG = 1; e.aC = -1
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(w.pendingInsert.filter { it.ax == 24 && it.az == 201 }.isEmpty())
    }
}
