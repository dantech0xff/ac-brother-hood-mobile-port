# Level decoder implementation

## Result

- Added `scripts/decode-gameloft-level-records.py`.
- Generated `reconstructed-project/resources/levels-decoded/` as 8 numeric pack
  directories plus `summary.json` and `manifest.json`.
- Input contract restricted to extractor metadata and decoded slot-0/slot-7
  payloads. No archive, class, MIDlet, emulator, or target code used.
- Raw bytes, signed/unsigned values, payload offsets, record ranges, operand
  widths, and conservative semantic annotations retained in every pack JSON.
- Shared reconstruction lock, output lock, validated managed ownership, staged
  generation, backup/restore publication, exact-tree cleanup, and strict stale
  lock refusal implemented.

## Reconciled corpus

| Measure | Result |
|---|---:|
| Payloads exact EOF | 16 / 16 |
| Slot-0 bytes / entities | 103,336 / 4,286 |
| Slot-7 bytes | 27,214 |
| Groups / lanes | 144 / 510 |
| Events / instructions | 2,366 / 3,705 |
| `group_meta == sum(lane event_count)` | 144 / 144 |

Managed tree: 10 files, 34,570,387 bytes,
SHA-256 `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`.
`manifest.json` SHA-256:
`75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`.

## Validation

- `python3 scripts/decode-gameloft-level-records.py reconstructed-project/resources/decoded reconstructed-project/resources/levels-decoded` — exit 0.
- `python3 scripts/decode-gameloft-level-records.py reconstructed-project/resources/decoded reconstructed-project/resources/levels-decoded --verify-rerun` — exit 0;
  full managed tree byte-identical.
- Independent `jq` checks — nested entity/group/lane/event/instruction totals and
  all group metadata invariants passed.
- Negative fixture: truncated copied pack-6 slot-7 payload by one byte. Decoder
  exited 2 on recorded size/SHA-256 mismatch before staging or publication.
- Lock directories absent after success and failure paths.

## Notes

- Requested convention source `scripts/reconstruct-jar-static.py` is absent in
  this workspace. Publication conventions matched the current
  `build-static-reconstruction.py`, extractor, and sprite decoder instead.
- No scripts/** changed; documentation and report follow-up was handled separately.
- Prior verification state is invalidated before regeneration, and stale
  backup/stage/lock siblings are rejected instead of auto-recovered.

## Unresolved questions

None.
