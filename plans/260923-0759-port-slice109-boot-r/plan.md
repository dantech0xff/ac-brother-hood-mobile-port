---
title: "Slice 109 — jC==0 boot R() driver (splash → sound prompt → title)"
phase: port
status: done
confidence: proven (logic) / inferred (tick-mapped timers, anim pacing)
sources:
  - reconstructed-project/src/structured/k.java:3949-4100  # R() boot driver
  - reconstructed-project/src/structured/k.java:796-799    # k.a() case 0 → R()
  - reconstructed-project/src/structured/k.java:1644       # l() resets cu=0
  - reconstructed-project/resources/decoded/pack-14/entry-000-strings.json  # d(0,63/65)
---

# Slice 109 — boot `R()` (j.c==0 / `cu` sub-phases)

## What the original does (`proven`)

`j.c == 0` is the boot state — `k.a()` case 0 calls `R()`, whose `cu`
sub-counter walks the startup sequence. `l()` resets `cu = 0` every call
(k.java:1644), and nothing `l()`s back to 0, so case 0 only ever runs at
real boot:

| cu | work | exit |
|----|------|------|
| 0 | logo clip `bX` load + first draw | `cu++`, `du = now` |
| 1 | fonts/strings/audio + version patch | `cu++` |
| 2 | `bX` anim 0 (Gameloft logo) | `now-du >= 3000` or `v(262144)` → `cu++`, `du=now`; skip plays `z(23)` |
| 3 | `bX` anim 1 (AC logo) + `d(0,63)` legal text | same 3000ms/skip → `cu++`, `du=now` |
| 4 | `d(0,65)` copyright + `e(false)` save load + `au` language | `cu++` (1 frame) |
| 5 | `S()` pack-2/`z[]` preload | `cu++` **falls through to 6** (no break) |
| 6 | copyright text still up | `now-du >= 5000` → `cu++` → `f.a()` resize + `l(23)` |

## Port

- `menuStates += 0`; `menuFrame` `0 -> bootR()` ahead of the catch-all.
- `bootR()` (`Level0World.kt:2480-2519`): cases 0/1/4/5 keep only their
  `cu++` transitions — asset loads and the `e(false)` record read are
  create()-time here (`saveLoad` in `Level0Game.create`). Case 5's
  fallthrough is preserved by calling `bootLoadCheck()` inline.
- Timers: original uses `System.currentTimeMillis() - du`; mapped onto
  `jG` ticks — `>= 3000ms` ⇔ `jG - kDu >= 49` (⌈3000/62⌉), `>= 5000ms` ⇔
  `>= 81` (`inferred` — same threshold semantics, deterministic clock).
- `kDu` = jG snapshot at each `cu` transition (the `du = now` writes).
- `bU` += decoded `d(0,63)` ("ALSO AVAILABLE ON…") and `d(0,65)`
  (Ubisoft copyright) from pack-14 entry-000 (`proven` strings).
- Renderer `bootScreen`: full black fill (every case `setColor(0) +
  j.b`), `bX` = clip 0 anim 0/1 at (200,120) with per-call anim tick
  (`(jG-kDu) % frameCount`), wrapped `d(0,63/65)` text.
- `Level0Game.create()`: `world.stateL(0)` after `saveLoad` — the real
  app now boots through the whole chain: splash → sound prompt → title
  → menu → play. Tests keep `jC=8` default (post-boot state).

## Tests (`Slice109Test`, 5/5 green)

- cu 0→1→2 load-frame advance; 47-tick dwell holds, tick 48→cu3.
- `v(262144)` (M_PAUSE edge) skips each logo dwell.
- cu6 has **no** pause skip (verbatim); after 81 ticks → `l(23)` +
  `cu=0` reset by `l()` (proven write, asserted).
- End-to-end: stateL(0) → 180 ticks → jc23 → confirm YES → jc18 →
  context press → jc2 main menu.

## Gates

- `verify-static-reconstruction.py` → `ok:true`, failures `[]`.
- `python3 -m unittest discover -s tests` → 57 pass.
- `:core:test` → all green; `:android:assembleDebug`, `:gdx:build` →
  BUILD SUCCESSFUL.
