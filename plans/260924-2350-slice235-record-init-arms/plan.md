---
title: "Slice 235 — bespoke record-init arms (ax16/21/29/41/43/51/58/61/66)"
phase: port
status: complete
---

## Goal

Port the remaining bespoke `i.<init>` record-init arms so every shipped
record type lands with the exact fields the original dispatch switch
(i.java:7600) assigns — closing the last gap where the generic
else-arm's `Z[i]=r8[7+i]` + `i(r8[5])` finish diverged from source.

## Source mapping (all proven, fallback i.java)

| ax | label | semantics |
|----|-------|-----------|
| 16 | L595 (:7988) | `az=200`; S∈{31,32,33} → `az=-1` |
| 21 | Lc8c (:9017) | S≤1 only: `aA=0;j=0;az=r8[8];aB=r8[7];Z[0..3]=r8[9..12];Z[4]=r8[24];Z[5..15]=r8[13..23]`; `k.B=r7`; **record mutated to ax48/S=0** and `ad=new i(r8)` spawns the delegate (Ld7c→i(0)); parent lands S=0. S>1 → plain `i(r8[5])` |
| 29 | L10a8 (:9671) | `az=100;aB=800;aD=2;m=2;aC=30;aF=30;n=60;Z[0..4]={r8[4],r8[7..10]}`; S≠30 → `k.aU=r7` |
| 41 | L1172 (:9799) | `az=r8[7]; P|=0x1000` |
| 43 | L11e8 (:9872) | `Z[0..2]=r8[8..10]`; r8[8]≠-1 → `h(k.s(r8[8]))`; `az=r8[7]` |
| 51 | Ldbc (:9213) | `az=r8[7]; Z[0]=0; Z[1]=r8[8]`; S==8 → `P|=0x80`; Z[1]≠-1 → `h+k(k.s(Z[1]))` |
| 58 | L1540 (:10402) | `Z[0]=r8[7]`; Z[0]≠-1 → `h(k.s(Z[0]))` + `P|=0x210`; `az=0` |
| 61 | La53 (:8673) | `az=101` |
| 66 | L17cd (:10819) | `az=r8[9]`; three Z arms on r8[5]: {12,14,19}→`Z[0]=r8[8],Z[1]=r8[10],aC=(S==14?Z0:Z1),Z[4]=r8[7],r8[4]==999→aA=999,P|=0x10`; [6,10]∪[24,28]→`Z[0]=r8[8],Z[1]=Z[0],Z[2]=ak,Z[3]=al`; else→`Z[0]=r8[7]`; all `P|=0x200` |

`h(k.s(x))`/`k(k.s(x))` → `bindScript(w.kSIndex(x))`/`scriptKeyStep(...)`,
returning -1 harmlessly for unlisted script handles (verified: both
helpers tolerate -1).

## Divergences fixed vs the generic else-arm

- **ax51 Z[0]**: else-arm wrote `Z[0]=r8[7]`; the original hardcodes `Z[0]=0`.
- **ax21 S**: else-arm left `S=r8[5]=1`; the original retypes the record
  to ax48/S=0 — parent lands **S=0** and spawns an `ad` delegate. The
  slice-234 test asserting S==1 encoded the wrong behavior and was
  corrected (evidence: Lc8c writes `r8[5]=0` before L1bea).
- **ax29/ax43/ax51/ax58**: Z-fill indices differ from `r8[7+i]`; claim
  binds and P-flags were missing entirely.
- **ax66**: three-way Z-shape keyed on the record anim — the generic
  uniform fill was wrong for all three arms.

## Files

- `NpcFsm.kt` — `initAx16/21/29/41/43/51/58/61/66` appended after
  `initAx32` (:9787-9933), same `rf(i)` guard + `setAnim(rf(5))` +
  `refreshBoxes()` tail convention.
- `Level0World.kt` — nine `else if` dispatch arms (:823-831) before the
  else arm.
- `Slice1Test.kt` — `Slice235Test`: 7 tests on real packs (ax21 retype
  on level4, ax51 Z/sensor on level5, ax66 three-arm split on level6,
  ax29 k.aU latch on level7, ax58 live/dead link on level6, ax16 az
  latch on level5/2, ax61 az=101 on level7). Stale ax21 S==1 test
  removed.

## Gates

- `verify-static-reconstruction.py` → ok:true
- `python3 -m unittest` → 57 pass
- `:core:test` → 1412 tests / 162 suites, 0 failures
- `:android:assembleDebug` + `:gdx:build` → clean

## Skipped / follow-up

- ax2 `Ld7f` (`az=300;P|=0x80;Z[0]=r8[7]`): ax2 records spawn via the
  separate checkpoint path, `bi[2]=1` is invisible — no shipped record
  routes through this switch arm.
- ax48 `Ld7c`: bare `goto L1bea` — the ax21 child needs only `i(0)`;
  no bespoke arm needed.
- Remaining else-arm types all correctly fall through the L1bea finish
  ported in slice 234.
