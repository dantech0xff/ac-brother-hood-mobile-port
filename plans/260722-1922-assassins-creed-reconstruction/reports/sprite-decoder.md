# Static sprite decoder report

## Result

Implemented `scripts/decode-gameloft-sprites.py` and ran it only against the
extracted `.bin` entries under `reconstructed-project/resources/decoded/`.
The tool does not accept a JAR, does not load a target class, and uses only the
Python standard library.

Output: `reconstructed-project/resources/sprites-decoded/`.

| Metric | Result |
|---|---:|
| Metadata entries detected as `Gameloft sprite binary` | 84 |
| Inputs processed / consumed exactly to EOF | 84 / 84 |
| Source size and SHA-256 matches | 84 / 84 |
| Success / partial / error | 83 / 1 / 0 |
| Modules inventoried | 4,376 |
| Pixel modules decoded | 4,371 |
| Modules not decoded as pixels | 5 |
| PNG palette variants emitted | 12,102 |
| PNG encoded bytes | 2,980,042 |
| PNG CRC and zlib validation | passed for all 12,102 |

The per-asset `metadata.json` retains pack, entry, part, pack offset, marker,
compression, recorded/actual size, and recorded/actual SHA-256. It also records
every section and record with source byte ranges, raw/runtime signed values,
conditional flag branches, reference observations, palette values, `aB`
payload offsets/hashes, pixel consumption, and generated PNG hashes.

The five modules without pixel PNGs are fully accounted for:

- 4 use parsed non-pixel runtime tags (`253`/`254`), so pixel output is not
  applicable.
- 1 pixel module is in pack 3 entry 6, whose optional palette/`aB` tail is
  absent at EOF.

The `0x27f1` pair in pack 3 entry 58 is a data-derived static recovery: 2,739
and 1,909 pixels across four PNGs. The recovered bytecode has no runtime fill
branch, so the output is corpus-derived rather than runtime-derived.

## Evidence implemented

Primary evidence:

- `b.a(byte[], int)` in structured, simple, fallback, and bytecode views.
- `b.a(byte[], int, int, int)` pixel decoder.
- Actual section boundaries validated by consuming all 84 extracted inputs to
  exact EOF.

Confidence is scoped: loader grammar/record offsets and the seven explicitly
listed pixel branches are proven; unlisted branch values are unsupported and
preserved without output.

Header:

```text
0x00  u16 LE  magic/version = 0x05df
0x02  u32 LE  aD flags
0x06  u16 LE  ab module count
0x08  ...     variable module records
```

Supported module tags exactly match the loader branches `0`, `255` through
`247`. Corpus distribution: tag `0` = 4,372; tag `253` = 2; tag `254` = 2.
No unknown tag was encountered.

Sections retained under their obfuscated runtime field names:

1. modules: `aU/aV/aW/ac/ad/ae/af/ag/ah`
2. `ap/ar/as/at/aq` records
3. optional `am/an` records
4. `ai/aj/ao` plus `ak/al` records
5. `av/aw/ax/ay/i` records
6. `au/h` records
7. palettes, `aL` pixel code, and per-module `aB/aC` payloads

Record totals beyond the 4,376 modules:

- `ap/ar/as/aq`: 41,215
- `am/an`: 2,022
- `ai/aj/ao`: 5,289
- `av/aw/ax/ay/i`: 7,635
- `au/h`: 2,969

Palette branches observed: `0x8888` in 81 assets and `0x5515` in 2 assets.
The tool also implements the loader's `0x6505` branch even though this corpus
does not use it.

Pixel branches translated from `b`:

- `0x64f0`: palette index plus run length
- `0x56f2`: signed-control literal/run stream
- `0x1600`: packed 4-bit indexes
- `0x0400`: packed 2-bit indexes
- `0x0200`: packed 1-bit indexes
- `0x5602`: direct 8-bit indexes
- `0xa640`: `0x64f0` indexes followed by alpha/overlay stream

