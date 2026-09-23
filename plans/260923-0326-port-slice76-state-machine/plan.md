---
title: "Port slice 76 — k.l(int) screen-state machine"
phase: rewrite-port
status: done
---

# Slice 76 — `k.l(int)` screen-state machine

## What

Ports `k.l(int)` (`reconstructed-project/src/simple/k.java:2031-2300`,
structured `k.java:1637-1850`) — the game's screen-state machine — as
`Level0World.stateL(i)`. It replaces the ad-hoc `failed`/`won`/
`dialogModal` freeze model with the original's real contract: the
`k.al` world-freeze flag that `i.I()` checks (simple `i.java` :4860).

## Evidence (proven unless noted)

- Preamble: `eG=0; ex=j.c; cZ=0; cb=true; cu=0; fd=-1; fe=0`.
- Arms ported: 27 audio-stop, 9 renderer-teardown preamble, 12/13
  (scroll refresh + `aD=null` + `13&&bx>=0 → 31` remap + `eC=25; K(3);
  eB=59` tail — simple decompile only, high-confidence), 15 (medal
  stamps `cc[] → bA[130..132]`, `any cc==1 → i=22` re-entry,
  next-mission redirect `(aj+1∈fP) && bA[15]!=1 && ex!=10 → i=10`),
  8/21&&j.c==9 → `B()`, 8&&cy==17 → 17, 29→K(2), 30→K(5)+fO=0,
  4→`cU=au`, 28→K(3), 6→`fd=dx?240:60`, 2 (fO=0+E+K(0)+dx=false +
  j.c-switch audio teardown), 14 (`b(true)` if j.c∈{8,21}, K(1),
  ao=an=false, `!e.a()`→fi=-1, `e.b()`), 23 (`eC=19;K(3);eB=70;bw=-1`),
  5/20 stubs (unported text/font arms).
- Commit tail: `al = i∈{13,12,17,16,31} || (i==21 && u∉{8,9})`;
  `cz=true` iff `i==21 && u==8`; `cy=j.c; j.c=i; if(!cz) v(); cz=false`.
- `K(n)` head: `bw=-1; bv=n; ey=eA[n].length; eD=0` (k.java:6956).
- `i.X()` writes `bA[15]=1` — the checkpoint-exists flag the l(15)
  redirect reads (i.java:2077).

## Mapping decisions

- `j.c` → `jC` (init 10 = in-play, inferred). `k.al` → `kAl`.
  `failed` ⇔ `jC==12`, `won` ⇔ `jC∈{13,31}`, `dialogModal` ⇔ `jC==21`,
  `inPlay` ⇔ `!kAl` — all derived now, no independent flags.
- Dialog dismiss (`leaveDialog`) restores `jC = cy` — inferred; the
  original's l()-based return target is unmined.
- `deaths++` moved inside the 12/13 arm (`i==12 && ex!=12`).
- `k.u` (mode 0-10) → `kMode` — distinct from `k.U` (boundMaxY, `kU`).
- `k.fP` = all eight missions unlocked (save system unbuilt, inferred).
- Unported callees stubbed with labels: `z(n)` screen init, `v()`
  (inputReset → `pad.edge=0`, inferred), `e.b()` audio, `b(true)`
  scroll refresh, `ac()/ad()/L()/E()/B()` renderer/mission-init arms,
  `e.a()` bool check → `fi=-1` (inferred).

## Known gaps (flagged)

- `j.g` reset writes are no-ops — `jG` is derived from `tickIndex`;
  the original's separate render-frame counter is unmodeled.
- Screen rendering of states 12/13/15/22/31 unported — the flags and
  transitions are real; visuals pending the `z()`/`b()` draw arms.
- `k.eA` row contents unknown; `K(0)`'s `Y()/Z()` checks unmined.

## Gates

- verifier: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: 620 tests green (19 new `Slice76Test`)
- `./gradlew :android:assembleDebug`: OK
