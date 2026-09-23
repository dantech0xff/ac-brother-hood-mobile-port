---
title: "Slice 71 — k.l(13) win screen + dispatch cleanup"
phase: 71
status: done
---

# Slice 71 — `k.l(13)` win path

`screenL(13)` was a no-op — script op105 (`w.kBw=-1; w.screenL(13)`,
Entity.kt:1935) and `i.a()` big-op arms fired the win transition into a void.

## Ported

- `won` flag (j.c==13 win screen active) mirroring `failed`; `inPlay` gates
  on `!failed && !won`.
- `screenL(13)` → `won = true; missionWon = true` (idempotent —
  `else if (n == 13 && !won)`).
- tick arm mirrors the fail screen: world frozen, `v(65568)` context edge →
  `reload()` (the win screen's confirm advances the milestone flow — only
  level 0 exists, so reload is the honest equivalent; `inferred`).
- `reload()` clears `won`.
- Merge-artifact cleanup: duplicated `ax==42`/`ax==35` dispatch arms at
  Level0World.kt:~1298.

## Source semantics (k.java:2031 `l(int)`)

`l(13)` and `l(12)` share the `L12` tail → `al=true` → `j.c = r6` (screen
swap off play = world frozen). Screen-13's own confirm handler
(k.java:1804/1806, `v(65568)` → `l(13)` re-entry / `bx=-1`) advances the
milestone flow — screen itself unported (`inferred`).

## Tests (`Slice71WinTest`, 2 tests)

freeze + `inPlay` false + context-edge reload refills meter; idempotency.

## Gates

verifier `ok:true`; 57 unittests; `:core:test` all green;
`:android:assembleDebug` builds.
