---
title: "Port slice 24 — g.az() interact scan (g / ci / i.at producers)"
phase: port-slice24
status: done
started: 2026-09-22
slice: 24
---

# Slice 24 — `g.az()` interact scan

Ports the per-tick interact scan that produces the three binding slots the
pickup/holding/mount loop depends on: `g.g` (interact target), `g.ci`
(carry target), and `i.at` (mount/assassinate link) — closing the loop opened
by slice 23's `aX()` pickup lifecycle.

## Source

- `g.java:5510-5757` — `az()` full transcription (head gates, maintenance
  arms for g/ci/at, `k.bd[]` scan loop, ax72 mount arm, path-A bind flow).
- `g.java:5465` — `i(i)` eligibility (`J&4 && ax==11 && Z[19]==1` → facing
  +|dx|≤200; `S∈{268,291}`/`aS.S==267` → true; `S==303`→`r()`;
  `S∈{295,357,358}`/`S∈[299,307]` → true).
- `g.java:5434` — `k(int)` mount whitelist
  {0,1,18,19,20,23,24,25,35,36,43,150,157,165,242,243,263,264,265,266,358}.
- `g.java:71,5266` — `J` static action bits via `g.g(mask)`; `var J` clashes
  with `var j` on JVM → field named `gJ`.
- `k.java:6839` — `h(dx,dy)` octagonal distance `(a+b)-(min>>1)-(min>>2)+(min>>3)`.
- `i.java:2407-2467` — `e(i)` Bresenham LOS in 20px cells between W-centers,
  `cell>=12` → blocked, both-major-axis with per-axis end checks.
- `i.java:15294` — `e(x,y)` cell read, OOB → 20 sentinel.
- `i.java:7699` — `P()` dead+release (`aB<=0 → G(); true`).
- `i.java:42,6007` — `i.at` static link (distinct from `aN` lockTarget).

## Ported semantics (proven unless noted)

- Head: `aA&8` hidden → `g=null; at=null; return`; `S∈{270,271}` → return;
  `S==268 → g=null` (rebind each tick).
- g maintenance: ax!=4 && aB<=0; h()>440 or |dy|>=60; ax11+Z[19]==1+behind;
  dead {11,17,73,9} via P(); S∈{295,303} && r() && !(ax4@S30 in-front);
  ax4 S!=30; ax58 → null.
- ci maintenance: aB<=0 → null; behind-facing → null (`inferred` — decompiler
  garbled, ported `!inFrontOf`).
- at maintenance: ax!=11||!P() → range checks (h>440, facing, `W[3]<at.W[1]`);
  drop unless `S∈{277,293,298}`.
- Scan gate L136: all of {g, at, J&4==0→ci} bound → return.
- Filters: P&32 skip; dead skip (releases `ae`); ax4 S!=30 skip;
  partition ax∈{11,17,23,73,29,9}|ax4@S30|ax58 → path-A; else ax72-only.
- ax72 arm: `J&4` + mountable S + `v()` (on-screen-close) + `Z[0]!=3` +
  facing + `h()<440` + (`Z[0]==1` → `h()<Z[3]`) + W-band
  (`e.W[3]<=p.W[1]`, `e.W[1]<=p.W[3]`) + `!losBlocked` + `at==null` → `at=e`.
- Path-A bind: `aB<=0 && ax∉{4,58}` skip; `I!=8 && ax∈{4,58}` skip;
  facing gate (av → dx sign; S∈{268,291} bypass); `d>=best` skip (L224 —
  candidates at/above best are dropped entirely, NOT routed into the bind
  path; corrected mid-slice); nearer → `best=d`, `g=null`; `S==268` →
  unconditional `g=e`; npcKind: `!i(e) && aA∈{0,2}` skip else `g=e` + first
  `ci=e`; non-npcKind: `!i(e) && |dy|>20` skip else `g=e`.

## Wiring

`playerFsm.tick` ends with `interactScan(p)`; Entity gains
`g`, `ci`, `gJ`, companion `at`, helpers `deadRelease`, `h`, `losBlocked`
(with `cellForLos`), `interactEligible`, `mountableState`, `isHolding`.

## Fixes during the slice

- `losBlocked`: `(W[0]+W[2]) shr 1 / 20` parsed as `shr (1/20)` → no shift —
  fixed to `((W[0]+W[2]) shr 1) / 20` on both endpoints + `dx/2` err init.
- Test isolation: `w.npcs.clear()` in slice-24 tests (level-0 records spawn
  ~454 real entities that can bind ahead of fixtures).

## Gates

- `:core:test` — 83 tests, green (8 new slice-24 cases).
- verifier → `ok: true`; `python3 -m unittest` — 57 tests OK.
- Emulator boot — `level0: 637 records, 627x55 cells, npcs=454`, no crash.

## Known gaps (flagged)

- `g.g(mask)` / `g.J` producers unmined → `gJ` stays 0 outside tests;
  the ax72 mount arm is unreachable in live play until a producer lands.
- `ci` behind-facing arm ported as `inferred` (decompiler-garbled).
- `i.at.ab`/`H()` mount internals unported — `at` binds but nothing
  consumes it yet (mount state machine is a later slice).
