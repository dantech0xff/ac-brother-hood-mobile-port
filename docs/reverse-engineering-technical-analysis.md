---
artifact: assassins_creed_-_br_320x240_136711.jar
sha256: 711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383
analysis_mode: static-only
game_executed: false
date: 2026-07-22
---

# Technical analysis và hướng dẫn dịch ngược

## 1. Kết quả cuối

Artifact là game Java ME **Assassin's Creed: Brotherhood** của Gameloft,
MIDlet version `1.2.7`, MIDP 2.0 / CLDC 1.0. Bản khôi phục tĩnh bao phủ:

- 37/37 JAR entry;
- 12/12 class, 666/666 method và 1.009/1.009 field;
- structured, simple, fallback và exact bytecode view cho mọi class;
- 17/17 resource pack, 260/260 slot;
- 265 string, 84 sprite binary/4.376 module, 13 MIDI, 18 WAV và 29 PNG IGP;
- 83 sprite asset full + 1 partial; 4.371 pixel module xuất thành 12.102 PNG
  palette variant, bảy module không xuất ảnh được account rõ;
- semantic family cho 238/238 payload không rỗng; 114/114 payload từng chỉ có
  signature generic đã được phân loại (106 trực tiếp/cao, 8 suy luận);
- full simple/fallback/bytecode body cho hard case `i.aV()`.

MIDlet không được chạy trong bất kỳ bước nào. Kết quả “full” ở đây có nghĩa mọi
bytecode và resource có trong artifact đều được bảo toàn/kiểm kê, không có nghĩa
phục hồi được tên symbol, comment hoặc project build gốc đã bị loại trước khi
đóng JAR.

## 2. Ranh giới bắt buộc

Không dùng các thao tác sau:

```text
java -jar <file>
MIDlet runner / Java ME emulator
Android/iOS simulator để chạy game gốc
dynamic class loading / reflection để khởi tạo class
instrumentation hoặc network request từ game
```

Các tool được dùng chỉ xem JAR như dữ liệu:

- `zipfile`: đọc central directory và bytes;
- JADX: class-to-source decompiler;
- `javap`: disassembler/class metadata reader;
- Python standard library: hash, LZMA, PNG CRC, parser và JSON inventory.

`javap -classpath jar ClassName` không gọi `main`, không tạo MIDlet và không chạy
static initializer; nó chỉ đọc class file.

## 3. Chain of custody

### Artifact identity

```bash
shasum -a 256 assassins_creed_-_br_320x240_136711.jar
```

Expected:

```text
711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383
```

Thông số:

| Thuộc tính | Giá trị |
|---|---|
| Size | 1.125.469 byte |
| ZIP entries | 37 |
| Class | 12 |
| Signature files | Không có `.SF/.RSA/.DSA` trong `META-INF` |
| Class version | 45.3, Java 1.1-era bytecode + CLDC StackMap |
| Build marker | Sun JDK 1.4.2 trong manifest |

Luôn kiểm hash trước và sau quy trình. Không sửa hoặc repack JAR gốc. Mọi output
được ghi sang `plans/research/` hoặc `reconstructed-project/`.

## 4. Toolchain tái lập

Phiên bản đã dùng cho bản giao:

| Tool | Version | Vai trò |
|---|---|---|
| Python | 3.14.3 | ZIP, parser, LZMA, hash, inventory, verification |
| JADX CLI | 1.5.3 | Ba decompilation mode |
| `javap` | 20.0.1 | Exact static class/disassembly view |

Extractor chỉ dùng Python standard library; không cần tải dependency runtime.

## 5. Cấu trúc output

```text
reconstructed-project/
  README.md
  reconstruction-manifest.json
  verification-report.json
  src/
    structured/       # Java dễ đọc
    simple/           # linear/goto, không hard stub
    fallback/         # raw instruction-oriented
  bytecode/           # javap verbose + disassembly
  inventory/
    summary.json
    methods.json
    fields.json
    calls.json
    field-accesses.json
    dependencies.json
    string-constants.json
  resources/
    archive/           # bản trích mọi non-directory JAR entry
    decoded/           # 17 pack + JSON/audio/PNG/hash
    sprites-decoded/   # module/palette PNG + section/range provenance

plans/research/assassins-creed-jar/
  extracted/
  jadx-original/
  jadx-simple/
  jadx-fallback/
  resources-decoded/
```

## 6. Quy trình từng bước

### Bước 1 — Intake và archive inventory

Đọc ZIP central directory, manifest, entry name, compressed/uncompressed size và
CRC. Không dùng launcher. Kết quả canonical được builder ghi vào
`reconstruction-manifest.json`.

