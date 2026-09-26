---
title: "Slice 267 — canyon ascent is shrine-refilled: ax24 drop-line sweep → S19→S20 → L181 meter refill"
phase: port
status: done
---

# Slice 267 — the canyon's real meter economy (proven end-to-end)

## Question

Can the full ~11480px bh3 canyon ascent be completed within the
`k.aE` meter's ~600t/leg budget? Prior verdict: **not via checkpoints**
— `aY()`'s `au<2` gate plus the fixed ~3.5px/t chase camera bottom out
~1500px above the cp1 window (minCamY≈9988 vs required [8000,8480]).

## Answer — the designed refill is the ax24-S20 shrine economy

End-to-end chain, all proven and now executable on the port:

1. **Emission** — in free glide the player auto-emits an ax24-S6
   "drop line" every ~10t (`k.aI≥10` gate, `S∉{0,3,17,18,20}`,
   `!i.bk`, `Q!=18`) via `e(false)`/`flap(p,false)`
   (g.java:6084-6087, 6330-6355; PlayerFsm.kt:2420, 2559-2578).
   Spawn: `(p.ak, W[1]-3)`, `ap = al-100`, `ah = -3840+k.X` (~15px/t
   upward), `P|16` force-tick, `af = aS`.
2. **Sweep** — the drop-line's `bc()` arm (i.java L171) recedes `ap`
   at conveyor speed and calls `bc()→i(9)` on any hit; `bc()`'s
   case-24 (`r0.S==19 && overlapStrict(r0.W, e.X)`) converts the
   overlapped lay-child into an S20 shrine (`i.java:13907+`,
   NpcFsm.kt:6945-6947). The S19's lay box is a tall column
   `(-15,-81,39,93)` — rising drop-lines pass straight through it.
3. **Refill** — the S20's L181 arm (NpcFsm.kt:7114-7127): on
   `overlapStrict(aS.W, this.W)` → `k.aF=min(e.aB, 100-k.aE)` (+3/t
   refill into `k.aE`), `i.e=30` wall-immunity, `iBh=0`,
   `k.X=kAJ;kAJ=0` conveyor restore, `aS.i(21)`, `sfx(25)`.
   Level-1 stations 6×S19 along the corridor
   (x∈{222,397,413,454,475,599} records).

## Fidelity bugs fixed

- **`initAx24` missing the L392 record-init tail** (i.java:3680→):
  every arm ends `goto L392` = `i(r8[5])` (+ax37/70 skip) then `t()`.
  The ported init stopped at `aB=r8[7]`, so ax24-S19 records spawned
  with `e.S=0` — `bc()` could never find `r0.S==19`. Fixed:
  `e.setAnim(rf(5)); e.refreshBoxes()`.
- **L171 arm inverted**: `al > ap → goto L173` *skips* `i(9)`; the
  port had `if (al > ap) setAnim(9)` — every drop-line died on its
  first tick before sweeping. Fixed to `if (e.al <= e.ap)`.
- **`S6 drop line anim-9` test corrected** to the proven semantics
  (was encoding the inversion).

## Test

`Slice245Test.bot flight drop-lines arm the canyon shrines and refill
the meter` (Slice1Test.kt:23379):

- Phase A (~400t real climb from spawn): `s6≥1` — the k.aI≥10
  auto-emitter produced 42 drop-lines on the live path.
- Phase B (hover under the S19 at 397,8437): a drop-line converted it
  at probe tick 11 (`SHRINE@probe11 uid32`), then with camera snapped
  (au<2) and the player inside the shrine box: `kAF=66` refill,
  `iE=29` (30 decaying), `p.S==21` — fired.

Notes: the canyon's leftward airflow drags an unsteered probe
off-column before the slow climb covers the ~50px gap — a real pilot
steers; the probe pins into the box instead. `au≥2` parking means the
shrine can't tick until the camera catches up — snapped via k.O/k.P.

## Gates

- verifier `ok:true`; `python3 -m unittest` 57 pass
- `:core:test` 1487 pass (incl. corrected L171 test)
- `:android:assembleDebug` + `:gdx:build` clean
