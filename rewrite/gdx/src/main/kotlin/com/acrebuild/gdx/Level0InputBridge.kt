package com.acrebuild.gdx

import com.acrebuild.core.InputQueue
import com.acrebuild.core.Level0World
import com.badlogic.gdx.Input
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

    /** Pointer id whose gesture began inside the letterboxed view; -1 =
     *  none. Taps/drags that start in the black bars must NOT register —
     *  the original canvas filled the whole screen so out-of-view input
     *  has no analog; clamping them to the view edge produced phantom
     *  edge taps (a right-bar tap landed at x=399 → footer/action zones).
     *  A gesture that STARTS in-view keeps tracking while the finger
     *  strays into the bars (clamped to the nearest edge). Tracked per
     *  pointer id, not a shared flag: a second finger starting outside
     *  the view must not clear the active finger's gesture — a dropped
     *  UP leaves held controls stuck. The original is single-touch, so
     *  at most one pointer drives the queue at a time. */
    private var activePointer = -1

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
        if (activePointer != -1 || !inView(sx, sy)) return true
        activePointer = pointer
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.DOWN, x, y)
        return true
    }

    override fun touchDragged(sx: Int, sy: Int, pointer: Int): Boolean {
        if (pointer != activePointer) return true
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.MOVE, x, y)
        return true
    }

    override fun touchUp(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        if (pointer != activePointer) return true
        activePointer = -1
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.UP, x, y)
        return true
    }

    override fun touchCancelled(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        if (pointer != activePointer) return true
        activePointer = -1
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.CANCEL, x, y)
        return true
    }

    /** Android BACK (ESCAPE on desktop) → a sequenced [InputQueue.Type.BACK]
     *  event; the world treats it as the right soft-key pill (slice 368).
     *  The key is caught (`Level0Game.create`), so it never closes the app.
     *  Every other key is left unhandled — the original reads none. */
    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.BACK && keycode != Input.Keys.ESCAPE) return false
        queue.post(InputQueue.Type.BACK, -1, -1)
        return true
    }
}
