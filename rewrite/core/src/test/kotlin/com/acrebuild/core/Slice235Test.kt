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

class Slice235Test {

    @Test fun `ax21 director retypes and spawns its ax48 delegate`() {
        // Lc8c (i.java:9017): the level-4 record has S=1 <= 1 — field
        // init + k.B registration, then the record is MUTATED to
        // ax48/S=0 and `ad=new i(r8)` spawns the ax48 delegate; the arm
        // ends with `r8[5] = 1` (slice 389 — ctor listing between L3212
        // and L3452), so the parent's shared tail is i(1): S=1.
        val w = world(aj = 4)
        val dir = w.npcs.firstOrNull { it.ax == 21 }
            ?: error("ax21 record not spawned")
        assertEquals(1, dir.S)
        assertSame(dir, w.kB)
        assertEquals(1000, dir.aB)                        // r8[7]
        assertEquals(0, dir.az)                           // r8[8]
        assertEquals(11, dir.Z[0]); assertEquals(15, dir.Z[1])
        assertEquals(16, dir.Z[2]); assertEquals(17, dir.Z[3])
        assertEquals(462, dir.Z[5])                       // r8[13]
        val ad = dir.ad ?: error("ax21 delegate missing")
        assertEquals(48, ad.ax)
        assertEquals(0, ad.S)
        assertEquals(dir.ak, ad.ak); assertEquals(dir.al, ad.al)
    }

    @Test fun `ax51 crates init the Z head and sensor flag`() {
        // Ldbc (i.java:9213): Z[0]=0 hardcoded (NOT r8[7]), Z[1]=r8[8],
        // S==8 records get P|=0x80.
        val w = world(aj = 5)
        val crates = w.npcs.filter { it.ax == 51 }
        assertEquals(15, crates.size)
        for (e in crates) assertEquals(0, e.Z[0], "aw=${e.aw} Z[0]")
        val s8 = crates.filter { it.S == 8 }
        assertEquals(2, s8.size)                          // aw=116,117
        for (e in s8) assertTrue((e.P and 128) != 0,
            "aw=${e.aw} should carry the sensor flag")
        for (e in crates.filter { it.S != 8 })
            assertTrue((e.P and 128) == 0)
    }

    @Test fun `ax66 platforms take the three record anim arms`() {
        // L17cd (i.java:10819): S∈{12,14,19} ride arm; S∈[6,10]∪[24,28]
        // conveyor arm; else → single-Z arm. All get P|=0x200.
        val w6 = world(aj = 6)
        val ride = w6.npcs.firstOrNull { it.ax == 66 && it.S == 12 }
            ?: error("no S12 platform")
        assertEquals(0, ride.az)                          // r8[9]
        assertEquals(40, ride.Z[0]); assertEquals(40, ride.Z[1])
        assertEquals(40, ride.aC)                         // S!=14 → aC=Z[1]
        assertEquals(73, ride.Z[4])                       // r8[7]
        assertTrue((ride.P and 16) != 0)
        val conveyor = w6.npcs.firstOrNull { it.ax == 66 && it.S == 6 }
            ?: error("no S6 platform")
        assertEquals(40, conveyor.Z[0]); assertEquals(40, conveyor.Z[1])
        assertEquals(conveyor.ak, conveyor.Z[2])
        assertEquals(conveyor.al, conveyor.Z[3])
        assertTrue((conveyor.P and 512) != 0)
        val plain = w6.npcs.firstOrNull { it.ax == 66 && it.S == 22 }
            ?: error("no S22 platform")
        assertEquals(-1, plain.Z[0])                      // r8[7]
        assertTrue((plain.P and 512) != 0)
    }

    @Test fun `ax29 boss registers the handle and loads duel fields`() {
        // L10a8 (i.java:9671): fixed duel fields + Z[0..4]; S!=30 records
        // register k.aU. Level-7 record aw=251 (S=0) vs aw=307 (S=30).
        val w = world(aj = 7)
        val boss = w.npcs.firstOrNull { it.ax == 29 && it.aw == 251 }
            ?: error("ax29 aw=251 missing")
        assertEquals(100, boss.az); assertEquals(800, boss.aB)
        assertEquals(2, boss.aD); assertEquals(2, boss.m)
        assertEquals(30, boss.aC); assertEquals(30, boss.aF)
        assertEquals(60, boss.nl)
        assertEquals(0, boss.Z[0]); assertEquals(280, boss.Z[1])
        assertEquals(281, boss.Z[2]); assertEquals(306, boss.Z[3])
        assertEquals(315, boss.Z[4])
        assertSame(boss, w.kAU)
        val s30 = w.npcs.firstOrNull { it.ax == 29 && it.aw == 307 }
            ?: error("ax29 aw=307 missing")
        assertEquals(30, s30.S)
        assertSame(boss, w.kAU, "S30 record must not steal the handle")
    }

    @Test fun `ax58 levers claim bind only when the link is live`() {
        // L1540 (i.java:10402): Z[0]=r8[7]; Z[0]!=-1 → h(k.s) + P|0x210.
        val w = world(aj = 6)
        val linked = w.npcs.firstOrNull { it.ax == 58 && it.aw == 32 }
            ?: error("ax58 aw=32 missing")
        assertEquals(115, linked.Z[0])
        assertTrue((linked.P and 512) != 0 && (linked.P and 16) != 0)
        val dead = w.npcs.firstOrNull { it.ax == 58 && it.aw == 53 }
            ?: error("ax58 aw=53 missing")
        assertEquals(-1, dead.Z[0])
        assertTrue((dead.P and 512) == 0)
    }

    @Test fun `ax16 markers latch az minus one on request anims`() {
        // L595 (i.java:7988): r8[5]∈{31,32,33} → az=-1, else az=200.
        val w5 = world(aj = 5)
        val markers = w5.npcs.filter { it.ax == 16 }
        assertTrue(markers.isNotEmpty())
        for (e in markers) assertEquals(-1, e.az,
            "S=${e.S} marker should take az=-1")
        val w2 = world(aj = 2)
        val m38 = w2.npcs.firstOrNull { it.ax == 16 }
            ?: error("level2 ax16 missing")
        assertEquals(200, m38.az)                         // S=38 → default
    }

    @Test fun `ax61 records get az 101 and spawn with clip71`() {
        // La53 (i.java:8673): `az=101` only; bi[61]=71 supplies the clip.
        val w = world(aj = 7)
        val tools = w.npcs.filter { it.ax == 61 }
        assertEquals(2, tools.size)
        for (e in tools) assertEquals(101, e.az)
        assertEquals(16, tools.first { it.aw == 317 }.S)
    }
}
