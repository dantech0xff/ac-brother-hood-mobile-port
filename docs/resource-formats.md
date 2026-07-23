# Đặc tả tài nguyên đã dịch ngược

Tài liệu này mô tả các format đã khôi phục từ
`assassins_creed_-_br_320x240_136711.jar`. Toàn bộ kết luận đến từ phân tích
tĩnh; JAR/MIDlet không được chạy.

## Quy ước độ tin cậy

- **Đã chứng minh**: parser trong game và dữ liệu thực tế cùng xác nhận.
- **Tin cậy cao**: call site và hình dạng dữ liệu thống nhất, nhưng tên gốc đã
  mất do obfuscation.
- **Suy luận**: cấu trúc phù hợp mạnh, song chưa tìm thấy consumer trực tiếp.
- **Chưa biết**: byte đã được bảo toàn nhưng chưa gán được ngữ nghĩa trường.

Nguồn kiểm chứng chính:

- [`j.java`](../reconstructed-project/src/structured/j.java): mở pack, chuyển
  part, giải nén, typed-object và string table.
- [`b.java`](../reconstructed-project/src/structured/b.java): sprite, font,
  palette và pixel codec.
- [`k.java`](../reconstructed-project/src/structured/k.java): schema level và
  consumer của từng slot.
- [`extract-java-me-resource-packs.py`](../scripts/extract-java-me-resource-packs.py):
  extractor độc lập có validation biên, offset, CRC và hash.
- [`summary.json`](../reconstructed-project/resources/decoded/summary.json) và
  `pack-*/metadata.json`: bằng chứng trên toàn bộ dữ liệu.
- [`levels-decoded/summary.json`](../reconstructed-project/resources/levels-decoded/summary.json)
  và `manifest.json`: output canonical của decoder slot `0/7`; managed tree hiện
  tại có 10 files, 34,570,387 bytes, tree SHA
  `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`, manifest
  SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`.

## 1. Indexed resource pack

Các file số `1`–`17` là container little-endian. Pack `3` được chia thành ba
file vật lý `3`, `3.1`, `3.2`; các pack khác có một part.

### Header của part đầu

| Offset | Kiểu | Ý nghĩa |
|---:|---|---|
| `0x00` | `u16 LE` | Tổng số entry logic. |
| `0x02` | `u16 LE` | Số part vật lý. |
| `0x04` | `u16 LE[partCount]` | Index entry đầu tiên của từng part; phần tử đầu phải bằng `0`. |
| tiếp theo | `u32 LE[entriesInPart + 1]` | Bảng offset tuyệt đối trong part đầu. |

Mỗi part tiếp theo bắt đầu trực tiếp bằng bảng `u32 LE` offset riêng, không lặp
header tổng. Với part `p`, phạm vi entry là
`partStarts[p] .. partStarts[p+1]-1`; part cuối kết thúc ở `totalEntries-1`.
Entry `n` là lát byte `[offset[n], offset[n+1])`. Hai offset bằng nhau biểu thị
slot rỗng hợp lệ, không phải lỗi.

### Marker và compression

Byte đầu của entry không thuộc payload:

```text
marker < 127   -> payload = bytes[1..], không nén
marker >= 127  -> logicalMarker = marker - 127
                  payload = LZMA-alone(bytes[1..])
