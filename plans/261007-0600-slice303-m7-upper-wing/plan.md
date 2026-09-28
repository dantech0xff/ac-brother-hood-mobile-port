---
title: "Slice 303 — m7 leg H: u252 release → lift band → r96 under-chamber"
phase: capstone
status: green
---

# Slice 303 — m7 upper-wing descent

## Leg
From the u252 claim release (~x410,y1299) descend the east wing to the
boss-2/top route: y1300 gap → ax66 sink lifts → '2'/r78 → drop past the
r87 gap → r96 under-chamber. Assert `u269Fired || (al>1760 && minAl<1200)`.

## Decoded geometry (proven — level7.aclv layer-0 grid dump)
- r78 is solid x0-1119 with the hole x1120-1499; below the hole sits
  r87 (platform x920-1599) sealed between the west column
  (cells r79-82 + ax27#309 prop W=[1043,1660,1060,1745]) and the east
  tower (x1500-1599, top y1600, abutting the level edge) — a trap with
  no walkable exit.
- The ax66 sink-lift row (x640-1058, y1500) carries the player down to
  r78. The lift bind zone (~80px wide, y1500-1520 hover) re-captures a
  standing player: the 56px hitbox head pokes into the zone.
- Crouch-walk (36px box, head ~y1523) clears under the bind zone —
  verified end-to-end: ride lift #24 → crouch west → ledge-drop at x879
  → falls through the r87 gap x680-919 → al>1760 reached mid-fall to r96.
- Airborne drift must always be WEST post-release: east drift enters
  the r78 hole (x1120+) → the trap.

## Production change (proven)
`groundedTail` L682 restructure (PlayerFsm.kt:1593+, g.java:2851-2855):
`aO>12 && aR>12` only re-sets the crouch anim — it does NOT gate the
tail; `l()` locomotion runs every grounded tick, so S79 still walks and
drops off edges. (Earlier the gate swallowed locomotion while embedded.)

## Proven assertions
`mission7UpperWing`: chainDone → descent → `u269Fired || (al>1760 &&
minAl<1200)`. Test-only.
