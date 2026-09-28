---
title: Slice 291 — mission-6 capstone leg A (Pantheon, aj6/pack-12)
phase: capstone-bot
status: done
---

# Slice 291 — mission-6 capstone, leg A: spawn → ax5 uid116 @(801,930)

## Goal

Prove the first leg of the mission-6 capstone end-to-end with real input
only: spawn (19,658) → first `ax5` milestone `uid116` at (801,930), inside
its claim zone `[801,930,1001,973]` on the underground pit floor.

## Route discovered (cell-grid probes + box-height dump)

The west-slab → cavern crossing had three previously-misread blockers; the
definitive structure (CELLS grid probes on `world(aj=6)`):

- Tower x720-840 (cols 36-40) at r20-28 (y400-580) sits directly ON the
  east slab — mounting over the gap is impossible (east lip `e(36,28)=20`
  fails `ledgeLipGrab`'s open-above check).
- Gap x660-720 (cols 33-35, 60px) from y580 down to the corridor floor
  y740.
- **Stub cols 36-37 (x720-760) at r32-34 (y640-700)** — a 40px passage
  under it at y700-740 leading into the cavern x760-1080.
- Pit opening at x800+ → pit floor r47-48 (y940) — the milestone's home.

### Solved mechanic: the crouch-crawl

`BOXH` dump: `S79/S80` box = **39px** (top 701 > stub floor 700) — the only
stance that fits the 40px passage; all standing boxes are 48-78px.

`groundedTail` (PlayerFsm.kt:1593): `aO<=12 || aR<=12` → normal path, else
`setAnim(79)` — sandwiched (solid above+below) auto-crouches.

`lShared`'s `pad.u(M_DOWN)` arm (PlayerFsm.kt:1739): `ag!=0` → `ag=0;
setAnim(32)` brake; else `aw()` → ledge-check fails → `setAnim(78)` dip.
**Direction keys route to `ax()` first** — the dip only fires on DOWN
alone (no LEFT/RIGHT held). S79's own `l()` arm uses the direction key to
`setAnim(32)` + `ag=±2560` crouch-walk; S32's case returns to S79
(`Q==79`) → repeatable crawl at ~60px/10tick.

So: DOWN alone brakes the run into S78→S79; once crouched, RIGHT arms the
S32 crawl under the stub; emerge x760+ → stand → walk to x800 → drop
into the pit → uid116 zone.

## Test

`Slice291Test.mission6CapstoneLegA` — `chaseMask291` driver, real input
only (`w.pad.e(mask)` + `w.tick`), checkpoint/world state untouched.

Mask additions vs the m5 pattern: `78/79/80/32 → DOWN+RIGHT` (crouch
family); grounded in `ak∈(680,800) && al>700` → `DOWN` alone for the dip
(the `S∈{33,36,60,61,101,203}` exclusion keeps wall-kick arms intact);
elsewhere the existing RIGHT+UP auto-vault east.

Verified: `reached=true marks=[M801@t206]` — the bot mounts the approach
platform (S61→S62 @(280,659)), mounts the west slab @(440,579), drops
into the gap, lands (685,739), dips S78→S79, crawls S32 under the stub
(685→755), emerges x805+, drops into the pit at (815,762), lands
(915,938) — **inside uid116's zone; `kC=5` (the ax5 claim fired)**.

## Provenance notes

- Waypoints confirm the intended route: uid210@(700,575) gap mouth,
  uid18@(702,709) gap floor, uid211@(830,731) inside the cavern
  (records.json pack-12).
- ax58 entities uid32@(770,936) S2 + uid572/573/592 S13 guard the pit
  floor — crushers/levers for later legs.
- The test's earlier loose assert (`al <= 940`) fired on the corridor
  floor before the pit drop; tightened to `al >= 920` (pit-floor band).

## Cleanup

Removed all debug probes from the solve session: `m6GridProbe`,
`m6WallProbe` (CELLS/BOXH/WP/BH/LIP prints), the `LIPPROBE` println in
`Entity.ledgeLipGrab`, and four stray uncommitted hunks in Slice245/282/289
tests. Diff is only the chaseMask291 + Slice291Test addition.
