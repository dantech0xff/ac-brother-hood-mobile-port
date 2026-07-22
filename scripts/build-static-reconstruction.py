#!/usr/bin/env python3
"""Build a static-only reconstruction package from previously decompiled data.

This script never launches the MIDlet. It treats the JAR as a ZIP/class input,
copies all three JADX source views, and asks `javap` to disassemble class files.
"""

from __future__ import annotations

import argparse
import atexit
import hashlib
import json
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile
from collections import Counter
from pathlib import Path


CLASS_SUMMARY = re.compile(
    r"interfaces:\s*(?P<interfaces>\d+),\s*fields:\s*(?P<fields>\d+),\s*"
    r"methods:\s*(?P<methods>\d+),\s*attributes:\s*(?P<attributes>\d+)"
)
EXPECTED_JAR_SHA256 = (
    "711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383"
)
EXPECTED_CLASS_NAMES = {
    "GloftASBR",
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
}
MANAGED_DIRECTORY_PATHS = (
    Path("src/structured"),
    Path("src/simple"),
    Path("src/fallback"),
    Path("bytecode"),
    Path("resources/archive"),
    Path("resources/decoded"),
)
MANAGED_MANIFEST_PATH = Path("reconstruction-manifest.json")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def line_count(path: Path) -> int:
    with path.open("r", encoding="utf-8", errors="replace") as stream:
        return sum(1 for _ in stream)


def atomic_write_json(path: Path, value: object) -> None:
    """Replace JSON without following an existing file symlink or leaving partial data."""

    path.parent.mkdir(parents=True, exist_ok=True)
    temporary_path: Path | None = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="w",
            encoding="utf-8",
            dir=path.parent,
            prefix=f".{path.name}.",
            suffix=".tmp",
            delete=False,
        ) as stream:
            temporary_path = Path(stream.name)
            json.dump(value, stream, indent=2, ensure_ascii=False)
            stream.write("\n")
        temporary_path.chmod(0o644)
        temporary_path.replace(path)
    finally:
        if temporary_path is not None:
            temporary_path.unlink(missing_ok=True)


def acquire_directory_lock(root: Path, name: str) -> Path:
    lock = root / name
    try:
        lock.mkdir()
    except FileExistsError as error:
        raise SystemExit(f"Another static reconstruction operation holds {lock}") from error

    def release() -> None:
        try:
            lock.rmdir()
        except OSError:
            pass

    atexit.register(release)
    return lock


def class_code_bytes(data: bytes) -> int:
    """Return the sum of JVM Code attribute byte lengths in one class file."""

    offset = 0

    def u1() -> int:
        nonlocal offset
        value = data[offset]
        offset += 1
        return value

    def u2() -> int:
        nonlocal offset
        value = struct.unpack_from(">H", data, offset)[0]
        offset += 2
        return value

    def u4() -> int:
        nonlocal offset
        value = struct.unpack_from(">I", data, offset)[0]
        offset += 4
        return value

    if u4() != 0xCAFEBABE:
        raise ValueError("invalid class magic")
    u2()  # minor
    u2()  # major
    constant_pool_count = u2()
    utf8: dict[int, str] = {}
    index = 1
    while index < constant_pool_count:
        tag = u1()
        if tag == 1:
            length = u2()
            utf8[index] = data[offset : offset + length].decode(
                "utf-8", errors="replace"
            )
            offset += length
        elif tag in {3, 4}:
            offset += 4
        elif tag in {5, 6}:
            offset += 8
            index += 1
        elif tag in {7, 8, 16, 19, 20}:
            offset += 2
        elif tag in {9, 10, 11, 12, 17, 18}:
            offset += 4
        elif tag == 15:
            offset += 3
        else:
            raise ValueError(f"unsupported constant-pool tag {tag}")
        index += 1

    offset += 6  # access_flags, this_class, super_class
    interface_count = u2()
    offset += interface_count * 2

    def skip_members(collect_code: bool) -> int:
        nonlocal offset
        total = 0
        member_count = u2()
        for _ in range(member_count):
            offset += 6  # access_flags, name_index, descriptor_index
            attribute_count = u2()
            for _ in range(attribute_count):
                name_index = u2()
                attribute_length = u4()
                attribute_start = offset
                if collect_code and utf8.get(name_index) == "Code":
                    offset += 4  # max_stack, max_locals
                    total += u4()
                offset = attribute_start + attribute_length
        return total

    skip_members(False)  # fields
    code_bytes = skip_members(True)  # methods
    return code_bytes


