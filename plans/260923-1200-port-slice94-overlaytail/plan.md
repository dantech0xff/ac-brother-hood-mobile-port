---
title: "Slice 94 — k.b(z2) overlay tail: HP bars, charge bar, rope lines"
phase: port
status: complete
---

# Slice 94 — entity overlay tail (k.java:2931-2990)

## Scope

Draws the per-entity overlay block the draw pass appends after `ab`:
mash/charge bar, NPC HP bars, ax10 rope-volume lines, claim-script hint
ropes, and the player's own grapple rope. Plus the auxiliaries they
need: `i.h` bar predicate, `i.ab` claim predicate, `i.bv` table,
`k.h` hypot, clip61 (rope dots), `g.i`/`g.a` dotted rope.

## Proven

- Outer gate (k.java:2931-2933): skipped entirely while a bound
  claim-script sits in its `cd[2]`-quiet state, unless the entity is
  `P&512` or the player; then `(P&32)==0 && (P&128)==0`.
- `bl>0` charge bar (:2935-2941): non-73/11 entities only — white
  42×5 outline @ (ak, al-70), red fill `bl*40/10`. `bl` is the
  `f()`/`g()` hold meter (i.java:5604-5638) — already wired.
- HP bar (:2942-2955): ax11 alive-anim + `h()`, ax73 `h()`, ax17
  `h()` && S!=69. 41×5 outline @ (ak-20, al-80); green when
  `aB > i33>>1`, else red/white blink on `j.g` parity; fill
  `aB*40/i33` halved to `*20` for `Z[0]∈{1,2}` or ax73.
  `i33 = i.bu[k.au]` soldiers / `i.bv[k.au]` civilians — bu/bv are the
  per-difficulty max-HP tables (bv confirmed by spawn init
  i.java:2340/2399); `i.bu` is the same array as `WEAPON_DMG`
  (dual use).
- `i.h(iVar)` (simple/i.java:20791 — structured folds the switch):
  strict `a(W, ac)` overlap, then ax11/73 → `!P() && aA>=1`,
  ax17/50 → true. `i.ab()` (i.java:18914): `ca>=0 && !cd[0] && cK>=0`.
- ax10 S==32 rope lines (:2956-2972): mid-W horizontal, colour
  -3584205; two lines toward `aS` when `aS.ac==self` (claimed rope),
  else the W span; each doubled at +1px.
- ax0 tail (:2973-2985): claimer `ab() && cd[9]` → `aS.a(cg,ch)` link
  rope (op110 carrier-link) or `g.a(cf[0..4])` dotted line; else
  `S∈{272..277,293,298}` → `aS.i()` own rope — `as()`/`au()` already
  maintain `cJ/cK` (player end) and `cH/cI` (anchor end) + `cy`.
- `g.a(i2..i6)` (g.java:4723-4733): `k.h(len)/6` clip61 dots stepped
  6px along `+angle` from (i4,i5); `k.z[61].a(bg,0,0,x,y,0,0,0)` —
  `i.bg` is the Graphics so anim0/frame0, no palette.
- `k.h(i,i2)` (k.java:5301): `(|a|+|b|) − min/2 − min/4 + min/8`
  integer hypot → `Trig.khypot`.
- clip61 = pack-3 entry-061 (2 modules) — converted via
  `convert_slice1.py` and loaded in `Level0Game`.

## Deferred / dead arms

- `k.db` debug flag (k.java:188, toggled :771) gates the W/X/Y-box and
  waypoint draw block (:2986+) — dev cheat overlay, not ported (flag
  stays false; noted here).
- `ag()→ah()` ghost-trail arm remains dead (`i.cU` producer unported).

## Files

- `gdx/Level0Renderer.kt` — `outlineAr`, `drawLine`, `ropeDots`,
  `drawOverlayTail` + call after the `ab` overlay.
- `core/Level0World.kt` — `showsHpBar`.
- `core/Entity.kt` — `claimAb`, `NPC_HP_BV`, WEAPON_DMG dual-use note.
- `core/Trig.kt` — `khypot`.
- `tools/convert_slice1.py` + `generated/clips/clip61/*` + `Level0Game`
  load.
- `core/Slice1Test.kt` — `Slice94Test` (8).

## Gates

verifier `ok:true`; unittests 57; `:core:test` green; `:android:assembleDebug` green.
