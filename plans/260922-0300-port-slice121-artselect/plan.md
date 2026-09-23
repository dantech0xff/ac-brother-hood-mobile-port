---
title: "port slice 121 — i.a(Graphics) draw-path art select (aa.l / aa.a / aa.g)"
phase: port
status: done
---

## What

Ports the art-select preamble of `i.a(Graphics)` (i.java:3040-3180, proven):
the per-`ax` palette select (`aa.l(i)` → `aH`), the az[] object-remap select
(`aa.a(i)` → `aA`), and palette alpha (`aa.g(aH, alpha)`), applied before the
`(P&128)==0` blit. This closes the "draw-path select" gap flagged since
slice 119.

## Findings (confidence)

- `aa.l(i)` sets `aH` = **palette-table index** → our `e.palette`
  (b.java:1964). Proven.
- `aa.a(i)` sets `aA` = **az[] object-remap slot** → NEW `e.remapTable`
  (b.java:736). `az[ab]` remaps the frame's *object* index at
  b.java:926 (composite walk) and :1057 (bounds). Proven.
- `aa.g(i,i2)` sets palette alpha → NEW `e.paletteAlpha` (b.java:1977).
  Proven.
- Only two clips ever receive `az` tables (k.java:5044-5053): clip-0 gets
  pack-4's entries 0-3 (`eh={0}`, `ei={4}`); clip-52 gets pack-5's
  entry-0. Every other `aa.a(n)` selects an identity table — a no-op
  (b.java:714-720). Proven.
- Pack-4 decoded: all pairs remap into object **253** (blank module) —
  the tables are hide/bake masks. Pack-5: pairs into 178 + renumbers.
- Per-ax selects ported verbatim, including the side effects:
  - ax45 → `l(Z[0])`; ax30/32 → `l(0)` + `cG` blink (odd→`l(1)`);
    ax11 → `l(Z[0]∈{1,2}?1:0)`; ax47/17/73 → `l(0)`;
    ax68 → `l(af.ax==30?1:0)`; ax61 → `l(0)`; ax74 → `l(7)` on S3/4/5.
  - ax0/9/4/(ax67&&Z0==11) && !bh3 → `ce?l(1):l(0)`, then:
    - ax9: mission palette `{2:5,3:6,5:7,6:4}[aj]`; `Z[2]==47` candle —
      S4/S5 skip unless `k.bK` (`l(5)`), `ce&&S4&&r()` → `k.c(this)`,
      `ce&&S2&&r()` → `P|=64`, S5 pins to camera + `aC*255/10` alpha fade
      then `k.c(this)`.
    - ax0: `l(bo[bL][0]); a(bo[bL][1])` — `k.bo` =
      `{{0,-1},{3,1},{5,2},{6,3}}` (k.java:274).
  - ax43: vision-split — draws only OUTSIDE `cv.W`'s x-span (left of
    `W[0]` when `ak <= mid`, right of `W[2]` else) via scissor.
  - ax79: `a(Z[1]); l(Z[0])` — **correction**: `Z[1]` is the remap slot,
    not the palette (slice-119 minimap reads `Z[0]` as palette — `Z[0]`
    is the palette index, `Z[1]` the remap; consistent).
  - ax46: `a(Z[6]==0 ? Z[7] : -1)`.
  - ax29 (Cesare): `by∈{2,3}` → `a(0)` + palette-row blink
    `j[0]=j[1 or 4]` on `j.g%3`; else `a(-1)`. The `j[]` palette-array
    blink maps to our `-palette-NN` slots — **inferred** (same visual
    effect channel).
- ax10's `aU()` early-out and ax13's rope draw stay in their own arms
  (drawn before this preamble in the original — unchanged here).

## Changes

- `core/Clip.kt`: `remapTables` ctor field + `remap(table, obj)`; ACPK
  tail parse `u8 tableCount { u16 pairCount (u16 src,dst)* }` — backward
  compatible (older blobs end before the tail).
- `core/Entity.kt`: `var remapTable = -1` (`aA`), `var paletteAlpha = 255`
  (`aa.g`).
- `gdx/Level0Renderer.kt`: `drawEntity` gains a `world` param; the full
  select preamble replaces `palette = e.palette`; `drawObject` receives
  `clip.remap(e.remapTable, fd.module)` + alpha via `batch.setColor`.
- `tools/convert_slice1.py`: `clip_remaps()` decodes `archive/4` entries
  0-3 → clip-0 az[0..3] and `archive/5` entry-0 → clip-52 az[0]; emits
  the ACPK tail for every clip (identity-empty for the rest).

## Gates

- `verify-static-reconstruction.py` → `ok:true`; `python -m unittest` →
  57 pass; `:core:test` → green (incl. new `Slice121Test`: 4-table decode,
  →253 pairs, identity semantics); `:gdx:build`, `:android:assembleDebug`
  → green.
