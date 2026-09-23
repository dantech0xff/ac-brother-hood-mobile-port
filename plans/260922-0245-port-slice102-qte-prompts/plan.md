---
title: "Port slice 102 — i.bA[] QTE prompt-card render"
phase: port
status: complete
---

# Slice 102 — `bA[]` script-prompt card render

## What the original does (proven)

`i.bA = new a[4]` (i.java:22336) — a static pool of class-`a` prompt
sprites spawned by claim-script ops 107/112 (already ported). The render
site k.java:3085-3117, gated on `C != null && C.ab()`:

- `C.cb[1] ∈ {0,1,2}` → single YES/NO card at screen (200,160).
- else `C.cc != null` → choice fan, all at y=160:
  `cc[0]==3 → x = 200+50*(i-1)`; `cc[0]==2 → x = 200+50*(i==1 ? 1 : -1)`;
  else `x = 200`.
- Each card runs `a.b(j.f)` (tick 62ms) then `a.c()`
  (a.java:99-114 — `d.a(g, e, f, a, b, c, 0,0)`, palette slot `k` when
  `k != -1` — no producer found, stays -1).
- Tail: `C.cd[8] && C.cb[3] > 0 → bW.l(3); C.cb[3]--` — palette-3 pulse.

## Port

- `ScriptPrompt` now wraps the existing `UiAnimObject` (the full
  class-`a` port from the menu slices): `a`/`b`/`e` delegate, `setState`
  = `anim.arm(s,f)` (verbatim `h = i2-1`), `attach(idx, clip)` binds the
  pack index + clip at spawn (op107/112 sites updated — `w.clipFor`).
- `UiAnimObject` gained null-`d` guards in `seek`/`len`/`frameDur`/`tick`
  (the original binds in `a(b)` first; our ops now do the same, the
  guard is belt-and-braces).
- `Level0Renderer.drawPrompt` + the `kC`/`claimActive` card block —
  single card or cc-fan, `tick(62)` + `drawFrame(clipIdx, e, f, a, b, c)`,
  plus the `cd[8] && cb[3]>0 → fontW.l(3)` fade tail.

## Gate results

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest` → 57 pass
- `./gradlew :core:test` → all pass (incl. op107/108/112/113 prompt tests)
- `./gradlew :gdx:compileKotlin :android:assembleDebug` → green
