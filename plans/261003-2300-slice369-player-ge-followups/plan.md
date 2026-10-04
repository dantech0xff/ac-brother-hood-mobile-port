---
title: "Slice 369 — g.e() follow-ups F2-F13: a(true) vs a(0), the combo/grounded/S5/S21 arms, the e() head and the J&4 press"
phase: "port"
status: "done"
slice: 369
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt:2214-2290 (e() 0-163: k.C return, g.v cheat, k.l(), g.r return)
  - reconstructed-project/bytecode/g.javap.txt:2476-2515 (e() 533-631: k.am block, i.aN.P() clear, az(), g.i)
  - reconstructed-project/bytecode/g.javap.txt:4061-4127 (e() 4689-4827: S5)
  - reconstructed-project/bytecode/g.javap.txt:4275-4551 (e() 5155-5787: S235-S240)
  - reconstructed-project/bytecode/g.javap.txt:4704-4844 (e() 6089-6357: S0 i.O() and the grounded arm)
  - reconstructed-project/bytecode/g.javap.txt:4845-4911 (e() 6360-6509: S199)
  - reconstructed-project/bytecode/g.javap.txt:4912-4968 (e() 6512-6630: S32, not fixed)
  - reconstructed-project/bytecode/g.javap.txt:5273-5330 (e() 7222-7362: S21/S233)
  - reconstructed-project/bytecode/g.javap.txt:6629-6798 (e() 10217-10616: combo arm)
  - reconstructed-project/bytecode/g.javap.txt:7540-7608 (e() 12248-12380: S217)
  - reconstructed-project/bytecode/g.javap.txt:7707-7833 (e() 12572-12858: S228/S358, S257)
  - reconstructed-project/bytecode/g.javap.txt:8085-8100 (e() 13423-13450: S311/S312)
  - reconstructed-project/bytecode/g.javap.txt:8240-8520 (e() 13739-14348: J&4 block)
  - reconstructed-project/bytecode/i.javap.txt:2622-2761 (i(int): same-S call keeps T)
  - reconstructed-project/bytecode/i.javap.txt:4979-5040 (i.a(int,int) masks 1/2048/4096/4)
  - reconstructed-project/bytecode/i.javap.txt:5534-5566 (x(): aZ = 0, bd = ah != 0)
  - reconstructed-project/bytecode/i.javap.txt:5898-6303 (i.a(boolean): t = box centre at 689-710, single return)
  - reconstructed-project/bytecode/i.javap.txt:20030-20063 (I() 1644-1700: ax11 k.M claim, not fixed)
  - reconstructed-project/bytecode/i.javap.txt:24547 (aj() 110: ax4 reads k.M)
  - reconstructed-project/bytecode/i.javap.txt:32603 (aJ() 11: ax73 reads k.M)
  - reconstructed-project/bytecode/k.javap.txt:4943-4976 (k.l() writes k.M)
  - reconstructed-project/bytecode/k.javap.txt:30583 (k.ah() 155 allocates k.M)
---

# Slice 369 — the `g.e()` follow-ups of slice 365

[Slice 365](../261003-1900-slice365-ge-exits/plan.md) mapped `g.e()`'s exits
and left follow-ups F1-F11. This slice fixes F2-F11 plus four items added
during the work: F12 (the S21/S233 arm) and F13a-c (the `e()` head before
the `k.E` follow). F1 (the type-2 kill) is slice 370.

## Method

Every item was re-read in `g.javap.txt` (the decompiled views were used only
to read arm bodies). Each fix got a test in `Slice369Test`; while the fixes
were made, each item's old code path stayed behind a temporary switch, so the
tests could be run with the slice-368 path put back item by item: 34 of 35
fail on the old paths, the F11 test is the documented exception. The same
switches bisected the capstone breaks to items before the bots were re-routed.
The switches are gone from the committed code.

## Items

`e()` offsets are g.javap.txt `e()`. "Port before" is the slice-368 head.

