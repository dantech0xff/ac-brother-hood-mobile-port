#!/usr/bin/env python3
"""Strictly decode extracted Gameloft level entity and script records.

The decoder accepts only the decoded resource tree produced by the static
extractor.  It never opens the source archive, loads target classes, or invokes
target code.  Packs 6 through 13 and their slot-0/slot-7 entries are pinned to
the proven corpus contract documented in ``docs/level-record-formats.md``.
"""

from __future__ import annotations

import argparse
import atexit
import hashlib
import json
import os
import re
import shutil
import stat
import struct
import sys
import tempfile
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Any


SCHEMA_VERSION = 1
OUTPUT_CONTRACT = (
    "decoded pack metadata plus extracted slot-0/slot-7 .bin entries; "
    "no JAR/class input"
)
DECODER_ID = "decode-gameloft-level-records.py"
PACK_IDS = tuple(range(6, 14))
MAX_METADATA_BYTES = 2 * 1024 * 1024
MAX_METADATA_ENTRIES = 1_024
MAX_PAYLOAD_BYTES = 2 * 1024 * 1024
MAX_ENTITY_RECORDS = 10_000
MAX_ENTITY_FIELDS = 25
MAX_GROUPS = 1_024
MAX_LANES = 10_000
MAX_EVENTS_PER_LANE = 10_000
MAX_EVENTS = 100_000
MAX_INSTRUCTIONS_PER_EVENT = 127
MAX_INSTRUCTIONS = 100_000
SHA256_PATTERN = re.compile(r"[0-9a-f]{64}\Z")
OUTPUT_PACK_PATTERN = re.compile(r"pack-(6|7|8|9|10|11|12|13)\Z")

SLOT_CONFIG = {
    "entities": {
        "index": 0,
        "semantic_type": "level object/entity descriptor stream",
    },
    "scripts": {
        "index": 7,
        "semantic_type": "level block/script descriptor stream",
    },
}

EXPECTED_PACKS: dict[int, dict[str, Any]] = {
    6: {
        "entity_bytes": 15_245,
        "entities": 637,
        "script_bytes": 4_371,
        "groups": 13,
        "lanes": 57,
        "events": 349,
        "instructions": 677,
        "modes": {0: 11, 1: 7, 2: 39},
        "max_tick": 279,
    },
    7: {
        "entity_bytes": 5_279,
        "entities": 225,
        "script_bytes": 735,
        "groups": 5,
        "lanes": 13,
        "events": 67,
        "instructions": 100,
        "modes": {0: 5, 1: 4, 2: 4},
        "max_tick": 165,
    },
    8: {
        "entity_bytes": 14_635,
        "entities": 567,
        "script_bytes": 3_925,
        "groups": 23,
        "lanes": 75,
        "events": 335,
        "instructions": 519,
        "modes": {0: 23, 1: 7, 2: 45},
        "max_tick": 438,
    },
    9: {
        "entity_bytes": 17_685,
        "entities": 691,
        "script_bytes": 8_942,
        "groups": 46,
        "lanes": 194,
        "events": 807,
        "instructions": 1_165,
        "modes": {0: 33, 1: 26, 2: 135},
        "max_tick": 392,
    },
    10: {
        "entity_bytes": 5_970,
        "entities": 254,
        "script_bytes": 413,
        "groups": 5,
        "lanes": 11,
        "events": 35,
        "instructions": 56,
        "modes": {0: 5, 1: 1, 2: 5},
        "max_tick": 28,
    },
    11: {
        "entity_bytes": 17_468,
        "entities": 740,
        "script_bytes": 3_072,
        "groups": 22,
        "lanes": 65,
        "events": 257,
        "instructions": 400,
        "modes": {0: 17, 1: 7, 2: 41},
        "max_tick": 149,
    },
    12: {
        "entity_bytes": 19_281,
        "entities": 849,
        "script_bytes": 2_986,
        "groups": 13,
        "lanes": 47,
        "events": 280,
        "instructions": 426,
        "modes": {0: 13, 1: 7, 2: 27},
        "max_tick": 421,
    },
    13: {
        "entity_bytes": 7_773,
        "entities": 323,
        "script_bytes": 2_770,
        "groups": 17,
        "lanes": 48,
        "events": 236,
        "instructions": 362,
        "modes": {0: 15, 1: 4, 2: 29},
        "max_tick": 225,
    },
}

EXPECTED_ENTITY_FIELD_COUNTS = {
    0: 7,
    2: 8,
    4: 12,
    5: 18,
    6: 8,
    7: 9,
    9: 9,
    10: 21,
    11: 20,
    13: 12,
    14: 13,
    15: 9,
    16: 7,
    17: 15,
    19: 7,
    21: 25,
    22: 12,
    24: 8,
    25: 7,
    27: 11,
    29: 11,
    30: 21,
    32: 11,
    35: 18,
    37: 19,
    40: 11,
    41: 8,
    42: 13,
    43: 11,
    44: 12,
    46: 13,
    51: 9,
    54: 21,
    55: 9,
    56: 20,
    58: 8,
    60: 10,
    61: 7,
    65: 9,
    66: 11,
    67: 9,
    69: 8,
    72: 13,
    73: 20,
    74: 8,
    78: 8,
    79: 9,
    80: 8,
}

EXPECTED_ENTITY_FIELD_HISTOGRAM = {
    7: 43,
    8: 689,
    9: 1_549,
    10: 7,
    11: 185,
    12: 679,
    13: 401,
    15: 16,
    18: 123,
    19: 201,
    20: 225,
    21: 167,
    25: 1,
}

EXPECTED_RAW_TYPE_HISTOGRAM = {
    0: 6,
    2: 50,
    4: 180,
    5: 95,
    6: 7,
    7: 9,
    9: 10,
    10: 124,
    11: 188,
    13: 14,
    14: 339,
    15: 35,
    16: 11,
    17: 16,
    19: 22,
    21: 1,
    22: 43,
    24: 12,
    25: 2,
    27: 41,
    29: 2,
    30: 2,
    32: 5,
    35: 28,
    37: 201,
    40: 15,
    41: 1,
    42: 6,
    43: 8,
    44: 442,
    46: 34,
    51: 15,
    54: 41,
    55: 184,
    56: 27,
    58: 10,
    60: 7,
    61: 2,
    65: 2,
    66: 114,
    67: 1_286,
    69: 3,
    72: 22,
    73: 10,
    74: 602,
    78: 2,
    79: 8,
    80: 2,
}

EXPECTED_OPCODE_HISTOGRAM = {
    11: 218,
    12: 32,
    13: 16,
    21: 1_421,
    22: 1_009,
    23: 166,
    24: 106,
    25: 17,
    37: 177,
    38: 63,
    100: 204,
    101: 1,
    102: 90,
    103: 7,
    104: 5,
    105: 59,
    106: 16,
    107: 30,
    108: 30,
    110: 9,
    111: 25,
    112: 1,
    113: 1,
    114: 2,
}

EXPECTED_TOTALS = {
    "payloads": 16,
    "payloads_exact_eof": 16,
    "entity_bytes": 103_336,
    "entities": 4_286,
    "script_bytes": 27_214,
    "groups": 144,
    "lanes": 510,
    "events": 2_366,
    "instructions": 3_705,
}
EXPECTED_LANE_META_HISTOGRAM = {0: 88, 1: 421, 2: 1}
EXPECTED_RETYPE_HISTOGRAM = {47: 12, 50: 8}


@dataclass(frozen=True)
class OperandPart:
    name: str
    storage: str
    size: int


