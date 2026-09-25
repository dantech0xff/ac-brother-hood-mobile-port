---
title: "Slice 221 — coverage-closeout provenance sweep"
phase: port
status: done
slice: 221
---

# Slice 221 — coverage-closeout provenance sweep

## Intent

Port-coverage audit found the game simulation is essentially complete: every
`g`/`i`/`k`/`j`/`e`/`c` proc has a verified Kotlin counterpart (all ax types,
the `cv`/`aF` wall-grab, floatie spawns, equip wheel, script ops 100-114,
HUD/menu/dialog render paths, `k.b(boolean)` window+veil, `k.s()` streak,
`k.r()` reset, `k.e(boolean)` ASBR save, `j.keyPressed/Released` pad-word
model). What remained misleading were ~15 comments still claiming
"unported"/"not ported" for code that later slices wired in — this slice
corrects the provenance labels so coverage reads true. No semantics change.

## Audit method

- Enumerated `g.java`/`i.java`/`k.java`/`j.java` method lists and name-matched
  each to its Kotlin port (Kotlin names carry `g.x()`/`i.x()`/`k.x()` source
  cites); checked the last unmatched procs (`i.S/T/U/V/R/O/P/Q`, `i.aA..aL`,
  `i.ax/ay`, `k.s/r/e/u`, `h.a`, `j.keyPressed`) — all ported or proven-dead.
- Grepped `unported|not ported|not yet ported` across `rewrite/core` +
  `rewrite/gdx`; verified each note against the source and the current port.

## Fixes (comment-only, all `proven`)

| File | Site | Was | Now |
|------|------|-----|-----|
| Entity.kt | `gD` field | "producer arm unported — stays false" | producers ported (S12 dismount + S90 launch arms, PlayerFsm:151/:209) |
| Entity.kt | after `releaseCascade` | orphaned stray "bit4 = mount request, producers unported" | removed (gJ bit4 producers landed slices 25–26) |
| Entity.kt | `land()` doc | ">20-cell fall-damage hook … skipped" | wired below via `applyHit(21,…)` |
| Entity.kt | `kAyAt` doc | "Pool unported → inert" | allocated but never filled — proven-dead (verbatim) |
| Entity.kt | `kM` doc | "internals are the unported camera system" | ported as `Level0World.kM` (k.m() tracker, slice 70) |
| Level0World.kt | `hintPending` | "hint arm itself is not yet ported" | ported as `tutorialHint` (:1658) + 3 NpcFsm callers |
| Level0World.kt | stateL i=27 | "audio stop (unported)" | `e.b()` ported (:1402 → `z()`) |
| Level0World.kt | jC21 modal doc | "visual draw is unported" | draw lives in renderer (slice 103) |
| NpcFsm.kt | header | "player stealth states not yet ported" | stealth gates ported (`losL`/`spotB`: aA&8, blind poses, ax69 blind, iBn) |
| NpcFsm.kt | tickDecor doc | "floatie — unported" | `spawnFloatie` at :3621 |
| NpcFsm.kt | projSweepBc doc | "floatie spawns remain unported" | `spawnFloatie` at :6944/:6959/:7041 |
| NpcFsm.kt | ax69 doc | "mission advance, unported" | `kBw=-1;kBx=57;screenL(13)` in the 4/11 arm |
| NpcFsm.kt | spotB doc | "k.aY pool is unported → inert" | allocated-never-filled, proven-dead |
| PlayerFsm.kt | header S10 | "`ab` mirror — not ported" | ported (:943 box track) |
| PlayerFsm.kt | flightAliveV doc | "u()/au>i … unported" | `offscreenScore`/`inPlayV` on Entity |

## Notes kept deliberately

Honest labels on live gaps stay: cheat/debug toggle (`Entity:1520`), `f.b()`
menu hook (:2987), `\0\2` font markup, `E()`/`ac()`/`ad()` render teardowns,
`k.bQ` map consumer, `o()→ao()` debug arm (g.java:3566), jc2 soft buttons,
`f.bF`-adjacent slots — each is a real stub or deliberately skipped arm.

## Gates

- `:core:compileKotlin` — clean (comment-only diff).
- `verify-static-reconstruction.py` → ok:true; `python3 -m unittest` (57);
  `:core:test`; `:android:assembleDebug`; `:gdx:build` — all green.
