---
title: "Slice 250 — S89 killTouch pin probe: pinner identity + verbatim standoff verdict"
phase: port
status: done
---

# Goal

Identify the entity pinning the player in S89 under the pillar and
decide whether the >7,900-tick pin is a port gap or a verbatim standoff.

# Verdict (measured)

- Pinner = **ax11#18 @ (1759,807), j==6** — the tumbler-head bounce
  class. It holds S2 patrol the whole pin.
- S89 (g.java L25eb, proven) has **no player-side release**: `h(1)` +
  zero velocity + `r()→P|=64`. Exits live entity-side only
  (NpcFsm.kt:9017-9050): ax11 `e.S==24`, ax47 `e.S==80`, ax50 `e.S==119`
  + `padHeld(65568)` → `applyHit6(90)` counter-kill.
- The pin snaps the player onto the guard's head top-center — outside
  the spotB alert set — so an UNAWARE patroller (S2) never strikes and
  the ride persists: a designed standoff, resolvable in play by pinning
  onto an already-alerted guard (it strikes → QTE window) or by not
  falling on unaware ones. killTouch's r13 suppression is proven-dead
  (aE() always false) — pinned r14/r15 stay live, so the pinned player
  still takes other hits.
- Test: `bot identifies the S89 pinner under the pillar` — asserts the
  pin lands and the pinner is ax11.

# Gates

verifier `ok:true`; unittest 57; `:core:test`, `:gdx:build`,
`:android:assembleDebug` green.
