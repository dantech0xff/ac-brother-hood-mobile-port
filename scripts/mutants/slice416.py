"""Slice 416 mutants: each puts one OLD (wrong) behaviour back; the whole `:core:test` suite must FAIL.

    python3 scripts/mutation-check.py scripts/mutants/slice416.py --check      # anchors still unique?
    python3 scripts/mutation-check.py scripts/mutants/slice416.py              # full run (36 mutants x full :core:test, long)

Result recorded in plans/261008-1800-slice416-parallel-audit-findings/plan.md: 36 mutants, 36 killed.

This is a SNAPSHOT of the sources at commit 91384919: an anchor drifts as soon as a later slice edits the same
line, and `--check` then fails.  Keep it as the FORMAT example (and as the evidence for the claim above) -- a new
slice writes its own `scripts/mutants/sliceNNN.py`; do not "repair" this file to keep it green.
"""
BASE = "rewrite/core/src/main/kotlin/com/acrebuild/core/"

# (id, file under BASE, [(old, new), ...])  -- every `old` must occur exactly once in the file
MUTANTS = [
 ('G1 S184 end gate', 'PlayerFsm.kt', [('if (p.animFinished() || aN == null) {   // `r() || aN == null`', 'if (p.animFinished()) {   // `r() || aN == null`')]),
 ('G2 S184 drag sign', 'PlayerFsm.kt', [('aN.ak = if (p.av) p.ak - 35 else p.ak + 35', 'aN.ak = if (p.av) p.ak + 35 else p.ak - 35')]),
 ('G3 S50 drain', 'PlayerFsm.kt', [('p.gDrain(999, world)        // @4580-4583', 'p.x1 = 0        // @4580-4583')]),
 ('G4 S22 ge release', 'PlayerFsm.kt', [('if (p.S == 22 && p.ge != null) { p.aA = p.aA and -9; p.az = 100; p.ge = null }', '')]),
 ('G5 drift clamp guard', 'PlayerFsm.kt', [('if (p.S == 25 || p.S == 15 || p.S == 19) {\n                p.ag = if (p.ag > 0) 512 else -512', 'if ((p.S == 25 || p.S == 15 || p.S == 19) && p.ag != 0) {\n                p.ag = if (p.ag > 0) 512 else -512')]),
 ('G6 S215 land/handover swap', 'PlayerFsm.kt', [('if (p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5) p.land(world, false)\n            else if (p.S == 215) { p.flingAirborne(0, world); p.av = !p.av }', 'if (p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5) {\n                if (p.S == 215) { p.enterFall(0, world); p.av = !p.av } else p.land(world, false)\n            }')]),
 ('G7 wall clamp first', 'PlayerFsm.kt', [('        world.scrollWallClamp(p)\n    }\n\n    // -- shared fall arm', '    }\n\n    // -- shared fall arm'), ('        p.airWallResolve(world)                         // av()\n', '        world.scrollWallClamp(p)\n        p.airWallResolve(world)                         // av()\n')]),
 ('G8 fall-site snap', 'PlayerFsm.kt', [('            if (p.av) p.W[0] / 20 * 20 + 1 else p.W[2] / 20 * 20 + 19', '            if (p.av) (p.W[0] + 20) / 20 * 20 + 1 else (p.W[2] - 20) / 20 * 20 + 19')]),
 ('G9 S92 fling', 'PlayerFsm.kt', [('if (p.ah == 0) p.flingAirborne(0, world)\n                    else p.enterStateMasked(36, 36, world)', 'p.enterStateMasked(36, 36, world)')]),
 ('G10 S375 sign', 'PlayerFsm.kt', [('p.ag = if (p.av) 1280 else -1280\n                if (p.animFinished()) p.setAnim(376)', 'p.ag = if (p.av) -1280 else 1280\n                if (p.animFinished()) p.setAnim(376)')]),
 ('G11 ap gate gE', 'PlayerFsm.kt', [('else if (p.z && !world.iBn && !Entity.gE) p.contextDispatch', 'else if (p.z && !world.iBn) p.contextDispatch')]),
 ('G12 ap gate bn', 'PlayerFsm.kt', [('else if (p.z && !world.iBn && !Entity.gE) p.contextDispatch', 'else if (p.z && !Entity.gE) p.contextDispatch')]),
 ('G13 knife gate', 'Entity.kt', [('if (S != 79) { ai = 0; ag = 0; setAnim(286); w.sfx(29) }', '{ setAnim(286); w.sfx(29) }')]),
 ('G14 lunge bq', 'Entity.kt', [('entBq = 0                    // @5-6', '                    // @5-6')]),
 ('G15 throw L/M', 'Entity.kt', [('w.spawnProjectile(av, gQL, gQM)          //', 'w.spawnProjectile(av, L, M)          //')]),
 ('G16 HUD lock reset', 'Level0World.kt', [('if (!z2 && weaponCornerArmed() && actionLock == 1) actionLock = 0', 'if (!z2 && weaponCornerArmed() && false) actionLock = 0')]),
 ('G17 corner gate', 'Level0World.kt', [('if (bh3 || !player.groundOrVehicle()) return false', 'if (bh3) return false')]),
 ('G18 followJ k.u', 'Entity.kt', [('if (world.jC != 21 || world.dlgU == 8) return', 'if (world.jC != 21 || world.dlgU == 9) return')]),
 ('G19 flight e() head', 'PlayerFsm.kt', [('        if (world.bh3) { flightTick(p, pad); return }\n', '')]),
 ('G20 flight bh--', 'PlayerFsm.kt', [('        if (world.iBh > 0) world.iBh--\n        // L14-L17 meter drain', '        // L14-L17 meter drain')]),
 ('G21 flight pad words', 'PlayerFsm.kt', [('if (pad.bB == 0 && pad.bC == 0 && p.animFinished() && z4)', 'if (p.animFinished() && z4)')]),
 ('G22 raw ac fling', 'Entity.kt', [('putAc(null)                    // raw `putfield ac` @31-37', 'ac = null                    // raw `putfield ac` @31-37')]),
 ('G23 raw ac as()', 'Entity.kt', [('f.putAc(r0)                              // g.as() @248', 'f.ac = r0                              // g.as() @248')]),
 ('G24 S90 flush', 'PlayerFsm.kt', [('pad.clearLatches()                     // @9768 k.v()', 'pad.eL = 0                     // @9768 k.v()')]),
 ('G25 S38 early exit', 'PlayerFsm.kt', [('                    p.setAnim(43)\n                    keys = false', '                    p.setAnim(43)')]),
 ('G26 S199 bn', 'PlayerFsm.kt', [('p.setAnim(if (world.iBn) 199 else 32)\n                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else -2560', 'p.setAnim(32)\n                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else -2560')]),
 ('D1 shake overload', 'Level0World.kt', [('        kNSet(-1)                                   // D() @299-300', '        // kNSet(-1)                                   // D() @299-300')]),
 ('D2 hints re-armed', 'Level0World.kt', [('        hintPending.fill(true)                      // i.br[] re-armed', '        // hintPending.fill(true)                      // i.br[] re-armed')]),
 ('D3 aw reset', 'Level0World.kt', [('        kAw = 0                                     // i.O() @4-5', '        // kAw = 0                                     // i.O() @4-5')]),
 ('D4 input lock', 'Level0World.kt', [('        player.unlockInput(this)                    // D() @338', '        // player.unlockInput(this)                    // D() @338')]),
 ('D5 k.F', 'Level0World.kt', [('        kF = null                                   // k.F (D() @17)', '        // kF = null                                   // k.F (D() @17)')]),
 ('D6 i.at', 'Level0World.kt', [('        Entity.at = null                            // i.at (D() @173', '        // Entity.at = null                            // i.at (D() @173')]),
 ('T1 ax21 W', 'Entity.kt', [('        if (ax == 21) {\n            // @1103-1172', '        if (false) {\n            // @1103-1172')]),
 ('B1 slide left', 'Entity.kt', [('            r2 = W[2]\n            aT = e(world, W[0] / 20, i9 / 20)', '            aT = e(world, W[0] / 20, i9 / 20)')]),
 ('B2 slide right', 'Entity.kt', [('            r1 = W[0]                                             // @57-70', '                                             // @57-70')]),
 ('B3 L36 return', 'Entity.kt', [('                // trailing `t()` — only the else-arm and the no-embed path reach @322 `t()`.\n                return true\n', '                // trailing `t()` — only the else-arm and the no-embed path reach @322 `t()`.\n')]),
]
