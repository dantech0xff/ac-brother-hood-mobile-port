# Audio và sprite usage

## Audio — pack `/17`, 34 slot (`e.java`, `proven`)

Loader `e.a("/17")` sniff byte đầu: `82` (`R` = RIFF) → `audio/x-wav`, `77`
(`M` = MThd) → `audio/midi`. Một `javax.microedition.media.Player` duy nhất —
một kênh phát, `setLoopCount(1)`, play mới preempt cái cũ. `e.a()` + bảng
duration `h.a[34]` (ms) mô phỏng "đang phát" để gate (Java ME player không là
timeline authority).

### Bản đồ slot → file

| Slot | File | Loại | Duration ms |
|---:|---|---|---:|
| 0–9 | `entry-00X-marker-001.mid` | MIDI music | h.a[0..9] = 38958, 14569, 7449, 16958, 9682, 11837, 6964, 3435, 5340, 9010 |
| 10–16,18–20,23–25,29–33 | `.wav` | SFX | 218–325 ms hầu hết; 25=1862, 30=930 |
| 17,21,28 | `.mid` | stinger ngắn | 2823, 1581, 930 — kích thước nhỏ, jingle |
| 22,26,27 | rỗng | 3 slot rỗng hợp lệ | — |

Gating (`e.a(i)`): `k.bE` (music option) gate slot `0..9`; `k.bF` (SFX) gate
`10..33`. Khi cả hai bật và một slot còn trong duration → skip (ngoại trừ nhánh
music-override). Call site: `k.A(n)→z(n)` bounds-check `<34`.

### Trigger histogram SFX (`k.A(n)`)

`13`×8, `16`×7, `12`×7, `30`×6, `15/24/27`×5, `18`×4, `14/23/29`×3,
`11/19/20/21/31/32`×2, `1/9/10/17/25/28/33`×1 — top hits: 12 (block/sword
clash tại `i.a(4)` op18 thực tế là `A(18)`), 13 (damage hit), 14 (hazard
touch), 16 (pickup?), 24 (death), 30 (dialogue/interact ping), 28 (fly drop).

### Music theo level (`proven`)

`k.ee = {5,2,3,3,2,4,5,1}` — `ee[aj]` play ở `k.java:1622`; `bh[aj]` flag
`{4,3,4,4,3,4,4,4,4}` (`bh==3` = flying levels 1,4 — khớp atlas). Music slots
0,6,7,8,9 không gắn level → title/menu/end (`inferred`).

## Sprite registry (`k.java:266-271`, `proven`)

`k.el[spriteIdx] = 1` đánh dấu sheet cần materialize khi load level. Bảng ánh
xạ entity→sprite:

| Bảng | Áp dụng cho | Giá trị |
|---|---|---|
| `bi[ax]` (77) | mọi entity type | `bi[0]=0` player, `bi[4]=4`, `bi[44]=33`, `bi[51]=28`, `bi[61]=71`, `bi[66]=49`, `bi[67]=57`, `bi[74]=56`, `bi[72]=54`, `bi[55]=19`, `bi[56]=20`… (full array ở source) |
| `bj[2]` | ax 56 | {19,68} |
| `bk[15]` | ax 67 subtype `Z[0]` | {24,27,27,27,34,35,37,41,64,64,65,67,49,69,70} |
| `bl[2]` | ax 46 | {29,0} |
| `bm[2]` | ax 7 | {60,66} |
| `bn[2]` | ax 9 | {47,72} |
| `ej[32]` byte | 4 sprite-set index per level (`ej[aj<<2..+3]`) | {11,10,12,10, 3,3,4,3, 6,5,7,5, 6,5,7,5, 3,3,4,4, 1,0,2,0, 8,10,9,10, 11,10,12,10} |

`el[59]=el[61]=1` luôn bật (sheet UI/base); `el[52]→el[71]` phụ thuộc.

`dv++` đếm entity ax ∈ {11,73,17,23,47,50,54,64,56,30} khi materialize →
tổng killable cho achievement/statistics (`proven`).

## g.bk caveat

`k.bk[Z[0]]==27` gate trong `bB()` (type 67) chỉ áp cho subtype 1,2,3 —
bounce/damage prop family; subtypes khác là collectible/zipline/helper.
