---
title: "Slice 261 — checkpoint7→win-fuse bot leg (tower, door 87→134, low road)"
phase: slice
status: done
---

# Slice 261 — cp7 → ax42 win fuse bot leg verified

The seventeenth headless bot leg: the level-0 end-game from checkpoint7
(10016,679) to the win fuse at x11410, driven entirely by real pad input.

## Route proven by the test

`bot runs checkpoint7 to the win fuse through the tower` (Slice1Test.kt)
parks the player at cp7 and drives it east:

1. **100px wall-kick well** — cp7 sits between pillar x9920-9940 and wall
   x10040-10120 (lip y560). A grounded hop arms S33 (airborne wall hit);
   the shaft-kick chain then holds `dir` on the *new* facing plus `M_UP`
   in S33/36/92/101, climbing the well onto the slab (top y660).
2. **Tower west column** — east run onto the column top (y439).
3. **Posted soldier uid571** (10547,437) stands on that top and binds
   `player.g`; the door arm faithfully requires `g == null`, so the bot
   slashes it (`M_CONTEXT` while bound) until the lock clears.
4. **ax10-S16 door uid87** (box x10587-10618 × y330-434, oId=134,
   script 600) — the bot settles inside the box with `held=0` then edges
   `M_UP` → `doorExitBh` fade-teleports to uid134 east of the shaft.
5. **Arrival release** — `doorArriveBi` pins the player mid-fade; the
   `kAo && bI>13` arm flings it free (`flingAirborne` clears `ac`).
6. **Ping-pong suppression** — the arrival lands the player *inside the
   destination door's own box*, so the generic stuck-UP arm is gated out
   of both door boxes (10580-10635, 10780-10835) — otherwise each
   teleport re-triggers the pair back. This is a bot-input fix only; the
   game code is verbatim.
7. **Low road** — drop to y780, run east past the 3-soldier pack
   uid89/90/92 (10966-11165) → `p.ak > 11400` trips at the ax42 fuse.

Result: `goal=true deaths=0 maxAk=11576` — the fuse is reachable.

## Fidelity notes

- The shaft gap x10660-10740 is a death pit sealed by '20' at the top;
  the door pair is the intended crossing, not the gap.
- Door pairs are bidirectional (verbatim): the suppressed stuck-UP is
  what keeps a generic bot from bouncing back and forth — a human player
  simply walks off the arrival spot.
- `player.g == null` gate + `flingAirborne` clearing `ac` are proven
  source behavior (i.java:12326-12431 arm; Entity.kt:615-619).

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test`, `:android:assembleDebug`, `:gdx:build` → green
