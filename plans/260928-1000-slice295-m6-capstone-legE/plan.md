---
title: Slice 295 — mission-6 capstone leg E (door mass → door179 teleport → win)
phase: capstone-bot
status: done
---

# Slice 295 — mission-6 capstone leg E

## Goal

Fifth and final leg of the headless mission-6 capstone bot: drive the
end-game door chain — from the door mass's top east of the east gauntlet,
down the shaft behind the mass's west edge, through door179's S16
door-teleport arm, out of door178's destination, down the masses east,
and into win zone uid271 — `k.l(15)` mission-complete.

## Route (proven end-to-end, trace in test)

1. Pin `(10700,800)` on the door mass `############## x10540-10819@y840-880`
   — the player lands on the top (y839).
2. Walk west on the mass top; at the west edge `x10540` the S26 bump fires —
   the probe hops UP (jump arc west) over the face into the shaft
   `x10420-10539@y740-980` (open channel west of the mass / east of the
   `##` wall `x10380-10419`).
3. Fall the shaft → land on the lower mass `########### x10380-10759@y1000-1020`.
4. Walk east on the lower mass → the player box overlaps door179's W
   `[10645,904,10695,991]` (its box top ~940 vs box bottom ~1000) → the
   S16 marker arm spawns `ae=105`.
5. Hold UP (`16388`): `w.padHeld(16388) && !w.playerAttacking() &&
   player.aZ` fires `doorExitBh` → zero velocity, center on the door,
   `bindAc(uid178)`, `fadeIn()`, `setAnim(284)` → teleport to door178's
   dest `(10275,1217)` — the ac-link `ac=178` confirms the pair.
6. Descend east: S295 crawl/shimmy across the floor → drop S43 → S33
   climb → S34 descend → land at `(10979,1325)` → win zone `uid271@(
   10924,1166)`'s ax5 → `k.l(15)` mission-complete at `t=179`.

## Key decode this leg

- `initTrigger`'s generic tail sets ax10 W = `[ak+r8[7], al+r8[8], +r8[9],
  +r8[10]]` — door179's W is `[10645,904,10695,991]`, i.e. *below* the
  mass top, inside the enclosed channel east of the `##` wall — the door
  is entered from the shaft floor, not walked into.
- UP is `padHeld`, not edge-triggered: the marker spawns on `rectsOverlap`
  while airborne (S43/S22), then the held UP at the grounded tick fires
  `doorExitBh` — no need to time the edge; holding UP through the landing
  works because `integrate` before the dispatch loop only eats `aZ` on a
  jump *trigger*, and `padHeld` keeps it true once grounded.

## Test

`mission6CapstoneLegE` in `Slice291Test` — pin `(10700,800)`; pad-driver
arms the west hop, shaft descent, east walk, UP-hold door trigger, and the
post-teleport descent + CONTEXT win-zone tap; asserts `w.jC == 15`.

Gates: `:core:test` all green, verifier `ok:true`, 57 unittests OK.
