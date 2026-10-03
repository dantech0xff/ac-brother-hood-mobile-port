---
title: "Slice 355 — ax37 scroll triggers run as entities (case 37 → al()), k.c removal semantics"
phase: "port"
status: "done"
slice: 355
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:2570-2580
  - reconstructed-project/src/structured/i.java:2900-2926
  - reconstructed-project/src/structured/i.java:5235-5237
  - reconstructed-project/src/structured/i.java:5739-5828
  - reconstructed-project/src/structured/i.java:14444-14446
  - reconstructed-project/src/structured/k.java:2220-2230
  - reconstructed-project/src/structured/k.java:2529-2596
  - reconstructed-project/src/structured/k.java:4563-4587
---

# Slice 355 — ax37 triggers are entities

Phase 1 item 1.7 (G7, ax37) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Finding (proven)

- ax37 records are ordinary `bb[]` entities: init `L633`
  (`i.java:2570-2580`) sets `Z = {r8[15..18]}`, `P |= 512`, and `P |= 16`
  unless the record is parked (`P & 32`); the constructor tail
  (`i.java:2913-2926`) stages the zone `W` and the bound `X`, which `t()`
  returns untouched. The dispatch runs `case 37 → al()`
  (`i.java:5235-5237`) from the entity loop, whose gate is
  `(P&256)==0 && ((au<2 && (P&32)==0) || (P&16)!=0)` (`k.java:2577`).
- So: live triggers tick every frame; parked ones (m2 uid 119, m7 uid 14)
  stay dormant until a script clears `P&32` and then tick only near the
  camera; `P|256` or a script `k.c` removal stops a trigger.
- `al()` (`i.java:5739-5828`): player in S9/S50 → return; holder
  (`k.ah == this`) zeroes R/S/T/U; **zone test first** (overlap for
  `Z[3]==1`, else containment) — a miss releases a holding trigger via
  `k.n()`; then the `Z[2] != -1` link gate (modes 0-2 skip, 3-5 `k.n()` +
  `k.c(this)`); containment claims `k.ah = this` unless another overlap
  holder stands; mask bits write the bounds while `W` overlaps `k.ac`.
- `k.c(i)` (`k.java:4563-4587`): `ah == i → n()`, `F == i → F = null`,
  `bg[as] = -99`, `p()`, and `bb[i] = null` at once — every loop re-checks
  `bb[i] != null` (`k.java:2531-2588`), so a removed entity never ticks
  again that frame.

The port built a separate `ScrollTrigger` list from the records at load
and fired all of it after the entity loop: the loop gates never applied
(parked triggers fired from frame one), scripts could not remove or
disable a trigger, the link gate ran before the zone test (modes 3-5
self-removed with the player anywhere), `S9/S50` was ignored, `k.ah` was a
synthetic stand-in, and a removed trigger left no save-image tombstone.

## Fix

- `initAx37` stages `W`/`X` from the record.
- `Level0World.scrollTriggerAl(e)` is `al()` verbatim on the entity,
  dispatched from `tickNpc` for ax37 (the `defaultArm` tail still runs);
  `k.ah` (`kAh`) is the trigger entity itself, `scrollWallClamp` reads its
  `Z[3]`, `X`, `Z[0]`; `k.ah?.I()` (door arrival) re-runs its `al()`.
- `removeEntity` = `k.c`: releases `k.ah` via `kN()` and `k.F`; both
  entity loops skip entities removed earlier in the frame (and stop an
  entity's `ac`/`ab` turn once it removed itself).
- `ScrollTrigger`, `scrollTriggers`, `scrollHolder`, `fireScrollTriggers`
  removed.

Not in this slice: `k.c`'s `p()` release on removal (the port has two
`p()` versions, `releaseCascade` and `deactivate`; to be unified).

## Tests

- `Slice355Test` (7): boxes from the record; a live trigger fires from the
  loop; a parked one waits for `P&32` to clear; `P|256` and a `k.c`
  removal stop it; S9/S50 no-op; `k.n()` on zone exit; m2 uid 119 and m7
  uid 14 spawn parked.
- `Slice162Test` rewritten for the entity path, plus the zone-before-link
  order (a dead link with the player outside removes nothing).

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test`, `:gdx:test`,
`:android:assembleDebug` green (counts in the commit).
