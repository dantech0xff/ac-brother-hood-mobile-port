# Entity Render Timeline Parity

**Date**: 2026-07-23 14:40 +08
**Severity**: High
**Component**: Static gameplay parity harness
**Status**: Resolved

## What Happened

Parity Slice 2 closed out the entity-store, render-order, and timeline-scheduler contracts without ever executing the MIDlet. The slice locked the current static boundary at a 12/42/41 class/method/field alias snapshot, 30 fixtures, and 30 passing tests. It also made the harness boundary explicit: the legacy entity capacity stays 1000, while the test harness uses reduced capacities of 2/4 for entity stores and 3/10 for render lists to force edge cases deterministically.

## The Brutal Truth

The annoying part was that two of the important contracts were wrong in ways that looked harmless until bytecode forced the issue. We had to stop pretending token/UID equality was good enough for entity removal, and we had to stop pretending dispatch could wait until the due cursor said so. Both mistakes would have produced a clean-looking harness with the wrong behavior under the hood.

## Technical Details

- Entity removal now uses object identity, not token or UID equality.
- Future lane events dispatch before due-cursor advancement; only the cursor move is gated by `event_tick <= evaluated_tick`.
- Signed `baload` behavior is preserved: raw opcodes `128..255` classify as inline-low because the comparison is done on `java_i8(opcode) < 100`.
- Exact-tick opcodes `108` and `113` require an explicit extended result; a negative result returns immediately as an abort, after the tick increment boundary but before the lane cursor advances.
- Full opcode effects and `bI()` completion effects/mutations are still intentionally deferred.

## What We Tried

First pass used the cheaper model: compare stable labels, then gate dispatch on due time. That was easier to reason about and wrong. We replaced it with the bytecode-backed sequence, kept the harness static-only, and verified the whole set with `python3 -B -m unittest tests.test_gameplay_parity_contracts`.

## Root Cause Analysis

The root mistake was overfitting the harness to tidy abstractions instead of the JVM behavior we actually recovered. Entity identity, dispatch timing, and signed-byte classification are not implementation details here; they are the contract. Hiding them behind friendlier logic made the model lie.

## Lessons Learned

- Never collapse reference identity into string identity when the bytecode compares references.
- Never let a due cursor suppress dispatch if the recovered scheduler clearly dispatches first.
- Never normalize signed bytes into unsigned values before checking opcode ranges.
- Keep the static-only boundary explicit; this slice is proof, not runtime execution.

## Next Steps

Keep the current contracts pinned and treat `bI()`/opcode side effects as the next deferred slice, not as missing cleanup in this one. Ownership stays with the static harness workstream, and the reduced fixture capacities should remain the default pressure test until a new corpus-backed boundary replaces them.
