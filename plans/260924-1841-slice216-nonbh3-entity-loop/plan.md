---
title: "Slice 216 — k.I() non-bh3 entity tick loop (au/park gate, ax71 skip, trail push)"
phase: port
status: done
---

# Slice 216 — `k.I()` `bh[aj]!=3` entity loop arm

## What

Ported the non-bh3 arm of `k.I()`'s per-frame entity loop
(`reconstructed-project/src/fallback/k.java`, L215→L2d9 ≈ :8660–8780,
**proven**) into `Level0World.tick`. Until now the port ticked every NPC
unconditionally — this arm is the original's "parked entity" LOD gate.

## Verbatim structure (proven)

Per entity in `k.bb[]`:

1. `u()` — recompute `au`, the Manhattan off-screen LOD score
   (`i.java:1871`; already ported as `Entity.recomputeAu`).
2. `P & 256` → skip frame (suspend flag).
3. `au < 2 ? (P&32 && !P&16 → skip) : (!P&16 → skip)` — the park gate:
   off-screen entities freeze unless the director set `P|16`; on-screen
   entities (`au<2`) freeze only when `P|32` is set without `P|16`.
4. `ax == 71` → skip frame (ax71 entities are purely claim/script-driven).
5. `ag() → af()` — ghost-trail ring shift when `cU != null`
   (`i.java:61098/61043`; `hasTrail()`/`pushTrail()`).
6. `I()` + `ac.I()` + `ab.I()` — entity + linked entities tick.
   **Verbatim asymmetry**: unlike the bh3 loop this arm has NO
   `ac.ax != 10` guard — linked ax10 zones tick here.

No ay-slot machinery in this arm (that lives only in the bh3 loop).

## Test regressions → faithful fixes (24 → 0)

The verbatim freeze broke tests that spawned entities off-camera and
expected them to tick. Resolution, per FSM semantics:

- `keepLive(e)` helper (`P |= 16` — the director's own keep-live flag,
  `k.java L25f/L26d`) applied to fixtures/tests whose entities legitimately
  run off-screen: `ax54At`, `ax56At`, `ax60At` fixtures + 15 test sites.
- **ax11 owns `P|16` itself** — `aA==0 → P &= -17` every tick
  (NpcFsm alert-state arm, proven). `keepLive` is futile there; the faithful
  path is player/camera proximity: patrol test now pins the player beside a
  soldier so `k.m` keeps `au<2`; the finisher test snaps `k.O/k.P` to the
  soldier (also required because `v()` consults `au` — off-camera victims
  cannot be hit at all, verbatim).
- `ax22` W-rebuild: `ax22` init sets `P|16` itself; the test needed one
  `tick()` after `keepLive` so the `I()` shared tail actually ran.

## Also in this slice

- `Entity.recomputeAu` + `hasTrail`/`pushTrail` wired into the loop.
- `ProbeZTest` debug class removed after diagnosis (it revealed `P=1` —
  bit-16 cleared by the entity's own FSM — and the claim-suspension
  ordering that motivated `settleIntro` in affected tests).

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 OK
- `:core:test` → 1384 tests, 0 failures
- `:android:assembleDebug`, `:gdx:build` → green