```

Đây là LZMA `FORMAT_ALONE`, tức payload tự chứa properties, dictionary size và
uncompressed-size header của LZMA-alone. Extractor chỉ ghi payload raw thay thế
nếu decoder báo lỗi và đồng thời ghi trường `error`; corpus hiện tại giải được
đầy đủ 24 entry LZMA.

### Validation bắt buộc

Một decoder tương thích phải từ chối pack nếu:

- `totalEntries == 0`, `partCount == 0` hoặc header vượt EOF;
- `partStarts[0] != 0`, dãy part không tăng, hoặc index vượt tổng entry;
- thiếu file part;
- bảng offset không tăng, offset đầu nằm trước cuối bảng, hoặc offset cuối vượt
  kích thước part.

Corpus đã kiểm kê 17 pack, 260 slot: 238 có dữ liệu, 22 rỗng, 24 LZMA và 214
raw. Dung lượng payload sau decode là 2.707.152 byte.

## 2. String table UTF-8

Format được chứng minh độc lập bởi `j.e(InputStream)`, `j.g(int)` và chín entry
trong pack `14`:

| Offset | Kiểu | Ý nghĩa |
|---:|---|---|
| `0x00` | `u32 LE` | `stringCount`. |
| `0x04` | `u32 LE[stringCount]` | End offset cộng dồn của từng chuỗi trong blob. |
| tiếp theo | `byte[]` | Các chuỗi UTF-8 nối liền, không có terminator. |

Start của chuỗi `i` bằng `0` nếu `i == 0`, ngược lại bằng `ends[i-1]`; end bằng
`ends[i]`. End cuối phải đúng bằng độ dài blob. Chuỗi rỗng được thể hiện bằng
hai end offset bằng nhau; runtime trả `null` cho lát dài 0.

Pack `14` có 265 chuỗi:

| Entry | Số chuỗi | Nội dung suy ra |
|---:|---:|---|
| `0` | 127 | UI, options, help, achievement, credits, touch tutorial. |
| `1`–`8` | 35, 13, 19, 22, 15, 10, 16, 8 | Hội thoại và mục tiêu của tám mission. |

JSON giữ nguyên index nằm tại
[`pack-14/all-strings.json`](../reconstructed-project/resources/decoded/pack-14/all-strings.json).

## 3. Typed-object stream

`j.a(InputStream)` đọc cây array có type động. Đây là format riêng, không phải
Java serialization.

```text
header       = u8
widthCode    = header >> 4
typeCode     = header & 0x07
length       = (header & 0x08) != 0 ? u16LE : u8
```

| `typeCode` | Kết quả | Cách đọc phần tử |
|---:|---|---|
| `0` | `byte[]` | `u8`, ép về byte. |
| `1` | `short[]` | signed byte khi `widthCode=0`, ngược lại `u16 LE`. |
| `2` | `int[]` | signed byte khi `0`, signed short khi `1`, ngược lại `u32 LE`. |
| `3`–`7` | nested array | Gọi đệ quy cùng parser; base element family lấy từ `typeCode & 3`. |

Nhánh nested còn chứa quyết định allocation dựa trên `widthCode == 2`; kiểu Java
cụ thể trong structured decompile bị nhiễu type-inference, vì vậy byte grammar và
đệ quy là phần đã chứng minh, còn type declaration chính xác của một số nested
array nên đối chiếu `src/simple/j.java` hoặc bytecode trước khi viết converter.

## 4. Sprite/font binary

84 entry bắt đầu bằng magic little-endian `df 05`. Ba trường đầu đã chứng minh:

| Offset | Kiểu | Field runtime | Ý nghĩa |
|---:|---|---|---|
| `0x00` | `u16 LE` | — | Version/magic `0x05df` (`1503`). |
| `0x02` | `u32 LE` | `b.aD` | Bit flags điều khiển độ rộng field và section có mặt. |
| `0x06` | `u16 LE` | `b.ab` | Số module. |

Sau header là record module biến độ dài. Byte tag đầu module chọn một trong các
nhánh `0`, `255`..`247`; mỗi nhánh quyết định có đọc pixel pointer/flags, kích
thước 8/16-bit, rectangle bổ sung hoặc alpha data hay không. Parser tiếp tục đọc
frame, animation và palette sections. Vì tag làm record thay đổi độ dài, không
được cắt record bằng một struct cố định.

Pixel decoder trong `b.a(byte[], int, int, int)` có các nhánh hành vi đã xác nhận:

- palette-indexed 8 bit;
- run-length encoding;
- packed index 4 bit, 2 bit và 1 bit;
- alpha/overlay đặc biệt;
- transform xoay/lật khi vẽ.

Decoder tĩnh [`decode-gameloft-sprites.py`](../scripts/decode-gameloft-sprites.py)
dịch trực tiếp loader và các nhánh pixel quan sát được. Nó đọc 84/84 input tới
đúng EOF, đối chiếu source size/SHA-256, kiểm kê 4.376 module và xuất 12.102 PNG
palette variant cho 4.371 module pixel. Kết quả là 83 asset full, 1 partial,
0 error; mọi PNG đã được mở lại để kiểm CRC, zlib stream, dimensions và scanline.

Phân bố module tag là `0`: 4.372, `253`: 2, `254`: 2. Năm module không có PNG
được giải thích hết: bốn tag `253/254` là branch runtime không phải pixel; một
module ở pack `3` entry `6` không có optional palette/payload tail vì file kết
thúc tại guard của loader.

Hai payload `0x27f1` ở pack `3` entry `58` là recovery dữ liệu mức tin cậy cao:
2,739 và 1,909 pixel, tổng 4 PNG. Recovered bytecode vẫn không có runtime fill
branch; đây là static recovery theo corpus, không phải runtime evidence.

Các pixel code đã dịch từ bytecode là `0x64f0`, `0x56f2`, `0x1600`, `0x0400`,
`0x0200`, `0x5602` và `0xa640`; palette format gặp trong corpus là `0x8888` và
`0x5515` (decoder cũng có branch `0x6505` dù corpus không dùng). Metadata giữ
byte range của module/frame/animation/palette/pixel payload, raw/runtime-signed
value và hash PNG. Output tại
[`sprites-decoded/summary.json`](../reconstructed-project/resources/sprites-decoded/summary.json).

Đây là module/palette recovery, chưa tự suy diễn thành assembled frame sheet khi
semantics của một số reference sentinel chưa được chứng minh. Tên codec thương
mại ban đầu không tồn tại trong bytecode; nhãn mô tả hành vi, không giả là tên
nội bộ của Gameloft.

### 4.1. Font Unicode → glyph, pack `1`, entry `2`

Payload là typed-object `short[609]`:

```text
u8    header = 0x19
u16LE valueCount = 609
u16LE values[609]
```

Trong `values`, phần tử đầu là `bucketCount=228`; tiếp theo là 228 cặp
`(codepoint, glyphId)` của bucket cơ sở. Phần đuôi gồm các group
`(bucketIndex, overflowCount, overflowPairs...)`. `b.a(short[])` dựng bảng và
`b.s(int)` tra theo `codepoint % 228`, theo thứ tự, first-match thắng.

Corpus có 206 bucket cơ sở có dữ liệu, 36 overflow group chứa 40 cặp; tổng 246
mapping, 241 codepoint duy nhất, glyph ID phủ `1..243`. Ba key lặp là `32`,
`186`, `1059`; converter không được tự canonicalize chúng. Bản decode đầy đủ:
[`entry-002-font-glyph-map.json`](../reconstructed-project/resources/decoded/pack-1/entry-002-font-glyph-map.json).

### 4.2. Sprite-module substitution, pack `4`–`5`

Các payload là dãy record không header:

```text
repeat until EOF:
    u16LE sourceModule
    u16LE replacementModule
