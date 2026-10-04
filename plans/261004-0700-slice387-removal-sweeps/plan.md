---
title: "Slice 387 — k.c() p()s at once, bt() kills the alive, bu() S10 falls into the body, b() ax21 S1 ad.s()"
phase: "port"
status: "done"
slice: 387
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/k.javap.txt (c(i) @0-98; b(Z) @1910-1966)
  - reconstructed-project/bytecode/i.javap.txt (p() @0-58; bt() @0-148; bu() @54-69, @134-162, @1095-1148)
  - reconstructed-project/src/structured/k.java:4563-4587
---

# Slice 387 — four leftovers from the slice 371/373/384/385 audits

## Findings (proven) and changes

1. **`k.c(i)` runs `p()` at once** (bytecode @26-91): the pool is searched
   for the entity; a member gets `bg[as] = -99`, `bb[i].p()` (boxes null,
   `ab`/`ad` + cascade/`ae`/`af`/`c` dropped, `aS()`), its slot nulled and
   freed. A second `k.c` on the same entity finds no slot; an entity that
   was never `k.b`-inserted (an `aS.a(n,x,y)` marker) is left alone. The
   port only queued `pendingRemove`, so for the rest of the tick a removed
   entity kept its boxes — and since slice 385 it also sits in `bd[]` until
   the next paint, where the original sees an inert one. `removeEntity` now
   calls `deactivate()` (= `p()`) on pool members (`npcs` / `pendingInsert`)
   not already queued.
2. **`bt()` sweeps the ALIVE** (`i.bt()` @95-98 `P(); ifne next` — `P()` is
   `aB <= 0` + `G()`): ax∈{17,11,23,50} overlapping a moving block are
   killed (`as()`: `aB = 0`, ax11 `i(0)`, ax17 `i(69)`, ax23 `i(79)`). The
   port's `sweepHostiles` skipped the alive ones — a falling ax15 S10 block
   or an ax78 rock only "killed" corpses. Callers: ax15 S10, ax78 S2/S3.
3. **`bu()` S10 falls into the S6/S8 body** (@54-69: `P |= 16; bt()` then no
   `goto` — @69 is the body). The port stopped after `bt()`. In the body
   `z2 = S == 10 && Z[0] != -1` (@134-162; the structured decompile prints
   it inverted, the bytecode is the authority) and `aM() || z2` settles
   (@1125-1148) — the port's L137 lacked `|| z2`.
4. **`b(Z)` ax21 S1** (@1910-1966): `ad != null` → `C == null || u != 9`
   clears `ad.P &= -65`; `ad.s()` is then unconditional. The port advanced
   only for `C == null || u == 9`, so a claim over a non-u9 dialog froze it.

## Not changed (noted)

- clip63's S2 box (ax78) is an anchor point, which `i.a` rejects — the ax78
  `bt()` sweep is inert on real data; ax15 S10 (clip25 rect `[-31,-50,57,51]`)
  is where item 2 shows.
- The port's extra `k.c` cleanups (`player.gd`, `lockTarget`, `claimed`)
  have no counterpart in `k.c` (only `ah`/`F`); left as they are.
- `k.b(i)` inserts into a recycled `bb[]` slot (`ea[--eb]`, LIFO) or
  appends at `bc++`, immediately; the port defers every insert to after the
  pass. Not audited here — see the remaining-work estimate.

## Tests

`Slice387Test` (12): `k.c` p()s a member / a second one is a no-op / a
non-member is untouched / a removed entity is inert in a `bd` scan; `bt()`
kills each alive type, passes corpses, other types and a resting block, and
an ax78 rock kills the soldier it lands on; ax15 S10 runs `bt()` then the
body (`ah = 4096` over air), a linked S10 block settles (`z2`), S6 still
falls and S9 skips the body; ax21 S1 `ad` under a claim with u≠9 / u==9 /
no claim. 8 of them fail without the fixes. The Slice58 S10 sweep test used
a dead soldier (passed trivially) — it now uses an alive one.
