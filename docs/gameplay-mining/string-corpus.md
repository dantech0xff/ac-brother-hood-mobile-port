# Kho văn bản game (string-table corpus)

Nguồn: `reconstructed-project/resources/decoded/pack-14/*.json` — pack `/14`,
9 bảng chuỗi (entry-000..008), 265 chuỗi. Loader `k.java` chứng minh ánh xạ:

- `j.a("/14", 0)` ở stage init → `bU[]` = bảng global UI (`d(0, i)`).
- `j.a("/14", 1 + aj)` ở `G(1)` (level-load stage 1) → bảng nhiệm vụ của level
  `aj` (`d(table!=0, i)` đi qua `j.g(i)`).

Vì vậy **entry-001..008 là script của mission 0..7 theo thứ tự level**
(`proven` bởi `k.java:4747`).

Toàn bộ nội dung tiếng Anh giữ nguyên verbatim (ký tự `\34\0`, `\^`, `^` là
escape/format nội bộ của font renderer — `\0` = đổi font/color lớn, `\1` =
font nhỏ, `^` = ngắt dòng cứng theo corpus).

## Bảng 0 — UI toàn cục (entry-000, 127 chuỗi)

### Menu và điều hướng
`0 MAIN MENU`, `1 NEW GAME`, `2 CONTINUE`, `3 SELECT LEVEL`, `4 OPTIONS`,
`5 HIGH SCORES`, `6 HELP`, `7 ABOUT`, `8 EXIT`, `9 TOUCH THE SCREEN`,
`10 LEVEL`, `11 RESUME`, `12 RESTART`, `16 NEXT`, `17 BACK`, `18 SKIP`,
`24 LOADING`, `67 LOAD MEMORY`, `68 TOUCH THE MAP TO SELECT`, `72 IN-GAME
MENU`, `79 OK`, `117 QUICK PLAY`, `119 ASSASSIN'S CREED BROTHERHOOD`,
`120 NEW`.

### Xác nhận / prompt
`13 ARE YOU SURE YOU WANT TO EXIT?`, `14 YES`, `15 NO`, `19 DO YOU WANT
SOUND?`, `25 DO YOU WANT TO RESTART?`, `69 THE GAME DATA WILL BE PERMANENTLY
DELETED. ARE YOU SURE?`, `73 ARE YOU SURE YOU WANT TO GO TO THE MAIN MENU?`,
`121 THE GAME DATA HAS BEEN DELETED.`, `126 WHICH CONTROL MODE WOULD YOU LIKE
TO USE? ...`.

### Options
`35 EASY`, `36 NORMAL`, `37 HARD`, `70 SOUND SET`, `71/97 DIFFICULTY`,
`83 MUSIC`, `84 SFX`, `85 HINT`, `86 VIBRATION`, `87 RESET GAME`,
`88/100 HIGH`, `89/101 MEDIUM`, `90/102 LOW`, `123 MODE`, `124 VIRTUAL PAD`,
`125 STYLE BOX`, `22 SOUND`.

### Thống kê / kết quả màn
`38 ENEMIES KILLED`, `39 SILENT KILLS`, `40 RETRIES`, `41 SOULS`, `42 TIME`,
`43 SCORE`, `23 TOTAL`, `78 ESCAPE TIME`, `122 CATCH TIME`, `60 MISSION
COMPLETE`, `59 MISSION FAILED`, `91 ASSASSINATION COMPLETE`, `111 CHECKPOINT`,
`61 BOSS`, `92 RUN`, `93 UP`, `94 JUMP`.

### Thất bại (fail conditions — proven strings, consumer theo state)
`56 MISSION FAILED. YOU DID NOT CATCH YOUR TARGET!` (chase fail),
`57 MISSION FAILED. THE GUARDS HAVE SOUNDED THE ALARM!` (stealth/alarm fail),
`58 MISSION FAILED. YOU DIDN'T REACH THE ESCAPE LOCATION IN TIME!`
(timed-escape fail).

