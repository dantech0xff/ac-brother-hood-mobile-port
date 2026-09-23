---
title: "Port slice — i.ad() speech-bubble renderer + bK() refill"
phase: rewrite-slices
status: done
---

# Slice: i.ad() speech-bubble renderer

## Source (verbatim anchors)

- `reconstructed-project/src/simple/i.java` `ad()` :20624-20755 —
  per-entity dialog-bubble tick + draw arm; consumed by the frame loop
  `k.java:3740-3749` (`r019.ad()` for `ax != 11 && ax != 17`).
- `i.java` `bK()` :20602-20622 — page refill: `cR = k.d(1+aj, cQ[5])+'\n'`,
  `cQ[2]=cQ[3]`, `cS = k.a(k.y, cR, 120)` wrap, `cQ[4]=min(3,cS[0])`,
  `cQ[0]=0`, `cQ[1]=cS[0]-1`, post-clamp `cQ[4]=cQ[1]-cQ[0]`.
- `b.java` `a(String,int,boolean)` :1919 — the font wrap that `k.a()`
  delegates to: returns `short[] U`, `U[0]` = wrapped line count.
- op106 (ported earlier, `Entity.kt:2032-2047`) already writes the
  `cQ[10]` producer state this function consumes.

## Ported semantics (proven)

`NpcFsm.tickBubble(e, w)` — called for every npc with `ax != 11 && ax != 17`
at the end of the entity loop (Level0World.kt npc dispatch tail):

- `cQ == null` / `cQ[5] < 0` / `cQ[5] > cQ[6]` → early out.
- `cQ[7] == 1` wait-mode: holds the bubble until `k.C == null ||
  !k.C.ab()` → full teardown (`cQ`/`cS`/`cT` nulled, `cQ[1] = -1`,
  `cR = ""`, `cQ[4] = 0`).
- `cQ[2] == -1` first arm: `cQ[5] <= cQ[6]` → `bK()` refill, else
  `cQ[6] = -1; cR = ""; return`.
- `cQ[2]--` each call; `> 0` → draw arm emits `w.bubbleDraw` descriptor
  (bubble rect `ak-kO, al-kP-70`, height `k.y.k(lines)+10`,
  `bh[k.aj]==3` suppresses the `av` flip and forces the tail-up/`cy<0`
  flip, `cQ[8]==1` inverts `av`, `av` flip shifts x by -120), then
  `k.y.l(1)` (typewriter advance — no core op), re-wraps `cS`, and
  re-clamps `cQ[4]`.
- `cQ[2] <= 0` → page/string advance: last-line test
  `cQ[0]+cQ[4] == cQ[1]`; if so `cQ[5]++` → refill (next string) or
  teardown; else `cQ[0] += cQ[4]` (next page). Then the `cQ[0]+cQ[4] >
  cQ[1]` clamp and `cQ[2] = cQ[3]` re-arm — verbatim even after teardown.
- Teardown release: `cQ[9]==1 && k.C != null && k.C.ab()` →
  `k.C.cd[2]=cd[1]=true`, `bh[k.aj] != 3` → `k.m(k.ad)` (stub — `kM`
  remains a no-op pending the `k.m()` port).

## Interface/renderer split (inferred where marked)

- `w.bubbleDraw: BubbleDraw?` — the draw descriptor channel
  (`volPaintRect`-style); renderer side still to consume it.
- `w.wrapDialogText(text, 120)` — `k.a(k.y,·,120)`; `inferred` greedy
  word-wrap at ~6px/char returning `[0]=lineCount, [1..]=line starts`.
- `w.dialogAdvance(lines)` — `k.y.k(lines)`; `inferred` fixed 10px/line.
- `k.d(1+aj, idx)` → `w.levelString(1+kAj, idx)` (real strings).
- `k.C.ab()` → `Entity.claimActive()`; `k.m(k.ad)` → `w.kM(w.kAd)`.

## Tests (`Slice69AdTest`, 7)

dead/no-cQ no-op; wait-mode teardown vs hold; refill wrap + descriptor;
page advance then next string; teardown `cd[1..2]` release on active
claim; `bh==3` style flip + `cy<0` tail-up. All green — verbatim
post-clamp quirk (`q[4]=1` for a 2-line string) asserted as behavior.

## Gates

verifier ok:true · 57 unittests · :core:test (all green incl. new 7) ·
:android:assembleDebug — no emulator run needed (state-machine + draw
descriptor only; no new assets).
