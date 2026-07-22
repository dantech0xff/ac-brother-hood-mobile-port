#!/usr/bin/env python3
"""Audit the recovered project without loading or executing any target class."""

from __future__ import annotations

import argparse
import atexit
import hashlib
import json
import os
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile
from collections import Counter
from pathlib import Path


EXPECTED_SHA256 = "711e0b1063725f99439f59579977f5001d0665c9bf9fe7f7615c0e10937aa383"
EXPECTED_SOURCE_MANIFESTS = {
    "structured": {
        "file_count": 12,
        "total_bytes": 1321148,
        "sha256": "ecea8893bdb23d165f32aa6133237080f35f943446e6f9ef2e0aa4c6c80c5dd1",
    },
    "simple": {
        "file_count": 12,
        "total_bytes": 1150450,
        "sha256": "755d8afe0df20680f80d9433b9851a9b28148b4658ab2a0daf51d4ac18b356a2",
    },
    "fallback": {
        "file_count": 12,
        "total_bytes": 3482215,
        "sha256": "d580a917a98e6b5ac51264c28581f6f47f8245703c325dbac3afa64b23bddf2d",
    },
}
EXPECTED_BYTECODE_MANIFEST = {
    "file_count": 12,
    "total_bytes": 5623439,
    "sha256": "f03f1a639358357ec17a8cf4d3eebf6fec16b4d6b05e1ada58f6841b15e07a5e",
}
ALIAS_TOP_LEVEL_KEYS = {
    "schema_version",
    "analysis_mode",
    "aliases_are_original_names",
    "classes",
    "methods",
    "fields",
}
ALIAS_ITEM_KEYS = {"original", "alias", "confidence"}
ALIAS_CONFIDENCE_VALUES = {
    "proven",
    "high-confidence",
    "inferred",
    "unknown",
}
EXPECTED_SEMANTIC_ALIAS_COUNTS = {"classes": 12, "methods": 31, "fields": 41}
EXPECTED_SEMANTIC_ALIAS_SHA256 = (
    "a9a5210ae2630784677341246df2d942e00423e717e2119ab092d06b5f7f4268"
)


