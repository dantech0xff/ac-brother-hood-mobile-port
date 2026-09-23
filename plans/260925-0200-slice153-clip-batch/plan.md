---
title: "Slice 153 — clip batch: 52/30/6/5/12/59 + bi[] map completion"
phase: port
status: done
---

# Slice 153 — clip conversions + ENTITY_CLIP completion

## Scope

Six more pack-3 clips converted and wired; the `bi[]`-derived
`ENTITY_CLIP` table filled for every converted clip.

## Converted (proven `bi[]` table, k.java:8442)

| clip | consumer |
|------|----------|
| 52 | ax29 Cesare boss (mission-8 level only) |
| 30 | ax41 knockable prop — 1 record on level 0, n() FSM already ported |
| 6  | ax10 trigger zones — load-valid, zero-pixel modules |
| 5  | ax8 knife projectile (`spawnProjectile`) |
| 59 | ax8 boss-knife param (op111 `spawnParam`) |
| 12 | `k.dA` HUD indicator (`T()`, k.java:4165) |

`clip8` (bi[12]=8) does NOT exist in pack-3 (62 entries, 0–61) → ax12
stays clipless, matching `J(8)=null` in the original.

## Converter fix

`pack_clip` previously asserted every module had PNGs unless
`runtime_type_aU ∈ {2,5}`. entry-006's module 0 is load-valid but
`runtime-nonrendering` (pixel tail absent at EOF) — the assert now also
accepts `pixel_reconstruction.runtime_image_behavior == "nonrendering"`,
emitting an empty-name slot so indices stay aligned.

## ENTITY_CLIP additions

`10→6`, `29→52`, `41→30`, `8→5`, `13→61`, `32→36` (same clip as ax30),
`68→26`. Corrected `64→6` comment (bi[64]=22 unconverted, not -1 —
clip6 stands in as the nonrendering stand-in). Removed the duplicate
`74→54` key.

## Remaining unconverted (next asset slice)

`bi[]>=0` clips still missing: 2 (ax3), 13 (ax21/48), 14 (ax22),
15 (ax26), 16 (ax25), 22 (ax64), 23 (ax66), 28 (ax51), 33 (ax45),
43 (ax70), 44 (ax31), 55 (ax75), 56 (ax76 — zero-size entry anyway),
57 (ax80).

## Gates

verifier `ok:true`; unittest 57; `:core:test` green;
`:android:assembleDebug`, `:gdx:build` green; converter idempotent
(51 clip dirs).
