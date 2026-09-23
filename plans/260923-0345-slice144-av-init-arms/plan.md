---
title: "Slice 144 — ax10 per-S init arms (S11/S24/S28/S30/S31/S39)"
phase: gameplay-port
status: complete
---

# Slice 144 — ax10 record-init arms

## What

Completes the ax10 `i.I()` per-S init sub-switch (`switch (sArr[5])`,
i.java:2119-2229, proven) in `initTrigger` — every listed S value
replaces the `default → L111` field map (`aE=f4, aF=f11, o=f12, p=f13,
aG=f14, ay=f15`) with its own record layout:

- **S11** (i.java:2173): `Z = {f0}` — single field.
- **S24** (i.java:2187): `aA = f11` — grab-spot script uid.
- **S28** (i.java:2190): `Z = {0}` — empty (the arm's only significance
  is *skipping* L111).
- **S30** (i.java:2194): `Z = {f12..f19}` — 8-slot config for the
  pursuer-pool spawner (La72, unported).
- **S31** (i.java:2205): `Z = {f4, f11, f13, f14, f15}` — the claim-QTE
  config (flags, lane-type nibble pack, required presses, done-sentinel
  aA, initial script uid). **Required for the slice-143 QTE arm** — real
  records otherwise spawn with empty Z and L111 fields.
- **S39** (i.java:2219): `P&32==0 → P|=16`.

S55 records correctly keep hitting `else → l111` (not listed in the
original switch → `default:` — verified i.java:2143-2165).

## Provenance

- Inner switch keyed on `sArr[5]` (record field 5 = the S value), cases
  i.java:2165-2228; explicit fall-through list to `default:` (L111) at
  i.java:2143-2165.
- Confidence: proven (decompiled switch + field indices verbatim).

## Gates

- `:core:test` → 1002 tests, 0 failures (+6 init-arm tests)
- verifier `ok:true`; 57 unittests; `:android:assembleDebug`;
  `:gdx:build` — all green
