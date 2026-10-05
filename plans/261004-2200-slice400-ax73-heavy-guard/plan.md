---
title: "Slice 400 — the ax73 heavy-guard FSM aJ() and its spot routine b(i) follow the bytecode"
phase: "port"
status: "done"
slice: 400
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (aJ() :32593-33600; b(i) :7598; g(i) :27382; aF() :9192+; aG(); am(); aE(); a(boolean) :5898; aC() :31121; C() :8812; g() :7928; c(IIII) :31583; l() :9765; T()/V()/c(II)/d(II)/G()/H()/o(II); aA() ax17 S170 arm)
---

# Slice 400

`aJ()` (ax73, "heavy guard": 10 shipped records — missions 2/3/5/6) was decompiled
with `bcdec` and every arm and helper compared with `tickAx73` and its `*73`
helpers. Where `bcdec` loses a ternary or a `dup_x1` (post-decrement), the raw
bytes were read instead.

## Verified equal

S131 (block/counter), S146, S147 (grab QTE + `g(II)` press gauge), S148, S149,
S156, S157, S158, S167, S190-S192, the head claim bid, `d()` awareness tiers,
`e()`, `l()` (two port copies — `sightCheck73` and `losL` — agree with the
bytes), `h()`/`i()` counter window, `j()` intake (tables `bw/I/J/H/bu` read from
the clinit), `C()` (enrage/death/react), `g()` knockback, `aB()`, `aG()`, `am()`,
`aC()` (tiers use the POST-decrement form, the chase timeout the pre-decrement
form; the port had both right), the tail and `c(IIII)`.

## Divergences fixed (`proven`)

| where | bytecode | port had |
|---|---|---|
| `this.g(k.aS)` @103-114 (r2 intake gate), @1289-1350 + @1392-1403 (S165 marker offer/release), @1179-1202 (S171) | `g(i o)` = `(o.ak < ak) == av`, receiver = the GUARD (`aload_0`) → "the guard faces the player"; only @994 and the tail @2165 are `aS.g(this)` | all read through the PLAYER's facing, and S165's offer/release were also swapped |
| S152 `b(aS)` @460-486 | the real `b(i)` @0-405: sight rect `l()`, `v()`, `aA ∈ {0,1}`, facing flip, `aA = 1`, `i(154)`/`i(155)+aq`, the ax69 hand-off | a three-line gate stub (`canEngage73`) that only set `aA = 1`: **a heavy guard never started the chase on its own** (shipped S152 guards: mission 5 ×4, mission 6 ×2) — it fought only after being hit |
| `b(i)` facing flip @101-124 | `if (!this.g(aS)) av = !av` — turn to the player only when not already facing it | `if (!g.g()) av = !av` (player dead): every spot flipped a guard that already faced the player away from it — ax11 soldiers included (they share `spotB`) |
| S153/154 shortcut @619-657 | `aC = 3; i(155); goto 2042` skips `aC()` | still ran the scheduler (aC → 2) |
| S164 @1907-2041 | every path `return`s — never the L2042 tail | the finishing tick fell into the tail (counter window, intake, strike, `aA` block, push-past) |
| S165 abort @1697-1711 | `am() != 0` falls into the abort block | a phantom "re-arm `ag = 2560`" |
| S171 @1176-1200 | `aG() && this.g(aS)` is the SAME disengage as the band miss | a phantom `i(151)` path |
| `aF()` @0-236 | no `s != null` bail-out — the two tile arms run for a rider too | `return false` for any rider |
| `a(true)` in S153/154/155/156/157 (and ax17 S170 @464) | `i.a(boolean)` = `collideSides` (ends with `t()`) | `wallProbe` = the PLAYER's `g.av()` unstick probe: lifts `al` 20 px and leaves `W` stale, so the S155 `aF()` read the row above the floor (a phantom ledge zeroed the walk), and `aO >= 20` flung the guard into S43 |
| `c(II)` @0-4, `T()` | `ae != null → return`; `T()` = `ae.aa == k.r(74)` (the clip-74 hand) | `c()` repositioned/re-pinned an existing marker; `T()` accepted any ax14 marker |
| `C()` @194-213 | `Z[0] == 2 → i(6)` | missing (no shipped ax73 carries variant 2) |
| `aE()` pin arm @179- | zeroes the guard's own `ah/ag` too | missing (unobservable — S152 zeroes them every tick) |

## Capstone bot (input/timing only)

`mission5CapstoneLegJ` met ax11 soldier uid127 on the tower-B roof. With the facing
fix it keeps its 50-90 px pacing distance and hits from range, so a bot that
swung in place never reached it. The bot now closes the gap before swinging
(`|dx| > 45 → RIGHT/LEFT toward the foe`) — what a player does. **No state was
written and no enemy changed.**

Corrected listing misreads: `Slice73Test` — two tests set the player's facing for
the r2 gate (now the guard's), and the S155 test relied on a stub crate making
`aF()` false (now a real flat ground; the stub no longer hides the tile probe).

## Tests

`Slice400Test` (14): the r2 gate both ways, the S152 spot (turn/alert/chase,
out-of-sight and claim-held stay idle), `spotB`'s flip for ax11 and ax73, the
S153 shortcut's `aC`, S164's finishing tick (`P|16` and the push-past stay off),
S165's marker offer/release polarity and `c()`'s `ae == null` guard, the wall
abort, S171's ledge disengage, `aF()` for a rider, the S155 box staying in step
(`collideSides`), ax17 S170's box, `C()` variant 2. Mutation-checked: 15 reverts,
15 killed.

## Observations (not fixed)

- **ax11's head default `r5/r6`** (`I()` @2131-2146): `if (!r2.g(k.aS)) r5 = r6 = true`
  — the soldier's intake/stealth-kill gates default ON only while it does NOT face
  the player; the port writes `if (!world.gG()) { tail[2] = tail[3] = true }`
  (player alive). Same receiver misreading as the ax73 head; arms that set the
  flags explicitly (patrol, chase, S5, S11...) are unaffected. Left for its own
  slice (many soldier states sit on the default).
- `L/M` (`i.L`, `i.M`) have four homes in the port (`Entity.L/M`, `markerLx/Ly`,
  `w.iL/iM`, `w.anchorLx/Ly`); only the last one feeds the touch hit-test.