| Item | Bytecode (proven) | Port before | Verdict | Tests (`Slice369Test` unless named) |
|---|---|---|---|---|
| F2 S311/S312 | 13423-13450: 13425 `invokevirtual #282 i.a:(Z)V` = `a(true)` (side rescan); `r() → ah=ag=0; l()`; `return` 13450 | `enterFall(1)` (read as `g.a(1)`, S43); `l()` treated as dead | fixed | `F2 S311 rescans with a(true) and holds until r()`, `F2 S312 at r() runs l()` |
| F3 S217 | 12329-12334: `bd = 1; a(true)` (#282) | `flingAirborne(1)` (S43, `al += 10`) | fixed | `F3 S217 on footing rescans instead of flinging`; `Slice196Test` `S217 on aZ support rescans with a(true) and clears P64` (corrected) |
| F4 `a(0)` | `invokevirtual #213 g.a:(I)V` at S235/S238 5503-5505 and 5515-5517, S236/S239 5774-5776, S228 12746-12748 (before `i(252)`), S257 12853-12855 | `setAnim(0)` | fixed (`flingAirborne(0)` = `i.a(43,32)`, `al += 10`, `ah = 0`, `aj = 1536`, links dropped) | `F4 S235 unbound end falls through a(0)`, `F4 S236 above its crate releases through a(0)`, `F4 S228 end falls through a(0) before i(252)`, `F4 S257 drop ends in a(0)`; `Slice189Test` `push entry unbound dives`, `push hold releases above crate` (corrected) |
| F5 combo arm | 10217-10616: footing loss is `a(0)` (#213, 10240-10258) then `return`; `z = 0` (10259); `aj()` + `k.E.K()` run on every tick, not only on a tap (10420-10433), and `aj()` tries `ck` only when `cj` fails (aj() 0-24); `R==112` on an ax51 `g.a` with `!r()` → `goto 13629` (10450-10480); the end is `l()` (10598-10602), not `i(0)`; `T==2 → k.A(10)` on every tick that gets there (10603-10613) | tap-only `aj()` running both tables, `enterFall()` footing loss, `i(0)` end, sfx only inside `cl \|\| r()` | fixed | `F5 combo end with nothing queued runs l()`, `F5 T2 combo sfx plays mid-anim`, `F5 a queued R fires at the window without a tap`, `F5 aj tries ck only when cj fails`, `F5 R112 on a crate holds until r()`, `F5 combo footing loss is the masked a(0)` |
| F6 S0 | 6089 `i.O()` then falls into the 6092 arm; S1/7/11/26/79 enter at 6092 | S0 skipped `i.O()` | fixed | `F6 S0 runs i_O, S1 does not` |
| F7 grounded arm | 6092-6116 `aO>12 && aR>12 → i(79); goto 13629` (no `l()`); 6119-6267 S79 only: `ag=ah=0`, `u\|v(33024)` → `al+=20; x(); al-=20` (probes kept) and `a(257,8)` when `aQ∈{20,5} && aR==0 && e(two over, one down) < 12`, then the arm goes on; 6270 S11 decay; 6291-6311 `u(33024) && am() → ab = null; goto`; 6314-6333 `y() → ag=1; a(true); ag=0`; 6336-6357 `!l() → ab = null; cq = 0; a(0)` | `i(79)` re-set but `l()` ran on; a shared `ledgeDrop257` vaulted any grounded state and returned; the `am()`/`!l()` exits kept `ab` | fixed | `F7 embedded grounded arm ends at i(79) without l()`, `F7 the S79 vault runs on into l() and falls`, `F7 a running S12 DOWN press slides instead of vaulting`, `F7 the airborne l() exit drops ab` |
| F8 S5 | 4689-4827: `i.O(); bM = null`; jump press `v(16388\|2\|8)`: `v(2) → av=true` else `v(8) → av=false`, `i(21); ab = null` and on (no return); `u(94324) → ab = null; l(); goto`; `r() → ab = null; i(aO>12 ? 79 : 0)` | jump press returned in S21; no `i.O()`/`bM`/`ab` clears; both tap edges applied | fixed | `F8 S5 runs i_O and drops bM`, `F8 the S5 jump press runs on into l() and the post-tail`, `F8 S5 tap edges pick the facing with else-if` |
| F9 e() head | 533-595 `k.am && S∉{183,184,311} → k.p(); i.O(); aN → aB=0; i.d(aN); aN=null`; 598-614 `aN != null && aN.P() → aN = null`; 621-631 `S != 43 → g.i = 1` | none of the three | fixed | `F9 g_i re-arms on every non-S43 tick`, `F9 k_am is released at the head outside the finishers`, `F9 a dead lock target is dropped at the head` |
| F10 J&4 block | press is `v(65568) \|\| (!k.k() && V())` at 13998-14016 and 14140-14158; also: the Z[0]==4 band `\|ak - at.ak\| < (W[2]-W[0])<<1 → r2 = 0` is live (13889-13962); the `k.k()` r98 tail is `T() → U()` then always `a(8, ak, al-85)` (14262-14312); 14315 is `putstatic #46 g.cm`, not `k.cm` | `v(65568)` only; band read as dead; `!T() → U() + a(8,…)`; wrote `k.cm = 1` (the touch-pad flag) | fixed | `F10 a touch on the hand fires the J4 lunge`, `F10 the Z4 cart mount needs a run-up`, `F10 the touch-pad r98 tail replaces a showing hand`, `F10 the r98 tail sets g_cm, not k_cm`; `Level0WorldTest` r98 test (corrected: `g.cm`, `k.cm` untouched) |
| F11 S199 | 6385-6410 `y() && ag != 0 → ag = 0; a(S, 4)` | `a(S,4)` skipped | ported; **not an observable divergence**: mask 4 does `ak += t - (W[0]+W[2])/2` (i.a(II) 58-95); `i(S)` with the same S keeps `T` (i(int) 236-268) and `t` is the box centre the head's `a(an())` wrote (i.a(Z) 689-710, its only `return` is at 732), with nothing moving `ak` in between | `F11 the S199 wall-stop snap is a no-op after the head rescan` (passes on the old path too, by design) |
| F12 S21/S233 | 7222-7362: `ag=ah=0`; `r() \|\| S==233 → i(22)`, `ah = g.a?.ax==51 ? -2560 : -5120`, `ag = (g.a?.ax==43 && g.a.ab()) ? ±1024 : ±2048`; `i.f(this)`; `goto 13629`. `g.a` is the port's `standingOn` | S233 waited for `r()`; no crate half-jump, no ax43 push | fixed | `F12 S233 jumps at once`, `F12 the crate half-jump and the ax43 claim push` |
| F13a | 0-19 `k.C != null && (k.aS.P & 512) == 0 → return`. `i.I()`'s L108 gate skips the whole tick only while `k.C.ab()` holds; this return also covers a bound but paused claimer, after the integrate | missing | fixed (`eHeadReturns`, run after the integrate, before the port's pre-dispatch `a(true)`) | `F13a a bound k_C stops e() at its head` |
| F13b | 154 `k.l()` writes the static `k.M = [av ? ak-60 : ak, al-60, +60, al]` (k.javap.txt:4943-4976; allocated zeroed by `k.ah()`); read by ax4 `aj()` 110 and ax73 `aJ()` 11 (and ax11 `I()` 1657, unported — see below) | each NPC site rebuilt the box from the live player | fixed: `LevelCellSource.kM` / `Level0World.kM`, written in `eHeadReturns`; NpcFsm `ctxZone()` and `reachRect73()` read it | `F13b k_l writes k_M at the e() head`, `F13b the ax4 claim reads k_M, not the live player`; `Level0WorldTest` (2) and `Slice73Test` (2) call `eHeadReturns` before the NPC tick |
| F13c | 157-163 `g.r → return` (the aP S7 grab-QTE frames), after `k.l()` | missing | fixed | `F13c g_r freezes e() after k_l()` |

Also fixed with F13a: `Level0World.cEntity` (the `k.C` that `g.ao()` reads,
g.javap.txt ao() 37-57, the same `#172 k.C`) was its own never-written field;
it now aliases `kC`.

### NpcFsm edits (minimal)

Two sites only, both F13b: `ctxZone()` (ax4 `aj()` claim bubble) copies
`world.kM`, and `reachRect73(w)` (ax73 `aJ()` head) returns `w.kM`.

### Stale comments corrected

- `aw()` header "climb/drop hooks omitted" → every arm of g.javap.txt aw()
  0-386 is ported (airborne fall, S79 overhead/UP, DOWN vault/crouch, UP
  carry, `j.g % 100` flicker, settle).
- `mountEntry` kdoc "L2048's o()→ao() tail is a debug/skip arm, unported" →
  the `o()→ao()` cycle at 14349 is the post-tail's first step
  (`groundOrVehicle() → cycleEquip()`); the kdoc now also states the press
  and the `g.cm` write as above.

### Unit tests corrected to the bytecode

`Slice129Test` (2, real clip mid-anim; the clamp test's lock target alive,
598-614), `Slice135Test` (2, the drag victim alive), `Slice189Test` (3: S228
mid-anim, S235/S236 end in S43), `Slice196Test` (S217, and `bd` as `x()`
rewrites it), `Slice218Test` (stand on the crate's open east side: west of it
the player was inside the x1400 block, where the grounded arm now ends at
`i(79)`), `Level0WorldTest` (3), `Slice73Test` (2, plus a claim assertion).

## Capstone re-routes (inputs only)

No enemy, door, timer or state was touched; every change is a route or
timing change in the bot.

| Test | Broke because | Re-route |
|---|---|---|
| `Slice245Test` x1400 wall, corridor | F8/F12: a held UP bunny-hops the y779 roof a tick per landing; the last hop overshoots the east edge | roof run with RIGHT alone, take-off at its east edge (x≥988) |
| `Slice245Test` mission-0 end to end | F8: S5 runs `l()` under a held direction (pit floor west hops, '5' top past the divider); F4: the S257 drop-through falls with the '5' strip at the head → `cw && aO==5 → i(280)` | pit floor: LEFT\|UP while facing west; '5' top east of the divider: walk east, steps to the x4707 ledge, hop into the S313 catch; under the strip: DOWN (S38's drop) instead of mantling back up |
| `Slice245Test` fence roof | timing: the hop into the ax10 launch now lands the ax22 catch (9874,514) when door uid211 is in S3T1 — its box overlaps the stale S65 box (NpcFsm ax44 S3 arm) | hold still through the last combo on the y359 ledge end and step off while uid211 rests in S0 (arrival then in its S1T2) |
| `Slice281Test` m2 leg B | F8: the landing off the S89 pin turns east under RIGHT+UP | LEFT+UP on that landing (x1960-2010, y1290-1305) |
| `Slice282Test` m3 leg A | F8: the y519 ledge hops turned east; then the y339 hops landed on the ax4 crates aw641/aw829 and the push-out dropped him west of them | LEFT+UP on the y519 ledge (x1100-1360); attack crates within reach on the ground |
| `Slice282Test` m3 leg H | F7: the landing on the raised block's top (head and feet cells solid) ends in S79, the y819 duel drifts west and `descend` never latches | latch `east` on the pit floor itself (x12690-12800, y≥1250) |
| `Slice289Test` m5 leg C2 | F8: the jump off the 40px base step carries the run's 10px and meets the tower face below its top | walk to the step's west end, RIGHT tap to turn (no step), UP alone |
| `Slice289Test` m5 leg I | F5: with the attack held the combo end's `l()` lets the post-tail start the next S67 the same tick; the tighter chain loses to the lip guards | release the attack on the S69 last frame (T4) |
| `Slice291Test` m6 legs B, C | F7: the S26 edge walk ends in S79 under the y699 lip, he lands ~6 ticks sooner, guard uid99 gives up the chase and never knocks him off heavy guard uid112, whose `f()` holds `cq` off (no jump) | attack uid112 (ax73) when it stands within 40px ahead on the floor |
| `Slice291Test` m6 leg D | F8: after door uid166's S284/S285 transfer the waypoint was still (8960,760) — LEFT; S5's old squat had kept the east hop by luck | once past x9400 aim for the uid311 point and a new east waypoint (9960,400) out of the ax35 volley band |
| `Slice291Test` m6 leg F | F8: on the hollow block top the run-carried hop overshoots onto the y519 shelf | UP alone on the block top (x10560-10699, y419) |
| `Slice303Test` m7 upper wing | F7: `a(257,8)` from the grounded arm is S79-only, and from S0 a held LEFT wins in `l()` | DOWN alone in S0 too at x620-1100 (l() → aw()'s DOWN arm vaults) |

## Further divergences found (not fixed here)

- **S32 arm (e() 6512-6630, proven; fixed in slice 372).** The port has only `i.f(this)` and
  `r() → ag = 0; i(Q==79 ? 79 : 0)`. Missing: `!aZ && g.a == null → cq = 0;
  a(0); goto` (6516-6538), `ag != 0 → a(true)` (6541-6550), `y() && ag != 0 →
  ag = 0` (6553-6569), and after `r()`: `S == 0 → x(); aO > 12 → i(79)`
  (6603-6627).
- **ax11 `I()` claim (i.javap.txt I() 1644-1700, proven).** `!aH() &&
  a(W, k.M) → k.a(this, 0, W)`, else `k.L.aw == aw → k.m()`. The port's
  soldier family never claims; with `k.M` now on the world it can read it.
- **F1, the type-2 kill (13662-13711)** — slice 370.
- `Slice245Test` fence roof: its death branch sets the respawn position
  before the level reload, which then puts the player at the level start;
  the branch only runs if the route dies (it no longer does).

## Gates

On `claude/slice369-370-player-fsm` (from 145a1429): `:core:test` 1775/1775,
`:gdx:test` 18/18, `:android:assembleDebug` OK, verifier `ok: true`,
`python3 -m unittest discover -s tests` 57/57.
