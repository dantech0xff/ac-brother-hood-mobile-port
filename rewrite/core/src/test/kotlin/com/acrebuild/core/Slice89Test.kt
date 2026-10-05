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

/** Slice 89 — `af()` jc30 medal/level browse screen (k.java:6230-6320,
 *  proven). */
class Slice89Test {

    @Test fun `af init counts unlocked rows and resets the cursor`() {
        val w = world()
        w.kBA[14] = 3                              // 3+1 = 4 unlocked → da=4
        w.kDt = false; w.kBA[69] = 0
        w.stateL(30)                               // bannerK(5); kFo=0
        w.tick(emptyList())                        // af() fO==0 arm runs
        assertEquals(4, w.kDa)
        // fQ = #i in 0..3 with da > fP[i]={0,2,5,7} → da=4 > 0,2 → fQ=2
        assertEquals(2, w.kFQ)
        assertEquals(2, w.kEy)
        assertEquals(0, w.kBL)
        assertEquals(-1, w.kFR)
        assertEquals(1, w.kFo)
        assertEquals(listOf(93, 46, 214), w.menuPanelRect().toList())
    }

    @Test fun `af da is 8 when difficulty-locked or data wiped`() {
        val w = world()
        w.kDt = true
        w.stateL(30)
        w.tick(emptyList())
        assertEquals(8, w.kDa)
        assertEquals(4, w.kFQ)                     // 8 > all thresholds
    }

    @Test fun `af title fade climbs 20 to 255`() {
        val w = world()
        w.stateL(30)
        w.tick(emptyList())                        // init sets fC=20 then
        assertEquals(40, w.kFC)                    // the same frame fades +20
        w.tick(emptyList())
        assertEquals(60, w.kFC)
        repeat(20) { w.tick(emptyList()) }
        assertEquals(255, w.kFC)                   // clamps at 255
    }

    @Test fun `af browse nav clamps and rearms`() {
        val w = world()
        w.kBA[14] = 7; w.kDt = false; w.kBA[69] = 0   // da=8 → fQ=4
        w.stateL(30)
        w.audioStop()
        w.tick(emptyList())                        // init: fQ=4
        // UP at bL=0 clamps without the rearm chain
        w.pad.queuePress(Pad.M_UP)
        w.tick(emptyList())
        assertEquals(0, w.kBL)
        assertEquals(0, w.kFR)
        // DOWN ×2 walks the cursor with the fade+shimmer rearm
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(1, w.kBL)
        assertEquals(1, w.kBw)                     // `bw=bL` on a real move
        assertEquals(255, w.kFE)
        assertEquals(20, w.kFC)
        assertEquals(21, w.menuFkArm)
        w.menuFkArm = -1                           // renderer consumed it
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(2, w.kBL)
        // DOWN past the end clamps at fQ-1 with no rearm
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(3, w.kBL)
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(3, w.kBL)
    }

    @Test fun `af confirm routes on fF`() {
        val w = world()
        w.kFF = 20
        w.stateL(30)
        w.tick(emptyList())
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(20, w.jC)                     // fF==20 → l(20)
        assertEquals(0, w.kFF)
    }

    @Test fun `af back routes on fF`() {
        val w = world()
        w.kFF = 19
        w.stateL(30)
        w.tick(emptyList())
        w.pad.queuePress(Pad.M_CYCLE)
        w.tick(emptyList())
        assertEquals(19, w.jC)                     // fF==19 → fO=3 + l(19)
        assertEquals(3, w.kFo)
        assertEquals(0, w.kFF)
    }

    @Test fun `af footer arms right pill to back`() {
        val w = world()
        w.stateL(30)
        w.tick(emptyList())
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
    }

