#!/usr/bin/env python3
"""Extract the indexed resource packs used by this Java ME game.

The game stores most assets in numeric files. Each pack starts with a small
little-endian index. Entries whose marker is >= 127 contain an LZMA-alone
stream; lower markers contain raw bytes. Split packs use `<name>.1`,
`<name>.2`, and so on for later index ranges.
"""

from __future__ import annotations

import argparse
import atexit
import binascii
import hashlib
import json
import lzma
import os
import re
import shutil
import struct
import sys
import tempfile
import uuid
import zipfile
from collections import Counter
from pathlib import Path


EXPECTED_JAR_SHA256 = (
    "711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383"
)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def acquire_directory_lock(root: Path, name: str) -> Path:
    lock = root / name
    try:
        lock.mkdir()
    except FileExistsError as error:
        raise SystemExit(f"Another static resource operation holds {lock}") from error

    def release() -> None:
        try:
            lock.rmdir()
        except OSError:
            pass

    atexit.register(release)
    return lock


def invalidate_verification_report(reconstruction_root: Path) -> None:
    """Fail closed before any canonical decoded-resource mutation attempt."""

    report = reconstruction_root / "verification-report.json"
    if report.exists() or report.is_symlink():
        if report.is_dir() and not report.is_symlink():
            raise SystemExit(f"Verification report path is a directory: {report}")
        report.unlink()


def read_u16(data: bytes, offset: int) -> int:
    return struct.unpack_from("<H", data, offset)[0]


def read_u32(data: bytes, offset: int) -> int:
    return struct.unpack_from("<I", data, offset)[0]


def detect_type(data: bytes) -> tuple[str, str]:
    if data.startswith(b"\xdf\x05") and len(data) >= 8:
        return ".bin", "Gameloft sprite binary"
    if parse_string_table(data) is not None:
        return ".bin", "Gameloft UTF-8 string table"
    signatures = (
        (b"\x89PNG\r\n\x1a\n", ".png", "PNG image"),
        (b"\xff\xd8\xff", ".jpg", "JPEG image"),
        (b"GIF87a", ".gif", "GIF image"),
        (b"GIF89a", ".gif", "GIF image"),
        (b"RIFF", ".wav", "RIFF/WAVE audio"),
        (b"MThd", ".mid", "MIDI audio"),
        (b"#!AMR", ".amr", "AMR audio"),
        (b"PK\x03\x04", ".zip", "ZIP archive"),
        (b"\xca\xfe\xba\xbe", ".class", "Java class"),
    )
    for signature, extension, description in signatures:
        if data.startswith(signature):
            return extension, description
    return ".bin", "game-specific binary"


def parse_string_table(data: bytes) -> list[str] | None:
    if len(data) < 8:
        return None
    count = read_u32(data, 0)
    if not count or count > 10_000:
        return None
    header_size = 4 + (count * 4)
    if header_size > len(data):
        return None
    ends = [read_u32(data, 4 + (index * 4)) for index in range(count)]
    payload_size = len(data) - header_size
    if ends != sorted(ends) or ends[-1] != payload_size:
        return None
    payload = data[header_size:]
    starts = [0, *ends[:-1]]
    try:
        return [payload[start:end].decode("utf-8") for start, end in zip(starts, ends)]
    except UnicodeDecodeError:
        return None


LEVEL_SLOT_TYPES = {
    0: "level object/entity descriptor stream",
    1: "primary tile layer",
    2: "primary layer dimensions",
    3: "primary-layer packed 2-bit flag/transform plane (runtime-unused)",
    4: "secondary tile layer",
    5: "secondary layer dimensions",
    6: "secondary-layer packed 2-bit flags",
    7: "level block/script descriptor stream",
    8: "tertiary tile layer",
    9: "tertiary layer dimensions",
    10: "tertiary-layer packed 2-bit flags",
    11: "quaternary/lower tile layer",
    12: "quaternary/lower layer dimensions",
    13: "quaternary/lower-layer packed 2-bit flags",
}