@dataclass(frozen=True)
class OpcodeLayout:
    label: str
    parts: tuple[OperandPart, ...]

    @property
    def width(self) -> int:
        return sum(part.size for part in self.parts)


def words(prefix: str, storage: str, count: int) -> tuple[OperandPart, ...]:
    return tuple(OperandPart(f"{prefix}_{index}", storage, 2) for index in range(count))


NO_OPERANDS = OpcodeLayout("no operands", ())
OPCODE_LAYOUTS: dict[int, OpcodeLayout] = {}
for opcode in (11, 12, 21, 25, 31, 41):
    OPCODE_LAYOUTS[opcode] = OpcodeLayout("2 * s16le", words("value", "s16le", 2))
OPCODE_LAYOUTS[13] = OpcodeLayout(
    "u8 lane index + opaque[4]",
    (OperandPart("lane_index", "u8", 1), OperandPart("opaque_tail", "opaque", 4)),
)
for opcode in (*range(14, 21), *range(26, 31), 33, 40):
    OPCODE_LAYOUTS[opcode] = NO_OPERANDS
for opcode in (22, 32, 42):
    OPCODE_LAYOUTS[opcode] = OpcodeLayout(
        "u16le", (OperandPart("value", "u16le", 2),)
    )
for opcode in (23, 24, 43, 44):
    OPCODE_LAYOUTS[opcode] = OpcodeLayout(
        "opaque mask[4]", (OperandPart("mask", "opaque", 4),)
    )
for opcode, count in ((34, 1), (35, 2), (36, 3)):
    OPCODE_LAYOUTS[opcode] = OpcodeLayout(
        f"u16le target + {count} * u16le",
        (OperandPart("target", "u16le", 2),) + words("value", "u16le", count),
    )
for opcode, count in ((37, 1), (38, 2), (39, 3)):
    OPCODE_LAYOUTS[opcode] = OpcodeLayout(
        f"{count} * u16le", words("value", "u16le", count)
    )
OPCODE_LAYOUTS.update(
    {
        100: OpcodeLayout(
            "3 * s16le (target/action/value)",
            (
                OperandPart("target", "s16le", 2),
                OperandPart("action", "s16le", 2),
                OperandPart("value", "s16le", 2),
            ),
        ),
        101: OpcodeLayout(
            "s16le script id", (OperandPart("script_id", "s16le", 2),)
        ),
        102: OpcodeLayout("2 * s16le", words("value", "s16le", 2)),
        103: OpcodeLayout(
            "opaque ignored[2]", (OperandPart("opaque", "opaque", 2),)
        ),
        104: OpcodeLayout("s16le", (OperandPart("value", "s16le", 2),)),
        105: OpcodeLayout(
            "u8 / u16le / u8",
            (
                OperandPart("byte_0", "u8", 1),
                OperandPart("word_0", "u16le", 2),
                OperandPart("byte_1", "u8", 1),
            ),
        ),
        106: OpcodeLayout(
            "4 * u16le / u8",
            words("word", "u16le", 4) + (OperandPart("byte", "u8", 1),),
        ),
        107: OpcodeLayout(
            "u16le input mask", (OperandPart("input_mask", "u16le", 2),)
        ),
        108: OpcodeLayout(
            "2 * u16le branch ids",
            (
                OperandPart("branch_id_0", "u16le", 2),
                OperandPart("branch_id_1", "u16le", 2),
            ),
        ),
        109: OpcodeLayout("4 * u16le", words("value", "u16le", 4)),
        110: OpcodeLayout(
            "2 * u16le entity uids",
            (
                OperandPart("entity_uid_0", "u16le", 2),
                OperandPart("entity_uid_1", "u16le", 2),
            ),
        ),
        111: OpcodeLayout(
            "3 * u16le / u8 / u16le",
            words("word", "u16le", 3)
            + (
                OperandPart("byte", "u8", 1),
                OperandPart("word_3", "u16le", 2),
            ),
        ),
        112: OpcodeLayout("3 * u16le", words("value", "u16le", 3)),
        113: OpcodeLayout(
            "2 * u16le branch ids",
            (
                OperandPart("branch_id_0", "u16le", 2),
                OperandPart("branch_id_1", "u16le", 2),
            ),
        ),
        114: OpcodeLayout("2 * u16le", words("value", "u16le", 2)),
    }
)


class LevelDecodeError(ValueError):
    """A strict format or corpus assertion failure at a payload offset."""

    def __init__(self, message: str, offset: int, branch: str) -> None:
        super().__init__(f"{branch} at 0x{offset:08x}: {message}")
        self.offset = offset
        self.branch = branch


class Cursor:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.offset = 0

    def require(self, size: int, branch: str) -> None:
        if size < 0 or self.offset + size > len(self.data):
            raise LevelDecodeError(
                f"truncated read: need {size} byte(s), "
                f"have {len(self.data) - self.offset}",
                self.offset,
                branch,
            )

    def take(self, size: int, branch: str) -> bytes:
        self.require(size, branch)
        start = self.offset
        self.offset += size
        return self.data[start : start + size]

    def u8(self, branch: str) -> tuple[int, int]:
        offset = self.offset
        return self.take(1, branch)[0], offset

    def u16le(self, branch: str) -> tuple[int, int]:
        offset = self.offset
        return struct.unpack("<H", self.take(2, branch))[0], offset

    def assert_eof(self, branch: str) -> None:
        if self.offset != len(self.data):
            raise LevelDecodeError(
                f"trailing data: {len(self.data) - self.offset} byte(s)",
                self.offset,
                branch,
            )


@dataclass
class PayloadAsset:
    pack: int
    slot_name: str
    entry: dict[str, Any]
    source_path: Path
    metadata_relative_path: str
    source_relative_path: str
    data: bytes

    def provenance(self) -> dict[str, Any]:
        digest = hashlib.sha256(self.data).hexdigest()
        return {
            "pack": self.pack,
            "slot": self.entry["index"],
            "slot_name": self.slot_name,
            "part": self.entry.get("part"),
            "container_offset": self.entry["offset"],
            "packed_size": self.entry["packed_size"],
            "marker": self.entry["marker"],
            "marker_value": self.entry.get("marker_value"),
            "compression": self.entry["compression"],
            "semantic_type": self.entry["semantic_type"],
            "semantic_confidence": self.entry.get("semantic_confidence"),
            "metadata_file": self.metadata_relative_path,
            "decoded_file": self.source_relative_path,
            "decoded_size_recorded": self.entry["decoded_size"],
            "decoded_size_actual": len(self.data),
            "sha256_recorded": self.entry["sha256"],
            "sha256_actual": digest,
            "size_matches": self.entry["decoded_size"] == len(self.data),
            "sha256_matches": self.entry["sha256"] == digest,
        }


def java_i8(value: int) -> int:
    return value - 0x100 if value & 0x80 else value


def java_i16(value: int) -> int:
    return value - 0x10000 if value & 0x8000 else value


def offset_hex(offset: int) -> str:
    return f"0x{offset:08x}"


def offset_range(start: int, end: int) -> dict[str, Any]:
    return {
        "start": start,
        "start_hex": offset_hex(start),
        "end_exclusive": end,
        "end_exclusive_hex": offset_hex(end),
        "size": end - start,
    }


def count8_record(raw: int, offset: int) -> dict[str, Any]:
    return {
        "storage": "count8-java-signed",
        "offset": offset,
        "offset_hex": offset_hex(offset),
        "raw_hex": f"{raw:02x}",
        "raw_u8": raw,
        "java_i8": java_i8(raw),
    }


