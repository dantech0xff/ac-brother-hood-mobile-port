---
title: "Slice 269 — all-mission pack boot smoke test"
phase: gameplay-mining
status: done
---

# Slice 269 — all-mission pack boot smoke test

## What

One regression test: `world(aj)` boots every `level<aj>` pack (0–7)
through the real load path (`stateL(9)` → play), spawns the record
entity set, and ticks 300 live frames clean. First coverage proof that
the slice-176 all-mission conversion actually loads every pack.

## Results (test output census)

- m0 `jC=8` npcs=637 — level-0 Rome (ax11 soldiers, ax44 poles, ax67 decor, ax74 wisps)
- m1 `jC=8` npcs=219 — bh3 canyon (ax24 shrines x61, ax54/56/68 flyers)
- m2 `jC=8` npcs=567 — town mission (ax11 guards x41, ax67 decor x117)
- m3 `jC=8` npcs=691 — city (ax44 poles x128, ax67 x144, ax66 movers x26)
- m4 `jC=12` npcs=215 — bh3 canyon variant (ax24 x54, ax68 x15); idle player starves
- m5 `jC=8` npcs=740 — assault (ax4 volumes x109, ax67 x204, ax66 x29)
- m6 `jC=8` npcs=849 — densest (ax67 x374, ax66 x26, ax80 sentinel props)
- m7 `jC=12` npcs=323 — finale (ax67 x141, ax29/ax58 bosses, ax60 lift x6); idle player dies

`jC∈{8,12,13,21}` are all valid states (play/KO-prompt/dialog) — the
idle player legitimately dies in the hazard missions.

## Files

- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  `all eight mission packs boot and tick clean` appended to
  `Slice245Test`.

## Gates

- verifier `ok:true`, 57 unittests, `:core:test` 1489 tests green,
  `assembleDebug` + `gdx:build` clean.
