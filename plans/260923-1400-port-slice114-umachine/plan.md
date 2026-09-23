---
title: "Slice 114 — jC==21 `u` dialog machine (k.java:860-1065)"
phase: port
status: done
---

## What

Full port of the case-21 `u` (0-10) dialog machine into `dialogModal`
(k.java:877-1019, proven) + renderer draw block (Level0Renderer
`dialogModal` arm). Replaces the old `sawPressPending` dismiss stub with
the verbatim per-kind behavior:

- `j() → E(65568)` (:874): release-edge taps feed the context edge via
  `pointerStrip() → padE(M_CONTEXT)` — lands on `bB` the NEXT tick
  (original's `E()` folds `bB=eK` before `eK|=i` — same one-frame delay).
- `u∈{0,4,5,7}` full-screen panels (:878-904): press → `l(2|15|8)` +
  `z(23)` (u7→2, u5→15, else→8).
- `u∈{1,2,3,6,8,9,10}` line dialogs (:905-1019):
  - suppressed gate (:944) `v(131072)&&C!=null&&u==9&&C.cd[2]` →
    `C.Z(); cd[1]=true; bh!=3 → m(ad); z(23); l(8); v=w` (brace-verified:
    this `else` pairs with the suppressed `if`, NOT `v==w`).
  - typing `bQ && !A()` (:945-955): `bR++; bT=bR*bS/16; >len → -1`;
    `v(65568) → bT=-1` (reveal).
  - `u==10` (:956-961): press → `D(v+1); cz=true; z(23)`.
  - `else if (!v(65568) || u==8)` (:962-973): u8 countdown `x--;x<=0 →
    x=48; D(v+1); press → z(23)` — runs every tick for u8.
  - `if (v == w)` (:975-1007) — unconditional per-frame exit dispatch:
    u9 → `C.Z(); l(8)`; u3 → `aj!=7 ? l(15) : l(24)`; u1 →
    `aj!=8 ? l(8) : (aj=0; W(); l(2))`; u8 → `cz=true; l(8)`; else →
    `l(8)`. **`v != w` does nothing** — u9/u3/u1 have no `D(v+1)` arm, so
    multi-page u9 presses are inert (pages step via the claim script).
- `fS` tip marquee (:1027-1039): `d(0,111)` = "CHECKPOINT" crawls one
  char per two frames (`j.g%2`), resets `-1` at len+10; draws
  `y.a(str,390,40,10)` (renderer arm added).
- `J()` pause-icon arm (:1040-1063): `v(262144)` → `C.Y(); bw=0; l(14)`
  while `j.c∉{12,13}`.
- blink rule (:418-429): u8 never blinks; all other u blink `d(0,9)` at
  (200,220,align3) when `j.g%10<5` (renderer `dialogPanel` updated).

## Also fixed

- **`u` field unification** (correctness bug): `subU`, `dlgU`, `kMode`
  were three names for the same `k.u`. `kMode` was NEVER WRITTEN, so the
  `l()` tail `i==21 && u∉{8,9} → al` freeze gate was broken (always
  froze). Unified ALL → `dlgU` across world/renderer/tests: u8/u9
  dialogs now correctly tick the sim (`al=false`) instead of freezing.
- `bU[111]` = "CHECKPOINT" added (was missing → fS marquee had nothing).
- Skip tail `l(8)` now uses `stateL(8)` (was `leaveDialog`→`jC=kCy` —
  stale-jC resurrect risk when KO'd mid-dialog).
- `kDlgX` (u8 countdown, init 48), `kFS` (-1), `tipStr` fields added.

## Faithful semantics discovered (tests rewritten to match)

- `v==w` exits fire EVERY frame, no press needed — single-page dialogs
  exit as soon as armed.
- The `v!=w` else-branch at :1008 does NOT exist — that `else` pairs
  with the suppressed `if` (:944). `v!=w` = wait (script steps pages).
- `pad.e(M_CONTEXT)` (test edge inject) lands on `bB` same tick;
  DOWN+UP events take 2 ticks (release → `j()` → `E()` → next `v()`).
- Intro claim-script u9 dialogs (`v<w`) can't be dismissed by a tap —
  `autoDismissDialog` tests now arm `v=w` or use `pad.e` edges; the
  eat-a-tick-per-dialog behavior is genuine (script fires dialogs
  back-to-back).
- `kJT` (`j.i()` held-bits flush) eats the first menuFrame tick after
  entering jC==12 — tests drain it before pressing retry.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` all pass ·
`:android:assembleDebug` · `:gdx:build` — all green.

## Left

- `i(0,bP)` panel frame art `A[4]` sprite + `j.h(MIN_VALUE)` 400×68 fill
  — renderer has the layout/geometry; A[]/h art packs partially decoded.
- `d(0,9)` "NEXT" string for the blink prompt (bU[9] = "TOUCH THE
  SCREEN" — the orig's d(0,9) text may differ; string corpus check).
- `m(ad)` goto-label for suppressed skips — script VM's label jump is
  armed but the `ad` label resolution inside claim groups is inferred.
