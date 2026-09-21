# Slice 2 — real `g.e()` grounded/air arms + ax11 soldier FSM

Track: `260921-1400-port-slice2-fsm` on branch `devin/1789994018-port-slice2`
(off main `fbc5c1e`, post-PR-#6).

Goal: replace the slice-1 `inferred` placeholder locomotion with the verbatim
`g.e()` arm graph, and make the 53 ax11 soldiers in level 0 actually patrol.

## Ported semantics (all `proven` unless tagged)

Source: `reconstructed-project/src/simple/{g,i}.java` (goto form — the
structured decompile gutted the case-0 family blocks).

### Player (`PlayerFsm.kt`, driven from `Level0World.tick`)
- L682 grounded tail for S∈{0,1,7,11,26,79}: `aO<=12||aR<=12` gate; S79
  zeroes velocity; S11 decays `ag=(ag<<1)/3`; `u(33024)||!am()` → `l()`;
  `!l()` → `a(0)` fall; embedded → `i(79)`.
- `l()` (g.java `boolean l()`): S79 dir-press → `i(bn?199:32)` run-start
  (ag=∓2560); other states: matching-facing → `ax()`; double-tap `x()` →
  `i(10)` dash ∓4096; DOWN+moving → `i(32)` slide-stop; idle → `i(11)`
  brake → `aw()`.
- `ax()`: `i(12)` sustained run `ag=∓2560` (∓512 in 18..23 side strips);
  `aR>18` → `i(26)` edge-walk ∓1280; slope cells 14/15 → `ah=ag>>1`,
  17 → `ah=-ag>>1`.
- `aw()`: settle — airborne → `a(0)`; DOWN → crouch `i(78)` (or ledge-drop
  on the `aQ∈{20,5}&&aR==0` open-forward corner); `tick%100==0` → `i(1)`
  flicker; else `i(0)`/`i(79)`.
- Post-switch tail (`cq && v(16398)`): `v(2)/v(8)` pick facing; standing
  states {79,32,199} → `i(21)` squat under headroom probe; grounded →
  `i(233)`; air+jump → `i(233)`; else `i(22)`. Opposite-tap → `i(25)`
  back-dash.
- Air family {20,21,22,23,25,215,233}: `aj=1536`; S21 → `i(22)` +
  `ah=-5120`/`ag=∓2048`; apex `i(23)`; land probes → `d(aR==4||aS==4)`.
- S43 fall arm; S5 land arm (jump → `i(21)`, dir → `l()`, end →
  `i(aO>12?79:0)`); S6 climb-prep shell; S10 `ag/=2` dash decay.

### Entity skeleton additions (`Entity.kt`)
- `t()` → `refreshBoxes()` (W hitbox / X attackbox, world px).
- `x()` → `probeCells()` (aO/aP/aR/aQ/aS/aV/aW + aZ/bd/v + slope r8 snap).
- `a(boolean)` → `collideSides()` (aT/aU strip maxima, bb/bc wall flags,
  pushout per L69-L77), `y()` → `hitWall()`, `av()` → `airWallResolve()`,
  `a(int,int)` → `enterStateMasked()` (mask bits transcribed),
  `a(int)` → `enterFall()` (S43, al+=10, ah, aj=1536),
  `d(boolean)` → `land()` (cell-top snap, i(5)/i(102)/i(152) variants).

### Pad word (`Pad.kt`)
`k.u()` held / `k.v()` press-edge / `k.x()` double-tap on the mined mask
values (4112/8256/16388/33024 held; 2/8 taps; 16398 action family; 94324
any-dir). `aA` = ~8-tick release window (`inferred` — window length not
recoverable statically).

### Touch zones (`Level0World`)
Top-third press → UP edge; middle-band halves → held LEFT/RIGHT (+ 2/8 tap
edges on press); bottom-third → held DOWN. `inferred` — J2ME keymap →
touch mapping has no original equivalent.

### ax11 NPC (`NpcFsm.kt` — `i.I()` subset)
- `initSoldier()` = L120 record→Z map verbatim (`az=r8[17]`, home `Z[3]`,
  ranges `Z[5]/Z[6]` cells, zone box `Z[9..12]` ← `r8[12..15]`).
- Spawn `i(0)` (`d(i)` case 11, proven). Patrol activation is
  director/script-driven in the original (unmined) → **inferred**: live
  soldier enters `i(3)` on idle-anim end.
- L357 patrol arm: S3 `k=true,ag=∓512,aC=20`; bounds via `(ak-Z[3])/20`
  vs `Z[5]/Z[6]`; `aC` 20-tick legs → `i(3)`+`av=!av` ping-pong;
  `am()`/`aG()` wall/edge nudge path (±3px).
- L451 chase arm: `Q()` face; `aG()` → `i(k?3:2)`; S4 `ag=∓2048` /
  else ∓512; W-overlap → `aC=3; i(23)`.
- L438 S5 attack-commit → `r() → i(4)`; L444 S23 windup-approach `ag=∓512`.
- `b(k.aS)` LOS simplified to: same ±1-cell band + inside Z[9..12] box +
  facing covers player (`inferred` for the stealth-state gates).
- L777 tail subset: `h()` ledge-fall → `i(25)`; gravity `aj=1536` when
  `!aZ`; cell-top land snap.

### Fixes uncovered while porting
- `collisionCell` now remaps `et` 255→0 at query time (proven `k` load
  behavior) — without it `aO==255` looked solid and the L682 gate never
  opened.
- `collideSides` pushout fold corrected to the L69/L75 mirror pattern.

## Verification
- `./gradlew :core:test` — 34 green (incl. new: run uses anim 12 after
  run-start, tap-top jump chain 21→22/23→43→5, soldiers patrol off home,
  soldier alerts on zone-box entry).
- `python3 scripts/verify-static-reconstruction.py` → `ok:true`;
  `python3 -m unittest discover -s tests` → 57 green.
- Android emulator (API 36): boots level 0, Altaïr idles on the rooftop;
  hold-right produces the vault-jump → S12 run on landing, camera follows;
  NPCs render + patrol (see `reports/slice2_boot.png`,
  `reports/slice2_run.png`).

## Deferred (explicit)
- `i.bn` blend path (save byte 79), `am()` ledge-hang, `a(257,8)`
  ledge-drop masked entry, climb `f()`/`i(6)` via `ci` refs, damage/`aB()`
  floatie spawner, `aE()`/`j()`/`k()` QTE & damage intake, palette variants
  (`k.bK` etc. — soldiers still render cyan), `eu` backdrop layer.
