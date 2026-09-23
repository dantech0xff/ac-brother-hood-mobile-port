---
title: "Slice 124 — combat-resolution chain h()→i()→j() verbatim"
phase: port
status: done
---

# Slice 124 — h()→i()→j() combat-resolution chain

## What the original does (proven, i.java:1271-1403)

Three functions run per-tick before an ax11/ax73 NPC's state arms, in order:

- **`h()` (i.java:1271)** — counter-engage immunity gate. `aS.S∈{216,217}` →
  false (unblockable finishers always resolve); `ax==11 && Z0==2` → true
  unless the NPC is posed {11,12,6} OR the player swings S68/S69; else
  `ax==73 && aS.S==69`.
- **`i()` (i.java:1278)** — the counter-engage, all side-effects: needs
  non-degenerate `aS.X` ∩ `W` + player in `g.b()` → player forced to S8
  (companion `k.E` hidden `P|=128`), NPC takes `aC=16` then `i(17)`
  (ax73 `i(167)` — verbatim `S != 17` check). Always returns false: its
  value is preempting `j()` by leaving the player out of `g.b()`.
- **`j()` (i.java:1305-1403)** — player→NPC strike resolution:
  `P()` dead gate (`aB<=0 → G()` = releaseAe); `ax11&&Z0==2` claims the
  `aN` finisher lock (`aN==null||aN.S!=18`) and gates on `g.b()` alone;
  every other case needs `g.b()` + ±160/±20 proximity + the `bf`
  first-swing latch (`aS.S==67` claims `aN` when free-or-self).
  Then `aS.X` ∩ `W` → `Q()`:
  - {183,184,216,217} → `aB -= J[au]`; `ax11&&{216,217}` → `aB=0`,
    launch `ag=±5120, ai=∓2560`, marker `a(8,50,…)` + `bK→a(8,59,…)`,
    `c(85,157)` react, `Z0==0→aB=30`, `g()` nudge, `k.A(13)`;
  - `ax11&&Z0==2` → `aB -= bw[au]` (weakened soldiers die to any hit);
  - `ax!=73` → half-HP every-3rd-hit engage: `ax11&&Z0==0&&aB==bu[au]/2
    → x++%3==0 → aS.i(8)+k.E.P|128 + aC=16+i(17)+g()` (no damage),
    else `aB -= H[au]`;
  - ax73 → `{286,287} ? I[au] : bw[au]`.
  Tail: `ax!=11||Z0!=0||aB>H[au]||S==85 → C()`; else weaken-line
  marker + `c(85,157)` + `g()` + `k.A(13)`.

## Fidelity corrections vs previous port

1. **Normal hits now deal `H[au]=50`, not `bw=80`** — the previous port
   applied bw to every non-finisher hit. (i.java:166-171 tables proven:
   `bu={300,400,500}`, `H={50}`, `I={20}`, `J={100}`, `bw={80}`.)
2. **Counter-engage `h()→i()` ported** — a weakened Z0==2 soldier now
   counter-engages every normal attack (player→S8, NPC→S17), which was
   previously missing. Only S216/217 reach `j()` on a weakened soldier —
   the S183/184 lock-finisher is genuinely unpunishable in the original
   (the engage preempts it; the weakened-kill path is the unblockable
   finisher or scripted react).
3. **Every-3rd-hit engage at half HP** (`aB==bu[au]/2`, shared static
   `i.x` counter → `w.iX`) ported — healthy soldiers periodically
   counter mid-combo.
4. **Z0 semantics confirmed**: `Z0==1` = weaken-eligible (records/
   scripts set it); level-0 ax11 records carry Z0=0 → they NEVER weaken
   (the marker+react+push tail arm runs instead). Slice-6's inferred
   `Z0=0→Z0=2 on low HP` was wrong — removed.
5. **`bf` proximity latch + ±160/±20 proximity gate** ported.
6. **216/217 launch arm** now spawns the clip-50 marker, `bK` clip-59
   blood, `c(85,157)` forced react, `Z0==0→aB=30` survive, `g()` nudge,
   `k.A(13)` sfx — previously only `aB=0+velocities`.

## Test updates

`assassination finisher kills a weakened locked soldier` rewritten to
the verbatim semantics: Z0=1 → hit weakens (Z0=2+S144+lock) → next
normal attack is counter-engaged (S17) → S216 (h()-exempt) lands the
finisher arm (aB=0 + launch). 867 core tests green.

## Gates

- verify-static-reconstruction: ok:true
- python unittests: 57 pass
- :core:test: 867 pass
- :android:assembleDebug + :gdx:build: green
