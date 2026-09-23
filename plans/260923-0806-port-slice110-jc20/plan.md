---
title: "Port slice 110 — j.c==20 story-typewriter screen"
phase: port
status: done
---

# Slice 110 — `k.a()` case 20 (story typewriter)

## Source (proven)

- `k.a()` case 20 — `reconstructed-project/src/structured/k.java:1208-1306`.
- `l(20)` arm — `k.java:1824-1826`: `fb = y.a(d(0,27), 390)`.
- `y.a(str,i)` wrap-and-join — `b.java:1707-1718` (wraps at 390, joins
  with `'\n'`; the `i` arg is ignored by the call).
- Reachability — `Q()` slot/difficulty confirm `bv==2 → fF=20 → l(30)`
  (`k.java:3631-3639`) then `af()` confirm `fF==20 → l(20)`
  (`k.java:6270-6274`). i.e. NEW GAME → difficulty → browse → story →
  `l(9)` load → play.

## Semantics ported

`cu` sub-phases (verbatim from `k.java:1213-1277`):

- `cu0 → cu1` (cT=10 init).
- `cu1` waits `cT→255` (`z[39]` anim-1 icon); `v(262144)` skips →
  `cu=2; eY=200; eZ=85; fc=0; fa=""` (+`z(23)`).
- `cu2` typewriter: one char per frame appended to `fa`; escape chars
  {`\1`, `\2`, `\\`} consume the next char too. `fc >= len-1` or pause →
  `cu3; eY=200`.
- `cu3` slides `eY` 200→100 at −4/frame → `cu4; cT=10`.
- `cu4` waits `cT→255` (`z[39]` anim-10 spinner at 300,80) → `cu5; fd=eZ`.
- Tail: `a(d(0,16), d(0,18))` NEXT/SKIP footer; `v(131072)` (NEXT) or
  `v(262144) && cu==5` (SKIP once complete) → `l(9); z(23)`.
- `eZ` — the `b.e`-height arm (`k.java:1291-1293`): wrapped text taller
  than 120px shifts the draw origin up. Ported at `l(20)` as
  `85 - max(0, linesHeight(lineCount) - 120)` — `inferred` (b.e's exact
  accumulation isn't decompiled cleanly; the intent and bound are).

## Renderer

`storyScreen` — `f(false)` world behind; `z[39]` anim-1 icon at (0,eY)
for cu2..4, wrapped text `storyText()` at (5,eZ) (`fa` while typing,
`fb` from cu3), spinner anim-10 at (300,80) for cu4+. clip39 added to
the app clip map.

## Wrap helper

`wrapJoin(str)` = `b.a(str, i)` (`b.java:1707-1718`): `FontClip.wrap`
at 390 → rejoin pieces with `'\n'`, skipping boundaries that already are
`'\n'` (the `s2` check, verbatim).

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` (Slice110Test ×4);
  `:android:assembleDebug`; `:gdx:build` — all green.
