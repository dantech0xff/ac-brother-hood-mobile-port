---
title: "Slice 329 — bU completion + ON/OFF polarity + scroll clip intersect"
phase: "verify"
status: "done"
---

# Slice 329 — bU string-table completion + ON/OFF polarity + clip intersect

Run-28 device re-verify residuals traced to `bU` gaps and one polarity
inversion.

## Fixes

1. **`d(0,19)` = "DO YOU WANT SOUND?" missing** → jC=23 sound-prompt
   title (`l(23)` arm: `eC=19, K(3), eB=70` k.java:1799; ae() draws
   `d(0,eC)` :6221 — `eB` is suppressed for jc23 by the `j.c != 23`
   gate :6216) never rendered. Added verbatim.
2. **ff/fg={21,20} polarity inverted** (k.java:306, :6055) —
   `bE ? ff[1](20=ON) : ff[0](21=OFF)`; port had `if(kBE) 21 else 20` →
   MUSIC/SFX rows showed the wrong state (and 20/21 were missing →
   blank suffix). Fixed polarity + added ON/OFF.
3. **bU completion** — every remaining non-empty entry-000 string added
   verbatim (44 potion, 45-46 memory-block, 61 BOSS, 64 recharge,
   67-68, 74-76/118 chase banners, 81-82, 85-96 hints/QTE labels,
   100-102, 104-110 heroes, 111-113 banners, 117, 119-121, 123-126).
4. **Wrong strings corrected** — 69→"THE GAME DATA WILL BE PERMANENTLY
   DELETED. ARE YOU SURE?", 73→"ARE YOU SURE YOU WANT TO GO TO THE MAIN
   MENU?", 103→"VIP ZONE" (PLAYER LIST is 105).
5. **Scroll bleed** — scrolled row labels drew above the title strip
   because each row's own `j.a` clip covers its band. `clipScissor`
   now intersects `clipViewport` when armed; pills/highlights between
   rows were already clipped by the armed scissor.

## Test

`Slice329Test` — 5 tests: d0(19)/menuPrompt after stateL(23), MUSIC
row ON/OFF polarity both ways, corrected strings, chase/progress
banners, hero names + QTE labels. Gates: verifier ok:true, 57
unittests, full :core:test, :android:assembleDebug all green.
