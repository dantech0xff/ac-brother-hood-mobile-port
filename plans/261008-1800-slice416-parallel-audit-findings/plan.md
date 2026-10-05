---
title: "Slice 416 — eight parallel read-only auditors over a frozen snapshot: g.e() arms, the flying g.n() head, the D() respawn reset, the raw `ac` stores"
phase: "port"
status: "done"
slice: 416
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (e() @0-15123 incl. @2164-2203, @4372-4543, @4580-4583, @6809-6832, @7365-8300, @8881-8927, @9768, @11282-11306, @14396-14415; n() @0-2262; a(I)V; as() @248; au() @1431; av() @0-191; ap() @0-238; c(i)V @5-6; m() @0-33; o()Z; aB() @117-163; aq()/ar() @207-269)
  - reconstructed-project/bytecode/i.javap.txt (D() @0-406; t() @0-1231; B() @0-340; b(III)V @0-91; c(III)V @0-95; J() @0-94; O() @0-47; a(Li;)V @0-48; o(II)V / b(II)Z / U(); I() @880-1330)
  - reconstructed-project/bytecode/k.javap.txt (n()V / n(I)V @11318-11360; p() @13625; r() @20310; V() @23055; W(); a(Z); c(Z) @1060-1200; m(I) @2590-2700; u:I writers)
---

# Slice 416

A frozen copy of the port (`PlayerFsm.kt`, `Entity.kt`, `NpcFsm.kt`, `Level0World.kt`, `Trig.kt`) and of the raw `javap`
text of `g / i / k / j` went to eight read-only subagent auditors, each owning a byte range of `g.e()` (head and
arms 2164-3432, 3432-6000, 6000-9000, 9000-13600, the default arm / L353d / J&4 block / post-tail), the `i` helper
families (`t()/a(Z)/s()/i(int)/E()/x()/v()/C()/B()/D()/f(i)/b(i)/e()/y/z/A` and `aA/aU/bc/bk/by/d(Z)/aK/aL/e(int)`).
Their hand-back messages carry no authority: every claim below was re-proven in the raw instructions (`rawm.py` /
`bcdec.py` over the javap text — never by running the JAR) before a line of the port changed.

## Fixed (`proven`, raw bytes)

### Player — `g.e()`

