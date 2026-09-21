# Port slice 5 — attack input → sword combo (S67/68/69) + gameplay kill

Bối cảnh: slice 4 đã có damage intake `j()` + corpse chain nhưng thiếu đường
vào đòn đánh. Slice này mine + port cánh tay input: tap phím context 65568 →
combo kiếm → giết lính qua input thật (không pin state).

## Mining — proven (simple/g.java)

- `ap()` (~g.java:3815): `v(65568)` khi grounded → `I==1` (kiếm):
  `ag=ah=aj=0; i(S==79 ? 81 : 67)`; `I==2` → `i(286)` + sfx 29; `I==8` →
  `i(303)` (ném dao). `I` = weapon selector, set bởi `h(int)` (i.java).
- Combo tables clinit (g.java:6401): `cj={67,68,69,112, 6,5,100,100,
  65568×4}`, `ck={112,113,114,115, 9,5,5,100, 65568×4}` — 4 hàng ×
  {anim, next-anim(+1), min-frame(+n), key(+2n)}.
- `a(int[],boolean)` matcher: `S==row.anim && v(row.key)` →
  `cl = T>=row.minFrame` (cửa sổ chain); `R==-1 && !cl` → `R=next`.
  Biến thể `r7=true` từ chối hàng cuối.
- Arm L1341 (S67/68/69): `v(65568)` với target yếu (`i.aN` ax11 Z0==2,
  `aB<=bw`) → `R = rand%2 → 183|184` = finisher ám sát (chưa port — cần
  lock-on `i.aN`); đỉnh `T==2` → `k.A(10)` swing sfx; `R!=-1 && (cl||r())`
  → `i(R)`, else `r()` → `l()` settle.
- L182 arm (S286/287): tap toggle 286↔287, end → `i(0)` — chain vũ khí 2.

## Port (rewrite/core)

- `Pad.M_CONTEXT = 65568`; Level0World: tap bottom-third → edge 65568
  (vẫn giữ hold = M_DOWN) — mapping inferred.
- `Entity`: `R=-1`, `cl`, `gI=1` (sword default).
- `PlayerFsm`: grounded tail gọi attack-entry; case `67,68,69,112..115` →
  `comboArm` (matcher `comboMatch` + R-consume + settle `i(0)`).

## Kiểm chứng

- `:core:test` 38 tests xanh. Mới: `bottom-zone tap swings the sword and
  can kill a soldier` — tap vùng dưới → S67 chain → lính `aB→0 → S139`.
- Verifier `ok:true`; unittest 57 xanh; emulator boot + render OK
  (`reports/slice5.png`).

## Gaps / tiếp theo

- Assassination finisher `R=183/184` (cần `i.aN` lock-on mining) + sfx hook.
- Weapon selector `h(int)`/`I==2,8` (heavy 286 chain, knives 303) chưa wire —
  `gI` cố định =1.
- `C()` counter-offer, `g.a()` block/QTE, player HP `g.x[1]` drain chưa port.
- NPCs vẫn render cyan (palette variants chưa mine).
