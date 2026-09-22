---
title: Port slice 21 — ax4 aj() destructible volumes + claim/marker/wisp machinery
phase: port-slice
status: complete
---

# Slice 21 — ax4 `aj()` destructible volumes

## Scope

Port the ax4 destructible-volume FSM (`aj()`, `i.java:6682-6849`, proven)
plus the shared machinery it exercises:

- `k.a(i,prio,rect)` context-claim system (`k.java:816-865`): strictly-lower
  priority wins (`co=6` unclaimed); equal bid only steals at `prio==1&&co==1`;
  claims persist until `k.m()` release — no per-frame reset. `cp` = claimed
  rect padded ±10.
- `k.l()` ctx bubble (`k.java:802-814`): `[ak-60 if av else ak, al-60, +60, al]`
  — 60px facing-lead zone.
- `k.c(x,y,aw)`/`k.k(aw)` marker popup (`k.java:870-902`): singleton ax14
  entity, clip9, `i(54)`, `az=302`, `cq`=aw-tag; later calls move it;
  `k.k` removes on tag match.
- `m(-1)` wisp burst (`i.java:21259`): `a(74,54,1,k.aS.az+1)` then
  `aD=j.a(0,360)` angle, `aE=j.a(70,90)` radius cap, `aC=2` drift legs,
  anchor `(aq,ar)`, `P=528`, `af=owner`, `aG=1` → `k.b(aK)` deferred insert.
- ax74 `bN()` (`i.java:21280-21389`): S1 polar flight `j+=15`/tick toward
  `aE`, `aF=aD*256/360`, pos = anchor + `j.b` offsets; saturated → `aC--`
  → `i(2)` + `k.A(15)` when `af.aG!=0`. S2: anchor on player head
  (`ak, al-30`), `r()`→`k.c(self)`.
- `j.b(int)` sin table (`j.java:340-357`): `|a|&255` mirrored quarter
  table. `T[]` is deserialized from pack-2 (`f(0)`) — NOT extracted; port
  generates `T[k]=round(sin(πk/128)·256)` — **high-confidence**, mirror
  logic proven.
- `k.aq`/`k.ap[r5]++`/`k.s()`/`k.A(int)` counters (`k.java:4304/5338/7372`,
  `i.java:3033/3142`).

## ax4 semantics ported

Init arm **L161** (`i.java:3132-3161`): `az=r8[11]; aD=r8[4]; aE=r8[5];
aF=r8[7]; n=r8[8]; m=r8[9]; p=r8[10]`; `r8[5]==5` → `k.aq+=m` + falls
into L166 `i=2`; `r8[5]==33` → `aA=0, p<<=8`; `r8[5]==7` → `az=1`,
`Z=int[4]` alloc (fill skipped since `r8[5]!=0`), `P|=512`. The arm reads
RAW `r8[5]` — `i(r8[5])` only runs at the L392 tail (`i.java:3733`).

Tick `aj()`:
- **S5/7** L5: player attacking (`g.b(aS.S)`) + W∩W → `i(S+1)`+`k.A(14)`.
  Else: S∈{37,38} or `W∩k.M` fails → L14 releases our claim (`k.m()` +
  `k.k(aw)`); inside `k.M` → `k.a(this,5,W)` + `k.c(ak,al-85,aw)`.
  L18 calls `a()` — the **solid-side push helper** (`i.java:914+`, L48-63):
  grounded player overlapping the volume is clamped to its edge. L24:
  `aS.X∩W` → `i(S+1)`+`k.A(14)`.
- **S6/8** L28: `r()` → up to two `m(-1)` bursts/tick while `m>0` (each
  `k.o(5)` + `k.s()`), then release claim + `k.c(self)`.
- Anim advance is the universal `s()` in the outer tick (`i.java:6404-6408`).

Level-0 bank (proven parse): S∈{5×2, 6×1, 7×3, 9×14, 21×1}. S9/S21 fall
to the same default no-op as upstream; S29/30/33 and all other `aj()`
states remain unported (absent from level-0 data).

## Files

- `core/Entity.kt`: ax4 fields (`aD`, `nl`=i.n (JVM clash with 8.8 `N`),
  `m`, `i`), ax74 fields (`j`, `aq`, `ar`, `af`), `ga`=g.a grapple link.
  `LevelCellSource` extended with claim/marker/counter/wisp contract.
- `core/Trig.kt`: `j.b` — mirrored quarter sin, generated table
  (high-confidence).
- `core/NpcFsm.kt`: `initDestructible` (L161 verbatim), `ctxZone` (k.l),
  `tickDestructible` (L5/L14/L18/L24/L28), `pushOut` (a() L48-63 subset),
  `tickWisp` (bN S1/S2).
- `core/Level0World.kt`: `ENTITY_CLIP 4→3`; spawn → `initDestructible`;
  tick → `tickDestructible`/`tickWisp`/marker `advanceAnim`; claim system
  (`claimPrio/claimed/claimPad` + `claim/clearClaim`); marker
  (`marker/markerTag` + `setMarker/clearMarker`); `aq/apStats/shake/sfxLog`
  counters; `spawnWisp`/`setMarker` via `pendingInsert` (the `k.b(aK)`
  deferred-insert buffer — faithful, and fixes a mid-iteration
  ConcurrentModificationException); all claim/marker state reset in
  `spawnEntities` for `f(false)` reloads.
- `tools/convert_slice1.py`: +clip3 (entry-003), clip9 (entry-009),
  clip54 (entry-054) from pack-3.
- `gdx/Level0Game.kt`, `gdx/Level0Renderer.kt`: clips 3/9/54 loading +
  module-dir routing.
- `core/Slice1Test.kt`: +4 tests (spawn bank + L161 fields; S5
  claim/marker lifecycle; attack-arm → S6 bursts + self-remove; wisp
  polar flight → S2 anchor → removal).

## Confidence

- **proven**: L161 field map, claim semantics, ctx bubble, marker popup,
  m(-1) spawn params, bN S1/S2 arms, `j.b` mirror logic, `k.b(aK)`
  deferred insert, `a()` push semantics for the ax4 path.
- **high-confidence**: generated sin table values (pack-2 table not
  extracted — standard 8.8 quarter sin, mirror logic proven).
- **inferred/omitted**: `aS.y()` predicate inside `a()` (unported → treated
  false = always push); `k.A` audio requests logged to `sfxLog` (no audio
  backend yet); `k.s()` shake ladder's `dE[]` thresholds unported (counter
  only); marker removal skips the original's `N.p()` play-out.
- `aD`/`aE`/`aF`/`nl`/`pv` for ax4 stored verbatim per L161; only `m` is
  consumed by the ported arms.

## Verification

- `:core:test` — 59/59 green (4 new).
- `verify-static-reconstruction.py` — `ok: true`.
- `python3 -m unittest discover -s tests` — 57 green.
- `:lwjgl3:run` — boots level0, 637 records / 150 npcs.
- Android emulator — `npcs=150`, zero crash; `reports/boot.png`.
