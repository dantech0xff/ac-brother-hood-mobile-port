---
title: "Slice 160 — i.d(i,x,y) score-floatie spawner + 4 wire-ups"
phase: port
status: complete
---

# Slice 160 — score floaties (i.java:16745)

`i.d(int i, int x, int y)` = the "+score" popup spawner: spawns an
ax24/clip40 `S=i` child at pixel (x,y), `av=false`, `N/O` 8.8 pos, vel 0,
`t()`, `k.b` insert — ported as `Entity.spawnFloatie(world, i, x, y)`
(:3544), built on `spawnChildFx` (`a(24,40,i,201)` — i.java:3644).
spawnChildFx's `settleToGround` runs before the pos re-anchor so it's a
no-op vs the raw `a()` — flagged.

## Wire-ups (4 sites — all now spawn the floatie verbatim)

| site | original | arm |
|---|---|---|
| NpcFsm:2961 | `d(8, ad.ak, ad.al)` (i.java:16450) | ax67 bk27 springboard ax68-link scan — after `ad.i(2)` |
| NpcFsm:6285 | `d(8, ad.ak, ad.al)` (i.java:14178) | `ba()` X-sweep ax54 arm — `ad` hit |
| NpcFsm:6300 | `d(8, ad.ak, ad.al)` (i.java:14201) | `ba()` X-sweep ax30 arm — `aB<=0` kill |
| NpcFsm:6382 | `d(9, ak, al)` (i.java:13663) | `ba()` aG==1 trail arm — `S==22` gate |

The 4-arg `d(i,x,y,az)` (i.java:8607, ax61/clip71 boss spark) was already
ported as `spawnBossFx` — untouched.

## Stale comments fixed

- `:553` ax10-S53 — "g.D producer arm unported" → set by the S90 launch
  arm (slice 159 landed it).
- `:1706` — "`aS.y()` unported" → `hitWall()` (slice 156 landed it).

## Tests (Slice160Test, 3)

- `spawnFloatie` fields: ax24/S=i/pos/N-O 8.8/vel 0/az=201/av=false.
- bk27 springboard ax68-link → `d(8)` floatie at child pos (+S+1,
  `ad.i(2)`, `r0.i(10)`).
- `ba()` aG==1 arm: `S==22` → `d(9)` floatie + retire flags; `S==23` →
  none.

## Gates

verifier `ok:true` · 57 unittests OK · `:core:test` green (incl. 3 new)
· `:android:assembleDebug` + `:gdx:build` green.
