# Shared NPC/Enemy FSM — `i.I()` family deep-pass

Phạm vi: outer `case 11/17/23/47/50/73` (`i.java:3983-5188`) + dedicated
handlers `aA/aJ/aK/aL` + helper predicates. Mọi S = animation index trong
clip 7 (xem `state-animation-map.md`).

## Kiến trúc dispatch (`proven`)

```
I() → case {11,17,23,47,50,73}:
    shared preamble (death->i(139)/remove, g.h air-assassination target,
                     sound A(24) landing)
    switch (ax):
        11 → inline mega-FSM (~95 state arms, 4005-5172)
        17 → aA()   civilian flee
        47 → aL()   ledge sentinel / pouncer variant
        50 → aK()   pouncer (knockdown từ trên)
        73 → aJ()   heavy guard (grab/struggle QTE)
        23 → (không có case) chỉ preamble + au() = scripted/civilian, no AI
    au()  // corpse drop: entity liên kết Z[21] nhảy ra khi chết
```

## Helper predicates (`proven` trừ khi ghi)

| Hàm | Nghĩa |
|---|---|
| `d()` | awareness tier: 0=unaware, 6=contact (đặt `aS.aA\|=16`), 4=≤55px, 7=close+alerted, 3=≤100, 1=≤180. Gán `j` mỗi tick. |
| `b(iVar)` | engage check → face player, `aA=1`, ax11→S5 / ax73→S154·155; chiếm launcher target `af`(ax69) của player |
| `f()` | player ở trên đầu (S38 hang ≤40y/20x, hoặc S203 + box overlap) → `g.h=this` = drop-assassinate target marker |
| `j()` | player-attack hit: `aB -= J[au]` (`J={100,100,100}` flat); `k.aS.S∈{183,184,216,217}`; ax11 Z[0]==2 có counter lock `aN` |
| `k()` | **stealth kill**: player ≤80x,≤5y (20y nếu player S38 hang), enemy `j==0` unaware, `v(65568)` special press → kill `i6=21` |
| `l()` | player trong sight-rect `Z[9..12]` (aggro box từ spawn) + Bresenham LOS `e()`; loại trừ player state (250/150/284/285…) |
| `aC()` | attack executor: track player/`aq,ar` target; launcher-target `af`ax69 overlap→`i(138)` throw; ax73 Z0==3 chase ceiling-probe→`i(165)` |
| `aD()` | đứng trên crate(51)/prop(43): ride `s.ag`, rơi mép→`i(22)`/`i(23)`, crate S==2→`i(96)` |
| `aE()` | **ceiling ambush** (Z[14]∈{1,6,7}): player dưới trong S∈{24,22,43,150,35,157} → player `i(89)` knockdown, self `i(24)` pin (aC=30); player quá xa (≥20 tile)→`i(20)` |
| `aF()`/`aG()` | edge detection (tile `e()==20/5` = void, crate edge) |
| `aH()` | inert/dead set {0,20,21,106,107,117,139,168,169,176} |
| `aI()` | **stomp-pad**: overlap player→player `i(243)` launch (±3328/−6656), self `i(184)`+`aB=0` — bounce-kill |
| `e(iVar)` | Bresenham LOS tile `e()>=12` chắn tầm nhìn |
| `Q()` | face player (`av = aS.ak<ak`) |
| `S()` | screen shake ×3 |
| `au()` | post-switch: chết + `Z[21]>=0` → teleport linked entity tới xác (drop pickup) |
| `aB()` | melee hitbox→player damage op4/20 qua `X[]` box |

## ax==11 — inline FSM (shared "soldier/wolf" core)

Spawn `Z[]` params: `Z[0]`=archetype (0 normal, 2 counter-killable…,
3 cho ax73 leap), `Z[3]`=home X, `Z[5]/Z[6]`=patrol extents, `Z[7]`=linked
crate uid, `Z[9..12]`=sight rect (điền bởi `e()` từ `Z[15..18]`),
`Z[14]`∈{1,6,7}=ceiling-ambush flag, `Z[21]`=drop-entity uid.

