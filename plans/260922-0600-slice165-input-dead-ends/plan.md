---
title: slice 165 — input dead-ends (jC21 soft-key strip, af() row-tap, jc11 exit)
phase: port
status: done
---

## Goal

Close the three touch dead-ends the emulator demo run surfaced: the
mission-intro `u9` script dialog undismissable by touch, the `af()`
mission-browser confirm that ignored row taps (+panel never drew), and
the `j.c=-1` ghost menu behind the EXIT item.

## Provenance

- `u9` dismiss is `v(131072) && C!=null && u==9 && C.cd[2]` → `C.Z();
  C.cd[1]=true; (bh!=3)→m(ad); z(23); l(8); v=w` (k.java:944-1010,
  proven). `131072` is the HARDWARE right soft key — live on every
  screen, independent of whether `a(str,str2)` draws a footer pill.
  Port adaptation (`inferred`): on jC==21 arm `padE(M_CYCLE)` for taps
  inside `(349,198,56,47)` — the same rect `a()` hit-tests for the
  right pill at default `cf=36` (k.java:2293-2309). The strip sits in
  the `k.j()` dead margin (`H>364`), so a tap there does not also arm
  the context edge — matches the original where the hardware key and
  `j()` are disjoint.
- `J()` pause icon runs on `j.c∈{8,21}` (k.java:2653, 1040-1063,
  proven). Order matters: the original arms `E(65568)` via `j()` at
  :874 then `E(262144)` via `J()` at :1054 — `E()` clears+latches
  (`clearLatches()` in `pad.e`, k.java:553), so the pause edge must be
  armed AFTER the context edge. Moved the jC21 pause arm from
  `consume()` into the dialog modal after `pointerStrip()`.
  Consequence: the edge lands `bB` on the next frame's commit — the
  same one-frame latency the original has (E() inside the frame, v()
  reads it next frame).
- `af()` confirm is `v(327712)` (k.java:6269, proven) — the confirm
  complex `M_CONTEXT | row-tap` (`E(32)` from the draw-loop row
  hit-test). `menuAf` now takes `pressY` and folds `menuRowAt(pressY)`
  into the arm exactly like `menuQ`; `panelVisible` gained `jC==30`
  (`d(93,46,214)` at :6254 — the panel was ported but gated off).
- `j.c==-1` = `A.notifyDestroyed()` (j.java:218, proven) — the EXIT
  path quits the MIDlet. `inferred` adaptation: `case 11 → jC=-1` now
  emits `Command.QuitApp` once; `Level0Game` drains it to
  `Gdx.app.exit()`.

## Files

- `rewrite/core/.../Level0World.kt` — consume() M_CYCLE strip arm on
  jC21; modal-side `pointerDownIn(354,0,46,37)`→`padE(M_PAUSE)` after
  the context arm; `menuAf(pressY)` + row-tap confirm; `panelVisible`
  += 30; case 11 emits `Command.QuitApp`.
- `rewrite/core/.../Commands.kt` — `object QuitApp`.
- `rewrite/core/.../TickEngine.kt` — hash arm for `QuitApp`.
- `rewrite/gdx/.../Level0Game.kt` — `QuitApp` → `Gdx.app.exit()`.
- `rewrite/gdx/.../AudioBridge.kt` — exhaustiveness arm.
- `Slice1Test.kt` — `Slice165Test` (6 tests): u9 strip-tap exit
  (resumeScript + cd[1] + v=w + l(8)), u9 ignores off-strip taps,
  left-of-strip boundary, pause icon → l(14) + cd[0], af() row-tap
  confirm + panel visible, jc11 → QuitApp once.

## Gates

- `python3 scripts/verify-static-reconstruction.py …` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → 1094 pass
- `:android:assembleDebug`, `:gdx:build` → green

## Still open

- The u9 `cd[2]` claim-wait also expects the claim script to advance
  `v` toward `w` across pages — `kC.aa()` still suspended under jC21
  (faithful). If a real mission dialog pages past one screen it will
  need the same strip tap to consume — covered.
- `af()` "LEVEL n" row labels are placeholders (`menuRowText` else
  branch) — real per-level names live in the string table; cosmetic.
