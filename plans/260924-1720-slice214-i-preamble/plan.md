---
title: "Slice 214 — I() preamble: y-freeze, s() advance, m() reset, L108 dispatch suspension"
phase: rewrite
status: done
---

## Goal

Port the `i.I()` per-entity preamble (i.java:15165-15262, proven) verbatim
into `Level0World.tickNpc`, in order:

1. `j.c == 14 → return` (pause screen)
2. `ax == 21 && k.C != null && k.u == 9 → return` (director under u9 dialog)
3. `if (!k.al || aa == k.z[12])` anim block:
   - `y > 0`: `y < 100 → y--`; `y == 0 → y--` (wraps to -1). `y >= 100`
     never decrements — the `aOp` latched-freeze sentinel.
   - `y <= 0`: `if (!i.cu && S >= 0 && (!aH || j.g % aI == 0)) s()`
4. `if ((k.C == null || !k.C.ab()) && ax != 0) m()` — `i.m()` is
   `{ y = 0 }` (i.java:11774), so the y-stall only persists while a claim
   suspends the world.
5. `b = 1` (La5)
6. L108 dispatch-suspension gate: while `k.C.ab()` (or `j.c==21 && k.u==9`),
   only `P|512` entities, the claimer itself, and ax8/ax24 still dispatch —
   everything else returns.
7. `i.cu && ax != 10 → return` (L109 world-freeze)

## Why

The single-`s()` model: the original advances each ticked entity's anim
once in `I()`'s preamble (:15232). The port had 12 compensating
`advanceAnim()` calls inside individual procs — every proc that ticked
through `tickNpc` double-advanced, and `tickAx7` had none at all (the
deadlock: its S1 release arm gates on `animFinished()`, which never
arrived → ax46 traps stuck mid-cycle, ax7 mouth-plant froze the player at
vault apex). All 12 internal calls removed; preamble is now the only
advance on the ticked-entity path.

## Changes

- `Level0World.tickNpc` rewritten with the full preamble above.
- `NpcFsm`: 12 proc-internal `advanceAnim()` calls removed (tickDoor tail,
  soldier tail, tickDestructible, tickDecor, tickPickup, tickRequestMarker,
  tickDirector, tickAx54, tickAx56, tickAx58, tickAx43, tickAx74).
- `Slice1Test`: `settleIntro(w)` helper — the spawn-intro claim binds
  `k.C` in *phases* (~70 ticks each, ~140 total) even with auto-dismiss;
  the helper fast-forwards until `kC == null` for 40 consecutive ticks.
  18 existing tests now call it before asserting post-intro behavior.
  Direct FSM-drive tests prepend `d.advanceAnim()` (mirrors preamble
  order); the ax35-cull test captures the record pre-settle (claim
  suspension holds its dispatch frozen, then the first free tick runs the
  verbatim cull); the ax4-burst test uses baseline deltas because a second
  ax4-S5 record legitimately bursts during settle when the idle player's
  X box overlaps it.

## New tests (Slice214Test, 4)

- ax7 swallow → S1 release: ejection slot captures the player, anim
  advances via preamble, `animFinished()` releases + flingAirborne.
- `y` latch: `y=101` never counts down (sentinel), `y=1..99` counts down
  and wraps to -1.
- Claim suspension skips dispatch AND the L1f35 tail (corrupt-W stays
  corrupt while suspended; rebuilds on release).
- `P|512` entities still dispatch under suspension (ax22 zone captures).

## Gates

- verify-static-reconstruction: `ok:true`
- python unittests: 57 OK
- :core:test: all pass (incl. the 3 regressions fixed above)
- :android:assembleDebug, :gdx:build: clean

## Known gap (flagged)

`F()` (i.java:11782) holds the only other `s()` call sites (:13043/:13047)
for `P&32 && !P&16` parked entities — a disjoint set from `I()`'s ticked
path. `F()` is unported → parked props' anims never advance. Noted for a
future slice; no regression (pre-preamble the set also never advanced —
proc-internal `s()` ran only for ticked entities).
