---
title: Slice 294 — mission-6 capstone leg D (lift/spring band → east masses)
phase: capstone-bot
status: done
---

# Slice 294 — mission-6 capstone leg D

## Goal

Fourth leg of the headless mission-6 capstone bot (spawn → win, leg by leg):
cross the lift/spring band and the three hanging masses — from leg C's
perch-uid139 deposit on the y304 lift row east past the ax5 uid311
script-8 zone (x9672).

## Route (proven end-to-end, trace in test)

1. Deposit `(8312,304)` on lift `uid410`'s top (proven landing of leg C's
   perch `uid139@(8304,452)` rise).
2. Hop WEST along the y304 lift row to `uid143@(8178,304)`, then `LEFT|UP`
   off the pad → spring `uid420@(8079,250)` launches (11520,-11520) →
   chains into spring `uid123@(8200,183)` → apex ~y86.
3. Land the y145 lift row (`uid146@8339 → uid149@8397 → uid191@8511`),
   hop east off `uid191` → mass-A top shelf (x8640-8900, y~220).
4. Edge-hop into the A-B gap (x8900-8980); drift east past the trap lift
   `uid156@(8938,580)` (oscillating box — catches a straight faller, so the
   driver drifts off it instead of riding).
5. Free-fall drift reaches the perch/rope cluster: vault near perch
   `uid159@(9164,497)` → rope `uid162@(9213,169)` swing → S101 wall-hops
   up mass C's west face → S284 edge grab → mantles east.
6. Lands east past ax5 `uid311@(9672,358)` (script 8); run continues to
   x~9890 before the milestone break.

## Key decodes this leg

- `uid156` is a fall-trap (r8[1]=22 long timer): it catches a faller into
  the gap and oscillates S259/260/263/265 forever — escaping = drift east
  while unbound; perches `uid158@(9072,570)` / `uid159@(9164,497)` are the
  designed mid-gap catches.
- The A-B gap fall (y220→y580 pad) is only survivable via the lift pad or
  the perch catch — a straight drop to the street y760 is a lethal ~540px.
- Mass tops are jumpable by committed edge hops; the gap faces give S101
  grabs as alternates.

## Test

`mission6CapstoneLegD` in `Slice291Test` — pin `(8312,296)`, waypoint-hop
driver with gap rules; asserts `!died` and `maxAk > 9650`.
Gates: `:core:test` all green, verifier `ok:true`, 57 unittests OK,
`:android:assembleDebug` builds.