    @Test fun `af draw contract — every renderer input is live`() {
        // jc30 "vẽ đen" regression guard: the renderer reads panelVisible,
        // menuPanelRect, menuRowCount/Rects/Text, menuI4/I5/I14 and the
        // footer pair — assert each is armed for jC==30 so the screen
        // cannot draw blank. (The black-screen report predates the panel
        // draw landing; this test pins the contract.)
        val w = world()
        w.kBA[14] = 7; w.kDt = false; w.kBA[69] = 0   // da=8 → fQ=4 rows
        w.stateL(30)
        w.tick(emptyList())
        assertTrue(w.panelVisible, "jc30 panel must draw")
        assertEquals(listOf(93, 46, 214), w.menuPanelRect().toList())
        assertFalse(w.menuPanelZ2(), "af() goes through d() → z2=false")
        assertFalse(w.menuPanelZ3(), "af() has no 40px title strip")
        assertEquals(4, w.menuRowCount(), "min(8, fQ=4)")
        val rects = w.menuRowRects()
        assertEquals(4, rects.size, "one rect per row")
        rects.forEach { assertEquals(4, it.size) }
        assertEquals(30, w.menuI4(0), "non-jc2 row height")
        assertEquals(170, w.menuI5(), "jc30 column width")
        // jc30 rows = d(0, eA[5][row]) — bannerK(5) on stateL(30)
        // (k.java:6095+, proven) — mission names, NOT 'LEVEL n'
        assertEquals(w.d0(106), w.menuRowText(0).first)
        assertEquals(w.d0(107), w.menuRowText(1).first)
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
    }

    @Test fun `menuRowEntry resolves eA table indices`() {
        val w = world()
        w.stateL(30)                                 // bannerK(5) → bv=5
        assertEquals(106, w.menuRowEntry(0))
        assertEquals(107, w.menuRowEntry(1))
        w.stateL(19)                                  // LEVEL-n arm: no table
        assertEquals(-1, w.menuRowEntry(0))
    }

    // Slice 227 — `L3ad8` ceiling-catch verdict (g.java:8255, proven):
    // `cw && aO==5` → `i(280)` + `al` snaps onto the '5' lip row. `cw` is
    // armed by the air family every tick, and the aO postTail reads is
    // av()'s shifted (al-20) probe — so a RISING jump reaching under a
    // '5' lip grabs it. '5' platforms are NOT pass-through-up: the demo
    // report's corridor trap is verbatim level design (route is from
    // above). A solid cell one row up instead fires av()'s `aO>=20`
    // head-bump → `a(0)` — also verbatim (i.java:1378).
    @Test fun `rise into 5-cell arms the S280 ceiling grab`() {
        val w = world()
        w.stateL(8)
        // find a '5' overhang edge in level0: '5' cell with open air
        // above AND below (a solid row above would instead fire av()'s
        // verbatim aO>=20 head-bump → enterFall, i.java:1378)
        var cx = -1; var cy = -1
        outer@ for (y in 1 until w.level.rows - 2) {
            for (x in 1 until w.level.cols - 1) {
                if (w.level.collisionCell(x, y) == 5 &&
                    w.level.collisionCell(x, y - 1) < 12 &&
                    w.level.collisionCell(x, y + 1) < 5 &&
                    w.level.collisionCell(x, y + 2) < 5) { cx = x; cy = y; break@outer }
            }
        }
        assertTrue(cx >= 0, "no '5' overhang edge in level0")
        val p = w.player
        p.S = 22                                     // air family — arms cw
        p.ah = -3000                                 // rising
        p.ak = cx * 20 + 10
        // W recomputes from `al` via clip rects. av()'s shifted probe
        // (al-20) is what postTail's aO reads, so walk `al` up until the
        // cell one row ABOVE the head is '5' — rising under the lip.
        p.al = cy * 20 + 80
        p.refreshBoxes()                             // a finished frame's t() (G12)
        var guard = 0
        while (guard++ < 60) {
            p.probeCells(w)
            if (p.e(w, p.ak / 20, p.W[1] / 20 - 1) == 5 && p.aO < 12) break
            p.al--
        }
        assertEquals(5, p.e(w, p.ak / 20, p.W[1] / 20 - 1),
            "could not place the head under a '5' lip")
        // the grab is e()'s consumer (cw && aO==5): drive e() on the set-up
        // state — the real tick integrates first (G12) and shifts the probe
        w.playerFsm.tick(p, w.pad); p.refreshBoxes()
        assertEquals(280, p.S, "cw && aO==5 must fire the S280 ceiling grab")
        assertEquals(cy * 20 + 10, p.al, "al snaps onto the '5' lip row +10")
    }

