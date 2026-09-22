---
plan: slice 48 — ax9 bM() contact/push block + a() push + l() overlay
date: 2026-09-22
status: shipped
confidence_legend: proven | high-confidence | inferred | unknown
---

# Slice 48 — ax9 `bM()` contact/push block

Port of entity type **9** (`bi[9]=47`, k.java:8442): the solid
contact block — pushes the player off its sides, rides ax51 crates,
spawns clip-31 fx overlays, and is the claim target for ax15 grapple
volumes. Level-0 spawns 3 records (one hidden S34 variant).

## What `bM()` is (i.java:21069-21196, proven)

- **Preamble** (`S<35 && Z[1]>0 && s==null`): resolve `k.q(Z[1])`; when
  the target is ax51/43 and `overlapStrict(Y, r0.W)` binds it to `s`,
  snaps `al = r0.W[1] - (Y[3]-Y[1]) + 5` and adds `r0.ag>>8` to `ak`.
  While `s.ax==51` it rides the crate: `az = s.az+1`, re-snap each tick.
- **Contact arms** `S ∈ {0,1,6,7,10,11,14,15}`:
  `overlapStrict(W, aS.X)` + `aS.S != 32` → `i(2)` then `a()`;
  otherwise just `a()` (pushContact).
- **Wind-down arms** `S ∈ {2,8,12,16}`: `aB=0`, anim-end → `i(3)`.
- **Settle arms** `S ∈ {3,9,13,17,38}`: `aB=0`, anim-end → `P|=32`,
  `P&=-17` (solid passive block, no contact).
- **S18** → `l(7)`; **S19** → `aG=10`, velocity slide
  `ak += (ae.ag>>8 + aG) / (slow-mo ? iAI : 1)`, `l(1)`;
  **S20** → same slide + `l(4)`, anim-end → `i(19)`;
  **S21** → `ad=null`, anim-end → `i(22)`; **S22** → `ad=null`;
  else → `L67` (no-op arm).

## `a()` pushContact (i.java:914-993, proven)

Early-outs: `S==139`, `aS.S==6&&aS.ax==11`, `g.a.ax==43`,
`S==18&&aS.S!=12`, `S∈{131,146}`→L25.

Body L29: `overlap(aS.W, W)` gate → `g.a!=null` out →
**ax15 grapple arm** `(S==6||8) && g.b(aS.S)` → `aS.i(209)`, `a(null)`,
`aC=0`, `ak` snap to nearer edge, `al=W[1]+1`, `g.a=this`.

Left snap L50 (`aS.ak<=ak && aS.ag>=0 && !y()`): `ai=0, ag=-1` → L63
`a(true), ag=0`. Right snap L57 (`ak<, ag<=0, !y()`): `ag=1` → falls to
the L25 loop — while `aS.S==12 && aS.g(this)` (player still in front,
climbing) the body repeats. `aS.S>43` skips everything.

## `l(int)` overlay (i.java:21198, proven)

`ad==null` → `a(43, 31, r8, az+1)` spawn, `ad=aK`; then
`ad.i(r8)`, `ad.ak/al = this.ak/al`, `ad.T = this.T`. Clip-31 ax43
child, NOT inserted via `k.b` — pure overlay.

## Init arm (i.java:2663 `case 9` → L50 at :2781)

`aB=10; az=99`; `r8[5]==0` → `k.aV=this` then shared `i(r8[5])`;
`r8[5]==34` → `P|=512, az=99` (hidden variant, skips Z load); else
`Z=int{0, r8[7], k.bn[r8[8]]}` (`k.bn={47,72}`), `aG=0`. All paths end
at shared `i(r8[5])` + `t()`.

## Converter fix — non-pixel modules

Module with `runtime_type_aU==2` emits zero PNGs by design (marker/
vector group, e.g. module 115 in clip47). `pack_clip` now keeps an
empty-name module slot (`("", w, h)`) instead of dropping it — keeps
module indices aligned; `Level0Renderer` skips empty module names.

## Port wiring

- `Entity`: `overlapStrict(a,b)` (:632-664 — strict 4-edge overlap
  rejecting point rects), `pushContact(world)`, `adChildOverlay(world,
  r8)`; `ac` P|256 setter; `ga`/`gc` hooks.
- `NpcFsm`: `K_BN = {47,72}`, `initAx9`, `tickAx9` — all arms
  transcribed 1:1.
- `Level0World`: `ENTITY_CLIP[9]=47`, init/tick dispatch.
- Clips converted: `clip47` (ax9), `clip31` (ad-child); renderer
  when-maps + `Level0Game` loads.

## Record-inventory

| aw | pos | r8[5] | role |
|----|-----|-------|------|
| 108 | 6177,758 | 1 | contact block, `k.aV` |
| 933 | 7000,700 | 8 | contact block |
| 980 | 6842,1106 | 43 | hidden (P\|512) |

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest` → 57 pass
- `:core:test` → 300 pass (12 new `Slice48Test` arms)
- `:android:assembleDebug` green; emulator boots, window focused

## Known limits

- `S==34` hidden variant renders nothing (P|512) — draw skip not yet
  wired in renderer (entity ticks fine; visual skip is `inferred` next).
- ax15 grapple arm verified in unit test only — no ax15 record exists
  in level 0, so the path is untested on device.
- `bM` arms for `S∈{4,5,…}` beyond the contact/wind-down/settle sets
  fall to `L67` as in the original (no-op tail).
