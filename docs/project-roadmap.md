# Lộ Trình Dự Án

Lộ trình này bám theo plan hiện có trong
`plans/260722-1922-assassins-creed-reconstruction/` và phản ánh trạng thái bàn
giao cuối đã được kiểm định tĩnh.

## Trạng thái phase

| Phase | Tên | Trạng thái |
|---|---|---|
| 1 | Baseline and Completeness Inventory | completed |
| 2 | Semantic Source Reconstruction | completed |
| 3 | Resource Format Reconstruction | completed |
| 4 | Reverse Engineering Technical Analysis | completed |
| 5 | Modern Mobile Technical Design | completed |
| 6 | Verification and Final Handoff | completed |

## Follow-up track: Semantic Registry and parity harness

Track này giữ riêng với reconstruction historical ở trên. Nó không renumber các
phase 1-6 cũ và chỉ mô tả slice static-only cho alias registry + host-side
contracts.

| Phase | Tên | Trạng thái |
|---|---|---|
| 1 | Canonical Semantic Registry | completed |
| 2 | Executable Gameplay Parity Contracts | completed |
| 3 | Verification and Documentation | completed |

## Follow-up track 2: Entity/Render/Timeline Parity Slice 2

Track này giữ riêng với reconstruction historical và parity harness cũ. Nó chỉ
mô tả slice static-only cho entity store, render ordering, slow-time và
timeline scheduler.

| Phase | Tên | Trạng thái |
|---|---|---|
| 1 | Promote Timeline Semantic Aliases | completed |
| 2 | Model Entity Store and Render Ordering | completed |
| 3 | Model Slow-Time and Timeline Scheduling | completed |
| 4 | Verify Corpus Contracts and Update Documentation | completed |

## Follow-up track 3: Timeline Opcode and Completion Parity Slice 3

Track này được quản lý tại
[`plans/260723-1820-timeline-opcode-completion-parity/`](../plans/260723-1820-timeline-opcode-completion-parity/plan.md).
Plan đã `completed`; implementation, kiểm định độc lập và tài liệu bàn giao đã
được khóa sổ.

| Phase | Tên | Trạng thái |
|---|---|---|
| 1 | Canonical Aliases and Contract Schema | completed |
| 2 | Timeline Completion Parity | completed |
| 3 | Extended Opcode Effect Parity | completed |
| 4 | Verification and Documentation | completed |

## Follow-up track 4: Rewrite toolchain spike

Track tại
[`plans/260921-0830-libgdx-toolchain-spike/`](../plans/260921-0830-libgdx-toolchain-spike/plan.md).
Đây là slice implementation đầu tiên của bản rewrite: thực thi mandatory
decision gate của ADR framework trên Kotlin + LibGDX `1.14.2`.

|| Phase | Tên | Trạng thái |
||---|---|---|
|| 1 | Project Scaffold | completed |
|| 2 | Core Simulation Slice | completed |
|| 3 | Content Conversion Slice | completed |
|| 4 | LibGDX Adapters and Launchers | completed |
|| 5 | Gate Verification and Handoff | completed — iOS half của gate item 1 pending macOS |

Kết quả gate:
[`reports/gate-results.md`](../plans/260921-0830-libgdx-toolchain-spike/reports/gate-results.md)
— Android + desktop pass trên mọi item; iOS pending; chưa phải bản port game.

## Follow-up track 5: Gameplay mining

Track tại
[`plans/260921-0929-gameplay-mining/`](../plans/260921-0929-gameplay-mining/plan.md).
Đào semantic gameplay từ artifact đã decode để làm content contract cho port:

| | Phase | Tên | Trạng thái |
|---|---|---|---|
| 1 | String-table corpus | completed |
| 2 | Level atlas | completed |
| 3 | Player mechanics | completed |
| 4 | Entity-type catalog | completed |
| 5 | Audio và sprite usage | completed |
| 6 | GDD synthesis | completed |

Kết quả: [`docs/gameplay-mining/`](gameplay-mining/) (corpus, atlas, catalog,
mechanics, audio/sprite) và [`docs/gameplay-design-document.md`](gameplay-design-document.md)
— 8 mission, entity dispatch 0–80, player FSM, physics constants, cheat/audio
tables.

## Kết quả thực tế

### Đã hoàn tất

