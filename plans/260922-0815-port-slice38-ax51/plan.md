---
title: "Slice 38 — ax51 bs() pushable crate"
phase: port
status: done
---

# Slice 38 — ax51 `bs()` pushable crate

Port of `i.bs()` (i.java:16440) — the pushable-crate entity tick — into
`NpcFsm.tickPushable`, dispatched at `n.ax == 51`.

## What was mined

- `bs()` (i.java:16440–16616, **proven**): full label graph.
  - `L6` — `aS.S==284 → return`.
  - `L6/L9` — `ab()→aa()` claim-script step (stub `runClaimScript`).
  - `L9/L22` — carried-off release: `g.a==this && !overlap &&
    S∉{235,238,50,9} → g.a=null + G()`.
  - `L22/L30` — land-mount arm (`g.j` kills it): enter on
    `aS.al < W[3] || (ah>0 && g.c==this)`; inner requires `g.a==null`,
    `S∉{146,236,239,235,238,237,240,0,277}`, overlap, `S!=8`.
    - `L62` snap-mount (`S==16 || Q==16 || (al-g.y)/20 < 20 || g.h()`):
      vel0 + `i(0)` + `bq=0` + `g.a=this`.
    - else hard mount: vel0 + `al=W[1]+4` + `a(21,0,0,this)` fall damage +
      `g.a=this`.
  - `L64/L72/L80/L82` — br() gate: runs when
    `S∉{236,239,235,238} || (g.a==this && ac==this)`; `br()` → marker
    `a(1, ak, al-85)`, else `G()`. (L82 G() path — caught this slice.)
  - `L84/L94` — board arm: `g.a!=this && S!=8 && (ac==null||ac==this) &&
    overlap && S∈{236,239}` → `i(237|240)` + vel0 + `g.a=this` + side-clamp
    `ak±20`.
  - `L116/L126` — **edge** grab (`k.v` 16388|2|8 — unlike bm()'s held
    `k.u`): `(g.a==this || bq()) && S∉{238,235,239,236} && ac!=null &&
    av==(ac.ak<ak) && !f() && S∈{0,1,12}` → `i(235|238)` (the S237→238
    branch is dead code kept verbatim).
  - `L157` — carry: `g.a==this → t() + aS.ak += ag>>8` + side-pins on
    `aS.S∈{7,32,12,0}`.
  - `L175` S-switch — `S0/1`: `bo()/bp()` bookkeeping on `g.c`
    (set/clear `g.c`); `g.a==this` → off-overlap release (`P&=-17`) else
    top-carry (`ak+=ag>>8, al=W[1]+4`). `S2`: `G()` + off-overlap release +
    `r() → P|=64|32`.
- `i.bp()` (i.java:16288): `g.j && aS.S∈{43,35} && ah>0 && W[0]<aS.ak<W[2]
  && aS.al<=W[3]` → `crateLandSpot`.
- `i.bo()` (i.java:16274): `g.j && overlap && g.a==null` → `crateContact`.
- `i.m()` (i.java:16311): grabbable-eligible — ax51 `S∈{0,1,6,8}`, ax66
  `S∈{11,12,13}` (already ported as `grabEligible`).
- `i.n()` (i.java:16337): facing reach — `av` needs `r3.ak < p.ak`,
  `!av` needs `r3.ak > p.ak`, `|dx|<180` (140 for ax66), `|dy|<80`
  (already ported as `grabInReach`).
- `i.bq()` (i.java:16363): grab zone — ax51 `S==8`, ax66 `S==13`, plus
  overlap (already ported as `grabZone`).
- `g.c` static crate-top link → `w.gc`; `i.bq` static floor-Y latch →
  `w.iBq`; `g.h()` = `g.s||g.t!=0` → `w.gH()` backed by `w.gs`/`w.gT`.

## Subtle findings

- **`ac==this` is released every tick by `br()`** (L18): a self-claim can
  never reach the L126 grab arm — the surviving claim is always a
  *different* in-reach grabbable (either preserved via L13 or re-set by
  the `k.bd` scan at L61). Tests must claim a second entity.
- **L82 `G()`** runs when `br()` is false — the marker is released every
  tick the grab check fails (mirrors bm()).
- Decompiler label gaps flagged: `L192→L195` fallthrough inside L177.

## Verification

- `:core:test` — green incl. 12 new ax51 tests (S284 guard, walked-off
  release, S16 snap-mount + `iBq` clear, op21 hard-mount drain, `g.j`
  gate, S236→237 board claim, edge-grab S235 via a second claimed
  grabbable, no-claim block, carry shift+pin, S2 release+flags, `g.c`
  claim, top-carry snap).
- `verify-static-reconstruction.py` → `ok:true`; 57 unittests OK.
- Emulator boot: pending logcat check.

## Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt` —
  `tickPushable` + `crateContact`/`crateLandSpot`.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Entity.kt` —
  `gc`/`iBq`/`gH()` interface members + `runClaimScript` stub.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt` —
  overrides + `gs`/`gT` fields + `ax==51` dispatch.
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  `pushableAt` fixture + 12 tests.
