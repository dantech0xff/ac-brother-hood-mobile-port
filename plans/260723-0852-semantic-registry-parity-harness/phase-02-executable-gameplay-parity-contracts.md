---
phase: 2
title: Executable Gameplay Parity Contracts
status: completed
priority: P1
dependencies:
  - 1
---

# Phase 2: Executable Gameplay Parity Contracts

## Overview

Create an importable, clean-room reference for the first gameplay invariants and
test it against both canonical decoded records and explicitly labeled synthetic
source-contract cases.

## Requirements

- Functional: entity route/materialization, raw-to-runtime remap ordering,
  legacy ID lookup, and normal dual-coordinate integration.
- Non-functional: Python standard library only; Java signed/truncation semantics;
  deterministic failures with fixture IDs and JSON paths; read-only corpus use.

## Architecture

`scripts/gameplay_parity_contracts.py` is an importable reference module, not a
game runtime. `tests/fixtures/gameplay-parity-contracts.json` stores small inputs,
expected outputs, confidence, basis, and pointers/hashes into the delivered
level JSON. Tests also import the existing level decoder in memory and compare
all packs with delivered output.

## Related Code Files

- Create: `/Users/dan/Desktop/ac-java-game/scripts/gameplay_parity_contracts.py`
- Create: `/Users/dan/Desktop/ac-java-game/tests/__init__.py`
- Create: `/Users/dan/Desktop/ac-java-game/tests/fixtures/gameplay-parity-contracts.json`
- Create: `/Users/dan/Desktop/ac-java-game/tests/test_gameplay_parity_contracts.py`
- Read: `/Users/dan/Desktop/ac-java-game/scripts/decode-gameloft-level-records.py`
- Read: `/Users/dan/Desktop/ac-java-game/reconstructed-project/resources/levels-decoded/`

## Implementation Steps

1. Write fixture/schema and behavior tests first.
2. Add typed record/entity/coordinate data structures and Java arithmetic helpers.
3. Implement constructor route, raw asset selector, remap, and player FSM route.
4. Implement `k.q(int)`-equivalent lookup with player precedence, first live slot,
   high-water boundary, and unconditional `-1 -> null`.
5. Implement normal `i.I()` coordinate reconcile/integrate order.
6. Re-decode packs 6–13 in memory; compare pack results, summary, manifest,
   exact EOF, totals, and remap histogram.
7. Run focused and discovery `unittest` commands.

## Success Criteria

- [x] Corpus fixtures cover raw `0`, raw `25`, `11/93 -> 47`, and `17/120 -> 50`.
- [x] Synthetic fixtures cover absent shipped cases: `11/80`, duplicate ID,
      helper ID `-1`, player precedence, null holes, and coordinate snap/reconcile.
- [x] Fixture basis is one of `corpus`, `synthetic-derived-from-corpus`, or
      `synthetic-source-contract`; corpus gaps remain explicit.
- [x] All 4,286 records re-decode and compare in memory with no output writes.
- [x] `python3 -B -m unittest discover -s tests -p 'test_*.py' -v` passes.

## Risk Assessment

A reference model can accidentally be mistaken for recovered source. Mitigation:
module/docstrings name it a clean-room executable specification; tests retain
obfuscated symbols/evidence and distinguish corpus from synthetic contracts.

## Security Considerations

Fixture paths must resolve inside the repository, sizes/counts are bounded by
existing decoder caps, and no fixture can request JAR/class execution.
