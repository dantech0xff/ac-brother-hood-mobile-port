# Báo cáo mức độ hoàn chỉnh của bản dịch ngược

## Kết luận

Bản khôi phục đã **bao phủ toàn bộ artifact có trong JAR** ở mức bytecode và
resource: 37/37 ZIP entry, 12/12 class, 666/666 method và 260/260 resource-pack
slot đều có dấu vết kiểm kê. Không có method nào bị bỏ im lặng. Method duy nhất
mà structured decompiler không dựng được, `i.aV()`, đã có đủ simple source,
fallback và disassembly bytecode.

Điều không thể gọi là “full source gốc” là tên class/method/field trước
obfuscation, comment, tên biến local, cấu trúc module/build ban đầu và file JAD
không có trong JAR. Những dữ liệu này không tồn tại trong artifact được giao nên
không decompiler nào có thể phục hồi nguyên văn. Vì vậy kết quả thành công được
định nghĩa là **full static behavioral artifact coverage**, không phải đồng nhất
byte-for-byte với source repository nội bộ của Gameloft.

## Danh tính artifact và ranh giới an toàn

| Thuộc tính | Giá trị |
|---|---|
| File | `assassins_creed_-_br_320x240_136711.jar` |
| Kích thước | 1.125.469 byte |
| SHA-256 | `711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383` |
| Chế độ | Static-only |
| JAR/MIDlet đã chạy | **Không** |
| Emulator/simulator/device | **Không** |
| Dynamic class loading | **Không** |

[`reconstruction-manifest.json`](../reconstructed-project/reconstruction-manifest.json)
đặt cờ máy đọc được `game_execution_performed: false` và lưu CRC/kích thước của
từng ZIP entry.

## Bao phủ code

Mỗi class có bốn view bổ sung cho nhau:

1. `src/structured`: Java dễ đọc nhất, dùng để hiểu semantics.
2. `src/simple`: control flow tuyến tính/goto; không có hard stub.
3. `src/fallback`: instruction-oriented, dùng khi type/control flow bị hỏng.
4. `bytecode`: `javap -c -p -s -constants -l -verbose`, authority chính xác theo
   class file.

| Class | Method | Field | Structured lines | Simple lines | Fallback lines | Hard structured stub |
|---|---:|---:|---:|---:|---:|---:|
| `GloftASBR` | 4 | 3 | 46 | 51 | 86 | 0 |
| `a` | 14 | 14 | 163 | 175 | 848 | 0 |
| `b` | 61 | 108 | 2.201 | 2.715 | 8.477 | 0 |
| `c` | 6 | 13 | 67 | 85 | 207 | 0 |
| `d` | 1 | 2 | 7 | 12 | 81 | 0 |
| `e` | 6 | 5 | 72 | 106 | 196 | 0 |
| `f` | 49 | 158 | 2.116 | 2.739 | 7.687 | 0 |
| `g` | 57 | 71 | 6.099 | 6.410 | 15.465 | 0 |
| `h` | 1 | 1 | 6 | 10 | 149 | 0 |
| `i` | 225 | 206 | 20.174 | 22.364 | 63.797 | 1 |
| `j` | 88 | 91 | 1.435 | 1.795 | 4.738 | 0 |
| `k` | 154 | 337 | 6.501 | 8.526 | 26.307 | 0 |
| **Tổng** | **666** | **1.009** | **38.887** | **44.988** | **128.038** | **1** |

### Method `i.aV()`

- Descriptor `()V`, `private`, stack 6, locals 4.
- Exact bytecode dài 7.880 byte, offset `0..7879`.
- Chỉ có một caller: `i.I()` khi entity type `ax == 10`.
- Top-level dispatch có 56 state `S=0..55`.
- Structured view có stub, nhưng simple view giữ đủ body 1.382 dòng;
  fallback và bytecode giữ mọi branch/field access/call.
- Phân tích semantic riêng: [`i-av-reconstruction.md`](./i-av-reconstruction.md).

