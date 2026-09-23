---
title: "Slice 96 — c(z2) HUD tail: emblems, score, weapon corner, stopwatch, banner"
phase: rewrite
status: done
---

# Slice 96 — the rest of `k.c(z2)` (non-bh3 arm)

Ports the remaining shared (non-bh3) arms of the in-game HUD proc
`c(boolean z2)` (reconstructed-project/src/structured/k.java:4176-4353)
plus the `A[4]` weapon card.

## Mined (all `proven` unless noted)

- **Top bar** (:4178-4186): `ax==0→ax=30`; `g.f(ax)` = `x[1]=min(x[1],ax)`
  (g.java:4425); z[12] anim2 frame `(ax/15)-1` at (2,30); `A[4]` anim
  `8+bL` at (22,30); z[12] anim6 same frame again at (2,30) after the
  `j.a(43,6,x1*11/15,20)` sync bar (already ported).
- **Score HUD** (:4247-4263, gate `!bh3 && z[54]!=null && aj<8`): `az`
  clamped to 0..32767; `dE[]={0,100,200,400,600,800}` (:229) descending
  threshold scan → `(az-dE[t])/(dE[t+1]-dE[t])` text, or `az-dE[last]`
  at the top tier, drawn at (200,-1) align 17; z[12] anim7 at
  `(200 - b.d/2 - 10, 13 + (j.g/3)%2)` — the bobble icon.
- **Weapon corner** (:4273-4287): gate `!z2 && !bh3 && aS.o() &&
  (C==null||P&512) && ((jc21&&u8)||jc8) && z[12]!=null`; `at==1→at=0`
  inside the gate; `d(355,197,30,26)` pointer rect → anim22 else anim8
  at (370,210); z[12] anim `dn[p(g.I)]` — `dn={10,12,9,11}` (:207),
  `p()` = first-set-bit scan (:3542).
- **Stopwatch** (:4289-4326): `aJ==1` slides `aK+=10` until `>80` →
  `aJ=2,aM=0`; `aJ==3` slides `aK-=20` until `<-40` → `aJ=0,aM=0`;
  `aJ==2` runs `i8=aL*1000-aM` clamped ≥0; draws `d(0,78)` ("ESCAPE
  TIME", `d(0,122)` "CATCH TIME" when aj==7) and `mm:ss:cc` at
  `(aK,40/60)`. Format proven via raw bytecode k.javap.txt:21130+
  (`(i8/1000/60)%60 : (i8/1000)%60 : (i8%1000)/10` — both decompilers
  aliased the scratch var).
- **Banner** (:4327-4335): `aB!=null && aC!=0` → black `j.b(0,200,400,40)`
  + centered text; `aC--`, `aC==0→aB=null`.
- **Timed line** (:4337-4343): `aO<0||aP==null → aP=null` else
  `y.a(aP,200,23,17)` + `aO-=j.f`.
- **Supporting**: `o()` = `S∉{2,20..29}` (i.java:5423); A[]/z[] = `J(i)`
  = pack-3 `entry-0XX-marker-003` (:5328); z[12] is entry-012 — already
  converted as `clip94`, aliased to `clips[12]`; bU[78]="ESCAPE TIME",
  bU[122]="CATCH TIME" added from pack-14 entry-0.

## Port layout

- `Level0World.hudStep()` — every `c(z2)` mutation hoisted into the
  tick under the verbatim claim gate `(C==null||!C.cd[6]||!C.ab())`
  (the orig call-site gate in `b(z2)`).
- Helpers: `playerAliveO()` (`i.o()`), `weaponCornerArmed()`,
  `weaponCornerPressed()` (`d(355,197,30,26)`), `hudScoreText()`,
  `weaponIconAnim()` (`dn[p(gI)]`), `stopwatchText()`.
- `Level0Renderer`: emblems + A[4] card around the existing sync bar,
  score text + bobble icon, weapon corner, stopwatch, banner, timed
  line — all `drawFrame`/`drawText` calls in the orig's order.
- New world fields `kAB`/`kAC`/`kAt`/`kTimerMs`; `kAJ/kAK/kAL/kAM`
  reused (the ax42 fuse already mutates the same globals, as the
  original does).

## Gates

- verifier `ok:true`; `python3 -m unittest` 57 green; `:core:test` 764
  green (11 new `Slice96Test`); `:android:assembleDebug` green.

## Deferred (next slices)

- bh3 arm of `c(z2)` (:4190-4245 — HP column, z[54] counter, alert
  meter with `aE/aH/aF` decay) → slice 97.
- `g.g` overhead z[10] anims 29/41 for S303/295.
- `aB`/`aC`/`aP`/`aO` producers live in unported claim ops
  (i.java:12134, :18907) — fields are wired and readable now.
