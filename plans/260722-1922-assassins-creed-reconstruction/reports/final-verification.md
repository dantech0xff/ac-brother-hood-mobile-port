# Final static verification

> Superseded snapshot. This report captures the pre-residual verifier state and
> is retained as historical evidence only. The root exact verifier/link-count
> review is still pending.

Date: 2026-07-22  
Artifact SHA-256: `711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383`  
Mode: static-only; target JAR/MIDlet/class execution: **never performed**

## Outcome

All three assignment deliverables are present. The delivered package and a new
clean output both pass the independent verifier with `ok: true` and
`failures: []`.

| Assignment item | Evidence | Result |
|---|---|---|
| Full static reconstruction | 12/12 classes in four views; 666/666 methods; 37/37 JAR entries; 260/260 pack slots; decoded resources and provenance | Pass |
| Reverse-engineering technical analysis | `docs/reverse-engineering-technical-analysis.md` plus format/save/level/symbol references and reproducible commands | Pass |
| Modern Android/iOS design only | `docs/modern-mobile-technical-design.md` and framework ADR; no implementation or game execution | Pass |

“Full” means all recoverable static artifacts and behavior evidence are
accounted for. It does not claim recovery of names/comments/build files removed
by obfuscation or absent from the JAR.

## Verification performed

1. `python3 -m py_compile` passed for all five Python tools.
2. Resource extraction from the pinned JAR completed with 17 packs,
   `skipped=[]`, `decode_errors=[]`, and 0 unclassified non-empty payloads.
3. Builder regenerated all managed source/bytecode/archive/decoded trees.
4. Inventory regenerated 666 methods, 1,009 fields, 114,642 instructions,
   7,154 calls, and 25,550 field accesses from declared bytecode input only.
5. Sprite decoder consumed 84/84 inputs to EOF. A second run with
   `--verify-rerun` reproduced the exact 12,182-file managed manifest:
   `12c3bf807de28d8bd651268643689d4bd9e1aea94569d4578ac4f0e735265a8e`.
6. Delivered-package verifier passed. It independently:
   - gates on the pinned JAR hash before ZIP parsing or tool invocation;
   - compares 12/12 delivered disassemblies with fresh normalized `javap`;
   - pins aggregate source and bytecode manifests;
   - regenerates inventory and compares the exact output tree;
   - re-extracts resources from the JAR and compares the exact decoded tree;
   - validates the 12,098 PNG/84 metadata sprite artifact set;
   - reconciles manifest summaries with derived class/resource evidence.
7. The complete builder → inventory → sprite first run → sprite deterministic
   rerun → verifier sequence also passed in a newly created temporary output.
8. Markdown check: 26 files, 87 local links, 0 missing.
9. Generated root/file modes were normalized to portable `0755`/`0644`.
10. Wrong-hash inputs were rejected before ZIP/class processing by extractor,
    builder, and verifier.
11. Builder, inventory, sprite generation, and verification share a
    reconstruction lock; extractor and builder share an analysis-resource lock.
    Mutating steps invalidate the prior report before publishing, preventing a
    concurrent/stale verifier from restoring `ok:true` for a changed tree.
12. Builder stages all six owned trees plus the manifest before manifest-last
    publication with ordinary-failure rollback. Verifier rejects symlinks at
    intermediate `src`/`resources` parents and inside every protected tree.

Canonical machine result:
[`reconstructed-project/verification-report.json`](../../../reconstructed-project/verification-report.json).

## Coverage highlights

- Code: 237,112 `Code` bytes; one structured stub (`i.aV`) is complete in
  simple/fallback/exact bytecode.
- Resources: 238 non-empty + 22 empty slots; 24 LZMA + 214 raw; 265 strings;
  13 MIDI; 18 WAV; 29 IGP PNGs.
- Sprites: 4,376 modules; 4,369 decoded pixel modules; 12,098 PNG variants;
  82 assets full, 2 partial, 0 errors; all seven non-output modules accounted.
- Levels: slot 0 has 4,286 entity records; slot 7 has 144 groups, 510 lanes,
  2,366 events, and 3,705 instructions; all eight payload pairs parse to EOF.
- Slot 3: exact 2-bit primary companion-plane sizing across eight packs and no
  runtime consumer in the recovered 12 classes.
- Save: RMS `/ASBR` record 1 is 512 bytes; 114 bytes have known/observed meaning
  and 398 bytes across exact reserved ranges are preserved.

## Residual unknowns

- Original author names, comments, locals, module/build project, and external JAD
  are absent or removed by obfuscation.
- Three sprite modules have no PNG output: one lacks an optional tail and two use
  pixel code `0x27f1`, for which recovered bytecode has no fill branch.
- Some entity subtype authoring names, ignored script metadata, mode 3, and
  recognized-but-unobserved opcodes 41–44 remain opaque despite exact framing.
- Slot-3 transform meaning and exporter/legacy provenance remain unknown; its
  runtime-unused status is proven.
- Runtime parity was intentionally not measured because launching the JAR is
  prohibited.

These are knowledge limits, not missing bytes: raw payloads, hashes, offsets,
source views, and exact bytecode remain available for further static research.

## Unresolved questions

None blocks the three requested deliverables. A genuine external JAD or original
editor/source artifact would be new evidence and should be inventoried separately.