    // Slice 228 — '5'-lip shimmy traversal (g.java:2101 L1502 + L1560,
    // proven): from the S38 hang, `u(16388)` probes the cell above the
    // head — on a '5' BAR the lip itself answers aO=5 (never 0), so the
    // S54 vault-out is dead here and the else-chain arms S37 — the
    // monkey-bar shimmy: facing dir held + facing cell open → `ag=∓1536`
    // step; opposite → `av` flip; anim end → back to S38. '5' cells are
    // standable tops from above AND shimmy bars from below. (At a '5'
    // lip EDGE the probed cell above IS open → S54 fires — the slice-273
    // dip-channel mantle takes exactly that arm.)
    @Test fun `5 lip hang shimmies along the bar`() {
        val w = world()
        w.stateL(8)
        var cx = -1; var cy = -1
        outer@ for (y in 1 until w.level.rows - 2) {
            for (x in 1 until w.level.cols - 2) {
                if (w.level.collisionCell(x, y) == 5 &&
                    w.level.collisionCell(x + 1, y) == 5 &&
                    w.level.collisionCell(x, y - 1) < 12 &&
                    w.level.collisionCell(x, y + 1) < 5 &&
                    w.level.collisionCell(x, y + 2) < 5) { cx = x; cy = y; break@outer }
            }
        }
        assertTrue(cx >= 0, "no two-cell '5' bar in level0")
        val p = w.player
        p.S = 22                                     // air family — arms cw
        p.ah = -3000                                 // rising
        p.ak = cx * 20 + 10
        p.al = cy * 20 + 80
        p.refreshBoxes()                             // a finished frame's t() (G12)
        var guard = 0
        while (guard++ < 60) {
            p.probeCells(w)
            if (p.e(w, p.ak / 20, p.W[1] / 20 - 1) == 5 && p.aO < 12) break
            p.al--
        }
        // the grab is e()'s consumer (cw && aO==5): drive e() on the set-up
        // state — the real tick integrates first (G12) and shifts the probe
        w.playerFsm.tick(p, w.pad); p.refreshBoxes()
        assertEquals(280, p.S, "precondition: the lip grab fires")
        guard = 0
        while (guard++ < 60 && p.S == 280) w.tick(emptyList())
        assertEquals(38, p.S, "S280 anim end arms the S38 hang")
        // Shimmy (verbatim else-chain, g.java L2cb7/L2cdc — the slice-273
        // fix restored `u(facing)` → S37 after the port had carried the
        // inverted `!u(facing)`): away-dir input only flips `av`
        // (`av ? v(8256) : u(4112)` — asymmetric edge/held); the facing
        // arm then fires the shimmy in the held direction.
        p.av = false                                 // face right
        w.pad.queuePress(Pad.M_LEFT)                 // away-dir → flip only
        w.tick(emptyList())
        assertTrue(p.av, "away key flips the hang to face the held dir")
        assertEquals(38, p.S, "the flip keeps the S38 hang — no S37")
        val ak0 = p.ak
        guard = 0
        while (guard++ < 40 && p.S != 37) {
            w.pad.e(Pad.M_LEFT); w.tick(emptyList()) // LEFT now faces → S37
        }
        assertEquals(37, p.S, "facing dir held arms the S37 shimmy")
        guard = 0
        while (guard++ < 40 && p.S == 37) {
            w.pad.e(Pad.M_LEFT); w.tick(emptyList())
        }
        assertTrue(p.ak < ak0, "the shimmy carries ak along the lip")
        assertTrue(p.S == 37 || p.S == 38 || p.S == 43,
            "shimmy holds, returns to S38, or drops at the bar's end")
    }

