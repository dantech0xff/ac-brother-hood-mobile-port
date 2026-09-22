---
title: "Port slice 15 — k.l(12) mission-fail screen + f(false) level reload"
phase: rewrite
status: done
---

# Slice 15: mission fail flow (k.l(12))

Replaces the inferred instant-respawn with the original's two-step flow:
death → frozen mission-fail screen → confirm → full level reload.

## Mined semantics

- **`k.l(int r6)`** (`k.java:2031`) — the screen/state dispatcher: writes
  `j.c = r6`; states 12/13 share the fail-screen handler at `k.java:1775`
  (L462): dark banner `b(93,67,214,true,true)` over the frozen world.
  *(proven)*
- **KO → fail**: `d()` drains `x[1]` to 0 → `k.l(12)` (i.java:1389 area).
  *(proven)*
- **Fall below camera → fail**: `i.java:1385-1389` —
  `if (v()==false && al > k.P+240) k.l(12)`. With the camera clamped at the
  world bottom, the player falling more than one screen below the camera
  top = desynchronized. *(proven)*
- **Retry key**: `v(65568)` context edge in the fail screen (`k.java:1804`
  L474 area) routes through `Q()` → `f(false)` = full level reload —
  entities re-spawned from records, player reset, meter refilled
  (`g.e(90)`). *(high-confidence — the reload call is proven; the exact
  checkpoint-vs-spawn placement is `inferred` to spawn for now)*

## Ported

`Level0World`:
- `spawnEntities()` extracted; `resetPlayerToSpawn()` restores
  position/meter/anim/velocities/iframes.
- `missionFail()` on `x1<=0` **or** `player.al > camY+240` (checked after
  the camera clamp update so it only fires when the camera can't follow).
- While `failed`: world sim skipped; `pad.v(M_CONTEXT)` edge → `reload()`
  = respawn all entities + reset player (mirrors `f(false)`).

`Level0Renderer`: fail banner `b(93,67,214,…)` → y-up dark box + red edge
lines (no font decoded — text is `unknown`, flagged).

## Tests

44 `:core:test` green. New:
- KO → `failed` true, world frozen, deaths=1; context tap → reload:
  meter 90, back at spawn, `failed` false.
- Killed soldier (S139 corpse) is respawned by reload — proves `f(false)`
  semantics over plain teleport.

## Honest gaps

- Checkpoint system (`r6==15` branch of `l()`: `cc[]`/`fP[]`/`ap[]`/`au`)
  unmined — retry currently reloads to level spawn, not last checkpoint.
- Fail-cause string (`bx`/`ey` index → text like "DESYNCED") not rendered
  (font pipeline not decoded).
- `j.c==13` variant and softkey-menu branch (`m(bv,bw)`/`eA[][]` dispatch)
  left unmined — single-confirm flow covers the gameplay path.
