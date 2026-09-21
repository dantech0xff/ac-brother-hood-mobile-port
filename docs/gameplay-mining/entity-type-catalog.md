# Catalog entity type (`ax`)

Bảng dispatch từ `i.I()` (`i.java:3920-5292`, `proven` ở mức wiring). Vai trò
được gán từ handler body; `inferred` khi chỉ có pattern, `proven` khi có side
effect trực tiếp (save write, spawn, sfx, state ép player).

Constructor route (`level-record-formats.md`, `proven`): raw `0`/`25` →
`g(short[])` (player), raw `55` → `c.a(short[])` (waypoint), còn lại →
`i(short[])`. Retype có thể đổi `ax` theo subtype (ví dụ pack-6 `47`←retype).

## Dispatch map

| ax | Handler (i) | Kích thước | Vai trò | Conf. | Dùng ở level |
|---:|---|---:|---|---|---|
| 0 | `k.aS.e()` | g.java | **Player FSM** — xem player-mechanics.md | proven | mọi màn (×1) |
| 2 | `aY()` | 48L | **Checkpoint**: overlap player → `k.y()`, `k.G=Z[0]` snapshot pos/facing/stats vào `k.bA`, tự xóa, và re-materialize respawn flags | proven | 0,2,3,4,5,6,7 |
| 4 | `aj()` | 123L | **Damage volume / attack hitbox**: S5/7 arm → overlap player → `k.A(14)`+hit FX; S6/8 one-shot `m(-1)` loop damage `k.o(5)`; S29 AoE (damage enemies `aB-=50`/`bu[au]<<1`, player `a(4)`); S30 armed-waiting; S33 spawns `aK` projectile | proven | 0,2,3,5,6,7 |
| 5 | `aq()` | 149L/21 st | enemy/NPC family FSM | inferred | 0,1,2,3,4,5,6,7 |
| 6 | `an()` | 24L/4 st | small helper | inferred | 0,2,6 |
| 7 | inline I() | S 0/1 | helper effect spawn từ trigger (aV S=24 giữ type 7) | high-confidence | 0,2,3,5,6,7 |
| 8 | `ar()` | 30L | helper | inferred | — |
| 9 | `bM()` | 92L/22 st | animated prop family | inferred | 0,2,3,5,6 |
| 10 | `aV()` | 7880B | **TriggerController** — 56-state FSM (zones, flag set/clear, launchers, minigame, wave grid, text). Xem `i-av-reconstruction.md` | proven | mọi màn |
| 11,17,23,47,50,73 | shared FSM trong `I()` | ~1200L/~170 st | **NPC/enemy FSM**: patrol/alert/attack/death; `aB` HP vs `bu[k.au]`; flee khi `aB<=bu/2`; `S==11` gọi `k.m()`+event | proven (family) | 0,2,3,5,6,7 |
| 13 | `aW()` | 140L | zone theo player pos (24 `k.aS` refs) | inferred | 0,2,5,6,7 |
| 14 | `aX()` | 55L | despawn/cleanup helper (3 remove calls) | high-confidence | 0,1,2,3,4,5,6,7 |
| 15 | `bu()` | 160L | entity phản ứng hit (`i(7)` + `Z[3]=1` khi S6), Venice ×30 | inferred | 0,5,6 |
| 16 | `bb()` | 227L | NPC-ish (5 msg, 47 player refs) | inferred | 2,5,6 |
| 19 | `aO()` | 27L/4 st | pickup/zone nhỏ | inferred | 0,1,3,4,5,7 |
| 21 | `bD()` | 479L/20 st | chase/escort target AI (msg:4) — pack-10 ×1 | inferred | 4 |
| 22 | `aN()` | 50L | zone/interact (23 player refs) | inferred | 0,2,3,5,6,7 |
| 24 | `ba()` | 295L/40 st | scripted NPC/controller (msg:3) | inferred | 1,4,5 |
| 25 | `k.aS.n()` | g.java | **Player flying-machine mode** — xem player-mechanics | proven | 1,4 |
| 27 | `bL()` | 193L/30 st | NPC/timed entity (str:1) | inferred | 0,2,3,5,6,7 |
| 29 | `aP()` | 595L/50 st | **boss-tier FSM** — chỉ pack-13 | high-confidence | 7 |
| 30,54 | `ax()` | 169L/11 st | flying-mode entity | inferred | 4 / 1,4 |
| 34 | `ak()` | 42L | player-proximity effect (42 refs) | inferred | — |
| 35 | `bQ()` | 415L/32 st | NPC/enemy variant (msg:8) | inferred | 0,2,3,6,7 |
| 37 | `al()` | 93L/6 st | **pickup** (msg:5 — potion/soul/memory) | inferred | 0,1,2,3,5,6,7 |
| 40 | `bx()` | 127L | player-proximity interactable | inferred | 0,3,5 |
| 41 | `n()` | 75L | **knockable prop**: gravity `aj=1536`, cap 2560, đẩy entity khác, `k.ae=aS` | proven | 0,2,3,5,6,7 |
| 42 | `bz()` | 76L | helper (1 msg) | inferred | 0,2,3,5,6,7 |
| 43 | `bw()` | 76L/3 st | interactable | inferred | 2,3,6 |
| 44 | `bv()` | 96L/17 st | **door/gate**: timed cycle (Z[3] countdown), slaved (type 58 link), proximity-open; player trong vùng → `i(50)` (đè/crush) | proven | 0,2,3,5,6,7 |
| 46 | `aZ()` | 129L/15 st | zone | inferred | 0,2,3,5,6,7 |
| 47 | shared | — | NPC + `k.e(0,aw)` khi mount op 90 | proven | 0 (retype) |
| 50 | shared | — | NPC family | proven | — |
| 51 | `bs()` | 118L | **pushable crate**: `g.a` grab ref, player states 235–240 push/pull, carry offset `W[1]+4`, `bp()/bo()/bq()` ground/edge probes | proven | 5 |
| 55 | `c.a()` ctor | — | **waypoint node** (dispatch rule raw 55→c) | proven | 1,4 |
| 56 | `ay()` | 167L/14 st | flying-mode entity | inferred | 1,4 |
| 58 | `bg()` | 65L/13 st | door-slave link target (`bv` resolves Z[5]→ax 58) | inferred | 3,5,6,7 |
| 60 | `bj()` | 160L/11 st | scripted NPC | inferred | 3,7 |
| 61 | `aR()` | 208L | **boss-support entity** (assassination target class — `a(4)` case đặc biệt `ax==61` → `c(iVar)`); chỉ pack-13 | high-confidence | 7 |
| 64 | `bl()` | 418L/44 st | NPC/enemy variant lớn | inferred | — |
| 65 | default arm | — | flying-mode marker | inferred | 1,4 |
| 66 | `bm()` | 249L | **moving platform/ride**: player lên → kế thừa vel (`g.a=this`), chu kỳ Z[0]/Z[1], quay về gốc Z[2]/Z[3] | proven | 2,3,5,6,7 |
| 67 | `bB()` | 101L | **multi-role prop** (`Z[0]`+`k.bk[]` gate): bounce-hazard (`ah=768+k.Y`, player dmg 40), collectible counter (`aA|=8`, `az-1`), **zipline anchor** (player S309 + helper S71) | proven | mọi màn (phổ biến nhất) |
| 69 | `bC()` | 218L/16 st | launcher object (aV S=14 attach/detach) | high-confidence | 5,6 |
| 72 | default arm | — | **interactable/assassinate spot**: `e()` check `at.ax==72` với submodes `Z[0]` 1/3/4 (radius `Z[3]`, directional `Z[4]`) | high-confidence | 0,2,3,5,6 |
| 73 | shared | — | NPC family | proven | 2,3,5,6 |
| 74 | `bN()` | 105L/8 st | waypoint/graph node (msg:3 — patrol routes) | inferred | 0,1,2,3,5,6 |
| 76 | `bO()` | 61L/7 st | helper | inferred | — |
| 78 | `bA()` | 54L/4 st | helper | inferred | 0 |
| 79 | default arm | — | static decor/marker | inferred | 0,2,3,5,6,7 |
| 80 | default arm | — | marker chỉ pack-12 | inferred | 6 |

## Nhận xét hệ thống

- **Enemy AI sống trong shared FSM** cho ax 11/17/23/47/50/73 — một FSM
  ~170 state phục vụ nhiều family phân biệt bằng `Z[0]`/`au` variant +
  `bu`/`bw` HP tables. Đây là trục "guards/civilians/targets" cần port cẩn
  thận nhất.
- **Damage pipeline**: type 4 volumes gọi `entity.a(4, …)`; player nhận qua
  `i.a(4)` case → block-check `g.b(S)`; enemy nhận qua trừ `aB` trực tiếp
  trong aj().
- **Interactive verbs** `i.a(op,…)`: op 4 hit, 6 pull/mount, 8/24 snap-mount,
  9/25 snap+launch — đây là RPC contract giữa entity.
- **Waypoint graph**: raw 55 → `c` store (≤400 node); type 74 = node runtime;
  escape levels chứa ~80–104 node 55 (đường bay) + 46–47 node 74.
