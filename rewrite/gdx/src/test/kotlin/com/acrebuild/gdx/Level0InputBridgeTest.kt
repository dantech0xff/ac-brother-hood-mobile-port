package com.acrebuild.gdx

import com.acrebuild.core.InputQueue
import com.badlogic.gdx.Input
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Multi-touch + letterbox semantics of `Level0InputBridge`: the view
 * tracked by pointer id — an outside-view second finger must not clear
 * the active gesture (a dropped UP leaves controls held), and at most
 * one pointer drives the queue (the original is single-touch).
 *
 * Default renderer geometry: scale=1, offsetX/Y=0 → in-view = sx∈[0,400)
 * and sy∈[0,240).
 */
class Level0InputBridgeTest {

    private fun rig(): Pair<Level0InputBridge, InputQueue> {
        val q = InputQueue()
        return Level0InputBridge(q, Level0Renderer()) to q
    }

    private fun drain(q: InputQueue) =
        q.drainTo(q.headSequence())

    @Test
    fun `second finger outside view does not drop the active finger's UP`() {
        val (b, q) = rig()
        b.touchDown(100, 100, 0, 0)          // finger 0 starts in-view
        b.touchDown(500, 100, 1, 0)          // finger 1 starts outside —
                                             // must NOT clobber the gesture
        b.touchUp(100, 100, 0, 0)            // finger 0 releases
        val types = drain(q).map { it.type }
        assertEquals(listOf(InputQueue.Type.DOWN, InputQueue.Type.UP), types)
    }

    @Test
    fun `second finger inside view is ignored while one is active`() {
        val (b, q) = rig()
        b.touchDown(100, 100, 0, 0)
        b.touchDown(200, 200, 1, 0)          // in-view but a gesture is active
        b.touchDragged(300, 300, 1)          // its drag must not post either
        b.touchUp(200, 200, 1, 0)
        b.touchUp(100, 100, 0, 0)
        val types = drain(q).map { it.type }
        assertEquals(listOf(InputQueue.Type.DOWN, InputQueue.Type.UP), types)
    }

    @Test
    fun `out-of-view finger then in-view finger — the in-view one drives`() {
        val (b, q) = rig()
        b.touchDown(500, 100, 0, 0)          // outside — never claims
        b.touchUp(500, 100, 0, 0)
        b.touchDown(100, 100, 1, 0)          // next finger in-view → active
        b.touchUp(100, 100, 1, 0)
        val types = drain(q).map { it.type }
        assertEquals(listOf(InputQueue.Type.DOWN, InputQueue.Type.UP), types)
    }

    @Test
    fun `active finger straying into the bars keeps tracking clamped`() {
        val (b, q) = rig()
        b.touchDown(100, 100, 0, 0)
        b.touchDragged(500, 100, 0)          // drifts into the bar — clamp
        b.touchUp(500, 100, 0, 0)
        val evs = drain(q)
        assertEquals(listOf(InputQueue.Type.DOWN, InputQueue.Type.MOVE,
            InputQueue.Type.UP), evs.map { it.type })
        assertEquals(399, evs[1].x)          // clamped to the right edge
        assertEquals(399, evs[2].x)
    }

    @Test
    fun `BACK and ESCAPE post a sequenced BACK event, other keys are left alone`() {
        val (b, q) = rig()
        b.touchDown(100, 100, 0, 0)
        assertTrue(b.keyDown(Input.Keys.BACK), "BACK is consumed — never closes the app")
        b.touchUp(100, 100, 0, 0)
        assertTrue(b.keyDown(Input.Keys.ESCAPE), "desktop stand-in for BACK")
        assertFalse(b.keyDown(Input.Keys.A), "the original reads no keys")
        val evs = drain(q)
        assertEquals(listOf(InputQueue.Type.DOWN, InputQueue.Type.BACK,
                            InputQueue.Type.UP, InputQueue.Type.BACK), evs.map { it.type })
        assertEquals(listOf(0L, 1L, 2L, 3L), evs.map { it.sequence }, "one sequence with touches")
    }
}
