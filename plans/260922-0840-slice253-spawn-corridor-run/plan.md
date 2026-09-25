---
title: "Slice 253 — spawn→corridor-floor end-to-end run (verified)"
phase: golden-path verification
status: done
---

# Slice 253 — bot runs spawn to the corridor floor end to end

## What

`Slice245Test` gains the ninth leg — a continuous run from the real
spawn record (85,940) to the corridor floor east of the x1400 wall,
using only held-east + stall-pulsed UP + crate-slash + the S65 zone
eject. No state pinning, no teleports.

## Route proven

1. Spawn platform x80-300 (y940) → east along the trench x300-380
   (bottom y940) → climb the 60px east face to floor x380-1120 (y880).
2. **ax4 destructible crates** at (528,881)/(546,880) wall the floor —
   the bot slashes them (M_CONTEXT) when they are within sword reach.
3. x900-1120 building: 100px west face → face-climb → roof y780.
4. Roof edge x1120 → jump → **ax22 zone1 (1214,636)** capture →
   **zone2 (1316,568)** capture → **ax7 wedge [1318,456..1334,472]** →
   over the wall.
5. Land east → descend to corridor floor ('05' cells x1600+, y800).

## Observed

`RUN corridor=true deaths=0 caps=2 minAl=435` — the whole leg clears
with both zone captures and no death.

## Bot-input lessons (same grammar a player needs)

- Constant UP-hold bounces in place; UP must be pulsed on stall or at
  known lip x-ranges.
- Slash only when an ax4 crate is within reach (`it.ax==4 && S!=139`
  inside ~90px) — otherwise a stall means a face to climb.
- Falling into the x300-380 trench and slashing forever is the
  failure mode when crates/stalls aren't distinguished.
