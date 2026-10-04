---
title: "Slice 406 — ax51 crate bs() and ax66 platform bm() re-read from the raw bytecode"
phase: "port"
status: "done"
slice: 406
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bs() @54827-56319, bm() @53272-55339, bo/bp/bq/br/bn, m(i)/n(i) @54455/54510)
---

# Slice 406

`bs()` (pushable crate, 1492 bytes) and `bm()` (moving platform / crate-ride-grab FSM,
2067 bytes) were ported from the jadx-level decompile. Both were re-read arm by arm against
the raw bytes (jadx and `bcdec` only as a map — the `(? ? X : Y)` ternaries and every
branch polarity were read from the bytes).

Verified equal: every other arm of `bs()` (head, release, land-mount, `br()` gate, grab
edge, carry clamps, S0/1/2 switch) and of `bm()` (S6/24 board, S8/26 stop, S9/27 sink,
S10/28 reset, S14, S15, S16, S18, the S11/12/13 board / grab / drift arms, S19/21/22
attack-grab), plus the helpers `bo()/bp()/bq()/br()/bn()` and `m(i)/n(i)`.

## Fixed (`proven`, raw bytes)

| arm | bytes | was | now |
|---|---|---|---|
| ax51 **board nudge** | `bs()` 684-753 | `av` arms swapped: a right-facing player sticking out on the RIGHT was shoved left, a left-facing one sticking out on the LEFT was shoved right | `av == false`: `W[0] < crate W[0]` → `ak += 20`; `av == true`: `W[2] > crate W[2]` → `ak -= 20` |
| ax66 S7/S25 ride | `bm()` 315-337 | S34 released the link (`g.a = null`) | S43 **and** S34 land: `aS.al = al; aS.i(0)`, link kept |
| ax66 **S20** | `bm()` 1725 | merged into the S19/21/22 attack-grab arm → anim end sent it to S18, so the timed return S19 (and its `Z[1]` countdown) was never reached | its own arm: `if (r()) { i(19); aC = Z[1] }` |
| ax66 S11/12/13 | `bm()` 1011-1052 | after the linked S16 kill-crate fling + `g.a = null` the arm ran on and re-bound the player (`g.a = this`) | `goto 2026` — the arm ends |

## Tests

`Slice406Test` (6): board nudge both facings and both edges; S34 landing; S20 hand-off and
no grab arm; S11/12/13 early exit with a linked S16. Two `Slice1Test` unit tests that
encoded the old behaviour were corrected to the bytes (S7/S34 keeps the link; S20 no longer
flings — the S18 + fling exit moved to a new S21 test). Mutation-checked: 4 reverts, 4
killed.

## Not done

- `bs()`'s S0/S1 `g.c` bookkeeping, `ab()`/`aa()` claim script head: verified equal, nothing
  to do.
