---
title: "Slice 395 — the ax29 boss aP() and its attack picker aQ() follow the bytecode"
phase: "port"
status: "done"
slice: 395
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (aQ() @0-832; aP() @0-3897; callers aP() S-switch @3009 S9/37, @3150-3168 S4, @1857-1897 S14/35, @1513-1516 arena clamp, @2364-2470 S15/16/6)
  - reconstructed-project/bytecode/d.javap.txt (static{} — the pick table d.a)
  - reconstructed-project/src/simple/i.java (10328-11170 — the misleading listing)
---

# Slice 395

Continuation of the boss/director audit (task "aP / bD against the
bytecode"). `aQ()` and `aP()` were re-read from the raw `javap -c` (the
simple decompile prints the block layout and inverts several conditions);
the port's `bossPick` / `tickBoss` were compared arm by arm.

## `aQ()` — the attack picker (`proven`, @0-832)

```
@0    by == 0                                    → idx 7                  goto @561
@42   by == 3 && ci[2] >= 160 && v() && idle(aS) && aS.S ∉ {9,375}
                                                 → ci[2] = 0, idx 3       goto @561   (STOPS)
@184  ci[3] >= 80 && v() && aB <= 500            → by == 1 && !cm : idx 9, e(17,…), cm = true   (ci[3] untouched)
                                                    otherwise     : idx 7, ci[3] = 0            (@264-272)
@277  bands on |Δx|:
        > 100       → ci[1] >= 48 && v() && idle && aS.S ∉ {9,375} : idx 2, ci[1] = 0, cj = false
                      else idx 4 (by3: 8)
        60 < … <= 100 → if (ci[1] >= 48 && aS.aZ) { idx 6; a(true,0) } else if (ci[0] >= 32) { idx 1; ci[0] = 0 }
                        (no pick → -1)
        <= 60       → ci[0] >= 32 : idx 0, ci[0] = 0   else idx 5
@561  idx outside 0..9 → ah = ag = 0; i(0)                          (@817)
      else ci[4] = 0; v = d.a[idx]; by3 remap 5→40, 8→39, 10→37, 14→35;
      v ∈ {15,16,17} → aura e(4/5/6, ak, al, az+1);  i(v);  sfx 31 for 17/14/35, 33 for 16/15
```

| defect in the port | effect |
|---|---|
| the by3 idle-window pick did not stop — the distance bands that followed overwrote `idx` | the final boss's S17 **finisher never fired** |
| `ci[3] = 0` ran on every finisher-arm exit | the by1 first-time arm (S33 + `e(17,…)` + `cm`) must leave `ci[3] >= 80` so the very next pick is the S7 grab; the port reset it |
| the mid band ran *both* tests (`ci[1]>=48 && aZ` and `ci[0]>=32`) and let S15 win | whenever both windows were open the S15 slash overrode the S5 strike (the original is `if … else if`) |
| by3 remap `5→40, 6→39` | the by3 far band picked table[8] = 10 unmapped: the **inert S10 froze the boss**; table[5] = 8 and table[7] = 14 stayed the by1 clips instead of S39/S35 |
| no aura spawn, no sfx on picks 15/16/17/14/35 | the S15/S16/S17 slashes never spawned their `e(4/5/6)` hit boxes — they could not hurt the player |

## `aP()` — the duel FSM (`proven`)

| where | bytecode | port had |
|---|---|---|
| S4 strike lunge @3150-3168 | `ag = av ? -2560 : 2560` (toward the player) | the sign inverted — the strike lunged **away** |
| S14/S35 barrage @1857-1897 | `while (e(cx,cy) is not standable) r3 += 10` — a loop to the floor under an airborne player | a single `+10` step: path fx aimed at the air |
| S6 / S15 / S16 @2364-2470 | S15/S16 zero `ah/ag`; S6 jumps to the punish check at @2374 without zeroing | all three zeroed |
| S9/S37 @3009 | the `r()` → `Q(); aQ()` test is independent of the `|Δx| < 100 → i(0)` test (it reads the anim just selected) | `else if` |
| arena clamp @1513-1516 | `a(1,0); return` — the S-switch is skipped that tick | fell through into the S-switch |

The rest of the method (head, by-switch, counter/stagger arms, `ci[]`
increment gate, `cn` ramp, `a()` call-site gate, by3 exhaust, S8/39, S2/38/5/40,
S17, S20, S21, S25-S28, S33, S41, S0/36, S3, S7) was re-read and matched
(`high-confidence` — read, not individually pinned by tests).

## Capstone bots (route/timing only)

The faithful picks make the duel longer and give the boss real S15/S16 auras;
that moved the mission-7 capstone's timeline and exposed two bot-side
assumptions. **No enemy was weakened, repositioned or patched and no state was
written** — only the inputs changed:

- `driveDuelWin300` (shared duel driver): steps away from the boss while it
  plays S15/S16 (the aura covers ≈ ±50 px in front of the boss) instead of
  standing inside it.
- `Slice307Test.mission7WestDescent`: the ax72 counterweight catch is the
  grab prompt (`Entity.at` armed in a mountable state). The old bot only tapped
  CONTEXT while the uid240 claim held, so the catch succeeded or failed by how
  its combo taps (S67→S68→S69 vs S67→S69) happened to line up with the walk —
  a 10-tick drift dropped it onto the y1559 static crushers (x480-723, S8 =
  permanent kill). The bot now taps the prompt as soon as it is armed, like a
  player who sees the indicator. (`p.gJ = 5` in that test is a pre-existing
  fixture.)

## Tests

`Slice395Test` (18): the by0 / by3-window / finisher / far / mid / near picks
(incl. off-screen boss, aB gate, `cm`/`ci[3]` bookkeeping, the by3 remap,
auras and sfx), S4 sign and contact, the S14 floor loop, S6 vs S15 zeroing,
S9's independent `r()` test, the arena clamp return. Mutation-checked:
32 reverts, 32 killed (4 survivors on the first pass — sfx 35, the out-of-table
`i(0)`, the `aB <= 500` gate, the S4 contact aura — each got a stronger
assertion and was re-run).
