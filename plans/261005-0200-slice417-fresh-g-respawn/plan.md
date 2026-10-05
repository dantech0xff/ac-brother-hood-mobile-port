---
title: "Slice 417 — trap T16/P1a: the reused `player` vs a fresh `g` — every ctor field re-armed on respawn"
phase: "port"
status: "done"
slice: 417
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (<init>()V @4-198; <init>([S)V record load @201-274, clip bind @283-468, tail i(r[5])/E() @7146-7199; D() @0-406)
  - reconstructed-project/bytecode/g.javap.txt (<init>([S)V @5-121)
  - reconstructed-project/bytecode/k.javap.txt (d(Z)V @7 invokestatic D(), @614 aS=g; a(Z)V restore arm @206-243)
  - reconstructed-project/inventory/fields.json (i: 123 instance + 83 static; g: 21 instance + 50 static)
---

# Slice 417 — fresh `g` vs the reused `player`

`k.d(Z)V` runs `i.D()` then `new g(r)` (k.javap @614): the original rebuilds the player object from scratch on
every spawn — record respawn, checkpoint restore, mission retry alike. The port keeps ONE `Entity` and patched
fields; every field the constructors write was a stale-state bug waiting to be observed, and every field the
constructors do NOT write that the port nonetheless reset was a freshness bug of the opposite sign (the
original's `g` statics outside `i.D()`'s clear list *persist* across spawns).

## The audit (`proven` — raw `javap` text only)

### Fresh-object end state

`i.<init>` explicit block (i.javap @4-198):
`Q=-1, R=-1, S=-1, aq=0, ar=0, d=false, au=10, i=1, k=false, y=0, bp=false, bs=0, C=0, bz=false, cr=null,
bM=null, bN=1, bO=0, bP=0, bR=false, cG=0, cH=null, cI=false, cJ=false, ca=-1, cM=-1, cN=-1, cP=-1, cR="",
cS=null, cT=null, cU=null, cV=false, cW=0` — everything else JVM-zero.

`i.<init>([S)V` record load (i.javap @201-274): `aw=r[1], ak=r[2], al=r[3], N=ak<<8, O=al<<8, ax=r[0],
av=(r[6]&1)!=0, **P=r[6]**` — then the clip bind `aa=k.r(bi[ax])` (@283-468), the ax arms (ax0: `az=100`,
`aA=2`, `aB=3`, `Z={0,0}` — i.java:1971-1981; ax25: `az=202`, `aA=2`, `aB=3`, `ah=-2560`, `aq=ar=-1`,
`ad` retype-26 companion — i.java:2415-2427), and the tail `i(r[5])` + `ax==0 → E()` (@7146-7199).

`g.<init>([S)V` tail (g.javap @5-121): `cl=false, cx=(8*j.m)/360, cy=cz=cA=cB=cC=cD=cE=0, cF=5120,
**cG=i.av** (@66-70 — latches the RECORD-facing, read before the restore arm rewrites `av`), cH..cM=0,
K=0, cN=0, L=0, M=0`.

`k.a(Z)V` restore arm (k.javap @206-243): `ak=bA[18], al=bA[20], av=bA[22]==1` — writes `ak/al/av` ONLY.
The fresh object's `N/O` keep the record-spawn 8.8 position: the player's logical position jumps to the
checkpoint while its fixed-point anchor stays at the spawn (desync quirk — the port's `setPositionPx`
wrongly synced the pair).

### `i.D()` clear list vs `g` statics (i.javap @0-406)

`D()` clears g.{b,a,h,c,e,g,d,l,m,j,q,r,A,F,k,x,E} + the i/k statics. NOT cleared (persist):
g.{B,C,D,G,H,I,J,ci,cj,ck,cm,cn,co,cp,cq,cr,cs,ct,cu,cv,cw,f,i,n,o,p,s,t,u,v,w,y,z}.

### What the port had wrong

| field(s) | was | now |
|---|---|---|
| whole ctor state | `resetPlayerToSpawn` re-armed ~15 fields; every other ctor/JVM-zero field stayed stale (P flag word incl. P&64 anim-hold / P&16 hidden / P&256 freeze, grab-lunge `cF/cx`, gauge `K/cN/L/M`, claim `ca/cP/cR/cS/cT/cU/cV/cW`, latches `cM/cN`, waypoint `cH`, arrays `cb/cc/cf/cQ/cd/Z`, links `cr/bM/F/g/gg/cg/ch/wpBt/wpF`, `bN=1`…) | `Entity.resetToFreshSpawn()` re-arms the complete ctor end-state |
| `P` | never written on respawn — stale flag word survived | `P = r[6]` (i.javap @269-274) |
| `av` | hard `false` on the no-checkpoint arm | `av = (r[6]&1)!=0` (i.javap @251-266) |
| `N/O` on checkpoint restore | `setPositionPx` synced 8.8 to the checkpoint | `ak/al` written directly — `N/O` stay at the record spawn (proven quirk) |
| `cG` | never written — stale grab-facing | `cG = av` at g.<init>-tail position (record-facing, before the restore arm) |
| `E()` on the checkpoint path | skipped (`s == null` guard) | runs at the record position inside the ctor, before the arm (probe side-effects `b`/aR/aZ describe the record ground — proven @7196 ordering) |
| `g.t/g.n/g.o/g.B` (`gt/gn/go/gB`) | cleared on every respawn | persist — not in `D()`'s clear list |
| `g.f` (`Entity.gf`) | `Entity.gf = null` in `spawnEntities` | removed — persists |
| `g.g/g.F` (`g`/`gg`/`F`), `g.b` second copy (`playerLinkB`) | never swept | `g/gg/F` die in `resetToFreshSpawn`; `playerLinkB = null` in `spawnEntities` |
| `clip` | rebound only on the no-checkpoint arm, hard `clips[0]` | always `k.r(bi[ax])` via `entityClipIndex` — ax25 record gives 16, mission-switch ax25→ax0 rebinds correctly |
| `gy` (`g.y`) | (listed stale) | `i(r[5])` writes `g.y = al` at the record position every spawn (Entity.kt:1032 — the ctor tail re-latches it, not a persist field) |

Fields intentionally untouched: `g.I/g.J` (persist statics — but `loadMission` → `F(aj)` rewrites them
`g.I=1` / `g.J|=f0do[aj]`; the checkpoint arm restores the snapshot pair), `x1` (`g.x[]` nulled by `D()`;
callers re-write via `g.e(k.ax)`), `asSlot`/`oId` (port sentinels, unobserved), `cb..cQ` kept nullable
(port's lazily-allocated claim-script arrays — the JVM ctor allocates them; parked divergence,
zeroed-when-present so no stale values ride the respawn), `i.bp` (unported), probe outputs
`tc/uc/bd/bb/bc/W/X/Y` (`a()`/`E()` rewrite them before any read — write-before-read).

## Tests — `Slice417Test` (+5)

* `mission reload re-arms every ctor field on the reused player` — ~90-field sweep through `resetLevel(false)`.
* `checkpoint restore keeps N and O at the record position` — the desync quirk + same sweep through `resetLevel(true)`.
* `cG latches the record facing not the restored facing`.
* `statics outside the D() clear list persist across respawn` — `gt/gn/go/gB/gD/cFlag/gcm/ci/z/cp..cu/Entity.gf` staged, survive; `g.I/g.J` verified as `F(aj)`-rewrites (1 / `f0do[0]=5`).
* `g-b links and D()-cleared statics die with the body` — `g/gg/F/playerLinkB` + `gq/grabLatch/gE`.

## Mutation check

12 mutants (`scripts/mutants/slice417.py`), narrow filter first, full suite last — see the report below.
Every restored pre-fix behaviour is caught.

## Parked

* `cb/cc/cf/cQ` nullable-vs-allocated divergence (JVM ctor allocates the claim-script arrays; the port
  lazily allocates — kept nullable, zeroed when present).
* NPC-side `Entity()` construction inits vs `i.<init>` values (`bN`, `S`, `Q`, `v`, `oId`, `asSlot`,
  `scriptStep`, `cK`-family) — same fresh-object contract applies to `new i()` entities; player-only
  this slice.
* P1b — `i.v()` camera-rect snapshot vs live rect (`inferred`), and the P2 unaudited method list.
