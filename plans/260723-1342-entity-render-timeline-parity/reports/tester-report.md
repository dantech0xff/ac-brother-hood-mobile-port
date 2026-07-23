# Tester Report

Date: 2026-07-23

Scope: independent static-only validation of current Parity Slice 2 contract
code, fixtures, and tests.

## Summary

- Verdict: `DONE`
- Current suite: 30/30 passed, zero failures or skips.
- Earlier 26-test result and guard-gap list are superseded.
- Coverage not run; explicitly outside this validation request.
- No blockers or unresolved verification concerns.

## Validated Files

- `scripts/gameplay_parity_contracts.py`
- `tests/test_gameplay_parity_contracts.py`
- `tests/fixtures/gameplay-parity-contracts.json`

No MIDlet, JAR, emulator, simulator, or device execution. No code or test
files edited.

## Fixture Schema

- Top-level keys: `schema_version`, `analysis_mode`,
  `target_code_execution`, `allowed_basis`, `harness_parameters`, `fixtures`
- Schema version: 1
- Analysis mode: `static-only`
- Target code execution: `false`
- Harness capacities: legacy entity capacity 1000; entity fixture capacities
  2 and 4; render fixture capacities 3 and 10
- Reduced fixture capacities expose ordering and overflow deterministically;
  they do not replace the legacy 1000-slot constant.
- Total fixtures: 30
- Basis counts: `corpus` 12, `synthetic-derived-from-corpus` 1,
  `synthetic-source-contract` 17
- Kind counts: `entity-materialization` 11, `entity-lookup` 4,
  `entity-store-lifecycle` 4, `coordinate-integration` 3,
  `script-group-lookup` 3, `render-interaction-order` 2,
  `timeline-scheduling` 2, `timeline-activity` 1

## Test Run

Command:

```bash
python3 -B -m unittest -v tests.test_gameplay_parity_contracts
```

Result:

- Ran 30 tests in 0.691s
- Passed: 30
- Failed: 0
- Errors: 0
- Skipped: 0

## Targeted Verification

| Contract | Status | Evidence |
|---|---|---|
| Reference identity and full-store handling | PASS | `test_reference_identity_is_not_token_or_uid_equality` passed. It rejects token/UID lookalikes and verifies same-reference full-store drop behavior. Fixture `store-full-drop-then-free-slot-reuse` also passed through `test_lifecycle_fixtures`. |
| Future-event dispatch | PASS | Fixture `timeline-step-slow-hold-one-event-per-lane` passed. The tick-6 event dispatches while tick 5 is evaluated, then its cursor remains held. |
| Signed-byte opcode branch | PASS | `test_timeline_host_contract_validation_edges` passed classifications for raw opcodes 128 and 255 as signed-byte inline-low cases, versus 100 and 127 as extended cases. |
| Opcode 108/113 abort result | PASS | `test_timeline_host_contract_validation_edges` iterates both opcodes and passes exact-tick missing-result rejection and negative-result abort assertions. Shared nonnegative continuation and future-event non-abort boundaries also pass. |

## Updated Gap Assessment

The prior report's guard-gap claims are now stale. Current direct tests cover
invalid render host inputs, invalid script IDs, timeline opcode bounds,
negative lane indexes, lane-count mismatch, invalid lane events, and tracked
remove tombstone bounds failure.

No failures observed in the exercised entity store, render ordering,
coordinate integration, script lookup, timeline activity, timeline scheduling,
or full-corpus oracle contracts.

## Unresolved Questions

None.
