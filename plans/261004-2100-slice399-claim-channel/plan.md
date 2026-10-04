---
title: "Slice 399 — one context-claim channel (k.L/k.co/k.cp) with the bytecode's rules, and the ax11 head bid"
phase: "port"
status: "done"
slice: 399
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/k.javap.txt (k.a(i,I,[I) :4990-5062; k.a([I) :5059-5110; k.m() :5112-5125; k.j(II) :4465-4750 (touch hit-test); k.k() :4383; k.c(i) :22384-22462; k.ah() reset :30578)
  - reconstructed-project/bytecode/i.javap.txt (I() case 11 head @1644-1697 :20029-20050; aH() :32409; aj() @119-175; aJ() @0-50; aL() @421; br() @91; bg() @240; D() tail k.m())
---

# Slice 399

While checking the ax4 claim bid in slice 398 the port turned out to carry **two
copies of the one static `k.L / k.co / k.cp`**: `claim / claimed / clearClaim /
claimPrio / claimPad` (fed only by the ax4 crates and released by ax58 and
`removeEntity`) and `registerClaim / kL / claimReset / claimCo / claimRect`
(ax73, ax47 and `br()`, and the only copy the touch hit-test `k.j(II)` read).
They never met: a tap on a crate never reached `k.j`, and a crate bid could not
be outranked by (or outrank) a soldier. The surviving copy also differed from
the bytecode, and the ax11 soldier's own `I()` bid did not exist.

## What the claim is for (`proven`)

`k.L` is read by `k.j(II)` @285-358 — the pointer hit-test: with the STYLE option
on the wheel/tap scheme (`k.cm == 0`, `!k()`), a tap inside the claim rect `cp`
resolves to action 4 (action 1 when `co == 1`), i.e. "attack / interact the
highlighted entity" — and by a debug overlay line in `k.b(Z)`. With the default
mounted pad (`cm == 1`) a tap outside the pad box returns -1 first @158-216, so
the keypad and the mounted pad never read the claim. Five bidders in the
original: `I()` case 11 (soldiers, prio 0), `aj()` (ax4, prio 5), `aJ()` (ax73,
prio 0), `aL()` (ax47 S81, prio 0), `br()` (the player's grab, prio 1).

## Divergences fixed (`proven`)

| where | bytecode | port had |
|---|---|---|
| one channel | a single `k.L/co/cp` | two copies; ax4/ax58/removeEntity on one, everyone else + the touch hit-test on the other |
| steal rule, `k.a` @54-70 | `prio < co \|\| (prio == 1 && co == 1)` | `prio < co \|\| prio == 1` — a prio-1 bid (`br()`) stole from a prio-0 soldier/guard |
| owner refresh, @0-39 | the holder is matched by `aw`; a re-bid by it only refreshes `cp` (never touches `co`, any prio) | identity match, same effect for one object but a same-uid twin took the steal path |
| `cp`, `k.a(int[])` @0-62 | the bid rect padded **±10**, written into one `int[4]` (a snapshot) | the raw rect stored **by reference**: taps up to 10 px beside a claimed entity missed, and the rect trailed the entity's `W` after the bid |
| ax11 `I()` case 11 @1644-1697 | after the family head: `!aH() && a(W, k.M) → k.a(this,0,W)`, else `k.L.aw == aw → k.m()` | not ported ("unported" in slice 369) — a tap on an adjacent soldier fell through to the wheel cell around the player |
| level reset | `i.D()` tail `k.m()` | cleared the ax4 copy only |

`removeEntity` keeps a **port-side safety net** releasing a claim held by the
removed entity. The original `k.c(i)` never touches `k.L` (its claimants all
`k.m()` before their own `k.c(this)`: aj S6/S8, aL S94, bg S2); the net only
guards the port's other removal sites. `inferred` — unobservable with today's
bidders.

## Tests

`Slice399Test` (16): the steal rules (lower wins, equal/higher don't, prio 1 vs
co 1 / co 0 / co 2, out-of-range prios), owner refresh by `aw`, the ±10 snapshot
(in-place `int[4]`, no trailing), ax51's `Y` rect, `k.m()` reset, the removal
safety net, ax4 and a soldier on one channel (a prio-0 holder keeps the crate's
prio-5 bid out), the touch hit-test through the real `resolvePadZone` (action 4
inside W and inside the pad, outside the pad falls through, action 1 for co 1,
a tap on an adjacent soldier), the ax11 head bid (in reach, out of reach
releasing only its own claim, every `aH()` state never bidding / releasing).
Mutation-checked: 13 reverts, 13 killed. `Slice1Test` / `Slice369Test` /
`Slice398Test` moved from the retired names to `kL / claimCo / registerClaim`
(the old ax73 asserts hedged `claimed === e || kL === e`).
