---
date: 2026-07-22
artifact: assassins_creed_-_br_320x240_136711.jar
sha256: 711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383
analysis: static-only
---

# Báo cáo dịch ngược Assassin's Creed Java ME

## Tóm tắt

Đây là `Assassin's Creed - Brotherhood` của Gameloft, MIDlet 1.2.7 cho Java ME MIDP 2.0 / CLDC 1.0. JAR có 12 class đã bị obfuscate, 666 method, 1.009 field, 114.642 JVM instruction chiếm 237.112 byte trong các `Code` attribute. Phân tích tĩnh đã giải nén 17 resource pack, 260 entry nội bộ, 265 chuỗi UI/cốt truyện, 31 file âm thanh và 29 PNG quảng bá; 238/238 payload không rỗng có semantic family. Không thực thi MIDlet.

JAR gốc: 1.125.469 byte, 37 ZIP entry, không mã hóa và không có chữ ký JAR trong `META-INF`. Class dùng bytecode version 45.3 (Java 1.1) với `StackMap` dành cho CLDC; manifest ghi được build bởi Sun JDK 1.4.2.

## Manifest và cấu hình thiết bị

| Thuộc tính | Giá trị |
|---|---|
| MIDlet | `GloftASBR` |
| Tên | `Assassin's Creed - Brotherhood` |
| Vendor | `Gameloft SA` |
| Version | `1.2.7` |
| Profile / configuration | `MIDP-2.0` / `CLDC-1.0` |
| Touch | `True` |
| Softkeys | `ReverseSoftkeys: True`, không dùng native commands |
| LCD target | manifest `240×400`; engine chạy landscape `400×240` |
| Biểu tượng | PNG indexed-color `32×32` |

Tên file có nhãn `320x240`, nhưng code đặt canvas và IGP thành `400x240`. Khả năng đây là nhãn phân phối không khớp biến thể thiết bị, hoặc game xoay màn hình từ target `240×400`.

Manifest không có `HAS-BLOOD`. Vì `GloftASBR.startApp()` bắt lỗi khi thuộc tính thiếu và đặt `k.bK = false`, hiệu ứng máu bị tắt trừ khi file JAD bên ngoài cung cấp thuộc tính này.

## Kiến trúc class

| Class | Field / method | Vai trò suy ra từ code |
|---|---:|---|
| `GloftASBR` | 3 / 4 | MIDlet lifecycle, đọc property, tạo game canvas |
| `a` | 14 / 14 | trạng thái animation: frame, loop, thời lượng và vẽ sprite |
| `b` | 108 / 61 | parser/render sprite Gameloft, font bitmap, palette và RLE pixel |
| `c` | 13 / 6 | registry tối đa 400 marker/object không gian theo ID |
| `d` | 2 / 1 | bảng hằng số gameplay |
| `e` | 5 / 6 | audio manager cho 34 slot MIDI/WAV |
| `f` | 158 / 49 | Gameloft IGP/cross-promotion, menu quảng bá và `platformRequest()` |
| `g` | 71 / 57 | actor điều khiển được (Ezio), movement/combat và trạng thái cheat |
| `h` | 1 / 1 | bảng thời lượng 34 âm thanh tính bằng ms |
| `i` | 206 / 225 | base actor/entity: AI, collision, animation, vật lý và event |
| `j` | 91 / 88 | engine Canvas: game loop, input, math, drawing, pack loader và LZMA |
| `k` | 337 / 154 | game controller: state machine, UI, level loading, save/load, rendering |

Luồng khởi động: `GloftASBR.startApp()` → `new k(MIDlet, Display)` → constructor `j` cấu hình input → thread game loop. Mỗi frame mục tiêu cách nhau 62 ms, xấp xỉ 16 FPS; delta time bị chặn tối đa 1.000 ms sau khi pause/resume.

Artifact giữ bốn view bổ sung lẫn nhau:

