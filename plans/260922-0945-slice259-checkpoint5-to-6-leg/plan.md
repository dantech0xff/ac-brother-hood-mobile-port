---
title: slice 259 — checkpoint5→checkpoint6 bot leg (fence row + rooftop pack)
status: done
---

# Slice 259

## What landed

`Slice1Test` — `bot runs checkpoint5 toward checkpoint6 through the
fence row` (15th golden-path leg). Parks on ax2 uid101 (7110,718) and
runs east on real input across the widest verified span yet (~1820px):
the ax5 director uid926, low-road soldier pack uid44/602/603 (y917-918),
ax13 rope uid333 (7444,325 aG=4), the ax4 destructible chain
(7688-9137), the 8-bar ax44 fence row (x7902-8183 @y717-799), ax37 cam
bounds, and the rooftop soldier pack uid587/537/523 (8620-8709 @y256) —
to ax2 uid102 (8926,757).

Result: `checkpoint=true deaths=0 maxAk=8929 minAl=325` — first attempt
clean. The marks show a real traversal: S69 combat at the destructible
line, S43 fall, S164 bound-state ride, S157 — the bot alternated the
low road and the y325 rooftop band rather than stalling on the fence
row. Generic policy only: foe-scan slash, stuck→jump, S89/S90
shake-off, S65 climb, op108 prompt-answer.

## Gates

- verify-static-reconstruction: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: 1479 pass
- `./gradlew :android:assembleDebug :gdx:build`: green
