package com.acrebuild.gdx

import com.acrebuild.core.CommittedTick
import com.acrebuild.core.FixedPoint
import com.acrebuild.core.SpikeWorld
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.ScreenUtils

/**
 * render-gdx adapter: draws the logical 400x240 scene into an offscreen
 * framebuffer, then integer-scales it to the display with nearest filtering.
 * Reads only the latest committed tick snapshot; never mutates world state.
 */
class PixelRenderer {

    /** Screen→logical transform shared with the input bridge. */
    @Volatile
    var scale: Int = 1
        private set

    @Volatile
    var offsetX: Int = 0
        private set

    @Volatile
    var offsetY: Int = 0
        private set

    private lateinit var fbo: FrameBuffer
    private lateinit var batch: SpriteBatch
    private lateinit var splash: Texture
    private lateinit var actor: Texture
    private lateinit var pixel: Texture

    fun create() {
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, SpikeWorld.FIELD_W, SpikeWorld.FIELD_H, false)
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        batch = SpriteBatch()
        splash = Texture(Gdx.files.internal("splash.png"))
        splash.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        actor = Texture(Gdx.files.internal("actor.png"))
        actor.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pm.setColor(1f, 1f, 1f, 1f)
        pm.fill()
        pixel = Texture(pm)
        pm.dispose()
    }

    fun render(latest: CommittedTick?) {
        val s = latest?.snapshot
        fbo.begin()
        ScreenUtils.clear(0.07f, 0.07f, 0.09f, 1f)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, SpikeWorld.FIELD_W.toFloat(), SpikeWorld.FIELD_H.toFloat())
        batch.begin()
        // Recovered title art, centered horizontally near the top.
        batch.draw(splash, (SpikeWorld.FIELD_W - splash.width) / 2f, SpikeWorld.FIELD_H - splash.height - 8f)
        if (s != null) {
            val ax = FixedPoint.toPixels(s.posX) - SpikeWorld.ACTOR_W / 2f
            val ay = FixedPoint.toPixels(s.posY) - SpikeWorld.ACTOR_H / 2f
            batch.draw(actor, ax, ay)
            if (s.touchHeld) {
                batch.setColor(1f, 0.3f, 0.3f, 1f)
                val wx = worldTouchX(latest)
                val wy = worldTouchY(latest)
                batch.draw(pixel, wx - 1f, wy - 1f, 3f, 3f)
                batch.setColor(1f, 1f, 1f, 1f)
            }
        }
        // Tick marker: 1px per 8 ticks along the bottom edge.
        batch.setColor(0.2f, 0.8f, 0.4f, 1f)
        val t = ((s?.tickIndex ?: 0) / 8 % SpikeWorld.FIELD_W).toFloat()
        batch.draw(pixel, t, 0f, 2f, 2f)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
        fbo.end()

        val sw = Gdx.graphics.width
        val sh = Gdx.graphics.height
        var sc = minOf(sw / SpikeWorld.FIELD_W, sh / SpikeWorld.FIELD_H)
        if (sc < 1) sc = 1 // degenerate small display: fall back handled below
        val dw = SpikeWorld.FIELD_W * sc
        val dh = SpikeWorld.FIELD_H * sc
        if (dw <= sw && dh <= sh) {
            scale = sc; offsetX = (sw - dw) / 2; offsetY = (sh - dh) / 2
        } else {
            // Exact-fit fallback if the display is smaller than 400x240.
            scale = 1
            offsetX = (sw - sw) / 2
            offsetY = (sh - sh) / 2
        }
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        batch.projectionMatrix.setToOrtho2D(0f, 0f, sw.toFloat(), sh.toFloat())
        batch.begin()
        // Framebuffer Y is flipped relative to screen space.
        batch.draw(
            fbo.colorBufferTexture,
            offsetX.toFloat(), offsetY.toFloat(), dw.toFloat(), dh.toFloat(),
            0, 0, SpikeWorld.FIELD_W, SpikeWorld.FIELD_H,
            false, true,
        )
        batch.end()
    }

    private var lastTouchX = 0
    private var lastTouchY = 0
    fun noteTouch(x: Int, y: Int) { lastTouchX = x; lastTouchY = y }
    private fun worldTouchX(t: CommittedTick) = lastTouchX.toFloat()
    private fun worldTouchY(t: CommittedTick) = lastTouchY.toFloat()

    fun dispose() {
        fbo.dispose(); batch.dispose(); splash.dispose(); actor.dispose(); pixel.dispose()
    }
}
