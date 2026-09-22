# Slice 14 — terminal fall velocity (E2E fall-through fix)

Date: 2026-09-21. Branch: `devin/<ts>-port-slice14`.

## Bug (from E2E report)

Vault/jump descents tunneled through solid floors into interior pockets /
under the world; camera clamp then hid the player. Root cause: the port let
`ah` grow unbounded — at >20px/tick the feet skip a whole floor cell between
probe ticks.

## Fix (proven)

`g.java:578-579` — the player's per-tick tail clamps
`if (ah <= 5120) skip; else ah = 5120`: terminal velocity 20px/tick = exactly
one cell per probe, which is why the original never tunnels. Ported verbatim
into `PlayerFsm.tick` after `postTail`. Same literal also appears at
`i.java:5686`/`9294` (NPC-side caps).

Regression test: `long falls cannot tunnel through floors` — drops the
player 20 cells onto real ground and asserts `al` never passes the floor.

## Fix 2 (proven): `d()` edge bump — the E2E "pocket jitter" / S5-pin

`g.java:4664-4688` `d(boolean r8)` — after `al = ((W[3]+1)/20)*20; al--`, the
original bumps `al += 20` whenever the landing was detected through `aS`
(below-feet row) instead of `aR` (feet row):

```
r8=false (normal land):  if aR<12 && (aS>=12 || aS==5) → al += 20
r8=true  (platform-4):   if aR!=4 && aS==4            → al += 20
```

The port only bumped for `aS==4 && aR<12`, so landings via `aS` left the
feet probe row in air (`aR=0` → x() early-return → `aZ` stays false) →
`ax()`/`aw()` bail → the player pins in S5 and jitters S5↔S43 on pocket
floors. Fixed `Entity.land` to the two-arm proven bump.

NOTE: the earlier "cell 18" theory was wrong — level-0's `et` plane contains
ZERO cells of value 18; the pin was the missing bump, and `aZ=false` came
from the `aR<10 && aR!=5` early-return with `aR=0`, not `aR==18`.
Cell-18 semantics remain unmined but no longer block this fix.

Also reverted the run-anim test to hold RIGHT — the tap-edge vault (S233)
plus the fixed bump now lands the player with correct `aZ`, so hold-right
reaches run anims 12/32 through the real input path.

## Checks

- `:core:test` — 44/44 green (incl. new tunnel test)
- `verify-static-reconstruction.py` — `ok:true`
- `python3 -m unittest discover -s tests` — 57 green
