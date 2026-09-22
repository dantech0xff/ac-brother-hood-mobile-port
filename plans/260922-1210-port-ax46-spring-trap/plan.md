---
title: "Port slice 50 — ax46 aZ() spring/trap prop"
phase: port-slice-50
status: done
source: reconstructed-project/src/simple/i.java:13568-13726 (aZ), :2700→L206 init (case 46), :4446 player intake ops 11/24, k.java:8442-8445 bi/bl tables
confidence: proven (bytecode transcription); decode verified against level-0 records
---

# Slice 50 — ax46 `aZ()` spring / trap prop

Ports the ax46 FSM — the spring pad, touch trap, and directional pusher
family — verbatim from `aZ()` (i.java:13568-13726).

## What it does in the original

Two record shapes share one FSM:

- **Spring pads** (S0/1 clip29): player-W overlap + falling (`ah>=0`) +
  body quarter-point above the pad top → `aS.a(11,0,0,this)` (op11
  intake: pin `al=W[1], ak=src.ak`, launch `ah=Z[1]`, `ag=Z[0]`, anim
  22/24, facing by Z[0] sign / `!src.av` for S11 pads) then `i(1)`
  sprung → `i(0)` on anim end.
- **Armed traps** (S327+ clip0 via `bl`): X-box (attack rect) overlap →
  `aS.a(24,330)` op24 pin (anim 330, all velocities zeroed, `ak/al` =
  src's W corner) → `i(328)` → on anim end release: `aS.i(165)` +
  `ag=±2048, ah=-5120` throw-off + `i(329)` → `i(327)` re-arm.
- **Unarmed touches** (S3/4, `Z[4]!=S`): same X overlap → immediate
  `aS.i(165)` + throw-off, `i(7)` → `i(2)` dead.
- **Pushers** (S10): W overlap → `ag=Z[0], ah=Z[1], av=!e.av, i(242)`
  directional air-jet.
- Cycle states 5/6/328, settle 7/8, dead 2, re-arm 329.

## Key decode

- The anim clip **rebinds per-record**: L15 `aa = k.r(k.bl[r8[10]])`
  with `k.bl = {29, 0}` — `bl[0]=29` (clip29: the 13-anim pad/launcher
  clip), `bl[1]=0` (the 393-anim mega clip hosting S327/328/329/330/
  165/242/110). Port binds `e.clip = w.clips[bl[rf(10)]]`.
- Palette pins each tick-head: `aa.l(Z[5])` → `e.palette = e.Z[5]`.
- Init `case 46` → L206: `Z=int[8]`; `Z[4]=r8[5]` (armed marker),
  `Z[0]=r8[8]<<8`, `Z[1]=r8[9]<<8` (throw vel), `Z[5]=r8[11]` (palette),
  `Z[6]=k.bl[r8[10]]`, `Z[7]=r8[12]`, `Z[3]=30`; shared `i(r8[5])+t()`.
- Player intake ops added to `applyHit`: **op11** (spring bounce:
  `al=W[1], ak=src.ak, ah/ag=Z[1]/Z[0]`, anim 22/24, face rule) and
  **op24** (pin at src W[0]/W[1], anim r11, `aj=ah=ag=0`).
- Level-0 records uid22 (2389,386) + uid143 (1067,650): S=327 armed,
  `r8[10]=1`→clip0, `r8[11]=2` palette.

## Port

- `initAx46` / `tickAx46` (+`ax46Spring`/`ax46Touch`/`ax46Cycle`/
  `ax46Pusher`) appended to `NpcFsm.kt`; `ENTITY_CLIP[46]=29`; init/tick
  dispatch arms in `Level0World`.
- `Entity.applyHit` gains the 11 and 24 `when` arms.
- clip29 converted (`pack-3/entry-029-marker-003`: 13 anims, 49 frames,
  31 modules, 33 rects); renderer module map + `clips[29]` load wired;
  fixture `29 to Clip.load(...)`.

## Verification

- `Slice50Test` — 11 tests: clip rebind by `bl`, Z-load, palette pin,
  spring bounce (all op11 effects), rising-player rejection, S1 settle,
  armed S327→op24 pin→S328, S328 release throw→329→327 re-arm, unarmed
  touch throw, S7→2 dead, S10 pusher jets.
- Gates: verifier `ok:true`; 57 unittests; 311 :core tests green;
  `assembleDebug`; emulator boots level 0 `npcs=482` (+2 ax46 records).
