---
title: "port slice 105 — G() completion: cy==14 frame + z[11]/z[54] arts + object-draw fix"
phase: port-slices
status: done
---

# Slice 105 — G() completion + the frame→object draw correction

## What (mined → ported)

**`drawFrame` fidelity fix (proven, b.java:907-913):** the 8-arg
`b.a(g, e, f, x, y, c, i6, i7)` resolves `frame → av` = an index in
the **object space** (`av[i8] | ((i[i8]&192)<<2)`), then calls the
6-arg object draw (:915) which walks composite placements (`ai[obj]`
count, `aj[obj]` start; `aq` bit-16 = nested object). Our `drawFrame`
was feeding `av` to `drawModule` — wrong for composite frames (drew a
module by object index). Now routes through `drawObject`, which falls
back to `drawModule` when `objPlaceCount==0` — identical for every
single-module frame already drawn, correct for composites (the z[11]
help art is one).

**`G()` completion (k.java:2412-2490):**

- `cy==14` framed variant — help opened from the jC14 pause menu:
  `b(true)` + `j.h(-856756498)` panel `j.d(57,10,285,eF+10)` + black
  title bar `j.b(57,18,285,18)` + `j.h(-2013265920)` 4 border rects
  (top 57,8,285,2 / bottom 57,eF+20,285,2 / sides 55 & 342 ×2 wide)
  + `bW.l(0)` `bW.a(d(0,6),200,20,17)` title + `f(true)` (world draw —
  render already composites over the world, equivalent).
- `bw==1` art pair: `iK2 = iK - y.k(iA)/2` (`iA` = the page's wrapped
  line count from `a(y,1,cV[bw],…)`), `z[11].a(cd,17,0,200,iK2+20)`,
  `z[54].a(cd,0,0,200,iK2+y.k(3)+cW)` — `cW` = full-page wrap height
  (`kCW`, computed at screen-init), `z[54].h(0,1)` = palette variant 1.
  Verbatim quirk kept: the cy==14/else branches draw the SAME z[54]
  call (k.java:2454-2458 byte-identical).
- `z[11]`/`z[54]` = pack-3 `J(11)`/`J(54)` loads (k.java:4024-4029) —
  our clip11 (ax19 pickup)/clip54 (ax74 wisp); the help arts reuse
  those entity clips. clip11 loaded in `Level0Game`.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` · `:gdx:build` ·
`:android:assembleDebug` — all green. Render-side slice (gdx), no new
:core tests per render-slice convention.

## Provenance

`k.java`: :2412-2490 (G), :1836 (cy=j.c), :4024-4029 (z[11]/z[54]
loads + `h(0,1)`), :5790-5799 (l(14) pause routes), :4114-4131
(a(bVar,…) returns `s` = wrapped line count); `b.java`:907-951
(8-arg → object draw → placements), :1609-1611 (`k(i)` line height).