PNG output is RGBA, non-interlaced, and contains independently checked IHDR,
IDAT, and IEND CRCs. Every decoded pixel module is emitted once per stored
palette; filenames use only pack/entry/module/palette indexes and dimensions.

## Explicit partial assets

| Asset | Offset | Static result |
|---|---:|---|
| pack 3, entry 6 | `0x02d5` (725) | `aD` declares the optional tail, but the file ends at the loader's `offset < length` guard. Palette and `aB` bytes do not exist, so no image is invented. |

The four tag `253`/`254` modules are parsed non-pixel runtime branches and are
reported as not applicable for PNG output, not failures.

## Verification

Executed:

```text
PYTHONPYCACHEPREFIX=/private/tmp/ac-sprite-decoder-pycache-final-3 \
  python3 -m py_compile scripts/decode-gameloft-sprites.py

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded \
  --verify-rerun
```

The two complete runs produced the same managed artifact manifest:

```text
files: 12,186
bytes: 63,940,532
sha256: bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9
rerun verification: passed
```

Reruns use an atomic sibling lock and generate into a fresh sibling staging
tree. The staged tree is schema-validated before a backup/replace transaction;
the prior complete output is restored if publication fails. Discovery rejects
empty inventories, path/symlink escapes, provenance mismatches, duplicate
indexes/sources/destinations, and filenames outside the extractor's exact
`entry-NNN-marker-NNN.bin` schema. Target-buffer writes are capped to the
runtime's 40,960-slot `b.g` buffer, including packed/RLE padding and the
`0xa640` alpha stream.

A read-only preflight parses and provenance-checks every source before the
staging tree is created. Aggregate work stayed within the fixed safety limits:

| Preflight measure | Observed | Limit |
|---|---:|---:|
| Assets | 84 | 4,096 |
| Input bytes | 1,401,582 | 67,108,864 |
| Modules | 4,376 | 100,000 |
| Section records | 63,506 | 250,000 |
| Decoded-pixel work | 1,879,979 | 50,000,000 |
| PNG variants | 12,102 | 100,000 |
| Conservative PNG-byte bound | 20,061,822 | 536,870,912 |

The PNG-byte bound uses the fixed PNG chunks plus zlib `compressBound` for
each RGBA filter-0 scanline stream. This prevents the module/palette Cartesian
product from exhausting disk or inodes before output generation starts.

A deliberate metadata-only `--verify-rerun` against the full baseline exited
nonzero with the expected 84-file-versus-12,186-file manifest mismatch. The
published full summary, manifest, and all 12,102 PNGs remained unchanged, and
no stage, lock, or backup directory remained.

`summary.json` stores the decoded input root workspace-relatively using an
invoking-CWD-independent rule, not as a machine-specific `/Users/...` path. A
full `--verify-rerun` launched from `/private/tmp` with absolute arguments
produced the same manifest and the same decoded-root label.

An independent pass reopened all 84 JSON files and all 12,102 PNGs, matched
listed versus actual files, recalculated every chunk CRC, decompressed every
IDAT stream, checked dimensions/scanline lengths, and asserted the summary
counts. Visual spot checks included an RGBA `0x56f2` module and an `0xa640`
alpha module.

Crafted read-only guard tests also passed for a final color RLE run that would
cross the 40,960-slot buffer, an `0xa640` alpha repeat that would cross it, a
truncated direct-index payload, zlib-bound calculations, and aggregate-limit
rejection.

## Unresolved questions

- The tool parses and validates the stored frame/animation record families but
  emits proven module/palette images, not speculative assembled frame sheets.
  Some otherwise valid assets store `0xff`/`0x3ff` reference-like sentinels;
  their reachability semantics are not established by the loader alone.
- Publication is failure-atomic for ordinary rename errors, not crash-atomic.
  A kill or power loss between the backup and staged renames can leave the live
  path absent with recoverable backup/stage/lock siblings. The next run refuses
  the stale lock; the CLI help and summary document manual recovery instead of
  risking automatic overwrite of an active writer. Cleanup failures are
  warnings and cannot mask the primary exception or a committed output.
