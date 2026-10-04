---
title: "Triage of the omitted/unported/stub markers left in rewrite/core"
phase: "port"
status: "done"
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (e() offsets 0-176, 7222-7362)
  - reconstructed-project/bytecode/i.javap.txt (putfield U)
  - reconstructed-project/src/structured/g.java:529-565, :5238-5288
  - reconstructed-project/src/structured/i.java:336-420, :1816-1819, :8320-8356
  - reconstructed-project/src/structured/k.java:640-652, :3640-3945
---

# Core marker triage

`grep -i 'unported|not ported|stub|TODO|FIXME|placeholder|omitted|
approximat|simplified'` over `rewrite/core/src/main` (spike files excluded)
gave 25 hits. Each was checked against the original; the verdicts below
decide what still needs a slice.

## A. Real gaps (fixed or queued)

| Marker | Finding (proven) | Where it goes |
|---|---|---|
| `PlayerFsm.preJumpArm` "rope/platform variants omitted" | e() 7222-7362: `r() \|\| S==233 → i(22)`; `ah = g.a.ax==51 ? -2560 : -5120`; `g.a.ax==43 && g.a.ab() → ag=∓1024` else `∓2048`. The port waits for the anim end on S233 too and always jumps full height/width. | slice 369 (F12) |
| `Level0World` "g.r(stub)" | g.r is read: e() 157-163 `if (g.r) return` — the boss grab lock (aP S7, i.java:8328/8352). The port writes `gR` and never reads it. | slice 369 (F13c) |
| (no marker) e() head | 0-19 `k.C != null && (aS.P&512)==0 → return` (any bound claimer, not only `ab()`); 154 `k.l()` writes the static `k.M` reach box that NPCs read a frame later; the port rebuilds it live (`NpcFsm.ctxZone`, `reachRect73`). | slice 369 (F13a/b) |
| `Level0World.tickPlayerI` pre-dispatch `collideSides(true)` "slice-2 superset" | An extra side-collide before `g.e()` that the original does not run; removing it stalls two bot crossings, so some arm's own collide is missing. Documented, but an approximation. | **done — slice 372**: removed; the only arm lacking its own collide was S32 (6550), the bot crossings were routes through states the superset had been masking |

## B. Deliberately not ported — no gameplay effect

| Marker | Why it is safe |
|---|---|
| `g.v` / `g.s` cheat toggles (`Entity.drainMeter`, e() 20-153) | Set only by the cheat-code sequences in `k.a()`'s head (debug features: god mode, free move, FPS, "Open all level!"). Unported: `g.v`/`g.s` stay false, which is the original's state without cheats. |
| `g.m` (Level0World D() clear) | Written once (`g.m = 0`, i.java:1816), never read anywhere — dead field. |
| `f.b()` (menu item 103) | IGP / cross-promotion launcher (`platformRequest`). |
| `E()`, `ac()`, `ad()` in `stateL` | Sprite-pack loads/teardowns (`A[1] = J(1)`…); render resources only. |
| `trailAb()` | `ab()` flying trail LUT; unreachable because `ef[]` is all false in this build (verbatim dead code). |
| "Set Boss Speed=" print (NpcFsm) | Debug `StringBuffer` with no effect. |
| ax64 barrage `r7 > 1` spread (NpcFsm) | ax64 only calls `a(1,false)`; the multi-shot geometry is never used. |
| `i.t()` `S < 0` arm (`Entity.refreshBoxes`) | Original sets `S = 0` and returns; the port zeroes the boxes. Only ax42 is ever at `S = -1` and it returns before that check (verbatim). Probe over all 8 levels: no other type. |
| `i.t()` `U < 0` arms (W/X via the anim-level rect) | Every `putfield U` in class `i` writes 0, `U+1` or a copy of the player's `U`; `g` never writes it. `U` is never negative, so the arms are dead. |

## C. Stale comments (code already ported)

To correct when the files are free of the in-flight slices 369/371:
`Entity.kt` `t()` header ("ax66/13/21/60 special cases and the Y bounds
rect omitted" — all ported below it), `Level0World.menuItem` kdoc ("callees
`e(true)`/`W()`/`a(bool)`/`f.*` stubbed" — they are `saveFlush`,
`teardown`, `reloadCheckpoint`, `loadingShow`), `PlayerFsm` "aw() settle
tail (… climb/drop hooks omitted)" (g.aw() is fully ported), the
`mountEntry` note that `o()→ao()` is unported (postTail runs
`cycleEquip`), and the orphan `b(k.aS) simplified` kdoc in `NpcFsm.kt`.
The two PlayerFsm ones were handed to slice 369.