def u8_record(raw: int, offset: int) -> dict[str, Any]:
    return {
        "storage": "u8",
        "offset": offset,
        "offset_hex": offset_hex(offset),
        "raw_hex": f"{raw:02x}",
        "raw_u8": raw,
    }


def short_record(raw: int, offset: int, storage: str = "s16le") -> dict[str, Any]:
    record = {
        "storage": storage,
        "offset": offset,
        "offset_hex": offset_hex(offset),
        "raw_hex": raw.to_bytes(2, "little").hex(),
        "raw_u16": raw,
        "java_i16": java_i16(raw),
    }
    return record


def counter_dict(counter: Counter[int] | dict[int, int]) -> dict[str, int]:
    return {str(key): counter[key] for key in sorted(counter)}


def require_nonnegative_count(
    record: dict[str, Any], maximum: int, branch: str
) -> int:
    value = record["java_i8"] if "java_i8" in record else record["java_i16"]
    if value < 0:
        raise LevelDecodeError(
            f"negative Java count {value}", record["offset"], branch
        )
    if value > maximum:
        raise LevelDecodeError(
            f"count {value} exceeds safety cap {maximum}", record["offset"], branch
        )
    return value


def read_count8(cursor: Cursor, branch: str, maximum: int) -> tuple[int, dict[str, Any]]:
    raw, offset = cursor.u8(branch)
    record = count8_record(raw, offset)
    return require_nonnegative_count(record, maximum, branch), record


def read_s16_count(
    cursor: Cursor, branch: str, maximum: int
) -> tuple[int, dict[str, Any]]:
    raw, offset = cursor.u16le(branch)
    record = short_record(raw, offset)
    return require_nonnegative_count(record, maximum, branch), record


def assert_corpus_equal(
    actual: Any, expected: Any, offset: int, branch: str
) -> None:
    if actual != expected:
        raise LevelDecodeError(
            f"pinned corpus mismatch: expected {expected!r}, got {actual!r}",
            offset,
            branch,
        )


def read_bounded_regular_file(path: Path, maximum: int, label: str) -> bytes:
    """Read at most ``maximum`` bytes without following a final symlink."""

    if path.is_symlink() or not path.is_file():
        raise ValueError(f"{label} must be a regular file: {path}")
    initial_size = path.stat().st_size
    if initial_size > maximum:
        raise ValueError(f"{label} exceeds {maximum} byte cap: {path}")

    flags = os.O_RDONLY | getattr(os, "O_BINARY", 0)
    flags |= getattr(os, "O_NOFOLLOW", 0)
    descriptor = os.open(path, flags)
    try:
        opened = os.fstat(descriptor)
        if not stat.S_ISREG(opened.st_mode):
            raise ValueError(f"{label} must be a regular file: {path}")
        if opened.st_size > maximum:
            raise ValueError(f"{label} exceeds {maximum} byte cap: {path}")

        chunks: list[bytes] = []
        remaining = maximum + 1
        while remaining:
            chunk = os.read(descriptor, min(64 * 1024, remaining))
            if not chunk:
                break
            chunks.append(chunk)
            remaining -= len(chunk)
        data = b"".join(chunks)
        if len(data) > maximum or os.fstat(descriptor).st_size > maximum:
            raise ValueError(f"{label} exceeds {maximum} byte cap: {path}")
        return data
    finally:
        os.close(descriptor)