def normalized_javap(javap: str, jar: Path, class_name: str) -> str:
    """Regenerate the exact static disassembly format used by the builder."""

    completed = subprocess.run(
        [
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
        ],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        detail = completed.stderr.strip() or completed.stdout.strip()
        raise RuntimeError(f"javap failed for {class_name}: {detail}")
    lines = completed.stdout.splitlines()
    if lines and lines[0].startswith("Classfile "):
        lines[0] = f"Classfile {jar.name}!/{class_name}.class"
    return "\n".join(lines) + "\n"


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def count_lines(path: Path) -> int:
    with path.open("r", encoding="utf-8", errors="replace") as stream:
        return sum(1 for _ in stream)


def class_code_bytes(data: bytes) -> int:
    """Derive total JVM Code-attribute bytes without loading the class."""

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
    u2()
    u2()
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

    offset += 6
    interface_count = u2()
    offset += interface_count * 2

    def skip_members(collect_code: bool) -> int:
        nonlocal offset
        total = 0
        member_count = u2()
        for _ in range(member_count):
            offset += 6
            attribute_count = u2()
            for _ in range(attribute_count):
                name_index = u2()
                attribute_length = u4()
                attribute_start = offset
                if collect_code and utf8.get(name_index) == "Code":
                    offset += 4
                    total += u4()
                offset = attribute_start + attribute_length
        return total

    skip_members(False)
    return skip_members(True)


def managed_sprite_manifest(root: Path) -> dict[str, object]:
    """Hash the deterministic per-asset JSON/PNG set, excluding root summary."""

    paths = sorted(
        (
            path
            for path in root.rglob("*")
            if path.is_file() and path != root / "summary.json"
        ),
        key=lambda path: path.relative_to(root).as_posix(),
    )
    aggregate = hashlib.sha256()
    total_bytes = 0
    for path in paths:
        relative = path.relative_to(root).as_posix()
        data = path.read_bytes()
        digest = sha256_bytes(data)
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


def directory_manifest(root: Path) -> dict[str, object]:
    """Return a path-sensitive aggregate for every regular file in a tree."""

    paths = sorted(
        (path for path in root.rglob("*") if path.is_file()),
        key=lambda path: path.relative_to(root).as_posix(),
    )
    aggregate = hashlib.sha256()
    total_bytes = 0
    for path in paths:
        relative = path.relative_to(root).as_posix()
        data = path.read_bytes()
        digest = sha256_bytes(data)
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


def managed_tree_symlinks(root: Path, protected_roots: list[Path]) -> list[str]:
    """Find symlinks without traversing a symlinked protected-root ancestor."""

    symlinked_paths: set[Path] = set()
    for protected_root in protected_roots:
        current = root
        for part in protected_root.relative_to(root).parts:
            current /= part
            if current.is_symlink():
                symlinked_paths.add(current)
                break
        else:
            if protected_root.exists():
                symlinked_paths.update(
                    path for path in protected_root.rglob("*") if path.is_symlink()
                )
    return [
        str(path)
        for path in sorted(symlinked_paths, key=lambda path: path.as_posix())
    ]


def write_report(path: Path, result: dict[str, object]) -> None:
    """Atomically replace the report so a failed rerun cannot leave stale success."""

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
            json.dump(result, stream, indent=2, ensure_ascii=False)
            stream.write("\n")
        temporary_path.chmod(0o644)
        temporary_path.replace(path)
    finally:
        if temporary_path is not None:
            temporary_path.unlink(missing_ok=True)


def acquire_reconstruction_lock(root: Path) -> Path:
    lock = root / ".static-reconstruction.lock"
    try:
        lock.mkdir()
    except FileExistsError as error:
        raise RuntimeError(f"reconstruction is locked by another operation: {lock}") from error

    def release() -> None:
        try:
            lock.rmdir()
        except OSError:
            pass

    atexit.register(release)
    return lock


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("reconstruction_root", type=Path)
    parser.add_argument("--report", type=Path, help="optional JSON report path")
    parser.add_argument("--javap", default="javap", help="static JDK disassembler")
    args = parser.parse_args()

    jar = args.jar.resolve()
    unresolved_root = Path(os.path.abspath(args.reconstruction_root))
    root = unresolved_root.resolve()
    if not root.is_dir():
        parser.error(f"reconstruction root is not a directory: {root}")
    if args.report is not None:
        unresolved_report = Path(os.path.abspath(args.report))
        if unresolved_report.is_symlink():
            parser.error("report path must not be a symbolic link")
        lexical_internal = (
            unresolved_report == unresolved_root
            or unresolved_root in unresolved_report.parents
        )
        expected_lexical_report = unresolved_root / "verification-report.json"
        if lexical_internal and unresolved_report != expected_lexical_report:
            parser.error(
                "inside the reconstruction root, --report must be verification-report.json"
            )
        report = unresolved_report.parent.resolve() / unresolved_report.name
        allowed_internal_report = root / "verification-report.json"
        if report == jar:
            parser.error("report path must not overwrite the original JAR")
        if (report == root or root in report.parents) and report != allowed_internal_report:
            parser.error(
                "inside the reconstruction root, --report must be verification-report.json"
            )
        if report.exists() and not report.is_file():
            parser.error("report path must be a regular file")
        args.report = report
    incomplete_result: dict[str, object] = {
        "ok": False,
        "checks": {
            "analysis_mode": "static-only",
            "game_execution_performed": False,
        },
        "failures": ["verification did not complete"],
    }
    if args.report is not None:
        write_report(args.report, incomplete_result)
    try:
        acquire_reconstruction_lock(root)
    except RuntimeError as error:
        locked_result: dict[str, object] = {
            "ok": False,
            "checks": incomplete_result["checks"],
            "failures": [str(error)],
        }
        canonical_report = root / "verification-report.json"
        if canonical_report.is_file() and not canonical_report.is_symlink():
            write_report(canonical_report, locked_result)
        if args.report is not None and args.report != canonical_report:
            write_report(args.report, locked_result)
        parser.error(str(error))

    failures: list[str] = []
    checks: dict[str, object] = {
        "analysis_mode": "static-only",
        "game_execution_performed": False,
    }

    def require(condition: bool, message: str) -> None:
        if not condition:
            failures.append(message)

    manifest_path = root / "reconstruction-manifest.json"
    require(jar.is_file(), f"missing JAR: {jar}")
    require(manifest_path.is_file(), f"missing manifest: {manifest_path}")
    require(not manifest_path.is_symlink(), "reconstruction manifest is a symbolic link")
    if failures:
        early_result = {"ok": False, "checks": checks, "failures": failures}
        if args.report is not None:
            write_report(args.report, early_result)
        print(json.dumps(early_result, indent=2))
        return 1

    jar_hash = sha256_file(jar)
    checks["jar_sha256"] = jar_hash
    if jar_hash != EXPECTED_SHA256:
        failures.append("original JAR SHA-256 changed")
        mismatch_result = {"ok": False, "checks": checks, "failures": failures}
        if args.report is not None:
            write_report(args.report, mismatch_result)
        print(json.dumps(mismatch_result, indent=2, ensure_ascii=False))
        return 1

    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    require(manifest.get("schema_version") == 1, "manifest schema version differs")
    protected_roots = [
        root / "src" / "structured",
        root / "src" / "simple",
        root / "src" / "fallback",
        root / "bytecode",
        root / "inventory",
        root / "resources" / "archive",
        root / "resources" / "decoded",
        root / "resources" / "sprites-decoded",
        root / "resources" / "levels-decoded",
    ]
    symlinked_paths = managed_tree_symlinks(root, protected_roots)
    if symlinked_paths:
        failures.append(f"managed artifact tree contains symlinks: {symlinked_paths}")
        symlink_result = {"ok": False, "checks": checks, "failures": failures}
        if args.report is not None:
            write_report(args.report, symlink_result)
        print(json.dumps(symlink_result, indent=2, ensure_ascii=False))
        return 1
    resources_root = root / "resources"
    transaction_searches = (
        (root, ".inventory.stage-*", "."),
        (root, ".inventory.backup-*", "."),
        (resources_root, ".static-resource-analysis.lock", "resources"),
        (resources_root, ".decoded.stage-*", "resources"),
        (resources_root, ".decoded.backup-*", "resources"),
        (resources_root, ".sprites-decoded.sprite-decoder.lock", "resources"),
        (resources_root, ".sprites-decoded.sprite-decoder-stage-*", "resources"),
        (resources_root, ".sprites-decoded.sprite-decoder-backup-*", "resources"),
        (resources_root, ".levels-decoded.level-decoder.lock", "resources"),
        (resources_root, ".levels-decoded.level-decoder-stage-*", "resources"),
        (resources_root, ".levels-decoded.level-decoder-backup-*", "resources"),
        (root.parent, f".{root.name}.stage-*", ".."),
        (root.parent, f".{root.name}.backup-*", ".."),
    )
    stale_transaction_paths = sorted(
        {
            (
                path.relative_to(root).as_posix()
                if path == root or root in path.parents
                else f"../{path.name}"
            )
            for search_root, pattern, _label in transaction_searches
            for path in search_root.glob(pattern)
        }
    )
    checks["decoder_transaction_artifacts"] = {
        "patterns_checked": [
            f"{label}/{pattern}" if label != "." else pattern
            for _search_root, pattern, label in transaction_searches
        ],
        "stale_paths": stale_transaction_paths,
    }
    if stale_transaction_paths:
        failures.append(
            f"stale decoder transaction artifacts remain: {stale_transaction_paths}"
        )
        transaction_result = {"ok": False, "checks": checks, "failures": failures}
        if args.report is not None:
            write_report(args.report, transaction_result)
        print(json.dumps(transaction_result, indent=2, ensure_ascii=False))
        return 1
    require(manifest["original"]["sha256"] == jar_hash, "manifest JAR hash mismatch")
    require(manifest["original"]["file"] == jar.name, "manifest JAR filename mismatch")
    require(manifest["original"]["size"] == jar.stat().st_size, "manifest JAR size mismatch")
    require(manifest["analysis_mode"] == "static-only", "manifest mode is not static-only")
    require(not manifest["game_execution_performed"], "manifest claims game execution")

    manifest_entry_records = manifest["original"]["entries"]
    require(len(manifest_entry_records) == 37, "manifest archive-entry cardinality differs")
    manifest_entry_names = [entry["name"] for entry in manifest_entry_records]
    require(
        len(manifest_entry_names) == len(set(manifest_entry_names)),
        "manifest contains duplicate archive-entry names",
    )
    manifest_entries = {entry["name"]: entry for entry in manifest_entry_records}
    with zipfile.ZipFile(jar) as archive:
        infos = {info.filename: info for info in archive.infolist()}
        checks["jar_entry_count"] = len(infos)
        require(len(infos) == 37, f"expected 37 JAR entries, got {len(infos)}")
        require(
            manifest["original"]["jar_entry_count"] == len(infos),
            "manifest JAR entry count mismatch",
        )
        require(set(infos) == set(manifest_entries), "JAR/manifest entry names differ")
        for name, info in infos.items():
            recorded = manifest_entries.get(name)
            if recorded is None:
                continue
            require(info.file_size == recorded["uncompressed_size"], f"size mismatch: {name}")
            require(info.compress_size == recorded["compressed_size"], f"packed size mismatch: {name}")
            require(f"{info.CRC:08x}" == recorded["crc32"], f"CRC mismatch: {name}")
            if not info.is_dir():
                copied = root / "resources" / "archive" / name
                require(copied.is_file(), f"missing archive copy: {name}")
                if copied.is_file():
                    require(
                        sha256_bytes(archive.read(name)) == sha256_file(copied),
                        f"archive copy differs: {name}",
                    )
        archive_root = root / "resources" / "archive"
        archive_paths = list(archive_root.rglob("*"))
        require(
            not archive_root.is_symlink()
            and not any(path.is_symlink() for path in archive_paths),
            "archive copy contains a symbolic link",
        )
        delivered_archive_files = {
            path.relative_to(archive_root).as_posix()
            for path in archive_paths
            if path.is_file()
        }
        expected_archive_files = {
            name for name, info in infos.items() if not info.is_dir()
        }
        require(
            delivered_archive_files == expected_archive_files,
            "archive copy contains missing or extra regular files",
        )

    class_names = sorted(
        name.removesuffix(".class")
        for name in manifest_entries
        if name.endswith(".class") and "/" not in name
    )
    require(len(class_names) == 12, f"expected 12 classes, got {len(class_names)}")
    manifest_class_records = manifest["code"]["classes"]
    require(len(manifest_class_records) == 12, "manifest class-record cardinality differs")
    manifest_class_names = [item["original_name"] for item in manifest_class_records]
    require(
        len(manifest_class_names) == len(set(manifest_class_names)),
        "manifest contains duplicate class records",
    )
    class_records = {item["original_name"]: item for item in manifest_class_records}
    require(set(class_names) == set(class_records), "class manifest does not match JAR")

    fresh_javap_matches = 0
    actual_code_bytes = 0
    for class_name in class_names:
        record = class_records[class_name]
        classfile = root / "resources" / "archive" / f"{class_name}.class"
        require(classfile.is_file(), f"missing classfile copy: {class_name}")
        if classfile.is_file():
            require(
                sha256_file(classfile) == record["classfile_sha256"],
                f"classfile hash changed: {class_name}",
            )
            try:
                actual_class_code_bytes = class_code_bytes(classfile.read_bytes())
            except (IndexError, struct.error, ValueError) as error:
                require(False, f"cannot parse classfile {class_name}: {error}")
            else:
                actual_code_bytes += actual_class_code_bytes
                require(
                    record["code_bytes"] == actual_class_code_bytes,
                    f"class Code-byte count differs: {class_name}",
                )
        for view in ("structured", "simple", "fallback"):
            expected_relative_path = f"src/{view}/{class_name}.java"
            require(
                record[f"{view}_source"] == expected_relative_path,
                f"noncanonical {view} manifest path: {class_name}",
            )
            path = root / expected_relative_path
            require(path.is_file(), f"missing {view} source: {class_name}")
            if path.is_file():
                require(
                    count_lines(path) == record[f"{view}_lines"],
                    f"{view} line count changed: {class_name}",
                )
                require(
                    sha256_file(path) == record[f"{view}_sha256"],
                    f"{view} source hash changed: {class_name}",
                )
        expected_bytecode_path = f"bytecode/{class_name}.javap.txt"
        require(
            record["bytecode"] == expected_bytecode_path,
            f"noncanonical bytecode manifest path: {class_name}",
        )
        bytecode = root / expected_bytecode_path
        require(bytecode.is_file(), f"missing javap output: {class_name}")
        if bytecode.is_file():
            require(
                sha256_file(bytecode) == record["bytecode_sha256"],
                f"javap hash changed: {class_name}",
            )
            try:
                fresh_disassembly = normalized_javap(args.javap, jar, class_name)
            except RuntimeError as error:
                require(False, str(error))
            else:
                matches = bytecode.read_text(encoding="utf-8") == fresh_disassembly
                require(
                    matches,
                    f"delivered javap differs from fresh JAR disassembly: {class_name}",
                )
                fresh_javap_matches += int(matches)

    structured_text = "".join(
        (root / "src" / "structured" / f"{name}.java").read_text(
            encoding="utf-8", errors="replace"
        )
        for name in class_names
    )
    simple_text = "".join(
        (root / "src" / "simple" / f"{name}.java").read_text(
            encoding="utf-8", errors="replace"
        )
        for name in class_names
    )
    require(structured_text.count("Method not decompiled:") == 1, "structured hard-stub count is not 1")
    require(simple_text.count("Method not decompiled:") == 0, "simple source contains a hard stub")
    require("private void aV()" in simple_text, "simple source does not contain i.aV()")
    source_manifests = {
        view: directory_manifest(root / "src" / view)
        for view in ("structured", "simple", "fallback")
    }
    require(
        source_manifests == EXPECTED_SOURCE_MANIFESTS,
        f"decompiler source manifests differ: {source_manifests}",
    )
    bytecode_manifest = directory_manifest(root / "bytecode")
    require(
        bytecode_manifest == EXPECTED_BYTECODE_MANIFEST,
        f"bytecode manifest differs: {bytecode_manifest}",
    )

    inventory = root / "inventory"
    methods = json.loads((inventory / "methods.json").read_text(encoding="utf-8"))
    fields = json.loads((inventory / "fields.json").read_text(encoding="utf-8"))
    calls = json.loads((inventory / "calls.json").read_text(encoding="utf-8"))
    accesses = json.loads((inventory / "field-accesses.json").read_text(encoding="utf-8"))
    dependencies = json.loads((inventory / "dependencies.json").read_text(encoding="utf-8"))
    string_constants = json.loads((inventory / "string-constants.json").read_text(encoding="utf-8"))
    aliases = json.loads((inventory / "semantic-aliases.json").read_text(encoding="utf-8"))
    canonical_alias_path = Path(__file__).with_name("java-me-semantic-aliases.json")
    require(
        canonical_alias_path.is_file() and not canonical_alias_path.is_symlink(),
        "canonical semantic alias source must be a regular non-symlink file",
    )
    canonical_aliases = json.loads(canonical_alias_path.read_text(encoding="utf-8"))
    normalized_alias_bytes = json.dumps(
        aliases, sort_keys=True, separators=(",", ":"), ensure_ascii=False
    ).encode("utf-8")
    alias_digest = sha256_bytes(normalized_alias_bytes)
    require(aliases == canonical_aliases, "generated semantic aliases differ from source")
    require(
        alias_digest == EXPECTED_SEMANTIC_ALIAS_SHA256,
        f"semantic alias content hash differs: {alias_digest}",
    )
    require(isinstance(aliases, dict), "semantic aliases are not a JSON object")
    if isinstance(aliases, dict):
        require(
            set(aliases) == ALIAS_TOP_LEVEL_KEYS,
            "semantic alias top-level schema differs",
        )
        require(aliases.get("schema_version") == 1, "semantic alias schema version differs")
        require(aliases.get("analysis_mode") == "static-only", "semantic aliases are not static-only")
        require(
            aliases.get("aliases_are_original_names") is False,
            "semantic aliases do not disclaim original names",
        )
        for category in ("classes", "methods", "fields"):
            items = aliases.get(category, [])
            require(isinstance(items, list), f"semantic alias category is not a list: {category}")
            if not isinstance(items, list):
                continue
            require(
                all(isinstance(item, dict) and set(item) == ALIAS_ITEM_KEYS for item in items),
                f"semantic alias item schema differs: {category}",
            )
            require(
                all(
                    isinstance(item, dict)
                    and item.get("confidence") in ALIAS_CONFIDENCE_VALUES
                    for item in items
                ),
                f"semantic alias confidence differs: {category}",
            )
            originals = [item.get("original") for item in items if isinstance(item, dict)]
            names = [item.get("alias") for item in items if isinstance(item, dict)]
            require(len(originals) == len(set(originals)), f"duplicate semantic alias original: {category}")
            require(len(names) == len(set(names)), f"duplicate semantic alias name: {category}")
            require(
                len(items) == EXPECTED_SEMANTIC_ALIAS_COUNTS[category],
                f"semantic alias count differs for {category}: {len(items)}",
            )
    require(len(methods) == 666, f"expected 666 methods, got {len(methods)}")
    require(len({item["id"] for item in methods}) == 666, "method IDs are not unique")
    require(len(fields) == 1009, f"expected 1009 fields, got {len(fields)}")
    require(len(calls) == 7154, f"expected 7154 call sites, got {len(calls)}")
    require(len(accesses) == 25550, f"expected 25550 field accesses, got {len(accesses)}")
    require(len(dependencies) == 29, f"expected 29 external dependencies, got {len(dependencies)}")
    require(len(string_constants) == 124, f"expected 124 string constants, got {len(string_constants)}")
    methods_by_class = Counter(str(item["class"]) for item in methods)
    fields_by_class = Counter(str(item["class"]) for item in fields)
    for class_name in class_names:
        require(
            class_records[class_name]["methods"] == methods_by_class[class_name],
            f"manifest method count differs for {class_name}",
        )
        require(
            class_records[class_name]["fields"] == fields_by_class[class_name],
            f"manifest field count differs for {class_name}",
        )
    derived_code_summary = {
        "class_count": len(class_names),
        "field_count": len(fields),
        "method_count": len(methods),
        "code_bytes": actual_code_bytes,
        "structured_lines": sum(
            count_lines(root / "src" / "structured" / f"{name}.java")
            for name in class_names
        ),
        "fallback_lines": sum(
            count_lines(root / "src" / "fallback" / f"{name}.java")
            for name in class_names
        ),
        "structured_not_decompiled_stubs": structured_text.count(
            "Method not decompiled:"
        ),
        "simple_lines": sum(
            count_lines(root / "src" / "simple" / f"{name}.java")
            for name in class_names
        ),
        "simple_not_decompiled_stubs": simple_text.count("Method not decompiled:"),
    }
    manifest_code_summary = {
        key: value for key, value in manifest["code"].items() if key != "classes"
    }
    require(
        manifest_code_summary == derived_code_summary,
        f"manifest code summary differs: {manifest_code_summary}",
    )
    instruction_count = sum(int(item["instruction_count"]) for item in methods)
    require(
        instruction_count == 114642,
        f"expected 114642 JVM instructions, got {instruction_count}",
    )
    require(
        manifest["code"]["code_bytes"] == 237112,
        f"expected 237112 code bytes, got {manifest['code']['code_bytes']}",
    )
    method_ids = {item["id"] for item in methods}
    field_ids = {item["id"] for item in fields}
    require(
        all(item["original"] in class_names for item in aliases["classes"]),
        "semantic class alias points to a missing class",
    )
    require(
        all(item["original"] in method_ids for item in aliases["methods"]),
        "semantic method alias points to a missing method",
    )
    require(
        all(item["original"] in field_ids for item in aliases["fields"]),
        "semantic field alias points to a missing field",
    )
    checks["code"] = {
        "classes": len(class_names),
        "methods": len(methods),
        "fields": len(fields),
        "instructions": instruction_count,
        "code_bytes": manifest["code"]["code_bytes"],
        "call_sites": len(calls),
        "field_accesses": len(accesses),
        "external_dependencies": len(dependencies),
        "string_constants": len(string_constants),
        "semantic_aliases": {
            "classes": len(aliases["classes"]),
            "methods": len(aliases["methods"]),
            "fields": len(aliases["fields"]),
            "normalized_sha256": alias_digest,
        },
        "source_manifests": source_manifests,
        "bytecode_manifest": bytecode_manifest,
        "bytecode_reproducibility": {
            "fresh_javap_matches": fresh_javap_matches,
            "expected_classes": len(class_names),
            "source": "pinned JAR",
        },
    }

    inventory_script = Path(__file__).with_name("inventory-java-me-bytecode.py")
    with tempfile.TemporaryDirectory(prefix="ac-java-game-inventory-verify-") as temporary:
        fresh_inventory = Path(temporary) / "inventory"
        completed = subprocess.run(
            [
                sys.executable,
                "-B",
                str(inventory_script),
                str(root / "bytecode"),
                str(fresh_inventory),
            ],
            check=False,
            capture_output=True,
            text=True,
        )
        require(
            completed.returncode == 0,
            f"fresh inventory regeneration failed: {completed.stderr.strip()}",
        )
        if completed.returncode == 0:
            require(
                directory_manifest(fresh_inventory) == directory_manifest(inventory),
                "delivered inventory differs from a fresh bytecode-derived inventory",
            )

    decoded = root / "resources" / "decoded"
    decoded_summary = json.loads((decoded / "summary.json").read_text(encoding="utf-8"))
    require(not decoded_summary.get("skipped"), "decoded summary contains skipped packs")
    require(not decoded_summary.get("decode_errors"), "decoded summary contains decode errors")
    require(
        decoded_summary.get("classification", {}).get("unclassified_nonempty_entries") == 0,
        "decoded summary contains unclassified nonempty entries",
    )
    pack_metadata_paths = sorted(decoded.glob("pack-*/metadata.json"))
    require(len(pack_metadata_paths) == 17, f"expected 17 packs, got {len(pack_metadata_paths)}")
    entries: list[dict[str, object]] = []
    for metadata_path in pack_metadata_paths:
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        for entry in metadata["entries"]:
            entries.append(entry)
            if "file" not in entry:
                continue
            require(
                "semantic_type" in entry,
                f"missing semantic classification: {metadata_path.parent.name}/{entry.get('index')}",
            )
            require(
                "semantic_confidence" in entry,
                f"missing semantic confidence: {metadata_path.parent.name}/{entry.get('index')}",
            )
            payload = metadata_path.parent / str(entry["file"])
            require(payload.is_file(), f"missing decoded payload: {payload}")
            if payload.is_file():
                require(payload.stat().st_size == entry["decoded_size"], f"decoded size mismatch: {payload}")
                require(sha256_file(payload) == entry["sha256"], f"decoded hash mismatch: {payload}")
            if "semantic_file" in entry:
                semantic_file = metadata_path.parent / str(entry["semantic_file"])
                require(semantic_file.is_file(), f"missing semantic decode: {semantic_file}")
                if semantic_file.is_file():
                    try:
                        json.loads(semantic_file.read_text(encoding="utf-8"))
                    except (json.JSONDecodeError, UnicodeDecodeError):
                        require(False, f"invalid semantic JSON: {semantic_file}")

    nonempty = [entry for entry in entries if "file" in entry]
    compression = Counter(str(entry["compression"]) for entry in nonempty)
    detected = Counter(str(entry["detected_type"]) for entry in nonempty)
    semantic = Counter(str(entry.get("semantic_type")) for entry in nonempty)
    confidence = Counter(str(entry.get("semantic_confidence")) for entry in nonempty)
    strings = sum(int(entry.get("string_count", 0)) for entry in nonempty)
    modules = sum(int(entry.get("sprite_module_count", 0)) for entry in nonempty)
    resources = {
        "packs": len(pack_metadata_paths),
        "entries": len(entries),
        "nonempty": len(nonempty),
        "empty": len(entries) - len(nonempty),
        "compression": dict(sorted(compression.items())),
        "detected_types": dict(sorted(detected.items())),
        "semantic_types": dict(sorted(semantic.items())),
        "semantic_confidence": dict(sorted(confidence.items())),
        "unclassified_semantic_entries": sum(
            count
            for semantic_type, count in semantic.items()
            if semantic_type.startswith("unclassified ") or semantic_type == "None"
        ),
        "strings": strings,
        "sprite_modules": modules,
    }
    checks["resources"] = resources
    require(resources["entries"] == 260, f"expected 260 resource entries, got {resources['entries']}")
    require(resources["nonempty"] == 238, f"expected 238 nonempty entries, got {resources['nonempty']}")
    require(resources["empty"] == 22, f"expected 22 empty entries, got {resources['empty']}")
    require(compression == Counter({"none": 214, "lzma-alone": 24}), "compression totals differ")
    require(strings == 265, f"expected 265 strings, got {strings}")
    require(modules == 4376, f"expected 4376 sprite modules, got {modules}")
    require(detected["game-specific binary"] == 114, "signature-level generic payload count differs")
    require(
        resources["unclassified_semantic_entries"] == 0,
        "some nonempty resource payloads lack a semantic family",
    )
    require(
        confidence == Counter({"proven": 131, "high": 99, "inferred": 8}),
        f"semantic confidence totals differ: {dict(confidence)}",
    )
    manifest_resource_summary = {
        "pack_count": resources["packs"],
        "indexed_entries": resources["entries"],
        "nonempty_entries": resources["nonempty"],
        "empty_entries": resources["empty"],
        "compression": resources["compression"],
        "detected_types": resources["detected_types"],
        "semantic_types": resources["semantic_types"],
        "semantic_confidence": resources["semantic_confidence"],
        "unclassified_semantic_entries": resources[
            "unclassified_semantic_entries"
        ],
        "decoded_bytes": sum(int(entry["decoded_size"]) for entry in nonempty),
        "string_count": strings,
        "sprite_module_count": modules,
    }
    require(
        manifest["resources"] == manifest_resource_summary,
        f"manifest resource summary differs: {manifest['resources']}",
    )

    font_map = json.loads(
        (decoded / "pack-1" / "entry-002-font-glyph-map.json").read_text(
            encoding="utf-8"
        )
    )
    require(font_map["mapping_count"] == 246, "font map mapping count differs")
    require(font_map["unique_codepoints"] == 241, "font map unique key count differs")
    require(
        font_map["duplicate_codepoints"] == [32, 186, 1059],
        "font map duplicate-key set differs",
    )

    remap_files = sorted(decoded.glob("pack-[45]/entry-*-module-remap.json"))
    remap_tables = [json.loads(path.read_text(encoding="utf-8")) for path in remap_files]
    require(len(remap_tables) == 5, f"expected 5 module-remap tables, got {len(remap_tables)}")
    require(
        sum(int(table["record_count"]) for table in remap_tables) == 307,
        "module-remap record total differs",
    )

    cosine = json.loads(
        (decoded / "pack-16" / "entry-000-cosine-table.json").read_text(
            encoding="utf-8"
        )
    )
    square_root = json.loads(
        (decoded / "pack-16" / "entry-001-sqrt-table.json").read_text(
            encoding="utf-8"
        )
    )
    require(cosine["value_count"] == 65, "cosine table length differs")
    require(square_root["value_count"] == 256, "square-root table length differs")
    require(cosine["formula_verified"], "cosine table formula did not verify")
    require(square_root["formula_verified"], "square-root table formula did not verify")

    igp_metadata = json.loads(
        (decoded / "data-igp-images" / "metadata.json").read_text(encoding="utf-8")
    )
    require(len(igp_metadata) == 29, f"expected 29 IGP PNGs, got {len(igp_metadata)}")
    for item in igp_metadata:
        image = decoded / "data-igp-images" / item["file"]
        require(image.is_file(), f"missing IGP image: {item['file']}")
        if image.is_file():
            require(sha256_file(image) == item["sha256"], f"IGP image hash mismatch: {item['file']}")

    extractor_script = Path(__file__).with_name("extract-java-me-resource-packs.py")
    with tempfile.TemporaryDirectory(prefix="ac-java-game-resource-verify-") as temporary:
        fresh_decoded = Path(temporary) / "decoded"
        completed = subprocess.run(
            [sys.executable, "-B", str(extractor_script), jar.name, str(fresh_decoded)],
            cwd=jar.parent,
            check=False,
            capture_output=True,
            text=True,
        )
        require(
            completed.returncode == 0,
            f"fresh resource extraction failed: {completed.stderr.strip()}",
        )
        if completed.returncode == 0:
            fresh_manifest = directory_manifest(fresh_decoded)
            delivered_manifest = directory_manifest(decoded)
            require(
                fresh_manifest == delivered_manifest,
                "delivered decoded resources differ from a fresh JAR-derived extraction",
            )
            checks["resource_reproducibility"] = {
                "fresh_extraction_matches": fresh_manifest == delivered_manifest,
                "manifest": delivered_manifest,
            }

    sprite_root = root / "resources" / "sprites-decoded"
    sprite_summary_path = sprite_root / "summary.json"
    require(sprite_summary_path.is_file(), "missing static sprite reconstruction summary")
    if sprite_summary_path.is_file():
        sprite_summary = json.loads(sprite_summary_path.read_text(encoding="utf-8"))
        sprite_metadata_paths = sorted(sprite_root.glob("pack-*/entry-*/metadata.json"))
        sprite_png_paths = sorted(sprite_root.glob("pack-*/entry-*/*.png"))
        sprite_status = Counter()
        for metadata_path in sprite_metadata_paths:
            metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
            sprite_status[str(metadata.get("status"))] += 1
            require(metadata.get("static_only") is True, f"sprite metadata is not static-only: {metadata_path}")
            require(metadata.get("consumed_to_eof") is True, f"sprite input did not reach EOF: {metadata_path}")
            source = metadata.get("source", {})
            require(source.get("size_matches") is True, f"sprite source size mismatch: {metadata_path}")
            require(source.get("sha256_matches") is True, f"sprite source hash mismatch: {metadata_path}")

        sprite_manifest = managed_sprite_manifest(sprite_root)
        expected_sprite_manifest = {
            "file_count": 12186,
            "total_bytes": 63940532,
            "sha256": "bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9",
        }
        coverage = sprite_summary.get("coverage", {})
        inventory_summary = sprite_summary.get("inventory", {})
        pixel_summary = sprite_summary.get("pixel_reconstruction", {})
        rerun = sprite_summary.get("determinism", {}).get("rerun_verification", {})
        require(sprite_summary.get("static_only") is True, "sprite summary is not static-only")
        require(
            sprite_summary.get("input_contract")
            == "decoded pack metadata plus extracted .bin entries; no JAR/class input",
            "sprite decoder input contract differs",
        )
        require(len(sprite_metadata_paths) == 84, f"expected 84 sprite metadata files, got {len(sprite_metadata_paths)}")
        require(len(sprite_png_paths) == 12102, f"expected 12102 sprite PNGs, got {len(sprite_png_paths)}")
        require(sprite_status == Counter({"success": 83, "partial": 1}), f"sprite status totals differ: {dict(sprite_status)}")
        require(coverage.get("full_assets") == 83, "sprite full-asset count differs")
        require(coverage.get("partial_assets") == 1, "sprite partial-asset count differs")
        require(coverage.get("error_assets") == 0, "sprite error-asset count differs")
        require(inventory_summary.get("module_count") == 4376, "sprite module count differs")
        require(inventory_summary.get("consumed_to_eof") == 84, "sprite EOF coverage differs")
        require(pixel_summary.get("decoded_pixel_modules") == 4371, "decoded pixel-module count differs")
        require(pixel_summary.get("not_decoded_modules") == 5, "non-output module count differs")
        require(pixel_summary.get("png_count") == 12102, "sprite summary PNG count differs")
        require(pixel_summary.get("png_crc_validation") == "passed", "sprite PNG validation did not pass")
        require(sprite_manifest == expected_sprite_manifest, f"sprite managed manifest differs: {sprite_manifest}")
        require(
            sprite_summary.get("determinism", {}).get("managed_artifact_manifest")
            == expected_sprite_manifest,
            "sprite summary managed manifest differs",
        )
        require(rerun.get("requested") is True, "sprite deterministic rerun was not requested")
        require(rerun.get("status") == "passed", "sprite deterministic rerun did not pass")
        require(rerun.get("before") == rerun.get("after") == expected_sprite_manifest, "sprite rerun manifests differ")

        recovery_evidence = (
            sprite_summary.get("evidence", {})
            .get("pixel_decoder", {})
            .get("data_derived_static_recovery", {})
        )
        require(
            recovery_evidence
            == {
                "classification": "data-derived-static-recovery",
                "confidence": "high",
                "evidence_basis": "empirical-corpus-invariants",
                "pixel_code": "0x27f1",
                "runtime_bytecode_branch": False,
            },
            "0x27f1 sprite recovery evidence classification differs",
        )
        recovered_metadata_path = (
            sprite_root / "pack-3" / "entry-058-marker-003" / "metadata.json"
        )
        recovered_metadata = json.loads(
            recovered_metadata_path.read_text(encoding="utf-8")
        )
        recovered_modules = recovered_metadata.get("module_pixel_results", [])
        expected_index_hashes = [
            "82689ff1bf8c118d797df7a70850240843882b025b2397bfe3deb94f179bc5cb",
            "dcb0e51e2fb55b6baabfef8c7dbe405f17eb36868822ade8773af4903cc291ea",
        ]
        require(
            recovered_metadata.get("static_recovery_authorization")
            == {
                "entry_index": 58,
                "matched": True,
                "pack": "3",
                "scope": "exact-pinned-corpus-only",
            },
            "0x27f1 static recovery is not restricted to its pinned evidence",
        )
        require(
            [item.get("pixel_index_sha256") for item in recovered_modules]
            == expected_index_hashes,
            "0x27f1 decoded index-stream hashes differ",
        )
        require(
            all(
                item.get("runtime_bytecode_branch") is False
                and item.get("pinned_corpus_match") is True
                and item.get("bytes_consumed") == item.get("payload_size")
                and item.get("trailing_bytes") == 0
                for item in recovered_modules
            ),
            "0x27f1 recovery lost its non-runtime or exact-consumption evidence",
        )
        require(
            len(recovered_metadata.get("generated_pngs", [])) == 4,
            "0x27f1 recovery did not emit four palette variants",
        )

        absent_tail_metadata = json.loads(
            (
                sprite_root
                / "pack-3"
                / "entry-006-marker-003"
                / "metadata.json"
            ).read_text(encoding="utf-8")
        )
        require(absent_tail_metadata.get("status") == "partial", "entry 6 sprite status differs")
        absent_tail_limitations = absent_tail_metadata.get("limitations", [])
        require(
            any(
                item.get("kind") == "palette_and_aB_section_absent_at_eof"
                and item.get("runtime_image_behavior") == "nonrendering"
                and item.get("pixels_synthesized") is False
                for item in absent_tail_limitations
            ),
            "entry 6 missing-tail limitation classification differs",
        )

        sprite_decoder = Path(__file__).with_name("decode-gameloft-sprites.py")
        with tempfile.TemporaryDirectory(
            prefix="ac-java-game-sprite-verify-"
        ) as temporary:
            fresh_root = Path(temporary) / "reconstructed-project"
            fresh_resources = fresh_root / "resources"
            fresh_resources.mkdir(parents=True)
            fresh_decoded = fresh_resources / "decoded"
            fresh_sprites = fresh_resources / "sprites-decoded"
            shutil.copytree(decoded, fresh_decoded)
            sprite_regeneration_ok = True
            fresh_sprite_tree_matches = False
            for extra_args in ([], ["--verify-rerun"]):
                completed = subprocess.run(
                    [
                        sys.executable,
                        "-B",
                        str(sprite_decoder),
                        str(fresh_decoded),
                        str(fresh_sprites),
                        *extra_args,
                    ],
                    check=False,
                    capture_output=True,
                    text=True,
                )
                if completed.returncode != 0:
                    sprite_regeneration_ok = False
                    require(
                        False,
                        "fresh sprite regeneration failed: "
                        + (completed.stderr.strip() or completed.stdout.strip()),
                    )
                    break
            if sprite_regeneration_ok:
                fresh_sprite_tree_matches = (
                    directory_manifest(fresh_sprites)
                    == directory_manifest(sprite_root)
                )
                require(
                    fresh_sprite_tree_matches,
                    "delivered sprites differ from a fresh two-pass static regeneration",
                )
        checks["sprites"] = {
            "assets": len(sprite_metadata_paths),
            "status": dict(sorted(sprite_status.items())),
            "modules": inventory_summary.get("module_count"),
            "decoded_pixel_modules": pixel_summary.get("decoded_pixel_modules"),
            "non_output_modules": pixel_summary.get("not_decoded_modules"),
            "pngs": len(sprite_png_paths),
            "managed_manifest": sprite_manifest,
            "rerun_verification": rerun.get("status"),
            "fresh_two_pass_regeneration_matches": fresh_sprite_tree_matches,
        }

    level_root = root / "resources" / "levels-decoded"
    level_summary_path = level_root / "summary.json"
    level_manifest_path = level_root / "manifest.json"
    require(level_summary_path.is_file(), "missing decoded level-record summary")
    require(level_manifest_path.is_file(), "missing decoded level-record manifest")
    if level_summary_path.is_file() and level_manifest_path.is_file():
        level_summary = json.loads(level_summary_path.read_text(encoding="utf-8"))
        level_manifest_data = json.loads(
            level_manifest_path.read_text(encoding="utf-8")
        )
        level_records_paths = sorted(level_root.glob("pack-*/records.json"))
        expected_level_inventory = {
            "entities": 4286,
            "entity_bytes": 103336,
            "events": 2366,
            "groups": 144,
            "instructions": 3705,
            "lanes": 510,
            "payloads": 16,
            "payloads_exact_eof": 16,
            "script_bytes": 27214,
        }
        expected_level_tree = {
            "file_count": 10,
            "total_bytes": 34570387,
            "sha256": "d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60",
        }
        expected_level_input_contract = (
            "decoded pack metadata plus extracted slot-0/slot-7 .bin entries; "
            "no JAR/class input"
        )
        require(level_summary.get("schema_version") == 1, "level summary schema differs")
        require(level_summary.get("static_only") is True, "level summary is not static-only")
        require(
            level_summary.get("input_contract") == expected_level_input_contract,
            "level decoder input contract differs",
        )
        require(
            level_summary.get("inventory") == expected_level_inventory,
            "decoded level-record totals differ",
        )
        require(
            all(value == "passed" for value in level_summary.get("assertions", {}).values()),
            "one or more level summary assertions did not pass",
        )
        require(len(level_records_paths) == 8, "expected eight per-pack level JSON files")
        require(
            directory_manifest(level_root) == expected_level_tree,
            f"decoded level managed tree differs: {directory_manifest(level_root)}",
        )
        require(
            level_manifest_data.get("static_only") is True
            and level_manifest_data.get("self_excluded_from_files") is True
            and level_manifest_data.get("managed_file_count_excluding_manifest") == 9
            and level_manifest_data.get("managed_total_bytes_excluding_manifest")
            == 34568514
            and level_manifest_data.get("managed_aggregate_sha256_excluding_manifest")
            == "8168a556ce52c787d2087d7bfa315fb68b12638f7102a7c731757c466d977c84",
            "decoded level manifest metadata differs",
        )
        require(
            sha256_file(level_manifest_path)
            == "75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648",
            "decoded level manifest file hash differs",
        )

        observed_level_totals = Counter()
        group_meta_matches = 0
        structural_mode_3 = 0
        structural_opcodes_41_44 = 0
        lane_meta_2_locations: list[tuple[int, int, int, int]] = []
        for records_path in level_records_paths:
            records = json.loads(records_path.read_text(encoding="utf-8"))
            require(records.get("static_only") is True, f"level JSON is not static-only: {records_path}")
            sources = records.get("sources", {})
            require(
                len(sources) == 2
                and all(
                    source.get("size_matches") is True
                    and source.get("sha256_matches") is True
                    for source in sources.values()
                ),
                f"level JSON source provenance differs: {records_path}",
            )
            entities = records.get("entities", {})
            scripts = records.get("scripts", {})
            require(entities.get("consumed_to_eof") is True, f"entity stream did not reach EOF: {records_path}")
            require(scripts.get("consumed_to_eof") is True, f"script stream did not reach EOF: {records_path}")
            observed_level_totals["entities"] += int(entities.get("record_count", 0))
            observed_level_totals["groups"] += int(scripts.get("group_count", 0))
            observed_level_totals["lanes"] += int(scripts.get("lane_count", 0))
            observed_level_totals["events"] += int(scripts.get("event_count", 0))
            observed_level_totals["instructions"] += int(scripts.get("instruction_count", 0))
            structural_mode_3 += int(scripts.get("mode_histogram", {}).get("3", 0))
            structural_opcodes_41_44 += sum(
                int(scripts.get("opcode_histogram", {}).get(str(opcode), 0))
                for opcode in range(41, 45)
            )
            pack = int(records["pack"])
            for group in scripts.get("groups", []):
                invariant = group.get("observed_group_meta_invariant", {})
                require(
                    invariant.get("matches") is True
                    and invariant.get("group_meta_u16le")
                    == invariant.get("lane_event_count_sum"),
                    f"group_meta invariant differs: {records_path}",
                )
                group_meta_matches += int(invariant.get("matches") is True)
                for lane in group.get("lanes", []):
                    if lane.get("lane_meta", {}).get("raw_u8") == 2:
                        lane_meta_2_locations.append(
                            (
                                pack,
                                int(group["index"]),
                                int(lane["index"]),
                                int(lane["lane_meta"]["offset"]),
                            )
                        )

        require(
            dict(observed_level_totals)
            == {
                key: expected_level_inventory[key]
                for key in ("entities", "groups", "lanes", "events", "instructions")
            },
            "per-pack level-record totals do not reconcile",
        )
        require(group_meta_matches == 144, "group_meta invariant coverage is not 144/144")
        require(structural_mode_3 == 0, "mode 3 unexpectedly occurs at parsed mode positions")
        require(
            structural_opcodes_41_44 == 0,
            "opcodes 41-44 unexpectedly occur at parsed opcode positions",
        )
        require(
            lane_meta_2_locations == [(9, 6, 5, 0x3F3)],
            f"sole lane_meta=2 location differs: {lane_meta_2_locations}",
        )

        level_decoder = Path(__file__).with_name("decode-gameloft-level-records.py")
        with tempfile.TemporaryDirectory(
            prefix="ac-java-game-level-verify-"
        ) as temporary:
            fresh_root = Path(temporary) / "reconstructed-project"
            fresh_resources = fresh_root / "resources"
            fresh_resources.mkdir(parents=True)
            fresh_decoded = fresh_resources / "decoded"
            fresh_levels = fresh_resources / "levels-decoded"
            shutil.copytree(decoded, fresh_decoded)
            level_regeneration_ok = True
            fresh_level_tree_matches = False
            for extra_args in ([], ["--verify-rerun"]):
                completed = subprocess.run(
                    [
                        sys.executable,
                        "-B",
                        str(level_decoder),
                        str(fresh_decoded),
                        str(fresh_levels),
                        *extra_args,
                    ],
                    check=False,
                    capture_output=True,
                    text=True,
                )
                if completed.returncode != 0:
                    level_regeneration_ok = False
                    require(
                        False,
                        "fresh level regeneration failed: "
                        + (completed.stderr.strip() or completed.stdout.strip()),
                    )
                    break
            if level_regeneration_ok:
                fresh_level_tree_matches = (
                    directory_manifest(fresh_levels)
                    == directory_manifest(level_root)
                )
                require(
                    fresh_level_tree_matches,
                    "delivered levels differ from a fresh two-pass static regeneration",
                )
        checks["levels"] = {
            "inventory": expected_level_inventory,
            "group_meta_invariant": "144/144 passed",
            "mode_3_at_parsed_positions": structural_mode_3,
            "opcodes_41_44_at_parsed_positions": structural_opcodes_41_44,
            "lane_meta_2_locations": lane_meta_2_locations,
            "managed_tree": expected_level_tree,
            "fresh_two_pass_regeneration_matches": fresh_level_tree_matches,
        }

    result = {"ok": not failures, "checks": checks, "failures": failures}
    if args.report is not None:
        write_report(args.report, result)
    print(json.dumps(result, indent=2, ensure_ascii=False))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
