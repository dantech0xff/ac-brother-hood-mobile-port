---
status: done
slice: 103
source: reconstructed-project/src/structured/k.java (dialog pipeline :350-450, case-21 u==9 arm :899-1017, a(bVar,…) :4114-4128); b.java:1721-1790 (drawWrapped X-cap)
confidence: proven (all lines below cite decompiled lines)
---

# Slice 103 — jC==21 modal dialog render (u==9)

The claim-script `k.b(idx,str,flag)` dialog (op105 producer, slice 43c) now
loads the full original `b(9,1+aj,str,str)` pipeline and renders the modal
dialog box exactly as `k` case 21 does.

## What landed

### World side (`Level0World.kt`)

- **`kDialog(idx, strRef, flag)`** — was a stub returning true; now ports
  `k.b` + `b(i,i2,i3,i4)` verbatim:
  - `bO = flag`; `bN[0] = idx>0?idx:-1` (:405-410)
  - `u = 9` (:353); loads `d(1+aj, strRef)`; wrap width **300**
    (`i!=6 → z2=true` arm, :385)
  - `a(str,0,true,9)` wrap-pager (:374-401): every >3-line chunk becomes a
    `bM[]` page via `s2 = sArrA[6*i4-1]`; the `z2` arm copies `bN[i]` into
    every page slot and writes `bN[i] = str.charAt(0)-'0'` only when
    `i2!=9` (never on this path — ported verbatim)
  - `w = iA+1`; `D(0)` = `v=min(0,w)` + `z()` when `bT==-1`;
    `bQ=true`; `z()` = `bS=30, bR=0, bT=0` (:437-443)
- **Typewriter** `dlgTypeTick(len)` (:947-957): `bR++`,
  `bT = bR*30/16`, `bT>len → bT=-1`; `v(65568)` while typing pins
  `bT=-1` (reveal-all).
- **Press tail** (dialogModal arm): `sawPressPending` → typing? reveal.
  Revealed? the u==9 catch-all (:1003-1016): `C.Z()` (resumeScript),
  `C.cd[1]=true`, `bh!=3→m(ad)` camera snap, `z(23)` sfx, `l(8)`,
  `v=w`. **Verbatim quirk:** u==9 has no `D(v+1)` arm — only u==8/10
  page-advance — so multi-page `bM[]` is unreachable and every press
  dismisses.
- **Suppression gate** `dlgSuppressed()` (:946): `v(131072) && u==9 &&
  C.cd[2]` eats the press AND the typewriter tick.

### Renderer side (`Level0Renderer.kt`)

- Case-21 u==9 arm (:899-1017) replaces the placeholder box:
  - icon/page pick: `bN[v]==1 → bP=137,i3=1`;
    `bN[v]∈2..10 → bP=50,i3=bN[v]`; else `aS.al-P<120 → 137 else 50`;
    then the u==9 `bO` override (`0→50, 1→137`) — i3 keeps its value
  - `i(0,bP)` panel (:414-429): A[4] anim-12 strip, 400×68 half-dark
    fill, u∈{8,9,10} `d(0,9)` blink hint at `j.g%10<5`
  - `i3==1 → A[4].a(cd,4+bL,0,378,bP+64)` portrait (bL = shared
    `k.bL` af()-cursor static);
    `2..10 → z[39].a(cd,i3,0,355,bP+66)` icon
  - text: `i3==-1 → a(y,0,page,200,bP+34,380,3,bT)` centered;
    else `a(y,0,page,10,bP+4,300,20,bT)`
- **`dialogText`** = `a(bVar,i,str,x,y,w,align,limit)` (:4114-4128):
  wrap at `w`, `fontY.l(0)` palette 0, draw from line `8*(cY-1)`,
  `bT` absolute char cap. The `bL`-EZIO rename arm (:4121-4124) is
  unreachable on level 0 (bL==0).
- **`FontClip.drawWrapped` + `limit`** (b.java:1721): `X = s3+limit`
  is an absolute char-index cap — line `w` clamps to it, so the
  typewriter reveals a growing prefix.
- **clip39 converted** (`z[39]` icon sheet) and `fillAr` ARGB helper
  retained from slice 102.

## Quirks kept verbatim (labeled)

1. `v=w` on every revealed press — u==9 has no `D(v+1)` page-advance
   arm (only u==8/10 do), so `bM[]` pages beyond page 0 never display
   and every revealed press takes the catch-all dismiss.
2. `bN[]` double duty: `bN[0]` carries the op105 speaker-icon idx into
   every page slot; `bN[i] = charAt(0)-'0'` write is gated by `i2!=9`
   so it never fires on the level-0 path.
3. `i3` survives the `bO` override — a `bN[v]==1` record with `bO==0`
   still picks the A[4]-portrait path at bP=50.
4. `bS=30/16` typewriter ≈ 1.875 chars/frame, not per-tick.

## Gates

- verifier: `ok:true`
- `python3 -m unittest discover -s tests -t .`: 57 OK
- `:core:test`: 781 green (5 new Slice103Test + updated op105 contract:
  autoDismissDialog=false → press1 reveals, press2 dismisses + `cd[0]`
  resume)
- `:android:assembleDebug`: OK

## Not ported (flagged)

- `bM[]` pages beyond page 0 — unreachable in the original (quirk #1).
- `bL`-EZIO rename arm (:4121-4124) — `k.bL` is the af() browse cursor
  on level 0.
- Other `u` kinds (0,4,5,7,1,2,3,6,8,10) — only 9 is produced by
  level-0 op105 records.
