---
title: "Slice 133 — i.a(boolean) side-strip rescan + S9/S10 stagger arm + al()/H()"
phase: port
status: done
---

## Scope

Started as `g.aB()` + `i.al()` backlog items; mining turned up a bigger
real port. All `proven` unless noted:

- **`i.a(boolean z2)` → `probeSnapSides`** (i.java:829-915): side-strip
  rescan — `v=true`, `x()` probe, clears `bb/bc/ba`/`aT`/`aU`, scans the
  left/right columns head-row→`W[3]-10` (player crouch states 12/7/32/
  199 add 10 more), records worst cell per side in `aT`/`aU`, clearance
  in `aX`/`aY`, flags `bb`/`bc` at ≥18 with early break. `z2 && v` then
  snaps `ak` off the flagged wall and re-probes; `z2`'s `bd` arm also
  pre-adjusts `al` by the ground strip. Ends `t()` + writes the
  `i.t`/`i.u` W-box center (ported as `centerX`/`centerY` — Kotlin JVM
  can't have `t` alongside `T`). Called from ~10 live sites in g.
- **`i.M()` → `floorAhead`** (i.java:5849): feet-level cell one column
  into facing is `>= 12 || >= 5` (verbatim — the second conjunct
  subsumes the first).
- **`i.al()` → `ledgeHangGrab`** (g.java:209-239): wide ledge mount —
  probe column `(W[0]-20)/20` / `(W[2]+20)/20 + 1` at hand row must be
  ≥19 with the 2×4 pocket clear → `ak = i2*20(+20)`, `al = i4*20-1`;
  cell 21 → bare true (mount deferred); else `k.v()` + `i(61)` +
  `aC=40`. Dead in the original (only caller is the dead case-0 block)
  but ported verbatim.
- **`i.H()` → `dropHeld`** (i.java:3684): `ab.p() + ab = null`.
- **S9/S10 shared arm** (g.java:1265-1290): `ag/=2` decay + `ab` tracks
  the player box + side-strip ≥19 kills `ag`; `r()` → `bl=0`, `G()`,
  `H()`, `i(1)`, `a(false)`, then `!M() && a==null → a(0)`; `Q∈{0,54}`
  pulses `ah=1` + `a(true)`; tail `i.f(this)`.

## Dead code documented, not ported

The g.java:824-844 case-0 block (`cp && ct` ledge-grab, `cu` down-drop,
`cv` aF-latch, `cw` one-way hang) is **proven dead**: e()'s head clears
`cp/cq/ct/cu/cv/cw` every tick (:617-623) and case 0 sets none of them
→ all four checks read false. Left as an omitted-with-citation comment
in `groundedTail`; porting it would *create* behavior the original
doesn't have (idle-at-edge grabs).

## Gate results

verifier `ok:true`; unittest 57; `:core:test` 917 (+12); android + gdx
builds green.
