# Game Design Document — Assassin's Creed Brotherhood (Gameloft 2010, J2ME 320×240)

Tổng hợp từ static mining (pack-14 strings, level records pack 6–13,
decompiled `g`/`i`/`k`). Mọi mục có nguồn trong `docs/gameplay-mining/`.
Confidence: `proven` = bytecode/data trực tiếp; `inferred` = suy luận có cấu
trúc; `unknown` = chưa chứng minh.

## 1. Pillars (từ strings + mechanics)

- **Side-scrolling stealth-action**: chạy/nhảy/crouch bằng touch-area hoặc
  virtual pad; combat proximity + block; assassination khi lén.
- **8 mission** theo cốt Brotherhood rút gọn (Rome/Florence/Venice, A.D. 1486,
  Ezio vs Borgia).
- **Ba loại màn**: platforming/combat (`bh=4`), flying-machine escape
  (`bh=3`, level 1 & 4), chase (lv 3 Juan Borgia, lv 5 Octavien),
  boss arena (lv 7 Cesare, entity ax 29/61 chỉ ở đây).
- Meta-progression: memory blocks → mở rộng lifebar; souls → life extension;
  3 achievements; unlockable hero skins (EZIO/EXECUTIONER/DOCTOR/NOBLEMAN);
  unlock HARD mode; difficulty easy/normal/hard; per-level best score.

## 2. Mission list (`proven` — `ec[]` + pack-14)

| # | Pack | Địa điểm | Objective | Hình thái | Đặc thù |
|---:|---|---|---|---|---|
| 0 | 6 | Rome, Colosseum | KILL WOLFMEN | platform | tutorial combat, cứu Claudio, Romulus miniboss |
| 1 | 7 | Rome | ESCAPE | **flying vertical** | burn crates → heat draft; pitch bombs lên tàu |
| 2 | 8 | Florence | KILL LUCREZIA & RESCUE CATERINA | platform | boss Lucrezia, cell key |
| 3 | 9 | Florence | KILL JUAN BORGIA | **chase** | crossbow pickup, horse, bridge, catch-fail |
| 4 | 10 | Rome | ESCAPE | **flying vertical** | phá armada Tiber |
| 5 | 11 | Venice | KILL OCTAVIEN | platform/chase | water gate, alarm escape, pushable crates ×15 |
| 6 | 12 | Pantheon | KILL MICHELOTTO | platform | block-path chase, lower/upper areas |
| 7 | 13 | Colosseum | KILL BORGIA & RECOVER APPLE | **boss arena** | Cesare + ax61 support, ending crawl |

Fail conditions (`proven` strings): target-escape, guards-alarm, escape-timer
(`bA[44]` byte → `dB` limit, default 30 — `k.G(1)`).

## 3. Core systems

### 3.1 Điều khiển (`proven`)
Touch zones: trái/phải assassin = move, trên = jump, dưới = crouch, chạm
assassin = attack/hook, icon = đổi weapon. Virtual pad thay thế toàn bộ.
`pointer*` → landscape transform (`gameX=inputY, gameY=240-inputX`).

### 3.2 Player FSM
`g.e()` ~150 states: locomotion family (0/1/7/11/26/79), crouch 21/22,
dash 25, hurt 50, attack 233 + 295 variant, push/pull crate 235–240,
zipline 309, spring-launch 243, launcher-hold 297, interact-lock
183/184/284/311. Flying mode `g.n()` khi ax==25: lateral ±2048, accel 768,
gauge `aE` countdown.

### 3.3 Combat & damage
- Hit pipeline: type-4 volume → `i.a(4)` op → block check `g.b(S)` →
  `g.aB -= dmg*K/6`; player attack tạo type-4 volume (S29 AoE pattern).
- Enemy HP `aB` vs `bu={300,400,500}` theo difficulty `au`; flee/threshold
  tại `bu/2`.
- `d.b={10,20,20,40,35,70}` damage tiers; `d.a` 10-entry variant table.
- Assassination: interactable type-72 (submodes Z[0]=1/3/4, radius Z[3],
  direction Z[4]); enemy aB<=`bw[au]` gate; `ax==61` special kill op.