Do đó method này là rủi ro **đọc/biên dịch lại**, không phải mất bytecode hoặc mất
hành vi quan sát được.

### Chất lượng decompile

| Mức | Số method | Ý nghĩa |
|---|---:|---|
| Structured body hiện diện | 665/666 | Có body Java có cấu trúc; vẫn cần review warning. |
| Hard structured failure | 1/666 | `i.aV()`; đã bù bằng simple/fallback/bytecode. |
| Có tag `Code decompiled incorrectly` | 25/666 | Body hiện diện nhưng control flow cần đối chiếu. |
| Invalid-Java type inference bổ sung | 5 method | `f.d(int)`, `j.a(InputStream)`, `j.c(int,int)`, `k.R()`, `k.G(int)`. |
| Behavioral artifact available | 666/666 | Có ít nhất simple/fallback + exact bytecode cho mọi method. |
| Runtime-verified | 0/666 | Cố ý không chạy JAR theo yêu cầu. |

Danh sách 25 warning-tagged:

- `f`: 4 method liên quan IGP/string/layout.
- `g`: `e()`, `ay()`, `az()`, `n()`.
- `i`: 12 method trung tâm gồm `I()`, `F()`, collision/AI helpers.
- `k`: 5 method gồm frame dispatcher, state transition và level/entity loader.

Các method này không bị bỏ; `src/simple`, `src/fallback` và bytecode là đường
đối chiếu bắt buộc khi viết lại.

## Static inventory

[`inventory/`](../reconstructed-project/inventory/) lập chỉ mục toàn bộ class
file:

| Chỉ số | Giá trị |
|---|---:|
| Method declaration | 666 |
| Field declaration | 1.009 |
| JVM instruction | 114.642 |
| JVM `Code` bytes | 237.112 |
| Call site | 7.154 |
| Internal / external call site | 5.875 / 1.279 |
| Field-access site | 25.550 |
| External class reference | 29 |
| Constant string riêng biệt | 124 |

Các file `methods.json`, `fields.json`, `calls.json`, `field-accesses.json`,
`dependencies.json` và `string-constants.json` cho phép truy vết mà không chạy
class. `methods.json` có 666 ID duy nhất, bao gồm 13 constructor và 11 class
initializer.

## Bao phủ archive và resource

