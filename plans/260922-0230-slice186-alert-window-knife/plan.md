---
title: "Slice 186 — g.e() L33-L88 block: alert window, knife pool, companion J()"
phase: port
status: done
---

# Slice 186 — g.e() pre-dispatch block + `k.aY` knife pool

## What

Ports the missing `g.e()` L33–L88 pre-dispatch block (g.java:535-583,
proven) and the proven-dead knife-spawn machinery it calls:

- `k.E.J()` → `Entity.followJ(p, w)` (i.java:7002-7020): the ax71
  companion overlay mirrors player `ak`/`al` + facing bit each tick,
  hides (`P|128`) on anim end or under the `j.c==21` dialog unless the
  `k.u==8` skip key is held.
- The `aA|256` alert window (g.java:538-555): `Z[1]` decays per tick
  unless blinded (`i.bn` && player `aA&8`); at `Z[1]>=120` it throws the
  knife via `k.aY[0].Z[4]` (proven-dead — see below) and flips
  `aA&=-257; aA|=16`.
- The `aA|16` cooldown (g.java:556-570): `Z[0]=3000` arms on entry,
  decrements by `j.f` (=62), at ≤0 re-arms `aA&=-17; aA|=256`.
- Dead → `i(50)` (g.java:576), `cn++` → `Entity.gCn` (g.java:580 —
  write-only, proven-dead counter), `k.aA`-gated `aA|=1` (g.java:581-583).
- `i.b(int,int,int,int,int)` → `Entity.spawnKnife(w, aG)`
  (i.java:4818-4845): the `a(5,1,8,300)` knife child — verbatim boxes
  re-seed, `n=1`, `P|128`, `Z` wiped {2:-1,3:-1}, `aG!=-1` → `h(k.s(aG))`
  bind + `P|512`, `k.b(aK)` insert. Args r8-r11 accepted unused.
- `k.aY` pool → `Level0World.kAY` (k.java:211 + :8425 `new i[3]`):
  declared before `init{}` (spawnEntities nulls it per-slot at
  i.java:2541 via `kAY.fill(null)`); `kAyAt(i)` accessor now live.
- Third dead call site wired: `b(i)`'s `bn` arm in NpcFsm.spotB
  (i.java:1588-1590) now calls `p.spawnKnife(w, aY[0].Z[4])` verbatim.

## Proven-dead call paths

`k.aY` is allocated but **never assigned a non-null entity** — all
three throw sites (g.java:541, i.java:1588, and the eval-tail's
`b(aY[0].Z[4],…)`) only gate on `aY[0] != null`, so the knife can never
actually spawn in the shipped game. The `aA`/Z-window machinery itself
is live code and ported verbatim; `spawnKnife` and `kAY` are ported for
parity so the dead calls type-check verbatim rather than stubbing.

## Notes

- `Z[0]/Z[1]` on the player are the alert-window counters (distinct use
  of the shared `Z[]` param array).
- `j.f` ported as the 62ms tick constant (matches NpcFsm.kt:3268
  convention); `(j.f % 2)` sites elsewhere already read `tickIndex`.
- `k.aA` (`kAA` accessor) still returns 0 — the `aA|=1` arm stays inert
  until the global is produced by an unported system.

## Gates

- `python3 scripts/verify-static-reconstruction.py …` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → all pass (incl. 8 new Slice186Test cases)
- `:android:assembleDebug`, `:gdx:build` → OK
