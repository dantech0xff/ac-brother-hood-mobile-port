---
title: "slice 277 — weakened-victim mount freeze: verdict + S312 deviation fix"
phase: 277
status: done
---

# Slice 277 — S277 mount freeze: verdict and port fixes

## Verdict (two parts)

The S277 weakened-victim mount freeze observed in the capstone is
**faithful once entered** — and the entry the bot hit was a **port bug**
introduced in slice 28.

### 1. Faithful soft-lock (proven)

Once mounted on a weakened (`Z[0]==2`) mountable (`Z[19]==1`) ax11
victim, no release arm is reachable:

- `g.i(i)` offer (g.java:12731-12787) has **no Z[0] gate** — weakened
  victims offer and bind identically (`az()` L56a:
  `i(e) || (aA∉{0,2} && |Δal|≤20)` + LOS via `e(i)` Bresenham,
  g.java:6955-7070).
- `au()` ax11 orbit arm (g.java:10800-10870) applies the constant
  tangent drag to the PLAYER with `cy` frozen at bind — no input arm
  (the cB/cL/cM wheel machine is ax72-only, `F.ax!=72 → L60b`).
- Every release fails: `P()` needs `aB≤0` (orbit never damages the
  victim), `h(Δ)>440` / `|Δal|≥60` once floor-pinned, the `aA|=8` carry
  arm requires victim `Z[0]==0` (i.java:36405), `aI()` needs 54px
  W-overlap, S303/295 need an anim-end S277 never reaches, and `i.at`
  is never set for a fresh ax11 mount (only ax72 cart arms
  g.java:13704 and carry-throw i.java:17072 set it).

The original soft-locks identically on a deliberate mount press —
a real design edge: weaken + mountable = unrecoverable grab.

### 2. Port bug — S311/312 arm (fixed)

The capstone's mount did NOT come through mountEntry. The trace showed
`S310→S312→S277`: an ax11 grabbed the player (S310 hold), released to
S312, and slice-28's guessed arm resumed `lungeTick` — whose F-pick
(`bound.ax==11 && Z[19]==1`, Entity.kt:640-644 — **no Z[0] check**)
auto-mounted the bound weakened victim with zero input.

The real arm is **L346f** (g.java:7456, `case 311/312 → goto L346f`):

```java
this.a(1);                 // g.a(int) = enterFall(vy) → S43 fall
if (this.r()) { ah=0; ag=0; this.l(); }   // dead: fresh S43 never r()
```

L346e (S310) is `return` — anim-only, already correct in the port.

### 3. Port bug — `az()` bind gates (fixed, earlier this slice)

The ported npcKind arm applied the `aA∉{0,2}` gate to ax9/29 and bound
non-offer victims at any vertical distance. Verbatim (g.java:13487-13505):
the aA gate covers only `{11,17,23,73}`; ax9/29 fall through to the
L56a `|Δal|≤20` gate like every other candidate.

## Bot policy (capstone)

`mountFrozen` suppression in the attack gate: never inject CONTEXT while
`p.g` is bound to a weakened mountable (`ax==11 && Z[0]==2 && Z[19]==1`).
The weakened victim stays passive (S183 settle) and unbinds out of
LOS/level — the bot walks past.

## Results

- `Slice277Test` (6 tests): weakened bind, |Δal|≤20 non-offer gate,
  CONTEXT-mount lunge, frozen-orbit mechanics, Z0==0 contrast, and the
  S312 grab-release regression (`a(1)` fall, no auto-mount).
- Capstone after fix: `won=false deaths=301 maxAk=8999` — the S277
  freeze is gone; the bot now dies dueling the posted ax11 at x8971 in
  front of the x8982-8997 wall (S9 staggers). Frontier unchanged —
  the wall guard duel is the next leg, not a port bug.

## Files

- `PlayerFsm.kt:937-948` — S311/312 arm corrected to L346f.
- `PlayerFsm.kt:2357-2367` — az() npcKind bind gates (aA set + |Δal|).
- `Slice1Test.kt` — capstone `mountFrozen` suppression + Slice277Test.
