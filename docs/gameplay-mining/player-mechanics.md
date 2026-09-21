# Cơ chế người chơi (player mechanics)

Nguồn: `src/structured/g.java` (PlayerActor, 2.116 dòng), `i.java` (physics +
interaction), `k.java` (input/save/audio). Tất cả hằng số giữ nguyên từ
bytecode/decompile; đơn vị fixed-point 8.8 trừ khi ghi khác.

## Input mask (touch)

`k.pointer*` biến đổi portrait→landscape (`gameX=inputY`,
`gameY=240-inputX`); `k.j(x,y)` trả vùng → `E(2 << zone)` set bit. Mask quan
sát được trong FSM (`inferred` labels từ hành vi dùng):

| Mask | Quan sát | Nhãn |
|---:|---|---|
| `4112` | `g.n()`: `ag -= 768` cap −2048; free-fly `ak -= 20` | left (held) |
| `8256` | `ag += 768` cap +2048; free-fly `ak += 20` | right (held) |
| `16388` | free-fly `al -= 20`; trigger action trong `aV` S=5, crate push, attack arm | action/up |
| `33024` | free-fly `al += 20`; crouch/drop edge tests | down |
| `65568` | assassinate/context trigger gần enemy (`e()`: → `c(g)`) | special/context |
| `2` / `8` | tap trái/phải; set `av` facing + nhánh attack | tap L / tap R |
| `16398` | `16388|2|8` composite gate trong case 0 | attack-family mask |

Double-tap cùng hướng (`k.x(8256/4112)`) → state 25 = dash/dodge.

## FSM `g.e()` (type 0)

~150 case arm. `i(n)` = `setState` (gồm state + animation + side-effect).
Nhãn `inferred` trừ khi có chuỗi/consumer trực tiếp:

| S | Hành vi | Evidence |
|---:|---|---|
| 0,1,7,11,26,79 | grounded locomotion family (idle/move/turn/brake) | case 0 khối input chính; `aO<=12` sub-states |
| 9 | grabbed/busy no-control (`ab.S==14` giữ) | case 0 nhánh |
| 21,22 | crouch (edge probe `e(x,y)<=12` → 21; action → 22) | `i(21)/(22)` |
| 25 | dash/dodge (double-tap) | `k.x` branch |
| 43 | interact channel state (excluded `i=true` rule) | `S!=43` guard |
| 50 | hurt/knockdown — reset vel, `e(0)`; hazard overlap ép về đây | `bv`,`aj`,`bb` ép `i(50)`; `g()` helper → `i(50)` |
| 148 | recovery after flag `A` (hit stun exit) | `i(148)` khi `!A` |
| 183,184,311 | exempt khỏi cleanup `k.am` (cutscene/dialogue-locked) | guard ở `e()` head |
| 199,32 | blocked attack windows | `S!=79&&S!=32&&S!=199` check |
| 233 | attack commit (touch-attack / proximity special) | `i(233)` + `i.bq` floor-guard khi ax==51 |
| 235..240 | push/pull crate (với type 51: `g.a==crate`) | `bs()` sets 235/236/237/238/239/240 |
| 243 | spring-launch (aV S=14 launch `±3328,−6656`) | i-av doc |
| 252 | riding platform (bm S=11-13 check) | bm |
| 280 | landing/slope-resolve (`cw && aO==5`, snap `al`) | e() |
| 284,285 | scripted interaction busy (miễn damage) | `a(4)` guard |
| 295 | attack variant kích hoạt hazard case 30 | `aj()` |
| 297 | launcher hold (aV S=17 bắt player) | i-av doc |
| 309 | zipline ride (`bs`/`bB` set `aC=5`, `ag=±2560`) | `bB()` case 28 |

States thương binh trong enemy check `g.b(S)` (`g.java:355`: `S==1||S==4`)
và `g.b(k.aS.S)` tại `i.java:5485,3432` — các state player "đang tấn công/
hungry" được enemy test.

## Physics (8.8 fixed, từ `i.java:3887-3916`)

```text
N += (ak − (N>>8)) << 8   // chống drift khi ak bị code chỉnh trực tiếp
N += ag ; ag += ai ; ai = 0 ; ak = N >> 8
```

Slow-mo mode `aH` (trừ ax==10): chia vận tốc/gia tốc cho `aI` (sub-tick
divisor, level flag `bh[aj]==3` chọn nhánh riêng cho trigger).

Hằng số quan sát (`proven` = literal trong source):

| Giá trị | Chỗ | Ý nghĩa |
|---:|---|---|
| `2560` = 10px/t | run/vel cap (`ah`/`ag` clamps ở aj/bB/bm) | tốc độ chạy chuẩn |
| `5120` = 20px/t | `ah` cap trong `g.e()` + counter `cn` | terminal fall / fall-damage threshold |
| `1536` = 6px/t² | `aj` gravity set ở launcher/push/pull/mount | gravity impulse |
| `768` = 3px/t | bounce impulse `ah = 768 + k.Y` (type 67) | bounce pad |
| `±2048` = 8px/t | fly lateral cap, accel 768/tap | flying mode |
| `±20` px | free-fly move step | debug fly |
| `3328/−6656` | aV S=14 launch vector | spring launch |
| `k.Y` | bonus term vào bounce/fly | độ khó/global lift factor (`inferred`) |

## Sát thương / máu

- `aB` = HP entity; `g.aB` = player HP. `g.aB = 0` → death path.
- `i.bu = {300, 400, 500}` — enemy max HP theo `k.au` (difficulty index:
  easy/normal/hard). `aB <= bu[au]/2` → AI flee/threshold branch.
