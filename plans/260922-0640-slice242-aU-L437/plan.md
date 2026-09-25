---
title: "Slice 242 — i.aU() L437: ax10-S31 claim-QTE zone draw arm"
phase: port
status: done
confidence: proven (i.java:32653-32840; j.java:2818/2844; a.java:196/215)
---

# Slice 242 — `aU()` S31/L437 claim-QTE zone draw

## Source arm (proven, i.java:32653-32840)

`case 31: goto L437` — the ax10 claim-QTE zone's draw:

- Gate: `a(W, aS.W)` player overlap `&& (P & 128) == 0` → else return.
- `r8 = 400/(aD+1)` column width; `r9 = r8-10` inset x;
  `r10 = 4-aD` card-slot offset.
- `n` latch checks: `m >= 10` → `bA[m-10].b() → n=1`; else
  `aA == Z[3]` → `k.bh[k.aj]==3 → n=1` or `bA[m].b() → n=1`.
- `Z[2] < 9999` → progress bar at (20,200): `j.b` cyan fill
  `0x33ebf4` `360*(Z[2]-aB)/Z[2]×10` + `j.c` white outline 360×10.
- Loop `r11<aD`: `bA[r11+r10]` → `a = r9+r11*r8`, `b = 160`,
  `b(j.f)` tick (62), `c()` draw.

## Port

- `aUDraw` (NpcFsm.kt:10471): verbatim S31 arm before the S34 arm.
- `j.b`/`j.c` verified (j.java:2818/2844): fillRect vs 1px drawRect
  — new `w.drawFxOutline` collector (`fxOutlines`) drained via
  `outlineAr`; `drawFxRect` doc corrected (it is `j.b` fill, not the
  `j.a` clip overload — ax43 stays setClip renderer-side).
- New `w.drawFxPrompt(slot)` collector (`fxPrompts`) → fxOverlay
  ticks+blits `scriptPrompts[slot]` via `drawPrompt(pr,62)` — the
  `a.c()` draw stays renderer-side; position writes + slot mark stay
  sim-side (F()'s draw-side split).
- `e.n` → `Entity.nl`; `bA[i]` → `Entity.scriptPrompts[i]`;
  `a.b()` → `UiAnimObject.stopped()`; `a.b(j.f)` folded into
  `drawPrompt`'s tick (one tick per armed slot per frame, verbatim).

## Tests (Slice242Test, 5)

Gate blocks (no overlap / P&128); armed draw emits
`{20,200,270,10,0x33ebf4}` + `{20,200,360,10,-1}` + slots `{2,3}`
at `(colW-10+i*colW, 160)`; `m>=10` stopped card → `nl=1` early;
`aA==Z[3]` bh3 → `nl=1`; non-bh3 needs `bA[m].b()`.

## Gates

verifier `ok:true`; unittests 57/57; :core:test all pass;
:android:assembleDebug + :gdx:build clean.
