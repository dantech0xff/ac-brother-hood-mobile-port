---
title: "Slice 155 — i.D() static-reset tail + load-order fix"
phase: port
status: done
confidence: proven
---

## What

Completes the entity-system static reset `i.D()` (i.java:1795-1865) in
`spawnEntities()` and fixes the load/reload ordering so the reset runs
BEFORE the `k.a(z2)` checkpoint restore — as in the original.

### i.D() tail added to spawnEntities (all proven at i.java:1795-1865)

- `iZ = true` (i.z), `iBn = false` (i.bn), `iAJ = 0` (i.aJ)
- `kB = kAU = kAV = null` (k.B/aU/aV), `kF = kC = kAD = null` (k.F/C/aD)
- `kAi = false` (k.ai), `kAZ = false` (k.aZ)
- `camAf = camAg = 0` (k.af/k.ag — camera-focus offsets written by ax10
  S0 zones; consumed by k.m tracker)
- `kAE = 100`, `kAF = 0`, `kAH = -1` (k.aE/aF/aH)
- `kN()` (k.n(-1) — scroll-wall release)

### Ordering fix (the real bug)

Original sequence on load/reload: `i.D()` full static reset → `k.a(z2)`
bA restore → respawn. The port ran restore first, so D() would clobber
restored globals. New order in both `init` and `reload()`:

    spawnEntities()      // i.D() + records
    statsReset()         // L() + a(z2) stat restore   (reload only)
    resetPlayerToSpawn() // bA pos/globals restore
    postSpawn()          // player-ctor k.b inserts (kD/kE at restored pos)

`spawnCompanions()` + the pendingInsert drain moved out of
`spawnEntities()` into `postSpawn()` — the original spawns the companion
overlays inside the player ctor arm AFTER pos is set (i.java:2748-2779).

## Tests (Slice153Test, 2 new)

- `entity reset clears k statics before restore` — checkpoint taken with
  camAf/AZ/bn set; after resetLevel, D() values stand (camAf=0, kAE=100,
  kAH=-1, kAi=false, kAD=null, iZ=true) while the snapshot-restored
  aZ/bn keep their checkpoint-time true values.
- `fresh load clears aZ and bn` — no checkpoint → D() defaults stand.

Also un-breaks Slice68Test `kD/kE companion spawn` (init order now gives
companions the positioned player.ak=85 again — caught by the suite).

## Gates

verifier ok:true | 57 unittests OK | :core:test all green (incl. new 2)
| :android:assembleDebug OK | :gdx:build OK
