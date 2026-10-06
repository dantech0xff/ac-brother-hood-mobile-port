---
title: Menu sweep audit — all jC states vs javap
phase: parity-audit
status: done
branch: devin/1791259576-jc5-help-backdrop
---

# Goal

User asked: "đã check lại tất cả menu chưa? Còn cái nào chưa làm không?" —
enumerate every menu screen state (k `jC` 0–34), verify each renders like the
J2ME original, fix divergences, report what remains unchecked.

# Method

- javap is authority: `reconstructed-project/bytecode/k.javap.txt`
  `a()` tableswitch @357 maps each `jC` → painter arm; each painter's
  `f(Z)` call sites determine whether `MENU_BACKDROP_STATES` must contain it.
- `f(false)` → `A[1].a(cd,1,0,0,0,0,0)` light-gradient backdrop;
  `f(true)` → no backdrop. `a(y,1,…)` internally `b.l(i2)`s fontY to
  palette-1 = pure BLACK text — only legible on that backdrop.
- Emulator verify: `stateL(N)` via jdb on GLThread 43 + screencap,
  crop dev[400:2000,60:1020].

# Findings

## FIXED — jc5 help screen invisible text
- `G()` else arm calls `f(false)` (@181) — backdrop required; port's
  `MENU_BACKDROP_STATES` lacked 5 → palette-1 black body text on black bg.
- Also removed a divergent `fontY.l(0)` before the page counter —
  G()'s only `b.l` is the cy==14 arm's `bW.l(0)` (@148), so counter is
  palette-1 in the original too.
- Verified on emulator: backdrop + 6 wrapped lines + chevrons + `1/6` render.
- New `HelpScreenWrapTest` pins the wrap/drawWrapped emit contract.

## Verified — no change needed
- Row-menus: jc2, 3, 14(×3 arms), 19, 23, 28, 29, 30 — pixel-verified earlier;
  backdrop arms `a()` + ae/af/ag/ah tables all contain their jC.
- Full screens: jc4 (`a(int,String)` self-backdrops @0-1), jc6, jc10, jc12/13
  (bPass+menuL+menuQ over gameplay), jc15, jc22 (fade-in medals), jc24
  (letterbox credits), jc27 (self-exit → jc2 when IGP absent), jc25
  (ABOUT-variant, backdrop verified live), jc31 (redirects → 13, restart
  confirm renders), jc20 story (kCu-gated fade/typewriter/slide/scroll),
  jc21/jc8 dialog, legal/boot (x=200 centered), jc18 title, jc9 load.
- jc17: `a()` arm @4064 = `b(false)` → transition-dim painter (`j.c`-gated
  rect fill), not a menu — correct that it's not in the backdrop list.
- Every `b.l(1)` (black-text) draw site confirmed to sit on a light surface.

## Not verifiable without destructive/deep flow
- jc11 exit-confirm → emits `Command.QuitApp` (destructive); code-read only.
- jc16/32/33/34: dead states that only consume ticks (dispatch).
- jc9 load: transient splash.
- jc1 hint: in-play overlay, verified earlier sessions.

# Evidence
- `/tmp/jc5_fixed.png` — help page readable on backdrop.
- `/tmp/jc25.png`, `/tmp/jc31.png` — ABOUT-variant + restart-confirm family.
