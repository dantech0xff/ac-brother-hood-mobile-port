---
title: Slice 323 — m2 perched-sentry wake chain verdict
phase: fidelity-verdict
status: complete
---

# Slice 323 — m2 posted-perch trio wake chain

## Question (from device validation)

The m2 play-through reported guards "hanging on the perch, cannot be hit"
(aw311/312/313 @ x~3800–4040, y~800). Are they meant to wake and fight, and
does the port drive that path?

## Verdict

Faithful — no port bug. The trio are dormant parked sentries that the
mission choreographs via claim scripts; the wake trigger sits on the
ROOFTOP approach, not on the ground path the repro took.

## Evidence chain

1. **Dormant by design** (slice 322 verdict): trio spawn `P|32` + `S=152`
   posted-perch. The non-bh3 entity loop (Level0World.kt:4966-4973,
   k.java L215 arm) skips `au<2 && P&32 && !P&16` and `au>=2 && !P&16` →
   they never tick → invulnerable + unresponsive while parked.

2. **Wake = scripted, not proximity**: `scripts.bin` byte-decode shows
   scripts uid 309/342/346/575 carry type-2 blocks targeting aw311/312/313:
   - s309 (setup, step 0): `op23(01000000)` P|=1, `op22(9800)` S=152,
     `op21` lerps to (3333/3414/3393, ~800);
   - s342 (descent, steps 140–188): `op24` P&~1, `op22(9a00)` S=154;
   - s346 (staging, steps 0–368): `op22` S=187/188/164/194.
   s309's type-0 block runs world ops + `op108` sub-script branches.

3. **Watchers own the scripts**: aclv record scan found two ax5-S8
   mission-logic entities — aw308 `W=[2667,493,2886,541]` (aG=309) and
   aw345 @(3695,256) (aG=346) — on the rooftop approach above the perch.
   `eventBind` (NpcFsm.kt:2889) binds the context claim on player overlap;
   `bindContext` arms `P|16` on the claimer (Entity.kt:2782-2793).

4. **Claim ops puppet parked entities**: `runClaimScript` type-2 resolves
   `r14 = findByAw(uid)`; op21 lerps `r14.ak/al` INSIDE THE CLAIMER's tick
   (Entity.kt:2217-2261), so a `P|32` entity moves without ticking.

5. **Live proof in the port**: driving the player through aw308's W bound
   `kC=aw308 ca=12` (script 309), ran steps, lerped all three guards to the
   exact op21 targets, and set `P|=1` — while the ground-path walk-through
   (x 3000→4400, y=800) bound nothing and left the trio parked. The repro
   never entered the rooftop watcher zones → "unfightable" = intended
   dormant state on that route.

## Change

Test-only: `Slice323Test.kt` — 3 verdict tests (watcher binds + lerps;
ground path stays dormant; descent-script watchers exist). No port changes.
