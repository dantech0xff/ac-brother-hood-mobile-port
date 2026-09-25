---
title: "Slice 245 — headless bot playthrough test (spawn → corridor)"
phase: playability
status: done
---

# Slice 245 — headless bot playthrough regression test

## What

`Slice245Test` (appended in `Slice1Test.kt`): a scripted "bot" that plays
level-0 end-to-end through the real `w.tick` loop — injects pad masks only,
never teleports or pins states. Progress is measured by `maxAk` / `minAl`
and every stall/KO is logged with nearby entities so blockers are judged
verbatim-vs-port, not silent.

## Verbatim route proven (maxAk = 1821)

Trace from the run (t:transition@ak,al):

```
12→33 @1391,763   wall-grab at the x1400 face
33→92 @1379,697   wall-kick bounce (S92)
92→36 @1345,670   launch arc back west
36→65 @1316,568   ax22 zone CAPTURE at (1316,568)
65→313 @1326,464  vault carry over the wall top
313→43→5→12       land roof, run east
12→74 @1714,524   edge dash off the roof into the shaft
43→315 @1734,613  ax10-S36 bound CATCH → hang at W edge (gk=1)
315→43 @1748,699  DOWN edge (33024) releases → falls to corridor
43→89 @1758,749   killTouch pin on ax11 head (e.j==6 bounce)
89 …held 65568…   grab-kill QTE (NpcFsm L129) → counter-kill
```

Also proven: `foeNear` combat at crates (x504), the corridor soldier
fight, `jC12/13` KO → `pad.e(327712)` restart-tap respawn, and the
killTouch/QTE kill path.

## Geometry decoded (level0.aclv)

- Street floor y860 x1240–1560; Wall A x1400–1560 solid y480–860.
- Corridor x1580–1780 open y680–800; '05' floor row40 x1580–2120.
- Chimney x1740–1820: faces x1740 (to y580) / x1820 (to y440), channel
  open to y440; left-block NE corner lip at (1740,520).
- Bound catch W=[1734,600,1770,679] (aw21, gk=1) = shaft safety net —
  hang is a rest pose; DOWN edge releases to the corridor. No lip in
  probe reach → intended continuation is the corridor (or re-kick).
- ax22 aerial capture chain (1975,605) / (2064,695) / (2104,546) =
  the designed route east over the corridor top.
- Second pack test: parked at (2650,519) the bot beats the x2773
  three-guard cluster — maxAk 3040, 1 KO.

## Faithful blockers (not bugs)

- The ax11 pair at x1759/x1865 in the corridor = 2-hit-KO wall for
  naive mashing (`x1=30` base meter is verbatim difficulty). Assertion
  set at `maxAk > 1790` — past the shaft catch into the corridor.

## Input vocabulary used (all proven masks)

- 16390/16396 vault edges, 16398 jump edge, 33024 DOWN release,
  65568 context/QTE, 327712 restart tap, directional holds + UP-hold
  wall-kick latch (`aF=1`).

## Files

- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  `Slice245Test` class appended (bot loop + pack test).

## Gates

verify-static-reconstruction ok:true; unittest 57; `:core:test` green;
`:gdx:build` + `:android:assembleDebug` green.
