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

Hai overload `g.b` KHÔNG phải một thứ (`proven`, `g.javap.txt`): `g.b(int)` là
bảng tĩnh các state **ở trên không / treo / leo** — `{18,19,20,22,23,24,25,35,36,
43,150,157,165,233,242,243,263..266}` (lookupswitch `b(I)Z`) — còn `g.b()` không
tham số mới là test "đang tấn công" (`{67,68,69,81,112..115,183,184,216,217,286,
287}`). Đầu `i.a(IIILi;)V` (op 4 → 18) dùng `g.b(aS.S)` — bảng trên không —
chứ không dùng test tấn công (slice 410).

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
- `g.a()` / `g.a(i)` = cổng "cú đánh có trúng không" (`proven`, g.java:139): `i.bh != 0`
  (khóa hit toàn cục 8 tick) → false; `h()` (`g.s` hoặc `g.t != 0`) → false; player đã
  chết → true (không trừ); còn sống → `d(…)` trừ máu rồi true. `g.b(S)` = bảng state
  trên không/treo/leo (xem trên), `g.b()` = bảng state tấn công.

## Interaction verb `i.a(int op, int a, int b, i src)` (`i.java:3431`)

| op | Hành vi |
|---:|---|
| 4 | melee hit on player (`proven`, `i.javap.txt:18292` @0-37 + @934-1064): đầu hàm `op==4 && g.b(aS.S) && g.t==0 && !g.s` → đổi thành 18 và `ag=0` — tức player đang **trên không / treo / leo** bị hạ gục; player đứng đất (kể cả giữa combo) đi nhánh op 4: `k.E.P\|=128`, `S∈{284,285,50}` thoát, `ax==61 && g.a(r4)` → `c(r4)`, `S!=9 && g.a() && ax∉{17,50,61}` → `c(r4)` (S9 flinch), `k.A(18)` |
| 18 | `g.a()` (trừ máu + cổng) rồi `i(43)`; 20 / 28: `g.b=null; i(43)` |
| 34 | phản hồi đỡ đòn: vel=0, spark `a(8,5,14,av,ak,midY+30,300)`, `k.A(11)` (slice 410) |
| 38 / 40 | marker-engage: `S==3` thoát → cổng `g.a()` (trừ máu) → (38: `S∈{6,7}` thoát) → `g.b=r4`, `aB=3`, `o()?i(3)`, quay mặt + đẩy ±512 |
| 6 | attach/mount tới `i3` x (hoặc `iVar` pos); spawn effect `a(8,59,...)` khi `bK`; `k.o(3)` + `k.A(20)` event/sfx; `ax==47` thêm `k.e(0,aw)` |
| 8,24 | snap về `iVar.W` top-left (mount/ride) |
| 9,25 | chỉ khi `S==arg`: trượt `ag/ah=Δ<<8`, snap, `aj=1536`, `a(43,32)` |

Mã nguồn gốc chỉ thực sự gửi op `{4, 6, 11, 18, 20, 21, 24, 32, 34, 38, 40}` (liệt kê mọi
`invokevirtual a:(IIILi;)V` trong i/g.javap, slice 410); 8, 9, 19, 25, 28, 29, 30 có arm
nhưng không có caller, 39/41 (cặp marker `k.Y`) chết và chưa port.

**Mount lunge `g.c(i)`** (`g.javap.txt` `c(Li;)V` @138-160, `proven`, slice 411): chọn anim bằng
`invokestatic g.b:(I)Z` — bảng state **trên không / treo / leo** — → S292; S298 giữ anim; còn lại
(đứng đất, kể cả đang giữa combo) chọn cung dốc 272/273/274/275 theo tỷ lệ rise:run `r04/r03`
(≤64 / ≤256 / ≤1024 / hơn; `r03 == 0` → 275, `r04 == 0` → 272). S292 không có rect W/X nên
người chơi **đứng nguyên tại điểm bấm** và bán kính quỹ đạo `cB` là khoảng cách từ điểm bấm tới tâm
bánh/cột; cung dốc nâng tay (X rect ≈ +21,−47) lên trên chân nên bán kính ngắn hơn. Bản port cũ
dùng `g.b()` (bảng tấn công) — cùng kiểu nhầm overload với op 4.

**Ba method tên `b`** (slice 412, `proven` — đếm mọi call site trong javap): `g.b(int)` static =
bảng trên không/treo/leo (17 caller trong `i` + `k.m(int)` + `g.c(i)`), `g.b()` static = bài test
đang vung kiếm/dao (13 caller trong `i` + `g.ap()`), và `i.b()` (private, instance, `i.javap`
`b()Z` @0-196) = **probe góc va chạm**: `t()`, nếu `k.ah` (holder scroll-wall) khác null thì trả
true trừ khi hộp nằm lọt hẳn trong `ah.W`, rồi đọc 4 ô góc W (`aT/aU/aV/aW`), true nếu có ô `>= 12`.
`i.b()` có 2 caller: S85 hit-react (`I()` @3390) và carry của mover ax60 (`c(Z)` @613).

