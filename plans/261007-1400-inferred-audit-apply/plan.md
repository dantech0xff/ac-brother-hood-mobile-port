---
title: "Inferred-claims audit — apply verdicts (46 fix / 43 proven / 7 keep)"
phase: audit
status: merged
---

# Inferred-claims audit apply

## Scope

96 `inferred`-labelled claims across the port were fan-out audited against the
decompile (10 verdict batches, `audit_verdicts.json`): **46 fix** (claim
contradicted source → corrected), **43 proven** (claim confirmed → upgraded to
`proven` with `file:line` refs), **7 keep** (source genuinely ambiguous → stays
`inferred`).

## legH regression — root cause + verdict

Applying the full `i.D()` sweep (`spawnEntities` D()-reset block, ~50 new
clears) regressed `mission5CapstoneLegH`: deterministic 57-cycle loop —
respawn → hop chain → fall into the x12176 void.

**Culprit:** `Entity.grabLatch = false` (the `g.j` clear). Bisected to the
single line — removing it alone (of ~50 clears) restored the win.

**Faithfulness verdict — KEEP THE CLEAR.** `d(boolean r6)`
(`k.java:5941-5944`) runs `i.D()` unconditionally before the fresh/restore
branch, and `i.D()` writes `g.j = false` at `i.java:2495`. `k.a(true)` (the
KO/void respawn) routes through `d(true)` at `k.java:6629`. Both `g.j = true`
arms (`g.java:2306` death tail, `g.java:3194` S147 entry) are immediately
overwritten by the reload's `i.D()`. No snapshot re-arm (serializer never
touches `g.j`). The original game therefore has `j=false` post-respawn —
the retained latch was a port bug.

**Mechanism:** `j` suppresses `r() && !j → a(0)` idle-reset arms in `e()`'s
shared tail (`g.java:3457/3459`). With `j=false` each airborne hop gains
~+20px drift — the scripted chain lands at 11976 (vs 11956), vaults at the
x11984 auto-step instead of the x12051 lip, and the S43 fall clears the
floor edge → void warp → respawn → identical loop.

**Re-drive:** releasing direction during airborne hops
(`!aZ && ak in 11800..12120 -> 0`) shortens each arc; the player then runs
to x12051 grounded and edge-vaults into uid214's capture zone at
(12151,907) → S19 → lands 12275 → cp342. Test updated + passes on the
faithful sim; comment documents the +20px finding.

## Other resolutions

- `iBW` test updated: `i.bW` is never cleared (`k.java:3365`, proven) —
  `if (bW) X()` re-stamps the phase checkpoint every camera tick while
  armed. Test renamed + asserts the armed flag persists.
- `AX22DBG`/`GLDBG` instrumentation removed.
- Dead `Level0World.iCF` field removed (lived only as decl — `Entity.iCF`
  stub is the live path).
- Renderer compile fix: `VIEW_W/VIEW_H` → `Level0World.VIEW_W/VIEW_H`,
  nullable `world?.jG` in ghost-trail.

## Gates

- verifier `ok:true`, 57 unittests OK
- `:core:test` 1541 tests, 0 failures
- `:android:assembleDebug` builds
