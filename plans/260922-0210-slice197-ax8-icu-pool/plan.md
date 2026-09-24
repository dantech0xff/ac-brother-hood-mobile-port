---
title: "Slice 197 — ax8 ar() + i.aw() waypoint materialization + k.aX pool + i.cu freeze + i.b/O slow-mo"
phase: port
status: done
---

# Slice 197

Mop-up of the last flagged stubs, all mined verbatim from fallback `i.java`.

## What shipped

- **ax8 `ar()`** (i.java:21786, proven) — the ax8 projectile/burst FSM:
  `g.f==this && r() → P|=64`; `aa==k.r(59)` (clip59 variant):
  `S∈{2,6,8}` finish → `P|=32|64` (flash, kept alive); other S → `r() &&
  aw==-1 → k.c(this)`; non-59 variant: `S==4 && W∩aS.W && aS.S!=317 →
  aS.a(4,0,0,this)` (player damage — the counter arm can stagger the ax8
  back, faithfully), then `r() && aw==-1 → k.c(this)`; `aw != -1` →
  script-bound, nothing.
- **`i.aw()`** (i.java:23083, proven) — waypoint materialization: for
  `Z[1..4]` uids, `c.a(uid)` lookup; hit → `Z[i+1] = c.j` mint,
  `c.a(node,this)` entity-x-shifted derived copy, `C++`. Wired at the
  `r8.Z[1] < 10000` respawn gate (i.java:51396) — earlier note's `e.aG`
  gate was wrong.
- **`k.aX` pool correction** — `av()` (i.java:22155): first slot with
  `P&128`; arming clears bit 128. Pool = real ax24 entities seeded by the
  ax24-S0 record constructor arm (`k.aW=50`, `a(24,40,0,200)` +
  `P|=640` + deferred `k.b` insert) — slots tick through the normal
  entity pass; there is **no** separate pool tick. The port's bogus
  `shotPool`/`allocShot`/`tickShotPool` stub was removed; iface now
  exposes `pooledShots`/`allocPooledShot()` over `projectilePool` and
  ax64 tether/barrage use it.
- **ax8 record init** — `initAx8` = the L94b shared arm
  (i.java:8220: `aE=f4, aF=f11, o=f12, p=f13, aG=f14, ay=f15` + `i(f5)` +
  `t()`); wired in the init dispatch.
- **`i.cu` consumers** (i.java:15294, proven) — `I()` L109 early-out:
  while set, every non-ax10 entity skips its tick (set by ax10-S55 claim
  zone at NpcFsm:1811/1820). The second read (L64: skips the `aH`/`P|128`
  sub-frame sampling) has no port analog — the port has no sub-frame
  anim clock (`inferred` no-op).
- **`i.b(int)`/`i.O()`** (i.java:21728/21749, proven) — slow-mo driver:
  `aH/aI=k.aw=0` always; `k.bh[k.aj]==3` (bh={4,3,4,4,3,...} → missions
  1/4) saves `k.X→aJ` (or `k.W`) + `k.X/=r3`; `O()` restores. The
  `timewarp`/`timewarpOff` stubs now carry the full body (same fn as
  `eventArm`/`eventDisarm` — i.java has one `b(int)`).
- **`g.h` consume comment** — `i.H()` was already ported as `consumeH()`
  (`ab.p(); ab=null` i.java:15150); the "unmined" comment was stale.
- **`k.au` difficulty** — verified ALREADY wired end-to-end
  (load `kAu=kBA[8]%3`, save, menu cycle, `bu[]`/`bv[]` HP + `dh[]` score
  tables). Removed from slice scope.

## Gates

verifier `ok:true`; `python3 -m unittest` 57 OK; `:core:test` 1328/1328;
`:android:assembleDebug` OK.

## Tests (Slice197Test, 9)

gf flash arm, clip59 S2 finish → flash bits, clip59 other-S removal,
non-59 S4 strike→sfx18→removal, `aw!=-1` no-op, `aw()` mint/chain/C++
semantics, pool alloc skip-armed/exhaust-1, `icu` freeze in real
Level0World, `timewarp` bh3 vs non-bh3.
