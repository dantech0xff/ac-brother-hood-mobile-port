---
title: "Port slice 34 — ax29 aP() boss duel FSM + aQ() attack picker"
phase: gameplay-port
status: done
---

# Slice 34 — ax29 boss duel (`i.aP` / `i.aQ`)

The largest single-entity FSM in the game: `aP()` at `i.java:10328-11076`
(~750 decompiled lines), the Cesare duel driver, plus `aQ()` at
`i.java:11108-11170` (attack picker, `d.a` table at `d.java:9`). ax29
records exist only in the finale level — dormant on level 0 — so the port
is exercised by synthetic entities.

## Ported verbatim

- `aP()` head: `k.aU = this; P |= 16; by == 2 → return`.
- by-switch → `r0` counter eligibility (`i.java:11057-11075` — the L9/L21/
  L22 arms are decompiler-dropped into a tail block): **by0 `r0=false`**,
  **by1** `S∈{8,0,9,11}` or `S==7&&T>9`, **by3 `r0=true` unconditional**
  (this corrects the earlier mined read that gated by3 on the same state
  set — it makes the `S∈{28,4}` stagger arm and the attack-window punish
  reachable, which the decompiler's own control flow confirms).
- L23 `r0 && W∩aS.X` → by1 `j.a(0,1000) <= 200+cn` counter `i(21)` +
  player-punish `aS.i(8)+k.E.P|=128` vs stagger (`co=S`, `bP()`, `i(20)`,
  `k.A(32)`, `a(8,50,1,av,ak,al-40,300)` spark, `aB-=40`; `aB<=300 → i(13)`
  + `q(Z[3])` ax10-S55 face flip) — by3 `S∈{28,4}` same-stagger with
  `aB<=0` variant + `ck.P|=128|32`, else `S∉{20,26}` attack-window punish.
- `ci[5]` move counters (L89-L104): `S∈{14,35,15,16,7,17,4}` skips;
  `ci==null||len<5 → new int[5]`; by1 `{0,1,3}++`, by3 `{0,1,2,3}++`.
- `cn` ramp: `S20&&T==0 → min(cn+100,800)`; `S21 → 0`.
- `a()` push-past skip-set `S∈{2,38,4,5,40,13,26,17,8,39}`.
- by3 exhaust: `S∈{28,20} → cp++; cp>=48 →` S28 `i(0)` / S20
  `i(16)+e(5)+ci[0]=0`, `cp=0`.
- Arena clamp (L169-L184): `S==8` falls straight into L184; the arm covers
  `S∈{8,39,20,21&&r(),5,40,4}`, each starting `a(true);t()`. `S∈{5,4,40}`
  clamp against `k.ac[0]/k.ac[2]` (L187→L190 — earlier read had S5 only),
  others against `k.R`/`k.S`. Edge hit: `ak` snap +
  (`S∈{5,4,40}` → `ah=ag=0, ck.P|=128|32` else `ah=ag=0, i(by3?38:2) +
  a(true,0)`).
- L215 S-switch: S13 `ag=±1280`; S14/35 `Q()`+punish+1-or-3 knife barrage
  (`e(ak/20,r14/20)` cell read, `r15=aS.ak+(r132-1)*100` by3,
  `a(X[0],X[1],r15,r14,61,71,8,az+1)` + `d(9,r15,r14,az)`, `r()→i(0)`);
  S20 `ag=±1024` + `ci[4]++` + `r()→(by3: co==28?28:0, else 0)`; S15/16/6
  inert+punish+`r()→i(0)`; **S7 grab-QTE** (`T<=6`: `g.r=true`, `aS.i(1)`,
  vel0, `k.E` arm, `aS.av=!av`, `v(16388)→cj`, `!cj→a(21,·) marker +
  ae@y-100 else G()`; `T==7`: `g.r=false`, `b(4)` timewarp, `k.o()` input
  lock, `ad` ±38400 throw projectile (`-999,9,100,300` child), `cj →
  aS.i(10)+aS.ag=±4096` else `aS.a(4,0,0,this)`, `G()+k.A(16)`; `T==9`:
  `O()` + `cj → (by3→Q+i(3)+e(0); cj=false; k.p())`; `r()→i(0)+cj=false+
  k.p()`); S8/39 `Q(), ag=±1024, |dx|>60→i(0)`; S9/37 `Q(), ag=∓1536,
  |dx|<100→i(0)` else `r()→Q+aQ()`; S4 `ag=±2560, W∩aS.W→i(6)+e(2,·,·,300),
  r()→bP+i(0)+ck.P|=128|32`; S28 `Q()` only; S26 `Q(), kE, a(80,·) marker,
  (W∩aS.W||W∩aS.X)&&v(65568)→(aS vel0, G(), q(Z[by3?2:1]) ax5→P|=16+N()),
  r()→G()+i(27)`; S21 punish+`r()→Q;r()→aQ`; S0/36 idle (`cl==null→
  a(61,71,19,99)+cl=aK+k.b`; `cl.az=az-1, cl.P&=-129|16|512`; `bP,Q,
  ci[4]++, ci[4]>=16→(by3?i(41):aQ)`); S41 `r()→aQ`; S3 `r()→Q+i(4)+e(1)+
  a(true,0)`; S2/38 `ag=∓5120`, S5/40 `ag=±3840`, `r()→bP+Q+idle-check→i(7)
  +ci[1]=0 else i(0), cj=false`; S17 `T==aa.b(S)-3` player snap +
  `aS.i(370)+d(11,·,·,99)`, `r()→P|=64`; S25 `r()→k.E+i(26)`; S27
  `r()→aB+=160+i(0)`; S33 `r()→i(14)+ci[3]=0`; all others inert (L495).
- `aQ()` attack picker: `d.a={16,15,7,17,9,8,5,14,10,33}`; by0 → `r02=7`;
  by3 `ci[2]>=160` idle-window → 3; `ci[3]>=80&&v()&&aB<=500` → finisher
  arm (by1&&!cm → `r92=9+e(17)+cm`, else 7; `ci[3]=0`, short-circuits the
  distance bands via the L31 `goto L90`); bands `r0>100 → ci[1]>=48 idle →
  2 else 4|8(by3)`; `60<r0<=100 → ci[1]>=48&&aS.aZ → 6+a(true,0)`, `ci[0]
  >=32 → 1`; `r0<=60 → ci[0]>=32 → 0 else 5`; `r02=r9` unconditional at
  L89; `r02<0||>=10 → i(0)`; by3 value-remap `5→40, 6→39`.
- Helpers: `i.a(int×3,boolean,int×3)` ax8 spark factory → `spawnFx8`;
  `i.d()` ax61/clip71 child → `spawnBossFx`; `i.a(8-arg)` param-curve
  projectile → `spawnPathFx` (Z[0..3] screen endpoints, Z[8,9] world dest,
  Z[4] ctrl mid-x, Z[5] src-y-100, Z[6,7]=0/16, `P=528`, `aK.a(true,0)`);
  `i.Q()` → existing `facePlayer`; `i.N()` → `claimKC` (claim `k.C`,
  `P|=16`); `i.e(int)` → `bossAura` (`ck` aura manager);
  `i.b(int)`/`i.O()` → `timewarp`/`timewarpOff` (`i.aH/aI`, `k.X/W/aw`,
  `k.bh[k.aj]==3` gate unmined); `k.o()`/`k.p()` → `lockInput`/`unlockInput`
  (`k.am`, `k.dd`, UI resets render-side `inferred`); `i.a(boolean,int)`
  → `startTrail` (5-dot `cU[10]` ring + `cV`), `i.af()` → `pushTrail`,
  `i.ag()` → `hasTrail`, `i.bP()` → `endTrail`; `i.y()` → `forwardWall`
  (`ag<0→bb; ag>0→bc; else av→bb else bc`); `i.a()` → `bossPushPast`
  (ax29-reachable subset only).
- Statics → world fields: `k.aU/E/C`, `i.by/ci[5]/cj/ck/cl/cm/cn/co/cp`,
  `i.aH/aI/aJ`, `k.X/W/aw`, `k.am/dd`, `g.r`, `k.J/K`, `k.ac[]`,
  `padHeld(mask)` = `k.v`, `k.S` aliased to `boundMaxX`, `k.R` aliased to
  `boundMinX` (one static each in the original — ax37 scroll triggers, the
  bD director's `k.R=-1` init, and the aP arena clamp share the field).
- Dispatch: `ax==29 → npcFsm.tickBoss(n, player, pad)`.

## Inferred / flagged

- The L9/L21/L22 `r0` arms are decompiler-dropped into a merged tail
  block; by3=`r0 true` is the read consistent with L56's S28/4 stagger arm
  being reachable — flagged in-code with the label citation.
- `i.N()` reduced to the claim: the old-claimer arm needs unported
  `ab()`/`bI()`/`k.c` sequencing fields (`inferred`).
- `b(int)`/`O()` slow-mo — `k.bh[]`/`k.aj` table unmined; port carries the
  flags only.
- `k.ac[]` arena-rect producer unmined → `kAc` stays null (`?:` falls back
  to `k.R`/`k.S`, matching the original's non-S{4,5,40} path).
- `k.J`/`k.K` held-touch point aliased to `lastTouchX/Y` (`inferred` —
  J2ME tracks the current point, port keeps the DOWN point).
- `cj` resets in `aQ`'s `r9=2` arm and the advance arm's tail are ported
  verbatim; `i.cj` is shared with `aP`'s grab state by design.

## Verified

- `:core:test` — 12 new boss tests (dormant pin, by1 counter at maxed
  `cn`, by3 S28 stagger + `co`/`aB`/`ck`/`sfx`, by3 unconditional-`r0`
  punish, `ci` per-tier increment, `cn` ramp cap, by3 48-tick exhaust,
  `aQ` by0→S14, S7 grab lock → T7 throw arm → T9 unlock, S4 overlap→S6,
  S26 ax5 claim on 65568, S2 advance→S7 grab) — suite green.
- verifier `ok:true`; `python3 -m unittest` 57/57.
- Emulator: `android-debug.apk` boots level0 `npcs=454` clean (boss
  dormant — no ax29 records on level 0).
