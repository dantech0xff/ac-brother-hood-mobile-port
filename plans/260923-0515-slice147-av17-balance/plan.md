---
slice: 147
date: 2026-09-23
confidence: proven
---

# Slice 147 — ax10 S17 balance zone (L1b44)

Port the `aV()` dispatch `case 17: goto L1b44` (i.java:9182) arm
(i.java:12794–12951) — the balance/center-pin zone. Ported verbatim
into `tickTrigger` at NpcFsm.kt (~:770).

## Semantics (proven)

`aA` phase machine:

- **Freeze** (L1b44): `k.C != null && k.C.ab() && aA == 1` → return —
  while a bound claim-script is active the centered player is pinned
  (no exit or action checks). `i.ab()` → `claimActive()`
  (`ca>=0 && !cd[0] && scriptStep>=0`, Entity.kt:1497).
- **`aA==0` entry**: player `W ∩ zone W` → `aA=1`, `i(297)` balance
  anim, `ak/al` pinned to the zone's center `(W0+W2)>>1,(W1+W3)>>1`,
  all four velocities zeroed (`ah,ag,aj,ai` — the register shuffle in
  the dump is just four `=0` writes).
- **`aA==1` exit**: player `W` leaves the rect → `aA=2` (checked only
  when `aA` entered nonzero — entry tick skips it via `goto L1be7`).
- **`aA==1` action** (L1be7): while player `S ∉ {298,293}` —
  `v(2)` → `i(19)`, `ag=-3328`, `ah=-3840`, `av=1` (leap left);
  `v(8)` → `i(19)`, `ag=+3328`, `ah=-3840`, `av=0` (leap right);
  `v(33024)` → `a(2560)` airborne fling (`flingAirborne`, g.java:126 —
  `a(43,32)`, `al+=10`, `ah=2560`, `aj=1536`, `ga=ac=null`). All three
  set `aA=2` and return.
- **`aA==2` reset** (L1c7b): `player.aZ || player.ga != null` (landed
  or holding a prop) → `aA=0` — re-arms the zone.

Init side: S17 not in the `sArr[5]` sub-switch → `default → L111`
(`aE=f4, aF=f11, oId=f12, pv=f13, aG=f14, ay=f15`) — already covered
by the existing default init arm. 5 S17 records across the packs.

## Tests

`Slice147Test` (5 tests): entry center-pin + S297 + vel zeroing;
v(2)/v(8) leap velocities + facing + aA=2; v(33024) fling `ah=2560`
and the S298 action block; leave→aA=2 then `aZ` reset; claim-busy
freeze + unfreeze.

Gates: verifier ok, 57 unittests, `:core:test` (608),
`:android:assembleDebug`, `:gdx:build` — all green.
