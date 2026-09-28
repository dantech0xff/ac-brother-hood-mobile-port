---
title: Slice 298 — m7 boss-1 duel phase-win via script-305 two-prompt QTE
phase: capstone
status: done
---

# Slice 298 — mission-7 boss-1 duel phase-win

## Goal

Prove the iBy=1 boss-1 duel phase-win on mission 7 through real input only:
the boss cannot be killed by hits (infinite heal cycle), the phase is won by
the scripted counter-QTE that sets `w.iBy = 2`.

## Mechanism (all proven)

1. Stagger hits (counter arm `c()` knockback, `aB -= 40`) drive `boss.aB`
   from 800 down to `<= 300` — normal hits are absorbed and never land.
2. `aB <= 300` → boss retreats `S13` → `S25` → `S26` (block posture,
   i.java boss FSM arm at L4837-4857).
3. During `S26`, `pad.v(65568)` with `overlapI(e.W, p.W) || overlapI(e.W, p.X)`
   → `bindContext` on ax5 uid280 → script **305** claims (i.java:8540-8564).
4. Script 305 is a **two-prompt QTE** (decoded from `level7/scripts.bin`):
   - key=5 `op107[32]` arms prompt-1 = CONTEXT (65568); `op108[0,314]` at
     key=20 evaluates `decided==1` → continue, else jump script 314.
   - key=58 `op107[16]` arms prompt-2 = LEFT (4112); `op108[0,314]` at
     key=81 evaluates — answered → continue, else jump 314 (heal-outro).
   - key=151 `op38[10,2]` → `w.iBy = 2` + `kAU.aB = 300`
     (i.java:18287 `runArgSub` case 10).
5. `iBy == 2` → `aP()` early-returns → boss dormant → the climb route opens.

## Bot input

- `S26`: `dir + M_CONTEXT` while overlapping (binds uid280).
- `S23/S25/S27` while `kC == uid280`: `M_LEFT + M_CONTEXT` — covers
  prompt-2's LEFT poll window (steps 58-81 of the claim).
- `S7` grab-QTE (boss `T <= 6`): `M_UP` — `pad.v(16388)` escapes via
  `iCj` before the `T == 7` `applyHit(4,…)` lands (i.java:L314).

## Verdict test

`Slice297Test.mission7BossPhaseWin` — spawns mission 7, duels boss uid251
through real masks only; asserts `sawGrabQte`, `sawHeal`, and
`sawPhaseWin` (`w.iBy == 2`). Phase won on the first S26→S23 cycle
(t≈1520); no production changes needed — the mechanism was already
faithful, the earlier bot simply never pressed LEFT for prompt-2.
