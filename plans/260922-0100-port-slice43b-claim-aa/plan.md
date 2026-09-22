---
title: "Slice 43b — aa() claim-script interpreter"
date: 2026-09-22
status: done
confidence: proven
sources:
  - reconstructed-project/src/simple/i.java (aa() at ~19429, a() big-op at ~20042)
  - reconstructed-project/bytecode/i.javap.txt (op dispatch + consume gates)
  - reconstructed-project/bytecode/k.javap.txt:23619 (bz = u16(header size))
---

# Slice 43b — `aa()` claim-script interpreter

Ports `i.aa()` — the per-tick claim-script VM — verbatim. Entities bind
script blocks (slice 43a's `k.by`/`k.bz` tables) and step a PC-per-block
interpreter that moves entities/camera, plays anims, arms flags and runs
nested sub-switches.

## Ported semantics (proven)

- `cd[0]` early-out; skip-latch `cd[2] && padHeld(0x20000)` → `cd[1]`,
  `k.m(k.ad)` (unless `bh[kAj]==3`), `sfx 23`.
- `scriptStep` (i.cK) runs when `scriptStep >= 0`; increments when
  `cd[1] || !i.bH || j.g % i.aI == 0` (j.g = tick index).
- Per block: type 0 → `k.ab=false`; 1 → `k.Z=!k.aa`; 2 → `r14` = self on
  `claimPositionType() && block 0`, else `k.q(uid)` lookup.
- Group `[key u16][cnt u8][ops]`: consume when `key <= step`; PC advances
  only on consume; all-blocks-done + `cd[5]` → `bI()` release.
- Ops: 11/12/21/25/31 = i16 lerp targets (arm while `step <= key`; op25 +
  claim latch for self; 11/12 → camera-delta unclamps); 13 = 5B block-
  index focus (`k.ae=k.q`, `k.Z=false`, `k.aa=true`); 22/32 = 2B anim id
  (fire when `key <= step`; ax11+139 → `k.e(0,aw)` + `k.o(3)` stats);
  23/24 = 4B `P` flag set/clear + `av` latch eval; 34-36 = lead-uid gated
  arg ops (run iff `k.q(uid)` misses); 37-39 = direct arg ops —
  `r013=(op-34)%3+1` args feeding the 26-case (sub-1) and 12-case (sub-2)
  switch (k.bK marker spawn, k.C release, i.bQ/bD/by/ce/z statics, aS.P,
  k.ae/aT/aV.ad/aU.by/aL writes…).
- Move lerp `r20<0` skip → `r04==1` camera `k.O/k.P`; else per-entity
  `(target-pos)/(r20-r02+1)` delta (halved by i.bH/i.aI slow-mo), carry
  for `(P&512||claimed) ax∈{43,51}` passenger (`g.a`/`g.c` link), `k.bb`
  follower scan (`u()`, `P&256`, `(au<2&&P&32==0)||P&16`,
  `ax∈{11,23,17,9} && s==r14`), `aw==205 && S∈{34,35}` skip-self,
  ax11 `Z[3,4,9..12]` resync + `aA=0`, `t();v()`.
- `i.cO != null` → `aS.b(cO)` mount-align at block end.
- L341 tail: `k.C==this && scriptStep>0 && !(P&512)` → zero aS.ag/ai/ah/aj.
- `runBigOp` stub → `w.kT(op)` (≥100 ops are slice 43c).

## Fixes folded in

- `aeRef`/`kAe` unified — same `k.ae` static (was a stale duplicate).
- `i.z` is `private static` → `w.iZ` (was wrongly writing `g.z`).
- `k.e(r5,r6)` vs `k.o(r5)`: `kStatE(gate)` (always `ap[0]`, gated
  `r6>0 && aj!=7`) vs `kStat(n)` (`ap[r5]++` unless `r5==3 && aj==7`).

## Live fallout = real behavior

- Catenary test: bound script (uid 938 → idx 11) slides gondola +12px/tick
  — first real script-driven travel (ak 7531, al 341 verified).
- ax5-spawn claim + L341 velocity-zeroing = the original's cutscene lock;
  `aZ` support → S233 pre-jump (jump test now asserts the real flow).

## Gates

- `:core:test` 267 tests green (10 new Slice43bTest).
- `verify-static-reconstruction.py` → `ok: true`.
- `python3 -m unittest` 57 OK.
- `:android:assembleDebug` + emulator boot `AcLevel0` clean, npcs=476.

## Honest gaps (inferred)

- `runBigOp` stub consumes only (correct lengths); semantics are slice 43c.
- `spawnStatic(ax,S,x,y)` shape inferred from `i.a(9,47,5,400)` call sites.
