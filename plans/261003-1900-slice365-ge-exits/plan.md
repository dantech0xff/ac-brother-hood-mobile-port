---
title: "Slice 365 — g.e() arm exits: the post-tail runs only for arms that reach L353d; az() runs in the head; the J&4 block jumps to 14204"
phase: "port"
status: "done"
slice: 365
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt:2214-8868 (g.e(), offsets 0-15124)
  - reconstructed-project/bytecode/g.javap.txt:2508-2899 (az() at 617, the S tableswitch at 638)
  - reconstructed-project/bytecode/g.javap.txt:8173-8520 (13598 default arm, 13629 L353d, the J&4 block to 14348)
  - reconstructed-project/bytecode/g.javap.txt:8521-8868 (14349-15124 post-tail)
  - reconstructed-project/bytecode/i.javap.txt:26079-26103 (i.L())
  - reconstructed-project/src/simple/g.java:3461-3474, 3739-3747
  - reconstructed-project/src/structured/g.java:634-700
---

# Slice 365 — which `g.e()` arms reach the post-tail

Follow-up from [slice 360](../261003-1400-slice360-player-l-aw-posttail/plan.md):
"map g.e()'s ~70 early returns to the port's arms — the port runs postTail
after every arm."

## Method

A small script parsed `g.e()` from `g.javap.txt` into an instruction
graph (branch, `goto`, `tableswitch`/`lookupswitch`, `return` edges) and
walked every arm from its `tableswitch` entry (offset 638, cases 0..377,
default 13598) until it hit a `return` or an offset at or past 13598 (the
shared tail). The decompiled views were used only to read arm bodies:
both drop jumps around the tail (the structured view renders `return` and
`goto 13629` alike as `break`; the simple view drops the `goto 13629`
after `i(79)` at 6116 and the jumps of the 13662 block).

## Findings (proven, bytecode)

1. **Layout of the tail.** 13598 is the default arm (`r() && !j && !l()
   && !j → a(0)`) and falls into 13629 = L353d. L353d runs 13629-13659
   (`S!=9 && ab.S==14 → ab=null`), 13662-13711 (type-2 kill, `return`),
   13712-13736 (`aO==6 || aR==6 → a(18,0,0,this)`), 13739-14348 (the J&4
   mount/assassinate block and the `g.cm` latch) and falls into the
   post-tail at 14349 (equip, `aA` bookkeeping, the `A` latch with its
   `return` at 14473, the jump tail with the ax66 `return` at 14745, the
   flag consumers, `return` at 15124).
