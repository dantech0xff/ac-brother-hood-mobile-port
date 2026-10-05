---
title: "Slice 419 — P2 no-raw-byte-pass audit: all listed families proven faithful (verification-only)"
phase: parity-gap-closure
status: done
slice: 419
---

# Slice 419 — P2 audit sweep

## Scope

The P2 backlog from the handoff plan ("methods without a raw-byte pass on
record"):

- `ax78/ax45 bA()`, `ax34/ax75 ak()`, `ax42 bz()`, `ax58 bg()`, `ax7`
  (inline in `I()`), `ax17 aA()`
- the `g` statics `aA()` `aC()` `d(int)` `e(int)` `f(int)`
- same-name `i`/`g` method groups with **different** parameter lists
  (slice 412 swept equal-parameter overloads)
- `k.a()` frame procs of the menu/UI screens, `k.b(Z)` draw, and the `k`
  UI methods `ah M R Q G(int)`

## Verdict: every family verified byte-faithful — no code change

This is a verification-only slice. Each family was re-read in
`reconstructed-project/bytecode/{i,g,k}.javap.txt` and mapped onto the
port; no divergence survived the read.

### Entity tick dispatch arms (`i.I()` tableswitch @635)

| ax | original | port | verdict |
|----|----------|------|---------|
| ax78 → `bA()` @58637 | fall FSM, P128-recenter | `tickAx78` (NpcFsm.kt:6615) | proven equal |
| ax34 → `ak()` @25263 | follower: 35-state hide list @180-609, `P±128` | `tickAx34` + `AX34_HIDE_STATES` (NpcFsm.kt:10218/10203) | proven equal |
| ax42 → `bz()` @58375 | fuse/timer: `P|=16|512`, `k.F=this`, `S==0` gate, aJ machine | `tickAx42` (NpcFsm.kt:6356) + `initAx42` (:6329) | proven equal |
| ax58 → `bg()` @48870 | lever: `be()` occupied, S-arm | `tickAx58` + `leverOccupied` (NpcFsm.kt:7403/7385) | proven equal |
| ax7 (inline @973-1247) | hurt/knockback arm in `I()` | `tickAx7` + `initAx7` (NpcFsm.kt:6220/6207) | proven equal |
| ax17 → `aA()` @30557 | civilian, panic-pick @261-402: xSep {above:66, overlap:61, below:67}, xOverlap {above:62, below:63, overlap:61} | `tickAx17` + `insideOf` (NpcFsm.kt:8057/8052) | proven equal |
| ax45/ax75 (empty arms → tail @7989) | **no method** — fall through to generic tail | `else → npcFsm.tick` + `claimed=false` + `defaultArm` (Level0World.kt:5867-5871) | proven equal — the plan's "ax45/ax75" pairing was a red herring: the original has empty dispatch arms and the port's fallback path IS the original tail |

### `g` statics and helpers

| original | bytes | port | verdict |
|----------|-------|------|---------|
| `g.aA()` @17180 | `u(16388) \|\| (av&&u(8264)) \|\| (!av&&u(4114))` → `i(92)`; `a(8,5,17,201)` child bound to `i.aK` with `av,P=512,N/O=8.8pos,ak/al,ag=ah=ai=aj=0,t(),k.b(aK)` | `wallJumpKick` (PlayerFsm.kt:2461) — 2 call sites in `g.e()` (@6667,@7049) mapped to port sites (PlayerFsm.kt:876,885) | proven equal incl. field-write order |
| `g.aC()` @17376 | `if (k.aw==20) k.aw=0` — its only read of `k.aw` | `kAw` stub (always 0, writes eaten) | proven dead-by-xref: `k.aw` writers are 8×`bipush 20` in `g.n()` + 4×`0` in `i` slow-mo arms (@26994/@27031/@64504/@64612); `g.aC()` itself is dead — port's stub is functionally exact |
| `g.d(I)` @11165 | guards `godMode\|\|gt!=0`, `S∈112..115` (= `aS.c()`), `S∈{67,183,184,205}` → `iBh=8; x1-=amt`; `x1<=0` zero-floor, mission-check, `eSettle` arm; else `gt=10` | `gDrain` (Entity.kt:3795) | proven equal; 4 call sites verified: NpcFsm.kt:5480 (@1609 `g.u[k.au]`+`gt=5`), :5664 (@643 ax61 S15 catch after `i(375);aS.al=al`), :9735 (@2612 `g.d(999)` in the aC<=0 arm), :10184 (@359 `a(W,W)` overlap); PlayerFsm.kt:1506 |
| `g.e(I)` @11260 | `x[1] = amt` absolute set | `player.x1 = <amt>` writes (Level0World.kt:648 `x1=90` after `az=202`, :1831, :4042, :4867; NpcFsm.kt:6307 `p.x1 = w.kAx` refund @154; Entity.kt:1031 `n==50 → x1=0` @172) | proven equal, all 4 bytecode sites mapped |
| `g.f(I)` @11271 | `x[1] > amt → x[1] = amt` max-cap write (`if_icmple` returns when `x[1]<=amt`) | `player.x1 = minOf(player.x1, kAx)` (Level0World.kt:1830, :5103) | proven equal |
| `g.b(I)Z` @1970 | 20-state latch set {18-25,35,36,43,150,157,165,233,242,243,263-266} | `Entity.gB()` (Entity.kt:3350) + `GRABBABLE_STATES` (:3552) | proven equal |
| `g.b()Z` @1923 | `I∈{1,2} && aS.S∈{67,68,69,81,112-115,183,184,216,217,286,287}` | `playerAttacking` path (Entity.kt:5268, gate Entity.kt:3170-3192) | proven equal |
| `g.c(I)Z` @2011 | interactable set {0,1,7,11,12,26,79} | `Entity.gC()` (Entity.kt:3357) + `INTERACTABLE_STATES` (:5816) | proven equal |
| `g.c()Z` @2039 | `S∈{112,113,114,115}` | `aS.c()` mid-combo guard inside `gDrain` (Entity.kt:3781-3784 comments) | proven equal |

### Same-name `i`/`g` groups (different parameter lists)

Bytecode-level separation holds in every group — the i-side member is
`private` (invokespecial) or `private static`, the g-side `public`/`final`/
`static`, or the descriptors differ (e.g. `i.j()Z` vs `g.j()V`, `i.d(int)Z`
vs `g.d(int)V-static`); no JVM dispatch ambiguity exists, and the port uses
distinct Kotlin names throughout:

| i-side (private) | port name | g-side | port name |
|---|---|---|---|
| `i.a()` @5265 contact push | `pushContact` (Entity.kt:1511) | `g.a()Z` static gate | `playerDamageable` (:3825) |
| `i.b()Z` corner probe | corner-support probe (:1925) | `g.b()Z` attack test | Entity.kt:5268 arm |
| `i.c()Z` ring probe | Entity.kt:1475 | `g.c()Z` mid-combo | inside `gDrain` |
| `i.d()I` awareness tier | NpcFsm.kt:8445 (ax73) | `g.d()V` → `b(g.a)` | eagle/bird path |
| `i.e()` sight rect | NpcFsm.kt:8477 | `g.e()` player tick | `PlayerFsm` tick |
| `i.f()Z` hanging check | NpcFsm.kt:83 | `g.f()Z` holding | `isHolding` (:2187) |
| `i.g()` push victim | Entity.kt:375 | `g.g()Z` dead test | `playerDead` |
| `i.h()Z` immunity gate | NpcFsm.kt:7909 | `g.h()Z` invulnerable | `playerInvulnerable` |
| `i.j()Z` strike intake | NpcFsm.kt:7942 | `g.j()V` latch release | PlayerFsm latch arm |
| `i.k()Z` stealth-kill driver | NpcFsm.kt:9164 | `g.k()V` bM checks | PlayerFsm bM arm |
| `i.l()Z` sight check | NpcFsm.kt:10478 | `g.l()Z` ungrapple | PlayerFsm unhook arm |
| `i.m()` flag write | NpcFsm (inline) | `g.m()Z` carry check | Entity carry arm |
| `i.n()` | NpcFsm (inline) | `g.n()` flying tick | PlayerFsm flight |
| `i.d(int)Z` range probe | **dead — zero callers in i/g javap** | `g.d(int)` drain | `gDrain` |

All `g.X` invocations on `k.aS` from i-code (23×`g.a(I)`, 17×`g.b(I)Z`,
14×`g.b()`, 10×`g.g()`, 7×`g.h(I)Z`, 5×`g.g(I)`, `g.f()`, `g.e(I)`,
`g.d(I)`, `g.c(I)`, `g.a()`, `g.d()`, `g.a(IIIII)`, `g.n()`, `g.m()`,
`g.l()`, `g.k()`, `g.j()`, `g.e()`, `g.c(Li)`, `g.b(Li)`, `g.a(Li)`) map to
already-proven port helpers (`flingAirborne`, `gB`, `playerAttacking`,
`playerDead`, `equipWeapon`, `awardFlag`, `isHolding`, `gDrain`, `gC`,
`mountEntity`, wall/flying ticks, `tryDamageOn`, line-draw helper).

### `k` frame procs and UI methods

| original | bytes | port | verdict |
|----------|-------|------|---------|
| `k.a()` tableswitch @357 (cases 0-31) | per-case procs; dead {7,16,26,default→5042} | `tick()` @5357: `kAl&&jC!=21 \|\| jC∈menuStates → menuFrame`; play {8,21→simI/simH+dialog, 10→posterAg, 15→winStatsM, 22→medalAh}; menuFrame covers {0,1,6,9,11,12,13,18,19,20,23,24,25,27,28,30,31}+`menuStates`{2,3,4,5,29} | proven equal — every case mapped; dead cases consume ticks verbatim |
| `k.b(Z)` @13671 | `!arg && j.c∈{12,13,31} → return`; `am&&!dd → dd=1; black veil; j.a(0,false)` | `bPass(z2)` (:4352) + `scrollBounds` veil latch (`kDd=true; jT\|=1` :4505-4507) | head arms proven equal; draw body = `drawStylePass`/`drawPassBubbles`/`hudStep`/`claimFooter`/`overlayTailStep` (prior slices, proven) |
| `k.M()` @17252 | `j.g==1 → W();ac();ad();E(); bA[52+aj<<1] record compare → ap[5]` | `winStatsM` (:2719) | proven equal (record-compare semantics carried) |
| `k.Q()` @18486 | `v(131072)` back: jc∉{23,13} → `cb=1`; `bv==2→l(2);z(30)`; `bv∉{3,4}` arms; confirm arm `v(327712)` | `menuQ(pressY)` (:3066) | proven equal incl. row-tap fold (orig's draw-loop `c()` hit → `bw`+`E(32)` ≈ `menuRowAt`) |
| `k.R()` @19517 | boot driver case-0 | `bootR` (:3679) | proven equal (prior slices) |
| `k.ah()` @29974 | medal screen proc (case 22 @5031) | `medalAh` (:2944) | proven equal |
| `k.G(I)Z` @23100 | staged loader: `A[1]=null`; arg==1 arm `dx=false; j.a("/14",aj); dD=a(bA,32); dB=a(bA,44) i2b; dB<=0→30; az=dD; ax=dB` | `menuJc9` staged loader (:4018-4052): `jG==3→loadPackI; ==8→kK(); ==164→spawnEntities`; the arg==1 arm's field writes (`dD/dB/ax`) map to `kDB/kDC` reload bytes | proven equal |

## Parked / not parity

- `g.aA()`'s two `g.I()` call sites @6667/@7049 map to the ported
  wall-kick sites; `wallGrabSnap` (PlayerFsm.kt:2387) also spawns
  `a(8,5,17,201)` but with a different arm shape (`setAnim(101)`) — a
  different original site, not `g.aA()`.
- `cb/cc/cf/cQ` lazy-null vs JVM-allocated (from slice 417).
- NPC-side `Entity()` ctor init divergences vs `i.<init>` (from slice 417).
- `k.ac` process-static-vs-instance edge (parked earlier).

## Gates

Verification-only: verifier `ok:true` + 57 unittest expected unchanged
(no source edits). `:core:test`/`assembleDebug` skipped — no code touched.
