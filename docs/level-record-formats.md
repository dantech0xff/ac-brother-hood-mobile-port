# Định dạng level record

Tài liệu này mô tả hai stream level pack đã khôi phục tĩnh trong các pack `6`–`13`:
slot `0` là stream mô tả object/entity, slot `7` là stream mô tả block/script.
Toàn bộ kết luận đến từ phân tích tĩnh trên bytecode và decompile; MIDlet / class
không bao giờ được chạy.

Offset trong các bảng bên dưới là offset của payload sau khi đã bỏ marker entry.
Offset trong `metadata.json` là offset của container, tức tính từ byte marker.

## Quy ước tin cậy

| Mức | Ý nghĩa |
|---|---|
| `proven` | Parser trong code và dữ liệu corpus cùng xác nhận. |
| `high` | Call site và hình dạng dữ liệu khớp ổn định, nhưng tên gốc đã mất vì obfuscation. |
| `inferred` | Cấu trúc phù hợp mạnh, nhưng chưa có consumer trực tiếp hoặc còn một lớp suy luận. |
| `unknown` | Byte được bảo toàn, nhưng chưa gán được ngữ nghĩa trường. |

## Slot `0`: object/entity descriptor stream

Nguồn chính:
- [`k.java`](../reconstructed-project/src/simple/k.java#L5941)
- [`k.javap.txt`](../reconstructed-project/bytecode/k.javap.txt#L22641)
- [`i.java`](../reconstructed-project/src/simple/i.java#L2581)
- [`i.javap.txt`](../reconstructed-project/bytecode/i.javap.txt#L10684)

### Scope tĩnh

- Không có MIDlet/class nào được thực thi.
- `k.G(8)` đọc slot `0` bằng `j.e(0)` và đưa payload vào `ek`.
- `k.d(false)` duyệt `ek` cho đến EOF; mọi record đều kết thúc đúng biên dữ liệu.

### Grammar chính xác

```text
slot0_stream ::= record* EOF
record       ::= count8 fields[count8]
count8       ::= signed byte
fields       ::= s16le[count8]
```

Ràng buộc:

- Mỗi record có biên `1 + 2N` byte, với `N = count8`.
- `count8` là byte có dấu theo Java; runtime dùng scratch buffer `short[25]`.
- Mọi trường 16-bit đều little-endian có dấu.
- Không có global count, terminator, hay padding giữa các record.
- Bytecode dispatch có 3 nhánh:
  - raw `0` và `25` -> `g(short[])`
  - raw `55` -> `c.a(short[])`
  - còn lại -> `i(short[])`

### Map trường trong record

| Index | Vai trò tại runtime | Ghi chú |
|---:|---|---|
| `0` | raw discriminator -> `ax` | Có thể bị retype theo subtype. |
| `1` | UID -> `aw` | Giá trị danh định của entity. |
| `2` | x -> `ak` / `N` | `N = ak << 8` trong constructor `i(short[])`. |
| `3` | y -> `al` / `O` | `O = al << 8` trong constructor `i(short[])`. |
| `4` | type-specific | Hình dạng phụ thuộc `ax`. |
| `5` | subtype / variant | Thường được dùng để chọn nhánh khởi tạo `i(field5)`. |
| `6` | flags -> `P` | Bit `0` của `field6` gán `av`. |
| `7+` | type-specific | Chỉ có nghĩa khi từng subtype yêu cầu. |

### Retype đã chứng minh

- raw `11` + subtype `80` hoặc `93` -> `47`; trong corpus chỉ thấy `12` record subtype `93`.
- raw `17` + subtype `120` -> `50`; trong corpus có `8` record.

### Hành vi constructor `i(short[])`

`i(short[])` đọc trực tiếp record đã tách:

- `ax = r8[0]`
- `aw = r8[1]`
- `ak = r8[2]`
- `al = r8[3]`
- `P = r8[6]`
- `av = (r8[6] & 1) != 0`

Sau đó constructor rẽ nhánh theo `ax`. Hai điểm đáng chú ý:

- raw `11` có thể bị đổi thành `47` khi `field5` là `80` hoặc `93`.
- raw `17` có thể bị đổi thành `50` khi `field5` là `120`.

### Bảng tổng hợp slot `0` theo pack

| Pack | Container offset | Bytes | Records | Distinct raw types | Field-count histogram | First rec | Last rec |
|---:|---:|---:|---:|---:|---|---|---|
| `6` | `66` | `15245` | `637` | `27` | `7x3,8x101,9x262,11x12,12x96,13x56,18x11,19x28,20x53,21x15` | `0x0000..0x0013` | `0x3b76..0x3b8d` |
| `7` | `66` | `5279` | `225` | `15` | `7x6,8x56,9x109,13x7,18x3,19x1,20x11,21x32` | `0x0000..0x000f` | `0x1478..0x149f` |
| `8` | `66` | `14635` | `567` | `27` | `7x2,8x114,9x122,11x19,12x100,13x71,15x5,18x32,19x35,20x45,21x22` | `0x0000..0x000f` | `0x391a..0x392b` |
| `9` | `66` | `17685` | `691` | `28` | `7x2,8x108,9x149,10x1,11x46,12x152,13x88,15x5,18x36,19x41,20x31,21x32` | `0x0000..0x002b` | `0x4502..0x4515` |
| `10` | `66` | `5970` | `254` | `16` | `7x8,8x55,9x130,11x5,13x3,18x1,20x16,21x35,25x1` | `0x0000..0x000f` | `0x1741..0x1752` |
| `11` | `66` | `17468` | `740` | `30` | `7x15,8x112,9x252,11x40,12x170,13x46,15x6,18x14,19x38,20x34,21x13` | `0x0000..0x000f` | `0x4429..0x443c` |
| `12` | `66` | `19281` | `849` | `29` | `7x3,8x138,9x381,11x36,12x101,13x100,18x9,19x39,20x31,21x11` | `0x0000..0x0025` | `0x4b38..0x4b51` |
| `13` | `66` | `7773` | `323` | `24` | `7x4,8x5,9x144,10x6,11x27,12x60,13x30,18x17,19x19,20x4,21x7` | `0x0000..0x000f` | `0x1e38..0x1e5d` |

Tổng cộng: `103336` bytes, `4286` records. Histogram gộp:
`{7:43,8:689,9:1549,10:7,11:185,12:679,13:401,15:16,18:123,19:201,20:225,21:167,25:1}`.
Tất cả 8 payload slot `0` parse đúng đến EOF.

### Bảng discriminator slot `0`

| Raw type | Total | Fixed field count | Observed field `5` values |
|---:|---:|---:|---|
| `0` | `6` | `7` | `0` |
| `2` | `50` | `8` | `0` |
| `4` | `180` | `12` | `5,6,7,9,21,30,43,44` |
| `5` | `95` | `18` | `0,3,5,8` |
| `6` | `7` | `8` | `5` |
| `7` | `9` | `9` | `0` |
| `9` | `10` | `9` | `0,6,8,14,34,43` |
| `10` | `124` | `21` | `0,2,3,10,14,16,17,20,24,30,31,33,34,36,43,48,50,51,53,54,55` |
| `11` | `188` | `20` | `0,2,3,5,93,152` |
| `13` | `14` | `12` | `0,1` |
| `14` | `339` | `13` | `0,1,2,6,7,8,10,12,21,27,28,29,32,38,71,73,74,75,78,79,80,93,98,99,105,107` |
| `15` | `35` | `9` | `6,9,10,11,15,25,26` |
| `16` | `11` | `7` | `31,32,38` |
| `17` | `16` | `15` | `57,64,120` |
| `19` | `22` | `7` | `17` |
| `21` | `1` | `25` | `1` |
| `22` | `43` | `12` | `0` |
| `24` | `12` | `8` | `0,19,33` |
| `25` | `2` | `7` | `4` |
| `27` | `41` | `11` | `0,2,4,8,10,11,16,18,23` |
| `29` | `2` | `11` | `0,30` |
| `30` | `2` | `21` | `4` |
| `32` | `5` | `11` | `13,17,21,33` |
| `35` | `28` | `18` | `0,6,9,20,25,29,33` |
| `37` | `201` | `19` | `0` |
| `40` | `15` | `11` | `0,2` |
| `41` | `1` | `8` | `3` |
| `42` | `6` | `13` | `0` |
| `43` | `8` | `11` | `1,2,7` |
| `44` | `442` | `12` | `0,8` |
| `46` | `34` | `13` | `0,1,4,11,12,327` |
| `51` | `15` | `9` | `1,4,8` |
| `54` | `41` | `21` | `4` |
| `55` | `184` | `9` | `0,1,2,5,10,15,30,150,180,260` |
| `56` | `27` | `20` | `0,2,4` |
| `58` | `10` | `8` | `0,2,4,13` |
| `60` | `7` | `10` | `9,11,13,15` |
| `61` | `2` | `7` | `0,16` |
| `65` | `2` | `9` | `0` |
| `66` | `114` | `11` | `6,12,15,22,23` |
| `67` | `1286` | `9` | `0-37,40-45` |
| `69` | `3` | `8` | `0` |
| `72` | `22` | `13` | `0` |
| `73` | `10` | `20` | `152,190` |
| `74` | `602` | `8` | `0` |
| `78` | `2` | `8` | `0` |
| `79` | `8` | `9` | `0,79` |
| `80` | `2` | `8` | `4` |

### Luồng tiêu thụ của slot `0`

- `k.G(8)` load `ek` rồi gọi nhánh phân tích entity/script.
- Trong `k.d(boolean)`, raw `67` tìm `bk[field7]` để gán `aa`.
- Các nhánh sprite phụ dùng mapping đặc biệt:
  - `67 -> bk[field7]`
  - `46 -> bl[field10]`
  - `7 -> bm[field8]`
  - `56 -> bj[field7]`
  - `9 -> bn[field8]`
  - còn lại -> `bi[type]`
- `G(10)..G(84)` load `b` qua `J(int)`, rồi `J(int)` gọi `b.a(byte[], int)`.
- `G(164)` đi vào `d(false)`.
- `b.java` không tham chiếu trực tiếp `ek`, `by`, `bz`, hay `eH`; nó chỉ là sprite parser downstream.

## Slot `7`: level block / script descriptor stream

Nguồn chính:
- [`k.java`](../reconstructed-project/src/simple/k.java#L6191)
- [`k.javap.txt`](../reconstructed-project/bytecode/k.javap.txt#L23100)
- [`i.java`](../reconstructed-project/src/simple/i.java#L19429)
- [`i.javap.txt`](../reconstructed-project/bytecode/i.javap.txt#L64769)

### Grammar chính xác

```text
slot7_stream ::= group_count:count8 group[group_count] EOF
group        ::= script_id s16le
                 lane_count count8
                 group_meta opaque[2]
                 lane[lane_count]

lane         ::= mode u8
                 lane_meta u8
                 mode_extra opaque[2]      ; chỉ có ở mode 2/3
                 event_count s16le
                 event[event_count]

event        ::= tick s16le
                 opcode_count count8
                 instruction[opcode_count]

instruction  ::= opcode u8
                 operands[width(opcode)]
```

Ràng buộc:

- `count8` và mọi `s16le` đều đọc theo Java sign semantics.
- `group_meta` luôn bị bỏ qua; high byte quan sát được luôn `0`.
- `lane_meta` phân bố: `0` xuất hiện `88` lần, `1` xuất hiện `421` lần, `2` xuất hiện `1` lần; runtime không dùng trực tiếp.
- `script_id` map qua `k.s(...)` để lấy chỉ số `eH`; `k.s` là first-match scan
  và trả `-1` khi không khớp.
- `tick` là `s16`, so với `cK`; corpus quan sát `0..438`.
- `i.ab()` là guard timeline-active chính xác `ca >= 0 && cd[0] != 1 && cK >= 0`.

### Hiệu chỉnh bytecode cho `bz`

Bytecode tại [`k.javap.txt`](../reconstructed-project/bytecode/k.javap.txt#L23610) PCs `1086..1098` ghi:

- JVM local8-local6 phản ánh `event_start - lane_start`.
- `by[r9][r12] = new byte[r011 + 2]`.
- `bz[r9][r12]` lưu chiều dài thô này.
- `System.arraycopy(r04, r07, by[r9][r12], 0, r011)`.
- hai byte cuối của `by[...]` là giá trị LE của `bz`.

Điểm quan trọng: `bz` là độ dài cursor thô `event_start - lane_start`; initial cursor là `4` cho mode `0/1` và `6` cho mode `2/3`. Blob `by[...]` dài hơn 2 byte vì có trailer synthetic. Ở [`k.java`](../reconstructed-project/src/simple/k.java#L6311), decompiler render `r72 - r07` thành tổng chiều dài đã mã hóa; đây là defect của decompiler chứ không phải bytecode.

### Ví dụ biên pack `6`

- Root tại offset `0x00`: `13`.
- Group đầu tiên tại offset `0x01`: `script_id = 116`, `lane_count = 1`, `group_meta = 01 00`.
- Lane đầu tiên tại offset `0x06`: `mode = 0`, `lane_meta = 1`, `event_count = 1`.
- Event đầu tiên tại offset `0x0a`: `tick = 0`, `opcode_count = 1`, `opcode = 37`, operand `01 00`.
- Raw lane kết thúc tại offset `0x10`.
- Runtime gắn thêm synthetic `04 00` và đặt `bz = 4`.
- Lane mode `2` đầu tiên trong corpus nằm tại offset `0x24`: `mode = 2`, `lane_meta = 1`, `UID = 249`, `event_count = 3`.

### Mô thức mode / meta

| Trường | Quan sát / hành vi |
|---|---|
| `mode 0` | Chỉ clear `k.ab` (`122`). |
| `mode 1` | Toggle `k.Z` dựa trên `k.aa` (`63`). |
| `mode 2` | Có thêm `u16LE` target UID; corpus có `325` lane. |
| `mode 3` | Parser hỗ trợ framing; executor không có case trong bytecode. |
| `lane_meta` | Ignored; phân bố quan sát: `0` xuất hiện `88` lần, `1` xuất hiện `421` lần, `2` xuất hiện `1` lần. |
| `group_meta` | Bị bỏ qua; byte cao luôn `0`, giá trị LE nhìn như `0..208`. |
| `script_id` | Map sang `eH` bằng `k.s`. |
| `tick` | `s16`; các tick thấy trong corpus nằm trong khoảng `0..438`. |

### Phân hoạch opcode thấp

| Nhóm opcode | Width | Diễn giải |
|---|---:|---|
| `11,12,21,25,31,41` | `4` | `2 * s16`; op `11/12/21/25/31` được consumer, `41` không có executor trong corpus. |
| `13` | `5` | `u8` lane + `opaque[4]`. |
| `14-20,26-30,33,40` | `0` | Không có operand. |
| `22,32,42` | `2` | `u16`; consumer quan sát rõ ở `22/32`. |
| `23,24,43,44` | `4` | `opaque mask`; `23/24` set/clear flags. |
| `34,35,36` | `4/6/8` | `u16` target + `1/2/3` `u16`. |
| `37,38,39` | `2/4/6` | `1/2/3` `u16`. |

Các opcode `41-44` được partition trong parser nhưng không xuất hiện trong executor lẫn corpus.

### Hành vi scheduler của slot `7`

- Executor dispatch mọi current event của từng lane trước khi xét điều kiện due;
  `event.tick <= evaluated_tick` chỉ gate việc advance cursor.
- Per-handler time guards vẫn nằm trong handler opcode, không ở scheduler.
- Byte opcode branch dùng signed `baload`:
  - raw `0..99` và `128..255` đi vào inline path;
  - raw `100..127` đi vào extended path.
- Host contract exposes exact-tick opcodes `108` và `113` qua
  `extended_dispatch_results` tường minh; legacy bytecode có thể trả `-1` từ
  helper. Khi result âm, harness surface `execution_aborted=true` sau phase tick
  nhưng trước khi advance current cursor hoặc ghi opcode/lane tiếp theo.

Observed low-opcode counts:

- `11: 218`
- `12: 32`
- `13: 16`
- `21: 1421`
- `22: 1009`
- `23: 166`
- `24: 106`
- `25: 17`
- `37: 177`
- `38: 63`

### Phân hoạch opcode cao

Tổng instruction của slot `7` trong corpus: `3705`.

| Opcode | Width | Diễn giải operand | Count |
|---:|---:|---|---:|
| `100` | `6` | `3 * s16` (target / action / value) | `204` |
| `101` | `2` | `s16` script id | `1` |
| `102` | `4` | `2 * s16` | `90` |
| `103` | `2` | opaque ignored | `7` |
| `104` | `2` | `s16` | `5` |
| `105` | `4` | `u8 / u16 / u8` | `59` |
| `106` | `9` | `4 * u16 / u8` | `16` |
| `107` | `2` | `u16` input mask | `30` |
| `108` | `4` | `2 * u16` branch IDs | `30` |
| `109` | `8` | `4 * u16` | `0` |
| `110` | `4` | `2 * u16` entity UIDs | `9` |
| `111` | `9` | `3 * u16 / u8 / u16` | `25` |
| `112` | `6` | `3 * u16` | `1` |
| `113` | `4` | `2 * u16` branch IDs | `1` |
| `114` | `4` | `2 * u16` | `2` |

### Bảng tổng hợp slot `7` theo pack

| Pack | Container offset | Bytes | Groups | Lanes | Events | Instructions | Modes `0/1/2` | Max tick | First group | Last group |
|---:|---:|---:|---:|---:|---:|---:|---|---:|---|---|
| `6` | `101540` | `4371` | `13` | `57` | `349` | `677` | `11/7/39` | `279` | `0x0001..0x0010` | `0x10f8..0x1113` |
| `7` | `38353` | `735` | `5` | `13` | `67` | `100` | `5/4/4` | `165` | `0x0001..0x0104` | `0x02ce..0x02df` |
| `8` | `90342` | `3925` | `23` | `75` | `335` | `519` | `23/7/45` | `438` | `0x0001..0x003e` | `0x0f00..0x0f55` |
| `9` | `153392` | `8942` | `46` | `194` | `807` | `1165` | `33/26/135` | `392` | `0x0001..0x001c` | `0x22d3..0x22ee` |
| `10` | `40419` | `413` | `5` | `11` | `35` | `56` | `5/1/5` | `28` | `0x0001..0x0050` | `0x013d..0x019d` |
| `11` | `141345` | `3072` | `22` | `65` | `257` | `400` | `17/7/41` | `149` | `0x0001..0x001c` | `0x0bbc..0x0c00` |
| `12` | `115612` | `2986` | `13` | `47` | `280` | `426` | `13/7/27` | `421` | `0x0001..0x0047` | `0x0b9b..0x0baa` |
| `13` | `33354` | `2770` | `17` | `48` | `236` | `362` | `15/4/29` | `225` | `0x0001..0x028c` | `0x0ab5..0x0ad2` |

Tổng cộng: `27214` bytes, `144` groups, `510` lanes, `2366` events, `3705` instructions.
Mỗi pack đều kết thúc đúng EOF; IDs đều unique trong phạm vi từng pack. Pack `11`
cho thấy 0-lane IDs `89`, `97`, `112`.

### Luồng tiêu thụ của slot `7`

- `k.G(8)` đọc slot `7` rồi phân nó thành `by`, `bz`, `eH`.
- `i.aa()` dùng `cK`, `cL`, `by` và `bz` để step event theo lane.
- `i.a(int, byte[], int, int, int)` là executor opcode của event.
- `k.s(int)` map script id sang chỉ số `eH`.
- `i.h(...)`, `i.bJ()`, `i.k(...)` là các điểm phụ trợ quanh path script/entity.
- `b.java` không có consumer trực tiếp cho `ek`/`by`/`bz`/`eH`; nó chỉ phục vụ parser sprite downstream.

## Residual unknowns

- `group_meta`
- `lane_meta`
- `mode 3`
- nhánh `41-44` ở low opcode
- các tên obfuscated gốc
- nghĩa tổng quát của một số field type-specific ở slot `0`
- chi tiết nội bộ của các helper ngoài phạm vi file được giao
- hành vi opaque của mask `23/24`
