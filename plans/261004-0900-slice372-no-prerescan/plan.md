---
title: "Slice 372 — the player slot runs no wall rescan before g.e(); the S32 run-start arm; x() keeps bd on a '5' cell"
phase: "port"
status: "done"
slice: 372
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (e() 494, 6092-6357, 6512-6630; every `i.a:(Z)V` call site)
  - reconstructed-project/bytecode/i.javap.txt (a(boolean) 5898-6330, x() 5534-5780, E() 15456-15489)
  - plans/261003-2300-slice369-player-ge-followups/plan.md (the S32 divergences, listed there as not fixed)
---

# Slice 372

## Finding (proven)

`i.I()` takes the player slot straight from the integrate into `g.e()`.
Every `invokevirtual i.a:(Z)V` in `g.javap.txt` — twelve, all listed — is
inside `e()` or `au()`:

| site | where | arg |
|---|---|---|
| e() 494 | head, `a(an())` (`an()` = S<=43, 150, 67-69, 199, 216, 217, 298, 259-266, 20, 49, 243) | `an()` |
| e() 6328 | grounded arm S1/7/11/26/79 | `true` (`y()` → `ag = 1; a(true); ag = 0`) |
| e() 6550 | **S32 arm**, `ag != 0` | `true` |
| e() 8259 | air family S20-25/215/233, anim end | `true` |
| e() 10143 / 10187 | stagger S9/S10 | `false` / `true` |
| e() 12334 | S217 | `true` |
| e() 12556 | S242/S243 | `true` |
| e() 13425 | S311/S312 | `true` |
| au() 427 / 1468 | mounted orbit | `false` |

plus `i.E()` (settle-sink, `ah = 1; b = true; a(true)` — S54, S49, S90, S267,
S269 and the `g.A` autowalk in the post-tail — all ported as `eSettle`).
There is no `a(true)` between `integrate` and `e()`.

The port ran one anyway (`Level0World.tickPlayerI`, "a slice-2 superset the
bot legs were proven against"): the player was pushed out of walls in every
state, including the ones whose arm leaves him embedded on purpose — S79's
crawl through a solid, a run into a face (`co > 2 && pushColumnBlocked` →
S33), a mantle that ends at the pillar's edge.

## Changes

- `Level0World.tickPlayerI`: the pre-dispatch `p.collideSides(this, true)` is
  removed.
- **S32 arm** (e() 6512-6630; slice 369 listed it as unfixed): `!aZ &&
  g.a == null → cq = 0; a(0)` (fall), `ag != 0 → a(true)`, `y() && ag != 0 →
  ag = 0` (the wall stop), and after `r()` → `i(Q == 79 ? 79 : 0)` a settled S0
  re-probes (`x()`) and re-embeds into S79 when the head cell is solid
  (`aO > 12`). The port had only `i.f(this)` and the `r()` exit.
- **`x()` and `bd`** (i.javap.txt x() 159-176): for `aR == 5` the bytecode falls
  straight into `r8 = r02 % 20`, so `bd` keeps its `ah != 0` value (cleared
  only by `aO >= 12 && aP >= 12`). The simple decompile prints the *block
  layout* `L9: if (aR != 5) goto L69; L5: bd = false`, and the port copied the
  store — `a(true)`'s ground pre-adjust (`al -= x()`) never fired on a `5`
  cell. `bd` is read once in the whole JAR (i.javap.txt a(boolean) @126), so
  nothing else moves.

## Faithful behaviour this exposes (all `proven` against the bytecode)

- The gate-row leg's rock crawl ends on the slope cells 24/25 at x4541: S32
  (run-start from S79, `Q == 79` → back to S79) pins there. On a slope cell
  `x()` clears `v`, so `a(true)` skips its `bb == bc` clear (a(boolean)
  532-540) and the side strips — which read the slope cells (≥ 18) — leave
  both flags set; the S32 wall stop then zeroes every run-start. A jump
  (UP|RIGHT) leaves it. Running the slope from the rock top in S12 is
  unaffected (identical trace with and without the old superset).
- The S62 mantle onto the pillar top (1740,519) ends ON the pillar (the ±10
  step → x1730), not 13px out over the channel where the superset dropped him.

## Tests

`Slice372Test` (13): `bd` on a `5` cell (kept / cleared by a solid head pair /
the `a(true)` snap), the S32 arm (airborne fall, ride spares it, wall stop,
runs on in the open, S0 re-embed under a solid head, free head → S0, `Q == 79`
→ S79) and the removal (S78 — outside `an()` — is not pushed out of a one-sided
wall; S0 is, by the head). Mutation check: the superset back → the S78 test
fails; no wall stop → "S32 stops against a wall"; no airborne fall → two
tests; `bd = false` on `5` → two tests; no re-embed → one.

## Capstones (routes only — no enemy, door, timer or state touched)

| Test | Broke because | Re-route |
|---|---|---|
| `Slice245Test` gate row | the S79↔S32 slope pin above (x4601) | jump out of it: UP\|RIGHT while grounded in S79/S32 east of x4560 |
| `Slice245Test` channel zigzag, S89 pinner | the mantle ends on the pillar top; held UP hops him back into the channel instead of walking off | RIGHT alone while standing on the pillar top (x1700-1745, y500-525) |
| `Slice360Test` brake | the spawn is 80px from the map edge: a 15-tick run is a wall run (S33) | start mid-floor (x40 east-bound, x250 west-bound) |

The S89 pin now releases after three ticks (the guard is already in S24 when
the bot lands on it); `Slice245Test` only asserts the pinner is the ax11.

## Gates

See the PR. `:core:test` green, `:gdx:test`, `:android:assembleDebug`,
verifier `ok: true`, `python3 -m unittest discover -s tests`.
