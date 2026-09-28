---
title: "Slice 305 — m7 leg I verdict: under-chamber sealed pit; route is UP via rope → slab → rail → crates"
phase: capstone
status: green
---

# Slice 305 — under-chamber verdict + west-tower route

## Verdict (proven — level7.aclv layer-0 u8 grid + entity/script decode)
- The r96 mid corridor floor is **full solid x0-1599, no holes**; the
  pocket r98-101 beneath it is a sealed void.
- The east exit is doorpost **ax27#308@(1069,1887) S=23** with real clip-48
  box W=[1060,1828,1077,1924]. `fuseArm` (NpcFsm.kt:3068-3160): `Z[0]=0 →
  findByAw(0)=null → P|4096` fire-push only; `Z[1]=-1000<0` kills the
  lever arm — **the door never opens** (no ax58 lever below y1760).
- The only door-opener is **script 311** (block1: post #308 anims 22→23,
  block2: post #309, block3: move ax0#107) — but its trigger u310@(1127,1843)
  sits EAST of the sealed door. Sealed.
- u280@(1265,1975) pocket zone + u306@(1320,1920) boss zone + boss-2
  ax29#251 are all east of the door — unreachable from the west leg.
  **The mid corridor is a verbatim trap pit, not the route.**

## The real route (proven end-to-end by the bot)
Script 352's block1 (`op21 [13,3,19,5]`) teleports boss #251 to (781,1299)
— **the boss comes UP**. The descent is designed as: chimney hole drop →
**auto-grab ax13#37 rope** (single-use, verbatim latch) → release onto the
slab → west.

The bot now runs: rope ride releases at (1579,1423) onto the slab → west
(jumps the x1340-1439 hole) → crosses **cp#233@(1244,1470)** → reaches the
**west tower ledge x300-559,y1300** → mounts **ax10#48 S34 rail@(376,1146)
(→ uid339 link)** at (391,1222) — the S164 rail ride carries east → lands
on the ax4 crates at (721,1255-1299). Then it loops (rail→walk back→rail):
the crate→props→lift climb is the next leg.

## Driver changes (test-only)
- `onSlab` latch (ropeBound && released && aZ && al∈1400..1560 && ak>1060):
  after the rope ride, drive WEST with a jump-cooldown hop to clear the
  slab hole — prevents the previous wander→S6-lift→corridor re-descent.
- Mid corridor driver: `ak∈1020..1090 → jump-east` (over the door; proven
  still sealed — push box is 96px, jump apex 52px, 39px short — left in
  as a dead-end demonstration; the corridor leg no longer gates the assert).
- Verdict flags: `cp233` (x∈1200..1300 && al<1520), `railRide` (S==164),
  `crateTop` (aZ && al<1310 && ak∈640..780).

## Gates
- `:core:test` — 186 classes, 0 failures.
- verifier — `ok:true`.
- `python3 -m unittest discover -s tests` — 57 pass.
- No production code changed (test-only slice; probes cleaned).
