---
title: "Slice 181 — ax25-init fidelity + ax10-S10 band-direction fix"
phase: port
status: done
---

# Slice 181 — ax25-init fidelity + ax10-S10 band-direction fix

## What

Two correctness passes on the flying level (mission 1, `bh==3`):

1. **ax25 init verbatim** (`i.java:2415-2429`, proven): the ax25 player
   record now gets the full original init — `az=202`, `x1=90`, `aA=2`,
   `aB=3`, `ah=-2560`, `aq=ar=-1`, **clip = `bi[25]=16`** (the
   glider-suit clip, not clip0), and `sArr[0]=26; ad=new i(sArr)` — the
   companion entity inherits the record uid (`aw`), runs its own init
   arm (`az=201`, `i(r8[5])`), and lands at the record position. The
   grounded branch restores `player.clip = clips[0]` and nulls `ad`.

2. **ax10-S10 band-direction fix** (`i.java:9480-9772` L323, proven):
   the perch offer arm's in-band test was inverted. Smali semantics:
   `dy<=Z[1] → out`, `dy>=Z[0] → out` → in-band is `Z[1]<dy<Z[0]`.
   Real records carry `Z[0]>Z[1]` (level-1 `{180,50,0,90,30}`), so the
   ported `Z[0]<=dy<=Z[1]` was unsatisfiable — the perch hand/arm offer
   could never fire and the climb could never bind. Fixed to
   `dy > e.Z[1] && dy < e.Z[0]` (approach-from-below window: player's
   top edge 50–180px below the zone bottom). The test fixture used
   `Z={-60,0}` which coincidentally made the inverted check non-empty —
   updated to record ordering `Z={180,50,10,90,7}` and repositioned the
   placement-sensitive tests.

## Also

- Test `world()` clip map gains `clip15`/`clip16` (was silently
  `player.clip=null` on bh3 → degenerate `W` rect → camera drift; the
  dL stamp-window test then asserted stale linear indices).
- Steering test rewritten for clip16's real geometry: its S32/S33 bank
  poses are **1-frame** anims → `animFinished()` is instantly true →
  the verbatim recover arm (`kBB==0 && kBC==0 && r() && z4 → i(4)`)
  resets S the same tick. Assertions pin the `Q` stamp (arm evidence)
  plus the S=4 recovery instead of the blip itself. Held keys now
  released between steer ticks (held pad re-steers).

## Verification

- `Slice180Test` — ax25 init arm asserts `ad.az=201`, `ad.aw=1`,
  `ad.S=4`, `p.clip == clipFor(16)`, grounded `clipFor(0)`/`ad==null`.
- `Slice141Test` — S10 suite green on corrected `Z[1]<dy<Z[0]` band.
- `:core:test` all green; verifier `ok:true`; 57 unittests;
  `:android:assembleDebug`; `:gdx:build`.

## Citations

- `i.java:2415-2429` — ax25 init arm.
- `i.java:9480-9772` — aV() S10 (L318-L5a8); L323 band check.
- `k.java:266` — `bi[25]=16`, `bi[26]=15`, `bi[65]=-1`.
