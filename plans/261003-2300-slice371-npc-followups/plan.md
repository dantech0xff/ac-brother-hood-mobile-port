---
title: "Slice 371 — the slice-364 NPC follow-ups: one v(), one bc(), aV() S47/S50, l() S117, k.bh==3"
phase: "port"
status: "done"
slice: 371
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:4451-4555 (u() @0-175)
  - reconstructed-project/bytecode/i.javap.txt:4579-4747 (v() @0-336)
  - reconstructed-project/bytecode/i.javap.txt:39788-43560 (aV() — S47 @537-541 at 39983-39985, S50 @7562-7750 at 43484-43560)
  - reconstructed-project/bytecode/i.javap.txt:9765-10113 (l() — @181-231 at 9838-9859, @713-753)
  - reconstructed-project/bytecode/i.javap.txt:45338-45494 (aX() @0-319)
  - reconstructed-project/bytecode/i.javap.txt:48119-48522 (bc() @0-865)
  - reconstructed-project/bytecode/i.javap.txt:48597-48711 (bd() @0-229)
  - reconstructed-project/bytecode/i.javap.txt:33907-33954 (a(III)V @0-82)
  - reconstructed-project/bytecode/i.javap.txt:19172-19215 (a(IIII)V @0-90)
  - reconstructed-project/bytecode/i.javap.txt:6981-6993 (B() @503-532)
  - reconstructed-project/bytecode/i.javap.txt:65599-65600 (aa() op37[17] @1714-1715)
  - reconstructed-project/bytecode/g.javap.txt:17392-17420, 18334-18358 (g.n() @0-46, @2117-2160)
  - reconstructed-project/bytecode/k.javap.txt:2850 (the only k.aQ: `static Image`), 5128-5172 (k.c(III)), 13068 (k.I() @318), 14471/14711 (k.b(boolean) @1592/@2146)
---

# Slice 371 — the NPC-side follow-ups of slice 364

Slice 364 recorded seven port bugs it did not fix
(`plans/261003-1800-slice364-overlap-ia/plan.md`, "Discrepancies found
along the way"). Each was re-read in the bytecode (the authority — the
decompiled views garble two of them) and fixed only where the bytecode
proves a divergence. Offsets are `i.javap.txt` unless prefixed.

## Per-item table

