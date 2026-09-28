---
title: "Slice 302 — m7 west-wing chain (spring landing → u252 claim)"
phase: capstone
status: green
---

# Slice 302 — m7 west-wing platform chain

## Leg
Drive the post-duel bot through the full west-wing approach:
duel → rope → shelf → spring u33 → **land (128,1379) → platform chain
u45@(184,1214) → u47@(236,1143) → u248@(318,1136) → mlogic u252@(301,1057)
claim fires** → continues east to ~(375,1058) on the '05' platform.

## Decoded geometry (proven — level7.aclv layer-0 grid)
- Spring arc (-50,-90) from u33@(336,1522) lands at ~(128,1379) on the
  wall-top edge beside the x100-120 wall (top y1340).
- x0-100 "pocket" is a dead-end shaft (wall-kick trap) — earlier bot
  loops were falling into it.
- The real route goes east-up through an open hall x20-460, y980-1280:
  '05' one-way platform x140-280@y1300 (jump up through it), then the
  ax66 platform chain u45/47/248 (S259-266 perch-launch family:
  UP-edge → S263 up-arc, direction-edge → S264 side-arc).
- mlogic u252@(301,1057) is a claim zone (S8 watcher): fires `kC=252`
  on arrival, releases — the progression marker for the west wing.
- Cap y940-960 (x0-460) has its opening at x460-580 — next leg.

## Proven assertions
`mission7WestWingLanding`: launched + landed + `kC.aw==252` fired +
minAl<1100 + maxAk>350. Test-only.
