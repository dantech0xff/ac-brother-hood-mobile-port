---
title: "Port slice 18 — ax37 al() scroll-bound triggers (camera walls)"
phase: port
status: done
base: devin/1790045148-port-slice18-triggers
---

# Slice 18 — ax37 scroll-bound triggers

Level 0 carries 28 ax37 records — until now invisible and inert. Mining
`al()` (i.java:7053) proves them to be the Gameloft camera-wall system:
crossing a trigger zone retargets the camera scroll bounds (k.R/T/S/U),
the classic anti-backtrack wall.

## Mechanism (proven)

- Init (i.java:2856 L85): `Z[0..3] = r8[15..18]`, `P |= 512 (+16)`.
  Record layout differs from the generic `f[7+i]` map: **f[7..10] = zone
  rect** (x0,y0,w,h offsets vs anchor), **f[11..14] = payload bound
  rect** (same encoding), f15 = write bitmask, f17 = linked uid (-1),
  f18 = fire mode. W/X materialize via the generic i.java:3702-3710 path.
- Fire gate (al()): `Z[3]==1` → while player W *overlaps* zone (`a()`,
  i.java:632 rect intersect); `Z[3]!=1` → on full *containment* (`b()`,
  i.java:666 = player W ⊆ zone W).
- Payload per `Z[0]` bits — each write additionally gated on
  `a(zone.W, k.ac)` (k.ac = viewport `[O,P,O+400,P+240]`, k.java:2700):
  `&1 → k.R = X[0]`, `&4 → k.T = X[1]`, `&2 → k.S = X[2]`, `&8 → k.U = X[3]`.
- Consumers (k.java:2430-2486, camera-follow): `camX ≥ R` floor,
  `camX ≤ S-400` ceiling, `camY ≥ T`, `camY ≤ U-240`; `<=0` = unset.
  Hard camera snap (`r5&ad`) clears all four — unported (our camera has
  no snap path yet; level-0 data only expands bounds forward).
- Linked-entity gate `switch(Z[1])` on `k.q(Z[2])` — **all 28 level-0
  records carry Z[2]==-1 → unexercised, unported.**
- `k.ah`/`k.n()` context-entity registration — cosmetic highlight path,
  unmined, unported.

## Port

- `scrollTriggers` data list (no entity needed — Z[2]==-1 payloads are
  idempotent writes, nothing to consume/animate).
- `fireScrollTriggers()` after checkpoint processing each tick;
  camera bounds applied after the world-edge clamp.
- `boundMinX/MinY/MaxX/MaxY` = k.R/T/S/U (0/negative = unset).

## Test

- `ax37 trigger writes camera bounds on containment i7053` — 28 records
  parsed; teleport inside aw=133's zone (913,553)-(1133,853) fires
  mask-9 payload → boundMaxY=853 (one-tick delay because the
  zone∩viewport guard waits for the camera to arrive — matches the
  original's ordering); camY then clamps to U-240=613.
- 35 core tests green, verifier `ok:true`, 57 unittests OK.
