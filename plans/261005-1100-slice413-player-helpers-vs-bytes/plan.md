---
title: "Slice 413 — player helpers re-read from the raw bytes: the equip request g.h(I)Z is not the claim bind i.h(I)V, g.a(int) always re-centres, g.c(Z) reads the raw k.g, lever / slope / landing"
phase: "port"
status: "done"
slice: 413
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (a(I)V @1284-1311; c(Z)Z @10908-10957; ar() @356-393; aq(); ax(); d(Z)V @88-281; j() @14135; am(); ak(); al(); an(); h(I)Z; m(); o())
  - reconstructed-project/bytecode/i.javap.txt (a(II)V masked enter @4979; a(Z)V tail t/u writes @6303-6316; f(i) @24069; e(II)I @51264; aW() @847; bC() @1014; aE() @129; aJ() @2021-2040; bb())
  - reconstructed-project/bytecode/k.javap.txt (g(II)I @25347; F(I)V @18415)
---

# Slice 413

Slice 412 found that the obfuscator reused one name for unrelated methods (`g.b(int)`, `g.b()`, `i.b()`).
This slice finished the same sweep for the other names shared between the entity class `i` (private
methods) and the player class `g extends i`, and re-read the player helper methods that had never been
diffed against the bytes. `bcdec.py` / `rawm.py` (a pseudo-decompiler and a raw-bytes dumper over the
javap text) were used; the original JAR was never run.

## The overload groups (`proven`)

