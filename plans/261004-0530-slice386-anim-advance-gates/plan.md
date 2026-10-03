---
title: "Slice 386 — i.s(): pause freeze and the dialog wrap tail"
phase: "port"
status: "done"
slice: 386
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:293-331
  - reconstructed-project/src/structured/i.java:2929-2941
  - reconstructed-project/src/structured/g.java:288-312
---

# Slice 386 — `i.s()`

Found while mining slice 383 (`N.s()`).

## The original (proven, i.java:293-331)

```java
if (j.c != 14 || aa == k.A[3]) {
    if ((P & 64) != 0 || aa == null || U < 0 || V > 0) { if (V > 0) V--; return; }
    … advance U/T along the frame durations …
    if (T >= aa.b(S)) {
        T = 0;
        if (j.c != 21 || k.u == 8) return;
        if (bh[aj] != 3) {
            aS.ag = 0; aS.ah = 0;
            if (!aS.aZ && (g.b(aS.S) || aS.S == 79)) { if (aS.S != 79) aS.i(0); aS.E(); }
        }
        if (k.C == null || ax == 67) return;
        P |= 64;
    }
}
```

On the pause screen only `k.A[3]`'s anims advance. A cycle that wraps
during a dialog other than u8 stops the ground player, settles an
airborne `g.b(S)`/S79 one (`i(0)` unless S79, `E()`), and — with a claim
bound — freezes the entity on frame 0 until its state changes (`i(n)`
clears `P&64`). Since slices 374/375 the b() pass (which calls `s()` on
the linked FX, `k.E`, parked entities) runs on pause and dialog frames,
so both gates matter.

## The port (before)

`Entity.advanceAnim()` had neither gate: behind the pause menu and
through claim dialogs everything kept looping.

## Changes

`advanceAnim()` checks `hostWorld.jC == 14` against `clipA3` (clip 95)
and calls `hostWorld.animWrapped(e)` on a wrap; `Level0World.animWrapped`
is the tail. `dlgU` and `clipA3` are on `LevelCellSource`.

## Tests

`Slice386Test` (3): no advance on the pause screen; a wrap under a claim
dialog freezes the entity and stops the player; play frames and u8 tips
loop normally.