| State | Vai trò | Transitions |
|---:|---|---|
| 0,106,107,135 | idle/walk base (106/107 spawn-variant sinh type-8 effect) | r()→139 die cleanup; aB≤0→i(0) |
| 1 | alert-turn (face, ag=±2048) | r()→25 fall |
| 2,3,92 | patrol (S3 chậm ±512, Z[3,5,6] extents, flip tại mép) | engage `b()`→aA=1 |
| 4,22 | chase (4 nhanh ±2048; 22 ±512) | LOS `a(aS.W,W)`→i(23) attack-init; `aG()`→2/3 |
| 5 | attack commit (`aL=this`) | r()→aq≠0?4 |
| 6 | leap attack (ai=±1280) | r()→23 |
| 11 | patrol-2 | r()→12 |
| 12 | **counter window**: player attacking (S6) + X overlap → i(18), `aN=this`, `g.E=true`; Z[0]==2 hoặc aB≤bu[au]/2 → `b(2)` counter-available | r()→2/23 |
| 16 | →17 (aC=16) | |
| 17 | grab-lock (player X[0]==X[2] check) | player release→i(11)/23; player attack→aS i(8) |
| 18 | counter execute; nếu aN==this && aS.aZ && v(65568) → player i(183/184) rand = **assassination riposte**, `O()` | r()→23 |
| 20 | instant death (door crush `n(44,0)`, script) | r()→139+`k.e(0,aw)` |
| 21 | corpse-under-door settle | r()→corpse flags |
| 23 | stagger retreat (ag=∓512) | aF()→stop |
| 24 | **pin player**: center player vào W, aC countdown → teleport player ra mép có chỗ (aT/aU walls) → i(5) | |
| 25 | falling (aj=1536, ah=5120) | land `e()≥18/2/3`→m(43) lethal / m(40,2) land |
| 27 | stunned | r()→25 |
| 85 | leap-off-ledge | r()→174 (az=aS.az+1 render trên player) |
| 96,97 | despawn-poof | r()→`k.c(this)` remove |
| 99 | scripted idle (no AI) | |
| 117 | scripted death | r()→kill count+`k.o(3)`+remove |
| 133,134,145 | **hostage carry**: mirror player pos khi aS.S==270/271 (Caterina) | else→135 |
| 138 | throw linked `af` (af→i(3)/i(10), SFX 18) | |
| 139 | dying → corpse (P\|=32\|64, bK corpse sprite) | |
| 140 | recover | r()→23 |
| 142,143 | squash death (al+=10, land→aB=0) | |
| 144 | block variant (T==3→c(1)) | r()→23 |
| 168,169 | **air-assassination**: r()→aB=0, kill count, player i(293), self S169 ascend tới `ar=aS.al` | |
| 174 | lunge+mount check | overlap player→175; else→140 |
| 175 | **grab-mount loop** (bl=40 timer): 65568 press→kill i(176)+player i(311); timeout→i(177)+player i(312) throw-off ±1280 | |
| 176 | executed | r()→139+count |
| 177 | dismount | r()→140 |
| 179 | | r()→2 |
| 180-183 | bounce-pad states (`aI()` mỗi tick — player stomp→kill) | r()→S+2 |
| 184 | ledge land +println "check phy right" | |

Intro branch (`ca!=-1`, spawn anim): `d`/`cd[7]`/bI()/aa() scripted intro,
xong→i(2) patrol.

## ax==73 — `aJ()` heavy guard (`proven`)

Death→`i(164)`. S=131 block/counter (aS.S==6→aN=this). S=147 **grab-QTE**:
`V()`+bl+=9; player 65568→kill `i(164)`+player `i(287)`; timeout bl==0→
`i(149)` release. S=149→155 với `aq` landing ±60. S=152 idle-reset (Z[0]=0).
S=153/154 chase (153 nhanh). S=155 attack + Z[0]==3 fast-retreat ±1536;
trong `aC()` ceiling-probe→`i(165)` jump-up-attack. S=156 leap (ai=±1280).
S=157/158 stagger→155. S=164 die.

## ax==17 — `aA()` civilian (`proven`)

aB≤0→`i(69)`. S=57 idle; player đến gần `l()` + SFX16 → chọn flee anim
theo hướng tương đối (nested ternary → 61-67). S=60-68 flee→57. S=69 dying;
S=129 alt-death; S=170 fall (al+=10)→129.

## ax==50 — `aK()` pouncer (`proven`)

S=120 perch: player-passing-below check (cùng pattern `aE` S-list) → player
`i(89)` knockdown + pin + `i(119)` aC=30; `zL`→flee dir states 121-128
(T==3→player hit op4, SFX 16). S=130 despawn.

## ax==47 — `aL()` ledge sentinel (`proven`)

S=0 idle return; 81 timer→82→80; 83 fall M()→84→0; 93 cùng pounce-check→80;
94 P|=512… (còn arms sau 7865 — same drop family).

## ax==23 — scripted NPC (`inferred`)

Không có inner case → chạy preamble + `au()` thôi: vẫn đếm kill (`dv`,
có trong killable list), điều khiển bởi level-script/`d(i)` reset `i(79)`.
Khớp vai trò dân/hostage/prop-người.

## Constants mới xác nhận

- `i.J={100,100,100}` — player→enemy damage theo difficulty (flat 100).
- Stealth kill window: ≤80×5px (20px khi player treo S38), `j==0`, press 65568.
- Ambush trigger states của player: {24,22,43,150,35,157}.
- Player counter/assassinate states tham chiếu từ phía enemy:
  8,89,183,184,216,217,270,271,287,293,297,310,311,312.

## Còn `inferred`/`unknown`

- `V()`, `M()`, `P()`, `t()`, `v()`, `ai()`, `u()`, `O()`, `aa()`, `bI()`,
  `n(44,·)` semantics — đọc signature thôi, chưa trace sâu (P=dead-flag?,
  v=onscreen, t=?, u=?).
- Player-side states 287/293/310-312 chỉ thấy từ phía NPC.
- `aJ()` tail (>7523) và `aL()` tail (>7864) có thêm arms chưa liệt kê.
