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
| Executable source contracts | `scripts/gameplay_parity_contracts.py` + fixture manifest + `tests/test_gameplay_parity_contracts.py` | Clean-room spec; từng fixture tự khai báo `corpus`, `synthetic-derived-from-corpus`, hoặc `synthetic-source-contract`. |

## Canonical registry slice

Current verified registry counts:

- 12 class
- 42 method
- 41 field

Eleven canonical method overlay entries are now pinned in the working registry.
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

`scripts/gameplay_parity_contracts.py` models the following small, offline
slices:

1. entity materialization và post-selection retyping;
2. UID lookup precedence;
3. fixed entity-store lifecycle theo JVM reference identity;
4. render/interaction ordered insertion;
5. normal/slow `i.I()` 8.8 fixed-point integration;
6. script-group lookup, activity predicate và timeline scheduler boundary.

The module never opens the JAR, loads target classes, or runs MIDlet code.

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
opcode `108`/`113` bắt buộc caller cung cấp `extended_dispatch_results`; result
âm làm `execution_aborted=true` sau tick stage nhưng trước current cursor
advance, later opcode/lane và completion handling. Opcode gameplay effects và
`i.bI()` completion mutations vẫn ngoài contract.

## Fixture manifest

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

## Command set

Use these commands to verify the slice:

```bash
python3 -B -m unittest tests.test_gameplay_parity_contracts -v
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
- full opcode gameplay effects hoặc `i.bI()` completion effects;
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
