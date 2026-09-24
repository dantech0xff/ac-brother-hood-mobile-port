---
title: "slice 205 — j.d table square root + ap() g.a rope guard"
phase: port
status: done
---

# Slice 205 — `inferred`-site sweep: `j.d` LUT sqrt + `ap()` `g.a` guard

## Scope

Two `inferred` sites verified against the source and ported verbatim:

1. **`j.d(int)` — piecewise table square root** (j.java:1097, **proven**).
   The original is NOT `floor(sqrt)`: it indexes a 256-entry Q4 LUT
   `j.U[]` by a shifting window, so large inputs quantize in steps of 256.

   - `j.U` loads from JAR resource `/16` (`j.a("/16",0,1)`, k.java:14367),
     blob at `reconstructed-project/resources/archive/16`.
   - **Blob-verified**: 255/256 entries match `floor(16·sqrt(i))` encoded
     as big-endian u16 starting at offset 154. The one mismatch is the
     verbatim quirk **`U[0] = 256`** → `j.d(0)` returns **16**, not 0.
   - Band dispatch (all proven from the decompile):
     `x<0→0`; `[0,2^8)→U[x]>>4`; `[2^8,2^10)→U[x>>2]>>3`;
     `[2^10,2^12)→U[x>>4]>>2`; `[2^12,2^14)→U[x>>6]>>1`;
     `[2^14,2^16)→U[x>>8]`; `[2^16,2^18)→U[x>>10]<<1`; `…<<2/3/4/5/6/7/8`
     through `x≥2^30→U[x>>24]<<8`.
   - Behavior delta vs the old `inferred` impl: `isqrt(1048575)` =
     **1020** (table) vs 1023 (true sqrt); `isqrt(Int.MAX_VALUE)` =
     **46080** vs 46340. Only consumer is `arcSolve` (projectile lead
     solver), so the quantization now matches the original exactly.

2. **`g.ap()` S79 crouch-rope guard reads `g.a`, not `standingOn`**
   (g.java:8488–8497, **proven**). The original checks the **static
   vehicle link** `g.a` — the grapple/ride entity (`ga` field, set by
   rope/carrier binds at g.java:249/2055/2991/3527/8630):
   `S==79 && g.a!=null && g.a.ax==51 && g.a.aD!=0 → return` (stays hung,
   skips the sword-swing entry). The port had approximated `g.a` with
   `standingOn` — semantically different (hang link vs support link);
   a player hanging on a rope would fail the check via `standingOn`.

## Still flagged `inferred` (unchanged)

- `grabLunge` `cy = j.b(-cz, cA)` sign convention — `cy` has no consumer
  on the arc path yet (zipline orbit reuses the field); left labeled.
- `spawnStatic` `a(9,47,5,400)` sub-op-15 marker arg mapping.
- `contextDispatch` zipline-`ac` block flag semantics (decompiler `r0`).

## Mining receipts

- `j.d` full body: `reconstructed-project/src/fallback/j.java:1097-1250`.
- `j.U` load: `j.java:670-683` (`f()` blob → int[]), call `k.java:14367`.
- `g.a` writes: `g.java:249,2055,2991,3527,8630; i.java:2714,7114,28616,
  36818,42048,42161,44789`.
- S79 guard: `g.java:8485-8497` (`S==79 && g.a.ax==51 && g.a.aD!=0 → ret`).

## Tests (Slice205Test, 8 tests)

- `table sqrt small inputs incl the U0 quirk` — d(-1)=0, d(0)=16,
  d(1)=1, d(3)=1, d(255)=15.
- `table sqrt mid band boundaries` — d(256)=16, d(4095)=63,
  d(65535)=255, d(65536)=256, d(262144)=512.
- `table sqrt quantizes large inputs` — d(1048575)=1020,
  d(2^30)=32768, d(MAX)=46080.
- `held rope stays crouched on context press` — S79 + `ga`(ax51,aD=1)
  → early return, S stays 79.
- `slack rope allows the swing` — `ga`(ax51,aD=0) → `i(81)`.
- `no vehicle link swings from crouch` — `ga=null` → `i(81)`.
- `non-rope vehicle link swings from crouch` — `ga`(ax43) → `i(81)`.

## Gates

verifier `ok:true` · `python3 -m unittest` 57 OK · `:core:test` green ·
`:android:assembleDebug` + `:gdx:build` SUCCESSFUL.
