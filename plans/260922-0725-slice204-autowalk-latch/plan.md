---
title: "Slice 204 — postTail jump-gate clears + g.A autowalk latch"
phase: port
status: done
---

# Slice 204 — jump-gate cq clears + g.A autowalk arm

Completes the head of `e()`'s shared tail between L3852 and the L38b8 jump
gate. The mount/interact block (L35da–L377c) was audited and found already
faithfully ported as `mountEntry` (dispatch tail, :1469); its original tail
`r9 → aA&=-5` is semantically covered by postTail's unconditional
`aA&4→aA&=-5` (nothing between L377c and L2051 sets aA|4).

## Mining receipts (fallback g.java, proven)

- **L3868** (:7979-7992): `if (g.A) { E(); av=g.B; ag=0; i(148); g.A=0;
  return }` — the autowalk latch. Producers: `i.aV()` ax10 zone arm
  (i.java:~36220, `aS.W` ∩ `this.W` && `aS.S∉{148,149,150}` → `g.B=av`,
  `g.l = (aG - aS.ak)<<8/11 or 0`, `g.A=1`) and the `i.D()` static reset
  (:7126,:7138). `g.l` is consumed by the S148 arm (g.java:6693 L2ed1:
  `ag = g.l!=0 ? g.l : ±1024`, anim-end → `g.l=0; i(150)`; L2f13 →
  `ag=g.l; i(157)`).
- **L388a** (:7993-7997): `f() → cq=0`. `g.f()` (g.java:8317) =
  `isHolding()`: `ci != null && g(ci) && |ci.ak-ak|<120 &&
  |ci.al-al|<20` — carried hands suppress jumping.
- **L3895** (:7998-8014): `ac != null && ac.ax ∈ {51,66} → cq=0` —
  standing on a crate/moving platform suppresses the jump.
- `i.E()` resolved to `eSettle` (i.java:11724): `ah=1; b=1; a(1); ah=0;
  loop aR∉{3,5,12} → al+=10` — NOT `settleToGround`.

## Port changes (PlayerFsm.postTail)

Order matches the original tail exactly:
`aO∈{7,9}→cq=0` (slice 203) → **new** `gA` arm (early return) →
**new** `isHolding → cq=false` → **new** `ac.ax∈{51,66} → cq=false` →
existing `cq && !gE && v(16398)` jump gate.

The L35ab–L377c mount/weakened-grab block was audited and is already
faithfully covered by `mountEntry` (dispatch tail :1469); its original
`r9 → aA&=-5` epilogue is semantically covered by postTail's
unconditional `aA&4 → aA&=-5` (nothing between L377c and L2051 sets
aA|4).

## Tests (Slice204Test, 7)

- gA latch: S→148, av=gB both directions, ag zeroed, latch consumed,
  tail skipped (UP edge can't fire the jump).
- isHolding: in-front `ci` suppresses; out-of-range `ci` does not.
  (`held.aB=1` required — az() releases dead ci each tick.)
- ac ax51/ax66 suppress; ax11 control still jumps.

## Gates

verifier `ok:true`; unittest 57/57; `:core:test --rerun-tasks`,
`:android:assembleDebug`, `:gdx:build` all green.
