---
title: "Slice 378 — b()'s i.bQ flash and i.ce white backdrop"
phase: "port"
status: "done"
slice: 378
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:2812-2860
  - reconstructed-project/src/structured/j.java:917-919
  - reconstructed-project/src/structured/i.java:18144-18148
  - reconstructed-project/src/structured/i.java:18241-18242
---

# Slice 378 — the `b()` backdrop fill

Found while mining slice 376's `b(z2)` pass.

## The original (proven)

After the tile blit and the `if (am)` clip restore, before the draw-list
build and the entity loop (structured k.java:2848-2859):

```java
if (i.bQ > 0) {
    cd.setColor(i.bQ % 4 <= 2 ? -1 : -65536);   // white, red every 4th
    j.b(cd, 0, 0, 400, 240);                      // fillRect, j.java:917
    i.bQ--;
} else if (i.ce) {
    cd.setColor(-1);
    j.b(cd, 0, 0, 400, 240);
}
```

`i.bQ` is set by claim-script sub-op 6 (i.java:18241), `i.ce` by
sub-ops 13/14 (i.java:18144-18148). The fill sits under the entities. It
runs in every `b()` pass, `b(true)` included.

## The port (before)

Sub-op 6 stored `iBQ` and nothing read or stepped it; `iCe` only picked
the palette. Neither fill was drawn.

## Changes

`Level0World.backdropFill`, computed in `bPass()` after the head (with
the `bQ--` step); the renderer fills the view with it between the tile
layers and the entity loop.

## Tests

`Slice378Test` (3): the white/red cadence and the step, `ce` under a
running flash, `b(true)` frames step it.
