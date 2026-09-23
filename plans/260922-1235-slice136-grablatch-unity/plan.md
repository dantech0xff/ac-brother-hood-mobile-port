---
title: "Slice 136 — g.j unification + ax11 aD() crate-ride"
phase: rewrite-port
status: done
slice: 136
---

# Slice 136 — g.j unification + ax11 `aD()` crate-ride (i.java:7167)

Two linked pieces: the `g.j` grab-latch readers were reading a dead world
flag, and the ax11 `aD()` crate-ride helper (which is one of the latch's
readers) wasn't ported.

## What landed

- **`w.gj` → `Entity.grabLatch`** (proven): `Level0World.gj` now delegates
  get/set to the real `g.j` companion var (slice 135). Before this, `w.gj`
  was declared but never written — the four readers were dead-gated:
  `crateContact`/`crateLandSpot`/`tickPushable` mount arm (NpcFsm:1113,
  :1120, :1132) and `canEngage73` (:6637). Now the S146/147 latch claim
  actually blocks mount offers + ax73 engage, and `teardown()`'s clear
  releases them.
- **`crateRide11(e)`** = `i.aD()` (i.java:7167-7205, proven): ax11's
  crate/carrier ride helper — binds `s` to a `Z[7]`-linked (`k.q`) ax51 /
  ax43 entity on `overlapI`, tracks its top (`al = s.W[1]+3`,
  `ak += s.ag>>8` on bind), `i(96)` while the crate plays S2,
  `!g.j && aA != 0 → Q()` face-player (`Q()` = `av = aS.ak < ak`,
  i.java:6207 — the latch read at i.java:7180), side-edge drop
  `i(22)/i(23)` when `e(cx,cy) < 5` (the `!h` open-cell gate) and
  `aA != 0`, unbind on lost overlap.
- **Call site** (i.java:4165-4170, proven): `patrolArm` tail — every
  patrol tick while `S != 85` runs `crateRide11` then feeds a moving
  crate's `ag` into the soldier.
- `world()` test factory resets `Entity.grabLatch` — the static leaks
  across tests otherwise (the in-game `teardown()` clear, mirrored).

## Verbatim quirks kept

- `i(23)` on both sides of the edge drop — the `ag<0` arm reads
  `av ? i(23) : i(23)` (degenerate ternary, i.java:7196-7199).
- `h(cx,cy)` = `e(cx,cy) >= 5` (i.java:5853) — `!h` = cell < 5 open.

## g.j reader inventory (i.java)

| reader | site | role | status |
|--------|------|------|--------|
| `aD()` `!g.j → Q()` | :7180 | crate-ride face-player | ported (this slice) |
| `bo()` `g.j && overlap && g.a==null` | :15564 | mount offer during wall seq | ported as `crateContact` (NpcFsm:1111) |
| `bp()` `if (g.j) return false` | :15567 | falling-onto-crate detect | ported as `crateLandSpot` (NpcFsm:1118) |
| `canEngage73` `w.gj` gate | NpcFsm:6637 | ax73 engage block | already ported, now live via unify |

## Tests (Slice136Test, 7)

bind via Z[7], top tracking + Q() flip, latch suppresses Q(), `i(96)` on
crate S2, unbind on lost overlap, `w.gj`↔`Entity.grabLatch` delegation,
patrolArm wire-in feeds `s.ag`.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` green ·
`:android:assembleDebug` green · `:gdx:build` green.