Các phát hiện manifest:

| Property | Giá trị/ý nghĩa |
|---|---|
| `MIDlet-1` | Entry class `GloftASBR` |
| `MIDlet-Name` | Assassin's Creed - Brotherhood |
| `MIDlet-Vendor` | Gameloft SA |
| `MIDlet-Version` | 1.2.7 |
| Profile/config | MIDP-2.0 / CLDC-1.0 |
| Touch | `True` |
| Display target | Manifest 240×400; engine dùng landscape 400×240 |
| `HAS-BLOOD` | Không có trong JAR manifest; code fallback về `false` khi property thiếu |

Tên phân phối chứa `320x240`, manifest ghi `240x400`, code/IGP dùng `400x240`.
Đây là mismatch có thật; không tự chọn một giá trị rồi xóa bằng chứng còn lại.

### Bước 2 — Multi-view decompilation

Tạo ba view riêng:

```bash
jadx --decompilation-mode restructure --show-bad-code --no-res \
  --output-dir plans/research/assassins-creed-jar/jadx-original \
  assassins_creed_-_br_320x240_136711.jar

jadx --decompilation-mode simple --show-bad-code --no-res \
  --output-dir plans/research/assassins-creed-jar/jadx-simple \
  assassins_creed_-_br_320x240_136711.jar

jadx --decompilation-mode fallback --show-bad-code --no-res \
  --output-dir plans/research/assassins-creed-jar/jadx-fallback \
  assassins_creed_-_br_320x240_136711.jar
```

Không dùng một decompiler output làm source of truth duy nhất:

| View | Ưu điểm | Rủi ro | Cách dùng |
|---|---|---|---|
| Structured/restructure | Dễ đọc, restore loop/switch/if | Có thể mất block hoặc sai type | Semantic analysis chính |
| Simple | Giữ label/goto và đủ method lớn | Không compile như Java thường | Kiểm control-flow |
| Fallback | Gần instruction nhất | Rất dài, register-style | Resolve nhánh khó |
| `javap` | Descriptor/offset/opcode chính xác | Không có high-level semantics | Authority cuối |

### Bước 3 — Tách resource pack

```bash
python3 scripts/extract-java-me-resource-packs.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar/resources-decoded
```

Extractor thực hiện:

1. đọc header/offset little-endian của pack;
2. ghép split pack `3`, `3.1`, `3.2` theo logical index;
3. tách byte marker;
4. decode entry marker `>=127` bằng LZMA-alone;
5. detect string/sprite/MIDI/WAV/signature;
6. giữ empty slot thay vì bỏ qua;
7. ghi size, SHA-256, marker, compression, detected type;
8. gắn semantic family/confidence và xuất dimension, glyph-map, module-remap,
   cosine/square-root JSON;
9. parse file `0` MIME, `999` catalog;
10. scan `dataIGP`, verify PNG chunk CRC và tách image.

`skipped` hoặc `decode_errors` làm command trả nonzero; builder từ chối summary
có lỗi. Format chi tiết và 14-slot level schema nằm tại
[`resource-formats.md`](./resource-formats.md).

### Bước 4 — Dựng package và disassembly

```bash
python3 scripts/build-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar \
  reconstructed-project
```

Script:

1. copy ba source view cho 12 class;
2. trích raw archive trực tiếp từ ZIP JAR và copy decoded resources;
3. gọi `javap -c -p -s -constants -l -verbose` cho từng class;
4. parse class summary để đếm method/field/interface;
5. hash từng source view và disassembly;
6. sinh manifest với `game_execution_performed=false`.

Mỗi lần chạy, builder chỉ reset các subtree nó sở hữu (`src` ba view,
`bytecode`, `resources/archive`, `resources/decoded`); output sprite/inventory/doc
không bị xóa. Không cần một thư mục `extracted/` dựng thủ công.

Builder chỉ chuẩn hóa dòng đầu `Classfile <absolute-path>` của `javap` thành
`Classfile <jar-name>!/<class>.class` để hash không phụ thuộc vị trí workspace;
descriptor, constant pool, opcode, offset và mọi phần disassembly khác giữ nguyên.

Kết quả source:

| View | Lines | Hard stub |
|---|---:|---:|
| Structured | 38.887 | 1 |
| Simple | 44.988 | 0 |
| Fallback | 128.038 | 0 |

### Bước 5 — Full method/call/field inventory

```bash
python3 scripts/inventory-java-me-bytecode.py \
  reconstructed-project/bytecode \
  reconstructed-project/inventory
```

