---
title: "Slice 412 — overload sweep of g.b() / g.b(int) / g.c(int) and i.b(): the ax69 zone bC() and the ax60 mover helper c(Z) re-read; the private corner probe i.b() is not the static attack test g.b()"
phase: "port"
status: "done"
slice: 412
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bC()V; bj()V @0-1338; c(Z)Z @0-1247; private b()Z @0-196; aN()V; aO()V; the 17 g.b(I)Z call sites)
  - reconstructed-project/bytecode/g.javap.txt (b(I)Z, b()Z, c(I)Z)
---

# Slice 412

Slices 410 and 411 each found the same mistake: the player class has TWO methods called `g.b` (the
static `b(int)` air / hang / climb set and the static `b()` attack test), and the port had swapped
them at the hit intake and at the mount lunge. This slice enumerates every call site of those
overloads (and of `g.c(int)`) in the raw bytes, maps each one to its port counterpart and re-reads
the methods that had not been diffed yet. The sweep found a third method with the same name.

## The call-site map (`proven`)

`g.b(int)` — 17 callers: `i.s()` @214, `i.a()` @170, `i.F()` @244, `i.a(IIILi;)V` @11 (slice 410),
`i.f(i)` @341, `aj()` @78, `aN()` @66, `aV()` @6167, `aW()` @362, `c(Z)` @137, `bm()` @981 and @1776,
`bu()` @592, `bC()` @498 and @648, `bQ()` @3003, `k.m(int)` @89, and `g.c(i)` @142 (slice 411).
`g.b()` — 13 callers + `g.ap()` @40; `g.c(int)` — `i.F()`, `aO()`, `c(Z)`, `bu()`, `g.e()` @381.
The port's three copies of the air set (`gB`, `GRABBABLE_STATES`, `AIR_ACTION`) are equal.
**`i.b()`** (class `i`, `private boolean b()`, @0-196) is a third, unrelated method: the entity corner
probe. Its callers are `I()` @3390 (the S85 hit-react) and `c(Z)` @613 — the latter was ported as the
static attack test.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **ax60 `c(Z)` carry push-back** | @610-636: `if (aS.b()) aS.ak -= (ag << 1) >> 8` — `b()` is the private instance corner probe `i.b()` | `w.playerAttacking()` (`g.b()`, the sword-swing test): a platform carrying its rider into a wall never pushed him back, a swinging rider got pushed back for no reason | `p.cornerSupported(w)` |
| **`i.b()`** itself | @34-89: with `k.ah != null` (the active scroll-wall holder) it answers true at once unless the box lies strictly inside the holder's `W` (`W0 <= ah.W0 \|\| W2 >= ah.W2 \|\| W1 <= ah.W1 \|\| W3 >= ah.W3`), without touching `aT..aW` | the four corner cells only | + the holder clause (affects the S85 hit-react while a scroll holder is armed) |
| **ax60 `c(Z)` non-locomotion arm** | @514: reached for a rider OR any `!g.c(S)` state: `al > W[3]` and S ∉ {209, 50} → `aS.a(0)` | rider only — a jump up into a moving platform from below passed straight through it | head-bonk: `a(0)` fall |
| **ax60 `c(Z)` carry and X contact** | @585-681 sit after the overlap if/else, so an S209 cling is carried even when the boxes just separated and the moving X box crushes (S50) regardless of the W overlap | both nested inside the W-overlap block | outside it |
| **ax60 `c(Z)` link block** | @0-107: `ac == null && Z[0] != -1` → `a(k.q(Z[0]))` and the pair latch / lever mode (`Z[4] = 1 / 3`) — on that tick only | re-evaluated every tick | link tick only |
| **ax60 `c(Z)` clamp** | @1033 `ifne 1119` falls into @1040 for `ag == 0`: a stationary mover snaps to its home bound and starts; the end probe @1119 is unconditional | `ag == 0` never started | added |
| **ax60 `bj()` ride arm** | @328-394: a rider outside S79/S78/S50 enters S78 and the arm ENDS (`goto 529`); one already crouched goes to S50 when the lift moves in either axis | S78 then S50 on the same tick, `ah` only (`ag` ran a push-out) | S78 entry ends; `ah \|\| ag` → S50 |
| **ax60 `bj()` pair handoff** | @1105 `if_icmple 1138`: the block runs for `ac.ak > ac.Z[3]` | `<=` | fixed (dead: no shipped pair) |
| **ax69 `bC()` zone bind (Z0 = 1/2)** | @693 `if_icmpge 801`: the zone catches a player whose feet are still ABOVE its mid-line | below | fixed (dead: every shipped ax69 has Z0 = 0) |
| **ax69 `bC()` S1** | @802 `if (r()) i(7)` falls into the S7 armed body @815 in the same tick | stopped after the anim switch | falls through (dead, as above) |

Live content: the ax60 records are the m7 lift/mover set (uids 54/55 S9 lifts, 56/57 S13 and 59/60 S11
lever-driven movers) and one m3 mover (uid 578, S15, lever 579); all movers link an ax58 lever, so
`Z[4] = 3`. The non-locomotion bonk, the carry placement and the push-back are therefore reachable
there; the `Z[4] = 1` pair handoff, the free-run clamp-start and the ax69 Z0 = 1/2 arms are dead in the
shipped data and fixed for fidelity only.

Verified equal (diffed against the bytes, no change): the ax22 zone `aN()` (capture, S1 vault / drop-through,
the four ternaries), the ax19 pickup `aO()` (`g.c(S)` set, the burst, `g.e(k.ax)`), the S9/S16/S10/S17
arms and the drive tails of `bj()` (auto-bounce, lever-driven vertical lift), `ax60Zones`, `i.a(i)` (the
port's `ac` property setter already clears `P&256` on the old target and sets it on the new — the
P|256 entities are skipped by `k.I()` and ticked by their owner), `k.m(int)`'s `g.b(S)` test, `i.s()`,
`i.F()`, `i.f(i)`.

## Tests

`Slice60Test` (+10, 20 in all): the ride arm (S78 entry then S50; sideways-only motion), the head-bonk,
the S209 cling carried without overlap, the carry push-back at a scroll holder, no push-back from the
attack test (S67 is carried like S5), the link-tick-only latch, the stationary clamp-start, the pair
handoff, and `i.b()`'s holder clause for each of the four edges. `Slice69Test` (+1, 14 in all): the Z0 = 1
mid-line polarity (corrected to the bytes) and the S1 fall-through.
Mutation-checked: 15 mutants — 15 killed (bC compare and S1 body; ride S78 fall-through and `ah`-only;
bonk rider-only; carry needing overlap; latch every tick; no `ag == 0` clamp; handoff polarity; carry
back to the attack test; no holder clause; each of the four holder compares).
No capstone needed a re-route: all 2213 tests pass.

## Docs

`docs/gameplay-mining/player-mechanics.md` lists the three `b` methods.

## Not done (parked)

ax41 `n()`, ax78/45 `bA()`, ax76 `bO()`, ax61 `aR()` (all but S8), ax34/75 `ak()`, ax64 `bl()`, ax74
`bN()`, ax42 `bz()`, ax58 `bg()`; the ax11 script-claim head @1700-1951; the `k` UI methods; the
remaining `g` methods (`au()`, `l()`, `as()`, `ar()`, `aq()`, `aw()`, `ax()`, `d(boolean)`, `o()`, `ap()`,
`am()`, `av()`, `i(i)`, `al()`, `j()`, `k(int)`, `ak()`, `aA/aB`).
