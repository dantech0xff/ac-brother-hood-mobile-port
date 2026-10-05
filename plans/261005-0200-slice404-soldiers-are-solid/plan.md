---
title: "Slice 404 — the soldier is solid to the player whether or not it is alerted; 9 capstone legs re-routed"
phase: "port"
status: "done"
slice: 404
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (I() @7545-7660 shared tail, i.a() @914 push, g.javap.txt f()/S203/S204 carry arms)
---

# Slice 404

The parked finding of plans 401/402. The shared tail of `I()` ends

```
7545: if (aA != 0) goto 7594
      P &= ~16
7563: if (aS.aA <= 2) goto 7644
      if (l()) aS.a(32,0,0,this)  ; goto 7644
7594: P |= 16
      if ((aS.aA & 1) != 0 && aS.g(this) && j != 0) aS.a(32,0,0,this)
7644: if ((aS.aA & 8) == 0) a()          // BOTH branches (and the shortcut) join here
```

`a()` (`i.a()` @914, `Entity.pushContact`) is the solid-body shove: it moves the player to
the nearer side of the body and skips `aS.S > 43`. The port ran it only in the
`aA == 0 && aS.aA <= 2` corner, so an **alerted** (attacking) soldier — the one that
matters — was a ghost: the player ran through it while it struck him.

## Fix (`proven`)

`NpcFsm.tick` tail: the `a()` call moved out of the branches and joins both
(`if ((player.aA & 8) == 0) e.pushContact(world)`); the `l()` / `a(32)` arms stay where
they were.

## The capstone legs (bot route / input only — no enemy, state or assertion touched)

Solid soldiers invalidated every route that ran past an alerted guard. Ten tests failed;
the faithful answers are the ones the game itself provides:

| test | what blocked it | now |
|---|---|---|
| `Slice184Test` S24 pin (unit) | fixture put the pinned victim in S0 — `a()` @265 skips `aS.S > 43` | the victim is in S89 (the S99 arm's `aS.i(89)`), as the arm sets it |
| mission-2 leg A | stationary sentinel aw38 (faces east, `Z5=Z6=0`) stands 15 px from the pillar lip: the mantle put the climber inside its body and the shove dropped him into the spike pit | **ledge assassination** — context button during the lip grab (S60 → S203 carry → S204 throw) while the sentinel is unaware |
| mission-2 leg C | two platform guards: the run fights, then arrives in S12/S233 instead of the S26 lip walk and the jump started too late (fell short of the gap) | jump from any grounded run state on x2360-2419 |
| mission-2 leg Win | three guards on `20`@1880 | strike whoever stands in front (the leg-A/C rule) |
| mission-3 leg E | rooftop sentinel aw640 | same melee rule |
| mission-5 leg B | two flank guards on the block2/3 tops (slice 401 ran past them) | the duel (`chaseMask289`'s melee override) |
| mission-5 leg D | the chain's drop lands on the 600-HP slab guard uid65 | S89 air pin → context button = the stab edge (as the mission-6 legs) |
| mission-5 leg H | two 600-HP soldiers on the x11780 block | stealth kill inside `k()`'s window (≤80x, ≤5y, unaware) |
| checkpoint3→4 (mission 0) | three trench guards, the last one on the rope's foot | strike the guard in front |
| mission-0 end to end | (1) lip sentinel e151 at x2213 (ledge assassination), (2) the 300-HP trench guards were *fled* (`fleeElite`) | assassination at S60/S203; no flee in x4700-5900; stealth stab (`j==0`, player faces, guard does not) before running into a posted guard |

## Tests

`Slice404Test` (6): an alerted soldier shoves the player; an unalerted one still does; a
player state above 2 still meets the body; the shove is skipped while `aS.aA & 8`; no shove
for `aS.S > 43`; the shove distance is `ak ± (pw/2 + ew/2)`.
Mutation: restoring the old tail kills the three tests that pin the new behaviour (the
other three are guards of behaviour the port already had).

## Not done (parked)

- The universal pre-dispatch `collideSides(true)` for ax11 (`NpcFsm.tick`) — plan 402.
- ax11 script-claim head @1700-1951 — plan 402.