2. **No arm jumps past L353d.** Every tail exit of every arm is `goto
   13629` (or the default's fall-through). No arm enters 14349 or the
   middle of the post-tail directly.
3. **Counts.** 107 arm entry offsets plus the default:
   - 41 arms (60 states) always `return`;
   - 58 arms (90 states) always reach 13629;
   - 8 arms (15 states) do both — S270, S271, S12, S33, the combo arm
     (S67-69, S112-115), S147, S228/S358, S303;
   - the default sends 213 states to 13598 → 13629.
   The arms hold 65 `return` sites. All 165 explicit states have a port
   arm; the port's `else` is the default.
4. **The port ran `postTail` after every arm** (`dispatch(p, pad);
   postTail(p, pad)`): a `return` inside `dispatch` skipped only the port's
   copy of L353d. 23 always-returning arms and the S270/S271 return paths
   returned from `dispatch` but still ran the post-tail; 18
   always-returning arms (34 states) and the return paths of 6 mixed arms
   had no `return` at all and ran L353d and the post-tail. The table below
   lists each arm.
5. **`az()` is a head step.** Its only call site is `g.e()` offset 617-618
   (g.javap.txt:2508), after the `k.am`/`i.aN` checks and before the
   `S != 43 → g.i = true` line and the switch. The port ran it in its
   L353d copy, after the arm, so returning arms never refreshed `g`/`ci`/
   `i.at` and every arm read the previous tick's links.
6. **The J&4 block jumps, it does not return.** 13741-13755: `(J&4)==0 ||
   S==50 → goto 14204`; with `r98 == 0` that reaches 14322, `g.cm → cm=0;
   k.k() ? G() : U()`. The port's `mountEntry` returned instead, so `g.cm`
   stayed armed on every tick without the mount request.
7. **The type-2 kill (13662-13711) is not ported** — see follow-up F1.

## Exit map (g.javap.txt e(), proven)

"→13629 from N" lists the instructions whose jump (or fall-through) enters
L353d. "Port before" is the behaviour this slice fixes.

| Entry | States | Exit | Exit sites (bytecode offsets) | Note | Port before slice 365 |
|---:|---|---|---|---|---|
| 2164 | 375 | L353d | →13629 from 2203 | | ok |
| 2206 | 370 | L353d | →13629 from 2210, 2220 | | ok |
| 2223 | 371 | L353d | →13629 from 2223 | | ok |
| 2226 | 376 | L353d | →13629 from 2254 | | ok |
| 2257 | 377 | L353d | →13629 from 2273 | | ok |
| 2276 | 374 | L353d | →13629 from 2290, 2318, 2326 | | ok |
| 2329 | 284 | L353d | →13629 from 2333, 2347 | | ok |
| 2350 | 313 | return | return 2350 | | returned from `dispatch`, still ran postTail |
| 2351 | 6 | return | return 2403 | | ran L353d + postTail |
| 2404 | 317 | return | return 2619, 2624 | | ran L353d + postTail |
| 2625 | 286, 287 | return | return 2643, 2730 | | returned from `dispatch`, still ran postTail |
| 2731 | 295 | return | return 2806 | | ran L353d + postTail |
| 2807 | 304-306 | return | return 2831 | | ran L353d + postTail |
| 2832 | 291 | L353d | →13629 from 2866, 2873, 2885, 3149, 3191 | | ok |
| 3194 | 280 | return | return 3207 | | returned from `dispatch`, still ran postTail |
| 3208 | 267 | return | return 3422, 3432 | | returned from `dispatch`, still ran postTail |
| 3433 | 268 | return | return 3608, 3799, 3864 | | returned from `dispatch`, still ran postTail |
| 3865 | 269 | L353d | →13629 from 3873, 3880, 3892, 3917 | | ok |
| 3920 | 270 | both | return 3987; →13629 from 3935 | `g.g == null → i(0)` → L353d (3935); else return (3987) | return path still ran postTail |
| 3988 | 271 | both | return 4174, 4237; →13629 from 4003 | `g.g == null → i(0)` → L353d (4003); else returns (4174 `bl<0`, 4237) | return paths still ran postTail |
| 4238 | 54 | L353d | →13629 from 4242, 4265 | | ok |
| 4268 | 183 | L353d | →13629 from 4506, 4523, 4543 | falls into the 4372 arm | ok |
| 4372 | 184, 205 | L353d | →13629 from 4506, 4523, 4543 | | ok |
| 4546 | 122 | L353d | →13629 from 4550, 4562 | | ok |
| 4565 | 50, 241 | return | return 4688 | | ran L353d + postTail |
| 4689 | 5 | L353d | →13629 from 4793, 4800, 4827 | | ok |
| 4830 | 107-109 | L353d | →13629 from 4834, 4893 | | ok |
| 4896 | 209 | return | return 5132, 5142, 5148, 5154 | | returned from `dispatch`, still ran postTail |
| 5155 | 235, 238 | return | return 5481, 5520 | | ran L353d + postTail |
| 5521 | 237, 240 | return | return 5716 | | ran L353d + postTail |
| 5717 | 236, 239 | return | return 5779, 5787 | | ran L353d + postTail |
| 5788 | 74 | return | return 5823 | | returned from `dispatch`, still ran postTail |
| 5824 | 12 | both | return 5944; →13629 from 5978, 6002, 6026, 6065, 6086, 6116, 6311, 6340, 6357 | A() ladder → `i(74)` return (5944); every other exit → L353d (shares 6089/6092 with S0/S1) | ladder path ran L353d + postTail |
| 6089 | 0 | L353d | →13629 from 6116, 6311, 6340, 6357 | `i.O()` then the 6092 grounded arm | ok |
| 6092 | 1, 7, 11, 26, 79 | L353d | →13629 from 6116, 6311, 6340, 6357 | | ok |
| 6360 | 199 | L353d | →13629 from 6382, 6448, 6486, 6495, 6509 | | ok |
| 6512 | 32 | L353d | →13629 from 6538, 6576, 6607, 6621, 6630 | | ok |
| 6633 | 78, 80 | L353d | →13629 from 6637, 6655, 6663 | | ok |
| 6666 | 34 | L353d | →13629 from 6712, 6723 | | ok |
| 6726 | 92, 101 | L353d | →13629 from 6750, 6821, 6832 | | ok |
| 6835 | 33 | both | return 6921; →13629 from 7021, 7052, 7059, 7219 | A() ladder → `i(74)` return (6921); else → L353d | ladder path ran L353d + postTail |
| 7222 | 21, 233 | L353d | →13629 from 7362 | | ok |
| 7365 | 22 | L353d | →13629 from 8065, 8300 | falls into 7396/7400 (air family) | ok |
| 7396 | 18, 19, 23, 36 | L353d | →13629 from 8065, 8300 | `cv = 1` then 7400 | ok |
| 7400 | 20, 24, 25, 157, 215 | L353d | →13629 from 8065, 8300 | | ok |
| 8303 | 16, 35, 43, 150, 252 | L353d | →13629 from 8452, 8458, 8472, 8577, 9054, 9090, 9100, 9109, 9118, 9128, 9135, 9144 | | ok |
| 9147 | 29, 315 | L353d | →13629 from 9160, 9180, 9189, 9196, 9207 | | ok |
| 9210 | 28, 318 | L353d | →13629 from 9235, 9246 | | ok |
| 9249 | 60 | L353d | →13629 from 9309 | | ok |
| 9312 | 61, 203 | L353d | →13629 from 9593, 9602, 9619 | | ok |
| 9622 | 204 | return | return 9636 | | returned from `dispatch`, still ran postTail |
| 9637 | 63 | L353d | →13629 from 9641, 9650 | | ok |
| 9653 | 62 | L353d | →13629 from 9657, 9704 | | ok |
| 9707 | 89 | L353d | →13629 from 9737, 9751 | | ok |
| 9754 | 90 | L353d | →13629 from 9840, 9847, 9873, 9885 | | ok |
| 9888 | 297 | L353d | →13629 from 9888 | | ok |
| 9891 | 298 | L353d | →13629 from 9912, 9924, 9931 | | ok |
| 9934 | 8 | L353d | →13629 from 10023 | | ok |
| 10026 | 9, 10 | L353d | →13629 from 10199 | | ok |
| 10202 | 27 | L353d | →13629 from 10206, 10214 | | ok |
| 10217 | 67-69, 112-115 | both | return 10258; →13629 from 10480, 10608, 10616 | footing loss `!aZ && g.a==null → a(0)` return (10258); else → L353d | footing-loss path ran L353d + postTail |
| 10619 | 86 | L353d | →13629 from 10633 | falls into 10633 (`goto 13629`) | ok |
| 10633 | 82-85, 326 | L353d | →13629 from 10633 | | ok |
| 10636 | 17 | L353d | →13629 from 10650, 10686 | | ok |
| 10689 | 102 | L353d | →13629 from 10701, 10742, 10766, 10790, 10820, 10828, 10837 | | ok |
| 10840 | 332 | L353d | →13629 from 10914, 10930, 10977, 11029, 11064 | | ok |
| 11067 | 56 | L353d | →13629 from 11081, 11098, 11107 | | ok |
| 11110 | 59 | L353d | →13629 from 11110 | | ok |
| 11113 | 65 | L353d | →13629 from 11113 | | ok |
| 11116 | 282 | return | return 11129 | | returned from `dispatch`, still ran postTail |
| 11130 | 38 | L353d | →13629 from 11306, 11422, 11444, 11481, 11497, 11509, 11528 | | ok |
| 11531 | 37 | L353d | →13629 from 11586, 11678, 11725, 11732, 11741 | | ok |
| 11744 | 110 | L353d | →13629 from 11748, 11762 | | ok |
| 11765 | 165 | L353d | →13629 from 11776, 11784 | | ok |
| 11787 | 147 | both | return 11878; →13629 from 11811 | `r()` → level reset `k.a(true)` return (11878); else → L353d (11811) | reset path ran L353d + postTail |
| 11879 | 146 | L353d | →13629 from 11925, 11933, 11958 | | ok |
| 11961 | 148 | L353d | →13629 from 11972, 11982 | | ok |
| 11985 | 149 | L353d | →13629 from 12029, 12048 | | ok |
| 12051 | 156 | return | return 12097 | | returned from `dispatch`, still ran postTail |
| 12098 | 152 | return | return 12130 | | returned from `dispatch`, still ran postTail |
| 12131 | 164, 211 | L353d | →13629 from 12131 | | ok |
| 12134 | 225 | return | return 12134 | | returned from `dispatch`, still ran postTail |
| 12135 | 216 | return | return 12247 | | returned from `dispatch`, still ran postTail |
| 12248 | 217 | return | return 12311, 12380 | | returned from `dispatch`, still ran postTail |
| 12381 | 214 | return | return 12407 | | returned from `dispatch`, still ran postTail |
| 12408 | 49 | return | return 12443 | | returned from `dispatch`, still ran postTail |
| 12444 | 283 | return | return 12456 | | returned from `dispatch`, still ran postTail |
| 12457 | 91 | return | return 12489 | | returned from `dispatch`, still ran postTail |
| 12490 | 242, 243 | return | return 12569 | | ran L353d + postTail |
| 12570 | 250 | return | return 12570 | | returned from `dispatch`, still ran postTail |
| 12571 | 244 | return | return 12571 | | returned from `dispatch`, still ran postTail |
| 12572 | 228, 358 | both | return 12768; →13629 from 12765 | `S != 228` → L353d (12765), else return (12768): held S228 returns, S358/235/252 reach L353d | held S228 ran L353d + postTail |
| 12769 | 257 | return | return 12858 | | ran L353d + postTail |
| 12859 | 260, 262 | return | return 12978 | | returned from `dispatch`, still ran postTail |
| 12979 | 258 | return | return 12995, 13082, 13107 | | returned from `dispatch`, still ran postTail |
| 13108 | 259, 261 | return | return 13144, 13189 | | returned from `dispatch`, still ran postTail |
| 13190 | 263, 264 | L353d | →13629 from 13253, 13271 | | ok |
| 13274 | 265, 266 | L353d | →13629 from 13294, 13312 | | ok |
| 13315 | 272-275, 292 | return | return 13344, 13349 | | ran L353d + postTail |
| 13350 | 277, 293 | return | return 13354 | | ran L353d + postTail |
| 13355 | 294 | return | return 13355 | | ran L353d + postTail |
| 13356 | 303 | both | return 13406; →13629 from 13369 | `u(62430) → i(0)` → L353d (13369); else return (13406) | return path ran L353d + postTail |
| 13407 | 299-302 | return | return 13421 | | ran L353d + postTail |
| 13422 | 310 | return | return 13422 | | ran L353d + postTail |
| 13423 | 311, 312 | return | return 13450 | | ran L353d + postTail |
| 13451 | 360 | return | return 13547 | | ran L353d + postTail |
| 13548 | 357 | return | return 13597 | | ran L353d + postTail |
| 13598 | default (213 states) | L353d | falls into 13629 after `r() && !j && !l() && !j → a(0)` | | ok |

## Fix (`PlayerFsm.kt`)

- `dispatch` returns `Boolean`: `false` on every original `return` path,
  `true` after L353d; `tick` runs `postTail` only on `true`.
- New `return false` paths: S6 (2403), S317 (2619/2624), S295 (2806),
  S304-306 (2831), S50/S241 (4688), S235/S238 (5481/5520), S237/S240
  (5716), S236/S239 (5779/5787), S242/S243 (12569), S257 (12858),
  S272-275/S292 (13344/13349), S277/S293 (13354), S294 (13355), S299-302
  (13421), S310 (13422), S311/S312 (13450), S360 (13547), S357 (13597);
  the S12 ladder snap (5944), the S33 ladder snap (6921), the combo
  footing loss (10258, `comboArm` now returns `Boolean`), the S147 reset
  (11878), a held S228 (12768) and S303 without `u(62430)` (13406).
- `interactScan` (`az()`) moved from the L353d copy to the head, right
  before `dispatch` (grounded ticks only; `g.n()` has no `az()` call).
- `mountEntry`: `(J&4)==0 || S==50` skips only the scan (`mountScan`); the
  r98/`g.cm` tail always runs.
- The L353d copy keeps its old type-2 reading, now flagged in the code as
  a known divergence pointing at F1.

## Tests

`Slice365Test` (11):

- table test over S0..377 from a bare start (open air, no input, no
  links, no clip): the post-tail's `A` latch (14440-14473) marks the ticks
  that reached it; the always-returning states plus the bare-start return
  paths (combo footing loss, S147 reset, S303) must leave it untouched and
  every other state must take it;
- S6 holding UP skips L353d's `ab` release and the latch; the S12 and S33
  ladder snaps return; a held S228 returns while S358 reaches the
  post-tail (real clip0, mid-anim); S270 with a grab target returns;
- `az()` runs for a returning arm (S49 drops a dead `g`) and before the
  arm (S291 with a dead `g` takes the `k.u()` exit to S285);
- the J&4 block drains `g.cm` through L353d (S59, `J&4 == 0`);
- guards: S303 with `u(62430)` and S59's `ab` release still reach the
  tail.

Old code (PlayerFsm.kt from HEAD, restored after the run): 9 of 11 fail —
the table test lists 69 states that wrongly reached the post-tail; the two
guards pass.

Updated tests (both encoded a misread, fixed against the bytecode):

- `Slice151Test` "gcm latch stays when mount request absent" → "gcm latch
  drains when mount request absent" (13741 → 14204 → 14322).
- `Slice196Test` "S291 near end hands off on fire edge": the fixture's
  target gets `aB = 1` and the player's `al`, because `az()` now runs
  before the arm and drops a dead or distant `g`, as the original does.

No capstone bot test changed.

## Follow-ups (found during the audit, not fixed here)

- **F1 — the type-2 kill (13662-13711, proven).** `(aR==2 || aO==2 ||
  L()) && g.a==null → ah=aj=0; e(0); i(50); return`, then `aO==6 ||
  aR==6 → a(18,0,0,this)`. Shipped levels carry type-2 strips on top of
  floors (level 0: 127 cells, e.g. the whole floor of the '5'-strip
  corridor, row 38, cols 194-212; level 2: 361; level 3: 341; level 5:
  109; level 6: 182; level 7: 94). The port never kills there
  (`Slice187Test` "aO6 suppressed on type2 ground" encodes the misread).
  With the faithful kill, 12 capstone tests die on these strips: m0
  end-to-end (died at 3899,779 on the corridor floor), m0 door-exit → cp3
  and cp4 → cp5 (`Slice245Test`), m2 leg B (`Slice281Test`, S50 at
  2080,1351 on the level-2 row-67 strip) and m7 `Slice301/302/303/304/
  306/308/309/310Test`. Land it with its own bot re-routes (or
  faithful-dead-end verdicts).
- **F2 — S311/S312 (13423-13450).** 13425 is `invokevirtual #282
  i.a:(Z)V` = `a(true)`, the side rescan, not `g.a(int)`: the arm stays in
  S311/S312 until `r()`, then `ah=ag=0; l()`. The port enters S43
  (slice 277 read it as `g.a(1)`).
- **F3 — S217 (12332-12334).** Same `a(true)` misread: the port calls
  `flingAirborne(1)`.
- **F4 — `a(0)` ported as `i(0)`.** `invokevirtual #213 a:(I)V` (the S43
  fall: `i.a(43,32)`, `al+=10`, `ah=0`, `aj=1536`, `g.a=ac=null`) at
  S235/S238 5503-5505 and 5515-5517, S236/S239 5774-5776, S228 12746-12748
  (before `i(252)`) and S257 12853-12855; the port calls `setAnim(0)`.
- **F5 — combo arm (10217-10616).** The end calls `l()` (10598-10602),
  not `i(0)`; `R==112 && g.a.ax==51 && !r() → goto 13629` (10456-10480)
  skips the rest; `T==2 → k.A(10)` (10603-10613) runs on every tick, not
  only inside the `cl || r()` block.
- **F6 — S0 runs `i.O()` (6089)** before the 6092 grounded arm that
  S1/7/11/26/79 enter directly; the port's S0 skips it.
- **F7 — grounded arm (6092-6357).** `aO>12 && aR>12 → i(79); goto 13629`
  (6092-6116) ends the arm, the port runs on; after `a(257,8)` (6261) the
  arm continues into `am()`, `y()` and `l()` (6270-6357), the port returns
  from `groundedTail`; the `am()` path clears `ab` (6306).
- **F8 — S5 (4689-4827).** `i.O(); bM = null` at the head, `ab = null` on
  the jump and `r()` paths, and the jump press does not end the arm
  (4764-4775 falls into the `u(94324)`/`r()` checks).
- **F9 — e() head.** `S != 43 → g.i = true` (621-631) is missing, so the
  port's `iFlag` never re-arms after S295 clears it; the `k.am` block
  (533-595) and the `i.aN.P()` clear (598-614) are not in the port's head.
- **F10 — J&4 press paths.** 13998-14016 and 14140-14158 fire on
  `v(65568) || (!k.k() && V())`; the port takes only `v(65568)`.
- **F11 — S199 (6386-6410).** `y() && ag != 0 → ag = 0; a(S, 4)`: the port
  skips the `a(S,4)` mask-4 snap.

F2-F11 are resolved in
[slice 369](../261003-2300-slice369-player-ge-followups/plan.md) (F11 ported;
not observable after the head's `a(an())`); F1 in
[slice 370](../261004-0800-slice370-type2-kill/plan.md).

## Gates

On `claude/slice365-ge-exits` (from `claude/phase2-faithful-ai` @
2ec62f28): verifier `ok:true`; `python3 -m unittest discover -s tests`
57/57; `:core:test` 1700/1700 (1689 + 11); `:gdx:test` 9/9;
`:android:assembleDebug` OK.
