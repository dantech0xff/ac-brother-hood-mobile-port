---
title: "port slice 104 — pack-2 A[] bank correction + N()/jc18/ag() art"
phase: port-slices
status: done
---

# Slice 104 — pack-2 A[] bank correction + load/title/poster art

## What (mined → ported)

**Bug fix (proven):** `A[]` is the pack-2 clip bank — `j.a("/2")` at
`k.java:4058` precedes the `J(0..5)` loads (`:4061-4075`). Our renderer
was drawing A[4] (dialog panel strip + portraits) from pack-3 clip4 —
the ax6 marker clip. Corrected all 3 sites to the new clip98
(pack-2 entry-004):

- `Level0Renderer.kt:230` — `i(i2,bP)` panel top strip `A[4].a(cd,12,0,0,bP)`
- `Level0Renderer.kt:762` — `A[4].a(cd,8+bL,0,22,30)` (k.java:4182)
- `Level0Renderer.kt:1033` — `A[4].a(cd,4+bL,0,378,bP+64)` (k.java:938)

New clip ids (converter + `Level0Game` loads): clip96=A[0] (title bg,
entry-000), clip97=A[1] (title art/E() backdrop, entry-001),
clip98=A[4] (entry-004, 13 anims: anim12 panel strip + poster frames
+ portrait anims), clip99=A[5] (N() icon, entry-005).

**New draw arms (unreachable-labeled):**

- `loadScreen` = `N()` (k.java:3472-3513, proven): black fill, lazy
  `dl` UiAnimObject(A[5],80,-40) `arm(0,-1)`; `j.g>=165` → `dm=165`,
  `dl.a(dl.a()-3)` freeze-frame seek + `d(0,9)` blink (`j.g%10<5`,
  align 17, bW); else `dm=j.g` + `bW.l(0)` `d(0,24)` at (395,230,40);
  progress bar `j.a(((dm<<8)/165)*300)` = `(((dm<<8)/165)*300+128)>>8`
  px at (50,205) color 7644855; `j.g>1` → tip typewriter
  `a(bW,d(0,51+eW[aj]))` + `y.a(d(1,0),null)` measure →
  `a(y,2,d(1,0), max(20,(400-b.d)>>1), 135, 360, 240,0,20)` — the
  9-arg overload drops i5/i6 (verbatim quirk) → effective
  align=20, limit=-1.
- `tipTypewriter` = `a(bVar,str)` (k.java:3450-3469, proven): moving
  `\2<char>\0` highlight bracket inside the FULL string (untyped tail
  still draws), `dj++`/frame, `dk=15` hold at end then restart.
- `titleScreen` = jc18 (k.java:1146-1157, proven): `A[1]` anim1 +
  `A[0]` frame0 + `A[1]` anim2; `d(0,9)` blink (200,205) `j.g%10>5`.
  Boot-flow only — unreachable in our jC8-first boot.

**Upgraded existing arms (stand-ins → real art):**

- `ag()` jc10 poster card: was a dark-plate stand-in; now the real
  `i(0,120)` dialog panel + `A[4].a(cd, i+4, 0, 200, 119)` with the
  verbatim `fP={0,2,5,7}` index loop (level 0 → i=4 → anim 8), brief
  via 8-arg `a(y,0,…,380,align=3,limit=-1)` (9-arg drop quirk), and
  `y.l(1)` so the blink draws palette 1.
- `M()` jc15 stats ribbon: was a gold-bar stand-in; now the real
  `a(i2,str)` proc — `A[3]` anim1 (200,i2) + anim2 (120,i2) +
  `j.b(87,i2+9,228,183)` dark panel (-14274509) + `bW.l(0)` title at
  align 3.

**Deliberately skipped:** `E()` (k.java:2313) is a painter helper that
composites `A[1]` into the dJ tile-cache during load — not a screen;
nothing displays dJ post-load. `i.a(IIIIZ)` minimap verified dead on
level 0 (the one ax35 record is S=6; minimap arms are S0/S1) — kept
deferred.

## Gates

- verifier: `ok:true` · unittest: 57 pass
- `:core:test` green · `:gdx:build` green · `:android:assembleDebug` green
- Render-side slice (gdx) — no new :core tests, matching prior
  render-slice convention.

## Provenance

`reconstructed-project/src/structured/k.java`: :299 (eW), :344 (fP),
:416/:938/:4182 (A[4]), :1146-1157 (jc18), :2313 (E), :2328-2336
(a(i,str)), :3450-3469 (a(bVar,str)), :3472-3513 (N), :3960-4082
(pack loads), :4114-4129 (a(bVar,…) 9/8-arg), :6358-6386 (ag);
`j.java`:310-312 (`j.a(i)` fixed-round); `a.java`:24-29 (ctor),
62-80 (a(i) seek / a() len); `b.java`:1336 (`b.j` no-op),
1900-1956 (font escapes `\0..9`, `_`, `^`).
