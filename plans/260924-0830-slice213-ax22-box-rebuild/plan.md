---
title: "Slice 213 — ax22 capture-zone box rebuild (I() preamble + L1f35 shared tail)"
status: done
---

# Slice 213 — ax22 `W` never populated: shared-tail port

## What broke

The east route through level 0 was unreachable: every `ax22` capture zone
spawned with `W = [0,0,0,0]`, so `aN()`'s `overlapI(p.W, e.W)` capture arm
could never fire. The (1214,636)→(1316,568) wall hopscotch and the
(2064,695)/(1975,605)→(2104,546) roof-B ascent were dead.

## Root cause (mined, proven)

Two source pieces our port skipped:

1. **`I()` preamble — `r0.b = 1` every tick** (i.java:15250). `b` is a
   per-tick box-dirty latch, not a persistent flag. Procs that manage `W`
   themselves clear it (ax15 all arms, ax60 head, ax66 ride states); the
   shared tail then refreshes everyone else.
2. **The L1f35 shared tail** (i.java:18904-18934) runs for EVERY dispatch
   arm — `if (b) t()` rebuilds `W` from the clip rect (clip14:
   `[-6,-10,34,33]` around the anchor), `av → P|=1 / else P&=-2` facing bit,
   `a(k.aS, P, W)` player push (gated `ax!=0`/`P&4096`).

Our `tickNpc` ran that tail only through `npcFsm.tick`'s default arm —
i.e. only for *unclaimed* types. Claimed-proc entities (ax22 and every
other dispatched type) never got it.

## Fix

- `NpcFsm.initAx22` — verbatim port of record-init arm **Le87**
  (i.java:9339-9381): `az=1`; fresh `int[4]` `Z` stays zero unless
  `r8[5]==0`, in which case `Z={0, r8[4], r8[7], r8[11]}`; `P|=512` →
  L1bea shared tail. Record dispatch now routes ax22 here instead of the
  generic `Z[i]=f[7+i]` fill (different semantics: uid319 gives
  `Z={0,0,1,1}` → 16396 east-vault + face-east, vs generic's
  `{1,10,-1,-1,1}` → same vault but keep-facing).
- `tickNpc` — `n.b = true` at head (preamble) + `claimed` flag so the
  shared tail (`refreshBoxes`, facing bit, `pushL897` via `defaultArm`)
  runs for every claimed-proc entity.
- Test `world()` map gains `clip14` (ax22 was clipless in tests; prod
  already loaded it at `Level0Game.kt:122`).

## Verification

`Slice213Test` — 4 tests, real level-0 records end-to-end:
- Le87 Z-fill semantics per record (`Z[0]=0`, `az=1`, `P|512`)
- `W` rebuilds to the clip-14 rect (34×33 around the anchor) via the real
  `w.tick` path — the direct regression check
- capture: player overlapping (1214,636) → snap + `p.S=65` + `e.S=1`
- vault edge `16396` → `i(19)` `ag=3328`/`ah=-3840` + zone reset to S0

Also folded in: `plans/260924-1400-slice211-kfi-gate/east-route-decode.md`
rev-2 (the VERIFIED ROUTE restatement).

## Notes

- The player-facing `pad` edge is consumed by an upstream entity's
  `k.v()` before the zone's own tick sees it in `w.tick`; the vault-arm
  test drives `tickZoneInteract` directly (same convention as slice-39).
- Collateral: `pushL897` + facing-bit now run for claimed types too —
  procs that set `P|4096` (NpcFsm:3195/3298/3313/3348/7506) previously
  never reached push.
