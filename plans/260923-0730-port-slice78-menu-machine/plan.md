---
title: "Slice 78 — menu machine: eA/K/L/m/Q + O/P stack + fail-dialog render"
phase: port
status: done
---

# Slice 78 — the menu machine

The `k.l(12)` fail screen (and every `l()` menu state) runs the game's real
menu machine. This slice ports it verbatim and renders the YES/NO dialog.

## Provenance (structured decompile unless noted)

| fn | lines | semantics |
|---|---|---|
| `eA[][]` | k.java:282 / :8458 | `{2,1,3,32},{11,12,4,6,0,8},{35,36,37},{14,15},{83,84,123,97,5,113,7,87},{106,107,108,109}` — item ids = `bU[]` indices |
| `K(i)` | :5386 | `bw=-1;bv=i;ey=eA[i].length;eD=0` + per-bv `eB` titles + `eC`/`bx` text heights + `eE`/`eF` panel math |
| `L(i)` | :5489 | cursor nav `v(16388)` up / `v(33024)` down, clamp `[0,i)`, `fH/fI` anim, `z(23)` blip gated by `e.a()` |
| `m(i,i2)` | :5472 | cursor resolve — bv==0 skips SAVE-LOAD idx 3 when `Z()` off; clamp len-1 |
| `Q()` | :3576-3940 | `v(131072)` back arm + `v(327712)` confirm arm + 20-case item switch |
| `O()`/`P()` | :3561-3573 | menu state stack `(j.c,bv,bw)` on `dp/dq/dr`, depth `ds` |
| `W()` | :5093 | full teardown on quit (clips/claims/records) |
| `a(z2)` | :5139 | level (re)load — mapped to `reload()` (`inferred`) |
| `Y()`/`Z()` | :5465 | `Y`= `bA[15]==1||bA[14]>0` has-save; `Z()` device picker — stubbed ready |
| L462/L466 | :1775/:1788 | frozen menu procs (fail/dialog; stats + `j.g%6` blink) |

## Semantics verified end-to-end

- `l(12)` → `K(3)` → rows `d(0,14)`=YES / `d(0,15)`=NO under `eC=25`
  "DO YOU WANT TO RESTART?" and `eB=59` "MISSION FAILED".
- First `v(327712)` press = `bw=-1→0` cursor wake (no dispatch); row taps
  hit-test to `bw=i13` + `E(32)` confirm (`inferred` layout — orig rects
  live in the unported draw proc :6020-6120).
- YES (`case 14, eC=25`) on 12/13 → `bx=-1; a(true); bv=0` = retry
  (reload); on a menu → `P(); a(false); az=dD` = continue.
- NO (`case 15`) on 12/13 → `bx=-1; W(); l(2)` = quit to main menu.
- Quit-confirm (`eC=73`) → YES = `P(); W(); l(2)`; back `v(131072)` on
  `bv∈{3,4}` → `z(30); fF=0; ex==8||j.c==14 → P()` etc.
- `l(13)`'s `bx>=0→31` remap + the 31-arm's `bx<0→l(13)` self-exit + the
  edge-eating `v()` inside `l()` are all preserved — the confirm edge on
  the stats screen legitimately takes two ticks to settle (orig same).
- Menu states `{2,3,4,5,6,14,19,28,29,30}` now freeze the world and run
  the generic `L(ey);Q()` frame (`inferred` — orig suspends sim there).

## Stubs / gaps (`unknown`/`inferred`, flagged)

- `e(true)` RMS flush → `saveFlush()` stub; `f.a(str,0)`/`f.b()`/`j.a()`/
  `E(n)` device procs stubbed; `Z()` slot picker → `menuSlotReady()=true`.
- `W()` teardown is a light port (entity pools + claimer released), not
  the full clip/record free-list.
- `y.k()` font measure → `menuTextHeight` = 18px rows (`inferred`).
- Menu render uses LibGDX `BitmapFont` + flat panel — the original's
  `bW`/`y` bitmap-font clips and the L462 row geometry are unported.
- `k.cm` (style flag, case 123) kept separate from the mount `cm` —
  `inferred` they are distinct fields sharing one name in the decompile.
- `k.ax`/`k.az` reused for menu progress bytes (same original fields).
- `bU` string table: corpus-mapped strings proven; unlisted indices are
  `inferred` guesses (save slots 32-34, eC 69/73 texts, etc.).

## Corrected in this slice

- `kBx` init 0 → `-1` (no stats pending) — else every `l(13)` remaps
  into the stats screen.
- `missionWon` now sets on `i==13` too (the remap to 31 previously did
  it implicitly; `bx=-1` flows would have missed it).

## Tests (Slice78Test, 18)

eA corpus, K(3) arm contents, cursor-wake-first-press, YES reload,
NO→teardown+l(2), YES row tap, nav clamp + `e.a()` blip gate, pause-menu
rows, menu states freeze the sim, `Y()` CONTINUE/117 switch, O/P stack
push-pop, stats-screen 31 remap + confirm + bx reset, jG counting,
options item toggles (83/84/97/123), `v(131072)` back pop.

## Gates

`verify-static-reconstruction` ok:true; 57 unittests; 628 `:core:test`;
`:android:assembleDebug`. No CI on repo.
