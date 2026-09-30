---
title: "Speech-bubble render — consume i.ad()'s bubbleDraw channel"
phase: port
status: merged
---

# Speech-bubble render

## Gap

`i.ad()` (i.java:20624-20755) was ported as `tickBubble` — the cQ state
machine + geometry — but its draw ops were only recorded into
`w.bubbleDraw`, which no renderer consumed. NPC speech bubbles computed
panel rect + wrap + page fields and then drew nothing.

## Port (Level0Renderer)

- `drawBubble` consumes the descriptor inline in the entity draw pass —
  same position `ad()` holds in the original's draw loop
  (k.java:3740-3749); the slot is nulled after drawing so a stale
  descriptor can't linger.
- Panel: bh3 `k.f` = white `fillRect` + black `drawRect` at (x,y,120,h);
  non-bh3 = white `fillRoundRect` + black `drawRoundRect` at
  (x-20, y, 160, h) — the ported geometry's `w=120` field is the bh3
  rect; the non-bh3 panel is wider (160 at x-20) per L68.
- Tail: `i.a(IIIII)` (i.java:20767) scanline wedge ported verbatim —
  per y-scanline a black outline span + white fill between the two
  lerped edges, collapsing to the apex point. Arms: flip →
  (x+90,x+100)→apex x+110; else (x+20,x+30)→apex x+10; base at the edge
  facing the entity (y+h normally, y when `tailUp`); apex at ±10.
- Text: `fontY.wrap(b.text,120)` + `drawWrapped(pageStart, lines,
  align=17 HCENTER|TOP)` at (textX, textY) with `fontY.l(1)` — the
  `k.y.l(1)` variant select (i.java:20687).

## Fix in tickBubble

Descriptor now emits AFTER the L84 `q[4]` clamp — the original calls
`k.y.a(..., cQ[0], cQ[4], ...)` on the clamped page-line count.

## Gates

verifier `ok:true`; 57 unittests; 1541 `:core:test` (0 fail);
`:android:assembleDebug` builds.
