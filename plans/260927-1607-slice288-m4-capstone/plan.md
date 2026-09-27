---
title: slice 288 — mission-4 capstone bot: full-shaft climb → director aA=7 win
status: done
---

## Result

The capstone bot completes mission-4 (aj=4, pack-10 — second vertical
bh3 flying ESCAPE canyon) end to end: `M4CAP won=true t=3104 leg=10
minAl=-9448 dirAA=7 jC=22`. Route: spawn → perch aw328 → shrine ×3 →
three mid-shaft S31 claim-QTE gates (aw326/330/332) → perch aw324 →
top claim aw338 → top arena director fight → 5 ax32 walls destroyed →
`mask==62` → director `aA=7` finale → `k.l(15)` → mission-complete.

## Root cause of the aA=7 "deadlock" (investigated this slice)

The finale arm (i.java:18395-18430, verbatim):

```java
L227: ag=0; ah=0; ad=null; bj=true; bT=false; k.aS.ag=0;
      if (v() == false) goto L230;      // director's on-view?
      k.aS.ah = k.Y;                    // player rides conveyor
L231: k.aS.t();                         // refresh boxes
      if (k.aS.v() == true) goto L235;  // player on-view?
      k.l(15); return;                  // WIN
L235: ...puff... return;                // fireworks, skip tail
L230: k.aS.ah = k.Y - 2560;             // plunge → L237 tail
```

For ax21 `v()` reduces to `au <= i` then `overlapI(camRect, this.Y)`
(i.java:730-790). In the port the director's `Y` was always
`[0,0,0,0]` → `v()` always false → only the L230 plunge arm ran →
infinite descent, `k.l(15)` unreachable.

**Cause: the test `world()` clip map lacked clip-13.** `bi[21]=13`
(proven) — the director's clip never resolved in tests, `refreshBoxes`
early-returned on `clip == null`, leaving zero boxes. Production
`Level0Game.kt` already loads `clips[13]` — only the test harness
was missing it. With the clip present: `dir.Y=[451,-9656,584,-9221]`
overlaps camRect (`v()` true) while the player's `Y=[487,-9497,583,-9449]`
sits right of camRect x-range 81-481 (`v()` false) → `k.l(15)` fires
immediately at the aA=7 arm, exactly as designed.

## Fixes kept (all proven against i.java/k.java)

1. `world()` test clip map: + `13 to Clip.load(clips/clip13)` —
   mirrors production (Level0Game.kt:121 `clips[13]`).
2. `waypoints` WaypointPool split: ax55 records feed BOTH the runner
   pool (`waypointPool`) and the director/pursuer pool (`waypoints`)
   — `c.a(short[])` (k.java:6049) loads one `c` pool; the port had
   split it and the director's half starved. `reset()` on reload
   matches `c.a()`'s re-init (i.java:2567).
3. Below-camera `B()` check (i.java:1386-1389 → `l(12)`): the old
   `else if` at end-of-tick gated on the tick-top `claimSuspended`
   LOCAL — stale on the tick a claim binds (the op runs mid-tick,
   after the local was computed). In the original `B()` sits inside
   `n()`, the player's own tick, so it always runs before any
   entity's claim ops and never sees a post-op camera pan — m4
   script-41's op11 snap + op21 hold can never arm it. Ported
   equivalent: the gate is now a LIVE re-check
   (`claimSuspendsPlayer()`) evaluated at the check point — on the
   bind tick the claim is already bound, so the gate is closed.
   (An earlier version of this slice moved the check inside the
   `!claimSuspended` block before `playerFsm.tick` — that position
   reads a stale camera and regressed `assassination finisher kills
   a weakened locked soldier`, which seeds the camera 250px from
   the player; the end-of-tick live-recompute keeps it green and
   is the closer match to the original ordering.)
4. `tickDirector` L73f gate inversion: `(!iAH || !kAm)` — was
   `(iAH || !kAm)`, which re-fired the timewarp every tick and
   decayed kX→0 (i.java:12414-12419).
5. `tickDirector` aA=3 loop-var bug: `e.Z[r93 + 13]` / `e.Z[r94 + 13]`
   — was hardcoded `e.Z[106]`/`e.Z[107]` slot reads via literals
   93/94 that never indexed the loop.
6. `tickDirector` L225 chase-pause: `r015 == null || r015.S != 22`
   → `d(false)` — matches the S22 pv3 charge-pose pause semantic.
7. `projSweepBc` cF gate: `!w.cFFlag` (was stub `!w.iCF`).

## Evidence

- `Slice288Test` passes: `won=true t=3104` (previously deadlocked at
  `aA=7` with the player plunging to -1.6M).
- The director's aA chain verified in trace: aA=8 chase → aA=5/6
  wall-watch (`l` accumulates 0→62 as the 5 walls die) → mask==62 →
  aA=7 → `k.l(15)` → jC=22.