Parser đọc declaration, JVM descriptor, flags, invoke opcode, field opcode,
constant-pool class và string. Nó đồng thời chép
[`java-me-semantic-aliases.json`](../scripts/java-me-semantic-aliases.json) vào
inventory và fail nếu alias trỏ tới ID không tồn tại; clean rebuild vì vậy không
phụ thuộc file hand-authored còn sót trong output. Kết quả:

| Metric | Count |
|---|---:|
| Unique method ID | 666 |
| Field | 1.009 |
| JVM instruction | 114.642 |
| JVM `Code` bytes | 237.112 |
| Call site | 7.154 |
| Internal/external call | 5.875 / 1.279 |
| Field access | 25.550 |
| External class reference | 29 |
| Constant string | 124 |

Luôn dùng `class.name:descriptor` làm ID vì obfuscation tạo nhiều overload trùng
tên. Ví dụ `j.a(String)` và các `j.a(...)` draw/math helpers không cùng nghĩa.

### Bước 6 — Dựng module/palette sprite hoàn toàn tĩnh

```bash
python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded \
  --verify-rerun
```

Tool chỉ nhận decoded `.bin`/metadata, không nhận JAR và không load class. Nó
dịch loader/pixel branch của `b`, giữ byte range/hash, kiểm EOF cho 84 input,
validate lại toàn bộ PNG và so managed-artifact manifest giữa hai lần chạy.
`full/partial` được tính theo pixel-bearing branch, không theo việc parser chỉ
đọc được header.

### Bước 7 — Semantic reconstruction

Thứ tự đọc hiệu quả:

1. `GloftASBR`: entry/lifecycle;
2. `j`: loop, platform helpers, pack/string/object parser;
3. `k`: screen FSM, level loader, update/render/save/UI;
4. `g`: player controller;
5. `i`: entity fields, update dispatcher, AI/trigger handlers;
6. `b/a`: sprite/font/animation;
7. `e/h`: audio;
8. `f`: IGP/external link;
9. `c/d`: waypoint và combat tables.

Với mỗi alias:

- tìm writer và reader trong `calls.json`/`field-accesses.json`;
- đối chiếu type/descriptor trong `javap`;
- kiểm structured source với simple/fallback ở branch phức tạp;
- gắn confidence `proven`, `high-confidence`, `inferred` hoặc `unknown`;
- không rename raw recovered source nếu alias chưa chắc.

Kết quả class/field/screen/input map nằm tại
[`symbol-map.md`](./symbol-map.md).

Hai level stream phức tạp đã được tách riêng thành
[`level-record-formats.md`](./level-record-formats.md): 4.286 entity record và
144 script group/510 lane/2.366 event/3.705 instruction đều parse tới exact EOF.

### Bước 8 — Xử lý decompiler failure

Structured output có đúng một hard failure: `i.aV():void`.

Quy trình phục hồi:

1. lấy exact descriptor/flags/bytecode boundaries từ `javap`;
2. xác định caller bằng `calls.json` và invoke offset;
3. lấy full label graph từ simple/fallback;
4. map fallback hexadecimal labels sang decimal bytecode offsets;
5. dựng top-level `switch(S)` 56 case;
6. lập read/write/call set cho từng case;
7. viết pseudocode cho các state phức tạp, không tối ưu oddity chưa chứng minh;
8. giữ exact bytecode làm audit authority.

Kết quả: body simple 1.382 dòng, exact code 7.880 byte, một caller từ `i.I()`,
56/56 state có branch target. Xem
[`i-av-reconstruction.md`](./i-av-reconstruction.md).

### Bước 9 — Audit toàn package

```bash
python3 scripts/verify-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  reconstructed-project \
  --report reconstructed-project/verification-report.json
```

Verifier đọc tĩnh và fail nếu:

- SHA-256 JAR thay đổi;
- ZIP name/CRC/size lệch manifest;
- raw archive copy khác bytes trong JAR;
- thiếu class/source/bytecode hoặc line/hash đổi;
- source-tree hoặc bytecode aggregate khác baseline đã khóa;
- bất kỳ delivered `javap` nào khác fresh normalized `javap` sinh trực tiếp từ
  pinned JAR (12/12 class phải match);
- summary trong reconstruction manifest không khớp class records, inventory,
  Code-attribute parser hoặc resource evidence đã derive;
- method/field/call counts sai hoặc fresh inventory từ bytecode khác file giao;
- decoded payload thiếu hoặc SHA-256/size sai;
- fresh extraction trực tiếp từ JAR khác decoded tree được giao, hoặc summary có
  `skipped`/`decode_errors`/unclassified payload;