**Method trùng tên giữa `i` và `g extends i`** (slice 413, `proven`): call site trong javap ghi rõ
descriptor (`Method g.h:(I)Z` ≠ `Method h:(I)V`), nên phải đọc từng chỗ. `g.h(I)Z` = *yêu cầu đổi
trang bị* (`I = n`, `k.at = 1`, consume link ax16) — gọi ở ax13 `aW()` @847 (**bắt dây = bỏ vũ khí**),
ax69 `bC()` @1014, ceiling ambush `aE()` @129 và ax73 `aJ()` @2037 (sau `g.g(2)` = `J |= 2; k.q()`);
còn `i.h(I)V` là bind claim-script private. `g.a(int)` luôn chạy `a(43,32)` (căn lại `al` theo tâm hộp
của lần `i.a(Z)` cuối, `u`) rồi `al += 10; ah = r5; aj = 1536` — 22 caller ngoài `g`, nên một cú
đụng trần scroll-holder (`i.f(i)` @354) hay `av()` dịch người chơi lên ≈ 25 px. `g.c(Z)` (bước
leo S37: ô đầu phía trước còn trống) đọc **`k.g` thô** (@40), không qua `i.e()` — nếu qua `e()` thì
trong S37 mọi ô `20` đọc thành `0` và người chơi đi xuyên tường; vì vậy shimmy dưới `'5'` dừng ở mặt
tường và lối đi tiếp là **UP** ở tick S38 (vault `a(54,8)`).

## Camera `k.m(int)` / `k.D()` và đồng hồ collect `k.s()` (slice 414, `proven`)

Mọi phép kẹp camera trong `k.m(int)` là chuỗi **if / else-if** (javap `k.javap.txt` `m(I)V`): holder scroll-wall
`ah.W` (@1954-2084), tường `R/S` và `T/U` (@2120-2235) và hộp tập trung (@1204-1277). Hệ quả: một
bound hẹp hơn khung nhìn 400 × 240 (holder ax37 của level 0 chỉ 350 × 180) giữ **mép trái / mép trên**;
nhánh dưới không bao giờ ghi đè nhánh trên. Nhánh gương của carrier ax43 (`ae.av == false`, @1666-1942):
150 % khi `dx > 200` hoặc `100 < dx <= 200 && ag == z1<<8`, `z1` khi `50 < dx <= 100 && ag == z1<<8`, 50 %
khi `dx <= 50 && ag <= z1<<8`, còn lại giữ tốc độ cũ. `g.c` (khác null) thắng dây `g.a` khi chọn `cA`
(@349); override `ab` / `cd[3]` chạy cả sau nhánh snap (@2428-2499). Camera bay/đuổi `k.D()`: quét hàng
của director dừng ở ô `22` **thứ hai** (@300 `goto 440`) và đặt `k.ae = k.aS` mỗi tick (@93). `k.s()`
(chuỗi thu thập → tier đồng hồ máu `ax = 30 + 15·tier`) thoát ngay khi `ax >= 105` (@8-16).

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
- Recovery (`proven`, `g.javap.txt` `n()` @1022-1048, slice 411): S1/9/12/27 hết anim → (nhả `bB`/
  `az=202`/`aC()`) `i(4)` rồi nhảy thẳng tới khối ma sát @2262 — không `av=0`, không steering;
  S3 hết anim → `i(4)` + @2262, S3 chưa hết → `av=0` + glide tail @1048. `k.B()` (nhạc nền)
  **không** được `n()` gọi (chỉ `k.l(int)`, `k.Q()`, `k.a(boolean)`).

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
| 375 | trap-caught drag →376: `ag = 1280; if (!av) ag = -1280` (`proven`, `g.e()` @2164-2182) — trượt **xa** boss mà người chơi đang quay mặt về | `aR` S15 damage-trap |
| 376 | escape recover | |

Bi-directional refs: enemy `i.aN` = assassination-lock victim; `aN`/`bx`/`at`
là các global lock; `g.E` = counter-window flag (set ở ax11 S12).

## Audit song song `g.e()` / `g.n()` / `D()` (slice 416, `proven` — byte `g/i/k.javap.txt`)

Tám auditor read-only chạy trên một bản chụp đóng băng của port + javap thô; mọi claim được dựng lại từ
bytecode trước khi sửa (jadx/structured đảo điều kiện, làm phẳng else-if, giấu descriptor). Các điểm đã chốt:

