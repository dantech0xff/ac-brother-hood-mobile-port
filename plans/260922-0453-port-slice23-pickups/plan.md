---
title: "Port slice 23 — ax14 aX() pickup/marker lifecycle"
phase: port
status: done
---

# Slice 23 — ax14 `aX()` pickups/markers

Ports the ax14 entity family (record-spawned pickups, persistent markers,
linked rewards) verbatim from `i.java` `aX()` + the L88 record-init arm.

## Mining (all proven unless noted)

- **Init — L88 arm** (`i.java:2867-2880`): `az=200`, `aD=r8[7]`,
  `aE=r8[8]` (arming threshold), `o=r8[11]` (linked entity `aw`,
  `-1` = none), `j=r8[12]` (watch state), `P|=512`, `o!=-1 → P|=128`
  (hidden until armed), `r8[4]==1 → aA=1` (persistent marker).
- **Record W fill — L419** (`i.java:3699-3725`): ax14 records fall through
  the `ax!=14` skip into `W = [ak+f7, al+f8, +f9, +f10]` — a live box.
  `a()`-spawned ax14s keep `W==null` → `aX()` head-returns → **inert
  visuals** (k.c markers, S71 edge arrows).
- **`aX()`** (`i.java:13410-13475`):
  - `W==null → return` (spawned visuals inert).
  - `a(aS.Y,W) && !aS.f()` → `aF=1`, `P&~128` (arm + unhide). `f()` =
    `g.java:3763` — carried-prop check suppresses pickup while holding.
  - `S==107` → follows the player anchor.
  - Linked (`o!=-1`, resolved via `k.q` at `k.java:5887` — `aS.aw==o →
    aS` else `bb[]` scan): in-play (`bh[aj]==3`) waits `bZ>=aE`, then
    unhides, `ah=k.Y`, `aC=15` countdown → `k.c(this)`; waits while the
    target `P&32` (held) or `P&128` (hidden). Off-play: re-hide on leave;
    `r5.S==j && aA!=1 → k.c(this)` (watch-state consume).
  - Unlinked: `aF==1` and player leaves W → `aA==1` hides (`P|=128`),
    else `k.c(this)` = collected.
- **`bZ` producer unmined** — never written inside `aX()`; the gate fires
  whenever `aE<=0`. Flagged `inferred`.
- **Spawner semantics** (`i.java:4799` 4-arg `a(ax,clip,anim,az)` +
  `i.a(int,int,int)` at `i.java:9810`): `aw=-1, au=0, ax=14, clip9,
  i(anim), az=302`, `t()` — **no `P|=512`** (that flag belongs to the
  7-arg ax8 particle spawner). Slice-22's `spawnPickup` corrected
  accordingly.
- **`k.c(x,y,aw)` marker** (`k.java:870-886`): `new i; ax=14; clip9;
  i(54); az=302; au=0; ak/al; t()` — zero-W → inert under `aX()`.

## Port

- `NpcFsm.initPickup` — L88 fields + L419 W fill verbatim.
- `NpcFsm.tickPickup` — `aX()` verbatim (incl. `aS.f()` via
  `Entity.isHolding`, `k.q` via `world.findByAw`).
- `Level0World`: `ENTITY_CLIP[14]=9`, record init dispatch, tick dispatch,
  `findByAw`, `spawnPickup` corrected (`au=0`, no `P|=512`), marker
  `au=0`.
- `Entity`: `ci`/`inFrontOf`/`isHolding` (`g.f`), `oId`/`j`/`bZ` fields
  wired. `ci` producer (g.bd[] interact scan) flagged `unported`.
- `refreshBoxes` L82 arm corrected: U<0 bounds index is raw `T` (not the
  object index used by the U>=0 arm — `r07 = d(S,T)|((i[r06]&0xC0)<<2)`
  IS the object index there).

## Verification

- `:core:test` — 75 green (9 new: init fields, zero-W inert, arm+collect,
  persistent hide/unhide, S107 follow, linked countdown, held/hidden
  waits, holding suppression).
- `verify-static-reconstruction.py` → `ok:true`; `python3 -m unittest
  discover -s tests` → 57 green.
- Emulator boot clean: `level0: 637 records … npcs=454` (no crashes).
- Level-0 carries **zero ax14 records** — lifecycle exercised by tests +
  dynamically-spawned pickups (S28 prop drops, k.c markers).
