---
title: "Port slice 47 — ax42 bz() fuse/timer zone + k.F claim slot"
phase: port
status: done
---

## Scope

Port `bz()` (i.java:17428-17523, the ax42 tick arm) plus its ctor init
arm — an invisible fuse/timer zone entity (`bi[42]=-1`, no clip) that
registers itself in the `k.F` claim slot every tick and runs the
`k.aJ` countdown phase machine.

## Bytecode proof

- Init: ctor `S=-1`; `case 42 → L382` (i.java:3589): `P|=16|512`,
  `Z[3]={r8[4] kind, r8[11] uid, r8[12] secs}`; tail → L419 (i.java:3698):
  `W/X/Y` alloc, `W = [ak+r8[7], al+r8[8], +r8[9], +r8[10]]`.
  **No `i()` call reaches the ax42 arm** (javap `<init>` — the only S
  write is the `iconst_m1` at the top; the four `i(I)V` sites belong to
  ax68/ax37/ax70/other arms), so `S` stays -1 → `bz()`'s `S==0` gate
  (`ifne 422`, i.javap:58379+) keeps the fuse dormant until a
  claim-script `i(0)` arms it — same shape as ax6's degenerate-W dormancy.
- `bz()` every tick regardless of S: `P|=16|512`, `k.F=this`.
- `k.aJ` machine (S==0 only):
  - `aJ==0`: kind 0/1 lazily binds `s=k.q(Z[1])` then fires
    `aJ=1, aK=-40, aL=Z[2], s=null, A(9)` when `s.P()` expired (kind 0)
    or `s.P&32==0` (kind 1). Kind 2 fires the same countdown when
    `Z[1]>0` — without the `s=null`/`A(9)` tail.
  - `aJ==2`: `aM += 50`/tick. Kinds 0/1: `aL*1000 <= aM` expiry →
    `bw=-1, bx = aj==7?56:58, l(13)` + `aL=-1, aM=0`; still ticking and
    `a(aS.W,W)` overlap → `k.c(this)`, aJ=3 (collect before deadline).
    Kind 2: expiry → `h(k.s(Z[1]))` script-bind + `k(k.s(Z[1]))`
    step-reset, aJ=3.
  - `aJ==3`: kind 2 only — `ab()`→`aa()` while the bound script is
    active, else `k.c(this)`, `aL=-1, aM=0, bw=2`.

## Ported

- `Entity` interface: `kAJ`/`kAK`/`kAM` (k.aJ/aK/aM fuse fields) +
  `Level0World` overrides.
- `NpcFsm.initAx42` (L382 + L419, explicit `S=-1` faithful) and
  `tickAx42` (verbatim three-phase machine; reuses `findByAw`=k.q,
  `deadRelease`=P(), `overlapStrict`=i.a, `bindScript`=i.h,
  `scriptKeyStep`=i.k, `claimActive`=i.ab, `runClaimScript`=i.aa,
  `kSIndex`=k.s).
- Wiring: `type==42 → initAx42`, `ax==42 → tickAx42`.

## Verification

7 `Slice47Test` cases — dormant registration (`k.F`+P bits, machine
skipped), kind-0 fire-on-expire, kind-0 wait-on-`P&32`, kind-1
fire-on-bind, `aJ==2` expiry fail (`bw=-1`, `bx=58` at `aj!=7`,
`aL/aM` reset), `aJ==2` overlap collect, kind-2 expiry → script-bind
→ `aJ==3` remove+reset. Gates: `:core:test`, verifier `ok:true`,
unittest 57, `:android:assembleDebug` — all green.

## Deferrals / unknowns

- What arms `S=0` on real records (claim-script `i(0)` on the uid, or
  spawn-side call) — `unknown`; the gate is ported verbatim so the
  port behaves identically dormant.
- `k.bx` 56-vs-58 screen payload for `aj==7` (mission-7 variant) —
  verbatim, not mined further.
