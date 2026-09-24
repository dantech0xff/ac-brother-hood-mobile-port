---
title: "Slice 177 — mission-switch runtime wiring (I(aj) pack swap + G(164) spawn)"
phase: port
status: done
confidence: proven
---

# Slice 177 — mission-switch runtime wiring

Closes the loop between slice 176 (all 8 mission packs converted) and the
staged loader `G(i)`: a `kAj` moved by af() mission-select, the win-screen
`aj++`, or CONTINUE (`kBA[14]`) now actually loads its pack and respawns.

## Source facts (k.java, all proven)

- `I(i)` (k.java:5244-5264) — pack swap: strings `/14` entry `1+aj`,
  records `ec[aj]` (pack 6+aj), layers `et/ep/eu/er` with `bh==3` gating
  `ep` out (flying levels 1/4), `er` for `bh==4||bh==3`.
- `G(i)` (k.java:4741-5090) — staged loader driven by load screen j.g:
  `G(1)` strings + save bytes, `G(2)` `U()+dL` (bh3 only), `G(3)` `I(aj)`,
  `G(4..7)` tileset clip loads `ej[aj*4+i]`, `G(8)` scripts, `G(9..163)`
  per-clip `z[]` loads/masks, `G(164)` `d(false)` entity spawn
  (k.java:5039), `i>164` done → press arm.
- `a(z2)` (k.java:5173) — full reload: `e.b();e.b();V();d(z2)` + field
  clears; `!z2` adds `X();I(aj)`. Call sites: :3785 `a(false)`
  (eC==25 restart confirm), :3790 `a(true)` (checkpoint restore).
- `aj` write sites: select `aj=bw` (:1186/:1202/:1206), win `aj++`
  (:3418/:3428/:3437) with `bA[14]` persist + `aj==7`→credits reset,
  CONTINUE `aj=bA[14]` (:3687), resets (:3667/:3796/:3927).

## Port decisions

- `MissionPack` (Level0World.kt top-level): `{level, levelStrings,
  scripts}` — one `level<aj>` asset triplet.
- `Level0World.level/levelStrings/scripts` promoted `val`→`var` +
  `packLoader: ((Int) -> MissionPack)?` ctor param + `loadedAj`.
- `loadPackI(aj)` = `G(3)` arm — swap only, emits
  `Command.MissionLoaded` on a real change (pack-16 dedup).
- `loadMission(mission)` = `a(false)` — `kAj` set + `loadPackI` +
  `reload()` tail; wired at `reloadCheckpoint(false)` (eC25 retry).
- `menuJc9()` runs the two runtime-relevant `G(i)` stages on their j.g
  ticks: `jG==3 → loadPackI(kAj)`, `jG==164 → spawnEntities();
  postSpawn()` (d(false)). Stats/L() deliberately NOT touched at play
  entry — that's `a(z2)`-only, and `ax=dB` in the press arm already
  carries position/save state.
- `Command.MissionLoaded` added; TickEngine hash branch
  (`md.update(6); putInt(aj)`); AudioBridge no-op branch.
- Renderer: `Level0Renderer.world` field, `tilesetBase(id)` resolves
  `level<loadedAj>/tileset-$id/modules`, `buildAtlas()` extracted,
  `rebuildTilesets()` disposes atlas + clears caches.
- `Level0Game`: `missionPack(aj)` provider (ACLV + strings-N.txt +
  scripts.bin), `loadMissionTilesets()` mirrors `G(4..7)` — `ej`
  tileset clips in/out of the shared negative-key clip map from
  `level<loadedAj>/` + `renderer.rebuildTilesets()`; `MissionLoaded`
  command drain in `render()`.

## Tests (Slice176Test additions, 5 new — all green)

- `loadMission swaps pack strings and entities` — aj7: 100×102, 323
  records, npcs>0, !bh3, MissionLoaded emitted.
- `loadMission flying pack gates bh3 and drops ep` — aj1: layers
  {0,2,3}, 225 records.
- `same-pack loadMission is a reload not a swap` — no MissionLoaded.
- `stats survive a mission swap` — `kBA[14]` persists (save bytes are
  world statics, matching original `F(aj)` semantics).
- `strings follow the swapped pack` — level2 strings ≠ level0.

## Gaps (flagged)

- `G(1)`/`G(2)`/`G(4..7)`/`G(8)`/`G(9..163)` load stages collapse to the
  two runtime stages — resource loading is eager in the port
  (high-confidence collapse, documented).
- `menuJc9` per-tick stage checks are additions to the original shape
  (G() was a per-tick function already — mapping is structural).
- Renderer rebuilds the whole atlas on pack swap (simplicity over
  incremental repack — same visual result).
- Emitted `MissionLoaded` is consumed by gdx/android only; a headless
  world never drains it → harmless queue growth (documented quirk).
