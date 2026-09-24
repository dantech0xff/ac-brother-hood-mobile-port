---
slice: 187
title: "g.e() case 8 → L1301 hit-connect lock + L1926 universal post-arm tail"
confidence: proven
sources:
  - reconstructed-project/src/simple/g.java:3011-3024  (L1301 S8 arm)
  - reconstructed-project/src/simple/g.java:3455-3473  (L1926-L1947 tail)
  - reconstructed-project/src/simple/i.java:7191-7198  (i.L() cell check)
  - reconstructed-project/src/simple/i.java:4446-4580  (i.a() op dispatcher, op18)
  - reconstructed-project/src/simple/g.java:135-155    (g.a() damage pay)
  - reconstructed-project/src/simple/i.java:285-292    (i() clears P&64)
---

## What

Ports the last unported explicit `g.e()` state arm — `case 8 → L1301`, the
S8 hit-connect lock — plus the L1926 universal tail that closes **every**
post-dispatch state (170 `goto L1926` sites in the original).

### S8 arm (L1301, g.java:3011-3024)

The struck victim's FSM drives `k.aS.i(8)` on the player when its W box
overlaps the player's X attack box during `g.b()` attack anims (call sites
i.java:1777/1914/1976/6040/6065/8090/10363/10425 — already wired in
NpcFsm.kt). Verbatim sequence:

- `T==1 && U==0 → k.A(11)` — footstep sfx at frame one.
- `aj=0; ah=0; ag = av ? +1280 : -1280` — a ±1280 recoil step *opposite*
  facing (`av==true` = facing left per `pad.u`/`am()` writes), i.e. a
  backward stumble.
- `r() → P|=64; l()==false → aw()` — on anim end the anim-hold bit is set
  and locomotion resumes via `aw()`. `P|64` is transient even in the
  original: `aw()`'s inner `i()` clears bit6 (`P &= -65`, i.java:290).
- `i.f(this)` — travel-side scroll-wall clamp.

### L1926 universal tail (g.java:3460-3473)

Runs after every `a(an())` dispatch arm (in our port: before
`interactScan`/`mountEntry`):

1. `S!=9 && ab!=null && ab.S==14 → ab=null` — the linked partner
   (mount/carry entity) is released once it reaches S14.
2. `aR!=2 && aO!=2 && !L() → aO==6 → a(18,0,0,this)` — when the head cell
   is type-6 and the entity is neither standing on/in a type-2 cell nor
   failing `i.L()` (`e(ak/20,al/20)==2` feet-cell check), the op
   dispatcher fires op18 = `g.a()` damage pay (`d(u[k.au])`) + `i(43)`
   drop-through knockdown — the type-6 ceiling-drop mechanic.

## Tests

`Slice187Test` — 8 tests in Slice1Test.kt:

- S8 back-steps opposite facing (`ag=∓1280` per `av`).
- S8 footstep `k.A(11)` recorded only at `T==1&&U==0`.
- S8 anim-end resumes out of the lock via `aw()` (asserts S transition;
  `P|64` transient per original `i()` semantics).
- L1926: `ab` released at partner S14; kept below S14; skipped in S9.
- `aO==6` head cell fires op18 (`x1` drain) when not on type-2 ground;
  suppressed on type-2 ground.

## Gates

verifier `ok:true` · 57 unittests · `:core:test` (all green incl. 8 new)
· `:android:assembleDebug` · `:gdx:build`.

## Notes / debt

- `aO==6` type-6 cell semantics (drop-through zones) remains partially
  inferred — the arm sequence is verbatim-proven, the level data that
  marks cells type-6 is decoded but untested in a live mission.
- Stale "unported" comments cleaned earlier remain as doc debt
  (Entity.kt:530, Level0World:2251/2500 — all proven-ported sites).