def parse_font_glyph_map(data: bytes) -> dict[str, object] | None:
    if len(data) < 3 or data[0] != 0x19:
        return None
    value_count = read_u16(data, 1)
    if len(data) != 3 + (value_count * 2):
        return None
    values = [read_u16(data, 3 + (index * 2)) for index in range(value_count)]
    if not values:
        return None
    bucket_count = values[0]
    cursor = 1
    if cursor + (bucket_count * 2) > len(values):
        return None
    base_buckets = []
    for bucket_index in range(bucket_count):
        codepoint, glyph_id = values[cursor : cursor + 2]
        cursor += 2
        base_buckets.append(
            {
                "bucket": bucket_index,
                "codepoint": codepoint,
                "glyph_id": glyph_id,
            }
        )
    overflow_groups = []
    while cursor < len(values):
        if cursor + 2 > len(values):
            return None
        bucket_index, pair_count = values[cursor : cursor + 2]
        cursor += 2
        if cursor + (pair_count * 2) > len(values):
            return None
        mappings = []
        for _ in range(pair_count):
            codepoint, glyph_id = values[cursor : cursor + 2]
            cursor += 2
            mappings.append({"codepoint": codepoint, "glyph_id": glyph_id})
        overflow_groups.append(
            {
                "bucket": bucket_index,
                "mappings": mappings,
            }
        )

    ordered_mappings = [
        {"codepoint": item["codepoint"], "glyph_id": item["glyph_id"]}
        for item in base_buckets
        if item["codepoint"] != 0
    ]
    ordered_mappings.extend(
        mapping for group in overflow_groups for mapping in group["mappings"]
    )
    codepoint_counts = Counter(item["codepoint"] for item in ordered_mappings)
    return {
        "format": "Gameloft ordered Unicode-to-glyph hash table",
        "typed_header": data[0],
        "value_count": value_count,
        "bucket_count": bucket_count,
        "base_buckets": base_buckets,
        "overflow_groups": overflow_groups,
        "mapping_count": len(ordered_mappings),
        "unique_codepoints": len(codepoint_counts),
        "duplicate_codepoints": sorted(
            codepoint for codepoint, count in codepoint_counts.items() if count > 1
        ),
        "lookup_rule": "ordered first-match; base bucket is codepoint % bucket_count",
    }


def parse_module_remap(data: bytes) -> dict[str, object] | None:
    if not data or len(data) % 4:
        return None
    mappings = [
        {
            "source_module": read_u16(data, offset),
            "replacement_module": read_u16(data, offset + 2),
        }
        for offset in range(0, len(data), 4)
    ]
    return {
        "format": "sprite module substitution table",
        "record_count": len(mappings),
        "record_schema": "u16LE source_module, u16LE replacement_module",
        "mappings": mappings,
    }


def parse_fixed_math_table(data: bytes, kind: str) -> dict[str, object] | None:
    if kind == "cosine" and len(data) >= 2 and data[0] == 0x12:
        count = data[1]
        cursor = 2
    elif kind == "sqrt" and len(data) >= 3 and data[0] == 0x1A:
        count = read_u16(data, 1)
        cursor = 3
    else:
        return None
    if len(data) != cursor + (count * 2):
        return None
    values = [
        struct.unpack_from("<h", data, cursor + (index * 2))[0]
        for index in range(count)
    ]
    if kind == "cosine":
        import math

        expected = [int(256 * math.cos(math.pi * index / 128)) for index in range(count)]
        formula = "trunc(256*cos(pi*i/128)), i=0..64"
    else:
        import math

        expected = [math.isqrt(256 * index) for index in range(count)]
        formula = "floor(16*sqrt(i)), i=0..255"
    return {
        "format": f"fixed-point {kind} lookup table",
        "typed_header": data[0],
        "value_count": count,
        "value_encoding": "signed i16LE stored in typed int[]",
        "formula": formula,
        "formula_verified": values == expected,
        "values": values,
    }


