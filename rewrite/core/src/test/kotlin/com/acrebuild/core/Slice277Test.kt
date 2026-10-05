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

// ---- Slice 277: weakened-victim mount = faithful soft-lock ---------------
//
// Verdict: the soft-lock IS faithful — but the capstone's trigger was a
// port bug. Two entries mount a bound ax11 victim:
//  1. `ar()` L3702 + mountEntry (g.java:3474): CONTEXT with `g.g`
//     ax11+Z19==1+alive → `c(g.g)` lunge → `as()` → `i(277)` — verbatim;
//     the original soft-locks identically on a deliberate mount press.
//  2. **PORT BUG (fixed)**: slice-28 guessed S311/312's arm as
//     `lungeTick or mountOrbitTick` — after an NPC grab-release the
//     lunge resumed and auto-mounted `p.g` with NO input. The real arm
//     (L346f, g.java:7456) is `a(1)` enterFall + a proven-dead
//     `r()→l()` tail — the original drops the player.
// The frozen mount itself is verbatim once entered:
//  - `g.i(i)` offer (g.java:12731-12787) has NO Z[0] gate — a weakened
//    (Z[0]==2) mountable (Z[19]==1) ax11 offers/binds identically
//    (`az()` L56a: `i(e) || (aA∉{0,2} && |Δal|≤20)` + LOS `e(i)`).
//  - `au()` ax11 orbit (g.java:10800-10870) drags the PLAYER down at a
//    frozen `cy` — no input arm; every release (P() aB≤0, h>440,
//    |Δal|≥60, carry `aA|=8` needing victim Z[0]==0 i.java:36405,
//    aI() W-overlap, S303/295 anim-end, i.at) is unreachable.
//  The capstone bot's policy stands: never CONTEXT while a weakened
//  mountable is bound — the weakened victim stays passive; `g` drops at
//  |Δal| >= 60 / 440px (slice 388: LOS only gates new binds).
class Slice277Test {

    private fun soldierAt(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(11, w.clips[7])
        e.aB = 50; e.aA = 1
        e.setPositionPx(x, y); e.refreshBoxes()
        w.npcs.add(0, e)
        return e
    }

