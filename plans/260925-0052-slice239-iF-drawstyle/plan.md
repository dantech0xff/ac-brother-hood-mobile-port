---
title: "slice 239 — i.F() draw-style/FX proc"
phase: port
status: done
slice: 239
---

# slice 239 — `i.F()` per-entity draw-style/FX proc

Ports `i.F()` (i.java:11782-14030, proven), the ~2.2k-line draw-style
proc that `k.I()`'s second `bd[]` pass (k.java:10032-10150) runs on each
draw entry, its `ad` link (minus ax76/ax29), the `k.E` companion, and the
player's `ae` marker.

## What landed

- **`Entity.drawStyleF(w): Int`** — the full proc: head gates (ax43/aw749
  claim suppress, ax11/17/73 alert anims → `az=99`, ax0 locomotion →
  `az=100`), ax15 S9/S10 marker arms (rope lines, `i.r` box, af-hop
  chain-walk tombstoning ax14 zones), the L455 palette/remap/alpha
  `when` dispatch (~20 ax arms incl. the `bh[k.aj]` guard, the ax9
  `Z[2]==47` burst chain, the `k.bo` outfit table, `i.by` remap
  flicker), the L910 parked-`s()` advance, the player FX counters
  (`k.C.cd[2]` shake counter, the `i.bB/bF` flash oscillator, `g.t--`
  La84, `i.e--` La92 + the 360-slot sparkle rosette), the Lcac counters
  (ax24 S11, ax64 scale ramps, ax -999 sentinel), and the Leec tail
  (ax60 `bk()`/ax40 `by()` renderer notes, ax11 speech bubble +
  `Z[20]` countdown, ax43 veil). Returns 0 = suppress draw, 1 = draw.
- **`Level0World.drawStylePass()`** — the k.java second pass: clears the
  FX collectors → `buildDrawList()` → per-entry `ad.F()` (excl. ax76/29)
  + `F()` → gated `kE.F()`+`s()` under `jC==8`/`jC==21 && u==8`. Runs
  once per tick, before `tickIndex++`/`jG++`.
- **`drawFx*` collectors** — `fxLines/fxRects/fxDots/fxBubbles` (+text)
  on the world; `Level0Renderer.fxOverlay()` drains them after entity
  draws (lines, veil rect, bubble fill+outline+nub+text, sparkle dots).
- **`NpcFsm.aUDraw(e, w, player)`** — `i.aU()` (i.java:32121): F()'s
  ax10 arm; holds the S34 slope rail verbatim. `tickTrigger`'s `34 ->`
  arm is now empty — source `aV()` S34 is a trivial `L5e9` (proven).
- **Decrement re-sites (fidelity fixes):** `i.e--` moved from the
  unconditional per-tick write to F()'s La92 player arm; `g.t--` moved
  from PlayerFsm's per-tick write to F()'s La84 (gated
  `jC==8 && !claimed-cd[2] && !g.s && !(i.bB && i.bF!=-1)`).
- **`Entity.K_BO`** = `{{0,-1},{3,1},{5,2},{6,3}}` outfit palette/remap
  table (k.java:24890, proven); `j0Frame`, `ax64Scale` entity fields.

## Test fixes (proven source-wrong, not workarounds)

- `iframes block a second drain for 10 ticks` — `g.t` only decays on the
  draw path while no `cd[2]` claim owns the player; the fixture's ax5
  director holds one. Now releases `kC.cd[2]` and drains via
  `drawStyleF` (the verbatim site).
- `Slice122Test` S34 rail tests — rail moved to `drawStyleF` (aU draw
  path), not `tickTrigger` (aV tick path, proven trivial).
- Slice239 test fixtures: uid `424242` (real records collide at 42);
  entities positioned at camera center (`au` in-play threshold).

## Flagged ambiguities (labeled in code)

- ax15-S10 stripped `aS.S` constants (~233/234/235) — `inferred`.
- af-hop chain guard `<64` — `high-confidence` (source loop bound stripped).
- ax43 veil colour `0x88000000` — `inferred`.
- ax64 scale `80-(80*cA/cz)` range const — `inferred`.
- ax -999 `+20` width — `inferred`.
- `g[i]<0` sparkle flip — proven-dead (g just set ≥60).
- `aU()` S31/L437 arm — unmined, labeled.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → green incl. `Slice239Test` (15 tests)
- `:gdx:build`, `:android:assembleDebug` → green
