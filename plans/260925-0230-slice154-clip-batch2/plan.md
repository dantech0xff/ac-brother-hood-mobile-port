---
title: "Slice 154 — clip batch 2: 13/14/15/16/23/28/44 + bi[] map completion"
phase: port
status: done
---

# Slice 154 — second clip conversion batch

## Converted (all proven `bi[]`, k.java:8442)

| clip | consumer | level-0 records |
|------|----------|-----------------|
| 14 | ax22 capture zone | **10** |
| 13 | ax21 director / ax48 | 0 (director entities) |
| 15 | ax26 | 0 |
| 16 | ax25 | 0 |
| 23 | ax66 moving platform | 0 |
| 28 | ax51 pushable crate | 0 |
| 44 | ax31 | 0 |

## ENTITY_CLIP — now matches bi[] exactly

Added `22→14`, `21→13`, `48→13`, `25→16`, `26→15`, `51→28`, `31→44`,
`66→23`; corrected `64→22` (bi[64]=22, pack-3 has no entry-022 →
clipless, matching `J(22)=null`).

## Faithfully clipless (pack-3 entries absent — `J(N)=null`)

clips 2 (ax3), 8 (ax12), 22 (ax64), 33 (ax45), 43 (ax70), 55 (ax75),
56 (ax76), 57 (ax80) — no `entry-NNN` in pack-3 (62 entries, 0–61);
the original itself spawns those types clipless.

## Gates

verifier `ok:true`; `:core:test`, `:android:assembleDebug`,
`:gdx:build` green; 58 clip dirs.
