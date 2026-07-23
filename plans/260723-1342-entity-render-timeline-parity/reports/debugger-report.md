# Debugger Report

Scope: static semantic audit of Parity Slice 2 against legacy bytecode for `k.b(i)`, `k.c(i)`, `k.d(i)`, `k.s(int)`, `i.I()`, `i.aa()`, `i.ab()`.

Read: `README.md`, `scripts/gameplay_parity_contracts.py`, `tests/test_gameplay_parity_contracts.py`, `tests/fixtures/gameplay-parity-contracts.json`, `reconstructed-project/bytecode/k.javap.txt`, `reconstructed-project/bytecode/i.javap.txt`, `phase-02-model-entity-store-and-render-ordering.md`, `phase-03-model-slow-time-and-timeline-scheduling.md`.

## Executive Summary

- Issue: final re-audit of the `i.aa()` scheduler slice after two more bytecode-sensitive fixes.
- Impact: no remaining proven mismatch in the audited Slice 2 surface.
- Root cause: all known drifts are resolved; entity-store removal uses reference identity, event dispatch classification now follows signed `baload` semantics, and the extended negative-return boundary preserves incremented `cK` while leaving the current `cL` cursor parked.
- Status: PASS for the audited slice.
- Verification: `python3 -B -m unittest tests.test_gameplay_parity_contracts` passed, 30/30.

## Hypotheses

1. `k.c(i)` parity drift exists in host identity/removal semantics.
2. `k.d(i)` tie ordering is reversed in the host insert comparator.
3. `i.I()/i.aa()/i.ab()/k.s(int)` drift on slow-time gating, due-event selection, or opcode dispatch boundary.

## Timeline

- Static source contract review: phase docs require identity-safe remove, newest-first exact render ties, pre-increment old-tick evaluation, and short-circuit slow gate.
- Bytecode inspection: checked `k.b(i)`/`k.c(i)`/`k.d(i)`/`k.s(int)` in `reconstructed-project/bytecode/k.javap.txt` and `i.I()`/`i.aa()`/`i.ab()` in `reconstructed-project/bytecode/i.javap.txt`.
- Fix re-audit: checked the updated entity-store identity model, the revised `i.aa()` scheduler boundary, the signed-opcode classifier, the `108`/`113` negative-return handling, and the new regression coverage in `scripts/gameplay_parity_contracts.py` and `tests/test_gameplay_parity_contracts.py`.
- Corpus sanity check: scanned decoded script opcodes; observed `max_opcode=114`, `count_ge100=480`, `count_gt127=0`.
- Fresh verification: ran unittest suite; all 30 tests passed.

## Findings

### 1. Fixed: host remove path now matches legacy identity semantics

Evidence chain:

- Legacy `k.c(i)` compares the argument object to each occupied slot with `if_acmpne`, not by UID and not by field equality.
  Source: `reconstructed-project/bytecode/k.javap.txt`, method `public static void c(i);`, offsets `35..41`.
- Legacy tombstone write also uses the argument object's own `as` field before cleanup.
  Source: same method, offsets `44..62`.
- Host `StoredEntityRef` is now mutable and `add_entity()` preserves the same object reference after resetting `snapshot_index`, instead of fabricating a replacement dataclass instance.
  Source: `scripts/gameplay_parity_contracts.py:276-288`, `374-432`.
- Host `EntityStoreState` now stores active/focus entity references, not token strings.
  Source: `scripts/gameplay_parity_contracts.py:291-360`.
- Host `remove_entity()` now:
  - clears globals only on `is` identity matches
  - finds the occupied slot with `slots[index] is entity`
  Source: `scripts/gameplay_parity_contracts.py:459-479`.
- New regression coverage explicitly proves that same token + same UID is still "not found" when the reference is different.
  Source: `tests/test_gameplay_parity_contracts.py:499-527`.

Verdict:

- Fixed.
- The previously reported token-collision aliasing defect is eliminated in the current code.

### 2. Eliminated: render insertion order matches legacy exact-tie behavior

Evidence chain:

- Legacy `k.d(i)` advances past existing entries while `existing.az < new.az`.
  Source: `reconstructed-project/bytecode/k.javap.txt`, method `private static void d(i);`, offsets `0..27`.
- For equal `az`, it advances only while `new.al > existing.al`.
  Source: same method, offsets `30..70`.
- Therefore exact `(az, al)` ties do not advance; the new item is inserted before existing ties.
- Host `insert_render_interaction()` uses the same two-stage comparator:
  - `current.depth < entity.depth`
  - then `entity.world_y > current.world_y`
  Source: `scripts/gameplay_parity_contracts.py:566-582`.

Verdict:

- No defect here.
- Host duplicate-preservation and newest-first exact ties are bytecode-backed, not accidental.

### 3. Fixed and re-verified: slow integration and scheduler boundary now match the proven slice

Evidence chain:

- `i.I()` has two integration paths:
  - normal path adds velocity and acceleration directly
  - slow path divides velocity and acceleration contributions by `aI` before addition
  Sources:
  - `reconstructed-project/bytecode/i.javap.txt`, `i.I()`, offsets `471..568` for normal path
  - same method, offsets `307..420` for slow path
