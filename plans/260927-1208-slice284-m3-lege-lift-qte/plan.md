---
title: "Slice 284 — mission-3 capstone leg E: tower→door→script-692 lift→cp273"
phase: capstone-bot-legs
status: done
---

# Slice 284 — mission-3 capstone leg E

## Goal

Prove the leg from the tower top (end of leg D's carrier ride at x8260)
east through the pillar door-teleport, the capture/platform/zipline
sequence, the script-692 lift ride with its FIRE QTE, and the rooftop run
to checkpoint aw273 @(10873,575) — via `w.pad.e(mask)` + `w.tick` only.

## Fixes landed this slice (both proven)

1. **`kBMark` field alias** (`Level0World.kt`): `k.b(8,level,row,span)`
   (k.java:349-360, proven) writes the lowercase `k.u` map-region slot —
   a write-only bookkeeping latch consumed nowhere — not the camera's
   `k.U` bottom bound, which is armed only by ax37 scroll triggers
   (i.java:7152). The aliased write made every S21 checkpoint poison
   `boundMaxY`. Port now writes `kMapSlot`.

2. **Below-camera fail gated on claim suspension** (`Level0World.kt`
   world tick): `if (!v()) { if (al > k.P + 240) l(12) }` lives in
   `B()` (i.java:1386-1389) inside `aB()` inside the PLAYER tick
   (g.java:5837). While the player is claim-suspended the tick returns
   early, so the check can never arm — that is how the original survives
   script-692's op11 camera pan 650→400 while the player stands at
   (10667,840) waiting for the lift. The world-tick equivalent now shares
   the `claimSuspended` gate; the `v()` in-play predicate still applies
   otherwise.

## Leg E route (verified end-to-end)

Tower top x8260 → east to pillar x8440-8540 (200px) → **ax10-S16 door
pair** aw898 box x8384-8421 × y655-740 (UP held while overlapping,
grounded) → teleports to aw899 @(8546,652) → cp667 @(8557,690) fires →
east corridor → capture/platform/zipline sequence (S65 capture →
padHeld(16396) vault out east) → y840 corridor (designed below-camera
pit — UP held keeps arcs high) → ax5 zone aw749 fires **script 692** at
x~10480: player suspended, camera claim, op105 dialogs (jc21 dismiss
loop), op11 pans, op21 teleports (10558,840)→(10667,840), S274 + op110
links player↔lift aw638, ride up (10787,683)→(10787,715)→(10817,605)
→ **FIRE QTE** (op107 mask 0x20→65568 at key 122; op108 decide at 132 —
miss → fail-branch script 25 → l(12)) → claim release → S149 ledge
shimmy east across the rooftop (10845→10969) → **cp273 @(10873,575)
consumed** → S150 drop down the east face → lands S43→S5→S21 at
(10969,839).

Assert: `cp273.consumed && p.ak >= 10800 && p.aZ` — passed at ~t390.

## Notes / flags

- The bot's FIRE press window is `p.al < 700 && p.ak >= 10600` — the
  lift-ride/rooftop region, matching the QTE's step window (122–132).
- camB stays 640 during the 650→400 pan because `kZ` (script camera
  claim) bypasses the `kM` settle path — the pan IS the script's designed
  "show the lift" beat, not a camera bug.
- Follow-up (deferred): run-state air-walk fidelity gap (S12 crossing
  pits on cells=0) — own slice/verdict when it blocks a leg.
