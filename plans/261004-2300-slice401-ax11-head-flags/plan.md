---
title: "Slice 401 — the ax11 I() head defaults: the intake / stealth-kill gates follow the soldier's facing"
phase: "port"
status: "done"
slice: 401
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (I() @2131-2146 :20244 — `aload_2; getstatic aS; invokevirtual g:(Li;)Z; ifne 2147; iconst_1; istore 6; iconst_1; istore 5`; consumers @7491-7518; g(i) :27382)
---

# Slice 401

Found while closing slice 400: the ax73 head misread `this.g(k.aS)` as the player's
facing, and the ax11 `I()` head makes the same call. `g(i o)` is
`(o.ak < ak) == av` — "o is on my facing side" — and in `I()` @2131 the receiver is
the soldier (`aload_2`), so

    r6 = r5 = !soldier.g(player)      // `ifne 2147` skips both stores

`r5` gates the damage intake `j()` and `r6` the stealth-kill driver `k()` @7491-7518.
They therefore default ON only while the soldier does **not** face the player. The port
wrote `if (!world.gG()) { tail[2] = tail[3] = true }` ("player alive"), so every state
that leaves the flags alone stayed hittable and killable from the front.

States that rely on the default (the arms of S85/S9/S25/S1… set nothing): the S85 hit
flinch above all — in the original a soldier that faces the player ignores further hits
until the flinch ends (one hit per flinch cycle, no combo juggling); a hit from behind
still lands. The arms that set the flags explicitly (S2/3/92 patrol, S4/22 chase, S5,
S11, S12, S18, S23, …) are unchanged.

## Fixed (`proven`)

`NpcFsm.tick`: `if (!e.faces(player)) { tail[2] = true; tail[3] = true }`.

## Capstone bots (input/route only)

- `chaseMask289` (mission-5 legs A/B/C2) swung at the FIRST soldier in range, in any
  state. It now targets the nearest soldier in front of the swing and swings only
  while that soldier is damageable (not flinching S85 / countering S17 — S17 answers a
  swing with the player's i(8) stun).
- `mission5CapstoneLegB`: on the block2/3 tops (x ≥ 2560) two soldiers flank the run to
  cp555 and the player's meter is 30; it now hop-runs past instead of dueling (soldiers
  pace at 2 px/tick against the player's 10).
No state was written and no enemy changed.

## Tests

`Slice401Test` (3): a facing soldier in S85 takes no hit and one looking away does; S9
(no arm) follows the same default; S11/S12/S23/S4 (explicit flag) still take it from the
front. Mutation-checked: 3 reverts, 3 killed.

## Observation, parked (slice 402 candidate, `proven`, NOT applied)

The same I() tail pushes the player in the original from **both** aA branches:
`@7644-7657 (aS.aA & 8) == 0 → r2.a()` is reached from the `aA == 0` and the `aA != 0`
arm alike (raw bytes; the jadx structuring and slice 393 put it inside the first
sub-branch only). In the port an alerted soldier — the one chasing — is therefore not
solid. Applying the fix makes 13 capstone legs fail (missions 0/2/3/5/6: bots that ran
through alerted soldiers now get shoved off ledges, e.g. mission-2 leg A at the aw38
block edge) — it needs a bot-by-bot re-route, so it stays a separate slice.
