---
title: Slice 307 — m7 capstone leg K: west descent (corridor → pit → deep floor → shimmy → far west)
phase: port
status: done
---

# Slice 307 — m7 capstone leg K: mission7WestDescent

## Verdict (proven by `Slice307Test.mission7WestDescent`, 200 ticks)

From the leg-J landing on the east corridor floor (1270,1250):
1. **cp339** at (1266,1244) — script-341 dialog pair (uid342) fires.
2. **Pit drop** at corridor-floor west end x1040-1219 → deep floor
   y1500 (the under-floor region below the west floor).
3. West along y1500 → **claim-QTE kC=240 at x791** — mount chain
   S273→S277 carries the player west to x637 (proven live).
4. **'5' ceiling underside** x34-270@y1310 — shimmy states S280/38/37
   carry him west under the west floor's slab.
5. Drop at x34 → **far-west region floor y1559** — leg complete.

## Map facts established this leg (probes, all proven)

- East corridor is a **dead end**: fuse box uid51 W=[1462,1199,1479,1284]
  (P|4096) arms S10→8→4 permanently — auto-vault loops at x1449
  west→east; passable only descending the chimney at y1259 (one-way).
- Grab-lift map: ax66-S12 Z[5]=100 catch zones at uid219@(1117,1302),
  221@(704,974), 222@(624,976), 223@(522,940), 224@(643,524),
  225@(723,521); counterweight pairs ax66-S23+ax72 at x510/866/1300/
  957/386.
- West column shaft x340-819, y860-1299 (floor slab y1300-1339 at
  cy65-66); mid-block x340-459@y900-979; ledge x420-779@y820-859;
  tower mass staircase x860-1219@y760-919.
- Arena band y560-579 x340-1599; arena floor y520-579 x1040-1619;
  pillar x340-359 tops at y540 (band level); east wall x1560-1599
  y600-1279; boss-3 uid307@(1377,238) dormant S30 until script-316;
  ax10-S55 uid315@(1380,520) = boss-reposition zone (k.aU), uid234
  claim @(782,368); win fuse uid303/uid271 script 304.

## Remaining legs (next slice)

west floor → mid-block/lift chain (uid221-223 catch falling riders;
unbound dive `ag=±4864, ah=-6656` onto ledge x420-779@y820) →
pillar x340-359 top → band y560 → x1040 vault → arena floor →
cp347@(1131,489) → uid281 script-316 arena trigger + S55 boss
reposition → boss-3 uid307 → fuse u303/u271 → missionWon.
