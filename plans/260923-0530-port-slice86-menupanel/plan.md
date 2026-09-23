---
title: "Slice 86 — b(x,y,w,z2,z3) menu panel proc"
phase: port
status: done
---

# Slice 86 — the `b()` menu panel draw proc

Ports `k.b(i,i2,i3,z2,z3)` (k.java:5903-6150, proven) — the menu/stats
panel used by every `j.c` screen: edge-band art, translucent fills, row
loop with selection pill, fit-scroll text, two-column layout.

## Ports (proven, file:line)

- `b(i,i2,i3,z2)` forwarder `:5868` → `b(i,i2,i3,z2,true)`.
- `a(i,i2,i3,z2,z3)` edge band `:5872`: frame pick
  `z3 ? (z2 ? 16,17 : 14,15) : (z2 ? 12,13 : 10,11)`; `fM/fN` = frame-0
  module dims; cap + `do{draw i7; i7+=fN}while(i7+fN<x+w)` + mirrored cap.
- `b(i,i2,i3,z2,z3)` `:5903`: lazy `fJ`/`fK` (`a(18,-1)`/`a(21,1)` on
  `A[2]`); `i10=min(8,ey)`; `i9=i2+10` (+40 when z3); fills
  `-856756498` panel, `-2013265920` bevel bars, `805306368` strips,
  `1879048192` selection, `-16777216` black dim; row loop `i4=35` iff
  `i13==0&&j.c==2` else 30; `i5=135` iff `(bv==4&&j.c!=14)||j.c==19`
  else 170; `i13==1&&j.c==2` → `i9+=13`; `zD=d(i,i9,i3,i4)` pressed
  test → fill+pill `a(cx,cy,i5,false,first)` + icons
  (jc30:i13+5 / first-jc2:9 / else 5) + `bW.l(0)`; unpressed → fJ
  arm 18/19 + fI-shimmer (`fI+=fH;fH+=8;fI>=i4→0`) + black `(i3+i5)>>1`
  fill + edge `a(cx2,cy,i5+4,true,first)` + icons (jc30:i13 /
  first-jc2:4 / else 0) + fK tick/rearm(20∞)/draw + `bW.l(1)`; clipped
  text at `i14-ez`; separators `(bv!=4&&j.c!=14)||j.c==19` at
  `i16=i10/2` (−1 even) → column 2 at `i=206`, `i9=i12-(i4+3)` +
  top strip; `i9+=i4+3` per row.
- `a(str,z2,i)` fit `:6335`: unpressed → truncate `length-3`+"..." while
  `b.d>i`; pressed&wide → `ez+=2; ez>i2 → ez=-i`; else `ez=0`.
- `strD` `:6043-6080`: `j.c==19` → `d(0,eA[bv][iM])` + decorations
  (32-34→`bW.l(3)`+z[12] blink; 83/84→`": "+ON/OFF`; 97→`": "+d(0,35+au)`;
  103→`l(3)`; 123→`": "+d(0,124+k()?0:1)`); non-19 → `d(0,10)+" "+(i13+1)`
  = "LEVEL n" (d(0,10)="LEVEL" verified table-0 entry-000).
- `a` class (a.java, 163 lines) → `UiAnimObject.kt`: `a(anim,loops)` arm
  guarded by `i||e!=anim`, `a(i)` seek modulo, `b()` done-check
  (`e<0→true; h<0→false; else i`), `b(ms)` tick, `c()` draw — verbatim.
- `A[2]` menu clip: `j.a("/2")` :4058, `A[0..5]=J(0..5)`; `A[2]` =
  pack-2 entry-002 → `clip93` (46 modules, converted); `z[12]` =
  pack-3 entry-012 → `clip94` (converted).

## Corrections to earlier inferred work

- Row hit-test now uses the verbatim `c(i,i9,i3,i4)` rects incl. the
  two-column overlap (`row0 x93..307` vs `row1 x206..420` — taps in
  x∈206..307 hit row 0 first, i.e. YES, matching the orig's draw order).
- j.c==2 chapter-select `i4=35` first-row + `i9+=13` second-row offsets
  preserved even though j.c==2 is currently unreachable.
- `fH/fI` shimmer arms consume the existing `kFH`/`kFI` world fields.

## Files

- `core/.../Level0World.kt`: `menuPanelY/menuPanelZ3/menuRowCount/
  menuI4/menuI5/menuI14/menuRowRects/menuRowText/menuRowSub`; `menuRowAt`
  resynced to `menuRowRects` + `pointerDownIn`.
- `core/.../UiAnimObject.kt`: new — verbatim `a` class.
- `gdx/.../Level0Renderer.kt`: `drawFrame`, `menuFj/menuFk/menuEz`,
  `fillAr`, `clipScissor/clipReset`, `panelEdge`, `fitText`, `menuPanel`;
  menu block now calls `menuPanel(...)` + prompt via `bW`.
- `gdx/.../Level0Game.kt`: `clips[93]/clips[94]` loaded.
- `tools/convert_slice1.py`: clip93 + clip94 entries.
- `core/.../Slice1Test.kt`: `Slice86Test` (7 tests); `Slice78Test` tap
  coords recalibrated to the verbatim two-column rects.

## Gates

- `python3 scripts/verify-static-reconstruction.py` → ok:true
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → green (Slice86Test + recalibrated Slice78Test)
- `:android:assembleDebug` → green

## Gaps (flagged)

- j.c==2 side soft-buttons at (10,167)/(10,204)/(354,204) with A[2]
  frames 22-31 — unported; j.c==2 unreachable until chapter select.
- `a(bVar,i,str,...)` marquee + footer `a(lbl,rgt)` — next slice.
- `ae()`/`af()` text-screen procs — pending.
- Clip scissor uses `glScissor` directly (panel-space rows only; the
  orig clips the whole row draw, ours clips text only — flag `inferred`).
