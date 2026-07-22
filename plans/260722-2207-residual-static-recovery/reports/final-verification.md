# Residual static recovery: final verification

Date: 2026-07-22  
Status: passed

## Safety boundary

- Static analysis only.
- JAR, MIDlet, classes, emulator, simulator, and target code were never run.
- Canonical machine report: [verification-report.json](../../../reconstructed-project/verification-report.json)
- Report result: `ok=true`, `failures=[]`, `analysis_mode=static-only`,
  `game_execution_performed=false`.
- Report SHA-256: `7d6ead308d021552f9b7c6a6d1f83e216c147be901e241b966de47634dcc90e4`.

## Recovered outputs

| Area | Final result |
|---|---|
| Bytecode inventory | 12 classes, 666 methods, 1,009 fields, 114,642 instructions |
| Semantic overlay | 12 classes, 31 methods, 41 fields; normalized SHA-256 `a9a5210ae2630784677341246df2d942e00423e717e2119ab092d06b5f7f4268` |
| Sprite tree | 12,186 managed files, 63,940,532 bytes, SHA-256 `bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9` |
| Sprite pixels | 4,371 decoded modules, 12,102 PNGs, 83 full / 1 partial / 0 errors |
| Level tree | 10 files, 34,570,387 bytes, SHA-256 `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60` |
| Level records | 16/16 payloads at exact EOF; 4,286 entities, 144 groups, 510 lanes, 2,366 events, 3,705 instructions |

Canonical summaries:

- [Sprite summary](../../../reconstructed-project/resources/sprites-decoded/summary.json)
- [Level summary](../../../reconstructed-project/resources/levels-decoded/summary.json)
- [Reverse-engineering analysis](../../../docs/reverse-engineering-technical-analysis.md)
- [Modern-mobile technical design](../../../docs/modern-mobile-technical-design.md)

## Verification performed

- Python syntax parsing passed for all six static reconstruction scripts.
- Inventory regenerated from the stored static bytecode disassembly.
- Sprite and level `--verify-rerun` checks passed byte-identically.
- Full verifier reproduced `javap` output, resource extraction, sprite output,
  and level output from pinned static inputs.
- All 175 JSON files in scripts, reconstruction output, and this plan parsed.
- No symlink or stale lock/stage/backup remained in the managed tree.
- Documentation validators reported 65 valid internal links in `docs/`, 16 in
  the root README, 4 in the residual plan, and 6 in its reports.
- Independent code review found no remaining P0, P1, P2, or P3 finding.

Negative tests also proved rejection of oversized metadata before an unbounded
read, bounded metadata enumeration, external pack-directory symlinks, stale
inventory stages, lock contention, and stale-success verification reports.

## Remaining evidence limits

- Pixel code `0x27f1` is a high-confidence corpus-derived recovery, not a
  recovered runtime decoder branch.
- Pack 3 entry 6 has no optional palette/pixel tail; no pixels were invented.
- Script mode 3 and opcodes 41–44 are structurally absent at parsed opcode
  positions, so their commercial names and intended live semantics remain lost.
- Primary-layer slot 3 is proven runtime-unused in this build; its original
  producer-side meaning remains inferred.

These are irreducible limits of the supplied binary evidence, not incomplete
decoder coverage. See [Phase 4](../phase-04-integration-and-audit.md).
