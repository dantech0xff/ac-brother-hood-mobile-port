---
title: "slice 58 — ax58 bg() lever/counterweight FSM"
phase: gameplay-port
status: done
pr: pending
---

# Slice 58 — ax58 `bg()` (i.java:14633-14697)

Port the lever/counterweight entity FSM verbatim. ax58 is the **gate
entity** other systems drive: ax44 `bv()` doors resolve their `Z[5]` uid
link onto an ax58 and read `bf()` (open-set check `S∈{1,6,8,10,12}` —
i.java:14599); ax13 `aG(4)` spawners gate segment growth on the linked
ax58's open set. Lives in levels 3/5/6/7 (level-atlas) — no level-0
records, so spawn goes through the generic `Z[]`-fill init arm
(`case 58: goto L777` inert init, proven).

## Mined verbatim (proven, i.java)

- `bg()` (:14633): `ab()→aa()` claim gate first — claimed levers run the
  script and return.
- **S{0,5,7,9,11}** (armed/closed): `be()` occupied test → `i(S+1)` +
  Z[0] bind tail (`k.C=this`, `h(k.s(Z0))` claim-anchor,
  `k(k.s(Z0))` script-key step, `Z0=-1`) + `k.A(21)` creak sfx.
  `k.s(int)` (k.java:7149) returns the `eH[]` claim-script ROW INDEX for
  a uid — not an entity; `h(int)`/`k(int)` are the claim-script entry
  points (:19300/:19325), not the static `h(i)` predicate or entity-pos
  `k(i)`.
- **S{1,6,8,10,12}** (open set): `r() && !P64 → P|=64` (hold-open latch —
  transient, `i()` clears it `P&=-65`); `!be() → i(S-1)` close.
- **S2** (crush arm): `a()` push-contact first; `W∩aS.X && aS.S!=22 →
  i(3) + k.A(21)` (player hit while climbing/moving under the
  counterweight, S22-exempt because S22 rests on the marker); releases
  own `k.L` claim (`k.L.aw==aw → k.m()`) + `G()` drops `ae`.
- **S3**: `r() → i(4)` + same Z[0] bind tail.
- **S4**: `P|=32` park.
- `be()` (:14585): player-`W` overlap short-circuits true BEFORE the npc
  scan; scan marks **every** overlapping ax∈{11,15} `P|=16` (no break).
- `bf()` (:14599): open-set check `S∈{1,6,8,10,12}` — already ported
  (Entity.isBf) for ax13/ax44 link consumers.

## Port

- `NpcFsm.kt`: `leverOccupied(e,w,p)` (be() port) + `tickAx58`
  (bg() port, all 5 arms verbatim).
- `Level0World.kt`: `58 to 20` in `ENTITY_CLIP` (bi[58]=20, proven
  k.java:8442); dispatch `n.ax==58 → tickAx58`.
- `tools/convert_slice1.py`: `clip20 = entry-020-marker-003` →
  `generated/clips/clip20/`; loaded in `Level0Game.kt` + test clip map.
- No dedicated init: inert `L777` arm = generic `Z[]` fill.

## Faithful quirks kept

- `be()` short-circuit: player overlap wins over the npc scan, so an
  ax11 already standing on the plate is NOT `P|=16`-marked when the
  player is also on it (i.java:14585 order).
- `i()` clears `P&=-65` — the L16 `P|64` latch only ever holds while
  `be()` keeps the lever open (observable only in the occupied case).
- `k.s()` returns the eH[] **index** (or -1); `bindScript(-1, w)` is
  indexOf-miss-safe by construction (`kEh.indexOf`).

## Tests (13, Slice1Test.kt:Slice58Test)

be() player short-circuit / empty stays armed / ax11 overlap→P|16+S1;
L9 bind consumes Z0+claims kC / Z0≤0 skips bind; L16 close-on-clear /
hold-while-occupied / anim-end P64 latch; S2 crush→i(3)+sfx21 (p.ga set
to skip a()'s push) / own-kL release+ae drop / foreign-kL untouched;
S3 anim-end→S4+bind; S4 P|32 park.

## Gates

- verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
  `:core:test` all green; `:android:assembleDebug` built.
- ax58 has no level-0 records (levels 3/5/6/7) — nothing new visible in
  the test level by design; unit tests are the coverage.

## Flagged gaps (carried, not fixed)

- gdx `Level0Game.kt` loader still lacks clips 19/36/40 (ax54/30/56/24
  spawn clipless on device).
- `bindScript`/`scriptKeyStep` take the eH[] index — level records using
  `Z[0]` uids only fire when that uid exists in `eH[]` (mission packs).
