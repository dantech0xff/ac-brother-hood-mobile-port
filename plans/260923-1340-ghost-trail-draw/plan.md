---
title: "Slice 174 — ghost-trail render arm (i.ah())"
phase: port-slice-174
status: done
commit: pending
---

# Slice 174 — `i.ah()` ghost-trail draw

## What

The last unported piece of the afterimage-trail feature. The sim side
(`cU` ring buffer, `startTrail`/`pushTrail`/`hasTrail`/`endTrail`) was
already ported; this slice adds the draw arm plus the card-clock fields
it needs.

## Source decode (i.java:19605-19616 `a(z2,i)`, :19619 `af()`,
:19633 `ag()`, :19648-19683 `ah()`, a.java `a` card, k.java:2578 tick
site, k.java:2916-2921 draw site)

- `a(z2,i)` arms 5 `a` cards: dot0 at `(ak,al)`, dots 1-4 parked
  `(-200,-120)`; each card plays anim `e = S`-at-arm, loop `h=-2`
  (infinite). `cV=true` and `cW=0` are **hardcoded** — the `z2`/`i`
  params are dead.
- `af()` ring-shifts `cU[i+1] = cU[i]`, writes `cU[0] = (ak,al)` —
  called per entity tick at k.java:2578 when `ag()` (armed).
- `ah()` draws, per live dot
  `(cU[i].a != ak && cU[i].a > -200) || (cU[i].b != al && cU[i].b > -120)`:
  - `card.b(j.g)` — ticks the card's ms accumulator (`a.g += i`); frame
    `f` advances when `g >= d.a(e,f)*40` (40ms per duration unit,
    `a.java:60` + static `j=40`), wrapping forever (`h<0`).
  - `cV` → `d.g(cW, 255*(100-i*20)/100)` — palette-slot-0 alpha ramp
    (255/204/153/102/51). The `d.h(cW, i+2)` palette-remap branch is
    unreachable (cV always true) — documented, not ported.
  - `d.l(cW)` — selects palette slot 0 for the fade (the card draws
    anim `e` from its own fields; `l(cW)` only matters for the `g/h`
    palette indexing).
  - `card.c |= 1` if `av` else `c &= -2` — flip bit only.
  - draws at `(cU[i].a - k.O, cU[i].b - k.P)` — world minus camera.
  - tail: `d.g(cW,255)`/`d.h(cW,1)` restores palette row 0.
- Draw site: k.java:2916-2921 — inside the per-entity draw dispatch,
  AFTER the entity blit and `ad` overlay, before the `E` companion —
  exactly where the port's stale `unported` comment sat
  (Level0Renderer.kt:1106).

## Port

- `Entity.kt`: `trailAnim` (`a.e` = armed S), `trailClock` (`a.g` ms
  accumulator, +1 per `pushTrail` — `j.g` is ~1 in gameplay: inferred
  on the rate), `trailFrame()` — walks `dur*40`ms thresholds through
  `trailAnim`, wrapping (`h=-2` semantics). Ghost dots sit on frame ~0
  for the trail's short life — faithful.
- `Level0Renderer.kt`: `drawGhostTrail` at the verbatim call site —
  per-dot `frameDraw(trailAnim, trailFrame(), av?1:0)` → `remap` →
  `drawObject` at `cU[i]-cam` with the 255/204/153/102/51 alpha ramp.
  Dots draw palette 0 (row cW=0 is the fade target; entities on non-0
  variants → `inferred` note in code). No palette mutation needed —
  alpha is per-draw.
- Tests (`Slice174Test`, 5): arm fields, ring-shift+clock, dur*40
  threshold walk + wrap, re-arm no-op, end-release.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` ·
`:android:assembleDebug` · `:gdx:build` — all green.
