---
title: slice 230 — S61 hang release arms verdict test
phase: fidelity-verdict
status: done
---

## Summary

Regression test covering the two reachable S61-hang release arms on real
level-0 geometry, completing the ledge-hang family coverage started in
slice 229 (grab → climb → mount). No code changes — the ported arms work
as mined.

## Arms verified (PlayerFsm.kt S61/203 shared arm, g.java ~L2500)

From the S61 hang (`aC=40` armed by `ledgeHangGrab`):

1. **Manual release** — front cell `e((ak±10)/20,(al+10)/20) >= 12`
   (wall still there) + `v(33024)` DOWN edge → drop:
   `H();G();al += W3-W1;a(0)` → masked S43 fling.
2. **Grace expiry** — `aC--` each tick; `aC==0` → same drop → S43.

The ax43-link block (`ga.ax==43` suppresses the release) is not covered
by geometry — it needs a ride-carrier link the test world doesn't spawn;
flagged for a future scripted test if it ever matters.

## Notes

- Both releases land in the same `a(0)` masked S43 fling — asserted
  exactly.
- `aZ` is still true on the release tick (probe hasn't re-run); don't
  assert groundedness until a later tick.

## Gates

- verifier `ok:true`, unittest 57 pass
- `:core:test` green (new Slice89 test passes)
- `:android:assembleDebug`, `:gdx:build` green
