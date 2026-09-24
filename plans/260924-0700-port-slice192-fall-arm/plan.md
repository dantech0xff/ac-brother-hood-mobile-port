---
title: "Port slice 192 — shared fall case complete (S16/35/150 + bound catch + grip drag)"
phase: port
status: done
---

# Slice 192 — `case 16/35/43/150/252` shared fall arm, completed

## Findings (mining)

The fall-arm case at g.java:1400-1475 covers five states — the port only
dispatched `43/252`. S16/35/150 hit the default arm (dead state). The
arm body also had unported content beyond the i.f head (slice 191):

1. **`I == 4 → z = true`** (:1414, proven): weapon slot 4 arms the
   grounded flag mid-fall.
2. **L1425 bound catch** (:1428-1445, proven): falling while inside an
   S36 context zone — `gk != -1 && ((go != 0 && al < go) || go == 0)` —
   re-binds instead of falling:
   - `gk == 0` → `a(29,32)` ride-mount state, `ak = gn` (zone center-x);
   - `gk == 1` → `ak = gd.W[0]; t(); i(315); av = gd.av; ak = gd.W[2|0]`
     (carrier snap — S29/315 already dispatch as climb arms);
   - then `ag = 0; ah = 1536; aj = 0` (drift parked).
3. **L1430 wall-grip tap block** (:1447-1455, proven): `y()` (hitWall) +
   `u|v(2|8|16388)` directional input → bound gate (`aF!=0 && !ba` exits
   the case) else `ag = ±512` drift drag. Ordering note: for S43 the
   head's `S==43 && y()` arm already zeroed `ag`, so the `ag != 0` drag
   is unreachable on S43 — fires on 35/16/150/252 only (proven order).
4. **:1465 tail** (proven): `S ∉ {43,150,35,29,252} && r() → i(35)` —
   S16 door-glide resolves to the S35 loop-fall on anim end.

## Port

- Dispatch: `16, 35, 43, 150, 252 -> fallArm(p, pad)` (pad needed for
  the tap-block input masks).
- `fallArm` gains: `gI==4 → z=true`; the `gk/go/gn/gd` bound catch
  (`enterStateMasked(29,32)` for gk==0; `gd`-snap + `setAnim(315)` for
  gk==1, with the existing-`gd?.let` null guard — the original's
  post-dereference null check is provably dead); the `y()&&dir` drag
  (`aF!=0 && !ba → return`; else `ag = ±512`); the `i(35)` tail.

## Tests (`Slice192Test`, 8)

- S16 routes to the arm (clamp called, flags set, `aj=1536`).
- S16 → i(35) on anim end; S35 self-loops (exclude set).
- gk==0 → a(29,32) + `ak=gn` + parked velocities.
- gk==1 → i(315) + `av/ak` from `gd` (real `world()` clips — the :1465
  tail re-checks `r()` on the new S315; clipless always ends).
- `al >= go` gate → no catch.
- Wall-grip drag on S35 (`ag=-4096` → `-512`) — S43 variant proven
  unreachable (head zeroes ag first).
- `gI=4 → z=true`.

## Gates

- `:core:test` → green (1246 tests incl. 8 new)
- verifier → `ok:true`; unittests → 57; `:android:assembleDebug` → ok;
  `:gdx:build` → ok
