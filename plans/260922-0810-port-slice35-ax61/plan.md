---
title: "Port slice 35 — ax61 aR() multi-tool (aura/projectile/grab-QTE)"
phase: port
status: done
---

# Slice 35 — `i.aR()` (ax61 Cesare multi-tool)

Ports `aR()` (i.java:11276-11529, proven) — the ax61 "aura" entity that
serves as: clip71 child follower, param-curve projectile shell, contact-harm
shell, aura grapple clip, grab-QTE overlay, and boss throw finisher. All
`S` dispatches are animation indices into clip71.

## Arms ported (`NpcFsm.tickAx61` + `ax61HarmArm`)

- **S8 projectile** — quadratic Bezier `j.a`/`j.b` (j.java:505/515):
  `(P0·ti² + 2·Pc·ti·t + P1·t²) >> 16`, `t = Z[6]·65536/Z[7]`, start Z[0..1],
  ctrl Z[4..5], screen-space dest `Z[8]-k.O / Z[9]-k.P` + k.O/kP restore.
  `Z[6]>=Z[7]` → land + `i(10)` + `k.A(12)`.
- **S9** — `r()` → `k.c(this)` removal.
- **S10** — `X[0]!=X[2] && a(X,aS.W)` → `aS.a(4,0,0,this)`; `r()` → remove.
- **S4/S5** — `T==9` → `k.A(31)` whiff sfx, then shared L25 harm arm.
- **S2/S17** — L25 harm arm (`ax61HarmArm`): overlap gate, `aS.S∈{9,375,376,377}`
  skip, `aS.ak<aU.ak` → `av=false` + no harm (L39); else `av=true` +
  `aS.a(4,0,0,this)`; S2 → `i(375)`+vel0+`al=aU.al` (grab snap);
  S17 → L39 `av=false` only. `r()` → `P|=128|32`.
- **S0/S6/S15** — L50 catch arm: `S15 && a(Y,aS.Y) && aS.S!=375` → player vel0
  + `i(375)` + `al=e.al` + `g.d(g.u[k.au])`; all → `r()` → `P|=128|32`.
- **S1** — aura follower `ak/al = aU`.
- **S11** — `r()` → `i(12)` + `k.o()` input lock.
- **S12** — grab-QTE: `k.k()` (mounted) → `a(11,ak,al)` `ae` marker; else
  `c/d` hand spawn + `V()` proximity → `bl+=9`. `f(4112,8256)` mash
  (i.java:6852 — `bl<=0→-1`, `bm` latch, other-mask edge `+8`, absence `-1`,
  `>=10` win). Win → `U()` (unless mounted) + `P|=128|32` + `k.c` + `bl=0` +
  `G()` + `aS.al-=20` + `aS.a(0)` fling + `k.p()` + `aU.i(28)` + `P&=-65`.
  `aS.S∈{370,371}` + `r()` → lose: `k.bJ>0?:=6` + `G()` + `aS.i(374)` +
  `i(13)` + `k.A(12)`. Else → abort: `k.c` + boss reset + unlock.
- **S13** — `r()` → `aS.a(4,0,0,this)` + `k.c` + boss reset (`aU.i(28)`,
  `P&=-65`, `k.p()`).
- **S19** — aura off-state (L105): `aU==null || k.C.ab() || aU.S==18` →
  `P|=128`; else `P&=-129` + glue `ak/al=aU` + `aU.S∈{36..41}` → `i(18)`.
- **S18** — aura on-state (L131): same despawn checks + glue; `aU.S∉{36..41}`
  → `i(19)`. S41 exits through the decompiler-dropped `L171` (method end)
  → stays — flagged in code comment.
- Default → return (L154).

## Support ported alongside

- `i.f(int,int)` mash QTE → `Entity.mashQte` (i.java:6852).
- `i.ab()` claim check → `Entity.claimActive` (i.java:20564): `ca>=0 &&
  !cd[0] && cK>=0`. `Entity.ca/cd` fields added.
- `i.bm` mash latch field.
- `k.bJ` (grab latch) → `Level0World.kBj`; `g.u` (i.java:6400 `{5,10,15}`)
  → `Level0World.GU`.
- `applyHit` op4 arm: `attacker.ax==61 && playerDamageable` →
  `attacker.counteredBy(this)` — self-counter flips ax61 to S9 and suppresses
  the S2 snap (faithful: `this.S` re-read after applyHit); excluded from the
  second counter pass along with ax17/50.
- Dispatch wired: `n.ax == 61 → npcFsm.tickAx61` (Level0World).

## Correction during this slice

- `g.d` gate 3 was ported as `nearLeftWall` — the original is `k.aS.c()`
  (g.java:421) = `S∈{112..115}` mid-combo check. Fixed; caught by the S15
  catch test (drain was silently skipped).
- Bezier test initially asserted the `t=4096` value after one tick — the arm
  evaluates `t = Z[6]·65536/Z[7]` BEFORE incrementing, so tick #1 uses `t=0`
  (start point). Test now asserts both.

## Honest gaps

- **X-harm untestable via real clips**: every converted clip gives
  `X[0]==X[2]` (zero-width point) at all ax61 arm states — the real
  hitboxes live in unconverted clip71. `ax61HarmArm`/`S10` arms are exercised
  through a manually-widened X after `t()` (the guard, branch, and `r()`
  tail are all covered — only the overlap input is synthetic). Tracked: a
  clip71/clip74 conversion pass would unblock.
- **S15 catch via Y-overlap** — clip0 S15 `Y=[272,115,312,152]` is real →
  covered end-to-end through `tickAx61`.
- S12 mounted variant (`k.k()=true` → `a(11,ak,al)` marker) has no mount
  harness yet — the pad-QTE path is covered, the marker arm is the same
  `spawnAeMarker` used elsewhere.

## Verified

- `:core:test` — 175 tests, all green (16 new ax61 tests).
- `verify-static-reconstruction.py` → `ok:true`; `python3 -m unittest` 57/57.
- `:android:assembleDebug` → install + cold boot on emulator `npcs=454`,
  no quarantine / crash.
