---
title: "Slice 420 — P3 latent list: ax64 bl() restructured (armed-path no longer releases its marker; @747 retract on non-S7)"
phase: parity-gap-closure
status: done
slice: 420
---

# Slice 420 — P3 latent list audit

## Scope

The P3 "low-value" list from the handoff plan:

- `ax64 bl()` — the harrier stalk FSM
- `ax76 bO()` — the hazard volume
- applyHit ops 39/41 — the `k.Y` marker-engage pair
- `Entity.sideFree` vs `crateEdge73` — two ports of `i.aF()`

## ax64 `bl()` — real divergence, fixed

`i.javap.txt` @51572. The pre-audit port (`ax64StalkArm`) merged the
bytecode's two disjoint cleanup paths into one unconditional flow, and
missed the non-S7 tail entirely.

### Bytecode control flow (proven @offsets)

```
@0-11     aA<0 → aA=0
@12-18    S != 7 → @747
@21-62    S7 only: dx, dy, dist = k.h(dx, dy); println("dy:"+dy) debug
@95       tableswitch player-S → vuln set {0,4,5,17,18,30,31,32,33}
@252-256  e.G must be 0
@259-300  ax==56 → e.al < aS.al; ax==64 → e.al > aS.al (strict)
@303-329  dx>25; dy<=100 OR ax==64; dist<200
@332-578  armed: ae==null → spawn table {42,48} above / {60,66} below,
          bl |= {2,8,128,512}; ae!=null && ax64 && ae.S∈{60,66} →
          ae=(e.ak, e.al-40) follow
@578-580  iconst_1 → @680 falls through to @683 (armed path only)
@683-714  !e.G && k.v(bl) EDGE → a(aS,this,1,false) tether + G=1
@715-744  ae.ak=e.ak; ae.al=e.al-20 pin → @744 goto 816 (SKIPS @747)
@582-676  fail path: ae!=null && ((e.ak<aS.ak && ae.S∈{42,60}) ||
          (e.ak>aS.ak && ae.S∈{48,66})) → aS.G()
@679-680  iconst_0 → ifeq 747 — fail path always reaches @747 too
@747-815  ae!=null && ((e.ak<aS.ak && ae.S==60) ||
          (e.ak>aS.ak && ae.S==66)) → aS.G()   [marker retract]
@816+     the S tableswitch 0-7
```

### Divergences found

1. **The armed path released its own marker (was/now).** The port ran the
   stale-release check unconditionally after the arm — so every armed
   tick spawned the S60/S66 marker and instantly released it (a 1-tick
   flicker the old tests even asserted). With `arm` cleared, the
   `k.v(bl)` tether and the pin could never fire — the harrier could
   never bind the player. Bytecode: @744 `goto 816` skips ALL release
   checks on the armed path. Fix: the stale-release now runs only in the
   `else` (gate-fail) arm.
2. **Fail-path right side was `{48}`; bytecode releases `{48,66}`**
   (@654-670). Reachable outcome was incidentally identical (the port's
   wrong-positioned tail released S66 anyway), now structured correctly.
3. **The @747 marker-retract never ran outside S7.** Bytecode routes
   every non-S7 tick through @747 (@18): a live same-side directional
   marker (S60 left / S66 right) is retracted via `aS.G()`. The port had
   no such check — a marker planted during S7 would outlive the harrier's
   stalk phase forever. Fix: `tickAx64` runs `ax64MarkerRetract` on every
   `S != 7` tick.
4. **Non-divergences kept**: the equal-y `p.ae?.let` pin stays null-safe
   (the original's unguarded `ae.ak` at @715 is a latent NPE on the
   equal-y arm, unreachable for ax64); `println("dy:"+dy)` (@64-86) is an
   unobservable debug print, correctly unported; `dy<=100` bypassed by
   `ax==64` (@315-321).

## ax76 `bO()` — verified byte-faithful (no change)

`i.javap.txt` @71512. Re-read end-to-end against `tickAx76`/`ax76Release`:
S0 arm (`W-overlap && aZ && g.g==null` → `i(1)`, `Z[1]==1` → `aA|=8`,
`az=az-1`, `g.e=this`), release (`aA&=-9; az=100; g.e=null` gated on
`g.e==this`), S1 disarm on W-exit, S2 `aC>0→aC--` (the `ifle 287` guard
matches the port exactly — an earlier suspect was disproven on re-dump),
the `o`-sibling chain (`k.q(o).ax76 && S!=2 → i(2)`), `a(W,aS.W)` →
`g.d(g.u[k.au])` overlap drain, S3/5 `a(W,aS.X)` → `i(S+1)`, S4/6 `r()` →
`k.c(this)`. All faithful.

## applyHit ops 39/41 — proven dead (no change)

Xref of every `invokevirtual a:(IIILi;)V` site in `i`/`g` javap: the op
constants used are {0,1,3,4,6,9,11,18,20,21,22,24,32,34,38,40,146,317,
327,328,768} (plus two variable-op sites). No caller ever passes 39 or
41 — the `k.Y` marker-engage pair is dead dispatch. The port's comment
("dead and not ported") is correct.

## `Entity.sideFree` vs `crateEdge73` — equivalent for all callers

Both port `i.aF()` (@32054: `s.ax==51` edge bands, else `e(II)I` facing
cell `!=20 && !=5`). `crateEdge73` uses the entity-aware `e()` probe;
`sideFree` uses raw `collisionCell`. The two differ only inside `e()`'s
`ax==0` carve-outs (crate-top void rows, S37/257 20→0) — and `sideFree`'s
only callers (`resolvePush`, `counteredBy`) run on NPC victims, never the
player. For `ax!=0` entities `e()` ≡ `collisionCell` + identical bounds.
No reachable divergence; left as-is (candidate for later unification).

## Tests

- `Slice420Test` (+6): follow-S60/S66 persistence, tether on pad EDGE,
  non-S7 retract both sides + narrow-set guard, fail-path side sets,
  equal-ak no-release.
- `Slice1Test` ax64 stalk block: the two tests that asserted the 1-tick
  flicker ("releases same tick") were wrong — updated to assert the bound
  marker + pin (`al-20`).
- Mutation: `scripts/mutants/slice420.py`, 8 mutants; 7 killed on the
  full suite (mutant G dropped: dropping S66 from the follow arm is
  unobservable — the @715 pin overwrites `ae=(ak,al-20)` the same tick).
- Gates: `:core:test` green; verifier/unittest/`:gdx:test`/assembleDebug
  run below.
