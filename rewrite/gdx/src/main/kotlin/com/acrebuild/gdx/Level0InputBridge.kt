package com.acrebuild.gdx

import com.acrebuild.core.InputQueue
import com.acrebuild.core.Level0World
import com.badlogic.gdx.InputAdapter

/**
 * Touch → logical 400x240 view coords → [InputQueue]. Zone semantics
 * (hold-left/right to run, tap upper third to jump) are interpreted inside
 * [Level0World]; this bridge only performs the pixel→logical mapping.
 */
class Level0InputBridge(
    private val queue: InputQueue,
    private val renderer: Level0Renderer,
) : InputAdapter() {

    /** Whether the current gesture began inside the letterboxed view.
     *  Taps/drags that start in the black bars must NOT register — the
     *  original canvas filled the whole screen so out-of-view input has
     *  no analog; clamping them to the view edge produced phantom edge
     *  taps (a right-bar tap landed at x=399 → footer/action zones).
     *  A gesture that STARTS in-view keeps tracking while the finger
     *  strays into the bars (clamped to the nearest edge). */
    private var gestureInView = false

    private fun inView(sx: Int, sy: Int): Boolean =
        sx >= renderer.offsetX &&
            sx < renderer.offsetX + Level0World.VIEW_W * renderer.scale &&
            sy >= renderer.offsetY &&
            sy < renderer.offsetY + Level0World.VIEW_H * renderer.scale

    private fun toLogical(sx: Int, sy: Int): Pair<Int, Int> {
        val lx = (sx - renderer.offsetX) / renderer.scale
        val ly = (sy - renderer.offsetY) / renderer.scale
        return lx.coerceIn(0, Level0World.VIEW_W - 1) to
               ly.coerceIn(0, Level0World.VIEW_H - 1)
    }

    override fun touchDown(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        gestureInView = inView(sx, sy)
        if (!gestureInView) return true
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.DOWN, x, y)
        return true
    }

    override fun touchDragged(sx: Int, sy: Int, pointer: Int): Boolean {
        if (!gestureInView) return true
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.MOVE, x, y)
        return true
    }

    override fun touchUp(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        if (!gestureInView) return true
        gestureInView = false
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.UP, x, y)
        return true
    }

    override fun touchCancelled(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        if (!gestureInView) return true
        gestureInView = false
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.CANCEL, x, y)
        return true
    }
}
