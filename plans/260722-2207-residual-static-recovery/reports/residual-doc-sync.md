# Residual documentation sync

## Result

- Updated the workspace and package docs to reflect the residual static recovery.
- Marked phases 1-3 as completed and left phase 4 pending.
- Added the current level managed-output tree and the final sprite managed
  manifest values to the docs that cite counts or hashes.
- Corrected the semantic aliases for `j.m(I)I`, `j.n(I)V`, and
  `e.a:(IZ)V`.
- Left the historical final-verification report superseded rather than treating
  it as current state.

## Current canonical values

| Artifact | Value |
|---|---:|
| Sprite managed manifest | 12,186 files, 63,940,532 bytes, SHA-256 `bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9` |
| Level managed tree | 10 files, 34,570,387 bytes, tree SHA `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`, manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648` |
| Sprite decode output | 83 full / 1 partial / 0 error; 4,371 pixel modules; 12,102 PNG variants |

## Notes

- `0x27f1` is now documented as a data-derived static recovery with no runtime
  fill branch, not as an unsupported mystery branch.
- Phase 4 remains pending until the root exact verifier and link-count review
  completes.

