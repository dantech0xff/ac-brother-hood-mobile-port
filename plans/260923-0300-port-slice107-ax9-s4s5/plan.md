---
title: "Slice 107 — ax9 S4 hint banner + S5 context pad"
phase: port
status: done
---

## Goal

Port the two missing ax9 `aV()` arms that level-0 scripts actually arm:
S4 (hint-banner zone) and S5 (context-press pad). The slice-48 `bM()` port
left them in `else -> {}`.

## Mining evidence

- Level-0 op22 setAnim calls onto ax9 uids {108,933,980} emit anims
  {0,1,2,3,4,5,6,7,8} (scripts.bin decode, verbatim EI/smallOpLen tables) —
  so S4 and S5 are live, not dead.
- `L15b9` (i.java:12121-12142, proven): S4 — while `a(W, aS.W)` overlaps,
  `k.aB = k.d(1+aj, aF)` + `k.aC = -1`; on exit `aC>0 → L1ec7` (bare
  return, keeps banner), else `k.aB = null`.
- `L15e8-L1625` (i.java:12144-12171, proven): S5 — overlap +
  `u(16388)||v(16388)` (up held/edge) OR `av && u(2)` OR `!av && u(8)`
  → `aS.i(22)`.
- `L1ec7` (i.java:13223) is literally `return` — the shared goto tail.
- False alarm corrected this slice: level-0 op105 dialogs really are all
  u==9 — the op's first arg is the speaker icon (values {1,5,6}), not a
  u-kind; `k.b()` hardcodes `b(9,…)` internally.

## Port

- `LevelCellSource` gains `kAB`/`kAC` (defaults); `Level0World` fields
  become `override`.
- `tickAx9` `when` gains `4 ->` and `5 ->` verbatim (NpcFsm.kt:4043-4057).
- Pad mapping: `u()` → `w.padDown` (held `bC`), `v()` → `w.padHeld`
  (edge `bB`) — same convention as existing call sites.

## Verification

- `Slice107Test` ×4: S4 shows `d(1+aj, aF)` + holds `aC=-1`; leave clears
  when `aC<=0` and keeps when `aC>0`; S5 up-press → `aS.i(22)`; S5
  facing-tap variants (av→u(2), !av→u(8), no-press → no rise).
- Gates: verifier `failures:[]`, 57 unittests OK, `:core:test` green,
  `:android:assembleDebug`, `:gdx:build` — all green.
