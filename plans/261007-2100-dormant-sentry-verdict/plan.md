---
title: "Slice 322 — S152 posted-perch guard: dormant-sentry verdict tests"
phase: capstone-verdicts
status: done
---

# Slice 322 — posted-perch guard dormancy verdict

## Finding (verified empirically + decompile)

m2's wall-perch trio aw311/312/313 are ax11 record-stamped `S=152` with
`P=33` (posted bit-0 + dormant-park bit-5). The non-bh3 entity tick gate
(`Level0World.kt:4966-4973`; `k.java` L215 arm, proven) drops a `P|32`
sentry in **both** branches:

- `au < 2` → skip when `P&32` set without `P&16` (force-tick)
- `au >= 2` → skip unless `P&16`

So a parked sentry **never ticks** — the shared tail's `j()` damage
intake, `k()` kill driver and `aB()` melee cannot run → invulnerable and
unresponsive while parked. This is the same au-park dormant-sentry
family proven for street soldiers (slice 219), **by original design**.

The original's wake is an external `P |= 16` force-tick (i.java:2269+)
or `P&~32` applied by alert/director arms — never a direct `P&=-33`
clear in static code.

## Verdict on the "unfightable wall-perch guard" repro

Not a bug: the sentry was still parked. Once woken (`P&~32`) and the
camera arrives (`au < 2` — guaranteed in real play since `k.m()` tracks
the player), the guard joins the tick path and `jIntake` lands
blind-side strikes → it dies (verified: `aB 300→0`, `S=139` corpse).

## Tests added (`Slice318Test`)

- `S152 posted guard is dormant while parked — verdict` — 60 ticks of
  overlapping player swings leave `aB=300`, `S=152` (never ticked).
- `woken posted guard is beatable — verdict` — after `P&~32`, the same
  swings kill it within 800 ticks; player survives.
- Earlier `holds its post` test comment updated: it drives
  `npcFsm.tick` directly, bypassing the entity-level gate — live play
  only reaches the tail after a wake.
