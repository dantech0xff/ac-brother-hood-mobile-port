---
title: "Slice 405 — I() has no universal side-collide: the invented per-tick a(true) for every soldier is gone"
phase: "port"
status: "done"
slice: 405
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (I(): `invokevirtual a:(Z)V` at 3085, 4574, 4676, 5032, 5464, 6167 only; ctor `i(short[])` @7211; aJ() @559/666/766/896; aA() @477)
---

# Slice 405

Parked since plan 402. `i.a(boolean)` is the wall/floor resolver (`Entity.collideSides`):
it probes the side columns, extrudes the body out of a wall and re-seats the feet on
slopes. `I()` calls it at **six** offsets and nowhere else —

| offset | arm |
|---|---|
| 3085 | S142 (dropped, falls 10 px/tick) |
| 4574 | S23 (back-off) |
| 4676 | S4/22 (chase) |
| 5032 | S18 (grab offer) |
| 5464 | S6 (lunge) |
| 6167 | S0/106/107/135 (death / dormant) |

(+ the constructor tail @7211, `aJ()` ×4 and `aA()` S170 — all already ported). The port
additionally ran `collideSides(true)` in `NpcFsm.tick` in front of the arm switch for every
soldier-family state except S25 (Devin's "per-tick safety collide"). That extruded
patrolling, flinching and fighting soldiers out of walls every tick and re-seated them on
slopes — neither of which the original does: those states integrate freely and rely on
their own probes (`am()`/`aG()`/`aF()`, the L777 open-cell fall gate, S25's anchor-cell
landing).

## Fix (`proven`)

The line is gone (with a comment listing the six sites). Observable differences over 900
idle ticks on the eight missions' soldiers: one patrol phase shift (mission 2, uid297:
the extrusion had been nudging it off the wall it turns at) and a 3-px corpse offset
(mission 5, uid4) — everything else is bit-identical, so the cost was invisible in the
suites; the benefit is that knocked-back and fighting soldiers now behave as the bytes say.

## Capstone

Mission-2 leg C: the platform's last guard now grabs the runner (S310 held by its S175
QTE) instead of being a ghost; the leg mashes the attack mask (edge presses fill the gauge
+8 each; a full gauge counter-executes the soldier → S311). Route / input only.

## Tests

`Slice405Test` (2): S11 / S5 / S85 overlapping a wall are not extruded; S23 / S18 (which
carry their own `a(true)`) still resolve it. Mutation: re-adding the universal call kills
the first.
