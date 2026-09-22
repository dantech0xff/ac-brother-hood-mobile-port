---
title: "Port slice 44 — ax35 `bQ()` scripted multi-tool entity"
phase: port-slice-44
status: done
---

# Slice 44 — ax35 `bQ()` (i.java:21594-22296)

Ports the largest remaining entity FSM — ax35, the script-spawned
multi-tool (clip 62, `bi[35]=62`). The original `bQ()` dispatch table
maps `S` → label arms; every arm below is a verbatim transcription with
`proven` labels unless noted.

## What ax35 is (evidence: i.java + level/pack records)

A bird-drone/dove director entity driven by an `af`-linked parent
(waypoint/director record `aw == Z[13]`):

- **S0/S29** — bind `af` (`af==null && Z[13]>0 → af = k.q(Z[13])`), hold
  `af.P|=16`, paint the `k.aQ` debug vol, countdown `aC` → `i(1)` +
  `af.i(191)`.
- **S1/S30** — capture the player fire-point (`Z[8]=aS.ak`,
  `Z[9]=(W[1]+W[3])>>1`) → `i(2)`/`i(31)` + `k.A(27)`.
- **S2/S27/S31** — wave director: per `aC` tick spawn the S18 marker +
  `f()` wave child; `Z[12]` wave budget, `aF` gap pacing via `cX={0..4}`.
- **S6..9** — flight sweep: polar march `ak/al ± 20·j.b(θ)/20·j.b(n-θ)`,
  player-hit arm (`overlapStrict W,W` → `i(9)` on S∈{0,1,7}, damage
  `gDrain(GU[weaponSlot])`, `gt=5`), NPC insta-kill over ax{17,11,23,47,
  50,73}, cell-20 bounce → `i(10+bR())`, edge despawn `kO-20/kO+420`.
- **S10..13** — quadrant-turn recoveries → `i(14+bR())`; **S14..17**
  off-camera cull via `b(Y,k.ac)`; **S18/19** one-shot despawn.
- **S20..24** — grabber carry arc: `ae` marker arm, link anim via
  `q(Z[10])`, `ak+=50` carry slide, `g.b`-grab-scan latch (S→260/262),
  `i(24)` release-fling `aS.a(2560)` + `P=128` + `i(20)` re-arm.
- **S25** — child `c` spawner (S28 at `kAe`/`kO+200,kP+120`).
- **S28** — flying-aim proxy: held-D-pad ±10px clamps to `kO/kP` box,
  feeds `af.Z[8]/Z[9]`; `!k.aT → k.c(this)`.

## Corrections applied during transcription

- `a(x,y,w,h,b)` = debug vol rasterizer (`k.aQ`) — `inferred` recorder
  `w.volPaintRect`; the `r14` flag is dead in the decompile.
- `f()` wave child writes `e.aG=r10` on the PARENT before spawning the
  S6-quadrant child (i.java:22176).
- Init arm is the sparse map (i.java:3614-3628): `Z[0..6]=r8[8..14]`,
  `Z[10]=r8[15]`, `Z[11]=r8[16]`, `Z[13]=r8[17]`, `Z[7]=0`, `Z[12]=1`
  → then shared `L395: i(r8[5])` + `t()`.
- `bR()` is a first-match ladder: `[18,36)∪[144,162)→1`,
  `[36,54)∪[126,144)→2`, `[54,72)∪[108,126)→3`, else 0.

## Verification

- `:core:test` — 276 tests green (12 new ax35 tests: sparse init, af
  resolve/cleanup, S1 capture, sweep insta-kill, flight march,
  S26→S27 wave counters, wave-child spawn, S28 aim proxy + `k.c`,
  grab latch + 2560 fling, S14 cull).
- `scripts/verify-static-reconstruction.py` → `ok:true`;
  `python3 -m unittest` → 57 green; `:android:assembleDebug` OK.
- `clip62` converted (`pack-3/entry-062-marker-003`, 51 modules, 34
  anims) + wired into `Level0Game.clips` + both renderer `when` maps.

## Known gaps (flagged in code)

- `volPaintRect` is `inferred` — the original rasterizes a debug Image
  (`k.aQ`); we record the rect without drawing.
- `j.b(j.n-θ)` written as `Trig.sin(Trig.N - th)` — `Trig.sin` takes
  `|a|`, matching the original quirk; `Trig.cos` intentionally absent.
- `e.aG = r10` on the parent inside `f()` is verbatim even though it
  looks like a write-through bug.
