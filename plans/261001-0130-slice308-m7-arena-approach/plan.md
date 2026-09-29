---
title: "Slice 308 — m7 capstone leg L: arena approach"
phase: capstone-m7
status: done
---

# Slice 308 — m7 capstone leg L: arena approach

## Goal

Prove the arena-approach route on the real mission-7 world: from the pillar
top above the west band, through the sky-lift ride and band-top crossing,
into the Colosseum arena and the win-fuse claim.

## Findings (probed live on `world(aj=7)`)

Route proven end-to-end (probe trace, then locked in as a verdict test):

1. **Pillar top (349,540)** — run east + repeated jump; the player sails
   ~300px east over the band-top airspace.
2. **Sky-lift catch** — mid-sail he enters uid225's catch zone
   (ax66-S12, r8[5]=100px) at (643,524) → S209 → S235/S236 bound ride;
   the ride homes him to the lift's position (723,521) then S358 hang.
3. **Jump-release** — M_UP during the ride releases into the ballistic arc
   that lands ON the band top (799,579 — S12 run).
4. **Band crossing** — run east along band top y560-579 to the arena's
   west step at x1040.
5. **Step vault** — at x>990 the 40px step (band top y560 → arena floor
   y520) clears with a run-jump: lands (1073,532) → (1180,515).
6. **Win fuse** — uid303@(1180,371) zone (x1060-1300, y191-551) overlaps
   → kC=303 claim → script 304 runs (uid107 op + boss-3 uid307 consume +
   dialogs) → boss-3 uid307 descends and is consumed by the script.

Decodes confirmed this leg:

- uid281@(1327,583)→script 316 is a long multi-key QTE chain ending at
  key225 with `(37,'0100')` (win op) + `(105,'01070000')` keypress; its
  ops reference uid251 (boss-1, deep floor) + uid107 — the finale chain,
  a separate leg. Its zone hangs at y583-618 just below the arena floor's
  west lip — not reachable by walking the arena floor (player W bottom
  y~559 < zone top 583).
- uid350@(1107,477)→script 349 = one dialog op (`0a000200`); uid303's
  script 304 = the Cesare intro cinematic that consumes boss-3.
- The arena floor runs x1040-1539; its east edge meets the east wall
  x1560 — no east drop-off. Standing on it does NOT reach uid281's zone.
- Script 316 references uid251 (boss-1 @1324,1920 deep floor) — the m7
  win path is the deep-floor Cesare duel, a separate leg from the arena
  approach proven here.

## Verdict test

`Slice308Test.mission7ArenaApproach` — parks (349,540), mask:
`kC→CONTEXT; S358→{av?RIGHT:UP}; ac→UP; aZ&&ak>990&&al∈[540,600]→RIGHT|UP;
aZ&&ak>1200→0; aZ→RIGHT; else 0`. Stage markers:
lift (ac.ax==66) t=47, bandTop (aZ, al 555-590, ak>700) t=74,
arenaFloor (aZ, ak>1040, al<540) t=118, fuse (kC==303) t=129.

Gates: `:core:test` green, verifier ok:true, 57 unittests OK.

## Remaining m7 legs

- leg M: under-arena uid281 zone → script-316 chain → Cesare deep-floor
  duel (boss-1 uid251) → win op 37 → `w.missionWon`.
- Also open: how the player first reaches the pillar top (349,540) — the
  upstream leg feeding leg L (wheel uid53 orbit-west fling or the
  under-arena space are the candidates).
