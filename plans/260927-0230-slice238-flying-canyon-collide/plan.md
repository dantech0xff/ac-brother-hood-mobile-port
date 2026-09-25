---
title: "Slice 238 — i.B() flying canyon-wall collide"
phase: port
status: done
---

# Slice 238 — `i.B()` flying canyon-wall collide

Port `i.B()` (i.java:3844-4204, **proven**) — the bh3 flying-mode collide
called once per flying tick at g.java:13907 (`r0.B()` inside `g.n()` head,
return value dead — side effects only). This was the last unported body in
the flying player loop; `flightTick` now matches the source's call order
(`k.aI++` → `i.B()` → `k.B()` music arm → `i.be` check).

## Shape (verbatim)

- `i.w` first-call latch (:3849) → `i.be` dead-drag arm (`ag=ah=0; al-=k.X`)
  → gate `k.ai || i.e>0` →
  - **L36 director pass**: `bb=bc=0`; clamp `ak` into `[k.O, k.O+400]`;
    4-corner probes `aT..aW = e(edge/20, (W[1|3]%260+260)/20)`; left-edge
    both ≥10 → `c()≠0&&!k.ai→b()`, `c()==0→c()` — `c()≠0&&k.ai` skips
    extrude entirely (kept verbatim); right-edge arm mirrors; `t()`; → 1.
  - **L149 quiet pass** (`!k.ai && i.e<=0`): same probes;
    `aT==21||aU==21` → kill arm (`ag=ah=0; al-=k.X; i.be=1; i(34)`);
    `!v() && al > k.P+240` → `k.l(12)`; same extrude arms; `t()`; → 0.
- `b(r7,r8,r9)` (:4206-4265, `slideLeftB`): `bb=1; aT=10; r10=3..0` loop —
  `ak -= (r8%20)+1; t(); aT=e(W[0]/20,r9/20)`; ends `ag=0`.
- `c(r7,r8,r9)` (:4267-4328, `slideRightC`): `bc=1; aU=10` same loop —
  `ak += 20-((r7+20)%20); t(); aU=e(W[2]/20,r9/20)`; ends `ag=0`.
- `c()` no-arg (:4330-4408, `freeSideC`): probe rings r10=1..4 —
  `aT<10 → true`, `aU<10 → false`, else false.
- `i.v()` (:1982-2164) alive whitelist — the player arm (L144, `a(k.ac,Y)`
  overlap) already ported as `flightAliveV` (PlayerFsm:2575); `yOverlapsCam`
  here is that same rect check.

## New world fields

- `iW` (`i.w` static) + `iE` (`i.e` static) on `LevelCellSource`, overridden
  on `Level0World`; both reset in the i.D() block (i.java:7166/7252).
- `i.e=30` producer wired at the ax24-S20 heal-shrine arm (NpcFsm — was
  `// i.e static unmapped` at i.java:39329).
- `iE` decays once per tick beside the `kAO` decay — the verbatim site is
  inside the still-unported `i.F()` per-entity proc (i.java:13192);
  per-tick cadence is **inferred**.

## Verbatim quirk kept

The `%260+260` wrap bounds every probe to rows 13-25 of the grid. Level-0's
two cell-21s are (133,23) and (86,26) — the second can never be probed:
proven-dead content. The kill cell that *can* fire is the x2660 rock.

## Tests (Slice238Test, 8)

Latch, camera-band clamp both ends, cell-21 kill arm (real level cell),
below-screen `k.l(12)`, `iE` per-tick decay + i.D() reset, ax24-S20
`iE=30` producer, dead-drag arm.

## Gates

verifier `ok:true`; `python3 -m unittest` 57 pass; `:core:test` green
(8 new); `:android:assembleDebug` + `:gdx:build` green.
