---
title: "Slice 70 — port k.m(int) camera tracker"
phase: 70
status: done
---

# Slice 70 — `k.m(int)` camera tracker

Ports the original camera tracker `k.m(int)` (k.java:2346-2713) verbatim into
`Level0World.kM(r5)`, replacing the inferred placeholder follow.

## Semantics ported (all arms, file:line cited in code)

- `m(1)` per-tick (dispatch `m(cJ)`, k.java:3320) and `m(2)` snap at init +
  `reload()`; `kAd=2` (k.java:8348).
- Snap arm `(r5 & ad) != 0` → `p.refreshBoxes()`, `kAe = p`, `i.aL = null`,
  `n()`, `camCF/camCE = 0`, `camM = 200`.
- `ai` freeze early-return; `ae==aS && p.aSC()` (S∈{112..115}) grounded-only
  early return.
- camA target chain: gg-midpoint → `i.at` (ax72) → gc/ga ax43 →
  `CAM_CENTER_STATES` + `g.j` + `ga.ax==51 && (S==38 && ac.ax==22)` → `ak-200`;
  S317 lookahead; else `ak - camM` with `camM` ramp 200→266/133.
- camB chain: `aZ`/ga-ax43 → S∈{203,204,62}→146 else `al-150`; line-146,
  CAM_B_DOWN {28,29,315,318}→`al+60`, CAM_B_CENTER→`al-120`, else sticky.
- W-clamps (40px), af/ag lookahead producers, ax43 {1,4} speed arms via `Z[1]`
  + `i.b(aS.Y, ac)` view check, scroll-wall `kAh.W`/`aF==1` clamp.
- Focus watch `camCI != ae.N → camXw++ (cap 40) else 20`; bounds kR/kS/kT/kU;
  slow-mo factor; `lerpStep(d,k) = clamp(d/2, -k, k)` — **dead-zone quirk kept
  verbatim** (|d|==1 → step 0, camera settles 1px short).
- Rope-cd[3] snap-x override; gV warp; edge floor `[0,br-400]×[0,bs-240]`;
  shake; av-latch → `r()` (`kAw=0; kAv=false; kDz=120`).

## Consequences discovered (all faithful)

- `camRect` now = real view `[camX,camY,+400,+240]`; spawn camY=790.
- **Intro-claim freeze**: ax5 zone W=[49,863,116,941] binds on first input
  once `camRect` overlaps it (true now, false under camY=0 placeholder) →
  `velClampTail` (Entity.kt:1977) locks player velocity while scriptStep>0 —
  matches g.java zeroing `aj` at :4037/:4118/:4673.
- **Post-release S22 stall**: releasing a claim bound mid-vault leaves `aj=0`
  → apex arm `ah+aj<0` unreachable → pinned rise until wall bounds. Original
  avoids it because its intro claim binds standing. Documented, not patched —
  the port keeps the original behavior.
- `k.X/k.Y` dual-purpose: `w.kX` stays −7 so `kY = kX shl 8` = −1792 is stable
  for physics; watch counter lives in private `camXw`.
- `k.D()` (k.java:2714-2859) scroll-region resolver remains UNPORTED — next
  slice; then `camXw` can return to real `kX` semantics.

## Tests (`Slice70CamTest`, 8 tests)

Snap placement, per-tick follow, margin ramp, camB sticky/down arms,
look-ahead, dead-zone ±1 settle, watch counter, snap-wall clear. Plus fixes
to stale-view tests: runner-burst test now `e.refreshBoxes()` after
`setAnim(5)` so `Y` lands in the real view (`inPlayV` gate); run test stages
past intro zone W edge; springboard test `setPositionPx(100,920)` inside
spawn view.

## Gates

- verifier: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: all green (incl. Slice70CamTest ×8)
- `./gradlew :android:assembleDebug`: builds

## Left for next slices

- `k.D()` scroll-region resolver (k.java:2714-2859).
- `k.af`/`k.ag` lookahead producers unmined; `ap[]` consumers.
- `d(8/9)` floatie, `ai()`; iOS gate half (needs macOS, RoboVM).
