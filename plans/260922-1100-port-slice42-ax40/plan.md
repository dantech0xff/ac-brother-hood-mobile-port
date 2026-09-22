---
title: "port slice 42 — ax40 bx() rideable zipline/gondola"
phase: port
status: complete
---

# Slice 42 — ax40 `bx()` zipline platform

Port of the rideable cable-gondola (`i.bx()`, i.java:17189 tick;
`i.by()` i.java:17360 draw arm — not ported, the renderer already draws
clips; record init L208, i.java:3259). Level 0 fields two runs: gondola
935 (endpoints 936@7480 → 937@7947) and gondola 49 (51@9137 → 50@9700).

## Mined semantics (proven unless noted)

Records: `[40,uid,x,y,0,S,0,az,scriptEnt,startAnchor,endAnchor]`; a run
is one S0 gondola record plus two S2 endpoint records that only draw the
wire.

- `Z` re-allocated to `int[7]`: `[0]`=script-entity uid, `[1]`=cable y
  (home `al`), `[2]`/`[3]`=resolved endpoints (anchor `ak`),
  `[4]`/`[5]`=anchor uids (−1 once resolved), `[6]`=home `ak`.
- L7/L11: anchor resolution arms run once each (start arm gated on
  `S==0`), latch `Z[4]/Z[5]` to −1, and `this.s` links the start anchor.
- L22 outer gate `a(aS.W, this.W) && ah==0`; L24 rider arm: context tap
  16388 or tap-left/right matching `aS.av` dismounts (`aS.i(157)`,
  `al=W[1]−20`, `ag=±1<<11`, `ah=−2560`, `aS.a(null)`, `O()`, `bI()`,
  `ca=−1`, departure `ah=512,aj=1536`). L43 ride pin: catenary
  `al=Z[1]+(10*(r05−r04))/r05` (`r05=(Z3−Z2)>>1`, `r04=|ak−centre|` — +10
  px sag at centre), face travel direction, `aS.i(164)` mount anim,
  `aS.ak=this.ak`, `aS.al=(al+25)+(aS.W[3]−aS.W[1])` hang offset.
- L53 bind arm (`aS.ac!=this && ah==0 && cK!=−2`): release a previous
  ax40 claim (`ac.bI()`), zero player motion, `aS.a(this)`, `aS.i(164)`,
  `aS.t()`, pin position, `h(k.s(Z[0]))` bind script, `k(ca)` key-step.
- L64 tail: `r7` = `ak` past the far endpoint in travel direction → L81
  departure kick (`ca=−1`, `ah=512`, eject a hanging rider via
  `aS.ac.P&=−257` + `aS.a(0)` fling). L88 walked-off unlink
  (`aS.a(null)` when `a(aS.W,this.W)` fails). L95 gravity `aj=1536` cap
  `ah<=2048`; L99 `M()` side-cell probe → `i(1)`; L102 crush scan —
  falling gondola `d()`-kills any overlapping ax11, then `i(1)`.
- L114 S1 reset arm: `aj=0,ah=0`, wait out anim 1, `i(0)`, respawn at
  `(Z[6],Z[1])`, clear claim latch/script ops/`cK`.

**Claim-script ownership of `ak` (inferred):** `bx()` never writes `ak`
outside the S1 reset — horizontal travel is driven by the bound claim
script (`ab()`→`aa()` runs `k.by` ops per tick). `runClaimScript` is
still a stub, so the gondola parks; `ah`/`aj` are the *vertical*
departure-fall speed+gravity until `M()` ends the fall.

**Faithful quirk:** the L53 bind refresh (`aS.t()`) runs *before* the
`aS.ak/al` pin writes, so `W` stays at the pre-hop position — one tick
later L88 sees the stale box miss the gondola by ~1 px and unbinds. The
player's own per-frame `t()` then lifts the box to the hang point and
the next tick's L53 re-binds for good. Bind → unbind → re-bind is real
original behaviour, reproduced in test `ax40 bind unbinds once then
re-binds`.

## Clip: bi[40]=45 (k.java:8442 table, proven)

Converted `pack-3/entry-045-marker-003` → `clips/clip45/`: 4 modules,
3 anims (S0 gondola, S1 depart anim, S2 endpoint), 1 rect — obj0 owns
`[-11,0,24,46]` (the basket hangs *below* the cable anchor). Loaded in
`Level0Game` + both renderer `when` arms.

## Files

- `NpcFsm.kt` — `initAx40`, `tickAx40`, `gondolaTail`,
  `frontCellBlocked` (`i.M()`), `killByType` (`i.d(i)` static).
- `Level0World.kt` — `ENTITY_CLIP` 40→45, spawn `initAx40`, dispatch
  `tickAx40`.
- `gdx/Level0Game.kt` + `gdx/Level0Renderer.kt` — clip45 load/draw.
- `tools/convert_slice1.py` — clip45 entry; `generated/clips/clip45/`.
- `Slice1Test.kt` — 10 tests (init fields, anchor resolve, bind cycle,
  catenary, dismount, S1 reset, far-end kick, ax11 crush, walk-off
  unlink) + `ax40At`/`gondolaRun`/`board` fixtures.

## Gates

- `:core:test` 247/247 green (`./gradlew :core:test --offline`).
- verifier `ok:true`; `python3 -m unittest` 57/57 OK.
- `:android:assembleDebug` OK.
- Emulator boot smoke: pending (clip45 is loaded only when an ax40
  entity renders; world npcs=150+ unchanged).
