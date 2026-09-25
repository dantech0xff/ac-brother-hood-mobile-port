---
title: "slice 255 — checkpoint→checkpoint2 bot leg + player link-tick tail (k.java L2df)"
phase: port-slice
status: done
---

## What

Two coupled pieces, verified by one bot playthrough test:

1. **Production fix (proven)**: `k.I()`'s player-link tail —
   k.java:8798-8810 (`L2df`–`L32a`). Immediately after `aS.I()` the
   original ticks the player's `ac`/`ab` links UNCONDITIONALLY (no
   `au`/`P&256`/`P&32` gate) and `ad` when `ax == -999`. Our port never
   ticked `player.ac`, so an ax10-S16 destination door — held via
   `bindAc` (`i.a(iVar)`, i.java:250-279, sets `P|256` on the bound
   entity, which the generic entity loop skips) — could never reach its
   S16 arm's `bi()` arrival. Result in the port: the door-enter `i(284)`
   parked forever mid-teleport. Fix ports the tail verbatim:
   `player.ac`/`player.ab` → `tickNpc` (the full `i.I()` path),
   `player.ad` gated `ax == -999`.

2. **Golden-path leg (bot input, no teleports)**: park on checkpoint
   (2594,519), run east — S12 autorun across the x2760-2880 void →
   S107 auto-vault onto the '02' walkway → LEFT|DOWN flips `av` and
   fires `a(257,8)` off the ledge's west edge → fall into the void →
   S62 ledge-hang → drop to the continuous y680 under-floor → through
   the x3040-3060 wall's open slit (y540-679) → x3120-3180 floor →
   second void → y840 floor → ax10-S16 door at 3784,737 (UP-held
   overlap) → `doorExitBh` + S284 → destination (3787,478) ticks via
   the new link tail → `bi()` teleports to the upper tier → east past
   x3830. `checkpoint=true deaths=0 maxAk=3832`.

## Provenance

- `aS.ac/ab/ad` unconditional link tick — k.java:8798-8810 (fallback).
- `bindAc` = `i.a(iVar)` P|256 hold — i.java:250-279.
- Door enter arm (overlap + `padHeld(16388)` = UP + `!attacking` +
  `aZ`) → `doorExitBh` + `i(284)` — i.java:35993-36092 (S16 `aV()` arm).
- `bi()` arrival (teleport + `i(285)` + `k.m(k.ad)` camera snap +
  `k.C(26)` fade-out) — i.java:41093-41149.
- `a(257,8)` ledge-drop arm (probe +20, `aQ∈{20,5}`, `aR==0`,
  `e(side,below)<12` in the FACING direction) — g.java arm at
  PlayerFsm.kt:1760-1790.

## Verification

- `Slice245Test` — 16/16 green incl. `bot runs checkpoint to checkpoint2
  through the guard pack` (checkpoint=true, deaths=0, maxAk=3832).
- Scratch probes kept in-file: `probe void under-floor landing`
  (under-route landing physics), `probe door park at 3801` (pre-fix
  reproduction — now superseded by the leg test).
- Gates: verifier `ok:true`; `python3 -m unittest` 57 pass;
  `:core:test` green; `:android:assembleDebug` + `:gdx:build` green.

## Notes / quirks kept verbatim

- `P|256` held entities skipping the generic entity gate is original
  behaviour (k.java:8505-8520) — the player's link tail is the designed
  escape hatch, not a workaround.
- The arrival pause (~50-75 ticks of S22 at the destination) matches
  the fade-out ramp (`kFk=26`) + `bi()` sequencing — the player waits
  out the fade like the original.
