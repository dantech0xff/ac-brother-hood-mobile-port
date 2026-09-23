---
title: "Slice 135 — S183/184/205 assassination finisher arms"
phase: rewrite-port
status: done
slice: 135
---

# Slice 135 — S183/184/205 assassination finisher arms (g.java:3041-3140)

Ports the player-side assassination finisher arms verbatim, replacing the
slice-6 approximation (`assassinArm`), plus the two victim helpers they call.

## What landed (proven, g.java:3041-3140)

- **`183 ->` arm** — while the finisher plays, the locked `i.aN` victim is
  dragged into `i(106)` at ∓30px (av→`-30`, !av→`+30`), each guarded tick
  paying `k.e(0,aw)` + `i.aN.S()`. The embedded `S==184` copy is a verbatim
  dead conjunct (documented, not executed). Release = `r() || aN==null` →
  `k.p(); i.O(); i(0); aN.aB=0; i.d(aN); aN=null`.
- **`184, 205 ->` arm** — S184 drags to `i(107)` at ±35px (av→`+35`,
  !av→`-35` — opposite sign of S183, verbatim); S205 shares the arm with no
  drag (`S==184` conjunct). Release = `r()` end-gate, same victim chain.
- **`victimPayoff(w)`** = `i.aN.S()` (i.java:7276): `repeat(3){ m(-1) →
  spawnWisp; k.o(5) → kStat(5); k.s() → kCollectStreak }` — 3 wisps + score
  + streak per kill tally.
- **`releaseAnimReset()`** = `i.d(iVar)` (i.java:1221): ax-dependent anim
  reset — ax11→`i(0)`, ax23→`i(79)`, else untouched. Replaces the old
  approximation's hardcoded `i(139)` corpse set (the corpse chain is the
  NPC FSM's own `aB<=0 → i(139)` arm).
- Removed the old `assassinArm` + `183, 184 ->` dispatch entry — it had
  the wrong `av` snap sign for S184, no payoff/tally, wrong `i.d` semantics.

## Decompile-reading note (confidence: high-confidence)

JADX emits two degenerate copies of each arm. The release gate renders as
`if (!r())` / `if (!r() || aN==null)` — `!r()` would release *while the anim
is still playing* (the finisher would end on tick 1 and never play), so the
gate is taken as the `r()` end-trigger idiom used by every other arm, plus
the `aN==null` early-out from the fullest copy. Drag guards and order come
from the fullest guarded copy.

## Known gaps

- Converted **clip7 has only 201 anims** — S205 (and 202-204) are out of
  range for it. The original drives S205 on the wider `k.z[75]` bank;
  flagged `unknown` — probably an anim-count gap in the clip conversion
  or S205 lives on a different bank. `setAnim` bounds-checks so no crash
  in-game; tests drive S205 via a null-clip entity.
- clip7's S183 is single-frame → `r()` fires the tick it enters; tests
  assert the combined drag+release effects for S183 (S184 is multi-frame
  in clip7 and tests the true mid-anim hold).
- `k.e(0,aw)` → `world.kStatE(aw)`; the `gate==0` lane bumps `ap[0]`
  (Level0World:988 semantics preserved).

## Tests (Slice135Test, 6)

- S183 drag → victim snap `ak+30` (av=false), tally `kStatE`, payoff
  3×(wisp+stat5+streak), `ag/ah=0`, release zeroed `aB` + cleared lock.
- S183 release → `i(0)`, `aB=0`, `i.d(ax11)→S0`, `aN=null`, `kAm=false`.
- S184 drag → `i(107)`, `ak+35` (av=true), tally + payoff.
- S184 no-drag guard when victim already S107.
- S205 no-drag (`S==184` conjunct false).
- `releaseAnimReset` → ax11→0, ax23→79, ax5 unchanged.

## Gates

verifier `ok:true` · `python3 -m unittest` 57 pass · `:core:test` green ·
`:android:assembleDebug` green · `:gdx:build` green.
