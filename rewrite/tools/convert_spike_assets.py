#!/usr/bin/env python3
"""tools-content spike converter.

Copies the decoded assets chosen for the toolchain spike into
rewrite/generated/ and emits provenance.json with source/output SHA-256 and
transform version for each entry. Deterministic: sorted keys, fixed ordering,
no timestamps. Runtime must read only rewrite/generated/ outputs.
"""
from __future__ import annotations

import hashlib
import json
import shutil
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DECODED = REPO_ROOT / "reconstructed-project" / "resources"
OUT = REPO_ROOT / "rewrite" / "generated"
PROVENANCE_VERSION = 1
TRANSFORM = "copy-v1"

SPIKE_ASSETS = [
    (
        "sprites-decoded/pack-2/entry-000-marker-003/module-0002-palette-00-232x129.png",
        "splash.png",
        "recovered title art, blit target in the 400x240 scene",
    ),
    (
        "sprites-decoded/pack-2/entry-000-marker-003/module-0000-palette-00-83x155.png",
        "actor.png",
        "recovered character art, the moving entity visual",
    ),
    (
        "decoded/pack-17/entry-020-marker-000.wav",
        "sfx.wav",
        "converted SFX played on touch (pack 17 audio slot)",
    ),
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    entries = []
    for src_rel, out_name, role in SPIKE_ASSETS:
        src = DECODED / src_rel
        if not src.is_file():
            print(f"missing source: {src}", file=sys.stderr)
            return 1
        dst = OUT / out_name
        shutil.copyfile(src, dst)
        entries.append(
            {
                "output": out_name,
                "output_sha256": sha256(dst),
                "output_bytes": dst.stat().st_size,
                "role": role,
                "source": f"reconstructed-project/resources/{src_rel}",
                "source_sha256": sha256(src),
                "transform": TRANSFORM,
            }
        )
    manifest = {
        "provenance_version": PROVENANCE_VERSION,
        "entries": entries,
    }
    manifest_path = OUT / "provenance.json"
    manifest_path.write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print(f"wrote {len(entries)} assets + {manifest_path.name} into {OUT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
