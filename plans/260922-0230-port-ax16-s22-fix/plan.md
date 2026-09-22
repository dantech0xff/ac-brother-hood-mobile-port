---
title: "port: ax16 bb() S22 rest-guard fix"
phase: gameplay-port
status: done
confidence: proven
---

# Slice 57 — ax16 `bb()` S22 rest-guard inversion fix

## Scope

Re-mining ax16 `bb()` (i.java:14088-14396) as the next slice turned up that
the full FSM was already ported in slices 25/31 (`tickRequestMarker` +
`markerArm`, NpcFsm.kt). Verifying every arm against the decompiled source
found one real divergence — the S22 rest guard was inverted.

## The bug

bb() L138 (i.java:14286-14292):

```java
if (this.S != 22) goto L146;
if (e(this.W[2] / 20, this.W[3] / 20) != 20) goto L146;
if (this.W[3] <= k.aS.W[1]) goto L146;   // skip when marker bottom NOT below player head
i(25);                                   // rest arm: reached iff W[3] > aS.W[1]
```

Semantics: a ceiling-anchored marker (cell 20 under its bottom-right edge)
rests into `i(25)` + zeroed velocity **only when its bottom edge hangs below
the player's head top** (`W[3] > aS.W[1]`). The port had `<=`, firing the
rest in exactly the inverted case (marker entirely above the player's head).

Fix: `e.W[3] <= player.W[1]` → `e.W[3] > player.W[1]` (NpcFsm.kt:2197).

## Verified-unchanged arms (re-checked this pass)

- S30/38/39 head pickups: `g.g/h` + `i(91)` + `A(15)` + `E()` + `k.c`.
- S15 `r()→t()+bd()+i(9)`; S16 impact (sfx12, flying-level `bc()`/op38 vs
  `bd()`/op4, `af=null`, `r()→k.c`).
- S17 ricochet: `aj=512`, `ah==0&&ag!=0 → a(4,ak,al-60)` trail, `X∩player`
  hit/deflect (attack-set `g.b()`), mirror-snap + `k.e(dy, ah>>8, ·, dx)`
  ballistic solve (`Entity.arcSolve`, javap-verified: arg2 is dead — the
  quadratic coefficient is literal 1, `t² + vy·t − dy = 0`), `bR&&ax23`
  link-kill, `i(16)+G()` settle, wall-column scan (`e>=12 → grid-snap +
  av-flip + ag=-ag/2`), floor bounce (`ah>2048 → halve` / `h(ag,ah)<1024 →
  i(16)` rest).
- S22/23 ceiling props (`W==null→k.c`, `W∩aS→a(4)+k.c`, S23 `!v()→k.c`).
- S25 `r()→k.c`. S31/S32/S33 kill prompts (`a(8,ak,al-85)` indicator,
  `v(65568)` tap → pin + `i(216)`/`i(214)` + `A(29)`; note L172 correctly
  has NO `g.b()` guard — port matches).
- Default/S18-21/24/26-30 → inert.

## Tests

New `S22 rests to S25 only when marker bottom hangs below player head`
(Level0WorldTest): OOB-x marker (`collisionCell(cx<0)=20`) with `W[3]` on
both sides of `aS.W[1]` — below → `S==25` + zeroed `ag/ah`; above → `S==22`.
Gates: `:core:test` green (all), verifier `ok:true`, 57 unittests pass.

## Provenance

- i.java:14286-14292 (S22 rest guard) — `proven` (decompiled source).
- LevelPack.kt:43 — OOB `collisionCell` → 20 mirrors `k.e`'s `r6<0 → 20`.
