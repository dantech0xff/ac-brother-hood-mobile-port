---
title: "Slice 68 — ax34 ak() player-follower overlay (k.D)"
phase: port
status: done
---

## Scope

Port `i.ak()` (i.java:6914–7000) — the ax34 **player-follower overlay** —
plus the player-init L43 companion spawns (i.java:2748–2779) that create
it.

## What ax34 is (proven)

`k.D` is a **per-level singleton** spawned inside the player's record
ctor (init dispatch case 0 → L43, i.java:2737+): `new i(); ax=34;
aa=k.r(42); i(0); az=player.az; ak/al=player pos; av=false; t();
P|=16|512; k.b(k.D)`. Same block also spawns `k.E` (ax71, `k.r(46)`,
`az=300`, `P|=16|128`, NOT `k.b`'d — a detached overlay slot). `k.V()`
(k.java:6640-6641) and `D()` (i.java:2503-2504) null both. `bi[34]=42`
matches the `k.r(42)` fetch.

`ak()` per-tick (dispatch case 34 → :5099): early-out when `k.aS` (the
player) is null; then `az=99; ak/al = aS.ak/al`, and a visibility rule:

- `r8` (grounded): `g.a` bound → ax15 rope: `al=aS.al+1; az=100;
  r8=true`; ax51 crate → `r8=false`; any other ax → `r8=false` (L10
  only arms on 15 — verbatim). `g.a==null` → `aS.aZ` standing:
  `al+1; r8=true`; else scan ≤5 cells down for cell-20:
  `al=(r02+r92)*20; r8=true` (L15–L20).
- `r9`: `aS.S ∉ hide-list` — the L23 `268` early check plus the L25
  chain {291,61,203,204,277,276,164,183,184,317,250,244,351,353,354,
  355,356,372,373,24,326,150,82,74,334,83,315,318,157,156,149,310,311,
  43,105}.
- `P|=128` hidden unless `r8 && r9 && i.z`; else `P&=-129`.

`i.z` is the static flag (`i.java:110`) already ported as `w.iZ`
(toggled by sub-ops 24/25).

## Port notes

- `LevelCellSource.kD` added beside the existing `kE` slot; both nulled
  in `spawnEntities()`'s clear block (`k.V()` parity) and respawned by
  `spawnCompanions()` at load end.
- Init-time `k.b` inserts now drain into `npcs` immediately at the end
  of `spawnEntities()` — matches the original where ctor-time `k.b`
  lands in the live pool (keeps entity-count assertions honest).
- clip42 + clip46 converted (pack-3 entries 042/046); wired into the
  test clip map and the gdx runtime loader.

## Gates

verifier `ok:true` · `python3 -m unittest` 57 pass · `:core:test` 553
green (Slice68Test, 12 tests) · `:android:assembleDebug` green.
