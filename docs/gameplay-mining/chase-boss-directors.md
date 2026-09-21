# Mission director + Boss FSMs — `bD()` (ax21), `aP()` (ax29), `aR()` (ax61)

## ax 21 — `bD()`: mission-phase director (`proven`, `i.java:16790-17210`)

Không phải entity "chase target" đơn giản — là **bộ điều phối màn** chạy
sub-state `aA` 0..8, điều khiển tới 7 entity khác qua UID trong `Z[]`.

### Spawn params `Z[]`

| Slot | Nghĩa |
|---|---|
| `Z[0..4]` | UID của 5 bound entities (targets/escorts); mỗi slot ↔ kill-flag bit |
| `Z[5..11]` | 7 slot: `Z[5+cC]` = UID waypoint-chain start cho phase `cC` |
| `Z[12]` | UID waypoint chain chính (khi `bV==0`) |
| `Z[13]`,`Z[14]` | UID hai "escort/pursuer" entities |
| `Z[15]` | UID entity thứ 6 nhận mirror-state |
| `Z[16..19]` | vị trí spawn pursuer (đọc từ `Z[15]/Z[16]` của escorts) |
| `bV` | mode: 0=single chain, 1=flag-2, 2=dual escort |

### Sub-states `aA`

| aA | Vai trò |
|---:|---|
| 0 | init: clone `P` flags cho 5 bound entity, đặt offset `aq/ar`, `az+1`; load chain `c.a(Z[12])` → aA=8 (hoặc 6 nếu null); `k.ai=true`, `k.A(1)` music, `bT=true` |
| 1 | chờ bound `Z[0]` chết (S16) → `l\|=2` → aA=6; else `bG()` update follower |
| 2 | →6 |
| 3 | **waypoint pursuit**: respawn pursuers `Z[13]/Z[14]` tại `Z[16..19]` khi chúng S10/offscreen; đến waypoint (`d(true)`) → `cB=cB.g` (linked-list `c` node: a=x,b=y,c,e,d,f,g=next); `cB.c==1` → release pursuers → aA=6; mỗi phase advance spawn pickup `aK` |
| 4 | chờ `Z[1]` die (S20)→`l\|=4`; `Z[2]` die (S20)→`l\|=8` |
| 5 | chờ `Z[3]` die (S26)→`l\|=16` |
| 6 | **phase router**: `l&62` bitmask {2,4,8,16,32} → `cC` phase index (2→6, 12→2, 16→3, 14→6, 46→5, 18→6); phase change → `bW`+`bX=cC` HUD/objective event, load chain `Z[5+cC]`; 62=full clear → despawn escorts, `k.e(0,aw)`, aA=7 |
| 7 | **finale**: launch player (`ah=k.Y` hoặc `k.Y-2560`, `t()`); player không land → `k.l(15)` score screen; spawn reward ax24 `aK` trong W |
| 8 | waypoint walk: `d(true)` → next `cB.g`; hết chain → aA=6, snapshot escort positions vào `Z[16..19]` |

### Post-switch kill tracker (`proven`)

Mỗi tick quét `Z[0..4]`; entity `r()` (anim done):
`Z[0]`→S16/`l|=2`; `Z[1],Z[2]`→S20/`l|=4,8`; `Z[3]`→S26/`l|=16` (S22→23 hold);
`Z[4]`→S36/`l|=32` (S37→33, flag 128→37 resume). Death-states dùng cho
"target eliminated" tracking.

`k.aR` cập nhật = HUD chase-progress meter khi `aS.al<260`
(`(bu/20-1) - aS.al/400`).

## ax 29 — `aP()`: boss melee (`proven`, `i.java:8012-8755`)

Romulus miniboss + Cesare finale cùng handler; `by` = phase tier (1,3; 2=inert
return). `k.aU=this` = boss ref toàn cục.

### Đặc tính
- `cn` = **counter-chance ramp**: +100 mỗi stagger (cap 800); stagger hit vào
  guard-window → RNG `j.a(0,1000)<=200+cn` → counter `i(21)` (player `i(8)`).
- `cp` = exhaust timer ≥48 → i(0)/i(16) rage fx `e(5)`.
- `ci[5]` = move counters; `ci[4]` idle timer ≥16 → `aQ()` pick attack
  (by3→`i(41)`).
- `co` = saved state resume sau stagger; `ck` = aura entity; `cl` = ax61 add.
- Arena clamp `W` vs camera `k.R/k.S` (hoặc `ac[0]/ac[2]` trạng thái dash).

### States

| S | Vai trò |
|---:|---|
| 0,36 | idle; spawn `cl` ax61 aura `a(61,71,19,99)`; 16t→`aQ()`/i(41) |
| 2,38 | advance ±5120 | r()→player idle/walk → **i(7) grab** else i(0) |
| 3 | attack windup → i(4) + `e(1)` fx |
| 4 | strike-run ±2560; W∩aS → i(6) hit + `e(2)` |
| 5,40 | advance ±3840 |
| 7 | **grab QTE**: 6 tick lock (`g.r`, player i(1)); press 16388 → `cj` counter; T==7 → `b(4)`+projectile `ad` ag=±38400; cj → player i(10) +±4096 break; T==9 cj&by3→i(3) chain |
| 8,39 | stalk ±1024; dx>60→0 |
| 9,37 | advance ±1536; dx<100→0; r()→aQ() |
| 13 | stagger-walk ±1280 (phase-shift) |
| 14,35 | **projectile barrage**: spawn 1 (by1) hoặc 3 (by3, ±100px) ax61 projectiles tại player + `d(9)` markers; player attack→i(8) punish |
| 15,16 | inert stun |
| 17 | **grab finisher**: T==last-3 snap player → `i(370)` + `d(11)` spawn; r()→P\|=64 hide |
| 20 | stagger ±1024; r()→i(28) (co==28) else i(0); `cn+=100` |
| 21 | counter pose: player attack overlap→player i(8); r()→aQ() |
| 25 | phase-outro: `k.E.P\|=128`, …→i(0) |
| 26,28 | by3 block/guard states; `co==28` resume sau stagger |
| 41 | by3 special attack opener |

`i()` hook: `aU==this && i==0 && by==3` → remap i(0)→i(36).

## ax 61 — `aR()`: boss multi-tool (`proven`, `i.java:8755-8890`)

Một type làm **3 vai**: aura-follower, projectile, grab-QTE overlay.

| S | Vai trò |
|---:|---|
| 0,6,15 | spawn/grow; S15 = damage-trap: `Y∩aS.Y` → player `i(375)` + `g.d(g.u[au])` (`g.u={5,10,15}`) |
| 1 | aura follow `k.aU` pos |
| 4,5 | warning ticks; T==9 → SFX 31 |
| 8 | **projectile flight**: param-lerp `j.a(Z[0..5])` progress `Z[6]/Z[7]` → đích `Z[8,9]` → i(10)+SFX12 |
| 10 | hitbox `X∩aS.W` → op4 damage → despawn |
| 9,11→12 | struggle overlay lifecycle |
| 12 | **grab-QTE UI**: marker c/d trên player; `V()` + `bl+=9` (struggle drain); input `f(4112,8256)` wiggle → escape: player `a(0)`, boss `aU.i(28)` stagger; player S370/371 grabbed → `i(13)`+player `i(374)`+`bJ` cooldown |
| 13 | release anim |

Boss duel loop: `aP` S7 grab ↔ `aR` S12 QTE overlay ↔ player 370/371/374;
projectile S8 bay trên đường cong param (không ballistic).
