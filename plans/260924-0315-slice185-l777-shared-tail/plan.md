---
title: "Slice 185 — L777 shared tail + L849/L897 tick structure fix"
phase: port
status: complete
date: 2026-09-24
---

# Slice 185 — `i.I()` tail restructure: L78 integrate at head, `goto L849` semantics, L897 shared tail

## Problem found

The ported `NpcFsm.tick` treated `goto L849` as "skip the tail". Verbatim
mining proved it wrong — and worse, the tick appended a physics block at
the *bottom* (integrate + `!aZ→aj=1536` + ground-snap) that does not exist
in the original:

- **L78 integrate is at the TOP** of `I()` (i.java:4886-4898, proven),
  before the `switch(ax)` and before L104's `a(true)` — arms read
  post-integrate positions. Under `aH` slow-mo it swaps to the aI-divided
  variant (L72→L75, i.java:6370-6384).
- **`goto L849` = `au(); goto L897`** (i.java:5197-5200, proven):
  `au()` is the corpse-drop — already ported as `corpseDrop`. Every
  early-return arm still ran box-refresh, facing sync, player push and
  anim advance.
- **L897 is the true tail** (i.java:6355-6372, proven): `t()` box refresh
  (unconditional — `b=true` set at :4866), `av → P bit0` facing sync,
  `a(k.aS, P, W)` entity→player push (i.java:15324-15380).
- **`s()` anim advance is a per-tick driver** (i.java:6397-6411, proven):
  skipped only while `cu`-held, `S<0`, or mid slow-mo interval
  (`j.g % aI == 0` gate). Entities in L849-arm states (S106 etc.) could
  never finish anims under the old structure — that was the S106 freeze.
- **`!aZ→aj=1536` is NOT in `I()`** — `aj=1536` is armed per-arm in the
  original (i.java:5685, :9295, :6480 …); the appended global gravity was
  an inference, removed.

## Change

`rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt`
- `e.integrate(div)` moved to the head: `integrate(if iAH) iAI else 1)`.
- `when(S)` + L777 checks extracted into `armsAndL777` — a `return` inside
  it is the verbatim `goto L849` (skips the L777 checks, still reaches
  the shared tail back in `tick`).
- `tick` tail: `corpseDrop` (au) → `refreshBoxes` (t) → facing bit
  (L900-902) → `pushL897` (a(k.aS,P,W)) → `advanceAnim` gated by
  `!cu && S>=0 && (!iAH || jG % iAI == 0)`.
- `pushL897` — verbatim `a(i,P,W)` port (i.java:15324-15380): P&4096 +
  W-overlap gate, L11 land-top / L28 push-under / L52 left / L65 right
  penetration resolve, velocity zeroing, ax27 always-side-push.
- Removed the misplaced bottom physics block and stale debug `println`s
  (spotB + the `ag`-setter tracer in Entity.kt).

`rewrite/core/src/main/kotlin/com/acrebuild/core/Entity.kt`
- `integrate(div: Int = 1)` — `ag/div`, `ai/div`, `ah/div`, `aj/div`.

`rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt`
- Stale assert `P&16==0` after S106 freeze removed — the aA-branch
  (i.java:6248) re-arms `P|=16` every tick while `aA=2` persists, so the
  bit legitimately returns under the verbatim tail. Commented.
- New `Slice185Test` (6 contracts): L78 integrate-at-head (`ai` consumed),
  slow-mo aI divide, early-return arm still gets the L849 facing tail,
  goto-L849 skips the L777 `!h&&!h→i(25)` fall arm (S20 vs arm-less S50),
  pushL897 lands a falling player on the entity's top edge
  (`p.al = r9[1]-5`, `ah=0`), `cu` holds `s()`.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 tests OK
- `:core:test` → 1195 tests, 0 failures
- `:android:assembleDebug` → `android-debug.apk` built
- `:gdx:build` → green
