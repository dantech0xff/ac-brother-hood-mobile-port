---
title: "Slice 159 — S90 kill-QTE launch state + g.D producer + ax10-S53 consumer"
phase: port
status: complete
---

# Slice 159 — S90 launch state (g.java:2641-2660)

Ports the `g.e()` `case 90` arm verbatim — the big ballistic launch the
kill-QTE finishers and perch sentinels throw the player into — plus the
`g.D` flag lifecycle it drives.

## Producers (already ported, verified this slice)

- `k.aS.a(6, 90, this.ak, this)` at i.java:1548 (ax11 Z[14]==6/7
  finishers → `g.p=1/2`), :1564 (ax47 → `g.p=Z[0]`), :1576 (ax50 →
  `g.p=Z[0]`) — ported as `applyHit6(90,…)` + `w.gP=…` at
  `NpcFsm.kt:8305-8330`.

## Ported arm (PlayerFsm.dispatch `90 ->`)

| original | port |
|---|---|
| `ae = null` | `p.ae = null` |
| `ah = 0` | `p.ah = 0` |
| `G()` (i.java:3637 — deactivate `ae`, drop link) | `p.releaseAe()` |
| `k.v()` (k.java:5609 — `eL=bC=bB=0`) | `pad.edge = 0` |
| `D = true` | `p.gD = true` |
| `p==1 → ag=4096,av=false` / `p==2 → ag=-4096,av=true` | `world.gP` same arms |
| `ah=-768; i(157); p=0` | `p.ah=-768; p.setAnim(157); world.gP=0` |
| `else if (r()) { aR>=20||aS>=20 → i(0)+E() else a(0) }` | `animFinished()` → `aR>=20||aS>=20 → setAnim(0)+eSettle(world)` else `flingAirborne(0, world)` |

`i.E()` (i.java:2929) = the `ah=1/b/a(true)/al+=10` settle loop — already
ported as `Entity.eSettle`. `g.a(0)` (g.java:126) = `flingAirborne(0)` —
already ported. S157 has no `case` in `e()` — the flight anim exits via
the default `else` arm (`animFinished → flingAirborne(0)`) — faithful.

## g.D lifecycle (complete)

- set: `p.gD = true` every S90 tick (g.java:2646)
- clear: `p.gD = false` on S12 entry (g.java:1317) — was already ported
- consume: ax10-S53 zone `g.D && overlap && player.S∈{0,1,5} → i(360),
  ag=ah=0, k.c(this)` (i.java:13192) — was already ported; now reachable
  end-to-end for the first time.

## Tests (Slice159Test, 5)

- launch arg 1 → `ag=4096/av=false/ah=-768/S=157/gP=0/gD/ae` all set
- launch arg 2 → `ag=-4096/av=true`
- gP=0 + anim end + `aR=20` → `i(0)` settle
- gP=0 + anim end + `aR=aS=0` → `a(0)` fling (S43, aj=1536)
- S12 entry clears `gD`

(pre-existing `ax10 S53 land-on-feet zone` test covers the consumer;
stale "producer unported" comment freshened.)

## Gates

verifier `ok:true` · 57 unittests OK · `:core:test` all green (incl. 5
new) · `:android:assembleDebug` + `:gdx:build` green.
