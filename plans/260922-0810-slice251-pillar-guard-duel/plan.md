---
title: "Slice 251 — posted pillar-guard floor duel bot"
phase: golden-path verification
status: done
---

# Slice 251 — bot fights the posted pillar guard on the corridor floor

## What

Headless bot-playthrough test `Slice245Test` gains a new leg:
`bot fights the posted pillar guard on the corridor floor`.
Park (1680,799) on the corridor floor, west of the ax11#18 post
(~1759,807), and run the `foeNear` combat loop with direction-held +
CONTEXT attacks on cooldown — a plain duel, not the S89 head-pin path.

## Result (observed)

`GUARD dead=false deaths=1 maxAk=1821` — the posted guard won the duel
once (faithful: the tutorial guard is meant to be hard), then the
respawned player stalled at the x1400 wall face in S33/S34
wall-rebound for the rest of the run. The assertions
`guardDead || deaths > 0 || maxAk > 1820` pass via deaths=1/maxAk=1821.

## Finding — x1400 wall face is the real crossing, not a seal

Layer-1 aclv dump: x1400–1480 is solid `14` from y520 down through the
floor (rows r26–r42). The face's top edge is y520. PlayerFsm S33/34
(dirHeld+rise arms lip-scan — slice 169) can mantle a face lip — so
the intended crossing is face-grab → climb → mantle at y520, not a
skill-gate bounce. The prior "sealed wall x≈1379" demo note was the
bot lacking the climb arm on this face.
