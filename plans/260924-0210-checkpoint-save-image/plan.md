---
title: "Slice 182 — checkpoint save-image fidelity (aY()/d(z2) bf[] system)"
phase: port
status: done
date: 2026-09-24
---

# Slice 182 — checkpoint save-image fidelity

Replaces the placeholder checkpoint model (teleport-home snapshot + re-home
on pickup) with the original's **bf[] slot-image** machinery: `i.aY()`
checkpoint write (simple i.java:13477-13550) + `k.d(z2)` restore
(k.java:5941-6080) + `k.a(iVar,i)`/`k.c(iVar)` stamp/tombstone helpers
(k.java:4576-4640).

## Proven semantics

- `aY()` gate: `bh[aj]==3` → `(k.ak!=0 || al<aS.al) skip`; else
  `!a(W,aS.W) → skip` (rect overlap).
- On pickup: `k.y()` → `fS=0` tip marquee, `k.G=Z[0]` director uid,
  `writeIX(aw)` stamps `bA[16]=aw` (checkpoint pointer — **not** bA[15],
  which is the mission-complete has-save flag set only at k.java:3430/4494),
  `k.c(this)` tombstones the record's slot.
- **Stamp loop**: every live `bb[i]` (`as!=-98`, `ax!=70`) gets
  `k.a(bb[i],as)` — writes `{S,T,ak,al,aA,P,bz,bs,av}` into
  `bf[as*22+{0,2,4,6,8,12,16,17,21}]`. A snapshot WRITE — entities never move.
  Then `bg[i]==-99 → bf[i*22]=-99` tombstone propagation.
- `d(z2)` slot map over records in file order: type 0/25 → player (`new g`),
  55 → waypoint (`c.a`), else `new i`. Counter `r102++` only for
  ax∉{0,25,70}; **ax70 takes `as=r102-1`** (aliases previous slot, no
  increment — verbatim quirk).
- `d(false)`: fresh spawn → `a(e,slot)` stamps. `d(true)`: `bf[slot*22]==-99`
  → skip entity creation entirely; ax60&&Z[4]==2 → skip-restore→stamp
  instead; else restore fields + fixups: ax27 `S==6→S=4`; ax11
  `(P&32)==0 && Z!=null && S!=2 && (Z[5]>0||Z[6]>0) → S=3,T=0`; ax21→`aA=0`;
  ax35/69/11/73 → `t()`.
- Mission-switch fresh path `L54` (`a(bA,16,0)` after `F(aj)`,
  k.java:6735): clears the checkpoint pointer — ported as
  `kBA[16]=0` in `loadMission` (previously only `checkpointSnap`/`kG` were
  cleared — fidelity bug found by the slice's own test).
- `k.G` restore arm (k.java:5177-5181): `G>0 && q(G).ax==5 → P|=16; N()`
  — rebinds the linked mission director's script context.

## Port changes

- `Entity.asSlot` (`i.as`) + `Level0World.slotImage/slotFlags`
  (`k.bf/bg`), `kG` (`k.G`), `hintPending` (`i.br`, persisted through
  `bA[76..]` — the `c(i)` hint arm itself stays parked: host arms unported).
- `fireCheckpoints()` rewritten verbatim: bh3/zone gates, `kFS=0` marquee
  arm, `kG` link, `writeIX`, self tombstone, live-npc stamp loop, bg→bf
  propagation. The old teleport-home loop + `checkpointDead` set deleted
  (semantically inverted vs `k.a(bb[i],as)`).
- `spawnEntities(restoreFromImage)`: slot map, tombstone-skip, restore arm
  with the proven fixups; `stampImage`/`applyImage` helpers.
- `rebuildRecordStructs()`: checkpoint+scrollTrigger recompute on
  `spawnEntities` (was construction-time — stale after pack swap).
- `removeEntity` writes the `bg[as]=-99` tombstone (`k.c`).
- `Snapshot` +`kG`,`br` (restored in `resetPlayerToSpawn`).

## Bug caught by the slice

- `seesPlayer` (`b(k.aS)` in NpcFsm) used point tests on `player.ak/al`;
  the original is rect-overlap `a(this.W, aS.W)` — a player hugging the
  zone edge never alerted. Ported to rect overlap. The pre-slice test was
  passing vacuously (`aA=2` patrol phase, not an alert).
- `loadMission` now clears `kBA[16]` per the L54 fresh arm.

## Gates

verifier `ok:true` · `python3 -m unittest` 57/57 · `:core:test` green
(0 failures) · `:android:assembleDebug` ✓ · `:gdx:build` ✓
