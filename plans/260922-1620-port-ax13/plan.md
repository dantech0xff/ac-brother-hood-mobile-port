---
title: "Slice 45 — ax13 aW() rope/vine swing"
phase: port
status: done
---

# Slice 45 — ax13 `aW()` rope/vine swing (i.java:13182-13367)

## What this is

The swingable rope/vine entity: a pendulum integrator the player can
grab mid-flight, pump with the D-pad, climb up/down segment by segment,
and release (or auto-fling at apex for aG==1 "launch ropes"). Fully
transcribed from `aW()` + the input handler `g.k()` (g.java:4821) +
release `g.j()` (g.java:4775) + `i.l(i)` arc placement (i.java:1226) +
`i.y()` wall check (i.java:1211) + `i.bf()` (i.java:14608) + `i.t()`
rope-arm rect override (i.java:369-410). All **proven**.

## Ported semantics

- **Pendulum** (L5/L6): integrates while `bO!=0 || bP!=0`:
  `bP += bO<<1; bO -= j.b(j.n - (bP>>8))<<1`. Swing-side latch `j`
  damps `bO -= bO>>3` on every zero crossing (L19-23).
- **Apex auto-release** (L9): aG==1 ropes fling the bound player when
  the angular velocity flips sign (`r0*bO<0`) — `av = (bP<0)` +
  `aS.j()` with the g.java:4780 leap arc `{i(23), ag=(bP*Z[1])>>5,
  ah=(Z[1]==0?0:4000*2048/Z[1])<<1}`.
- **Bound side** (L27-41): `aS.k()` input each tick + `l(bM)` parks the
  rider on the arc tip; aG==2 (re)binds `aS.ab` marker via
  `a(14,9,11,302)` and pins it at `W`-midY/`ak`/`av=false`.
- **Grab scan** (L43-101): while unbound (`aS.bM!=this`, `aS.aA&64==0`,
  `aA==0`, `g.b(aS.S)`, player below the anchor) a swept-arc W-box
  (`bP`-rotated segment `r04` chosen on the `r8&65534` mask; -1 = miss)
  catches the player → `aS.h(1)`, `k.v()`, `bN=r04`, `aA=1`, two-way
  `bM`, `aS.az=101`, ±{256,512} kick by facing, aG==1 `<<2` boost,
  aG==4 freeze, `l(aS)`, `aS.aA|=64`, `i(326)`.
- **Input** `g.k()`: `u(8256/4112)` pumps only while the swing opposes
  (±512 into the ±1280 clamp, `(20480±bP)/80` velocity feed, anims
  {84,85}); `x()` double-tap flips `av`; `u(2)/u(8)` sets `av` and
  releases; `u(65568) → i(86)`; `u(16388)` climbs (`bN--`/`i(82)`,
  `bN-2<0 → i(326)` at top) or zeroes a live swing; `u(33024)` descends
  (`bN++`/`i(83)`, `bN+2 > Z[1]-2 → i(43)` drop — **the original keeps
  `aS.bM` set on this drop** (L116), ported verbatim); idle + anim
  finished → `i(326)`.
- **Spawner** (L103): aG==4 with `Z[6]` ax58-door link — grows `bN` to
  `Z[1]` while `Z[6]==-1` or the linked door's `bf()` is open.
- **Clamp/zero-snap** (L114-126): `bP∈[-20480,20480]` (zero `bO` at cap);
  `(bP&-128)==0 && (bO&-128)==0 → bO=bP=0`.
- `initAx13` = the shared `L111` map (aE/aF/oId/pv/aG/ay) + `i(r8[5])`
  + `t()`.

## New helpers

- `Entity.ropeArcPlace` = `i.l(i)`; `releaseRope` = `g.j()`;
  `ropeInput` = `g.k()`; `climbCheck` = `i.y()`; `isBf` = `i.bf()`.
- Fields `bN/bO/bP/bM` (+ `ropeGrabSeg` debug mirror of the L89 write).
- `LevelCellSource.padTap` = `k.x(mask)` double-tap edge (`pad.x`) —
  bound on `Level0World` for the `x(4112/8256)` facing flips.
- `Pad.held`/`tap` already expose the `u/v/x` sets — no new input path.

## Tests (6, all green — 281 total :core tests)

Pendulum integrate-only-while-displaced; sign-flip damping; grab latch
(326 hang + aA|=64 + az=101); aG4 spawner vs `bf()` door gate; aG1 apex
auto-fling (`i(23)` leap); `g.k()` climb `bN--`/drop keeps `bM`
(verbatim quirk asserted).

## Gate

`verify-static-reconstruction ok:true` · `python3 -m unittest` 57 pass ·
`:core:test` 281 green · `:android:assembleDebug` OK.

## Follow-ups

- Rope segment renderer (`i.a(Graphics,…)` i.java:13384+) — gdx layer,
  later slice with the other deferred draws.
- `ad()` dialogue bubble, `ai()` static — remaining tick arms.
