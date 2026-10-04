---
title: "Slice 388 — g.az() and g.i(i) follow the bytecode (interact target, mount link, k.bd scan, LOS)"
phase: "port"
status: "done"
slice: 388
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (az() 0-1819, i(i) 0-188, k(I) 0-183)
  - reconstructed-project/bytecode/i.javap.txt (e(i) 0-287 — the LOS walk, g(i) 0-26)
---

# Slice 388

`g.az()` (1.8 KB, called at e() 617 for every state) maintains the three
links the context system hangs off: `g` (interact target), `ci` (carry
target) and `i.at` (mount/assassination link). The port had it from the
simple decompile. Audited against the bytecode, offset by offset:

## Divergences fixed (all `proven`)

| @ | original | port had |
|---|---|---|
| 0-38 | `(aA & 8) != 0 \|\| S == 250` → `g = at = null; return` | `aA & 8` only |
| 39-65 | `g != null && (S == 270 \|\| S == 271)` → return | returned for S270/271 whether or not `g` was bound |
| 66-101 | `S == 268 \|\| k.aS.S == 267 \|\| S == 291` → `g = null` | S268 only |
| 102-182 | `g != null && g.ax != 4` → drop on `aB <= 0 \|\| dist > 440 \|\| \|Δal\| >= 60` — the whole block skips an ax4 | an ax4 skipped the `aB` test only |
| 244-319 | ax11 `Z[19] == 1` stays unless `(!av && dx < 0) \|\| (av && dx > 0)` | dropped through `inFrontOf` (also at dx == 0) |
| 1191-1231 | `S ∈ {268, 267, 291}`: `g = e; continue` — before `best` narrows, no LOS | S268 only, after `best` narrowed |
| 1234 | `if (this.e(bd[i])) continue` — the LOS walk (`i.e(i)`, Bresenham over the two W centres, `cell >= 12` blocks) on every NPC candidate; the ax72 arm (1786) already had it | no LOS test on the NPC path |
| 706-1819 | iterates `k.bd[0..be)` — the last paint's draw list | iterated `npcs` |
| g.i(i) 0-188 | offer window `(av && dx < 0 \|\| !av && dx > 0) && \|dx\| <= 200` → true, **else falls through** to the state list; the state list has `S == 267` and `S ∈ [299, 307]` (S303 is inside it — `S == 303 && r()` is redundant) | read `!av` backwards, returned `false` from inside the block, dropped S267, answered `r()` for S303 |

`ci` is cleared unconditionally at @183-243 (the keep branch needs `av`
both true and false) — as the port already had it; the @680-705 early exit
therefore never fires (`ci` was just cleared), kept for the record.

## Consequences (faithful, observed)

LOS is the one that moves play. A candidate behind a wall `continue`s
before the `g = null` boundary at @1283, so the old accidental unbind —
"any soldier ahead within 440px resets `g` every tick, wall or not" — is
gone: an alert soldier stays bound while it lives inside the 440px / 60px
band, facing away does not unbind it, and only a fall (|Δal| >= 60), death
or distance releases it.

- Level 0, the tower: the posted soldier uid571 wakes as the player lands on
  the column top and keeps `g`, which the ax10 door arm refuses (`g == null`).
  The legs bait it off the west edge (two opening strikes, then a jump west;
  it chases and falls — S25, |Δal| >= 60) instead of relying on the flicker.
- Mission 6 leg E: the two lower-mass patrol guards wake as he drops in and
  hold `g` / `ci` (a held UP then picks the guard up — S6); the leg fights
  them like leg F.

## Tests

`Slice388Test` (12). Mutation check — each fix has a failing test: `S250`
(1), the S270 guard (1), the head rebind (1), the scan rebind (1), the ax4
exemption (1), `Z[19]` dx == 0 (1), the NPC LOS (1), the `k.bd` domain (1),
`i(i)` `!av` (2), the early return (1), S303 (1), S267 (1). Existing az()
unit tests paint their neighbours (`Level0World.scanInteract`; the
`MarkerWorld` double's draw list is its `npcs`); three tests that hand-staged
a bound `g` for S291 / the mounted action now stage the draw list too.

## Capstones (routes only — no enemy, door, timer or state touched)

| Test | Broke because | Re-route |
|---|---|---|
| `Slice245Test` cp7 → fuse, finale pack, mission-complete stats (tower) | the door legs faced west to "unbind" `g` and kept slashing uid571 into a stalemate (the soldier turns Z2/aB300 and the counter fires) | face west for `ci` only; two opening strikes on uid571 then none — it chases the west-edge jump and falls |
| `Slice291Test` m6 leg E | alert guards hold `g`/`ci` at the door; held UP → S6 | leg F's `foe` clause: fight within 50px |

## Open

- The old unbind flicker hid how the tower soldier is meant to be beaten;
  the baited fall is a route, not a proof of the designed solution (the
  weakened Z2 soldier is finished by the counter-riposte window, not by
  hits). A device play-through should confirm the designed way.
