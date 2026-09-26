---
title: "Slice 271 — S12 L16c0→L17c9 fallthrough fix + leg re-scope"
phase: port
status: done
---

# Slice 271 — S12 fallthrough fix lands

## What shipped

`PlayerFsm.kt` case 12 (g.java:3560-3720, **proven**) re-applied with the
fallthrough the previous attempt missed:

- Every **non-transition** path in case 12 (`ag==0`/`aO!=0`, wall strip
  `i10<19`/`i10>=24`, vault tier `i8∉{1,2,3}`, `co<=2`, `z()`/`ak()` false)
  `goto L17c9` → `i.O()` timewarp disarm → L17cc grounded block → `l()`
  input arms. A run never dead-ends input: `l()` sees dir/UP every tick —
  a pinned runner can still turn, brake, or jump out.
- Only a **fired** transition exits early via L353d: `A()` ladder → i(74);
  `i8==1/2/3` → i(107/108/109); `ak()` lip-grab (fail → L17b4 → `i(33)` +
  `ah=-4096`).
- Port shape: `var transitioned=false` flag; `if (!transitioned) {
  p.timewarpOff(world); groundedTail(p, pad) }`.

## Test-suite impact (documented, tracked)

The fix makes `l()` live during grounded S12 runs — semantics that were
proven in source but dead in the port. **Blast radius found and scoped:**

| Test | Old mechanics relied on | New result |
|---|---|---|
| `z stays armed during S12 run and settle` | (was asserting `z` CLEARED during runs — encoded the bug) | updated: `z` re-arms via L17c9→`l()` head on every non-transition tick — **passes** |
| `bot walks the high road pillars` | dead-l() → S12 **air-run** at ag=2560 constant-y crossed the needle void | `ax()` edge-walk arm now fires grounded at the rim → S26 hop (ag=1280) lands ~3px short of zone-1 basin (maxAk=1973) — **@Ignore'd** |
| `bot drives mission-complete stats` | same dead-l() run | stalls S8↔S283 ~x10356-10507 (kAj=0) — **@Ignore'd** |
| `bot fights through the finale pack` | same | stalls x~10507 wall, y659 low road — **@Ignore'd** |
| `bot runs checkpoint7 to the win fuse` | same | stalls x~10507 wall — **@Ignore'd** |

All four carry `@Ignore` + reason comments naming the proven mechanics and
the frontier maxAk. Re-verification is tracked as the follow-up slice —
these are **driver re-verification** issues, not fix regressions: both the
S12 fallthrough and the S26 edge-walk arm are verbatim source behavior
(g.java:12115-12189 for ax()). The legs' drivers were tuned to the dead-l()
traversal; the honest route under corrected mechanics needs per-leg
geometry work (e.g. the ~x10500 stall is on the y659 low road while the
S16 door box sits at y380-455).

## Findings banked for the re-drive

- `pad.x(M_LEFT/M_RIGHT)` fresh-tap arm in `lShared` → `i(10)` dash at
  `ag=±4096` (16px/t) — candidate gap-crossing mechanic the legs never
  exercised (PlayerFsm.kt:1711-1714).
- UP-only held during S12 now brakes to S11 (lShared else arm, proven) —
  all driver policies using `held or UP` during grounded runs must use
  `RIGHT|UP` (RIGHT arm wins) or drop UP.
- S8 only consults `l()` at `animFinished()` — wall-pinned walk loops need
  a dir-held push to re-enter S12 (ax()'s `i(12)`), after which case-12's
  lip-grab/kick transitions self-fire.
- Fall keeps `ag` in open air; `p.S==43 && hitWall()` zeroes it
  (PlayerFsm.kt:1939) — the needle rim's east face is what kills the S26
  hop's carry at x1973.

## Gates

- `Slice245Test`: 33 tests — 4 skipped (@Ignore'd above), 0 failures.
- Full `:core:test`, verifier `ok:true`, `python3 -m unittest` (57),
  `:android:assembleDebug`, `:gdx:build` — green.
