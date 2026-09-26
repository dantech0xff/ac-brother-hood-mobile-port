---
title: "slice 272 — level-0 end-game bot legs re-driven (moat + rope + finale)"
phase: slice-272
status: done
---

# Slice 272 — end-game leg re-drive

Re-drove the four `@Ignore`/stalled bot legs in `Slice245Test` under the
corrected S12 mechanics (slice 271 fallthrough fix). All four now pass;
no production-code changes were needed — every stall was a driver-
behavior mismatch, not a port bug.

## What the re-drive proved (on real level-0 geometry)

1. **`bot crosses the plateau dip to the checkpoint`** — plateau dip
   leg (spawn 2300,460 → checkpoint) under the RIGHT-only driver.
2. **`bot runs checkpoint7 to the win fuse through the tower`** —
   cp7 (10016,715) → kick well → slab → moat → ax22 zone catch →
   S19 eject over the terrace lip → terrace top → door uid87 → teleport
   (S149 fling) → east road y779 → ax13 rope (uid93) auto-lift → over
   the x11340 wall mass → `maxAk=11576`, `goal=true`, `deaths=0`.
3. **`bot fights through the finale pack to mission complete`** — same
   route continuing east; the ax5-S8 watcher box (11448-11526, 503-530)
   on the mass top fires mission complete → `jC=15`, `deaths=0`.
4. **`bot drives mission-complete stats into mission 1`** — continues
   through jC==15 stats → af() browse → G() loader → `jC=8, kAj=1,
   loadedAj=1, bh3=true` with the mission-1 canyon pack live
   (219 npcs, ax25 player record).

## Root causes fixed (driver-only)

- **Moat pinball**: holding UP through the moat armed `aF`
  (postTail producer, PlayerFsm.kt:2085) → `wallGrabSnap` fired on
  terrace-face contact → S101 grab → S36 bounce → ax22 catch → eject →
  loop at (10499,484). Fix: strip UP in the moat band
  (`ak>10380 && al>480`, except kick states) so the zone's S19 eject arc
  (ag=3328, ah=-3840) sails over the lip y440.
- **Z2 road-blocker**: the x10497 soldier is record-spawned with
  `aB=300` (> `BW_MOCK=80`) — unkillable by design; its counter fires
  only while `playerAttacking()`. Fix: never send CONTEXT at it —
  entities don't physically block, the bot walks past.
- **x11340 wall**: a 260px mass face at the east road's end. The
  ax13 rope uid93 (11312,441, aG=4, `Z[6]=-1` → grows once in camera
  range, `bN→24`, tip box ~x11308-11316×y681-729) is the designed
  auto-lift: airborne grab → `ropeInput` decrements `bN` per tick →
  `bN<2` → `releaseRope` → `i(23)` east (ag=2048, ah=-2560) from the
  anchor height y441 → clears the mass top y520.
- Re-enabled `bot fights through the finale pack to mission complete`
  and `bot drives mission-complete stats into mission 1` (dropped the
  `@Ignore`s that were parked on the pre-slice-271 S12 stall).

Scratch dump probes used during the re-drive were removed before commit
(per "don't let code changes slip" — test file only carries the leg
drivers + marks).

## Gates

- verifier `ok:true`; `python3 -m unittest discover -s tests` 57 pass;
  `:core:test` 1490 tests / 0 failures / 0 skipped;
  `:android:assembleDebug` + `:gdx:build` green.
