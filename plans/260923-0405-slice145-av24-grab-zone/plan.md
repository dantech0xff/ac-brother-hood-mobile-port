---
title: "Slice 145 — ax10 S24 rope/grab trigger zone (L21e)"
phase: gameplay-port
status: complete
---

# Slice 145 — `aV()` S24 arm

## What

Ports the `aV()` **S24** arm (i.java:9361-9467 `L21e`-`L313`, proven;
dispatch `case 24: goto L21e` i.java:9193) — the rope/grab trigger zone:

- **Eligibility**: `aS.af != e` && overlap `a(aS.W, e.W)` && player
  `S ∉ {291,270,271,90,89,43}` && `!ae()` — where `i.ae()`
  (i.java:19074, proven) = "any live humanoid ax 17/11/23/50/73 near the
  player" (ported as `enemiesAlert`).
- **Eligible + `v(16388)` press** → grab: `aS.af = e` (bind zone to
  player link field), `i(0)`, `i.E()` settle (`eSettle`), `ag=ah=0`,
  `i(267)` grab pose, `G()` (drop marker), `k.v()` clear latches.
- **Eligible + no press + record `aA==1`** → keep marker-7 pinned at
  `(ak, al-15)` via `e.a(7,…)` → `spawnMarker`.
- **Ineligible** → `G()` (`releaseAe`) drops the marker link.

Same mechanics as ax27's S0 arm (`tickAx27`, i.java:19086+) but owned by
an ax10 zone and with the marker offset `al-15` instead of `al-85`. The
only real S24 record (pack-9) has `f11=0` → `aA=0` → marker gate off
(verbatim; slice-144's init arm binds `aA = rf(11)`).

## Provenance

- L21e eligibility chain + grab + marker arms read verbatim from the
  decompiled labels; `ae()`/`h(i)`/`G()`/`E()`/`a(int,int,int)` all
  resolved to already-ported helpers (`enemiesAlert`, `releaseAe`,
  `eSettle`, `spawnMarker`).
- Confidence: proven.

## Gates

- `:core:test` → 1006 tests, 0 failures (+4 S24 tests)
- verifier `ok:true`; 57 unittests; `:android:assembleDebug`;
  `:gdx:build` — all green