- pack/string/sprite/audio/IGP totals sai;
- sprite managed manifest/rerun/84 metadata/12.102 PNG khác baseline;
- simple source còn hard stub;
- manifest không còn `static-only`/`game_execution_performed=false`.

Verifier pin SHA-256 trước khi mở ZIP/gọi tool. Nếu input không đúng artifact,
nó fail closed. Khi có `--report`, file report được atomic-replace bằng trạng thái
fail/incomplete ngay đầu run, nên exception hoặc input lỗi không để lại `ok:true`
từ một lần kiểm cũ.

Builder/verifier/inventory/sprite decoder dùng chung transient directory lock tại
reconstruction root; extractor và builder dùng lock riêng ở analysis root.
Mutating step invalidate report trước khi publish. Vì vậy một run song song không
thể tạo race trong đó verifier cũ ghi pass lên package vừa bị thay đổi.
Builder dựng đủ sáu cây do nó sở hữu cùng manifest trong sibling staging tree,
sau đó publish theo manifest-last với backup/rollback cho lỗi thông thường;
failure trước publish giữ nguyên package live. Verifier đồng thời từ chối symlink
ở cả parent `src`/`resources` lẫn mọi protected subtree trước khi đối chiếu nội
dung.

Bản giao hiện có được ghi lại trong
[`final-verification.md`](../plans/260722-1922-assassins-creed-reconstruction/reports/final-verification.md)
với `ok: true` và `failures: []`.

## 7. Phát hiện kiến trúc

### Class roles

| Class | Vai trò recovered |
|---|---|
| `GloftASBR` | MIDlet lifecycle/bootstrap |
| `j` | Canvas runtime, 62 ms loop, pack/LZMA/string/object I/O |
| `k` | Game/screen FSM, loader, UI, camera, render, save, audio wrappers |
| `i` | Generic actor/entity/trigger, physics/collision/AI |
| `g` | Player/Ezio movement/combat/parkour |
| `b` | Sprite/font/palette/pixel parser/renderer |
| `a` | Sprite animation player |
| `c` | Waypoint + 400-node registry |
| `d` | Combat/AI tables |
| `e` | 34-slot audio manager |
| `h` | Audio-duration table |
| `f` | IGP/cross-promotion controller |

### Main flow

```text
startApp -> new k -> j thread -> run -> paint -> k frame dispatcher
gameplay -> k world update -> i.I per entity -> type/state handler
render -> tile layers -> depth-sort entities -> i.F -> b sprite modules
```

Loop target là 62 ms (~16,13 tick/s); delta bị clamp 1.000 ms sau pause/resume.
Rewrite không nên gắn simulation trực tiếp vào refresh 60/120 Hz.

## 8. Platform/API footprint

Java ME classes được tham chiếu:

- LCDUI: `Canvas`, `Command`, `CommandListener`, `Display`, `Displayable`, `Font`,
  `Graphics`, `Image`;
- Media: `Manager`, `Player`;
- MIDlet: `MIDlet`;
- Persistence: `RecordStore`.

Không thấy `Connector`, `HttpConnection`, socket hoặc datagram trong constant
pool/call inventory. `f` chỉ chuyển URL quảng cáo lịch sử qua
`MIDlet.platformRequest()`.

## 9. Gameplay/data đã đọc được

### Input

- Touch coordinates xoay thành `gameX=inputY`, `gameY=240-inputX`.
- Virtual pad, action zones và gesture dùng bitmask held/pressed/released/repeat.
- `k` override key callbacks thành empty; biến thể này thực tế touch-first.

### Save

- RMS store `/ASBR`, record 1, buffer cố định 512 byte.
- IGP store riêng `igp19`.
- In-memory entity resume snapshot dùng 22 byte/entity, không phải toàn RMS.

Byte map, reset/corruption behavior và phần reserved được mô tả tại
[`save-format.md`](./save-format.md).

### Audio

- 34 slot; `0..9` music, `10..33` SFX.
- 13 MIDI, 18 WAV PCM mono 8-bit/8 kHz, 3 empty.
- Một Java ME `Player` tại một thời điểm, loop count 1.
- `h` giữ duration millisecond cho từng slot.

### Debug/cheat

20 lần tap vùng góc trên-trái `60×60` mở overlay:

- `Free fly`;
- `God Mode`;
- `FPS OPEN/close`;
- `Open all level!`.

Góc trên-phải đóng overlay. Còn chín input-bitmask cheat và debug strings như
`Set Boss Speed=`, memory total/free, map/sprite/actor errors. Rewrite release
phải gate toàn bộ dưới build flag dev-only.

