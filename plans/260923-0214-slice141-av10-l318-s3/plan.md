---
title: "Slice 141 — ax10 aV() real case-10 (L318 climb sequence) + case-3 flag-clear"
phase: port-slice
status: done
---

# Slice 141 — correct the S10 label, port L318 + L14a7

## What changed

- **Correction (proven)**: the arm ported in slices 137/138 under the label
  "S10 wall-run" is `aV()` **case 46 → L1e1** (gq/gd latch). The real case-10
  target is `L318` (i.java:9480–9772). The body moved to `46 ->` with a
  comment; `10 ->` now holds the L318 port.
- **S10 = L318 scripted wall-climb/column sequence** (proven,
  i.java:9480–9772): while `dy = aS.W[1] - W[3] ∈ [Z0,Z1]` the zone locks
  input (`k.o()`), parks a clip-74 hand card at view center
  (`c(O+200,P+120)`/`d(...)`), and on `v(1)` OR `V()` (finger on card) arms
  the climb: `i.bB=1, i.bF=-1, i.bG=-1, k.v(), i(4), i.bi=1, az=199`.
  Outside the band: overlap + (`!bB` or `bF ∉ [Z2,Z3]`) → L46b abort
  (`G(); a(71,ak,al-85); i.be=1; i(34)`); overlap + armed + `bF ∈ [Z2,Z3]` →
  grip marker 35 tracked to `ak,al-85`. No overlap → `ae.S==39` cleanup,
  `bB&&bi&&bF==-1` finish-reset (`i(28); bC=bD=0; bF=100; bE=Z[4]; A(25)`),
  `0<dy<Z0` marker window, `dy<0` completion (`bi=0; k.c(this); S∈{26,28,29}
  → i(27)`), `k.p()` unlock tail.
- **S3 = L14a7** (proven, i.java:11975–12050) — S2's mirror: same `Z0` overlap
  gate and `Z0==2` guard check, but `t.P &= ~Z[3]` (flags cleared) and
  `t.ax==21 → t.P |= 16` (director re-activate bit). One-shot remove.
- **init**: `S∈{2,3}` share the L59 quad `Z={f4,f12,f13,f14}` (i.java:2114);
  `S=10` gets the L95 five-slot `Z={f11..f15}` — no `L111` tail on either.

## World surface added

`LevelCellSource.iBB/iBi/iBC/iBD/iBE/iBF/iBG/iBe/kDd` (+ existing `camAf/
camAg`, `clipFor`, `findByAw`, `spawnPickup`, `lockInput/unlockInput`,
`kO/kP`); `Level0World` overrides the `i.b*` climb statics it owns.

## Tests

`Slice141Test` (12): iBe suppression, in-band input lock + hand spawn,
press-arm (iBB/iBF/iBG, S4, bi, az=199, ae released), unarmed overlap abort
(marker 71 + iBe + S34), armed bF∈[Z2,Z3] marker-35 tracking, bF∉window
abort, off-zone finish reset (S28, bC/bD off, bF=100, bE=Z4, sfx 25),
dy<Z0 completion (remove + S28→27 + unlock), dy≥Z0 unlock-only, S3
mask-clear, S3 ax21 +16, S3 overlap gate.

Existing `ax10 S10 zone` test relabeled to `S46` (it exercises L1e1).

## Gates

verifier `ok:true`; `python3 -m unittest` 57 pass; `:core:test` 986 pass;
`:android:assembleDebug` + `:gdx:build` green.

## Known gaps (labeled inferred/deferred)

- The player-side `g.e()` climb states (26/27/28/29/34) that consume
  `i.b*` remain on the porting backlog — the zone arms them now, so the
  halves meet in one later slice.
- `handUp` stub uses `clipFor(74)` — stub worlds return null, so any ae
  counts as "the hand"; flagged in test comments, real path is proven.
