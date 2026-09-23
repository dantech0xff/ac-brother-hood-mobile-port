---
title: "port slice 127 — i.e() cell-read overrides + i.bq crate-top level"
phase: rewrite/port
status: done
confidence: proven
---

# Slice 127 — `i.e(int,int)` overrides + `i.bq` lifecycle

## i.e(int,int) (i.java:14828)

The entity cell read now ports both ax0 (player) override arms that were
flagged unported:

- OOB: `cx<0 || cx>=k.bp || cy>=k.bq → 20` (note: `cy<0` is NOT OOB in
  the original — passes to `k.g`; our level read already returns 20).
- `k.aS.m()` armed (player standing on an ax51 crate, or `i.bq` set)
  and `cy` outside the feet band `{i3-1, i3, i3+1}` (`i3 = (W3+1)/20`)
  → reads 0 — the crate-top mask.
- `S ∈ {37,257}` (vault/climb) and the cell is solid-20 → reads 0 — the
  pass-through that lets vaults and climbs cross solid geometry.

`k.bp`/`k.bq` added to `LevelCellSource` (grid dims, k.java:99-100).

## i.bq (i.java:163) — crate-top level static

`Entity.entBq` companion object field with the full lifecycle:

- **set** (g.java:805): `i(233)` landing while `standingOn.ax == 51` →
  `i.bq = al + 20` — wired in `postTail`'s `aZ||a!=null` arm.
- **clear** (g.java:594): `(bq != 0 && al > bq) || c(S)` → 0 — per-tick
  in `PlayerFsm.tick` beside the `i.bh` decay. `c(S)` = the grounded
  set {0,1,7,11,12,26,79} (g.java:316) → `GROUNDED_C`.
- **read**: `g.m()` (g.java:5352) = `(a!=null && a.ax==51) || bq != 0`
  — drives the `e()` crate-band override. The g.java:1422 dismount and
  :4991 `aO<20||ah>=0||bq!=0` gate sites remain in unported `ay()`/air
  regions — noted, not faked.

## Tests (+5)

S37/257 solid→empty, OOB bounds + cy<0 pass-through, crate-top band
blanking via both `standingOn` and `entBq`, set-on-landing →
clear-on-grounded lifecycle.

Gates: verifier ok:true; 57 unittests; :core:test green; :gdx:build +
:android:assembleDebug green.