### Meta/progression
`26 YOUR MAX TEMPERATURE^GAUGE HAS INCREASED.` (tăng chỉ số — cơ chế
"temperature gauge"), `44 YOU GOT A POTION.`, `45 YOU FOUND A MEMORY BLOCK!
COLLECT \34\0 OF THEM TO EXPAND YOUR LIFEBAR.` (memory block → mở rộng máu),
`46 MEMORY BLOCK COMPLETED^LIFE EXPANDED!`, `98 COLLECT ENOUGH SOULS TO OBTAIN
A LIFE EXTENSION.` (souls → life extension), `99 CONGRATULATIONS! YOU
UNLOCKED HARD MODE!`, `110 CONGRATULATIONS! YOU UNLOCKED A NEW HERO!`,
`64 WEAPON RECHARGED`.

### Hướng dẫn điều khiển (47–50, 95–96)
- `47`: chạm trái/phải assassin = MOVE; phía trên = JUMP; phía dưới = CROUCH;
  chạm assassin = ATTACK/HOOK; icon vũ khí = CHANGE WEAPON.
- `48`: potion hồi máu. `49–50`: virtual pad thay thế hoàn toàn touch-area.
- `95`: game không hỗ trợ landscape handset (build này render world landscape
  400×240 trên canvas portrait).
- `96`: minigame mở khóa — "TOUCH THE TOP AREA TO HIT THE SPRING. TOUCH THE
  LOCK TO MOVE THE LATCH WHEN THE SPRING IS UP."

### Achievements (50, 112–116)
`112 ACHIEVEMENT`, `113 ACHIEVEMENTS`; ba huy hiệu:
- `114/INCREDIBLE ASSASSIN` — kill 7 enemies trong một level.
- `115/HARDCORE` — hoàn thành một level ở HARD.
- `116/BLOOD KILLER` — kill 28 enemies trong level 2 ở HARD.

### Nhân vật mở khóa (104–109)
`104 AC BROTHERHOOD`, `105 PLAYER LIST`, `106 EZIO`, `107 EXECUTIONER`,
`108 DOCTOR`, `109 NOBLEMAN` — danh sách hero/skin; `110` unlock hero.

### Cốt truyện khung
- `27`: intro — Cesare giết Mario, cướp Apple of Eden; Ezio tìm Niccolò
  Machiavelli.
- `55`: ending — Cesare bị đánh bại; Ezio giấu Apple; "spying eyes" theo dõi.
- `28 THE END`. `51–54`: tên địa danh VENICE/FLORENCE/ROME/PANTHEON
  (chapter-select map labels).

### Guard barks
`74 SEIZE HIM!`, `75 STOP HIM!`, `76 BLOCK HIS PATH!`, `118 KILL HIM!`.

### Credits
`77`: credits đầy đủ (Gameloft Chengdu: producers Luo Jun Jie, Ma Lin; game
design Cai Qian, Shi Yao, Pan Xin, Li Yi Nan, Jiang Wei, Wang Ji, Zhang3 Lei;
programmers Wen Yan Bin, Shi Feng, Zhou Chao Feng, Zhao Yu; sound director
Arnaud Galand, sound designer Emanuel Burcea; localization/QA/studio
managers). Placeholder `$VVV` được thay bằng `GloftASBR.b` (MIDlet-Version)
lúc runtime (`k.java:3988-3991`, `proven`).

### IGP/mạng ngoài
`29/80 GET FREE SKIN`, `30 THE CODE IS:`, `31 PLEASE WAIT...`,
`32/33/34 PLAY MORE GAMES!/FREE CHAT!`, `63/66` quảng bá AC trên PS3/PSP +
gameloft.com, `81` Verizon Ezio skin (AC2 cross-promo), `82` network error,
`103 VIP ZONE`.

## Bảng mission (entry-001..008) — script nhiệm vụ

