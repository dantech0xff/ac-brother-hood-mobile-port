---
title: "Slice 356 — I()'s soldier-family head (t(), corpse landing, g.h), case 23 → L849, ax73 au(), stale comments"
phase: "port"
status: "done"
slice: 356
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:3976-4004
  - reconstructed-project/bytecode/i.javap.txt:19348-20030
  - reconstructed-project/src/structured/i.java:1232-1234
  - reconstructed-project/src/structured/i.java:6191-6201
  - reconstructed-project/src/simple/i.java:5172-5185
  - reconstructed-project/src/structured/g.java:2335-2375
  - reconstructed-project/src/structured/g.java:2475-2500
---

# Slice 356 — the soldier-family head

Phase 1 item 1.7 (G7: ax23 + stale comments) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md);
mining it surfaced a larger gap (G13 below).

## Findings (proven)

`I()`'s `case 11/17/23/47/50/73` runs a shared head before the per-type
switch (structured `i.java:3982-4004`; bytecode `i.javap.txt` I() offsets
1377-1578):

1. `if (!P()) t()` — a dead member releases its `ae` marker (`G()`)
   instead of refreshing its boxes; a live one refreshes before its arm.
2. Corpse landing: in a corpse state (S21; S0 unless entered from 25/184;
   S135/20/69/106/107/164/168/94/117/78/184; S85 at `aB<=0`), on screen
   (`v()`), at `T==1 && U==0` → `k.A(24)`; if the body came out of S24
   (player pinned on its head — the air kill) or S175 (held by the
   player), the player is released with `aS.a(0)`, `bl = 0` (for S20 not
   while the player is in S157).
3. `g.h`: `f() && g.h == null → g.h = this`, `!f() && g.h == this → null`;
   `f()` (`i.java:1232-1234`) is the player in S38 just below the member
   or in S203 with its `X` on the member's `W`. The player's S203 ledge
   kill (`g.java:2353-2364`) reads `g.h` for its prompt and pull.
4. The switch sends `case 23` straight to L849 (`simple/i.java:5180`);
   L849's `au()` drop (`i.java:6191-6201`) covers 11/17/73.

The port had none of the head (G13): `g.h` was never assigned — so the
S203 ledge kill at the 11 ax10-S43 zones (m0 2, m2 4, m5 2, m7 3) always
fell through to `G()` — and it kept two fields for it (`grabHolder`,
`player.gh`). ax23 ran the ax11 arms (unreached: only the ax10-S30 wave
grid's flavour `Z[6]==2` spawns ax23, and both S30 records use 0), and
ax73 never ran `au()` (no ax73 record carries a drop link today).

Also fixed while here: the combo arm's assassination shortcut had an
extra `t.aB > 0` term that `g.java:2479` does not have (the tail's own
dead-target guard handles that case), and `BW_MOCK` is `i.bw[k.au]`
(`{80,80,80}`, `i.java:171`).

## Fix

- `NpcFsm.familyHead` (+ `ledgeKillF` = `i.f()`), run by `NpcFsm.tick`
  for ax11/23 and before `tickAx17/47/50/73` in `Level0World.tickNpc`.
- `case 23 → L849`: ax23 runs the head, then `au()`/`t()`/facing/push.
- ax73 runs `corpseDrop` (`au()`) after `aJ()`.
- `Level0World.grabHolder` is `player.gh` — one `g.h`.
- `losL` is `internal` (tests).
- Stale comments: NpcFsm header (described slice 2), the orphaned combo
  KDoc ("shortcut omitted" — it is ported), Level0Game audio ("MIDI
  log-skip" — AudioBridge plays the OGG renders).

## Tests

- `Slice356Test` (7): `g.h` for S38 below / S203 X-on-W; first-come
  hold; corpse landing sfx + pinned-player release; S157 exception and
  plain-Q sound only; dead member releases `ae` without `t()`; ax73 drop
  through `au()` end to end.
- `Slice350Test`: the ax23 test now checks `l()`'s containment directly
  and that ax23 runs no arm (it encoded the old routing).

## Impact

- Mission-5 capstone leg I (`Slice289Test`): the S90 air kill on uid226
  now ends when the corpse lands (the release above), ~5 ticks earlier,
  so the bot met foe 534 mid-swing and was beaten back. The bot stands
  16 ticks after that release (deterministic: 15-17 pass). Bisected: with
  the release disabled the unchanged bot passes. Also: the ax50 pouncer
  uid225 now hits with fresh boxes (head `t()`), which the bot avoids by
  the same re-phasing.

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test`, `:gdx:test`,
`:android:assembleDebug` green (counts in the commit).
