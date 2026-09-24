---
slice: 188
title: "f.bF LOADING overlay + stale-comment cleanup"
confidence: proven (call-site/flag/paint), inferred (fill progress)
sources:
  - reconstructed-project/src/simple/f.java:879-930   (f.a(str,int) IGP entry)
  - reconstructed-project/src/simple/f.java:1850-1870 (f-loop paint: bF + bar)
  - reconstructed-project/src/simple/f.java:1399      (bF = null on exit)
  - reconstructed-project/src/simple/k.java:1460      (save-slot call site)
  - reconstructed-project/src/simple/k.java:4786      (state-27 call site)
  - reconstructed-project/src/simple/k.java:5259      (GloftASBR.c call site)
  - reconstructed-project/resources/decoded/pack-14/entry-000-strings.json
    (id 24 = "LOADING")
---

## What

Ports the `f.bF` LOADING overlay that the original shows during save-slot
loads and IGP/store entry — plus cleanup of stale "unported" comments on
code that is already ported.

### `f.a(d(0,24),0)` → `kLoading`

The original calls the IGP machinery's `f.a(loadingMsg, appLanguage)`
with `d(0,24)` = "LOADING"; it sets `bF` and spawns the f-thread, whose
paint loop draws `bF` centered `HCENTER|BOTTOM` over the live screen plus
a white-outlined progress bar (`drawRect` + red `fillRect`, f.java:1857-
1863). `bF = null` on IGP exit (f.java:1399) clears the overlay.

Port: `loadingShow()` and `enterIgp()` set `world.kLoading = true` (the
IGP shop machinery itself stays proven-dead). `stateL(i)` clears it when
`i != 27` — screen 27 is the load/store state the overlay accompanies.
Both are now `public` (the J2ME `f.a`/`f.b` are public statics).

### Renderer

`Level0Renderer` draws the overlay last (over everything, like the
f-thread): white 1px rect outline (drawn as 4 thin fills, J2ME stroke),
red fill stub ~35% (progress counter is IGP-internal, `inferred`), and
`d(0,24)` = "LOADING" centered via the `y` bitmap font.

### Stale comments

- `Entity.kt:530` — mount/grab consumer arms marked ported
  (mountEntry/lungeTick).
- `Level0World.kt:2251` — `B() — unported` corrected to `B() →
  missionInit` (mission music/init picker, k.java:2021).
- Orphaned `/** e(true) — RMS save flush; unported → stub */` line
  deleted (`saveFlush` is fully ported at :2577+).

## Tests

`Slice188Test` — 4 tests:

- `loadingShow`/`enterIgp` arm `kLoading`.
- `stateL(27)` keeps `kLoading` (overlay outlives the transition).
- `stateL(8)` clears `kLoading` (`bF = null` on exit).

## Gates

verifier `ok:true` · 57 unittests · `:core:test` green ·
`:android:assembleDebug` · `:gdx:build`.
