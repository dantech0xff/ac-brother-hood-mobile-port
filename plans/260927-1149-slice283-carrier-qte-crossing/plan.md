---
title: "Slice 283 — mission-3 carrier-QTE crossing (leg D)"
status: done
provenance: proven
date: 260927
---

## Goal
Prove mission-3's (`world(aj = 3)`, pack-9 Florence) mid-map crossing:
the scripted ax43 carrier-ride chain with embedded button QTEs, ridden
end-to-end by a real-input bot (no state pinning at runtime).

## What the mining showed (proven)
- The crossing is a chain of ax5-S8 claim zones each armed with a
  scripts.bin script whose op107 arms a button prompt and op108 branches
  at the prompt key on a scripted block index. The right button during
  the armed window sets `cb[1]=1` (pass); anything else binds the FAIL
  script — a teleport to mid-pit plus `setAnim(50)`, a designed death.
- Chain (pack-9 records.json + scripts.bin):
  - zone uid674 @(6548,802) → script 693: prompt mask `u16=4` → normalized
    `16388 = M_UP` (op107 table, Entity.kt:2491). Boarding move
    (6551,905)→(6611,905)→(6715,835)→(6743,805)+anim295; fail→685. Then
    carrier aw594 auto-binds (`grappleOffer Z[2]>0` → `p.ga = 594`).
  - Bound ride (ax43 arm, NpcFsm.kt:7591): pins `p.ak/al` to the carrier
    X-box center while `p.S < 308`; `padDown(8256)` (RIGHT) → 150% speed
    (13.5 px/t vs 9 px/t); `padHeld(16388)` → release-variant anim only.
  - zone uid847 @(7208,702) → script 848: prompt `u16=8` — 8 is NOT in the
    op107 normalization map, stays `8 = M_TAP_R`; ends the player at
    (7410,650)+anim43 but the bound pin pulls back — transient; carrier
    continues to (7351,845); fail→846.
  - zone uid672 @(8048,755) → script 671: prompt M_TAP_R; ends player at
    (8231,735); carrier→(8131,845) then S7 park → `p.ga` cleared →
    designed release; fail→691.
  - script 918 @zone uid917 removes the carrier (op100 uid594).
- The mid-ride civ gauntlet is a designed attrition check: ax17 panic
  flail T==3 pays a range-free op4 hit when its W sits inside camRect
  (NpcFsm.kt:7857, 7839). Entering bound at x1=30 takes exactly 2 hits.

## Production fixes kept (proven)
- `NpcFsm.tickAx43` L29 ride-arm: `!g.g() || g.a != this` → release.
  `g.g()` is the `x[1]<=0` death check (g.java:4431) — NOT an attack.
  Was `w.playerAttacking()` (attack released the bind — wrong).
- `Level0World` L339 camera speed-follow: `ae.S != 1` → `ae.S == 1 ||
  ae.S == 4` — follow only while the carrier is moving (S1) or in the
  release-variant (S4); parked/staged states no longer speed-follow.

## Test (`Slice282Test.mission3CapstoneLegD`)
- Checkpoint-style start (6520,939) at the zone-674 boarding edge
  (designed boarding position; street-level approach otherwise eats
  unavoidable civ hits and enters the ride below survivable x1).
- Bot dispatch: `w.kC?.aG` picks the QTE button — script 693 → M_UP,
  other ax5 claims → M_TAP_R; between claims `p.ga != null` → M_RIGHT
  (150% ride). No state pinning.
- Passes: boards on UP-QTE, rides through 2 civ hits (x1 30→20), passes
  both TAP-R QTEs, carrier parks, player lands aZ at ~8230,791.
- Scratch removed: all probe prints (S50kill/SCRkill/BIND674/BINDCTX/
  HIT4/DRAIN/TP43/LD).

## Gates
- `verify-static-reconstruction.py` → ok:true
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → all green
- `:android:assembleDebug` → green

## Open
- cp274 @(7552,224) is isolated in air — a later-leg checkpoint, not
  this leg's target. The tower landing (~8230) is this leg's designed
  end; cp274→cp667→cp273→win-fuse aw780 remains for later slices.
