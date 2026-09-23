---
title: "Slice 172 — level-0 mission-complete verified: aw252 zone -> script 116 -> screenL(15)"
phase: port
status: done
---

# Slice 172 — mission-complete path verified on real level-0 data

## Finding

Level-0's mission-complete path was already fully wired — it just needed
reading end-to-end. Chain (all proven):

1. **Trigger**: ax5 S8 watcher `aw252` at the level end —
   `W = [12131, 221, 12561, 811]` (a 430×590 box covering the exit),
   `aG = 116` (its bound claim script).
2. **Bind**: player∩W → `missionResolve` → `eventBind`/`bindContext` →
   the zone claims script uid 116 and steps it (`k.s(116)` in the pack-6
   script table — 13 scripts total, parsed cleanly end-to-end: 4371 B).
3. **Fire**: script 116's only block runs `op37 args=[1,0]` →
   `r013=1, r12=1` → arg-sub case 1: **`screenL(15)` + `kStat(0)`**
   (Entity.kt:2036 — mission-complete + stats record).
4. Observed headlessly: `jC = 15` on the overlap tick; the zone removes
   itself (`k.c(this)` / claim release).

## The ax42 fuse is a different mechanism

The fuse (slice 171, now spawning) is armed by a claim `i(0)` and watches
aw531's `P&32` — but **no level-0 script block targets aw59 or aw531**
(dumped all blocks of all 13 scripts). It's an escape/defend timer used
elsewhere in the mission set (records are shared); level-0 completes via
the zone→script→`l(15)` path above. Both chains are now proven in tests.

## Route context for the demo

S8 watchers walk the level eastward binding scripts on player overlap —
x49 (tutorial), x3650, x4272, x5616, x6009, x7148, x11448, then x12131
(mission-complete). Their bound scripts (uid 327, 104, 313, 83/85-style,
96, 250, 116) drive objective text, NPC behavior, waves, and the finale —
the ported claim interpreter runs them as the player traverses.

## Tests (Slice172Test)

- aw252 spawns with `S=8, aG=116, W=[12131,221,12561,811]`.
- Player inside the box → `jC == 15` on the overlap tick; zone consumed.
- Player outside → no fire; zone persists.

## Gates

verifier `ok:true` · unittests 57 · `:core:test` (incl. Slice171 win-fuse
+ Slice172 mission-complete) — all green.