    // Slice 273 — the S38 '5'-lip EDGE mantle regression. The port had
    // the shimmy arm inverted (`!u(facing)` → i(37)): UP-only input at a
    // lip always fed the else-chain, overwriting the
    // `enterStateMasked(54,8)` the UP arm had just run — '5'-edge
    // mantles were impossible (the dip-channel crossing was a dead-end).
    // Verbatim (g.java L2c2d): `u(16388)` probes the cell above — at a
    // lip edge it reads open → `a(54,8)`.
    @Test fun `5 lip edge hang mantles via S54 on UP`() {
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        // Dip-gap west lip (x2109 — '5' floor x1580-2120 at cy40): rise
        // into it → S280 ceiling grab → S38 hang, same setup as the bar
        // test but at a lip EDGE so the UP probe reads open air above.
        p.S = 22
        p.ah = -3000
        p.ak = 2109
        p.al = 40 * 20 + 80                          // dip '5' cy40 + 80 (same offset the '5'-grab tests use)
        p.refreshBoxes()                             // a finished frame's t() (G12)
        var guard = 0
        while (guard++ < 60) {
            p.probeCells(w)
            if (p.e(w, p.ak / 20, p.W[1] / 20 - 1) == 5 && p.aO < 12) break
            p.al--
        }
        // the grab is e()'s consumer (cw && aO==5): drive e() on the set-up
        // state — the real tick integrates first (G12) and shifts the probe
        w.playerFsm.tick(p, w.pad); p.refreshBoxes()
        assertTrue(p.S == 280 || p.S == 38,
            "precondition: the dip lip grab fires — S${p.S}@${p.ak},${p.al}")
        guard = 0
        while (guard++ < 60 && p.S == 280) w.tick(emptyList())
        assertEquals(38, p.S, "S280 anim end arms the S38 hang")
        // UP-only at the lip edge must mantle — under the inverted arm
        // the same input fed S37 (away=right edge for av=true) and the
        // mantle never fired.
        guard = 0
        while (guard++ < 40 && p.S == 38) { w.pad.e(Pad.M_UP); w.tick(emptyList()) }
        assertTrue(p.S == 54 || p.S == 0 || p.S == 62,
            "UP at the lip edge → S54 mantle chain — S${p.S}")
        assertNotEquals(37, p.S, "UP-only input must never feed S37")
    }

    // Slice 229 — ledge auto-grab + climb-mount chain (g.java:8197
    // L3a2d consumer + i.al() g.java:209-239 + S61/S62 arms — proven):
    // falling past a wall top fires `ct && ledgeHangGrab`: the facing
    // column one cell out must hold ≥19 at hand row with air above and
    // the player's own column open → snap `ak` to the wall edge, hang
    // `al = lip*20-1` in S61. `v(16388)` → `H();G();i(62)` climb-up;
    // S62's `r()` steps `ak±10` into `a(aO>12?79:0,9)` settle — the
    // player mounts the wall top.
    @Test fun `fall past a wall lip auto-grabs and climbs to the top`() {
        val w = world()
        w.stateL(8)
        // a wall top edge facing left-open air: cell(x,y)>=19, air
        // above it, and column x-1 open rows y-1..y+2 (the pocket the
        // hang probe requires).
        var wx = -1; var wy = -1
        outer@ for (y in 6 until w.level.rows - 3) {
            for (x in 2 until w.level.cols - 1) {
                if (w.level.collisionCell(x, y) >= 19 &&
                    w.level.collisionCell(x, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y) == 0 &&
                    w.level.collisionCell(x - 1, y + 1) == 0 &&
                    w.level.collisionCell(x - 1, y + 2) == 0 &&
                    w.level.collisionCell(x - 2, y - 1) == 0 &&
                    w.level.collisionCell(x - 2, y) == 0 &&
                    w.level.collisionCell(x - 2, y + 1) == 0 &&
                    w.level.collisionCell(x - 2, y + 2) == 0) { wx = x; wy = y; break@outer }
            }
        }
        assertTrue(wx >= 0, "no open-side wall top in level0")
        val p = w.player
        p.S = 43                                     // fall — arms ct
        p.ah = 2560                                  // falling
        p.av = false                                 // face right (toward wall)
        // hang probe: i2 = (W[2]+20)/20+1 must equal wx → the right
        // edge stays ≥21px left of the wall — place ak ~1.5 cells out.
        p.ak = (wx - 2) * 20 + 5                     // air left of the wall
        p.al = wy * 20 - 80                          // start above the lip
        p.refreshBoxes()                             // a finished frame's t() (G12)
        var guard = 0
        while (guard++ < 80 && p.S == 43) w.tick(emptyList())
        assertEquals(61, p.S, "the fall must auto-grab the wall lip")
        assertEquals(wy * 20 - 1, p.al, "hang snaps al to the lip top")
        // climb press: UP edge → H();G();i(62) — the mount animation.
        w.pad.queuePress(Pad.M_UP)
        w.tick(emptyList())
        assertEquals(62, p.S, "UP at the hang arms the S62 climb-up")
        guard = 0
        while (guard++ < 120 && p.S == 62) w.tick(emptyList())
        // the mount anim carries the box up over the lip; al is the feet
        // anchor so standing on the lip row's top edge keeps al ≈
        // wy*20-1 — check the feet rest ON the wall top and ak stepped
        // into the wall column.
        p.probeCells(w)
        assertTrue(p.aZ, "climb settles grounded on the wall top")
        assertTrue(p.W[3] <= wy * 20, "feet rest on the lip row top edge")
        assertTrue(p.ak >= wx * 20, "mount steps ak into the wall column")
        assertTrue(p.S == 0 || p.S == 79,
            "settle lands a grounded state on the wall top")
    }

