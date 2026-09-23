---
title: "Slice 115 — jC==1 hard-mode toast + jC==19 mission select + l(9) arm internals"
phase: port-screens
status: merged
date: 2026-09-23
---

# Slice 115 — screens 1 & 19, l(9) internals, menu polish

Ports three remaining pieces of the `k.a()` screen machine plus
correctness fixes discovered while mining them.

## What landed

### jC==19 mission select (k.java:1178-1206, proven)

- `ey = da` every frame; footer `a(d(0,79), d(0,17))` = OK/BACK pills.
- Rows: `d(0, eA[bv][m(bv,i13)])` — verbatim-weird for `bv=0` (shows
  main-menu strings; rows ≥3 clamp onto the promo slot). The real
  per-mission label is the city sub-label `d(0, eX[eW[i13]])`
  (:6083-6100): `eW={2,2,1,1,2,0,3,2,2}`, `eX={51,52,53,54}` →
  ROME/ROME/FLORENCE/FLORENCE/ROME/VENICE/PANTHEON/ROME — matches the
  mined 8-mission order.
- `v(327712)` confirm: `bw==-1→0; aj=bw; a(bA,16,0); eg[aj] →
  fF=19; l(30); z(23)`. `eg[]` is all-true with no writer — the
  `!eg[aj]` lock (:1188) is dead code, ported verbatim.
- `v(131072)` → `l(2); z(30)`; `v(16388/33024)` → `L(da); aj=bw`.
- Note: `bw` lands at -1 again after confirm because `l(30)` runs
  `K(5)` which writes `bw=-1` (:1847) — original behavior, proven
  in test.

### jC==1 hard-mode unlock toast (k.java:800-811, proven)

- Body ported verbatim: `a(y,0,d(0,99),200,120,220,240,0,3)` +
  `v(262144)||j()` → `l(25);v();z(23)` + `y.l(1)` + `j.g%10<5` blink
  `d(0,9)` at (200,220,3).
- **Verbatim quirk discovered**: the toast *self-skips* in the
  original. `l()` resets `fd=-1` on every transition (k.java:1645),
  so case 1's own `a()` call sees `fd < i3-iK` on frame 1 and re-fires
  the `dx` end-tail (:5666-5678): `bA[69]==0 → {a(bA,69,1); e(true);
  l(1)}` once, then `bA[69]!=0 → l(25)`, and case 25's `dx && !Z()`
  → `l(2)` on non-IGP devices. The "CONGRATULATIONS!" text never
  draws (a() early-returns). Tests assert this exact chain.
- The only non-jc24 path that arms `l(1)`: the story screen's scroll
  panel (case 20 `kCu==5`) — `fd < 85-iK` → the `dx` tail. jc24 is
  exempt (`j.c==24` takes the `dw=30` arm, exits via `dw`).
- Renderer: blink hint arm added (`pack=92`, unreachable in practice,
  kept verbatim).

### l(9) arm internals (k.java:1685-1690 + L() :3273-3280, proven)

- `fO=0; ac(); ad(); e.b(); L()` — `ac()`/`ad()` are the soft-unpack
  blit-bank resets (fB/fz/fA + b.b/b.c frame-steal cache) with **no
  port equivalent** (clips decode once) — documented no-op.
- Ported: `fO=0; e.b()` (audio stop) + `L()` = `dg=0; ap[0..5]=0`.

### Supporting fixes

- `menuStates` gained `1` (case 1 is a pure UI screen — was falling
  through to the play-tick path).
- `bU` gained strings 51–54 (VENICE/FLORENCE/ROME/PANTHEON — proven
  in string corpus, required by the jc19 sub-labels).
- `menuL` arms `menuFkArm = 21` (`fK.a(21,1)` cursor-arrow anim) on
  both up/down moves — all callers get it.

### Also discovered in-tree (pre-existing, not this slice)

- The `subU`/`kMode`/`dlgU` unification flagged in prior notes was
  already merged as part of slice 114's u-dialog machine — `devin/land`
  carries `dlgU` alone. No re-unification needed (this slice's earlier
  in-progress unification collapsed into a no-op rebase).

## Gates

- verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
- `:core:test` green (838 tests incl. 12 new Slice115Test);
- `:android:assembleDebug` + `:gdx:build` green.

## Files

- `rewrite/core/.../Level0World.kt` — `menuJc1`, `menuJc19`, dispatch,
  `menuStates`, strings, l(9) arm, `menuL` fK arm.
- `rewrite/gdx/.../Level0Renderer.kt` — jc1 blink arm.
- `rewrite/core/src/test/.../Slice1Test.kt` — `Slice115Test` (12 tests).
