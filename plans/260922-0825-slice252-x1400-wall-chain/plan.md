---
title: "Slice 252 — x1400 wall crossing via ax22 aerial chain + ax7 wedge (verified)"
phase: golden-path verification
status: done
---

# Slice 252 — bot vaults the x1400 wall via the ax22 aerial chain

## What

Headless bot-playthrough test `Slice245Test` gains the eighth leg:
`bot vaults the x1400 wall via the ax22 aerial chain`. Park (800,879)
west of the x900-1120 building, hold east+up, let the zone chain
carry the player over the wall.

## The decoded route (all verbatim geometry)

- Wall x1400-1480 is solid '14' y360→floor — the level's first real
  gate.
- Spawn (85,940) → pit jump → floor x380-1120 top y880.
- Building x900-1120 rises to y780; its 100px west face is a normal
  face-climb (bot used S60-63 grab/climb states, grabs=10).
- Roof east edge x1120 y780 → jump → apex ~y690 reaches
  **ax22 zone1 (1214,636)** catch box `[1208,626..1248,669]`
  (clip-14 rect `[-6,-10,34,33]` rebuilt each tick via the `I()`
  preamble `b=1` + L1f35 `t()` tail, i.java:15250).
- Zone1 S1 ejects on direction edge: `ag=+3328, ah=-3840` east (Z[2]≠0).
- **ax22 zone2 (1316,568)** catches the arc `[1310,558..1350,601]` →
  ejects again → **ax7 ejection wedge** catch box `[1318,456..1334,472]`
  at the wall top edge → throws the player over.
- ax14 pickups (969,654)→(1341,477) breadcrumb the exact arc.

## Observed

`WALL crossed=true jumps=3 caps=2 grabs=10 minAl=456` — the player
climbed the building, jumped off the roof, was captured by both ax22
zones, hit the ax7 wedge box at y456, and crossed x1480.

## Fidelity notes

- ax22 W is not record-derived — it is rebuilt from the clip-14 rect
  by the shared `b=1`/`t()` tail each tick (i.java:15250 + L1f35).
  Zones parked (`au<2` + `P&32`) keep `W=0` until they wake — faithful.
- The "sealed wall x≈1379" demo note and the slice-251 post-death
  stall were the bot lacking this route, not a dead end.
- Same mechanic class as the east-corridor ax22 chain proven in
  slice 247.
