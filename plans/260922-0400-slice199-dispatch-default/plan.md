---
title: "Slice 199 — i.I() dispatch default arm L1f35 (unclaimed-ax tail)"
phase: port
status: done
---

# Slice 199

- **Mined the full `I()` ax-dispatch** (i.java:15493, proven): mapped
  every case → label. All 48 handlers already ported; four "missing"
  cases (ax 28/31/45/75 → L1e35/L1f1d/L1f27/L1f13) are verbatim `goto
  L1f35` — the default arm.
- **`default:` arm is not a no-op** — L1f35→L1f76 (i.java:18904-18939):
  `if (b) t()` box refresh on the bounds-dirty flag, the `av` facing
  bit → `P|1`, then the `a(k.aS, P, W)` player push (`ax!=0` guard, which
  pushL897's own gate covers). No `s()` advance — unclaimed entities
  never animate.
- **Wired**: `tick()`'s family gate now routes non-family ax through
  `defaultArm(e, player)` instead of early-return. Effect: static props
  (ax79/80 etc.) whose records set `P|4096` now push the player like the
  original.
- **`i.b` relabeled**: bounds-dirty flag (movers set it; handler heads
  and L1f35 call `t()` on it) — was "ridden/carried latch".

## Gates

verifier `ok:true`; 57 unittests; `:core:test` green incl. Slice199Test
(3): dirty-bounds refresh + facing bit, L11 land-on-top push, no anim
advance.
