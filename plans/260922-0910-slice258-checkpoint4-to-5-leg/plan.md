---
title: slice 258 — checkpoint4→checkpoint5 bot leg (gate guards)
status: done
---

# Slice 258

## What landed

`Slice1Test` — `bot runs checkpoint4 to checkpoint5 through the gate
guards` (14th golden-path leg). Parks on ax2 uid100 (5927,732) and runs
east on real input: past the ax11 gate-guard pair uid302/303 (6129-6144,
y753-760), under the ax44 door-bar row (x6420-6761 @y857 — below the
walk line), by waypoint uid933 (7000,700), to ax2 uid101 (7110,718).

Result: `checkpoint=true deaths=0 maxAk=7107` in ~120 ticks — the bot
crossed at S12 run without an engagement (the dormant S=2 gate pair
never entered its alert window). Reuses the leg harness: foe-scan
slash (ax11/ax4), stuck→jump, S89/S90 shake-off, and the op108
prompt-answer arm (`claimActive` → `pad.e(M_CONTEXT)`).

## Record layout verified

`runtime_type` = `f[0]` is the dispatch ax (uid302/303 are ax11 soldiers
at the gate, not checkpoints); `f[5]` is the initial S variant. Level-0
checkpoints in this span: uid100 (5927), uid101 (7110), uid102 (8926).

## Gates

- verify-static-reconstruction: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: 1478 pass
- `./gradlew :android:assembleDebug :gdx:build`: green