- Khóa artifact gốc ở trạng thái static-only.
- Ghi hash, counts, bytecode coverage và resource inventory.
- Tạo package phục hồi `reconstructed-project/`.
- Gắn semantic alias có evidence cho code và phân loại 238/238 payload không rỗng.
- Đặc tả pack, sprite, level entity/script, save, font/remap và lookup table.
- Giao technical analysis có lệnh tái tạo static-only.
- Giao technical design Android/iOS, không triển khai hoặc chạy game.
- Chạy audit cuối cho hash, counts, links, deterministic outputs và mọi claim;
  verifier đạt `ok: true`, `failures: []`.
- Trong Slice 2 historical, registry alias canonical đã mở rộng từ 31 lên 42
  method và harness parity host-side đã bao phủ materialization, lookup, render
  ordering, slow-time và timeline scheduling.
- Parity Slice 2 historical đã pass 30/30 tests; manifest 30 fixture giữ nguyên
  và cả 4 phase của track đó đã completed.
- Slice 3 đã mở rộng overlay hiện tại lên 12 class / 44 method / 41 field,
  mô hình hóa completion `i.bI:()V` và extended opcode `100..114`.
- Contract Slice 3 biểu diễn direct field writes bằng immutable state
  transitions và downstream calls bằng ordered effect intentions, không thực
  thi chúng.
- Manifest Slice 3 riêng có 15 fixture: 14 corpus và một
  synthetic-source-contract cho opcode `109`, opcode duy nhất không xuất hiện
  trong corpus. 480 extended opcode occurrence trên 3.705 instruction đã được
  đối chiếu.
- Slice 3 đạt 27/27 focused tests và 57/57 full unittest discovery bằng phân
  tích static-only; không chạy target JAR, MIDlet, class, emulator, simulator
  hay thiết bị. Cả bốn phase đã completed.

## Deliverables theo phase

| Phase | Output chính |
|---|---|
| 1 | Manifest, counts, baseline evidence. |
| 2 | Four-view code package (ba decompile + exact bytecode) và symbol mapping. |
| 3 | Resource extraction, decode metadata, format documentation. |
| 4 | `docs/reverse-engineering-technical-analysis.md`, `docs/codebase-summary.md`, `README.md`. |
| 5 | `docs/modern-mobile-technical-design.md` và decision record liên quan. |
| 6 | Final handoff, kiểm chứng và đối soát toàn bộ artifact. |
| Follow-up 1 | Canonical alias registry update, inventory pin, and overlay sync. |
| Follow-up 2 | Gameplay parity contracts, fixture manifest, and `unittest` oracle. |
| Follow-up 3 | Verification and documentation for the parity slice. |
| Slice 2 / Phase 1 | Promote three timeline semantic aliases. |
| Slice 2 / Phase 2 | Entity-store lifecycle and render ordering contracts. |
| Slice 2 / Phase 3 | Slow-time and timeline scheduler contracts. |
| Slice 2 / Phase 4 | Corpus verification, review, reports, and documentation sync. |
| Slice 3 / Phase 1 | Completion/extended-opcode aliases and contract schema; implementation verified. |
| Slice 3 / Phase 2 | `i.bI:()V` completion contract; implementation verified. |
| Slice 3 / Phase 3 | Opcode `100..114` contracts and separate 15-fixture manifest; implementation verified. |
| Slice 3 / Phase 4 | Verification, final documentation and handoff; completed. |

## Quy tắc roadmap

- Không đánh dấu hoàn tất cho tài liệu chưa được tạo.
- Không đổi trạng thái phase nếu chưa có artifact tương ứng.
- Không ghi “buildable” khi mới chỉ có reverse-engineering hoặc design note.
- Không gọi parity harness là proof of runtime execution hoặc MIDlet fidelity.

## Hậu bàn giao tùy chọn

1. Nếu bắt đầu viết lại game, dùng technical design làm đầu vào cho một plan
   implementation riêng; việc đó nằm ngoài bài tập hiện tại.
2. Chỉ mở thêm vòng static research khi có evidence mới cho ba sprite module,
   low-opcode effects, recursive helpers, mode `3`, opcode `41..44`, slot-3
   semantics hoặc JAD gốc.
3. Sau mọi thay đổi artifact, chạy lại toàn bộ static verifier trước khi công bố.
