---
title: "Slice 140 — ax10 S0 camera-focus zone + S2 flag-apply trigger"
phase: port-slices
status: done
date: 2026-09-23
---

# Slice 140 — ax10 S0 camera-focus zone + S2 flag-apply trigger

## Goal

Port the two ax10 trigger-zone states that level records actually spawn
(`S∈{0,1,2}`; histogram: 104×S0, 4×S1, 16×S2): S0's camera-focus zone and
S2's one-shot flag-apply trigger, plus the per-S record-init arms they need
(`L59` S2/S3 Z-quad, `L95` S10 five-slot Z).

## Source (proven)

`i.aV()` dispatch (i.java:9162): `case 0 → L5ac`, `case 1 → L1ec7`,
`case 2 → L13a1`, `case 11 → L1ec7`, `case 52 → L1ec7`,
`default → L1ec7`. `L1ec7` (i.java:13223) is a bare `return` — the shared
no-op tail, so S1/S11/S52/default need no port.

### S0 — L5ac→L5e0 (i.java:9772-9800)

```
if (a(k.aS.W, this.W) && this.v()) {
    if (p != 0) k.af = p;
    if (aG == 0) return;        // → L1ec7 (sticky: af/ag keep prior values)
    k.ag = aG;  return;
}
k.af = 0; k.ag = 0;             // L5e0 — clear on leave/invisible
```

`k.af`/`k.ag` are the camera focus offsets consumed by `k.m()`
(k.java:1959-1965): `cA = ae.ak - 200 + af`, `cB = ae.al - 120 + ag`.
The port's tracker (slice 70) already reads them as `camAf`/`camAg`;
this slice wires the producer. Record params come via `L111`
(default arm): `p = r8[13]` → `pv`, `aG = r8[14]` → `aG`.

Verbatim quirks kept:
- `p==0`/`aG==0` leave the globals at whatever they were (sticky).
- A non-overlapping or invisible S0 zone clears BOTH globals every tick —
  with several S0 zones the last-ticked loser wipes the winner's write.

### S2 — L13a1→L14a2 (i.java:11834-11976)

```
if (Z[0]==1 || Z[0]==2)  →  require a(aS.W, W) else return
if (Z[0]==2)             →  Z[2]!=0 required; gate=k.q(Z[2]);
                            gate!=null && gate.v() → return
t = k.q(Z[1]); if (t != null) {
    t.P |= Z[3];  if (t.P&1) t.av = true;
    if (t == aS && g.a != null)   → Z[3]&512 ? g.a.P|=512 : g.a.P&=~512;
    for each bb[] entity b:  b.ax==11 && b.s!=null && b.s.P&512
        → b.P |= 512              // squad-arming sweep
}
k.c(this)                        // one-shot — removes itself either way
```

### Init — i.java:2114 `case 10` sub-switch on `sArr[5]`

- S∈{2,3} → `Z = {r8[4], r8[12], r8[13], r8[14]}` (L59) — **no L111 tail**.
- S=10 → `Z = {r8[11..15]}` (L95) — likewise no L111 (latent fix: S10
  records were wrongly running `l111`).
- S=0/1 and every other unlisted S → `L111` (`aE/aF/o/p/aG/ay`).

## Port

- `NpcFsm.initTrigger`: added `2, 3 ->` (L59) and `10 ->` (L95) arms.
- `NpcFsm.tickTrigger`: added `0 ->` (L5ac-L5e0) and `2 ->`
  (L13a1-L14a2); S1/S11/S52 intentionally no-arm (L1ec7).
- `LevelCellSource`: `var camAf`/`var camAg` (k.af/k.ag).
- `Level0World`: `private var camAf/camAg` → `override var` so zones can
  write what `k.m()` reads.

### Symbol map

`k.q(uid)` → `w.findByAw`; `i.v()` → `wasHitRecently(w)`; `g.a` →
`player.ga`; `k.bb[]` sweep → `w.npcs`; `i.p` → `pv`; `i.aG` → `aG`;
`k.c` → `w.removeEntity`; `a(int[],int[])` → `rectsOverlap`.

## Tests — Slice140Test (12)

- S0: publish offsets on overlap+visible; clear both on leave; sticky
  when p==0/aG==0; invisible zone still clears despite overlap.
- S2: Z0==1 overlap gate (holds + fires); Z0==2 onscreen guard blocks /
  absent guard fires; target=player propagates 512 to `ga`; L1451 squad
  sweep arms only linked ax11s; one-shot removal.
- initTrigger: S2 record → `{f4,f12,f13,f14}` without L111 (`aE==0`);
  S10 record → `{f11..f15}` without L111 (`pv==0`).

## Gates

verifier `ok:true`; `python3 -m unittest discover -s tests -t .` 57 pass;
`:core:test` pass (incl. 12 new); `:android:assembleDebug` pass;
`:gdx:build` pass.

## Deferred

- Remaining unported `aV()` arms (~35 of S3-S54) — no level records spawn
  them; they are entered only via `i()` transitions from other states.
- Init arms for S11 (`Z={r8[0]}`), S24 (`aA=r8[11]`), S28, S30, S31, S39
  — added when those S arms are ported.
- `k.af`/`k.ag` are never reset by the tracker itself (verbatim) — only
  S0 zones clear them; a zone removed while overlapping leaves the last
  offsets until another S0 ticks (same as original).
