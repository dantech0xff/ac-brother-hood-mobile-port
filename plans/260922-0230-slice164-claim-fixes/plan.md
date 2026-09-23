---
title: "Slice 164 — claim-field correctness + stale-comment sweep"
phase: port
status: done
---

# Slice 164 — claim-field correctness

Not a new-FSM slice: the ax dispatch audit showed every ax type is already
covered and all flagged "unported" claim machinery exists. What remained
were two real fidelity bugs from the field-split era plus stale comments.

## Changes

### `claimAb()` read the wrong `i.cK` split (Entity.kt)

The original has ONE `private short cK` (i.java:132) serving two roles the
decompiler conflates in name only: the claim-script key counter used by
`ab()`/`bI()`/`k()` (ported as `scriptStep`, Entity.kt:169) and an
unrelated lunge X[1] snapshot used by `c()`/`as()` (ported as `cK`,
Entity.kt:233). `claimAb()` (:2654) checked the snapshot `cK` — which
defaults to 0 — making `claimAb()` effectively `ca>=0 && !cd[0]`, i.e.
claims stayed "active" forever after `bI()` wrote `cK=-2` to the real
counter. Fixed: `claimAb() = claimActive()` (`ca>=0 && !cd[0] &&
scriptStep>=0`, i.java:18914 proven). Callers (NpcFsm:936,
Level0World:4083, draw paths) unchanged.

### `claimKC` reduced-`N()` deleted (Entity.kt:3683, NpcFsm.kt:4290)

`i.N()` (i.java:7284) claims `k.C`, evicts a still-`ab()` previous holder
(`bI()` + `k.c`), binds `h/k(k.s(aG))`, `P|=16`. The early port reduced
it to `kC=this; P|=16` under `claimKC`. `bindContext` (:2499) is the full
verbatim port — the ax21-director S26 call site (original `iVarQ3.N()`,
i.java:8561) now routes through it: previous claimers are properly
released+removed and the ax5's script binds.

### `cellForLos` routes `e()` (Entity.kt:2878)

`i.e(i)` LOS walk (i.java:2407) reads cells via `i.e(x,y)` (i.java:15294)
which applies the ax0 overrides — `k.aS.m()` outside-row→0 and
`S∈{37,257}` cell-20→0 — but only for `this.ax==0`. The port read raw
`collisionCell`, dropping those arms. `cellForLos` now delegates to
`e()`; NPC walkers (`ax!=0`) get identical raw reads, a player walker in
S37/257 sees through cell-20 as the original intends.

### Stale "unported" comments freshened

- `ga` field doc — producers now exist (ax15 bind, lunge Entity:1210/1285,
  ax66/72 arms).
- `g.d` meter-drain doc — `g.s` is the cheat/debug toggle (k.java:762
  cheat-code arm), not a cutscene flag; deliberately unported.
- `losBlocked` doc — ax0 arms now routed (above).
- NpcFsm aV() tail — table closed per the i.java:9160-9260 audit; every
  remaining state is a proven dead arm or no-op.

## Test fixtures corrected (not weakened)

Three tests modeled "armed claim" by poking the lunge `cK` — meaningless
now. Fixtures arm `scriptStep` (the real `i.cK`, matching the original's
`k()`-armed `cK=0` state): `claimAb needs bound ca armed cK` (:10504),
`S17 claim busy freezes` (:14517), `S31 claimAb ticks` (:13570 — also
needs `kBy[0]` to exist since the armed `aa()` reads `k.bz[ca]`; the
world gets one empty op-block via an anonymous `kBy` override, which is
what the original requires of any `ca`-bound entity).

## Gates

- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → 1086 tests, 0 failures (4 new: release/claim,
  `N()` eviction, ax0 `e()` overrides, LOS routing)
- `:android:assembleDebug`, `:gdx:build` → clean