| Hạng mục | Kết quả | Trạng thái |
|---|---:|---|
| JAR entries | 37/37 | Đã kiểm kê CRC, packed/unpacked size. |
| Class files | 12/12 | Raw, 3 decompile view và bytecode. |
| Manifest | 1/1 | Đã trích xuất và phân tích. |
| Numeric resource files | 21 file vật lý | Gồm 17 logical pack, 3 split parts của pack 3, catalog/MIME. |
| Logical packs | 17/17 | Parse thành công. |
| Indexed pack slots | 260/260 | 238 non-empty, 22 empty hợp lệ. |
| LZMA/raw payload | 24/214 | Tất cả non-empty entry được tách. |
| String table | 9 | 265 chuỗi giữ nguyên index. |
| Sprite binary | 84/84 | 83 full, 1 partial, 0 error; 4.376 module inventoried, 4.371 pixel module → 12.102 PNG palette variant. |
| Level record JSON | 10/10 | 10 files, 34,570,387 bytes, tree SHA `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`, manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`. |
| MIDI/WAV | 13/18 | Tách thành file riêng; 3 audio slot rỗng. |
| `dataIGP` embedded PNG | 29 | CRC PNG, offset, size, dimension, SHA-256. |
| Payload detector gọi generic | 114 | Đã gán family 114/114: 106 trực tiếp/tin cậy cao; 8 primary 2-bit plane được chứng minh runtime-unused, transform meaning còn suy luận. |

Schema pack, string, typed object, sprite, audio và level được mô tả tại
[`resource-formats.md`](./resource-formats.md). Cả 14 slot của level pack `6`–`13`
đã được account; slot `3` dài chính xác `ceil(primaryTiles/4)` và được chứng minh
chỉ bị đọc-bỏ khi seek, không có runtime consumer. Grammar sâu của slot `0`/`7`
nằm tại [`level-record-formats.md`](./level-record-formats.md): 4.286 entity
record và 3.705 script instruction parse exact EOF. Font glyph map, năm
sprite-module remap và hai bảng toán fixed-point cũng đã có JSON/schema riêng.

## Bao phủ semantic

| Mục tiêu | Bao phủ | Evidence |
|---|---:|---|
| Vai trò class | 12/12 | [`symbol-map.md`](./symbol-map.md) |
| MIDlet lifecycle | Đã xác định | `GloftASBR` → `k` → `j.run/paint` |
| Screen FSM | State chính đã gắn nhãn | `k.l(int)`, `k.a()` |
| Entity/player FSM | Dispatcher và field contract đã xác định | `i.I()`, `i.aV()`, `g.e()` |
| Input | Touch transform và bitmask semantics | `k.pointer*`, `u/v/w/x` |
| Rendering | Tile → depth-sort entity → sprite/font | `k.b`, `i.F`, `b` |
| Audio | 34 slot, gating music/SFX, duration table | `e`, `h`, pack 17 |
| Save | RMS `/ASBR`, record 1, 512 byte | [`save-format.md`](./save-format.md), `k.e(boolean)` |
| Resource loading | Multipart pack/LZMA/typed data/string | `j`, extractor |
| External surface | IGP + `platformRequest`, không direct HTTP/socket | `f` |

## Những gì còn chưa biết chính xác

| Unknown | Granularity | Vì sao | Cách xử lý trong rewrite |
|---|---|---|---|
| Tên symbol gốc | 11 class obfuscated và phần lớn member | Bị xóa khi obfuscate. | Dùng semantic alias có confidence, không giả là tên gốc. |
| Original comments/local names | Toàn project | Class file không có debug/source metadata tương ứng. | Viết documentation mới dựa trên hành vi. |
| Tên field theo từng object/script subtype | Một phần field-level | Framing/opcode width đã chứng minh, nhưng obfuscation và consumer phức tạp làm mất authoring names. | Converter versioned + validation + giữ raw source. |
| Sprite codec canonical names | Codec label | Tên không tồn tại trong bytecode. | Đặt tên theo hành vi packing. |
| Một pixel module partial | 1 thiếu optional tail | Artifact/bytecode không cung cấp dữ liệu hoặc fill branch. | Giữ raw/range/hash; explicit partial gate, không invent pixels. |
| Level slot `3` provenance/transform meaning | Một slot trên 8 pack | Không có consumer đã được chứng minh; không thể phân biệt legacy feature với exporter residue. | Giữ raw/hash, archive-only mặc định; không invent semantics. |
| File JAD/property deployment | Ngoài JAR | Artifact không được cung cấp. | Mặc định an toàn; `HAS-BLOOD=false`. |
| Runtime parity | Toàn game | Bị loại khỏi scope do cấm chạy JAR. | Rewrite dùng static fixtures, golden conversion và deterministic tests. |

## Đánh giá ba yêu cầu bài tập

| Yêu cầu | Trạng thái | Tiêu chí |
|---|---|---|
| 1. Dịch ngược full dự án | **Đạt ở mức static artifact coverage** | 12/12 class, 666/666 method, 37/37 entry, 260/260 slot; unknown được ghi rõ. |
| 2. Technical analysis hướng dẫn dịch ngược | Được giao trong tài liệu riêng | Quy trình tái lập, evidence, command, validation và giới hạn. |
| 3. Technical design cho Android/iOS | Được giao trong tài liệu riêng | Chỉ có thiết kế; không code hoặc chạy game mới. |

Không tuyên bố source structured hiện tại compile trực tiếp. Mục tiêu số 1 được
đáp ứng bằng bộ khôi phục có thể kiểm toán, trong đó exact bytecode là lớp bảo
toàn cuối cùng và semantic docs là lớp diễn giải.
