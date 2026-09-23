---
title: "port slice 80 — M() win-stats proc + ap[] unification"
phase: port-slice-80
status: done
---

# Slice 80 — `M()` win-stats screen (j.c==15)

## Scope

- `M()` (k.java:3280-3445, `proven`) — the j.c==15 win-stats frame proc,
  dispatched from `c()` `case 15` (k.java:1141). The whole entity sim is
  replaced while j.c==15.
- `j.g` (j.java:27 static, `proven`) is a GLOBAL frame counter —
  `g++` runs before each `a()` dispatch (j.java:255), reset to 0 inside
  `l()` (k.java:1835), and `l(15); j.g = 1` (k.java:778) deliberately
  skips the first-frame teardown on resume. Ported: `jG++` moved ahead
  of `winStatsM()`.
- `j.g==1` arm: `W();ac();ad();E()` teardown + best-time persist
  `bA[52+aj<<1] = max(ap[5])`.
- Panel grow: `eE+=10` → `eF=37+eE` until `eE==140` → `j.g=1` (next frame
  →2 → rows begin).
- Rows reveal by `j.g` gates (>0 kills, >2 collects, >4 deaths, >6 bonus,
  >8 time, >10 total). `cb=false` latch at >10.
- Score `i4` (`proven`): base `aj==7→3000 | aj>=8→5000`; `+ap[0]*dh[au]`;
  `+ap[3]*di[au]`; `−min(ap[1],4)*300`; `+(bh3?ap[4]:ap[5])*30`;
  `−min(1000,(dg/16−180)*2)` when `dg/16>180`; clamp `≥0`.
  `dh=di={100,200,300}` (k.java:199-200).
- Fire `v(458784)` (`proven`): `z(23)`; `j.g≤10 → j.g=10` reveal-skip.
  Else persist `bA[81+au<<4+aj<<1]=max(i4)`, stash
  `dB=ax,dC=ay,dF=aN,dD=az`, write `bA[32]=az, bA[8]=au, bA[44]=ax,
  bA[46]=ay, bA[48]=aN, bA[36]=0`; then `v(327712)` confirm —
  `aj<7 → aj++, bA[14]=max, eg[aj]?l(30):l(2)` — or finale
  `aj==7 → aj=0,bA[14]=0,bA[15]=1,bw=0,W(),dz=0,l(24)`;
  `v(131072)&&aj<7` skip → `aj++,bA[14]=max,l(2)`; `e(true)` RMS;
  `j.c==2 → K(0)`.
- `458784 == 327712|131072` exactly (`proven`): any fire press also
  passes the confirm arm — it's the OR of the two soft keys.
- Typewriter `a(b,str)` dj/dk retype loop (k.java:3447, `proven shape`,
  font-markup bytes unported).
- `L()` stats reset (k.java:3270): `dg=0; ap[i]=0` + `a(z2)` else-arm
  stash restore `az=0;ax=dB;az=dD;ay=dC;aN=dF` (:5207-5216) → `statsReset()`
  wired into `reload()`.
- Frame counters (k.java:1652): `dg++/ap[2]++` gated
  `(aS.P&512)!=0 || ((C==null||!C.ab())&&(j.c!=21||u!=9))`.
- `ap[]` unification: `apStats`+`kAp` merged into `kAp IntArray(6)`
  (interface member), matching the orig array size.

## Verification

- `Slice80Test` (15 tests): state-15 dispatch, jG==1 arm, panel grow,
  score rows per-difficulty + bh3 variant, overtime cap, floor, reveal
  skip, persist writes, confirm/skip/finale nav, ap[1] deaths, typewriter.
- Gates: verifier `ok:true`; `python3 -m unittest` 57 OK;
  `:core:test` 656+ green; `:android:assembleDebug` BUILD SUCCESSFUL.

## Deferred

- `ac();ad();E()` renderer teardowns inside the j.g==1 arm — `unported`.
- Stats screen rendering (`b(x,y,214,...)` panel, `d(0,38+i3)` rows,
  value strings) — `inferred` surface fields `statsTitleY/statsScore/
  statsScoreVisible/statsTypeNext/statsTimeSec` expose the computed
  state for the renderer slice.
