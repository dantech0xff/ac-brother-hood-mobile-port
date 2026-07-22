# Assassin's Creed Brotherhood Java ME — Static Reconstruction

This directory is the recovery package produced from the supplied Java ME JAR.
The MIDlet was **never launched**: no `java -jar`, emulator, simulator, device, or
dynamic class loading was used. The archive was handled only as ZIP/class data.

## What is preserved

| Path | Purpose |
|---|---|
| `src/structured/` | Readable JADX reconstruction for all 12 original classes. |
| `src/simple/` | Linear, goto-based JADX source for all 12 classes; includes the full `i.aV()` body. |
| `src/fallback/` | Instruction-oriented JADX fallback for all 12 classes. |
| `bytecode/` | Static `javap -c -p -s -constants -l -verbose` view of every class; only the absolute-path header is normalized. |
| `inventory/` | Machine-readable methods, fields, instructions, calls, field accesses, dependencies, constants, and evidence-scored semantic aliases. |
| `resources/archive/` | Every non-directory file extracted from the original JAR. |
| `resources/decoded/` | Split packs, LZMA payloads, strings, audio, IGP PNGs, and metadata recovered by the extractor. |
| `resources/sprites-decoded/` | Static section metadata plus 12,102 module/palette PNG variants; 83 assets full, one explicitly partial, and five non-output modules accounted. |
| `resources/levels-decoded/` | Static slot-0/slot-7 decoder output; 10 files, 34,570,387 bytes, tree SHA `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`, manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`. |
| `reconstruction-manifest.json` | Hashes, archive inventory, class/method/field counts, source coverage, and resource totals. |
| `final-verification.md` | Historical static integrity audit snapshot for the delivered package. |

The four code views are intentional. Obfuscation removed original symbol names,
and a Java decompiler must reconstruct source-level control flow that is not
stored in bytecode. The structured source is the primary reading view; simple
source, fallback source, and `javap` are the recovery authorities when that view
is ambiguous.

## Measured coverage

- Original SHA-256: `711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383`
- 37 JAR entries inventoried, including 12 classes.
- 666 methods and 1,009 fields represented in bytecode.
- 114,642 JVM instructions occupying 237,112 `Code`-attribute bytes.
- 7,154 bytecode call sites and 25,550 field-access sites indexed for tracing.
- Structured source: 38,887 lines; simple source: 44,988 lines; fallback source:
  128,038 lines.
- One structured-view failure: `i.aV()`. Its complete 1,382-line simple body,
  fallback representation, and exact bytecode remain available, so the method is
  fully present in the recovery rather than silently omitted.
- 17 resource packs with 260 indexed entries: 238 non-empty and 22 empty.
- 24 LZMA entries and 214 raw entries decoded.
- 265 strings, 84 sprite binaries, 13 MIDI files, 18 WAV files, and 29 embedded
  IGP PNG files recovered.
- All 238 non-empty payloads have a semantic family. The 114 payloads that remain
  generic at signature level split into 106 direct/high-confidence families and
  eight primary-layer 2-bit planes proven runtime-unused whose transform meaning
  remains inferred.
- The static sprite decoder consumes 84/84 binaries to EOF, inventories 4,376
  modules, and reconstructs 4,371 pixel modules as 12,102 validated PNG variants;
  five non-output modules are individually accounted for. The `0x27f1` pair is a
  data-derived static recovery; pack 3 entry 6 remains the lone load-valid partial.
- Level slots 0 and 7 parse to EOF across all eight packs: 4,286 entity records,
  144 script groups, 510 lanes, 2,366 events, and 3,705 instructions. The
  managed level tree has 10 files / 34,570,387 bytes / tree SHA
  `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60` /
  manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`.
  Slot 3 is structurally a 2-bit primary companion plane but has no runtime consumer.

This is a forensic reconstruction, not a claim that the original pre-obfuscation
source tree or author-chosen identifiers can be recovered byte-for-byte. It is
also not yet a modern buildable game: Java ME APIs and the original runtime model
must be replaced according to the separate mobile technical design.

## Rebuild the package statically

From the workspace root:

```bash
python3 scripts/extract-java-me-resource-packs.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar/resources-decoded

python3 scripts/build-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  plans/research/assassins-creed-jar \
  reconstructed-project

python3 scripts/inventory-java-me-bytecode.py \
  reconstructed-project/bytecode \
  reconstructed-project/inventory

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded

python3 scripts/decode-gameloft-sprites.py \
  reconstructed-project/resources/decoded \
  reconstructed-project/resources/sprites-decoded \
  --verify-rerun

python3 scripts/verify-static-reconstruction.py \
  assassins_creed_-_br_320x240_136711.jar \
  reconstructed-project \
  --report reconstructed-project/verification-report.json
```

The artifact-specific commands fail closed unless the JAR matches the pinned
SHA-256, then read it only as ZIP/class data. Extraction is staged and publishes
an exact output tree, so stale files from an earlier run are not merged.
`build-static-reconstruction.py` invokes the JDK disassembler `javap`; it never
invokes `java` or a MIDlet launcher. The sprite decoder accepts only previously
decoded payloads and metadata.

## Reading order

1. Start with `src/structured/GloftASBR.java`, then `j.java`, `k.java`, `g.java`,
   and `i.java`.
2. Consult the symbol map, resource-format, and level-record documentation in
   `../docs/`.
3. For any suspicious decompiler output, compare the same method in
   `src/simple/`, `src/fallback/`, and `bytecode/` before assigning semantics.
4. Use the completeness report to distinguish proven recovery, strong inference,
   and remaining unknown fields or binary schemas.

## Safety boundary

Do not run the original JAR. Reproduction and future porting should use only the
static artifacts in this directory and the documented conversion pipeline.
