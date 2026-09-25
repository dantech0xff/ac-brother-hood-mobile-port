---
title: Slice 262 — finale win chain verified (fuse → mission-complete)
phase: golden-path bot legs
status: done
---

# Slice 262 — finale win chain verified (fuse → mission-complete)

Closes the level-0 golden path: the bot leg from checkpoint 7 through the
finale — door teleport, channel crossing, '41' road, the uid115 win claim
(script 250), and the uid252 win zone (script 116) — now reaches
`w.jC == 15` (mission complete) end-to-end with real input, no teleports
beyond the game's own door-teleport and script-op lerps.

## Root causes fixed (all proven against the source)

- **`player.aw = rec[1]`** (`Level0World.kt` spawn init): the original's
  record-spawn keeps the record uid on `k.aS` (k.java:17204-17223, proven).
  Without it `findByAw(5)` never resolved and script 250's player block
  (tgt=5) could not lerp the player onto the road.
- **`inPlayV` pit-fail gate** (`Level0World.kt:4837`): i.java:4137-4145
  gates the below-camera kill on `!v()` — falling out of play AND below
  `k.P+240`. The ungated port killed scripted descents mid-cutscene
  (e.g. script 104's gap descent).
- **`kZ` camera-ownership in `kM`** (`Level0World.kt:1980-2004`):
  k.java:5507 `if (k.Z != 0) goto L9cd` skips BOTH the tracking-target
  computation AND the settle lerp (`camCC/camCD` + L331 snap + kAb/rope
  snap-x). The port only skipped the target computation, so the settle
  lerp fought every script-op camera lerp during claims (camY oscillation
  between the script target and stale `camB`). Moved the whole settle
  block inside `if (!kZ)` — claim scripts now get sole write access to
  `k.O/k.P` until op13 hands the camera back to the tracker.
- **Bot input arms** (`Slice1Test.kt` FIN leg): win check now evaluated
  BEFORE the input arms — the win screen (jC==15) fires mid-tick inside
  `w.tick()`, and the previous order let the `jC != 8` cycle arm
  advance past it before the goal check ran. Also added the S164
  ride-pose dismount (`padHeld 33024`, NpcFsm.kt:10505) as a defensive
  arm; the win fires before it is needed.

## Verified chain (marks from the test's `<system-out>`)

- t322 S326 rope catch (bM=93), t330 `kC=115` bind at step 3;
- t351-t453 script-250 walk/ride: S17 → S157 → S164 (`op22` anims),
  `kC.step` advancing +1/tick, cd[9] latched at step 36;
- t454: player's `W[2]` (12133) crosses uid252's zone edge (x12131) →
  ax5-S8 watcher → `eventBind` → `bindContext` preempts uid115
  (`releaseClaim` + `k.c`), binds script 116 → single `op37 0100`
  (`r013=1, r9=1` → `screenL(15)` + `kStat(0)`, Entity.kt:2308) fires at
  `key==step==0` → `jC=15`; uid252 removes itself the same tick.
- The `p.ak > 12131` physical goal is not strictly needed — the win op
  fires on the W-edge overlap, ~11px before the zone's left edge.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → all green (incl. the cp3→cp4 claim leg — the `kZ` fix
  made the scripted camera lerps land exactly)
- `:android:assembleDebug`, `:gdx:build` → SUCCESSFUL
