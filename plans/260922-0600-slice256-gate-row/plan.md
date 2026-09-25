---
title: Slice 256 — gate-row crossing bot leg (door-exit → checkpoint3)
phase: bot-playthrough
status: complete
---

# Slice 256 — bot runs door-exit to checkpoint3 through the gate row

## Goal

Extend the golden-path bot legs one more leg: from the ax10-S16 door's
upper-tier deposit (~3812,559) east across the ax44 slam-gate row to the
next ax2 checkpoint at (4629,646), driven purely by `pad.e()` input on
the real level-0 geometry. Verdict test only — no engine changes.

## Findings (all proven on the real level-0 grid)

### The east face is a faithful dead-stall

The '20' stack at x4000-4060 (face y360-560) presents `aU=20, aY=4` to a
westbound wall. g.java L16c0's case-12 arm exits **only** via
`ag==0||aO!=0` (timewarp), ladder `A()`, vault `i9∈[19,24)&&i8∈{1,2,3}`,
or `co>2 && pushColumnBlocked` → `ledgeLipGrab`/`i(33)`. At (4001,579):
`aU=20`, `aY=4`, `z()=false` (probe reads past the stack's east edge
into air) → nothing fires → permanent S12 pin with `ag=2560`. Input is
ignored inside S12 (`cq=false` — only `l()`-head re-arms it). **Verdict:
faithful.** The upper '20' plates face is not a route.

### The walkway under the shelf crosses the whole gate row unharmed

- `aZ + u(M_DOWN)` on '5'@580 fires `a(257,8)` (PlayerFsm ~1760-1790):
  probe +20 → `aQ∈{20,5}`, `aR==0`, `e(side,below)<12` → enterFall →
  S257 → lands walkway y779. **Cannot fire inside S12** — this is why
  the bot hop-runs the approach (one grounded direction-tick → `ax()` →
  `i(12)` forever).
- The 11 ax44 gates x3898-4245 all read `W=[0,0,0,0]` in this build →
  `rectsOverlap(player.W, e.W)` false → `Z[3]` countdown and crush never
  fire. Pure `u(M_RIGHT)` autorun ran x3880→4251 through all of them.
  All gates sit S0 for ~140 ticks after spawn anyway (probe: staggered
  4-phase wave only starts ~t150).
- **Flag for later:** whether the empty `e.W` is faithful needs a
  follow-up check — the crush path is unreachable in the current port
  state (harmless shells).

### x4260 column face → '5'-lip shimmy is the intended crossing

- S33 climb on the 200px face stalls ~70-90px per cycle; **pulsing UP
  during S33 (`t%8<2`) drives the lip-scan → S92 mantle** (dirHeld+rise
  arm order, slice 169) → lands in the '5'@580 east-lip hang at
  (4205,590).
- UP vault-out is **dead** on '5' hangs (slice-227/228 verdicts — the
  probe hits the '5' lip itself, `aO=5`) — the else-chain arms S37
  instead. Holding RIGHT shimmies east along the bar: 4205→4271.
- Bar end → S43 drop → lands on '20'@580's east face → S79 slides down
  to the deep floor (y678+) → east walk to checkpoint x4650.

## Verification

`bot runs door-exit to checkpoint3 through the gate row` — parks at
(3812,559), drives `pad.e()` only, asserts `p.ak > 4640`. Trace:
drop@3941 → S12 walkway 3951→4251 → S33/S34/S92 → S38 hang@4205 →
S37 shimmy→4271 → S43/S79 → maxAk=4650, deaths=0, checkpoint=true.

Gates: verifier `ok:true`, 57 unittests, `:core:test`, `:android:assembleDebug`,
`:gdx:build` — all green.

## Notes / follow-ups

- Death respawns at the level spawn (x85) not the park point — the bot
  re-parks (3812,559) + re-syncs `N/O` after each `jC12/13` reload.
- Scratch probes used during the solve (cycle dump, climb probes, cell
  dumps) were removed; the leg itself is the committed artifact.
- Next legs after (4629,646): crusher bars x4752-4796 @y899, guard packs
  to x5794, ax13 rope @5487, ax22 at 7518+, level end ~x12728.
