---
title: "Port slice 25 — g.J action-request producers (k.F + ax16 bb())"
phase: port-slice25
status: done
started: 2026-09-22
slice: 25
---

# Slice 25 — `g.J` producers: `k.F(aj)` input reset + ax16 `bb()` request markers

Slice 24 ported the `az()` consumers (`J&4` mount gate, `g`/`ci`/`i.at`
slots); this slice ports the producers so `g.J` is driven live.

## Source

- `k.java:4644` — `F(int r3)`: `i.bS=0; r3>0→i.j(1); g.I=1; g.J=0;
  g.g(f0do[r3])` — per-key input handling resets `J` to `f0do[key]`.
- `k.java:8384` — `f0do = {5,5,5,5,5,5,5,5,5}` — all 9 key indices → mask 5
  (bits 0+2; bit2 = the mount-request bit). Called per held-key frame
  (`ef[aj]==true` gate at k.java:1418) → `J=5` while any key held.
- `g.java:5266` — `g.g(int r3)`: `J |= r3` + `k.q()` (queue refresh,
  unmined).
- `g.java:5270` — `g.h(int r4)`: request/consume — `r4!=0` requires `J&r4`
  pending; sets `I=r4`, `k.at=1`, consumes `i.at.H()` when `i.at.ab.ax==16`;
  `r4==1`→true, `S!=38`→true, `S==38` re-consume + false.
- `i.java:14088` (`bb()`, dispatched at i.java:4977 `case 16 → L870`) —
  ax16 tick head: `S==30 → g.g(2)` + `k.aS.h(2)` + `i(91)` + `A(15)` +
  `E()` + `k.c`; `S==38` → bit 8; `S==39 → g.g(4)` + `k.c` (mount request).
- `i.java:3760` — `E()`: settle loop `al+=10` until `aR>=12||5||3`.
- `i.java:632` — `a(int[],int[])` rect overlap (already `overlapI`).
- `k.java:7372` — `A(r)` = `z(r)` = `e.a(r,false)` → `world.sfx`.
- `k.java:4608` — J-bit→`ar[]` queue drain (bits 0-4, excludes bit2/mount) —
  noted, not ported (queue consumers unmined).
- `k.java:6682` — `g.J = a(bA,24)` — J persists in the 49-byte save (noted).

## Ported

- `Entity.requestAction(mask)` = `g.g(int)`; `requestH(r4)` = `g.h(int)`
  verbatim (H() internals unmined → `consumeH()` marker +
  `consumedH` flag); `settleToGround()` = `i.E()` loop (guarded); fields
  `ab`, `consumedH`.
- `NpcFsm.tickRequestMarker` = ax16 `bb()` head (S30/38 hurt markers,
  S39 mount point); L21 tail (S15-24) unported — `unknown`.
- `Level0World`: `ENTITY_CLIP += 16 to 10` (bi[16]=10, clip10 newly
  converted — 42 anims/33 modules); `n.ax==16` dispatch; `pointerDown →
  player.gJ = 5` (F(aj) per-held-frame equivalent; cadence `inferred`,
  value proven).

## Map-namespace fix (real bug caught this slice)

`bi[]` values 10 (ax16) and 11 (ax19) collide with pack-15 tileset ids
{10,11,12} in the flat `clips` map — entity clips and tilesets are
separate namespaces in the original (pack-3 vs pack-15). Tileset keys are
now **negated** at load (`clips[-10..-12]`; `Level0Renderer`/`Level0Game`/
test maps updated). Without this, ax16 would have bound the tileset-10
clip.

## Gates

- `:core:test` — 90 tests green (7 new: J=5 input map, S39/S30 marker
  request+consume, no-overlap inert, `requestH` gating + H() consume,
  `settleToGround`, end-to-end ax16→az() ax72 mount bind).
- verifier → `ok: true`; `python3 -m unittest` — 57 OK.
- Emulator boot — `npcs=454` clean (ax16 records live in levels 2/5/6).

## Known gaps

- `k.q()` (J-queue refresh) + the `ar[]` request-queue consumers unmined.
- `i.at.H()` mount internals — `consumeH()` is a marker stub.
- `bb()` L21 tail (S15-24 dispatch) unported.
- `g.J` save-field (bA[24]) not yet written/read by the port's save.
