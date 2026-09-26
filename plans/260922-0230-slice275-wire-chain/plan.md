---
title: "Slice 275 — wire-chain solve: rail jump-off misfire fix, frontier x8990"
phase: porting
status: done
slice: 275
---

# Slice 275 — crusher corridor wire chain crossed (maxAk 6052→9000)

## Goal
Push the capstone bot's east frontier past the x8118 crusher corridor.

## Findings (proven this slice)

- The corridor floor (y772-800) plus second slab row (y900-919) are death
  strips — no timed foot crossing exists. The only route is the wire
  chain, all verified verbatim in prior slices:
  **ax40 `bx()` zipline (S164, binds `p.ac`) → rail2 ax10-S34 `aUDraw`
  (binds `p.af`, Z[1]==1 auto-dive S157) → lands the x8800 corridor.**
- `aUDraw` (NpcFsm.kt, i.java:10515+ ported) runs on the **draw path**
  (`drawStyleF:4591`): catch `free && inside && W[1] ∈ ry-20..+30` →
  `af=e, al=ry+75, ah=(slope*1024)>>8, ag=±2560, setAnim(164)`.
  `padHeld(33024)` (M_DOWN edge) fires the **jump-off arm**:
  `af=null; al+=40; probeCells; aR<=20→S43`.
- **Root cause of the 200+ deaths at the catch tick:** the bot pressed
  `M_DOWN` every `p.S == 164` tick ("bound carrier: dismount" arm,
  Slice1Test.kt). `padHeld(33024)` reads the fresh-edge word `bB` — the
  DOWN press on the catch tick fired the rail's own jump-off arm,
  releasing the player mid-window → fell → crushed at x8118. Same
  misfire hit the earlier S34 rail aw=56 (x7318/x7584).
- Secondary contributor: an active claim script's wait-mask
  (`w.kC.cb[0]=33024`) held during airborne catch ticks had the same
  effect — the claim arm now silences pad in the wire corridor while
  airborne.

## Changes (test-only — engine behavior already verbatim)

`Slice1Test.kt` capstone bot:
- `p.S == 164 → pad.e(33024)` scoped to `ak > 10000` (finale carriers
  still dismount; corridor rides fall through to the route policy).
- Claim-silence window: airborne (`!aZ`) in x7400..8300 y300..700 →
  `pad.e(0)` so a DOWN wait-mask can't fire the rail jump-off.
- `ak < 10000` wire arm: S164 → RIGHT (ride to auto-dive); S157 → RIGHT;
  airborne in the catch band x7940..8120 y400..680 → hold 0.
- Assert frontier: `maxAk > 5800` → `maxAk > 8900` with the wire-chain
  description. New frontier: posted ax11 guard x8930 before the x8990 wall.

## Verification

- `bot completes mission-0 end to end` — capstone now crosses the whole
  gauntlet: `maxAk=9000` (was 8118), 0 rail jump-off misfires; auto-dive
  + landing verified; deaths only at the x8990 wall vs posted guard.
- `:core:test`, verifier `ok:true`, unittests, `:android:assembleDebug`,
  `:gdx:build` — all green.

## Next slice frontier
x8990 wall kick-climb + the posted ax11 guard duel at x8930; the x7719
stall pocket (S12 wedged at W=[7712,864,7727,920]) also remains.
