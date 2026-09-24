---
title: "Slice 175 — ax11 verbatim record init + shared death arm + I() head"
phase: port
status: done
---

# Slice 175 — ax11 `initSoldier` verbatim + S0/106/107/135 shared arm + `I()` head death check

## What

- **`initSoldier(e, f, w)` rewritten verbatim** (i.java:2230-2271 record
  init + :2860-2900 ctor tail): `Z[14]=f[4]`, `Z[0]=f[10]`, `Z[1]=0`,
  `Z[2]=-1`, home `Z[3]/Z[4]`, bounds `Z[5..8]`, zone `Z[9..12]` derived
  from `Z[15..18]=f[12..15]`, `Z[19]=f[18]`, `Z[21]=f[19]`, `Z[20]=0`,
  `Z[13]=f[16]` script-claim (`bindScript`+`scriptKeyStep` → `scriptBound`
  = `i.d`, consumed by the S117 release arm at i.java:5147), hardened
  `aB = BU[0] << 1` when `Z[0]==1 || ax==73` (:2256), `P|=16` when
  `f[5]==33` (:2269), `setAnim(f[5])` — the record's own anim, replacing
  the stale `setAnim(0)` placeholder that forced all 53 level-0 soldiers
  into the S0 death arm — `refreshBoxes()` (`t()`), `collideSides(true)`
  (`a(true)`), `settleToGround` (`E()`).
- **S0/106/107/135 arm replaced with the verbatim shared arm**
  (i.java:4044-4096): `a(true)`, `G()` (`releaseAe`), `P|=512`, `ab=null`,
  `ag=ah=0`, crate-ride `s.ag` carry, `aA=2`; S106 T∈{1,5,7} / S107 T∈{2,5}
  blood `spawnFx8(world,50,1,…)` + kBK `spawnFx8(world,59,1,…)`; on
  `r()`: `aB>0 → i(0)`, `lockTarget`/`player.gb` release,
  `S∉{106,107,135} → i(139)+statTally(aw)` else `P&-17|P|32|P|64` freeze
  + kBK `spawnFx8(59,2,±18)`.
- **`I()` head death check added** (i.java:4024): `aB<=0 && !aH() &&
  S!=184 → i(0)` before the S-switch — the kill-chain gap that let aB=0
  soldiers cycle S4/23/12/9 forever instead of dying. `aH()` corpse-family
  set {0,20,21,106,107,117,139,168,169,176} (i.java:7287).
- **`tick()` gated to the `I()` family** {11,17,23,47,50,73}: the
  dispatch `else` branch funneled unclaimed types (ax80 props…) through
  the soldier FSM — with `aB` defaulting 0 the head-check killed them.
  The original only runs `I()` for family members (default arm L897 =
  no-op for ax80 etc.).
- **Stale `106, 107` case removed** — superseded by the verbatim shared
  arm (Kotlin `when` first-match shadowed it).

## Test updates (stale fixtures contradicted proven semantics)

- Spawn-position test: ax11/73 settle window (`a(true)` pushes ak ≤10px,
  `E()` sinks al in +10 steps) — the original runs both at spawn.
- S216 launch test: `i.g()` (i.java:1237) zeroes `ag`/`ai` unconditionally
  — the ±5120 launch written before it is dead code in the original;
  assert the `c(85,157)` victim path instead.
- `Slice136Test.soldierAt`: sets `e.aB = 50` — synthetic soldiers need HP
  (`initSoldier` always sets `aB = BU[0] > 0`); aB=0 entities are dead
  under the verbatim head-check.
- New `Slice175Test` ×5: record-S spawn (uid44→S3), hardened ×2 HP
  (uid60→600), S0→139 + lock release, S106 freeze, Z[13] bind →
  scriptBound.

## Gates

- verifier `ok:true`; unittests 57 pass; `:core:test` green (incl. the
  previously-failing kill-chain test — the head-check closes it);
  `:android:assembleDebug` + `:gdx:build` clean.
