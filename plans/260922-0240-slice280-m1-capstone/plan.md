---
title: Slice 280 — mission-1 (flying canyon) capstone bot
phase: capstone
status: done
---

# Slice 280 — mission-1 capstone bot

Mission-1 (pack-7, bh3 flying canyon) won end-to-end by a real-input bot:
spawn → 5 ax24 shrine refills → ax10-S10 perch climb gate → S31 claim-QTE
at the top → `missionWon`. `t=1889`, 2 deaths (designed fuel/leash stalls
→ respawn → continue), all 5 shrine fires, claim `aA=8`.

## Fidelity fix in this slice

`Level0World.kt` claim block gated verbatim: the claimer fast-forward AND
the bh3 `cA=O; cB=P` target snap now sit inside `k.C != 0 && cd[2] &&
cd[1]` (k.java:8954-8997 L49b-L4cf). Previously the snap ran every tick
on bh3 — `camB=camY` each frame halved the conveyor to ~3px/t and starved
the canyon fuel economy (~1500px short of the cp1 window). Slice-245's
verdict updated to the new proven fact: cp1 (418,8360) DOES fire under
the correct gate; respawns land at cp1.

## What the capstone proves (proven refs)

- Canyon route: mid channel is the only lane — divider cols ~24-33
  unbroken rows ~200-498 (pack-7 `entry-001-marker-003.bin` et layer).
- Perch gate S10 (NpcFsm.kt:1279-1361): mandatory — its box [295,521]
  spans the narrowed channel at the rows-347-358 choke. Bind via
  mask-1 while head `dy ∈ (Z[1],Z[0])` → `iBB`/`iBi` scripted crossing.
- `iBi` scripted flight latch (PlayerFsm.kt:2437): bound sim owns pad;
  crossing self-drives ~7px/t on `ah=kY` conveyor.
- `iBB` is a shared flash latch — S20 shrine arm also sets it
  (NpcFsm.kt:7114-7118); perch rebind tolerates stale `iBF`.
- `iBF` hang oscillator (Entity.kt:4660-4686): overlap valid only while
  `iBF ∈ [Z2,Z3]=[0,90]`; L4b2 resets 100/`iBE=30` once the zone ticks.
- `kQ` flight leash (Level0World.kt:2242-2251): hover/claims ride
  [camY+117, camY+230]; the band is the only legal altitude.
- Win chain: S31 overlap → `gE` → M_UP edge → `aA=8` → `bindScript(8)`
  → `missionWon` (medals re-enter jC as 22, so the latch is missionWon).

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → all green (Slice245 verdict updated for the fix)
- `:android:assembleDebug` → clean
