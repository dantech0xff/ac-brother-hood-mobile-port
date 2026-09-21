# Atlas level — thực thể và script theo màn

Nguồn: `reconstructed-project/resources/levels-decoded/pack-{6..13}/records.json`
(parser exact-EOF, assertions `passed`). Mapping level↔pack `proven` qua
`k.java:260` (`ec = {"/6"…"/13"}` indexed by `aj`) và string table `1+aj`.

## Bảng tổng

| Lv | Pack | Mission | Records | Extent x | Extent y | Shape | Instr | Groups | Lanes |
|---:|---|---|---:|---|---|---|---:|---:|---:|
| 0 | 6 | Colosseum / Wolfmen | 637 | 28..12728 | 124..1106 | dài ngang (~12.7k px) | 677 | 13 | 57 |
| 1 | 7 | Rome / Escape | 225 | −355..914 | −350..11963 | dọc (~12k px) | 100 | 5 | 13 |
| 2 | 8 | Florence / Lucrezia | 567 | −2..5438 | −21..2059 | ngang | 519 | 23 | 75 |
| 3 | 9 | Florence / Juan Borgia | 691 | 8..13716 | −279..1950 | ngang rất dài (chase) | 1165 | 46 | 194 |
| 4 | 10 | Rome / Escape | 254 | −527..2049 | −1745..12403 | dọc (~14k px) | 56 | 5 | 11 |
| 5 | 11 | Venice / Octavien | 740 | 25..15439 | 18..1416 | ngang dài nhất (chase) | 400 | 22 | 65 |
| 6 | 12 | Pantheon / Michelotto | 849 | 15..10969 | 10..1322 | ngang | 426 | 13 | 47 |
| 7 | 13 | Colosseum / Cesare | 323 | −3..1711 | 238..2032 | gọn (boss arena + preamble) | 362 | 17 | 48 |

Nhận xét shape (`high-confidence`, từ extent + mission text):
- Lv 1 và 4 (`ESCAPE`) là map **dọc** — khớp flying-machine/vertical climb trong
  dialogue (heat draft, flaming pitch). Entity type riêng `54, 55, 56, 65`
  chỉ xuất hiện ở hai pack này → bộ type của flying-machine mode.
- Lv 3 và 5 là chase ngang dài; lv 7 gọn = arena boss Cesare.

## Runtime-type histogram theo pack

(gộp `runtime_type_histogram`; retype `47` chỉ xảy ra ở pack-6, 4 record)

| Type | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | Vai trò hiện có |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| 0 | 1 | – | 1 | 1 | – | 1 | 1 | 1 | Player spawn (`g` ctor) |
| 2 | 8 | 3 | 8 | 8 | 4 | 9 | 7 | 3 | (generic helper/decor) |
| 4 | 21 | – | 26 | 14 | – | 109 | 6 | 4 | Enemy phổ biến ở lv 2,5 |
| 5 | 10 | 3 | 21 | 23 | 1 | 14 | 7 | 16 | |
| 6 | 4 | – | 2 | – | – | – | 1 | – | |
| 7 | 3 | – | 3 | 1 | – | 1 | 1 | 1 | helper effect (aV S=24) |
| 9 | 3 | – | 1 | 3 | – | 2 | 1 | – | |
| 10 | 15 | 8 | 22 | 32 | 16 | 13 | 11 | 7 | TriggerController (`i.aV`, 56 states) |
| 11 | 49 | – | 43 | 30 | – | 30 | 28 | 4 | Linked entity (aV S=23 sync flag) |
| 13 | 4 | – | 6 | – | – | 1 | 1 | 2 | |
| 14 | 51 | 7 | 58 | 71 | 3 | 36 | 91 | 22 | Rất phổ biến — interactable/zone |
| 15 | 1 | – | – | – | – | 30 | 4 | – | |
| 16 | – | – | 1 | – | – | 8 | 2 | – | |
| 17 | – | – | 5 | 5 | – | 6 | – | – | |
| 19 | 2 | 5 | – | 1 | 7 | 6 | – | 1 | |
| 21 | – | – | – | – | 1 | – | – | – | |
| 22 | 10 | – | 7 | 10 | – | 3 | 13 | 5 | |
| 24 | – | 7 | – | – | 4 | 1 | – | – | |
| 25 | – | 1 | – | – | 1 | – | – | – | Player alt (`g.n`, machine/vehicle) |
| 27 | 6 | – | 4 | 9 | – | 8 | 9 | 5 | |
| 29 | – | – | – | – | – | – | – | 2 | chỉ boss level |
| 30 | – | – | – | – | 1 | – | – | – | chỉ pack-10 |
| 32 | – | – | – | – | 5 | – | – | – | chỉ pack-10 |
| 35 | 1 | – | 11 | 13 | – | – | 2 | 1 | |
| 37 | 28 | 1 | 35 | 41 | – | 38 | 39 | 19 | phổ biến — pickup/zone |
| 40 | 6 | – | – | 6 | – | 3 | – | – | |
| 41 | 1 | – | – | – | – | – | – | – | chỉ pack-6 |
| 42 | 1 | – | 1 | 1 | – | 1 | 1 | 1 | |
| 43 | – | – | 2 | 5 | – | – | 1 | – | |
| 44 | 61 | – | 61 | 128 | – | 57 | 81 | 54 | Rất phổ biến — tile/decor/trigger |
| 46 | 2 | – | 9 | 10 | – | 3 | 8 | 2 | |
| 51 | – | – | – | – | – | 15 | – | – | chỉ Venice |
| 54 | – | 23 | – | – | 18 | – | – | – | flying-mode entity |
| 55 | – | 80 | – | – | 104 | – | – | – | flying-mode entity |
| 56 | – | 11 | – | – | 16 | – | – | – | flying-mode entity |
| 58 | – | – | – | 1 | – | 1 | 6 | 2 | |
| 60 | – | – | – | 1 | – | – | – | 6 | lv 3 + boss |
| 61 | – | – | – | – | – | – | – | 2 | chỉ boss level |
| 65 | – | 1 | – | – | 1 | – | – | – | flying-mode |
| 66 | – | – | 13 | 26 | – | 29 | 26 | 20 | |
| 67 | 253 | 28 | 117 | 144 | 25 | 204 | 374 | 141 | type phổ biến nhất |
| 69 | – | – | – | – | – | 2 | 1 | – | interactable (aV S=14 launch) |
| 72 | 2 | – | 3 | 6 | – | 6 | 5 | – | |
| 73 | – | – | 2 | 1 | – | 4 | 3 | – | |
| 74 | 86 | 46 | 104 | 99 | 47 | 99 | 121 | – | waypoint/graph node |
| 78 | 2 | – | – | – | – | – | – | – | chỉ pack-6 |
| 79 | 2 | – | 1 | 1 | – | 1 | 1 | 2 | |
| 80 | – | – | – | – | – | – | 2 | – | chỉ Pantheon |

Ghi chú: mỗi level đều có đúng 1 record type `0` (player spawn). `55`/`74` tỷ
lệ cao ở escape packs → waypoint cho chuyến bay; type `74` = waypoint nodes
khớp class `c` (`raw 55 → c.a(short[])` — retype có thể gộp vào đó).
Sematics per type hoàn thiện ở `entity-type-catalog.md` (Phase 4).

## Scripts

`instruction_count`/`group_count`/`lane_count` lấy từ records.json. Lane =
multi-lane timeline (theo `level-record-formats.md`). Lv 3 có 194 lane và
1165 instruction — scripted chase nặng nhất; lv 1/4 escape tối giản.