- `i.bw[k.au]` — bảng song song (threshold khác, `Z[0]==2` assassinate
  gate).
- `d.a = {16,15,7,17,9,8,5,14,10,33}` (10 entries — per-variant table,
  `inferred` animation/damage class), `d.b = {10,20,20,40,35,70}` (6 damage
  tiers).
- Player damage: `g.aB -= (i7 * this.K) / 6` (`i.java:4472/4531`) — `K` là
  hệ số theo attack/difficulty (`inferred`).
- `g.s` = **god mode** flag (cheat 0): `!g.s` guard ở `i.java:3192,3432`.
- `g.a()` = blocking check; `g.b(S)` = player-in-attack-state test.

## Interaction verb `i.a(int op, int a, int b, i src)` (`i.java:3431`)

| op | Hành vi |
|---:|---|
| 4 | melee hit on player: nếu `g.b(S)` (đang block?) → đổi thành 18; `k.A(18)` sfx; `ax==61` target → `c(iVar)` (assassination path); `g.a()` guard |
| 6 | attach/mount tới `i3` x (hoặc `iVar` pos); spawn effect `a(8,59,...)` khi `bK`; `k.o(3)` + `k.A(20)` event/sfx; `ax==47` thêm `k.e(0,aw)` |
| 8,24 | snap về `iVar.W` top-left (mount/ride) |
| 9,25 | snap + launch `aj=1536` + spawn helper `a(43,32)` |

## Flying machine — `g.n()` (player ax==25)

- Heat/energy gauge: `k.aE` giảm 1 mỗi `k.aG`=6 tick; hết (`aE<=0` &&
  `aH<0`) → state 24 forced, cờ `i.bB/bC/bD`, `i.bE=999` — "temperature
  gauge" khớp chuỗi `MAX TEMPERATURE^GAUGE`.
- Lateral: tap ±768, cap ±2048 (8px/t). States 17/18/20/24/30–33 (bank
  left/right variants `bD>=15` chọn 30/31 vs 32/33).
- `k.Q >= 230` altitude ceiling kẹp `ah`; `i.aH` chế độ tăng tốc (`ah=k.Y*aI`
  hoặc `k.Y<<1`).
- `i.be` → win-check (`k.l(12)` — screen 12 = milestone).
- `e(false)` fired khi `k.aI>=10` — periodic drop weapon (flaming pitch).

## Cheat system (`k.java:740-790`)

`eT` sequence table, match index `eS`, log `"Cheat:"+eS`:

| idx | Effect |
|---:|---|
| 0 | `g.s` god mode toggle |
| 1 | `eR` toggle (debug flag) |
| 2 | `dc`, 3 `db`, 4 `eU`, 7 `dt`, 8 `eV` — debug toggles (`inferred`: collision/FPS/level flags) |
| 5 | `l(15)` + `j.g=1` — nhảy screen 15 (score calc / level-complete) |
| 6 | `l(13)` — screen 13 modal |

`g.v` = free-fly (đường riêng ở `e()` head, ±20px/mask).

## Save/progress liên quan player

Checkpoint (type 2, `aY()`) ghi vào `k.bA`: level pos, facing, `g.J`/`g.I`
(weapon slots, `inferred`), `ap[0]` memory blocks, `ap[3]`, `ap[2]/16` score,
`ap[4]` souls, `ap[5]` per-level best (`52+aj*2`), flags `aZ`/`bn`/`br[]`.
Xem `save-format.md` cho byte map.

## Player states nhìn từ phía NPC/boss (`proven` — `g.e()` arms)

| S | Vai trò | Tác nhân gọi |
|---:|---|---|
| 8 | grabbed/staggered; slide ±1280, SFX 11, end→`P\|=64` | ax11 counter (S12/18), boss `aP` counter/grab, ax73 |
| 89 | knockdown — zero vel, `h(1)` hurt | ax50 pounce, `aE()` ambush, ax47 S93 |
| 183,184,205 | **assassination anims** — drive `i.aN` victim: `aN.i(106/107)`, `k.e(0,aw)` kill-count, `aN.S()` shake, snap `aN.al=al`, offset ±30 | ax11 S18 riposte, stealth `k()` |
| 216,217 | heavy lunge attack ±5120 (frames 2-3), wall-check `aT/aU` | NPC `j()` incoming-hit deals `aB-=J[au]` vs these |
| 243 | spring/launcher launch (±3328/−6656) | `aI()` stomp-pad, prop 67/69 |
| 270 | **hostage pickup**: companion `g.i(133)`; r()→271 | ax11 hostage S133/145 |
| 271 | **carrying**: companion `i(134)` mirror; drop-off `ae.S==8` | idem |
| 287 | grabbed-by-guard struggle | ax73 S147 QTE |
| 293 | post air-assassination recover (`au()`) | ax11 S169 |
| 297 | lever/crank hold (và launcher); share arm 326/371 | trigger ax10, NPC S175 `aS.Q==297` check |
| 310,311,312 | mount-QTE dismount/throw-off (311 success, 312 thrown ±1280) | ax11 S175 loop |
| 370,371 | boss-grabbed intro/loop (370 r()→371) | `aP` S17 finisher, `aR` S12 overlay |
| 374 | grab release (r()→376 hoặc `k.l(12)`) | `aR` S12 escape path |
| 375 | trap-caught drag ±1280 →376 | `aR` S15 damage-trap |
| 376 | escape recover | |

Bi-directional refs: enemy `i.aN` = assassination-lock victim; `aN`/`bx`/`at`
là các global lock; `g.E` = counter-window flag (set ở ax11 S12).
