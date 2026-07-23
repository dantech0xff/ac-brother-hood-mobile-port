# Semantic Registry and Parity Harness — Implementation Verification

Date: 2026-07-23  
Result: **PASS**  
Analysis mode: `static-only`  
Target game execution: **not performed**

## Delivered

| Area | Result |
|---|---|
| Semantic Registry | Canonical overlay expanded to 12 class / 39 method / 41 field aliases. |
| Registry integrity | Source and generated copy match; normalized SHA-256 `af1648df6d2906514e221a2bf340fa1cd9a1cfb4dc4d9d1a200fb29402d79c90`. |
| Executable specification | Clean-room Python contracts for record routing/materialization, selector-before-remap, `k.q(int)` lookup, and normal `i.I()` 8.8 integration. |
| Fixtures | 16 total: 10 corpus, 1 synthetic-derived-from-corpus, 5 synthetic-source-contract. |
| Corpus oracle | Packs 6–13, 4,286 entity records, 16/16 payloads exact EOF; remaps `47: 12`, `50: 8`. |
| Documentation | Evergreen guide, entry points, PDR, roadmap, symbol map, and legacy dossier synchronized. |

## Verification commands

### Focused contract suite

```bash
python3 -B -m unittest tests.test_gameplay_parity_contracts -v
```

Result: 11/11 passed; 0 failure, 0 error, 0 skip. Independent tester run:
0.793 s.

### Full test discovery

```bash
python3 -B -m unittest discover -s tests -p 'test_*.py' -v
```

Result: 11/11 passed; final controller run 0.731 s. The suite validates fixture
schema/provenance, representative bytecode-grounded expectations, all 4,286
decoded records, and in-memory regeneration of all eight level packs.

### Inventory regeneration

```bash
python3 -B scripts/inventory-java-me-bytecode.py \
  reconstructed-project/bytecode \
  reconstructed-project/inventory
```

Result: PASS. A separate static regeneration under `/private/tmp` compared
byte-identically with the committed managed inventory. No target class was
loaded.

### Broad forensic verifier

```bash
python3 -B scripts/verify-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  reconstructed-project \
  --report reconstructed-project/verification-report.json
```

Result:

| Check | Value |
|---|---|
| `ok` | `true` |
| `failures` | `[]` |
| `analysis_mode` | `static-only` |
| `game_execution_performed` | `false` |
| Fresh `javap` | 12/12 match |
| Fresh extraction | match |
| Fresh sprite regeneration | match |
| Fresh level regeneration | match |
| Stale transaction artifacts | none |

The verifier reads the pinned JAR only as ZIP/class data and uses `javap`; it
does not launch Java target code or a MIDlet.

### Documentation and hygiene

```bash
node /Users/dan/.claude/scripts/validate-docs.cjs docs/
git diff --check
```

Result: 80/80 internal links working; `git diff --check` passed. Validator
warnings are non-blocking legacy heuristics: 141 semantic/obfuscated code-name
references and 6 unrelated uppercase tokens treated as config keys. No broken
relative link was reported.

JSON parsing passed for the canonical alias source, generated alias copy,
fixture manifest, and broad verification report.

## Independent review

- Tester: focused and discovery suites PASS; independent broad verifier PASS;
  no repository write when report target was `/private/tmp`.
- Debug audit: found an initial `k.q(-1)` validation-order mismatch and missing
  special selector coverage. Fixes preserve early returns, validate slots
  lazily, and cover `bk/bl/bm/bj/bn` with pinned corpus records. Re-audit PASS.
- Code review: initial Medium gap for raw type `55` waypoint destination. Added
  pinned pack-7 record 98 fixture plus full-corpus destination assertion.
  Targeted re-review PASS; no unresolved Critical, High, or Medium finding.

## Evidence boundary

This harness validates selected statically recovered invariants. It is not
recovered commercial source and does not prove MIDlet/runtime, full-frame,
rendering, collision, AI, script-executor, or full-game parity. Raw type
`11`/subtype `80`, duplicate IDs, and lookup ID `-1` are explicitly labeled
derived/source-contract cases because the shipped decoded corpus lacks them.

## Known limitations

- Slow-time `i.I()` division branch is not modeled in this slice.
- Sprite registry contents and renderer output are outside this contract.
- Complete entity subtype/FSM and script opcode parity remain future work.
- Kotlin/LibGDX implementation remains out of scope.

## Unresolved questions

None blocking this plan. Future slices must retain the same evidence/basis
labels and static-only boundary.

Docs impact: major.
