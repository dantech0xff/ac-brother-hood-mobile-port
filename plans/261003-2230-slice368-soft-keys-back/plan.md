---
title: "Slice 368 — soft-key parity (claim SKIP pill, ce/cf, pause icon) and the BACK key"
phase: "port"
status: "done"
slice: 368
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/k.javap.txt:26544-26556
  - reconstructed-project/bytecode/k.javap.txt:16335-16373
  - reconstructed-project/src/structured/k.java:147-149
  - reconstructed-project/src/structured/k.java:486-521
  - reconstructed-project/src/structured/k.java:576-583
  - reconstructed-project/src/structured/k.java:859-867
  - reconstructed-project/src/structured/k.java:1040-1063
  - reconstructed-project/src/structured/k.java:2270-2311
  - reconstructed-project/src/structured/k.java:2653-2655
  - reconstructed-project/src/structured/k.java:2679-2690
  - reconstructed-project/src/structured/k.java:3163-3165
  - reconstructed-project/src/structured/i.java:17940-17946
  - reconstructed-project/src/structured/g.java:4344-4355
---

# Slice 368 — soft keys as the original hit-tests them, and BACK

Phase 3 step 6 of the parity/hardening plan asked to mine the soft-key
mapping before adding BACK. The mining turned up three input parity bugs on
the same path; they are fixed here together with BACK.

## Findings (proven)

1. **No hardware keys.** `k.keyPressed(int)` / `k.keyReleased(int)` are
   bare `return` (bytecode k.javap.txt:26544-26556). The only producer of
   the `131072` edge is the right soft-key pill hit-test inside
   `a(str,str2)`: `c((395-cf)-10, 198, cf+20, 47) → E(131072)`
   (k.java:2308-2309). `c()` reads `k.H/k.I`, the release point
   (k.java:519-521).
2. **The claim SKIP pill.** `b(false)` — the HUD pass that `case 8/21`
   runs after `I()` (k.java:859-867) — ends with `if (!z2 && C != null &&
   (C.ab() || u == 9) && C.cd[2]) a("", d(0,18))` (k.java:3163-3165;
   bytecode b(Z) offsets 5604-5650). Its edge feeds `i.aa()`'s skip latch
   `cd[2] && v(131072) → cd[1] = true` (i.java:17940) in play and the
   dialog's `:944` gate on jC 21. `b()` returns at entry on
   jC ∈ {12,13,31} (k.java:2679-2690). The port drew the pill but had no
   hit-test in play; on jC 21 it armed a stand-in rect `(349,198,56,47)`
   at press time, documented as a "new affordance".
   Mission 0 opens on such a claim (ax5 uid 299, `cd[2]` set on the first
   frame): the original lets the player skip the intro; the port did not.
3. **`ce/cf` are state.** `ce/cf` start at 60/60 (k.java:147-148) and are
   rewritten only by `a(str,str2)` (reset to -1 at entry, then the label
   widths, k.java:2271-2298). `j(x,y)` excludes the soft-key margins with
   them (k.java:581) and `j()` reads them too. The port's wheel used
   constants 60/60; its renderer wrote `kCe/kCf` from the draw path.
4. **The pause icon is a release.** `pointerPressed` arms only the wheel
   edge `E(2<<iJ)` (k.java:486-494). The pause icon is `J()` after
   `I()` and `b(false)`: `c(354,0,46,37) → E(262144)`, then
   `v(262144) → C.Y(); bw = 0; l(14)` (k.java:1040-1063; `J()` = jC ∉
   {12,13}, k.java:2653-2655). The port armed it on the press and paused
   at the head of the same tick, before the world ran.

## Changes

- `Level0World.softKeys(left, right)`: the hit-test half of
  `a(str,str2)` (`footerQ` now delegates to it). `claimFooter()` = the
  `b(false)` gate above, called after the world frame in play (jC 8) and
  ahead of `j()` in the dialog block (jC 21). The stand-in rect is gone.
- `resolvePadZone` reads `kCe/kCf`; the renderer no longer writes them.
- `consume()` DOWN arms only the wheel; the play path ends with
  `claimFooter()` then `J()` (release hit-test + `v(262144)` read).
- **BACK (port mapping, not original behaviour).** `InputQueue.Type.BACK`
  is a sequenced event; `consume()` latches it for the tick and
  `softKeys` treats it as a release inside the right pill. Where a screen
  draws no right pill (title root, yes/no prompts, plain gameplay) BACK
  does nothing. `Level0InputBridge.keyDown` posts it for `Keys.BACK` (and
  `ESCAPE` on desktop); `Level0Game` catches BACK so it never closes the
  app.

## Tests

- `Slice368Test` (13): the mission-0 intro SKIP (tap and BACK); pill gate
  arms (`ab()`, `u==9`, `cd[2]`); BACK inert without a pill; the u==9
  dialog skip by BACK; `j()` overriding the pill's left edge; wheel
  margins after a footer screen; pause on release only, one frame after
  the release, with the world running both frames; BACK ≡ right-pill tap
  on jc 4/5/6/14/19/22/30; BACK inert on a pill-less prompt.
- `Level0InputBridgeTest`: BACK/ESCAPE posted in sequence with touches,
  other keys unhandled.
- Re-timed to the original (no weakening): `pause rect tap emits M_PAUSE
  edge and l(14)` (a tap, paused the frame after the release) and
  `u9 claim-wait dialog exits on a tap in the right soft-key strip`
  (exits the frame after the release, like the pause test beside it).

## Device check (pending)

Next device run: intro SKIP pill and BACK on m0, BACK on the pause menu and
the level select, pause icon on release.
