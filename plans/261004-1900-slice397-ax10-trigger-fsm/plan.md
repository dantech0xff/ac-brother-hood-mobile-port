---
title: "Slice 397 — the ax10 trigger FSM aV() follows the bytecode (S16 doors, S30 wave spawner)"
phase: "port"
status: "done"
slice: 397
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (aV() @0-7879; ae(); aT(); at(); aS(); k(i); bh(); bi(); a(III)V; G(); c(II)V; d(II)V; V(); aU() S31/S34 arms)
  - reconstructed-project/bytecode/g.javap.txt (g.b(I)Z static)
  - reconstructed-project/bytecode/k.javap.txt (k.t(), k.j(), k.k(), k.c/d rect tests)
---

# Slice 397

`aV()` is the biggest method of the ax10 trigger zones (124 records over the 8
missions): a 56-way switch on `S`. It was re-read arm by arm from the raw
`javap -c` (pseudo-decompiled with `bcdec`) and compared with `tickTrigger`.

## Verified equal

S0, S2, S3 (the Z[0]-gated flag appliers incl. the `bb[]` squad sweep), S4-S8,
S10 (the perch/climb band logic, hand helpers `i.c/d/V`), S12-S14, S17, S18,
S21-S24, S28, S29, S31 (the QTE zone incl. the consumed reset, prompt cards,
`k.t()` = `j() || bB != 0`, and `aU()`'s S31 draw arm that latches `n`), S32
(`k(i)` balance arc), S33, S34 (`aU()`'s rail ride), S36, S41-S51, S53-S55, the
`aT()/at()/aS()/ae()` helpers, `bh()/bi()` and the dead states 1/9/11/15/19/
20/25-27/35/37-40/52. The ax10 record init (the ctor's S switch) was covered by
the slice-389 constructor differential.

## Divergences fixed (`proven`)

| where | bytecode | port had |
|---|---|---|
| S16 door teleport @6127-6149 (**12 shipped zones**, missions 0/3/6) | `aload_0; bipush 105; … invokevirtual a:(III)V` — the ZONE spawns and owns the 105 prompt | spawned it on the PLAYER's `ae`: the door's own `G()` / leave arm never cleared it — a stale prompt stayed at the door |
| S16 mid-fade arm @6088-6092 | `k.aS.a((i) null)` = `invokespecial g.a:(Li;)V`, the entity overload (unbind + `P &= ~256`) | the int overload `a(0)` — the S43 fling: the player never stood up out of the S285 door-emerge anim |
| S16 exit tap @6152-6179 | `k.v(16388) && !g.b(aS.S) && aS.aZ` — `g.b(I)` is the aerial/action set `{18-20,22-25,35,36,43,150,157,165,233,242,243,263-266}` | `!playerAttacking()` (the attack list, itself gated on `gI`) |
| S30 pursuer-pool spawner, Z[6] == 3 @3222-3243, @4001-4022 | `Z[5] = (col >= 1) ? 1 : 0` — column 0 spawns on the left, the rest on the right | both inverted. (No shipped record uses flavour 3: the two S30 zones of mission 3 are Z[6] = 0.) |

## Capstone bots (input/timing only)

The faithful door gate refuses the tap in an aerial/action anim. The mission-6
legs D, E and F entered doors uid166 / uid179 by holding UP while hopping around
the door box, which only ever fired during a landing frame the old gate let
through. They now stand inside the box and tap UP from a standing state — what a
player does. **No state was written and no enemy was changed.**

Corrected listing misreads: `Slice99Test` (the 105 prompt owner, the mid-fade
unbind) and `Slice146Test` (the S30 Z[6]==3 side rule, two tests).

## Tests

`Slice397Test` (5): the door owns / releases its prompt, the mid-fade unbind
(no fling, bind mark cleared), the exit-tap gate (S67 passes, S43/243/157/22
refuse), the S30 wave side rule on the initial grid and on a respawn.
Mutation-checked: 5 reverts, 5 killed.
