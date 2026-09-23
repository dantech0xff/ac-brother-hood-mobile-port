---
title: "Slice 171 — clipless record spawn (ax42 win fuse) + win-chain verification"
phase: port
status: done
---

# Slice 171 — ax42 clipless spawn: the win fuse now exists in the world

## What

`spawnEntities` skipped every record whose `ENTITY_CLIP` lookup missed.
`bi[42] = -1` (k.java:266, proven) is a *clipless spawn* — in the original
the entity exists invisibly and ticks `bz()`; the port dropped it entirely.
So the level-0 win fuse (ax42 aw59 at 11410,418) never existed → `l(13)`
was unreachable. Fix: spawn clipless (`clip = null`, which `Entity`
handles defensively) for `type == 42`; other unmapped types still skip
until their init arms are verified (ax13/ax35 ropes & multi-tool records
are likewise unspawned today — noted for a future slice).

## The win chain (verified end-to-end on real level-0 records)

1. `spawnEntities` → ax42 aw59 exists, `S = -1` (dormant; no ctor `i()`
   reaches ax42 — faithful).
2. A claim script arms it: `i(0)` → `S = 0` (script-driven; recorded as
   `bindScript`/`scriptKeyStep` ops).
3. Fuse kind `Z[0]=1` watches `k.q(Z[1])` = aw531 (ax14 flag pickup at
   6903-7067,615-793, `P=160` = bits 32|128 at spawn). It fires when
   `s.P&32` clears — via claim op `op100 arg=1` (`r05.P &= ~32`) or the
   `au()` corpse-drop (`Z[21]` link, i.java:6190).
4. Fire → `kAJ=1, kAK=-40, kAL=Z[2]=70, sfx(9)`; `kAJ==2` accumulates
   `kAM+=50`/tick; at `kAL*1000 <= kAM` (~1400 ticks) → `kBw=-1, kBx=58`
   (aj!=7) → `k.l(13)` win screen.

Observed headlessly: `won=true` at ~tick 1412 after arm, `kAJ=2`, `kBx=58`.

## Tests (Slice171Test)

- ax42 spawns clipless: `S=-1`, `Z=[1,531,70]`.
- arm + flag-clear → `won=true`, `kBw=-1`, `kBx=58`.
- armed but flag still `P&32` → `kAJ=0`, not won (dormancy gate).

## Gates

verifier `ok:true` · unittests 57 · `:core:test` · `:android:assembleDebug`
· `:gdx:build` — all green.

## Remaining for a full-playthrough win

- The scripted arm/flag-clear ops live inside the level's claim scripts
  (S8 watchers bind `k.s(aG)` on player overlap — aG ∈ {250,300,313,105,
  104,927,327,116}). The interpreter is ported; whether the tables emit
  `i(0)` on the fuse + `op100(flag,1)` is exercised only by real
  playthrough to x~7000+. The mechanism is now present and proven.