def read_metadata(path: Path) -> dict[str, Any]:
    raw = read_bounded_regular_file(path, MAX_METADATA_BYTES, "pack metadata")
    try:
        value = json.loads(raw.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as error:
        raise ValueError(f"invalid pack metadata {path}: {error}") from error
    if not isinstance(value, dict):
        raise ValueError(f"pack metadata must be a JSON object: {path}")
    return value


def validate_target_entry(
    decoded_root: Path,
    metadata_path: Path,
    pack: int,
    slot_name: str,
    entry: dict[str, Any],
) -> PayloadAsset:
    config = SLOT_CONFIG[slot_name]
    index = config["index"]
    marker = entry.get("marker")
    expected_filename = f"entry-{index:03d}-marker-003.bin"
    required_values = {
        "index": index,
        "marker": 3,
        "marker_value": 3,
        "compression": "none",
        "file": expected_filename,
        "semantic_type": config["semantic_type"],
    }
    for key, expected in required_values.items():
        if entry.get(key) != expected:
            raise ValueError(
                f"pack {pack} slot {index} {key} must be {expected!r}, "
                f"got {entry.get(key)!r}"
            )
    for key in ("offset", "packed_size", "decoded_size"):
        value = entry.get(key)
        if not isinstance(value, int) or isinstance(value, bool) or value < 0:
            raise ValueError(f"pack {pack} slot {index} has invalid {key}")
    if entry["decoded_size"] > MAX_PAYLOAD_BYTES:
        raise ValueError(
            f"pack {pack} slot {index} exceeds {MAX_PAYLOAD_BYTES} byte cap"
        )
    if entry["packed_size"] != entry["decoded_size"] + 1:
        raise ValueError(
            f"pack {pack} slot {index} packed/decoded size relation is invalid"
        )
    digest = entry.get("sha256")
    if not isinstance(digest, str) or SHA256_PATTERN.fullmatch(digest) is None:
        raise ValueError(f"pack {pack} slot {index} has invalid SHA-256")
    source_path = metadata_path.parent / expected_filename
    if source_path.is_symlink() or not source_path.is_file():
        raise ValueError(f"decoded payload must be a regular file: {source_path}")
    if source_path.resolve().parent != metadata_path.parent.resolve():
        raise ValueError(f"decoded payload escapes its pack directory: {source_path}")
    data = read_bounded_regular_file(
        source_path, MAX_PAYLOAD_BYTES, "decoded payload"
    )
    actual_digest = hashlib.sha256(data).hexdigest()
    if len(data) != entry["decoded_size"] or actual_digest != digest:
        raise ValueError(
            f"decoded payload provenance mismatch for pack {pack} slot {index}: "
            f"size {len(data)}/{entry['decoded_size']}, "
            f"sha256 {actual_digest}/{digest}"
        )
    return PayloadAsset(
        pack=pack,
        slot_name=slot_name,
        entry=entry,
        source_path=source_path,
        metadata_relative_path=metadata_path.relative_to(decoded_root).as_posix(),
        source_relative_path=source_path.relative_to(decoded_root).as_posix(),
        data=data,
    )


def discover_payloads(decoded_root: Path) -> dict[int, dict[str, PayloadAsset]]:
    payloads: dict[int, dict[str, PayloadAsset]] = {}
    for pack in PACK_IDS:
        pack_directory = decoded_root / f"pack-{pack}"
        if pack_directory.is_symlink() or not pack_directory.is_dir():
            raise ValueError(f"required pack directory must be regular: {pack_directory}")
        metadata_path = pack_directory / "metadata.json"
        metadata = read_metadata(metadata_path)
        if str(metadata.get("pack")) != str(pack):
            raise ValueError(
                f"metadata pack {metadata.get('pack')!r} does not match pack-{pack}"
            )
        entries = metadata.get("entries")
        if not isinstance(entries, list) or len(entries) > MAX_METADATA_ENTRIES:
            raise ValueError(f"pack {pack} metadata entries are invalid or exceed cap")
        indexes: set[int] = set()
        indexed: dict[int, dict[str, Any]] = {}
        for entry in entries:
            if not isinstance(entry, dict):
                raise ValueError(f"pack {pack} metadata entry must be an object")
            index = entry.get("index")
            if (
                not isinstance(index, int)
                or isinstance(index, bool)
                or index < 0
                or index in indexes
            ):
                raise ValueError(f"pack {pack} has invalid or duplicate entry index")
            indexes.add(index)
            indexed[index] = entry
        pack_payloads: dict[str, PayloadAsset] = {}
        for slot_name, config in SLOT_CONFIG.items():
            index = config["index"]
            if index not in indexed:
                raise ValueError(f"pack {pack} lacks required slot {index}")
            semantic_matches = [
                entry
                for entry in entries
                if entry.get("semantic_type") == config["semantic_type"]
            ]
            if len(semantic_matches) != 1 or semantic_matches[0] is not indexed[index]:
                raise ValueError(
                    f"pack {pack} semantic type for slot {index} is not uniquely pinned"
                )
            pack_payloads[slot_name] = validate_target_entry(
                decoded_root, metadata_path, pack, slot_name, indexed[index]
            )
        payloads[pack] = pack_payloads
    return payloads


def field_value(fields: list[dict[str, Any]], index: int) -> int:
    return fields[index]["java_i16"]


def entity_annotations(raw_type: int, fields: list[dict[str, Any]]) -> dict[str, Any]:
    if raw_type in (0, 25):
        construction_route = "player constructor"
    elif raw_type == 55:
        construction_route = "waypoint registry"
    else:
        construction_route = "actor entity"

    annotations: dict[str, Any] = {
        "construction_route": {
            "value": construction_route,
            "confidence": "proven",
        }
    }
    if construction_route == "actor entity":
        if raw_type in (67, 46, 7, 56, 9):
            selector_index, registry = {
                67: (7, "bk"),
                46: (10, "bl"),
                7: (8, "bm"),
                56: (7, "bj"),
                9: (8, "bn"),
            }[raw_type]
            annotations["sprite_selector"] = {
                "source": "record field",
                "field_index": selector_index,
                "value": field_value(fields, selector_index),
                "runtime_registry": registry,
                "confidence": "proven",
            }
        else:
            annotations["sprite_selector"] = {
                "source": "raw discriminator",
                "value": raw_type,
                "runtime_registry": "bi",
                "confidence": "proven",
            }
    if raw_type != 55:
        if raw_type == 37:
            annotations["field_5"] = {
                "interpretation": "not passed to actor initial-state setter",
                "value": field_value(fields, 5),
                "confidence": "proven",
            }
        else:
            annotations["field_5"] = {
                "interpretation": (
                    "controller_state"
                    if raw_type == 10
                    else "initial_state_or_animation"
                ),
                "value": field_value(fields, 5),
                "confidence": "high",
                "ambiguity": (
                    "obfuscated setter call is proven; original commercial field name is lost"
                ),
            }
    if raw_type in (10, 14, 37, 42):
        annotations["bounds"] = {
            "model": "position-relative rectangle",
            "offset_x": field_value(fields, 7),
            "offset_y": field_value(fields, 8),
            "width": field_value(fields, 9),
            "height": field_value(fields, 10),
            "field_indexes": [7, 8, 9, 10],
            "confidence": "proven",
        }
    elif raw_type == 5:
        annotations["bounds"] = {
            "model": "rectangle anchored at entity x/y",
            "x": field_value(fields, 2),
            "y": field_value(fields, 3),
            "width": field_value(fields, 12),
            "height": field_value(fields, 13),
            "field_indexes": [2, 3, 12, 13],
            "confidence": "proven",
        }
    return annotations


def decode_entities(data: bytes) -> dict[str, Any]:
    cursor = Cursor(data)
    records: list[dict[str, Any]] = []
    field_histogram: Counter[int] = Counter()
    raw_type_histogram: Counter[int] = Counter()
    runtime_type_histogram: Counter[int] = Counter()
    retype_histogram: Counter[int] = Counter()

    while cursor.offset < len(data):
        if len(records) >= MAX_ENTITY_RECORDS:
            raise LevelDecodeError(
                f"entity record cap {MAX_ENTITY_RECORDS} exceeded",
                cursor.offset,
                "slot0.record_count",
            )
        record_start = cursor.offset
        field_count, count_record = read_count8(
            cursor, "slot0.field_count", MAX_ENTITY_FIELDS
        )
        if field_count == 0:
            raise LevelDecodeError(
                "zero-field entity cannot satisfy runtime dispatch",
                count_record["offset"],
                "slot0.field_count",
            )
        fields: list[dict[str, Any]] = []
        for field_index in range(field_count):
            raw, field_offset = cursor.u16le(f"slot0.field[{field_index}]")
            field = short_record(raw, field_offset)
            field["index"] = field_index
            fields.append(field)

        raw_type = field_value(fields, 0)
        expected_field_count = EXPECTED_ENTITY_FIELD_COUNTS.get(raw_type)
        if expected_field_count is None:
            raise LevelDecodeError(
                f"unsupported raw discriminator {raw_type}",
                fields[0]["offset"],
                "slot0.raw_discriminator",
            )
        assert_corpus_equal(
            field_count,
            expected_field_count,
            count_record["offset"],
            "slot0.discriminator_field_width",
        )

        subtype = field_value(fields, 5)
        runtime_type = raw_type
        retype: dict[str, Any] = {
            "applied": False,
            "runtime_type": runtime_type,
            "confidence": "proven",
        }
        if raw_type == 11 and subtype in (80, 93):
            runtime_type = 47
            retype = {
                "applied": True,
                "runtime_type": runtime_type,
                "rule": "raw 11 and field[5] in {80,93}",
                "confidence": "proven",
            }
        elif raw_type == 17 and subtype == 120:
            runtime_type = 50
            retype = {
                "applied": True,
                "runtime_type": runtime_type,
                "rule": "raw 17 and field[5] == 120",
                "confidence": "proven",
            }

        if raw_type in (0, 25):
            dispatch = "g(short[]) / player constructor"
        elif raw_type == 55:
            dispatch = "c.a(short[]) / waypoint registry"
        else:
            dispatch = "i(short[]) / actor entity"

        record_end = cursor.offset
        record = {
            "index": len(records),
            "offsets": offset_range(record_start, record_end),
            "raw_hex": data[record_start:record_end].hex(),
            "field_count": count_record,
            "fields": fields,
            "interpreted": {
                "raw_discriminator": raw_type,
                "runtime_type": runtime_type,
                "uid": field_value(fields, 1),
                "x": field_value(fields, 2),
                "y": field_value(fields, 3),
                "subtype_or_variant": subtype,
                "flags": field_value(fields, 6),
                "flag_bit_0_av": bool(field_value(fields, 6) & 1),
                "dispatch": dispatch,
                "retype": retype,
                "semantic_annotations": entity_annotations(raw_type, fields),
            },
        }
        records.append(record)
        field_histogram[field_count] += 1
        raw_type_histogram[raw_type] += 1
        runtime_type_histogram[runtime_type] += 1
        if retype["applied"]:
            retype_histogram[runtime_type] += 1

    cursor.assert_eof("slot0.eof")
    return {
        "grammar": "record* EOF; record = count8 + count * s16le",
        "byte_length": len(data),
        "consumed_bytes": cursor.offset,
        "consumed_to_eof": True,
        "record_count": len(records),
        "field_count_histogram": counter_dict(field_histogram),
        "raw_type_histogram": counter_dict(raw_type_histogram),
        "runtime_type_histogram": counter_dict(runtime_type_histogram),
        "retype_histogram": counter_dict(retype_histogram),
        "records": records,
    }


def byte_views(data: bytes, start: int) -> list[dict[str, Any]]:
    return [
        {
            "index": index,
            "offset": start + index,
            "offset_hex": offset_hex(start + index),
            "raw_hex": f"{raw:02x}",
            "raw_u8": raw,
            "java_i8": java_i8(raw),
        }
        for index, raw in enumerate(data)
    ]


def decode_operand_parts(
    raw: bytes, start: int, layout: OpcodeLayout
) -> list[dict[str, Any]]:
    parts: list[dict[str, Any]] = []
    position = 0
    for part in layout.parts:
        part_start = start + position
        value = raw[position : position + part.size]
        if len(value) != part.size:
            raise LevelDecodeError(
                "internal operand-layout truncation", part_start, "slot7.operand_layout"
            )
        record: dict[str, Any] = {
            "name": part.name,
            "storage": part.storage,
            "offsets": offset_range(part_start, part_start + part.size),
            "raw_hex": value.hex(),
        }
        if part.storage == "u8":
            record["raw_u8"] = value[0]
        elif part.storage in ("u16le", "s16le"):
            raw_u16 = int.from_bytes(value, "little")
            record["raw_u16"] = raw_u16
            record["java_i16"] = java_i16(raw_u16)
        elif part.storage != "opaque":
            raise LevelDecodeError(
                f"internal unsupported storage {part.storage}",
                part_start,
                "slot7.operand_layout",
            )
        parts.append(record)
        position += part.size
    if position != len(raw):
        raise LevelDecodeError(
            f"internal layout consumed {position} of {len(raw)} byte(s)",
            start + position,
            "slot7.operand_layout",
        )
    return parts


def decode_instruction(
    cursor: Cursor, instruction_index: int
) -> tuple[dict[str, Any], int]:
    start = cursor.offset
    opcode, opcode_offset = cursor.u8("slot7.instruction.opcode")
    layout = OPCODE_LAYOUTS.get(opcode)
    if layout is None:
        raise LevelDecodeError(
            f"opcode {opcode} has no proven width",
            opcode_offset,
            "slot7.instruction.opcode",
        )
    operand_start = cursor.offset
    raw_operands = cursor.take(layout.width, f"slot7.opcode_{opcode}.operands")
    end = cursor.offset
    return (
        {
            "index": instruction_index,
            "offsets": offset_range(start, end),
            "raw_hex": cursor.data[start:end].hex(),
            "opcode": u8_record(opcode, opcode_offset),
            "operand_width": layout.width,
            "operand_layout": layout.label,
            "operands": {
                "offsets": offset_range(operand_start, end),
                "raw_hex": raw_operands.hex(),
                "bytes": byte_views(raw_operands, operand_start),
                "interpreted_parts": decode_operand_parts(
                    raw_operands, operand_start, layout
                ),
            },
        },
        opcode,
    )


def decode_scripts(data: bytes) -> dict[str, Any]:
    cursor = Cursor(data)
    group_count, group_count_record = read_count8(
        cursor, "slot7.group_count", MAX_GROUPS
    )
    groups: list[dict[str, Any]] = []
    script_ids: set[int] = set()
    mode_histogram: Counter[int] = Counter()
    lane_meta_histogram: Counter[int] = Counter()
    opcode_histogram: Counter[int] = Counter()
    total_lanes = 0
    total_events = 0
    total_instructions = 0
    ticks: list[int] = []

    for group_index in range(group_count):
        group_start = cursor.offset
        raw_script_id, script_id_offset = cursor.u16le("slot7.group.script_id")
        script_id = short_record(raw_script_id, script_id_offset)
        signed_script_id = script_id["java_i16"]
        if signed_script_id in script_ids:
            raise LevelDecodeError(
                f"duplicate script id {signed_script_id}",
                script_id_offset,
                "slot7.group.script_id",
            )
        script_ids.add(signed_script_id)
        lane_count, lane_count_record = read_count8(
            cursor, "slot7.group.lane_count", 127
        )
        group_meta_raw, group_meta_offset = cursor.u16le("slot7.group.group_meta")
        group_meta = short_record(group_meta_raw, group_meta_offset, "opaque[2]/u16le")
        lanes: list[dict[str, Any]] = []
        group_event_count = 0

        if total_lanes + lane_count > MAX_LANES:
            raise LevelDecodeError(
                f"aggregate lane cap {MAX_LANES} exceeded",
                lane_count_record["offset"],
                "slot7.lane_count",
            )

        for lane_index in range(lane_count):
            lane_start = cursor.offset
            mode_raw, mode_offset = cursor.u8("slot7.lane.mode")
            if mode_raw not in (0, 1, 2, 3):
                raise LevelDecodeError(
                    f"unsupported lane mode {mode_raw}",
                    mode_offset,
                    "slot7.lane.mode",
                )
            mode = u8_record(mode_raw, mode_offset)
            lane_meta_raw, lane_meta_offset = cursor.u8("slot7.lane.lane_meta")
            lane_meta = u8_record(lane_meta_raw, lane_meta_offset)
            mode_extra: dict[str, Any] | None = None
            if mode_raw in (2, 3):
                mode_extra_raw, mode_extra_offset = cursor.u16le(
                    "slot7.lane.mode_extra"
                )
                mode_extra = short_record(
                    mode_extra_raw, mode_extra_offset, "opaque[2]/u16le"
                )
                mode_extra["interpretation"] = (
                    "target_uid" if mode_raw == 2 else "opaque_mode_3_extra"
                )
                mode_extra["confidence"] = "proven" if mode_raw == 2 else "unknown"
            event_count, event_count_record = read_s16_count(
                cursor, "slot7.lane.event_count", MAX_EVENTS_PER_LANE
            )
            event_cursor = cursor.offset - lane_start
            expected_event_cursor = 6 if mode_raw in (2, 3) else 4
            assert_corpus_equal(
                event_cursor,
                expected_event_cursor,
                lane_start,
                "slot7.lane.initial_event_cursor",
            )
            if total_events + event_count > MAX_EVENTS:
                raise LevelDecodeError(
                    f"aggregate event cap {MAX_EVENTS} exceeded",
                    event_count_record["offset"],
                    "slot7.event_count",
                )
            events: list[dict[str, Any]] = []

            for event_index in range(event_count):
                event_start = cursor.offset
                tick_raw, tick_offset = cursor.u16le("slot7.event.tick")
                tick = short_record(tick_raw, tick_offset)
                ticks.append(tick["java_i16"])
                opcode_count, opcode_count_record = read_count8(
                    cursor,
                    "slot7.event.opcode_count",
                    MAX_INSTRUCTIONS_PER_EVENT,
                )
                if total_instructions + opcode_count > MAX_INSTRUCTIONS:
                    raise LevelDecodeError(
                        f"aggregate instruction cap {MAX_INSTRUCTIONS} exceeded",
                        opcode_count_record["offset"],
                        "slot7.instruction_count",
                    )
                instructions: list[dict[str, Any]] = []
                for instruction_index in range(opcode_count):
                    instruction, opcode = decode_instruction(
                        cursor, instruction_index
                    )
                    instructions.append(instruction)
                    opcode_histogram[opcode] += 1
                event_end = cursor.offset
                events.append(
                    {
                        "index": event_index,
                        "offsets": offset_range(event_start, event_end),
                        "raw_hex": data[event_start:event_end].hex(),
                        "tick": tick,
                        "opcode_count": opcode_count_record,
                        "instructions": instructions,
                    }
                )
                total_instructions += opcode_count

            lane_end = cursor.offset
            raw_lane = data[lane_start:lane_end]
            trailer = event_cursor.to_bytes(2, "little")
            lanes.append(
                {
                    "index": lane_index,
                    "offsets": offset_range(lane_start, lane_end),
                    "raw_hex": raw_lane.hex(),
                    "mode": mode,
                    "lane_meta": lane_meta,
                    "mode_extra": mode_extra,
                    "event_count": event_count_record,
                    "runtime_encoding": {
                        "raw_length": len(raw_lane),
                        "initial_event_cursor_bz": event_cursor,
                        "initial_event_cursor_bz_hex": f"0x{event_cursor:04x}",
                        "synthetic_trailer_hex": trailer.hex(),
                        "blob_with_synthetic_trailer_hex": (raw_lane + trailer).hex(),
                        "confidence": "proven",
                    },
                    "events": events,
                }
            )
            group_event_count += event_count
            total_lanes += 1
            total_events += event_count
            mode_histogram[mode_raw] += 1
            lane_meta_histogram[lane_meta_raw] += 1

        if group_meta_raw != group_event_count:
            raise LevelDecodeError(
                f"group_meta u16le {group_meta_raw} does not equal lane event "
                f"count sum {group_event_count}",
                group_meta_offset,
                "slot7.group.group_meta_invariant",
            )
        group_end = cursor.offset
        groups.append(
            {
                "index": group_index,
                "offsets": offset_range(group_start, group_end),
                "raw_hex": data[group_start:group_end].hex(),
                "script_id": script_id,
                "lane_count": lane_count_record,
                "group_meta": group_meta,
                "observed_group_meta_invariant": {
                    "group_meta_u16le": group_meta_raw,
                    "lane_event_count_sum": group_event_count,
                    "matches": True,
                    "confidence": "proven corpus invariant",
                    "scope_note": "original obfuscated field name remains unknown",
                },
                "lanes": lanes,
            }
        )

    cursor.assert_eof("slot7.eof")
    return {
        "grammar": (
            "group_count:count8; group(script_id:s16le,lane_count:count8," 
            "group_meta[2],lanes); exact EOF"
        ),
        "byte_length": len(data),
        "consumed_bytes": cursor.offset,
        "consumed_to_eof": True,
        "declared_group_count": group_count_record,
        "group_count": len(groups),
        "lane_count": total_lanes,
        "event_count": total_events,
        "instruction_count": total_instructions,
        "mode_histogram": counter_dict(mode_histogram),
        "lane_meta_histogram": counter_dict(lane_meta_histogram),
        "opcode_histogram": counter_dict(opcode_histogram),
        "max_tick": max(ticks) if ticks else None,
        "script_ids_unique_within_pack": True,
        "group_meta_invariant": {
            "assertion": "group_meta u16le == sum(lane event counts)",
            "groups_checked": len(groups),
            "groups_matching": len(groups),
            "status": "passed",
        },
        "groups": groups,
    }


def validate_pack_result(
    pack: int, entities: dict[str, Any], scripts: dict[str, Any]
) -> None:
    expected = EXPECTED_PACKS[pack]
    checks = {
        "entity_bytes": entities["byte_length"],
        "entities": entities["record_count"],
        "script_bytes": scripts["byte_length"],
        "groups": scripts["group_count"],
        "lanes": scripts["lane_count"],
        "events": scripts["event_count"],
        "instructions": scripts["instruction_count"],
        "max_tick": scripts["max_tick"],
    }
    for name, actual in checks.items():
        assert_corpus_equal(actual, expected[name], 0, f"pack{pack}.{name}")
    actual_modes = {int(key): value for key, value in scripts["mode_histogram"].items()}
    assert_corpus_equal(actual_modes, expected["modes"], 0, f"pack{pack}.modes")


def build_pack_result(pack: int, assets: dict[str, PayloadAsset]) -> dict[str, Any]:
    try:
        entities = decode_entities(assets["entities"].data)
    except LevelDecodeError as error:
        raise ValueError(f"pack-{pack} slot-0 decode failed: {error}") from error
    try:
        scripts = decode_scripts(assets["scripts"].data)
    except LevelDecodeError as error:
        raise ValueError(f"pack-{pack} slot-7 decode failed: {error}") from error
    try:
        validate_pack_result(pack, entities, scripts)
    except LevelDecodeError as error:
        raise ValueError(f"pack-{pack} corpus validation failed: {error}") from error
    return {
        "schema_version": SCHEMA_VERSION,
        "static_only": True,
        "input_contract": OUTPUT_CONTRACT,
        "pack": pack,
        "sources": {
            "slot_0_entities": assets["entities"].provenance(),
            "slot_7_scripts": assets["scripts"].provenance(),
        },
        "entities": entities,
        "scripts": scripts,
        "assertions": {
            "pinned_pack_totals": "passed",
            "both_payloads_exact_eof": "passed",
            "source_size_and_sha256": "passed",
            "group_meta_equals_lane_event_count_sum": "passed",
        },
    }


def merge_histogram(target: Counter[int], value: dict[str, int]) -> None:
    target.update({int(key): count for key, count in value.items()})


def build_summary(pack_results: list[dict[str, Any]]) -> dict[str, Any]:
    totals: Counter[str] = Counter()
    field_histogram: Counter[int] = Counter()
    raw_type_histogram: Counter[int] = Counter()
    runtime_type_histogram: Counter[int] = Counter()
    retype_histogram: Counter[int] = Counter()
    mode_histogram: Counter[int] = Counter()
    lane_meta_histogram: Counter[int] = Counter()
    opcode_histogram: Counter[int] = Counter()
    per_pack: list[dict[str, Any]] = []

    for result in pack_results:
        entities = result["entities"]
        scripts = result["scripts"]
        totals["payloads"] += 2
        totals["payloads_exact_eof"] += int(entities["consumed_to_eof"])
        totals["payloads_exact_eof"] += int(scripts["consumed_to_eof"])
        totals["entity_bytes"] += entities["byte_length"]
        totals["entities"] += entities["record_count"]
        totals["script_bytes"] += scripts["byte_length"]
        totals["groups"] += scripts["group_count"]
        totals["lanes"] += scripts["lane_count"]
        totals["events"] += scripts["event_count"]
        totals["instructions"] += scripts["instruction_count"]
        merge_histogram(field_histogram, entities["field_count_histogram"])
        merge_histogram(raw_type_histogram, entities["raw_type_histogram"])
        merge_histogram(runtime_type_histogram, entities["runtime_type_histogram"])
        merge_histogram(retype_histogram, entities["retype_histogram"])
        merge_histogram(mode_histogram, scripts["mode_histogram"])
        merge_histogram(lane_meta_histogram, scripts["lane_meta_histogram"])
        merge_histogram(opcode_histogram, scripts["opcode_histogram"])
        per_pack.append(
            {
                "pack": result["pack"],
                "records_file": f"pack-{result['pack']}/records.json",
                "entities": entities["record_count"],
                "groups": scripts["group_count"],
                "lanes": scripts["lane_count"],
                "events": scripts["event_count"],
                "instructions": scripts["instruction_count"],
                "slot_0_sha256": result["sources"]["slot_0_entities"][
                    "sha256_actual"
                ],
                "slot_7_sha256": result["sources"]["slot_7_scripts"][
                    "sha256_actual"
                ],
                "payloads_exact_eof": 2,
            }
        )

    actual_totals = {key: totals[key] for key in EXPECTED_TOTALS}
    if actual_totals != EXPECTED_TOTALS:
        raise ValueError(
            f"aggregate totals mismatch: expected {EXPECTED_TOTALS}, got {actual_totals}"
        )
    aggregate_assertions = {
        "entity_field_count_histogram": (
            dict(field_histogram) == EXPECTED_ENTITY_FIELD_HISTOGRAM
        ),
        "raw_type_histogram": dict(raw_type_histogram) == EXPECTED_RAW_TYPE_HISTOGRAM,
        "retype_histogram": dict(retype_histogram) == EXPECTED_RETYPE_HISTOGRAM,
        "lane_meta_histogram": (
            dict(lane_meta_histogram) == EXPECTED_LANE_META_HISTOGRAM
        ),
        "opcode_histogram": dict(opcode_histogram) == EXPECTED_OPCODE_HISTOGRAM,
    }
    failed = [name for name, passed in aggregate_assertions.items() if not passed]
    if failed:
        raise ValueError(f"aggregate pinned-corpus assertions failed: {', '.join(failed)}")

    return {
        "schema_version": SCHEMA_VERSION,
        "static_only": True,
        "decoder": DECODER_ID,
        "input_contract": OUTPUT_CONTRACT,
        "decoded_root": "reconstructed-project/resources/decoded",
        "pack_order": list(PACK_IDS),
        "inventory": actual_totals,
        "per_pack": per_pack,
        "histograms": {
            "entity_field_count": counter_dict(field_histogram),
            "entity_raw_type": counter_dict(raw_type_histogram),
            "entity_runtime_type": counter_dict(runtime_type_histogram),
            "entity_retype": counter_dict(retype_histogram),
            "lane_mode": counter_dict(mode_histogram),
            "lane_meta": counter_dict(lane_meta_histogram),
            "opcode": counter_dict(opcode_histogram),
        },
        "assertions": {
            "expected_totals": "passed",
            "all_16_payloads_exact_eof": "passed",
            "all_source_sizes_and_sha256_match_metadata": "passed",
            "all_group_meta_values_equal_lane_event_count_sums": "passed",
            **{name: "passed" for name in aggregate_assertions},
        },
        "determinism": {
            "ordering": "numeric pack/record/lane/event/instruction; JSON keys sorted",
            "json_encoding": "UTF-8, two-space indent, final newline",
            "managed_manifest": "manifest.json",
            "rerun_check": (
                "--verify-rerun compares every managed file byte-for-byte via hashes; "
                "the flag does not alter generated artifacts"
            ),
        },
        "publication": {
            "model": "shared lock, output lock, staged tree, backup rename, staged rename",
            "managed_cleanup": "whole validated decoder-owned tree replacement",
            "ordinary_failure_recovery": "restore prior output when staged rename fails",
            "stale_lock_policy": "refuse automatic recovery",
        },
        "evidence_scope": {
            "method": "static parsing of already-extracted payloads only",
            "target_code_execution": False,
            "raw_values_and_offsets": "authoritative",
            "semantic_annotations": (
                "limited to proven/high-confidence consumer behavior; lost names remain noted"
            ),
        },
    }


def write_json(path: Path, value: Any) -> None:
    path.write_text(
        json.dumps(value, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def manifest_entry(root: Path, path: Path) -> dict[str, Any]:
    data = path.read_bytes()
    return {
        "path": path.relative_to(root).as_posix(),
        "size": len(data),
        "sha256": hashlib.sha256(data).hexdigest(),
    }


def aggregate_manifest_hash(files: list[dict[str, Any]]) -> str:
    aggregate = hashlib.sha256()
    for item in files:
        aggregate.update(item["path"].encode("utf-8"))
        aggregate.update(b"\0")
        aggregate.update(item["sha256"].encode("ascii"))
        aggregate.update(b"\n")
    return aggregate.hexdigest()


def write_managed_manifest(root: Path) -> dict[str, Any]:
    paths = [root / f"pack-{pack}" / "records.json" for pack in PACK_IDS]
    paths.append(root / "summary.json")
    files = [manifest_entry(root, path) for path in paths]
    manifest = {
        "schema_version": SCHEMA_VERSION,
        "static_only": True,
        "decoder": DECODER_ID,
        "input_contract": OUTPUT_CONTRACT,
        "self_excluded_from_files": True,
        "managed_file_count_excluding_manifest": len(files),
        "managed_total_bytes_excluding_manifest": sum(item["size"] for item in files),
        "managed_aggregate_sha256_excluding_manifest": aggregate_manifest_hash(files),
        "files": files,
    }
    write_json(root / "manifest.json", manifest)
    return manifest


def expected_output_paths() -> tuple[set[str], set[str]]:
    directories = {f"pack-{pack}" for pack in PACK_IDS}
    files = {"summary.json", "manifest.json"}
    files.update(f"pack-{pack}/records.json" for pack in PACK_IDS)
    return directories, files


def validate_existing_output_root(output_root: Path) -> None:
    if output_root.is_symlink() or not output_root.is_dir():
        raise ValueError(f"output root must be a regular directory: {output_root}")
    entries = list(output_root.iterdir())
    if not entries:
        return
    summary_path = output_root / "summary.json"
    manifest_path = output_root / "manifest.json"
    for marker_path in (summary_path, manifest_path):
        if marker_path.is_symlink() or not marker_path.is_file():
            raise ValueError(f"nonempty output lacks managed marker: {marker_path}")
    try:
        summary = json.loads(summary_path.read_text(encoding="utf-8"))
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as error:
        raise ValueError(f"invalid existing managed output metadata: {error}") from error
    for value, name in ((summary, "summary"), (manifest, "manifest")):
        if (
            not isinstance(value, dict)
            or value.get("schema_version") != SCHEMA_VERSION
            or value.get("decoder") != DECODER_ID
            or value.get("input_contract") != OUTPUT_CONTRACT
        ):
            raise ValueError(f"existing output {name} is not owned by this decoder")

    expected_directories, expected_files = expected_output_paths()
    actual_directories: set[str] = set()
    actual_files: set[str] = set()
    for path in output_root.rglob("*"):
        relative = path.relative_to(output_root).as_posix()
        if path.is_symlink():
            raise ValueError(f"refusing symlinked managed output: {path}")
        if path.is_dir():
            actual_directories.add(relative)
        elif path.is_file():
            actual_files.add(relative)
        else:
            raise ValueError(f"refusing non-regular managed path: {path}")
    if actual_directories != expected_directories or actual_files != expected_files:
        raise ValueError(
            "output root contains missing or unmanaged paths: "
            f"directories={sorted(actual_directories)}, files={sorted(actual_files)}"
        )

    files = manifest.get("files")
    if not isinstance(files, list):
        raise ValueError("managed manifest files must be a list")
    recorded_paths = [item.get("path") for item in files if isinstance(item, dict)]
    expected_recorded = [
        f"pack-{pack}/records.json" for pack in PACK_IDS
    ] + ["summary.json"]
    if recorded_paths != expected_recorded:
        raise ValueError("managed manifest file order or inventory is invalid")
    actual_entries = [manifest_entry(output_root, output_root / path) for path in recorded_paths]
    if files != actual_entries:
        raise ValueError("managed manifest hashes or sizes do not match output files")
    if manifest.get("managed_aggregate_sha256_excluding_manifest") != aggregate_manifest_hash(
        actual_entries
    ):
        raise ValueError("managed manifest aggregate hash is invalid")


def tree_fingerprint(root: Path) -> dict[str, Any]:
    validate_existing_output_root(root)
    files = sorted(
        (path for path in root.rglob("*") if path.is_file()),
        key=lambda path: path.relative_to(root).as_posix(),
    )
    entries = [manifest_entry(root, path) for path in files]
    return {
        "file_count": len(entries),
        "total_bytes": sum(item["size"] for item in entries),
        "sha256": aggregate_manifest_hash(entries),
        "files": entries,
    }


def acquire_directory_lock(root: Path, name: str) -> Path:
    lock = root / name
    try:
        lock.mkdir()
    except FileExistsError as error:
        raise ValueError(f"another static reconstruction operation holds {lock}") from error

    def release() -> None:
        try:
            lock.rmdir()
        except OSError:
            pass

    atexit.register(release)
    return lock


def release_lock(lock: Path | None) -> None:
    if lock is None:
        return
    try:
        lock.rmdir()
    except FileNotFoundError:
        return
    except OSError as error:
        print(f"warning: could not remove lock {lock}: {error}", file=sys.stderr)


def publish_staged_output(staging_root: Path, output_root: Path) -> str | None:
    backup_root = output_root.parent / (
        f".{output_root.name}.level-decoder-backup-{os.getpid()}"
    )
    if backup_root.exists() or backup_root.is_symlink():
        raise ValueError(f"refusing existing transaction backup: {backup_root}")
    moved_existing = False
    try:
        if output_root.exists():
            os.replace(output_root, backup_root)
            moved_existing = True
        os.replace(staging_root, output_root)
    except BaseException:
        if moved_existing and not output_root.exists() and backup_root.exists():
            try:
                os.replace(backup_root, output_root)
            except OSError as restore_error:
                print(
                    f"warning: could not restore output backup {backup_root}: "
                    f"{restore_error}",
                    file=sys.stderr,
                )
        raise
    if moved_existing:
        try:
            shutil.rmtree(backup_root)
        except OSError as cleanup_error:
            return f"could not remove committed backup {backup_root}: {cleanup_error}"
    return None


def generate_staged_tree(
    staging_root: Path, payloads: dict[int, dict[str, PayloadAsset]]
) -> tuple[dict[str, Any], dict[str, Any]]:
    pack_results: list[dict[str, Any]] = []
    for pack in PACK_IDS:
        result = build_pack_result(pack, payloads[pack])
        pack_directory = staging_root / f"pack-{pack}"
        pack_directory.mkdir()
        write_json(pack_directory / "records.json", result)
        pack_results.append(result)
    summary = build_summary(pack_results)
    write_json(staging_root / "summary.json", summary)
    manifest = write_managed_manifest(staging_root)
    staging_root.chmod(0o755)
    validate_existing_output_root(staging_root)
    return summary, manifest


def resolve_roots(
    parser: argparse.ArgumentParser, decoded_argument: Path, output_argument: Path | None
) -> tuple[Path, Path, Path]:
    decoded_unresolved = Path(os.path.abspath(decoded_argument))
    if decoded_unresolved.is_symlink():
        parser.error(f"decoded root must not be a symlink: {decoded_unresolved}")
    decoded_root = decoded_unresolved.resolve()
    if not decoded_root.is_dir() or decoded_root.name != "decoded":
        parser.error(f"decoded root must be a decoded resource directory: {decoded_root}")
    if decoded_root.parent.name != "resources":
        parser.error("decoded root parent must be named resources")
    if output_argument is None:
        output_unresolved = decoded_root.parent / "levels-decoded"
    else:
        output_unresolved = Path(os.path.abspath(output_argument))
    if output_unresolved.is_symlink():
        parser.error(f"output root must not be a symlink: {output_unresolved}")
    output_root = output_unresolved.resolve()
    if output_root.name != "levels-decoded" or output_root.parent != decoded_root.parent:
        parser.error("output must be the levels-decoded sibling of decoded")
    unsafe_roots = {Path(output_root.anchor), Path.cwd().resolve(), Path.home().resolve()}
    if output_root in unsafe_roots:
        parser.error(f"unsafe level-decoder output root: {output_root}")
    if decoded_root == output_root or decoded_root in output_root.parents:
        parser.error("decoded input and output roots must not overlap")
    reconstruction_root = decoded_root.parent.parent
    return decoded_root, output_root, reconstruction_root


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "decoded_root",
        nargs="?",
        type=Path,
        default=Path("reconstructed-project/resources/decoded"),
        help="extractor decoded root (default: reconstructed-project/resources/decoded)",
    )
    parser.add_argument(
        "output_root",
        nargs="?",
        type=Path,
        help="levels-decoded sibling output (default: derived from decoded_root)",
    )
    parser.add_argument(
        "--verify-rerun",
        action="store_true",
        help="require a prior output and prove the newly staged tree is byte-identical",
    )
    args = parser.parse_args()
    decoded_root, output_root, reconstruction_root = resolve_roots(
        parser, args.decoded_root, args.output_root
    )

    prior_report = reconstruction_root / "verification-report.json"
    if prior_report.exists() or prior_report.is_symlink():
        if prior_report.is_dir() and not prior_report.is_symlink():
            parser.error(f"verification report path is a directory: {prior_report}")
        prior_report.unlink()

    reconstruction_lock: Path | None = None
    output_lock: Path | None = None
    staging_root: Path | None = None
    try:
        reconstruction_lock = acquire_directory_lock(
            reconstruction_root, ".static-reconstruction.lock"
        )
        output_root.parent.mkdir(parents=True, exist_ok=True)
        output_lock = acquire_directory_lock(
            output_root.parent, f".{output_root.name}.level-decoder.lock"
        )
        if output_root.exists():
            validate_existing_output_root(output_root)
        if args.verify_rerun and (
            not output_root.exists() or not any(output_root.iterdir())
        ):
            raise ValueError("--verify-rerun requires a prior managed output tree")
        before_fingerprint = (
            tree_fingerprint(output_root) if args.verify_rerun else None
        )
        payloads = discover_payloads(decoded_root)
        staging_root = Path(
            tempfile.mkdtemp(
                prefix=f".{output_root.name}.level-decoder-stage-",
                dir=output_root.parent,
            )
        )
        summary, manifest = generate_staged_tree(staging_root, payloads)
        after_fingerprint = tree_fingerprint(staging_root)
        if args.verify_rerun and before_fingerprint != after_fingerprint:
            raise ValueError(
                "rerun determinism check failed: staged managed tree differs from prior output"
            )
        publication_warning = publish_staged_output(staging_root, output_root)
        staging_root = None
        if publication_warning is not None:
            print(f"warning: {publication_warning}", file=sys.stderr)
        result = {
            "status": "success",
            "static_only": True,
            "output": "reconstructed-project/resources/levels-decoded",
            "inventory": summary["inventory"],
            "managed_manifest_sha256": hashlib.sha256(
                (output_root / "manifest.json").read_bytes()
            ).hexdigest(),
            "managed_tree": after_fingerprint,
            "rerun_verification": (
                "passed" if args.verify_rerun else "not-requested"
            ),
            "manifest_aggregate": manifest[
                "managed_aggregate_sha256_excluding_manifest"
            ],
        }
        print(json.dumps(result, indent=2, ensure_ascii=False, sort_keys=True))
        return 0
    except (OSError, KeyError, TypeError, ValueError, struct.error) as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    finally:
        if staging_root is not None and staging_root.exists():
            try:
                shutil.rmtree(staging_root)
            except OSError as cleanup_error:
                print(
                    f"warning: could not remove staging directory {staging_root}: "
                    f"{cleanup_error}",
                    file=sys.stderr,
                )
        release_lock(output_lock)
        release_lock(reconstruction_lock)


if __name__ == "__main__":
    raise SystemExit(main())
