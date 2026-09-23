---
title: "Slice 128 — bs() bp() polarity, S43 aQ==3 dismount, i.D() link sweep"
phase: port
status: complete
slice: 128
---

# Slice 128 — crate-contact chain fixes

Continuation of the ax51 crate / `i.bq` crate-top work from slice 127.
Three verbatim arms in the same call chain were wrong or missing.

## 1. `i.bp()` polarity inversion (proven fix)

`bp()` at i.java:15567 is `if (g.j) return false;` then the
falling-over-top check — our `crateLandSpot` had `w.gj && (...)`:
always-false, so the `g.c` claim at bs() S0/1 (i.java:15712-15713) was
unreachable through the `bp()` arm. Fixed to `!w.gj && (...)`.
(`g.j` is never set true in the original — only cleared — so `bo()`
is dead and `bp()` is the live arm; cite updated to the shared pair
i.java:15563/15567 — there is only one `bo`/`bp` pair in `i`.)

## 2. S43 `aQ == 3` dismount arm (g.java:1421, proven — was missing)

Inside the case 16/35/43/150/252 airborne block:

```java
if (this.aQ == 3) {
    if ((c != null && c.ax == 51 && this.al > c.W[3]) ||
        ((i.bq > 0 && this.al > i.bq && c == null) || (c == null && i.bq == 0))) {
        i(147);
        i.bq = 0;
    }
} else if (land cells) { d(...) } else { aj = 1536; ledge }
```

Ported into `PlayerFsm.fallArm` as the first arm: feet cell is
marker-3 → crate-top dismount probe (`g.c` = `world.gc`; `i.bq` =
`Entity.entBq`) → `i(147)` + `i.bq = 0`. Note: `aQ == 3` never fires on
level 0 — the et collision layer contains zero cell-3 values (verified
by histogram), so the arm is data-driven dead code here; marker-3 cells
on other levels come from tiles, not entity writes.

## 3. `i.D()` link sweep (i.java:1795-1821, proven — was missing)

The entity-system reset clears all `g.*` links (`b/a/h/c/e/g/d/l/m/j/
q/r`) plus `k.B`, `k.F`, `k.aD`. Our `spawnEntities()` (level reload /
checkpoint reload / mission restart) never cleared `player.ga`,
`player.ac`, `player.standingOn`, or `world.gc` — stale links to
destroyed entities survived reload. Added the sweep.

## Also in this slice

- `w.iBq` removed — a dead duplicate of `Entity.entBq` (`i.bq` is a
  static on `i`); bs() mount arm now clears `Entity.entBq` directly.
- `airWallResolve` (`g.av()`, g.java:4984): bail arms completed —
  `aO >= 20 → a(0)` (enterFall, NO return — falls through to the bail
  check, verbatim), `aO < 20 || ah >= 0 || i.bq != 0 → return`.
  **Proven dead tail**: after `a(0)` sets `ah = 0`, the `ah >= 0` arm
  always returns; and `aO < 20` returns for anything under 20 — the
  wall-kick tail can never run. Kept verbatim with the finding
  documented.

## Tests (Slice128Test, 7 tests)

- S43 + `aQ==3` + no contact/`entBq` → `i(147)`, `entBq` cleared.
- S43 + `aQ==3` + `al > entBq` → `i(147)` (second dismount arm).
- S43 + `aQ!=3` → falls to the normal cell ladder (`aj=1536`).
- `bp()` claim: player at `al == crate.W[3]` in S43 falling → `gc`
  claimed (position chosen so the mount arm's `al < W[3]` is false).
- `P&1024` latch suppresses the `gc` claim.
- Mission-fail reload sweeps `ga`/`ac`/`standingOn`/`gc`.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` 877 · `:android:assembleDebug` · `:gdx:build`
