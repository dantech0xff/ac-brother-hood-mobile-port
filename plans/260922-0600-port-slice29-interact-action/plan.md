---
title: "Port slice 29 — g.ar() the 65568 standing interact action"
phase: port
status: done
slice: 29
---

# Slice 29 — `ar()` interact action + `i.C()` victim hit-react

## What was mined (all `proven` unless flagged)

### `g.ar()` — g.java:4030-4114
The standing context action fired from the S303 arm when the gauge
filled and the player taps 65568:

```
if (g == null) { i(0); k.v(); return; }     // no target → anim 0 + latch clear
k.A(16);                                     // sfx 16
aj = ai = ah = ag = 0;                       // zero velocity
r02 = (Y[1]+Y[3])>>1;  r04 = (g.Y[1]+g.Y[3])>>1
r05 = |g.ak - ak|;     r06 = |r04 - r02| << 8
if (S != 364) pick reach anim:
    r05<=0 || r06<=0 || r04-r02 >= 20 → L29 (r04-r02>20 → i(302); r06==0 → i(301))
    else r08 = r06/r05:  <=64 → i(301); <=256 → i(300); <=1024 → i(299); >1024 → keep
L34: r07 = i.bu[k.au] << 1                   // weapon dmg ×2
if (K <= 3) return                           // gauge under-charged → nothing
i.a(8, 5, 14, av, L, M, 300)                 // THROW the knife (ax8 clip5 anim14 az300, P|=512)
g.ax==4  → g.S==30 → g.i(29)                 // armed destructible → trigger
g.ax==58 → g.i(S+1) on {0,5,7,9,11}, g.i(3) on S==2   // lever toggle
else     → g.aB -= (r07·K)/6; g.C()          // normal: gauge-scaled damage + hit-react
```

Ported as `Entity.interactAction(w, pad)`; the S303 arm now calls it on
`pad.v(65568)` (L1879, g.java:3445).

### `i.C()` — i.java:1955-2054
The victim-side hit-react dispatcher, keyed on `aS.S` (attacker anim):

```
aB <= 0 → L61 death arm: ax11 → ab=null+i(0)+G(); ax73 → i(164)+zero vel;
          ax17 → i(69); ax50 → i(129); ax23 → i(78)
ax11 Z0==1 && aB<=bu[au] → Z0=2, i(144) block stance, aS.a(45, ak, al-85)
ax73 Z0==0 && aB<=bu[au] → Z0=3, Q() face player, i(155), aq = ak∓60,
          aS.i(8), k.E.P|=128   (k.E link unported — flagged)
ax17 S!=68 → i(68) + k.A(13)            (civilian panic)
ax23 → i(73) + k.A(13); aS.S==69 → pull aS to ak∓45/al
ax11/73 live → Z0==2 → i(6) stagger; aS.S==67 → c(6,156);
          aS.S ∈ {68,69,286} → g() + c(6,156); aS.S==287 → L61 dead arm
```

Ported as `Entity.hitReact(w): Boolean`.

### Helpers
- `i.Q()` (7771) face player → `facePlayer`
- `i.aF()` (9192) side-free probe → `sideFree`
- `i.c(4)` (9053) per-type hit anim → `hitAnimByType` (ax11→r4, ax73→r5)
- `i.g()` (1678) resolve-push out of the player box → `resolvePush`
- `i.a(3)` (9810) marker spawn into `ae` → `spawnMarker` (wraps `spawnPickup`)
- `i.a(7)` (6898) ax8 knife projectile → `Level0World.spawnProjectile`
- `i.bu[]` = {300,400,500} (i.java:22315) → `Entity.WEAPON_DMG`
- `k.A(n)` → `e.a(n,false)` = sfx (k.java:7363) → `w.sfx`
- `k.v()` (k.java:7260) clears `bC/bB/eK/eL/eM/eN` held+edge+accumulators
  → `Pad.clearLatches()`
- `k.au` weapon slot → `LevelCellSource.weaponSlot` (default 0)

## Ported
- `Entity.interactAction`, `hitReact`, `spawnMarker`, `resolvePush`,
  `sideFree`, `hitAnimByType`, `facePlayer`; new `s` field (i.s ax51
  side-link, producer unported); `WEAPON_DMG` table.
- `LevelCellSource`: `player`, `weaponSlot`, `spawnProjectile`.
- `Level0World`: `spawnProjectile` (ax8 clip5 — **clip5 unconverted**,
  entity runs with a null clip like other unconverted clips), `player` override.
- `Pad.clearLatches`.
- S303 arm: `v(65568) → interactAction`.

## Gates
- `:core:test` 102/102 green (+15 new tests).
- `verify-static-reconstruction.py` → `ok:true`; `python3 -m unittest` 57 green.

## Flags (honest gaps)
- `k.E` held-entity static (ax73 arm's `E.P|=128`) unported — assignment
  sites at i.java:2504/2767 not yet mapped.
- clip5 (knife projectile sprite) unconverted → ax8 spawns with null clip.
- `i.aG()` (the mirror of aF at i.java:9218) not needed by ar(); unported.
