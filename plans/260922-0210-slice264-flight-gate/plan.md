---
title: "slice 264 — flight L437 gate fix (i.bi || g.E) + mission-1 canyon bot leg"
phase: port
status: done
slice: 264
---

# Slice 264 — flight `i.bi || g.E` gate fidelity fix + mission-1 shaft leg

## What

Two pieces, one slice:

1. **Fidelity fix** — `flightTick`'s scripted-vs-input gate was inverted.
   Source `g.java:14393-14396` reads `if (i.bi || g.E) → L443 scripted arm;
   else → L46c aC()+input arm`. The port had `if (!world.iBi || Entity.gE)`,
   which ran the *scripted* arm at spawn (`i.bi=0`, `g.E=0`): the countdown
   wind `ah = kY*i.aI` (`-7*256 = -1792`) pinned every tick and live input
   was dead — `ag` never banked, `ak` never moved. Fixed to
   `world.iBi || Entity.gE` (`PlayerFsm.kt:2403-2413`). `i.bi` is the
   "sequence owns player" latch (S10 climb-confirm `i.java:33238`, ax64
   grab `i.java:44583`); `g.E` is the claim-QTE latch — neither set at
   spawn, so the input arm correctly runs.

2. **Bot leg** — `bot climbs the mission-1 shaft until the alert stall`
   (`Slice245Test`): faithful entry via `stateL(9)` + the G() loader
   (arms `camResetC()`'s bh3 init `kQ=230, kX=-7` at `pad.w(65568)` past
   `jG=164`), then real input only: hold climb `u(16388)` (ah-768, floor
   `-2048+kY`), bank `u(4112/8256)` toward the side with more open cells
   on the `%260`-wrapped row ahead, drift toward the perch band x≈295.

## Semantics proven this slice

- **`i.bi` writers** (all verified): S10 climb-confirm arm `i.java:33238`
  (`i.bi=1` + `aS.i(4)` + `az=199`), ax64 grab `i.java:44583` set /
  `:44629`/`:44560` clear, init `i.bi=0` (`i.java:7250`).
- **`i.b(r3)`** = the countdown event (`i.aH=1; i.aI=r3; k.aw=0`), fired by
  ax5-S8 records — `i.aI` doubles as the wind factor in the scripted arm
  (`ah = kY*i.aI` → the launch updraft) while `kX /= r3` flips the scroll.
- **`k.aE` alert meter**: `k.aE=100` init, drains every ~7 ticks while
  `k.aH<0` (`k.aH=80` only comes from the ax21 wave arm `i.java:51154` —
  absent from level-1 records) → ~600-700t ≈ 4000px designed flight
  window. `k.aE<=0 && k.aH<0` → stall `i(24)` → `x1=0` → S2 → `l(12)`.
- **z3 tail** (`PlayerFsm.kt:2527-2530`): `ah` decays 768/tick toward
  `kY=-1792` with an exact settle when inside ±768.
- **Launch**: ax25-init `ah=-2560` direct (`i.java:9567`), decaying via
  the z3 tail — an initial boost, not a sustained updraft.

## Test corrections

The 4 slice-180 flight tests set `iBi=true` to reach the input arm —
correct under the *inverted* gate, wrong now. Flipped to `iBi=false`
(the latch is false at spawn; input arm owns the tick), and the glide
test now asserts the z3 decay sequence `0→-768→-1536→kY` instead of the
scripted-arm pin.

## Result

Leg outcome: `deaths=1` at the designed stall, `al` 11963→8192 (3771px
climbed), input live (`ag` banks ±, `ak` traverses 435-530), `jC=12` via
`x1=0→S2→l(12)`. The ~700t meter is the designed leg window — the
mid-shaft perch zone (uid127 ax10-S10 at 295,6959) is the next leg's
target and likely the intended checkpoint boundary.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → all green (incl. 4 corrected slice-180 tests + new leg)
- `:android:assembleDebug`, `:gdx:build` → green
