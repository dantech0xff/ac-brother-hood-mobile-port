---
title: "Slice 382 — one k.e(0,aw) kill tally and one k.o(n)"
phase: "port"
status: "done"
slice: 382
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:3257-3269
  - reconstructed-project/src/structured/k.java:1666-1700
  - reconstructed-project/src/structured/i.java:5770-5818
  - reconstructed-project/src/structured/i.java:5907-5911
  - reconstructed-project/src/structured/i.java:6195-6203
---

# Slice 382 — the stats tallies

Reported by slice 371 ("`k.e(0,aw)` has three ports").

## The original (proven)

- `k.e(i, i2)` (k.java:3264-3269): `if (i2 <= 0 || aj == 7) return;
  ap[0]++` — the first argument is unread. Every NPC/player kill calls
  `k.e(0, aw)`. `ap[0]` is the stats screen's "ENEMIES KILLED" row and
  feeds the `ap[0] >= 7` medal stamp (k.java:1666-1700).
- `k.o(i)` (k.java:3257-3262): `if (i == 3 && aj == 7) return; ap[i]++`.

## The port (before)

Three `k.e(0,aw)` ports: `countKill` and `kStatE` (both `ap[0]`) and
`statTally`, which fed a `statTally0` counter nothing read — six NPC kill
sites called it (hostage secure S168, S20 and S176 corpse drops, S117,
the S106/107/135 r() arm, the boss kill-bitmap router). Two `k.o(n)`
ports (`kStat`, `kCount`) plus raw `kAp[3]++` at four `o(3)` sites
without the mission-7 guard.

## Changes

`countKill` is the one `k.e`, `kCount` the one `k.o`; `statTally`,
`statTally0`, `kStatE`, `kStat` are gone and every site calls the two.
The m6 capstone legF counts the `l(15)` → `l(22)` medal redirect as the
win (its kills now stamp the `ap[0] >= 7` medal) — the win event is the
same `l(15)`.

## Tests

`Slice382Test` (2): an S20 corpse drop reaches `ap[0]`; mission 7 counts
neither `ap[0]` nor `ap[3]`. Slice135's fake world and the S176 test
moved to `countKill`/`kAp[0]`.