def semantic_classification(
    pack_name: str, entry_index: int, data: bytes, detected_type: str
) -> tuple[str, str, str | None, dict[str, object] | None]:
    if detected_type != "game-specific binary":
        return detected_type, "proven", None, None

    pack_id = int(pack_name)
    if 6 <= pack_id <= 13 and entry_index in LEVEL_SLOT_TYPES:
        label = LEVEL_SLOT_TYPES[entry_index]
        confidence = "inferred" if entry_index == 3 else "high"
        if entry_index in {2, 5, 9, 12} and len(data) == 4:
            return (
                label,
                confidence,
                "dimensions.json",
                {
                    "format": "tile layer dimensions",
                    "width": read_u16(data, 0),
                    "height": read_u16(data, 2),
                    "encoding": "u16LE width, u16LE height",
                },
            )
        return label, confidence, None, None

    if pack_id == 1 and entry_index == 2:
        parsed = parse_font_glyph_map(data)
        return "font Unicode-to-glyph hash table", "high", "font-glyph-map.json", parsed
    if pack_id in {4, 5}:
        parsed = parse_module_remap(data)
        return "sprite module substitution table", "proven", "module-remap.json", parsed
    if pack_id == 16 and entry_index in {0, 1}:
        kind = "cosine" if entry_index == 0 else "sqrt"
        parsed = parse_fixed_math_table(data, kind)
        return f"fixed-point {kind} lookup table", "proven", f"{kind}-table.json", parsed
    return "unclassified game-specific binary", "unknown", None, None


def extract_auxiliary_resources(archive: zipfile.ZipFile, output_root: Path) -> dict[str, object]:
    result: dict[str, object] = {}

    mime_data = archive.read("0")
    mime_count = mime_data[0] if mime_data else 0
    mime_offset = 1
    mime_types: list[str] = []
    for _ in range(mime_count):
        length = mime_data[mime_offset]
        mime_offset += 1
        mime_types.append(mime_data[mime_offset : mime_offset + length].decode("ascii"))
        mime_offset += length
    (output_root / "mime-types.json").write_text(
        json.dumps(mime_types, indent=2) + "\n", encoding="utf-8"
    )
    result["mime_types"] = mime_types

    catalog_data = archive.read("999")
    catalog_count = read_u32(catalog_data, 0)
    catalog_offset = 4
    catalog: list[dict[str, object]] = []
    for _ in range(catalog_count):
        name_length = read_u32(catalog_data, catalog_offset)
        catalog_offset += 4
        name = catalog_data[catalog_offset : catalog_offset + name_length].decode("ascii")
        catalog_offset += name_length
        declared_size = read_u32(catalog_data, catalog_offset)
        catalog_offset += 4
        actual_size = len(archive.read(name))
        catalog.append(
            {
                "file": name,
                "declared_size": declared_size,
                "actual_size": actual_size,
                "size_matches": declared_size == actual_size,
            }
        )
    (output_root / "archive-catalog.json").write_text(
        json.dumps(catalog, indent=2) + "\n", encoding="utf-8"
    )
    result["catalog_entries"] = len(catalog)
    result["catalog_sizes_match"] = all(item["size_matches"] for item in catalog)

    igp_data = archive.read("dataIGP")
    printable_runs = [match.decode("ascii") for match in re.findall(rb"[ -~]{4,}", igp_data)]
    (output_root / "data-igp-strings.txt").write_text(
        "\n".join(printable_runs) + "\n", encoding="utf-8"
    )

    png_output = output_root / "data-igp-images"
    png_output.mkdir(parents=True, exist_ok=True)
    png_signature = b"\x89PNG\r\n\x1a\n"
    pngs: list[dict[str, object]] = []
    search_offset = 0
    while True:
        start = igp_data.find(png_signature, search_offset)
        if start < 0:
            break
        cursor = start + len(png_signature)
        valid = True
        width = None
        height = None
        while cursor + 12 <= len(igp_data):
            chunk_length = struct.unpack_from(">I", igp_data, cursor)[0]
            chunk_type = igp_data[cursor + 4 : cursor + 8]
            chunk_end = cursor + 12 + chunk_length
            if chunk_end > len(igp_data):
                valid = False
                break
            chunk_data = igp_data[cursor + 8 : cursor + 8 + chunk_length]
            stored_crc = struct.unpack_from(">I", igp_data, cursor + 8 + chunk_length)[0]
            calculated_crc = binascii.crc32(chunk_type + chunk_data) & 0xFFFFFFFF
            if stored_crc != calculated_crc:
                valid = False
                break
            if chunk_type == b"IHDR" and chunk_length >= 8:
                width, height = struct.unpack_from(">II", chunk_data, 0)
            cursor = chunk_end
            if chunk_type == b"IEND":
                break
        if valid and cursor > start and igp_data[cursor - 8 : cursor - 4] == b"IEND":
            filename = f"image-{len(pngs):03d}-{width}x{height}.png"
            png_bytes = igp_data[start:cursor]
            (png_output / filename).write_bytes(png_bytes)
            pngs.append(
                {
                    "file": filename,
                    "offset": start,
                    "size": len(png_bytes),
                    "width": width,
                    "height": height,
                    "sha256": hashlib.sha256(png_bytes).hexdigest(),
                }
            )
            search_offset = cursor
        else:
            search_offset = start + len(png_signature)
    (png_output / "metadata.json").write_text(
        json.dumps(pngs, indent=2) + "\n", encoding="utf-8"
    )
    result["data_igp_printable_runs"] = len(printable_runs)
    result["data_igp_pngs"] = len(pngs)
    return result


