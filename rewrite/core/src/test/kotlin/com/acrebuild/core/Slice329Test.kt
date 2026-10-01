package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 329 — string-table completion + ON/OFF polarity.
 *
 * Run-28 re-verify residuals traced to `bU` gaps:
 *   - `d(0,19)` = "DO YOU WANT SOUND?" missing → jC=23 sound-prompt
 *     title (eC=19, `l(23)` arm k.java:1799) never drew.
 *   - ff/fg={21,20} (k.java:306) → `bE ? ff[1](20=ON) : ff[0](21=OFF)`
 *     — port had the polarity inverted, and indices 20/21 were absent
 *     so MUSIC/SFX rows rendered "MUSIC: " with nothing.
 *   - eC=121 title missing + wrong strings at 69/73/103.
 * Verbatim text from `pack-14/entry-000-strings.json` (proven).
 */
class Slice329Test {

    @Test
    fun `sound-prompt title d0(19) is DO YOU WANT SOUND`() {
        val w = world(aj = 0)
        assertEquals("DO YOU WANT SOUND?", w.d0(19))
        // l(23) arm (k.java:1799): eC=19, K(3), eB=70 — eB is NOT drawn
        // for jc23 (ae() `j.c != 23` gate :6216), so the title is d0(19).
        w.stateL(23)
        assertEquals("DO YOU WANT SOUND?", w.menuPrompt())
    }

    @Test
    fun `music sfx rows show ON when enabled OFF when disabled`() {
        val w = world(aj = 0)
        assertEquals("ON", w.d0(20))
        assertEquals("OFF", w.d0(21))
        // options menu bv=4 row 83=MUSIC: kBE=true → "MUSIC: ON"
        // (bE ? ff[1]=20 : ff[0]=21 — the port had this inverted).
        w.stateL(3); w.kBv = 4; w.kEy = 8
        // find the MUSIC row index (row id 83) via menuRowText
        var musicRow = -1
        for (i in 0 until 8) {
            if (w.menuRowText(i).first.startsWith("MUSIC")) { musicRow = i; break }
        }
        assertTrue(musicRow >= 0, "options has a MUSIC row")
        assertEquals("MUSIC: ON", w.menuRowText(musicRow).first)
        w.kBE = false
        assertEquals("MUSIC: OFF", w.menuRowText(musicRow).first)
    }

    @Test
    fun `wipe and quit confirm strings match pack-14`() {
        val w = world(aj = 0)
        assertEquals("THE GAME DATA WILL BE PERMANENTLY DELETED. ARE YOU SURE?",
                     w.d0(69))
        assertEquals("ARE YOU SURE YOU WANT TO GO TO THE MAIN MENU?",
                     w.d0(73))
        assertEquals("THE GAME DATA HAS BEEN DELETED.", w.d0(121))
        assertEquals("VIP ZONE", w.d0(103))
        assertEquals("PLAYER LIST", w.d0(105))
    }

    @Test
    fun `chase and progress banners present`() {
        val w = world(aj = 0)
        assertEquals("SEIZE HIM!", w.d0(74))
        assertEquals("STOP HIM!", w.d0(75))
        assertEquals("BLOCK HIS PATH!", w.d0(76))
        assertEquals("KILL HIM!", w.d0(118))
        assertEquals("CHECKPOINT", w.d0(111))
        assertEquals("YOU GOT A POTION.", w.d0(44))
        assertEquals("WEAPON RECHARGED", w.d0(64))
        assertEquals("ASSASSINATION COMPLETE", w.d0(91))
    }

    @Test
    fun `hero names and qte labels present`() {
        val w = world(aj = 0)
        assertEquals("EZIO", w.d0(106))
        assertEquals("EXECUTIONER", w.d0(107))
        assertEquals("DOCTOR", w.d0(108))
        assertEquals("NOBLEMAN", w.d0(109))
        assertEquals("RUN", w.d0(92))
        assertEquals("UP", w.d0(93))
        assertEquals("JUMP", w.d0(94))
    }
}
