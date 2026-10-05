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

class Slice73Test {
    private fun ax73At(w: Level0World, x: Int, y: Int, vararg f: Int): Entity {
        val e = Entity(73, w.clips[7])
        e.aw = 90 + w.npcs.size
        val rec = mutableListOf(73, e.aw, x, y)
        rec += f.toList()
        while (rec.size < 20) rec += -1
        // shared-block layout (i.java:2230, proven): Z[0] = sArr[10] —
        // default the archetype field to 0 when the caller omits it.
        if (f.size <= 6) rec[10] = 0
        e.setPositionPx(x, y)
        w.npcFsm.initAx73(e, rec.toList())
        w.npcs.add(e)
        return e
    }

    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    @Test fun `init — minimal record — Z0=f4, az=f17, aB=bu, S=f5`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 152, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 300)
        assertEquals(0, e.Z[0], "Z[0] = sArr[10] = 0")
        assertEquals(300, e.az, "az = r8[17]")
        assertEquals(600, e.aB, "aB = bu[au=0] shl 1 = 600 (ax==73 doubles)")
        assertEquals(152, e.S, "i(r8[5])")
        assertTrue(e.ae == null, "no marker bound at init")
    }

    @Test fun `head — in reach registers the interact claim at prio 0`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 152)
        // 60×60 reach box ahead of the player — park the player inside.
        w.player.setPositionPx(e.ak + 20, e.al - 10)
        w.player.av = true                        // facing left → reach left
        w.player.refreshBoxes()
        // k.M is written by k.l() at the player's g.e() head (slice 369)
        w.playerFsm.eHeadReturns(w.player)
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(w.kL === e, "k.a(this,0,W) claimed")
    }

    @Test fun `head — dead guard releases its claim`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 152)
        w.player.setPositionPx(e.ak + 20, e.al - 10); w.player.av = true
        w.player.refreshBoxes()
        w.playerFsm.eHeadReturns(w.player)        // k.l() → k.M (slice 369)
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(w.kL === e, "claimed in reach first")
        e.aB = 0
        e.setAnim(0)
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(w.kL !== e, "k.m() released")
        assertEquals(164, e.S, "aB<=0 && S!=164 → i(164)")
    }

    @Test fun `S152 idle — G + zero vel + Z0=0 and engage-able raises aA=1`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 152, 0, 0, 0, 0, 3)
        e.ae = Entity(14, null); e.ah = 5; e.ag = 5
        w.player.setPositionPx(e.ak + 100, e.al)   // outside W, LOS-clear
        w.player.refreshBoxes()
        // make the player engage-able: facing-gated sight uses Z rect →
        // minimal record is contact-only, so overlap W for the alert.
        w.player.setPositionPx(e.ak + 5, e.al); w.player.refreshBoxes()
        e.cq = false
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(e.ae == null, "G() released the marker")
        assertEquals(0, e.Z[0], "Z[0] re-zeroed")
        assertEquals(1, e.aA, "b(aS) LOS-clear → aA=1")
        assertEquals(0, e.ah); assertEquals(0, e.ag)
    }

    @Test fun `S131 block — rolling player in X is countered, i(146) on end`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 131)
        w.player.S = 6
        w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes()
        e.X[0] = w.player.W[0] - 10; e.X[1] = w.player.W[1]
        e.X[2] = w.player.W[2] + 10; e.X[3] = w.player.W[3]
        w.player.av = e.ak < w.player.ak           // player faces the guard
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(w.lockTarget === e, "aN = this on the counter")
        finish(e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(146, e.S, "r() → i(146)")
    }

    /** A flat run of solid `20` cells with open air above, found in the level-0
     *  collision map: `aF()` @93-236 reads the cell under the feet one column
     *  behind the guard, and an edge (`c ∉ {20, 5}`) zeroes the S155 walk. */
    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 3 until 400) {
            var ok = true
            for (dx in -2..8) {
                if (w.collisionCell(cx + dx, cy) != 20 || w.collisionCell(cx + dx, cy - 1) >= 12 ||
                    w.collisionCell(cx + dx, cy - 2) >= 12 || w.collisionCell(cx + dx, cy - 3) >= 12) { ok = false; break }
            }
            if (ok) return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    @Test fun `S155 — normal aC=20 and ag retreats backward, enraged aG`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = ax73At(w, gx, gy, 0, 155)           // real ground: aF() sees no ledge
        e.av = false                               // facing right
        e.aq = 0; e.j = 0
        w.player.setPositionPx(e.ak + 1000, e.al)  // j==0 stalk, r7>180
        w.player.refreshBoxes(); w.player.S = 0
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(-512, e.ag, "L76-L79: av==false → ag=-512")
        val e2 = ax73At(w, gx + 80, gy, 0, 155, 0, 0, 0, 0, 3)
        e2.av = true; e2.Z[0] = 3; e2.aq = 0; e2.j = 0
        w.player.setPositionPx(e2.ak + 1000, e2.al); w.player.refreshBoxes()
        w.npcFsm.tickAx73(e2, w, w.player)
        assertEquals(1536, e2.ag, "L68-L74: Z0==3 → ag=1536 (enraged)")
    }

    @Test fun `j() intake — backstab arms the lock, a surviving hit enrages`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 146)        // S146 keeps r11
        e.s = Entity(51, null)                     // disable edge probes
        e.aB = 300
        w.player.S = 67                            // windup anim 67
        w.player.gI = 1
        w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes()
        // r2 gate (aJ() @103-114 `this.g(aS)`): the GUARD must not face the
        // player — it looks left, the player stands on its right (slice 400:
        // this test set the PLAYER's facing, the wrong receiver)
        e.av = true
        w.npcFsm.tickAx73(e, w, w.player)          // arms bf via S67
        assertTrue(w.iBf, "L48 engage latch armed on S67 windup")
        assertTrue(w.lockTarget === e, "aN = this")
        w.player.S = 68                            // strike lands
        w.player.X[0] = e.W[0] - 10; w.player.X[1] = e.W[1]
        w.player.X[2] = e.W[2] + 10; w.player.X[3] = e.W[3]
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(300 - 80, e.aB, "L98: aB -= bw")
        // Z0==0 && aB<=bu → every surviving sword hit enrages.
        assertEquals(3, e.Z[0], "C() L13: enrage Z0=3")
        assertEquals(155, e.S, "→ i(155)")
        assertTrue(e.aq != 0, "aq = ak∓60 retreat latch")
        assertEquals(8, w.player.S, "aS.i(8)")
    }

    @Test fun `j() — enraged guard ignores damage entirely`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 146, 0, 0, 0, 0, 3)  // Z0=3 already
        e.aB = 300
        w.player.S = 68; w.player.gI = 1
        w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes()
        w.player.av = false
        w.player.X[0] = e.W[0] - 10; w.player.X[1] = e.W[1]
        w.player.X[2] = e.W[2] + 10; w.player.X[3] = e.W[3]
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(300, e.aB, "L234: Z0==3 skips j() — armored")
    }

    @Test fun `j() — aB zero → C() dies to i(164), finisher costs J=100`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 146)
        e.aB = 100
        e.av = true                                // guard looks away: backstab angle
        w.player.S = 183                           // assassination anim
        w.player.gI = 1
        w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes()
        w.player.X[0] = e.W[0] - 10; w.player.X[1] = e.W[1]
        w.player.X[2] = e.W[2] + 10; w.player.X[3] = e.W[3]
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(164, e.S, "aB zero → L64 i(164)")
        assertEquals(0, e.ag); assertEquals(0, e.ah)
    }

    @Test fun `S165 grab gate — opposite-facing and held → i(147) QTE`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 165)
        e.av = false                               // guard faces right
        w.player.av = true                         // player faces left (xor)
        w.player.aZ = true
        w.player.setPositionPx(e.ak - 90, e.al)    // 90 < 140 → no abort
        w.player.refreshBoxes()
        e.X[0] = w.player.W[0] - 10; e.X[1] = w.player.W[1]
        e.X[2] = w.player.W[2] + 10; e.X[3] = w.player.W[3]
        w.pad.commit(16388)                        // k.u(16388) held
        w.pad.commit(16388)                        // second commit: held, edge gone
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(294, w.player.S, "aS.i(294)")
        assertEquals(147, e.S, "i(147) hold")
        assertEquals(40, e.bl, "bl = 40")
        assertTrue(w.iBx === e, "bx = this")
        assertTrue(e.P and 64 == 0, "P&=-65 invisibility flag cleared")
    }

    @Test fun `S165 abort — overshot 140 facing-side → i(171), P minus 65`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 165)
        e.av = false                               // moving right
        w.player.av = false
        w.player.aZ = false                        // grab gate fails
        w.player.setPositionPx(e.ak - 300, e.al)   // e.ak - p.ak = +300 ≥ 140
        w.player.refreshBoxes()
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(171, e.S, "L199 abort → i(171)")
        assertTrue(e.P and 64 == 0, "P&=-65")
    }

    @Test fun `S147 QTE — mash 65568 to bl 80 kills the guard, frees player`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 147)
        e.bl = 40
        w.player.S = 294
        repeat(10) { w.pad.commit(65568); w.npcFsm.tickAx73(e, w, w.player); w.pad.commit(0) }
        assertEquals(164, e.S, "g(65568,80) → i(164) escape")
        assertEquals(287, w.player.S, "aS.i(287)")
        assertEquals(0, e.aB, "aB = 0")
        assertTrue(w.iBx == null, "bx = null")
    }

    @Test fun `S147 release — bl hits zero → i(149) + aS l + G`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 147)
        e.bl = 1
        w.player.S = 294
        w.pad.commit(0)                            // decay: bl 1 → 0
        w.npcFsm.tickAx73(e, w, w.player)
        w.pad.commit(0)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(149, e.S, "bl==0 → i(149) release")
        assertTrue(w.iBx == null)
        assertTrue(e.ae == null, "G() released")
    }

    @Test fun `S171 enraged — player above sight rect disengages to S152`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 171, 0, 0, 0, 0, 3)
        e.aq = 0
        e.s = Entity(51, null)                     // edge probes off
        w.player.setPositionPx(e.ak, e.al + 120)   // W[1] > Z[12] (=200)
        w.player.refreshBoxes()
        finish(e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(152, e.S, "L135 → i(152)")
        assertEquals(0, e.aA)
    }

    @Test fun `S164 death end — P or 32 or 64, minus 17, gJ or 2 + sheath`() {
        val w = world()
        val e = ax73At(w, 100, 200, 0, 164)
        w.kBK = false
        w.player.gJ = 0
        finish(e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(e.P and 32 != 0 && e.P and 64 != 0, "P |= 32|64")
        assertTrue(e.P and 16 == 0, "P &= -17")
        assertTrue(w.player.gJ and 2 != 0, "g.g(2) → gJ |= 2")
    }
}
