---
title: "Port slice 1 — clip runtime, level-0 render, player locomotion"
date: 2026-09-21
status: done
track: rewrite-port
---

# Mục tiêu

Lát cắt port đầu tiên của game thật (không còn spike): chạy level 0
(pack-6) trên spine Kotlin+LibGDX với sprite/tile/anim decode trung thực
theo bytecode gốc — nền cho mọi FSM port sau này.

# Phạm vi

1. **`b` clip runtime** → `core/Clip.kt` + `tools/convert_slice1.py`
   (ACPK binary): module table, frame pool `av|((i&0xC0)<<2)` object
   index, `au/h` anim table, `ao` rect spans per object, `ai/aj` +
   `ap/aq/ar/as` composite placement pool.
2. **`i` entity skeleton** → `core/Entity.kt`: `ak/al` px + `N/O` 8.8,
   `S/T/U/Q/P`, `av` facing; `i()`/`s()`/`r()`/`q()` verbatim
   (i.java:240-330); `integrate()` per i.java:3887-3916.
3. **Level-0 content** → `core/LevelPack.kt` (ACLV): `et` collision
   (OOB→20), visual layers `ep`/`er` (tileset clips 11/12 qua `k.ej`),
   2-bit flag planes MSB-first, `eu` backdrop deferred; 637 entity
   records verbatim.
4. **Player slice** → `core/Level0World.kt`: idle/run/jump/land on
   proven constants (run ±2560, gravity 1536, terminal 5120, jump −5120),
   hitbox từ rect pool per-frame (`aa.a(S,T,0,W,P&7)`), camera follow.
5. **gdx adapters** → `Level0Game` (62ms tick ≤4 catch-up),
   `Level0Renderer` (recursive composite-object draw + J2ME TRANS_*,
   400×240 nearest letterbox), `Level0InputBridge` (hold L/R, tap = jump).

# Kết quả

- `python3 rewrite/tools/convert_slice1.py` → clips clip0/clip7 +
  tilesets 10/11/12 + level0.aclv (deterministic, đọc decoded artifacts).
- `:core:test` — 29 tests green (12 mới: pack shapes, rect mirroring,
  `i()`/`s()`/`r()` semantics, spawn/collision/player-fall/run).
- LWJGL3 desktop + Android emulator: Altaïr renders (composite
  multi-module đúng), idle/run/jump hoạt động, camera follow, ep/er
  layers pixel-đúng.

## Lỗ hổng đã biết (ghi trong code + report)

- `eu` (entry-8, 21×13) chưa render — layout/anchor chưa mine đủ
  (`inferred` backdrop; tile call site `k.java:4505` cần anchor mining).
- Palette variants `aA`/`az`/`aP` chưa port — một số NPC/prop hiện
  palette-00 (cyan sạch) thay vì biến thể màu đúng.
- Player anim picks (S_IDLE=0, S_RUN=15, S_JUMP=22, S_FALL=24) là
  `inferred` — đúng index-space nhưng chưa mapped từ `g.e()` arms.
- Friction/air-accel split là `inferred` (J2ME typical), sẽ thay bằng
  verbatim khi port `g.e()` đầy đủ.
- Entity FSMs chưa chạy — NPCs đứng anim 0 tại vị trí mined (đúng nghĩa
  của slice này).

# Liên kết

- Converter: `rewrite/tools/convert_slice1.py`
- Runtime: `rewrite/core/src/main/kotlin/com/acrebuild/core/{Clip,Entity,LevelPack,Level0World,PackReader}.kt`
- gdx: `rewrite/gdx/.../{Level0Game,Level0Renderer,Level0InputBridge}.kt`
- Tests: `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt`