```

`b.a(int, byte[])` khởi tạo identity map rồi áp các cặp thay thế. Bốn bảng của
pack `4` có lần lượt 83, 92, 33 và 55 record, áp vào bốn variant của sprite bank
`z[0]`; toàn bộ 263 record trỏ tới module `253`. Pack `5` có 44 record cho
variant 0 của `z[52]`: `0..27 → 178`, `34..37 → 38..41`, `42..49 → 54..61`,
và `50..53 → 65,62,63,64`. Mọi source/target đều nằm trong module count của
bank tương ứng. Ý nghĩa hình ảnh/tên gameplay của từng variant chưa được chứng
minh chỉ từ mapping.

### 4.3. Bảng toán fixed-point, pack `16`

- Entry `0`: typed header `0x12`, 65 signed `i16 LE`; đúng công thức
  `floor(256*cos(pi*i/128))` cho `i=0..64` (quarter-wave cosine Q8).
- Entry `1`: typed header `0x1a`, `u16LE length=256`, 256 signed `i16 LE`; đúng
  `floor(16*sqrt(i))` cho `i=0..255` (square-root Q4).

Extractor kiểm chứng lại công thức và xuất cả giá trị tại
[`pack-16`](../reconstructed-project/resources/decoded/pack-16/metadata.json).

## 5. Schema 14 slot của level pack `6`–`13`

`k.ec` và `k.ed` cùng liệt kê `/6`..`/13`. `k.I(int)` mở pack hai lần để lấy các
nhóm layer tùy theo loại level `bh[level]`; `k.G(8)` đọc slot `0` và `7` cho
object/script. Mỗi pack có đúng 14 slot.

Grammar field-level, discriminator, opcode width và corpus validation đầy đủ của
hai stream này nằm tại [`level-record-formats.md`](./level-record-formats.md).

| Slot | Nhãn semantic mạnh nhất | Consumer | Độ tin cậy |
|---:|---|---|---|
| `0` | Object/entity descriptor stream (`ek`) | `k.G(8)`, rồi `k.G(9)`/`k.d(boolean)` phân record và bật sprite/entity cần dùng. | Cao |
| `1` | Collision/logical tile plane (`et`); normal-render absent | `k.H(1)`, `k.g(x,y)` collision queries; chỉ vẽ bởi debug flag `dc`. | Cao |
| `2` | Width/height của slot `1` | `k.H(1)`: hai `u16 LE`, luôn dài 4 byte. | Cao |
| `3` | Packed 2-bit companion plane của collision layer, runtime-unused | Không có `j.e(3)`, field giữ lại hay downstream read; mỗi payload dài chính xác `ceil(slot1.length/4)`. | Không dùng: chứng minh; nghĩa transform: suy luận |
| `4` | Secondary tile layer (`ep`) | `k.H(4)`, renderer layer. | Cao |
| `5` | Width/height của slot `4` | `k.H(4)`, luôn 4 byte. | Cao |
| `6` | Packed 2-bit flags của slot `4` (`eq`) | Truy cập `eq[index >> 2]`. | Cao |
| `7` | Timeline group/lane/event/instruction stream | `k.G(8)` phân thành `by`, `bz`, `eH`; mỗi lane có cursor độc lập. | Cao |
| `8` | Tertiary tile layer (`eu`) | `k.H(8)`, renderer layer. | Cao |
| `9` | Width/height của slot `8` | `k.H(8)`, luôn 4 byte. | Cao |
| `10` | Packed 2-bit flags của slot `8` (`ev`) | Truy cập `ev[index >> 2]`. | Cao |
| `11` | Quaternary/lower tile layer (`er`) | `k.H(11)`, renderer lower strip. | Cao |
| `12` | Width/height của slot `11` | `k.H(11)`, luôn 4 byte. | Cao |
| `13` | Packed 2-bit flags của slot `11` (`es`) | Truy cập `es[index >> 2]`. | Cao |

Pack `7` và `10` để rỗng nhóm `4`–`6`, chứng minh layer phụ là optional. Các cặp
`1/3`, `4/6`, `8/10`, `11/13` có tỷ lệ data/flags ổn định. Mission mapping dưới
đây dựa trên tương quan thứ tự với string table nên chỉ có độ tin cậy trung bình:

#### Slot `3`: plane hợp lệ nhưng runtime bỏ qua

Data-flow toàn bộ 12 class không có call `j.e(3)`. `k.I(int)` chỉ materialize
`1/2`, optional `4/5/6`, `8/9/10` và `11/12/13`; `k.G(8)` chỉ lấy `0` và `7`.
Khi stream seek vượt slot `3`, `j.o(int)` chỉ đọc chunk vào scratch buffer private
`j.am` rồi bỏ để tiến cursor. Scratch buffer không có reader nào khác. Vì vậy
“không được runtime dùng” là kết luận từ bytecode, còn tên transform/orientation
chỉ là suy luận từ ba sibling plane có consumer.

| Pack | Dimensions primary | Slot `1/3` bytes | Giá trị 2-bit sau unpack |
|---:|---:|---:|---|
| `6` | 627×55 | 34.485 / 8.622 | `0: 34.485` |
| `7` | 44×600 | 26.400 / 6.600 | `0: 26.400` |
| `8` | 275×110 | 30.250 / 7.563 | `0: 30.250` |
| `9` | 775×70 | 54.250 / 13.563 | `0: 54.250` |
| `10` | 44×625 | 27.500 / 6.875 | `0: 27.069`, `1: 431` |
| `11` | 786×63 | 49.518 / 12.380 | `0: 49.518` |
| `12` | 550×70 | 38.500 / 9.625 | `0: 38.500` |
| `13` | 100×102 | 10.200 / 2.550 | `0: 10.200` |

Bảy plane toàn zero. 431 cell khác zero của pack `10` nằm trên tile ID `20`
(339 flagged/3.934 unflagged) và `22` (92/1.179), nên không thể suy trực tiếp
từ tile ID và cũng không phải padding căn lề. Provenance lịch sử—legacy feature
hay residue của exporter—không thể xác định từ artifact hiện có.

| Pack | Mission suy ra |
|---:|---|
| `6` | Rome/Colosseum: Wolfmen, Claudio, Romulus. |
| `7` | Rome escape bằng flying machine. |
| `8` | Florence: Lucrezia, Caterina. |
| `9` | Florence: truy đuổi Juan Borgia. |
| `10` | Rome: flying machine phá armada. |
| `11` | Venice: Baron Octavien de Valois. |
| `12` | Rome/Pantheon: Michelotto. |
| `13` | Rome/Colosseum: Cesare Borgia, Apple of Eden. |

## 6. Audio pack `17`

Pack có 34 slot: 13 MIDI, 18 RIFF/WAVE và 3 slot rỗng. Runtime xác định MIME từ
signature `MThd` hoặc `RIFF`, tạo `ByteArrayInputStream`, rồi dùng Java ME
`Manager.createPlayer`. Các WAV được quan sát là PCM mono 8-bit, 8 kHz.

File [`h.java`](../reconstructed-project/src/structured/h.java) chứa bảng thời
lượng 34 slot theo millisecond. Đây là metadata hữu ích khi transcode MIDI/WAV
sang format mobile hiện đại: ID và timing phải được giữ, không chỉ filename.

## 7. File phụ `0`, `999` và `dataIGP`

### File `0`: MIME catalog

```text
u8 count
repeat count times:
    u8 asciiLength
    byte[asciiLength] mime
