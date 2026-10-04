---
title: "Slice 393 — i.a() push-past is one straight line (and the boss no longer pushes the wrong way)"
phase: "port"
status: "done"
slice: 393
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (a() @0-477; callers I() @5429/@7657, n() @319, aj() @179, aA() @792, aJ() @2247, aP() @1099, bg() @200, bu() @263, bM() @409)
  - reconstructed-project/src/simple/i.java (914-993 — the misleading listing)
---

# Slice 393

Found by the settle-site audit (slice 391): the boss's push-past ended in
the stub, and reading it against the bytecode showed more.

## The method (`proven`, raw `javap -c`)

```
@0   S == 139                               → return      // corpse
@11  aS.S == 6 && ax == 11                  → return      // rolling player vs soldier
@32  g.a != null && g.a.ax == 43            → return      // grapple claim
@50  S == 18 && aS.S != 12                  → return
@71  S ∈ {131,146}: continue only if aS.S == 12 && aS.g(this)   // @91-112
@113 !a(aS.W, W)                            → return      // overlap
@131 g.a != null                            → return
@137 ax == 15 && S ∈ {6,8} && g.b(aS.S)     → grab: ag=ah=0; i(209); a(null); aC=0;
                                               aS.ak = (aS.ak-ak > 0) ? W[2] : W[0];
                                               aS.al = W[1]+1; g.a = this; return
@265 aS.S > 43                              → return
@276 aS.ak <= ak && aS.ag >= 0 && !aS.y()   → ak = ak - half(aS) - half(this); ai = 0; ag = -1
@371 aS.ak >  ak && aS.ag <= 0 && !aS.y()   → ak = ak + half(aS) + half(this); ai = 0; ag = 1
@463 (both arms AND the no-push case)       → aS.a(true); aS.ag = 0
```

## What the port had

| copy | defect |
|---|---|
| `Entity.pushContact` (ax9, ax15, ax17, ax58, ax73, `I()` ×2) | the simple decompile prints the S131/146 gate (@71-112) after the right arm; the port read it as "the right arm falls into L25" and modelled a `while (aS.S == 12 && aS.g(this))` loop. Effect: the **right-push arm skipped `aS.a(true); aS.ag = 0`** (the player kept `ag = 1` and no side rescan) |
| `NpcFsm.pushOut` (ax4 `aj()`, ax41 `n()`) | correct for those two callers (they cannot reach the missing guards) |
| `NpcFsm.bossPushPast` (ax29 `aP()`) | **inverted**: left block on `ak < boss.ak && ag < 0`, right block on `ag > 0` — a player walking INTO the boss was never stopped (and a player moving away was snapped onto it); no `ag = 0` tail on the no-push path; `forwardWall()` stub tail was a settle sink (slice 391) |

`pushOut` and `bossPushPast` are deleted; the boss, ax4 and ax41 call
`Entity.pushContact`. Call-site gates were re-read against the originals
(`aP()` @1011-1099 skips S∈{2,38,4,5,40,13,26,17,8,39}; the two `I()` sites
are `armsAndL777`'s L559 / L777 arms; ax9/15/17/58/73 call it unconditionally
in the arms the port already had).

## Tests

`Slice393Test` (15): left/right blocks, standing player, moving away, the
wall test, no overlap, airborne, the two `g.a` guards, S131/146 gate, S18,
the S6-vs-ax11 pass-through, the boss call site (player walking into the
boss is stopped and loses `ag`), ax41. `Slice48Test`'s right-block unit
corrected (`ag == 0` after the tail). Mutation-checked: 15 reverts, 14 killed; the survivor (dropping the `g.a.ax == 43` early return) is an equivalent mutant — the `g.a != null` return after the overlap gate subsumes it and the overlap test has no side effects.
