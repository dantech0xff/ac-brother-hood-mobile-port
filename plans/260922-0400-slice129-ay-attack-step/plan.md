---
title: "Slice 129 — ay() attack-step + i.f scroll-wall clamp + combo-case tail"
phase: port
status: done
---

## Scope

Ported the verbatim arms around the sword-combo case (g.java:2469-2525) that
were missing:

1. **`ay()` → `PlayerFsm.attackStep`** (g.java:5363-5421, proven):
   - Ledge guard: the `a`-support edge probe + facing-side cell checks
     (`z2`) — never advances off the support edge; `!z2 → ai=ag=0`.
     - Conjunct polarity proven: facing right (av=false) probes the
       right columns (iE +1, iE3 +2), left probes left (iE2 −1, iE4 −2).
     - `i.aN != null` widens the guard to ±2 columns on the same side.
     - Decompiler gap: aN==null arm leaves `z2` unassigned — ported as
       the ±1 near-column check (high-confidence minimal read).
   - `S67/69`: `ag = ±1792` iff `T==1 && V<=0`; `S68`: `T∈{0,1} && V<=0`.
     `i.V` = hit-pause counter (decremented in `i.s()`); never assigned
     in the decompiled source → always ≤0 (kept verbatim).
   - `aN` approach clamp: stops `ag` when the step would cross the
     target's hitbox edge (`+2px` tolerance).
   - Tail `i.f(this)` — the scroll-wall clamp call site.

2. **`i.f(i)` → `Level0World.scrollWallClamp`** (i.java:5382-5430, proven):
   gate `k.ah != null && k.ah.Z[3]==1` ↔ `scrollHolder?.mode == 1`;
   `k.ah.X` ↔ `scrollHolder.bound`, `k.ah.Z[0]` ↔ `.mask`. Player-only —
   all 16 call sites are inside g's motion arms. Top-bound hit while in a
   `g.b(int)` aerial/action anim (`isAirAction`, g.java:288) drops the
   player via `k.aS.a(0)` → `enterFall()`. Two live mode-1 triggers on
   level 0: zone(1577,619)-(1877,799) bound(1577,619)-(1927,799) mask=1,
   and (5849,590)-(6249,830) mask=1.

3. **Combo case tail** (g.java:2469-2525, proven): after `ay()` —
   `ag!=0 && y() → ag=0` (`forwardWall`), `!aZ && a==null → a(0)`
   (`enterFall` — never swing with no floor), `a==null` added to the
   weakened-lock finisher gate, `cl||r()` arm gains the R==112 ax51
   interrupt, `(aN.aB>0 && R==183) || R∈{184,205} → k.o() + ag=ah=0`
   (`lockInput`), dead-target `R=-1` guard, `T==2 → k.A(10)` sfx.

## Fixes

- `probeCells` reads stale W only between probes — verified `x()` calls
  `t()` at its head in the original (i.java:771) and our port matches
  (`refreshBoxes()` inside `probeCells`).

## Gate results

- verifier: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `:core:test`: 886 pass (5 new Slice129 tests + 1 fixture fix)
- `:android:assembleDebug`, `:gdx:build`: green
- `Slice128Test.MarkerWorld` gained `cell` ctor param + `clampCalls`
  recorder (made class-visible for the new test).
- Pre-existing test `assassination finisher kills a weakened locked
  soldier` needed `+4px` feet embed — the verbatim `!aZ → a(0)` arm fires
  when feet hover above the floor row (aR=0 → aZ=false), exactly as the
  original.
