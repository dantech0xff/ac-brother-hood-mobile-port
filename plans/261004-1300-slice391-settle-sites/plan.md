---
title: "Slice 391 — which sites run E() and which only t(): settle sites, the bD finale arm, bG's S18 knife"
phase: "port"
status: "done"
slice: 391
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (E() 2929; a(IIII) 4799; b(Z) 6635; p(II) 18031; g(I) 18922; bb() 47201; bC() 59185; aU() 38980; aV() 39788; bD() 60231 @2069-2260; bG() @152-585, @1070-2015; s() 2879; aP() @1292)
  - reconstructed-project/bytecode/g.javap.txt (d(int) 11165, e() 2214)
  - reconstructed-project/bytecode/j.javap.txt (static{} — j.m = 256; c(I)I tan; a(II)I rand)
---

# Slice 391

Follow-up of slice 389's "settle sites" note. `i.E()` is the settle-sink
(`loop { ah=1; b=1; a(1); ah=0; aR∈{≥12,5,3} → return; al+=10 }`). The port
had two things called "settle": the faithful `eSettle` (slice 389) and an
older `settleToGround` stub — `t()` followed by an `al += 10` sink with no
`a(1)` per pass. The stub stood in at ~20 call sites; most of them are `t()`
in the original.

## Method (static)

`E()` has 19 call sites in the original (grep over the three class listings,
each mapped to its enclosing method): `g.e()` ×6 (already `eSettle` in
`PlayerFsm`), `g.d(int)`, `i.s()`, the constructor tail ×3 (slice 389),
`aU()` ×2, `aV()` ×3 (already `eSettle`), `bb()` ×2, `bC()`. (`k.E()` is a
different, static method.) Every other port call of the stub was read against
its original method with the pseudo-decompiler (`reports/` of slice 389) and
the raw `javap -c`.

## Real `E()` sites — now `eSettle` (all `proven`)

| port site | original | note |
|---|---|---|
| `Entity.gDrain` (`g.d(int)` @113-127) | `if (aZ==0 && g.a==null) k.aS.E()` | death release |
| `Level0World.animWrapped` (`i.s()` @242-252) | `…i(0) unless S79; k.aS.E()` | jc21 wrap tail |
| `NpcFsm.tickRequestMarker` (`bb()` S30/38) | `g.g(n); h(n); i(91); k.A(15); k.aS.E(); k.c(this)` | pickups |
| `NpcFsm` ax69 S4 (`bC()` @1371) | kill landing `aS.i(0); P&=-65; aS.E()` | |
| `aUDraw` ×2 (`aU()` @500-556, @656-704) | `x()` probe 40 px lower; `aR>20 → …E()` | exits over a slope cell |

## Sites that only run `t()` — the stub removed (all `proven`)

| port site | original |
|---|---|
| `Entity.spawnChildFx` = `i.a(IIII)` (4799) | ends `aK.t()` — the child stays where it spawned (ax24 pool seeds had sunk 1 330 px below their parent) |
| `Entity.popupDmg` = `i.p(II)` (18031) | `…ag=ah=0; aK.t(); P\|=16; af=this; k.b` — floaties spawn above the enemy, they are not dropped to the floor |
| `Entity.spawnBarrage` = `i.g(I)` (18922) | `aK.t(); P\|=16; af=this; bR=0; k.b; k.A(27)` |
| `Entity.syncAd` = `i.b(Z)` (6635) | `ad.t()` |
| `bG()` knives (S18, 7-way, pv4) | `aK.t()` |
| boss arena clamp (`aP()` @1292) | `a(1); t()` = `collideSides(true); refreshBoxes()` — an airborne boss was dropped to the floor each tick |
| boss push-past tail (`a()` @463) | `aS.a(1); aS.ag = 0` — `collideSides`, not a sink (the full port of `a()` is slice 393) |
| `bD()` finale (case 7) | `aS.t()` — see below |

`settleToGround` is deleted; the unit that exercised it now tests `eSettle`.

## `bD()` finale (case 7, @2069-2260, `proven`)

```
ag = ah = 0; ad = null; bj = true; bT = false; aS.ag = 0
aS.ah = v() ? k.Y : k.Y - 2560           // both arms join at @2131
aS.t()
if (!aS.v()) { k.l(15); return }
a(24,40,9,az+1); aK.ag=ah=0; av=false
aK.ak = j.a(W[0],W[2]); aK.al = j.a(W[1],W[3])   // a RANDOM point of W, x then y
aK.P|=16; aK.af = aS; k.b(aK); return
```

The port (a) took the in-play branch only — with `v()` false it set
`aS.ah` and **fell into the L237 tail** (no `k.l(15)`, no floatie), (b) sank
the player with the stub, (c) placed the floatie at the centre of `W`. The
arm now ends in `return` on every path, refreshes `aS` with `t()`, and draws
the two RNG values in the original order.

## `bG()` S18 knife (`proven`)

- `r3 = (j.c(30 * j.m / 360) * r2) >> 8` with **`j.m = 256`** (j.javap
  `static{}` @62-65) → `j.c(21)` = tan(29.5°) in 8.8 = 145; then ONE
  `j.a(0, r3)` draw feeds `|draw| * (-512 - k.Y) / |r2|`. The port read `j.m`
  as 0 (`j.c(0)` = `Int.MAX_VALUE`) and drew from a range of 8.4 M — the
  launch speed overflowed into noise.
- The `j == 3` block (@460-530): `ag >>= 2; k = true;` the aG1/aG2 mirror
  flips `ag` (@511), and `ah = k.Y + 128` closes the block on **every** path
  (@520 is the join of all three jumps). The port flipped nothing for aG2 and
  wrote `ah` only in that one sub-case.

## Consequence found by the capstones

Fixing the S18 range made the escort knives real; mission 4's capstone bot
died to them and never recovered — which led to slice 392 (the knives'
flight FSM `ba()` had the arrival test inverted, so a knife never actually
flew). With both fixed the bot wins mission 4 again **without any change to
the bot** (it now takes ~14 800 ticks instead of ~3 700; the escorts survive
the flight for longer).

## Tests

`Slice391Test` (20): the five `t()`-only helpers, the knife sites, the real
`E()` sites (death release, both `bb()` pickups, jc21 wrap, both `aU()`
exits over a slope cell, `bC()` S244), the boss arena clamp, the finale (random
point, off-play branch, no tail, no sink), the S18 draw range (|ag| bound from
`j.c(21)`, `Trig.tan(21) == 145`) and the `j == 3` block for aG0/1/2.
Mutation-checked: 20 reverts, see the table in the commit message.

## Not done here (tracked)

- Slice 392 — `ba()`.
- Slice 393 — `i.a()` push-past: `pushOut` (ax4, ax41) and `bossPushPast` are
  older duplicates of `Entity.pushContact`; the latter itself models a loop
  the bytecode does not have.
