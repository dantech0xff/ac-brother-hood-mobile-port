---
title: "Slice 260 — cp6→cp7 bot leg: the roof route"
phase: golden-path
status: done
---

# Slice 260 — checkpoint6 → checkpoint7 bot leg verified

## What shipped

`Slice1Test.kt` — new test `bot runs checkpoint6 toward checkpoint7 past
the fence roof`: parks on the '05' roof band (8780,265), drives the bot
east on the roofline, asserts `p.ak > 10010` (checkpoint7 ax2 uid426).

## Verdict

`CP67 checkpoint=true deaths=0 maxAk=10018 minAl=95` — clean run, no
deaths. Bot ran the '05' one-way band (y260-280, x8760-9320) east,
fought roof guard uid534 (S69/S67) and destructibles, dropped to y359
fighting uid515/582, crossed to x10018.

## Route analysis (the wall was NOT climbable)

- The x8960-9020 '20' column (y440-780) blocks the low road outright —
  grid dump `STALLGRID` proved it: solid face, '02' walkway only tops
  it at y420-440.
- S33 is a wall-**rebound** (g.java:1938-2015, proven): dir-held while
  *rising* arms a lip scan; every other case probes and falls
  (`a(43,32)`) or kicks — a single 340px face just slides. Test marks
  showed `S12→S33@783→S5` loops gaining nothing — this is the game's
  intended "not this way" signal, not a port bug.
- The '05' one-way ceiling band at r13 (y260-280) spans x8760-9320 —
  ~180px above the wall top — with a second '05' band at y380-400.
  The prior leg's `minAl=325` near x8650 is the same roofline (rooftop
  pack uid587/537/523 patrol there). The level intends the player to
  stay high through the fence structure.
- Below the '05' band: the 12-bar ax44 fence row (x9026-9567 @y440),
  soldier packs at y355/712/654, cam bounds uid589/462/556.

## Notes

- Respawn/park both at (8780,265) on the '05' band — the band is
  one-way so the player lands on top.
- `minAl=95` means the bot briefly climbed higher than the roofline
  (r10-12 tower top at y200 near x8720-8860) — S33 lip grab onto a
  ledge above.
- Gates: verifier `ok:true`, 57 unittests, full :core:test,
  :android:assembleDebug, :gdx:build all green.

## Remaining legs

- cp7 uid426 (10016,679) → win fuse ax42 uid59 (11410,418) — past the
  ax21/ax10 zone cluster (uid572 S43, uid87/134 S16 doors, uid88 S33,
  uid578 S53), rope uid93 (11312,441 aG=4), heavy guards uid465/45.
- → mission-complete ax5 uid252 (12131,221) through the 7-soldier
  finale pack (uid287-297 @y595-602) — script 116.
