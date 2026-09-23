---
title: "Slice 116 — jC==9 play-entry arm (save restore + C() + T() + F(aj))"
phase: port-screens
status: merged
date: 2026-09-23
---

# Slice 116 — the load→play hand-off, complete

Completes the `k.a()` case-9 exit arm (k.java:797-812, proven) — the
last unported piece of the load screen. Pressing fire after the
`G(j.g)` load gate now runs the full verbatim arm, replacing the
placeholder that only restored the save bytes.

## What landed

### The complete exit arm (k.java:802-812)

`bG=0; dl=null; A[5]=A[1]=null; a(bA,16,0); ax=dB; ay=dC; aN=dF;
g.e(ax); C(); T(); dz=120; aw=0; if(ef[aj]) ab(); l(8); z(23); F(aj)`

- `kBg = 0` — one-shot music-request slot reset (field added;
  `bG=-1` init per :310).
- `dl = null` / `A[5] = A[1] = null` — soft-async clip loader +
  pack-2 art releases — **no port equivalents** (eager decode),
  documented.
- `kBA[16] = 0` — `a(bA,16,(short)0)` save-byte clear.
- `player.x1 = kAx` — `g.e(ax)` → `x[1] = ax` (g.java:4421) — the
  HP meter byte IS the health cap/restore.

### `C()` — camera/sim accumulator reset (k.java:1851-1875)

`P=O=cB=cA=cD=cC=Q=0; Z=false; ab=false`; non-bh3 → `aR=-1; ak=0;
X=0; n(); m(ad)` (bound release + mode-2 snap — so `P=0` gets
overwritten by the re-track, verbatim); bh3 (missions 1/4) →
`dU=0; dR=-1; ak=0; aR=-1; dS=-2; dT=(bu-20)-(20*aR)=bu; Q=230;
D(); cA=O=aS.ak-200; cB=P=aS.al-230; W=0; X=-7; V=-7` — the flying
arm is testable via `kAj=1`.

### `T()` — HUD indicator spawn (k.java:4165-4173)

`dA = new i(); dA.aa = z[12]; dA.i(0); dA.ak=al=0; aA=0` → `kDA`
standalone entity (never enters `bb[]`).

### `F(aj)` — per-mission input setup (k.java:3551-3558)

`i.bS=0` (write-only dead flag — `i.bS` is never read anywhere in
this build, kept verbatim as `Entity.entBSLatch`), `aj>0 → i.j(1)`
(`bS|=1`), `g.I=1; g.J=0; g.g(f0do[aj])` — `f0do={5×9}` (k.java:210)
→ `gJ|=5; rebuildEquip()` (`k.q()` — mask-4 skip verified: bits
{1,4} → only slot mask-1 lands).

### `ab()` / `ef[]` — verbatim dead code

`ef[]` is all-false with no writer (k.java:264) → `trailAb()` is an
unreachable documented stub (needs `z[58]` frame-rect API anyway).

### Proven init corrections

- `kAx`: 90→**30** (`byte ax = 30`, k.java:223) — the meter-cap byte.
- `kDB`/`kDC`: 0→**30** (`byte dB = 30`, `byte dC = 30`, :225-226).
- This *engaged* the previously-inert HUD `g.f(ax)` clamp
  (`x1 = min(x1, ax)` — the cap is real). Two old tests that baked
  the `kAx=90` fudge were pinned with explicit `kAx` setup.

### New fields

`kBg, kAk, kQ, kV, kDU/kDR/kDS/kDT, kDA, kEfArr, kF0Do`,
`Entity.entBSLatch`.

## Gates

- verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
- `:core:test` green (844 tests incl. 8 new Slice116Test);
- `:android:assembleDebug` + `:gdx:build` green.

## Files

- `rewrite/core/.../Level0World.kt` — arm + procs + fields + init fixes.
- `rewrite/core/.../Entity.kt` — `entBSLatch` companion static.
- `rewrite/core/src/test/.../Slice1Test.kt` — `Slice116Test` (8 tests)
  + `kAx` pins in `iframes`/`collectStreak` tests.
