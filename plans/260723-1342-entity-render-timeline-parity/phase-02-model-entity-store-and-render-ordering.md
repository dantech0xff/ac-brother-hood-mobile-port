---
phase: 2
title: Model Entity Store and Render Ordering
status: completed
priority: P1
dependencies:
  - 1
---

# Phase 2: Model Entity Store and Render Ordering

## Context Links

- [Plan](./plan.md)
- [Existing parity module](../../scripts/gameplay_parity_contracts.py)
- Evidence: `reconstructed-project/bytecode/k.javap.txt` methods
  `k.b:(Li;)V`, `k.c:(Li;)V`, and `k.d:(Li;)V`.

## Overview

Add pure host-side transitions for the fixed entity store and the per-frame
render/interaction ordered list.

## Requirements

- Functional: preserve high-water, LIFO free slots, first identity removal,
  tombstone-before-cleanup order, tracked-global reset, silent full-store drop,
  duplicates, and render tie reversal.
- Non-functional: default legacy capacity remains 1,000; smaller fixture
  capacity is labeled harness parameterization.

## Architecture

Python object references preserve JVM reference identity; tokens are labels and
UIDs are data only. Cleanup `i.p()` is an ordered trace event rather than
reimplemented gameplay. Render rebuild starts from an empty list each frame and
inserts candidates sequentially.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/gameplay_parity_contracts.py`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/fixtures/gameplay-parity-contracts.json`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/test_gameplay_parity_contracts.py`

## Implementation Steps

1. Add validated entity-store state and mutable entity reference records.
2. Implement add/remove transitions and ordered observable traces.
3. Add render entry, ordered insert, and rebuild helpers.
4. Add synthetic source-contract fixtures for lifecycle, duplicates, full
   capacity, exact ties, and overflow.
5. Test operation ordering and verify old lookup behavior stays unchanged.

## Success Criteria

- [x] Free slots pop in reverse removal order and never reduce high-water.
- [x] Dropped add still resets the entity snapshot index to `-98`.
- [x] Removal compares object identity with `is`, clears tracked globals before scan,
  writes `-99` before cleanup, and removes only the first matching slot.
- [x] Render sort is ascending `(az, al)` and exact ties are newest-first.
- [x] Duplicate render entries survive; insertion at capacity raises the
  host equivalent of legacy array overflow.

## Risk Assessment

Risk: Python equality accidentally replaces JVM identity. Mitigation: keep the
same mutable entity reference through add/drop, compare with `is`, and lock
token/UID collisions plus full-store mutation with regression tests.

## Security Considerations

Inputs are bounded fixture data. No dynamic imports beyond the existing local
test loader and no legacy code execution.
