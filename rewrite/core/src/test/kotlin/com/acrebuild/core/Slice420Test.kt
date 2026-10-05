package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 420 (P3 — `proven` in `i.javap.txt` @51572, `bl()`): the ax64
 * harrier's S7 stalk head runs TWO disjoint cleanup paths, not the
 * merged flow the decompile suggested:
 *
 *   armed path  (@332-578 → @683-744 → `goto 816`): spawn / follow the
 *               directional marker, tether on `k.v(bl)` EDGE, pin
 *               `ae = (ak, al-20)`. `ae` is NEVER released here.
 *   fail path   (@582-676 → @679-680 → @747): retract the stale marker —
 *               left {42,60}, right {48,66} — then the @747 tail, which
 *               is vacuous post-@582.
 *   non-S7 tick (@18 → @747): retract the planted marker — left S60,
 *               right S66.
 *
 * The pre-audit port ran the stale-release check on BOTH paths: every
 * armed tick spawned the marker and instantly released it, so
 * `ax64Tether` and the pin could never fire; and the @747 retract never
 * ran outside S7.
 */
class Slice420Test {

    private fun ax64At(w: Level0World, x: Int, y: Int, anim: Int,
                       z: List<Int> = List(10) { 0 }): Entity {
        val e = Entity(64, w.clips[6])
        e.aw = 700 + w.npcs.size
        val rec = mutableListOf(64, e.aw, x, y, 0, anim, 0)
        rec += z
        while (rec.size < 17) rec += 0
        e.setPositionPx(x, y)
        w.npcFsm.initAx64(e, rec)
        w.npcs.add(e)
        return e
    }

    @Test
    fun `armed tick follows a live S60 marker instead of releasing it`() {
        val w = world()
        val e = ax64At(w, 160, 150, anim = 7)
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        val m = w.spawnPickup(60, 0, 0)
        w.player.ae = m                          // marker already bound
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertSame(m, w.player.ae,
            "@512-575: a live S60 is followed, not released")
        assertEquals(e.ak, m.ak)
        assertEquals(e.al - 20, m.al,
            "@549-575 reposition (al-40) then @715 pin (al-20)")
    }

    @Test
    fun `pad edge on the latch mask fires the tether`() {
        val w = world()
        w.npcFsm.initAx24(Entity(24, null), listOf(0), w)  // seed k.aX pool
        val e = ax64At(w, 160, 150, anim = 7)
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        w.pad.commit(128)                        // v() edge on bl=128 mask
        w.npcFsm.tickAx64(e, w, w.player)
        assertTrue(e.runnerG, "@710-712: G latches after the tether spawn")
        assertEquals(60, w.player.ae?.S,
            "marker stays bound — tether consumed it, not released it")
        assertTrue(w.pooledShots!!.any { it!!.af === e && it.c === w.player },
            "ax64Tether arms a pooled shot owned by the harrier")
    }

    @Test
    fun `non-S7 tick retracts the planted marker on the harrier side`() {
        val w = world()
        val e = ax64At(w, 300, 400, anim = 0)    // S0 — @18 → @747
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        w.player.ae = w.spawnPickup(66, e.ak, e.al)  // e.ak > p.ak → right
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertNull(w.player.ae,
            "@783-807: right-side S66 retracted on a non-stalk tick")
    }

    @Test
    fun `non-S7 tick retracts S60 on the left, keeps other markers`() {
        val w = world()
        val e = ax64At(w, 100, 400, anim = 0)    // left of the player
        w.player.setPositionPx(300, 100)
        w.player.setAnim(0)
        w.player.ae = w.spawnPickup(60, e.ak, e.al)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertNull(w.player.ae,
            "@756-780: left-side S60 retracted")

        val e2 = ax64At(w, 100, 500, anim = 0)
        val m = w.spawnPickup(48, e2.ak, e2.al)  // S48 — outside the @747 set
        w.player.ae = m
        w.npcFsm.tickAx64(e2, w, w.player)
        assertSame(m, w.player.ae,
            "@747 releases only {60,66} — S48 survives")
    }

    @Test
    fun `armed tick follows a live S66 marker too`() {
        val w = world()
        val e = ax64At(w, 300, 150, anim = 7)    // right of player → S66
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        val m = w.spawnPickup(66, 0, 0)
        w.player.ae = m
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertSame(m, w.player.ae,
            "@521-546: S66 takes the same follow arm as S60")
        assertEquals(e.ak, m.ak)
        assertEquals(e.al - 20, m.al)
    }

    @Test
    fun `vuln-fail retracts S60 on the left side`() {
        val w = world()
        val e = ax64At(w, 100, 150, anim = 7)    // left of player
        w.player.setPositionPx(200, 100)
        w.player.setAnim(1)                      // not in AX64_VULN → fail
        w.player.ae = w.spawnPickup(60, e.ak, e.al)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertNull(w.player.ae,
            "@604-629: fail path releases left-side {42,60}")
    }

    @Test
    fun `non-S7 tick keeps the marker when ak is equal`() {
        val w = world()
        val e = ax64At(w, 200, 400, anim = 0)
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        val m = w.spawnPickup(60, e.ak, e.al)
        w.player.ae = m
        e.ak = w.player.ak                       // @766/@793: no side → skip
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertSame(m, w.player.ae,
            "@747 releases only on a real left/right side")
    }

    @Test
    fun `vuln-fail retracts the stale directional marker, equal-ak keeps it`() {
        val w = world()
        val e = ax64At(w, 300, 150, anim = 7)    // right of player
        w.player.setPositionPx(200, 100)
        w.player.setAnim(1)                      // not in AX64_VULN → fail
        w.player.ae = w.spawnPickup(66, e.ak, e.al)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertNull(w.player.ae,
            "@632-670: fail path releases right-side {48,66}")

        val e2 = ax64At(w, 200, 150, anim = 7)
        w.player.setPositionPx(200, 100)
        val m = w.spawnPickup(60, e2.ak, e2.al)
        w.player.ae = m
        e2.ak = w.player.ak                      // equal ak — no side
        w.npcFsm.tickAx64(e2, w, w.player)
        assertSame(m, w.player.ae,
            "@642/@793 `if_icmple`: equal ak releases nothing")
    }
}