| Chỗ | Bytecode | Hành vi gốc |
|---|---|---|
| S184/S205 finisher | `g.e()` @4372-4543 | nạn nhân bị kéo tới `av ? ak - 35 : ak + 35`; kết thúc khi `r() \|\| aN == null` (`k.p(); i.O(); i(0)`) |
| S50/S241 | @4580-4583 | frame 1 gọi `g.d(I)V` **static** (`i.bh = 8` khoá-đánh toàn cục + nhả khi chết) |
| S92/S101 | @6809-6832 | ô đầu đặc (`ah == 0`) → `g.a(0)` (rơi), ngược lại `a(36,36)` |
| S22 | @7365-7393 | nhả chủ ô ẩn nấp `g.e` (`aA &= -9; az = 100`) trước khi vào họ trên không |
| họ trên không | @8068-8296 | kẹp trôi `±512` **không** có điều kiện `ag != 0`; hạ cánh `d(false)` cho mọi S, nhánh S215 là ELSE của phép thử hạ cánh (`g.a(0); av = !av`); `i.f(this)` là bước **cuối** |
| bám tường — 2 chuỗi byte | trên không @7886-7938 `((W0+20)/20)*20+1`; nhánh rơi @8881-8927 `(W0/20)*20+1` | nhánh rơi đặt người chơi gần tường hơn một ô — các cú đá ống khói / khe phụ thuộc vào đây |
| S90 | @9768 | `k.v()` xoá cả sáu từ phím (`eL bC bB eM eK eN`) |
| S38 | @11282-11306 | không bám + ô đầu mở → `al = W[3]; i(43)` và **kết thúc** arm (bộ xử lý DOWN/quay mặt @11383+ chỉ thuộc nhánh bám) |
| cổng `ap()` hậu-tail | @14396-14415 | `z && !i.bn && !g.E` — hai static sống; `i.bn` cũng quyết `g.l()` (`i(bn ? 199 : 32)`) và S79 |
| `ap()` dao | @200-235 | `I == 2 && S != 79` → `ai = ag = 0; i(286); k.A(29)` |
| `g.c(i)` | @5-6 | `i.bq = 0` — mức đỉnh thùng mà `g.m()` đọc |
| điểm gauge | `g.aB()` @117-163; `aq()/ar()` @230-263 | `g.L/g.M` là field **instance** của người chơi (gauge + điểm ném); `i.L/i.M` static là neo chạm (`o(II)V`, `b(II)Z`, `U()`) — hai thứ khác nhau |
| khoá đổi vũ khí `k.at` | `k.c(Z)` @1159-1167 | HUD tail `at == 1 → 0` dưới `g.o()Z` (đứng đất hoặc trên xe) — một field duy nhất |
| `i.J()` (overlay đồng hành ax71) | @60-79 | ẩn khi `r() \|\| (j.c == 21 && k.u != 8)`; `k.u` = loại hộp thoại (`k.b(IIII)Z`, `ag()`), **không** phải từ phím giữ |
| `putfield ac` thô | `g.a(I)V` @31-37, `g.e()` @12688, `g.as()` @248, `g.au()` @1431 | không đụng `P & 256`; chỉ `i.a(Li;)V` (14 call-site trong `i`) mới nhả/đặt cờ |
| `g.n()` (bh3) | `i.I()` @1253-1256 | method riêng: **không** có đầu `e()` (không `a(Z)V`, `an()`, `J()`, `i(50)`, kẹp `ah`), `i.bh--` nằm sau lệnh `return` của dead-drag; level-out về S4 chỉ khi `k.bB == 0 && k.bC == 0` (từ phím) |
| `D()` (mỗi spawn / retry / vào màn) | @0-406 | `k.n(-1)` là overload **rung camera** (`cO = 1`), không phải nhả tường; tái vũ trang `i.br[]` (gợi ý hướng dẫn màn 0); `k.aw = 0` (qua `i.O()` và `k.r()`); `k.p()` nhả khoá nhập; `k.F = null`; `i.at = null` |
| `i.t()` ax21 | @1103-1172 | `W[0] += bY - k.O; W[1] += bZ` — hộp trong không gian camera |
| `i.B()` | `b(III)V` @33-83, `c(III)V` @33-84, @278 | các lần trượt kênh đọc lại `W[0]/W[2]` sau mỗi `t()` (lượt thứ 2+ trượt cả ô 20px); nhánh góc nhúng L36 trả về **không** gọi `t()` cuối |


## `player` tái dùng vs `g` tạo mới mỗi spawn (slice 417, `proven` — byte `g/i/k.javap.txt`, bẫy T16/P1a)

