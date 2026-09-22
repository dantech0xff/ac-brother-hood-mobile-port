---
title: "Slice 54 — ax54/ax30 waypoint runner + ax24 projectile pool + ax55 waypoint records"
phase: port
status: done
---

## Scope

Ports the flying-level waypoint-runner subsystem (packs 7/10, missions 1/4)
verbatim from `i.java`/`c.java`/`k.java`:

- **ax55 records → `Waypoint.Pool`** (`k.java:6049` — `r0[0]==55 → c.a(r0)`):
  data-only records `{uid,x,y,c,d,e,f,g}`; never spawn entities. 400-slot
  pool, copy uid base `j=10000` (`c.java`, proven).
- **`initAx54`** — the shared L214 init arm (`i.java:3281`): `az=100`,
  `aB=300`, `Z[15/16]`=spawn, `Z[0]`=mode, `Z[1..14]=r8[7..20]`, `aC/aD/aF`
  timers, `n=Z[14]`; mode≠0 spawns a bound ax68 child (`ad.af=this`,
  `az=-1` for ax54 / `99` for ax30).
- **`resolveRunnerWaypoints`** = `R()`/`aw()` (`i.java:8131/8127`): after
  level load, each runner resolves up to 4 waypoint uids in `Z[1..4]` into
  entity-x-shifted pool copies (remapped to minted uid, `C`=count).
- **`tickAx54`** = `ax()` (`i.java:8170`): trigger latch `bz`, offscreen
  removal `k.c`, waypoint homing legs (`bt.a/b` vs scroll-pos `bY/bZ` —
  `bF()` refreshes every tick under `bh[aj]==3`, i.java:4906), `D` dwell
  counter + `bs` chain advance, `F` companion bind, walk anims 0–4 /
  attack burst 5–9 / exit 10, velocity caps (L123), `integrate()` tail.
- **`initAx24`** — `case 24 → L75` (`i.java:2837`): `aB=r8[7]`; the S==0
  record seeds `k.aX[50]` with `a(24,bi[24]=40,S0,az200)` children
  (`P=this.P`, `aG=-1`). `projectileAlloc` = `av()` (`i.java:7813`) —
  picks slots with `P&128` **set** (reserved); arming clears bit128.
- **`runnerBurst`** = `a(int,boolean)` (`i.java:7826`): count>1 angular-fan
  volley vs count==1 single shot; launch `X`-center<<7 for ax54/30/56
  (else `W`-center<<7); aim = companion `F` or the player `W` center;
  speed 2048 (ax54 `Z[9]==2` → 1280); velocity `(dir*speed)/dist + k.Y`;
  facing `av`/`P|1`, anim `i(Z[8],dirVar)`, `aG=Z[9]` (ax54/ax30),
  `af=owner`, `aC=n`; first-shot sfx 27 (ax30) / 16 (else).
- Helpers proven-verbatim: `Q()` face-player, `d(400)` proximity,
  `j()/k()` walk/arrive anim pickers (ax30 remaps 4/9), `i(int,int)`
  anim-set selector, `b(4)` dominant-direction index (5120 threshold),
  `e.h` approximate distance.

## Decode finds

- `P&128` on `k.aX` slots is inverted vs. first read: `av()` returns slots
  **with** bit128 set (reserved); `P &= -129` frees them for re-pick… the
  seed arm's `P |= 640` (512+128) is what marks pool children reserved.
- `bY/bZ` are NOT stale on ax54's path: `b(true)` calls `bF()` every tick
  when `bh[k.aj]==3` (i.java:4906) → homing uses live scroll-coords.
- L226 quirk kept verbatim: `Z[8]==1 && Z[9]==1 → Z[9]=0`.
- `ba()` (ax24 projectile flight FSM) + `ay()` (ax56) remain for the next
  slices — pool children currently never tick (default arm).

## Gates

- verify-static-reconstruction.py → ok:true
- python3 -m unittest → 57/57
- :core:test → all green (+11 Slice54Test)
- :android:assembleDebug → APK built
