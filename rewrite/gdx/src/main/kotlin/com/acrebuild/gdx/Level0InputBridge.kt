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

    private fun toLogical(sx: Int, sy: Int): Pair<Int, Int> {
        val lx = (sx - renderer.offsetX) / renderer.scale
        val ly = (sy - renderer.offsetY) / renderer.scale
        return lx.coerceIn(0, Level0World.VIEW_W - 1) to
               ly.coerceIn(0, Level0World.VIEW_H - 1)
    }

    override fun touchDown(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.DOWN, x, y)
        return true
    }

    override fun touchDragged(sx: Int, sy: Int, pointer: Int): Boolean {
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.MOVE, x, y)
        return true
    }

    override fun touchUp(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        queue.post(InputQueue.Type.UP, toLogical(sx, sy).first, toLogical(sx, sy).second)
        return true
    }

    override fun touchCancelled(sx: Int, sy: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = toLogical(sx, sy)
        queue.post(InputQueue.Type.CANCEL, x, y)
        return true
    }
}
