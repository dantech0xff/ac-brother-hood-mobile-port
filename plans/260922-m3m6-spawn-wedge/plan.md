---
title: Slice 312 — briefing-entry spawn wedge (menuJc9 missing resetPlayerToSpawn)
phase: fix
status: done
---

# Slice 312 — mission-entry spawn wedge (m3/m6 insta-fail)

## The bug (device-verified)

Clean entry into m3 / m6 insta-failed: the player fell from (85,940)
and died at the world bottom. The KO→YES reload landed the pack's real
spawn (m3→21,699, m6→17,740) and played fine.

## Root cause

`menuJc9` (the briefing LOADING→TOUCH screen) ran `spawnEntities();
postSpawn()` at `jG==164` but **never `resetPlayerToSpawn()`**. So the
player entered every briefing-loaded mission at his *previous* position
— level0's record spawn (85,940) on a fresh boot, or the prior mission's
end position on sequential switches. m3/m6 have no floor under (85,940)
→ fall → death.

`reload()` (KO→YES) runs the full `spawnEntities → statsReset →
resetPlayerToSpawn → postSpawn` — which is why every reload landed the
correct spawn.

## Fix

menuJc9's `jG==164` arm now clears checkpointSnap (a new mission always
spawns `d(false)` fresh, never from image — k.java:4741+) and mirrors
reload()'s order: `spawnEntities → statsReset → resetPlayerToSpawn →
postSpawn`. Player lands on the pack record.

## Provenance

`G(164)=d(false)` fresh spawn (k.java:4741+) — the original respawns the
player `new i(sArr)` at the record on mission entry.
