---
title: "Slice 376 — j.t is a fail/win frame-skip latch, and b(z2) is one pass"
phase: "port"
status: "done"
slice: 376
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/j.java:101-105
  - reconstructed-project/src/structured/j.java:1313-1349
  - reconstructed-project/src/structured/k.java:486-516
  - reconstructed-project/src/structured/k.java:1104-1122
  - reconstructed-project/src/structured/k.java:1448-1475
  - reconstructed-project/src/structured/k.java:2653-2698
  - reconstructed-project/src/structured/k.java:2812-2860
  - reconstructed-project/src/structured/k.java:3131-3253
  - reconstructed-project/src/structured/k.java:4276-4279
  - reconstructed-project/src/structured/k.java:4833-4835
  - reconstructed-project/src/structured/k.java:5093-5136
  - reconstructed-project/src/structured/k.java:5172-5175
  - reconstructed-project/bytecode/g.javap.txt (e() 533-614)
---

# Slice 376 — `j.t` and the `b(z2)` pass

## Findings (proven)

`j.t` (j.java:105):

- Set only by `j.a(i, false)` for `i < 5` (j.java:1334-1342). Two callers:
  the veil latch at the head of `b(z2)` — `am && !dd → dd = true; …;
  j.a(0,false)` (k.java:2692-2698) — sets bit 0, and `K()` (k.java:2672-2676)
  sets bit 4. `K()` runs in the loader's step 8 (`G(8)`, k.java:4834) and in
  the checkpoint reload `a(true)` (k.java:5174).
- Cleared by `k.p()` (`if (am) {…; j.b(0,false); j.i(0)}`, k.java:2662-2669 —
  bit 0), `W()` (`j.b(4,false)`, k.java:5132 — bit 4), `j.h()` (all,
  j.java:1322; boot only, k.java:4010) and the skipped frames themselves
  (`j.t = 0`, k.java:1119 and :1470).
- Read only by `j.i()` (`t != 0`) in case 12/13 (k.java:1109) and case 31
  (k.java:1453): when set, the frame skips `b(true)`, the panel and `Q()`
  and clears `j.t`.
- The pointer handlers (k.java:486-516) and the key handlers
  (j.java:267-277, overridden empty in k.java:5578-5584) never touch it.
  `j.q`, written alongside, has no reader.

So `j.t` makes the first fail/win/stats frame after a level load, a
checkpoint reload or an input lock a skipped frame; it never gated input.

The port read it as a pad-held latch: `kJT` set on DOWN/MOVE in a pad zone
and cleared on UP, plus `inputLockT` set by the veil latch, which made
`consume()` drop every input event until `k.p()`.

`b(z2)` (structured k.java:2679-3253) is one pass: entry return on jc
12/13/31 when `!z2`; head (veil latch, visible window); draw list and
every `F()`; `ad()` bubbles when `!z2`; `c(z2)` under `(C == null ||
!C.cd[6] || !C.ab())` (:3131); the claim SKIP pill when `!z2` (:3160);
then the tail counters (`an`/`ao` fades, vignette, letterbox, flicker —
:3163-3253) with no gate. The port ran the head only on `b(true)` sites
(so the veil latch never fired on play or dialog frames), ran `c()` and
the tail only on play frames, and called the tail from inside `c()`'s
gate, so a claim that hid the HUD (or the bh3 meter's early `return`)
also froze the fades. `c()`'s weapon-corner arm opens with `!z2`
(k.java:4276); the port cleared `at` from `b(true)` too.

The veil latch's `j.a(cd,-1,-1,1,1,true)` is a setClip collapse restored
by `if (am)` at k.java:2820, before the entity loop: the latch frame keeps
the previous frame's background under the new entities. The renderer
blacked out the whole frame instead.

## Changes

- `Level0World.jT` replaces `kJT`/`inputLockT` (`LevelCellSource.jT`):
  bit 0 from the veil latch in `scrollBounds()`, bit 4 from `kK()` (`K()`)
  at loader step 8 and in `reloadCheckpoint(true)`; `teardown()` (`W()`)
  clears bit 4; boot's `j.h()` clears it; `Entity.unlockInput` (`k.p()`)
  is a no-op unless `kAm` and clears bit 0.
- `consume()` no longer drops input, and pad presses no longer touch `jT`.
- jc12/13 and jc31 skip their frame on `jT != 0` and clear it.
- `bPass(z2)` is the one `b(z2)`: `scrollBounds()`, `drawStylePass()`,
  `drawPassBubbles()` (over the same draw list), `hudStep(z2)` under the
  claim gate, `claimFooter()`, `overlayTailStep()`. Play frames call
  `bPass(false)` after the knockout check, dialog frames call it before
  the dialog machine, and every `b(true)` site calls `bPass(true)`.
- `hudStep(z2)` clears the weapon-corner latch only when `!z2`.
- Renderer: the black veil frame is gone.

Not reproduced: the one-frame background smear of the latch frame and of
`K()`'s frame (the renderer draws each frame fresh).

## Tests

`Slice376Test` (9): dialog-frame veil latch, input under the lock, the
skipped fail-screen and stats-screen frames, `G(8)`, `a(true)`/`a(false)`,
`W()`/`k.p()`, dialog-frame tail counters, `b(true)` and the weapon
corner. `Slice88Test` corrected to the bytecode (pad presses leave `jT`
alone; the fail-screen skip follows `K()`).

## Follow-ups found while mining

- Slice 377: `ap[1]` counts `a(true)` reloads (`o(1)`, k.java:5175), not
  `l(12)` entries; the checkpoint restore keeps it (k.java:5185-5190).
- Slice 378: `b()` paints the `i.bQ` white/red flash (and steps `bQ--`)
  and the `i.ce` white backdrop (k.java:2848-2859); the port does neither.
- Slice 379: the port opens the death screen the tick `x[1]` hits 0; the
  original waits for the S50 death animation (g.java:2200-2219).
- Slice 380: jc21 runs `I()` under `u == 8` dialogs and `H()` on bh3
  missions (k.java:861-866); the port freezes the world on every dialog.
- Slice 381: the renderer steps claim prompt cards and `cb[3]` per
  rendered frame (k.java:3085-3127).
- The `g.e()` head's `k.am` release (g.javap.txt e() 533-614) is slice
  369's F9.
