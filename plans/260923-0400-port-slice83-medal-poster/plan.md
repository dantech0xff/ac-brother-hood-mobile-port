---
title: "port slice 83 — ag()/ah() poster card + medal viewer + IGP Z() fix"
phase: port-slice-83
status: done
---

# Slice 83 — `ag()` poster card, `ah()` medal viewer, `Z()`/`f.a()` correction

## Original facts (proven)

- `ag()` (k.java:6358) — jC==10 mission poster: `u=8`; `i(0,120)` card
  overlay (:415 — `A[4]` frame12 @y + `j.h`/`j.d` black fill; `u==8`
  suppresses the in-proc hint); `A[4]` frame `i+4` at (200,119), i =
  `fP` index of `aj+1` (default 4→frame 8); brief
  `a(y,0,d(0,110),200,150,380,240,0,3)`; `v(327712)||j()` → `l(15);z(23)`;
  `j.g%10<5` → `d(0,9)` blink.
- `ah()` (k.java:6392) — jC==22 medal viewer: title `d(0,113)`; panel
  (114,59,172,155) + rows (114,70+45i,172,40); `ex==3` → fixed 3 rows
  (`cc==2` → icon i3 + `y.l(2)`; else locked frame3 + `y.l(4)`) +
  `v(131072)` → `l(3);K(4);z(30)` + `a("",d(0,17))`; else compact
  `cc==1` rows (icon=i5, label `d(0,114+i5)`); `j.g<10` → fade
  `(10-j.g)*25<<24`; confirm → `cc 1→2` + `bA[130+i]` then
  `!fP-member||bA[15]==1 → l(15)` else `l(10)`.
- `fP = {0,2,5,7}` (k.java:344) — chapter-unlock thresholds (prior
  port had it mistyped `{1..8}` → fixed, Slice76 test updated).
- `Z()`/`f.a()` correction: `f.a()` (f.java:755) = IGP shop check —
  `(aE && g()>0) ? slot : -1`, `g()` counts `br[]` available store
  items (f.java = Gameloft in-game-purchase client, `bp[]` URLs
  `&ctg=CCTL`, `igp19` RMS). The port has no shop → `menuShopCheck()`
  = false → `m()` skips row 3 as on every non-IGP device. Replaces
  the mislabeled `menuSlotReady = hasSaveRecord`.
- `f(false)` (:6197) inside both procs = `A[1]` frame-1 backdrop draw.

## Ported

- Dispatch: `jC==10 → posterAg`, `jC==22 → medalAh` before the jC==15
  arm — full-screen procs replacing the entity sim (same as M()).
- Render fields: `posterVisible/posterFrame/posterBrief/cardOverlayY`,
  `medalVisible/medalTitle/medalRowIcon[3]/medalRowText[3]/medalRowDim[3]
  /medalRowCount`, `hintBlink`, `hintBack`, `screenFadeAlpha`.
- Renderer: jC==10 poster plate + brief + blink hint; jC==22 title,
  dark panel, icon-coloured rows, jG<10 fade, back/next hints.
  Procedural stand-ins for unported A[4]/z[73] clip frames (`inferred`).
- `j()` tap-equivalent = `sawPressPending` (`high-confidence`).

## Tests (Slice83Test, 7)

Poster fields/confirm-nav, fP frame index, compact rows + fade,
confirm persist `bA[130]` + nav 15/10, ex==3 rows/dim/back-exit.

## Gates

`:core:test` green, `:android:assembleDebug` BUILD SUCCESSFUL.
