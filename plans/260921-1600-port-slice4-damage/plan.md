# Port slice 4 — player→NPC damage + soldier death/corpse

Bối cảnh: tiếp nối slice 3 (đã có NPC→player damage qua `i.a(op)` dispatcher).
Slice này đóng vòng combat theo chiều ngược lại: player đánh trúng lính →
lính mất HP → chết → xác (S139) nằm lại.

## Khai thác (mining) — trạng thái nguồn

- `g.b()` no-arg = bảng "player đang tấn công" (**proven**, simple/g.java):
  gate `I==1||I==2` (control mode) rồi `switch(k.aS.S)` L10 =
  `{67,68,69,81,112,113,114,115,183,184,216,217,286,287}`.
  Nhóm `{18..25,35,36,43,150,157,165,233,242,243,263..266}` thuộc hàm
  `b(int)` riêng (airborne/busy set) — không phải tấn công.
  ⇒ S203/204 KHÔNG phải đòn đánh; bộ combo kiếm đứng thật là 67/68/69
  (attackbox X ở frame 1, đo trong clip0).
- `i.P()` = dead check `aB<=0` (proven). `i.C()` = nhánh counter/ escalation
  (ax11 Z0==1 && aB<=bu[au] → `Z[0]=2; i(144)`).
- Bảng clinit (proven, i.java static{}): `bu={300,400,500}` max HP,
  `bw={80,80,80}` normal-hit dmg, `J={100,100,100}` assassin dmg,
  `H={50,50,50}` counter line. `k.au` = difficulty index (mặc định 0).
- `g.x[1]` = player HP meter (op21 trừ theo độ cao rơi; `g.g()` =
  `x[1]>0→false`). Port trừ khi cần — chưa wire.
- `j()` intake (i.java ~5585+): live NPC + `g.b()` + player X ∩ NPC W →
  `Q()` face; S216 → `aB=0` + launch `ag=±5120, ai=∓2560`; {183,184,217} →
  `aB-=J`; còn lại → `aB-=bw`; ax11 `Z0==0` bị heavy-hit → `aB=30` (L83);
  hit-react `c(85,157)` → port đơn giản `i(85)`; `aB<=0` → `i(0)` →
  arm L633 `r()` → `i(139)` corpse + `P&=~16`/`P|=32|64` freeze frame cuối.
  (`aw>=5000 → k.c(this)` despawn: inferred, chưa port.)

## Port (rewrite/core)

- `PlayerFsm.isAttackState` = đúng bảng `g.b()` L10 (proven); thêm arm
  "attack anims play to completion then settle" (inferred — arm thật của
  67/68/69 chưa mine).
- `NpcFsm`: `BU/BW/JD` + `ATTACK_ANIMS` tables; `initSoldier` sửa
  `aB=bu[0]` (max HP theo difficulty, proven — trước đây dùng record[17]);
  khối `j()` subset sau dispatch; arm `S85` (hit-react → quay lại chase/patrol)
  và `S139` corpse (P flags + giữ frame cuối); arm `S0` giờ rẽ đúng:
  `aB<=0 → i(139)` (proven), còn sống → `i(3)` (inferred activation).

## Kiểm chứng

- `:core:test` 37 tests xanh (mới: `player vault attack damages and kills a
  soldier` — S67 combo trừ aB tới 0 → S0 → S139).
- `verify-static-reconstruction` ok:true; `unittest` 57 tests xanh.
- Emulator: boot level 0 render đúng (`reports/slice4.png`).

## Gaps / tiếp theo

- Đòn đánh chưa gắn phím: cần mine arm `e()` vào S67 (tap direction? attack
  key) — sẽ bật kill loop trong gameplay thật.
- `C()` counter-offer (`aB<=H[au] && S!=85` → floatie + `c(85,157,-1,-1)`)
  chưa port; `g.a()` block/QTE chưa port.
- Player HP `g.x[1]` chưa giảm (no death/respawn). Palette NPCs cyan.
- `k.c(this)` despawn + `k.e(0,aw)` drop pickup chưa wire.
