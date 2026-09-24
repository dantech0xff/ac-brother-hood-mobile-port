---
title: "Slice 219 — dormant street soldiers: verbatim au-park gate, not a missing trigger (verdict test)"
phase: port
status: done
---

# Slice 219 — dormant-soldier verdict: the park gate IS the wake mechanism

## Reported blocker

Golden-path demo: "street soldiers dormant without trigger zone" —
soldiers east of spawn never moved.

## Verdict: verbatim `u()` LOD, no trigger needed

Slice 216 ported the `k.I()` non-bh3 entity loop's park gate
(k.java L215 arm, proven): `au >= 2 && !P|16 → skip` — off-screen
entities freeze. The corollary: `au < 2` releases them — camera
proximity alone wakes a parked entity, exactly like the original.
No ax10 trigger arm is involved (S8 alert-toggle / S18 spawn-release
arms exist for scripted reveals, not for ordinary patrols).

## Proof

`Slice219Test`: street-patrol record ax11 uid151 at (2213,472) —

- 40 ticks with the camera at spawn: `S`/`T` never advance (`au ≥ 2`
  freeze, verbatim).
- Bring player + camera to it: `au < 2` → ticks on the next frame
  (`T`/`S` advance) — wake by proximity alone.

With the au-gate now ported, the earlier "soldiers wake anyway" demo
behavior (everything ticked unconditionally) was actually the bug; the
frozen-until-approached behavior is faithful.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` 0 failures;
  `:android:assembleDebug`; `:gdx:build` — all green.
