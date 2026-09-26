---
title: "Slice 274 — door gauntlet phase-gate: capstone crosses x3884–4259"
phase: port-slice-274
status: done
---

# Slice 274 — the door gauntlet is beaten

The capstone bot now crosses the entire 11-crusher corridor
(x3884–4259) and pushes ~2000px further east to **x6052** (assert moved
to `maxAk > 5800`). The route legs were proven individually across this
slice's probe runs; this slice closes the loop by phase-gating the
lethal floor landing.

## The corridor entry chain (proven end-to-end)

1. '5' strip top (x3900–3999, y579) — stand west, wait for phase.
2. **Phase gate**: drop via `a(257,8)` ONLY on door-2's 2nd S1 tick.
3. S257 falls through the '5' bar → exits at x3919 → S43 fall → land.
4. Floor: `S21` squat (3t, forced by `landArm` edge-check) → `S22` rise
   drifting west → `hitWall` + aF + UP|TAP → **S101 face-grab** on the
   mass east face x3879.
5. S36 auto-bounce east+up → head crosses '5' band (cy29 y580–599) →
   **S280 '5' grab** → S38 hang → S37 shimmy east over all 11 doors →
   east-lip mantle (4330,580).

## Door-phase math (proven by instrumented traces)

- The 11 ax44 crushers run a 7-tick cycle `S1×3,S2×1,S3×2,S0×1`, offset
  ~1 tick each (west lags east: door-1 @3898 = door-2 pos −1).
- **Only S0's slam is lethal** — the bot survives S3 on the floor
  (verified: 8 ticks of floor exposure across S1/S2/S3, no death).
- Down-press → landing is exactly +14 ticks ≡ same cycle position, so
  the gate fires on door-2's `doorS1Ticks == 2` → lands at door-2 pos2
  → door-1 pos1, door-3 pos3 — all non-S0; the next S0s fall at
  land+4/+5/+6 once he is rising above the y748 blade band.
- The vulnerable window is only while W-bottom ≥ 748 (~6 ticks:
  S5→S21→early S22); rising past y740 he's untouchable.

## Dead ends ruled out this slice (proven by probes)

- **Mid-fall '5' grab**: the fall's head crosses the cy29 band in a
  single tick; `aO` lags ~1 tick and never samples it. Dead.
- **Mid-fall face grab**: S257 exit lands W0 ≈ x3886–3891, 7–12px short
  of the face x3879; `ag=0` gives no drift. Dead.
- **S233 vault zone** (x≤~3926): DOWN+dir resolves to a west vault over
  the lip into door-B's teleport — the S257 fireable band is x3928+.
- **Floor landing without the gate**: every un-gated landing dies —
  deterministic respawn replays the same landing phase.

## Capstone result

`CAPSTONE won=false deaths=301 maxAk=6052` — assert `maxAk > 5800`.
New frontier: combat vs the x6490 patrol (dies at x6390 in a respawn
loop) plus a pit-return loop at x5918 — next slice's work.

## Changed

- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  capstone route policy for x3900–4700 (phase gate `doorS1Ticks==2`,
  strip-top converge/flip, floor+jump arms, shimmy east holds) + door
  phase instrumentation in the trace + assert to >5800. No main-source
  changes — the corridor chain runs on the shipped FSM verbatim.