- Host `reconcile_and_integrate_coordinates()` mirrors that split exactly.
  Source: `scripts/gameplay_parity_contracts.py:615-680`.
- `i.ab()` is exactly `ca >= 0 && cd[0] != 1 && cK >= 0`.
  Source: `reconstructed-project/bytecode/i.javap.txt`, `ab()`, offsets `0..31`.
- Host `is_timeline_script_active()` mirrors that predicate.
  Source: `scripts/gameplay_parity_contracts.py:699-711`.
- `k.s(int)` is a first-match linear scan over signed `short[] eH`.
  Source: `reconstructed-project/bytecode/k.javap.txt`, method `public static int s(int);`, offsets `0..28`.
- Host `find_script_group_index()` matches first-hit semantics and signed-short candidate decoding.
  Source: `scripts/gameplay_parity_contracts.py:683-696`.
- `i.aa()` evaluates the old tick first, then increments `cK` only when latched or when the slow gate passes, then processes at most one event record per lane for that call.
  Sources:
  - old tick capture: `i.aa()` offsets `83..87`
  - gate + conditional increment: offsets `89..122`
  - per-lane event parse/dispatch happens before the due test: offsets `146..158`, `307..367`
  - due-gated follow-up/advance happens later from the already parsed event data: offsets `2204..2238`
  - loop continuation after one event: offsets `2936..2939`
- Host `step_timeline_script()` now matches those scheduler-boundary rules:
  - always emits `dispatch-event` for the current lane event
  - advances the cursor only when `event.tick <= evaluated_tick`
  Source: `scripts/gameplay_parity_contracts.py:844-965`.
- The opcode split in legacy bytecode compares the sign-extended `baload` result directly against `100` before any masking.
  Source: `reconstructed-project/bytecode/i.javap.txt`, `i.aa()`, offsets `357..369`.
- Host `classify_timeline_opcode()` now matches that by using `java_i8(opcode) < 100`, so opcodes `128..255` classify the same way legacy signed bytes do.
  Source: `scripts/gameplay_parity_contracts.py:788-793`.
- For extended opcodes, legacy `i.aa()` calls the helper `a:(I[BIII)I`; if that helper returns negative, the method returns immediately.
  Source: `reconstructed-project/bytecode/i.javap.txt`, `i.aa()`, offsets `372..390`.
- That return happens after the optional `cK` increment at offsets `114..122`, but before any later `cL[ lane ]` write at offsets `2212..2231`.
- Host `step_timeline_script()` now models that exact boundary:
  - exact-tick opcodes `108` and `113` require an explicit supplied extended result
  - a negative supplied result returns with `execution_aborted=True`
  - `current_tick` stays at the already incremented value
  - `lane_event_indexes` stay unchanged for the current lane
  Source: `scripts/gameplay_parity_contracts.py:803-951`.
- Fixture coverage now exercises the previously wrong case where a future-timestamp event still dispatches but keeps its cursor parked.
  Source: `tests/test_gameplay_parity_contracts.py:729-750`.
- Regression coverage also now exercises the exact-tick negative extended return for opcode `108`, proving the abort happens after `cK` advance and before `cL` advance.
  Source: `tests/test_gameplay_parity_contracts.py:898-927`.

Extra check:

- I scanned all decoded corpus opcodes. Highest observed opcode is `114`; none exceed `127`.
- That eliminates the only plausible signed-byte dispatch ambiguity for current corpus-backed fixtures.

Verdict:

- Fixed and now bytecode-aligned.
- Remaining omissions are deliberate scope limits, not silent mismatches:
  - normalized lanes are passed in, so full `k.by[ca]` storage lookup is outside this function
  - gameplay side effects after dispatch remain intentionally out of scope

## Test Evidence

Command:

```bash
python3 -B -m unittest tests.test_gameplay_parity_contracts
```

Result:

- 30 tests ran
- 30 passed
- 0 failed

## Recommendations

### Immediate

- No immediate parity fix required for the audited slice.
- Keep the new identity-collision regression; it closes the exact gap that was previously untested.

### Short-term

- If future work serializes/deserializes `StoredEntityRef`, preserve object-identity semantics across any helper layer that reconstructs store state for tests or fixtures.

### Long-term

- If future slices need alias-safe identity, use an opaque unique object-id allocator instead of caller-supplied strings.
- If future fixtures introduce synthetic opcodes above `127`, re-audit `i.aa()` dispatch signedness before extending the `<100` / `>=100` classification beyond current corpus.

## Unresolved Questions

- None for the current slice.

Status: DONE
Summary: All known Slice 2 defects are now closed: entity-store removal uses reference identity, `step_timeline_script()` dispatches/parses the current event before due-gating only cursor advancement, opcode classification follows signed `baload` semantics, and negative extended returns preserve incremented `cK` while leaving the current `cL` cursor unchanged. Fresh verification passed 30/30 tests.
Concerns/Blockers: none.
