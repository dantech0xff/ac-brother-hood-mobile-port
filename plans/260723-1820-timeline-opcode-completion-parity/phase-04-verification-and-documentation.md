---
phase: 4
title: "Verification and Documentation"
status: completed
priority: P1
dependencies:
  - 3
---

# Phase 4: Verification and Documentation

## Context Links

- [Plan](./plan.md)
- [Static verifier](../../scripts/verify-static-reconstruction.py)
- [Parity guide](../../docs/semantic-registry-and-parity-harness.md)

## Overview

Prove compatibility and corpus coverage, regenerate managed artifacts, audit the
diff, and document the new evidence boundary.

## Requirements

- Functional: exhaustive 3,705-instruction/480-extended oracle; test discovery;
  inventory regeneration; verifier pins; docs and journal.
- Non-functional: no stale pass report, no target execution, no broken links,
  no changed Slice 2 fixtures.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/verify-static-reconstruction.py`
- Regenerate:
  `/Users/dan/Desktop/ac-java-game/reconstructed-project/inventory/semantic-aliases.json`
- Regenerate:
  `/Users/dan/Desktop/ac-java-game/reconstructed-project/verification-report.json`
- Update: `/Users/dan/Desktop/ac-java-game/README.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/codebase-summary.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/project-overview-pdr.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/project-roadmap.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/semantic-registry-and-parity-harness.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/symbol-map.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/inferred-legacy-game-architecture.md`
- Update: `/Users/dan/Desktop/ac-java-game/docs/level-record-formats.md`
- Create:
  `/Users/dan/Desktop/ac-java-game/plans/260723-1820-timeline-opcode-completion-parity/reports/implementation-verification.md`
- Create plan-scoped tester/debugger/reviewer/PM reports only when those agents
  produce material evidence.
- Create:
  `/Users/dan/Desktop/ac-java-game/docs/journals/2026-07-23-timeline-opcode-completion-parity.md`

## Implementation Steps

1. Run syntax/focused tests, then full `unittest` discovery.
2. Exhaustively assert per-pack opcode counts, layouts, offsets, and provenance.
3. Regenerate bytecode inventory and update intentional alias count/hash pins.
4. Run the broad static verifier and confirm target execution false.
5. Run edge-case scout, debugger, spec review, code review, and fresh tests.
6. Update docs/counts/scope, validate links/claims, sync all plan checkboxes.
7. Write implementation, test, review, PM, and journal reports.

`verify-static-reconstruction.py` changes are limited to intentional alias
count/hash pins after inventory regeneration; verifier logic stays unchanged.

## Validation

```bash
python3 -B -m unittest discover -s tests -p 'test_*.py' -v
python3 -B scripts/inventory-java-me-bytecode.py reconstructed-project/bytecode reconstructed-project/inventory
python3 -B scripts/verify-static-reconstruction.py assassins_creed_-_br_320x240_136711.jar reconstructed-project --report reconstructed-project/verification-report.json
git diff --check
node /Users/dan/.claude/scripts/validate-docs.cjs docs/
```

## Success Criteria

- [x] Existing 30 tests/fixtures pass unchanged.
- [x] New focused tests and exhaustive corpus oracle pass.
- [x] Registry source/generated copies match at 12/44/41.
- [x] Verifier reports `ok=true`, `failures=[]`,
  `analysis_mode=static-only`, `game_execution_performed=false`.
- [x] `git diff --check`, JSON parse, and docs validation complete.
- [x] Independent tester/debugger/reviewer report no blocking defect.

## Risk Assessment

Risk: generated inventory/verifier output diverges from source registry.
Mitigation: regenerate before pinning and rerun the verifier afterward.

Rollback: restore source registry/verifier pins, then regenerate inventory and
verification report from that source state; reverse only the listed docs and
report/journal additions.

## Security Considerations

Verifier may inspect ZIP/class bytes statically but must never load or execute
target code.