    @Test
    fun `hang release arms drop the player off the wall`() {
        val w = world()
        w.stateL(8)
        // same wall-top pocket the grab test scans for.
        var wx = -1; var wy = -1
        outer@ for (y in 6 until w.level.rows - 3) {
            for (x in 2 until w.level.cols - 1) {
                if (w.level.collisionCell(x, y) >= 19 &&
                    w.level.collisionCell(x, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y) == 0 &&
                    w.level.collisionCell(x - 1, y + 1) == 0 &&
                    w.level.collisionCell(x - 1, y + 2) == 0 &&
                    w.level.collisionCell(x - 2, y - 1) == 0 &&
                    w.level.collisionCell(x - 2, y) == 0 &&
                    w.level.collisionCell(x - 2, y + 1) == 0 &&
                    w.level.collisionCell(x - 2, y + 2) == 0) { wx = x; wy = y; break@outer }
            }
        }
        assertTrue(wx >= 0, "no open-side wall top in level0")
        val p = w.player
        fun rideToHang() {
            p.S = 43; p.ah = 2560; p.av = false
            p.ak = (wx - 2) * 20 + 5
            p.al = wy * 20 - 80
            p.refreshBoxes()                             // a finished frame's t() (G12)
            var g = 0
            while (g++ < 80 && p.S == 43) w.tick(emptyList())
            assertEquals(61, p.S, "the fall must auto-grab the wall lip")
        }

        // arm 1 — front cell still ≥12 + v(33024) DOWN edge → manual
        // release (PlayerFsm.kt S61 arm).
        rideToHang()
        assertTrue(p.aC > 0, "hang arms the grace counter")
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        // release → `H();G();al += W3-W1;a(0)` — the masked S43 fling.
        assertEquals(43, p.S, "DOWN edge releases the hang into a fall")

        // arm 2 — idle hang: aC-- runs each tick; aC==0 → auto drop.
        rideToHang()
        var guard = 0
        while (guard++ < 60 && p.S == 61) w.tick(emptyList())
        assertEquals(43, p.S, "aC grace expiry drops into the same fling")
    }

