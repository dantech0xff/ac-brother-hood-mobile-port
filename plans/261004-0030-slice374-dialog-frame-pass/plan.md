---
title: "Slice 374 — the dialog typewriter and the dialog frame's b(false) pass run in the world"
phase: "port"
status: "done"
slice: 374
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:859-867
  - reconstructed-project/src/structured/k.java:905-957
  - reconstructed-project/src/structured/k.java:446-448
  - reconstructed-project/src/simple/k.java:3450-3760
  - reconstructed-project/src/structured/i.java:2957-3125
---

# Slice 374 — no game state stepped by the renderer (part 2)

Continues slice 373's audit of the renderer.

## Findings (proven)

1. **Typewriter.** The case-21 line-dialog frame draws the page with `bT`
   and then, unless the skip gate fires, `if (bQ && !A()) { bR++;
   bT = (bR*bS)/16; bT > len → -1; v(65568) → bT = -1 }` (k.java:933-957;
   `A()` = `bT == -1`, :446-448). The port's world handled the press but
   the step (`dlgTypeTick`) was called by the renderer per rendered frame:
   text typed 3–4× too fast on a 60 Hz screen and never advanced headless.
2. **`b(false)` on dialog frames.** `b(z2)` (simple k.java:3450) builds the
   draw list (with the visible NPCs' linked-FX `ae.s()`) and calls every
   entity's `F()` (structured i.java:2957: the art-select with the cG hit
   flash, ax9 `ad = null`, the candle `P|=64`/fade/`k.c()` removal, parked
   `s()` advance) before the `ad()` bubbles. Case 8/21 calls `b(false)` on
   every frame (k.java:859-867), so all of it also runs behind a dialog.
   The port ran that pass (`drawStylePass`) only on play frames.
3. **Duplicated `F()` mutations.** The renderer's art-select re-ran `F()`'s
   writes per rendered frame on top of the sim-side `drawStyleF`: the cG
   flash counted down 4–5× per tick and the candle removal / `P|=64` /
   `ad=null` ran from the draw code.

`F()`'s player arm (`g.t--`, the RNG sparkle field) is gated on jC 8
inside `drawStyleF`, so running the pass on dialog frames does not move
the shared `j.j` RNG stream.

## Changes

- The dialog block steps the typewriter (`dlgTypeTick`) once per frame in
  the typing arm; the renderer draws `dlgBT`.
- The dialog block runs `drawStylePass()` then `drawPassBubbles()` on every
  jC 21 frame (auto-dismiss harness included).
- The renderer reads `palette` for ax30/32 and drops the ax9/candle
  writes; `drawStyleF` owns them.

## Not changed (recorded)

`b(true)` also runs the build + `F()` pass on jc 10–14 and 30/31 frames
(pause, death/loading, mission end — k.java:1110/1123/1454/1657/1792); the
port freezes those effects there. User-timed or transitional screens;
left for a later slice if replay parity across pauses is needed.

## Tests

`Slice374Test`: five dialog frames → `bR == 5`, `bT == (5*30)/16`, then
revealed without advancing the page; an ax30's cG flash steps on a dialog
frame. Both fail on the old code. Full suite green.
