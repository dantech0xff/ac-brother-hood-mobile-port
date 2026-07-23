# Timeline Opcode Completion Parity

**Date**: 2026-07-23 14:40 +08
**Severity**: High
**Component**: Static gameplay parity harness
**Status**: Resolved

## What Happened

Slice 3 finished the bytecode-first/static-only contract for `i.bI:()V` and `i.a:(I[BIII)I` without running the target MIDlet. The evidence is split across [the plan](../../plans/260723-1820-timeline-opcode-completion-parity/plan.md), [implementation verification](../../plans/260723-1820-timeline-opcode-completion-parity/reports/implementation-verification.md), [tester report](../../plans/260723-1820-timeline-opcode-completion-parity/reports/tester-report.md), [debugger report](../../plans/260723-1820-timeline-opcode-completion-parity/reports/debugger-report.md), and [reviewer report](../../plans/260723-1820-timeline-opcode-completion-parity/reports/reviewer-report.md). The harness now pins 12/44/41 semantic aliases, 15 fixtures, 3,705 decoded instructions, 480 extended opcode occurrences, and the current validation state of 27/27 focused tests plus 57/57 full discovery tests. The broad verifier snapshot stayed `ok=true` with `game_execution_performed=false`.

## The Brutal Truth

The painful part was how easy it would have been to fake this slice with tidy abstractions and still miss the real JVM boundaries. Completion handling, opcode decoding, and abort composition are not “nice to have” details here; they are the contract. If we had blurred those lines, we would have shipped a prettier lie.

## Technical Details

- `i.bI:()V` is modeled as direct state mutation plus ordered downstream intentions, not as runtime cleanup.
- Opcode coverage is locked across `100..114`, including the signed-byte split, exact-width decoding, and prompt-state handling.
- `108`/`113` preserve the exact-tick abort boundary: a negative extended result returns `execution_aborted=true` after the tick-stage boundary and before later cursor advancement.
- Opcode `109` is intentionally synthetic-only because the corpus has zero occurrences.
- `114` carries the null-aware localized text path, and the corpus still contributes the 3,705-instruction / 480-extended oracle.

## What We Tried

The first pass tried to treat completion and dispatcher behavior as a single generic transition. That was too coarse. We split the schema, kept helper effects as ordered intentions, and verified the result against the pinned corpus and the bytecode anchors in the implementation-verification, tester, debugger, and reviewer reports.

## Root Cause Analysis

The root mistake was trusting an abstract “state machine” shape more than the recovered bytecode. That would have hidden the real return boundaries and operand semantics. The fix was to make the host contract narrow, explicit, and boring: direct writes where bytecode writes, intentions where bytecode delegates, and no target execution to blur the evidence.

## Lessons Learned

- Bytecode-first beats model-first when the model can easily lie.
- Completion effects/mutations must stay separate from downstream helper intentions.
- Signed opcode handling must stay signed; raw `0..255` values are not all equivalent.
- Static-only validation is the right boundary here, and it needs to stay visible in the docs.

## Next Steps

Keep Slice 3 pinned as a deferred-boundary record: completion and opcode
contracts are closed, but any recursive downstream runtime emulation remains
out of scope. Further work belongs to explicitly planned future slices, not
target execution.
