---
title: "Slice 398 — the ax4 crate FSM aj() and the wisp spawner m(int) follow the bytecode"
phase: "port"
status: "done"
slice: 398
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (aj() @0-1065 — i.javap.txt:24516; m(int) @0-124 — :71017; a(IIII) child spawn — :19173; S() — :32381; bN() S0/S1 af.aG arms; p(); a())
  - reconstructed-project/bytecode/g.javap.txt (g.b(I)Z static, pinned in slice 397)
  - reconstructed-project/bytecode/k.javap.txt (k.c(i) — :22384; k.a(i,I,[I) — :4990; k.m(); k.j(II) — :4465 touch hit-test)
---

# Slice 398

`aj()` is the ax4 crate/prop FSM (the breakable crates, fly-out props and blast
props: 180 shipped records in missions 0/2/3/5/6/7). It was decompiled with `bcdec` and
compared arm by arm with `NpcFsm.tickDestructible`, then the helpers it calls
were re-read from the raw `javap -c`.

## Verified equal

S29 (blast sweep over `k.bd`, player `a(4,0,0,this)`, the ax4/29/11/17/73/23/15
effects, sfx 12), S30 (proximity re-arm incl. the S295 grapple link), S33
(fly-out prop and its a(24,40,35,200) spark child), the S5/S7 context-bubble
sub-arm (`k.a(this,5,W)` + `k.c(ak,al-85,aw)` / the `k.L.aw == aw` release),
`a()` push-past, the ax4 record init (slice-389 constructor differential), and
`a(IIII)V`'s child creation order used by the wisp.

## Divergences fixed (`proven`)

| where | bytecode | port had |
|---|---|---|
| S5/S7 gate, aj() @78-81 | the **static** `g.b(k.aS.S)` — the aerial/action set `{18-20,22-25,35,36,43,150,157,165,233,242,243,263-266}` (the set slice 397 pinned for the door tap). In it a BODY overlap cracks the crate (`i(S+1); k.A(14)` @185-216); outside it the bubble + `a()` push-past run; the attack-hitbox test @217-248 is shared | gated on `isAttackState` (the no-arg attack list): a jumping / diving player bounced off the crate, an attack-combo player skipped the prompt + push-past |
| S6/S8 wisp loop, aj() @249-352 | first wisp `m(-1); m--; k.o(5); k.s()` then `while (m > 0) { m(-1); k.o(5); k.s(); m--; }` — **every** remaining wisp in the tick the anim ends | at most two. `initDestructible` (slice 389) already added the full `m` to `k.aq` (the HUD wisp total), so the m > 2 crates — **25 shipped crates** (m = 3..7 in missions 2/3/5/6, ≈ 45 wisps) — could never be emptied |
| `m(int)` @107-109 | `aload_0; iconst_1; putfield aG` — the "has spawned" flag lands on the **spawner** (`this`); the wisp reads it back as `af.aG != 0` → the second `k.A(15)` at orbit end (S1) / collect (S0) | `aG = 1` on the wisp itself; `af.aG` stayed 0 unless the spawner's record happened to carry one, so the burst wisps never played the second sfx 15 |

## Capstone bot (input/timing only)

The faithful aerial gate makes the mission-2 leg A prop-hop crack aw4 open
in mid-air and land past it in the S5 landing recovery; the bot's generic
"airborne ⇒ hold UP" rule then re-hopped at x≈1126 instead of walking to the
block's east edge for the S26 jump, and fell into the pit at x≈1310. The bot now
holds RIGHT only during that S5 landing (`p.S == 5 && p.ak in 1090..1200`) —
what a player does. **No state was written, no enemy changed; the leg's pass
criterion is untouched.**

Corrected listing misread: `Slice1Test` "ax4 armed by attack then S6 bursts …"
armed the crate by a body overlap in S67 (an attack-list anim, not in `g.b(I)`);
it now arms through the real attack hitbox (S67 T1 X ∩ W, @217-248) — the
first branch reads the aerial set, not "mid-attack".

## Observations (not fixed — out of scope)

- `k.L` / `k.co` (the context claim the ax4 arm bids on) is **only** consumed
  by the touch hit-test `k.j(II)` @285-358 (a tap inside the padded claim rect
  returns action 1/4) and a debug overlay line in `k.b(Z)` — never by the
  keypad path. The original has five bidders (`I()` @1672, `aj()` @125, `aJ()`
  @26 prio 0, `aL()` @421, `br()` @91 prio 1); the port only models the ax4 one.
  `proven` for the consumers, so the missing bidders only matter for a future
  touch-to-interact feature on the mobile port.
- The original `k.c(i)` never touches `k.L`; the port's `removeEntity` releases
  a claim held by the removed entity. Unobservable here (nothing in the port
  consumes the claim), so left alone.

## Tests

`Slice398Test` (9): the full `g.b(I)` set cracks S5 and S7 by body overlap; the
attack-list anims do not; the attack hitbox cracks from any state; the bubble
arm is skipped inside the aerial set and runs outside it (incl. every
attack-list anim); S6/S8 spawn all `m` wisps in the finishing tick for
m ∈ {0,1,2,3,5,7} with `k.o(5)`/`k.s()` once per wisp; no claim left behind; a
census over all eight missions (every shipped S5/S7 crate drops exactly its
record's `m`); `m(int)` flags the spawner and the orbit end plays the second
sfx 15. Mutation-checked: 8 reverts, 7 killed, 1 equivalent (the explicit
`k.m()` before `k.c(this)` — the port's `removeEntity` reaches the same end
state, see Observations).
