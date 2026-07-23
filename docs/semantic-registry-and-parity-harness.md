# Semantic Registry and Gameplay Parity Harness

Tài liệu này mô tả slice static-only cho Semantic Registry và harness parity
host-side. Đây là contract reference sạch, không phải recovered source runtime,
và không chứng minh MIDlet/gameplay parity bằng thực thi game.

## Mục đích

- Giữ canonical alias registry đồng bộ với inventory sinh từ bytecode.
- Khóa một số invariants gameplay đã quan sát được bằng `unittest` thuần Python.
- Tách rõ ba authority layer: registry, decoded corpus oracle, và synthetic
  source contracts.

## Authority layers

| Layer | Nguồn | Vai trò |
|---|---|---|
| Registry | `scripts/java-me-semantic-aliases.json`; bản sinh ở `reconstructed-project/inventory/semantic-aliases.json` | Canonical alias overlay cho class, method, field. |
| Decoded corpus oracle | `scripts/decode-gameloft-level-records.py` + `reconstructed-project/resources/levels-decoded/` | Parser và artifact chuẩn cho 8 pack/4.286 record đã pin. |
| Legacy parity contracts | `scripts/gameplay_parity_contracts.py` + `tests/fixtures/gameplay-parity-contracts.json` + `tests/test_gameplay_parity_contracts.py` | Clean-room spec cho entity, render, integration và scheduler boundary. |
| Completion/extended parity contracts | `scripts/timeline_opcode_contracts.py` + `tests/fixtures/timeline-opcode-contracts.json` + `tests/test_timeline_opcode_contracts.py` | Immutable host transitions cho `i.bI()` completion và opcode `100..114`; external helper/UI/audio calls chỉ được ghi thành ordered intentions. |

## Canonical registry slice

Current verified registry counts:

- 12 class
- 44 method
- 41 field

Thirteen canonical method overlay entries are now pinned in the working
registry. The two Slice-3 promotions are:

- `i.bI:()V` -> `completeTimelineScript`
- `i.a:(I[BIII)I` -> `executeExtendedTimelineOpcode`

The three slice-2 promotions are:

- `i.aa:()V` -> `stepTimelineScript`
- `i.ab:()Z` -> `isTimelineScriptActive`
- `k.s:(I)I` -> `findScriptGroupIndex`

The earlier eight overlay entries remain canonical:

- `i.u:()V` -> `updateCameraDistanceTier`
- `i.s:()V` -> `advanceAnimationFrame`
- `i.t:()[I` -> `rebuildFrameBounds`
- `k.d:(Z)V` -> `materializeLevelEntities`
- `k.b:(Li;)V` -> `addEntity`
- `k.c:(Li;)V` -> `removeEntity`
- `k.q:(I)Li;` -> `findEntityById`
- `k.d:(Li;)V` -> `insertIntoRenderInteractionList`

`k.d:(Z)V` is documented as `materializeLevelEntities`; the boolean parameter
meaning is not canonized.

## What the harness covers

Hai host modules model the following offline slices:

1. entity materialization và post-selection retyping;
2. UID lookup precedence;
3. fixed entity-store lifecycle theo JVM reference identity;
4. render/interaction ordered insertion;
5. normal/slow `i.I()` 8.8 fixed-point integration;
6. script-group lookup, activity predicate và timeline scheduler boundary;
7. direct state writes và return boundaries của `i.bI()` completion;
8. decode, direct state writes và return boundaries của opcode `100..114`.

Các module không mở JAR, load target class hay chạy MIDlet code.

### Entity store và render ordering

- `k.b(i)` giữ nguyên object reference, đặt `as=-98`, pop free slot theo LIFO,
  nếu không thì append tại high-water; store đầy thì drop im lặng sau mutation.
- `k.c(i)` so sánh reference identity, không dùng token, UID hay value equality.
  Method clear matching globals trước scan, chỉ xóa slot identity-match đầu tiên,
  ghi tombstone `-99` trước `i.p()`, rồi clear slot và push free index.
- Contract chỉ giữ vị trí gọi cleanup trên normal-return path; side effect hoặc
  exception bên trong `i.p()` chưa được mô phỏng.
- `k.d(i)` insert tăng theo `(az, al)`; exact tie đặt item mới trước, duplicate
  reference vẫn tồn tại, và overflow backing array vẫn là unsafe legacy path.

### Fixed-point và timeline boundary

Slow integration chia riêng velocity/acceleration contribution bằng Java
`idiv`, rồi mới cộng; truncate-toward-zero, int overflow, `MIN/-1` và zero
divisor đều theo JVM. Với timeline, mỗi lane dispatch current event trước khi
xét due, kể cả event tương lai. Điều kiện
`event.tick <= evaluated_tick` chỉ quyết định cursor advance.

Opcode trong fixture được chuẩn hóa thành raw `0..255`, nhưng bytecode so sánh
signed `baload`: raw `0..99` và `128..255` đi inline, chỉ `100..127` đi extended.
Corpus hiện có max opcode `114`, không có raw opcode trên `127`. Exact-tick
opcode `108`/`113` có thể trả `-1`; scheduler composition biến result âm thành
`execution_aborted=true` sau tick stage nhưng trước current cursor advance,
later opcode/lane và completion handling.

