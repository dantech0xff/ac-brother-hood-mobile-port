---
title: "Slice 353 — projectile volley i.a(int,boolean): fan, speed and anim from the bytecode"
phase: "port"
status: "done"
slice: 353
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:27553-27900
  - reconstructed-project/bytecode/i.javap.txt:27470-27510
  - reconstructed-project/bytecode/i.javap.txt:28347-28390
  - reconstructed-project/src/structured/i.java:6244-6345
---

# Slice 353 — the volley `i.a(int, boolean)` verbatim

Found while auditing the `j.b` call sites for slice 352. The structured
view of this method carries JADX's "decompiled incorrectly" banner, and
the port had followed it; the bytecode is the authority here.

## Findings (proven, bytecode offsets in `i.javap.txt` from :27553)

Callers: `ax()` (ax54, `a(Z[10], …)`), `ay()` (ax56, `a(Z[5], …)`),
`ba()` (the ax24 grenade burst, `a(9, false)`), `bl()` (ax64,
`a(1, false)`; ported separately as `ax64Barrage`).

1. **Fan (458-762).** `iB = j.b(dx, dy)`; when `i3 > 0 || count even`:
   odd slot `iB + r02·k` (k = `i3%2 + i3/2`), halved only for an **even**
   count's slot 1; even slot `iB − r02·(i3/2)` for an odd count, else slot
   0 `iB − r02/2` and slots ≥ 2 `iB − r02·(i3/2+1)`. Then the 729 re-aim.
   The port had the parity inverted (halving odd counts' slot 1, stepping
   odd counts' even slots one notch too far) and shot even-count slot 0
   straight. Example — the ax24 nine-shot burst: original
   `0, ±32, ±64, ±96, ±128` (a 45° ring, back shot doubled); port
   `0, +16, ±64, ±96, ±128, −160`.
2. **Speed (764-857).** 2048, except ax54 `Z[9]==2` → 1280, ax30's first
   shot of a `Z[9]==2` triple → 2560, ax56 `Z[4]==2` → 1280 and **every
   other caller → 1280** (`835: if ax != 56 goto 854`). The structured
   view shows that last store as an empty `else if` block; the port used
   2048 for the ax24 burst and for ax56.
3. **Anim (1054-1119).** `ax == 54 || ax == 30 → i(Z[8], dir)`;
   `ax == 56 → i(Z[3], dir)`; else `i(0, dir)`. The port gave ax30 `Z[3]`.
4. **`i(int,int)` (27470-27510).** 1 → `i(dir+23)`, 2 → `i(22)`,
   **3 → `i(28)`**, else `i(dir)`. The port mapped 3 to `i(dir)`.
5. **`b(IIII)` (28347-28390).** `|dx| ≤ 5120` (or `dx == 0`) →
   `dy ≥ 0 ? 4 : 0`. The port had two copies; the volley's `dirVariant`
   returned 2 there. `ax64Barrage`'s `dirIndex5` was right and is now the
   only one.

## Fix

`runnerBurst` (now `internal` for tests) follows the offsets above;
`animVariant` gains the `3 → 28` arm; `dirVariant` is gone in favour of
`dirIndex5`. KDoc rewritten with the offsets.

## Tests

- `Slice353Test` (5): the ax24 burst's nine velocities (incl. the doubled
  back shot); even counts 2 and 4 straddle the aim symmetrically; speed
  tiers for ax24/ax64/ax56/ax54; anim sources (ax30 → `Z[8]`, set 3 →
  28, ax56 → `Z[3]`); `b(IIII)` up/down/diagonal indices.

## Impact

- Mission-4 capstone (`Slice288Test`): with slices 352+353 the escort
  fans are aimed where the original aims them and the bot, tuned against
  the old rotated fans, died at the ax30 escort mid-shaft (y≈5180) and in
  the top arena (ax24 ring). The bot now projects live enemy shots 24
  ticks ahead and sidesteps when one would pass within 30 px
  (`dodgeMask288`, test code only). It wins with one mid-shaft death.
  No other capstone changed.

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test`, `:gdx:test`,
`:android:assembleDebug` green (counts in the commit).
