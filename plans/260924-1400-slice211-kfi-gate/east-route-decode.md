---
title: "East traversal route — full et-layer decode (level 0)"
phase: analysis
status: done
---

# East route decode — shaft → poles → checkpoint

Cell map (et layer, 20px cells). Verified against records:

## Segment 1 — the shaft (x1740–1820)

- West wall face x1720–1740 (solid y520–820); shaft floor continues at y960.
- East face **x1820** — building A, solid y420–840, roof at y420, x1820–1920.
- ax10-S36 bound zone at (1734,600) rect [1734,600,1770,679] hangs INSIDE
  the shaft: `aE=1 → gk=1` carrier mode — a fall into it snaps the player
  to the zone edge (x1770) → S315 carrier pose → `al>go(679)` → S318.
- Intended ascent — ZIGZAG kicks, verified in `Slice212Test`: grab the
  x1820 face → L1a46 bounce (`ag=-2048` west, `ah=-5120`) arcs onto the
  WEST face x1740 (lip at y520) → re-grab higher → bounce east → re-grab
  higher still. Measured chain: y731 → y665 → y599 → landed the west lip
  y519 in 3 grabs (~66px gain per grab). Zone-≤4 directional hold arms
  `aF` (Pad zones 0–4 → `2 shl iJ`; zone-5 taps emit mask 64 and never
  arm `aF`).

## Segment 2 — rooftop gap A→B

- Building A roof x1820–1920 @ y420 → gap x1920–2120 (200px).
- '05' one-way strip at y800 spanning x1900–2100 = the catch platform.
- Building B west face x2120 (solid y480–840), roof at y480, x2120–2280.
- Kick chain on B's west face from '05' (y800 → y480) if the direct
  jump misses; wisps x1984–2140 y490–881 mark those kick-arc peaks.

## Segment 3 — poles to checkpoint

- Building B roof y480 (guard ax11 at (2213,472)) → jump east into the
  x2280–2540 air gap:
  - ax44 pole chain: (2383,657), (2432,657), (2479,657)
  - '02' one-way ledge x2360–2480 @ y640 (row 32)
  - ax22 capture zones (2064,695)/(1975,605)/(2104,546) — fall guards
- Building C west face x2540 (top ~y460); ax2 checkpoint at (2594,485).
- Corridor floor y960 also passes UNDER building B to x2380 (the
  x2120–2280 face occupies y480–840; open below y840 at x2280+).

The x2163 "dead end" the tester hit was building B's west face blocking
the y800-960 corridor — the corridor itself continues under it.
