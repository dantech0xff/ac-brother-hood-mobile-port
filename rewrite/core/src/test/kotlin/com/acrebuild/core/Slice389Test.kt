package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 389 — entity constructor `i(short[])` (i.javap.txt:10684-15455)
 * parity over EVERY shipped level record.
 *
 * Method (static): the constructor's bytecode listing was evaluated per
 * record by a throw-away mini interpreter over the `javap -c` text (JAR never
 * loaded or run; calls other than the same-class helpers `i(I)`, `x()`,
 * `a(Z)`, `E()`, `h(I)`, `k(I)` are recorded as effects) and diffed field by
 * field against the port's post-spawn entities for all 8 missions (3 800
 * records). Everything the diff reported is pinned here; the golden numbers
 * below are the interpreter's outputs (confidence `proven` — the same arms
 * were also read straight off the bytecode, see plans/261004-1100-...).
 *
 * Found: ax4 (`k.aq` wisp total, `i=2`, phantom `az=1/P|=512`), ax6 (`az`),
 * ax9 (S34 arm), ax13 (`bN=1`), ax14 (held-record hide), ax19 (`az`, `P|=160`),
 * ax21 (parent `i(1)`), ax24 (`az`), ax37 (`S=-1`), ax42 (`i(r8[5])` runs →
 * fuse armed), ax54/30 (`Z[9]==1` chain, child `az`/`Z`), ax60 (`Z[4]==2`
 * hide), ax11/73 (the real `E()` tail instead of `settleToGround`).
 */
class Slice389Test {
    /** record index → spawned entity (records of 0/25/55 and size<7 never spawn). */
    private fun spawned(w: Level0World): Map<Int, Entity> {
        val m = HashMap<Int, Entity>(); var n = 0
        for ((i, f) in w.level.entities.withIndex()) {
            if (f.isEmpty() || f[0] == 0 || f[0] == 25 || f[0] == 55 || f.size < 7) continue
            m[i] = w.npcs[n++]
        }
        return m
    }

    private val missions = lazy { (0..7).map { world(aj = it) } }

    private fun forRecords(type: Int, body: (Int, IntArray, Entity) -> Unit) {
        for ((aj, w) in missions.value.withIndex()) {
            val by = spawned(w)
            for ((i, f) in w.level.entities.withIndex()) {
                if (f.isEmpty() || f[0] != type) continue
                body(aj, f, by[i] ?: error("aj$aj rec#$i ax$type not spawned"))
            }
        }
    }

    // ---------------------------------------------------------------- ax4
    @Test fun `ax4 takes i=2 and its record az on every shipped record`() {
        var n = 0
        forRecords(4) { aj, f, e ->
            assertEquals(2, e.i, "aj$aj aw=${f[1]} i")
            assertEquals(f[11], e.az, "aj$aj aw=${f[1]} az=r8[11]")
            assertEquals(0, e.P and 512, "aj$aj aw=${f[1]} no P|=512 (ax22's arm)")
            n++
        }
        assertEquals(180, n)
    }

    @Test fun `the wisp HUD total k_aq sums ax4 S5 and S7 burst counts plus ax74 S0`() {
        // ctor L3676 (`k.aq += m` for r8[5]∈{5,7}) and L2606 (`k.aq++` for an
        // ax74 anim-0 wisp) bump ONE static; the HUD prints `ap[4]/aq`.
        // Expected totals = the interpreter's `k.aq` after each pack's records.
        val expected = intArrayOf(96, 46, 142, 117, 47, 123, 133, 0)
        for ((aj, w) in missions.value.withIndex())
            assertEquals(expected[aj], w.kAq, "aj$aj wisp total")
    }

    // ---------------------------------------------------------------- ax6 / ax19 / ax24
    @Test fun `ax6 spawns az100`() = forRecords(6) { aj, f, e ->
        assertEquals(100, e.az, "aj$aj aw=${f[1]}")
    }

    @Test fun `ax19 takes az200 and P160 when the record is held`() {
        var held = 0
        forRecords(19) { aj, f, e ->
            assertEquals(200, e.az, "aj$aj aw=${f[1]} az")
            if (f[6] and 32 != 0) {
                held++
                assertEquals(160, e.P and 160, "aj$aj aw=${f[1]} P|=160")
            }
        }
        assertTrue(held >= 1, "shipped data has a held ax19 (aj3 aw=535)")
    }

    @Test fun `ax24 record entities take az200`() = forRecords(24) { aj, f, e ->
        assertEquals(200, e.az, "aj$aj aw=${f[1]}")
    }

