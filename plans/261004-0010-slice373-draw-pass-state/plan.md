---
title: "Slice 373 — draw-pass state steps once per frame, in the world (i.ad() bubbles, row band, jc20 eZ)"
phase: "port"
status: "done"
slice: 373
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/k.javap.txt:14699
  - reconstructed-project/src/structured/k.java:859-867
  - reconstructed-project/src/structured/k.java:1208-1306
  - reconstructed-project/src/structured/k.java:2679-2690
  - reconstructed-project/src/structured/k.java:2904-2934
  - reconstructed-project/src/structured/k.java:5985-6013
  - reconstructed-project/src/simple/k.java:3740-3749
---

# Slice 373 — the renderer stops stepping game state

Found while checking the renderer for writes into the world (slice 368
removed its `kCe/kCf` writes). In the original a frame is one `paint`: the
logic and the draw run once per 62 ms tick. LibGDX renders at the display
rate, so anything the port stepped inside the renderer ran 3–4× per tick
on a 60 Hz screen (more on 120 Hz) and not at all in headless tests.

## Findings (proven)

1. **`i.ad()` (speech bubble) has one call site**: `b(false)`'s entity
   loop, `if (z2 == 0 && ((ax != 11 && ax != 17) || aB > 0)) ad()`
   (bytecode k.javap.txt:14699, `b(Z)` offset 2118; structured
   k.java:2927-2929; simple k.java:3740-3749 is the same loop). `b(false)`
   runs after `I()` on jC 8 and on every jC 21 frame (k.java:859-867) and
   returns at entry on jC ∈ {12,13,31} (k.java:2682-2686).
   The port read it as two sites: a per-tick `tickBubble` in `tickNpc` for
   ax≠11/17 (visible or not) plus a renderer call per rendered frame. So
   bubble timers (`q[2]`, page advance, the `q[9]` teardown that sets the
   claimer's `cd[1]/cd[2]`) ran at the display rate on device, live
   soldiers' bubbles never stepped headless, and the renderer consumed the
   descriptor after one frame — the bubble showed on ~1 of 4 frames at
   60 Hz.
2. **Row band** (k.java:6005-6013): inside the hovered row's paint, before
   `L()`/`Q()`: draw a band of height `fI`, then `fI += fH; fH += 8;
   fI >= i4 → 0`. The renderer stepped it per rendered frame.
3. **jc20 `eZ`** (k.java:1289-1296): a story block taller than the 120 px
   window slides `eZ` up; `fd = eZ` reads it at cu4→5. The renderer wrote
   it (headless it never changed). The frame order is state machine →
   `eZ` → footer `a(d(0,16),d(0,18))` → `v(131072)`; the port hit-tested
   the footer first.

## Changes

- `Level0World.drawPassBubbles()`: the `b(false)` entity loop's `ad()`
  over the draw list, once per frame — after the world update on jC 8
  (before the claim footer and `J()`), on every jC 21 frame (the
  auto-dismiss test harness included). `bubbleDraw` is re-derived each
  pass and tagged with `bubbleOwner`; the renderer draws it after that
  entity for every rendered frame and never clears it. The `tickNpc` call
  is gone.
- `menuRowBandStep()` at the head of `menuFrame` (panel paint precedes
  `L()`/`Q()`), handing `menuBandDraw` (the height drawn this frame) to
  the renderer.
- jc20: `eZ` computed in `menuJc20` after the state machine; the footer
  hit-test moved after it, as in the original.
- Not changed (visual one-shots only, no game state): `veilVoid`,
  `fadeSolidFrame`, `menuFkArm` consumption; the renderer's own
  `buildDrawList()` call (idempotent).

## Tests

`Slice373Test` (5): a live soldier's bubble steps exactly once per tick
and owns the descriptor; a dead soldier's does not; the descriptor lasts
the tick and clears with the bubble; the band heights 1, 1, 9, 25, 0 over
five hovered frames; the jc20 slide with the real font. Full suite green
(capstones unchanged).

## Follow-up (PR #384 review)

The bubble state is the entity's own (`cQ`/`cR`/`cS`/`cT`,
i.java:18935-19010) and `ad()` draws inline after each owner's blit, so
several speakers can show a bubble in one frame. The world kept a single
`bubbleDraw`/`bubbleOwner` slot and the renderer drew only the last one;
`drawPassBubbles` now also fills `bubbles` (owner → descriptor, draw-list
order) and the renderer draws every owner's bubble. Test: two speakers in
one frame both keep their bubble (`Slice373Test`, now 6).