### Story/content

Pack `14` entry 0 có UI/help/options/credits; entry 1–8 là tám mission dialogue:

1. Rome/Colosseum — Wolfmen, Claudio, Romulus;
2. Rome — escape bằng flying machine;
3. Florence — Lucrezia, Caterina;
4. Florence — Juan Borgia;
5. Rome — flying machine phá armada;
6. Venice — Baron Octavien de Valois;
7. Rome/Pantheon — Michelotto;
8. Rome/Colosseum — Cesare Borgia, Apple of Eden.

Mapping pack `/6`..`/13` theo thứ tự trên có confidence trung bình vì dựa trên
correlation string order; loader chỉ chứng minh sequence tám pack.

## 10. IGP và security surface

`f` là Gameloft In-Game Promotion 2.1:

- đọc `/dataIGP`;
- render catalog quảng cáo;
- lưu state `igp19`;
- gọi `platformRequest()` khi người dùng chọn item.

Static scan tách 29 PNG và 880 printable run. Không thấy credential, secret,
token hoặc personal data hard-code. Thiết kế mới phải loại IGP endpoint/flow;
asset quảng cáo được giữ trong archive forensic, không đi vào runtime content.

## 11. Cách đánh giá confidence

| Level | Điều kiện |
|---|---|
| Proven | Bytecode/parser và nhiều payload/call site cùng xác nhận. |
| High-confidence | Data flow thống nhất nhưng tên gốc đã mất. |
| Inferred | Size/order/symmetry phù hợp, thiếu consumer trực tiếp. |
| Unknown | Bytes/hash đã giữ nhưng field semantics chưa gán. |

Ví dụ:

- pack marker/LZMA: proven;
- `i.ak/al = world position`: high-confidence;
- level slot 3 = primary 2-bit companion plane và runtime không consume: proven;
  nghĩa transform/orientation cụ thể: inferred;
- canonical marketing name của sprite codecs: unknown.

## 12. Sai lầm cần tránh

- Chỉ đọc structured source rồi kết luận method bị mất.
- Đếm file thay vì JVM method descriptor; overload sẽ bị nhập nhằng.
- Bỏ empty resource slot; index sẽ lệch toàn bộ game data.
- Giải LZMA từ byte marker thay vì payload `entry[1:]`.
- Đọc pack integer big-endian; pack dùng little-endian.
- Tự đổi oddity bytecode vì trông “sai”, ví dụ duplicate call trong `i.aV`.
- Gọi mọi `i.aV` state là enemy AI; nhiều state là map trigger/controller.
- Đưa IGP/network cũ vào design mới.
- Tuyên bố decompiled Java compile được khi còn goto/type-inference corruption.

## 13. Residual unknowns

Phần chưa giải quyết không bị che giấu:

- original symbol/comment/local names không tồn tại trong class file;
- 114/114 payload detector từng gọi generic đã có semantic family; field schema
  của một số object/entity và block/script subtype vẫn chưa hoàn chỉnh;
- một pixel module còn partial (thiếu optional tail); assembled frame semantics
  vẫn tách khỏi module/palette recovery;
- level slot 3 không có runtime consumer trong 12 class; `j.o(int)` chỉ đọc-bỏ
  khi seek. Ý nghĩa transform và provenance exporter/legacy vẫn chưa chứng minh;
- JAD ngoài JAR có thể từng cung cấp deployment property;
- runtime parity không được đo vì JAR tuyệt đối không chạy.

Chi tiết định lượng nằm tại
[`reconstruction-completeness.md`](./reconstruction-completeness.md). Những gap này
không làm mất artifact: raw bytes, hashes, ba source view và exact bytecode đều đã
được lưu để tiếp tục nghiên cứu tĩnh.

## 14. Checklist tái lập

- [ ] Hash JAR đúng `711e...a383`.
- [ ] Không dùng `java`, emulator hoặc MIDlet launcher.
- [ ] JADX tạo đủ 12 file cho cả ba mode.
- [ ] Builder báo 12 class, 666 method, 1.009 field.
- [ ] Inventory có 666 unique method ID.
- [ ] Extractor báo 17 pack, 260 slot, không pack bị skip.
- [ ] Sprite decoder báo 84 input tới EOF, 83 full/1 partial/0 error và rerun
  manifest giống hệt.
- [ ] `src/simple/i.java` có `aV()` và không hard stub.
- [ ] Verifier trả `ok: true`, `failures: []`.
- [ ] Mọi semantic rename có confidence/evidence.
- [ ] Không gọi package hiện tại là source gốc hoặc buildable game.
