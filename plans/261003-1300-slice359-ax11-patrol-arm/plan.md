---
title: "Slice 359 — ax11 patrol arm (case 2/3/92): turn test, am() support guard, crate-link skip, single aD()"
phase: "port"
status: "done"
slice: 359
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:4104-4135
  - reconstructed-project/bytecode/i.javap.txt:20940-21160
  - reconstructed-project/src/structured/i.java:5832-5843
  - reconstructed-project/src/simple/i.java:5918-5990
---

# Slice 359 — the ax11 patrol arm

Found while porting `aG()` for slice 358. Lands on the Phase 2 integration
branch `claude/phase2-faithful-ai` (with G5, slice 357): it changes how
every patrolling soldier moves, so the capstone bots are re-validated
against it there.

## Findings (proven)

`case 2/3/92` (structured `i.java:4104-4135`; bytecode `i.javap.txt` I()
offsets 4019-4523, which agree):

1. **Turn test.** With `k` (patrolling), `z4` = `am() || aG()` (an edge
   with `ag != 0` first steps back 3 px), else
   `(cells < -Z[5] && av) || (cells > Z[6] && !av)` with
   `cells = (ak - Z[3]) / 20` (offsets 4149-4202). The port had
   `av && cells >= -Z[5]`: a guard facing west inside its range stopped
   and turned at once, and no guard ever turned at the east end of its
   range — it walked east until a wall or an edge.
2. **Crate-link skip.** `z4` runs `i(2)` + the `aC` countdown → `i(3)` +
   facing flip **unless** `s == null && Z[7] > 0 && k.q(Z[7]).ax == 51`
   (offsets 4206-4259). The port tested a `platform` field that nothing
   ever wrote (a dead duplicate of `s`), so the skip never applied.
3. **`am()`** (`i.java:5832-5843`) is false while riding a support
   (`s != null`); the port's copy had no such guard (ax73's
   `ceilingProbe73` was already verbatim).
4. **One `aD()`.** The arm has no `aD()` call — only the L777 tail does
   (offsets 7338-7392). The port ran `crateRide11` + the `s.ag` copy in
   the arm too, so a patrolling soldier bound/rode a crate twice per tick
   (`al = s.W[1]+3` instead of `+1` on the bind tick, `ak += s.ag>>8`
   twice).

Also: the L777 fall check carried an extra `standingOn == null` term
(`standingOn` is the player's `g.a`, never set on an NPC) — removed.

## Fix

- `patrolArm` follows the offsets above; `wallAhead` delegates to the
  verbatim `am()` (`ceilingProbe73`); `Entity.platform` removed.

## Tests

- `Slice359Test` (5): west-facing inside range keeps walking; past either
  end stops (`aC` 19); the countdown ends in `i(3)` facing back; a
  crate-linked soldier never stops; `aD()` binds once per tick. Four fail
  on the old code.

## Status

Capstones broken by the faithful patrol (alone, before G5): m0 finale
(`Slice245Test` cp7 tower, finale pack, stats → mission 1) and m5 leg I
(`Slice289Test`). Re-validated on the integration branch together with
G5's five.

## Resolution (Phase 2, 2026-10-03)

Re-validated with G5 and G12 on `claude/phase2-faithful-ai`; all capstones
pass with re-routed bots (`reports/capstone-revalidation.md` in the parent
plan). Gates on 1e7bb59d: `:core:test` 1689/1689.
