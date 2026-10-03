---
title: "Slice 366 — app pause/resume is k.c()/k.d() (hideNotify/showNotify)"
phase: "port"
status: "done"
slice: 366
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/j.java:154-183
  - reconstructed-project/src/structured/k.java:5767-5836
  - reconstructed-project/src/structured/k.java:2653-2655
  - reconstructed-project/bytecode/k.javap.txt:33113-33122
---

# Slice 366 — hideNotify / showNotify

Found while doing Phase 3 step 7 (lifecycle): the port's `suspendAudio`/
`resumeAudio` stashed the current track on pause and replayed it on resume.

## Findings (proven)

- `j.hideNotify() → c()` / `j.showNotify() → d()` (structured j.java:154-183)
  dispatch to `k.c()` / `k.d()`.
- `k.c()` (k.java:5817-5836): once per hide (`fy` latch) — `v()` clears the
  input latches; in play a `cd[6]` claim script is paused (`C.Y()`);
  `bG = (!e.a() || fj >= 10) ? -1 : bH`; `e.b()` stops the channel.
- `bH` and `fj` are written only by `<clinit>` (= -1, k.javap.txt
  3713-3722; no other `putstatic`), so `bG` is always -1.
- `k.d()` (k.java:5767-5813): `bv == 3` (yes/no prompt) → eC 13/19/25/69/73
  set `bw = -1`; in play (`j.c` 8/21) `J()` (= `j.c ∉ {12,13}`, k.java:2653)
  is true → `l(14)` (pause menu; the `C.Z()` arm behind it is unreachable);
  on the pause menu `bw = 0`. Then `j.c != 14 → z(bG)` if `bG != -1`, else
  `fi = bG` if `bG != -1`; `v()`.
- Consequence: resuming the app in play always lands on the pause menu, and
  — because `l(14)` clears `fi` when no track is live (k.java:5180) after
  hide's `e.b()` — the music is gone until something plays again.

## Fix

`Level0World.hideNotify()` / `showNotify()` follow the above
(`suspendAudio`/`resumeAudio` removed); `Level0Game.pause()/resume()` call
them. The stale `kEc` comment ("screen timer") now says what it is: the
yes/no prompt string id.

## Tests

`Slice366Test` (6): hide clears latches/stops the channel/stashes -1; hide
pauses a `cd[6]` script; show in play opens the pause menu, replays nothing
and leaves `fi = -1`; show on the pause menu resets `bw`; show on a yes/no
prompt sets `bw = -1`; the `fy` latch. The old API replayed the track and
never opened the pause menu.
