---
title: Slice 162 — ax37 Z[2] linked-entity gate + S32 bindAc fidelity
phase: port
status: done
---
# Scope

- **ax37 `Z[2]!=-1` linked-entity gate** (i.java:5764-5811, proven):
  six `Z[1]` modes on the `k.q(Z[2])` link — 0: live combatant mid-anim
  or non-combatant link skips the trigger tick; 1: link missing OR P|32
  skips; 2: missing OR P&~32 skips; 3: missing-or-`j()`-dead →
  `k.n()+k.c(this)` self-remove; 4/5: same on P-bit polarity.
  `ScrollTrigger` gains `linkCond`/`linkUid` (f16/f17); `scrollTriggers`
  is now MutableList with deferred removal (`deadTriggers` drained after
  the loop — `k.c` semantics, avoids mutating the iterated list).
  All level-0 records carry `Z[2]==-1` → gate is unexercised in level 0
  but ported verbatim for later levels.
- **`i.j(i)` dead check** (i.java:5926, proven): `Entity.isDeadCheck` —
  null → true; ax ∈ {11,17,29,27} → member `P()` side-effect release;
  else false.
- **S32 `bindAc` fidelity** (NpcFsm tickTrigger): `player.ac = e` →
  `player.bindAc(e)` / `bindAc(null)` — the original `aS.a(iVar)` toggles
  `P|256` on bind/unbind, which other arms read (`aS.ac == this` checks).
- **aV() audit**: all 55 dispatch cases verified ported or proven dead
  (L1ec7 `return` / L15b8 / L1a02 no-op exits) — table is closed.

# Tests (4)

- linkCond-3 removes on dead ax11 link / keeps on alive link.
- linkCond-1 flagged link skips the k.ah claim; unflagged claims.

# Gates

- verifier `ok:true`; 57 unittests; 420 `:core:test` (4 new);
  `:android:assembleDebug` + `:gdx:build` clean.
