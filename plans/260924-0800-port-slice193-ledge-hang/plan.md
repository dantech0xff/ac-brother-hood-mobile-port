---
title: "Port slice 193 — ledge-hang family (S56/60/61/62/63 + S203 victim carry)"
phase: port
status: done
---

# Slice 193 — the ledge-hang family, verbatim

The traversal loop's missing half: grab → hang → climb-up. Sourced from
`reconstructed-project/src/fallback/g.java` (the structured decompile
drops the goto bodies here).

## Mining results

- **S56** (L2b3b, case 56): `ah=0; ag=0; r() → u(127999) ? i(65) : i(59)`
  — grab-settle → shimmy (keys held) or hang-idle.
- **S60** (L2421, case 60): ledge-hang. `Q!=63 || u(16388) ||
  (av&&u(4114)) || (!av&&u(8264)) → i(62)` climb-up; `cu = 1` latch
  every tick (the `u()` held-reads — not `v()`).
- **S61** (L2460, cases 61/203 shared): `ag=ah=0`; S61 decrements `aC`
  (grace set to 40 by `al()`). One-shot `aC==0`, front-cell
  `e(ak±10/20,(al+10)/20) <12` with no `g.a` link, or `v(33024)` → the
  drop-release `H(); G(); al += W[3]-W[1]; a(0)` — `a(0)` resolves to
  S43 free-fall with `al+=10`. An `ax43` carrier link blocks the drop.
  S203 (victim carry) skips `aC` and instead runs the `g.h` arm at
  L24f3: missing/dead `g.h` → `G()`; alive → `k.c(ak,al-85,gh.aw)`
  marker + `v(65568)` dumps the victim (`G(); i(204); gh.ak=ak±10;
  gh=null`). Shared tail L255c: `v(16388)` UP edge or `v(4114/8264)`
  toward-wall edge → `H(); G(); i(62)`.
- **S62** (L25b5, case 62): climb-up finish — `r()` → `ak ±10` →
  `a(aO>12 ? 79 : 0, 9)` settle.
- **S63** (L25a5, case 63): `r() → i(60)` — the climb anim hands off to
  the hang. **Fixes the port's inferred `i(0)`** — wrong target.
- **S59/65** (L2989 family): bare `goto L353d` — the default arm is
  verbatim for them; no dispatch needed.

## Fidelity finding: tick-head flag deviation (recorded, not reverted)

Original `e()` head (g.java:617-623) **clears** `cp/cq/cr/cs/z/ct/cu/
cv/cw` every tick; each arm re-arms only what it needs (`l()` head arms
`cp/cq/z`; S60 arms `cu`; fall arms `cv/cp/ct/cw`). The port's head
sets `cp/cq/ct/cw/zz = true` universally — a slice-168 workaround that
fixed `ap()` starvation in pinned states. Side effect: postTail's jump
gate (`cq && v(ACTION_FAMILY)`) reads true in arms that never armed
`cq`, so a `v(16388)` edge inside S61/S60 got hijacked into `i(233)`
instead of reaching the climb arm. Each new arm now clears the flags it
doesn't set (`p.cp/cq/ct/cw = false`) — the faithful per-arm outcome —
with a comment noting the deviation. A systemic revisit (head-clear +
arming the specific arms that need `z`/`cq`) is deferred; tracked here.

## Changes

- `Entity.gh: Entity?` — the `g.h` victim link (S203 arm).
- Dispatch: `56`, `60`, `61, 203`, `62`, `63` arms ported verbatim.
- `Slice193Test` (12 tests): settle→hang/shimmy, S60 UP-climb + `cu`,
  S61 grace-expire/edge-loss/carrier-block drops → S43 + `al+=30`,
  wall-hold hang, UP-edge climb → S62, S62 `ak±10`+settle, S63→S60,
  S203 victim mark+dump (i(204), `gh=null`, `gh.ak=ak-10`).

## Gates

- `:core:test` full rerun → green (1258 tests)
- verifier → `ok:true`; unittests → 57; `:android:assembleDebug`,
  `:gdx:build` → ok
