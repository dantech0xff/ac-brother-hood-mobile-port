---
phase: 1
title: Parity-Gap Closure
status: done
priority: P1
dependencies: []
---

# Phase 1: Parity-Gap Closure

## Context Links

- [Plan](./plan.md) · [Baseline audit §3](./reports/baseline-audit.md)
- Dispatch/FSM mining: `docs/gameplay-mining/npc-fsm.md`,
  `docs/gameplay-mining/state-animation-map.md`, `docs/i-av-reconstruction.md`
- Original views: `reconstructed-project/src/{simple,structured}/`, bytecode
  `reconstructed-project/bytecode/{g,i,j,k}.javap.txt`

## Overview

Fix the proven divergences G1–G6 from the baseline audit, one slice each
(slice numbers continue after 345), and give every G7 deviation an explicit
verdict. Each fix cites the original `file:line`, carries a confidence label,
and lands with unit tests that would have caught it.

## Progress

| Item | State | Slice |
|---|---|---|
| 1.1 ax47/ax50 FSM swap (G1) | done | [346](../261003-0723-slice346-ax47-ax50-fsm-pairing/plan.md) |
| G9 ax46 S5/S6 re-pin missing (found re-validating 346) | done | [347](../261003-0730-slice347-ax46-fire-cycle-repin/plan.md) |
| 1.6b `l()` containment + full sight test for ax17/23/50 (G8, found in 346) | done | [350](../261003-0816-slice350-l-sight-containment/plan.md) |
| 1.2 `applyHit` op 40 (G2) | done | [348](../261003-0747-slice348-applyhit-op40/plan.md) |
| 1.3 player S18 arm (G3) | done | [349](../261003-0804-slice349-player-s18-s371/plan.md) |
| 1.4 player S371 arm (G4) | done | [349](../261003-0804-slice349-player-s18-s371/plan.md) |
| 1.6 jC=28 ghost rows (G6) | done | [351](../261003-0827-slice351-ae-121-no-panel/plan.md) |
| 1.7 `Trig` tables (G7 → high impact: `j.b` is cos) | done | [352](../261003-0900-slice352-trig-tables-verbatim/plan.md) |
| G10 volley `i.a(int,boolean)` fan/speed/anim (found auditing `j.b` sites) | done | [353](../261003-0930-slice353-volley-fan-speed-anim/plan.md) |
| G11 pickup sparkle field in `i.F()` (RNG gate, rays, `f` advance) | done | [354](../261003-1000-slice354-pickup-sparkle-field/plan.md) |
| 1.7 ax37 scroll triggers as entities (+ `k.c` holder release / same-frame skip) | done | [355](../261003-1030-slice355-ax37-triggers-as-entities/plan.md) |
| G12 frame order: `k.I()` ticks `bb[]` before `aS.I()` (port: player first) | done (Phase 2 branch) | [G12](../261003-1530-g12-frame-order/plan.md) |
| 1.7 ax23 `case 23 → L849` + stale comments; G13 family head (`t()`, corpse landing, `g.h` → S203 ledge kill), ax73 `au()` | done | [356](../261003-1100-slice356-family-head-ax23/plan.md) |
| 1.5 ax11 `aC()` scheduler (G5) | done (Phase 2 branch; bots re-validated) | [357](../261003-1200-slice357-ax11-ac-scheduler/plan.md) |
| `v()` overlaps are `i.a` (strict) + one `aG()` (split out of 357) | done | [358](../261003-1230-slice358-v-strict-single-aG/plan.md) |
| ax11 patrol arm (`case 2/3/92`): turn test, crate-link skip, `am()`, one `aD()` | done (Phase 2 branch) | [359](../261003-1300-slice359-ax11-patrol-arm/plan.md) |
| `g.l()`/`g.aw()`/`e()` `aA` head + post-tail order; one `k.bD` (found in Phase 2) | done (Phase 2 branch) | [360](../261003-1400-slice360-player-l-aw-posttail/plan.md) |
| `setAnim` is `i.i(int)`; `enterStateMasked` x snaps are `i.a(int,int)`'s chain (found in Phase 2) | done (Phase 2 branch) | [361](../261003-1500-slice361-setanim-i-i/plan.md) |
| ax44 door `bv()`: last frame's box, mode-1 polarity, resolve-tick promotion, `i.a`, crush arms (found in Phase 2) | done (Phase 2 branch) | [362](../261003-1600-slice362-ax44-door-bv/plan.md) |
| below-the-camera kill is `i.B()`'s (flying player only) (found in Phase 2) | done (Phase 2 branch) | [363](../261003-1700-slice363-below-camera-kill-flying-only/plan.md) |
| every `i.a(int[],int[])` port rejects point boxes (140 sites) (found in Phase 2; follow-ups closed by 371) | done (Phase 2 branch) | [364](../261003-1800-slice364-overlap-ia/plan.md) |
| `g.e()` arm exits: post-tail only after L353d, `az()` in the head (found in Phase 2; follow-ups F1-F11 open) | done (Phase 2 branch) | [365](../261003-1900-slice365-ge-exits/plan.md) |
| `g.e()` follow-ups F2-F13: `a(true)` vs `a(0)`, combo/grounded/S5/S21 arms, the `e()` head (`k.C`/`g.r` returns, `k.am` release), the J&4 press (found in Phase 2) | done (Phase 2 branch) | [369](../261003-2300-slice369-player-ge-followups/plan.md) |
| soldier melee gate reads `i.ab()` (running claim suppresses `aB()`) (found in Phase 3) | done (Phase 2 branch) | [367](../261003-2200-slice367-claim-ab-melee-gate/plan.md) |
| soft keys: claim SKIP pill hit-test (m0 intro skippable), `ce/cf` wheel margins, pause icon on release (found in Phase 3) | done (Phase 2 branch) | [368](../261003-2230-slice368-soft-keys-back/plan.md) |
| slice-364 follow-ups: one `v()` port (`u()` ax13 gate, `bh[aj]==3`, ax78 S3, ax14 `W==null`; runtime ax14 markers leave `npcs`), one `bc()` (ad-only ax54, ax32 abort first, ax67/ax24 END), aV() S50 else-arms, l() S117, S47/op37[17] clear `k.aQ` = `volPaintRect`, g.n() S2/S24 `!v()` → `l(12)` (found in Phase 2; follow-ups open) | done (Phase 2 branch) | [371](../261003-2300-slice371-npc-followups/plan.md) |
| draw-pass state steps once per frame in the world: `i.ad()` bubbles (one call site), row band, jc20 `eZ` (found in Phase 4 renderer audit) | done (Phase 2 branch) | [373](../261004-0010-slice373-draw-pass-state/plan.md) |
| dialog typewriter + the jC 21 frame's `b(false)` `F()` pass in the world; renderer drops duplicated `F()` writes | done (Phase 2 branch) | [374](../261004-0030-slice374-dialog-frame-pass/plan.md) |
| `b(true)` runs the draw-list build + `F()` pass behind pause/death/end screens, not only its head | done (Phase 2 branch) | [375](../261004-0100-slice375-btrue-world-pass/plan.md) |
| `j.t` is the jc12/13/31 frame-skip latch (veil latch bit 0, `K()` bit 4), not an input lock; `b(z2)` is one pass (head, `F()`, bubbles, gated `c()`, SKIP pill, un-gated tail) on play, dialog and `b(true)` frames; no black veil frame | done (Phase 2 branch) | [376](../261004-0130-slice376-jt-skip-latch/plan.md) |
| the restart `a(z2)` step by step: `ap[1]` counts `a(true)` retries (`o(1)`), checkpoint arm keeps `ap[1]`/`dg` and reads `ap` from `bA`, `g.e(ax)` full meter, `aA` alert carry on a fresh stance, `i.bW/bX/bV` clears, `F(aj)`/`q()`/`T()`/`l(8)`/`B()`; one `k.bG`; player `az=100` | done (Phase 2 branch) | [377](../261004-0200-slice377-reload-a-z2/plan.md) |
| `b()`'s `i.bQ` white/red flash (stepped once per pass) and `i.ce` white backdrop under the entities | done (Phase 2 branch) | [378](../261004-0230-slice378-bq-flash-ce-backdrop/plan.md) |
| per-frame UI state steps in the world: claim cards + `cd[8]` banner (now drawn), `fS` marquee on play frames, pause icon, load-screen `dl` + typewriter, `an` black frame for a whole tick | done (Phase 2 branch) | [381](../261004-0300-slice381-ui-steps-in-world/plan.md) |
| one `k.e(0,aw)` (`countKill`, six NPC kill sites fed a dead counter) and one `k.o(n)` (`kCount`, mission-7 guard on every `o(3)`) | done (Phase 2 branch) | [382](../261004-0330-slice382-kill-tally/plan.md) |
| case 8/21 is one body: the world runs under u8 tips, `H()` behind flying-mission dialogs, the dialog switch on its opening frame, dialog time counts | done (Phase 2 branch) | [380](../261004-0400-slice380-case-8-21-body/plan.md) |
| no knockout check in `k.I()`: the death screen opens at the end of the S50 (ground) / S2 (flight) death anim | done (Phase 2 branch) | [379](../261004-0430-slice379-death-anim/plan.md) |
| `k.N` tap prompt: one object, ticked in `k.I()` (a tap fires the context press — the touch assassination), drawn, outlives a reload | done (Phase 2 branch) | [383](../261004-0500-slice383-tap-prompt/plan.md) |
| `i.s()` gates: anims freeze on the pause screen (except `A[3]`); a wrap under a non-u8 dialog stops/settles the player and freezes the entity while a claim is bound | done (Phase 2 branch) | [386](../261004-0530-slice386-anim-advance-gates/plan.md) |
| slice-371 leftovers: `bB()` S28 replaces a foreign `ae` with a pinned 71, one `i.p()` (`ab`/`c`/`cr` released), one `u()` | done (Phase 2 branch) | [384](../261004-0600-slice384-npc-leftovers/plan.md) |
| sim neighbour scans walk `k.bd` (the last paint's list: on-screen, player, `ae`), not `bb[]`; renderer no longer rebuilds it; `W()` nulls it; per-method bytecode fixes (`n()` ax11 arm + self-match, `be()`/`c(Z)`/`bu()` first-hit ends, `bl()` cz, `br()` z2, `bQ()` S12) | done (Phase 2 branch) | [385](../261004-0630-slice385-bd-scans/plan.md) |
| `k.c()` runs `p()` at once on pool members; `bt()` kills the ALIVE hostiles (the port skipped them); `bu()` S10 falls into the S6/S8 body (`z2` settle); `b()` ax21 S1 `ad.s()` unconditional | done (Phase 2 branch) | [387](../261004-0700-slice387-removal-sweeps/plan.md) |
| the type-2 kill + type-6 hit at the head of `g.e()`'s shared tail (F1 of slice 365); `g.c(i)` ax72 orbit setup; capstone re-routes and three faithful-dead-end verdicts | done (Phase 2 branch) | [370](../261004-0800-slice370-type2-kill/plan.md) |
| the player slot's pre-dispatch `a(true)` removed (no counterpart in `i.I()`/`g.e()` — every `a(Z)` site listed); the S32 run-start arm (6512-6630); `x()` keeps `bd` on a `5` cell; three capstone re-routes | done (Phase 2 branch) | [372](../261004-0900-slice372-no-prerescan/plan.md) |
| `g.az()` / `g.i(i)` audited against the bytecode: `S==250`, the S270/271 `g` guard, S267/291 rebind, ax4 exemption, ax11 `Z[19]` dx==0, the NPC-path LOS test, `k.bd` domain, the `i(i)` offer window / S267 / S303; tower + m6 leg E re-routes | done (Phase 2 branch) | [388](../261004-1000-slice388-az-audit/plan.md) |
| the entity constructor `i(short[])` diffed field by field over all 3 800 shipped records: ax4 (`k.aq` wisp total, `i=2`, phantom S7 block), ax6/8/19/24 `az`, ax9 S34, ax13 `bN=1`, ax14 P&32 hide, ax21 `i(1)`, ax37 `S=-1`, **ax42 fuse armed from spawn**, ax54/30 `Z[9]` chain + ax68 child, ax60 `Z[4]==2` hide, the real `E()` tail for ax11/73 and the player, the rope flip writes the rope's `av` | done (Phase 2 branch) | [389](../261004-1100-slice389-init-audit/plan.md) |
| per-record clip tables (`k.bk/bj/bl/bm/bn`), the nine pack-3 clips (decor 24/34/37/41/65/67/69, ax7 mouth 66, ax9 72), **seven converted clips the app never loaded** (4/19/27/35/36/40/71), loader/test parity guard; slice 215's wedge verdict superseded | done (Phase 2 branch) | [390](../261004-1200-slice390-clip-tables/plan.md) |
| which sites run `E()` and which only `t()`: the sinking `settleToGround` stub deleted (19 original `E()` sites mapped; `a(IIII)`/`p(II)`/`g(I)`/`b(Z)`/the `bG()` knives/boss arena clamp end `t()` / `a(1);t()`); `bD()` finale (always `aS.t()` + return, random-point floatie, `k.l(15)` reachable from the off-play branch); `bG()` S18 knife range `j.c(30·j.m/360)` (`j.m = 256`, not 0) and the `j==3` block | done (Phase 2 branch) | [391](../261004-1300-slice391-settle-sites/plan.md) |
| `ba()` — the ax24 projectile FSM: S13/14/45 `bZ`/`ap` compares inverted (**no knife ever flew**), S16-18 never hit, the heal shrine never spent (`i(21)`), popups never expired, retire fall-through, `projB`/`projK` phantom fields | done (Phase 2 branch) | [392](../261004-1400-slice392-ba-ax24-fsm/plan.md) |
| `i.a()` push-past is one straight line: the right-push arm ends `aS.a(true); aS.ag = 0` like the left one, the S131/146 gate is `aS.S==12 && aS.g(this)` (not a loop); the boss's copy was **inverted** (never stopped a player walking into it); `pushOut`/`bossPushPast` removed | done (Phase 2 branch) | [393](../261004-1500-slice393-push-past/plan.md) |
| the claim-script VM `aa()` and its op decoder re-read op by op: op37 arg1 `k.o(0)` gate (final mission only), op22 kill uid, op100 sub0 = `az` store (38 shipped z-order ops did nothing) / sub4, op108/113 QTE accept-vs-reject (a tap outside the prompt rejects, it no longer accepts) | done (Phase 2 branch) | [394](../261004-1600-slice394-script-vm/plan.md) |
| the ax29 boss `aQ()` picker (by3 finisher never fired, `ci[3]` reset on the wrong path, mid band ran both tests, by3 remap `5→40, 8→39, 10→37, 14→35`, S15/16/17 auras + sfx were missing) and `aP()` arms (S4 lunge sign, S14/35 floor loop, S6 vs S15/16 zeroing, S9/37 independent `r()`, arena-clamp return) | done (Phase 2 branch) | [395](../261004-1700-slice395-boss-aq/plan.md) |
| the ax21 director `bD()` and the pursuer script `bG()` re-read from the bytecode: arm 0 `f` bit, arms 1/4/5 (dying pursuers keep scripting, arm 4's `r2`), arm 3's pursuer loop, tail case 4's S37; pv0 six-volley burst (`S14` is one call long), pv3 j==2 fall-through (**the seven homing knives were unreachable**), pv3 scatter parity / used-mark, pv4 `l &= -2` inverted (gunner shielded) | done (Phase 2 branch) | [396](../261004-1800-slice396-director-pursuer/plan.md) |
| the ax10 trigger FSM `aV()` (7.9 KB, 56-way switch) re-read arm by arm: S16 door teleport (the zone owns its 105 prompt; the mid-fade arm UNBINDS instead of flinging; the tap gate is `!g.b(S)` = the aerial/action set) and the S30 wave spawner's `Z[5]` side rule; everything else matched | done (Phase 2 branch) | [397](../261004-1900-slice397-ax10-trigger-fsm/plan.md) |
| the ax4 crate FSM `aj()` re-read from the raw bytecode: the S5/S7 gate is the static `g.b(I)` aerial set (a diving body overlap cracks the crate; the attack list is wrong), S6/S8 spawn **every** remaining wisp in one tick (the port stopped at two — 25 shipped crates never emptied while `k.aq` counted them), and `m(int)` flags the spawner's `aG`, not the wisp's | done (Phase 2 branch) | [398](../261004-2000-slice398-ax4-destructible/plan.md) |
| the context claim `k.L/k.co/k.cp`: the port carried two copies (ax4-only vs everyone else + the touch hit-test) — now one channel with the bytecode's steal rule (`prio<co \|\| (prio==1&&co==1)`), `aw` owner refresh and the ±10 padded snapshot; plus the ax11 `I()` head bid that never existed | done (Phase 2 branch) | [399](../261004-2100-slice399-claim-channel/plan.md) |
| the ax73 heavy guard `aJ()` re-read from the raw bytecode: every `this.g(aS)` read through the wrong receiver, S152 ran a stub instead of the real `b(i)` (**heavy guards never started the chase**), `b(i)`'s facing flip tested `g.g()`, S164/S153/S165/S171 arm exits, `aF()` rider bail-out, `a(true)` was the player's `g.av()` probe (stale `W`) — plus ax17 S170 | done (Phase 2 branch) | [400](../261004-2200-slice400-ax73-heavy-guard/plan.md) |
| the ax11 `I()` head default flags: the intake / stealth-kill gates `r5/r6 = !soldier.g(aS)` (the soldier's facing), not "player alive" — a soldier facing the player no longer takes hits in the flag-less states (S85 flinch…); the parked finding that alerted soldiers should be solid (`a()` after both aA branches) is recorded in the plan | done (Phase 2 branch) | [401](../261004-2300-slice401-ax11-head-flags/plan.md) |
| the ax11 `I()` arms re-read from the raw bytecode, exit by exit: **S21 had no arm (stabbed soldiers looped their death forever at full HP)**, wave members (`aw>=5000`) tallied and left corpses, S175's prompt marker lived on the player, S17/S144/S12/S25/S180-183/S27/S18 exited through the wrong label (S144 threw an attacked weakened soldier into S17), `r6` left on in S4/22/S12/S23, the S24 release dropped the player into the wall side, S6 used an inverted `aF()`, `k()`'s facing gates (hanging-ledge kill only offered on the lucky side) | done (Phase 2 branch) | [402](../261005-0000-slice402-ax11-arm-exits/plan.md) |
| the helpers behind the arms re-read from the raw bytes — `j()` has five callers and the port had grown private copies: **ax17 civilians took the sword without the proximity band, the first-swing latch or `C()` (never staggered or sounded)**, the `g.b()` weapon gate was only an anim check, the `C()` weaken prompt spawned at the soldier's coordinates, and **ax47/ax50 init HP and the ax73 enrage line indexed `bu[]` with the entity's own screen-distance `au` instead of the difficulty `k.au`** (sentinels one tier too weak at the default difficulty, the heavy guard enraged by camera distance); the ax73/ax47/ax50 copies now call the shared `h()/i()/j()/C()/g()` | done (Phase 2 branch) | [403](../261005-0100-slice403-shared-j-c-helpers/plan.md) |

Slice 346 changed the m3 capstone route (legC/legD now traverse the
sentinel chain and the upper path); both legs pass unchanged once slice 347
is in.

## Requirements

- Mirror the original exactly; where the original is not yet mined, mine
  first and record the mining note before porting.
- Fix test fixtures that encode the wrong behaviour (e.g. `Slice64Test`)
  instead of adding parallel tests that leave the wrong ones green.
- Do not touch capstone bot routes in this phase; breaks are recorded and
  handed to Phase 2.

## Work items

### 1.1 ax47 / ax50 FSM swap (G1) — proven · done (slice 346)

- Original: `case 47 → aL()` (ledge sentinel: S0/80–84/93/94),
  `case 50 → aK()` (pouncer: S119–130) — `bytecode/i.javap.txt:20024-20025`,
  `simple/i.java:5181-5194`, bodies at `simple/i.java:9910` (`aK`) and `:10008` (`aL`).
- Port: dispatch at `Level0World.kt:5432-5433`; `tickAx47` holds the `aK` body
  and `tickAx50` the `aL` body (`NpcFsm.kt:9219-9332`). The retype
  (`Level0World.kt:842-846`), the claim states (`NpcFsm.kt:9154`, `:9166`) and
  the death anim (`:9215`) already assume the correct mapping.
- Steps:
  1. Rename/swap so each type runs its original method and the KDoc names
     match (`aK` = pouncer, `aL` = sentinel); audit every `ax == 47/50`,
     `seen47`, `HDM47`, `damageIntakeSentinel` use for consistency.
  2. Rewrite `Slice64Test` (`Slice1Test.kt:7505+`, header "ax47 aK() + ax50 aL()")
     so pouncer cases use ax50 at S120 and sentinel cases use ax47 at S93/S80–84/S94.
  3. Add a pack-level test: load pack-9 (m3, has both types), find a retyped
     ax47 and ax50, and assert each reacts through its own arms (S93
     ceiling-grab → S80 + claim; S120 quadrant pick → S121–128 pounce).
- Data impact: 12 sentinels (m0 4, m2 2, m3 3, m5 3), 8 pouncers (m3 3, m5 5).

### 1.2 `applyHit` op 40 (G2) — proven · done (slice 348)

- Original `structured/i.java:3615-3624`: `if (S != 3 && g.a() && o())`
  → `g.b = attacker; aB = 3; i(3); av = attacker.ak < ak; ag = av ? 512 : -512`.
- Port caller: ax67 clip-27 springboard, `NpcFsm.kt:3591`; `applyHit` has no
  `40` arm (`Entity.kt:3493+`). Model the arm on the existing op 38 arm
  (`Entity.kt:3581-3590`: `playerDamageable` = `g.a()`, `oState()` = `o()`,
  `world.playerLinkB` = `g.b`) but keep op 40's own gates — `S != 3`,
  `g.a()`, `o()`, each one exits (op38 only skips `i(3)` on `o()`).
- Count the affected springboard records per pack and record it in the slice plan.
- Tests: guard matrix (S==3, not damageable, `o()` false) and the positive
  arm (S3, aB=3, facing, ±512, link set).

### 1.3 Player S18 arm (G3) — proven · done (slice 349)

- `g.e()` switch: 18/19/23/36 → offset 7396 (`L1ce4`: `cv=1`, then the
  `L1ce8` air tail) — `bytecode/g.javap.txt:2537-2555`, `:5344-5346`.
- Port: add 18 to the `19, 36 -> { p.cv = true; airFamily(p, pad) }` arm
  (`PlayerFsm.kt:266`); `airFamily`'s comment (`:1919`) already lists 18.
- Tests: S17 wall-kick → S18 gets `aj=1536`, `cv/cp/ct/cw`, the `T==1`
  launch impulse, and lands through the air tail instead of the default fling.

### 1.4 Player S371 arm (G4) — proven · done (slice 349)

- Bytecode: `371 → 2223: goto 13629` (`L353d`, bare tail) —
  `g.javap.txt:2890`, `:2924`; same as 59/65/164/211/297.
- Port: add 371 to the empty-arm group (`PlayerFsm.kt:381`).
- Tests: S370 → S371 (`:1213-1215`) is never flung on anim end; the ax61 S12
  grab (`NpcFsm.kt:5510-5516`) resolves through its QTE/lose path only.

### 1.5 ax11 `aC()` attack scheduler (G5) — proven · done (slice 357, landed with Phase 2)

- Gap: ax11's chase arm calls `aC()` (`simple/i.java:5557-5560`); the port
  has only a chase-timeout subset (`NpcFsm.kt:1076-1085`) and nothing sets an
  NPC to S11, so the S11 wind-up arm (`NpcFsm.kt:371`) is dead. ax73 already
  has a verbatim port, `attackScheduler73` (`NpcFsm.kt:8541+`, citing
  `i.java:8893-9144`).
- Steps:
  1. Mining note (append to `docs/gameplay-mining/npc-fsm.md`): walk
     `i.aC()` arm by arm and mark which branches depend on `ax` / `Z[0]` /
     `aq`, which S targets ax11 reaches (distance tiers, `aq/ar` target walk,
     bound ax69 throw), and what `attackScheduler73` hard-codes for ax73.
  2. Port one shared scheduler (parameterised only where the original
     branches) and call it from the ax11 chase arm; keep the ax73 behaviour
     byte-identical (its existing tests must stay green unchanged).
  3. Tests per tier at controlled player distances; S11 → S12 strike reachable.
- Timebox: if step 1 finds unknowns, split into its own slice plan and
  continue with the other items.
- Impact: 176 ax11 records (m0 49, m2 41, m3 27, m5 27, m6 28, m7 4).

### 1.6 jC=28 ghost rows (G6) — proven · done (slice 351)

- Confirm in `k.java:6204-6228` that the `eC==121` arm of `ae()` draws no
  menu panel/rows; if so, stop `panelVisible` (`Level0World.kt:3095-3097`)
  or the renderer's panel pass from drawing rows for `jC==28 && kEc==121`.
- Test: the jC=28 / kEc=121 frame produces no row rects or pills.

### 1.6b `l()` camera gate for ax17/23/50 (G8) — proven, found in slice 346 · done (slice 350)

- Original `l()` (`simple/i.java:2255-2406`): `case 17/23 → L70`,
  `case 50 → L77`; both arms are `bn → false; else b(this.W, k.ac)`, and
  `b(int[],int[])` (`simple/i.java:665-676`) is **containment** (W ⊆ ac).
- Port `losL` (`NpcFsm.kt`, the `17, 23, 50 ->` arm) uses
  `Entity.overlapI(e.W, ac)` — overlap — so a half-visible ax17/23 already
  "sees" the player in `spotB` (alert) and the L827 tail (counter-alert).
  ax50 is unaffected in practice (`aK` uses `seen50`, which is containment).
- Fix: containment in that arm; tests at the camera edge (overlap but not
  contained → blind). Expect ax17 alert timing changes in capstones.

### 1.7 Deviations (G7) — verdict each

- ax37 scroll triggers: decide whether the separate `ScrollTrigger` list
  (`Level0World.kt:592-614`, fired at `:5275`) must honour P&32/P&256 and
  script removals like the entity path; check the two parked records (m2, m7)
  against the data and fix or document.
- ax23 family filter (`NpcFsm.kt:264`): document as unreachable with current
  data, or route `case 23` straight to the tail (`simple/i.java:5180`).
- `Trig` — done in slice 352: the tables come from archive `/16`
  (`j.a("/16",0,1)`, `structured/k.java:4009`), not pack-2, and `T` is a
  **cosine** table, so every `j.b` consumer was a quarter turn off (not a
  low-impact deviation). `V[]` atan2 and `U[0] = 0` ported verbatim.
- Stale comments: `NpcFsm.kt:29-31`, `NpcFsm.kt:9219`, `PlayerFsm.kt:2197`,
  `Level0Game.kt:209-212`.

## Related Code Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/{NpcFsm,PlayerFsm,Entity,Level0World,Trig}.kt`
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` (Slice64Test),
  new `Slice346Test.kt`… per slice
- `docs/gameplay-mining/npc-fsm.md`, `docs/gameplay-mining/level-atlas.md`

## Success Criteria

- G1–G6 fixed, each with a slice plan (`plans/YYMMDD-HHMM-sliceNNN-*/plan.md`,
  `status: done`) and tests citing the original lines.
- Every G7 item has a written verdict (fixed, or accepted deviation with evidence).
- Verifier `ok:true`, 57 unittests, `:core:test`, `:gdx:test`,
  `:android:assembleDebug` green — except capstone tests that now fail
  because enemies engage; those are listed for Phase 2 and annotated, not
  weakened.

## Risk Assessment

- G1, G3 and G5 change combat and traversal timing in most missions; expect
  capstone breaks (handled in Phase 2).
- G5 may reveal more unported `aC()` dependencies; the timebox keeps the
  phase moving.