`k.d(Z)V` chạy `i.D()` rồi `new g(r)` (k.javap @614): **bản gốc dựng object người chơi mới tinh mỗi lần spawn**
(respawn record, restore checkpoint, retry màn). Port giữ một `Entity` duy nhất nên `resetPlayerToSpawn` phải
tái vũ trang toàn bộ trạng thái ctor — giờ gói trong `Entity.resetToFreshSpawn()`.

| Pha | Bytecode | Nội dung |
|---|---|---|
| `i.<init>()V` | i.javap @4-198 | `Q=R=S=-1, aq=ar=0, d=false, au=10, i=1, k=false, y=0, bs=0, C=0, bz=false, cr=bM=null, bN=1, bO=bP=0, bR=false, cG=0, cH=null, cI=cJ=false, ca=-1, cM=cN=cP=-1, cR="", cS=cT=cU=null, cV=false, cW=0`; còn lại JVM-zero |
| `i.<init>([S)V` record load | @201-274 | `aw=r[1], ak=r[2], al=r[3], N=ak<<8, O=al<<8, ax=r[0], av=(r[6]&1)!=0, P=r[6]` — **`P` chở cả flag word record** (P&64 giữ anim, P&16 ẩn, P&256 đóng băng) |
| clip bind | @283-468 | `aa = k.r(bi[ax])` — luôn rebind theo record, ax25 → clip 16 |
| đuôi ctor | @7146-7199 | `i(r[5])` rồi `ax==0 → E()` — **E() chạy cả trên đường checkpoint**, ở vị trí record (aR/aZ/b mô tả nền ở spawn, không phải chỗ restore) |
| `g.<init>([S)V` | g.javap @5-121 | `cl=false, cx=(8*j.m)/360, cy..cE=0, cF=5120, **`cG = i.av`** (@66-70 — chốt hướng MẶT TỪ RECORD, đọc trước khi arm restore ghi đè `av`), cH..cM=0, K=0, cN=0, L=0, M=0` |
| `k.a(Z)V` restore arm | k.javap @206-243 | `ak=bA[18], al=bA[20], av=bA[22]==1` — **không ghi N/O**: vị trí logic nhảy tới checkpoint nhưng neo 8.8 giữ ở record spawn (quirk desync — port từng `setPositionPx` đồng bộ cả đôi) |

`i.D()` (i.javap @0-406) xoá static `g.{b,a,h,c,e,g,d,l,m,j,q,r,A,F,k,x,E}` + các static `i`/`k`. **Không** xoá:
`g.{B,C,D,G,H,I,J,ci..cw,f,i,n,o,p,s,t,u,v,w,y,z}` — port từng xoá nhầm `g.t` (iframes), `g.n/g.o` (vùng S36),
`g.B`, `g.f` (FX marker); `g.I/g.J` cũng không xoá nhưng `I(aj)`→`F(aj)` ghi lại `g.I=1`, `g.J|=f0do[aj]` trên
đường loadMission (không phải persist). `g.y` bị `i(r[5])` ghi `= al` ở vị trí record mỗi spawn — không phải
field persist. Hai bản copy của `g.b` (`gb` + `playerLinkB`) đều phải chết theo spawn.

## `i.v()` cull theo SNAPSHOT `k.ac` (slice 418, `proven` — byte `i/k.javap.txt`, P1b)

`k.ac` là mảng `int[4]` thật — **không phải rect suy diễn live**. Chỉ hai method ghi (`iastore`):
`k.m(int)` ở đuôi (@2680-2719, sau latch `ai()`) và `k.D()` ở cả arm snap (`@52-91`: `cA=O; cB=P;
ac={O,P,O+400,P+240}; return`) lẫn đuôi autoscroll (@651-690); `k.<clinit>` alloc `{0,0,400,240}`.
Trong tick `k.I()`, mười lần dispatch `i.I()` + `i.v()` của player (@318) đều chạy **TRƯỚC** camera
phase (@1020 `D()` / @1029 `m(I)`); `i.w()` @1089 chạy sau nên thấy rect MỚI — hỗn hợp cố ý.

Hệ quả: mọi ghi `k.O/k.P` giữa tick — arm lerp camera `r04==1` của group-script (`w.kO += …`,
Entity.kt:2342), shift `kDU` (`camY = camB`, simI), claim-camera `k.Z` — **không** đổi mép cull của
`v()` cho tới phase camera kế tiếp. `i.u()` (chấm khoảng cách `au`) vẫn đọc `k.O/k.P` LIVE (@4/@17)
— chỉ phép overlap `a(k.ac, W/Y)` là stale một tick. Port giờ giữ `ac` thật + `rebuildCamRect()` tại
đúng 3 site trên; test stage `kO/kP` phải gọi rebuild sau staging (= "camera phase đã chạy xong").
