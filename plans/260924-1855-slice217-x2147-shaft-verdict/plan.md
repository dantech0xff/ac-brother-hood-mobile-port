---
title: "Slice 217 — x2147 shaft: instant-fail is verbatim level geometry (verdict + regression test)"
phase: port
status: done
---

# Slice 217 — x2147 "hazard" verdict: verbatim trap shaft

## Reported blocker

Golden-path demo: approaching x≈2147 on the ground route → instant
mission fail on contact.

## Probe result (reproduced in test)

Player at (2147,470) enters `S43` fall, drops through a floorless
column, and `k.v` fires mission-fail at tick 9. The only entities
overlapping at fail time are an incidental ax14 marker (uid129) and the
ax34 companion overlay — neither has a fail arm.

## The shaft is real level geometry (proven)

`et` layer (pack-6, verbatim): column 107 (x2140–2159) is `-1`
top-to-bottom — `k`'s load normalization (`k.java` ~:19175 La6,
proven: `et[i]==-1 → 0`) rewrites it as air. There is no floor from
y≈400 to the world bottom; the '02' ledge east of the ax4 crates simply
ends. The port already applies the same `255 → 0` remap
(`LevelPack.collisionCell`).

So "contact → instant fail" is the original's pit-fall: walking into
the shaft drops the player and `k.v` fails — identical to the J2ME
game. The ax14 marker at x2104 was a misattribution.

## Intended route (records, proven)

The ax74 wisp arc x2033–2140 (y490–736) marks the jump-over trajectory
across the shaft onto the east ledge — ax37 trigger uid311 (2225,417) +
ax10 zone uid241 (2176,468) — toward the ax44 poles at x2480 and the
checkpoint (2594,485). The mouth-plant finding from slice 215 applies:
trap geometry, not a launch assist.

## Change

`Slice217Test`: asserts the shaft cells are verbatim air (and the row-40
floor resumes west), then reproduces the fall → `k.v` fail through real
tick path.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` 1385/0 failures;
  `:android:assembleDebug`; `:gdx:build` — all green.
