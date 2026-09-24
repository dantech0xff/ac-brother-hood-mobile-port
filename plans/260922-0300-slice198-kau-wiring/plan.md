---
title: "Slice 198 — k.au difficulty index wiring + stale-comment sweep"
phase: port
status: done
---

# Slice 198

- **`bu`/`bv` difficulty indexing wired** — `initSoldier` `e.aB = bu[k.au]`
  (i.java:2230) and `initAx17` `e.aB = bv[k.au]` (i.java:3011) now index
  by `w.kAu` (load `kBA[8]%3`, save, menu cycle already wired). Was a
  fixed `[0]`; hard difficulty now gives soldiers 500/ax17-200 HP.
- **Stale "unmined" comment sweep** — five labels corrected to their now-
  proven/coded paths: `i.Y` bounds row (`aa.d(S,T)` = `av|(i&C0)<<2` →
  ak_or_al quads, already ported in `refreshBoxes` L22f arm), `i.cu`
  consumers (L109 early-out wired slice 197), `k.l(15)` → `stateL(15)`,
  `k.E` producer = ax71 overlay spawn (Level0World:915), `k.bh[k.aj]==3`
  = `MISSION_BH[kAj]==3`.

## Gates

verifier `ok:true`; 57 unittests; `:core:test` green (Slice198Test: bu/bv
index by kAu).