```

Ba giá trị: `audio/x-wav`, `audio/midi`, `audio/x-amr`.

### File `999`: archive catalog

```text
u32LE count
repeat count times:
    u32LE nameLength
    byte[nameLength] ASCII name
    u32LE declaredSize
```

Có 20 record resource; mọi `declaredSize` khớp file thật trong JAR.

### `dataIGP`

Đây là blob Gameloft In-Game Promotion, không phải một PNG đơn. Phân tích signature
và CRC đã tách 29 PNG hợp lệ cùng 880 printable ASCII run. Offset, kích thước,
dimensions và SHA-256 của từng PNG nằm tại
[`data-igp-images/metadata.json`](../reconstructed-project/resources/decoded/data-igp-images/metadata.json).
IGP là cross-promotion cũ; thiết kế rewrite không nên đưa endpoint hoặc hành vi
`platformRequest()` vào runtime mới.

## 8. Những phần chưa được đặt schema hoàn chỉnh

- Detector theo signature vẫn gọi 114 payload là `game-specific binary`, nhưng
  semantic pass đã phân loại family cho đủ 114/114: 106 có bằng chứng
  trực tiếp/tin cậy cao và tám slot `3` là plane runtime-unused với ý nghĩa
  transform còn suy luận có kiểm soát. Không còn
  payload không rỗng chỉ mang nhãn generic.
- Field-level schema bên trong object/entity stream và block/script stream vẫn
  chưa hoàn chỉnh cho mọi record/subtype; raw bytes và consumer đều đã giữ.
- Level slot `3` được chứng minh không có runtime consumer; ý nghĩa transform và
  nguồn gốc exporter/legacy của plane vẫn chưa thể đặt tên chắc chắn.
- Tên chính thức của các sprite pixel codec không thể phục hồi từ symbol đã bị
  obfuscate.
- Pixel code `0x27f1` không có nhánh fill trong method runtime recovered. Offline
  decoder vẫn dựng được bốn PNG từ hai module bằng data-derived recovery đã pin
  provenance và gắn `runtime_bytecode_branch=false`; không được dùng kết quả đó
  để suy ra một runtime codec branch. Một module khác thiếu optional
  palette/payload tail ngay trong artifact.
- File JAD ngoài JAR có thể từng bổ sung property như `HAS-BLOOD`; artifact được
  giao không chứa JAD nên không thể chứng minh giá trị deployment ban đầu.

Các unknown này không đồng nghĩa mất dữ liệu: toàn bộ payload, offset, marker,
hash, structured/simple/fallback source và bytecode đều có trong
`reconstructed-project/` để tiếp tục phân tích tĩnh.
