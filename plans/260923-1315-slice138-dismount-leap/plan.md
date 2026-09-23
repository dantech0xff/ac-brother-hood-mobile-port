---
title: "Slice 138 — dismount/leap family (S54 settle, S357 leap, S360 perch, S370 windup, S374 KO) + i.E() sink"
phase: port
status: done
---

# Slice 138 — dismount/leap family

Ports the five remaining `e()` arms discovered while auditing cases
54/357/360/364/370/374, plus the `i.E()` settle-sink they depend on.
All arms `proven`; the folded empty-if tails after each are the JADX-
collapsed shared L1364 block already covered by the default arm.

## Mined source

| Arm | Source | Behaviour |
|-----|--------|-----------|
| `i.E()` | i.java:2929 | settle-sink: `ah=1;b=true;a(true);ah=0`; loop `aR∈{3,5,12}` → done else `al+=10` |
| S54 | g.java:2223 | `r()` → `al-=20; E(); i(0)` — dismount settle. No `i(54)` call sites (script-entered, like S317) |
| S357 | g.java:4154 | `ag=3328` (`av` → `ag=-1280` — asymmetric, verbatim); `r()` → `ag=0;K=4;i(364);ar()` |
| S360 | g.java:4167 | respawn lead marker `a(i21,ak+i22,al-85)` per tick — (9,40) right / (106,-40) left; fwd press → `G();i(357)`; `r()` → `G();i(0)`. No call sites |
| S370 | g.java:4185 | `r()` → `i(371)` — boss-grab windup |
| S374 | g.java:4211 | `ah=0;ag=0`; `r()` → `x[1]>0` → vel0 + `i(376)` else `k.l(12)` mission-fail |

## Verbatim findings kept

- S357's `av` branch gets a **different** speed (-1280, not -3328) —
  asymmetric by design in the source, kept as-is.
- `i(364)` immediately followed by `ar()`: with no interact anchor
  (`g==null`) `ar()` idles via `i(0)`; the `S!=364` guard inside `ar()`
  (Entity.interactAction) keeps 364 only when the anchor is live —
  the S364 leap-landing anim is a special case by design.
- S360 runs `G();i(357)` on forward press *then* `r() → G();i(0)` —
  both sequential `if`s, so `r()` wins same-tick (the relaunch only
  survives mid-anim).
- S364/S371 need no arms: they land in the shared default arm
  (`r() && !j && !l() && !j → a(0)`, g.java:1145) which the port
  already carries.
- `i.E()` at i.java:2929 is distinct from the older `settleToGround`
  (i.java:3760-shaped probe loop) — ported faithfully as `eSettle()`
  using `probeSnapSides(true)` for `a(true)`; the older function is
  left untouched.

## Gates

- verifier `ok:true`; 57 unittests OK
- `:core:test` 952 tests, 0 failures (8 new Slice138Test cases)
- `:android:assembleDebug`, `:gdx:build` — BUILD SUCCESSFUL
