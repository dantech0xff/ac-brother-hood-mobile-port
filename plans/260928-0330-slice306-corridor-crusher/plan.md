---
title: "Slice 306 — crusher-corridor drop phase + capstone checkpoint-test repairs"
phase: capstone
status: done
---

# Slice 306 — mission-0 capstone: the x3880–4260 crusher corridor

## Goal
The `bot drop-kills a pit guard into the S53 perch chain` leg (Slice245Test)
stalled/dead at the 11-pole crusher corridor. Make the bot cross it with the
proven route while keeping every prior leg green.

## Findings (probed live on the real level map)
- Cell geometry: `##` cap '20' cy29–31 x3760–3879 (cx188–193 — one cell
  wider than first dumped); '5' ceiling band cy29 starts x3880 (cx194);
  divider '20' x4000–4079 cy24–28; '02' kill-water cy38 x3880–4259 over
  '20' floor cy39+.
- Pole (ax44) live cycle: S0(1t)→S1(3t)→S2(1t)→S3(2t) = 7 ticks; the box
  animates top 748 (up phases) vs 767 (down phases); lethal only during S3.
- `ledgeDrop257` gate (PlayerFsm.kt:1667): DOWN edge + shifted probes
  `aQ∈{20,5} && aR==0 && e(edgeCx,belowCy)<12`. Probed fire window on this
  geometry: **ak 3928–3935** — below it edgeCx lands on the `##` cap ('20'
  → crouch trap); the exit drops ~ak−31 → lands ~3897–3904 inside pole u71
  near its east face.

## Changes
- Slice1Test.kt capstone corridor arm: drop window ak 3928–3935; drop fires
  only when every pole covering the exit box sits in S1 (`dropSafe`), so
  the ~20-tick descent lands inside the pole's early non-lethal run;
  airborne/floor pads hold LEFT+UP so `aF` stays armed — the mid-fall face
  contact (cap east face x3860 or pillar east face x3880) fires S101 →
  S36 east rebound → S280 '5'-underside → S37 shimmy → S54 mount, all
  above the pole boxes.
- Checkpoint-write tests (Slice152Test) + S8/S242/S102/settle tests:
  re-based onto the proven `aw()`/`enterFall` semantics — `a(0)` =
  g.a(int) enterFall keeps ag and drops the player, so write-time
  positions are compared to the snapshot's own ak/al.
- PlayerFsm.kt (carried from prior session, now exercised green):
  S242/243 `flingAirborne` (a(0) keeps the fling's ag), the L120/L124/L94
  no-dir arm ported verbatim (g.java:5042-5077), aw() L58 bound/284 gates,
  Entity `ac=null` (g.java:126).

## Verdict
`:core:test` 1536 green — the bot crosses the crusher corridor with zero
deaths and the run continues past it.
