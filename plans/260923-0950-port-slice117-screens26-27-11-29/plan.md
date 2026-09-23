---
title: "Port slice 117 — j.c 27/11/26/-1 screen states + j.c==29 i13 row cap"
phase: port
status: done
slice: 117
---

# Slice 117 — j.c 27/11/26/-1 screen states + j.c==29 `i13` row cap

## Scope

Closes the remaining reachable-but-unported `j.c` states around the IGP
store flow, plus a fidelity fix on the difficulty screen:

| j.c | Original (structured k.java) | Port |
|-----|------------------------------|------|
| 27 | case 27 (:1422-1435, **proven**) — IGP offscreen canvas: `cv==null→createImage(400,240)+cw`, `cd=cw`, `f.a(0)` pump → true → `l(2)` + release, `f.a(cd)` unconditionally | `menuJc27()` — `kCvOn` marker models the Graphics handles; `igpTick0()` returns true non-IGP → exits to `l(2)` first tick |
| 26 | **no `case 26` exists** — `a()`'s switch jumps 25 → 27 (:1388/:1422); a dead screen consuming ticks verbatim | `26 -> { }` consume arm + added to `menuStates` |
| 11 | `case 11: j.c = -1; return` (:1104, **proven**) — the suspend/exit state | `11 -> jC = -1` |
| -1 | no `a()` case either — consumes ticks | `-1 -> { }` consume arm + `menuStates` |
| 29 | `i13 = bA[69]!=0 ? 3 : 2` row cap in `L(i13)` (:1440, **proven**) | `stateL(29)` now sets `kEy = if (kBA[69]!=0) 3 else 2` |

## Fidelity fix: jc29 HARD row

Case 29's body runs `L(i13)` per frame with `i13 = bA[69]!=0 ? 3 : 2`
(k.java:1440) — the HARD row is neither drawn nor navigable until the
hard-mode unlock byte is set. Our `bannerK(2)` unconditionally armed
`kEy = eA[2].size = 3`, leaving the locked row reachable. `bA[69]`
cannot change mid-screen, so setting the cap at the transition is
equivalent to the per-frame write. `menuL(kEy)`/`menuRows()` then read
the capped count → nav clamp + draw count both fixed.

## Real bug fixed: j.c==27 fell through to the play tick

`menuItem 32/33/34` (save slots) → `stateL(27)` existed, but 27 was in
neither `menuStates` nor the `kAl` freeze set, so the frame routing at
the tick top sent j.c==27 into the **entity sim** — a live-world tick
running underneath a dead screen. Same hole covered j.c==26/-1. All four
now route to `menuFrame()` where their verbatim arms run.

## New non-IGP stubs (labeled)

- `igpZ()` — `Z()` (k.java:5456): `f.a()==0` check + `eA[0][3]` promo
  stamp. `f.a()` (f.java:755) is `aE&&g()>0 ? 0 : -1` → const `false`.
- `igpTick0()` — `f.a(int)` (f.java:1185): `!aE → true`.
- `igpBlit()` — `f.a(Graphics)` (f.java:1438): `(aE||bZ)&&!bZ` → no-op.
- `enterIgp()` — `f.a(String,0)` (f.java:759): `enterIGP("LOADING",0)`,
  wired into `menuJc25`'s fire arm where the original calls it (:1410)
  — replaces the "unported" comment.

## Gates

- `scripts/verify-static-reconstruction.py` → `"ok": true`
- `python3 -m unittest discover -s tests -t .` → 57 tests OK
- `:core:test` → green (+10 `Slice117Test` tests)
- `:android:assembleDebug`, `:gdx:build` → BUILD SUCCESSFUL

## Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt`
  — `menuStates` ±`{-1,11,26,27}`; `menuFrame` arms `26/27/11/-1`;
  `menuJc27()` + `kCvOn` + `igpZ()/igpTick0()/igpBlit()/enterIgp()`;
  `menuJc25` fire arm calls `enterIgp()`; `stateL(29)` sets `kEy` cap.
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt`
  — `Slice117Test` (10 tests).
