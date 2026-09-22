---
title: Slice 61 — ax69 bC() air-assassination target zone
phase: port
status: done
---

# Slice 61 — ax69

## What the original does

- Dispatch `case 69 → bC()` (i.java:5096; bC():17721-18000, proven) —
  the **air-assassination target zone**: a marker volume placed at perch
  spots (all three records: pack-11 `[69,152,8255,1100]` and
  `[69,38,6316,1101]`, pack-12 `[69,43,2881,1045]`). `bi[69]=38` →
  clip38 (W = 76×76 trigger box anchored ~76px above the perch).
- Init arm L194 (i.java:3191) + shared tail L392 (:3680): `az=0`,
  `Z[0]=r8[4]` zone flavor, `Z[1]=r8[7]` victim-link uid, `aA=0`,
  `i(r8[5])` + `t()`.
- S0 idle (L54): `Z[0]==0` binds a `g.b(S)`-grabbable player overlapping
  W → `aS.i(250)` hang, center on the zone, `af=k.q(Z[1])`, `i(7)`;
  `Z[0]∈{1,2}` additionally requires `W[3] ≥ W-mid` (feet hanging at or
  below the perch midline) → `i(1)` perch.
- Preamble (L2–L50, runs while `Z[0]∉{0,2} && aA!=1 && S∈{6,7} &&
  aS.af==this`): scans `k.bd[]` for an ax11 victim `!g(aS)` (facing
  away), Y-overlapped with the zone, within 40px of a zone edge →
  `af` = victim + `c/d(ak+52, al-85)` hand marker. Keep-alive drops
  `af` via `G()` when the victim drifts >40px OR overlaps the zone.
- S7 kill arm (L71): `Z[0]!=0` auto-eligible; `Z[0]==0` needs prompt
  eligibility `(victim.ak-W[2]<40 && !victim.g(aS))` → marker at
  `(W[2]+35, W[1]-35)`. `v(65568) || V()` → `aS.i(244)` leap, `P|=64`,
  snap to zone right-center, `aS.h(1)` claim, `af.i(117)` death anim,
  `af.az=-1`, `i(2)` windup, `G()`.
- S6 carry-drift (L103): `a(true)` side-probe, d-pad `u(8256|4112)`
  drifts zone+player ±1536; `w(12368)` (eM release latch — **new 4th
  pad register**, k.java:555/7203-7230) → `i(7)`; `y()` wall-edge →
  `ag=0`.
- Tail anims pick by `k.bK` — mined this slice as the **HAS-BLOOD JAD
  flag** (GloftASBR.java:36): the manifest lacks the property →
  `null.equals("0")` NPE → catch → `bK=false` → the censored S10/11/12
  set everywhere. S2→`bK?3:10` + recenter player; S3/S10→`bK?4:11` +
  `af=null;G()`; S4/S11: `aA==1` → `bw=-1,bx=57,l(13)` mission advance
  (screen-L impl pending); else Z[0]-routed park/release/settle tails;
  S5/S12 share the park tail.

## Port deltas

- `Entity.kt`: `faces(o)` (`i.g(i)` :7758 — `(o.ak<ak)==av`, "other on
  my facing side" under this port's av=facing-left), `markerAlive`,
  `markerPoint/moveMarker/markerTouched` (`i.o/c/d` :9825-9856),
  `markerLx/markerLy`, interface `padRelease(mask)`.
- `Pad.kt`: 4th register `released` = `held & ~nextHeld` (`k.w`/`eM`
  latch at k.java:555 `eN=eL` on pointerReleased) + `w(mask)`.
- `Level0World.kt`: `padRelease` impl, `findByAw(-1)→null` head guard
  (`k.q` :5887 — previously wrongly matched `player.aw==-1`),
  `ENTITY_CLIP 69→38`, init/tick dispatch, `kBK` doc corrected to the
  HAS-BLOOD flag.
- `NpcFsm.kt`: `tickAx69` + `initAx69` verbatim (L-references in
  comments); 4 fidelity fixes applied on re-read — keep-alive drop ON
  overlap, S4/S11 Z[0] dispatch order, S6 `padRelease` + unconditional
  `y()` end-check, S7 `r8` polarity.
- `Level0Game.kt` + `Level0Renderer.kt`: clips **21 and 38** added —
  clip21 was missing since slice 60 (ax60 spawned with a null clip).
- `convert_slice1.py`: clip38 entry (`pack-3/entry-038-marker-003`).
- `Slice69Test` — 12 tests: init fields, S0/Z0=1 perch binds (+gB gate),
  preamble bind/facing-reject/keep-alive drop, kill arm (auto + prompt
  gating), censored tails, `aA==1` advance, S6 drift/release.

## Confidence

- **proven**: all bC() arms, init arm, `k.q` -1 guard, `k.bK` flag
  semantics, pad `w()` register, `i.g(i)` polarity, `j.b` tables.
- **inferred**: `i.c(x,y)` spawns a clip-74 hand marker (`aa=k.r(74)`);
  the port reuses `spawnPickup` (ax14 clip9 az302) — marker art differs
  from the original, flagged since slice 21.
- **unported**: `l(13)` is a screen-L no-op (only 12/15/21 handled);
  `aS.h(1)` bind-claim slot rides the existing `bindScript` stub.

## Verification

- `python3 scripts/verify-static-reconstruction.py` → `ok:true`.
- `python3 -m unittest discover -s tests` → 57 pass.
- `./gradlew :core:test` → **443 pass** (+12 Slice69Test).
- `./gradlew :android:assembleDebug` → green; emulator boot pending in
  the wrap-up run (all three ax69 records spawn in packs 11/12 — not
  level 0, so the boot check is clip-load coverage only).