`scripts/timeline_opcode_contracts.py` tách ba phase của `108`/`113`: future
poll, exact-tick branch và past-tick no-op. Direct field/array/global writes của
completion và opcode `100..114` được biểu diễn bằng immutable host transitions.
Các call legacy sang helper, UI, audio, entity lookup/removal hoặc script switch
không được thực thi; contract giữ symbol, arguments, observed result và order
bằng effect intentions. Vì vậy đây là parity của direct transition/call
boundary, không phải runtime backend parity.

## Fixture manifest

### Legacy manifest

`tests/fixtures/gameplay-parity-contracts.json` currently contains 30 fixtures:

| Basis | Count |
|---|---:|
| `corpus` | 12 |
| `synthetic-derived-from-corpus` | 1 |
| `synthetic-source-contract` | 17 |

Corpus fixtures are pinned to decoded level packs and raw record hashes. The
corpus does not include shipped cases for:

- raw type `11` / subtype `80`;
- duplicate UID;
- UID `-1`.

Các gap materialization đó và những control-flow edge chỉ có trong bytecode là
lý do tồn tại synthetic fixtures. Store capacity/overflow, arithmetic,
scheduler, signed-byte và abort fixture không được trình bày như shipped corpus
evidence.

The reduced fixture capacities `[2, 4]` for entity slots and `[3, 10]` for
render ordering are deterministic harness parameters only; they do not replace
the legacy 1000-slot capacity.

Current static validation passes 30/30 tests, with the broad verifier reporting
`ok=true`, `failures=[]`, `analysis_mode=static-only`, and
`game_execution_performed=false`.

### Completion/extended manifest

`tests/fixtures/timeline-opcode-contracts.json` là manifest riêng gồm 15
fixtures:

| Basis | Count |
|---|---:|
| `corpus` | 14 |
| `synthetic-source-contract` | 1 |

Mười bốn corpus fixture pin first occurrence của mọi opcode quan sát được trong
`100..114`; mỗi entry giữ pack/path hash, slot-7 hash, group/lane/event/
instruction index, offset và raw bytes. Opcode `109` có đúng `0` occurrence
trong corpus nên fixture duy nhất của nó là `synthetic-source-contract`, pin
descriptor `i.a:(I[BIII)I` và bytecode evidence thay vì giả làm shipped data.

Exhaustive oracle kiểm tra toàn bộ `3.705` instruction slot `7`, trong đó có
`480` extended occurrences. Current static validation passes 27/27 focused
tests và 57/57 full discovery tests, với `0` failure/error.

## Command set

Use these commands to verify the slice:

```bash
python3 -B -m unittest tests.test_gameplay_parity_contracts -v
python3 -B -m unittest tests.test_timeline_opcode_contracts -v
python3 -B -m unittest discover -s tests -p 'test_*.py' -v
python3 -B scripts/inventory-java-me-bytecode.py reconstructed-project/bytecode reconstructed-project/inventory
python3 -B scripts/verify-static-reconstruction.py assassins_creed_-_br_320x240_136711.jar reconstructed-project --report reconstructed-project/verification-report.json
```

The broad verifier currently reports `ok=true`, `failures=[]`, and
`game_execution_performed=false`.

## Extension rules

When adding new registry or parity material:

- keep JSON canonical and static-only;
- preserve exact JVM descriptor, confidence, evidence, and unique alias fields;
- regenerate and update intentional pins instead of layering ad hoc overrides;
- for fixtures, require exact `id`, `basis`, `confidence`, `source`, `input`,
  and `expected` fields;
- corpus fixtures must pin path, hash, record index, and raw record bytes;
- derived fixtures must list exact mutations from the pinned corpus source;
- synthetic source-contract fixtures must cite symbol and bytecode evidence.

## Known gaps

This slice does not prove:

- runtime or MIDlet parity;
- full-frame parity;
- pixel/full-frame rendering parity; chỉ list ordering được model;
- collision parity;
- AI parity;
- low-opcode direct effects;
- internal effects của recursive/legacy helpers được ghi dưới dạng intentions,
  gồm `i.bJ()`, `i.h(int)`, `i.k(int)` và `k.c(i)`;
- script mode `3` và low opcodes `41..44`;
- backend UI/audio/media behavior ngoài ordered call boundary;
- recovered source fidelity.

The guide is intentionally narrower than the full static verifier. It validates
selected statically recovered invariants against pinned decoded artifacts and
explicitly labeled synthetic source contracts only.

## Related links

- [README](../README.md)
- [Codebase summary](./codebase-summary.md)
- [Project overview and PDR](./project-overview-pdr.md)
- [Project roadmap](./project-roadmap.md)
- [Legacy architecture dossier](./inferred-legacy-game-architecture.md)
- [Level record formats](./level-record-formats.md)
