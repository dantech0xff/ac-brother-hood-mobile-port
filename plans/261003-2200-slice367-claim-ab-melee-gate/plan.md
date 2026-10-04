---
title: "Slice 367 — the L777 melee gate reads i.ab()"
phase: "port"
status: "done"
slice: 367
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/simple/i.java:6251-6255
  - reconstructed-project/src/structured/i.java:4188-4192
  - reconstructed-project/bytecode/i.javap.txt:68769-68793
---

# Slice 367 — a running claim suppresses soldier melee

Found while mining the soft-key paths for the BACK key (slice 368): the
claimer gate helpers in `Entity.kt` disagreed with each other.

## Findings (proven)

1. The soldier family's L777 tail: `if (k.C != null && k.C.ab()) r13 =
   false; if (r13) aB();` (simple/i.java:6251-6255; structured
   :4188-4192).
2. `i.ab()` (bytecode i.javap.txt:68769-68793): `ca < 0 → false;
   cd[0] == true → false; return cK >= 0`, where `cK` is the claim-script
   step (the port's `scriptStep`). This is `Entity.claimActive()` /
   `claimAb()`.
3. The port read `claimLive()` = `ca >= 0 && cd[0] && cK >= 0`: `cd[0]`
   inverted, and `cK` taken from the drag-anchor field (`c()`'s `X[1]`
   snapshot), not the script step.

Effect: during a running cutscene claim (`cd[0]` false) soldiers struck
the player; while a claim was paused (`cd[0]` true, e.g. under the pause
menu or a dialog) they could not.

## Fix

`NpcFsm` L777 reads `claimAb()`; `claimLive()` is removed (no other
caller).

## Tests

`Slice367Test`: soldier S12 frame 1 with the player in the sword box —
running claim → no damage; paused, unbound (`ca < 0`) or finished
(`cK < 0`) claim → damage lands. The tests fail on the old helper in both
directions.