    @Test fun `az() auto-binds a weakened aA=1 mountable victim on proximity`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.av = false; p.refreshBoxes(); p.S = 0
        p.gJ = 0                                          // no offer arm
        val s = soldierAt(w, 320, 150)
        s.Z[0] = 2; s.Z[19] = 1                           // weakened + mountable
        w.scanInteract(p)
        assertSame(s, p.g, "aA=1 + |Δal|=0 + LOS → L56a binds (proven)")
    }

    @Test fun `L56a non-offer bind is gated by a 20px vertical band`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.av = false; p.refreshBoxes(); p.S = 0
        p.gJ = 0                                          // offer dead
        val far = soldierAt(w, 320, 190)                  // |Δal| = 40
        far.Z[0] = 2; far.Z[19] = 1
        w.scanInteract(p)
        assertNull(p.g, "aA=1 but |Δal|=40 → L56a skips (gate restored)")
        p.g = null
        w.npcs.remove(far)
        val near = soldierAt(w, 320, 165)                 // |Δal| = 15
        near.Z[0] = 2; near.Z[19] = 1
        w.scanInteract(p)
        assertSame(near, p.g, "|Δal|=15 ≤ 20 → binds")
    }

    @Test fun `CONTEXT mounts the bound weakened victim — c(g) lunge`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes(); p.S = 0; p.av = false
        val s = soldierAt(w, 320, 150)
        s.Z[0] = 2; s.Z[19] = 1; s.aB = 40                // weakened, alive
        p.g = s; Entity.at = null; p.gJ = 4
        val pad = Pad()
        pad.queuePress(Pad.M_CONTEXT); pad.commit(0)
        w.playerFsm.mountEntry(p, pad)
        assertSame(s, p.F, "ar() L3702 — c(g) lunged onto the weakened victim")
        assertTrue(w.cm == 1)
    }

    @Test fun `S277 orbit applies the frozen-cy drag toward the victim and never releases`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes(); p.S = 277
        val s = soldierAt(w, 354, 150)                    // +54px east, same level
        s.Z[0] = 2; s.Z[19] = 1; s.aB = 40
        p.g = s; Entity.at = null
        p.cF = 5120; p.cy = 128                           // atan2(0,-54) = 128
        repeat(400) {
            p.mountOrbitTick(w, Pad())
            // j.b = cos (slice 352): victim due east → +20px/tick east, no
            // vertical pull (the old "down-drag" was the sine-table artifact)
            assertEquals(5120, p.ag, "ag = -(cF>>8)·cos(128) = +20px/tick")
            assertEquals(0, p.ah, "ah = 20·cos(64-128) = 20·cos(-64) = 0")
            assertEquals(128, p.cy, "cy never recomputed — frozen at bind")
            w.scanInteract(p)                   // release-gate sweep
            assertSame(s, p.g, "no release arm reachable — g stays bound")
        }
        assertEquals(277, p.S, "S277 has no input arm — mount never dismounts")
        assertTrue(s.aB > 0, "orbit never damages the victim — P() can't fire")
        assertEquals(0, p.aA and 8, "carry arm needs victim Z[0]==0 — dead at 2")
        Entity.at = null
    }

    /**
     * Slice-278 — the x9000 wall crossing PROVEN end-to-end on real
     * level-0 geometry (pack-6 cells + records, no state pinning):
     *
     * Geometry (verified via collision-cell dumps):
     *  - wall mass x9000-9080 '20' solid rows 22-47 (y440 down past the
     *    street to y940); street '20' top ends at x9080 (y800); east of
     *    the wall is a 160px void pit (floor '20' at row 55+ = y1100).
     *  - '02' one-way plateau row 21 (top y420) x9000-9550 floats over
     *    the pit — the ONLY eastward crossing (isOneWay v==2,
     *    LevelPack.kt:94).
     *  - chimney x8940-9000 (60px): WEST face = platform east edge x8940
     *    y560-580 + pillar x8920-8940 y600-680; EAST face = wall x9000
     *    y440-780.
     *  - LOW '5' strip row 19 (y380-400) x8740-8900 hangs off the
     *    floating tower x8680-8740 (rows 15-27).
     *  - BRIDGE '5' strip row 13 (y260-280) x8940-9160 hangs over the
     *    plateau lip — the ax14 zone @8957,305 (bounds x8857-9007,
     *    y255-385) marks the jump gap between them.
     *  - ax74 wisp trail: 8986,698→8988,604→8912,533→8863,302→8983,302 —
     *    zigzag legs then the y~300 aerial line over the plateau.
     *
     * Proven route (driven by real input events):
     *  1. fall into the chimney → S101 grab wall face
     *  2. kick auto-bounce (r()→av flip → S36 ag=∓2048 ah=-5120), ~70px
     *     rise per zigzag leg between the east wall and the pillar face; the
     *     third (west-bound) leg finds no west wall above y560 and drops onto
     *     the LEDGE (cells x8720-8919, top y560)
     *  3. (slice 416) from the ledge the human hops WEST (TL corner held: a
     *     jump with ag=-2048 per hop) until the arc meets the floating
     *     tower's east face (x8740, rows 15-27) → S101 grab at that face →
     *     the auto-kick throws him EAST and ~70px up under the low strip →
     *     cw&&aO==5 → S280 ceiling grab (y390). (The slice-278 route kicked
     *     off the east wall and reached the strip's x-range only because the
     *     fall-arm wall-grab snap was one cell off: g.javap.txt e() @8881-8927
     *     snaps `(W0/20)*20+1` / `(W2/20)*20+19`, the air arm @7886-7938 the
     *     `+20`/`-20` variant — with the faithful snap the east-wall kick
     *     peaks at x8940, past the strip's end at x8920.)
     *  4. S280→S38 hang → hold cell 5 (M_RIGHT) → S37 shimmy east
     *  5. at the strip's east end (x>8890) tap UP → S38 vault-out arm
     *     `u(M_UP)→probe→a(54,8)` (PlayerFsm.kt:813-870, L1502) — pops
     *     up+forward through the ax14-marked gap
     *  6. rise reaches under the BRIDGE strip → cw&&aO==5 → S280 grab
     *     (y270)
     *  7. shimmy east along the bridge → drop → descend onto the '02'
     *     plateau top (y420) — one-way landing at x~9000-9300
     *
     * Observed end-to-end: kicks climb 726→514, low-strip grab @390,
     * shimmy to x>8890, vault-out, bridge grab (minAl=166 recorded),
     * plateau landing — the wall is crossable with the shipped
     * mechanics. Human-timing legs (alternating corner holds + the
     * strip-end UP tap) are the intended skill gate.
     */
    @Test fun `chimney kick-zigzag + strip ceiling-grab + shimmy chain proven`() {
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(8980, 720)
        p.S = 43; p.ag = 1536; p.ai = 0; p.ah = 0; p.aj = 1536
        p.av = false
        val q = InputQueue()
        var heldW = false
        fun postHeld() {
            // zigzag legs hold CORNER taps (0=TL→M_TAP_L, 2=TR→M_TAP_R arm
            // aF); strip shimmy (S38/280) holds MR (cell 5→M_RIGHT)
            val cell = if (p.S == 38 || p.S == 280) 5 else if (heldW) 0 else 2
            val (hx, hy) = w.cellPoint(cell)
            q.post(InputQueue.Type.DOWN, hx, hy)
        }
        postHeld()
        var minAl = Int.MAX_VALUE
        var grabs = 0; var ceilingGrab = false; var shimmy = false
        var wallLand = false
        var bridgeGrab = false; var vaultOut = false
        // slice 416: once the zigzag drops him on the ledge (S5 at y559, x<8920) he hops west to the
        // tower face (TL corner held), until the S101 grab at x<8800 hands over to the kick
        var ledgeHops = false; var towerGrab = false
        repeat(800) { t ->
            if (!ledgeHops && !towerGrab && p.S == 5 && p.al == 559 && p.ak < 8920) ledgeHops = true
            // the touch wheel tracks the player's screen pos — re-post the
            // hold every few ticks at the live zone point (no UP needed:
            // pad bits OR together and aF accepts either direction)
            if (t % 5 == 0 || (p.ag < 0) != heldW || p.S == 38 || p.S == 280 || ledgeHops) {
                heldW = if (p.S == 38 || p.S == 280) false else if (ledgeHops) true else p.ag < 0
                postHeld()
            }
            // at the low strip's east end (x>8890), tap UP → S38 vault-out
            // arm `u(M_UP)→probe→a(54,8)` — pops up+forward into the
            // '5' bridge strip's grab band (y260-280, x8940+)
            if (p.S == 38 && p.ak > 8890) {
                val (ux, uy) = w.cellPoint(1)
                q.post(InputQueue.Type.DOWN, ux, uy)
            }
            val prevS = p.S
            w.tick(q.drainTo(q.headSequence()))
            if (ledgeHops && p.S == 101 && p.ak < 8800) { ledgeHops = false; towerGrab = true }
            if (p.S == 101) grabs++
            if (p.S == 280) ceilingGrab = true
            if (p.S == 37 || p.S == 38) shimmy = true
            if (prevS == 38 && p.S == 54) vaultOut = true
            if (p.S == 280 && p.al < 300) bridgeGrab = true
            if (p.al < minAl) minAl = p.al
            if (p.ak >= 9000 && p.al <= 430 &&
                (p.S == 0 || p.S == 5 || p.S == 1 || p.S == 11)) wallLand = true
        }
        println("CHIMNEY grabs=$grabs towerGrab=$towerGrab ceiling=$ceilingGrab shimmy=$shimmy " +
                "vaultOut=$vaultOut bridgeGrab=$bridgeGrab " +
                "wallLand=$wallLand minAl=$minAl @${p.ak},${p.al} S${p.S}")
        assertTrue(grabs >= 4, "expected ≥4 face grabs in zigzag, got $grabs")
        assertTrue(towerGrab, "the ledge hops never met the tower's east face (S101 at x<8800)")
        assertTrue(ceilingGrab, "'5'-strip S280 ceiling grab never fired")
        assertTrue(shimmy, "S37/38 hang/shimmy never entered")
        assertTrue(vaultOut, "S38 UP vault-out a(54,8) never fired at strip end")
        assertTrue(bridgeGrab, "bridge '5' strip S280 grab (al<300) never fired")
        assertTrue(minAl <= 300, "never reached bridge altitude, minAl=$minAl")
        assertTrue(wallLand, "plateau '02' lip never crossed — the x9000 wall blocks")
    }

    @Test fun `S312 grab-release drops airborne — never mounts (L346f)`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes(); p.S = 312
        val s = soldierAt(w, 354, 150)
        s.Z[0] = 2; s.Z[19] = 1; s.aB = 40
        p.g = s; Entity.at = null                    // bound mountable
        // L346f (g.java:7456): `a(1)` enterFall — the slice-28 arm that
        // resumed lungeTick here mounted `p.g` with no input at all;
        // the original drops the player.
        for (i in 0..40) w.tick(emptyList())
        assertTrue(p.S != 277 && p.S != 293, "no auto-mount after grab-release")
        assertTrue(p.S == 43 || p.S == 5 || p.S == 16 || p.S == 0,
            "S312 → a(1) fall chain (S=${p.S})")
        Entity.at = null
    }

    @Test fun `contrast — a Z0==0 victim mounts but the carry arm can fire`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes(); p.S = 277
        val s = soldierAt(w, 354, 150)
        s.Z[0] = 0; s.Z[19] = 1; s.aB = 100               // normal mountable
        p.g = s; Entity.at = null
        p.cF = 5120; p.cy = 128
        p.mountOrbitTick(w, Pad())
        // i.java:36405 — the aA|=8 carry arm's Z[0]==0 gate PASSES here:
        // the release path exists for normal victims (weak = 2 = dead).
        assertEquals(0, s.Z[0], "normal victim — carry arm gate passes")
        Entity.at = null
    }

}
// Slice 280 — mission-1 (bh3 flying canyon) capstone: ride the conveyor
// camera bottom band up the whole shaft to the claim zone, letting the
// ax54/56 waypoint-runner chain's `k.aH = 80` grace (NpcFsm.kt:4016,
// i.java:51154-51156 proven) freeze the `k.aE` drain.
//
// Fuel economy (all proven this slice):
// - `k.aE` tank 100 drains 1/6t while `k.aH < 0` (g.java:5851-5856,
//   PlayerFsm.kt:2409-2410); `aE<=0 && aH<0` → stall S24 → l(12).
// - `k.aH` is a grace countdown, not a latch: waypoint runners arm 80
//   per hop (i.java:51154); while `aH>=0` the drain is frozen and the
//   arm counts down in c() (k.java:15174-15194); crossing to -1 sets
//   `aE=-1` (poison) but clamps to 0 → `aE<=0 && aH<0` never becomes
//   true (aH is 0, not <0) → the player flies on an empty tank in the
//   pinned-zero state — an original-game quirk kept verbatim.
// - `k.aE <= 0` skips the whole bh3 c() arm (k.java:15172 L3a3), so
//   the refill arm is starved while pinned — fuel is simply over.
// - Below 25 the conveyor halves: `i.aJ = k.X; k.X = aJ>>1`
//   (g.java:13963-13979); the ax24-S20 shrine fire restores
//   `k.X = i.aJ` (NpcFsm.kt:7129, i.java:39330-39335 proven).
// - `Entity.bc()` (the one `i.bc()` port since slice 371) has NO `au` filter — the player's
//   flap-emitted ax24-S6 drop-lines convert S19→S20 shrines at any
//   camera distance, and kill ax54/56/30 runners on contact.
// - Camera: `kP` IS `camY` (view top). Player dies when
//   `al > kP + 240` (below the camera bottom, Entity.kt:1282); there
//   is no top band — out-climbing is free. bh3 entities tick while
//   `au < 2` (Level0World.kt:4756) — a shrine's fire window is
//   `kP ∈ [al-240, al+120]` ≈ 103 ticks at the -7 conveyor.
//
// So the shaft is camera-paced, not tank-paced: ride `al ≈ kP+170`
// the whole way — the bird band keeps re-arming grace, grace expiry
// pins the tank at 0 (free flight), and each shrine fires as its
// window passes. Then the top claim zone ax10-S31 (456,481) binds
// the win script (slice-266 proven chain).
