---
title: "Slice 95 — b(z2) tail: z[74] touch overlay + k.bJ damage flash"
phase: port-slice-95
status: complete
---

# Slice 95 — HUD tail machinery

## Scope

The tail of `k.b(int z2)` (k.java:3078+) — the pieces after the `bd[]`
draw-order pass (slice 93) and overlay tail (slice 94):

1. **k.bJ damage flash** (k.java:2516-2526 `I()`, :3100 draw, :5131
   `f()` clear) — full-screen ARGB fade `df=(255<<24)|(c<<16)|(c<<8)|c`
   with `c=120·bJ/8` while `bJ>0` decrements per tick.
2. **z[74] touch-controls overlay** (k.java:3142-3161) — the virtual
   D-pad + A/B buttons drawn as clip-74 **objects** (the 6-arg
   `b.a(cd,obj,x,y,flags,cx,cy)` object draw, b.java:915) gated by
   `k() && z2==0 && jc∉{14,5} && (jc!=21||u!=9)` and the claimer strip.
3. **Claim footer** `(C==null||!C.cd[6]||!C.ab()) && C.cd[2]` →
   `a("",d(0,18))` — the soft-key pill.

## Key semantics

- `k()` = `cm==1` is the **touch-controls-enabled flag**, not mount —
  `cm` defaults to **1** (k.java:159); cheat 123 toggles it
  (k.java:3937). Our `cm` previously defaulted to 0 → the whole
  mounted arm of `j()` was dead. Fixed.
- `b(x,y,cx,cy,70)` = `a(x,y,cx+35,cy+35,35)` — the r35 circle at the
  70px box **center** (k.java:541-546). Earlier code treated the args
  as center+radius directly — fixed both call sites.
- `i55` = `iC+1` for `iC∉{-1,4}`, `iC>4→i55--` — an index into the
  clip-74 object table (objects 0..12 = pad frames 0 + directional 1-8,
  buttons 9-12). Cell 4 is unreachable in the mounted arm (mid-row
  splits to 3/5), so the `iC==4 → i55=0` arm is dead code in the
  original too.
- `cn = bh3 ? 50 : 5` (k.java:3146).
- Sole `k.bJ=6` producer: boss-grab release (i.java:8869) — arm
  unported; the flash machinery is inert until then (flagged).

## Files

- `rewrite/core/.../Level0World.kt` — `cm` default 1, `kBJ/kDe/kDf`,
  `tick()` I() ramp, `reload()` `de=false`, `padCn`/`padPressed`/
  `padZone`/`padZoneFrame`/`padButton`/`touchPadVisible`,
  `wheelCell` public, insideRadial call-site fix.
- `rewrite/gdx/.../Level0Renderer.kt` — df full-screen fill, z[74]
  overlay block, claim footer.
- `rewrite/gdx/.../Level0Game.kt` — `clips[74]` load.
- `rewrite/tools/convert_slice1.py` — clip74 = pack-3
  `entry-074-marker-003` (25 modules, 3 anims, 5 frames, 22 objects).
- `rewrite/generated/clips/clip74/` — converted acpk+meta+modules.

## Tests (Slice95Test, 11 cases)

cm default; padCn; padPressed (-1 guard); padZone 9-zone + mounted
mid-split; padZoneFrame map; padButton r35-at-center; touchPadVisible
gates; kBJ decrement + df ramp + f() reload clear via fail path;
resolvePadZone mounted arm (circles → pad box → dead-center cell4
unreachable).

## Gates

- verifier `ok:true`; `python3 -m unittest` 57 pass; `:core:test` 744
  pass (8 pre-existing tests updated for the cm=1 default — they now
  set `w.cm = 0` explicitly to exercise the `!k()` arm);
  `:android:assembleDebug` OK.

## Follow-ups

- aQ minimap offscreen compositor (i.java:20128) — deferred, needs
  `k.a`/`k.b` marker overlays + `k.bt` map grid mining.
- `bJ` producer arm (i.java:8869, boss-grab release) unported.
- Rest of `c(z2)` non-bh3 branch (`dE[]`/`aj<8` score HUD) + bh3 alert
  HUD (`aE/aH/aF`, `z[54]`, `A[4]` weapon icon, `dA`).
