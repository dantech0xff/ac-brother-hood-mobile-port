---
title: "Slice 286 — m3 capstone legG: wall top -> lift stair -> capture -> zipline -> east mass"
phase: capstone-bot-legs
status: done
---

# Goal

Prove mission-3 (`world(aj = 3)`, pack-9 bh4 platformer) leg G end-to-end with
real input only (`w.pad.e(mask)` + `w.tick(emptyList())`, no state pinning
inside the loop): from the x12000 wall top (leg F's end) ride the three-lift
stair into the ax22 capture, get launched onto the ax40 zipline, and land on
the east mass — the designed pit crossing.

# Route (decoded from level3.aclv)

- West face x12000 wall top at y680 (leg F terminus).
- Lift stair (ax66 movers): aw597@(12117,644) → aw572@(12214,625) → aw598@(12315,604).
- ax22 capture zone aw835@(12383,544) Z[4]=1 — pinned grab, UP vaults out.
- ax40 zipline y440: aw604 west anchor → aw602 east anchor @(13069,440).
- East mass x13080-13279, top y540-660 → assert `ak>=13080 && al<=700 && aZ`.

# Blocker found and fixed (route, not code)

First spawn (12100,839) sat on the y840 corridor floor inside the ax44 crusher
gauntlet (doors aw881-892 at y843-844). `tickDoor` → `crush()` box-overlap →
`player.setAnim(50)` → `setAnim` zeroes `x1` on ax==0 S==50 (Entity.kt:929,
proven pattern) → instant `stateL(12)` at t0. The corridor is a kill-route,
not the path — the leg start moved to the wall top (12000,679) where leg F
actually ends.

# Result

`mission3CapstoneLegG` passes (~139 ticks): vault off the wall → S235 grab
lift aw597 → transfer aw598 → S65 capture aw835 → S19 launch arc → S164 wire
slide (~5px/t, ac=603) → S174 dismount arc → grounded (13098,539).

# Fidelity notes

- No production-code changes in this slice — the instant-KO was the leg
  spawning into a designed kill corridor, not a port bug.
- Test-only scratch (probe prints) removed before commit.
