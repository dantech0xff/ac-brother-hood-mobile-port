---
title: "Port slice 43a — claim-script tables (k.by / k.bz / k.eH loader)"
phase: port
status: done
confidence: proven
---

# Slice 43a — script-table loader + `k.eH`/`k.s`/`k.t`/`k.by`/`k.bz` wiring

Unblocks the `aa()` claim-script VM (slices 43b/43c) — the interpreter
cannot run until its op-blocks exist on the world.

## What was mined (i.java / k.java)

- `k`'s pack-type-8 loader (k.java:6196-6320, **proven** format — verified
  by parsing pack-6 `entry-007` to EOF exactly): `[u8 scriptCount]`,
  per script `[eH:u16 uid][u8 blockCount][u16 pad]`, per block
  `[u8 type][u8 pad][u16 targetUid iff type 2/3][u16 groupCount]`,
  then `groupCount × [key:u16][opCount:u8][ops]` with op payloads:
  `11/12/21/25/31/41→4B`, `13→5B`, `22/32/42→2B`, `23/24/43/44→4B`,
  `34-36→2+2*argc`, `37-39→2*argc`, `≥100→eI[op-100]`.
- `k.eI` (k.java:8466, **proven**): `{6,2,4,2,2,4,9,2,4,8,4,9,6,4,4}`
  payload lengths for ops 100-114.
- `k.s(uid)` (k.java:7149, **proven**): linear scan of `eH`, -1 miss.
- `k.t(op)` (k.java:7162, **proven**): `eI[op-100]`.
- `j.e(7)` of the mission pack is the script-table entry
  (k.java:6190-6320); `ec[aj]` (k.java:8434) selects the pack — level 0 →
  pack-6 `entry-007-marker-003.bin` (4371 B, 13 scripts).
- `by[r][b]` = raw block bytes + 2-byte appended size tail — the
  interpreter's done-check is `cL[b] >= r03.length - 2`.
- `bz[r][b]` = **first-group byte offset** = block header size
  (4 for type 0/1, 6 for type 2/3) — `bJ()` copies `bz` into `cL`, the
  per-block PC that `aa()` reads group keys through (`high-confidence`:
  the decompiled `bz = r72 - r07` literally reads block *size*, but a
  size-seeded PC would skip every block and release the claim
  instantly — only the header-offset reading lets scripts run; the
  appended tail value is functionally dead either way).
- `i.h(int)` `cd[7]=true` only on the first `cd` allocation
  (i.java:19302) — fixed the earlier slice's unconditional write via a
  `cdAllocated` flag.

## Ported

- `core/ScriptTables.kt` — verbatim parser + `eH`/`by`/`bz`/`s()`/`t()`.
- `LevelCellSource`: `kBy`, `kBz`, `kT(op)` defaults; `claimOps` doc.
- `Level0World`: `scripts` ctor param (nullable for spawn-smoke tests),
  `kEh = scripts.eH` (was `IntArray(0)` — every `bindScript` call had
  been a no-op since claims landed), `kBy`/`kBz`/`kT` overrides,
  `claimOps(ca) → kBz.getOrNull(ca)`. All declared above `init{}` so
  `spawnEntities` sees them.
- `Entity.bindScript`: `cd[7]` now set only on first alloc
  (`cdAllocated` flag — matches the original's `cd == null` branch).
- `convert_slice1.py`: copies pack-6 `entry-007` →
  `generated/level0/scripts.bin` (raw bytes; `ScriptTables.load`
  parses).
- `Level0Game`: loads `scripts.bin` → `ScriptTables.load` → world.

## Verified

- `:core:test` 255 tests green (8 new in `ScriptTablesTest`: parse
  shape, `eH` values incl. `eH[11]=938` the gondola script uid,
  `s()`/`t()` lookups, `bz` = first-group offset, group re-walk of every
  script-938 block, `LevelCellSource` exposure, `cd[7]` once-only).
- verifier `ok:true`; `python3 -m unittest` 57/57.
- `:android:assembleDebug` clean; `scripts.bin` in APK; emulator boots
  `level0: 637 records ... npcs=476`, no crashes.

## Still open (next slices)

- `aa()` interpreter core (block loop, ≤99 ops, move lerp, 34-39
  arg-ops, L341 velocity clamp) — `runClaimScript` remains a stub, so
  bound scripts do nothing yet (gondola stays parked).
- `a()` ≥100 decoder ops (button-QTE 107/108, menus 112/113, spawn
  111, dialog 105).
- `k.c(boolean)`/`k.d(boolean)` mission-pack entity-stream spawner —
  `ek = j.e(0)` = same record stream as the aclv entities, so level-0
  spawns need no separate path; flag for mission switching later.
