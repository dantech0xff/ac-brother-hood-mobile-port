---
title: "Port slice 43c — i.a() big-op decoder (script ops 100-114)"
phase: port
status: done
confidence: proven
---

# Slice 43c — the `aa()` ≥100 op switch (i.java:20043-20560)

Completes the claim-script interpreter: slices 43a/43b shipped the table
loader and the sub-100 op decoder; this slice ports the 15-op big switch
that every real script in `scripts.bin` bottoms out on — dialogs, prompts,
entity commands, QTE branches, objective text.

## Prologue semantics (i.java:20060, proven)

`r0 = k.t(op)` bytes consumed unconditionally. Body executes only when
`step == groupKey || op == 108 || op == 113` — **108/113 are poll ops** that
run every step they are live; all others fire once at their key.

## Ops ported (`Entity.runBigOp` + `bigOp100Arg`)

- **100** (6B `[uid][sub][arg]`, i.java:20083+): uid → `k.q(uid)`/`this`;
  sub 0/1 = arg-switch (sub0 skips on arg==0), sub 2 = `k.c` removeEntity,
  sub 3 = `cz/cA`, sub 4 = `k.ab`+`az`, sub 5 = `az`. Arg-switch (r04) =
  the 12-case P-flag/stagger/lock-writer — incl. `r04=0 → P|=32|128 +
  G()`, `r04=4 → P&=~512 + zero velocities`, `r04=7 → aS.aA^256`,
  `r04=10 → P=32 + i(0) + i(152) chain`.
- **101** (2B): `cP` pending-branch uid — the `bI()` release tail rebinds
  `h/k(k.s(cP))` when the claim ends.
- **102** (4B): `w.sfx(u16)` — the flag arg is dead (k.A→k.z alias).
- **103** (2B): consume-only.
- **104** (2B): `k.n` screen transition → `w.kNSet` (`kCO`/`kCP`).
- **105** (4B, i.java:20290+): dialog modal — `cd[0]=true` halt +
  `k.b(idx,strRef,flag)` → `k.l(21)`. When `cd[1]` set, just `padRearm`.
- **106** (9B): alloc `cQ` dialog-bubble params on `cT` (uid-resolved).
- **107** (2B): prompt arm — mask normalized `{32→65568, 4→16388,
  16→4112, 64→8256, 256→33024}`, `cb[2]` = bit position (ct[] index),
  spawn `scriptPrompts[0]` (clip74 unmounted / clip9+`ct[cb[2]]` mounted).
- **108** (4B `[pass][fail]`, i.java:20351+): single-button QTE — hover/
  press polls every step (`padHeld(cb[0])` OR `pointerDownIn` OR
  `pointerStrip`), `cb[1]` records the result, `k.k()&&padHeld(1020)` =
  deselect; at key → `h/k(k.s(uid))` rebind pass/fail + `return -1`.
- **109** (8B): `cf` track rect + `cf[4] = Trig.atan2(dy,-dx)`; `cd[9]`
  armed at key.
- **110** (4B): `cg`/`ch` entity pair + same `cd[9]` arming.
- **111** (9B): `w.spawnParam` projectile (ax8 clip59, `P|=512`) gated
  `w.kBK`.
- **112** (6B): sequential-choice list — `cc[1..3]` filtered 0..9,
  `cc[0]` = count, prompts per choice.
- **113** (4B, i.java:20490+): sequential-choice poll — `1<<cc[1+cc[4]]`
  per step, deselect-all on `padHeld(1020)`; at key → pass/fail rebind.
- **114** (4B): objective HUD — `k.aP = k.d(1+k.aj, u16)` + `k.aO`.

## Dialog lifecycle (this slice fixes the freeze the port had)

`i.Y()`/`i.Z()` (i.java:19421/19425) = `cd[0]` pause/resume — the
dialog screen's dismiss calls `k.C.Z()`. **`k.l(21)` suspends the whole
entity sim** (k.java:1535 arm): `Level0World.tick` now freezes the
simulation while `dialogModal`, drains a one-tick cooldown, then the
next DOWN edge = `kC.resumeScript()` + the edge is eaten (`pad.edge=0`)
so it never leaks as a gameplay tap. `bJ()` = `reloadScriptOps`
(cd[0]=false, cd[2]=false, cK=-1, cb=null, cL=bz-copy) already existed.

Test-harness escape: `autoDismissDialog` on the `world()` fixture =
"instant-tapping player" — real play keeps the press-required path.

## Pointer helpers (k.java:575-605, proven)

`k.c(x,y,w,h)` = `pointerDownIn` on `k.H/k.I` (last DOWN point);
`k.d` = `pointerMoveIn` on `k.J/k.K` (NEW `lastMoveX/Y` — DOWN seeds
both, MOVE updates only the move pair); `k.j()` = `pointerStrip`
(bottom strip `I∈(204,240)` inside `H∈[36,364]` → true; `I∈[0,204]` →
true elsewhere — the 36px margins are `inferred`).

## Files

- `Entity.kt`: `runBigOp` (~280 lines), `bigOp100Arg`, `dropAeLink`
  (i.G), `pauseScript`/`resumeScript`, interface members
  `kDialog/bO/bN0/kNSet/kCO/kCP/padRearm/pointerDownIn/pointerMoveIn/
  pointerStrip/spawnParam`, companion `CT`/`scriptPrompts`/`ScriptPrompt`.
- `Level0World.kt`: `lastMoveX/Y` (k.J/k.K), `padRearm`, `kCO/kCP`,
  `bO/bN0/dialogLine`, `kDialog`, pointer rect/strip impls,
  `spawnParam`, `dialogModal/dialogCooldown/autoDismissDialog`,
  `screenL(21)` arm, tick modal-suspend block.
- `Slice1Test.kt`: `Slice43cTest` — 17 tests, one per op + prologue
  gating + the dialog-modal end-to-end.

## Verified

- `:core:test` green (incl. all pre-existing fixture tests now under the
  real modal — `world()` auto-dismisses).
- `verify-static-reconstruction.py` → `ok: true`.
- `python3 -m unittest discover -s tests` → 57 green.
- `:android:assembleDebug` clean; emulator boots `npcs=476` (AcLevel0),
  no FATAL.

## Deferred (consumers not yet ported)

`ad()` (i.java:20624) the dialog-bubble typewriter driver that consumes
`cQ[]`; `k.l(21)`'s actual screen visuals (`dialogModal` covers the
sim-suspension contract); op109/110's `cf`/`cg-ch` *consumers* (they arm
`cd[9]` for a carrier-follow loop whose tick side is still unported);
`bI()`'s remaining release branches already covered in 43b.
