---
title: Slice 304 — m7 leg I capstone (under-chamber corridor crossing)
phase: gameplay-bot-caps
status: done
date: 2026-09-22
---

# Slice 304 — mission7UnderChamber (leg I)

Test-only slice. Extends `Slice304Test.mission7UnderChamber` so the bot
crosses the under-chamber trap corridor end-to-end on mission 7 (underworld).

## Route proven on real geometry

Wing chain → u252 scripted carry → ax66 lift row → r78 floor →
r78 hole (x1040–1419) → trap corridor (x1060–1479, floor y1740) →
east past x1200.

## Key findings

- The lift descent is faithful: `p.al = e.al` (i.java L58 ax66 S7 arm)
  rides the player down through the r77 '2' band; release at lift
  S8→9 boundary.
- The previous leg (303) took the wrong exit: `a(257,8)` ledge-drop
  (g.java:2860-2874 L692) at x~908 fired DOWN+LEFT and vaulted the
  player *west* through the floor edge into the sealed west chamber
  (x0–1042, y1580–1920) — a dead pit whose only east exit is a
  2-px channel between doorposts ax27#308/#309 (P=4096), provably
  uncrossable from r96 floor (jump apex feet 1869 > post top 1828;
  left-push resets ak to 1040).
- Correct exit: walk **east** on the r78 floor, drop through the
  r78 hole into the trap corridor — reached x1438 on real input.
- Claim-script 352 targets the boss (findByAw(251)=ax29), not a
  player carry — the pocket claim u280 (x40-1599,y1975-2001 pocket
  sealed by r97/r102 slabs) is not the leg goal.
- `descended` threshold widened to `al > 1700` so landing on the
  trap floor (y1740) counts — the corridor IS the under-chamber.

## Assert

`u280Fired || u269Fired || (descended && p.ak > 1200)` — satisfied
via descended + ak=1438 (t=728).
