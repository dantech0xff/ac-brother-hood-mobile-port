---
title: "Slice 67 — ax76 bO() hazard/damage volume"
phase: port
status: done
---

## Scope

Port `i.bO()` (i.java:21407–21475) — the invisible damage/hazard volume
entity (pack-3 slot 56 is a zero-size entry → clipless by design) — plus
its init arm (`a(boolean,int)` L361 :3546–3552, sharing the L392
`i(r8[5])+t()` tail).

## State map (all proven, i.java:21407–21475)

- `S=0` (L5): if `overlap(e.W, p.W) && p.aZ && p.g==null` → `i(1)`; and
  when `Z[1]==1` (L55 arm) bind the player as trap victim:
  `p.aA|=8; p.az=e.az-1; p.ge=e`. Otherwise the L15 arm runs: release
  the bind iff `p.ge==e && Z[1]==1` (`p.aA&=-9; p.az=100; p.ge=null`).
- `S=1` (L21): no longer overlapping → `i(0)` + same release.
- `S=3,5` (L29): `overlap(e.W, p.X)` (player attack box) → `i(S+1)`.
- `S=4,6` (L33): `r()` → `k.c(this)` despawn.
- `S=2` (L37): `aC>0→aC--`; at `aC==0 && o!=-1` chain-activate the
  sibling (`k.q(o)` → `r0.ax==76 && r0.S!=2 → r0.i(2)`). Then
  unconditionally each tick while overlapping:
  `p.gDrain(g.u[k.au])` — damage is throttled internally by `g.d`'s own
  `gt!=0` iframe check (sets `gt=10` per hit).
- Init: `az=r8[7]; aC=r8[8]; o=r8[9]` (field `o` → Kotlin `oId`), the
  22-slot `Z` is reused (`Z[1]=r8[4]` only), then `i(r8[5])+t()` tail.

## Port notes

- `LevelCellSource.findByAw` (`k.q`) resolves the chain target; already
  wired for ax21/ax24.
- `p.ge` (`g.e`) is the trap/hide-spot owner slot added in slice 24;
  `p.g` is the interact target (both from earlier slices).
- `Z` realloc folded into the shared 22-array — `bO()` never reads
  `Z[2+]`.

## Findings

- `aw` ids collide across record types: `k.q(aw)` searches every entity,
  so a level-0 door record (ax44) legitimately shares the `aw` space —
  the S2 sibling chain then no-ops exactly as the original (the
  `r0.ax==76` guard rejects it). Tests use ids outside the record range.
- The S2 damage arm fires every tick — the original relies on `g.d`'s
  internal `g.t=10` iframe window for the 10-hit pacing.

## Gates

verifier `ok:true` · `python3 -m unittest` 57 pass · `:core:test` all
green (incl. Slice67Test, 13 tests) · `:android:assembleDebug` green.