| Level | Pack | Entry | Briefing (string 0) |
|---:|---|---|---|
| 0 | /6 | 001 | `LOCATION: ROME, COLOSSEUM / DATE: A.D. 1486 / OBJECTIVE: KILL WOLFMEN` |
| 1 | /7 | 002 | `LOCATION: ROME / OBJECTIVE: ESCAPE` |
| 2 | /8 | 003 | `LOCATION: FLORENCE / OBJECTIVE: KILL LUCREZIA & RESCUE CATERINA` |
| 3 | /9 | 004 | `LOCATION: FLORENCE / OBJECTIVE: KILL JUAN BORGIA` |
| 4 | /10 | 005 | `LOCATION: ROME / OBJECTIVE: ESCAPE` |
| 5 | /11 | 006 | `LOCATION: VENICE / OBJECTIVE: KILL OCTAVIEN` |
| 6 | /12 | 007 | `LOCATION: ROME, PANTHEON / OBJECTIVE: KILL MICHELOTTO` |
| 7 | /13 | 008 | `LOCATION: ROME, COLOSSEUM / OBJECTIVE: KILL BORGIA & BRING BACK THE APPLE` |

### Mission 0 — Rome Colosseum (KILL WOLFMEN)
Dialogue: Machiavelli giao nhiệm vụ cứu assassin Claudio bị Wolfmen (lãnh đạo
Romulus) bắt; đổi lấy lòng tin của assassins Rome. Guard barks `7–10`,
region labels `11 LOWER COLOSSEUM AREA`, `12 UPPER COLOSSEUM AREA`. Confront
Romulus (`16–21`), thoả thuận với assassins (`26–29`). Tutorial combat strings
`30–32`: proximity special attack + block bằng swipe-up.

### Mission 1 — Rome (ESCAPE)
`8`: Machiavelli — Borgia's men hunting, phải thoát. `9–11`: flying-machine
segment — đốt crates để tạo heat draft leo cao, thả flaming pitch lên tàu
Borgia. `12`: charge-attack tutorial (hold assassin/attack icon rồi release
để nổ lớn hơn).

### Mission 2 — Florence (KILL LUCREZIA & RESCUE CATERINA)
`1`: Caterina Sforza bị Lucrezia bắt; kill Lucrezia, lấy key cell.
Confront `4–7`. `3`: có key, thoát nhanh.

### Mission 3 — Florence (KILL JUAN BORGIA)
Chase structure: `6` Juan escapes → phải cản. `8` tutorial chase controls
(jump/speed up/slow down). `9` crossbow pickup. `14` cần ngựa để đuổi.
`13` cây cầu nhiều guard. `15–16` đối thoại giết banker. `21` fail —
`JUAN BORGIA IS ESCAPING! YOU HAVE FAILED...` (catch fail).

### Mission 4 — Rome (ESCAPE)
`4`: đi Venice bằng flying machine, phá armada Borgia trên sông Tiber.

### Mission 5 — Venice (KILL OCTAVIEN)
Baron Octavien de Valois (French troops). Chase: `4` catch up, `6` alarm
escape, `7` water gate closed → đường khác. `8` guard bark, `9` thanks.

### Mission 6 — Rome Pantheon (KILL MICHELOTTO)
`9 LOWER PANTHEON AREA`, `10 UPPER PANTHEON AREA`. `2` block his way, `3` bị
phát hiện → phải tới chỗ Cesare, `6` guards coming → escape. `14` unlock
final: kill Cesare.

### Mission 7 — Rome Colosseum (KILL BORGIA & RECOVER APPLE)
Final confrontation `3–7`: Cesare ra lệnh giết Ezio; `7 MAY NO ONE REMEMBER
YOUR NAME. REQUIESCAT IN PACE.` — kill line kết thúc.

## Confidence

- Bảng↔level mapping, split global/mission: `proven` (call sites trên).
- Phân vai dialogue (ai nói câu nào): `inferred` từ ngữ cảnh; consumer chính
  xác từng index cần lần call site `d(table,idx)`/opcode script.
- Escape/format codes: `inferred` từ font-wrap code `k.a(b,String,int)`.
