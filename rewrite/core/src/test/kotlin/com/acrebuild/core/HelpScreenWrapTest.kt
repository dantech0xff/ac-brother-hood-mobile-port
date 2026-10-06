package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Slice-MenuAudit: pin the jc5 help-screen contract that the page body
 *  renders — `a(y,1,cV[bw],200,iK,261,240,0,3)` (k.javap G() @440,
 *  proven): wraps at 261, draws the 8-line window from `8*(cY-1)`,
 *  centered (align 3) — with fontY at palette-1 (the `1` arg is applied
 *  by `a()`'s internal `b.l(i2)` @100-101 — black text ON the
 *  `f(false)` light-gradient backdrop @181, which the port's
 *  MENU_BACKDROP_STATES list had missed for jC=5). */
class HelpScreenWrapTest {
    @Test
    fun helpWrapEmitsWindowedGlyphs() {
        val cm = charmap()
        val c92 = Clip.load(asset("clips/clip92/clip.acpk"))
        val fy = FontClip(c92, FontClip.loadCharmap(cm), 4)
        val page = "TOUCH THE AREA TO THE ASSASSIN'S LEFT/RIGHT: MOVE\n" +
            "TOUCH THE AREA ABOVE THE ASSASSIN: JUMP\n" +
            "TOUCH THE AREA BELOW THE ASSASSIN: CROUCH\n" +
            "TOUCH THE ASSASSIN: ATTACK/HOOK\n" +
            "TOUCH THE WEAPON ICON: CHANGE WEAPON"
        val u = fy.wrap(page, 261)
        assertTrue(u[0] >= 8, "help page wraps to >=8 lines at 261 (got ${u[0]})")
        var glyphs = 0
        var minX = Int.MAX_VALUE; var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE; var maxY = Int.MIN_VALUE
        fy.l(1)
        fy.drawWrapped(page, u, 200, 122, 0, 8, 3, -1) { _, gx, gy, pal ->
            minX = minOf(minX, gx); maxX = maxOf(maxX, gx)
            minY = minOf(minY, gy); maxY = maxOf(maxY, gy)
            assertEquals(1, pal, "page body glyphs draw at palette-1")
            glyphs++
        }
        assertTrue(glyphs > 100, "page body emits real glyphs (got $glyphs)")
        // align-3 centers the block at x=200 vertically-centered region —
        // every glyph lands on-canvas.
        assertTrue(minX >= 0 && maxX < 400 && minY >= 0 && maxY < 240,
            "glyphs on-canvas (x $minX..$maxX, y $minY..$maxY)")
    }
}
