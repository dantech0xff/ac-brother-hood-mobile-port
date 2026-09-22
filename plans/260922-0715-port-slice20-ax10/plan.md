---
title: "Port slice 20 — ax10 aV() trigger zones (S33/34/36/43/53) + k.c removal"
phase: port-rewrite
status: done
---

# Slice 20 — ax10 TriggerController zone arms

## Mined semantics (proven)

`i.aV()` (i.java:11800-13181, 7880-byte method — full dispatch table and
semantics already reconstructed in `docs/i-av-reconstruction.md`).

- **Init arm L96** (i.java:2882): `aB=0; P|=512; az=0`, then a switch on
  `r8[5]` (the record's S field): `34 → L102` (`Z={r8[11],r8[13]}; P|=16`),
  `43 → L104` (`Z={0}`), `16 → L110` (`Z={r8[20]}`, falls to L111),
  everything else → **L111** (i.java:2989): `aE=r8[4]; aF=r8[11];
  o=r8[12]; p=r8[13]; aG=r8[14]; ay=r8[15]`.
- **Generic tail** (i.java:3728-3705): `i(r8[5])` at L395, then ax10
  takes L419: `W = [ak+r8[7], al+r8[8], +r8[9], +r8[10]]` — record-built
  hitbox, `t()` returns it directly (i.java:370-375, proven).
- **S33 (L706, i.java:12887)**: player∩W && player.S∉{148,149,150} →
  `g.B=av`, `g.l=(aG!=0 ? (aG-player.ak)<<8)/11 : 0`, `g.A=true`;
  no overlap → `g.A=false`.
- **S34**: direct return (no-op).
- **S36 (L722, i.java:12905)**: overlap → publish context zone
  `g.n=Wcx, g.o=W[3], aE>=0→g.k=aE, g.d=this, P|=16`; leaving clears
  only while this trigger owns (`g.d==this → null/n=0/o=0/k=-1/P&=~16`).
- **S43 (L744, i.java:12942)**: overlap && player.S∈{60,61} → `i(203)`.
- **S53 (L886, i.java:13154)**: `g.D && overlap && player.S∈{0,1,5}` →
  `i(360); ag=0; ah=0; k.c(this)` (self-remove).
- `k.c(e)` removal: deferred-set drained after the npc tick pass.

## Level-0 data

15 ax10 records → S ∈ {36×1, 34×4, 33×2, 43×2, 16×4, 53×2}. S33 record
at (9808,259) carries `aG=9869` (slope anchor). `bi[10]=6` (clip slot 6)
but clip6 isn't converted — triggers spawn clipless (invisible, like the
original zones).

## Ported

- `NpcFsm.initTrigger` (L96 + generic-tail W-build) and `tickTrigger`
  with the five zone arms; `LevelCellSource.removeEntity` + deferred
  `pendingRemove` drain in `Level0World.tick`.
- Player-singleton fields published by the zones (original `g` statics):
  `gB/gL/gA` (S33), `gn/go/gk/gd` (S36), `gD` (S53 gate — producer arm
  g.java:2054 unported, stays false).
- `i.o`→`oId` and `i.p`→`pv` renames: `o`/`p` collide with fixed-point
  `O`/`P` at the JVM accessor level.
- 5 contract tests (spawn bank/W, S36 publish+clear, S33 publish+clear,
  S43→203, S53 gating+removal).

## Verified

- `:core:test` green (51 tests), verifier `ok:true`, `unittest` 57/57.

## Honest gaps

- S16 (rope-attach, L625) needs `g.a`/`k.an`/`k.ao`/`k.bI`/helper-105 —
  stubbed; 4 level-0 records inert.
- `g.n/g.o/g.k/g.d`, `g.B/g.l/g.A` consumers (contextual-move arm +
  HUD prompt) unported — fields publish correctly, nothing reads them.
- `g.D` producer (g.java:2054) unported → S53 stays inert end-to-end.
