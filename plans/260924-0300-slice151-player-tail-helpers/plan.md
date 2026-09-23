---
title: Slice 151 — player tail mount/vehicle helpers (o()/g.cm/mounted arm)
phase: gameplay-port
status: merged
---

# Slice 151 — player tail helpers

Provenance pass on the `g.e()` tail's mount/vehicle helpers plus the
`g.cm` indicator-refresh latch, decoded from
`reconstructed-project/src/fallback/g.java`:

- **`g.o()` (g.java:6090, proven)** — `aZ → true`; `a == null ||
  a.ax == 43 → false`; else `a.ax ∈ {51,15,43}` (trailing `ax==43`
  unreachable dead code, kept verbatim). `groundOrVehicle()` fixed to
  the verbatim semantics: the old port returned **true** for ax43 —
  a real divergence (ax43 carrier wrongly enabled the weapon cycle).
- **`g.d()`/`g.b(i)` (g.java:346-357)** — carrier snap (`d() = b(a)`;
  `b(i)` snaps `ak/al` to an ax43 `S∈{1,4}` carrier's X-center).
  **Proven dead**: zero call sites in all three decompiler dumps —
  documented, not ported.
- **`g.cm` latch pair** — `g.cm` is a g-class static distinct from
  `k.cm` (the `k.k()` touchpad flag). `L37eb` sets `g.cm = 1` after
  any r98 mount-event tick; `L37f2` drains it on the next quiet tick
  (`r9 == 0`): `g.cm = 0; k.k() ? G() : U()` — `releaseAe()` mounted /
  `dropIndicator()` unmounted. Ported as `Entity.gcm` + the consumer
  arm in `mountEntry`'s r98 tail.
- **L37b6/L37c1 mounted arm (fallback g.java:7882-7898, proven)** —
  on the r98 tail, `mounted && !T()` → `U()` (a verbatim no-op when
  `T()==0`, since `U()` itself re-gates on `T()`) + `i.a(8, ak,
  al-85)` clip-9 ax14 marker spawn + `ae` pinned to `(ak, al-85)` —
  the mount-confirm indicator flash.
- **`o()?ao()` placement** — the L380d weapon-cycle gate already runs
  in `postTail` (`if (p.groundOrVehicle()) p.cycleEquip(...)`) at the
  correct position before the `aA` fixups; not duplicated into
  `mountEntry`.
- **case-34 dedup** — slice 150's transient `34 ->` arm removed; the
  pre-existing port kept and its `enterFall()` swapped for the
  verbatim `a(0)` (`flingAirborne(0)` also clears `ac`).

Also verified already-faithful ports (no change): `g.ao()` →
`cycleEquip` (simple-dump clean form — the fallback's extra
`ae`/`a(...)` lines were adjacent-arm bleed), `g.ap()` →
`contextDispatch`, `i.U()` → `dropIndicator`, `i.G()` → `releaseAe`,
`i.T()` → `indicatorIsHand`, `g.f()` → `isHolding`.

## Verification

- `Slice151Test` (4 tests): o() matrix, gcm mounted/unmounted drain,
  latch-held when `J&4 == 0`.
- `Slice150Test` — 7 tests, all green.
- verifier `ok:true`; `python3 -m unittest` 57 pass; `:core:test`,
  `:android:assembleDebug`, `:gdx:build` green.
