---
title: "Slice 189 — bound-interaction family (S37/38, S228/358, S235-243, S252)"
phase: port
status: done
---

# Slice 189 — bound-interaction family

Ports the last non-trivial arms of `g.e()` (the bound-interaction family) —
the S37/S38 grapple-and-bound stance pair, the S228/358 ride-bound stance,
the S235–243 push/pull/release/crush chain, and the S252 shared-fall arm.

## Mined sources (verbatim)

- `structured/g.java:2101-2159` — case 38 bound stance (grapple `i(23)` spring,
  `aO==5` snap + `enterStateMasked(54,8)`, down-drop `i(43)`, facing flip).
- `structured/g.java:2160-2174` — case 50/241 crush death (`d(999)` +
  `k.A(18)` + `k.l(12)` fail).
- `structured/g.java:3282-3349` — case 228/358 ride-bound (release input →
  `i(235)` + `a((i)null)` + `G()`, anim-end `i(0)`/`i(252)`).
- `structured/g.java:3350-3389` — case 235/238 push-entry (T==1 homing on the
  ax51 crate, unbound ±4864/-6656 dive, `r()` → `i(236|239)` / `a(0)`).
- `structured/g.java:3390-3427` — case 236/239 push-hold + 237/240 align.
- `structured/g.java:3428-3440` — case 242/243 release anims.
- `fallback/g.java:6461-6560` — case 37 raw gotos (authoritative for the
  grapple-climb branch — simple's `L1589→L1577` reconstruction loops forever;
  fallback resolves it to the `av` flip).
- `simple/g.java:3779` — `c(boolean)` facing-side head-cell probe → new
  `Entity.facingCellOpen`.

## Ported (`PlayerFsm.kt`, `Entity.kt`)

- `Entity.facingCellOpen` — `c(z2)` verbatim: facing-side head cell `< 12`.
- `37` — grapple-climb step: bound-grapple or grounded + dir-into-facing +
  `facingCellOpen` → `i(37)` ±1536; opposite dir → `av` flip; anim end →
  `i(38)`; non-bound airborne → `al=W[3]; i(43)`.
- `38` — bound stance: grapple release inputs → `G(); al-=20; i(23);
  ah=2560; cq=1`; non-grapple `aO!=5` → `al=W[3]; i(43)`; `aO==5` snap +
  `u(16388)` → `G(); enterStateMasked(54,8)`; `u(33024)` → head-cell probe,
  `aO==0` → `al=W[3]+10; i(43)`; facing not held → `i(37)`; opposite →
  `av = !av`.
- `43, 252` → shared `fallArm` (S252 rides the plain fall).
- `50, 241` — crush death: `T==1&&U==0` → `d(999)` + `ab=null` + `k.A(18)`;
  `ag>>=2`; ax51/ax43 ga attachments; `r()` → `k.l(12)`.
- `228, 358` — ride-bound: velocity zero; release input with facing/crate
  side mismatch → `i(235)` + `a((i)null)` + `G()`; else `v(4112/8256)` face;
  `r()&&S==228` → `i(0)` then `i(252)` (verbatim sequential-if quirk — i(0)
  lands before i(252) on the same tick).
- `235, 238` — T==1 homing: `r96=10` (ax51 within-60-on-either-axis → `>>=1`);
  `ag/ah = (target-pos)<<8 / r96`; unbound → ±4864/-6656; bound `r()` →
  `i(236|239)`; unbound → `av(); y(); r()→a(0)`.
- `236, 239` — push-hold: gate lost → `aj=512`; `al<=ac.al` → `a=null; a(0)`.
- `237, 240` — align: `r()` + ax51 W-edge snap ±20 → `i(0)`; else `a.S`
  branch → `i(228|358)`; `ag=ah=0`; non-crate `a` → `ak=a.ak`.
- `242, 243` — `aj=1536`; S242 `ah>=0` → `i(243)`; S243 `r()` → `a(0)`;
  `av(); y()` → `a(true); ai=ag=0`.

## Notes

- The shared L2083 jump tail (`postTail`) consumes the SAME input word as
  the bound arms — an UP edge in S38 arms the spring (`al-=20`, `ah=2560`)
  AND the tail's `ah=0` + `i(233)`. Kept verbatim; tests assert the arm
  effects (bind kept/dropped, `al`, meter), not the transient S.
- `aO==6` op18 drop-through stays ported-but-never-fires (zero type-6 cells
  in all 8 level et grids — data-verified).

## Gates

- `verify-static-reconstruction.py` ��� `ok:true`
- `python3 -m unittest discover -s tests` → 57 OK
- `:core:test` → green (11 new Slice189Test cases)
- `:android:assembleDebug`, `:gdx:build` → green