    // ---------------------------------------------------------------- ax9
    @Test fun `ax9 loads Z for every record and binds kAV for anim 0 and 34`() {
        var s34 = 0
        forRecords(9) { aj, f, e ->
            assertEquals(0, e.Z[0], "aj$aj aw=${f[1]}")
            assertEquals(f[7], e.Z[1], "aj$aj aw=${f[1]} Z[1]=r8[7]")
            assertEquals(intArrayOf(47, 72)[f[8]], e.Z[2], "aj$aj aw=${f[1]} Z[2]=bn[r8[8]]")
            assertEquals(f[6] and 512, e.P and 512, "aj$aj aw=${f[1]} the arm adds no P bit")
            if (f[5] == 34) s34++
        }
        assertTrue(s34 >= 1, "shipped data has an anim-34 ax9 (aj5 aw=...)")
    }

    @Test fun `k_aV is the last ax9 record spawned with anim 0 or 34`() {
        for ((aj, w) in missions.value.withIndex()) {
            val by = spawned(w)
            val last = w.level.entities.withIndex()
                .lastOrNull { (_, f) -> f.isNotEmpty() && f[0] == 9 && (f[5] == 0 || f[5] == 34) }
            if (last == null) assertNull(w.kAV, "aj$aj")
            else assertSame(by[last.index], w.kAV, "aj$aj rec#${last.index}")
        }
    }

    // ---------------------------------------------------------------- ax13
    @Test fun `ax13 ropes start with the ctor head bN=1`() = forRecords(13) { aj, f, e ->
        assertEquals(1, e.bN, "aj$aj aw=${f[1]}")
    }

    // ---------------------------------------------------------------- ax14
    @Test fun `ax14 hides a held record even without a linked uid`() {
        var held = 0
        forRecords(14) { aj, f, e ->
            val hidden = f[11] != -1 || (f[6] and 32) != 0
            assertEquals(hidden, e.P and 128 != 0, "aj$aj aw=${f[1]} P&128")
            // 12 other held records already carry r8[6]&128 — only this one
            // depends on the `P&32` clause:
            if (f[11] == -1 && (f[6] and 32) != 0 && (f[6] and 128) == 0) held++
        }
        assertEquals(1, held, "the one shipped unlinked held pickup (aj7 aw=239)")
    }

    // ---------------------------------------------------------------- ax21 / ax37 / ax42
    @Test fun `ax21 parent ends in i(1) and keeps its ax48 delegate`() {
        forRecords(21) { aj, f, e ->
            assertEquals(1, e.S, "aj$aj")
            assertEquals(48, assertNotNull(e.ad).ax)
        }
    }

    @Test fun `ax37 never takes an anim - S stays -1`() = forRecords(37) { aj, f, e ->
        assertEquals(-1, e.S, "aj$aj aw=${f[1]}")
    }

    @Test fun `ax42 fuse runs i(r8_5) on its clipless body - S0 armed from spawn`() {
        var n = 0
        forRecords(42) { aj, f, e ->
            assertNull(e.clip)
            assertEquals(f[5], e.S, "aj$aj aw=${f[1]}")
            n++
        }
        assertEquals(6, n)
    }

    // ---------------------------------------------------------------- ax54 / ax30
    @Test fun `runner Z9 chain forces Z10-12 and its own Z8 test`() {
        var z9one = 0
        for (type in intArrayOf(54, 30)) forRecords(type) { aj, f, e ->
            var z8 = f[14]; var z9 = f[15]; var z10 = f[16]; var z11 = f[17]; var z12 = f[18]
            if (z9 == 0) z10 = 1
            else if (z9 == 1) { z9one++; if (z8 == 0) z8 = 3; z10 = 1; z11 = 1; z12 = 0 }
            if (z8 == 1 && z9 == 1) z9 = 0
            assertEquals(z8, e.Z[8], "aj$aj aw=${f[1]} Z[8]")
            assertEquals(z9, e.Z[9], "aj$aj aw=${f[1]} Z[9]")
            assertEquals(z10, e.Z[10], "aj$aj aw=${f[1]} Z[10]")
            assertEquals(z11, e.aD, "aj$aj aw=${f[1]} aD=Z[11]")
            assertEquals(z12, e.aF, "aj$aj aw=${f[1]} aF=Z[12]")
            if (f[4] != 0) {                                  // child present
                val c = assertNotNull(e.ad, "aj$aj aw=${f[1]} ad")
                assertEquals(68, c.ax)
                assertEquals(99, c.az, "ax68 arm: az=99")
                assertTrue(c.Z.all { it == 0 }, "ax68 has no Z")
            }
        }
        assertTrue(z9one >= 6, "mission 4 carries the Z[9]==1 runners")
    }

