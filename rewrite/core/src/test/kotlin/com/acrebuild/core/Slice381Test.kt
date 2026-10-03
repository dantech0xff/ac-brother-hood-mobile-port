package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Slice 381 — per-frame UI state the renderer used to step per rendered
 * frame now steps once per world frame:
 * - `b()`'s claim block (k.java:3085-3128): card positions + `b(j.f)`,
 *   the `cd[8]` pulse `cb[3]--` and its "ASSASSINATION COMPLETE" banner
 *   (drawn only on the last 16 pulse frames — the scaled `b.a` overload
 *   is an empty stub, b.java:1832);
 * - the case-8/21 tail: the `fS` "CHECKPOINT" marquee (k.java:1027-1039,
 *   play frames included) and the pause icon `fL` (k.java:1041-1052);
 * - `N()`'s `dl` anim and its typewriter on the shared `k.dj/dk`
 *   (k.java:3450-3513).
 */
class Slice381Test {
    private fun claimWorld(): Pair<Level0World, Entity> {
        val w = world()
        settleIntro(w)
        val c = Entity(5, null).apply { ca = 0; scriptStep = 0; cb = intArrayOf(0, 1, 0, 0) }
        w.kC = c
        return w to c
    }

    @Test fun `claim cards are placed and stepped once per world pass`() {
        val (w, _) = claimWorld()
        val pr = ScriptPrompt().apply { attach(9, w.clips[9]); setState(0, -1) }
        Entity.scriptPrompts.fill(null); Entity.scriptPrompts[0] = pr
        try {
            w.stateL(14)                           // l(14) from play: b(true)
            assertEquals(200, pr.a); assertEquals(160, pr.b)
            repeat(5) { w.tick(emptyList()) }      // case 14: b(true) per frame
            val ref = UiAnimObject(w.clips[9]).apply { arm(0, -1) }
            repeat(6) { ref.tick(62) }
            assertEquals(ref.currentFrame, pr.anim.currentFrame, "one b(j.f) per pass")
        } finally {
            Entity.scriptPrompts.fill(null)
        }
    }

    @Test fun `the cd8 banner counts down and shows its last 16 frames`() {
        val (w, c) = claimWorld()
        c.cd[8] = true; c.cb!![3] = 20
        val seen = ArrayList<Boolean>()
        w.stateL(14); seen += w.claimBannerDraw
        repeat(21) { w.tick(emptyList()); seen += w.claimBannerDraw }
        assertEquals(List(4) { false } + List(16) { true } + List(2) { false }, seen)
        assertEquals(0, c.cb!![3])
    }

    @Test fun `the checkpoint marquee types out during play`() {
        val w = world()
        settleIntro(w)
        w.kFS = 0
        repeat(6) { w.tick(emptyList()) }
        assertEquals(3, w.kFS, "one char per two play frames (k.java:1029-1031)")
        assertEquals("CHE", w.tipStr)
    }

    @Test fun `the pause icon steps once per play frame`() {
        val base = world()
        val clip93 = Clip.load(java.io.File("../generated/clips/clip93/clip.acpk").readBytes())
        val w = Level0World(base.level, base.clips + (93 to clip93), DeterministicRandom(1L))
        repeat(20) { w.tick(emptyList()) }
        val fl = w.pauseIcon
        assertNotNull(fl)
        assertEquals(25, fl.e, "idle: fL.a(25,-1)")
        val ref = UiAnimObject(clip93, 377, 19).apply { arm(25, -1) }
        repeat(20) { ref.tick(62) }
        assertEquals(ref.currentFrame, fl.currentFrame, "fL.b(j.f) once per frame")
    }

    @Test fun `the load screen steps dl and its typewriter in the world`() {
        val w = world()
        w.stateL(9)
        repeat(5) { w.tick(emptyList()) }        // N() with j.g 0..4
        assertNotNull(w.loadDl)
        assertEquals(3, w.kDj, "a(bW, d(0,51+eW[aj])) from j.g > 1 (k.java:3508)")
        assertTrue(w.typewriterText.isNotEmpty())
    }
}
