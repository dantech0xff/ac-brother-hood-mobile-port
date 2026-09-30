---
title: "Slice 314 — text-panel k.b(IIII) verbatim + medal icons + sweep fixes"
phase: coverage-closeout
status: done
---

# Slice 314 — arm sweep: k.b(IIII), k.x field, advanceAnim note, typewriter markup, z[73] medals

Audit of the remaining unported/stubbed arms produced a small real-delta set.
Everything else on the list resolved to proven-dead or already-covered.

## Changes (all proven unless noted)

### `k.b(int,int,int,int)` — jC==21 text-panel initializer (k.java:349-372)
The previous `kBMark` was mislabeled as a "checkpoint-map marker" that wrote a
dead `kMapSlot` field. Verbatim port now:

- `u = slot` — the panel **kind** (0/4/5/7 = full-screen single text wrapped at
  220px via `a(d(i2,i3),0,false,i)`; other kinds = per-row dialog expanded from
  rows `i3..i4` via `a(d(i2,i3+i5),i5,i!=6,i)` with `w += iA-i5` growth).
- `D(0)` + `bQ=true` + `z()` tails.
- `i3==-1 → false`.

`dlgLoadPage` gained the verbatim `z2`/`i2` params of `a(String,int,boolean,int)`
(k.java:374-401): `z2` selects the 300px wrap + writes the leading-digit `bN[i]`
(skipped when `i2==9`) + propagates `bN[i]` into continuation slots
(`z2 && i4>1` / `z2 && i4>0` guards — previously unconditional). Call sites
`kDialog` (u9) and `tutorialDialog` (u10) pass `z2=true`; the u10 digit seed
moved inside where the original writes it.

Dead on shipped content (zero ax10-S21 records in all 8 .aclv packs) but the
S21 arm now drives the verbatim path.

### ax10-S21 `k.x` field fix (i.java:12851)
`w.kX = 48` wrote `k.X` (the crossfade field). The original writes lowercase
`k.x` — the u==8 auto-page countdown — so the arm now writes `kDlgX`
(new interface field, Entity.kt:5270). Regression test asserts `kDlgX==48`
and `kX` untouched.

### `advanceAnim` doc correction (Entity.kt:959)
The comment claimed the wrap tail was "level-code, not ported". Corrected:
it is the claim-hold release tail — fires only while a `j.c==21 && k.u!=8`
modal is up (the sole draw-pass caller), clears the player's `ag/ah` pin for
bound-states, then `k.C!=null && ax!=67 → P|=64` re-arms the entity's hold
(ax67 = score floaties is the EXEMPTION). No behavior change — entities don't
tick during the modal in our model, so the tail can never fire.

### `typewriterStep` — `\2`/`\0` moving-cursor markup (k.java:3447-3470)
The M() "NEXT ▸" typewriter previously produced the raw string — the original
inserts `\2` before and `\0` after the char at `dj` (palette-2 highlight sweep
across the full string; `dk=15` hold then `dj=0` restart). Now emits the same
decorated text; `FontClip.drawChars` already maps `\<digit>` escapes to `l()`.

### z[73] medal icons (k.java:6417-6445)
Converted `pack-3/entry-073` → `clips/clip73` (4 objects: medals 0–2 + locked
frame 3), registered `clips[73]` in `Level0Game`, and replaced the jc22
placeholder square with the verbatim `z[73].a(cd,i3,0,140,i4+91)` draw
(frame = medal index unlocked, frame 3 locked; `y.l(4)`/`y.l(2)` palette on
the row text).

## Triaged — no port needed (documented verdicts)

- **kAm input-lock veil**: already drawn (renderer :1260).
- **ae() `d(0,eB)` subline**: decompile discards the call result — unrecoverable.
- **Loading-bar fill**: load is synchronous; fixed-fill is equivalent.
- **`i==2` quit `E()`**: lazy splash loader — our clips load eagerly.
- **font truncation "NEW G"**: original J2ME artifact — kept faithful (user's call).
