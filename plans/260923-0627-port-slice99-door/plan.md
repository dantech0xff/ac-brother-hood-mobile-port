---
title: "Port slice 99 — ax10-S16 door-teleport (bh()/bi() fade transitions)"
phase: port-slice-99
status: done
---

# Slice 99 — ax10-S16 door-teleport

Ports the paired-door teleport arm of the ax10 `aV()` TriggerController,
unstubbed by slice 98's fade machinery (`k.B(26)`/`k.C(26)` + `bI` ramp).
The slice-20 label "rope-attach" for S16 was wrong: S16 is the
door-transition arm.

## Semantics (all proven — i.java)

### S16 tick arm (i.java:12326-12431 = L177d→L1850)

```
if (g.a != null) return                          // L177d: grapple actor live
az = 300                                         // L1784: marker TTL
r8 = k.q(Z[0])                                   // destination door entity
if (aS.ac == this) {                             // player bound to THIS door
    if (!an && !ao) { bi(); return }             // fades idle → arrive
    if (ao && bI > 13) { r8?.i(19); aS.a(0) }    // mid-fade-out → open dest + fling
    return
}
if (o == -1) return                              // unbound, no link → dead zone
if (a(aS.W, W) && g.g == null) {                 // overlap, no interact target
    aS.a(105, (W0+W2)>>1, al)                    // spawn id-105 prompt marker
    if (k.v(16388) && !g.b(aS.S) && aS.aZ) {
        G(); bh(); aS.i(284)                     // exit!
    }
    if (an && bI > 13) r8?.i(17)                 // mid-fade-in → dest opens
} else {
    G()                                          // L1850
}
```

### `bh()` — door-exit (i.java:14421)

`aS.aj=ai=ah=ag=0`; `aS.ak = (W0+W2)>>1`; `k.q(this.o)` dest door →
`aS.a(iVar)` bind (`P|=256`); `k.B(26)` fade-in. The destination door's own
S16 tick then sees `aS.ac == this` and runs `bi()` once fades idle.

### `bi()` — door-arrival (i.java:14437)

`aS.ac==this` guard; `av=(aD&1)!=0`; `ak=(W0+W2)>>1`, `al=W[3]` (bottom-center);
`i(285)` + `t()`; `k.ah?.I()` scroll-wall tick; `k.m(k.ad)` camera snap;
`k.C(26)` fade-out.

### `i.a(iVar)` — ac bind (i.java:231)

`ac?.P &= ~256; ac = iVar; iVar?.P |= 256` — ported as `Entity.bindAc()`.

## Port mapping

| original | port |
|---|---|
| `g.a` (grapple actor) | `player.ga` |
| `k.q(uid)` | `LevelCellSource.findByAw` |
| `this.o` (link uid) | `Entity.oId` (r8[12] — i.java:2182) |
| `k.an`/`k.ao`/`k.bI` | world `kAn`/`kAo`/`kBI` (new interface decls) |
| `k.B(26)`/`k.C(26)` | `fadeIn()`/`fadeOut()` (new interface decls) |
| `k.ah?.I()` | `refreshScrollBounds()` → `fireScrollTriggers()` (inferred mapping: our synthetic `kAh` has no per-tick fn; the ax37 holder's `al()` work is the bounds refresh) |
| `k.m(k.ad)` | `kM(kAd)` (existing) |
| `aS.a(105,cx,al)` | `player.spawnMarker(w,105,cx,al)` |
| `aS.a(0)` | `player.flingAirborne(0,w)` |
| `k.v(16388)` | `padHeld(16388)` edge |
| `g.b(aS.S)` | `playerAttacking()` |
| `G()` | `dropAeLink()` (on the door) |

## Signature change

`NpcFsm.tickTrigger(e, player)` → `tickTrigger(e, w, player)` — needed for
`findByAw`/`padHeld`/fade fields. 7 call sites in `Slice1Test.kt` updated.

## Files

- `rewrite/core/.../Entity.kt`: `bindAc()`; LevelCellSource decls `kAo`,
  `kBI`, `fadeIn()`, `fadeOut()`, `refreshScrollBounds()`.
- `rewrite/core/.../Level0World.kt`: `override` on kAo/kBI/fadeIn/fadeOut;
  `refreshScrollBounds()` → `fireScrollTriggers()`; call site `this`.
- `rewrite/core/.../NpcFsm.kt`: S16 arm in `tickTrigger`; `doorExitBh`,
  `doorArriveBi`; header comment corrected.
- `rewrite/core/.../Slice1Test.kt`: `Slice99Test` — 9 tests.

## Tests

9 contract tests: overlap marker (105, az=300), leave → G(), exit tap →
bind+284+fadeIn, g/g.a gates, arrival → bottom-center+285+fadeOut,
mid-fade-out arm (i(19)+fling), mid-fade-in arm (i(17)), o==-1 no-op.

## Gates

- verifier `ok:true`; 57 unittests OK; `:core:test` green (incl. 9 new);
  `:android:assembleDebug` green.
