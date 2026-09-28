---
title: Slice 293 — m6 capstone leg C (chasm scripted carry + perch chain)
phase: capstone
status: done
---

# Slice 293 — mission-6 capstone leg C

## Milestone
Player pinned at the leg-B checkpoint (6060,219) descends the chasm
S164 slide into ax5 zone uid240 [7663,341,7903,661] and survives the
mission's scripted carry sequence end-to-end.

## What the trace proved
1. `S164` slide (ag=2560, ah=752) lands the player at (7690,559) inside
   uid240's zone → `eventBind` + `bindScript(kSIndex(239)=9)` — claim
   script 9 (the big mission script, 41-group block0 + 16 type-2 blocks).
2. The script is a **chained QTE + carry cutscene**:
   - `op107` (mask 32→65568 CONTEXT) at key 47 arms the prompt;
     `op108 [pass=0, fail=241]` polls until key 55. Timeout → bind
     script 241 = `[op37 r12=2 → screenL(12)]` (mission fail). The bot
     answers every prompt with `M_CONTEXT` while `kC != null`.
   - Second QTE at keys 109/117 (fail→script 242 — same kill op).
   - Keyed timeline then pans the camera (~200 steps), carries the
     player down to y775 (S24), raises `jC=21` dialog (op100), runs an
     S337 anim, then S246 carries the player back up to (8025,560) and
     releases at step ~420 (uid240 removed).
3. Released on the platform row r28 (x7620–8100, type 20) — checkpoint
   uid328 @(8068,523) right there.
4. East of x8100 the platform ends; the designed route continues via the
   ax22 perch mounts @(8158,476) → @(8304,452) — the bot vault-jumps off
   the edge (`M_RIGHT|M_UP|M_TAP_R` at ak 8000–8080), catches the perch
   chain, and crosses x8400 on the band.

## Verdict
`mission6CapstoneLegC` green: `released=true`, `maxAk=8432`. Death at
t≈993 is a ~475px lethal fall after the spring/lift chaos at x8320–8430 —
that band crossing (3 ax66 lifts + 2 ax46 springs + ax14 arc pickups at
y261–421) is leg D's frontier.

## No production changes
The kill-on-timeout and carry QTE semantics were already ported; this
slice is test-only (driver + assertions).