    @Test
    fun `fall brushing a wall lip auto-mantles onto it`() {
        val w = world()
        w.stateL(8)
        // same wall-top pocket as the hang tests.
        var wx = -1; var wy = -1
        outer@ for (y in 6 until w.level.rows - 3) {
            for (x in 2 until w.level.cols - 1) {
                if (w.level.collisionCell(x, y) >= 19 &&
                    w.level.collisionCell(x, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y - 1) == 0 &&
                    w.level.collisionCell(x - 1, y) == 0 &&
                    w.level.collisionCell(x - 1, y + 1) == 0 &&
                    w.level.collisionCell(x - 1, y + 2) == 0 &&
                    w.level.collisionCell(x - 2, y - 1) == 0 &&
                    w.level.collisionCell(x - 2, y) == 0 &&
                    w.level.collisionCell(x - 2, y + 1) == 0 &&
                    w.level.collisionCell(x - 2, y + 2) == 0) { wx = x; wy = y; break@outer }
            }
        }
        assertTrue(wx >= 0, "no open-side wall top in level0")
        val p = w.player
        p.S = 43; p.ah = 2560; p.av = false
        // lip probe: i2 = (W[2]+5)/20 must equal wx — the right edge
        // kisses the wall face (~1px out), not the 1.5-cell hang gap.
        p.ak = wx * 20 - 12
        p.al = wy * 20 - 80
        p.refreshBoxes()                             // a finished frame's t() (G12)
        var guard = 0
        while (guard++ < 80 && p.S == 43) w.tick(emptyList())
        assertEquals(60, p.S, "the near lip probe grabs the wall edge")
        assertEquals(wy * 20 - 1, p.al, "lip grab snaps al to the lip top")
        // Q=43 != 63 → the S60 arm auto-fires i(62) next tick — the
        // no-input auto-mantle (PlayerFsm.kt S60 arm).
        w.tick(emptyList())
        assertEquals(62, p.S, "lip grab auto-arms the climb-up")
        guard = 0
        while (guard++ < 120 && p.S == 62) w.tick(emptyList())
        p.probeCells(w)
        assertTrue(p.aZ, "mantle settles grounded on the wall top")
        assertTrue(p.W[3] <= wy * 20, "feet rest on the lip row top edge")
        assertTrue(p.S == 0 || p.S == 79,
            "settle lands a grounded state on the wall top")
    }

    @Test
    fun `S63 climb-down entry is dead on shipped content`() {
        // `wallClimb`/`am()` (PlayerFsm.kt:1610) gates on aV/aW/aR
        // == 19 EXACTLY (g.java:301, verbatim) — but no shipped level
        // pack's collision grid contains a type-19 cell, so the only
        // S63 entry is unreachable and S60's `Q == 63` hang-wait branch
        // is dead-letter. S60 itself stays live via ledgeLipGrab (Q=43).
        for (aj in 0..7) {
            val level = LevelPack.load(asset("level$aj/level$aj.aclv"))
            var n19 = 0
            for (y in 0 until level.etRows) {
                for (x in 0 until level.etCols) {
                    if (level.collisionCell(x, y) == 19) n19++
                }
            }
            assertEquals(0, n19, "level$aj contains type-19 cells")
        }
    }

    @Test
    fun `down at a thin platform edge vault-drops into S257`() {
        val w = world()
        w.stateL(8)
        // the a(257,8) vault-drop (S0: l() → aw()'s DOWN arm) needs:
        // support cell aQ ∈ {20,5} (shifted probe = the cell under the
        // feet), aR == 0 below it (thin platform), and open space two
        // cells out one row below in the facing dir.
        var px = -1; var py = -1
        outer@ for (y in 2 until w.level.rows - 2) {
            for (x in 2 until w.level.cols - 3) {
                if (w.level.collisionCell(x, y) >= 19 &&
                    w.level.collisionCell(x, y + 1) == 0 &&
                    w.level.collisionCell(x, y - 1) == 0 &&
                    w.level.collisionCell(x, y - 2) == 0 &&
                    w.level.collisionCell(x + 2, y + 1) < 12) { px = x; py = y; break@outer }
            }
        }
        assertTrue(px >= 0, "no thin platform edge in level0")
        val p = w.player
        p.S = 0
        p.av = false                            // face right, toward the drop
        p.ak = px * 20 + 10
        p.al = py * 20 - 1                      // feet on the platform top
        p.refreshBoxes()                             // a finished frame's t() (G12)
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(257, p.S, "DOWN at the thin edge arms the vault-drop")
        var guard = 0
        while (guard++ < 80 && p.S == 257) w.tick(emptyList())
        assertNotEquals(257, p.S, "vault-drop anim must end")
        assertTrue(p.al > py * 20, "drop carries the player below the ledge")
    }
}
