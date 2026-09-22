---
title: "Slice 55 — ax56 ay() flyer variant (waypoint-free runner)"
phase: port
status: done
---

## Scope

Ports `ay()` (i.java:8385, proven) — the ax56 flyer: same clip family as
ax54 (bi[56]=19, clip19) but waypoint-free — lerps to `(aq,ar)` instead.

- **`initAx56`** — L235 arm (i.java:3336): `az=100`, `aB=300`,
  `Z[0]=r8[4]`, `Z[1..12]=r8[8..19]`, `aC/aD/aF` timers, `n=Z[9]`,
  `aq=ar=spawn` then `Z[10]==1 → aq+=Z[11]` / `Z[10]==2 → ar+=Z[11]`;
  `Z[4]` quirks verbatim (`Z4==0→Z5=1`; `Z4==1→Z5=Z6=1,Z7=0`, `Z3==0→3`).
- **`tickAx56`** — `ay()` verbatim: latch `bz = al > k.P` (no offset —
  differs from ax54's `kP+Z[7]`); `Z[8]--` every armed tick (attack
  cooldown); offscreen removal only when `al > kP+240`; `F` companion
  bind (Z[5]); S 0-4 walk / 5-9 attack (frame `T==3 && U==0` window,
  `aF` reset `Z[7]`, `a(Z[5], aD<Z[6])` burst) / 10 exit (`al > kP+240`
  → k.c) / 13-15 travel (move x or y toward `aq`/`ar` at `Z[12]<<8`,
  one-step clamp when inside `Z[12]`, `l()` anims, L94 cooldown →
  `k()` arrive-anim + stop).
- Helpers shared with slice 54: `runnerBurst` (ax56 arms: `i(Z[3],dirVar)`
  anim, `aG=Z[4]`); new `runnerTraveling` = `az()`, `runnerTravelAnim` =
  `l()` (14/15 vertical, 13 on x-mismatch); `runnerG` = i.G field added.
- bi[56]=19 → `56 to 19` clip map entry (clip19 already converted).

## Notes

- `i.G` is written externally (script/director path, i.java:14075) —
  field plumbed, producer pending the script op.
- `a(i,i,int,boolean)` aimed-shot overload (`i.java:8640+`) used by ax25
  remains unported — ax56's burst goes through `a(int,boolean)`.

## Gates

verify `ok:true` · 57 unittests · `:core:test` all green (+9 Slice55Test)
· `:android:assembleDebug` builds.
