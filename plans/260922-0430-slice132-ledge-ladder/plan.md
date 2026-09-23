---
title: "Slice 132 — i.A()/i.z()/g.ak() helpers + S12/S33/S34 ledge & ladder arms"
phase: port
status: done
---

## Scope

Ported the ladder/ledge traversal chain (all `proven`):

- `i.A()` (i.java:940) — `ladderCell()`: facing-side column contains a
  cell ==21 anywhere in the box's row span.
- `i.z()` (i.java:928) — `pushColumnBlocked()`: every cell in the
  push-direction side column is ≥19.
- `g.ak()` (g.java:187) — `ledgeLipGrab()`: wall cell ≥19 at hand-row +
  5-cell pocket clear → snap `ak=i2*20(+20)`, `al=i4*20-1`, `i(60)`.
- `g.aA()` (g.java:5545) — `wallJumpKick()`: `u(16388)` or direction-
  toward-facing (`u(8264|4114)`) → `i(92)` + ax8/clip5/S17/az201 marker
  spawn (`P=512`, `N/O` 8.8 pos, `t()`, `k.b()`).
- S12 arm (g.java:1313): `D=false; co++`; `ag!=0&&aO==0` → `A()` ladder
  snap `i(74)` | side-strip ∈[19,24) → i8∈{1,2,3} → `i(107|108|109)` |
  `co>2 && z()` → `ak()` lip grab else `i(33); ah=-4096` rebound.
- S33 arm (g.java:1938): `cp=true; aj=512`; `A()` snap | direction press
  → `x()` → `aR|aS∈{5,20}` → `a(43,32)` else `a(34,36)+aA()` | `ah<0` →
  `r()` → i4/i5 wall-pocket scan → `ak()`.
- S34 arm (g.java:2018): `aA()` per tick, `aU!=20 → a(0)`,
  `aR>=19||==5 → l()+k.v()`.

## Corrections caught by tests

- The earlier S12 stub wrongly ran `groundedTail` (l() chain) after the
  arm — the original `case 12` breaks to post-switch, so `i(74)/i(33)`
  were being overwritten by the L120 fold (`S!=11 → i(11)`). Removed.
- The JADX `while(true)` around the i13 pocket scan drops an
  else-break (the loop can never observe `i13==2` without hanging —
  `while (i13 < 2)` is the only shippable reconstruction,
  `high-confidence`).

## MarkerWorld change

`cell` param gained an optional `cellFn: (Int,Int)->Int` for
position-dependent cells (defaults keep every existing test).

## Gate results

- verifier `ok:true`; unittest 57; `:core:test` 905 (+10 Slice132);
  `:android:assembleDebug` + `:gdx:build` green.
