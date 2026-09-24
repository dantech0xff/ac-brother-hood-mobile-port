---
title: slice 231 — ledgeLipGrab auto-mantle verdict test
phase: fidelity-verdict
status: done
---

## Summary

Regression test covering the shallow `i.al()` sibling arm —
`ledgeLipGrab` (Entity.kt:1632, `i.ak()` in the original naming) — the
near-column wall-edge grab that produces the no-input auto-mantle.
Completes the ledge-catch family: S61 deep hang (229), S61 releases
(230), S60 shallow lip mantle (this slice).

## Chain verified on real geometry (wx, wy) = (200, 9)

1. **Near-edge grab**: S43 fall with the box's right edge ~1px off the
   wall face (`ak = wx*20-12`) → `i2 = (W[2]+5)/20` hits `wx`,
   `i3 = wx-1` pocket open rows i4-1..i4+2 → `setAnim(60)`,
   `ak = i2*20`, `al = i4*20-1`. The hang probe's `(W[2]+20)/20+1`
   column is one cell further right — at this distance it never
   matches, so the lip arm (which runs first anyway) owns the catch.
2. **Auto-mantle**: arriving with `Q=43 ≠ 63`, the S60 arm
   (`Q != 63 || u(UP) || toward-wall`) fires `i(62)` on the very next
   tick — no input required. `Q==63` (arriving from the climb-down
   anim) is the only case that stays hanging; `cu` latches either way.
3. **Mount**: S62 `r()` → `ak += 10`, settle masked → `aZ`, `S0`,
   feet on the lip top.

## Semantics pinned

- `ledgeLipGrab` = the brushing catch → auto-mantle (no hang wait).
- `ledgeHangGrab` = the deeper catch → S61 hang wait (UP mounts,
  DOWN/grace releases).
- Which fires is purely geometric: edge within ~5px of the wall face →
  lip; edge ~1.5 cells out → hang.

## Gates

- verifier `ok:true`, unittest 57 pass
- `:core:test` green (new Slice89 test passes)
- `:android:assembleDebug`, `:gdx:build` green
