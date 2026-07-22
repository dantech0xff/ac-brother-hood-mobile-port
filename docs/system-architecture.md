# Kiến Trúc Hệ Thống

## Mục tiêu tài liệu

Tài liệu này tách bạch hai lớp:

1. kiến trúc legacy đã khôi phục từ JAR Java ME;
2. kiến trúc đích được đề xuất cho bản rewrite mobile hiện đại.

Hai phần này không được nhập nhằng với nhau.

## 1. Kiến trúc legacy đã khôi phục

### Khởi động

`GloftASBR.startApp()` khởi tạo MIDlet, đọc property, rồi tạo controller/game canvas.
Luồng khởi động và vòng lặp chính đi qua `k` và `j`. Đây là sự thật đã khôi phục từ
bytecode và decompile, không phải suy đoán.

### Vai trò các class chính

| Class | Vai trò khôi phục |
|---|---|
| `GloftASBR` | MIDlet lifecycle và bootstrap. |
| `j` | Canvas/engine: game loop, input, math, rendering, pack loader, LZMA. |
| `k` | Game controller: state machine, level load, UI, save/load, progression. |
| `i` | Base actor/entity: AI, collision, animation, physics, event handling. |
| `g` | Player/controllable actor và combat/movement. |
| `b` | Sprite/font/palette codec và render helper. |
| `f` | IGP/cross-promotion và `platformRequest()`. |
| `e` | Audio manager. |
| `a` | Animation state/frames/loop/timekeeping. |
| `c` | Marker/object registry. |
| `d` | Gameplay constants. |
| `h` | Duration table cho audio slots. |

### Dòng dữ liệu runtime

```text
JAR/class bytes
  -> structured/simple/fallback sources
  -> inventory + javap
  -> resource packs
  -> decoded text/sprite/audio/PNG metadata
  -> reconstructed-project/
```

### Resource pipeline

- Pack index được đọc theo header little-endian.
- Entry có marker `>=127` được giải LZMA-alone.
- String table dùng header count + end offsets.
- Sprite/font binary bắt đầu bằng magic `0x05df`.
- Static sprite decoder parse 84/84 input tới EOF; 4.371/4.376 module có pixel
  được xuất thành 12.102 palette PNG, một asset được đánh dấu partial và năm
  module không xuất ảnh đã được account riêng.
- Level record decoder xuất `reconstructed-project/resources/levels-decoded/`
  với 10 files, 34,570,387 bytes, tree SHA
  `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60` và
  manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`.
- `dataIGP` chứa các PNG hợp lệ và text run printable.

## 2. Kiến trúc đích được đề xuất

Phần này là design recommendation, không phải code hiện có.

### Stack đề xuất

- Shared core: Kotlin.
- Render/game loop: LibGDX.
- Launcher: Android và iOS mỏng.
- Simulation: fixed-step deterministic.
- Content pipeline: offline conversion có provenance.

### Module boundaries

| Module | Trách nhiệm |
|---|---|
| `core` | State machine, combat, AI, collision, mission flow. |
| `content` | Asset chuẩn hóa từ resource đã khôi phục. |
| `runtime` | Input, audio, save, renderer, timing. |
| `launcher-android` | Bootstrap platform. |
| `launcher-ios` | Bootstrap platform. |
| `pipeline` | Chuyển đổi resource, hash, manifest, provenance. |

### Nguyên tắc thiết kế

- Một source of truth cho content đã chuyển đổi.
- Không đọc trực tiếp JAR gốc ở runtime.
- Deterministic simulation cho mọi platform.
- Asset đã chuyển đổi phải versioned và có provenance.

## 3. Ranh giới sự thật

### Đã chứng minh

- 12 class, 666 method, 1.009 field.
- `j`, `k`, `i`, `g`, `b`, `f` là các trụ cột hành vi chính.
- 17 pack, 260 entry, 24 LZMA, 214 raw, 265 string, 84 sprite, 13 MIDI, 18 WAV, 29 PNG IGP.
- 10 level-record files được xuất và kiểm định bằng managed manifest riêng.

### Suy luận có kiểm soát

- Semantic family đã có cho toàn bộ 238 payload không rỗng; một số field/subtype
  trong object/script stream còn cần call-site proof bổ sung.
- Level slot `3` được chứng minh không có runtime consumer; nó chỉ bị đọc-bỏ khi
  seek. Transform meaning và provenance exporter/legacy vẫn chưa biết.

### Chưa biết

- Một số object/entity và block/script record subtype chưa có field schema đầy đủ.
- Tên codec sprite gốc không thể khôi phục chắc chắn từ symbol bị obfuscate.

## 4. Điều không nên làm

- Không coi decompile là source gốc.
- Không gộp proposal mobile mới với legacy facts.
- Không ghi runtime plan như thể nó đã được triển khai.
