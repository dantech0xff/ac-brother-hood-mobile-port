---
title: "Slice 380 — case 8 / case 21 is one frame body"
phase: "port"
status: "done"
slice: 380
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:859-1063
  - reconstructed-project/src/structured/k.java:2507-2513
  - reconstructed-project/src/structured/k.java:350-372
  - reconstructed-project/src/structured/i.java:12432-12456
---

# Slice 380 — the case-8/21 body

Found while mining slice 376.

## The original (proven)

```java
case 8: case 21:
    if (!fy) {
        if ((j.c == 21 && u == 8) || j.c == 8) I();      // :861-863
        else if (bh[aj] == 3) H();                        // :864-866
        b(false);
        if (j.c == 21) { …; if (j()) E(65568); switch (u) {…} }   // :868-1019
        if ((aS.P & 512) != 0 || ((C == null || !C.ab()) && (j.c != 21 || u != 9))) {
            dg++; ap[2]++;                                // :1022-1026
        }
        fS marquee;                                       // :1027-1039
        if (J()) { pause icon; c(354,0,46,37) → E(262144); v(262144) → l(14) }
    }
```

- `u == 8` dialogs are the in-play tips: the ax10 S21 zone calls
  `k.b(8, 1+aj, aF, p)` + `l(21)` and sets `k.x = 48` (i.java:12432-12456);
  the world keeps running under them and the pages advance on the
  48-frame countdown.
- `H()` (k.java:2507-2513): behind any other dialog on a flying mission,
  the ax24 shots in S8/9/10 still tick.
- The switch runs whenever `j.c == 21` after `b(false)` — including the
  frame whose `I()` opened the dialog.
- The mission timer counts dialog frames (not u9 ones, not while a claim
  runs) and runs after the sim and the switch.

## The port (before)

A separate dialog block for jC 21 that never ran the sim (the world froze
under the tips) and no `H()`; the switch started the frame after the
dialog opened; the timer ran only on play frames, before the sim.

## Changes

`Level0World.tick()`: `if (jC == 8 || jC == 21)` runs `simI()` (the old
play-path sim, unchanged) or `simH()`, `bPass(false)`, `dialogSwitch()`
when `jC == 21`, then `frameTail()` (timer, marquee, `J()`).

## Tests

`Slice380Test` (5): the player walks under a u8 tip and not under a line
dialog; dialog time counts unless u9; a tip opened by the sim is
switched (its typewriter steps) in the same frame; a flying mission's
shot moves behind a line dialog. Fixtures: the u8 countdown test settles
the m0 intro first (its claim otherwise opens a dialog over the tip);
the bh3 dU-shift test clears the intro claim (now resumed in the frame
its dialog opens). Capstones unchanged.
