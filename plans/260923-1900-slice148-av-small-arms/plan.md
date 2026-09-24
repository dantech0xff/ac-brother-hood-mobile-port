---
title: "Slice 148 — aV() small-zone arm batch (16 arms + 3 dead)"
phase: port-slice-148
status: merged
---

# Slice 148 — aV() small-zone arm batch

Batch-ports every remaining small `aV()` trigger arm on ax10 zones —
17 proven arms in `NpcFsm.tickTrigger`, bringing the `aV()` dispatch
coverage to 39 of the ~60 reachable zone states (S9/S19/S39 documented
as verbatim bare-`return` arms — no port needed).

## Arms ported (proven, file:line in `i.java`)

| S | label | semantics |
|---|-------|-----------|
| 4 | L15b9 | dialog zone: overlap → `k.aB=k.d(1+k.aj,aF)`, `k.aC=-1`; leave + `aC<=0` → `k.aB=null` |
| 5 | L15e8 | overlap + (fire `u(16388)`/`v(16388)` or facing dir-held) → `i(22)` |
| 6 | L162e | overlap → `k.aZ=1` (save byte 68) |
| 7 | L1643 | overlap → `k.aZ=0` |
| 12 | L175c | overlap → `k.n(aF)` + `k.c` |
| 13 | L1778 | unconditional `k.c(this)` |
| 14 | L1659 | `af`=ax69 ∧ Z[0]∈{0,1,2} ∧ overlap → settle target+player; Z[0]∈{1,2} pins `ak` center, else hurl (`ag=∓3328`, `ah=-6656`, `i(243)`); `af=null` + `k.c` |
| 21 | L1855 | overlap → `k.b(8,1+k.aj,aF,p)` → `k.l(21)`; `k.x=48` + `k.c` |
| 22 | L157d | overlap → `aF<0→k.w()` else `z/A(aF)` (the `aF==2` `k.z(2)` arm is verbatim redundant) + `k.c` |
| 23 | L1889 | `o`→ax11 ∧ overlap → `cq=P()?0:1` else `cq=0` |
| 28 | L1aa7 | enter `gg==null∧aA&8==0∧overlap∧Z0==0` → `aA\|=8,z=0,Z0=1` (`i.bn`→`az=-1`); exit inverse (`az=100`) |
| 29 | L12e4 | release-zone: `o` member P32\|P128 wait → `p/aG/ay` `at()` gate → `P&=~32`,`G()`,`bi!=-1→P&=~128`,`k.c` |
| 32 | L18d1 | balance-hold: `ac` bind (`i(38)`+`k.v()`), arc `al=cy+10*(hw-\|dx\|)/hw`, `ah=0` |
| 41 | L1a03 | `ga`=ax51 ∧ overlap → `k.c` |
| 42 | L1d75 | overlap → `k.c` |
| 44 | L1a59 | overlap → `k.W=k.V-aE` + `k.c` |
| 45 | L1a79 | overlap → `bh[k.aj]==3→i(34)` else `k.l(12)` + `k.c` |

Dead arms (verbatim `return`): S9=L1658, S19=L15b8, S39=L1a02 —
documented in `tickTrigger`'s fallthrough; no code needed.

## New seams

- `LevelCellSource`: `kAZ` (k.aZ, persisted byte 68), `kV` (k.V), `kBQ`
  (k.bQ dirty flag), `kBMark(slot,level,row,span)` (k.b :350 — `row==-1
  → false`), `audioStop()` (e.b :87 = `k.w()`), `audioTrackPlay(n)`
  (e.a(n,false) :50 = `k.z()`/`k.A()`).
- `Entity.atDone()` — `at()` i.java:6174 (`ax==11 ∧ S∈{0,85,20,176} →
  false; else P()`), the S29 member-settle gate.
- `Entity.cr` pool field (added earlier this block) used by the S30 arm.

## Fixes folded in

- `claimAb()` vs `claimActive()` — the S17 arm switched to `claimAb()`
  (the `cK>=0` variant that matches `ab()` i.java:18914).
- `k.u` JADX collision noted: `kBMark`'s `u=slot` writes the same
  original static the port exposes as `kU` (boundMaxY) — faithful.

## Gates

`python3 scripts/verify-static-reconstruction.py` → ok; 57 unittests;
`:core:test` 1040+ tests green (25 new Slice148); `:android:assembleDebug`,
`:gdx:build` clean.
