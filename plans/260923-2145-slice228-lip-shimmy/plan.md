---
title: slice 228 — '5'-lip monkey-bar shimmy traversal verdict test
phase: fidelity-verdict
status: done
---

## Summary

Regression test on the '5'-hang traversal arms (S38 → S37 → steps),
fallback g.java:6461-6570 + structured :2101 L1502/L1560 — all proven.
Completes the corridor question opened by slice 227's grab verdict.

## Verdict: '5' cells are monkey bars, not mountable ledges

From the S38 hang:

- `u(16388)` (UP) probes the cell above the head — for a '5' hang that
  cell is the lip itself, so `aO==5` (never 0) and the S54 vault-out is
  dead code on '5' hangs. No climbing onto the bar's top from below.
- `u(33024)` (DOWN) probes below and drops to S43 when open.
- `!u(facing)` arms S37 — the else-if chain treats "direction not
  held" as the shimmy entry.

In S37 (bound to ax10-S32 grapple OR `aO==5`):

- `u(facing) && c(av)` → `i(37)` + `ag = ∓1536` — the shimmy step.
- else the flip arm `av ? v(8256) : u(4112)` — **asymmetric, verbatim**:
  facing-right flips on LEFT *held*; facing-left flips on RIGHT *edge*.
- anim end → back to S38.

Net control: hold the direction opposite your facing → S37 entry → the
held-check flips you to face it → steps carry `ak` along the lip while
the facing cell stays open. Past the bar's end (`aO!=5`) → `al=W[3]` +
S43 drop.

## Demo consequence

The corridor's '5' sequence is a monkey-bar crossing — the traversal is
grab → hang → shimmy under the lip → drop at the far end. The Run-3
"trap" resolves by shimmying, not by mounting the top. Combined with
slice 227: rise under a '5' → `cw && aO==5` → grab → hang → shimmy.

## Test

`5 lip hang shimmies along the bar` — finds a two-cell '5' bar with
open air above/below in level0, rides the rise → grab (S280) → hang
(S38) chain, holds LEFT while facing right: S37 arms, the flip turns
`av` true, `ak` decreases along the lip.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` green · assembleDebug · gdx:build
