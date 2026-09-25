---
title: slice 257 — tickAx13 restructure + killTouch pin-hold + checkpoint3→4 bot leg
status: done
---

# Slice 257

## What landed

- `tickAx13` restructure (i.java L147→L440, proven): the
  `p.bM !== e && (p.aA and 64)==0 && e.aA == 0` gate now wraps BOTH the
  L43 grab-scan AND the L400 aG==4 door-linked growth (`e.Z[6] == -1 ||
  linked.isBf()` → `bN++` toward `Z[1]`). Latch arm sets `aA=1`,
  `bM=p`/`p.bM=e`, `p.az=101`, and the aG-variant arc seeds
  (`aG==1` → `bP/bO` shl2; `aG==4` → `bO=0,bP=0`). `r93==0` divide
  guard preserved.
- `initAx13` (L502, proven): `aG = when(r8[4]){1->1,2->2,3->4,else->0}`
  into `Z[0]`; `Z[1]=r8[7]` (segment max), `Z[3]=r8[10]`, `Z[6]=r8[11]`
  (door link), `Z[7]=1`; `bP = aG<<12` (inferred seed — aG4 → 16384 =
  straight-down rigid hang, verified against the uid48 record).
- `killTouch` (i.java Lb3, proven): the close-apex bounce branch
  (`(e.al - p.gy)/20 < 20`) now returns TRUE after the pin snap
  (`p.i(89)`; `p.al=e.W[1]`; `p.ak=W-center`) and adds the missing
  `e.i(24)` + `e.aC=30` pin-hold — the S99 arm's `tail[3]` now runs the
  kill driver exactly as the original.

## Root-cause: the S22 mid-air hover was verbatim, not a bug

`velClampTail` (i.java:20011 — `aa()`'s L341 tail) zeroes `p.ag/ah/ai/aj`
every tick while a claim script owns the player. ax5 uid39 (S8
bound-entity watcher) → `missionResolve` → `eventBind` → script 104
binds and parks at op108 prompt-choice — the original literally freezes
the player mid-arc for a story prompt. Fix on the bot side: when
`w.kC.claimActive()`, answer with `pad.e(M_CONTEXT)` (bit-32 overlaps
`cb[0]=32` via any-bit `padHeld`) → script proceeds → leg passes.

## Bot leg: checkpoint3 → checkpoint4 — PASSING

`Slice245Test` now crosses the checkpoint3→4 span end-to-end on real
input: rope grab → climb → release fling → ax5 prompt answered →
success gate `p.ak > 5860`.

## Test-side corrections (wrong record layout)

`ax13At`/`ax13` helpers encoded a stale 16-field layout with `ag` at
`r8[14]`; real level0.aclv records are 12 fields with the aG variant at
`r8[4]` — mapped `ag → f[4]` as `{0→0,1→1,2→2,4→3}` in both helpers.
The rope-climb test now stills the pendulum first (`bO=bP=0` — the
L92 climb arm requires a still rope; `bP=aG<<12` seeds aG1 tilted).
The S99-bounce assert band-checks the top-edge snap (`|p.al-e.W[1]|≤12`)
since `e.W` resnaps on `i(24)` and the tick tail — verbatim outcomes
(S89 pin, e.S=24, aC=30) now asserted directly.

## Gates

- verify-static-reconstruction: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: 1477 pass
- `./gradlew :android:assembleDebug :gdx:build`: green
