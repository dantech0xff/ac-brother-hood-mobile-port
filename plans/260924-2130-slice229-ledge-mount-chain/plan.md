---
title: slice 229 — ledge auto-grab + climb-mount chain verdict test
phase: fidelity-verdict
status: done
---

## Summary

Regression test covering the full verbatim ledge-hang mount chain on real
level-0 geometry: fall → `ledgeHangGrab` (S61) → UP edge → S62 climb →
grounded settle on the wall top. All ported arms proven working
end-to-end; no code changes were needed — the slice pins behavior only.

## Chain verified on real geometry

The test scans level-0's collision grid for a wall top with the pocket
`i.al()` (g.java:209-239) requires: `cell(x,y) >= 19`, air above, and the
adjacent column open rows y-1..y+2. Found (wx, wy) = (200, 9).

1. **Auto-grab**: S43 fall at `ah=2560` crossing the lip row →
   `i4 = (W[1]+10)/20` hits `wy`, `i2 = (W[2]+20)/20+1` hits `wx`,
   `iE=20 ≥ 19` → `i(61)`, `ak` snapped to `wx*20`, `al = wy*20-1`,
   `aC=40`. (PlayerFsm.kt postTail L3a2d arm; Entity.kt `ledgeHangGrab`.)
2. **Climb press**: `v(16388)` UP edge at the hang → `H();G();i(62)`
   (PlayerFsm.kt S61/S203 shared arm tail).
3. **Mount**: S62 `r()` → `ak += 10` (into the wall column) then
   `enterStateMasked(aO>12 ? 79 : 0, 9)` → settles S0, `aZ=true`, feet
   `W[3] ≤ wy*20` resting on the lip row's top edge.

## Notes for future slices

- `al` is the feet anchor: standing on a lip keeps `al ≈ lipY-1`, so
  mount assertions must check `W[3]`/`aZ`/`ak`, not `al` dropping.
- The hang probe reaches ~1.5 cells out (`(W[2]+20)/20+1`); the player
  must fall in the column ~2 cells left of the wall for `i2` to align.
- OOB rows return the 20 sentinel and count as solid — falls started
  above the grid "land" instantly; test geometry needs `wy ≥ 6`.

## Gates

- verifier `ok:true`, unittest 57 pass
- `:core:test` green (new Slice89 test passes)
- `:android:assembleDebug`, `:gdx:build` green
