---
title: "Port slice 41 — ax27 bL() fuse/message prop"
phase: 41
status: complete
---

# Slice 41 — ax27 `bL()` (fuse barrel / message trigger)

## What

Port the ax27 entity (`i.bL()`, i.java:20843-21068) verbatim: the timed
fuse-barrel + HUD-message-trigger family. Six records in level 0, all of
them tutorial message triggers (4×S16, 2×S18; `Z[3]` ∈ {11,12,-1}).

## Source (all proven, i.java)

- **Dispatch**: `I()` → `bL()` for ax 27 (dispatch table).
- **Record init** — L197 arm (i.java:3213): `az=1; aA=r8[7]; aB=30;
  Z={r8[8], r8[9]*1000, 0, r8[10]}; r8[5]==4 → P|4096`; `r8[5]==15` falls
  into the **L203 shared ax29 arm** (az=100, aB=800, aD=2, m=2, aC=30,
  aF=30, n=60, Z={r8[4],r8[7..10]}, `r8[5]!=30 → k.aU=this`).
- **`bL()` S-arms**: S0 idle/interact (engage iff p.S∉{89,90,267},
  `p.W∩e.W`, `!ae()` → tap-16388 binds `k.aS.af=this`+`i(267)`+`k.v()`;
  `aA==1` pins an `a(7,…)` marker at `(ak, al-85)`); S1/S21 mount-in →
  L58; S2/S22 pin `p.ak`; S3 release `af`+`az=100`; S4/S23 `fuseArm`;
  S5→`i(15)`; S6 fuse tick `Z[2]+=j.f ≥ Z[1] → i(8)+k.A(23)` clearing
  `P&~4096` and setting `P|64`; S7 pin; S8/S20 explode (`k.aD` release →
  `i(4)`); S12 burnout (`i(13)`, clears P4096+P16); S15 damage
  (`p.X∩W → aB-=10 → i(5)|i(12)`); S16 message
  (`k.aO≥0 → k.aP = k.d(1+k.aj, Z[3])`); S17→`i(18)`; S19→`i(16)` +
  `k.aO=4000`; else + always-tail → L121.
- **L58 `fuseArm`**: `P|4096`; ax58 link `k.q(Z[0])` with
  `S∈{1,4,6,8,10,12}` → `i(6)`+`P|16`+claim `k.aD`.
- **L121 `fuseTail`**: push every ax11 within |Δak|≤50 out of `e.W` via
  `i.a(i,int,int[])` (i.java:15324).
- **Helpers**: `ae()` = `g.a!=null || ∃` on-screen dangerous
  ax∈{17,11,23,50,73} via `h(i)` (i.java:20791: `W∩k.ac` then
  {17,50}→true, {11,73}→`!P()&&aA>=1`).
- **`k.d(1+k.aj, idx)`** (k.java:486) = per-level string table → pack-14
  entry-001 for level 0; emitted as `level0/strings-1.{json,txt}` and
  loaded into `Level0World.levelStrings` by the gdx layer + tests.

## Decompiler artifacts handled

- **L53 head cut**: release arm fires when `af === e` (garbled label made
  the condition read inverted) — transcribed as release-on-match.
- **`i(18.Z[3])`** (S17's tail in some arms): invalid operator-loss →
  ported as `18 + Z[3]` and marked `inferred` where it appears (S17's own
  arm is a plain `i(18)`).
- **L31 literal fallthrough** into L12 would re-enter the engage gate
  forever — transcribed as the marker pin loop it is.
- **L203 arm shares ax27 record init for r8[5]==15** — Z re-allocation to
  5 slots collapses into the fixed 22-slot array (superset; all reads
  overwritten).

## Port shape

- `NpcFsm.kt`: `initAx27`, `tickAx27` (full `when(e.S)`), `fuseArm`,
  `fuseTail`, `enemyDanger`, `enemiesAlert`, `pushApart` (the shared
  `i.a(i,int,int[])` push-apart routine — also callable by other ax
  families later).
- `Entity.kt` interface: `kAD`/`kAO`/`kAP` (k.aD fuse-claim, k.aO
  countdown, k.aP message), `levelString(level, idx)`; world decrements
  `kAO` by `j.f` per tick (k.java:5527).
- `Level0World.kt`: `ENTITY_CLIP[27]=48` (bi[27], k.java:8442),
  `type==27 → initAx27` in spawnEntities, `ax==27 → tickAx27` dispatch,
  `levelStrings` ctor param.
- `convert_slice1.py`: `clip48` (pack-3 entry-048) + `strings-1.json`/`.txt`
  emit; gdx `Level0Game`/`Level0Renderer` + test map wired.

## Tests (7 new, 237 total green)

L197 init fields (incl. `Z1=r8[9]*1000`), S16 string load/`kAO<0` clear,
S6 fuse tick → S8+sfx23, S15 aB-drain → S5/S12, S0 engage on 16388 tap
(player `af` + `i(267)` + latch clear), S0 armed without tap.

## Gates

`verify-static-reconstruction.py` → `ok:true`; `python3 -m unittest` 57/57;
`./gradlew :core:test` 237/237; `:android:assembleDebug` green.

## Honest gaps

- **Message HUD draw**: `k.aP` text is set but the k.java:5503 HUD
  rendering arm is not ported (renderer has no HUD text layer yet).
- **Fuse-bar draw** (k.java:4073): `k.aD` claimed correctly; the bar UI
  unported for the same reason.
- ax58 link arm (`fuseArm`) is dead in level 0 — no ax58 records —
  transcribed for other levels, untested end-to-end.
- `aA==1` marker pinning untested (level-0 records all `aA=0`).
