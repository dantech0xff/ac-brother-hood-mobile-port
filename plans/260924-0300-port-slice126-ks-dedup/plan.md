---
title: "port slice 126 — k.s() dedup: shake() → kCollectStreak() + provenance sweep"
phase: rewrite/port
status: done
confidence: proven
---

# Slice 126 — `k.s()` dedup + stale-"unported" provenance sweep

`k.s()` (k.java:5338) is the collect-streak increment: `az++` then the
`dE[] = {0,100,200,400,600,800}` medal-band ladder — `ax<30→30`, band
`tier` found while `az < dE[tier]`, `ax = 30 + tier*15`, `g.f(ax)` +
`old<ax → g.e(ax)` meter refill, `ax>=105 → return` cap.

The port had it twice: `kCollectStreak()` (correct, at Level0World:1077)
and a vestigial `shake()`/`shake` field that only did `shake++`. Three
`k.s()` call sites used the wrong one:

- i.java:5514 + :5519 (ax4 destructible burst `m(-1)` loop) →
  NpcFsm:740/:743 now call `kCollectStreak()`.
- i.java:7280 `S()` (3× wisp burst) → NpcFsm:7281 now calls
  `kCollectStreak()`.

The fourth site (claim-script wisp collect, i.java:19447 → NpcFsm:7966)
already used `kCollectStreak()`.

Removed the vestigial `var shake` / `fun shake()` from `LevelCellSource`
and `Level0World`; test assert updated `shake==2` → `kAz==2` (the real
`az` counter). With the dedup, two consecutive collect bursts now
accumulate toward the 100-point medal threshold — previously the second
implementation's counter went nowhere.

Also swept stale `unported` provenance labels superseded by slices
124/125 (h()/i()/j() chain, `k.E.K()` heldRelease, `d(8,…)` debris,
`k.bz` op table, charge-fill `aB+=5`) — comments now name the ported
symbol. Remaining `unported` notes are real gaps (cutscene `g.s`,
S37/257 LOS-cell arms, camera internals).

Gates: verifier ok:true; 57 unittests; :core:test green; :gdx:build +
:android:assembleDebug green.
