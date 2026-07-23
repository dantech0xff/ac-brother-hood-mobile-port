# Slice 3 Defect-Closure Diagnostic

## Executive Summary

- **Issue:** Re-audit nine bytecode-parity defects found during Slice 3 review.
- **Impact:** Incorrect guard order, field conflation, helper observations, or
  prompt/null handling could make the host contract diverge from `i.bI()` or
  `i.a:(I[BIII)I`.
- **Root cause:** Initial contract draft normalized several JVM call/state
  boundaries too aggressively.
- **Status:** Resolved. Fresh focused and discovery suites pass; final static
  verifier reports no failure.
- **Safety:** Host Python/static artifacts only. No JAR, class, MIDlet,
  emulator, simulator, or device execution.

## Defect Audit

| Defect | Closure evidence | Verdict |
|---|---|---|
| Common guard before operand bytes | `scripts/timeline_opcode_contracts.py:1262-1278` resolves opcode width and returns `operands=None` before `decode_extended_timeline_opcode`. `tests/test_timeline_opcode_contracts.py:852-899` covers every non-prompt opcode and accepts malformed `b""` on a non-exact tick. Matches bytecode PCs 0-27. | Closed |
| Distinct legacy `bM` and `af` | `TimelineEntityState` has separate `linked_bm_ref` and `animation_af_ref` (`:526-527`). Completion clears only the linked-`bM` chain (`:1049-1075`); opcode 100 subaction 10 follows only `animation_af_ref` (`:1520-1534`). Identity/preservation regressions: tests `:442-512` and `:1144-1190`. | Closed |
| Opcode 105 post-call latch | When initial `cd[1]` is false, `advance_latched_after_media` is mandatory, written into the projected owner, then freshly read for `k.v=k.w` (`:1616-1660`). Tests `:1300-1377` cover true, false, already-latched, and missing-observation paths. Matches opcode 105 PCs 939-989. | Closed |
| Static/helper intention arguments | Static `i.ai:()Z` and `i.O:()V` intentions have no receiver argument (`:1120-1122`, `:1346-1353`); `a.<init>:()V` uses an empty argument tuple (`:1723-1729`, `:1974-1983`); static spawn call carries exactly `(8, 59, word0, byte>0, word1, word2, word3)` (`:1932-1943`). Tests assert these tuples at `:521-532`, `:1658`, and `:1918-1928`. | Closed |
| Null prompt widgets | Opcode 108 permits a null keypad widget while no transition dereferences it, but rejects confirm/cancel paths that would dereference it (`:1761-1824`). Opcode 113 preserves its null-aware keypad confirmation and per-slot cancel behavior (`:2038-2156`). Regressions: tests `:1607-1628` and `:2049-2073`. Touch-mode current-widget dereferences remain explicit validation failures. | Closed |
| Opcode 112 empty option set | Option filtering occurs first; keypad/presence observations are requested only when options remain (`:1960-1973`). Empty setup still writes `MultiplePromptState((), 0)` and `prompt_response=0` with no intention (`:2012-2024`). Test: `:1998-2008`. | Closed |
| Nullable localization | `LocalizedTextObservation` distinguishes missing context from an observed null (`:626-635`, `:793-870`). Opcode 114 accepts the wrapper’s `None` and writes nullable `TimedTextState` (`:2190-2212`). Test `:2286-2301` verifies null propagation. | Closed |
| Java `int` wrap | Opcode 114 computes the localization table index with `_java_i32(1 + level_index)` (`:2197-2203`). Test `:2269-2285` proves `JAVA_INT_MAX + 1 -> JAVA_INT_MIN`. Matches bytecode PC 3317-3324 `iadd`. | Closed |
| Opcode 106 sticky slots | Existing message slots 7 and 9 are written only when their bytecode predicates hold (`:1683-1696`). Test `:1400-1422` proves values 77/99 survive the false-predicate path. | Closed |

## Validation Evidence

Focused contract suite:

```text
python3 -B -m unittest tests.test_timeline_opcode_contracts -v
Ran 27 tests in 0.317s
OK
```

Full host-side discovery:

```text
python3 -B -m unittest discover -s tests -p 'test_*.py' -v
Ran 57 tests in 1.312s
OK
```

The focused suite includes the exhaustive corpus oracle: 3,705 instructions,
480 extended occurrences, canonical layouts, and the synthetic-only opcode 109
source contract.

Final `reconstructed-project/verification-report.json` read after its
transactional reset/run completed:

```json
{
  "ok": true,
  "failures": [],
  "analysis_mode": "static-only",
  "game_execution_performed": false,
  "semantic_aliases": {"classes": 12, "methods": 44, "fields": 41},
  "instructions": 3705
}
```

## Findings

- All previously reported defects have direct regression coverage and matching
  implementation branches.
- No blocking defect reproduced.
- External legacy calls remain ordered intentions; target behavior is not
  recursively simulated.
- No implementation, test, fixture, verifier, or documentation file changed by
  this diagnostic.

## Recommendations

- No P0/P1 corrective action.
- Retain the named regressions and exhaustive corpus oracle as merge gates.

## Unresolved Questions

None.

Status: DONE

Summary: Fresh static audit and 27/27 focused, 57/57 discovery, and broad
verifier evidence confirm all reviewed defects are closed.

Concerns/Blockers: None.
