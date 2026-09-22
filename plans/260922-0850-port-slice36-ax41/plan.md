---
title: "Port slice 36 — ax41 n() knockable prop"
phase: port
status: done
---

# Slice 36 — `i.n()` (ax41 knockable prop)

Ports `n()` (i.java:6414-6522, proven) — the knockable prop family
(vases/props the player knocks into enemies): S3 idle-interact, S4 settle +
damage sweep + removal, S6 tumble physics, S5/default no-op.

## Arms ported (`NpcFsm.tickKnockable`)

- **S3 (L44)** → shared `a()` interact — `pushOut` (already ported).
- **S4 (L4)** — settle: `ag/ah/ai/aj=0`; sweep `k.bd` for touching entities:
  outer filter `{0,11,51,41,4}`, inner dispatch `0→i(9)` (player hurt),
  `11→s==null ? i(0) : s.ax==51 ? skip : this.S==6 ? i(7)`, `51→i(2)`,
  `41/4→` box-check only. `T == aa.b(S)-2` → `k.ae = k.aS` (knock
  attribution → `w.aeRef`). `r()` → `k.c(this)`.
- **S6 (L46)** — tumble: `aj=1536`, `ah=2560` cap → `aj=0`; `bd=true`;
  `a(true)` → `collideSides(w,true)`; `bb` wall-hit → `ag=-ag` bounce;
  `av = ag<0`; `i(k.aS)` player impact or `i(bd)` entity impact over
  `{51,11,41,15}` — each requires `ag!=0 || ah!=0` → `i(4)` (settle entry).
- **S5/default (L92)** — dropped label → no-op (flagged).

## Support ported alongside

- `i(i)` entity collision (i.java:6655, proven) → `knockOverlap`: W-overlap
  always; "carrier" entities (player `g.a.ax==51`, ax11 `s.ax==51`) also
  require `|al diff| <= 20`.
- Dispatch `n.ax == 41 → tickKnockable` (Level0World).

## Correction during this slice

- `k.bd` includes the player; `w.npcs` does not — the S4 sweep appends
  `p` to the iteration (caught by the i9 test).

## Honest gaps

- Inner `case 15 → i(7)` is unreachable: the outer filter admits `r0==4`,
  which maps to the inner default-skip — likely a decompiler constant swap
  (`4` ↔ `15`; ax15's hit-react role fits the inner case). Ported verbatim
  and flagged `inferred`.
- `aS.y()` in `pushOut` remains `inferred`-false (pre-existing flag).

## Verified

- `:core:test` — 185 tests, all green (10 new ax41 tests).
- `verify-static-reconstruction.py` → `ok:true`; `python3 -m unittest` 57/57.
