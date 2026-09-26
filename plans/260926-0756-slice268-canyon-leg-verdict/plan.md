---
title: "Slice 268 — canyon gap-leg verdict + claim-suspension player gate"
phase: gameplay-mining
status: done
---

# Slice 268 — canyon gap-leg verdict + player claim suspension

## What this slice proves

The bh3 flying canyon's fuel economy is **camera-paced**, and the
6016→2925 shrine gap cannot be crossed on a single 100-point tank under
the verbatim model — three independent lines of evidence:

1. **Conservation arithmetic.** `ah` relaxes to the conveyor
   `kY = -1792` (-7 px/t) unconditionally (g.java:6290-6297 settle arm);
   the UP input arm caps at `-2048+kY` (-15 px/t) and DOWN at
   `+2048+kY` (+1 px/t). The camera is a fixed metronome — `cB += kX`
   (-7/t) then `camY += l(camB-camY, 30)`, with the L142 tail snapping
   `cB=P` — i.e. camY moves ~3.5 px/t no matter what the player does.
   Shrine entities tick only while `au <= 1` (screen-distance gate,
   i.java:700-800), so a shrine's window opens `leg_px / 3.5` ticks
   after the previous window closed. Fuel cost per leg is therefore
   `leg_px / 21` regardless of player speed or policy. The gap
   6016→2925 = 3091 px ≈ ~147 fuel > 100 tank.

2. **Mechanism elimination.** Every alternative is absent from the
   level-1 ACLV or proven dead in source: no ax21 fuse director, no
   ax10-S44/45 wind zones, `aq/ar` waypoint override is never armed
   (probe: `aq=-1, ar=-1`), `dU` world-shift requires a zero-entity
   tick (never happens), op100/scripts 26/96 dead, `i.bB` is an
   abort gate not a drain gate.

3. **Empirical probe** — the new verdict test parks the player inside
   the 2925 shrine's W box with a full tank and the camera snapped to
   the 6016-respawn state (k.C() snap verified). The tank hits zero
   (jC==12 starve) while the camera is still ~1245 px short of the
   shrine's tick window — the assertion `deadCamY > shr.al + 400`
   locks the bound.

Whether the original J2ME mission-1 was completable cannot be proven
statically — if a surviving mechanism exists it lives in data the
decompile lost. The port keeps the proven-faithful economy and locks
the outcome as a regression contract.

## Fidelity fixes folded in

- **Player claim-suspension gate** (Level0World tick): while
  `k.C.claimAb()` or a u9 dialog suspends the world, the player skips
  `collideSides + fsm.tick + integrate` but still `advanceAnim` —
  verbatim `i.I()` L108 for entities without `P|512` (claimant, ax8,
  ax24 exempt). This is what lets bound rides drive `ak/al` via `aa()`
  without `g.n()`'s deadly-band probes running.
- **Shrine stash field fix** (NpcFsm tickAx24): refill un-halves the
  conveyor via `i.aJ` (the halving stash, g.java:6321-6333), not the
  `k.aJ` fuse phase — was un-halving the wrong field.
- **Mercy off-by-one** (PlayerFsm drain arm): conveyor halving fires at
  `aE < 25` (g.java:6317-6333 gates both `aE>25` and `aE>=25` to skip) —
  was `<=`.
- **Record anim on respawn** (Level0World respawn): player gets the
  record's own `f[5]` (4 = flyer for ax25) instead of S0 — S0 latched
  `z4=false` and killed the auto-flap gate, which is why early probes
  showed refills dying.
- **Debug scaffolding removed**: `dbg*` fields + `dbgLastBe` capture
  from Level0World/Entity, scratch probe classes from Slice1Test.
- **`P512 zones` test fix**: under the new player gate a suspended
  player no longer falls into a capturable state — pin S=43 airborne
  and refresh W (the zone's `g.b(S)` gate rejects grounded S=0;
  faithful).

## Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt` —
  respawn record anim, `iBe=false`, claim-suspension gate, dbg removal.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt` —
  `kAJ→iAJ` stash fix in tickAx24 refill arm.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/PlayerFsm.kt` —
  mercy `<25` fix + drain comment.
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  verdict test replacing the uncompletable capstone; P512 test fix.
- `.agents/skills/android-emulator-testing/SKILL.md` — repo skill.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → 1488 tests, 0 failures
- `:android:assembleDebug`, `:gdx:build` → green
