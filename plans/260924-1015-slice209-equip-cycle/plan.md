---
title: "Slice 209 — equip-cycle verbatim: k.q() at-reset + k.p(I) bit index"
phase: port
status: done
---

## Summary

Two verbatim discrepancies in the equip machinery, found while auditing
the demo-path blocker "ax4 crates never bind `p.g` (`interactScan` gates
`gI==8`)". The `gI==8` gate itself is faithful (g.java:13324 —
`g.I != 8` skips ax4/ax58 candidates); the bugs were upstream in the
equip list that feeds `gI`.

## Fixes

1. **`k.q()` drops `at=0`** (k.java:13152, proven). The verbatim head is
   `as=0; at=0; ar[]=-1`; the port kept `as`/`ar` but skipped `at`.
   `at` is the cycle-button lock — after an unlock grant (`g.g(mask)` →
   `q()`) the original re-arms the button; the port could leave
   `actionLock` wedged at 1.
   `Level0World.rebuildEquip()` now resets `actionLock = 0` first.

2. **Weapon cycle used the slot index, not `k.p(I)`** (g.java:3792 +
   k.java:13230, proven). The original computes
   `h(ar[(p(I)+1)%as])` where `k.p(I)` is the *lowest set-bit index* of
   `I` — not the `ar[]` slot. The port's `equipList.indexOf(gI)` claimed
   equivalence; it differs whenever `ar` has gaps below `I`:
   `ar=[1,2,8,16]`, `I=8` → `p=3` → `ar[(3+1)%4]=ar[0]=1` (the cycle
   skips slot-3's 16 — `indexOf` would go 8→16). Likewise
   `I=16` → `p=4` → `ar[1]=2` — equip-1 is skipped on that hop.
   New `Entity.kp()` ports `p()` verbatim; `cycleEquip` calls it.

## Demo-path context (audit results)

- `aX()` (ax14, i.java:37807+) has no damage/fail arm — the "ax14
  hazard" at (2180,848) is the pit kill-line, faithful.
- `g.I != 8` skipping ax4 binds (g.java:13324) is verbatim — crate
  carry requires equip-8 (fists/hidden-blade slot) armed by an
  ax16-S38 `g.g(8)` pickup; level-0 has none — sword smash is the
  intended crate interaction and already works.
- `rebuildEquip` itself was already correctly implemented in the world
  (the empty stub seen was only the iface default).

## Tests

`Slice209Test` — 3 tests: `at` reset on rebuild; `I=8 → gI=1` (bit-index
skip); `I=16 → gI=2` (wrap past slot 0). Existing cycle tests unchanged
(identical semantics for contiguous low bits).

## Gates

verifier `ok:true` · `python3 -m unittest` 57 pass · `:core:test
--rerun-tasks` 1370 pass · `:gdx:build` · `:android:assembleDebug`.
