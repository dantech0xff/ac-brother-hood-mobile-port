---
title: "Slice 237 — ax2 checkpoint entity fidelity (init Ld7f + entity-driven aY + k.c self-removal)"
phase: port
status: done
---

# Slice 237 — ax2 checkpoint entity fidelity

Completes ax2 checkpoint coverage: the last unclaimed tick arm in the
`i.I()` ax dispatch, plus its bespoke init arm — and retires the
record-iteration `fireCheckpoints()` approximation for the real
entity-driven `aY()` path.

## Verbatim findings

- **`i.I()` dispatch (i.java:15165, table :15493-15555, proven):**
  ax2 → `L1e2e` = `aY()` call → shared `L1f35` tail. With this arm
  wired, every shipped record type's tick arm is now claimed
  (ax16 already ticked; ax28/31/45/75 are proven bare `goto L1f35`
  — shared-tail-only, nothing to port).
- **ax2 init `Ld7f` (i.java:9177, proven):** `az=300; P|=0x80;
  Z=new int[1]; Z[0]=r8[7]` → `L1bea` record-anim finish.
- **`aY()` gate (i.java:37974-38020):** `bh[k.aj]==3` → flying arm
  (`k.ak!=0` gate + `al>=aS.al`); else `a(W, aS.W)` box overlap —
  the entity needs a real `W` box, which clip1 provides
  (`bi[2]=1`; clip1 = invisible palette clip whose rect row is
  `[0,-100,40,128]` → W = {ak, al-100, ak+40, al+28}).
- **`k.c(this)` (i.java:38143 → k.java:16681, proven):** inside aY()
  the fired checkpoint tombstones its `bg[as]` slot AND nulls its own
  `bb[]` slot — the entity leaves the tick list immediately. Prior
  port only tombstoned the slot and left the entity alive
  (consumed-guarded); now `removeEntity(e)` runs the full arm.

## Port changes

- `NpcFsm.initAx2` — Ld7f verbatim (az=300, `P|128`, `Z[0]=r8[7]`,
  then `L1bea`'s `i(r8[5])` + `t()` finish).
- `ENTITY_CLIP` — `2 to 1` (bi[2]=1). Without it the entity was
  clipless → `t()` early-return → W=0 → gate could never fire.
- Init dispatch — `type == 2 → initAx2`.
- Tick dispatch — `n.ax == 2 → fireCheckpoint(n)` (entity-driven).
  Retired `fireCheckpoints()` record-pass call at tick end.
- `fireCheckpoint(e)` — same fire body as before plus the missing
  `k.c(this)` arm via `removeEntity(e)` (tombstone + bb removal).

## Test findings — the `au` park gate

The 9 pre-existing checkpoint tests all failed after the switch to
entity-driven aY() — for a *correct* reason: they teleport the player
onto the checkpoint, but the camera never follows, so the entity's
`au` stays ≥2 and the `k.I()` eligibility gate (k.java L215→L2d9)
skips its tick — exactly what the original does to off-screen
checkpoints. Tests now arm the entity's `P|16` force-tick bit (the
original's own park-bypass, already the repo's `keepLive` convention)
via a new `overlapCheckpoint(w, cp)` helper. The injected-fake-
`Checkpoint` test was rewritten: records can't fire without an entity,
so it now writes `Z[0]=36` into a real ax2 entity instead (the
`k.G=Z[0]` read is entity-side). One test also needed `settleIntro`
after `removeEntity` — removal re-binds the intro claim and the
L108 suspension gate freezes non-`P|512` entities.

## New tests (Slice237Test)

- `ax2 init takes the Ld7f arm verbatim` — az=300, `P|0x80`,
  `Z[0]=-1` (level-0 dead link), S=0, W = clip1's 40×128 box.
- `aY fires through the entity tick and self-removes` — overlap →
  consumed + `kFS=0` + snapshot written + entity gone from `npcs`
  after the pendingRemove drain.

## Gates

- verifier: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `:core:test`: all green (was 9 failures → 0)
- `:android:assembleDebug`, `:gdx:build`: clean
