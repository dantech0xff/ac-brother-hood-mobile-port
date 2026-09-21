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

## Side discovery (open gap, not fixed here)

While debugging the run-anim test: landing on cell **18** leaves `aZ=false`
(x() semantics: `aZ = aR != 18` — proven), so `ax()`/`aw()` bail and S5 can
pin. Original land gate is `aR>=12 || aS>=12 || aR/aS∈{4,5}` — 18 IS in the
>=12 solid range yet marked non-standing. Likely a hazard/special tile;
the E2E "pocket jitter" may be the same tile. Needs a dedicated pass on what
cell 18 is (damage/slippery?) before changing behavior — flagged `unknown`.

Also changed the run-anim test to hold LEFT (opposite facing) so the press
edge doesn't fire the proven `v(8)` directional-vault (S233) first.

## Checks

- `:core:test` — 44/44 green (incl. new tunnel test)
- `verify-static-reconstruction.py` — `ok:true`
- `python3 -m unittest discover -s tests` — 57 green
