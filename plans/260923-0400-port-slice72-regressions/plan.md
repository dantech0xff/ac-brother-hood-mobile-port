---
title: "Slice 72 — emulator regression fixes (boot crash, invisible modal, camera lock)"
phase: port
status: done
---

# Slice 72 — emulator regression fixes

The `main-progress-showcase` emulator run (testing agent, PR #82 build)
surfaced three regressions on main. This slice fixes all three.

## 1. Renderer boot crash — clip-pack path map (proven fix)

`Level0Renderer.create()` resolved pack module dirs through a `when(packId)`
that only listed a few ids; packs 20/42/46 fell into the `else` tileset
branch and produced `level0/tileset--20` → `FileNotFoundException` → FATAL
at boot on the stock build.

Fix: compute the path — positive `packId` → `clips/clip$packId/modules`,
negative → `level0/tileset-${-packId}/modules` (literal dash; dirs are
`tileset-10` etc.). Matches the converter's output layout.

## 2. Invisible `k.l(21)` modal (inferred presentation)

`k.l(21)` arms a modal dialog that freezes the sim; nothing was drawn, so
~15 screen presses at boot appeared to do nothing. Added a bottom dialog
panel (380x60 dark box + accent lines + hint marker) in `Level0Renderer`
while `dialogModal` is armed. Panel art is `inferred` — the original draws
into the UI pack; geometry/behavior (bottom-anchored, dismiss on press)
mirrors `k.l(21)` semantics.

## 3. Camera scroll-bound lock — ax37 `al()` release semantics (proven)

Runtime evidence: `bnds=9,0,0,0` + `kAh` armed from tick 1; teleporting to
x3000 left the camera pinned at ~8,743.

Root cause had two parts:

- `kAh` synthesis set `aF = 1` and `W = bound rect`. The aclv record for
  the spawn strip is `zone=[9,756,249,979] bound=[9,756,41,979] mask=1` —
  the degenerate 41px bound made m()'s L282 wall clamp
  (k.java:2404-2421) force `camA = 41-400 = -359`, then the `kR` floor
  re-floored it to 9 — `camA` pinned at 9 forever.
- `al()`'s claim/release lifecycle was unported: bounds could be set but
  never released.

Fix — faithful port of `i.al()` (i.java:7053-7159) into
`fireScrollTriggers`:

- `scrollHolder` tracks the `k.ah` claimant.
- Holder head-of-`al()` reset: clears `R/S/T/U` every tick then re-writes
  (i.java:7062 L8) — bounds always reflect the currently-firing set.
- Claim on containment `b(k.aS.W, this.W)` unless a mode-1 (overlap)
  holder already stands (i.java:7131-7143 L79/L85).
- `k.n()` release when the holder's zone stops firing (i.java:7068/:7230).
- `kAh` entity now carries `W = zone` and `aF` left unset — every level-0
  ax37 record carries `aF=0`, so the L282 wall clamp skips ax37 as in the
  original (bound bits `R/T/S/U` still apply via mask).

Also updated `Level0WorldTest.i7053` — it asserted the old
persist-forever model; it now asserts the ceiling holds inside the zone
and `k.U` clears on exit.

## Tests

- `Slice72ScrollReleaseTest.bound release on zone exit frees the camera`
- `Slice72ScrollReleaseTest.spawn wall does not pin camera`
- `Level0WorldTest.ax37 trigger writes camera bounds on containment i7053`
  (revised for release semantics)

## Gates

- verifier `ok:true`
- `python3 -m unittest discover -s tests` — 57 pass
- `:core:test` — 570 tests green
- `:android:assembleDebug` — builds

## Left honest

- The `Z[2]!=-1` linked-entity gate (i.java:7119-7127) remains skipped —
  all level-0 records carry -1.
- `k.D()` autoscroll (missions 1/4) still deferred — dead code on level 0.
