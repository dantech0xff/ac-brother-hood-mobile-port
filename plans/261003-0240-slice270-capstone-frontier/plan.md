---
title: Slice 270 — mission-0 capstone bot + channel-lip blocker characterization
status: landed
---

# Goal

One continuous headless run from spawn to mission-complete (`jC==15`) with no
teleports — the capstone driver for the whole traversal program. Stitched from
the per-leg drivers (slices 245-262) keyed on `ak`/`al` position, so respawn
positions replay their own leg naturally.

# Outcome

- The bot drives spawn → corridor → **ax7 ejection slot** (swallow at
  (1326,464), eject `ag=+2048`, land shelf top (1664,579)) → under-shelf pocket
  floor y800 → **wall-B zigzag** (pillar face x1740 y540-680 ↔ wall-B face
  x1820 y400-780) up to a deterministic ceiling at the **pillar lip (1740,519)**.
- Assert: `maxAk > 1790` (proven frontier: reached the zigzag past the pillar,
  `maxAk=1816`, `minAl=455`). The full-completion assert `>2500` is parked in a
  comment with the blocker write-up — raise it when the crossing is proven.

# Channel-lip blocker (characterized this slice)

The 119px gap lip→ledge (y519→y400) has no proven traversal:

- **Lip wins the race**: `ledgeLipGrab` (Entity.kt:1756) scans the ADJACENT
  column `i2=(W[0]-5)/20` at hand-row `(W[1]+10)/20` — fires 20-70px before the
  wall-grab window (box top-left cell==20) opens. Every west-arc fall ending at
  `ak≤1748` snaps to S60 at (1740,519).
- **Mantle-off-lip is a dead exit**: S62 pops `ak+10` → lands (1750,519) → box
  west edge ≥1742 scans open col87 → dodges lip AND grab → free-falls to pocket.
- **'15'-contact (L1d4 → i(34)) can't fire**: needs box TOP-LEFT in col86 at
  rows 26-27 while the lip band (hand-row 26) opens first — postTail scan runs
  after collide and always wins the overlap.
- **ax22 vault arcs can't top the ledge**: `ag=±3328, ah=-3840` apex ~y470 >
  y400. All five region chains exit east (`Z[2]=1`).
- **Under-route sealed**: wall-B '14' sits on the '05' floor x1560-2200 at y800;
  below-floor space (y820-940 open, '14' at 960) is a dead pocket.
- **Reverted (this slice)**: the S12 `L16c0→L17c9` fallthrough fix (proven —
  every non-transition path re-arms `l()` input) breaks 5 tests: 4 verified bot
  legs + the z-flag test. Parked for its own slice with test updates.

# Next attack vectors

- ax5-S3 countdown zone (block top+east face [1385-1505,349-553]) — fires
  `eventArm(2)` every pass; check whether its script advances level state.
- Re-verify S12 fallthrough + pin/un-pin arms on the channel faces — the
  designed climb may rely on `l()` input during S12 pins.
- Phase-tune the kick cycle: land a fall with box-top skipping the lip band
  (510-530) into the 530-540 '15'-contact band → S34 ride → UP kick east.
