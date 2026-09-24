---
title: "East traversal route — full et-layer decode (level 0)"
phase: analysis
status: done
---

# East route decode — '05' bridge → B face → poles → checkpoint

Cell map (et layer, 20px cells). Verified against records + live play.
Revision 2: the '5' strip y800 spans **x1580–2100 continuous** — it IS
the bridge, not just a catch. B's west face is **x2200** (not x2120),
solid y480–980: the corridor y960 dead-ends INTO it — the corridor is
the trap/fail route, not the way through. Entry: checkpoint ax2
(1594,546) drops straight down onto the strip (one-ways catch from
above). Score breadcrumbs (ax67-S5 at x1580/1648/1717/1786/1856/1925/
1994, all y799) mark the strip as the intended path; an ax11 patrols
it at (1759,797).

## Segment 0 — entry (x1440–1580)

- West block x1440–1560 solid y640–800 (top y640); corridor floor y960.
- Checkpoint ax2 uid98 at (1594,546) — respawn drops ~250px onto the
  '5' strip at x1580.
- Pocket x1580–1720 y680–780 sits above the strip (shaft-side entry).

## Segment 1 — the shaft (x1740–1820, above '5')

- '5' strip y800 inside the shaft = its floor; corridor y960 is BELOW
  it (one-way — no climbing back up through it; S38's `aO==0` probe
  refuses the up-vault — faithful).
- West face x1700–1720 solid y540–680 (`21` climb cell at (1720,520)).
- East face **x1820** — building A, solid y400–800, roof at y380–400,
  x1820–1900.
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

- Building A roof x1820–1900 @ y380–400 → gap x1900–2200 all the way
  down to the '5' strip y800 (the catch).
- Building B west face **x2200** (solid y480–980), roof at y480,
  x2200–2280.
- From the strip east end x2100: jump + hold east → grab B's face
  (solid y480–960, no lips) → kick/climb to roof y480. Wisps
  x1984–2140 y490–881 mark those kick-arc peaks.

## Segment 3 — poles to checkpoint

- Building B roof y480 (guard ax11 at (2213,472)) → jump east into the
  x2280–2540 air gap:
  - ax44 pole chain: (2383,657), (2432,657), (2479,657)
  - '02' one-way ledge x2360–2480 @ y640 (row 32)
  - ax22 capture zones (2064,695)/(1975,605)/(2104,546) — fall guards
- Building C west face x2540 (top ~y460); ax2 checkpoint at (2594,485).
- Corridor floor y960 dead-ends AT B's face x2200 (solid to y980).

The x2163–2180 "dead end" the tester hit was B's west face blocking the
corridor — the corridor goes nowhere (trap route, guard patrol
x2142–2180). Recovery from the corridor = reload (can't climb back
through the '5' one-way).