- Weapon switch icon + `WEAPON RECHARGED` (charge-attack tutorial m1).

### 3.4 World systems
- **Triggers ax=10** (`aV`, 56 states): flag enable/disable, text zones,
  interact zones (mask 16388), launchers, minigame 4-symbol, wave grid,
  attach zones.
- **Doors ax=44**: timed/proximity/slaved (link ax=58).
- **Props ax=67**: 15 subtypes qua `bk[]` — bounce hazard, collectible
  (memory/soul), zipline anchor.
- **Platforms ax=66**: carry player, timed cycles.
- **Crates ax=51**: push/pull (player S235–240), Venice puzzle.
- **Props ax=41**: knockable physics.
- **Checkpoint ax=2**: overlap → snapshot progress vào `bA` save buffer.
- **NPC/enemy family** ax ∈ {11,17,23,47,50,73} shared ~170-state FSM:
  patrol→alert→attack→flee→death; `dv` count → achievement kill totals.
- **Chase targets**: ax 21 (`bD` 479L), ax 29 boss-tier (`aP` 595L, lv7),
  ax 61 (`aR`, lv7 support/boss-add; `a(4)` assassinate hook).
- **Waypoints**: raw 55 → store `c` (≤400); runtime nodes ax 74; escape
  levels dùng dày (đường bay).

### 3.5 Audio
34 slot pack-17, một Player duy nhất. Music MIDI 0–9 (per-level `ee[]`),
SFX wav 10–33 trừ stinger mid 17/21/28; 3 slot rỗng (22,26,27).

### 3.6 Save/progression
RMS `/ASBR` 512B (`save-format.md`): checkpoint fields từ `aY()` — pos,
facing, weapon `g.J/g.I`, `ap[0]` blocks, `ap[2]` score×16, `ap[4]` souls,
`ap[5]` per-level best, flags. In-memory resume `k.bf` 1000×22B.

## 4. Screens (`proven` từ `j.c` map)

Bootstrap 0 → title 18/1 → menu 2 → options 3 / scores 4 / help 5 / about 6
→ level select 19 (map labels Venice/Florence/Rome/Pantheon) / chapter 30 /
difficulty 29 → staged load 9 → gameplay 8 (+dialogue sub-FSM 21) → pause 14
→ milestone 10 → score 15 → modals 12/13/28 → achievements 22 → ending
crawl 24 → exit 11. IGP promo: 25/27. Cheat seq → l(15)/l(13).

## 5. Content inventory cho rewrite

| Asset | Trạng thái | Nguồn |
|---|---|---|
| 8 mission scripts (text) | decoded, indexed | `string-corpus.md` |
| 4.286 entity records | parsed exact-EOF | `levels-decoded/` |
| 3.705 script instructions | parsed | `level-record-formats.md` |
| 12.102 sprite PNG | decoded | `sprites-decoded/` |
| 13 audio files | decoded | `decoded/pack-17/` |
| Entity dispatch table | mined | `entity-type-catalog.md` |
| Player FSM/constants | mined | `player-mechanics.md` |
| Enemy FSM semantics | partial — ~170 states, cần pass sâu hơn | `entity-type-catalog.md` + `i-av-reconstruction.md` |
| Per-state animation↔sprite map | `unknown` — cần mine `i.i()`/`aa.b(S)` | next phase |

## 6. Rewrite contract implications

- Fixed 62ms tick, 8.8 fixed point, `N` drift-correct — đã khớp spike core.
- Player needs ~150 state FSM → ưu tiên mô hình hóa nhóm state family thay
  vì port nguyên switch; giữ behavior-proven states trước (locomotion,
  attack 233, hurt 50, push 235-240, ride 252/309).
- Entity catalog → port order theo phổ dụng: 67 (props) > 44 (doors) > 4
  (hitboxes) > 10 (triggers) > 2 (checkpoint) > 41/66/51 > NPC FSM family.
- Level 1/4 flying mode là subsystem riêng (ax 25 player + 30/54/55/56/65).
- `unknown` còn lại: per-state sprite/anim map, `au` difficulty→`bu/bw`
  indexing contract chi tiết, chase-target FSM (bD) chi tiết.
