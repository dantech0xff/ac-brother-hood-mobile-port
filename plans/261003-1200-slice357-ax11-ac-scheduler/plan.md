---
title: "Slice 357 — ax11 runs the real aC() attack scheduler (G5) — parked pending capstone re-validation"
phase: "port"
status: "blocked"
slice: 357
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:7009-7161
  - reconstructed-project/bytecode/i.javap.txt:31121-31560
  - reconstructed-project/src/simple/i.java:5507-5566
  - reconstructed-project/src/structured/i.java:7258-7274
---

# Slice 357 — the `aC()` attack scheduler for ax11 (G5)

Phase 1 item 1.5. **Branch `claude/g5-ac-scheduler`** — kept off the main
port branch until the capstone bots are re-validated (Phase 2).

## Finding (proven)

- `aC()` (structured `i.java:7009-7136`; bytecode `i.javap.txt` from
  :31121, offsets 355-673 checked) is shared by ax11 and ax73. It differs
  by type only through `c(a11, a73, …)` (`i.java:7146-7161`: ax11 takes the
  first state, ax73 the second), the ax73 `Z0==3` leaper, and the j==0
  timeout's last step (ax11 `i(k?3:2); k=false`, ax73 `i(152)`).
- ax11 calls it from three arms (`simple/i.java`): S4/S22 chase when the
  windup does not start (:5562-5566), S5 on anim end after
  `aq!=0 → i(4); G(); aC=0` (:5507-5517), and S23 every tick — S23 backs
  away (`ag = av ? 512 : -512` after `Q()`), runs `H()`, `aC()`, and
  `aF() → ai=ag=0` (:5518-5532).
- The port ran only a chase-timeout subset, turned S23 toward the player
  and jumped S23→S12 on anim end — S11 (the windup the scheduler sends
  contact/close tiers to) was unreachable. ax73's port also fell through
  when a bound ax69 did not overlap (the original returns either way).
- `aG()` (`i.java:7258-7274`) is one function; the ax11 copy lacked the
  crate-edge rule. `v()`'s final overlaps are `i.a` (strict).

## Change (on the branch)

- `attackSchedulerAC` (+ `cPick` = `c()`) replaces `attackScheduler73`
  and runs for ax11's S4/S22, S5 and S23 arms as above.
- `edgeAhead` delegates to the verbatim `aG()`; `inPlayV` uses
  `overlapStrict`.
- `Slice318Test`'s scripted player faces the guard it strikes.

## Status — blocked on Phase 2

`:core:test` 1651/1656 on the branch. Five capstone bots die to soldiers
that now fight as in the original (S23 back-off → S11 → S12): m0 end to
end (`Slice245Test`, 301 deaths at x≈1927), m3 leg H (`Slice282Test`),
m5 legs B and I (`Slice289Test`), m6 leg E (`Slice291Test`). Their duel
and flee policies were tuned to the old S23→S12 shortcut. Phase 2 rule:
re-route/re-time the bots, never weaken enemies. The bisect showed the
strict `v()` and the single `aG()` are not the cause; both landed on the
main branch separately.