- Structured Java: 38.887 dòng; có đúng một hard stub tại `i.aV()`.
- Simple Java: 44.988 dòng; chứa trọn body 1.382 dòng của `i.aV()`.
- Fallback: 128.038 dòng, giữ representation gần instruction cho control-flow khó.
- `javap -c -p -s -constants -l -verbose`: exact bytecode authority cho cả 12 class.

## Resource pack

Format được xác nhận từ `j.class`:

1. Header little-endian chứa tổng entry, số part và entry bắt đầu của từng part.
2. Mỗi part chứa bảng offset 32-bit.
3. Byte đầu entry là marker. Marker `>=127` nghĩa phần còn lại là LZMA-alone; game trừ `127` để lấy marker gốc.
4. Pack `3` chia thành `3`, `3.1`, `3.2`.

Kết quả giải mã:

| Nội dung | Kết quả |
|---|---:|
| Pack | 17 |
| Entry indexed | 260 |
| Entry có dữ liệu / rỗng | 238 / 22 |
| Entry LZMA / raw | 24 / 214 |
| Gameloft sprite binary | 84, tổng 4.376 module |
| Static sprite recovery | 82 asset full, 2 partial; 4.369 pixel module → 12.098 PNG palette variant |
| Payload signature-generic | 114; semantic pass phân loại 114/114 |
| UTF-8 string table | 9, tổng 265 chuỗi |
| MIDI / WAV | 13 / 18 |
| Dung lượng sau decode | 2.707.152 byte |

Level record pass tĩnh đã parse exact EOF cho đủ tám pack: slot `0` có 4.286
entity/object record; slot `7` có 144 script group, 510 lane, 2.366 event và
3.705 instruction. Slot `3` là plane 2-bit companion dài đúng
`ceil(primaryTiles/4)` nhưng không có runtime consumer trong 12 class.

Ý nghĩa pack:

- `0`: ba MIME type `audio/x-wav`, `audio/midi`, `audio/x-amr`.
- `1`–`5`: sprite/font/UI và module mapping. `pack-1/entry-002` là Unicode→glyph hash table 609 `short`; pack `4`–`5` có năm sprite-module substitution table.
- `3`: 75 slot sprite, 62 slot có dữ liệu; lưu thành ba file vật lý.
- `6`–`13`: tám bộ level, mỗi bộ 14 entry cho map, actor, collision và script dữ liệu.
- `14`: một bảng UI/credits và tám bảng hội thoại nhiệm vụ.
- `15`: 13 sprite pack theo level/cảnh.
- `16`: cosine Q8 65 phần tử và square-root Q4 256 phần tử; cả hai khớp công thức fixed-point.
- `17`: 34 slot audio, gồm 13 MIDI, 18 WAV PCM mono 8-bit 8 kHz và ba slot rỗng.
- `999`: catalog 20 file resource cùng kích thước; mọi kích thước khớp archive.

## Chuỗi UI và cốt truyện

`pack-14/entry-000` có 127 chuỗi: menu, options, help, achievement, credits, copyright, mission result và hướng dẫn touch. Các entry `001`–`008` chứa 138 chuỗi hội thoại/nhiệm vụ:

1. Rome, Colosseum — giết Wolfmen, cứu Claudio, đối đầu Romulus.
2. Rome — thoát bằng flying machine.
3. Florence — giết Lucrezia, cứu Caterina.
4. Florence — truy đuổi và giết Juan Borgia.
5. Rome — dùng flying machine phá armada.
6. Venice — giết Baron Octavien de Valois.
7. Rome, Pantheon — giết Michelotto.
8. Rome, Colosseum — giết Cesare Borgia và lấy lại Apple of Eden.

Credits lộ tên producer, game designer, artist, programmer, sound director/designer và nhóm localization. Toàn bộ chuỗi giữ nguyên index trong `pack-14/all-strings.json`, thuận tiện cho dịch hoặc tra cứu cross-reference với `k.d(table, index)`.

## Input, lưu game, audio và debug