| what | bytes | was | now |
|---|---|---|---|
| **S375** (the ax61 aura / trap knockback skid) | @2164-2182: `ag = 1280; if (!av) ag = -1280` | `av ? -1280 : 1280` — the skid ran INTO the boss | away from the boss the player faces |
| **S184 / S205 finisher** | @4440-4473: the victim goes to `av ? ak - 35 : ak + 35`; @4496-4509 the end-gate is `r() \|\| aN == null` (`k.p(); i.O(); i(0)`) | the signs were swapped; no `aN == null` exit | both |
| **S50 / S241 frame-1 drain** | @4580-4583: the STATIC `g.d(I)V` (`i.bh = 8`, the death release) | `drainMeter` set an instance copy of `bh` and skipped the release | `gDrain(999, world)`; `drainMeter` deleted |
| **S92 / S101 bounce** | @6809-6832: `ah == 0` (solid head cell) → `g.a(0)`, else `a(36,36)` | always `a(36,36)` | the fall for a solid head |
| **S22** | @7365-7393: S22's own entry releases the hide-spot owner (`aA &= -9; az = 100; g.e = null`) | missing | added |
| **air-family drift clamp** | @8068-8112: no `ag != 0` guard | guarded — an idle S25 against a wall was not pushed | `ag = ag > 0 ? 512 : -512` |
| **air-family land block** | @8119-8223: the landing is `d(false)` for EVERY S; the S215 test is the ELSE of it (`g.a(0); av = !av` for an S215 arc that turned without floor contact) | the two branches swapped (S215 on the floor fell on, S5 etc. landed) | as the bytes |
| **air-family tail** | @8296: `i.f(this)` is the LAST step | clamp ran before the fall / apex swap | moved last |
| **wall-grab snap** — *two byte sequences* | air site @7886-7938 `((W0+20)/20)*20+1` / `((W2-20)/20)*20+19`; fall arm @8881-8927 `(W0/20)*20+1` / `(W2/20)*20+19` (one cell nearer) | one formula for both | keyed on the site (`wallGrabSnap(flipOn215)`) |
| **S90** | @9768: `k.v()` wipes all six pad words (`eL bC bB eM eK eN`) | `eL = 0` only | `pad.clearLatches()` |
| **S38** | @11282-11306: unbound over an open head cell → `al = W[3]; i(43); goto 13629` — the arm ENDS (the DOWN / facing handlers @11383+ belong to the grapple and `aO == 5` branches) | the handlers ran after it | `keys = false` |
| **post-tail `ap()` gate** | @14396-14415: `z && !i.bn && !g.E` — two statics | the port tested a dead `PlayerFsm.bn` and a never-written `world.eFlag`: `ap()` ran in counter-binds and alert zones, `g.l()`'s `i(bn ? 199 : 32)` / S79 `!bn` were unreachable | `world.iBn` / `Entity.gE`; both dead copies deleted |
| **`ap()` knife arm** | @200-235: `I == 2 && S != 79 → ai = ag = 0; i(286); k.A(29)` | no S79 gate, no `ai = ag = 0` (a running player slid through the throw) | as the bytes |
| **`g.c(i)` head** | @5-6: `putstatic i.bq = 0` — the crate-top level `g.m()` reads | wrote a dead copy (`Entity.bq`) | `entBq = 0` |
| **the gauge point** | `g.aB()` @117-163 writes the PLAYER's instance `g.L / g.M` (read by `ar()` / `aq()` @230-263 and the HUD); the statics `i.L / i.M` are the touch anchor (`o(II)V`, `b(II)Z`, `U()`) | one companion pair served both, the port kept four disjoint copies of the statics (the anchor zone was dead) and the gauge drew at (-camX, -camY) | `gQL / gQM` for the gauge and the throws, ONE `Entity.L / M` for the anchor |
| **`k.at` weapon-cycle lock** | `k.c(Z)` @1159-1167: the HUD tail `at == 1 → 0` under `g.o()Z` (grounded or aboard) | reset a never-written `kAt` (the lock lives in `actionLock`): cycling worked once per level / equip rebuild; the gate used the private `i.o()Z` | one field; `groundOrVehicle()` |
| **`i.J()`** (the ax71 companion overlay) | @60-79: hides on `r() \|\| (j.c == 21 && k.u != 8)` — `k.u` is the dialog kind (writers `k.b(IIII)Z`, `ag()`) | compared the pad's held word to 8 | `dlgU == 8`; `padHeldWord()` deleted |
| **raw `ac` putfields** | four sites outside `i.a(Li;)V`: `g.a(I)V` @31-37, S228/358 release @12688, `g.as()` @248, `g.au()` @1431 — no `P & 256` bookkeeping | every write ran the setter (the old target was un-frozen / the cart's track entity was frozen) | `putAc()` at those four sites; the 14 `a(Li;)V` call sites keep the setter |

### Flying player — `g.n()` is its own method (`i.I()` @1253-1256 vs @964-967)

| what | bytes | was | now |
|---|---|---|---|
| the shared e() head | `n()` @0-272 calls no `a(Z)V`, `an()`, `J()`, `i(50)` and has no latch clears, `aA` fixes, `bq` clear or `ah > 5120` clamp; `i.bh--` @47-61 comes AFTER the dead-drag `return` | the whole e() head ran for bh3 too — `a(an())`'s wall resolve and its box refresh BEFORE `B()` | `tickBody` goes straight to `flightTick`; `iBh--` at @47 |
| the S4 level-out | @1654-1684: `k.bB == 0 && k.bC == 0` are the pad's edge / held words | two never-written stubs (always true): a held bank / dive key blipped back to S4 on every anim wrap | `pad.bB == 0 && pad.bC == 0`; `kBB / kBC` deleted |

### `i` core

| what | bytes | was | now |
|---|---|---|---|
| **`D()` (every spawn / retry / mission entry)** | @299-300 `k.n(-1)` is the SHAKE overload (`cO = 1; cP = false`); @192-211 re-arms `i.br[]`; @335 `i.O()` + @384 `k.r()` zero `k.aw`; @338 `k.p()` releases the input lock; @17 `k.F = null`; @173 `i.at = null` | the wall-release overload (`kN()`); hints never re-armed; stale `kAw`, `kAm`, `kF`, `Entity.at` | all six (the wall release stays as `V()`'s @49) |
| **`i.t()` for ax21** | @1103-1172: `W[0] += bY - k.O; W[1] += bZ` — a camera-space box | `W += ak/al` | the translate keyed on ax21 (the finale floatie lands where the original's does) |
| **`B()` canyon slides** | `b(III)V` @33-83 / `c(III)V` @33-84 re-read `r1 = W[0]; r2 = W[2]` after every `t()`; the L36 embedded-corner arm @278 returns WITHOUT the trailing `t()` | passes 2-4 repeated the first pass's 1-px-edge step; the extra `t()` | whole-cell steps; `return true` |

## Capstone bots — route / timing / input only

The fall-arm snap (`@8881-8927`) puts a kick's arc a cell nearer the wall it left, which three bots had been
exploiting by accident:

- **chimney (x9000 crossing, `Slice277Test`)**: the east-wall kick now peaks at x8940, past the low strip's end (x8920). The
  human route hops WEST along the ledge (TL corner held) to the floating tower's east face (x8740), grabs it (S101) and is
  thrown east / up under the strip (ceiling grab → shimmy → vault-out → bridge → plateau, all unchanged).
- **mission 3 leg A**: the x1400-wall kick drops onto the y519 ledge's east LIP (S61 hang) instead of past it — pull UP
  (S62), and hop west from S0 (the frame after the pull-up still has `aZ` false).
- **mission 5 leg B**: the slot kick catches the slab lip (x1880, y580) the same way — pull UP, then hop west keeping `av`;
  the block-top duel turns to the soldier at the player's back first (it hit him from behind while the bot struck a stunned
  S144 one in front).

## Tests

`Slice416Test` (+29): one per fix above (S184 end-gate, S50 `g.d`, S22 owner, clamp-last, S215 land / hand-over, S25 clamp,
fall-site snap, S92, S375, `ap()` gates `g.E` / `i.bn`, knife arm, lunge `bq`, throw point, weapon-cycle lock + corner gate,
`i.J()`, flying head / level-out, raw `ac`, S90 flush, S38 exit, S199, the `D()` reset, ax21 `t()`, canyon left / right slides + L36).
Misread unit tests corrected to the bytes: `Slice130Test` (S375 sign), `Slice135Test` (S184 side), `Slice195Test` (S92 aO 20),
`Slice369Test` (S184 keeps the lock only with a live victim), `Slice180Test` (the bank blip), the weapon-corner / `kAt`
tests. `Slice415Test` +2 (gauge point, one anchor). Mutation-checked: 36 mutants (each old behaviour put back; now
`scripts/mutants/slice416.py` run by `scripts/mutation-check.py`) — 36 killed
(the first pass left the right slide alive: only the left one had a test; the mirror test killed it). The harness that produced the
first pass counted any red build as a kill; the 36 were therefore re-run with the committed harness, which takes the kills from the
JUnit XML and reports a compile error as `ERROR`: 35 are killed by named `Slice416Test` testcases and one (G2, the S184 drag side)
by `Slice135Test`. Gates: verifier `ok:true`, 57 unittests, `:core:test` 2265, `:gdx:test` 18, `:android:assembleDebug`.

## Verified equal / data-checked (no change)

I1's ten methods (`aA aK aL aU bc bk by d(Z) e(int) a(i,i,int,Z)`): randomized differential of a JVM-subset interpreter
over the javap text against a transliteration of the Kotlin — 0 mismatches over 20k-60k trials each, 100 % of the raw
instructions covered except ten provably unreachable. One `unknown` (signed-byte waypoint `e / f`) is moot: a census of
all 8 missions finds 184 type-55 rows, every `row[6]` / `row[7]` within `-128..127`.

## Not done (parked)

- `i.v()`'s `k.ac` snapshot (the previous tick's camera rect) vs the port's live rect — one-tick cull edge, `inferred`.
- The port reuses one `player` entity across `D()` where the original builds a fresh `g`: `resetPlayerToSpawn` re-inits
  the documented fields; a field-by-field audit of the rest is the next slice.
- The remaining `g.e()` ranges the auditors found EQUAL are listed in the hand-back reports (not committed).
