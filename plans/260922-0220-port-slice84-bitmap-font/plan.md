---
title: "Port slice 84 — bW/y bitmap-font renderer (s/measure/draw)"
phase: rewrite
status: done
slice: 84
---

# Slice 84 — `bW`/`y` bitmap-font text renderer

Ports `b`'s text path (proven): the game keeps TWO `b` clip instances as
fonts — `bW = J(1)`, `y = J(3)` (k.java:3966-3967). `J(i)` parses decoded
entry `i` of the open pack container; `j.a("/1")` opens pack-1 → **bW =
pack-1 entry-1 (167 modules, 244 glyph objects), y = pack-1 entry-3 (166
modules, ~244 objects)** — the charmap is shared: `sArr = j.f(2)` = pack-1
entry-2's `short[609]` hash table, `bW.a(sArr); y.a(sArr)` (:3974-3976).

## Decoded semantics (b.java, proven)

- `s(c)` (:1590): hash lookup `Q[c%228]` — base pair at [0],[1], overflow
  pairs from index 2 stepping 2; miss → 1 (fallback glyph). Exported as
  `generated/fonts/charmap.bin` (u16 buckets + pairs, duplicates kept).
- Metrics from the placement pool: `aj[obj]` = objPlaceStart; `i(g,0)` =
  `ar[aj[g]]` = the glyph object's advance; `j(0,f)` = `as[aj[0]+f]` = Y
  metrics. `N=-as(0,0)`, `J=N+as(0,1)`, `K=as(0,2)-as(0,1)`, `L=advance of
  s(32)` (:1584-1587). Verified on the real clip: `s('A')=39` → object 39
  ar=9px advance; obj 0 = the metrics object (3 placements, as=-11/...).
- `a(Graphics,str,x,y,align)` (:1839-1962): `i5=y+N`; align mask 43
  (8 right `-d`, 1 center `-d>>1`, 32 bottom `-e`, 2 vcenter `-e>>1`);
  `\_` underline toggle, `\^` bold toggle, `\<digit>` → `l(d)` palette,
  char 1 → `l(c)` if `c<k` (`255` restores aH), char 2 → literal glyph
  embed; space → `L` (+centered `s(95)` under underline); `\n` →
  `x=i4; y+=K+J`; bold draws twice at +1. **Glyph draw is a full OBJECT
  draw** `a(cd, obj, x, y, 0,0,0)` — glyph objects are 1-2-placement
  composites (placement-0 = aU:5 metric carrier module 97 holding the
  advance in `ar`).
- `a(str,cArr)` (:1772): measure pass — `d`=max line width (+1 under
  bold), `e=J` + `(K+J)` per `\n` — verbatim `if(d>0)d=d` no-op dropped.

## Ported

- `core/FontClip.kt` — `s()`, `loadCharmap()` (charmap.bin), `baseN/J/K`,
  `spaceL`, `measure(str)`, `draw(str,x,y,align,drawGlyph)` with the full
  escape/state machine (`O` underline, `f` bold, `aH` palette).
- converter: `clip91` = pack-1 entry-001-marker-130 (bW), `clip92` =
  pack-1 entry-003-marker-130 (y); aU:5 modules get empty-name slots
  (metrics carriers, not drawn).
- `Level0Game`: `clips[91]`/`clips[92]`; `Level0Renderer`: `fontW`/`fontY`
  (paletteCap 4), `drawText(str,x,y,align,palette,pack)` → glyph objects
  through `drawObject` — all jC==10/15/22 text call sites swapped from
  the placeholder BitmapFont (orig align codes 20/24/17/3/6 kept).
- renderer lookup fix: `drawObject`/`drawTileCell`/`moduleRegion` now
  accept both pack-key signs (`clips[pack] ?: clips[-pack]`) — entity
  clips (positive) and tilesets (negative) both resolve.

## Confidence

proven: charmap + metrics + draw semantics (b.java citations above);
high-confidence: entry→font mapping (J(1)/J(3) on the pack-1 container);
inferred: `pack` arg on `a(cd,…)` resolves object space of that clip only.

## Gates

verifier ok:true; 57 unittests; :core:test 306+8 green (Slice84Test:
charmap lookups incl overflow/fallback, pool metrics, measure, cursor,
newline, escapes, align); :android:assembleDebug green. Emulator glyph
visual comes with the first in-menu drawText slice (menu text still on
the placeholder font — out of scope here).
