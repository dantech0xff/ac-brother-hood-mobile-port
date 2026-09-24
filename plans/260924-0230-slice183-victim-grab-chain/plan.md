---
title: "Slice 183 — victim-side grab/counter-kill chain (i.I dispatch arms)"
phase: port
status: done
---

## Scope

Ports the `i.I()` victim-side grab/counter-kill chain onto `NpcFsm`'s
dispatch arms — the states an ax11 soldier traverses when the player grabs,
mash-escapes, or gets flung/counter-killed. All mined verbatim from
`reconstructed-project/src/simple/i.java`.

## Source → port map (proven, `i.java`)

- `S85` (L302, :5447-5476): corner-kick — `ai=∓1280` accel when `ag!=0`,
  `e.av`-driven; supported → vel0; `r()` → `ah=ag=0` + `i(174)` +
  `az=aS.az+1`.
- `S144` (L548-L559, :6048-6066): weakened block — `T==3 → c(1)`
  (tutorialHint); `aS.X∩W + g.b() + aS.S!=8` → counter `aS.i(8)` +
  `k.E.P|=128`; `a()` body-push; `r()` → `i(23)` + `aS.G()`.
- `S174` (L318-L327, :5478-5515): grab approach — `c(0)`; player-W ∩ W →
  `i(175)` + `aS.i(310)` (held) + player freeze/snap to `this.ak` +
  `aS.av=!av` + `bl=40` + `iBx=this` + vel0; else `r()` → `i(140)`.
- `S175` (L329-L347, :5864-5906): grab hold — `g.g()` dead → `i(177)` +
  `G()` + `az=100` (bx NOT cleared — verbatim quirk); else mash gauge
  `g(65568,80)` (k.v EDGE presses, `bl+=8` per press else `bl--`):
  - FILLED → `bl=0` + `G()` + `aB=0` + `i(176)` + `k.A(24)` + `S()` +
    `aS.i(311)` + `aN=null` + `az=100` + `k.o()` + `bx=null`;
  - `bl==0` (player escapes) → `i(177)` + `G()` + `aS.i(312)` +
    `az=100` + `bx=null` + `aS.ag = aS.av ? 1280 : -1280` (fling);
  - L341: `aS.S ∈ {310,311,312}` → hold, else → `i(177)` + `az=100`.
- `S176` (L348): `r()` → `i(139)` + `k.e(0,aw)` stat tally.
- `S177` (L351): `r()` → `i(23)`.
- `S18` (L506, finisher-offer) via `grabOfferArm18` label machine.
- `S96` (:L737): `P|=512`; `r()` → release `g.h` grab-holder + `k.c()`
  removal.
- `S140` → `i(23)`; `S16`/`S17` counter-engage arms as mined.
- `g.h` grab-holder static → `LevelCellSource.grabHolder` /
  `Level0World.grabHolder`.

## Ordering divergence (established, documented)

The original runs `h()→i()` engage BEFORE the `switch(S)` (i.java:4184);
our `hGate`/`iEngage` run post-when. `S144`'s arm reproduces the original
precedence: when its counter conditions hold it consults `hGate` first —
engage lands `i(17)` and shadows the arm's own counter exactly like the
original; the arm's own `aS.i(8)+P|=128` only fires under `h()` posed
immunity. (Fixed this slice — was consuming the attack and pinning S144.)

## Corrections to prior port

- `mashGauge` (Entity.kt:1659): uses `padHeld` (k.v = EDGE per-press
  `bl+=8`) not `padDown` (k.u = HELD) — i.java:6881.
- `playerBodyPush` helper deleted — deduped to existing `pushContact`
  (Entity.kt:1217), which already encodes `i.a()` L29-L63 correctly.
- S175 dead-player arm faithfully keeps `iBx` (bx NOT cleared) — verified
  verbatim, not a bug.

## Tests

`Slice183Test` — 14 tests: S85 kick/bind, S174 overlap-bind + snap,
S175 fill/drain/dead arms (asserts verbatim bx-keep quirk), S176 tally +
S139, S177/S140 → S23, S18 finisher-offer edge, S144 counter via
engage-first, S182 throw params (ag=±3328, ah=-6656), S184 ground snap,
pushContact directions. Recipe for anim-end transitions:
`e.T = frameCount(S)-1 && e.U = frameDuration(S,T)-1` then one
`npcFsm.tick`. Parked players need `setPositionPx + refreshBoxes()` or
stale X boxes drive `hitReact` (writes `ag=±1536` via `i.c`).

## Regression fixes found in gates

- Two kill tests in `Level0WorldTest` assumed a passive dummy — soldiers
  now counterattack faithfully (`hitReact → hitAnimByType(6)` → the S6
  lunge arm → strike → player stagger). Pinned `s.setAnim(0)` per tick in
  the damage-intake tests to isolate `j()` — test semantics unchanged.
- S85 test: airborne entity's tail integrate applies `aj=1536` gravity —
  assert relaxed to the S-transition + `az` bump only.

## Gates

- verifier `ok:true`; 57 unittests pass; `:core:test` all green
  (incl. 14 new); `:android:assembleDebug`; `:gdx:build`.