    @Test fun `runner Z8==1 with Z9==1 clears Z9 after the forced Z10-12`() {
        // not in shipped data: the second `if` runs on the updated Z[8]
        val w = world(); w.npcs.clear()
        val e = Entity(54, null)
        val f = MutableList(21) { 0 }
        f[0] = 54; f[2] = 100; f[3] = 100; f[4] = 0
        f[14] = 1; f[15] = 1; f[16] = 7; f[17] = 8; f[18] = 9      // Z8=1, Z9=1
        w.npcFsm.initAx54(e, f, w)
        assertEquals(0, e.Z[9], "Z[8]==1 && Z[9]==1 → Z[9]=0")
        assertEquals(1, e.Z[8])
        assertEquals(1, e.Z[10]); assertEquals(1, e.aD); assertEquals(0, e.aF)
    }

    @Test fun `ax60 arms Z4 only for S9 and S16 even when r8_9 is 1`() {
        val w = world(); w.npcs.clear()
        for (s5 in intArrayOf(10, 14, 15, 17, 11)) {
            val e = Entity(60, null)
            e.setPositionPx(100, 200)
            val f = MutableList(22) { 0 }
            f[0] = 60; f[2] = 100; f[3] = 200; f[4] = 5; f[5] = s5; f[8] = 40; f[9] = 1
            f[7] = -1
            w.npcFsm.initAx60(e, f, w)
            assertEquals(0, e.Z[4], "S$s5 ignores r8[9]")
        }
    }

    // ---------------------------------------------------------------- ax60
    @Test fun `ax60 auto-bounce hides the piston and only S9 S16 arm it`() {
        var bounce = 0
        forRecords(60) { aj, f, e ->
            val s5 = f[5]
            val z4 = if ((s5 == 9 || s5 == 16) && f[9] == 1) 2 else 0
            val hide = s5 == 6 || s5 == 11 || s5 == 13 || z4 == 2
            assertEquals(z4, e.Z[4], "aj$aj aw=${f[1]} Z[4]")
            assertEquals(if (hide) 0 else 1, e.az, "aj$aj aw=${f[1]} az")
            assertEquals(hide, e.P and 16 != 0, "aj$aj aw=${f[1]} P&16")
            if (z4 == 2) bounce++
        }
        assertEquals(2, bounce, "aj7 aw=54/55")
    }

