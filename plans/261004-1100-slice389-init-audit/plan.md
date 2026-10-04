---
title: "Slice 389 — the entity constructor i(short[]) follows the bytecode (ax4/6/8/9/13/14/19/21/24/37/42/54/60, E() tail, player settle)"
phase: "port"
status: "done"
slice: 389
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (public i(short[]) 10684-15455; i(I) 2622; E(); a(Z); x(); h(I); k(I); aW() 44397)
  - reconstructed-project/bytecode/g.javap.txt (j() rope release, b(I) grabbable states, k() rope input)
  - reconstructed-project/bytecode/k.javap.txt (static{} 30496 — bi/bj/bk/bl/bm/bn, bu/bv)
---

# Slice 389

`i(short[])` builds every level entity (7.5 KB of bytecode, one `switch (ax)`
over 0-80 plus a shared tail). The port had hand-ported each arm from the
simple decompile, which prints block layout rather than control flow — arms
that sit next to each other in the listing got merged or cross-read. This
slice replaces "read the arms by eye" with a field-by-field differential
over **every shipped record** (8 missions, 3 800 records, 47 actor types).

## Method (static, nothing is run)

- `reports/ctor-i-annotated.txt` — the constructor as pseudo-code, one label
  per switch target (`reports/bcdec.py`: a stack simulation over the
  `javap -c` text, conditions kept as the bytecode states them).
- A throw-away interpreter over the same text evaluated the constructor per
  record (the `static {}` initialisers of `i`/`k` for the clip/HP tables; the
  same-class helpers `i(I)`, `x()`, `a(Z)`, `E()`, `h(I)`, `k(I)` run; every
  other call is recorded as an effect, never executed) and diffed the result
  against the port's post-spawn entity dumped from the real level packs.
  The JAR is never loaded or run; the interpreter is not part of the repo
  (the golden numbers it produced are pinned in `Slice389Test`).
- Raw residuals: `reports/init-diff-residuals.txt` (header explains the
  classes).

## Divergences fixed (all `proven`)

| actor | bytecode | port had | shipped records hit |
|---|---|---|---|
| ax4 | L3607: `az=r8[11]; aD..p`; `r8[5]∈{5,7} → k.aq += m`; `i = 2` for **every** record; `r8[5]==33 → aA=0, p<<=8`. No `P` write | read the ax22 block that follows it in the listing: S7 got `az=1, P\|=512`, skipped `i=2` and the `k.aq += m`; the `S5` total went to a second counter that was never drawn | 15 of 180 (and the **wisp HUD total** `ap[4]/aq` was short in every mission: expected 96/46/142/117/47/123/133/0) |
| ax6 | L1420: `az = 100` only | generic aE/aF/o/p/aG/ay map, no `az` | 7 |
| ax8 | L1261: `P \|= 512; az = 99` | the ax10 generic map | 0 (spawned via `a(8,5,S,201)`) |
| ax9 | L1185: `aB=10; az=99; r8[5]∈{0,34} → k.aV=this`; `Z={0,r8[7],bn[r8[8]]}; aG=0` for every record, no `P` bit | invented `S34 → P\|=512, Z skipped`; `k.aV` for S0 only | 2 (aj5) |
| ax13 | ctor head `bN = 1` | `bN = 0` (rope arc length 0 until the first grab/growth tick) | 14 |
| ax14 | L1739: `o != -1 \|\| (P & 32) != 0 → P \|= 128` | `o != -1` only | 1 (aj7 aw=239) |
| ax19 | L3788: `r8[6]&32 → P\|=160`; `r8[5]==5 → ag=2048`; `az=200` | generic map, no `az` | 22 |
| ax21 | after the ax48 child's `new i(rec)` the arm ends `rec[5] = 1`; the shared tail is `i(1)` | `i(0)`: the director sat in S0 and slice 387's `b()` S1 arm never ran from a real spawn | 1 (aj4) |
| ax24 | L1468: `az = 200` for every record (the pool seed adds `P\|=640`) | `az` left 0 | 12 |
| ax37 | tail skips `i()` for ax37/70; ctor head `S = -1` | `S = 0` | 201 |
| ax42 | L6715 sets `P`/`Z`; the tail's `i(r8[5])` still runs and, with `aa == null`, **skips the anim-range test** (`i(I)` @21 `aa == null → L88`) → `S = r8[5] = 0` | `S = -1` ("no i() reaches ax42"): `bz()`'s `S==0` gate never opened, so **every mission timer was dormant** | 6 |
| ax54/30 | L4874-L4957: `Z[9]==0 → Z[10]=1`; `Z[9]==1 → (Z[8]==0 → 3), Z[10]=1, Z[11]=1, Z[12]=0`; then an independent `Z[8]==1 && Z[9]==1 → Z[9]=0`; `aD=Z[11]; aF=Z[12]`. Child ax68: `az=99`, no `Z` | one `else-if` chain, raw r8[17]/r8[18] into aD/aF; child `az=0` with the record copied into `Z` | 6 (aj4) + 39 children |
| ax60 | L5604-L5928: `Z[4]=2` only inside the `r8[5]∈{9,16}` arm; hide test `r8[5]∈{6,11,13} \|\| Z[4]==2 → az=0, P\|=16`; horizontal probe for S14/15 only, S16 scans down | `Z[4]=2` for any record with `r8[9]==1`, hide test without `Z[4]`, S16 on the horizontal probe | 2 (aj7) |
| ax11 / ax73 | tail L7200: ax11 `a(1); E()`, ax73 `E()` alone; `E()` = `loop { ah=1; b=1; a(1); ah=0; aR∈{≥12,5,3} → return; al+=10 }` | `collideSides` then the `settleToGround` stub (no `a(1)` per pass: no snap to the cell top, `b` never set) | 154 of 176 soldiers spawned 1-35 px off (level 0: −4…+35) |
| ax0 (player) | tail `if (ax == 0) { E(); return }`; the checkpoint restore overwrites ak/al afterwards | spawned on the record's y | aj0 940→939, aj2 1840→1839, aj5 582→579, aj6 740→739, aj7 1740→1739 |
| rope (aW @96-109) | the integrator flip writes **the rope's** `av = bP < 0` (`aload_0; aload_0; getfield bP; … putfield av`) and `g.j()` reads the rider's own facing | wrote `p.av` — the rider was flipped by the swing side | aG1 ropes only (none shipped: all 14 ax13 are `r8[4]=3` → aG4) |

