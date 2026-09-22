---
title: "Port slice 51 — ax7 ejection slot"
phase: port-slice-51
status: done
source: reconstructed-project/src/simple/i.java:2661 (case 7 → L112 init), :5110-5149 (tick L88/L90/L94); g.java:3763 (f); k.java:8442 (bi[7]=60)
confidence: proven (bytecode transcription)
---

# Slice 51 — ax7 ejection slot

Ports the ax7 swallow-and-eject warp volume verbatim — a 2-state FSM.

## Mechanics (proven)

- **S0 idle (L90)**: `W∩playerW && !aS.f()` (the holding-marker check
  g.java:3763 = `p.isHolding()`) → `i(1)` + `aS.i(313)` swallow:
  `aS.P|=64` slot-hold flag, center-snap `ak/al` to the W midpoint,
  `aS.av=e.av`, `ah=ag=aj=ai=0`.
- **S1 swallow (L94)**: every tick keeps `P|=64`, `t()` refreshes the
  slot's own box, re-snaps the player to center, and frame-syncs
  `aS.T=this.T` so the player's anim tracks the slot's. On `r()`
  anim-end → `i(0)` + `aS.a(0)` (resume, `flingAirborne(0,w)`) + eject
  `ag=-2048` when `e.av` else `+2048`.
- **Init (case 7 → L112)**: `az = r8[7]` only — no Z — then shared
  `i(r8[5])+t()`.
- bi[7]=60 → clip60 (2 anims: idle/swallow; 15 frames, 14 rects).
  Level-0 has 3 records: uid12 (1397,506), uid30 (4801,674),
  uid64 (10125,499), all S=0.

## Port

- `initAx7`/`tickAx7` appended to `NpcFsm.kt`; `ENTITY_CLIP[7]=60`;
  init/tick dispatch arms; clip60 converted
  (`pack-3/entry-060-marker-003`); renderer map + `clips[60]` load;
  fixture `60 to Clip.load(...)`.

## Verification

- `Slice51Test` — 5 tests: init/az/clip bind; S0 swallow (all effects
  incl. center-snap + P|=64 + anim313 + zeroed velocities + av copy);
  `isHolding()` gate; S1 hold (`T`-sync, re-snap, P|=64) then eject
  (`i(0)`, `flingAirborne`, `ag=-2048` when av) and `+2048` when not.
- Gates: verifier `ok:true`; 57 unittests; 316 :core tests green;
  `assembleDebug`; emulator boots level 0 `npcs=485` (+3 ax7 records).
