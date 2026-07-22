# Định dạng save RMS `/ASBR`

Tài liệu này khôi phục format save từ bytecode/source tĩnh của
`assassins_creed_-_br_320x240_136711.jar`. JAR/MIDlet không được chạy, không có
emulator hay dynamic class loading.

Nguồn chính:

- [`k.java`](../reconstructed-project/src/structured/k.java): boot, progression,
  menu, achievement, RMS codec và restore.
- [`i.java`](../reconstructed-project/src/structured/i.java): hai checkpoint
  writer `i.aY()`/`i.X()` và entity state.
- [`k.javap.txt`](../reconstructed-project/bytecode/k.javap.txt) và
  [`i.javap.txt`](../reconstructed-project/bytecode/i.javap.txt): authority khi
  decompiler làm mất kiểu hoặc control flow.

## 1. Contract vật lý

- Store: `"/ASBR"`.
- Chỉ record ID `1` được dùng.
- Buffer runtime: `k.bA`, đúng 512 byte.
- `k.e(false)`: load record 1 vào `bA`.
- `k.e(true)`: `addRecord` nếu store rỗng, ngược lại
  `setRecord(1, bA, 0, 512)`.
- Boot zero đủ 512 byte trước khi thử load.
- Multi-byte value là little-endian. Helper đọc trả Java signed `short`; helper
  ghi short đặt low byte trước.
- Không có magic, version, checksum, CRC, timestamp, record-length field,
  checkpoint-level ID hay backup record.

## 2. Byte map đầy đủ

Confidence:

- **Cao**: read/write/call-site cùng xác nhận layout và vai trò.
- **Trung bình**: layout chắc, tên domain còn suy ra một phần.
- **Thấp**: chỉ xác định được scalar/flag runtime, chưa đặt được nghĩa gameplay.

