---
title: "Slice 137 — wall-run family (g.q/g.f/g.E statics + ax10 S10/S55 zones + S17/S102/S317/S332 arms)"
phase: port
status: done
---

# Slice 137 — wall-run family

Ports the wall-traversal set verbatim: the three shared statics that wire
it (`g.q` wall-run latch, `g.f` marker-FX ref, `g.E` jump-tail suppress),
the two ax10 zone arms that produce them (S10 → `g.q`/`g.d`, S55 → `g.E`),
and the four player arms that consume them.

## Mined source (all `proven`)

| Arm | Source | Behaviour |
|-----|--------|-----------|
| ax10 S10 zone | i.java:9343-9363 (`aV()` L1e1) | overlap → `g.q=1`,`g.d=this`; leave+still-owner → `g.q=0`,`g.d=null` |
| ax10 S55 zone | i.java:9818-9862 (L611-L651 head) | `P\|=128`; overlap → release stale `k.C`, `P&=~128`, `g.E=1`, `Z[0]!=0`→`i.cu`; leave → `g.E=0` |
| S17 wall-kick | g.java:1490-1497 | `ah=0;ag=0`; `r()` → `i(18)`, `ag=av?-2048:2048`, `ah=-5120` |
| S102 wall-cling | g.java:2736-2757 | `aZ`→`l()`; else countdown `aC--`, expiry/`aR!=4` → `a(0)`+`al+=21`; `u(4112/8256)`→`av`, `i(332)`; `v(16388)`/tap-edges → `i(17)` |
| S317 wall-run | g.java:4065-4099 | `aj=128`; `ah` cap 512; `ag=av?-2560:2560`; `v(16388)`→`ah=-2048`; wall/`aR` → `ag=0` + clip-30 marker FX (`g.f`) → `r()` → `i(50)`; `!q`→`G()` else `a(1,ak,al-60)` |
| S332 wall-shimmy | g.java:4100-4138 | `ag=av?-2560:2560` clamped by `aT/aU≥12`; `aZ`→`l()`; `aR!=4`→`a(0)`; `v(16388)`/tap → `i(17)`; `u(4112/8256)`→`av`,`i(332)`; `r()`→vel0,`aC=18`,`i(102)` |
| jump tail gate | g.java:788 | `cq && !E` — the `!E` conjunct (was unmodeled) |

## The `g.E` mechanism (key decode)

The 3 test failures on first compile were the shared post-dispatch jump
tail overriding arm-level `i(17)`/`ah` — the tail sees the still-latched
`bB` edge and fires `i(233)`. The original's suppression is **zone-side**,
not arm-side: the ax10 S55 zone sets `g.E` while the player overlaps it,
and the tail is `cq && !E`. So S102/S332/S317's `i(17)` wall-kick only
survives inside an E-zone — faithful behaviour now modeled:

- `Entity.gE` companion static + `Entity.icu` (i.cu) + resets in
  `teardown()` and the test `world()` factory.
- `postTail` gate → `p.cq && !Entity.gE && pad.v(M_ACTION_FAMILY)`.
- NpcFsm `tickTrigger` gains the S55 arm (head portion verbatim; the
  `aA`-gauge/`k.C`-rebind/`k.bh[k.aj]` tail L66e-L737 is claim machinery
  left for its own slice — flagged `unknown`).

## Findings kept verbatim

- S317 has **no** `i(317)` call sites — it is script/zone-entered; only
  guards at i.java:6128/:5692 reference `aS.S==317`.
- clip 30 is unconverted, so the `k.z[30]!=null` marker-FX spawn is gated
  off exactly like the original's own missing-clip path.
- `g.d` is a single static shared by the S36 trigger arm (NpcFsm:527) and
  the S10 zone — reused `player.gd`.
- `a(1,ak,al-60)` resolves to `i.a(int,int,int)` = `spawnMarker`.
- `flingAirborne(0)` already does `al+=10` internally; the S102 arm's
  `al+=21` totals +31 (fixed in tests).
- S55 arm sets `P|=128` then clears it on overlap (`P&=~128`) — verbatim.

## Gates

- verifier: `ok:true`, rerun_verification passed
- `python3 -m unittest discover -s tests -t .`: 57 tests OK
- `:core:test`: 944 tests, 0 failures (13 new Slice137Test cases)
- `:android:assembleDebug` + `:gdx:build`: BUILD SUCCESSFUL
