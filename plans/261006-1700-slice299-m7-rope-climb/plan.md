---
title: slice 299 — m7 post-duel climb via east-wall rebound + ax13 rope
phase: m7-capstone
status: done
---

# Slice 299 — m7 leg: post-duel rope climb

## Goal

After the boss-1 phase-win (`iBy=2`), drive the bot out of the dormant-boss
arena onto the shelf above (y1500) — the first leg of the post-duel chain
toward boss-2 `uid307@(1377,238)`.

## What the map actually does (proven by entity/et-grid decode)

- The west barrier is airtight: `ax27#309` fuse `P|4096` push-solid fills
  x1043-1060 y1660-1745; hanging tongue x1020-1080 y1560-1660 above it;
  floor y1740 below. Grabbing the tongue's east face (x1080) only reaches
  its bottom lip (y~1640) — pull-up lands mid-air; kicks bounce east with
  no opposing face to chain. Dead end.
- `ax5 uid351@(1193,1510)` is an S8 arrival-trigger claim zone on the
  shelf — not a carry-up.
- `ax13 uid37@(1423,1423)` is a `aG==4` door-linked rope: `bN` grows to
  `Z[1]=16` segments → hangs straight down 192px; grab sliver x1419-1427,
  y1567-1615; catch gate `p.O <= e.O + 3072*Z[1] + 16384` → `al <= 1679`
  (i.java:37300). Standing-jump head ~1620 misses the sliver bottom by
  ~5px — but the S33 wall-rebound off the arena's east wall x1500
  (S33→S92→S36 arc) reaches it.

## The proven route (trace)

`S12 run east → S33@1491 rebound → S92 → S36 kick arc → S326@1591,1423
rope-bound → retract to y1423` — al<1460 reached at t=77 of the climb leg.

## Changes

- `Slice1Test.kt` — `Slice298Test.mission7PostDuelClimb`: phase-1 duel
  driver (identical to `mission7BossPhaseWin`) then phase-2 climb driver:
  `bM!=null → M_UP` while rope-bound, airborne east-drift, east baseline
  past x1250, face-approach `M_LEFT+M_UP` west of it, stall pulses
  `16390|M_LEFT` (into face) / `16388` (up under rope) / `16398|M_RIGHT`
  (east hops). Assert `al < 1460`.
- Test-only slice — no production changes.

## Gates

- verifier ok:true, 57 unittests OK, `:core:test` green (~1525 tests).