| # | Item | Bytecode evidence | Verdict | Conf. | Test (`Slice371Test`) |
|---|---|---|---|---|---|
| 1 | aV() S50 else-arms | aV() tableswitch `50: 7562`; @7562-7596 `g.a != null && g.a.ax==43 && S∈{1,4}` else → @7746 `G(); return`; @7609 `i.a(aS.W, W)` false → @7737 `G(); g.C = true; return` (javap 43484-43560) | **divergence, fixed** (`NpcFsm.kt:2173`). Leaving the zone kept the `a(7,…)` marker and left `g.C` false, which mutes the rope's own `k.v(16388) && g.C → i(4)` release (bw() @222-239, the only `g.C` reader). Level 2 has two S50 zones (uid 191, 496). | proven | `aV S50 rope leaving the zone drops the marker and re-arms g-C`; `aV S50 without a rope drops the marker` |
| 2 | l() ax11 S117 | @181-231: `af.ax==69 && af.Z[0]==0` → `aS.S∈{250,150}` → `iconst_0; goto 713`; `S==117` → `iconst_1; goto 713` — the af.S checks (@234-353) and the W/rect arm (@356+) never run (javap 9838-9859). Structured i.java:1664-1666 has this exit; it garbles the other states' fall-through to @234 | **divergence, fixed** (`NpcFsm.kt:10388`): S117 is `resolved = true, r0 = true`. The port "primed" r0 and let af.S∈{7,1} / af.S==6 / L54 overwrite it. | proven | `l() ax11 S117 on a Z0==0 ride goes straight to the LOS tail` |
| 3 | `inPlay` vs `k.bh[k.aj]==3` | v() @206-214 `getstatic k.bh; getstatic k.aj; iaload; iconst_3; if_icmpeq` (javap 4688); aX() @101-109 same (javap 45389). `k.bh = {4,3,4,4,3,4,4,4,4}` (k.java:263) — `!k.al` is a different thing | **divergence, fixed**: aX() (`NpcFsm.kt:3774`) and the v() arm (now only in `inPlayV`) read `missionBh() == 3`; `inPlay` (= `!k.al`) keeps its test readers only; stale doc lines fixed (`Level0World.kt:385-390`, `Entity.kt` interface). No shipped ax14 record is linked (`f[11] == -1` everywhere), so aX()'s linked arm is data-dead; the v() arm matters for every ax14 guard/zone. | proven | `aX() linked arm is the bh3 arm only — mission 0 consumes on r5-S == j`; `v() ax14 arm on a non-bh3 mission tests the player's W` |
| 4 | `wasHitRecently` vs `inPlayV` (+ W null) | v() @0-336 (javap 4579-4747) and u() @0-175: u() @39-65 is `(ax==13 && aG==4) \|\| ax==21 → /400,/240`; v() @296-313 sends ax78 S3 to `i.a(k.ac, W)`; @197-205 `W == null → true`. All 39 bytecode call sites of `v:()Z` (i 31, g 4, k 4) were mapped to the port: 23 already on `inPlayV`; 9 on `wasHitRecently` (aV() @1469/@5095/@5357, bB() @432, w() @1/@38/@63, g.e() @13806, g.az() @1591); 5 on partial copies — B() @504 `yOverlapsCam`, g.n() @35/@2149 `flightAliveV` (item 6), bb() @1233 `markerVisible` (returned `au > i`, the inverse, no camera test), bl() @2499 `ax64Alive` (`au <= i → true` without the camera test); k.I() @318 (result discarded) ran a bare `u()`; k.b(boolean) @4137 sits in the debug entity overlay (aw/coordinate strings, box rects — unported; high-confidence, its gate was not traced); `onscreen73` had no caller | **divergence, fixed**: `inPlayV` (`Entity.kt:3895`) is the one v() port; `wasHitRecently`, `updateAu` (the divergent u() copy), `yOverlapsCam`, `flightAliveV`, `markerVisible`, `ax64Alive`, `onscreen73` deleted; every site above calls `inPlayV`. **W == null**: an ax14 `W` is allocated only by the record ctor (`i(short[])` @7269); `t()` returns before its allocation for ax14 (@40-53), so runtime ax14s (`a(III)V`'s `ae` markers, `aW()`'s `ab`, `k.c(III)`'s `k.N`) keep `W == null`, and `p()` nulls it (@2). The port's fixed arrays carry that as the all-zero rect (`aX()`'s convention); `W.isEmpty()` never fired. No shipped ax14 record has a zero W. The faithful arm exposed a second divergence: `spawnPickup` (`a(III)V`), `setMarker`/`showPrompt` (`k.c(III)`) queued the marker into `npcs`, but neither method calls `k.b` (a(III)V @0-82, a(IIII)V @0-90, k.c(III) @0-96) — with v() true a released marker would keep drawing from the list. The insert is gone (`Level0World.kt:352/477/4369`). | proven | `v() u() keeps the aG==4 arm for ax13 only`; `v() sends ax78 S3 to the camera-vs-W arm`; `v() ax14 W==null is the zero rect — the rope ab marker is in play`; `a(III)V markers live only in ae — never in npcs, never drawn after G()`; `bb() S23 prop survives while v() holds — the inverse of the old score test`; `bl() S7 harrier with its Y off camera is removed even at au 0` |
| 5 | `sweepNeighborsB` (bc()) | bc() @0-865 (javap 48119-48522): per neighbor the type arms run in order, each with its own `i.a(…, X)`; ax54 @68-142 `ad != null && i.a(ad.W, X)` → hit + `goto 864` (no body test); ax32 @616-646 `l&1` then `S∈[21,27] && !cF → goto 864` BEFORE the overlap at @666; ax67 hit → @555 `goto 864`; ax24 hit → @604 `goto 864`; ax30 `i(9)` only on the kill branch (@260-289); `k.e(0,aw)` = k.e(II)V → `ap[0]++` | **divergence, fixed** (`Entity.kt:3735`): rewritten to the bytecode order. `ba()` ran a second copy (`projSweepBc`: double ax54 tally on `ad`+`W`, ax30 `i(9)` on a survivor, no END after ax67/ax24) and a second bd() copy (`projSweepBd`, wrong sweeper-is-ax23 arm @100-115); both deleted — ba() @693/@1367 and @738 call `sweepNeighborsB`/`sweepNeighbors`. `k.e(0,aw)` is now `countKill` (ap[0]); the old copy fed `statTally` (an unread counter). The duplicate `i.d(III)V` port `spawnDebris24` is gone (`spawnFloatie` is the `a(24,40,n,201)` one). A null `af` would NPE in the original (@45/@194/@341); the port skips that prop arm (inferred — no caller passes one). | proven | `bc() ax54 hit through its ad alone`; `bc() armed ax32 aborts the scan before any overlap`; `bc() ax67 and ax24 hits end the scan`; `bb() S16 sweep tallies kills in ap 0`; `ba() sweeps through the same bc() — one kill, no i(9) on a survivor` |
| 6 | `yOverlapsCam` / `flightAliveV` | B() @503-532: `v(); ifne 532; al > k.P+240 → k.l(12)` (javap 6981-6993); g.n() @17-46 `i.be → k.X=0; r() \|\| !v() → k.l(12)`; g.n() S2/S24 @2117-2160: `ag>>=1; ah>>=1; r6=r7=false; r() → @2155; v() → ifne 2262; @2155 k.l(12)` (g.javap 18334-18358) | **divergence, fixed**: all three call the full v() (`Entity.kt:1298`, `PlayerFsm.kt:2569/2620`) — `u()` and the `au > i → false` guard included. While there: the S2/S24 `!v()` exit is `k.l(12)` (falls through to @2155); the simple decompile wires it into L137 (the S3 glide arm), which the port had copied — fixed to the bytecode. | proven | `B() kills through the full v() — the au guard counts`; `g-n() S2 off camera fails the mission (bytecode 2152-2157)` |
| 7 | aV() S47 `k.aQ` | aV() @537-541 `aconst_null; putstatic k.aQ:Ljavax/microedition/lcdui/Image;` (javap 39983); class k has exactly one `aQ`: `public static Image aQ` (k.javap.txt:2850); readers: k.b(boolean) @5228-5253 (`drawImage(aQ, 198 - w, 5)`) and `i.a(IIIIZ)` @0-14 (re-create when null); writers: bQ() (ax35) @197/@361/@372, aV() S47 @538, aa() op37[17] @1715, D() @388 | **divergence, fixed — it matters (visual only)**: no gameplay state reads `k.aQ`; it is the ax35 eagle-view HUD inset the port already keeps as `volPaintRect` (drawn by `Level0Renderer.minimap`). S47 and aa() op37[17] cleared a write-only `kAQ: Entity?` duplicate (the old "i-typed claim owner" note was a decompile misread), so the inset was never dropped by them. Both now null `volPaintRect`; `kAQ` is deleted. S47 has no shipped record; op37[17] runs in level 2 (script uids 89, 321, 470) and level 6 (uid 154). | proven | `aV S47 clears the eagle-view inset k-aQ`; `aa() op37 sub-op 17 clears the eagle-view inset k-aQ` |

Every `Slice371Test` case (20) fails with the four main sources restored
to `145a1429` and passes on the new code (checked by checking out the old
`NpcFsm/Entity/Level0World/PlayerFsm.kt` over the new tests).

## Test-side edits (no capstone touched)

- `Slice364Test` ×2 — `wasHitRecently(w)` → `inPlayV(w)` (same
  assertions; the method is gone).
- `Level0WorldTest` `ax14 linked waits then counts down aC to removal`,
  `ax14 linked waits while target P&32 (held) or P&128` — setup
  `w.kAj = 1`: the countdown arm is aX()'s `k.bh[k.aj]==3` arm; on
  mission 0 it never ran in the original. Assertions unchanged.
- `Level0WorldTest` `kind-5 S28 spawner arms a 71 pickup pinned to view
  edge` — its last line asserted the `k.b` insert of the `aS.a(71,…)`
  pickup; rewritten to the bytecode (`a(III)V` has no `k.b`): the pickup
  is neither queued nor in `npcs` after the drain.
- `Slice65Test` stalk S66/S60 — the same-tick spawn+release marker is
  observed at the `spawnPickup` call through a `LevelCellSource`
  delegate (`SpawnSpy`) instead of the insert buffer; the "ax14 S66/S60
  marker spawned" assertions are unchanged.
- `Slice142Test` `S47 clears the claim slot every tick L219` — asserts
  `volPaintRect` (the real `k.aQ`) instead of the deleted `kAQ`.
- `Slice306Test.probeAtArm` (a print-only probe) — `wasHitRecently` →
  `inPlayV` in its `println`.

## Impact

`:core:test` 1760/1760 (1740 + 20), `:gdx:test` 18/18 — no capstone bot
needed a route or timing change (Slice245/281/282/288/289/291/297/303/
304/306/309 all pass as they were). Visible changes: the player's rope
`ab` marker (`aW()` anim 11) now draws (`k.b(boolean)` @2146 `ab.v()`
was always false); `ae` prompt markers animate once per tick (they were
stepped by both the entity loop and the draw pass) and no longer sit in
`npcs` (bh3 group counting, `k.q` scans); the eagle-view inset drops on
op37[17].

## Further divergences found (not fixed — follow-ups)

1. **`k.bd[]` is the draw list, the port scans `npcs`** (systemic):
   bc() @5/@11 (`getstatic k.be`, `k.bd`), bd(), and ~18 other methods
   (be, br, c(boolean), n(int,int), W, aj, az, bB, bC, bL, bQ, bj, bl, bu,
   bx, by, n) iterate the previous paint's `k.bd` — v()-gated, az-sorted,
   player and `ae` children included. The port iterates `npcs` in
   insertion order: membership and order differ, which matters for the
   first-hit loops (bc()'s END arms and the ax32 abort).
2. **`k.e(0,aw)` has three ports**: `countKill`/`kStatE` (→ `kAp[0]`, the
   k.e(II)V @0-21 `ap[0]++`) and `statTally` (→ `statTally0`, never
   read). Six kill sites still call `statTally` (`NpcFsm.kt:524, 669,
   695, 763, 846, 4240` — k.e(II)V calls in I() @2956/@3977/@6525/@6917/
   @7034 and bD() @1764, mapping inferred from context), so those kills
   never reach the win-stats `ap[0]`.
3. **bB() S28 @438-537**: with `aS.ae` set but `S != 71` the original
   does `aS.G()` + `aS.a(71, ak, al)` + pin + return; the port falls to
   the shove check (`NpcFsm.kt:3683`).
4. **`k.N` has two ports** (`marker`/`setMarker`/`clearMarker` and
   `kN`/`showPrompt`/`clearPrompt`) and neither is ever drawn: its tick
   (k.I() @810-1000 — `N.s()`, S54 press → `i(55)` + `E(32)`, S55 end →
   `k(-1)`, hover `P|=64` + `q()`) and its draw (k.b(boolean) @4521
   `N.F()`) are unported.
5. **`p()` vs `deactivate()`**: p() (@0-58) also nulls `ab` and `c` and
   calls `aS()`; the port's `deactivate()` keeps them.
6. Two identical u() ports remain (`recomputeAu` for k.I()'s direct
   `u()` calls, `offscreenScore` inside v()) — no divergence, a dedupe
   candidate.

## Gates

`python3 scripts/verify-static-reconstruction.py … reconstructed-project`
→ `ok: true`; `python3 -m unittest discover -s tests` → 57/57;
`:core:test` 1760/1760, `:gdx:test` 18/18; `:android:assembleDebug` OK.
