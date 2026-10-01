# Play Store listing — Brotherhood Mobile Port

Tư liệu phát hành cho bản port LibGDX của game AC Brotherhood J2ME.
Assets gốc trong thư mục này được tạo mới (không dùng art Ubisoft).

## Assets

| Asset | File | Kích thước | Trạng thái |
|-------|------|-----------|-----------|
| App icon (hi-res) | `icon-512.png` | 512×512 | sẵn sàng |
| Feature graphic | `feature-graphic-1024x500.png` | 1024×500 | sẵn sàng |
| Screenshots | `../screenshots/` | 7 ảnh, đúng luồng boot→gameplay | sẵn sàng |
| Adaptive icon | `rewrite/android/src/main/res/mipmap-*/` | 5 density + XML | đã wire |

Icon/feature graphic vẽ theo phong cách game (assassin áo trắng-đỏ,
kiến trúc Ý Phục Hưng) — nhân vật generic, KHÔNG phải likeness Ubisoft.

## Tên app (đề xuất — cần quyết trước khi publish)

- Hiện tại trong manifest: `Brotherhood Mobile Port`
- Không dùng "Assassin's Creed"/"AC" trong tên store — trademark Ubisoft.
- Gợi ý thay thế: "Brotherhood: Blade of Roma", "Renaissance Shadow".

## Short description (≤80 chars, EN)

> Leap across Renaissance rooftops — stealth, swordplay and free-running in 8 missions.

## Full description (EN)

A faithful mobile remake of the classic handheld action game — rebuilt
frame-perfect for modern Android.

- **8 missions** across Rome, Florence and Venice — rooftop chases,
  canyon glider flights, and a Colosseum boss arena.
- **Free-running movement**: run, vault, wall-kick, rope-swing and
  ledge-climb through hand-built tile levels.
- **Sword combat**: strike, counter and weaken your foes — then finish
  with an assassination.
- **Touch controls** with an on-screen D-pad, contextual jump button and
  attack button — tuned for landscape play.
- **Checkpoints and saves**: die, retry, keep your progress — plus
  unlockable missions and medals.

Built as a frame-faithful engine recreation: 62 ms tick, fixed-point
physics and the original level data.

## Full description (VI)

Bản remake trung thực của game hành động cầm tay kinh điển — dựng lại
đúng khung hình cho Android hiện đại.

- **8 mission** qua Rome, Florence và Venice — rượt đuổi trên mái nhà,
  bay lượn hẻm núi, đấu boss trong Colosseum.
- **Parkour**: chạy, vault, đá tường, đu dây và leo mép qua các màn
  tile dựng tay.
- **Đấu kiếm**: chém, phản đòn, làm suy yếu đối thủ — rồi kết liễu bằng
  đòn ám sát.
- **Điều khiển cảm ứng**: D-pad trên màn hình, nút nhảy ngữ cảnh và nút
  tấn công — tối ưu chơi ngang.
- **Checkpoint + lưu**: chết, chơi lại, giữ tiến trình — mở khoá mission
  và huy chương.

## Checklist trước khi upload Play Console

- [ ] Quyết tên app cuối (tránh trademark Ubisoft) → sửa `android:label`
      + `applicationId` (hiện `com.acrebuild.spike` — nên đổi trước khi
      publish lần đầu vì không đổi được sau này).
- [x] `versionName` → `1.0.0` (slice 321); `versionCode` vẫn 1.
- [x] Release signing config đã wire (slice 321): tạo keystore local
      (KHÔNG commit) rồi truyền qua env `RELEASE_STORE_FILE` /
      `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` /
      `RELEASE_KEY_PASSWORD` → `assembleRelease` ra APK ký v2.
- [ ] Tạo keystore release THẬT của anh (giữ an toàn — mất key là mất
      quyền update app); Play App Signing khuyến nghị bật.
- [ ] Release build: `:android:assembleRelease` (minify đang tắt — bật
      R8 sau khi test kỹ vì LibGDX cần keep rules).
- [ ] Content rating questionnaire (IARC).
- [ ] Privacy policy URL (nếu collect data — hiện game không collect).
- [ ] Data safety form: "no data collected" nếu đúng.
- [ ] Chọn category: Games → Action.
- [ ] Screenshots tối thiểu 2 (đã có 7 trong `docs/screenshots/`).
