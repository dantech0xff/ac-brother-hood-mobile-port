---
title: "Port slice 196 — the last g.e() arms (26 states, 18 labels)"
phase: port
status: done
---

# Slice 196 — the entire remaining `g.e()` dispatch backlog, verbatim

Exhaustive sweep closes the `g.e()` table: built the full
`case → label` map from `fallback/g.java:1348-1700` (353 entries) and
diffed against the port's `when`. The 189 undispatched states all
route to L351e — already covered by the `else ->` shared settle arm.
Four states (S59/65/164/211) are bare `goto L353d` — they get explicit
empty arms so `else` can't fling them. What remained was 18 unique
labels, all decoded and ported verbatim here.

## Arms ported

- **S209** (L1320) vehicle ride: `al`/`ak` follow the mount
  (ax66 S11/S12 ride its own coords; ax60 last-frame ak snap);
  ax66-S12 exits to S228 (`Z[0]>0`) or S358; else `i(0)`.
- **S216** (L2f67) attack-jump windup: `ag=∓5120` while `T∈[2,3]`;
  facing probe (`aT`/`aU`) → `a(0)`; `r() → ag>>1, i(217), P|=64`.
- **S217** (L2fd8) dive descent: `T==0 → aj=1536`; floor+no mount →
  `ah=aj=0, x1=0 (g.e(0)), i(50)`; `aZ||aR==5 → bd=1, a(1), P&=~64`,
  velocity zeroed; `r() → i(0)`.
- **S258** (L32b3) wall-perch: UP-edge → `i(259)`; dir edges → `av`
  + `i(261)`; DOWN-edge → `a(0)` + `al+=10` (double-drop verbatim).
- **S259/S261** (L3334) perch launch: `r()` → up (`ag=0, ah=-7680,
  i(263)`) or sideways (`ag=∓3072, ah=-7680, i(264)`).
- **S260/S262** (L323b) perch-entry transitions: same input fork,
  `r() → i(258)`.
- **S263/S264** (L3386) up-launch: `cw=1, aj=2560`; `ah>=0` → 264→266
  / 263→265; `av()` wall resolve; `y()` grab → `ai=ag=0, a(0)`.
- **S265/S266** (L33da) launch descent: `r() → a(0)`; same
  resolve/grab tail.
- **S267** (Lc88) carry-init: `gt=100`; non-ax27/ax10 `af` needs
  `i.ae()` (ported verbatim) or `gt=0, i(0)`; walks to the target
  centre until |dx|≤4 → snap, ax10→`i(291)` else `i(268)`+`af.i(1)`.
- **S268** (Ld69) hostage carry: `ag=ah=0`; body gated on
  `r()||T>=len-2`; markers 102/8; `af.i(2)`+`az=-2` once;
  `k.u()`-edge release → `af.i(1), k.v(), G()`; `v(65568)` handoff →
  `i(270), A(13)`.
- **S269** (Lf19) carry-drop: `r()` → `i(0)+E()+af.i(2)+af=null`;
  tail, no early return.
- **S270** (Lf50) grab-QTE lead-in: `g.g` forced to S133; `r()` →
  `i(271), g.g.i(145), bl=5`; no-`g.g` path → `i(0)` + tail.
- **S271** (Lf94) grab-QTE mash: `g.g` → S134; marker 8; `v(65568)`
  `bl+=2` else `j.g%2==0` `bl--`; `bl<0` → release, `i(0)`,
  `g.g.i(5), aA=1, Q()`; `bl>=10` → release, `P&=~64`, `i(0)`,
  `g.g.aB=0, i(135)`, `k.e(0,aw)` kill credit, `k.o(3)`.
- **S280** (Lc7a) `r() → i(38)`.
- **S286/S287** (La41) knife-aim hold: airborne w/o mount → `a(0)`;
  `v(65568)` queues `R` (286↔287); `r() → i(R!=-1?R:0), R=-1`.
- **S291** (Lb10) ax10 drag-carry: S268's mirror; every exit →
  L353d tail (verbatim).
- **S313** (L92e) bare `return`.
- **S59/65/164/211** bare `goto L353d` → explicit `{}` arms.

## Helpers added

- `carryAvailable`/`carryClaimable` — `i.ae()`/`i.h()`
  (i.java:59138/59097, proven): `standingOn!=null` or a `W∩k.ac`
  ax∈{17,11,23,50,73} entity; ax11/73 need `!P() && aA>=1`.
- `padAnyEdge` — `k.u()` (k.java:20271, proven): `bB!=0` minus the
  two soft-key bits.
- `countKill` on `LevelCellSource` — `k.e(int,int)` (k.java:12222,
  proven): `ap[0]++` when `uid>0 && aj!=7`.

## Conventions applied

- Early-`return` arms clear `cp/cq/ct/cw` so postTail's `cq` jump
  gate can't hijack the edges they consume (the established
  slice-168 workaround for the head's universal latch set).
- `k.v()` no-arg = `world.clearLatches()` (verified: it clears
  `eL/bC/bB/eM/eK/eN` — the full flush, not `eL` alone).
- Asymmetric exits kept verbatim: S269/291 never early-return;
  S270/271 take the tail only on the no-`g.g` path; S268 always
  returns.

## Gates

- `Slice196Test`: 33 tests, all green (full `:core:test` suite green).
- Verifier `ok:true`; `python3 -m unittest` 57 pass;
  `:gdx:build` + `:android:assembleDebug` green.

## Deviations / flags

- `c?.frameCount ?: 0` guards for clipless entities in S268/S291 —
  the original would NPE on `aa.b()` identically; the guard only
  keeps test doubles alive (labelled inferred-null, never reachable
  for real records which always carry a clip).
- `g.e()` is now fully dispatched: every one of the 353 case labels
  has a real arm or the shared `else` settle.
