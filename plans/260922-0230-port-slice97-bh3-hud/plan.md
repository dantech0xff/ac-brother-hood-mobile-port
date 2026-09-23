---
title: "Slice 97 — c(z2) bh3 arm: boss HP column, z[54] counter, alert meter, g.g overhead"
phase: rewrite
status: done
---

# Slice 97 — the bh3 arm of `k.c(z2)` + `g.g` overhead icon

Completes `c(boolean z2)` (k.java:4176-4353): the `bh[aj]==3` branch and
the trailing `g.g` grab-QTE overhead icon.

## Mined (all `proven`)

- **Boss HP column** (:4188-4199): gate `i.bT && k.B != null`; black
  `j.b(389,60,6,100)`; `B.aB` clamped to `i.bU`; red fill
  `(100*aB)/bU` drawn from the bottom up (`160-h`); `aB<=0 || h!=0` →
  the fill, else a 1px stub at y159 (`h==0` with `aB>0`).
  Fields: `k.B` = the boss-tracked entity (`k.B = this` at i.java:2378),
  `i.bU` = its max-HP snapshot (i.java:17585), `i.bT` = column armed
  (i.java:16864).
- **Soul counter** (:4200-4207): `z[54]` anim0 frame `j.g % z[54].b(0)`
  at (300,8); `ap[4]<0→0` then `y.a(ap[4]+"/"+aq, 312,5,20)`.
- **Alert meter** (:4208-4244): `aE>0` arm — `aH>30 → aH--`;
  `aH∈[0,30]` decrements and at `<0` writes `aE = aH` (=-1, poisons the
  meter) + `aH=0` + early `return` (skips the rest of `c()`);
  `aF` trickles +3/tick (or the remainder) into `aE`;
  `i3 = aH∈[0,30] ? 30-aH : 0` (slide offset);
  `i4 = min(aE,100)`; `aE<25` → z[12] anim5 frame `j.g%b(5)` at
  (15-i3,165) else anim3 frame0; z[12] anim4 frame0 at
  (10-i3, 49+116·(100-i4)/100).
- **`g.g` overhead** (:4344-4350): `g.g != null && (aS.S==303||295) &&
  z[10]!=null` → z[10] anim 41 (S303) or 29 (S295) frame `aS.K` at
  `(aS.L-O, aS.M-P)`. `g.g` = the player's focus/interact target —
  already mapped to `player.g` in the port; `aS.L/M` = g.java:57-58
  grab-display coords → new `gQL`/`gQM` fields; `aS.K` = `Entity.K`.
- `k.aS` is class `g` (the player), proven by raw bytecode
  (`g r0 = defpackage.k.aS`) — `aS.L/M/K` resolve to g-fields.

## Port layout

- `hudStep()` gained the bh3 mutations under `if (bh3)`: `B.aB` clamp,
  `ap[4]` floor, the `aE/aH/aF` countdown → derived `alertSlide`
  (`i3`) + `alertFill` (`i4`) renderer fields. The `return` poisons
  `kAE` and exits hudStep, mirroring the orig's early exit.
- `Level0Renderer`: `if (world.bh3)` arm — HP column, z[54] anim0,
  `ap[4]/aq` text, alert icons — `else` the existing score arm;
  `g.g` overhead block at the tail before the z[74] overlay.
- Tests `Slice97Test`: column clamp, ap[4] floor, aH 80→30 hold →
  30-frame slide → aE=-1 poison, aF trickle (3/remainder), i4 cap,
  g.g gate fields. `tickClean` helper keeps hudStep ungated
  (no claimer, player inside the autoscroll camera).

## Gates

verifier `ok:true` · `unittest` 57 ✓ · `:core:test` 770 ✓ (6 new
`Slice97Test`) · `:android:assembleDebug` ✓

## Deferred

`aF=min(aB,100-aE)` and `aE>0→aH=80` producers (i.java:13820/:16860)
live in unported arms — fields are wired and ticked now.
