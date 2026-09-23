---
title: "Slice 156 — i.y() travel-side wall flag + a() push-out guard"
phase: port
status: done
confidence: proven
---

## What

Ports `i.y()` (i.java:918-927) — the wall-contact query for the side an
entity is travelling toward — and wires it into the shared `a()`
solid-side push-out arm (i.java:749-758).

```java
// i.java:918-927
ag < 0            → bb        // moving left: left-wall flag
ag <= 0 && av     → bb        // stopped facing left: still bb
else              → bc
```

`Entity.wallOnFacingSide()` added (Entity.kt, next to `bb`/`bc`).

## pushOut fix (NpcFsm.kt)

The arm at i.java:749-758 requires `!k.aS.y()` before shoving the
player to the volume edge — previously stubbed "y()=false (inferred)",
so the push fired even when the player was wall-blocked. Now:

```kotlin
if (p.ak <= e.ak && p.ag >= 0 && !p.wallOnFacingSide())  → push left,  ag=-1
else if (p.ak > e.ak && p.ag <= 0 && !p.wallOnFacingSide()) → push right, ag=1
p.collideSides(w, true)   // a(true) — was missing entirely
p.ag = 0                  // L63, every overlapping tick
```

The missing `k.aS.a(true)` side-strip rescan+snap is also restored.

## Tests (Slice156Test, 3)

- `y()` truth table (ag<0, ag=0+av, right travel → bb/bc selection).
- wall-blocked player overlapping an ax41 prop: no push, `ag=0`,
  `v=true` (a(true) ran).
- unblocked player: pushed to `e.ak - pw/2 - ew/2`, `ag=0`.

## Noted (not fixed)

`probeSnapSides`/`collideSides` are duplicate ports of the same
`i.a(boolean)` (only one exists, i.java:829) — dedup backlog.

## Gates

verifier ok:true | 57 unittests | :core:test all green (+3) |
android+gdx builds OK
