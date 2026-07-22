#!/usr/bin/env python3
"""Statically inspect and decode the extracted Gameloft sprite binaries.

The input is the decoded resource directory produced by
``extract-java-me-resource-packs.py``.  This tool deliberately accepts only
the extracted ``.bin`` entries and their JSON provenance; it never opens the
original JAR and never loads target classes.

The binary grammar and seven runtime pixel branches implemented here are
direct translations of ``b.a(byte[], int)`` and
``b.a(byte[], int, int, int)``.  One additional static recovery branch is
derived empirically from exact corpus invariants and is labeled separately in
the generated metadata.  The obfuscated runtime field names are retained where
no original semantic name survives.
"""

from __future__ import annotations

import argparse
import atexit
import binascii
import hashlib
import json
import os
import re
import shutil
import stat
import struct
import sys
import tempfile
import zlib
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Any


SPRITE_TYPE = "Gameloft sprite binary"
EXPECTED_MAGIC = 0x05DF
OUTPUT_CONTRACT = "decoded pack metadata plus extracted .bin entries; no JAR/class input"
RUNTIME_PIXEL_BUFFER_CAPACITY = 40_960
MAX_ASSETS = 4_096
MAX_INPUT_BYTES = 64 * 1024 * 1024
MAX_METADATA_FILES = 1_024
MAX_METADATA_BYTES_PER_FILE = 2 * 1024 * 1024
MAX_TOTAL_METADATA_BYTES = 16 * 1024 * 1024
MAX_METADATA_ENTRIES_PER_PACK = 4_096
MAX_TOTAL_METADATA_ENTRIES = 16_384
MAX_TOTAL_MODULES = 100_000
MAX_TOTAL_SECTION_RECORDS = 250_000
MAX_TOTAL_DECODED_PIXELS = 50_000_000
MAX_PNG_VARIANTS = 100_000
MAX_ESTIMATED_PNG_BYTES = 512 * 1024 * 1024
ENTRY_FILE_PATTERN = re.compile(r"entry-(\d{3,})-marker-(\d{3,})\.bin\Z")
OUTPUT_PACK_PATTERN = re.compile(r"pack-(\d+)\Z")
OUTPUT_ENTRY_PATTERN = re.compile(r"entry-(\d{3,})-marker-(\d{3,})\Z")
OUTPUT_PNG_PATTERN = re.compile(
    r"module-(\d{4,})-palette-(\d{2,})-(\d+)x(\d+)\.png\Z"
)

# Runtime module type and the exact conditional reads selected by each tag.
# The labels describe parser actions, not lost commercial codec/type names.
MODULE_TAG_LAYOUTS: dict[int, dict[str, Any]] = {
    0: {
        "runtime_type": 0,
        "reads_aW": False,
        "reads_dimensions": True,
    },
    255: {
        "runtime_type": 1,
        "reads_aW": True,
        "reads_dimensions": True,
    },
    254: {
        "runtime_type": 2,
        "reads_aW": True,
        "reads_dimensions": True,
    },
    253: {
        "runtime_type": 5,
        "reads_aW": False,
        "reads_dimensions": True,
    },
    252: {
        "runtime_type": 3,
        "reads_aW": True,
        "reads_dimensions": True,
        "extra_u16_count": 2,
    },
    251: {
        "runtime_type": 4,
        "reads_aW": True,
        "reads_dimensions": True,
        "extra_u16_count": 2,
    },
    250: {
        "runtime_type": 6,
        "reads_aW": True,
        "reads_dimensions": False,
        "extra_u16_count": 4,
    },
    249: {
        "runtime_type": 7,
        "reads_aW": True,
        "reads_dimensions": False,
        "extra_u16_count": 4,
    },
    248: {
        "runtime_type": 8,
        "reads_aW": True,
        "reads_dimensions": True,
    },
    247: {
        "runtime_type": 9,
        "reads_aW": True,
        "reads_dimensions": True,
        "reads_type_9_extra": True,
    },
}


FLAG_EFFECTS: dict[int, str] = {
    0x00000004: "tag 0 records include one aV byte",
    0x00000010: "module ae/af dimensions are u16 LE instead of u8",
    0x00000020: "tag 0 records include u16 LE ac/ad values",
    0x00000080: "aB payload lengths are u32 LE instead of u16 LE",
    0x00000400: "ar/as and rectangle values use u16 LE storage",
    0x00001000: "ak/al rectangle records are omitted and derived later",
    0x00004000: "ap/ar/as/aq records include one at byte",
    0x00008000: "am/an records and per-ai ao deltas are present",
    0x00040000: "ax/ay values use u16 LE storage instead of signed bytes",
    0x01000000: "palette, pixel-code, and per-module aB payload section may follow",
}


PALETTE_FORMATS = {
    0x8888: {
        "bytes_per_color": 4,
        "behavior": "u32 LE value copied directly to runtime ARGB int",
    },
    0x5515: {
        "bytes_per_color": 2,
        "behavior": "u16 LE expanded by the 0x5515 branch in b",
    },
    0x6505: {
        "bytes_per_color": 2,
        "behavior": "u16 LE expanded by the 0x6505 branch in b",
    },
}


RUNTIME_PIXEL_BRANCHES = {
    0x64F0: "palette index plus run length in one byte",
    0x56F2: "signed-control literal/run branch",
    0x1600: "two 4-bit palette indexes per byte",
    0x0400: "four 2-bit palette indexes per byte",
    0x0200: "eight 1-bit palette indexes per byte",
    0x5602: "one 8-bit palette index per pixel",
    0xA640: "0x64f0 indexes followed by the alpha/overlay stream",
}

STATIC_RECOVERY_PIXEL_BRANCHES = {
    0x27F1: "literal palette index or 7-bit run followed by palette index",
}

PINNED_0X27F1_RECOVERY = {
    "pack": "3",
    "entry_index": 58,
    "source_sha256": "01de766ddea14ab4ab5cd836338c3c52a25241ba47eaddc22a31daa620a9a7b5",
    "palette_count": 2,
    "palette_size": 98,
    "modules": (
        {
            "width": 83,
            "height": 33,
            "payload_sha256": "a53e03b927768e97789c6e91833ad3290255e85b824b4e90882b5f093f9f8a3b",
        },
        {
            "width": 83,
            "height": 23,
            "payload_sha256": "5d49205e7047d1744b3cd2aa4c3da664c406be53786377bb6ed4a9a21c536966",
        },
    ),
}

PIXEL_BRANCHES = {
    **RUNTIME_PIXEL_BRANCHES,
    **STATIC_RECOVERY_PIXEL_BRANCHES,
}

PIXEL_BRANCH_EVIDENCE = {
    **{
        code: {
            "classification": "runtime-proven",
            "confidence": "proven",
            "evidence_basis": "recovered-runtime-bytecode",
            "runtime_bytecode_branch": True,
        }
        for code in RUNTIME_PIXEL_BRANCHES
    },
    0x27F1: {
        "classification": "data-derived-static-recovery",
        "confidence": "high",
        "evidence_basis": "empirical-corpus-invariants",
        "runtime_bytecode_branch": False,
    },
}


EVIDENCE = {
    "loader": {
        "structured_source": "reconstructed-project/src/structured/b.java:123",
        "simple_source": "reconstructed-project/src/simple/b.java:123",
        "fallback_source": "reconstructed-project/src/fallback/b.java:114",
        "bytecode": "reconstructed-project/bytecode/b.javap.txt:1002",
        "method": "b.a(byte[], int)",
    },
    "pixel_decoder": {
        "structured_source": "reconstructed-project/src/structured/b.java:1371",
        "method": "b.a(byte[], int, int, int)",
        "runtime_proven_codes": [
            f"0x{code:04x}" for code in sorted(RUNTIME_PIXEL_BRANCHES)
        ],
        "data_derived_static_recovery": {
            "pixel_code": "0x27f1",
            **PIXEL_BRANCH_EVIDENCE[0x27F1],
        },
    },
    "confidence_scope": {
        "loader_grammar_and_record_offsets": "proven",
        "seven_runtime_pixel_branches": "proven",
        "pixel_code_0x27f1_static_recovery": "high empirical/data-derived",
        "unlisted_branch_values": "unsupported and preserved without image output",
    },
    "basis": (
        "direct translation of the loader and seven runtime pixel branches, plus "
        "an explicitly non-runtime 0x27f1 recovery derived from exact corpus "
        "dimensions, payload consumption, and palette bounds"
    ),
}


class SpriteDecodeError(ValueError):
    """A range, branch, or structural error with an exact source offset."""

    def __init__(self, message: str, offset: int, branch: str) -> None:
        super().__init__(message)
        self.offset = offset
        self.branch = branch


class PixelDecodeError(ValueError):
    """An unsupported or truncated pixel payload."""

    def __init__(self, message: str, offset: int, branch: str) -> None:
        super().__init__(message)
        self.offset = offset
        self.branch = branch


