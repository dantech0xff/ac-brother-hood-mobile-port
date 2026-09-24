---
title: "Slice 211 — pause-entry k.fi gate (`if (!e.a()) fi = -1`)"
phase: port
status: done
---

# Slice 211 — pause-entry music-slot gate

## Source (k.java:5176-5183, proven)

```
boolean r0 = defpackage.e.a()
if (r0 != 0) goto L327        // e.a() → skip
defpackage.k.fi = -1          // !e.a() → fi = -1
L327:
defpackage.e.b()              // stop the track
```

`k.fi` = the current music-slot index that the RESUME arm dispatches on
(`fi==1 → z(1); fi==9 → z(9); fi==-1 → silent; else → B()` mission BGM).

The port wrote `kFi = -1` unconditionally at pause entry — so RESUME
always took the `-1` (silent) branch, dropping the paused mission track.

## Fix

```kotlin
if (!audioPlaying()) kFi = -1   // `if (!e.a()) fi = -1` verbatim
audioStop()                     // L327 `e.b()`
```

`audioPlaying()` is the port's verbatim `e.a()` (track live within its
`h.a[e]` duration window). Ordering preserved: `e.a()` evaluates before
`e.b()` stops the track.

## Tests (Slice211Test)

- pause entry with a live track → `kFi` kept (RESUME replays it)
- pause entry with no track → `kFi = -1` (RESUME silent)

## Gates

`:core:test` green, verifier `ok:true`, 57 unittests, `:gdx:build`,
`:android:assembleDebug`.
