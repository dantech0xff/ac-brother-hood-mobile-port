---
title: "Slice 254 — high-road pillars → checkpoint bot leg verified"
phase: golden-path traversal
status: done
---

# Slice 254 — high-road pillars → checkpoint

Bot test proving the spire→needle→east-run leg of the level-0 golden
path is traversable end to end with real input, no teleports.

## What the leg is

Parked on the spire base ledge (x1820-1860, top y400), the player must:

1. jump + face-grab the needle's west face (S101/S33 climb),
2. shimmy onto the needle top (S60/S62 mantle → S0 at (1890,199)),
3. walk east off the needle and keep running at y199 — a continuous
   top surface carries the run east past x2010,
4. reach the checkpoint region x2520-2700, al<540 (ax2 record at
   (2594,485)).

## Verified trajectory (observed marks)

`S233@1851,399 → S22 rise → S5 land → S12 run → S33 wall-climb
1859,303→261 → S60/S62 shimmy → S0@1890,199 → S12 run east at
y199 to x2010+ → checkpoint=true deaths=0 maxAk=2520 minAl=199`.

## Dead ends eliminated during iteration

- **Needle-top jump overshoots the ax22 void chain** — jumping east
  from y200 sails over the zone tops (y595-728); the low zones are
  built for a *falling* trajectory, not a jump arc. The working route
  never uses them: the y199 top surface runs straight to the
  checkpoint region.
- **Spire base (1850,399) parked clean** — earlier park attempts on
  y279/y319 embedded the player in S79 (aO>12 && aR>12 inside solid).
  Park = feet at top-1, verified by per-column top-cell dump.

## Bot grammar additions

- `stuck`-pulse UP gated to `ak < 1860` — at the needle top a 1-tick
  idle (S0, ag=0) must not trigger a jump; the correct move is walking
  east off the edge.
- Past x1910 airborne, direction released (held=0) — near-vertical
  fall only matters for the ax22 chain legs; here the surface run
  keeps east held anyway.

## Gates

- verifier `ok:true`; 57 unittests OK; `:core:test` green;
  `:android:assembleDebug` + `:gdx:build` green.
