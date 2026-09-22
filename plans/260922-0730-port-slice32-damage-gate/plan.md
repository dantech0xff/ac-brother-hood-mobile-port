---
title: "Port slice 32 — g.d()/g.a() player damage-intake chain (i.c() wall-guard + i.bh hit-lock + i.W() boss damage)"
phase: port
status: complete
confidence: proven
source: reconstructed-project/src/simple/{g,i,d,k}.java
---

# Slice 32 — player damage intake, verbatim

## What was mined

| Symbol | Site | Semantics | Confidence |
|---|---|---|---|
| `g.d(int r5)` | g.java:3885–3924 | Full player damage intake. Guard chain in order: `s` (godmode) → `t` (iframes) → `k.aS.c()` (wall-guard probe) → `S∈{67,183,184}` (attack-anim immunity) → `S==205` (decompiler `goto L35` artifact — reads as skip; `inferred`). Then `i.bh=8; x[1]-=r5`; `x1<=0 → x1=0; bh[k.aj]==3→return` (missions 1/4 deathless); `aZ?L27:(a!=null?L27:E()+L27)`; `L27: bl=0; G(); H(); a=null`; `x1>0 → t=10`. | proven (205 arm inferred) |
| `g.a(i r3)` | g.java:139–160 | NPC-side damage gate. `i.bh!=0→false`; `h()` invulnerable → false; `g()` dead → `return true` (no drain). Else `d(r3?.ax==61 ? r3.W()(/3 when aS.S==6) : u[k.au]); true`. | proven |
| `g.g()`/`g.h()` | g.java:3939/3946 | `g()`=`x[1]<=0` dead-check; `h()`=`s \|\| t!=0` invulnerable. | proven |
| `i.bh` (static) | i.java:104; write g.java:3905; dec g.java:572 + 5848; render k.java:4165 | Global 8-tick hit-lock — every `g.d()` drain sets it; decremented in `g.e()` tail L71 and the flying tail; render side gates the `fs` red-flash at `j.c==8` (visual, unported). | proven |
| `i.c()` | i.java:1445 | Wall-guard probe. `r0=(W[1]%260)+260`; r10=1..4: `aT=e((W[0]-r10*20)/20, r0/20)`, `aU=e((W[2]+r10*20)/20, r0/20)`; `aT<10→true`, `aU<10→false`, loop-done→false. Writes entity fields `aT`/`aU`. | proven |
| `i.W()` | i.java:10300 | Boss damage value. `S∈{2,4,10,17}→d.b[k.au<<1]`; `S==13→d.b[(au<<1)+1]`; else 0. | proven |
| `d.b` | d.java:10 | `{10,20,20,40,35,70}` — per-weapon {light,heavy} pairs for `i.W()`. | proven |
| `g.u` | g.java:6400 | `{5,10,15}` — player meter cost per weapon slot `k.au`. | proven |
| `i.H()`/`G()` | i.java:4847/4792 | `H`=`ab?.p(); ab=null` — already ported as `consumeH()`; `G`=`ae` release — `releaseAe()`. | proven |

## Port decisions

- **`g.d` → `Entity.gDrain(amt, w)`** — verbatim guard chain; `x1` is the meter
  (init 90 per slice 8). On `x1<=0` the mission-fail fires next tick via the
  existing `player.x1<=0 → missionFail()` in `Level0World.tick` — `d()` itself
  never fails the mission.
- **`g.a(i)` → `Entity.playerDamageable(attacker, w)`** — the `i.bh` static
  becomes `w.iBh` (world field; the entity instance `bh` keeps its slice-9
  hit-flash role — distinct fields). `h()` → `w.playerInvulnerable()`
  (`godMode || player.gt!=0`); `g()` → `w.playerDead()`.
- **`i.c()` → `nearLeftWall(w)`** — reads `LevelCellSource.e()` cells; writes
  `aT`/`aU` fields like the original.
- **`i.W()` → `bossDamage(w)`** on the NPC; `DMG_B` table on Entity companion.
- **`op4` rewired**: `S∈{284,285,50}→return` (from i.java:4540); ax61 boss →
  `playerDamageable` + counter; `S!=9` arm → `playerDamageable(g)` + attacker
  `ax∉{17,50,61}` → `counteredBy`; `k.A(18)` tail fires unconditionally
  (`world.sfx(18)`).
- **`op18`** = `playerDamageable(g); setAnim(43)` — knockdown pays meter.
- **`op38`** gained the `playerDamageable` gate before the grab
  (`playerLinkB=attacker; aB=3`), per i.java source.
- **`i.bh` decrement** added to `PlayerFsm` tail at the g.java:572 position.
- **`bl` field** added — `g.d` clears it on death; set by `t()`/riders.

## The `i.c()` wall-guard is REAL

`nearLeftWall` correctly returns true at open-air test positions — the probe
row `(W[1]%260+260)/20` hits open cells `<10` on both sides, so `g.d()`
legitimately skips damage there (it guards the player from taking hits while
a wall exists within 4 cells of either side on that probe row). This surfaced
as 7 test failures where positions were picked without geometry awareness —
fix: `damageSpot(w)` test helper scans `level0` for the first position where
`!nearLeftWall`, and the counter test picks a soldier whose strike-adjacent
side passes the probe. Also `setPositionPx` doesn't refresh `W` — tests call
`refreshBoxes()` before probing.

## Verification

- `./gradlew :core:test` — all green (incl. updated KO/iframes/ax2/kill-restore
  tests draining through `damageSpot` + `w.iBh=0` resets between hits).
- `verify-static-reconstruction.py` → `ok:true`; `unittest discover` 57 pass.
- Emulator: `./gradlew :android:installDebug`, boot line `npcs=454`, no
  crash — same as slice 31.

## Still unported / follow-ups

- `k.au` weapon slot still constant 0 — `u[]`/`d.b` index it but no producer
  sets it yet (weapon-switch arm unmined).
- Render-side `i.bh` red-flash (`k.java:4165`, `fs` + `j.h`/`j.d` bars) —
  visual-only, needs the HUD pass.
- `d(8,x,y)`/`p(r8,r9)` ax24 floatie spawner — mined, not wired.
- `g.s` godmode producer (cheat path) — `godMode` reads false for now.
