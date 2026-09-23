---
title: Slice 161 — S284 rope-grab anim arm + S16 stale-comment fix
phase: port
status: done
---
# Scope

- **S284 arm** (g.java:3837-3841, proven): `r() → P |= 64`. The ax10-S16
  door zone's `aS.i(284)` (i.java:12391) plays the door-enter anim; at
  anim end the flag arm sets `P|=64` and the player STAYS in S284 — the
  default arm's anim-end `a(0)` fling must not run. (The rope-attach
  reading in the old header comment was wrong: S16 is a door/teleport
  pair — `bi`/`bh` are the door arrive/exit arms, `r8.i(17)`/`i(19)`
  open/close the linked door, `aS.a(105)` = "press up" marker,
  `k.bI>13` = mid-fade handoff, `aS.a(0)` = post-fade fling.)
- **Stale comment fix** (NpcFsm.kt:473): replaced "S16 rope-attach,
  L625, stubbed" — S16 has been fully ported since slice 99
  (bh()/bi() fade transitions).

# Tests

- `S284 rope grab anim end sets P64 no fling` — anim end sets `P|64`,
  S stays 284, `aj != 1536` (no `a(0)` fling).

# Gates

- verifier `ok:true`; 57 unittests; 416 `:core:test` (1 new);
  `:android:assembleDebug` + `:gdx:build` clean.
