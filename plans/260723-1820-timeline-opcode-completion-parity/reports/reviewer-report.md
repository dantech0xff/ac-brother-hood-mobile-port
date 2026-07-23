# Slice 3 Bytecode Parity Review

## Scope

- Review type: static-only, bytecode-first production-readiness review.
- Contract reviewed: `scripts/timeline_opcode_contracts.py`.
- Supporting evidence reviewed: focused tests, pinned fixture, plan requirements,
  structured source, and semantic aliases.
- Target JAR, MIDlet, class loading, emulator, simulator, and game execution:
  not used.
- Review edits: none outside this report.

## Bytecode Anchors

- `reconstructed-project/bytecode/i.javap.txt:64541` —
  `i.bI:()V`, including identity checks and return PCs 131, 270, 288, 339,
  and 344.
- `reconstructed-project/bytecode/i.javap.txt:66643` —
  `i.a:(I[BIII)I`, including the PC 0-27 width/common guard and opcode
  tableswitch.
- Opcode 105 PCs 939-989 — helper call followed by a fresh `cd[1]` read.
- Opcode 106 PCs 1121-1269 — conditional initialization and sticky slots 7/9.
- Opcode 108 PCs 1536-1938 — future poll, equality cleanup, and branch return.
- Opcode 113 PCs 2739-3267 — null-aware keypad polling, cleanup, branch order,
  and `cc` clearing.
- Opcode 114 PCs 3317-3332 — Java `iadd`, nullable text write, and duration.
- `reconstructed-project/src/structured/j.java:1161` — localization lookup can
  return null.

## Defects Found and Fixed

1. Common non-exact guard decoded operands too early. It now resolves only
   opcode width/ticks and returns with `operands=None` before raw payload
   normalization or access.
2. One attachment field represented distinct legacy fields `bM` and `af`.
   State now uses separate linked-`bM` and animation-`af` references.
3. Opcode 105 treated the post-media `cd[1]` latch as optional. The fresh
   post-call observation is now mandatory whenever the media call path runs.
4. Intention descriptors/arguments were inaccurate for `a.a:(Lb;)V`,
   static `i.O:()V`, static `i.ai:()Z`, and zero-argument `a.<init>:()V`.
   They now match JVM descriptors and receiver conventions.
5. Prompt widget existence/null behavior was over-constrained. Opcodes 108 and
   113 now preserve bytecode-safe keypad/null paths, require observations only
   when dereference or per-slot effects demand them, and retain the repeated
   current-index touch-cancel quirk.
6. Opcode 112 required widget/keypad observations when all options were
   invalid. Empty-option setup now performs only the direct state transition.
7. Opcode 114 could not distinguish a missing localization observation from an
   observed null result. A presence-bearing observation now supports null text.
8. Opcode 114 used unbounded Python addition for `1 + level_index`. It now
   applies signed JVM 32-bit wrapping.
9. Opcode 106 sticky-slot behavior lacked a dedicated regression. Existing
   slots 7 and 9 are now explicitly verified as preserved when their write
   predicates are false.

## Final Assessment

Fresh static re-review found no remaining critical, high, medium, or low
parity defect. Verified areas include:

- `i.bI()` identity semantics, direct-write order, attachment cleanup, repeat
  return, chained-script return, reset, retention, flag-clear, and removal.
- Opcode 100 outer/inner switch behavior without decompiler fallthrough.
- Exact/future/past tick partitions and unreachable opcode 109/110 cleanup.
- Opcode 108/113 cleanup before branching, duplicated `k.s` lookups, `-1`
  abort returns, and state clearing.
- Operand widths, signedness, descriptors, helper observation requirements,
  immutable state transitions, and external-effect intention ordering.
- Concurrency/shared mutation: host contract is immutable; legacy global
  mutations remain explicit ordered intentions.
- Trust boundary: raw operands and typed context values are validated; no
  secrets, PII, authorization surface, database queries, or network boundary
  exists in this static contract.

## Validation Evidence

- Controller-reported focused suite: 27/27 passed.
- Controller-reported discovery suite: 57/57 passed.
- Tests include the regressions listed above and the exhaustive pinned corpus
  oracle.
- This reviewer did not execute the target game or target classes.

Status: DONE
Summary: Static bytecode review is clean after all identified parity defects were fixed; no remaining findings.
Concerns/Blockers: None.
