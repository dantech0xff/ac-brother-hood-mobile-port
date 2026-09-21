# Gameplay-mining report

## What was extracted (all static-only, no JAR execution)

| Artifact | Source | Result |
|---|---|---|
| Full game script | `decoded/pack-14` (9 tables / 265 strings) | UI table + 8 per-mission briefing/dialogue tables; mission↔level mapping proven via `j.a("/14", 1+aj)` at `k.java:4747` |
| Level atlas | `levels-decoded/pack-6..13/records.json` | per-level entity histograms, extents (two vertical flying levels confirmed), script lane counts |
| Player mechanics | `g.java`/`i.java`/`k.java` | FSM state families, physics constants verbatim (2560 run cap, 5120 terminal, 1536 gravity, 768 bounce, ±2048 fly), damage model, cheat table, flying-machine gauge |
| Entity catalog | `i.I()` dispatch `i.java:3920-5292` | all `ax` 0–80 mapped to handlers; checkpoint/damage-volume/door/crate/platform/prop/npc/boss families classified |
| Audio/sprite | `e.java`, `h.java`, `k.java:260-271` | 34-slot table (music 0-9 mid / SFX wav + 3 stinger mid), per-level music `ee[]`, sprite registries `bi/bj/bk/bl/bm/bn`, per-level sprite sets `ej`, mode flags `bh` |

## Key proven claims worth noting

- `k.bu={300,400,500}` enemy HP per difficulty; `d.b={10,20,20,40,35,70}` damage tiers.
- `bh[aj]==3` marks flying-machine levels (packs 7,10) — matches vertical extents.
- `ee[aj]` per-level music; single `Player` audio channel with duration gating.
- Type-2 entity = checkpoint that writes the `bA` save buffer on overlap.
- `dv++` killable-entity counter gates achievement thresholds (7/28 kills).
- Cheats: 8 toggles + 2 screen jumps + free-fly `g.v`; `g.s` god mode.

## Docs produced

`docs/gameplay-mining/{string-corpus,level-atlas,player-mechanics,entity-type-catalog,audio-sprite-usage}.md`
and `docs/gameplay-design-document.md`.

## Known gaps (honest)

- ~170-state NPC FSM (ax 11/17/23/47/50/73 shared handler) summarized at
  family level; per-state semantics need a dedicated pass.
- `g.e()` structured view partially damaged; some arms read via simple view.
- Per-state sprite/animation mapping (`i.i()`/`aa.b(S)`) not yet mined.
- `d.a`/`bw`/`au` exact indexing contract inferred, not fully proven.
