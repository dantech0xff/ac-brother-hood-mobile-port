---
title: "Slice 403 — the shared j()/C()/i() helpers: ax17 intake, the g.b() weapon gate, k.au vs the entity's own au"
phase: "port"
status: "done"
slice: 403
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (j() @8297-9000, C() @8812-9010, P() @27218, G() @19153, g() @7928, Q(), aA() @764-795, aJ() @2042-2100, aK() @521, aL() @473-482, ctor arms @2533/@2652/@3055, bu[k.au] sites)
---

# Slice 403

Slice 402 re-read the ax11 arms; this slice re-reads the helpers every arm funnels
into, with the raw bytes (jadx / `bcdec` only as a map). `j()` has **five** call sites
in `i.javap` — the soldier tail @7509 plus `aA()` (ax17), `aJ()` (ax73), `aK()` (ax50)
and `aL()` (ax47) — and `C()`/`i()`/`g()` are equally shared. The port had a
`jIntake` for the soldiers and **three private copies** for the others; the copies
had drifted.

Verified equal: `d()`, `l()`, `g(i)`, `b(i)`, `Q()`, `y()`, `H()`, `P()`/`G()`, `j()`'s
ax11/ax73 arms (damage table, finisher launch, half-HP engage, weaken tail), `C()`'s
ax11/ax73/ax23/ax50 arms, `aJ()`'s tail order (`h()→i()`, `j()`, `aB()`).

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| `C()` weaken marker | 54-74 | `p.spawnMarker(45, soldier.ak, soldier.al-85)` | `aS.a(45, aS.ak, aS.al - 85)` — receiver **and** coordinates are the player's |
| **ax17 civilian intake** | `aA()` 764-795, `j()` 70-136 | an inline copy of `j()`: no ±160/±20 band, no `aN`/`bf` first-swing latch, no `C()` — a struck civilian never staggered or sounded, a killed one waited a tick for the head's dead-check | `if (j()) return; if (S != 69 && S != 129) a()` with the shared `jIntake`: band, latch, `C()` = `S==68 → false` else `i(68)` + `k.A(13)`; dead → `i(69)` at the blow, no sound; a corpse is skipped by `P()` (+`G()`) |
| `g.b()` weapon gate | `j()` 62-77/70-77, `i()` | `p.S in ATTACK_ANIMS` only | `g.b()` = `(gI == 1 \|\| gI == 2) && S in attacks` (`w.playerAttacking()`) in `jIntake` and `iEngage` |
| **`k.au` vs `e.au`** | every `bu[...]`: `getstatic k.au` (@8589, 8832, 8866, ctor @2533/2652/3055, I() 4893/5061, aj() 741, aV() 2977-3932) | ax47/ax50 init HP and the ax73 `j()`/`C()` copy indexed the tables with the entity's own `au` — the screen-distance score `u()` rewrites (default 10 → slot 0): sentinels always had 300 HP (400 at the default difficulty), and the heavy guard's enrage line (`aB <= bu[k.au]`) moved with its distance to the camera | `bu[k.au]`: init `BU73[world.kAu]`; the ax73 tail calls the shared helpers (`weaponSlot`) |
| ax73 tail copies | `aJ()` 2042-2085 | `counterWindow73`/`counterStrike73`/`damageIntake73`/`reactOrEnrage73`/`hitKnockback73` — private copies of `h()`/`i()`/`j()`/`C()`/`g()` | `hGate` → `iEngage`, `jIntake` (→ `Entity.hitReact` → `Entity.resolvePush`); 101 lines gone |
| ax47/ax50 `j()` | `aK()` 521, `aL()` 478 | `damageIntakeSentinel`: yet another copy | delegates to `jIntake` (survivor → true, dead ax50 → `i(129)`, dead ax47 nothing) |

`kAu` is the real difficulty: NEW GAME / RESET GAME set 1 (Medium), the CONTROL menu
cycles 0..2 (Hard only once unlocked) — so the default game ran at `kAu == 1` and the
sentinels were one tier too weak.

## Tests

`Slice403Test` (13): weaken marker owner + coordinates + occupied `ae`; ax17 stagger /
kill / S68 re-hit / ±160·±20 band (inclusive) / first-swing latch / corpse `P()`+`G()`;
`g.b()` weapon gate for `j()` and `i()`; ax47/ax50 init HP per `k.au`; ax73 enrage line
per `k.au` (and not per `e.au`); ax47/ax50 `C()` arms.
Mutation-checked: 12 mutants (marker coords, marker owner, band, both `g.b()` sites,
S68 guard, corpse `G()`, latch, ax17 tail, init `au`, enrage `au`, ax50 death anim) —
12 killed.

## Not done (parked)

- Everything parked in plans 401/402 (alerted soldiers solid; the universal
  pre-dispatch `collideSides`; the ax11 script-claim head).
- `Entity.sideFree` (used by `resolvePush`) and `crateEdge73` are the same `aF()` twice;
  equal on every input, left as is.
