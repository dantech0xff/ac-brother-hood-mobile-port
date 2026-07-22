# Sprite Decoder Implementation

## Outcome

- Added static `0x27f1` literal/7-bit-run recovery.
- Kept seven prior pixel branches classified runtime-proven.
- Classified `0x27f1` high-confidence, empirical/data-derived, static-only, and
  explicitly not a runtime bytecode branch.
- Preserved pack 3 entry 6 partial: structurally complete/load-valid and
  runtime-nonrendering because optional pixel tail absent; no pixels synthesized.
- The empirical recovery is gated by exact source/payload/dimension pins; if the
  pinned evidence does not match, raw bytes stay preserved and no image is emitted.

## Recovered Modules

| Module | Dimensions | Pixels | Payload | Index SHA-256 |
|---:|---:|---:|---:|---|
| 0 | 83x33 | 2,739 | 1,557/1,557 bytes | `82689ff1bf8c118d797df7a70850240843882b025b2397bfe3deb94f179bc5cb` |
| 1 | 83x23 | 1,909 | 1,038/1,038 bytes | `dcb0e51e2fb55b6baabfef8c7dbe405f17eb36868822ade8773af4903cc291ea` |

Both recoveries have exact dimensions, zero trailing bytes, positive runs, and
in-range palette indexes.

## Generated Inventory

- Decoded pixel modules: 4,371; undecoded: 5.
- PNG variants: 12,102; four added for `0x27f1`.
- Assets: 83 full, 1 partial, 0 error.
- Managed files: 12,186; 63,940,532 bytes.
- Manifest SHA-256:
  `bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9`.
- Two-run verification: passed; before/after manifests identical.

## Validation

- `PYTHONPYCACHEPREFIX=/private/tmp/ac-sprite-pycache python3 -m py_compile scripts/decode-gameloft-sprites.py`
- Focused direct checks: two corpus payloads, literal/run success, zero run,
  truncation, overshoot, palette bounds, trailing bytes, undershoot, entry 6
  classification.
- Two full decoder runs against an isolated copy of static decoded inputs;
  second used `--verify-rerun`.
- Revalidated four recovered PNGs for CRC, zlib stream, dimensions, and
  filter-0 scanlines after publication.
- Never executed or loaded JAR, MIDlet, class, emulator, simulator, or device.

## Unresolved Questions

None.