| Offset | Rộng/encoding | Category | Ý nghĩa và hành vi | Confidence |
|---|---:|---|---|---|
| `0–7` | 8 raw byte | Reserved | Không static access; buffer mới là zero, byte đã load được round-trip. | Cao |
| `8` | `u8` | Setting | Difficulty: `0=Easy`, `1=Normal`, `2=Hard`; default hiệu lực `1`. Boot áp `%3`; Hard bị từ chối nếu byte `69` bằng zero. | Cao |
| `9` | 1 raw byte | Reserved | Không static access. | Cao |
| `10` | `u8 bool` | UI/IGP | Cờ đã acknowledge nhãn promotion/IGP “NEW” (`eJ`), không phải sound setting. | Cao |
| `11–13` | 3 raw byte | Reserved | Không static access. | Cao |
| `14` | `u8` | Campaign | Level continue/high-water, bình thường `0..7`; tăng khi unlock level sau, reset về `0`. | Cao |
| `15` | `u8 bool` | Campaign | Main campaign completed; set sau level `7`, reset về `0`. | Cao |
| `16–17` | `i16/u16 LE` | Checkpoint gate | Trigger/entity ID `aw`; `0` nghĩa không có checkpoint. `i.aY()`/`i.X()` ghi ID; clear chỉ zero hai byte này. | Cao |
| `18–19` | `i16 LE` | Checkpoint | Respawn X, từ checkpoint entity `ak`, restore vào player `aS.ak`. | Cao |
| `20–21` | `i16 LE` | Checkpoint | Respawn Y, từ `al`, restore vào `aS.al`. | Cao |
| `22–23` | `i16 LE bool` | Checkpoint | Facing/orientation `aS.av`; loader chỉ coi đúng giá trị `1` là true. | Trung bình-cao |
| `24–25` | `i16 LE bitmask` | Checkpoint | `g.J`, bitmask weapon available/owned. | Cao |
| `26–27` | `i16 LE bit` | Checkpoint | `g.I`, weapon đang chọn. | Cao |
| `28–29` | `i16 LE` | Checkpoint | Max-life hiện tại `ax`; restore qua byte cast. Default hiệu lực `30`, tăng theo bước `15`. | Cao |
| `30–31` | `i16 LE` | Checkpoint | Capacity phụ `ay`, default hiệu lực `30`; không thấy producer độc lập, có thể là legacy temperature capacity. | Trung bình-thấp |
| `32–33` | `i16 LE` | Dual | `az`, cumulative souls/memory-block progression; copy sang baseline `dD` khi hoàn tất level. | Cao |
| `34–35` | `i16 LE` | Checkpoint | Scalar opaque `aN`, restore qua byte cast; default `0`. | Thấp |
| `36–37` | `i16 LE` | Checkpoint stat | `ap[0]`, enemies killed từ level/checkpoint; result flow clear về `0`. | Cao |
| `38–39` | `i16 LE` | Checkpoint stat | `ap[3]`, silent kills. | Cao |
| `40–41` | `i16 LE`, đơn vị 16 tick | Checkpoint stat | `ap[2]`, elapsed gameplay ticks; save coarsen `/16`, restore `<<4`. | Trung bình-cao |
| `42–43` | `i16 LE` | Checkpoint stat | `ap[4]`, souls/objective counter cho mode có `bh[level] == 3`. | Trung bình |
| `44–45` | `i16 LE` | Permanent baseline | Baseline max-life `dB/ax` khi start không checkpoint; zero được hiểu là `30`; reset ghi `30`. | Cao |
| `46–47` | `i16 LE` | Permanent baseline | Baseline `dC/ay`, zero được hiểu là `30`. | Trung bình |
| `48–49` | `i16 LE` | Permanent baseline | Baseline opaque `dF/aN`, default `0`. | Thấp |
| `50–51` | `i16 LE` | Checkpoint | Mission countdown `aL` theo giây; `-1` nghĩa inactive, dùng cho “ESCAPE TIME”/“CATCH TIME”. | Cao |
| `52–67` | `8 × i16 LE` | Dual array | `ap[5]` ordinary-souls counter theo level, offset `52 + 2*level`; result giữ max, checkpoint ghi entry hiện tại. | Cao |
| `68` | `u8 bool` | Checkpoint | Flag gameplay opaque `k.aZ`; nonzero restore true. | Thấp |
| `69` | `u8 bool` | Campaign unlock | Hard mode unlocked; set sau màn “YOU UNLOCKED HARD MODE!”, gate difficulty `2`, reset-game clear. | Cao |
| `70–75` | 6 raw byte | Reserved | Không static access. | Cao |
| `76–78` | dự kiến `3 × u8 bool` | Checkpoint | `i.br[index]`; ba phần tử được suy ra mạnh vì byte `79` là scalar riêng. Nghĩa từng flag chưa rõ. | Trung bình |
| `79` | `u8 bool` | Checkpoint | Flag entity/player toàn cục `i.bn`; nonzero restore true, nghĩa gameplay chưa rõ. | Thấp |
| `80` | `u8` | Setting write-only | Control mode: `1=VIRTUAL PAD`, `0=STYLE BOX`. Không có boot/read path restore byte này và đổi setting không lập tức save; vì vậy durable setting bị lỗi trong build này. | Cao |
| `81–96` | `8 × i16 LE` | Record | Easy scores cho level `0..7`. | Cao |
| `97–112` | `8 × i16 LE` | Record | Normal scores cho level `0..7`. | Cao |
| `113–128` | `8 × i16 LE` | Record | Hard scores cho level `0..7`. | Cao |
| `129` | 1 raw byte | Reserved | Không static access. | Cao |
| `130` | `u8 state` | Achievement | “Incredible Assassin”: ít nhất 7 kill trong một level. `0=locked`, `1=new/pending`, `2=acknowledged`. | Cao |
| `131` | `u8 state` | Achievement | “Hardcore”: hoàn tất một level ở Hard; cùng lifecycle `0/1/2`. | Cao |
| `132` | `u8 state` | Achievement | “Blood Killer”: ít nhất 28 kill ở level index `1` trong Hard; cùng lifecycle `0/1/2`. | Cao |
| `133–511` | 379 raw byte | Reserved | Không static access; new buffer là zero, loaded bytes được whole-record save bảo toàn. | Cao |

Các range không được code tham chiếu chính xác là:

