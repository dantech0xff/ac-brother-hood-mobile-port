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

class Slice53Test {

    /** ax80 record helper: f = {80, uid, x, y, 0, S, 0, az, ...} — init reads
     *  r8[7] only (L389 :2734→ :3636, proven); clip is always null because
     *  pack-3 has no entry-057 (bi[80]=57 → J(57)=null). */
    private fun ax80At(w: Level0World, x: Int, y: Int, s: Int,
                       az: Int = 0): Entity {
        val e = Entity(80, w.clips[57])
        e.setPositionPx(x, y)
        val f = mutableListOf(80, 0, x, y, 0, s, 0, az)
        while (f.size < 16) f += 0
        w.npcFsm.initAx80(e, f, w)
        w.npcs.add(e)
        return e
    }

    @Test fun `ax80 init stores az and anim verbatim (L389)`() {
        val w = world()
        val e = ax80At(w, 7537, 968, 4, az = 300)
        assertEquals(300, e.az, "az = r8[7]")
        assertEquals(4, e.S, "S = r8[5] via setAnim")
        assertNull(e.clip, "bi[80]=57 → pack-3 entry-057 empty → null clip")
    }

    @Test fun `ax80 never ticks (default arm L897)`() {
        val w = world()
        val e = ax80At(w, 7534, 1016, 4, az = 300)
        repeat(20) { w.tick(emptyList()) }
        assertEquals(4, e.S)
        assertEquals(300, e.az)
        assertTrue(w.npcs.contains(e), "static prop is never removed or moved")
    }
}