Verified equal (no diff on any shipped record): ax2, 5, 7 (except the clip,
slice 390), 10, 11→47 and 17→50 retypes, 15, 16, 17, 22, 27, 29, 32, 35, 40,
41, 43, 44, 46, 51, 56, 58, 61, 65, 66, 67, 69, 72, 74, 78, 79, 80, the
ax25 flyer + its ax26 glider child, and every record's `W/X/Y` box for the
box-from-record types (5, 10, 14, 37, 42; 645 records). The `x()`/`a(Z)`/`E()`
pass was evaluated on 186 soldier spawns and matches the port's
`probeCells`/`collideSides`/`eSettle` field for field (`aO..aZ, ba..bd, ak,
al, b`).

## Residuals (benign, recorded)

- `Q` starts −1 (head) vs 0 and `cK` starts 0 vs −1, `o` 0 vs −1: every
  reader compares them with real values (`Q == 25/184/175/24/6/297/16`,
  `cK` behind `ca >= 0`, `o` only in arms whose ctor arm writes it).
- The flying player is `ax = 25` in the original (`g.e()`'s bh==3 arms are
  keyed on it); the port keeps `ax = 0` for the player slot. Not touched here
  — any `ax == 0` test the flyer reaches (`i(I)`'s `S50 → g.e(0)`, `e()`'s
  crate override) is the follow-up to look at (`inferred`, no shipped path
  found).
- Arms with no shipped record (ax23, 28, 31, 45, 48, 64, 75, 76) were read
  against the listing: equal.

## Not fixed here (tracked)

- **Settle sites.** `settleToGround` is still the stub at ~20 call sites and
  `spawnChildFx` (= `i.a(IIII)`, bytecode ends `aK.t()`) sinks its child to
  the floor — the ax24 pool seeds sat 1 330 px below their parent. The
  original calls `E()` at 15 specific sites; each needs mapping (task #48).
- The rope same-tick re-latch (aW @327 runs after a release) is faithful to
  the bytecode; aG1 never ships, so the unit isolates it (`Z[1] = 0`).

## Tests

`Slice389Test` (20): per-type invariants over all 8 missions, the golden
`k.aq` totals, the 186 soldier spawn rows, the 8 player rows, the Z[9]
chain, the ax60 hide, the rope `av`. Mutation-checked: 22 reverts of the
fixes above, 21 killed; the survivor (ax73 with the extra `a(true)`) is an
equivalent mutant — `E()`'s first pass performs the same `a(1)`.
Stale tests corrected to the bytecode: ax4 spawn, ax42 ×3, ax21, ax37, ax9
S34, ax60 S9, the soldier spawn window, the briefing respawn row, the rope
release isolation; the sword/strike scenario tests now stand the player on
the ground line with `eSettle` (the soldiers moved up to the real E() line).

## Consequences (observed)

- The HUD `x/y` wisp total counts the ax4 bursts (mission 0: 96, was 86).
- Mission timer fuses (ax42) run from spawn: the countdown phases and the
  `k.l(13)` expiry are reachable (the capstone bots still clear every
  mission).
- Soldiers and the player stand exactly on the ground line at spawn.
