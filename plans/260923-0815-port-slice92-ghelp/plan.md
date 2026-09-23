---
title: "Slice 92 — G() jc5 help scroller + b word-wrap engine"
phase: port
status: complete
---

# Slice 92 — `G()` help scroller (jc5) + `b.a` wrap + wrapped-lines renderer

## Scope

Ports k.java `G()` (:2412-2488) — the 4-page instructions/scores scroller —
plus the full `b` wrap engine it depends on: `b.a(String,w,z)` (:1618-1760)
word-wrap into the `U[]` {end,width} pair table, `b.k(i)` (:1609) lines
height, `b.a(g,str,U,…)` (:1721-1769) the windowed wrapped-lines renderer,
and `k.a(bVar,str,w)` (:463-479) the '%' non-break punctuation pass.

## Proven (k.java unless noted)

- `l(5)` init (:1806-1823): `eE=0`; per page `cV[i5]=d(0,i5+47)`; i5==1 →
  `cW=y.k(a(y,cV[1],261)[0])` + padded `"\n\n"+…+"\n\n\n"+d(0,98)`;
  `s=a(y,cV[i5],261)[0]`; `eE=max(y.k(s))`; `cZ+=(s+7)/8`; `cX[i5]=(s+7)/8`;
  then `eE=y.k(11);eF=37+eE`. (stats fields eE/eF shared verbatim.)
- `G()` (:2412-2488): `cb=true`; `iK=47+(eE-y.k(1))/2`; chevron boxes
  `c(45,iK-15,50,30)`→`E(4112)`, `c(305,iK-15,50,30)`→`E(8256)`;
  chevron draw uses `d()` move-point (J,K) → frames 40/36 left, 39/35 right
  (clip 93); `a(y,1,cV[bw],200,iK,261,240,0,3)` viewport = wrap@261, 8 lines
  from `i3=8*(cY-1)`, align 3; page counter `(ΣcX[0..bw)+cY/cZ` @200,220
  anchor 33; footer `a("",d(0,17))`.
- Nav: `v(4112)` → `cY>1?cY-- : (bw=(bw-1+4)%4; cY=cX[bw])`;
  `v(8256)` → `cX[bw]>cY?cY++ : (cY=1; bw=(bw+1)%4)`;
  `v(131072)` → `cY=1; l(cy)` back to previous screen (kCy). `cy==14` art
  variant flagged unported.
- `b.a(String,w,z)` U-table: `U[0]`=line count, then {endIndex,width}
  pairs; bold latch z2/z4 restores pre-space bold at break (b.java:1618-1760).
- `b.a(g,str,U,x,y,i3,i4,i5,i6)`: draws `i4` lines from line `i3`,
  per-line align on own width `U[(i9+1)<<1]`, skips leading '\n',
  escape state persists across lines (b.java:1721-1769).
- `k.a(bVar,str,w)` (:463-479): ' ' before {'.','!','?',',',':'} → '%'
  (bV table :142) then `bVar.a(string,w,false)`.
- `pad E()` (k.java:553): `v(); eK |= i` — accumulate, not assign.

## Inferred

- Mid-frame `padE` injections commit next frame (62ms deferral vs orig's
  same-frame `eK` visibility) — the pad plumbing's `eK`→`bB` commit model is
  kept; documented, matching the deferred behavior of the existing pointer
  path.
- `d0` strings 47-50/98 added to bU from decoded pack-14 verbatim.

## Files

- `core/…/FontClip.kt` — `wrap`, `linesHeight`, `drawWrapped`, `drawChars`.
- `core/…/Level0World.kt` — `menuG`, `menuGIK`, `wrapPage`, `helpWrap`,
  `stateL(5)` arm, kCV/kCX/kCY/kCW fields, menuFrame route, menuFooter case,
  bU strings 47-50/98.
- `core/…/Pad.kt` — `eK |= mask` accumulation fix.
- `gdx/…/Level0Renderer.kt` — `helpScreen`, chevron `d()`→pointerMoveIn
  (also fixed in `scoreScreen`).
- `core/…/Slice1Test.kt` — `Slice92Test` (7), clip92 in test clips map.

## Gates

- verifier `ok:true`; `python3 -m unittest discover -s tests -t .` 57 pass;
  `:core:test` green; `:android:assembleDebug` green.
