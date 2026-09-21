package com.acrebuild.gdx

import com.acrebuild.core.InputQueue
import com.acrebuild.core.SpikeWorld
import com.badlogic.gdx.InputAdapter

/**
 * input-runtime adapter: LibGDX callbacks append sequenced events to the core
 * queue immediately; gameplay consumes them at tick boundary. Screen pixels
 * are mapped into logical 400x240 space through the renderer's transform.
 */
class InputQueueBridge(
    private val queue: InputQueue,
    private val renderer: PixelRenderer,
) : InputAdapter() {

    private fun toLogical(screenX: Int, screenY: Int): Pair<Int, Int> {
        val lx = (screenX - renderer.offsetX) / renderer.scale
        val ly = (screenY - renderer.offsetY) / renderer.scale
        return lx.coerceIn(0, SpikeWorld.FIELD_W - 1) to ly.coerceIn(0, SpikeWorld.FIELD_H - 1)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = toLogical(screenX, screenY)
        queue.post(InputQueue.Type.DOWN, x, y)
        renderer.noteTouch(x, y)
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val (x, y) = toLogical(screenX, screenY)
        queue.post(InputQueue.Type.MOVE, x, y)
        renderer.noteTouch(x, y)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = toLogical(screenX, screenY)
        queue.post(InputQueue.Type.UP, x, y)
        return true
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        queue.post(InputQueue.Type.CANCEL, screenX, screenY)
        return true
    }
}
