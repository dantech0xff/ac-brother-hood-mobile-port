---
title: "Slice 106 — L157 goal-milestone blit + win-path regression tests"
phase: port
status: done
---

# Slice 106 — `z[9]` milestone blit + win-path verification

## Mined (proven)

- `k.java:3321-3358` — the L142 tail's goal arm: while `k.aV.Z[0]==1`
  (armed/disarmed by claim arg-ops `(37,1,4)`/`(37,1,5)` — i.java:18118/18121)
  and `aV.S∉{4,5}` and `j.c∉{13,31}`:
  - `aV.w()==1` (goal `ak` in `(ac[2], ac[2]+200)` — the 200px band just
    past the camera right edge): if `j.f%2==0`, draw
    `z[9].a(cd,38,0,360,120,0,0,0)` — the milestone font blink at
    (360,120). `j.f` = the 62ms tick counter, so it pulses on even ticks.
  - `aV.w()==2` (goal `ak ≥ ac[2]+200` — more than 200px ahead): `bx=56;
    l(13); bw=0` — the scripted win. `ac[2]=O+400` = camera right edge
    (k.java:2085-2088: `ac` = `{O, P, O+400, P+240}`).
  - `w()` verbatim (i.java:632-640): `v()` → 0; `ak≤ac2` → 3; band → 1;
    `ak≥ac2+200` → 2.
- `z[9]` = pack-3 entry-009 = our `clips[9]` (109 anims, anim 38 exists —
  already loaded).

## Ported

1. **`goalTicker` parity fix** (`Level0World.kt:1600`): was
   `if (tickIndex&1==0) goalTicker=true` — a sticky flag (once true, stays
   true on odd ticks = constant draw, no blink). Now
   `goalTicker = (tickIndex and 1L) == 0L` — drawn only on even ticks,
   matching `j.f%2==0`.

2. **Milestone blit draw** (`Level0Renderer.kt:1013`): `if (world.goalTicker)
   drawFrame(9, 38, 0, 360, 120, 0)` in the HUD overlay tail, after the
   aO/aP timed line — same z[]-bank `a(cd,anim,frame,x,y)` call, pack 9.

3. **Win-path regression tests** (`Slice106Test`): the chain is
   `initAx9` binds `kAV` (uid-108 ax9, `fields[5]==0`, at 6177,758) →
   script arg-op `(37,1,4)` sets `Z[0]=1` → `w()==2` → `screenL(13)` +
   `kBx=56` + `kBw=0`. Tests assert the bind, the `Z[0]=1 → won` fire
   (`l()` remaps 13→31 for the milestone variant — both latch `won`), and
   band `goalTicker` alternation.

## Diagnostic outcome (the E2E "script-wait freeze")

Not a script stall: the win is fully **scripted**, not traversal-gated.
`Z[0]` only arms via claim arg-op `(37,1,4)`, and while the goal sits
>200px past the camera (true almost always during approach — goal at
6177, spawn camX≈0), any `Z[0]=1` instantly fires `l(13)`. Teleport
probes failed because they skipped the trigger/script sequence that arms
`Z[0]`, not because a wait op parks. Claim scripts are step-gated
(`r06 ≤ scriptStep`), always resolving — no unported wait exists.

## Gates

- verifier `ok:true`; 57 unittests OK; `:core:test` green (incl.
  `Slice106Test` ×3); `:android:assembleDebug` + `:gdx:build` OK.
