---
title: "Slice 169 — S33 wall-rebound branch-order fix (fidelity bug)"
phase: port-slice-169
status: done
---

# Slice 169 — S33 wall-rebound branch-order fix

## What

Traversal probing at the x800 plateau edge surfaced a real fidelity bug in
the ported S33 (wall-rebound) arm: **the two input branches were inverted**
vs the original `g.e()` dispatch.

**Original** (`reconstructed-project/src/fallback/g.java` L1ab3–L1c33,
proven):

```
if (A()) -> ladder snap i(74)
else if (dirKey && ah < 0) { ct = 1; -> lip-scan (r()-gated pocket scan -> ak()) }
else { x(); aR|aS in {5,20} -> a(43,32) fall; else ct=0, ag=0, ah=1536,
       a(34,36) + aA() wall-kick }
```

i.e. holding the direction **toward** the wall *while still rising* is what
arms the `r()`-gated pocket scan → `ak()` lip grab. Any other input state
(no direction held, or holding while `ah >= 0` falling) runs the probe:
edge cells `{5,20}` → `a(43,32)` fall, otherwise the `a(34,36)`+`aA()` kick.

**Ported (old, wrong)**: `if (dirKey) probe->fall/kick else if (ah<0)
lip-scan` — exactly backwards. Consequences observed live:

1. Holding right during the rebound always degraded to the fall branch at
   a pit lip (`aS==20`) — the climb chain could never gain height.
2. Falling in S33 with **no** direction held ran *no* arm at all — the
   player froze in S33 on the pit floor indefinitely (observed 30+ ticks
   at x779,y899 in probe).
3. The lip-scan was unreachable the one case it was designed for.

## Fix

`rewrite/core/.../PlayerFsm.kt` S33 arm — reordered to the original:
`dirKey && ah < 0` → `ct=true` + `animFinished()`-gated pocket scan +
`ledgeLipGrab`; else → `probeCells` → `aR|aS∈{5,20}` → `a(43,32)` else
`a(34,36)`+`aA()` kick. Doc comment updated to the true semantics.

## Traversal findings (context, all verified faithful)

- **Mounted touch wheel is fixed-position** (`k.cm==1`, Level0World.kt
  :3594-3629): D-pad rect `x∈[-5,111], y∈[124,240]`; RIGHT cell (95,180);
  attack radial (305,200)=cell4→mask32⊂CONTEXT; jump radial (355,145)=cell1.
- **ax4 crates at x504 break correctly**: slash arms S5/7→S6/8
  (`tickDestructible` L5/L24), post-arm anim ends → wisp burst + removal
  (verified `gone=true` ~3 ticks after arm).
- **x800 wall face is not climbable** — faithful geometry: the r39-40 roof
  continues left of the x840 column (cx40-41 solid), so the pocket scan's
  lip pattern can't form; the original rebounds+kicks off it too. The pit
  below (x800-1240, open r44+) is real level space, not a port bug.
- **Camera tracking verified working** — the earlier "frozen camera"
  probes were the mission-fail screen (`al > camY+240 → stateL(12)`),
  not a tracker bug.
- jC==21 intro dialog is a transient — auto-closes ~t70 (intro claim).

## Tests

`Slice169Test` (3 tests): rising+held → stays S33 with `ct` armed;
falling+held → probes out of S33 (fall/kick); falling+no-hold → still
resolves out of S33 (the old freeze). Probe classes removed before commit.

## Gates

- `:core:test` green (incl. Slice169Test + full regression)
- `:android:assembleDebug` + `:gdx:build` green
- `python3 -m unittest discover -s tests` — 57 OK
- `verify-static-reconstruction.py` → `"ok": true`