    // ---------------------------------------------------------------- ax11 / ax73 settle tail
    @Test fun `soldiers spawn on the E loop ground line of every shipped record`() {
        // (record index, ak, al) per mission — the interpreter's result for
        // `i(r8[5]); a(1); E()` (ax73: `E()` alone) on the real grids.
        val golden = mapOf(
        0 to intArrayOf(105,1759,798,106,2213,478,107,3799,1098,108,1865,958,109,2825,678,110,2798,678,111,2773,678,112,5717,758,113,5794,758,114,5745,758,115,5820,758,116,8850,798,117,8856,558,118,7361,918,119,6916,1098,120,6980,1098,121,7470,918,122,7401,918,123,10966,778,124,11048,778,125,11165,778,126,5020,858,127,5108,858,128,5421,858,129,11761,598,130,11819,598,131,11936,598,132,11859,598,133,12007,598,134,12065,598,135,12159,598,136,12241,598,137,8709,258,138,9014,258,139,8620,258,140,8667,258,141,9723,358,142,9753,358,143,9762,718,144,10547,438,145,10178,658,146,10300,658,147,10351,658,148,10470,658,213,4684,698,214,4697,698,215,4674,698,219,6129,758,223,6144,758),
        1 to intArrayOf(),
        2 to intArrayOf(193,2037,1538,194,1628,1838,195,975,1818,196,2549,1938,197,2838,1818,198,422,1958,199,521,1958,200,622,1958,201,3161,1878,202,3242,1878,203,3476,1878,204,1816,1838,205,627,1478,206,202,1478,207,515,1478,208,2215,1938,209,1988,1298,210,2304,1938,211,651,778,212,1538,858,213,2576,798,214,3001,1138,215,4700,498,216,2190,838,217,1883,1298,218,1883,458,219,2280,838,220,4254,278,221,4509,498,222,4643,498,223,5293,1518,224,3440,518,225,3800,798,226,3992,798,227,4041,798,228,5384,1518,229,4783,278,230,4759,278,231,4725,278,232,4808,278,233,1904,458,242,2909,1141,243,2844,400),
        3 to intArrayOf(20,979,698,26,4806,1398,29,4492,598,32,3789,1398,33,4707,1398,69,10769,1398,70,10785,1398,71,10801,1398,72,10817,1398,73,11297,838,74,11492,838,75,10833,1398,76,10849,1398,213,1083,698,214,2900,558,215,3985,158,216,4093,498,217,1682,498,219,1757,498,220,1872,498,221,1320,338,222,12564,-132,223,10020,718,224,13318,818,225,12799,1078,288,12974,1078,289,12990,1078,292,10851,607),
        4 to intArrayOf(),
        5 to intArrayOf(173,656,978,174,823,658,175,1619,398,176,1338,758,177,2576,398,178,2672,398,179,3654,698,183,4843,958,184,13654,698,185,5541,498,186,5686,498,187,6322,378,188,6110,218,189,6467,718,190,6383,1098,191,6483,1098,192,7324,738,193,6230,378,194,8364,1199,195,7761,1409,196,8434,1199,197,7831,1410,198,11825,958,199,11856,958,200,12987,938,201,12969,938,202,12961,938,203,1845,398,207,7772,1258,208,7847,618,209,12971,938),
        6 to intArrayOf(98,7157,267,119,2934,1038,120,3796,718,121,6946,738,122,3506,718,123,3997,718,124,4373,718,125,6168,538,126,6258,538,127,6358,538,128,6741,738,129,6847,498,130,7506,1398,131,7533,1398,132,1269,638,133,549,578,134,10022,398,135,10022,398,136,10073,578,137,9898,758,138,10199,578,139,10735,518,140,10814,698,141,10445,998,142,10479,998,143,10395,1218,144,10209,1218,397,7764,778,398,7876,778,404,7730,778,405,7914,778),
        7 to intArrayOf(252,764,2038,253,863,1918,254,841,1918,255,893,1918)
        )
        var checked = 0
        for ((aj, flat) in golden) {
            val w = missions.value[aj]
            val by = spawned(w)
            var k = 0
            while (k < flat.size) {
                val e = by[flat[k]] ?: error("aj$aj rec#${flat[k]}")
                assertEquals(flat[k + 1] to flat[k + 2], e.ak to e.al,
                    "aj$aj rec#${flat[k]} ax${e.ax} aw=${e.aw} settled position")
                k += 3; checked++
            }
        }
        assertEquals(186, checked)
    }

    @Test fun `the grounded player spawns on the E loop ground line and the flyer is untouched`() {
        // ctor tail `if (ax == 0) { E(); return }`; ax25 has no E().
        // (aj → record y, settled y): the interpreter's `al` after the ctor.
        val expected = mapOf(0 to (940 to 939), 2 to (1840 to 1839), 3 to (699 to 699),
            5 to (582 to 579), 6 to (740 to 739), 7 to (1740 to 1739),
            1 to (11963 to 11963), 4 to (12403 to 12403))
        for ((aj, w) in missions.value.withIndex()) {
            val (rec, settled) = expected.getValue(aj)
            assertEquals(rec, w.level.playerSpawn()!!.second, "aj$aj record y")
            assertEquals(settled, w.player.al, "aj$aj spawn y")
        }
    }

    @Test fun `the soldier ctor tail leaves b set and ah cleared like E()`() {
        val w = missions.value[0]
        val s = w.npcs.first { it.ax == 11 }
        assertTrue(s.b, "E() sets b=1 before its a(1) pass")
        assertEquals(0, s.ah, "E() ends with ah = 0")
    }

    // ---------------------------------------------------------------- rope release facing
    @Test fun `aG1 rope integrator flip latches the rope av and leaves the rider facing`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = Entity(13, null)
        e.setPositionPx(p.ak, p.al - 40)
        val f = mutableListOf(13, 0, p.ak, p.al - 40, 1, 0, 0)
        for (i in 7..15) f += 0
        w.npcFsm.initAx13(e, f)
        e.Z[1] = 0                                        // isolate from the same-tick grab scan
        w.npcs.add(e)
        p.av = true
        e.aA = 1; e.bM = p; p.bM = e; p.aA = p.aA or 64
        e.bO = 300; e.bP = -512
        repeat(6) { if (p.bM === e) w.npcFsm.tickAx13(e, w, p) }
        assertNull(p.bM, "the flip released the rider")
        assertEquals(e.bP < 0, e.av, "@96-109: the ROPE's av = bP<0")
        assertTrue(p.av, "the rider keeps his facing (g.j reads it for ag)")
    }
}