def parse_pack(
    archive: zipfile.ZipFile, pack_name: str, output_root: Path
) -> dict[str, object]:
    base = archive.read(pack_name)
    if len(base) < 6:
        raise ValueError("header is too short")

    total_entries = read_u16(base, 0)
    part_count = read_u16(base, 2)
    header_end = 4 + (part_count * 2)
    if not total_entries or not part_count or header_end > len(base):
        raise ValueError("invalid entry or part count")

    starts = [read_u16(base, 4 + (index * 2)) for index in range(part_count)]
    if starts[0] != 0 or starts != sorted(starts) or starts[-1] >= total_entries:
        raise ValueError("invalid split-pack entry ranges")

    part_names = [pack_name if index == 0 else f"{pack_name}.{index}" for index in range(part_count)]
    archive_names = set(archive.namelist())
    missing = [name for name in part_names if name not in archive_names]
    if missing:
        raise ValueError(f"missing split-pack files: {', '.join(missing)}")

    pack_output = output_root / f"pack-{pack_name}"
    pack_output.mkdir(parents=True, exist_ok=True)
    entries: list[dict[str, object]] = []
    string_tables: dict[str, list[str]] = {}

    for part_index, part_name in enumerate(part_names):
        part_data = base if part_index == 0 else archive.read(part_name)
        first_entry = starts[part_index]
        last_entry = starts[part_index + 1] if part_index + 1 < part_count else total_entries
        entries_in_part = last_entry - first_entry
        table_offset = header_end if part_index == 0 else 0
        table_end = table_offset + ((entries_in_part + 1) * 4)
        if table_end > len(part_data):
            raise ValueError(f"offset table is truncated in {part_name}")

        offsets = [read_u32(part_data, table_offset + (index * 4)) for index in range(entries_in_part + 1)]
        if offsets != sorted(offsets) or offsets[0] < table_end or offsets[-1] > len(part_data):
            raise ValueError(f"invalid entry offsets in {part_name}")

        for local_index in range(entries_in_part):
            entry_index = first_entry + local_index
            start = offsets[local_index]
            end = offsets[local_index + 1]
            chunk = part_data[start:end]
            metadata: dict[str, object] = {
                "index": entry_index,
                "part": part_name,
                "offset": start,
                "packed_size": len(chunk),
            }
            if not chunk:
                metadata["status"] = "empty entry"
                entries.append(metadata)
                continue

            marker = chunk[0]
            payload = chunk[1:]
            metadata["marker"] = marker
            metadata["marker_value"] = marker - 127 if marker >= 127 else marker
            metadata["compression"] = "lzma-alone" if marker >= 127 else "none"

            try:
                decoded = lzma.decompress(payload, format=lzma.FORMAT_ALONE) if marker >= 127 else payload
            except lzma.LZMAError as error:
                decoded = payload
                metadata["error"] = f"LZMA decode failed: {error}"

            extension, detected_type = detect_type(decoded)
            filename = f"entry-{entry_index:03d}-marker-{marker:03d}{extension}"
            (pack_output / filename).write_bytes(decoded)
            metadata.update(
                {
                    "file": filename,
                    "decoded_size": len(decoded),
                    "sha256": hashlib.sha256(decoded).hexdigest(),
                    "detected_type": detected_type,
                }
            )
            semantic_type, semantic_confidence, semantic_suffix, semantic_data = (
                semantic_classification(pack_name, entry_index, decoded, detected_type)
            )
            if semantic_suffix is not None and semantic_data is None:
                semantic_confidence = "unknown"
                metadata["semantic_parse_error"] = (
                    f"payload did not match expected {semantic_type} schema"
                )
            metadata["semantic_type"] = semantic_type
            metadata["semantic_confidence"] = semantic_confidence
            if semantic_suffix is not None and semantic_data is not None:
                semantic_filename = f"entry-{entry_index:03d}-{semantic_suffix}"
                (pack_output / semantic_filename).write_text(
                    json.dumps(semantic_data, indent=2, ensure_ascii=False) + "\n",
                    encoding="utf-8",
                )
                metadata["semantic_file"] = semantic_filename
            strings = parse_string_table(decoded)
            if strings is not None:
                strings_filename = f"entry-{entry_index:03d}-strings.json"
                (pack_output / strings_filename).write_text(
                    json.dumps(strings, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
                )
                string_tables[f"entry-{entry_index:03d}"] = strings
                metadata["string_count"] = len(strings)
                metadata["strings_file"] = strings_filename
            if detected_type == "Gameloft sprite binary":
                metadata["sprite_version"] = read_u16(decoded, 0)
                metadata["sprite_flags"] = read_u32(decoded, 2)
                metadata["sprite_module_count"] = read_u16(decoded, 6)
            entries.append(metadata)

    if string_tables:
        (pack_output / "all-strings.json").write_text(
            json.dumps(string_tables, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )

    result: dict[str, object] = {
        "pack": pack_name,
        "total_entries": total_entries,
        "part_count": part_count,
        "part_starts": starts,
        "entries": entries,
    }
    (pack_output / "metadata.json").write_text(
        json.dumps(result, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    return result


def publish_directory(stage: Path, output: Path) -> str | None:
    """Publish an exact tree and return any non-fatal post-commit cleanup warning."""

    backup = output.with_name(f".{output.name}.backup-{uuid.uuid4().hex}")
    prior_moved = False
    try:
        if output.exists():
            output.rename(backup)
            prior_moved = True
        stage.rename(output)
    except OSError:
        if prior_moved and not output.exists() and backup.exists():
            backup.rename(output)
        raise
    if prior_moved:
        try:
            shutil.rmtree(backup)
        except OSError as error:
            return f"could not remove committed-output backup {backup}: {error}"
    return None


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path, help="input Java ME JAR")
    parser.add_argument("output", type=Path, help="directory for decoded entries")
    args = parser.parse_args()

    jar = args.jar.resolve()
    if not jar.is_file():
        raise SystemExit(f"Missing JAR: {jar}")
    jar_digest = sha256_file(jar)
    if jar_digest != EXPECTED_JAR_SHA256:
        raise SystemExit("Refusing a JAR that does not match the pinned assignment SHA-256")

    unresolved_output = Path(os.path.abspath(args.output))
    if unresolved_output.is_symlink():
        raise SystemExit(f"Refusing symlinked extractor output path: {unresolved_output}")
    output = unresolved_output.resolve()
    unsafe_outputs = {Path(output.anchor), Path.cwd().resolve(), Path.home().resolve()}
    if output in unsafe_outputs:
        raise SystemExit(f"Refusing unsafe extractor output path: {output}")
    if output == jar or output in jar.parents:
        raise SystemExit("Extractor output must not replace the JAR or an ancestor")
    output.parent.mkdir(parents=True, exist_ok=True)
    if output.name == "decoded" and output.parent.name == "resources":
        candidate_root = output.parent.parent
        if candidate_root.name == "reconstructed-project":
            invalidate_verification_report(candidate_root)
            acquire_directory_lock(candidate_root, ".static-reconstruction.lock")
    acquire_directory_lock(output.parent, ".static-resource-analysis.lock")
    if output.is_symlink() or (output.exists() and not output.is_dir()):
        raise SystemExit(f"Extractor output must be a regular directory: {output}")
    stage = Path(
        tempfile.mkdtemp(prefix=f".{output.name}.stage-", dir=output.parent)
    )
    report: dict[str, object] = {
        "jar": jar.name,
        "packs": [],
        "skipped": [],
        "decode_errors": [],
    }
    detected_types: Counter[str] = Counter()
    semantic_types: Counter[str] = Counter()
    semantic_confidence: Counter[str] = Counter()

    published = False
    try:
        with zipfile.ZipFile(jar) as archive:
            report["auxiliary"] = extract_auxiliary_resources(archive, stage)
            names = set(archive.namelist())
            candidates = sorted(
                (
                    name
                    for name in names
                    if name.isdigit() and name not in {"0", "999"}
                ),
                key=int,
            )
            for pack_name in candidates:
                try:
                    result = parse_pack(archive, pack_name, stage)
                    report["packs"].append(
                        {
                            "pack": pack_name,
                            "entries": result["total_entries"],
                            "parts": result["part_count"],
                        }
                    )
                    for entry in result["entries"]:
                        if entry.get("status") == "empty entry":
                            continue
                        if "error" in entry:
                            report["decode_errors"].append(
                                {
                                    "pack": pack_name,
                                    "entry": entry["index"],
                                    "error": entry["error"],
                                }
                            )
                        detected_types[str(entry["detected_type"])] += 1
                        semantic_types[str(entry["semantic_type"])] += 1
                        semantic_confidence[str(entry["semantic_confidence"])] += 1
                except (KeyError, ValueError, struct.error) as error:
                    report["skipped"].append(
                        {"file": pack_name, "reason": str(error)}
                    )

        report["classification"] = {
            "detected_types": dict(sorted(detected_types.items())),
            "semantic_types": dict(sorted(semantic_types.items())),
            "semantic_confidence": dict(sorted(semantic_confidence.items())),
            "unclassified_nonempty_entries": sum(
                count
                for semantic_type, count in semantic_types.items()
                if semantic_type.startswith("unclassified ")
            ),
        }

        (stage / "summary.json").write_text(
            json.dumps(report, indent=2, ensure_ascii=False) + "\n",
            encoding="utf-8",
        )
        print(json.dumps(report, indent=2, ensure_ascii=False))
        if report["skipped"] or report["decode_errors"]:
            return 1
        stage.chmod(0o755)
        publication_warning = publish_directory(stage, output)
        published = True
        if publication_warning is not None:
            print(f"warning: {publication_warning}", file=sys.stderr)
        return 0
    finally:
        if not published and stage.exists():
            shutil.rmtree(stage)


if __name__ == "__main__":
    raise SystemExit(main())
