---
title: "Slice 143 — ax10 S31 claim-QTE attribution fix + real S55 (Lf4)"
phase: gameplay-port
status: complete
---

# Slice 143 — ax10 S31/S55 dispatch correction

## What

Corrects a misattribution from slice 139 and ports the real S55 arm.

- **`aV()` `case 31 → L5ea`** (i.java:9200 dispatch, body i.java:9804-10466):
  the 4-lane claim-QTE zone arm ported in slice 139 was placed under `55 ->`
  in `tickTrigger`. The dispatch table routes **case 31 → L5ea**; the arm is
  now under `31 ->` with an updated provenance comment. Body is unchanged —
  verified verbatim against L5ea-La66 (suppress flag → `ab()`/`aa()` →
  P&8192 remove → overlap latches `g.E`/`i.cu` → consumed reset → claim
  bind `h/k(k.s(aA))` → lane arming from `Z[1]` nibbles → `i.cs[]` pad-mask
  scan → `i.ct[]` prompt-card states → `m>=10` resolved / `aA=Z[3]` done).
- **`aV()` `case 55 → Lf4`** (i.java:9224 dispatch, body i.java:9227-9253):
  the real S55 arm — a **boss-reposition zone**. Gates: `k.aU != null`
  (the ax29 boss binds itself at init, i.java:2468 `sArr[5]!=30 →
  k.aU=this`, already ported at NpcFsm.kt:2020), `k.aU.S == 13`, and
  `a(k.aU.Y, this.W)` (boss interact-rect overlaps the zone). Fires:
  `k.aU.i(25)` (anim 25), `k.aU.ak = this.ak` (snap boss x to the zone),
  `k.aU.ah = 0; k.aU.ag = 0` (zero both velocities). The `aB`=800 HP pool
  and `al` are untouched. `k.aU` also backs the boss HP bar
  (k.java:3243-3255: `125*aU.aB/800` px at (120,215)).
- **Tests retargeted**: all slice-139 `zone.S = 55` fixtures → `S = 31`
  (the body they exercise is case-31's), test names `S55 …` → `S31 …`,
  world class `S55World` → `ClaimZoneWorld` (subclasses S140/141/142
  follow automatically). Two slice-137 `zone.S = 55` gE/icu latch tests
  retargeted to S31 (Lf4 does not touch `g.E`/`i.cu`).
- **New S55 tests (4)**: S13 boss overlap → anim 25 + x-snap + zeroed
  velocities (+`al`/HP untouched); no `k.aU` bound → bare return; boss
  not S13 → no-op; rect miss → no-op.

## Provenance

- `aV()` dispatch census: i.java:9162-9265 — `case 31: goto L5ea` (:9200),
  `case 55: goto Lf4` (:9224), `case 34: goto L5e9` (:9203; bare `return`
  at :9802 — the S34 slope-rail work lives in `aU()`, ported slice 141).
- Lf4 body: i.java:9227-9253. `k.aU` static: k.java:94; bound at
  i.java:2468 (ax29 init), cleared k.java:5096/i.java:1829; consumed by
  the boss HP bar k.java:3243-3255 and claim-VM op 10.
- Confidence: proven throughout (decompiled control flow + dispatch table).

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → 996 tests, 0 failures (Slice139Test: 14 — 10 S31 + 4 S55)
- `:android:assembleDebug` + `:gdx:build` → green
