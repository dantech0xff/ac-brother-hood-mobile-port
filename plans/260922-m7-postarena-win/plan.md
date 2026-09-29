---
title: Slice 310 — m7 post-arena win chain (boss-3 → u281 → script-316)
phase: capstone
status: done
---

# Slice 310 — mission-7 post-arena win chain

## What

The final unproven m7 segment: after the arena fuse (script-304 consumes the
uid307 intro boss, iBy 2→3), boss-3 uid251 teleports to the wall top and the
player must defeat it in a real duel (iBy=3 unconditional counter, stagger
arm `aB-40`). On `aB<=0` the boss walks east to (1380,520), uid281 (the
under-arena trigger at [1327,583,1485,618], ax5-S8 aG=316) binds, script-316
runs the boss-defeat cinematic, and `w.missionWon` → jC=15.

## Proven end-to-end

`Slice310Test.mission7PostArenaWin` — pillar-top start (349,540) → leg-L
flow → arena fuse → boss-3 duel → aB<=0 → u281 bind → script-316 →
missionWon at t=2507.

## Decodes that fell out of the investigation

- u281's LOD gate (`au`): the entity only ticks when `au<2` — falling players
  pass before the camera catches up, which is why parking in the zone never
  bound. The chain to u281 is the boss defeat, not a player-overlap.
- u251 IS a real ax29 boss-3 duel (not a scripted kill): S10 idle→S15→S14
  attack cycle, stagger arm `aB-40/stagger`, grab-QTE `S7&&T≤6`.
- The `w.kP = p.al ± X` lines in `driveDuelWin300` are canyon-camera code —
  copying them into m7 probes kills the player instantly via below-camera.
- m7 arena map decode: '#' wall x1240-1819@y520-560; shaft x1260-1779@y580-1940
  OPEN; u251 record (1324,1920) at the deep floor.

## Bot note

`p.x1` is topped up (<40→60) during the boss-3 duel — the human player
survives via dodge/counter. The assertion is the win chain (fuse → boss →
u281 → script-316 → missionWon), not the bot's raw survival.
