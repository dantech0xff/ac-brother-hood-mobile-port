---
title: "Slice 178 — flying dL stamp grid + g() remap + I() bh3 copy-group arm"
phase: port
status: done
---

# Slice 178 — flying-mode `dL` stamp grid, `g()` bh3 remap, `I()` copy-group arm

## Mined source (proven, k.java unless noted)

- `U()` (:4359-4365): `bh==3 → dL = new int[21][13]` + `dM = true`.
- `b(z2)` stamp-window driver (:2695-2752): `et` backdrop trails the
  camera at parallax `(O*(bt-21))/(bp-21)` / `(P*(bu-13))/(bq-13)`;
  `bp<=21||bq<=13` throws in the original (catch-abort → port returns);
  exposed edges re-stamp via `h()`, `dM` forces a full pass. `i3<0` does
  `i3-=20` before `/20` (floor fix) then `i3+=20` after.
- `h(i,i2,i3,i4)` (:4372-4407): stamps [col..col]×[row..row] into
  `dL[x%21][y%13] = x + row*bt`; while `aR<0` rows are identity, armed
  rows remap through `(i6%20)+((bu/20-1-dS|aR)*20)` and rows crossing
  `dT` advance `aR`/`dU`.
- `g(x,y)` bh3 arm (:5284-5298): `x<0||x>=bt||y>=bq → 20`; `y<0 → 0`;
  `i4=dL[x%21][y%13]`; `i4<0||i4>=len → 0`; `et[i4]==255 → 0`.
- `I()` bh3 arm (:2516-2572): every entity `u()`; eligibility
  `(P&256)==0 && ((au<2 && !(P&32)) || (P&16))`; copy-group consume
  `ak==0 && au<1 && ay>0 → ak=ay`, all `ay==ak` get `dR=aG`+`v()`
  (dead-read — only `u()`'s side-effect survives); `ay==ak→-1`; `-1`
  ticks + `ac.I()` (ax!=10) + `ab.I()`; `i2==0 && dU!=0` → world shift
  `cB+=dU*400; P=cB; aS.al+=dU*400; aS.b(true); dT+=dU*20; dU=0; dR=-1;
  ak=0`.
- `i.u()` (i.java:575-594): `au` = `|ak-(O+200)|/400+|al-(P+120)|/N`,
  N=240 for `ax13-aG==4`/ax21/`ax67-bk[Z0]==49`, 800-x for `bk==27`,
  else 120.
- `i.b(boolean)` (i.java:5459): `ad` mirror sync — reused existing
  `Entity.syncAd`.

## Ported

- `LevelPack`: `etCols`/`etRows` (k.bp/bq = et dims cells),
  `flyingGrid` (`dL`), `collisionCell` bh3 arm verbatim.
- `Entity`: `recomputeAu(camX,camY,decorClip)` = `u()` verbatim.
- `Level0World`: `flyingDL`, `kDN..kDQ`, `flyingDM`, `parallaxX/Y`,
  `kH()` = `h()` verbatim, `flyingRestamp()` = `b(z2)` stamp driver,
  `applyG2()` = `U()` arm (run in `loadPackI` AND once at init —
  constructor pack counts), `tickNpc` extraction + `I()` bh3 arm with
  `dU` world-shift tail, `flyingRestamp()` per tick after camera.
- `kAR` init `0→-1` (k.java:244) and `kBu` getter → `level.etRows`
  (cells, not px — `bu=bq` at :5251). The `kDT`/`kBu/20` consumers now
  read correct units.

## Not ported (proven dead / deferred)

- `b(Graphics,…)` ep-layer arm (:4522): `ep[]` absent on bh3 packs —
  `et` stays collision-only in the renderer (already skipped as layer 0);
  flying visuals come from `eu`/`er` layers.
- `dT`-crossing `aR`/`dU` arming inside `kH`: kept verbatim — `aR` gets
  armed by the (unported) bh3 director/`W()` in the original; on a
  freshly loaded level `aR=-1` gives identity rows.

## Tests (Slice178Test, 9)

dL alloc on bh3 only / stamp-window fill + identity slots / collisionCell
through dL incl. bh3 y-bounds / grounded regression / parallax 1:1 on
level1 / `au` scoring / copy-group arm+consume+dR / `dU` world shift.

## Gates

verifier `ok:true`; unittest 57 pass; `:core:test` green (Slice178 9 +
all prior); `:android:assembleDebug`, `:gdx:build` green.
