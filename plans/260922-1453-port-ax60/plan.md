---
title: Slice 60 — ax60 bj() lift/piston platform
phase: port
status: done
---

# Slice 60 — ax60

## What the original does

- Dispatch `case 60 → bj()` (i.java:14752-15260, proven) — lift/piston
  platform: vertical lifts (S 9/10/16/17), horizontal movers (S 11/13/14/15),
  auto-bounce (`Z[4]=2`), lever-driven (`Z[4]=3` via ax58 `bf()`), and
  S13↔S11 counterweight pairs (`Z[4]=1`).
- **Correction**: first pass wired clip71 (`bi[61]`); the true `bi[60]`
  index is 21 — fixed before PR.
- Init `L259` (i.java:3394-3440, proven): `Z[0]=r8[7]` link uid,
  `Z[1]=r8[4]` speed, `Z[2]=r8[8]` delay, `r8[9]==1`→`Z[4]=2`,
  `P|=4096` for ride S, `Z[3]=` anchor (al for S9/16 else ak), probes
  scanning to the first solid cell → `Z[5]` bound (up for S9, horizontal
  for S14/15/16 with `Z[1]<0`), `az=0,P|=16` for S∈{6,11,13},
  `aC=Z[2]` for S∈11..15, then `i(r8[5])+t()`.
- `bj()` arms: S∈{9,16} idle arm (resolve `k.q(Z[0])` → ax58 binds `s`
  +`Z[4]=3`; miss→`i(10|17)`,`Z[0]=-1`,`aC=Z[2]`; `Z[4]==2` auto-start)
  → L26 ride arm; S∈{10,17} → L26; S∈{11,14} → `c(false)`; S∈{13,15} →
  `c(true)` + L171 pair handoff; else → L177 zone scan (`k.bd[]` ax10/S39
  overlap: bounce `Z[4]==2`, latch `k` `Z[4]==3`, settle `i(9|16)`).
- L26 (i.java:14845-14980): ride anims when the player's box straddles
  `W[3]` (`W[1]≤W[3]≤W[3]` + `ak∈[W0,W2]` + `r72` + `aZ`), S→78/50; L58
  push-out for `S==277` (`±5` edge clamps, `ag=0`, airborne → `a(0)`);
  drive tail by `Z[4]` mode: `2`=probe/bound auto-bounce, `3`=lever arm
  (`s.bf()`→move+latch `k` at bounds), `0`=L149 free-run (`aC` countdown
  then `ah=Z[1]<<8` until probe lands → `i(9|16)`).
- `c(boolean)` (i.java:15060-15260): lazy `ac` resolve + pair/lever
  latching; player mount (`g.b(S)||S∈{209,34}` + `al≤W[3]` → `g.a=this`
  + `al=W[1]+1`), side walk-into (`i(209)` + clamp), `g.c(S)` clip push
  `±5` + `a(0)` fling, drift-off fling, ride-carry `ak+=ag>>8` (−2× while
  `g.b()` attacking), `p.W∩X`→`i(50)`, L92 X-box pair velocity swap,
  lever arm (`ac.bf()`→`ag`+`bz`; idle+latched→unlatch-reverse;
  `!bz`→hold), `aC` cooldown, bound clamp `ak=Z[3]` or `ag` bounce, and
  the end-probe (`e()`≥12 → `Z[4]==3` latch `k` else reverse `ag`).
- **`t()` ax60 arm** (i.java:543-581, proven — the L103/L91 tail):
  travel edge stretched to the `Z[5]` probe — S9/10 `W[1]=Z[5]`,
  S16/17 `W[3]=Z[5]`, S13/15 `W[0]=Z[5]`, S11/14 `W[2]=Z[5]`. With
  clip-71's mostly-empty W rects this yields a zero-width/-height strip
  covering the whole travel corridor.
- Clip: `bi[60]=21` (k.java:8442, proven — index 60 of the `bi` array;
  71 is `bi[61]`, Cesare's clip) → `clip21.acpk` converted (18 anims,
  11 modules, one frame each). Rects: anim9/10/16/17 `W=(1,0,36,37)`
  platform box, anim11 `W=(0,-36,16,36)` + `X=(-11,-36,16,36)` left,
  anim13 `W=(0,-36,22,36)` + `X=(17,-36,16,36)` right — so the L92
  pair-swap (`a(n.X,e.X)`) IS reachable for the S13/S11 pairs.

## Port

- `NpcFsm.tickAx60` + `ax60Ride`/`ax60PushOut`/`ax60Mount`/`ax60Zones` +
  `initAx60` — verbatim goto-arm transcription with `file:line` cites;
  `e.s`/`e.ac` links, `e.runnerBz`=`i.bz`, `e.k` latch.
- `Entity.refreshBoxes` gained the ax60 `t()` tail (`S→W edge = Z[5]`).
- `ENTITY_CLIP[60]=21`, init `type==60→initAx60`, tick `ax==60→tickAx60`.
- `convert_slice1.py`: added `clip21` → `rewrite/generated/clips/clip21/`
  (`clip71` also generated, parked for the ax61 wiring slice).
- Tests: 11 cases — init flags/probes/`i(r8[5])`, link resolution (bind/
  miss/ax58), ride anim 50, auto-bounce, lever bf/unlatch, mount+carry
  inside S13's real box, pair-swap velocity handoff, zone latch.

## Records found

pack-13 ×6 (S9 auto lifts `r8[9]=1`, S13+S11 counterweight pairs via
Z[0] links 58/61) + pack-9 ×1 (S15 horizontal rail, `Z[1]=-10`).

## Gates

verifier `ok:true` · `python3 -m unittest` 57 · `:core:test` green ·
`:android:assembleDebug` green.