def run_javap(javap: str, jar: Path, class_name: str) -> str:
    command = [
        javap,
        "-classpath",
        str(jar),
        "-c",
        "-p",
        "-s",
        "-constants",
        "-l",
        "-verbose",
        class_name,
    ]
    completed = subprocess.run(command, check=True, capture_output=True, text=True)
    lines = completed.stdout.splitlines()
    if lines and lines[0].startswith("Classfile "):
        # javap embeds the absolute input path in its first line. Normalize that
        # presentation-only line so disassembly hashes are workspace-independent.
        lines[0] = f"Classfile {jar.name}!/{class_name}.class"
    return "\n".join(lines) + "\n"


def copy_sources(source_dir: Path, output_dir: Path) -> list[Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    copied: list[Path] = []
    for source in sorted(source_dir.glob("*.java")):
        target = output_dir / source.name
        shutil.copy2(source, target)
        copied.append(target)
    return copied


def validate_managed_output_path(
    output_root: Path, relative_path: Path, *, directory: bool
) -> None:
    """Reject unsafe live targets before any builder-owned artifact is published."""

    target = output_root / relative_path
    cursor = output_root
    for component in relative_path.parts[:-1]:
        cursor = cursor / component
        if cursor.is_symlink():
            raise SystemExit(f"Refusing symlinked managed-output parent: {cursor}")
        if cursor.exists() and not cursor.is_dir():
            raise SystemExit(f"Managed-output parent must be a directory: {cursor}")
    if target.is_symlink():
        raise SystemExit(f"Refusing symlinked managed output: {target}")
    if target.exists() and (
        (directory and not target.is_dir())
        or (not directory and not target.is_file())
    ):
        expected_kind = "directory" if directory else "file"
        raise SystemExit(f"Managed output must be a regular {expected_kind}: {target}")
    resolved_root = output_root.resolve()
    resolved_target = target.resolve()
    if resolved_target == resolved_root or resolved_root not in resolved_target.parents:
        raise SystemExit(f"Refusing unsafe managed output path: {resolved_target}")


def publish_staged_outputs(stage_root: Path, output_root: Path) -> str | None:
    """Publish all builder-owned artifacts together, rolling back on failure.

    The old manifest is moved out first and the new manifest is installed last.
    An interrupted publication therefore cannot make mixed managed trees appear
    to be a complete package behind a canonical manifest.
    """

    for relative_path in MANAGED_DIRECTORY_PATHS:
        staged_path = stage_root / relative_path
        if staged_path.is_symlink() or not staged_path.is_dir():
            raise SystemExit(f"Missing staged managed directory: {staged_path}")
        validate_managed_output_path(output_root, relative_path, directory=True)
    staged_manifest = stage_root / MANAGED_MANIFEST_PATH
    if staged_manifest.is_symlink() or not staged_manifest.is_file():
        raise SystemExit(f"Missing staged reconstruction manifest: {staged_manifest}")
    validate_managed_output_path(
        output_root, MANAGED_MANIFEST_PATH, directory=False
    )

    backup_root = Path(
        tempfile.mkdtemp(
            prefix=f".{output_root.name}.backup-", dir=output_root.parent
        )
    )
    moved_previous: list[Path] = []
    installed: list[Path] = []
    backup_order = (MANAGED_MANIFEST_PATH, *MANAGED_DIRECTORY_PATHS)
    install_order = (*MANAGED_DIRECTORY_PATHS, MANAGED_MANIFEST_PATH)
    try:
        for relative_path in backup_order:
            live_path = output_root / relative_path
            if not live_path.exists():
                continue
            backup_path = backup_root / relative_path
            backup_path.parent.mkdir(parents=True, exist_ok=True)
            live_path.rename(backup_path)
            moved_previous.append(relative_path)
        for relative_path in install_order:
            staged_path = stage_root / relative_path
            live_path = output_root / relative_path
            live_path.parent.mkdir(parents=True, exist_ok=True)
            staged_path.rename(live_path)
            installed.append(relative_path)
    except OSError as publication_error:
        rollback_errors: list[str] = []
        for relative_path in reversed(installed):
            live_path = output_root / relative_path
            staged_path = stage_root / relative_path
            try:
                staged_path.parent.mkdir(parents=True, exist_ok=True)
                live_path.rename(staged_path)
            except OSError as error:
                rollback_errors.append(f"remove new {relative_path}: {error}")
        for relative_path in reversed(moved_previous):
            backup_path = backup_root / relative_path
            live_path = output_root / relative_path
            try:
                if live_path.exists() or live_path.is_symlink():
                    raise OSError(f"rollback target still exists: {live_path}")
                live_path.parent.mkdir(parents=True, exist_ok=True)
                backup_path.rename(live_path)
            except OSError as error:
                rollback_errors.append(f"restore prior {relative_path}: {error}")
        if rollback_errors:
            raise RuntimeError(
                "Staged publication failed and rollback was incomplete; "
                f"prior-output backups remain at {backup_root}: "
                + "; ".join(rollback_errors)
            ) from publication_error
        try:
            shutil.rmtree(backup_root)
        except OSError:
            pass
        raise

    try:
        shutil.rmtree(backup_root)
    except OSError as error:
        return f"could not remove committed-output backup {backup_root}: {error}"
    return None


def resource_summary(decoded_root: Path) -> dict[str, object]:
    pack_metadata = [
        json.loads(path.read_text(encoding="utf-8"))
        for path in sorted(decoded_root.glob("pack-*/metadata.json"))
    ]
    entries = [entry for pack in pack_metadata for entry in pack["entries"]]
    nonempty = [entry for entry in entries if "file" in entry]
    semantic_types = Counter(
        entry.get("semantic_type", "missing semantic classification")
        for entry in nonempty
    )
    semantic_confidence = Counter(
        entry.get("semantic_confidence", "missing") for entry in nonempty
    )
    return {
        "pack_count": len(pack_metadata),
        "indexed_entries": len(entries),
        "nonempty_entries": len(nonempty),
        "empty_entries": len(entries) - len(nonempty),
        "compression": dict(sorted(Counter(entry["compression"] for entry in nonempty).items())),
        "detected_types": dict(
            sorted(Counter(entry["detected_type"] for entry in nonempty).items())
        ),
        "semantic_types": dict(sorted(semantic_types.items())),
        "semantic_confidence": dict(sorted(semantic_confidence.items())),
        "unclassified_semantic_entries": sum(
            count
            for semantic_type, count in semantic_types.items()
            if str(semantic_type).startswith("unclassified ")
            or semantic_type == "missing semantic classification"
        ),
        "decoded_bytes": sum(entry["decoded_size"] for entry in nonempty),
        "string_count": sum(entry.get("string_count", 0) for entry in nonempty),
        "sprite_module_count": sum(
            entry.get("sprite_module_count", 0) for entry in nonempty
        ),
    }


def build_staged_package(
    jar: Path,
    structured_source: Path,
    simple_source: Path,
    fallback_source: Path,
    decoded_resources: Path,
    stage_root: Path,
    javap: str,
) -> dict[str, object]:
    """Build and validate every builder-owned artifact under one staging root."""

    structured_output = stage_root / "src" / "structured"
    simple_output = stage_root / "src" / "simple"
    fallback_output = stage_root / "src" / "fallback"
    bytecode_output = stage_root / "bytecode"
    archive_output = stage_root / "resources" / "archive"
    decoded_output = stage_root / "resources" / "decoded"

    structured_files = copy_sources(structured_source, structured_output)
    simple_files = copy_sources(simple_source, simple_output)
    fallback_files = copy_sources(fallback_source, fallback_output)
    bytecode_output.mkdir(parents=True, exist_ok=False)
    archive_output.mkdir(parents=True, exist_ok=False)
    shutil.copytree(decoded_resources, decoded_output)
    decoded_output.chmod(0o755)

    with zipfile.ZipFile(jar) as archive:
        jar_entries = []
        class_names = []
        class_data: dict[str, bytes] = {}
        for entry in archive.infolist():
            jar_entries.append(
                {
                    "name": entry.filename,
                    "is_directory": entry.is_dir(),
                    "uncompressed_size": entry.file_size,
                    "compressed_size": entry.compress_size,
                    "crc32": f"{entry.CRC:08x}",
                    "compression_method": entry.compress_type,
                }
            )
            if not entry.is_dir():
                target = (archive_output / entry.filename).resolve()
                if archive_output.resolve() not in target.parents:
                    raise SystemExit(f"Unsafe JAR entry path: {entry.filename}")
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(archive.read(entry))
            if entry.filename.endswith(".class") and "/" not in entry.filename:
                class_name = entry.filename.removesuffix(".class")
                class_names.append(class_name)
                class_data[class_name] = archive.read(entry)

    classes = []
    if set(class_names) != EXPECTED_CLASS_NAMES or len(class_names) != len(
        EXPECTED_CLASS_NAMES
    ):
        raise SystemExit("Root class names do not match the pinned assignment inventory")
    for class_name in sorted(class_names):
        disassembly = run_javap(javap, jar, class_name)
        bytecode_file = bytecode_output / f"{class_name}.javap.txt"
        bytecode_file.write_text(disassembly, encoding="utf-8")
        match = CLASS_SUMMARY.search(disassembly)
        if not match:
            raise SystemExit(f"Could not parse javap class summary for {class_name}")
        structured_file = structured_output / f"{class_name}.java"
        simple_file = simple_output / f"{class_name}.java"
        fallback_file = fallback_output / f"{class_name}.java"
        classes.append(
            {
                "original_name": class_name,
                "fields": int(match.group("fields")),
                "methods": int(match.group("methods")),
                "interfaces": int(match.group("interfaces")),
                "classfile_sha256": hashlib.sha256(class_data[class_name]).hexdigest(),
                "code_bytes": class_code_bytes(class_data[class_name]),
                "structured_source": str(structured_file.relative_to(stage_root)),
                "structured_lines": line_count(structured_file),
                "structured_sha256": sha256(structured_file),
                "structured_not_decompiled_stubs": structured_file.read_text(
                    encoding="utf-8", errors="replace"
                ).count("Method not decompiled:"),
                "simple_source": str(simple_file.relative_to(stage_root)),
                "simple_lines": line_count(simple_file),
                "simple_sha256": sha256(simple_file),
                "simple_not_decompiled_stubs": simple_file.read_text(
                    encoding="utf-8", errors="replace"
                ).count("Method not decompiled:"),
                "fallback_source": str(fallback_file.relative_to(stage_root)),
                "fallback_lines": line_count(fallback_file),
                "fallback_sha256": sha256(fallback_file),
                "bytecode": str(bytecode_file.relative_to(stage_root)),
                "bytecode_sha256": sha256(bytecode_file),
            }
        )

    if (
        len(structured_files) != len(class_names)
        or len(simple_files) != len(class_names)
        or len(fallback_files) != len(class_names)
    ):
        raise SystemExit("Structured/simple/fallback class counts do not match the JAR")

    manifest = {
        "schema_version": 1,
        "analysis_mode": "static-only",
        "game_execution_performed": False,
        "original": {
            "file": jar.name,
            "size": jar.stat().st_size,
            "sha256": sha256(jar),
            "jar_entry_count": len(jar_entries),
            "entries": jar_entries,
        },
        "code": {
            "class_count": len(classes),
            "field_count": sum(item["fields"] for item in classes),
            "method_count": sum(item["methods"] for item in classes),
            "code_bytes": sum(item["code_bytes"] for item in classes),
            "structured_lines": sum(item["structured_lines"] for item in classes),
            "fallback_lines": sum(item["fallback_lines"] for item in classes),
            "structured_not_decompiled_stubs": sum(
                item["structured_not_decompiled_stubs"] for item in classes
            ),
            "simple_lines": sum(item["simple_lines"] for item in classes),
            "simple_not_decompiled_stubs": sum(
                item["simple_not_decompiled_stubs"] for item in classes
            ),
            "classes": classes,
        },
        "resources": resource_summary(decoded_resources),
    }
    atomic_write_json(stage_root / MANAGED_MANIFEST_PATH, manifest)
    return manifest


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("analysis_root", type=Path)
    parser.add_argument("output_root", type=Path)
    parser.add_argument("--javap", default="javap")
    args = parser.parse_args()

    jar = args.jar.resolve()
    analysis_root = args.analysis_root.resolve()
    unresolved_output_root = Path(os.path.abspath(args.output_root))
    if unresolved_output_root.is_symlink():
        raise SystemExit(f"Refusing symlinked output-root path: {unresolved_output_root}")
    output_root = unresolved_output_root.resolve()
    unsafe_output_roots = {
        Path(output_root.anchor),
        Path.cwd().resolve(),
        Path.home().resolve(),
    }
    if output_root in unsafe_output_roots:
        raise SystemExit(f"Refusing unsafe output root: {output_root}")
    if output_root.name != "reconstructed-project":
        raise SystemExit("Builder output root must use the reconstructed-project leaf")
    if output_root == jar or output_root in jar.parents:
        raise SystemExit("Builder output must not replace the JAR or an ancestor")
    if output_root == analysis_root or output_root in analysis_root.parents:
        raise SystemExit("Builder output must not replace the analysis input or an ancestor")
    structured_source = analysis_root / "jadx-original" / "sources" / "defpackage"
    simple_source = analysis_root / "jadx-simple" / "sources" / "defpackage"
    fallback_source = analysis_root / "jadx-fallback" / "sources" / "defpackage"
    decoded_resources = analysis_root / "resources-decoded"

    required = [
        jar,
        structured_source,
        simple_source,
        fallback_source,
        decoded_resources,
    ]
    missing = [str(path) for path in required if not path.exists()]
    if missing:
        raise SystemExit(f"Missing required static-analysis inputs: {', '.join(missing)}")
    if sha256(jar) != EXPECTED_JAR_SHA256:
        raise SystemExit("Refusing a JAR that does not match the pinned assignment SHA-256")
    acquire_directory_lock(analysis_root, ".static-resource-analysis.lock")
    decoded_report_path = decoded_resources / "summary.json"
    if not decoded_report_path.is_file():
        raise SystemExit(f"Missing decoded resource summary: {decoded_report_path}")
    decoded_report = json.loads(decoded_report_path.read_text(encoding="utf-8"))
    if decoded_report.get("skipped") or decoded_report.get("decode_errors"):
        raise SystemExit("Decoded resource summary contains skipped packs or decode errors")
    if decoded_report.get("classification", {}).get("unclassified_nonempty_entries") != 0:
        raise SystemExit("Decoded resource summary contains unclassified nonempty entries")
    expected_source_names = {f"{name}.java" for name in EXPECTED_CLASS_NAMES}
    for source_directory in (structured_source, simple_source, fallback_source):
        actual_source_names = {
            path.name for path in source_directory.iterdir() if path.is_file()
        }
        if actual_source_names != expected_source_names:
            raise SystemExit(
                f"Decompiler source set differs from pinned classes: {source_directory}"
            )

    output_root.mkdir(parents=True, exist_ok=True)
    atomic_write_json(
        output_root / "verification-report.json",
        {
            "ok": False,
            "checks": {
                "analysis_mode": "static-only",
                "game_execution_performed": False,
            },
            "failures": ["reconstruction was rebuilt and requires verification"],
        },
    )
    acquire_directory_lock(output_root, ".static-reconstruction.lock")
    stage_root = Path(
        tempfile.mkdtemp(
            prefix=f".{output_root.name}.stage-", dir=output_root.parent
        )
    )
    published = False
    try:
        manifest = build_staged_package(
            jar,
            structured_source,
            simple_source,
            fallback_source,
            decoded_resources,
            stage_root,
            args.javap,
        )
        publication_warning = publish_staged_outputs(stage_root, output_root)
        published = True
        if publication_warning is not None:
            print(f"warning: {publication_warning}", file=sys.stderr)
        print(
            json.dumps(
                {
                    key: manifest[key]
                    for key in ("analysis_mode", "code", "resources")
                },
                indent=2,
            )
        )
        return 0
    finally:
        if stage_root.exists():
            try:
                shutil.rmtree(stage_root)
            except OSError as error:
                state = "committed" if published else "unpublished"
                print(
                    f"warning: could not remove {state} staging directory "
                    f"{stage_root}: {error}",
                    file=sys.stderr,
                )


if __name__ == "__main__":
    raise SystemExit(main())
