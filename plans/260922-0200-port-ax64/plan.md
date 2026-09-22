---
title: "Port slice 65 — ax64 bl() grabber/harrier NPC"
phase: port
status: done
created: 2026-09-22
---

# Slice 65 — ax64 `bl()` grabber/harrier NPC

Port of `i.bl()` (i.java:15391-15897, ~500 decompiled lines): the airborne
"harrier" NPC that patrols waypoints, stalks the player from below with a
blinking clip-74 prompt marker, binds a pooled `k.aX` tether shot on a pad
edge, dive-bombs on a ballistic arc, grabs on contact, and holds the player
in a mash-to-escape QTE whose expiry is lethal.

## Ground truth

Every label of `bl()` was re-verified verbatim against
`reconstructed-project/src/simple/i.java:15391-15897` before writing the
port. Decompiler labels below refer to that range.

- **Head arm (L6-86)** runs only while `S==7`. Computes `k.h` octagonal
  distance; when the player shows a vulnerable anim
  (`S ∈ {0,4,5,17,18,30,31,32,33}`), the harrier is strictly below the
  player, `|dx| > 25`, `dist < 200`, it picks a directional prompt marker
  (`a(42|48|60|66, x, y)` on the player) and latches the matching pad mask
  into `bl` (`2|8|128|512`). Cleanup arms release mismatched-side markers;
  an armed + unbound + `k.v(bl)` pad edge fires the tether shot
  (`i.a(k.aS, this, 1, false)`, i.java:8637) and latches `G`.
  - `inferred`: the decompiler's `ae.S==66 → L19` re-entry loop can only
    spin forever (all gates re-pass); collapsed to the same reposition arm
    as `S==60`.
  - `proven` quirk kept: a just-picked marker on the matching side is
    released again by the L53-67/L76 cleanup the same tick — the prompt
    effectively blinks at 62 ms.
- **S0 (L88-183)**: lob window `dy ∈ (150,190) && |dx| < 100` primes a
  ballistic arc to `p.ak, p.al+32` (`cz = dy/5` ceil, `cx = |dx|·5<<8/dy`,
  `ar += cz·(-7)` — the −7 is the `k.X` wind). Otherwise `aA` walks the
  `Z[2+aA]` waypoint chain at pace `Z[1]<<8` with dominant-axis crawling.
  Arrival advances `aA`; `aA<4` re-fetches the NEXT node and checks its
  `c` hop flag; `aA>=4` keeps the old node (L144 verbatim). `aA>=4` also
  primes the dive target (L109: anchor ±100/±190 clamped to the player).
  - `proven` decompiler quirk preserved: the y-dominant arm's
    proportional-x term divides by `|wdx|` and immediately multiplies by
    `|wdx|` — cancels to `ag = ±|ah|`.
- **S1 (L186-232)**: waypoint-hop (`cy`) lerps across the anim's remaining
  frames, or the target-dive flies `aq/ar` at `±cx`/`±1280`. `cA > cz`
  times out by clearing the target. `!g.s && W-overlap` snaps onto the
  player and enters S2 with `aC = 30` (the grab). Arrival tails to S6
  (`Z[7]>0`) or S3.
- **S2 (L263-322)**: mirrors the player's velocity and `al`; keeps the
  clip-74 prompt marker pinned at the view centre (`k.O+200, k.P+120`);
  `aC` expiry runs `g.d(999)` + release + `i(4|5 by Z[6])`. While held it
  sets `i.bi`, forces the player to S15, resets the quake fields
  (`bC/bD/bE/bG` iff `bB`), and runs `i.f(4112,8256)` — the alternating
  mash QTE whose win releases `bl`, `k.e(0,aw)`, clears `i.bi`, `p.i(9)`
  and resets EVERY S2-bound ax64 sibling.
- **S3/4/5 (L300-306)**: despawn tail. `aS.bl = 0` only on the S4/S5 (and
  S2-fail) entry — S3 enters at L301 and skips it (proven).
- **S6 (L234)**: `r() → i(7)`; below `k.P-20` sinks at `k.Y`.
- **S7 (L240)**: per-tick `Z[8]--`; `aC--` ≤ 0 fires the barrage
  `i.a(1,false)` and reloads `aC = Z[9]`; hovers `ah = k.Y>>1` vs `k.Y`
  below the screen bottom; horizontal chase `±512` within `|dx| > 10`,
  halted while bound; `v()` fail → `k.c(this)`.

## New world hooks (Entity.kt `LevelCellSource`)

- `iBi` — `i.bi` grab-hitlag flag (i.java:15686).
- `gS` — `g.s` cutscene flag.
- `allocShot()` — `i.av()` (i.java:7813) over the `k.aX[50]` pool
  (k.java:8423 `aW=50`): lazily grown `new i()`-blank slots,
  `P&128` = free.
- `tickShotPool()` — pooled-shot step (`inferred`; original pool tick is
  unmined): `am+=ag; an+=ah; ah+=k.Y`, `aC--` lifetime frees the slot.

## Corrected globals

`k.X` was ported as `0`; the level init assigns `X = -7`
(k.java:2334) and `Y = X << 8 = -1792` (k.java:2336/2742). Both are now
faithful: `w.kX = -7`, `w.kY = kX shl 8` (derived so `k.X` wind writes
re-price the bias exactly like the original). One prior test expectation
updated (springboard impulse `768 + kY`).

## Confidence

- `proven`: all S-arms, constants, marker anims, latch masks, dive/lob
  math, QTE thresholds (4112/8256, 30-tick hold, +9 touch), pool flag
  semantics.
- `inferred`: L19-loop collapse; the pooled-shot integration step
  (`tickShotPool`); slot ctor shape (`new i()`-blank); clip-identity check
  in S2 relaxed to `ax==14` (our markers spawn clip-9 via `spawnPickup`);
  `k.aX` slots live outside `bb[]` (spawn writes never call `k.b`).
- Level-0 spawn: pack-6 has zero ax64 records (verified histogram) and
  `bi[64] = -1` → records spawn clipless. `ENTITY_CLIP[64] = 6` is
  unconverted → `null` clip, matching the original's clipless path.

## Gates

verifier `ok:true` · `python3 -m unittest discover -s tests` 57 pass ·
`./gradlew :core:test` 514 pass (12 new in `Slice65Test`) ·
`./gradlew :android:assembleDebug` green.
