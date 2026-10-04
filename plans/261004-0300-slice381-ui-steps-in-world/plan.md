---
title: "Slice 381 — per-frame UI state steps in the world, not per rendered frame"
phase: "port"
status: "done"
slice: 381
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:1027-1063
  - reconstructed-project/src/structured/k.java:1067-1088
  - reconstructed-project/src/structured/k.java:3085-3128
  - reconstructed-project/src/structured/k.java:3163-3175
  - reconstructed-project/src/structured/k.java:3450-3513
  - reconstructed-project/src/structured/b.java:1832-1837
  - reconstructed-project/src/structured/j.java:222-262
---

# Slice 381 — UI steps belong to the frame procs

Follow-up of slices 373/374 (renderer audit). The original advances
these once per frame inside its frame procs (`j.f` ≈ 62 ms); the port's
renderer advanced them per rendered frame (60 Hz → about 4x fast) and
never headless.

## Findings (proven) and changes

| State | Original | Port before | Now |
|---|---|---|---|
| Claim cards `i.bA[]` | `b()` after the entity loop: positions (200,160 / fan) and `b(j.f)` while `C.ab()` (k.java:3085-3117) | renderer wrote the positions and ticked 62 ms per drawn frame; script op 108 hit-tests the pointer against those positions, so headless runs saw stale ones | `Level0World.claimCardsStep()` in `bPass`; renderer draws |
| ax10 S31 lane cards | `aU()` draw arm `b(j.f)` (already in the world) | renderer ticked them again per drawn frame | renderer draws only |
| `cd[8]` pulse + banner | `cb[3]--` (floor 0) and `d(0,91)` "ASSASSINATION COMPLETE" at (200,120), palette 3; the `cb[3] > 15` frames use the 6-arg `b.a`, an empty stub (b.java:1832), so the banner shows on the last 16 of 20 frames (k.java:3117-3128) | renderer decremented per drawn frame; the banner never drew | world steps + `claimBannerDraw`; renderer draws the banner |
| `fS` "CHECKPOINT" marquee | case-8/21 tail, every play and dialog frame (k.java:1027-1039) | stepped on dialog frames only — stuck during play | `marqueeFS()` on both paths |
| Pause icon `fL` | `J()`: held → `a(30,1)` else `a(25,-1)`, `b(j.f)` (k.java:1041-1052) | renderer armed and ticked 62 ms per drawn frame | `pauseIconStep()` in both `J()` blocks |
| Load screen `dl` | `N()`: `b(j.f)`, from `j.g >= 165` re-seek to its third-last frame; `dl = null` at the jc9 exit (k.java:1071, 3485-3500) | renderer per drawn frame | `loadScreenN()` at the top of `menuJc9` |
| `N()` typewriter | `a(bW, d(0,51+eW[aj]))` on the shared `k.dj/dk` (k.java:3450-3470, 3508) | renderer kept its own `dj/dk` | `typewriterStep` (shared with `M()`); renderer draws `typewriterText` |
| `an` fade black frame | one frame (k.java:3171-3174) | renderer cleared it on its first drawn frame | the world clears it at the next tail step |

Left as is: the menu row arrows `fJ/fK` tick by the real render delta,
which matches `b(j.f)` with `j.f` = real elapsed ms (j.java:244-252).

## Tests

`Slice381Test` (5): card placement and one step per pass, the banner
frames, the marquee during play, the pause icon, the load screen. The
fade test checks the black frame lasts one tick.
