---
title: "Slice 88 — j.t pad-held latch + j.i() fail/win input flush"
phase: port
status: done
---

# Slice 88 — the held-pad flush on fail/win screens

Ports `j.t` / `j.i()` (j.java:105-345, proven): a bitmask latch of held
pad bits 0-4 that the fail/win screen cases (`k.java:1109`) flush so a
key held through the transition can't instantly confirm the dialog.

## Semantics (proven, file:line)

- `j.a(i,z)` (:1335): pad press → `t |= 1<<i` for `i<5` (`bit&31`).
- `j.b(i,z)` (:1344): release → `t &= ~(1<<i)` per bit.
- `j.i()` (:1330): `t != 0`.
- `j.t = 0` (:1322): force-clear — used by screen cases 12/13:
  `if (!j.i()) { draw } else { j.t = 0 }` (k.java:1109-1120) — the
  frame's draw + `L()`/`Q()` are skipped while a low bit is latched.

## Port mapping (`inferred` where the models differ)

- J2ME `keyPressed/keyReleased` → our touch DOWN/UP: `iJ<5` wheel cells
  latch `kJT |= 1<<iJ` on `padE`; any UP/CANCEL clears all (`kJT = 0` —
  single-touch makes per-bit and whole-mask release equivalent).
- `menuFrame`'s 12/13 arm: `if (kJT != 0) { kJT = 0; return true }`
  before `scrollBounds()/menuL/menuQ` — verbatim `j.i()` skip.
- Key-repeat devices re-fire `keyPressed` while held (t stays latched
  until release); our model flushes once — flagged `inferred`.

## Files

- `core/.../Level0World.kt`: `kJT`, latch/clear in `consume()`, flush
  arm in `menuFrame` 12/13.
- `core/.../Slice1Test.kt`: `Slice88Test` (4 tests).

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` green;
  `:android:assembleDebug` green.
