---
title: "Slice 236 — final bespoke init arms + clipless spawn gate removal"
phase: port
status: complete
---

## Goal

Port the last five bespoke `i.<init>` record-init arms and remove the
unfaithful clipless-spawn gate so EVERY record spawns — the original's
dispatch (i.java:7600) never skips records; `bi[ax]=-1` means invisible,
not absent.

## Source mapping (all proven, fallback i.java)

| ax | label | semantics |
|----|-------|-----------|
| 37 | L633 (:8074) | `Z[0..3]=r8[15..18]`; `P|=0x200`; `(P&0x20)==0 → P|=0x10`. L1bea SKIPS ax37 — no `i()` finish |
| 75 | Ldb1 (:9206) | `az=r8[7]` only |
| 68 | Lfc9 (:9536) | `az=99` only (L1bea's ax68 arm takes `i(0)`) |
| 45 | L122b (:9916) | bare `goto L1bea` — finish only, Z stays zero |
| 31 | L1a79 (:11259) | `az=r8[7]`; `Z[0]=r8[8]*1000, Z[1]=r8[9]*1000, Z[2]=r8[10], Z[3]=0` |

Only ax37 ships records (all 8 packs); the others are ported for
dispatch completeness.

## Gate removal + Z-fill correction

The slice-234 clipless gate (`clipIdx==null && type!=42 && type!=65 →
continue`) silently dropped every `bi=-1` type — including ax37 scroll
bounds in all 8 levels. The original spawns every record
(`Entity.clip` is `Clip?` and draw paths tolerate null). Removed:
`clipIdx` only picks the sprite now.

The generic else-arm `Z[i]=r8[7+i]` fill had no source counterpart —
the remaining types all route `L1bc7→L1bea` (Unknown Actor Type →
record-anim finish) with NO Z writes. Removed the fill; Z stays the
ctor's zero array. Regression-covered by `else arm records take zero Z
fill`.

## Files

- `NpcFsm.kt` — `initAx37/75/68/45/31` appended (:9925-9970).
- `Level0World.kt` — five dispatch arms (:832-836), gate removal
  (:765-768), Z-fill removal + corrected else comment (:837-845).
- `Slice1Test.kt` — `Slice236Test` (ax37 arm incl. the `P&0x20` gate
  case via level-2 record r8[6]=32; zero-Z-fill contract).
- Slice-69 test fixture fix: level-0 carries an ax2 checkpoint at
  `aw=102` (previously gate-skipped, now spawning — `k.q(102)` resolves
  it before test fixtures, as the original would). The ax69-zone test's
  injected victim/link moved to unused uid `9999`.

## Gates

- `verify-static-reconstruction.py` → ok:true
- `python3 -m unittest` → 57 pass
- `:core:test` → 1414 tests, 0 failures
- `:android:assembleDebug` + `:gdx:build` → clean

## Effect

Every record type in the 8 packs now spawns and initializes via its
verbatim dispatch arm. The entity-type coverage for spawn-init is
complete: ~45 bespoke arms + correct L1bc7 fallback.
