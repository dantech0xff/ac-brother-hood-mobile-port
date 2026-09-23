---
title: "port slice 125 — i.K() held-release + i.d(3-arg) debris + k.bG music slot"
phase: rewrite/port
status: done
confidence: proven unless noted
---

# Slice 125 — held release, debris spark, pending-music slot

Three verbatim ports mined from `reconstructed-project/src/structured/`.

## i.K() — companion held release (i.java:5716)

`aS.T<=2 && aS.S∈{67,68,69,106}` → held entity `i(0/1/2/3)` + `P&=-129`
(unhide). Called from g.java:4370 (attack entry) and g.java:2495 (combo
chain). Ported as `Entity.heldRelease(p)`; wired into `contextDispatch`
(`gI==1` arm) and `PlayerFsm.comboArm`'s else-branch — both call
`w.kE?.heldRelease(p)` now that `k.E` (the ax34 companion) exists.

## i.d(int,int,int) — debris spark (i.java:16745)

`a(24,40,anim,201)` spawns an ax24 clip-40 debris entity: `av=false`,
`N/O=(x,y)<<8`, zeroed velocity, `k.b()` queue insert. Its two
`d(8,x,y)` call sites sit inside `bc()` (:14182/:14201) at the
`ad.i(2)` destruction points. Ported as `Entity.spawnDebris24(w,anim,x,y)`;
called from `sweepNeighborsB`'s ax54/ax30 destruction arms. NOTE: the
2-arg `i.d(int,int)` (:7708) is a different function (ae-marker re-park)
— already ported as `moveMarker`/`markerTapped74` in slice 65.

## k.E fills

Previously-dropped `k.E.P |= 128` writes (companion hide) filled at both
sites: `iEngage` (i.java:1298) and `hitReact`'s ax73 enrage arm.

## k.bG — pending-music slot (k.java:310)

- `bG = 0` at play-entry menuJc9 (was already ported at :2884).
- **New**: the play-entry tail `if (bG>=0) B()` (k.java:5229) — mission
  music `ee[aj]` now actually starts after `l(8)`; added to `menuJc9`.
- **New**: suspend/resume pair (k.java:5817-5830, proven): pause →
  `bG = !e.a()||fj>=10 ? -1 : bH`; our command-queue audio is always
  available → `suspendAudio() { kBg = kFi }`. Resume → `bG>=0` replays
  `z(bG)`, or `fi=bG` while `j.c==14`. Hooked into
  `Level0Game.pause()/resume()` (were stubs).
- Verbatim quirk kept: the resume gate `bG==1 || bG!=-1` collapses to
  `bG != -1`.

## Test fix

`jc9 restores mission state…` asserted `audioTrack==23` (last z());
`B()` now runs after `z(23)` and plays `ee[0]=5` — assert updated to
`audioTrack == kEE[kAj]` with the reasoning documented.

## Gates

verifier ok:true; 57 unittests; :core:test 867 green; :gdx:build green.
