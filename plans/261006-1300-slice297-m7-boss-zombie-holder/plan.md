---
title: Slice 297 — m7 boss duel: zombie scroll-holder root cause + fix
phase: capstone-frontier
status: done
---

## Problem

Mission 7's ax29 duel (boss uid251@(1324,1920)) showed a periodic
teleport: `boss ak 1324 -> -7` on a ~200-tick cycle. The boss fought
normally (aB 800 -> 360), then slammed to x=-7 and walked back.

## Root cause — zombie `scrollHolder`

`fireScrollTriggers` tracks `scrollHolder` — the ScrollTrigger instance
standing in for the original's `k.ah` wall entity. Claims are gated by a
mode-1 lock (`scrollHolder == null || === t || mode != 1`), and
release/head-clear are `scrollHolder === t` identity checks.

`rebuildRecordStructs()` recreates **new** ScrollTrigger instances every
`loadPackI` (level load / fail-retry `loadMission`). `scrollHolder` was
**never reset** → after any reload it pointed at a stale instance:

- head-clear `scrollHolder === t` → always false → bounds never cleared
  by a holder;
- `!fired` release `scrollHolder === t` → always false → holder never
  released;
- claims blocked: stale holder is non-null, `===t` false, `mode==1` →
  `kAh` stays null forever;
- `kM(1)` head `if (kAh == null) kN()` → zeroes `boundMinX..boundMaxY`
  every tick (faithful — the original `k.m()` does the same when
  `k.ah == null`, k.java:2347-2348);
- the arena clamp `W[0] <= r12 || W[2] >= r13` (i.java:10522-10530,
  verbatim — no unset-bounds gate in the original either) then clamps
  against `r13 = 0` → `ak = 0 + (ak - W[2])` ≈ -7 every chase cycle.

Probe evidence: `holder=41 same=false fired=true contains=true
viewOv=true claimOk=false kAh=false` while the player stood inside
uid41's zone — the holder was uid41's *old* instance.

## Fix — `scrollHolder = null` inside `kN()`

`k.n()` (k.java:2861-2864, proven): `ah = null; R = S = T = U = 0`.
`scrollHolder` is the port's stand-in for `k.ah`'s claimant — it must
release when `k.ah` does. `loadPackI` already calls `kN()` before
rebuilding the trigger list (Level0World.kt:725), so the release is
automatic on every reload path (`loadMission` / fail-retry
`reloadCheckpoint(false)`). Checkpoint restore (`reloadCheckpoint(true)`
→ `reload()`, no `loadPackI`) keeps the same trigger list → holder
surviving is correct there.

## Legs (Slice297Test)

- `mission7ScrollHolderReload` — bounds arm pre-reload inside uid41's
  zone; `resetLevel(false)` (loadMission → loadPackI → trigger rebuild);
  intro replays; walk back inside → bounds re-arm. Pre-fix: post-reload
  bounds stayed 0 (claim lock wedged).
- `mission7BossArenaClamp` — real-input duel bot: boss leaves S0 (duel
  engages), `aB` drops below 800 (takes damage), and `ak` never falls
  below -50 across 3000 ticks incl. death/respawn; post-respawn the
  holder re-arms (no re-zombie).

## Also in the slice

- `iBy = 1` statics + per-load reset (i.java:22326 / :2475 `i.D()`,
  proven) — boss phase tier inits to the active tier, not the
  inert-boss arm 0.

## Note

The `-7` teleport artifact exists in the *original* too — the clamp has
no bounds-validity gate — but it's only reachable when the holder-desync
state is entered. The port's bug was entering that state at all.
