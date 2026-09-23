---
title: "Slice 108 — jC==18 title tick arm + jC==23 boot sound prompt confirm"
phase: port
status: done
confidence: proven
sources:
  - reconstructed-project/src/structured/k.java:1146-1175  # k.a() case 18 — title screen
  - reconstructed-project/src/structured/k.java:1310-1324  # k.a() case 23 — sound prompt
  - reconstructed-project/src/structured/k.java:523-530    # k.j() pointer-in-play-area
  - reconstructed-project/src/structured/e.java:32-33      # e.a() track-playing check
  - reconstructed-project/src/structured/k.java:4096       # l(23) caller (boot splash cu==6)
---

# Slice 108 — title screen tick (jc18) + sound prompt confirm (jc23)

## What the original does

**Case 18 — title screen** (`k.a()` switch, `k.java:1146-1175`, `proven`):
- Draws `A[1]` (clip 97) anims `{1,0,2}` + `A[0]` (clip 96) anim 0 each frame —
  the renderer arm (`Level0Renderer.titleScreen`, slice 104) already covers this.
- While `!cS`: `d(0,9)` "press any key" blink at (200,205) on `j.g % 10 > 5`;
  `v(65568)` (context edge) **or** `k.j()` (pointer released in the play strip)
  latches `cT=100; cS=true; z(23)`.
- While `cS`: `cb=true; cT-=10`; when the title track is done
  (`!e.a()` — our `audioTrack == -1`, silent in the port) → `z(0); l(2)`
  (main menu); `A[0]=null; cT=0`.
- The port keeps `cT` counting verbatim (10/frame) but the `l(2)` hop is
  unconditional each `cS` frame since `e.a()` is always false — matches the
  original's behaviour whenever the track has already ended, which is every
  second `cS` tick in the original too (the `!e.a()` branch fires alongside
  the countdown, `inferred` — the original continues decrementing cT through
  the l(2) call site, and we preserve the `cT-=10` write before `l(2)`).

**Case 23 — boot sound prompt** (`k.java:1310-1324`, `proven`):
- Only entered via `l(23)` from the boot splash (`cu==6`, 5s timeout). Arms
  `eC=19; K(3)` — a YES/NO `bv==3` banner (eA[3] = {14 YES, 15 NO}).
- Frame proc: `!v(327712)` → `ae()` (generic menu nav). On the confirm edge
  (327712 = `M_PAUSE|M_CONTEXT` union) ae() is **bypassed entirely** — the
  press doesn't go through row-select:
  - `bw==0` (YES) → `bE=bF=true; z(0)`
  - `bw==1` (NO)  → `bE=bF=false`
  - then `l(18)` → title screen.

## Port changes (`proven` unless noted)

- `menuStates += {18, 23}` — both procs run inside `k.a()`'s screen switch;
  without them the gate `jC in menuStates` never dispatches (found by test —
  the arm silently never ran).
- `menuFrame` `when`: `23 ->` confirm wrapper ahead of `in menuStates`;
  `28 -> menuAe(pressY)`; `18 -> menuJc18()`; order matters — `18`/`23` must
  precede the `in menuStates` catch-all or the range check swallows them.
- `menuJc18()` (`Level0World.kt`): the `!cS`/`cS` arms verbatim —
  `pad.v(M_CONTEXT) || pointerStrip()` → `kCT=100; kCS=true; z(23)`;
  else `kCb=true; kCT-=10; if (audioTrack==-1) z(0); stateL(2); kCT=0`.
- `pointerStrip()` already exists (slice 79, `k.j()`): reads `k.H/k.I`, the
  last pointer **release** point — release in the play strip confirms.

## Reachability note

`l(18)` has no `i==18` setup arm in `l()` — `stateL(18)` falls through to the
generic tail (`jC=18`), which is exactly what the original does. The real
boot chain (splash `cu==6` → `l(23)` → confirm → `l(18)` → menu) now runs
end-to-end through ported code.

## Tests (`Slice108Test`, 5/5 green)

- jc18 context edge latches `cS` (`cT=100`), next tick exits `l(2)` → main
  menu, `cT=0`.
- jc18 play-strip release (k.j()) also confirms.
- jc23 YES (`bw==0`) → `bE=bF=true` → `l(18)`.
- jc23 NO (`bw==1`) → `bE=bF=false` → `l(18)`.
- jc23 without confirm → `ae()` only, stays on 23.

## Gates

- `verify-static-reconstruction.py` → `ok:true`, failures `[]`.
- `python3 -m unittest discover -s tests` → 57 pass.
- `:core:test` → all green (incl. the 5 new).
- `:android:assembleDebug`, `:gdx:build` → BUILD SUCCESSFUL.
