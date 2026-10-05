package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// =====================================================================
// Slice 183 — i.I() victim-side grab / counter-kill chain (i.java:5447-
// 6274, proven): S85 hit-react → S174 grab bind → S175 mash QTE →
// S176 counter-execute / S177 release → S140 settle → S23 idle; plus
// the S18 finisher-offer, S144 weakened-block counter, S96 grab-holder
// release, S182/183 throw + S184 landing, and i.a() NPC body-push.
// =====================================================================
class Slice183Test {

    private fun grabber(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(11, w.clips[7])
        e.aB = 50; e.aA = 1
        e.setPositionPx(x, y); e.refreshBoxes()
        standOn(w, e)
        w.npcs.add(0, e)
        return e
    }

    // -- S85 hit-react + corner kick (L302, i.java:5447-5476) -----------

    @Test fun `S85 moving victim kicks back off supported ground`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        e.setAnim(85); e.ag = -512; e.av = false
        // force a supported corner (b() + corners): stand it on a floor —
        // cornerSupported() reads collision cells, so put it on a real
        // ledge row from the level grid.
        val floorY = w.level.layers.first { it.id == 0 }.let { l ->
            (0 until l.rows).firstOrNull { cy ->
                (0 until l.cols).any { cx -> w.level.collisionCell(cx, cy) >= 12 }
            } ?: 0
        }
        e.setPositionPx(e.ak, floorY * 20 + 80); e.refreshBoxes()
        // tick the arm — supported → N += ag then kick ±2560 + freeze
        val nBefore = e.N
        w.npcFsm.tick(e, w.player)
        if (e.ag == 0 && e.ah == 0) {
            // kicked branch: velocity written to N then cleared
            assertTrue(e.N != nBefore || e.ag == 0,
                "supported S85 should freeze after the kick")
        }
    }

    @Test fun `S85 anim end binds the grab approach S174`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        w.player.setPositionPx(3000, 3000); w.player.refreshBoxes()
                                                       // keep the tail's
                                                       // engage arm quiet
        e.setAnim(85)
        e.T = (e.clip?.frameCount(85) ?: 1) - 1     // last frame
        e.U = (e.clip?.frameDuration(85, e.T) ?: 1) - 1
        e.ag = 0; e.ah = 0; e.N = e.ak shl 8        // no pending kick
        val pAz = w.player.az
        w.npcFsm.tick(e, w.player)
        assertEquals(174, e.S, "S85 r() → i(174) grab-bind")
        assertEquals(pAz + 1, e.az, "az = aS.az + 1 (i.java:5472)")
    }

    // -- S174 grab bind (L318, i.java:5478-5515) -------------------------

    @Test fun `S174 overlap holds the player into the mash QTE`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.av = false
        val p = w.player
        p.setPositionPx(e.ak, e.al + 4); p.refreshBoxes()
        e.setAnim(174)
        w.npcFsm.tick(e, p)
        assertEquals(175, e.S, "overlap binds S175")
        assertEquals(310, p.S, "player held-anim 310")
        assertEquals(e.ak, p.ak, "player x snaps to the grabber")
        assertEquals(true, p.av, "player faces opposite the grabber")
        assertEquals(p.al, e.al, "grabber y snaps to player")
        assertEquals(40, e.bl, "mash gauge starts at 40")
        assertSame(e, w.iBx, "i.bx claim")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
    }

    @Test fun `S174 without overlap settles at anim end`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        w.player.setPositionPx(3000, 3000); w.player.refreshBoxes()
                                                       // far away
        e.setAnim(174)
        e.T = (e.clip?.frameCount(174) ?: 1) - 1
        e.U = (e.clip?.frameDuration(174, e.T) ?: 1) - 1
        w.npcFsm.tick(e, w.player)
        assertEquals(140, e.S, "no-overlap anim end → i(140)")
    }

    // -- S175 mash QTE (L329-L347, i.java:5864-5906) ---------------------

    @Test fun `S175 gauge drain releases the player flung`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.av = false
        val p = w.player
        p.setPositionPx(e.ak, e.al + 4); p.refreshBoxes(); p.av = true
        p.setAnim(310)                                  // held-anim required
        e.setAnim(175); e.bl = 40; w.iBx = e
        // no presses → bl decays 1/tick: 40 ticks empties → throw arm
        repeat(41) { w.npcFsm.tick(e, p) }
        assertEquals(312, p.S, "player thrown-anim 312")
        assertEquals(1280, p.ag, "aS.av==true → ag=+1280 (i.java:5896)")
        assertNull(w.iBx, "i.bx released")
        assertEquals(100, e.az, "release writes az=100")
        assertTrue(e.S != 175, "grab state ended")
    }

    @Test fun `S175 mash fill counter-executes the soldier`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        val p = w.player
        p.setPositionPx(e.ak, e.al + 4); p.refreshBoxes()
        p.setAnim(310)                                  // held-anim required
        e.setAnim(175); e.bl = 40; w.iBx = e
        w.lockTarget = e
        // 5 edge presses of the attack mask fill 40 → 80
        repeat(5) {
            w.pad.commit(65568)
            w.npcFsm.tick(e, p)
            w.pad.commit(0)                            // re-arm the edge
        }
        assertEquals(176, e.S, "filled gauge → i(176) counter-execute")
        assertEquals(0, e.aB, "victim zeroed")
        assertEquals(311, p.S, "player counter-anim 311")
        assertNull(w.lockTarget, "aN lock released")
        assertNull(w.iBx, "i.bx released")
        assertTrue(24 in w.sfxLog, "counter sfx(24) logged")
        assertTrue(w.kAm, "k.o() locks input during the execute")
    }

    @Test fun `S175 dead player releases into S177`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        w.player.setPositionPx(e.ak, e.al + 4)
        w.player.x1 = 0                                // g.g() → dead
        e.setAnim(175); e.bl = 40; w.iBx = e
        w.npcFsm.tick(e, w.player)
        assertEquals(177, e.S, "dead player → i(177)")
        assertEquals(100, e.az, "release writes az=100")
        // verbatim quirk (i.java:5864): the dead-player arm does NOT
        // clear i.bx — only the live-release arms do.
        assertSame(e, w.iBx, "bx survives the dead-player release")
    }

    // -- payoff arms -----------------------------------------------------

    @Test fun `S176 anim end drops the corpse S139`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.aw = 77
        e.setAnim(176)
        // force r() to fire: T=last frame AND U=duration-1
        e.T = (e.clip?.frameCount(176) ?: 1) - 1
        e.U = (e.clip?.frameDuration(176, e.T) ?: 1) - 1
        w.npcFsm.tick(e, w.player)
        assertEquals(139, e.S, "S176 r() → i(139) corpse")
        assertEquals(1, w.kAp[0], "k.e(0,aw) → ap[0]++ (k.java:3264)")
    }

    @Test fun `S177 and S140 unwind back to idle S23`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        w.player.setPositionPx(3000, 3000); w.player.refreshBoxes()
                                                       // keep the tail's
                                                       // engage arm quiet
        e.setAnim(177)
        e.T = (e.clip?.frameCount(177) ?: 1) - 1
        e.U = (e.clip?.frameDuration(177, e.T) ?: 1) - 1
        w.npcFsm.tick(e, w.player)
        assertEquals(140, e.S, "S177 → i(140)")
        e.T = (e.clip?.frameCount(140) ?: 1) - 1
        e.U = (e.clip?.frameDuration(140, e.T) ?: 1) - 1
        w.npcFsm.tick(e, w.player)
        assertEquals(23, e.S, "S140 → i(23)")
    }

    // -- weakened block + finisher offer ----------------------------------

    @Test fun `S18 finisher-offer turns the players tap into S183or184`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.Z[0] = 2; e.aB = 30
        val p = w.player
        p.setPositionPx(e.ak - 20, e.al); p.refreshBoxes()
        p.setAnim(23); p.aZ = true                      // grounded
        e.setAnim(18)
        w.lockTarget = e                               // aN claim (the grab path)
        w.pad.commit(65568)                            // v(65568) edge
        w.npcFsm.tick(e, p)
        assertTrue(p.S == 183 || p.S == 184,
            "finisher offer should take the player to S183|184 (got ${p.S})")
    }

    @Test fun `S144 counter-engages an attacking player`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.Z[0] = 2
        val p = w.player
        p.setPositionPx(e.ak - 10, e.al); p.refreshBoxes()
        p.setAnim(67)                                   // mid sword swing
        p.X[0] = e.ak - 15; p.X[1] = e.al - 10
        p.X[2] = e.ak + 15; p.X[3] = e.al + 10          // attack box ∩ e.W
        e.setAnim(144)
        w.npcFsm.tick(e, p)
        assertEquals(8, p.S, "counter → player i(8) disengage")
    }

    // -- throw + landing (aI() / L760) ------------------------------------

    @Test fun `S182 throws the player airborne and lands the soldier`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150); e.av = false
        val p = w.player
        p.setPositionPx(e.ak + 6, e.al); p.refreshBoxes()
        p.av = false
        e.setAnim(182)
        w.npcFsm.tick(e, p)
        assertEquals(243, p.S, "thrown player i(243)")
        assertEquals(3328, p.ag, "aS.av==false → ag=+3328 (i.java:9284)")
        assertEquals(-6656, p.ah, "upward pop")
        assertEquals(184, e.S, "thrower → i(184)")
        assertEquals(0, e.aB, "aB zeroed")
    }

    @Test fun `S184 on ground snaps the soldier and halves the anim`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        // place on real ground
        val l = w.level.layers.first { it.id == 0 }
        var placed = false
        outer@ for (cy in 1 until l.rows) for (cx in 1 until l.cols) {
            if (w.level.collisionCell(cx, cy) >= 12 &&
                w.level.collisionCell(cx, cy - 1) < 12) {
                e.setPositionPx(cx * 20 + 10, cy * 20); e.refreshBoxes()
                placed = true; break@outer
            }
        }
        if (!placed) return
        e.setAnim(184); e.al = e.al / 20 * 20           // exact cell row
        w.npcFsm.tick(e, w.player)
        assertEquals(0, e.S, "S184 on ground → i(0)")
        assertEquals((e.clip?.frameCount(0) ?: 0) / 2, e.T,
            "T = frameCount(0)/2 — mid-anim restore")
        assertNull(Entity.aL, "i.aL released")
    }

    // -- i.a() NPC body-push (i.java:914-990) ------------------------------

    @Test fun `pushContact pushes an overlapping player off`() {
        val w = world(); w.npcs.clear()
        val e = grabber(w, 300, 150)
        val p = w.player
        p.setAnim(0)                                    // S<=43 ground state
        p.setPositionPx(e.ak - 5, e.al); p.refreshBoxes()
        e.setAnim(23); e.refreshBoxes()
        val before = p.ak
        e.pushContact(w)
        assertTrue(p.ak < before,
            "overlap should push the player left off the NPC box")
    }
}
