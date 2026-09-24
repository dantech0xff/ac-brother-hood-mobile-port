---
title: "Port slice 191 — i.f(this) scroll-wall clamp: remaining call sites"
phase: port
status: done
---

# Slice 191 — `i.f(this)` scroll-wall clamp call sites

## Finding (mining)

`i.f(iVar)` (i.java:5382, proven) is the scroll-wall barrier: while the
`k.ah` holder (ax37 trigger in `Z[3]==1` overlap mode) stands, it pins
the moving entity's `Y` box inside the holder's `X` bound per the
`Z[0]` side mask (&1 left, &2 right, &4 top, &8 bottom), zeroing the
velocity component and `ai`/`aj`. Player-only — all 17 `i.f(this)`
call sites sit inside g's motion arms.

The clamp body + `kAh`/ax37 claim machinery were ported earlier, but
only 6 of the 17 call sites were wired (S8 :1242, S9/10 arm :1293,
S375/376/377 :4253/:4282/:4309, `ay()` :5421). This slice wires the
remaining 10 reachable sites:

| g.java site | arm | ported position |
|---|---|---|
| :1406 | `case 16/35/43/150/252` fall | `fallArm` head |
| :1600 | air family `!y()` free-air path | `airFamily` inside `!hitWall()` block |
| :1740 | `case 233` spring-jump | `preJumpArm` tail (unconditional) |
| :1895 | `case 32` | S32 arm head |
| :3153/:3160 | `case 199` `u(4112)`/`u(8256)` | `case199` inside each dir-held branch |
| :5172/:5182 | `l()` S79 facing-branches | `l()` inside the already-facing `else` |
| :5319 | `ax()` sustained run | `ax()` tail before `return true` |

Not wired (faithful): the 3 sites in the `cv && aF != 0` bound-climb
branches (:1614, :1645 family — S25/15/19 while bound) — those branches
are unported; the climb states live in their own arms. S16/35/150 share
the original fall-arm case but don't dispatch in the port (separate gap,
flagged).

## Port

`PlayerFsm.kt` — 8 new `world.scrollWallClamp(p)` calls at the exact
positions above (interface no-op default; `Level0World` impl is the
verbatim i.f body against `scrollHolder.bound`/`mask`/`mode==1`).

## Tests (`Slice191Test`, 8)

- Call-site presence: S32 head, fallArm head, ax tail, l() S79 branch,
  case199 dir-held, preJumpArm tail, airFamily free-air — all assert
  `p in w.clampCalls` (MarkerWorld's recording override).
- `wall pin stops a run at the bound edge` — end-to-end: verbatim
  right-side pin math re-staged on a MarkerWorld override; S1 run with
  `Y[2]=235`, `ag=2560` crosses `wallX=240` → `ag=ai=0`, `ak=(200-235)+240=205`.

Caught while testing: `Y[]` is world-space box coords (matching the
existing i5382 mode1 test), not entity-relative — corrected staging.

## Gates

- `:core:test` → green (1238 tests incl. 8 new)
- verifier → `ok:true`; unittests → 57; `:android:assembleDebug` → ok;
  `:gdx:build` → ok
