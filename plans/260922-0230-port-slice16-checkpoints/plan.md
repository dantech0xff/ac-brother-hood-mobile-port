---
title: "Port slice 16 — ax2 aY() checkpoints → reload at snapshot"
phase: rewrite
status: done
---

# Slice 16: checkpoints (ax 2, `aY()`)

Reloads now restore to the last checkpoint instead of level spawn.

## Mined semantics (`i.java:13477 aY()`)

- **Fire gate** (proven): `a(this.W, k.aS.W)` — W-rect overlap with the
  player (alternate `bh[aj]==3` phase uses `this.al >= k.aS.al`).
- **On fire** (proven): `k.y()` (checkpoint sfx/flash — unported, no audio
  hook yet); `k.G = Z[0]`; snapshot written into `bA`: record id
  (`aw`, bA[16]), position (bA[18..22]), facing (bA[22]), `g.J` (bA[24]),
  `g.I` (bA[26]), mission counters `ap[]`, flags `ax/ay/az/aN/aL`, and
  `br[]` respawn flags (bA[76+]); then `k.c(this)` — self-remove.
- **Re-materialize** (proven, i.java:13535+): after the save,
  `k.a(bb[i], bb[i].as)` sends every entity back to its home slot,
  skipping `as == -98` (consumed/dead) and `ax == 70`.
- **`f(false)` reload restores from the bA snapshot** — position/facing/
  stats are the checkpoint's, not level spawn (high-confidence; reload
  plumbing proven in slice 15, snapshot layout proven above).

## Ported (`Level0World`)

- `checkpoints`: ax2 records (`f[1]=aw`, `f[2]=ak`, `f[3]=al`) — level 0
  has 8, x from 1594 to 10016.
- `fireCheckpoints()` per tick: `|cp.ak - player.ak| <= cellPx` and
  `player.al >= cp.al - cellPx` (W-rect approximated to a one-cell
  crossing — `inferred` box dims; overlap *semantics* proven).
- On fire: `consumed`, `checkpointSnap = (cp pos, player facing, x1)`,
  `checkpointDead = aw-set of S139 corpses`, and every live non-ax70 npc
  re-homed (`homeX/homeY` captured at spawn) + `setAnim(0)`.
- `resetPlayerToSpawn()` uses the snapshot (pos+facing+meter) when set.
- `spawnEntities()` re-marks `checkpointDead` `aw`s as S139 — corpses
  before the checkpoint stay dead through reload; later kills respawn.

## Tests

46 `:core:test` green. New: checkpoint fires on overlap + reload restores
checkpoint pos (not spawn); live npc re-homed, pre-checkpoint corpse
stays dead both at save and after reload.

## Honest gaps

- `k.y()` checkpoint sfx/UI flash not wired (no audio path yet).
- bA snapshot fields beyond pos/facing/x1 (`g.I` weapon, `ap[]` counters,
  `aL`, `br[]` granular flags) modeled only as `checkpointDead` aw-set.
- W-box dims of the trigger entity unknown → one-cell crossing heuristic.
