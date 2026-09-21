# State → Animation Map — cơ chế clip-as-FSM

## Cấu trúc cốt lõi (`proven`)

`i.S` không phải enum state trừu tượng — **S là index trực tiếp vào bảng
animation của sprite clip** (`i.i(int)` tại `i.java:240` bounds-check
`i >= aa.a()`). Mỗi entity giữ `aa` = một đối tượng `b` (Gameloft
sprite-clip); các hàm:

| Hàm | Nghĩa |
|---|---|
| `aa.a()` | số animation của clip |
| `aa.b(S)` | số frame của anim S (đọc `au[S]`) |
| `aa.a(S,T)` | duration tick của frame T trong anim S |
| `i.i(n)` | setState: Q=S cũ, S=n, T=U=0 (chốt bounds bởi `aa.a()`) |
| `i.r()` | "anim đã chạy hết" — `T==b(S)-1 && U==a(S,T)-1` |
| `i.q()` | nhảy thẳng tới frame cuối |

Hệ quả cho port: bảng animation **là** bảng state. Port một entity = port
clip sprite của nó + FSM arm theo S.

## Clip space (`proven`)

`k.z = new b[75]` — bảng clip toàn cục. `k.J(i)` load clip từ pack hiện tại
qua `j.e(i)`; `k.r(i)` tra `z[i]` (bounds <75). Pack-3 chứa clip entity
(`/3` mở trước khi fill `z[]`, `k.java:4024`); pack-2 = UI clips (`A[0..5]`);
pack-15 = scenery theo level qua `ej[aj<<2..+3]` → `bY/bZ/ca`;
pack-1 = clip 0 dùng sớm.

### Entity → clip index

| Nguồn | Áp dụng | Giá trị |
|---|---|---|
| `k.bi[ax]` (i.java:1962) | mọi entity mặc định | xem bảng dưới |
| `k.bk[Z[7]]` | ax 67 prop subtype | `{24,27,27,27,34,35,37,41,64,64,65,67,49,69,70}` |
| `k.bj[Z[7]]` | ax 56 | `{19,68}` |
| `k.bl[Z[10]]` | ax 46 | `{29,0}` |
| `k.bm[Z[8]]` | ax 7 | `{60,66}` |
| `k.bn[Z[8]]` | ax 9 | `{47,72}` |

`bi[]` đầy đủ (77 entry, ax→clip, `-1`=không clip mặc định):
`{0,-1,1,2,3,1,4,60,5,47,6,7,8,61,9,25,10,7,-1,11,-1,13,14,7,40,16,15,48,
-1,52,36,44,36,-1,42,62,-1,-1,-1,-1,45,30,-1,31,32,33,29,7,13,-1,7,28,
-1,-1,19,-1,19,-1,20,-1,21,71,-1,-1,22,-1,23,-1,26,38,43,-1,51,7,54,55,
56,-1,63,0,57}`

Điểm mấu chốt: **bi[11]=bi[17]=bi[23]=bi[47]=bi[50]=bi[73]=7** — cả 6 type
chia sẻ clip 7 (generic humanoid), đó là lý do chúng đi chung FSM.

### Clip sizes (đo từ `sprites-decoded/pack-3` metadata `au_h_records`)

| Clip | Entry | Anims | Frames | Ghi chú |
|---:|---|---:|---:|---|
| 0 | pack-3/entry-000 | 393 | 1544 | player Ezio (FSM tới S312+) |
| 7 | pack-3/entry-007 | 201 | 942 | shared NPC/enemy (FSM tới S184) |

Mọi state mà family clip-7 dùng đều có anim thật (0 missing trên 95 state).

## Frame-count table — clip 7, các state được dùng

S→frames (nguồn `au_h_records`):

```
0:12  1:1   2:6   3:8   4:10  5:1   6:4   11:9  12:7  16:1
17:3  18:12 20:7  21:6  22:8  23:8  24:1  25:1  27:1  57:10
60-67:5/5/4/4/4/4/4/4  68:4  69:11 79:1  80:1  81:4  82:7  83:2
84:4  85:9  92:7  93:4  94:8  96:1  97:1  99:13 106:13 107:8
117:5 119:1 120:4 121-128:4 129:1 130:8 131:6 133:6 134:8 135:1
138:8 139:1 140:7 142:1 143:5 144:6 145:1 146:9 147:4 148:1
149:8 151:1 152:11 153:10 154:8 155:8 156:8 157:1 158:1 164:9
165:9 168:4 169:1 170:1 171:2 173:4 174:8 175:2 176:7 177:6
178:10 179:4 180:4 181:4 182:1 183:1 184:4
```

Anim có 1 frame = "pose" state (5,16,24,25,27,135,139,157,158,182,183):
FSM dùng `r()`→chuyển ngay → chúng là transition pose, không phải loop.

## Special hooks trong `i.i()` (`proven`, `i.java:240-280`)

- ax==29 & i==27 → spawn type-15 effect tại vị trí entity (Cesare SFX burst).
- ax==43 & i==11 → zero vận tốc.
- ax==0 (player): i==50 → `g.e(0)` footstep SFX; i∈{0,43,148} → `g.y=al`
  (ground reference cho drop-check); i==43 khi S==61 → al += W[3]-W[1]
  (ledge-correction).
- Đặc biệt: `k.aU==this && i==0 && by==3` → remap i(0)→i(36).

## Port checklist

1. Convert pack-3 entry n → clip asset giữ `au[]` (frame count), `h[]`
   (frame start), frame records (module + duration `a(S,T)`), rects.
2. Entity runtime: `S,T,U,a` counters + `aa` clip ref.
3. `r()`-based transitions đã đủ để chạy mọi state "wait-for-anim-end".
