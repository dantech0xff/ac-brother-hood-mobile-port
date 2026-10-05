---
title: "Slice 410 — the hit intake's op-4 head tests the AIR set, not the attack anims; op 34/20/38/29/30 and the dead arms re-read; 4 capstone legs re-routed"
phase: "port"
status: "done"
slice: 410
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (a(IIILi;)V @0-1429, i.javap.txt:18292)
  - reconstructed-project/bytecode/g.javap.txt (b(I)Z lookupswitch, b()Z, a()Z, d(I)V, h()Z)
---

# Slice 410

`Entity.applyHit` (`i.a(IIILi;)V`) is the one method every enemy, hazard and trap funnels a hit
through. Its port comments cited `i.java:4446` (the decompiled text) — the raw bytes were never
diffed. Done now, arm by arm.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **the op-4 head** | @0-37: `op == 4 && g.b(aS.S) && g.t == 0 && !g.s → op = 18; ag = 0`, with `invokestatic g.b:(I)Z` — the lookupswitch over the **air / hang / climb set** `{18-20, 22-25, 35, 36, 43, 150, 157, 165, 233, 242, 243, 263-266}` | `PlayerFsm.isAttackState(S)` — the `g.b()Z` *attack* test: a soldier strike on a player **mid-combo** was turned into the S43 knock-down; a hit on a player who was actually airborne / hanging took the flinch arm | the air set. A grounded player — combo included — takes the op-4 arm (`c(r4)` → S9); an airborne one is knocked down |
| **op 34** (parried-hit feedback) | @204-262: `aj=ah=ag=0` **and** `a(8, 5, 14, av, ak, midY+30, 300)` **and** `k.A(11)` | velocities only (+ the hit counter) | + the clip-5 anim-14 spark and the clash sound |
| **op 20 / 28** | @622: `g.b = null; i(43)` | `i(43)` only | drops the grab link |
| **op 38** | @703-798: `S==3` → return; `g.a()` (drains); *then* `S==6` / `S==7` → return | `S∈{3,6,7}` returned before the gate | the gate (and its drain) runs before the S6/S7 exits |
| **op 29** | @348: `av = !r4.av` | `av = r4.av` | inverted |
| **op 30** | @322: `i(arg); ag = ag > 0 ? 1792 : -1792` | absent | ported |
| ops **8 / 9 / 19 / 25 / 28** | @444 (8 = 24), @487 (9/25, gated on `S == arg`), @1413 (19), @622 (28 = 20) | absent | ported (dead: nothing sends them) |

Verified equal: ops 6 (`applyHit6`), 11, 18, 21, 24, 26, 32, 40 and the op-4 arm itself. An
enumeration of every `invokevirtual a:(IIILi;)V` in i/g.javap shows the shipped code only sends
`{4, 6, 11, 18, 20, 21, 24, 32, 34, 38, 40}`; 17 is a bare `return`; 39 / 41 (the `k.Y` marker
pair) are dead and not ported.

## What the fix changed in play — and the capstone re-routes (bot route/input only)

Two chains had quietly depended on the old reading:

1. **A soldier strike on a combo S67-69 used to throw the player into S43.** That dropped `al`
   12 px and the camera (`kP`) with it. It is the thing that kept the two `ax50` pouncers up at
   y783-789 outside `k.ac` (`l()` L77 wants their W *fully inside*) on the mission-5 lip. With the
   faithful arm the hit is an S9 flinch: the S9 hop is `al-1`, the lerped camera settles one pixel
   higher (kP 790 → 789) and the next perch frame at W[1] = 789 sees the player — every pounce
   after that is 5 HP, for good.
2. **The m7 duel's S33 aura pulse and S14 knife barrage** (`ax61` S17 harm arm / S10 shells) now
   flinch a bot that stands and trades blows instead of throwing it clear.

Re-routes — input / timing only; no enemy, state or assertion touched:

| test | what blocked it | now |
|---|---|---|
| `Slice297Test.mission7BossPhaseWin`, `Slice298Test.mission7PostDuelClimb`, the shared `driveDuelWin300` (10 tests) | the bot stood in the pulse / the shells (23 deaths, floor `aB` 440) | `bossDodge297`: step out of the S33 pulse (it only harms a player on the boss's right, ±60 px) and off every knife's landing spot (`ax61` S8 `Z[8]`, then the S10 shell) — phase win at t≈900 with 0-1 hits |
| `Slice307Test.mission7WestDescent` | the uid240 claim taps (every 10 ticks) and the counterweight-catch tap shared one cooldown: a duel that ended a few ticks earlier left the cooldown running exactly when the `ax72` prompt showed and the bot fell past | the catch has its own cooldown (and priority) |
| `Slice289Test.mission5CapstoneLegI` | the lip: every guard strike flinched the bot, the camera ratcheted, the pouncers took over (195 of 196 lives died on the same two cycles) | a spacing policy a human would use: back off while a soldier strike is imminent (`S11` windup from T3, `S12` T≤1; the heavy's `S131/146/155`), never walk INTO a windup, poke only from inside its own reach (48 px), hold the heavy at ≈120 px until it recovers (`S171`/`S156`). Tuned over a 1500-point grid (`rRet 80-90 × tWind × aRange 46-50 × waitFar 115-130 × hRet 130-150 × hHold 108-122`): 292 pass, the chosen point reaches the launch pad with 1 hit and 0 flinches |

## Tests

`Slice410Test` (15): the head (every air-set state upgrades and zeroes `ag`; every attack-set state
flinches; the drain-immune S67 flinches without paying; `g.t != 0` blocks the upgrade; other ops
untouched), op 34 (spark position / anim / `az` / facing, sfx 11, counter), op 20/28, op 38 (gate
before the S6/S7 exits, S3 before the gate, the engage), op 29, op 30, ops 8/24, 9/25, 19.
`Slice407Test` bb-S17 corrected (a bounced knife on an attacking player is the S9 flinch).
Mutation-checked: 13 mutants — 13 killed (head set, `gt`, `ag`, spark, sfx 11, link, op-38 order,
op-29 `av`, op-30 sign, op 28 / 8 / 9 gate / 19).

## Docs

`docs/gameplay-mining/player-mechanics.md`: the two `g.b` overloads, the `g.a()` gate, the op table
(it said "`g.b(S)` (đang block?)" and "attack-state test").

## Not done (parked)

ax60 `bj()`/`c(Z)` (7 records), ax41 `n()`, ax61 `aR()` (all but S8), ax69/72/78/79/32/25/65,
ax76 `bO()`; the ax11 script-claim head @1700-1951; the `k` UI methods (`ah`, `M`, `R`, `Q`,
`G(int)`); `applyHit` ops 39/41 (dead).
