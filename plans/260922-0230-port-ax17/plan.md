---
title: "Slice 62 — ax17 aA() civilian FSM"
phase: port-slices
status: done
---

# Slice 62 — ax17 `aA()` civilian FSM

Port of the civilian entity (ax17, `i.aA()`, i.java:8708-8847) — 16 records
across packs 8/9/11, shares clip-7 with the soldier family (`bi[17]=7`,
k.java:8442).

## Scope

- `initAx17` — init arm (i.java:3004 L113 + L392 tail, proven):
  `Z=int[22]; Z[1]=0; Z[2]=-1; aG=r8[4]; az=r8[13]; Z[21]=r8[14];
  aB=bv[k.au]; aF=r8[2]; aD=r8[7]; m=r8[8]; o=r8[9]` → `i(r8[5])` + `t()`.
  `bv` = {100,140,200} civilian HP table (i.java:22317).
- `tickAx17` — the `aA()` tick, verbatim arm order:
  - L7 dead-check: `aB<=0 && S∉{69,129} → i(69)`.
  - S57 idle (`L12`): `ag=ah=0`; notice gate `l()` ax17 arm
    (i.java:2385 L70) = `!bn && b(W, k.ac)` — fully inside the camera rect
    (k.java:2700-2703; `bn` = `bA[79]` checkpoint alert flag, k.java:6696,
    ported as `LevelCellSource.iBn`); player S9/S50 suppress; `k.A(16)` sfx;
    `av` faces the player; quadrant pick — X-separated → 66/67 by above/
    below, X-overlap → 62/63/61 (i.java:8754-8775, proven).
  - S60-68 panic flail (`L45`/`L68`): `T==3 → aS.a(4,0,0,this)` strike;
    `r() → i(57)`.
  - S69 collapse (`L61`): `P|=512`, `ab=null`, `ag=ah=0`, ax51 ride-crate
    `ag=s.ag` (L67), `aA=2`, `r() → [bK]` blood-fx `a(8,59,2,av,ak,al,az-1)`
    (gated — `k.bK` HAS-BLOOD=false, GloftASBR.java:36), `P&=-17|P|=32|64`,
    releases `aN`/`g.b` locks.
  - S129 dead-on-spot: `r() → aB=0` + same flag drop.
  - S170 knockdown (`L53`): `al+=10; a(true)` wall probe → `aZ → i(129)`.
  - L87 tail: `j()` ax17 intake (i.java:1803, proven) — `P()` (:7699)
    gates corpses; live = `g.b()` anim + `aS.X` non-degenerate + strict
    overlap → `Q()` face + `aB -= J=100` for {183,184,216,217} else
    `H=50`; no react anim, no weaken/lock (ax11/73-only); always false.
    Then `S!=129 → a()` (i.java:914, the solid-body player push — ported
    as `Entity.pushContact`), then `au()` (:7738) → `corpseDrop` (already
    covers ax17).
- Dispatch wiring: `type == 17 → initAx17`, `ax == 17 → tickAx17` before
  the `npcFsm.tick` fallback (Level0World.kt).
- Damage-op immunity (proven): the `a(int,int,int,i)` dispatcher's
  `case 17 → L141 return` (i.java:4584) — props/destructibles cannot hurt
  civilians; only the player's sword via `j()` reaches them.

## Test coverage (Slice17Test, 14 tests)

init field map; on-screen notice (sfx16 + pick + facing); off-screen idle;
`bn` suppression; player S9/S50 suppression; all five quadrant picks;
flail strike at T==3 (op4 → sfx18); `r()` returns; damage intake
H=50/J=100; dead-check → S69; S69 flag/lock release; S129; S170 knockdown.

## Gates

- verifier `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `:core:test`: all pass (Slice17Test 14/14)
- `:android:assembleDebug`: builds clean