```text
0–7, 9, 11–13, 70–75, 129, 133–511
```

Tổng cộng **398/512 byte**. Đây không phải hidden header quan sát được: code
không kiểm hoặc populate chúng.

## 3. Load, checkpoint và reset

### Boot/load

1. Zero `bA[0..511]`.
2. Mở `/ASBR` với `createIfNecessary=true`.
3. Nếu record 1 tồn tại, đọc vào offset `0`; return length của `getRecord` bị bỏ
   qua.
4. Nếu store chưa có record, chỉ đặt in-memory difficulty `bA[8]=1`; record được
   tạo ở lần save sau.

### Checkpoint

`i.aY()` và `i.X()` ghi offset `16–43`, entry hiện tại trong `52–67`, byte `68`
và `76–79`. Hai hàm không tự gọi `k.e(true)`: thay đổi chỉ xuống RMS khi một
menu/progression event sau đó save cả buffer.

Checkpoint invalidation thường chỉ clear `16–17`. Payload còn lại vẫn stale
nhưng bị bỏ qua khi marker bằng zero.

### “Reset game” quan sát được

Reset:

- clear campaign `14/15`, progression `32`, hard unlock `69`;
- clear scores `81–128` và achievements `130–132`;
- ghi `30` cho current/baseline life ở `28` và `44`.

Nó không bulk-zero record và không delete `RecordStore`. Settings/promotion,
reserved bytes, phần lớn checkpoint payload và array `52–67` vì thế còn tồn tại
sau immediate reset write.

## 4. Entity snapshot không nằm trong RMS

`k.bf` là buffer RAM-only `1000 × 22` byte; `k.bg` giữ tombstone/deletion state.
Mỗi entity slot:

| Offset slot | Encoding | Field |
|---:|---|---|
| `0–1` | `i16 LE` | `S`, state/animation |
| `2–3` | `i16 LE` | `T`, substate/frame |
| `4–5` | `i16 LE` | `ak`, X |
| `6–7` | `i16 LE` | `al`, Y |
| `8–11` | `i32 LE` | `aA` |
| `12–15` | `i32 LE` | `P`, entity flags |
| `16` | `u8 bool` | `bz` |
| `17–20` | `i32 LE` | `bs` |
| `21` | `u8 bool` | `av`, facing/orientation |

`i.aY()`/`i.X()` populate snapshot qua `k.a(i,int)`; entity bị loại dùng sentinel
`-99`. `k.d(true)` restore snapshot khi retry checkpoint. Không code nào copy
`bf`/`bg` vào `bA` hoặc RMS. Do đó riêng file `/ASBR` không thể tái tạo exact
world/entity state sau process loss.

## 5. Integrity và corruption behavior

- Không checksum, signature, magic, versioning, backup hay rollback.
- Không range-check level index, coordinate, counter, achievement state hoặc
  checkpoint consistency.
- Difficulty chỉ được normalize hạn chế (`%3` và Hard gate); corrupt byte âm vẫn
  có vấn đề theo Java remainder.
- Score/counter là signed 16-bit; vượt `32767` wrap âm, score display coi giá trị
  không dương là absent.
- Hầu hết bool chấp nhận mọi nonzero; orientation offset `22` chỉ nhận đúng `1`.
- Achievement startup map byte không dương về `0` nhưng giữ positive value bất kỳ.
- Exception bị swallow sau khi dựng một error string không dùng.
- Record ngoài ID `1` bị bỏ qua; short/long record không được validate rõ vì
  return length của `getRecord` bị bỏ.
- Mỗi save ghi đủ 512 byte, nên unknown/reserved byte đã load được round-trip.

## 6. Unknown còn lại

Layout/endianness đã khóa; ý nghĩa domain chính xác còn thấp confidence cho
`aN/dF`, `aZ`, `i.bn` và ba `i.br[]` flag. Độ dài `i.br == 3` là suy luận cấu
trúc mạnh, không phải declaration rõ trong artifact được phép. Những điểm này
phải giữ raw ID/confidence trong legacy importer của bản rewrite, không được tự
đặt tên như fact.