class Cursor:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.offset = 0

    def require(self, size: int, branch: str) -> None:
        if size < 0 or self.offset + size > len(self.data):
            raise SpriteDecodeError(
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

    def u8(self, branch: str) -> int:
        return self.take(1, branch)[0]

    def i8(self, branch: str) -> int:
        return struct.unpack("<b", self.take(1, branch))[0]

    def u16(self, branch: str) -> int:
        return struct.unpack("<H", self.take(2, branch))[0]

    def u32(self, branch: str) -> int:
        return struct.unpack("<I", self.take(4, branch))[0]


@dataclass
class ParsedSprite:
    metadata: dict[str, Any]
    modules: list[dict[str, Any]]
    palettes: list[list[int]]
    payloads: list[bytes]


def java_i8(value: int) -> int:
    return value - 0x100 if value & 0x80 else value


def java_i16(value: int) -> int:
    return value - 0x10000 if value & 0x8000 else value


def offset_range(start: int, end: int) -> dict[str, Any]:
    return {
        "start": start,
        "start_hex": f"0x{start:08x}",
        "end_exclusive": end,
        "end_exclusive_hex": f"0x{end:08x}",
        "size": end - start,
    }


def read_positive_short_count(cursor: Cursor, branch: str) -> tuple[int, int]:
    offset = cursor.offset
    raw = cursor.u16(branch)
    value = java_i16(raw)
    if value < 0:
        raise SpriteDecodeError(
            f"u16 value 0x{raw:04x} becomes a negative Java short",
            offset,
            branch,
        )
    return value, offset


def byte_value(raw: int) -> dict[str, int]:
    return {"raw_u8": raw, "java_i8": java_i8(raw)}


def short_value(raw: int) -> dict[str, int]:
    return {"raw_u16": raw, "java_i16": java_i16(raw)}


def parse_modules(
    cursor: Cursor, flags: int, module_count: int
) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    start = cursor.offset
    modules: list[dict[str, Any]] = []
    for index in range(module_count):
        record_start = cursor.offset
        tag = cursor.u8("modules.tag")
        layout = MODULE_TAG_LAYOUTS.get(tag)
        if layout is None:
            raise SpriteDecodeError(
                f"module tag {tag} has no proven loader branch",
                record_start,
                "modules.tag",
            )

        record: dict[str, Any] = {
            "index": index,
            "tag": tag,
            "runtime_type_aU": layout["runtime_type"],
        }
        if tag == 0 and flags & 0x04:
            record["aV"] = byte_value(cursor.u8("modules.aV"))
        if layout["reads_aW"]:
            raw = cursor.u32("modules.aW")
            record["aW"] = {"raw_u32": raw, "hex": f"0x{raw:08x}"}
        if layout.get("reads_type_9_extra"):
            raw = cursor.u32("modules.type_9_extra_u32")
            record["type_9_extra"] = {
                "raw_u32": raw,
                "hex": f"0x{raw:08x}",
                "trailing_i8": cursor.i8("modules.type_9_extra_i8"),
            }
        if tag == 0 and flags & 0x20:
            record["ac"] = short_value(cursor.u16("modules.ac"))
            record["ad"] = short_value(cursor.u16("modules.ad"))
        if layout["reads_dimensions"]:
            if flags & 0x10:
                width = cursor.u16("modules.ae")
                height = cursor.u16("modules.af")
                storage = "u16-le"
            else:
                width = cursor.u8("modules.ae")
                height = cursor.u8("modules.af")
                storage = "u8"
            record["ae_width"] = width
            record["af_height"] = height
            record["dimension_storage"] = storage
        else:
            record["ae_width"] = 0
            record["af_height"] = 0

        extra_count = int(layout.get("extra_u16_count", 0))
        if extra_count:
            record["ag_values"] = [
                cursor.u16("modules.ag") for _ in range(extra_count)
            ]
        record["source_range"] = offset_range(record_start, cursor.offset)
        modules.append(record)

    section = {
        "id": "modules",
        "runtime_fields": ["aU", "aV", "aW", "ac", "ad", "ae", "af", "ag", "ah"],
        "count": module_count,
        "source_range": offset_range(start, cursor.offset),
        "confidence": "proven",
        "records": modules,
    }
    return modules, section


def parse_ap_records(cursor: Cursor, flags: int) -> dict[str, Any]:
    start = cursor.offset
    count, count_offset = read_positive_short_count(cursor, "ap_ar_as_aq.count")
    records: list[dict[str, Any]] = []
    for index in range(count):
        record_start = cursor.offset
        ap = cursor.u8("ap_ar_as_aq.ap")
        if flags & 0x400:
            ar_raw = cursor.u16("ap_ar_as_aq.ar")
            as_raw = cursor.u16("ap_ar_as_aq.as")
        else:
            ar_raw = cursor.u8("ap_ar_as_aq.ar")
            as_raw = cursor.u8("ap_ar_as_aq.as")
        record: dict[str, Any] = {
            "index": index,
            "ap": byte_value(ap),
            "ar": (
                short_value(ar_raw)
                if flags & 0x400
                else byte_value(ar_raw)
            ),
            "as": (
                short_value(as_raw)
                if flags & 0x400
                else byte_value(as_raw)
            ),
        }
        if flags & 0x4000:
            record["at"] = byte_value(cursor.u8("ap_ar_as_aq.at"))
        aq = cursor.u8("ap_ar_as_aq.aq")
        record["aq"] = byte_value(aq)
        referenced_index = ap | ((aq & 0xC0) << 2)
        record["referenced_index"] = referenced_index
        record["reference_target_from_aq_bit_0x10"] = (
            "ai_frame_record" if aq & 0x10 else "module_record"
        )
        record["transform_bits"] = aq & 0x0F
        record["source_range"] = offset_range(record_start, cursor.offset)
        records.append(record)
    return {
        "id": "ap_ar_as_aq_records",
        "runtime_fields": ["ap", "ar", "as", "at", "aq"],
        "count": count,
        "count_offset": count_offset,
        "source_range": offset_range(start, cursor.offset),
        "consumer_evidence": (
            "b reconstructs an index from ap/aq; aq bit 0x10 selects recursive "
            "ai-frame drawing, otherwise the index selects a module"
        ),
        "confidence": "proven",
        "records": records,
    }


def parse_am_an_records(cursor: Cursor, flags: int) -> dict[str, Any] | None:
    if not flags & 0x8000:
        return None
    start = cursor.offset
    count, count_offset = read_positive_short_count(cursor, "am_or_an.count")
    records: list[dict[str, Any]] = []
    for index in range(count):
        record_start = cursor.offset
        if flags & 0x400:
            raw = [cursor.u16("am_or_an.value") for _ in range(4)]
            values = [java_i16(raw[0]), java_i16(raw[1]), raw[2], raw[3]]
            storage = "u16-le"
        else:
            raw = [cursor.u8("am_or_an.value") for _ in range(4)]
            values = [java_i8(raw[0]), java_i8(raw[1]), raw[2], raw[3]]
            storage = "u8"
        records.append(
            {
                "index": index,
                "runtime_values": values,
                "raw_values": raw,
                "value_order_from_consumer": ["x", "y", "width", "height"],
                "storage": storage,
                "source_range": offset_range(record_start, cursor.offset),
            }
        )
    return {
        "id": "am_or_an_records",
        "runtime_fields": ["am", "an"],
        "count": count,
        "count_offset": count_offset,
        "source_range": offset_range(start, cursor.offset),
        "confidence": "proven",
        "records": records,
    }


def parse_ai_records(cursor: Cursor, flags: int) -> dict[str, Any]:
    start = cursor.offset
    count, count_offset = read_positive_short_count(cursor, "ai_aj_ao.count")
    records: list[dict[str, Any]] = []
    cumulative_ao = 0
    for index in range(count):
        record_start = cursor.offset
        ai = cursor.u8("ai_aj_ao.ai")
        aj = cursor.u16("ai_aj_ao.aj")
        record: dict[str, Any] = {
            "index": index,
            "ai": byte_value(ai),
            "aj": short_value(aj),
            "ap_record_span": {"start": aj, "count": ai, "end_exclusive": aj + ai},
        }
        if flags & 0x8000:
            delta_raw = cursor.u8("ai_aj_ao.delta")
            delta = java_i8(delta_raw)
            record["ao_start"] = cumulative_ao
            record["ao_delta"] = {"raw_u8": delta_raw, "java_i8": delta}
            cumulative_ao += delta
            record["ao_end_exclusive"] = cumulative_ao
        record["source_range"] = offset_range(record_start, cursor.offset)
        records.append(record)

    rectangles_start = cursor.offset
    rectangles: list[dict[str, Any]] = []
    if not flags & 0x1000:
        for index in range(count):
            record_start = cursor.offset
            if flags & 0x400:
                raw = [cursor.u16("ak_or_al.value") for _ in range(4)]
                values = [java_i16(raw[0]), java_i16(raw[1]), raw[2], raw[3]]
                storage = "u16-le"
            else:
                raw = [cursor.u8("ak_or_al.value") for _ in range(4)]
                values = [java_i8(raw[0]), java_i8(raw[1]), raw[2], raw[3]]
                storage = "u8"
            rectangles.append(
                {
                    "index": index,
                    "runtime_values": values,
                    "raw_values": raw,
                    "value_order_from_accessors": ["x", "y", "width", "height"],
                    "storage": storage,
                    "source_range": offset_range(record_start, cursor.offset),
                }
            )

    return {
        "id": "ai_aj_ao_and_ak_or_al_records",
        "runtime_fields": ["ai", "aj", "ao", "ak", "al"],
        "count": count,
        "count_offset": count_offset,
        "source_range": offset_range(start, cursor.offset),
        "ai_aj_ao_range": offset_range(start, rectangles_start),
        "ak_or_al_range": offset_range(rectangles_start, cursor.offset),
        "ao_total": cumulative_ao if flags & 0x8000 else None,
        "confidence": "proven",
        "records": records,
        "ak_or_al_records": rectangles,
    }


def parse_av_records(cursor: Cursor, flags: int) -> dict[str, Any]:
    start = cursor.offset
    count, count_offset = read_positive_short_count(cursor, "av_aw_ax_ay_i.count")
    records: list[dict[str, Any]] = []
    for index in range(count):
        record_start = cursor.offset
        av = cursor.u8("av_aw_ax_ay_i.av")
        aw = cursor.u8("av_aw_ax_ay_i.aw")
        if flags & 0x40000:
            ax_raw = cursor.u16("av_aw_ax_ay_i.ax")
            ay_raw = cursor.u16("av_aw_ax_ay_i.ay")
            ax = java_i16(ax_raw)
            ay = java_i16(ay_raw)
            storage = "u16-le"
        else:
            ax_raw = cursor.u8("av_aw_ax_ay_i.ax")
            ay_raw = cursor.u8("av_aw_ax_ay_i.ay")
            ax = java_i8(ax_raw)
            ay = java_i8(ay_raw)
            storage = "u8"
        flags_i = cursor.u8("av_aw_ax_ay_i.i")
        records.append(
            {
                "index": index,
                "av": byte_value(av),
                "aw": byte_value(aw),
                "ax": {"raw": ax_raw, "runtime_signed": ax},
                "ay": {"raw": ay_raw, "runtime_signed": ay},
                "i": byte_value(flags_i),
                "referenced_ai_frame_index": av | ((flags_i & 0xC0) << 2),
                "transform_bits": flags_i & 0x0F,
                "offset_storage": storage,
                "source_range": offset_range(record_start, cursor.offset),
            }
        )
    return {
        "id": "av_aw_ax_ay_i_records",
        "runtime_fields": ["av", "aw", "ax", "ay", "i"],
        "count": count,
        "count_offset": count_offset,
        "source_range": offset_range(start, cursor.offset),
        "consumer_evidence": (
            "b reconstructs an ai-frame index from av/i, then applies ax/ay and transform bits"
        ),
        "confidence": "proven",
        "records": records,
    }


def parse_au_records(cursor: Cursor) -> dict[str, Any]:
    start = cursor.offset
    count, count_offset = read_positive_short_count(cursor, "au_h.count")
    records: list[dict[str, Any]] = []
    for index in range(count):
        record_start = cursor.offset
        au = cursor.u8("au_h.au")
        h = cursor.u16("au_h.h")
        records.append(
            {
                "index": index,
                "au": byte_value(au),
                "h": short_value(h),
                "av_record_span": {"start": h, "count": au, "end_exclusive": h + au},
                "source_range": offset_range(record_start, cursor.offset),
            }
        )
    return {
        "id": "au_h_records",
        "runtime_fields": ["au", "h"],
        "count": count,
        "count_offset": count_offset,
        "source_range": offset_range(start, cursor.offset),
        "confidence": "proven",
        "records": records,
    }


def decode_palette_color(format_code: int, raw: bytes) -> int:
    if format_code == 0x8888:
        return struct.unpack("<I", raw)[0]
    value = struct.unpack("<H", raw)[0]
    if format_code == 0x5515:
        alpha = 0xFF000000 if value & 0x8000 else 0
        color = alpha | ((value & 0x7C00) << 9) | ((value & 0x03E0) << 6) | (
            (value & 0x001F) << 3
        )
    elif format_code == 0x6505:
        alpha = 0 if value == 0xF81F else 0xFF000000
        color = alpha | ((value & 0xF800) << 8) | ((value & 0x07E0) << 5) | (
            (value & 0x001F) << 3
        )
    else:
        raise ValueError(f"unsupported palette format 0x{format_code:04x}")
    return 0x00FF00FF if color == 0x00F800F8 else color


def parse_palette_and_payloads(
    cursor: Cursor,
    flags: int,
    modules: list[dict[str, Any]],
) -> tuple[dict[str, Any] | None, list[list[int]], list[bytes], list[dict[str, Any]]]:
    limitations: list[dict[str, Any]] = []
    if not flags & 0x01000000:
        pixel_modules = [
            module["index"]
            for module in modules
            if module["runtime_type_aU"] == 0
            and module["ae_width"] > 0
            and module["af_height"] > 0
        ]
        if pixel_modules:
            limitations.append(
                {
                    "kind": "pixel_modules_without_palette_payload_flag",
                    "offset": cursor.offset,
                    "module_indexes": pixel_modules,
                    "effect": "no palette/aB bytes are read by b for these pixel modules",
                }
            )
        if cursor.offset != len(cursor.data):
            limitations.append(
                {
                    "kind": "unparsed_tail_without_palette_flag",
                    "offset": cursor.offset,
                    "size": len(cursor.data) - cursor.offset,
                }
            )
        return None, [], [], limitations

    if cursor.offset == len(cursor.data):
        limitations.append(
            {
                "kind": "palette_and_aB_section_absent_at_eof",
                "offset": cursor.offset,
                "flag": "0x01000000",
                "effect": (
                    "structurally complete/load-valid and runtime-nonrendering because "
                    "the optional palette/pixel payload tail is absent at EOF"
                ),
                "runtime_image_behavior": "nonrendering",
                "pixels_synthesized": False,
            }
        )
        return None, [], [], limitations

    start = cursor.offset
    format_offset = cursor.offset
    palette_format = cursor.u16("palette.format")
    palette_count = cursor.u8("palette.count")
    palette_size_byte = cursor.u8("palette.size")
    palette_size = palette_size_byte or 256
    format_info = PALETTE_FORMATS.get(palette_format)
    palettes: list[list[int]] = []
    palette_records: list[dict[str, Any]] = []
    if format_info is None:
        limitations.append(
            {
                "kind": "unsupported_palette_format",
                "offset": format_offset,
                "value": f"0x{palette_format:04x}",
                "effect": "b has no palette-byte consumption branch for this value",
            }
        )
    else:
        bytes_per_color = int(format_info["bytes_per_color"])
        for palette_index in range(palette_count):
            palette_start = cursor.offset
            colors: list[int] = []
            for _ in range(palette_size):
                raw = cursor.take(bytes_per_color, "palette.colors")
                colors.append(decode_palette_color(palette_format, raw))
            palettes.append(colors)
            palette_records.append(
                {
                    "index": palette_index,
                    "source_range": offset_range(palette_start, cursor.offset),
                    "argb": [f"0x{value:08x}" for value in colors],
                }
            )

    pixel_code_offset = cursor.offset
    pixel_code = cursor.u16("aL.pixel_code")
    pixel_branch_evidence = PIXEL_BRANCH_EVIDENCE.get(
        pixel_code,
        {
            "classification": "unsupported",
            "confidence": "unsupported",
            "evidence_basis": None,
            "runtime_bytecode_branch": False,
        },
    )
    if pixel_code not in PIXEL_BRANCHES:
        limitations.append(
            {
                "kind": "unsupported_pixel_code",
                "offset": pixel_code_offset,
                "value": f"0x{pixel_code:04x}",
                "effect": (
                    "b.a(byte[], int, int, int) has no pixel-fill branch for this value"
                ),
            }
        )

    payloads: list[bytes] = []
    payload_records: list[dict[str, Any]] = []
    for module in modules:
        length_offset = cursor.offset
        if flags & 0x80:
            length = cursor.u32("aB.length")
            if length & 0x80000000:
                raise SpriteDecodeError(
                    "aB length becomes a negative Java int",
                    length_offset,
                    "aB.length",
                )
            length_storage = "u32-le"
        else:
            raw_length = cursor.u16("aB.length")
            length = java_i16(raw_length)
            if length < 0:
                raise SpriteDecodeError(
                    "aB length becomes a negative Java short",
                    length_offset,
                    "aB.length",
                )
            length_storage = "u16-le"
        data_offset = cursor.offset
        payload = cursor.take(length, "aB.data")
        payloads.append(payload)
        module["aB_payload"] = {
            "length": length,
            "length_offset": length_offset,
            "data_range": offset_range(data_offset, cursor.offset),
            "sha256": hashlib.sha256(payload).hexdigest(),
        }
        payload_records.append(
            {
                "module_index": module["index"],
                "length": length,
                "length_storage": length_storage,
                "length_offset": length_offset,
                "data_range": offset_range(data_offset, cursor.offset),
                "sha256": hashlib.sha256(payload).hexdigest(),
            }
        )

    section = {
        "id": "palette_pixel_code_and_aB_payloads",
        "runtime_fields": ["j", "k", "aG", "aL", "aB", "aC"],
        "palette_format": {
            "value": palette_format,
            "hex": f"0x{palette_format:04x}",
            "offset": format_offset,
            "implemented_branch": format_info is not None,
            "behavior": format_info["behavior"] if format_info else None,
        },
        "palette_count": palette_count,
        "palette_size_byte": palette_size_byte,
        "palette_size_effective": palette_size,
        "palettes": palette_records,
        "pixel_code": {
            "value": pixel_code,
            "hex": f"0x{pixel_code:04x}",
            "offset": pixel_code_offset,
            "implemented_branch": pixel_code in PIXEL_BRANCHES,
            "behavior": PIXEL_BRANCHES.get(pixel_code),
            **pixel_branch_evidence,
        },
        "payload_count": len(payload_records),
        "payloads": payload_records,
        "source_range": offset_range(start, cursor.offset),
        "confidence": "proven",
        "confidence_scope": "loader structure and source boundaries",
    }
    return section, palettes, payloads, limitations


def collect_reference_observations(metadata: dict[str, Any]) -> list[dict[str, Any]]:
    sections = {section["id"]: section for section in metadata["sections"]}
    module_count = metadata["header"]["module_count_ab"]
    issues: list[dict[str, Any]] = []

    ap_section = sections["ap_ar_as_aq_records"]
    ai_section = sections["ai_aj_ao_and_ak_or_al_records"]
    for record in ap_section["records"]:
        target_is_frame = (
            record["reference_target_from_aq_bit_0x10"] == "ai_frame_record"
        )
        limit = ai_section["count"] if target_is_frame else module_count
        if record["referenced_index"] >= limit:
            issues.append(
                {
                    "kind": "ap_aq_index_outside_loaded_count_or_sentinel",
                    "record": record["index"],
                    "target": record["reference_target_from_aq_bit_0x10"],
                    "value": record["referenced_index"],
                    "limit": limit,
                }
            )

    for record in ai_section["records"]:
        if record["ap_record_span"]["end_exclusive"] > ap_section["count"]:
            issues.append(
                {
                    "kind": "ai_aj_span_outside_loaded_count",
                    "record": record["index"],
                    "span": record["ap_record_span"],
                    "limit": ap_section["count"],
                }
            )

    av_section = sections["av_aw_ax_ay_i_records"]
    for record in av_section["records"]:
        if record["referenced_ai_frame_index"] >= ai_section["count"]:
            issues.append(
                {
                    "kind": "av_i_index_outside_loaded_ai_count_or_sentinel",
                    "record": record["index"],
                    "value": record["referenced_ai_frame_index"],
                    "limit": ai_section["count"],
                }
            )

    au_section = sections["au_h_records"]
    for record in au_section["records"]:
        if record["av_record_span"]["end_exclusive"] > av_section["count"]:
            issues.append(
                {
                    "kind": "au_h_span_outside_loaded_count",
                    "record": record["index"],
                    "span": record["av_record_span"],
                    "limit": av_section["count"],
                }
            )

    palette_section = sections.get("palette_pixel_code_and_aB_payloads")
    if palette_section is not None and "at" in ap_section["runtime_fields"]:
        palette_count = palette_section["palette_count"]
        for record in ap_section["records"]:
            if "at" in record and record["at"]["raw_u8"] >= palette_count:
                issues.append(
                    {
                        "kind": "at_index_outside_loaded_palette_count_or_sentinel",
                        "record": record["index"],
                        "value": record["at"]["raw_u8"],
                        "limit": palette_count,
                    }
                )
    return issues


def parse_sprite(data: bytes) -> ParsedSprite:
    cursor = Cursor(data)
    magic = cursor.u16("header.magic")
    if magic != EXPECTED_MAGIC:
        raise SpriteDecodeError(
            f"unexpected magic/version 0x{magic:04x}", 0, "header.magic"
        )
    flags = cursor.u32("header.aD")
    module_count, module_count_offset = read_positive_short_count(
        cursor, "header.ab"
    )
    header = {
        "source_range": offset_range(0, cursor.offset),
        "magic_or_version": magic,
        "magic_or_version_hex": f"0x{magic:04x}",
        "flags_aD": flags,
        "flags_aD_hex": f"0x{flags:08x}",
        "module_count_ab": module_count,
        "module_count_offset": module_count_offset,
        "set_bits": [
            {
                "mask": f"0x{1 << bit:08x}",
                "loader_effect": FLAG_EFFECTS.get(
                    1 << bit, "set in input; no byte-read effect in b.a(byte[], int)"
                ),
            }
            for bit in range(32)
            if flags & (1 << bit)
        ],
        "confidence": "proven",
    }

    modules, module_section = parse_modules(cursor, flags, module_count)
    sections = [module_section, parse_ap_records(cursor, flags)]
    am_an = parse_am_an_records(cursor, flags)
    if am_an is not None:
        sections.append(am_an)
    sections.extend(
        [
            parse_ai_records(cursor, flags),
            parse_av_records(cursor, flags),
            parse_au_records(cursor),
        ]
    )
    palette_section, palettes, payloads, limitations = parse_palette_and_payloads(
        cursor, flags, modules
    )
    if palette_section is not None:
        sections.append(palette_section)

    if cursor.offset != len(data):
        limitations.append(
            {
                "kind": "unparsed_tail",
                "offset": cursor.offset,
                "size": len(data) - cursor.offset,
            }
        )

    metadata: dict[str, Any] = {
        "format": "Gameloft sprite binary parsed by b",
        "evidence": EVIDENCE,
        "header": header,
        "sections": sections,
        "parse_end": cursor.offset,
        "input_size": len(data),
        "consumed_to_eof": cursor.offset == len(data),
        "limitations": limitations,
    }
    reference_observations = collect_reference_observations(metadata)
    metadata["reference_observations"] = {
        "status": (
            "all-indexes-within-loaded-counts"
            if not reference_observations
            else "observed-outside-count-or-sentinel-values"
        ),
        "items": reference_observations,
        "interpretation": (
            "Preserved as observations, not parse failures: b does not establish "
            "whether every stored record is drawn, and 0xff/0x3ff values occur in "
            "otherwise structurally valid assets."
        ),
    }
    return ParsedSprite(metadata, modules, palettes, payloads)


def payload_byte(payload: bytes, offset: int, branch: str) -> tuple[int, int]:
    if offset >= len(payload):
        raise PixelDecodeError("pixel payload is truncated", offset, branch)
    return payload[offset], offset + 1


def palette_mask_and_shift(palette_size: int) -> tuple[int, int]:
    remaining = palette_size - 1
    mask = 1
    shift = 0
    while remaining != 0:
        remaining >>= 1
        mask <<= 1
        shift += 1
    return mask - 1, shift


def decode_pixel_indexes(
    payload: bytes, width: int, height: int, pixel_code: int, palette_size: int
) -> dict[str, Any]:
    total = width * height
    branch = f"pixel_code_0x{pixel_code:04x}"
    if palette_size <= 0:
        raise PixelDecodeError("palette size must be positive", 0, branch)
    if total > RUNTIME_PIXEL_BUFFER_CAPACITY:
        raise PixelDecodeError(
            f"pixel count {total} exceeds b.g capacity "
            f"{RUNTIME_PIXEL_BUFFER_CAPACITY}",
            0,
            "runtime_pixel_buffer_capacity",
        )
    indexes: list[int] = []
    alpha: list[int] | None = None
    offset = 0
    produced_slots = 0

    def emit(index: int) -> None:
        nonlocal produced_slots
        if produced_slots >= RUNTIME_PIXEL_BUFFER_CAPACITY:
            raise PixelDecodeError(
                "decoded write exceeds b.g capacity "
                f"{RUNTIME_PIXEL_BUFFER_CAPACITY}",
                max(0, offset - 1),
                "runtime_pixel_buffer_capacity",
            )
        if index >= palette_size:
            raise PixelDecodeError(
                f"palette index {index} is outside palette size {palette_size}",
                max(0, offset - 1),
                branch,
            )
        produced_slots += 1
        if len(indexes) < total:
            indexes.append(index)

    if pixel_code in (0x64F0, 0xA640):
        mask, shift = palette_mask_and_shift(palette_size)
        while len(indexes) < total:
            value, offset = payload_byte(payload, offset, branch)
            palette_index = value & mask
            repeat = (value >> shift) + 1
            for _ in range(repeat):
                emit(palette_index)
        if pixel_code == 0xA640:
            alpha = []
            while len(alpha) < total:
                control_offset = offset
                value, offset = payload_byte(payload, offset, branch)
                if value == 254:
                    repeat, offset = payload_byte(payload, offset, branch)
                    alpha_value, offset = payload_byte(payload, offset, branch)
                    if len(alpha) + repeat > RUNTIME_PIXEL_BUFFER_CAPACITY:
                        raise PixelDecodeError(
                            "alpha write exceeds b.g capacity "
                            f"{RUNTIME_PIXEL_BUFFER_CAPACITY}",
                            control_offset,
                            "runtime_pixel_buffer_capacity_alpha",
                        )
                    alpha.extend([alpha_value] * repeat)
                else:
                    if len(alpha) >= RUNTIME_PIXEL_BUFFER_CAPACITY:
                        raise PixelDecodeError(
                            "alpha write exceeds b.g capacity "
                            f"{RUNTIME_PIXEL_BUFFER_CAPACITY}",
                            control_offset,
                            "runtime_pixel_buffer_capacity_alpha",
                        )
                    alpha.append(value)
            alpha = alpha[:total]
    elif pixel_code == 0x56F2:
        while len(indexes) < total:
            raw_control, offset = payload_byte(payload, offset, branch)
            control = java_i8(raw_control)
            if control < 0:
                repeat = max(1, control + 128)
                for _ in range(repeat):
                    value, offset = payload_byte(payload, offset, branch)
                    emit(value)
            else:
                value, offset = payload_byte(payload, offset, branch)
                repeat = max(1, control)
                for _ in range(repeat):
                    emit(value)
    elif pixel_code == 0x27F1:
        while len(indexes) < total:
            control_offset = offset
            control, offset = payload_byte(payload, offset, branch)
            if control < 0x80:
                emit(control)
                continue

            repeat = control & 0x7F
            value, offset = payload_byte(payload, offset, branch)
            if repeat == 0:
                raise PixelDecodeError(
                    "run length must be positive",
                    control_offset,
                    branch,
                )
            remaining = total - len(indexes)
            if repeat > remaining:
                raise PixelDecodeError(
                    f"run of {repeat} pixels exceeds remaining output size {remaining}",
                    control_offset,
                    branch,
                )
            for _ in range(repeat):
                emit(value)

        if offset != len(payload):
            raise PixelDecodeError(
                f"pixel payload has {len(payload) - offset} trailing byte(s)",
                offset,
                branch,
            )
    elif pixel_code == 0x1600:
        byte_count = (total + (2 if total & 1 else 0)) >> 1
        for _ in range(byte_count):
            value, offset = payload_byte(payload, offset, branch)
            emit((value >> 4) & 0x0F)
            emit(value & 0x0F)
    elif pixel_code == 0x0400:
        byte_count = (total + (4 if total & 3 else 0)) >> 2
        for _ in range(byte_count):
            value, offset = payload_byte(payload, offset, branch)
            for shift in (6, 4, 2, 0):
                emit((value >> shift) & 0x03)
    elif pixel_code == 0x0200:
        byte_count = (total + (8 if total & 7 else 0)) >> 3
        for _ in range(byte_count):
            value, offset = payload_byte(payload, offset, branch)
            for shift in (7, 6, 5, 4, 3, 2, 1, 0):
                emit((value >> shift) & 0x01)
    elif pixel_code == 0x5602:
        for _ in range(total):
            value, offset = payload_byte(payload, offset, branch)
            emit(value)
    else:
        raise PixelDecodeError(
            f"pixel code 0x{pixel_code:04x} has no proven decoder branch",
            0,
            "pixel_code",
        )

    if len(indexes) != total:
        raise PixelDecodeError(
            f"decoded {len(indexes)} pixels, expected {total}", offset, branch
        )
    return {
        "indexes": indexes,
        "alpha": alpha,
        "bytes_consumed": offset,
        "payload_size": len(payload),
        "trailing_bytes": len(payload) - offset,
        "produced_slots_before_crop": produced_slots,
        "pixel_count": total,
    }


def png_chunk(chunk_type: bytes, data: bytes) -> bytes:
    crc = binascii.crc32(chunk_type + data) & 0xFFFFFFFF
    return struct.pack(">I", len(data)) + chunk_type + data + struct.pack(">I", crc)


def encode_rgba_png(
    width: int,
    height: int,
    indexes: list[int],
    palette: list[int],
    alpha: list[int] | None,
) -> bytes:
    scanlines = bytearray()
    position = 0
    for _ in range(height):
        scanlines.append(0)
        for _ in range(width):
            color = palette[indexes[position]]
            scanlines.extend(
                (
                    (color >> 16) & 0xFF,
                    (color >> 8) & 0xFF,
                    color & 0xFF,
                    alpha[position] if alpha is not None else (color >> 24) & 0xFF,
                )
            )
            position += 1
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    return (
        b"\x89PNG\r\n\x1a\n"
        + png_chunk(b"IHDR", ihdr)
        + png_chunk(b"IDAT", zlib.compress(bytes(scanlines), 9))
        + png_chunk(b"IEND", b"")
    )


def validate_png(png: bytes, expected_width: int, expected_height: int) -> None:
    if not png.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError("invalid PNG signature")
    offset = 8
    idat = bytearray()
    dimensions: tuple[int, int] | None = None
    saw_iend = False
    while offset < len(png):
        if offset + 12 > len(png):
            raise ValueError("truncated PNG chunk")
        length = struct.unpack_from(">I", png, offset)[0]
        chunk_type = png[offset + 4 : offset + 8]
        end = offset + 12 + length
        if end > len(png):
            raise ValueError("PNG chunk exceeds EOF")
        chunk_data = png[offset + 8 : offset + 8 + length]
        stored_crc = struct.unpack_from(">I", png, offset + 8 + length)[0]
        actual_crc = binascii.crc32(chunk_type + chunk_data) & 0xFFFFFFFF
        if stored_crc != actual_crc:
            raise ValueError(f"CRC mismatch in {chunk_type!r}")
        if chunk_type == b"IHDR":
            dimensions = struct.unpack_from(">II", chunk_data)[0:2]
        elif chunk_type == b"IDAT":
            idat.extend(chunk_data)
        elif chunk_type == b"IEND":
            saw_iend = True
            offset = end
            break
        offset = end
    if not saw_iend or offset != len(png):
        raise ValueError("missing IEND or trailing PNG bytes")
    if dimensions != (expected_width, expected_height):
        raise ValueError(f"unexpected PNG dimensions {dimensions}")
    raw = zlib.decompress(bytes(idat))
    expected_size = expected_height * (1 + expected_width * 4)
    if len(raw) != expected_size:
        raise ValueError(
            f"unexpected decompressed PNG size {len(raw)} != {expected_size}"
        )
    stride = 1 + expected_width * 4
    if any(raw[row * stride] != 0 for row in range(expected_height)):
        raise ValueError("unexpected PNG scanline filter")


def generate_module_pngs(
    parsed: ParsedSprite,
    output_directory: Path,
    metadata_only: bool,
    static_recovery_authorized: bool,
) -> dict[str, Any]:
    palette_section = next(
        (
            section
            for section in parsed.metadata["sections"]
            if section["id"] == "palette_pixel_code_and_aB_payloads"
        ),
        None,
    )
    results: list[dict[str, Any]] = []
    pngs: list[dict[str, Any]] = []
    limitations: list[dict[str, Any]] = []
    if palette_section is None:
        optional_pixel_tail_absent = any(
            limitation.get("kind") == "palette_and_aB_section_absent_at_eof"
            for limitation in parsed.metadata["limitations"]
        )
        for module in parsed.modules:
            if module["runtime_type_aU"] != 0:
                reconstruction = {
                    "status": "not-applicable-runtime-non-pixel-module"
                }
            elif optional_pixel_tail_absent:
                reconstruction = {
                    "status": "runtime-nonrendering-optional-pixel-tail-absent",
                    "structure": "complete",
                    "load_valid": True,
                    "runtime_image_behavior": "nonrendering",
                    "pixels_synthesized": False,
                }
            else:
                reconstruction = {
                    "status": "unsupported-no-palette-payload-section"
                }
            module["pixel_reconstruction"] = reconstruction
        return {
            "module_results": results,
            "pngs": pngs,
            "limitations": limitations,
        }

    pixel_code = palette_section["pixel_code"]["value"]
    palette_size = palette_section["palette_size_effective"]
    for module, payload in zip(parsed.modules, parsed.payloads):
        runtime_type = module["runtime_type_aU"]
        width = module["ae_width"]
        height = module["af_height"]
        if runtime_type != 0:
            module["pixel_reconstruction"] = {
                "status": "not-applicable-runtime-non-pixel-module",
                "runtime_type_aU": runtime_type,
            }
            continue
        if width == 0 or height == 0:
            module["pixel_reconstruction"] = {
                "status": "not-applicable-zero-dimension",
                "width": width,
                "height": height,
            }
            continue
        if not parsed.palettes:
            limitation = {
                "kind": "module_has_no_decoded_palette",
                "module_index": module["index"],
                "offset": module["aB_payload"]["data_range"]["start"],
            }
            limitations.append(limitation)
            module["pixel_reconstruction"] = {"status": "unsupported", **limitation}
            continue
        if pixel_code not in PIXEL_BRANCHES:
            limitation = {
                "kind": "module_uses_unsupported_pixel_code",
                "module_index": module["index"],
                "offset": palette_section["pixel_code"]["offset"],
                "value": f"0x{pixel_code:04x}",
            }
            limitations.append(limitation)
            module["pixel_reconstruction"] = {"status": "unsupported", **limitation}
            continue
        if pixel_code in STATIC_RECOVERY_PIXEL_BRANCHES and not static_recovery_authorized:
            limitation = {
                "kind": "data_derived_recovery_outside_pinned_evidence",
                "module_index": module["index"],
                "offset": palette_section["pixel_code"]["offset"],
                "value": f"0x{pixel_code:04x}",
                "effect": "raw payload preserved without inferred image output",
            }
            limitations.append(limitation)
            module["pixel_reconstruction"] = {"status": "unsupported", **limitation}
            continue

        try:
            decoded = decode_pixel_indexes(
                payload, width, height, pixel_code, palette_size
            )
        except PixelDecodeError as error:
            source_offset = (
                module["aB_payload"]["data_range"]["start"] + error.offset
            )
            limitation = {
                "kind": "module_pixel_decode_error",
                "module_index": module["index"],
                "offset": source_offset,
                "branch": error.branch,
                "detail": str(error),
            }
            limitations.append(limitation)
            module["pixel_reconstruction"] = {"status": "error", **limitation}
            continue

        reconstruction = {
            "status": "decoded" if not metadata_only else "decoded-metadata-only",
            "pixel_code": f"0x{pixel_code:04x}",
            **PIXEL_BRANCH_EVIDENCE[pixel_code],
            "width": width,
            "height": height,
            "pixel_count": decoded["pixel_count"],
            "bytes_consumed": decoded["bytes_consumed"],
            "payload_size": decoded["payload_size"],
            "trailing_bytes": decoded["trailing_bytes"],
            "produced_slots_before_crop": decoded["produced_slots_before_crop"],
            "palette_variants": len(parsed.palettes),
            "png_count": 0,
        }
        if pixel_code == 0x27F1:
            reconstruction["pixel_index_sha256"] = hashlib.sha256(
                bytes(decoded["indexes"])
            ).hexdigest()
            reconstruction["pinned_corpus_match"] = True
        module["pixel_reconstruction"] = reconstruction
        result = {"module_index": module["index"], **reconstruction}
        results.append(result)

        if decoded["trailing_bytes"] != 0:
            limitation = {
                "kind": "module_payload_has_unconsumed_bytes",
                "module_index": module["index"],
                "offset": (
                    module["aB_payload"]["data_range"]["start"]
                    + decoded["bytes_consumed"]
                ),
                "size": decoded["trailing_bytes"],
            }
            limitations.append(limitation)
        if metadata_only:
            continue

        for palette_index, palette in enumerate(parsed.palettes):
            png = encode_rgba_png(
                width,
                height,
                decoded["indexes"],
                palette,
                decoded["alpha"],
            )
            validate_png(png, width, height)
            filename = (
                f"module-{module['index']:04d}-palette-{palette_index:02d}-"
                f"{width}x{height}.png"
            )
            path = output_directory / filename
            path.write_bytes(png)
            png_record = {
                "file": filename,
                "module_index": module["index"],
                "palette_index": palette_index,
                "width": width,
                "height": height,
                "size": len(png),
                "sha256": hashlib.sha256(png).hexdigest(),
                "crc_validation": "passed",
            }
            pngs.append(png_record)
            reconstruction["png_count"] += 1
            result["png_count"] += 1

    return {
        "module_results": results,
        "pngs": pngs,
        "limitations": limitations,
    }


def numeric_pack_key(path: Path) -> tuple[int, str]:
    suffix = path.parent.name.removeprefix("pack-")
    return (int(suffix), path.as_posix()) if suffix.isdigit() else (sys.maxsize, path.as_posix())


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


def discover_assets(decoded_root: Path) -> list[dict[str, Any]]:
    assets: list[dict[str, Any]] = []
    destinations: set[tuple[str, str]] = set()
    resolved_sources: set[Path] = set()
    total_metadata_bytes = 0
    total_metadata_entries = 0
    metadata_paths: list[Path] = []
    for metadata_path in decoded_root.glob("pack-*/metadata.json"):
        metadata_paths.append(metadata_path)
        enforce_run_limit(
            "metadata files", len(metadata_paths), MAX_METADATA_FILES
        )
    metadata_paths.sort(key=numeric_pack_key)
    enforce_run_limit("metadata files", len(metadata_paths), MAX_METADATA_FILES)
    for metadata_path in metadata_paths:
        pack_directory = metadata_path.parent
        if pack_directory.is_symlink() or not pack_directory.is_dir():
            raise ValueError(
                f"pack directory must be a regular directory: {pack_directory}"
            )
        if pack_directory.resolve().parent != decoded_root:
            raise ValueError(f"pack directory escapes decoded root: {pack_directory}")
        if metadata_path.is_symlink() or not metadata_path.is_file():
            raise ValueError(f"pack metadata must be a regular file: {metadata_path}")
        metadata_bytes = read_bounded_regular_file(
            metadata_path,
            MAX_METADATA_BYTES_PER_FILE,
            f"metadata in {metadata_path.parent.name}",
        )
        total_metadata_bytes += len(metadata_bytes)
        enforce_run_limit(
            "total metadata bytes", total_metadata_bytes, MAX_TOTAL_METADATA_BYTES
        )
        try:
            metadata_text = metadata_bytes.decode("utf-8")
        except UnicodeDecodeError as error:
            raise ValueError(f"pack metadata is not UTF-8: {metadata_path}") from error
        pack_metadata = json.loads(metadata_text)
        if not isinstance(pack_metadata, dict):
            raise ValueError(f"pack metadata must be an object: {metadata_path}")
        pack_value = pack_metadata.get("pack")
        if not isinstance(pack_value, (str, int)) or isinstance(pack_value, bool):
            raise ValueError(f"invalid metadata pack value in {metadata_path}")
        pack = str(pack_value)
        directory_pack = metadata_path.parent.name.removeprefix("pack-")
        if not pack.isdigit() or pack != directory_pack:
            raise ValueError(
                f"metadata pack {pack!r} does not match directory {directory_pack!r}"
            )
        entries = pack_metadata.get("entries")
        if not isinstance(entries, list):
            raise ValueError(f"metadata entries must be a list: {metadata_path}")
        enforce_run_limit(
            f"metadata entries in pack {pack}",
            len(entries),
            MAX_METADATA_ENTRIES_PER_PACK,
        )
        total_metadata_entries += len(entries)
        enforce_run_limit(
            "total metadata entries",
            total_metadata_entries,
            MAX_TOTAL_METADATA_ENTRIES,
        )
        sprite_entries: list[dict[str, Any]] = []
        sprite_indexes: set[int] = set()
        for entry in entries:
            if not isinstance(entry, dict):
                raise ValueError(f"metadata entry must be an object: {metadata_path}")
            if entry.get("detected_type") != SPRITE_TYPE:
                continue
            index = entry.get("index")
            marker = entry.get("marker")
            if (
                not isinstance(index, int)
                or isinstance(index, bool)
                or index < 0
                or not isinstance(marker, int)
                or isinstance(marker, bool)
                or marker < 0
            ):
                raise ValueError(f"sprite entry has invalid index/marker in {metadata_path}")
            if index in sprite_indexes:
                raise ValueError(f"duplicate sprite entry index {pack}/{index}")
            sprite_indexes.add(index)
            filename = entry.get("file")
            expected_filename = f"entry-{index:03d}-marker-{marker:03d}.bin"
            if not isinstance(filename, str) or filename != expected_filename:
                raise ValueError(
                    f"sprite entry {pack}/{index} filename must be {expected_filename!r}"
                )
            match = ENTRY_FILE_PATTERN.fullmatch(filename)
            if match is None:
                raise ValueError(f"sprite entry filename is outside extractor schema: {filename}")
            destination = (pack, Path(filename).stem)
            if destination in destinations:
                raise ValueError(f"duplicate sprite output destination: {pack}/{filename}")
            destinations.add(destination)
            source_path = metadata_path.parent / filename
            if source_path.is_symlink() or not source_path.is_file():
                raise ValueError(f"decoded sprite entry must be a regular file: {source_path}")
            source_resolved = source_path.resolve()
            if source_resolved.parent != metadata_path.parent.resolve():
                raise ValueError(f"unsafe entry path: {filename}")
            if source_resolved in resolved_sources:
                raise ValueError(f"duplicate decoded sprite source: {source_path}")
            resolved_sources.add(source_resolved)
            sprite_entries.append(entry)

        for entry in sorted(sprite_entries, key=lambda item: item["index"]):
            filename = entry["file"]
            source_path = metadata_path.parent / filename
            assets.append(
                {
                    "pack": pack,
                    "entry": entry,
                    "source_path": source_path,
                    "source_relative_path": source_path.relative_to(decoded_root).as_posix(),
                    "metadata_relative_path": metadata_path.relative_to(decoded_root).as_posix(),
                }
            )
    return assets


def source_provenance(
    asset: dict[str, Any], data: bytes | None = None
) -> dict[str, Any]:
    entry = asset["entry"]
    actual_hash = hashlib.sha256(data).hexdigest() if data is not None else None
    actual_size = len(data) if data is not None else None
    return {
        "pack": asset["pack"],
        "entry_index": entry["index"],
        "part": entry.get("part"),
        "pack_offset": entry.get("offset"),
        "packed_size": entry.get("packed_size"),
        "marker": entry.get("marker"),
        "marker_value": entry.get("marker_value"),
        "compression": entry.get("compression"),
        "detected_type": entry.get("detected_type"),
        "metadata_file": asset["metadata_relative_path"],
        "decoded_file": asset["source_relative_path"],
        "decoded_size_recorded": entry.get("decoded_size"),
        "decoded_size_actual": actual_size,
        "sha256_recorded": entry.get("sha256"),
        "sha256_actual": actual_hash,
        "size_matches": (
            entry.get("decoded_size") == actual_size if data is not None else False
        ),
        "sha256_matches": (
            entry.get("sha256") == actual_hash if data is not None else False
        ),
    }


def pinned_static_recovery_matches(
    asset: dict[str, Any], parsed: ParsedSprite, data: bytes
) -> bool:
    """Authorize the empirical branch only for the two payloads that prove it."""

    palette_section = next(
        (
            section
            for section in parsed.metadata["sections"]
            if section["id"] == "palette_pixel_code_and_aB_payloads"
        ),
        None,
    )
    if (
        palette_section is None
        or palette_section["pixel_code"]["value"] != 0x27F1
    ):
        return True
    expected_modules = PINNED_0X27F1_RECOVERY["modules"]
    if (
        asset["pack"] != PINNED_0X27F1_RECOVERY["pack"]
        or asset["entry"]["index"] != PINNED_0X27F1_RECOVERY["entry_index"]
        or hashlib.sha256(data).hexdigest()
        != PINNED_0X27F1_RECOVERY["source_sha256"]
        or len(parsed.palettes) != PINNED_0X27F1_RECOVERY["palette_count"]
        or palette_section["palette_size_effective"]
        != PINNED_0X27F1_RECOVERY["palette_size"]
        or len(parsed.modules) != len(expected_modules)
        or len(parsed.payloads) != len(expected_modules)
    ):
        return False
    return all(
        module["runtime_type_aU"] == 0
        and module["ae_width"] == expected["width"]
        and module["af_height"] == expected["height"]
        and hashlib.sha256(payload).hexdigest() == expected["payload_sha256"]
        for module, payload, expected in zip(
            parsed.modules, parsed.payloads, expected_modules
        )
    )


def zlib_compress_bound(size: int) -> int:
    """Return zlib's documented conservative compressed-size bound."""

    return size + (size >> 12) + (size >> 14) + (size >> 25) + 13


def enforce_run_limit(name: str, observed: int, limit: int) -> None:
    if observed > limit:
        raise ValueError(f"run limit exceeded: {name} {observed:,} > {limit:,}")


def preflight_run(
    assets: list[dict[str, Any]], metadata_only: bool
) -> dict[str, Any]:
    """Validate every source and bound aggregate work before staging any output."""

    enforce_run_limit("assets", len(assets), MAX_ASSETS)
    source_sizes = [asset["source_path"].stat().st_size for asset in assets]
    input_bytes = sum(source_sizes)
    enforce_run_limit("input bytes", input_bytes, MAX_INPUT_BYTES)

    module_count = 0
    section_records = 0
    decoded_pixels = 0
    png_variants = 0
    estimated_png_bytes = 0

    for asset in assets:
        source_path: Path = asset["source_path"]
        if source_path.is_symlink() or not source_path.is_file():
            raise ValueError(f"decoded sprite entry is no longer regular: {source_path}")
        if source_path.resolve().parent != source_path.parent.resolve():
            raise ValueError(f"decoded sprite entry moved outside its pack: {source_path}")
        data = read_bounded_regular_file(
            source_path, MAX_INPUT_BYTES, "decoded sprite entry"
        )
        provenance = source_provenance(asset, data)
        if not provenance["size_matches"] or not provenance["sha256_matches"]:
            raise ValueError(
                "decoded input does not match pack metadata during preflight: "
                f"pack {asset['pack']} entry {asset['entry']['index']}"
            )

        parsed = parse_sprite(data)
        module_count += len(parsed.modules)
        section_records += sum(
            int(section.get("count", 0)) for section in parsed.metadata["sections"]
        )
        enforce_run_limit("modules", module_count, MAX_TOTAL_MODULES)
        enforce_run_limit(
            "section records", section_records, MAX_TOTAL_SECTION_RECORDS
        )

        palette_section = next(
            (
                section
                for section in parsed.metadata["sections"]
                if section["id"] == "palette_pixel_code_and_aB_payloads"
            ),
            None,
        )
        if palette_section is None or not parsed.palettes:
            continue
        pixel_code = palette_section["pixel_code"]["value"]
        if pixel_code not in PIXEL_BRANCHES:
            continue
        if (
            pixel_code in STATIC_RECOVERY_PIXEL_BRANCHES
            and not pinned_static_recovery_matches(asset, parsed, data)
        ):
            continue

        for module in parsed.modules:
            if module["runtime_type_aU"] != 0:
                continue
            width = module["ae_width"]
            height = module["af_height"]
            if width == 0 or height == 0:
                continue
            pixel_count = width * height
            enforce_run_limit(
                "pixels in one module", pixel_count, RUNTIME_PIXEL_BUFFER_CAPACITY
            )
            decoded_pixels += pixel_count
            enforce_run_limit(
                "decoded pixel work", decoded_pixels, MAX_TOTAL_DECODED_PIXELS
            )
            if metadata_only:
                continue

            palette_variants = len(parsed.palettes)
            png_variants += palette_variants
            enforce_run_limit("PNG variants", png_variants, MAX_PNG_VARIANTS)
            raw_scanline_bytes = height * (1 + width * 4)
            estimated_png_bytes += palette_variants * (
                57 + zlib_compress_bound(raw_scanline_bytes)
            )
            enforce_run_limit(
                "estimated PNG bytes",
                estimated_png_bytes,
                MAX_ESTIMATED_PNG_BYTES,
            )

    return {
        "status": "passed",
        "limits": {
            "assets": MAX_ASSETS,
            "input_bytes": MAX_INPUT_BYTES,
            "metadata_files": MAX_METADATA_FILES,
            "metadata_bytes_per_file": MAX_METADATA_BYTES_PER_FILE,
            "total_metadata_bytes": MAX_TOTAL_METADATA_BYTES,
            "metadata_entries_per_pack": MAX_METADATA_ENTRIES_PER_PACK,
            "total_metadata_entries": MAX_TOTAL_METADATA_ENTRIES,
            "modules": MAX_TOTAL_MODULES,
            "section_records": MAX_TOTAL_SECTION_RECORDS,
            "decoded_pixel_work": MAX_TOTAL_DECODED_PIXELS,
            "png_variants": MAX_PNG_VARIANTS,
            "estimated_png_bytes": MAX_ESTIMATED_PNG_BYTES,
            "runtime_pixel_buffer_per_module": RUNTIME_PIXEL_BUFFER_CAPACITY,
        },
        "observed": {
            "assets": len(assets),
            "input_bytes": input_bytes,
            "modules": module_count,
            "section_records": section_records,
            "decoded_pixel_work": decoded_pixels,
            "png_variants": png_variants,
            "estimated_png_bytes": estimated_png_bytes,
        },
        "estimated_png_bytes_basis": (
            "PNG fixed chunks plus zlib compressBound over RGBA filter-0 scanlines"
        ),
    }


def asset_output_directory(output_root: Path, asset: dict[str, Any]) -> Path:
    stem = Path(asset["entry"]["file"]).stem
    return output_root / f"pack-{asset['pack']}" / stem


def write_json(path: Path, value: Any) -> None:
    path.write_text(
        json.dumps(value, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def portable_decoded_root_label(path: Path) -> str:
    """Return a CWD-independent label without serializing a host home path."""

    resolved = path.resolve()
    if (
        resolved.name == "decoded"
        and resolved.parent.name == "resources"
        and resolved.parent.parent.name == "reconstructed-project"
    ):
        return "reconstructed-project/resources/decoded"
    digest = hashlib.sha256(resolved.as_posix().encode("utf-8")).hexdigest()
    return f"resolved-path-sha256:{digest}"


def managed_artifact_manifest(output_root: Path) -> dict[str, Any]:
    """Hash the deterministic decoder-owned artifact set, excluding summary.json."""

    paths = sorted(
        (
            path
            for path in output_root.glob("pack-*/entry-*-marker-*/*")
            if path.is_file() and (path.name == "metadata.json" or path.suffix == ".png")
        ),
        key=lambda path: path.relative_to(output_root).as_posix(),
    )
    aggregate = hashlib.sha256()
    total_bytes = 0
    for path in paths:
        relative = path.relative_to(output_root).as_posix()
        data = path.read_bytes()
        digest = hashlib.sha256(data).hexdigest()
        aggregate.update(relative.encode("utf-8"))
        aggregate.update(b"\0")
        aggregate.update(digest.encode("ascii"))
        aggregate.update(b"\n")
        total_bytes += len(data)
    return {
        "file_count": len(paths),
        "total_bytes": total_bytes,
        "sha256": aggregate.hexdigest(),
    }


def validate_existing_output_root(output_root: Path) -> None:
    """Require an exclusively decoder-owned tree before transactional replacement."""

    if output_root.is_symlink() or not output_root.is_dir():
        raise ValueError(f"output root must be a regular directory: {output_root}")
    entries = list(output_root.iterdir())
    if not entries:
        return
    summary_path = output_root / "summary.json"
    if summary_path.is_symlink() or not summary_path.is_file():
        raise ValueError(
            "nonempty output root lacks a regular sprite-decoder summary.json marker"
        )
    try:
        summary = json.loads(summary_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"invalid existing sprite-decoder summary: {error}") from error
    if (
        summary.get("schema_version") != 1
        or summary.get("input_contract") != OUTPUT_CONTRACT
    ):
        raise ValueError("existing output root is not owned by this decoder schema")

    for path in output_root.rglob("*"):
        relative = path.relative_to(output_root)
        parts = relative.parts
        if path.is_symlink():
            raise ValueError(f"refusing symlinked managed output: {path}")
        valid = False
        if len(parts) == 1:
            valid = (
                (path.is_file() and parts[0] == "summary.json")
                or (path.is_dir() and OUTPUT_PACK_PATTERN.fullmatch(parts[0]) is not None)
            )
        elif len(parts) == 2:
            valid = (
                path.is_dir()
                and OUTPUT_PACK_PATTERN.fullmatch(parts[0]) is not None
                and OUTPUT_ENTRY_PATTERN.fullmatch(parts[1]) is not None
            )
        elif len(parts) == 3:
            valid = (
                path.is_file()
                and OUTPUT_PACK_PATTERN.fullmatch(parts[0]) is not None
                and OUTPUT_ENTRY_PATTERN.fullmatch(parts[1]) is not None
                and (
                    parts[2] == "metadata.json"
                    or OUTPUT_PNG_PATTERN.fullmatch(parts[2]) is not None
                )
            )
        if not valid:
            raise ValueError(f"output root contains an unmanaged path: {path}")


def acquire_output_lock(output_root: Path) -> Path:
    """Refuse concurrent writers using an atomic sibling-directory lock."""

    lock_path = output_root.parent / f".{output_root.name}.sprite-decoder.lock"
    try:
        lock_path.mkdir()
    except FileExistsError as error:
        raise ValueError(
            f"output lock already exists (another writer or stale lock): {lock_path}"
        ) from error
    return lock_path


def acquire_reconstruction_lock(root: Path) -> Path:
    lock_path = root / ".static-reconstruction.lock"
    try:
        lock_path.mkdir()
    except FileExistsError as error:
        raise ValueError(
            f"another static reconstruction operation holds {lock_path}"
        ) from error

    def release() -> None:
        try:
            lock_path.rmdir()
        except OSError:
            pass

    atexit.register(release)
    return lock_path


def publish_staged_output(staging_root: Path, output_root: Path) -> str | None:
    """Publish via backup/replace and return a non-fatal cleanup warning."""

    backup_root = output_root.parent / (
        f".{output_root.name}.sprite-decoder-backup-{os.getpid()}"
    )
    if backup_root.exists() or backup_root.is_symlink():
        raise ValueError(f"refusing existing transaction backup path: {backup_root}")

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
            return f"could not remove committed-output backup {backup_root}: {cleanup_error}"
    return None


def process_asset(
    asset: dict[str, Any], output_root: Path, metadata_only: bool
) -> dict[str, Any]:
    output_directory = asset_output_directory(output_root, asset)
    source_path: Path = asset["source_path"]
    result: dict[str, Any] = {
        "schema_version": 1,
        "static_only": True,
        "source": source_provenance(asset),
        "status": "error",
    }

    try:
        output_directory.mkdir(parents=True, exist_ok=True)
        if source_path.is_symlink() or not source_path.is_file():
            raise ValueError(f"decoded sprite entry is no longer a regular file: {source_path}")
        if source_path.resolve().parent != source_path.parent.resolve():
            raise ValueError(f"decoded sprite entry moved outside its pack: {source_path}")
        data = read_bounded_regular_file(
            source_path, MAX_INPUT_BYTES, "decoded sprite entry"
        )
        provenance = source_provenance(asset, data)
        result["source"] = provenance
        if not provenance["size_matches"] or not provenance["sha256_matches"]:
            result["provenance_error"] = (
                "decoded input does not match pack metadata; parsing and PNG output skipped"
            )
            write_json(output_directory / "metadata.json", result)
            return result
        parsed = parse_sprite(data)
        result.update(parsed.metadata)
        static_recovery_authorized = pinned_static_recovery_matches(
            asset, parsed, data
        )
        palette_section = next(
            (
                section
                for section in result["sections"]
                if section["id"] == "palette_pixel_code_and_aB_payloads"
            ),
            None,
        )
        if (
            palette_section is not None
            and palette_section["pixel_code"]["value"]
            in STATIC_RECOVERY_PIXEL_BRANCHES
        ):
            result["static_recovery_authorization"] = {
                "scope": "exact-pinned-corpus-only",
                "matched": static_recovery_authorized,
                "pack": asset["pack"],
                "entry_index": asset["entry"]["index"],
            }
            if not static_recovery_authorized:
                palette_section["pixel_code"].update(
                    {
                        "classification": "candidate-outside-pinned-evidence",
                        "confidence": "unsupported",
                        "evidence_basis": "no-exact-pinned-corpus-match",
                    }
                )
        generated = generate_module_pngs(
            parsed,
            output_directory,
            metadata_only,
            static_recovery_authorized,
        )
        result["module_pixel_results"] = generated["module_results"]
        result["generated_pngs"] = generated["pngs"]
        result["limitations"].extend(generated["limitations"])
        if any(
            limitation.get("kind") == "module_pixel_decode_error"
            for limitation in result["limitations"]
        ):
            result["status"] = "error"
        elif result["limitations"]:
            result["status"] = "partial"
        else:
            result["status"] = "success"
    except SpriteDecodeError as error:
        result["parse_error"] = {
            "detail": str(error),
            "offset": error.offset,
            "offset_hex": f"0x{error.offset:08x}",
            "branch": error.branch,
        }
    except (OSError, struct.error, ValueError, zlib.error) as error:
        result["parse_error"] = {
            "detail": str(error),
            "offset": None,
            "branch": "host_validation",
        }

    write_json(output_directory / "metadata.json", result)
    return result


def counter_dict(counter: Counter[Any], hexadecimal: bool = False) -> dict[str, int]:
    if hexadecimal:
        return {
            f"0x{int(key):08x}": counter[key]
            for key in sorted(counter, key=int)
        }
    return {str(key): counter[key] for key in sorted(counter, key=str)}


def build_summary(
    decoded_root: Path,
    assets: list[dict[str, Any]],
    results: list[dict[str, Any]],
    metadata_only: bool,
    preflight: dict[str, Any],
    artifact_manifest: dict[str, Any],
    rerun_verification: dict[str, Any],
) -> dict[str, Any]:
    status_counts = Counter(result["status"] for result in results)
    pack_counts = Counter(asset["pack"] for asset in assets)
    flag_counts: Counter[int] = Counter()
    tag_counts: Counter[int] = Counter()
    palette_formats: Counter[int] = Counter()
    pixel_codes: Counter[int] = Counter()
    section_record_totals: Counter[str] = Counter()
    png_count = 0
    png_bytes = 0
    decoded_module_count = 0
    module_status_counts: Counter[str] = Counter()
    unsupported: list[dict[str, Any]] = []
    exact_eof = 0
    input_bytes = 0
    module_count = 0
    provenance_matches = 0

    for asset, result in zip(assets, results):
        source = result.get("source", {})
        actual_size = source.get("decoded_size_actual")
        if isinstance(actual_size, int):
            input_bytes += actual_size
        if source.get("size_matches") and source.get("sha256_matches"):
            provenance_matches += 1
        if "parse_error" in result:
            unsupported.append(
                {
                    "pack": asset["pack"],
                    "entry_index": asset["entry"]["index"],
                    **result["parse_error"],
                }
            )
        if "provenance_error" in result:
            unsupported.append(
                {
                    "pack": asset["pack"],
                    "entry_index": asset["entry"]["index"],
                    "kind": "provenance_mismatch",
                    "detail": result["provenance_error"],
                }
            )
        if "header" not in result:
            continue
        exact_eof += int(result["consumed_to_eof"])
        flags = result["header"]["flags_aD"]
        flag_counts[flags] += 1
        module_count += result["header"]["module_count_ab"]
        module_section = next(
            section for section in result["sections"] if section["id"] == "modules"
        )
        tag_counts.update(record["tag"] for record in module_section["records"])
        module_status_counts.update(
            record.get("pixel_reconstruction", {}).get("status", "not-classified")
            for record in module_section["records"]
        )
        for section in result["sections"]:
            if "count" in section:
                section_record_totals[section["id"]] += section["count"]
        palette_section = next(
            (
                section
                for section in result["sections"]
                if section["id"] == "palette_pixel_code_and_aB_payloads"
            ),
            None,
        )
        if palette_section is not None:
            palette_formats[palette_section["palette_format"]["value"]] += 1
            pixel_codes[palette_section["pixel_code"]["value"]] += 1
        decoded_module_count += len(result.get("module_pixel_results", []))
        png_count += len(result.get("generated_pngs", []))
        png_bytes += sum(item["size"] for item in result.get("generated_pngs", []))
        for limitation in result.get("limitations", []):
            unsupported.append(
                {
                    "pack": asset["pack"],
                    "entry_index": asset["entry"]["index"],
                    **limitation,
                }
            )

    return {
        "schema_version": 1,
        "static_only": True,
        "input_contract": OUTPUT_CONTRACT,
        "decoded_root": portable_decoded_root_label(decoded_root),
        "metadata_only": metadata_only,
        "preflight": preflight,
        "publication": {
            "model": "sibling lock, staged tree, backup rename, staged rename",
            "ordinary_failure_recovery": "prior output restored when the staged rename fails",
            "hard_interruption_limit": (
                "a process kill or power loss between the two renames can leave the "
                "live path absent and recoverable backup/stage/lock siblings"
            ),
            "stale_lock_policy": "refuse automatic recovery to avoid overwriting an active writer",
        },
        "coverage": {
            "full_assets": status_counts["success"],
            "partial_assets": status_counts["partial"],
            "error_assets": status_counts["error"],
            "interpretation": (
                "Full means all pixel-bearing modules used either one of seven "
                "runtime-proven branches or an explicitly labeled high-confidence "
                "data-derived static recovery. Partial includes a structurally "
                "complete/load-valid runtime-nonrendering asset whose optional pixel "
                "tail is absent. Error includes malformed supported pixel payloads, "
                "provenance failures, and parse failures."
            ),
        },
        "inventory": {
            "discovered_sprite_binaries": len(assets),
            "processed": len(results),
            "success": status_counts["success"],
            "partial": status_counts["partial"],
            "error": status_counts["error"],
            "consumed_to_eof": exact_eof,
            "provenance_size_and_sha256_match": provenance_matches,
            "input_bytes": input_bytes,
            "module_count": module_count,
        },
        "source_pack_counts": counter_dict(pack_counts),
        "flags_aD_counts": counter_dict(flag_counts, hexadecimal=True),
        "module_tag_counts": counter_dict(tag_counts),
        "section_record_totals": counter_dict(section_record_totals),
        "palette_format_counts": {
            f"0x{key:04x}": palette_formats[key] for key in sorted(palette_formats)
        },
        "pixel_code_counts": {
            f"0x{key:04x}": pixel_codes[key] for key in sorted(pixel_codes)
        },
        "pixel_reconstruction": {
            "decoded_pixel_modules": decoded_module_count,
            "not_decoded_modules": module_count - decoded_module_count,
            "module_status_counts": counter_dict(module_status_counts),
            "png_count": png_count,
            "png_bytes": png_bytes,
            "png_crc_validation": "passed" if png_count else "not-applicable",
        },
        "determinism": {
            "ordering": "numeric pack/entry/module/palette; JSON keys sorted",
            "managed_artifact_manifest": artifact_manifest,
            "rerun_verification": rerun_verification,
        },
        "unsupported_or_error_locations": unsupported,
        "evidence": EVIDENCE,
    }


def main() -> int:
    parser = argparse.ArgumentParser(
        description=__doc__,
        epilog=(
            "Hard interruption during the two-rename publication window can leave "
            "recoverable backup/stage/lock siblings. Confirm no decoder is active, "
            "restore the backup if the live output is absent, then remove the stale lock."
        ),
    )
    parser.add_argument(
        "decoded_root",
        type=Path,
        help="decoded resource root containing pack-*/metadata.json and .bin entries",
    )
    parser.add_argument(
        "output_root",
        type=Path,
        help=(
            "exclusive decoder-owned directory for per-asset JSON and supported "
            "module PNGs"
        ),
    )
    parser.add_argument(
        "--metadata-only",
        action="store_true",
        help="parse and validate pixels but do not write PNG files",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="return nonzero when any asset is partial as well as on errors",
    )
    parser.add_argument(
        "--verify-rerun",
        action="store_true",
        help=(
            "compare the pre-existing managed artifact manifest with this run; "
            "requires a prior output in the same directory"
        ),
    )
    args = parser.parse_args()

    decoded_root = args.decoded_root.resolve()
    unresolved_output_root = Path(os.path.abspath(args.output_root))
    if unresolved_output_root.is_symlink():
        parser.error(f"output root must not be a symlink: {unresolved_output_root}")
    output_root = unresolved_output_root.resolve()
    unsafe_output_roots = {
        Path(output_root.anchor),
        Path.cwd().resolve(),
        Path.home().resolve(),
    }
    if output_root in unsafe_output_roots or output_root.name != "sprites-decoded":
        parser.error(f"unsafe sprite-decoder output root: {output_root}")
    if not decoded_root.is_dir():
        parser.error(f"decoded root is not a directory: {decoded_root}")
    if output_root.parent.name != "resources" or decoded_root != output_root.parent / "decoded":
        parser.error("sprite input/output must be decoded and sprites-decoded siblings")
    if (
        decoded_root == output_root
        or decoded_root in output_root.parents
        or output_root in decoded_root.parents
    ):
        parser.error("decoded input and output roots must not overlap")

    reconstruction_root = output_root.parent.parent
    prior_report = reconstruction_root / "verification-report.json"
    if prior_report.exists() or prior_report.is_symlink():
        if prior_report.is_dir() and not prior_report.is_symlink():
            parser.error(f"verification report path is a directory: {prior_report}")
        prior_report.unlink()
    try:
        acquire_reconstruction_lock(reconstruction_root)
    except ValueError as error:
        parser.error(str(error))

    try:
        assets = discover_assets(decoded_root)
    except (OSError, KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        parser.error(f"could not discover sprite inputs: {error}")
    if not assets:
        parser.error("no Gameloft sprite binaries were discovered")

    output_root.parent.mkdir(parents=True, exist_ok=True)
    try:
        lock_path = acquire_output_lock(output_root)
    except ValueError as error:
        parser.error(str(error))

    staging_root: Path | None = None
    try:
        if output_root.exists():
            try:
                validate_existing_output_root(output_root)
            except ValueError as error:
                parser.error(str(error))
        before_manifest = managed_artifact_manifest(output_root)
        if args.verify_rerun and before_manifest["file_count"] == 0:
            parser.error("--verify-rerun requires a prior managed output set")
        try:
            preflight = preflight_run(assets, args.metadata_only)
        except (OSError, KeyError, TypeError, ValueError) as error:
            parser.error(f"preflight failed before output generation: {error}")

        staging_root = Path(
            tempfile.mkdtemp(
                prefix=f".{output_root.name}.sprite-decoder-stage-",
                dir=output_root.parent,
            )
        )
        results = [
            process_asset(asset, staging_root, args.metadata_only) for asset in assets
        ]
        after_manifest = managed_artifact_manifest(staging_root)
        if args.verify_rerun:
            rerun_status = "passed" if before_manifest == after_manifest else "failed"
            rerun_verification = {
                "requested": True,
                "status": rerun_status,
                "before": before_manifest,
                "after": after_manifest,
            }
        else:
            rerun_status = "not-requested"
            rerun_verification = {
                "requested": False,
                "status": rerun_status,
            }
        summary = build_summary(
            decoded_root,
            assets,
            results,
            args.metadata_only,
            preflight,
            after_manifest,
            rerun_verification,
        )
        write_json(staging_root / "summary.json", summary)
        staging_root.chmod(0o755)
        validate_existing_output_root(staging_root)

        errors = summary["inventory"]["error"]
        partial = summary["inventory"]["partial"]
        if errors == 0 and rerun_status != "failed":
            publication_warning = publish_staged_output(staging_root, output_root)
            if publication_warning is not None:
                print(f"warning: {publication_warning}", file=sys.stderr)
        print(json.dumps(summary, indent=2, ensure_ascii=False, sort_keys=True))

        if errors or rerun_status == "failed" or (args.strict and partial):
            return 1
        return 0
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
        try:
            lock_path.rmdir()
        except OSError as cleanup_error:
            print(
                f"warning: could not remove output lock {lock_path}: {cleanup_error}",
                file=sys.stderr,
            )


if __name__ == "__main__":
    raise SystemExit(main())
