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

class Slice209Test {

    @Test fun `rebuildEquip resets the action lock`() {
        val w = world(); val p = w.player
        // k.q() (k.java:13152): `as=0; at=0; ar[]=-1` head — the port
        // skipped `at=0`, leaving the cycle button wedged after a grant.
        p.gJ = 1 or 8
        w.actionLock = 1
        w.rebuildEquip()
        assertEquals(0, w.actionLock, "k.at = 0 (verbatim q() head)")
        assertEquals(2, w.equipCount)
        assertEquals(listOf(1, 8, -1, -1, -1), w.equipList.toList())
    }

    @Test fun `weapon cycle indexes by bit position not slot`() {
        val w = world(); val p = w.player
        // k.p(I) = lowest set-bit index; ar[(p(I)+1)%as] for
        // ar=[1,2,8,16]: I=8 → p=3 → ar[0]=1 — slot-3 equip skipped
        // (indexOf would land on 16). Verbatim quirk.
        p.gJ = 1 or 2 or 8 or 16
        w.rebuildEquip()
        p.gI = 8
        val pad = Pad(); pad.queuePress(Pad.M_CYCLE); pad.commit(0)
        assertTrue(p.cycleEquip(w, pad))
        assertEquals(1, p.gI, "ar[(p(8)+1)%4] = ar[0] = 1")
    }

    @Test fun `weapon cycle from the top bit wraps to ar one`() {
        val w = world(); val p = w.player
        // I=16 → p=4 → ar[(4+1)%4] = ar[1] = 2 — the original never
        // reaches ar[0] from 16 (equip-1 is skipped on that hop).
        p.gJ = 1 or 2 or 8 or 16
        w.rebuildEquip()
        p.gI = 16
        val pad = Pad(); pad.queuePress(Pad.M_CYCLE); pad.commit(0)
        assertTrue(p.cycleEquip(w, pad))
        assertEquals(2, p.gI, "ar[(p(16)+1)%4] = ar[1] = 2")
    }
}