`i` and `g` both declare `h(int)` with different return types — `g.h(I)Z` is the **equip request**
(`I = n`, `k.at = 1`, the ax16 link consume), `i.h(I)V` is the private **claim-script bind** that
takes `k.s(..)` indexes. A call site names its target in the constant pool (`Method g.h:(I)Z` vs
`Method h:(I)V`), so every one of the 6 + 20 sites was mapped. The static `g.g(int)` is
`J |= mask; k.q()` (the equip-list rebuild). `g.a(int)` is `a(43, 32); al += 10; ah = r5; aj = 1536;
a = null; ac = null` and has 22 external call sites.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **`aS.h(n)` at four sites** — ax13 rope grab (`aW()` @847), ax69 armed kill (`bC()` @1014), the ceiling ambush `aE()` @129 (ax73 copy), ax73 death (`aJ()` @2037) | `invokevirtual g.h:(I)Z` | `bindScript(n)`: bound claim script 1/2 on the player (`ca`, script ops) and never switched the equip | `requestH(n)`: `gI = n` (a rope grab unequips, the ceiling drop-kill and the guard's death arm too), no claim |
| **`g.g(2)` in the ax73 death arm** | `invokestatic g.g:(I)V` @2030 = `J \|= 2; k.q()` | `gJ \|= 2` only — the equip list was never rebuilt | `requestAction(2)` |
| **`g.a(int)` re-centre** (`a(43, 32)` → `al += u − (W1+W3)/2`, `u` = the box centre of the last `i.a(Z)` pass) | `i.a(II)V` @289-330, `u` written only at `i.a(Z)` @710-729 | `enterFall()` without a world skipped the masked entry at `av()` (the air ceiling `aO >= 20`), `i.f(i)` (the scroll holder's top bound) and the S377 recovery; the port now takes a mandatory world | all three sites re-centre (a ceiling bonk moves the player ≈ 25 px up instead of dropping him 10) |
| **`g.c(Z)`** (S37 grapple-climb step: facing head cell open) | `invokestatic k.g:(II)I` @40 — the RAW cell read, the only gameplay one outside `i.e()` | `e()`: while `S == 37` the player's `e()` override reads every `20` cell as `0`, so the shimmy walked **through walls** | `w.collisionCell` — a solid cell stops the step |
| **ax58 lever from `ar()` / `aq()`** | @384-393 `g.g.i(this.S + 1)` — `aload_0` is the PLAYER | `lever.S + 1` | `S + 1` of the player: out of the lever clip's range for every reach / throw anim, so a thrown knife never flips a lever |
| **`g.ax()` slope pull** | @224-250 `aR == 17 \|\| aR == 16 → ah = −ag >> 1` | cell 17 only | both |
| **`g.d(Z)` landing** (`Entity.land`) | @88-211 anim first, `if (I == 4) h(1)` (an equip-4 player is handed his sword back), then the fall damage reading `S` / `Q` after the switch | no `h(1)`, damage before the anim | as the bytes |

## Capstone re-routes (route / timing only)

Every faithful change above that moved a capstone is listed here; no enemy, state or assertion was
weakened.

* **m3 gate row** (`Slice245Test`) — the `'5'@580` shimmy now ends at the `20` mass (x4260); UP on an S38
  tick vaults onto the lip (`S54 → S0`), then the old autorun continues east.
* **m7 legs G/H/I/J** (`Slice302/303/304/306Test`) — the old route shimmied west **through** the slab
  (x < 140) and climbed back; the faithful route is UP-vault from the `'5'` hang onto the ledge top
  (`S54 → S0 @(270,1298)`), which is where the legs' next phase starts (`landed` = grounded at y > 1280).
* **m7 leg K "west descent"** (`Slice307Test`) — **faithful verdict**: the far-west pocket (x0-99,
  floor y1559) is sealed (slab `cols 0-6 × rows 65-66`, wall `cols 5-6 × rows 67-77`, floor row 78 —
  pinned by the test), so the "west region floor" ending only existed through the pass-through. The
  leg now asserts the route up to the vault and the walk west along the slab top to the map edge
  (`x18 @ y1299`).
* **m7 feed-in** (`Slice309Test`) — the scroll-holder ceiling's `a(0)` re-centres, the release arc lands
  ~38 ticks later: loop budget 60 → 200 (same end state).

## Tests

`Slice413Test` (+15): ar/aq lever arms, slope 16/17, landing equip-4 / damage gating, the `a(0)`
re-centre at the three sites (`enterFall`, S377 tick, scroll-holder ceiling, `airWallResolve`),
`facingCellOpen` against a real wall, and the `h` call sites (ax13, ax69). `Slice400Test` (+2): ax73
death grant + request, ceiling ambush drop-kill. `Level0WorldTest` lever test corrected to the bytes,
`Slice1Test` `enterFall` test given a world.
Mutation-checked: 12 mutants — 12 killed (lever own-S ×2, slope 16, landing `h(1)`, damage gate,
world-less `a(43, 32)`, the four `h` sites, `g.g` without `k.q`, `facingCellOpen` through `e()`).
Gates: core + gdx tests, `assembleDebug`, verifier `ok:true`, `unittest` 57 OK.

## Verified equal (no change)

`g.j()` (rope release), `g.k()`, `g.ak()` / `g.al()` (ledge lip grabs), `g.am()` (dead: no type-19 cell
ships), `g.an()`, `g.d()` / `g.b(i)` (ax43 mount align), `g.m()`, `g.o()`, `g.h(int)` body, `g.k(int)`,
`g.i()` (the dotted trail; the renderer's `ropeDots`), `i.e(II)`, `i.H()`, `i.y()`, `k.F(int)`,
`aS.l()` from the ax73 grab timeout (`grabResolve`), the 22 `g.a(int)` callers' arguments.

## Not done (parked)

ax41 `n()`, ax78/45 `bA()`, ax76 `bO()`, ax61 `aR()` (all but S8), ax34/75 `ak()`, ax64 `bl()`, ax42
`bz()`, ax7 inline, ax5 `aq()`, ax17 `aA()`; the ax11 script-claim head @1700-1951; the `k` UI
methods; `g.aA()` / `g.aC()` / `g.d(int)` / `g.e(int)` / `g.f(int)` statics; the `i`/`g` same-name
groups with different *parameter lists* (only the equal-parameter ones were swept).
