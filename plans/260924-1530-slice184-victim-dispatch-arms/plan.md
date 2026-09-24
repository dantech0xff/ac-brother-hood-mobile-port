---
title: "Slice 184 — remaining i.I() victim/dispatch arms (S11/12/24/99/133-145/168-169)"
phase: port
status: done
---

# Slice 184 — victim-side `i.I()` dispatch arms

Closes the mined remainder of the `switch(S)` (i.java:5240): the windup-2,
roll counter-bind, thrown-release, kill-touch, carry-tracking trio,
hostage-free, carry-drop/land, hostage-secure + carry-rise, and the
corpse wisp. Every arm is a verbatim transcription with `file:line`
citations; nothing inferred.

## Source → port map

| S | label | i.java | behaviour |
|---|-------|--------|-----------|
| 11 | L475 | :5578-5585 | windup-2 — `Q()` face, `r()` → `G()`+`i(12)` → L777 (r14→j()) |
| 12 | L478-L502 | :5580-5640 | counter-bind — `W∩X + aS.g(this) + aS.S==6` → `aS.a(34,0,0,this)` + `i(18)`; `Z0==2` → L490 (`aN=this`, `g.E=true`, `b(2)`, marker `a(8,ak,al-85)`) → L849; `Z0==0 && aB>bu[k.au]/2` → L849; `Z0==0 && aB<=bu/2` → L495 (`r()` → `g.g()? i(2) : i(23)+aC=10`) → L777 |
| 24 | L623 | :5720-5746 | thrown-release — `k.aA=60`; player pinned `ah=ag=0, al=W[1], ak=(W0+W2)>>1, O/N snap` while `aC-->0` → L777; expiry → `aS.a(0)` fling + `aS.G()`; edge pick `aT=e(W0-pw,W1)>=12 → ak=lx,av=false` / `aU=e(W2+pw,W1)>=12 → ak=rx,av=true`; `aA=1` + `i(5)` → L777 |
| 99 | L299 | :5442-5445 | kill-touch — `aE()` fires → r13=false; always → L777 (r15→`k()`) |
| 133 | L293 | :5850-5856 | carry-track — `aS.S==270` → `ak/al/T/U = aS` else `i(135)` → L849 |
| 134 | L297 | :5860-5863 | carry-hold — `aS.S==271` no-op else `i(135)` → L849 |
| 138 | L280-L287 | :5826-5842 | hostage-free — `r() && af!=null` → `af.i(k.bK?3:10)` + `k.A(18)` + `af=null` + `P|=32|64` → L777 |
| 142 | L271 | :5436-5441 | carry-drop — `al+=10` + `a(true)`; `!aZ` → L849 else `i(143)` → L849 |
| 143 | L276 | :5820-5825 | landed-dead — `r()` → `aB=0` + `P|=32|64` → L849 |
| 145 | L289 | :5840-5849 | carry-track — `aS.S==271` → `ak/al/T/U = aS` else `i(135)` → L849 |
| 168 | L264 | :5808-5818 | hostage-secure — `r()` → `G()` + `aB=0` + `i(169)` + `k.e(0,aw)` + `k.o(3)` + `aS.i(293)` + `aS.P&=-65` + `ar=aS.al` + `az=301` → L849 |
| 169 | L267 | :5427-5435 | carry-rise — `P|=512` + `t()` + `ah=-2048`; `W[1]>ar` → L849 (still rising) else `ah=0` + `P|=32|64` → L849 |
| 139 | L699 | :6158-6167 | corpse — `r()` → `P&=-17` + `P|=32|64`; `k.bK` → wisp `a(8,59,2,av,ak,al,az-1)` → L849 (upgraded from the earlier subset arm) |

## New helper

`killTouch(e,w,p)` = `i.aE()` (i.java:9144-9185, proven): gates `j==6`,
`aS.S∈{24,22,43,150,35,157}`, `aS.W[3]<(W[1]+W[3])>>1`; then
`(al - g.y)/20 < 20` → **bounce** (L22: `aS.i(89)`, vel0, player snapped
to `W[1]`/mid + `O/N` refresh); `>= 20` → **flying kill** (`g.x[1]=0`,
`aS.h(1)`, vel0, `aS.al=al`, `i(20)`). Always returns false.
(Note: the kill branch is the ≥20-cells one — L22 is the fallthrough
for the `<20` check, opposite of an earlier skim.)

## Also fixed

- Removed the dead duplicate `144 ->` arm (a pre-slice-183 stub
  shadowed by Kotlin first-match `when` semantics — unreachable).
- `S12` arm: subset → full L478 (counter-bind path now reachable —
  roll into the bind claims `aN` + arms `b(2)` slowmo + marker).
- `S139` arm: subset → full L699 (death wisp under `k.bK`).
- `Level0World.spawnEntities` i.D()-sweep now clears `grabHolder=null`
  + `player.gb=null` — the g.h/g.b slots were never reset on respawn.

## Faithfulness notes

- `i.b(int)` → `eventArm(r3)` already existed (Entity.kt:2689) —
  `b(2)` = the counter-cutscene slowmo (`aH`, `aI`, `k.X /= r3` on
  bh[aj]==3 missions).
- `aE()` returns false in both branches — the port keeps that shape so
  the L299 arm's `r13=false` decision stays `proven`-dead-neutral.
- L777/L849 semantics unchanged: arms that `return` skip the shared
  physics tail; non-returning arms fall into j()/push/integrate.
- `Q()` in S11 is `av = aS.ak < ak` (face toward player).

## Tests — `Slice184Test` (17)

- S12: roll-bind claims `aN`+`gE`+`b(2)` under `Z0==2`; `Z0==0 && aB<=bu/2`
  still lands `i(18)` but skips the claim.
- S11: `r()` → `i(12)` + `av` flips toward player.
- S24: pin while `aC>0` (`kAA=60`, vel0, `al=W[1]`, `ak=mid`, `aC` decremented);
  `aC=0` → `i(5)` + `aA=1`.
- S99: `j==6` + flying player + feet-above-mid → close-apex bounce
  (`aS.i(89)`, snapped `al=W[1]`) / far-apex kill (`x1=0`, `i(20)`).
- S133/145: `ak/al` track during 270/271; S134: `S!=271` → `i(135)`.
- S138: `r()` → `af.i(3|10)` + `af=null` + `P|=64`.
- S142: `al+=10` while airborne. S143: `r()` → `aB=0` + `P|=64`.
- S168: `r()` → `i(169)` + `aS.i(293)` + `ar=aS.al` + `az=301` + `kAp[3]++`.
- S169: `W[1]>ar` keeps `ah=-2048`; `W[1]<=ar` → `ah=0` + `P|=64`.
- S139: `r()` + `k.bK` → `ax8/S2` wisp queued in `pendingInsert`.

## Gates

verifier `ok:true` · `python3 -m unittest` 57/57 · `:core:test` ~1206
green · `:android:assembleDebug` · `:gdx:build` — all green.
