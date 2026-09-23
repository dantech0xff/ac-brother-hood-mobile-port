---
title: "Slice 168 — level-0 traversal unblock: S107-109 vault arm + sticky g.z + ap() b() guard"
phase: port-slice-168
status: done
date: 2026-09-22
---

# Slice 168 — traversal unblock

## Symptom → cause chain

"Player frozen at spawn" decomposed into three layers:

1. **Intro claim pin (FAITHFUL)** — `velClampTail` (Entity.kt:2130, i.java
   equivalent) zeroes `ag/ai/ah/aj` every tick while a claim script owns a
   slot on the player and `P&512==0`. The ax5-aw299 intro claim binds via
   `aB!=0 && nl!=0`, runs ~69 ticks of script ca=3 (blocks bind uids
   249/45/5), then releases. The original pins the player the same way.
2. **Missing S107/108/109 arm (PORT GAP — fixed)** — the directional
   edge-vault anims had no dispatch arm: they fell into the default
   `flingAirborne(0)` arm and re-armed forever at the first wall (ak≈351).
   Ported g.java L12de verbatim: on `r()` → `al -= (S-107+1)*20`,
   `ak += if(av) -20 else 20`, `a(0,9)` (snap to cell center).
3. **ax4 crate pin (FAITHFUL) + `g.z` starvation (PORT GAP — fixed)** —
   `pushOut` (a(), i.java) zeroes `ag` every overlapping tick exactly like
   the original; crates block the runner at ak≈504. In the original the
   player escapes by slashing — `ap()` fires because `g.z` is a STICKY
   latch armed by `l()`'s head (`cp=1; cq=1; z=1`, g.java l-head), not a
   per-tick recompute. The port cleared `p.z` every tick at dispatch top,
   so `z` was 0 in S12 and `ap()` never ran. Fix: sticky `z` armed in
   groundedTail, cleared at the same `cq=0; z=0` airborne arms and grab
   sites the original uses.
4. **`ap()` `b()` guard (PORT GAP — fixed)** — `ap()`'s entry test is
   `ac.ax==10 || b()` → blocked; `b()` is the busy-anim set {67-69,81,
   112-115,183-184,216-217,286-287}. Added `isAttackState(S)` to the
   blocked check so a context tap mid-swing can't re-enter `i(67)`.

## Verified live (probe → regression tests)

- hold-right → S12 run → wall at 351 → S109 vault → al 940→880, ak→504.
- pinned at aw10 crate (S5 [512,854,546,887]) with `z=true` → context taps
  fire `i(67)` → crate breaks (`setAnim(S+1)` chain, entity removed) →
  player advances to ak=644.

## Files

- `rewrite/core/.../PlayerFsm.kt` — S107-109 arm (L12de verbatim); sticky
  `z` (removed per-tick clear; `z=false` beside each `cq=false` at the
  leave-ground arms); comment rewrites.
- `rewrite/core/.../Entity.kt` — `contextDispatch`: `b()` busy-anim guard.
- `rewrite/core/.../Slice1Test.kt` — `Slice168Test` (5 tests).