- Lớp engine còn bảng key/D-pad/softkey, nhưng callback key của biến thể này bị override rỗng; input thực tế là touch-first. Touch được biến đổi sang hệ tọa độ landscape `400×240`; virtual pad và gesture nằm trong `k`.
- Save game dùng Java ME RMS store `/ASBR`, đúng một record 512 byte. IGP dùng store riêng `igp19`.
- Audio dùng `javax.microedition.media.Manager`, phân loại bằng byte đầu `RIFF`/`MThd`, phát mỗi clip một lần và quản lý music/SFX riêng.
- Có debug/cheat overlay ẩn: 20 lần chạm liên tiếp vùng góc trên-trái `60×60` mở menu touch gồm `Free fly`, `God Mode`, `FPS OPEN/close`, `Open all level!`; chạm góc trên-phải để đóng.
- Code còn chín chuỗi cheat bằng input bitmask và các debug string như `Set Boss Speed=`, memory total/free, lỗi map/sprite/actor.

## IGP và bề mặt mạng

`f.class` là Gameloft In-Game Promotion 2.1. Nó đọc `/dataIGP`, hiển thị catalog và gọi `MIDlet.platformRequest()` khi người dùng chọn quảng cáo. Constant pool chứa endpoint lịch sử `ingameads.gameloft.com/redir`; không thấy import hoặc call tới `Connector`, `HttpConnection`, socket hay datagram, nên JAR không tự mở kết nối mạng trực tiếp trong code đã phân tích.

`dataIGP` đã bóc được 29 PNG hợp lệ và 880 đoạn ASCII. Nội dung quảng bá gồm Real Football, Resident Evil: Uprising, Blokus, Shrek Forever After, Ferrari GT 2, Splinter Cell Conviction, Block Breaker Deluxe 2, Miami Nights 2, Brain Challenge 3 và Iron Man. Không thấy secret, credential, token hoặc dữ liệu cá nhân hard-code.

## Artifact đầu ra

- `reconstructed-project/src/{structured,simple,fallback}/`: ba source view đủ 12 class.
- `reconstructed-project/bytecode/`: exact static disassembly.
- `reconstructed-project/inventory/`: method/field/call/access/dependency/string và semantic alias JSON.
- `reconstructed-project/resources/{archive,decoded}/`: archive nguyên trạng và 17 pack đã decode, audio, text/semantic JSON, 29 PNG IGP, metadata/hash.
- `reconstructed-project/resources/sprites-decoded/`: module/palette PNG và metadata section/range/hash; bảy non-output module được account.
- `reconstructed-project/reconstruction-manifest.json` và `verification-report.json`: provenance và audit tĩnh.
- `scripts/`: extractor, package builder, bytecode inventory, sprite decoder và verifier tái lập được.

Chạy lại:

```bash
python3 scripts/extract-java-me-resource-packs.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar/resources-decoded

python3 scripts/build-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar \
  reconstructed-project

python3 scripts/inventory-java-me-bytecode.py \
  reconstructed-project/bytecode reconstructed-project/inventory

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded \
  --verify-rerun

python3 scripts/verify-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar reconstructed-project \
  --report reconstructed-project/verification-report.json
```

Các command trên chỉ đọc JAR như ZIP/class data; không command nào khởi chạy
MIDlet hoặc nạp class mục tiêu.

## Phần nghiên cứu tĩnh có thể mở rộng

1. Tiếp tục đặt authoring name cho field/opcode subtype còn opaque bằng call-site proof.
2. Chỉ reinterpret slot `3` nếu có source/editor evidence mới; legacy runtime-unused đã được chứng minh.
3. Đối chiếu mọi sprite branch còn partial với `b` bytecode; không dùng chạy game làm lối tắt.
4. Nếu có JAD gốc do giảng viên cung cấp, kiểm kê nó như artifact tĩnh riêng.

## Câu hỏi chưa giải quyết

- File JAD gốc có thể cung cấp thêm property như `HAS-BLOOD`, operator hoặc URL mà JAR không chứa hay không?
- Một số object/entity và block/script record subtype còn thiếu tên field chính xác.
- Tên thương mại/canonical của các sprite codec không tồn tại trong symbol đã obfuscate.
- Runtime parity không được đo vì yêu cầu tuyệt đối không chạy JAR.
