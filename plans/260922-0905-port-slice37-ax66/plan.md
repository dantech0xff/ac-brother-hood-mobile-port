---
title: "Port slice 37 — ax66 bm() moving platform + grab/claim machinery"
phase: port
status: done
---

# Slice 37 — ax66 `bm()` (i.java:15899)

Transcribes the full ax66 `S`-switch: ride pairs (6,24)→(7,25)→(8,26)
→(9,27)→(10,28), the L113 crate-ride/grab arm (S11/12/13), and the
linked-kill/timed-return arms (S14–S23). Every arm is a verbatim port of
the decompiled labels; `g.a` → `p.ga`, `aS` → `p`.

## Ported arms (all `proven`, i.java:15899–16270)

| S | Label | Behavior |
|---|-------|----------|
| 6/24 | L5 | board: overlap + `g.g()` + `S!=50` → `i(7|25)`, `g.a=this`, player inherits vel; `S∈{11,12}` skip anim else `al` snap + `i(0)`; `Z[1]=Z[0]` |
| 7/25 | L21 | ride: overlap→rebind + `ah/aj` clamps + `S43` snap; `S34`/no-overlap→release (L34); `Z[1]--` every tick; `<0 && r()` → `i(8|26)` |
| 8/26 | L46 | `r()` → `i(9|27)` + release + `aS.a(0)` fling |
| 9/27 | L55 | sink: cell<12 → `al+=10, ah=2560, aj=1536` (+`aS.a(2560)` if riding); else `i(10|28)` + vel0 |
| 10/28 | L65 | `r()` → teleport `Z[2]/Z[3]` + `i(6|24)` re-arm |
| 11/12/13 | L113 | crate-ride/grab (~110 lines): `br()` claim check, L121 mid-air aid spawn, L131 board arm (S236→i237 / S239→i240 / S12 snap→i228|358 / else i0), L173 S13 in-box fling, L186 `k.u` grab input (16388 or dir-mask 2|8 by `av`), L200/L204 blocked-set {238,235,239,236,43,252,147} + `aS.f()` + facing check, L227 i(235)|i(238) + `G()`, L232 drift release |
| 14 | L241 | `aC--` → `i(20)` at 0 |
| 15 | L75 | linked crate (Z[0]) ax66 S12 holding player → `i(16)` |
| 16 | L88 | kill-linked: `k.c(r0)` (pre-r() arm + again at L102), `P|=64|32` |
| 17/23 | — | fall to L288 tail |
| 18 | L239 | `r()` → `i(14)` + `aC=Z[0]` |
| 19 | L274 | `Z[1]>=0` → `aC--` → `i(21)` at 0 |
| 20/21/22 | L281 | `r()` && S!=22 → `i(18)` + release + `aS.a(2560)` |
| tail | L288 | `S∈[6,10]∪[24,28]` → `b=false` (new `i.b` latch) |

## New helpers + plumbing

- `NpcFsm.grabEligible` = `m(i)` (i.java:16311): ax51 `S∈{0,1,6,8}` /
  ax66 `S∈{11,12,13}`.
- `NpcFsm.grabInReach` = `n(i)` (i.java:16337): facing-side, `|dx|<180`
  (140 for ax66), `|dy|<80`.
- `NpcFsm.grabZone` = `bq()` (i.java:16363): self holding (ax51 S8 /
  ax66 S13) && W-overlap.
- `NpcFsm.platformGrabCheck` = `br()` (i.java:~16395): claim
  release/steal; `k.bd` scan → `w.npcs + p`.
- `NpcFsm.platformSpawnAid` = `bn()` (i.java:~16272): `aS.ac!=null` →
  `spawnAeMarker(1, ak, al-60)`.
- `LevelCellSource`: `padDown` = `k.u` held-input (`pad.u`) — distinct
  from `padHeld` = `k.v` edge (used by `i.f` mash-QTE, which is correct
  as edge). `gj` = `g.j` latch. `kL`/`claimCo`/`claimRect` = the `k.L` /
  `k.co` / `k.cp` interact-claim channel; `claimReset` = `k.m()`
  (k.java:863); `registerClaim` = `k.a(i,int,int[])` (k.java:816:
  same-entity rect refresh else `prio<co || prio==1` steals; ax51 binds
  its Y rect).
- `Entity.var b` = `i.b` ridden latch (cleared on ride states only).
- `Entity.var ac` → custom setter = `i.a(i)` (i.java:229): clears
  `P|256` on the old target, sets it on the new. All writes to `ac` now
  run the claim-bit dance (matches the original where every write went
  through `a(i)`).

## Fixes caught while porting

- `k.q(-1)` must be null: `Level0World.findByAw(-1)` returns the player
  (`player.aw == -1`) — S16 link lookup guarded by `Z[0] != -1`.
- `return@tail` label trick doesn't compile (label must enclose site):
  `when` wrapped in `run {}` with `return@run`; only the S252 early
  `return` skips the tail (matches `return;` at L115).

## Tests (17, all green)

`platformAt` fixture (ax66 + clip7); `ridePlayer` positions the player
with real hitboxes. Covers: board bind + vel inherit, dead/throw gates,
ride clamps + S34 release + timer expiry, stop→fling, sink, reset,
L113 board (S236→i237 snap), S13 in-box fling, S15/S16 link arms,
S18→S14→S20 countdown chain, S19/S20/21 timed-return + fling, L232
drift release, `br()` dead-claim steal, tail `b` clear.

Honest gaps: level-0 has ax66 records but they're behind unreachable
camera walls on the flat demo path — exercised via the dispatch + unit
tests; full in-game ride demo pending ax37 scroll-bound tuning.

## Gates

`:core:test` green (incl. 17 new); verifier `ok:true`;
`python3 -m unittest` 57 pass; emulator boot `level0: npcs=454` clean.
