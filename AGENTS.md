# AGENTS.md — working rules for this repository

## Mục tiêu dự án (đọc trước khi làm việc)

**Remake toàn bộ game Assassin's Creed Brotherhood J2ME (320×240) bằng
Kotlin + LibGDX.** Đây là bản remake thật, không phải demo/spike: port đúng
core của bản gốc — tick 62ms, fixed-point 8.8, clip-as-FSM (`S` = animation
index), entity dispatch ax 0–80, 8 mission, flying/chase/boss — dùng assets
đã decode từ JAR, không vẽ lại.

## Luật bất biến

- **Static-only cho JAR gốc**: `assassins_creed_-_br_320x240_136711.jar`
  không bao giờ được chạy — không `java -jar`, emulator, simulator, hay
  thiết bị. Decompiler/phân tích dữ liệu thì OK. (Riêng `rewrite/` —
  bản port mới — được build và chạy bình thường trên emulator/device.)
- **Provenance + confidence**: mọi claim về hành vi gốc gắn nhãn
  `proven` / `high-confidence` / `inferred` / `unknown` và trích dòng
  nguồn (`file:line` trong `reconstructed-project/src/`).
- **Verifier phải xanh**: `python3 scripts/verify-static-reconstruction.py
  assassins_creed_-_br_320x240_136711.jar reconstructed-project` → `ok:true`;
  `python3 -m unittest discover -s tests` → toàn bộ pass. Chạy trước mỗi PR.
- **Không xấp xỉ ngầm**: cơ chế gameplay phải theo semantic đã mine trong
  `docs/gameplay-mining/`; chưa mine thì mine trước, không đoán.

## Conventions

- Docs repo viết tiếng Việt; file trong `plans/` viết English với YAML
  frontmatter (`title`, `phase`, `status`, …).
- Plan track: `plans/YYMMDD-HHMM-slug/` gồm `plan.md` + `phase-NN-*.md` +
  `reports/`; cập nhật status khi xong.
- Contract cốt lõi cho port (đã pin trong `rewrite/`): tick 62ms, ≤4
  catch-up, fixed-point 8.8, RNG parity Java-LCG, input queue có sequence,
  `PlaySfx` deferred, save 49-byte `ACRS`.
- Entity/anim: `S` = index vào clip sprite (`b`), `r()` = anim-hết-frame;
  clip space `k.z[75]` trên pack-3; bảng `bi/bj/bk/bl/bm/bn` map ax→clip —
  xem `docs/gameplay-mining/state-animation-map.md`.

## Điểm vào

- `docs/gameplay-design-document.md` — GDD tổng hợp = content contract.
- `docs/gameplay-mining/` — corpus, level atlas, mechanics, entity catalog,
  NPC FSM, animation map, chase/boss directors.
- `docs/modern-mobile-technical-design.md` — thiết kế port; ADR
  `docs/decisions/mobile-game-framework.md`.
- `rewrite/README.md` — trạng thái build spike + cách chạy.
