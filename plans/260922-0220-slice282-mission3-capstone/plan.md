---
title: slice 282 — mission-3 capstone bot legs (cp1 + massA→massB tower arc)
status: done
---

## Result

`Slice282Test` holds two proven legs on `world(aj = 3)` (bh4 platformer,
pack-9 Florence, 691 records, horizontal chase map x8..13716):

- **Leg A** — spawn (21,699) → run east (striking live ax11 in melee) →
  the x1400 wall crossing → cp1 aw157 @(2229,647). The wall is a
  ~64px-thick double face (west x1390 / east x1439, top y519) lined by
  ax67-S15 prop blocks with ax74 wisps arcing (1385,672)→(1427,621).
  Verified live: the chain is kick up the west face → over the top →
  and the ONLY UP-free descent — past x1415 airborne the mask drops UP
  so the auto-grab (S101/S62) on the east face cannot fire a kick back
  west (the observed failure loop without it).
- **Leg B** — the mass A → mass B crossing (x4240-4360 gap). Verified
  live: the aw849 ax37 scroll bound [4246,344,4383,893] clamps `ag` to
  zero inside its y-band (~x4210 walk stall via `i.f scrollWallClamp`),
  so the designed route crosses ABOVE the wall: run east off the west
  tower top (3980,160) → S164 vault → S157 bound launch (ag 8192,
  ~32px/t) → arc 470px east → mass B top @(4519,279). The leg enters
  at the tower top — the rail→massA→duel→tower ascent chain is the
  preceding leg's frontier.

## File recovery (this session)

The working tree's `Slice1Test.kt` tail was found corrupted: the
uncommitted append had re-pasted the file tail (duplicate
`Slice277Test`/`Slice280Test`/`Slice282Test`/`Slice281Test`
redeclarations) and a mid-file splice had joined `mission3CapstoneLegB`'s
head onto another test's body — compile was already broken; earlier
"passing" output was stale `TEST-*.xml` from a prior run. Recovered by
restoring the file to HEAD and re-appending a clean `Slice282Test`
containing only legs A + B; legA was authored fresh (no legA survived).

All debug scratch removed with it: `Entity.dbg*` companion fields +
`ah`/`S` setter stack logging + `setAnim` DBG in Entity.kt, the same
dbg writes in PlayerFsm.kt, `/tmp/sticklog.txt` writer and a stray
`tickIndex++` double-increment in Level0World.kt — main sources are
back at HEAD (the two proven slice-268 fixes — the claim-suspension
gate Level0World.kt:4736 and `iBe=false` — are already committed).

## Route map (assembled, pack-9)

mass-face drop → pocket → edge jump → spring aw335 → middle mass →
rail aw578 mount → mass A top → posted-guard duel aw393 → lever aw579
(4128) → west tower (x3860-4060 @y160-240) → **legB arc** → mass B →
aw70 bound-catch / balance aw868 → cp272@(5360,795) cp274@(7552,224)
cp667@(8557,690) cp273@(10873,575) → win fuse aw780 @(13628,1045).

## Gates

- `Slice282Test.mission3CapstoneLegA` — pass (fresh compile + run)
- `Slice282Test.mission3CapstoneLegB` — pass (fresh compile + run)
- verifier `ok:true`, `python3 -m unittest discover -s tests`,
  `:core:test`, `:android:assembleDebug` — see reports/gate-results.
