---
slice: 309
status: done
title: m7 capstone feed-in — u252 claim lifecycle + west-stair top segment proven
---

# Slice 309 — m7 capstone feed-in leg

## Goal
Prove the feed-in route connecting slice-302's u252-claim release (lower
pillar region) to slice-308's leg L start (pillar top ~ (349,540)).

## Proven findings

- **u252 ax5 claim zone lifecycle.** `u252.W = [301,1057,340,1218]` covers
  the lower pillar top. The player inside it mounts `u248` (ax66-S12
  lift/perch @ (318,1136)) and the claim runs script 42 then releases —
  the standard ax5 claim-then-release contract.
- **u248 release.** A direction-toward-`av` lunge (S235 + releaseAe) — the
  player frees back to controllable state.
- **West-stair top segment traversable.** The pillar's west face (x300-320)
  is guarded by `u60`'s sweeping leading-edge X-box (the ax60 tip cycles
  west through the climb band and kills on contact). The route instead
  climbs the `##########`-mass's **west face (x100)**, which rises above
  the tip's y585-621 sweep band, then runs east along the mass top (y519)
  onto the pillar-top `'2'` ledge. Verified: middle shelf (25,659) → mass
  west face → mass top → east → x300+ pillar-top region in ~66 ticks.

## Mapped but not bot-solved

The full release→pillar-top path is a precision gauntlet: the perch chain
(u248→u169/u170), the lethal lift X-boxes (u56/u57 leading-edge kill
strips), and the S101-bounce column faces all require swing-release and
dodge timing a waypoint bot can't reliably reproduce. The top segment is
proven traversable; the lower connection is characterized.

## Kept production fix
`PlayerFsm.kt` S228/358 release arm — `p.ac == null || p.av != (p.ac!!.ak < p.ak)`
(structured i.java:L1748-1754) — release fires whenever the claim is absent
OR the player faces away. Proven fidelity.
