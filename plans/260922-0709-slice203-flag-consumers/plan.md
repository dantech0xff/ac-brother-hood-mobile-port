---
title: "Slice 203 — e() tail flag consumers + air/fall wall-grab blocks"
phase: port
status: done
---

# Slice 203 — flag consumers now that the flag model is faithful

Slice 202 made the per-tick flag latch model faithful (`cp/cq/ct/cu/cv/cw/z`
cleared at e() head, re-armed per state). This slice ports the five
consumers that read those flags in `e()`'s shared tail, plus the two
wall-grab blocks that consume the `aF` grab-intent latch.

## Mining receipts (fallback g.java, proven)

Tail order after the state dispatch:
`z→ap()` L383c (:7959) → `aO==7||9 → cq=0` L3852 (:7968) → g.A block →
`cq&&!E→jump` L38b8 (:8020) → jump inners → back-dash L3a17 (:8188) →
`cp` corner-9 gate L3a2d (:8199) → `ct` ledge-mount probes (:8203) →
`cu` ledge-drop L3a71 (:8235) → `cv→aF` producer L3a9d (:8253) →
`cw` ceiling-grab L3ad8 (:8279) → `return` L3b14 (:8308).

- **L3852** `aO==7 || aO==9 → cq=0` — head cell 7/9 drops the jump latch
  *before* L38b8 reads it. Ported at `postTail` head (`PlayerFsm.kt`).
- **L3a2d** `cp && (aO==9||aP==9) && aQ==9` corner-9 gate skips the
  consumer; `ct && (ak()||al())` fires the ledge-mount probes — `ak()`
  = `ledgeLipGrab` (`i(60)` snap), `al()` = `ledgeHangGrab` (`i(61)` +
  `aC=40`) — and zeroes `ag/ah/aj` on a mount.
- **L3a71** `cu && v(33024)` (DOWN edge) → `a(2560)` fast-drop
  (`flingAirborne`); `ab.ax==14` → `H()` (`dropHeld`).
- **L3a9d** `cv && (u|v)(16388|8|2)` → `aF=1` — the grab-intent
  producer; `aF` persists across ticks (not in the head clear) until a
  grab site or `l()` head consumes it.
- **L3ad8** `cw && aO==5` → `i(280)` + motion zero +
  `al = (W[1]/20)*20+10` ceiling snap.

## Wall grabs (proven)

- **L1e3e/L1e6e → L1e94** (air arm :4578-4730): `y()` gates only the
  grab check — `cv && aF!=0` or `S==215`, `!ba`, side cell 20
  (`av?aT:aU`) → snap + `aK` marker + `i(101)`, `goto L353d`. The
  prior port's `if (!hitWall)` wrapper wrongly skipped av()+land on
  wall contact — restructured: grab check inside `hitWall`, then
  `av()`+land checks run unconditionally; `L1f84` `S∈{25,15,19}` ±512
  drift clamp added for the contact-no-grab path.
- **L2231/L2268 → L2298** (fall arm :5049-5210): reached on the
  `gk==-1` no-bound path. The prior port had `aF!=0 && !ba → return`
  (grab conditions *exiting*) — inverted: grab-gate success →
  `wallGrabSnap`; failure → `L2361` ±512 drift.
- **`wallGrabSnap`** shared helper: `aF=0`, `ag=ah=ai=aj=0`,
  `S==215 → av=!av` (air site only), `ak` wall-edge snap
  (`av: ((W[0]+20)/20)*20+1`; `!av: ((W[2]-20)/20)*20+19`),
  `a(8,5,17,201)` marker (`aK.av`, `N/O=pos<<8`, pos, vel0, `t()`,
  `P=512`, `k.b`), `i(101)`.

## `cv` arm correction (proven)

`cv=1` lives at **L1ce4** (:4410) — the entry for cases {18,19,23,36}
direct and 22 via `L1cc5`. Cases {20,24,25,157,215} enter at L1ce8 and
never arm `cv`. `airFamily` now arms `cv` only for S22/23.

## Behavioral notes

- A cell-20 wall *at hand row with a clear pocket* is ALSO a mountable
  lip — postTail's `ct` probes run after the grab (the original's
  `goto L353d` enters the tail *before* the consumers), so the lip
  mount wins when it matches. A real S101 stick needs a tall wall
  (pocket `e(i2,i4-1)>0` kills `ledgeLipGrab`). Verified by the tests.
- `aF` consumed by a grab is immediately re-armed while the direction
  tap is still held — the latch models "still pushing toward the wall".

## Tests (Slice203Test, 12)

cv→aF produce + persistence; fall grab → 101 (+marker +vel0) and the
re-armed aF; S35 no-intent drift ±512; air grab via cv+aF (S22, no
flip); S215 grab + av flip; ct lip mount → 60 + vel0; cu drop → 43
(ah=2560, +10) + ax14 `dropHeld`; cw ceiling grab → 280 + al snap + vel0;
aO==7 cq clear vs open-head jump control.

## Gates

verifier `ok:true`; `python3 -m unittest` 57/57; `:core:test
--rerun-tasks` green; `:android:assembleDebug` green; `:gdx:build` green.
