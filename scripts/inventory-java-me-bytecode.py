#!/usr/bin/env python3
"""Create static method, call, field-access, dependency, and string inventories.

Input is the text emitted by ``javap -verbose -c``. The script never loads or
executes the target classes and therefore preserves the static-only boundary.
"""

from __future__ import annotations

import argparse
import atexit
import json
import os
import re
import shutil
import sys
import tempfile
import uuid
from collections import Counter
from pathlib import Path


CLASS_REF = re.compile(r"^\s*#\d+ = Class\s+.*//\s+(.+)$")
STRING_REF = re.compile(r"^\s*#\d+ = String\s+.*//\s?(.*)$")
INVOKE = re.compile(
    r"^\s*(?P<offset>\d+):\s+(?P<opcode>invoke\w+)\s+.*//\s+"
    r"(?:InterfaceMethod|Method)\s+(?P<target>.+)$"
)
FIELD_ACCESS = re.compile(
    r"^\s*(?P<offset>\d+):\s+(?P<opcode>getfield|getstatic|putfield|putstatic)\s+"
    r".*//\s+Field\s+(?P<target>.+)$"
)
INSTRUCTION = re.compile(r"^\s*(?P<offset>\d+):\s+(?P<opcode>[a-z][a-z0-9_]*)\b")
TARGET = re.compile(
    r"^(?:(?P<owner>[^.\s]+)\.)?(?P<name>\"?<[^>]+>\"?|[^:]+):(?P<descriptor>.+)$"
)
JAVA_MODIFIERS = {
    "public",
    "protected",
    "private",
    "abstract",
    "static",
    "final",
    "synchronized",
    "native",
    "strictfp",
}
MANAGED_OUTPUT_NAMES = {
    "summary.json",
    "methods.json",
    "fields.json",
    "calls.json",
    "field-accesses.json",
    "dependencies.json",
    "string-constants.json",
    "semantic-aliases.json",
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


def validate_alias_schema(aliases: object) -> dict[str, object]:
    if not isinstance(aliases, dict) or set(aliases) != ALIAS_TOP_LEVEL_KEYS:
        raise SystemExit("Canonical semantic aliases have an invalid top-level schema")
    if aliases.get("schema_version") != 1:
        raise SystemExit("Canonical semantic alias schema version is not 1")
    if aliases.get("analysis_mode") != "static-only":
        raise SystemExit("Canonical semantic aliases are not static-only")
    if aliases.get("aliases_are_original_names") is not False:
        raise SystemExit("Canonical semantic aliases must disclaim original names")
    for category in ("classes", "methods", "fields"):
        items = aliases.get(category)
        if not isinstance(items, list):
            raise SystemExit(f"Canonical semantic alias category is not a list: {category}")
        originals: set[str] = set()
        names: set[str] = set()
        for item in items:
            if not isinstance(item, dict) or set(item) != ALIAS_ITEM_KEYS:
                raise SystemExit(f"Invalid semantic alias item in {category}")
            original = item.get("original")
            alias = item.get("alias")
            confidence = item.get("confidence")
            if not isinstance(original, str) or not original:
                raise SystemExit(f"Invalid semantic alias original in {category}")
            if not isinstance(alias, str) or not alias:
                raise SystemExit(f"Invalid semantic alias name in {category}")
            if confidence not in ALIAS_CONFIDENCE_VALUES:
                raise SystemExit(f"Invalid semantic alias confidence in {category}")
            if original in originals or alias in names:
                raise SystemExit(f"Duplicate semantic alias in {category}")
            originals.add(original)
            names.add(alias)
    return aliases


def normalize_target(raw: str, current_class: str) -> tuple[str, str, str]:
    match = TARGET.match(raw.strip())
    if not match:
        return current_class, raw.strip(), ""
    owner = (match.group("owner") or current_class).replace("/", ".")
    name = match.group("name").strip('"')
    return owner, name, match.group("descriptor")


def member_kind(declaration: str) -> str:
    return "method" if "(" in declaration or declaration == "static {};" else "field"


def parse_class(path: Path) -> dict[str, object]:
    class_name = path.stem.removesuffix(".javap")
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    class_refs: set[str] = set()
    strings: set[str] = set()
    methods: list[dict[str, object]] = []
    fields: list[dict[str, object]] = []
    calls: list[dict[str, object]] = []
    accesses: list[dict[str, object]] = []
    in_members = False
    pending: dict[str, object] | None = None
    current_method: dict[str, object] | None = None

    for line_number, line in enumerate(lines, start=1):
        if not in_members:
            class_match = CLASS_REF.match(line)
            if class_match:
                class_refs.add(class_match.group(1).strip('"').replace("/", "."))
            string_match = STRING_REF.match(line)
            if string_match:
                strings.add(string_match.group(1))
            if line == "{":
                in_members = True
            continue

        stripped = line.strip()
        if line.startswith("  ") and not line.startswith("    ") and stripped.endswith(";"):
            declaration = stripped
            pending = {
                "class": class_name,
                "kind": member_kind(declaration),
                "declaration": declaration,
                "javap_line": line_number,
            }
            if pending["kind"] == "method":
                pending["instruction_count"] = 0
                current_method = pending
                methods.append(pending)
            else:
                current_method = None
                fields.append(pending)
            continue

        if pending is not None and stripped.startswith("descriptor:"):
            pending["descriptor"] = stripped.removeprefix("descriptor:").strip()
            if pending["kind"] == "method":
                declaration = str(pending["declaration"])
                if declaration == "static {};":
                    name = "<clinit>"
                else:
                    prefix = declaration.split("(", 1)[0]
                    tokens = prefix.split()
                    semantic_tokens = [
                        token for token in tokens if token not in JAVA_MODIFIERS
                    ]
                    name = tokens[-1]
                    if semantic_tokens == [class_name]:
                        name = "<init>"
                pending["name"] = name
                pending["id"] = f"{class_name}.{name}:{pending['descriptor']}"
            else:
                name = str(pending["declaration"]).rstrip(";").rsplit(" ", 1)[-1]
                pending["name"] = name
                pending["id"] = f"{class_name}.{name}:{pending['descriptor']}"
            continue

        if pending is not None and stripped.startswith("flags:"):
            pending["flags"] = stripped.removeprefix("flags:").strip()
            continue

        if current_method is None or "id" not in current_method:
            continue

        if INSTRUCTION.match(line):
            current_method["instruction_count"] = (
                int(current_method["instruction_count"]) + 1
            )

        call_match = INVOKE.match(line)
        if call_match:
            owner, name, descriptor = normalize_target(
                call_match.group("target"), class_name
            )
            calls.append(
                {
                    "caller": current_method["id"],
                    "caller_javap_line": current_method["javap_line"],
                    "bytecode_offset": int(call_match.group("offset")),
                    "opcode": call_match.group("opcode"),
                    "target_class": owner,
                    "target_name": name,
                    "target_descriptor": descriptor,
                    "target": f"{owner}.{name}:{descriptor}",
                }
            )
            continue

        field_match = FIELD_ACCESS.match(line)
        if field_match:
            owner, name, descriptor = normalize_target(
                field_match.group("target"), class_name
            )
            accesses.append(
                {
                    "caller": current_method["id"],
                    "bytecode_offset": int(field_match.group("offset")),
                    "opcode": field_match.group("opcode"),
                    "target_class": owner,
                    "target_name": name,
                    "target_descriptor": descriptor,
                    "target": f"{owner}.{name}:{descriptor}",
                }
            )

    return {
        "class": class_name,
        "methods": methods,
        "fields": fields,
        "calls": calls,
        "field_accesses": accesses,
        "class_references": sorted(class_refs),
        "string_constants": sorted(strings),
    }


def write_json(path: Path, value: object) -> None:
    path.write_text(
        json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )


def publish_directory(stage: Path, output: Path) -> str | None:
    """Atomically publish an exact inventory tree and preserve prior output."""

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
            return f"could not remove committed inventory backup {backup}: {error}"
    return None


def acquire_reconstruction_lock(root: Path) -> Path:
    lock = root / ".static-reconstruction.lock"
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


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bytecode_dir", type=Path)
    parser.add_argument("output_dir", type=Path)
    args = parser.parse_args()

    bytecode_dir = args.bytecode_dir.resolve()
    unresolved_output_dir = Path(os.path.abspath(args.output_dir))
    if unresolved_output_dir.is_symlink():
        raise SystemExit(f"Refusing symlinked inventory output path: {unresolved_output_dir}")
    output_dir = unresolved_output_dir.resolve()
    unsafe_outputs = {
        Path(output_dir.anchor),
        Path.cwd().resolve(),
        Path.home().resolve(),
    }
    if output_dir in unsafe_outputs or output_dir.name != "inventory":
        raise SystemExit(f"Refusing unsafe inventory output path: {output_dir}")
    if output_dir == bytecode_dir or output_dir in bytecode_dir.parents:
        raise SystemExit("Inventory output must not replace bytecode input or an ancestor")
    output_dir.parent.mkdir(parents=True, exist_ok=True)
    prior_report = output_dir.parent / "verification-report.json"
    if prior_report.exists() or prior_report.is_symlink():
        if prior_report.is_dir() and not prior_report.is_symlink():
            raise SystemExit(f"Verification report path is a directory: {prior_report}")
        prior_report.unlink()
    acquire_reconstruction_lock(output_dir.parent)
    files = sorted(bytecode_dir.glob("*.javap.txt"))
    if not files:
        raise SystemExit(f"No .javap.txt files found in {bytecode_dir}")

    classes = [parse_class(path) for path in files]
    internal_classes = {str(item["class"]) for item in classes}
    methods = [method for item in classes for method in item["methods"]]
    fields = [field for item in classes for field in item["fields"]]
    calls = [call for item in classes for call in item["calls"]]
    accesses = [access for item in classes for access in item["field_accesses"]]

    for call in calls:
        call["scope"] = (
            "internal" if call["target_class"] in internal_classes else "external"
        )
    for access in accesses:
        access["scope"] = (
            "internal" if access["target_class"] in internal_classes else "external"
        )

    dependencies = sorted(
        {
            reference
            for item in classes
            for reference in item["class_references"]
            if reference not in internal_classes and not reference.startswith("[")
        }
    )
    string_constants = sorted(
        {constant for item in classes for constant in item["string_constants"]}
    )
    call_targets = Counter(str(call["target"]) for call in calls)
    callers = Counter(str(call["caller"]) for call in calls)
    instructions = sum(int(method["instruction_count"]) for method in methods)
    aliases_path = Path(__file__).with_name("java-me-semantic-aliases.json")
    if aliases_path.is_symlink() or not aliases_path.is_file():
        raise SystemExit(
            f"Canonical semantic aliases must be a regular file: {aliases_path}"
        )
    aliases = validate_alias_schema(
        json.loads(aliases_path.read_text(encoding="utf-8"))
    )
    method_ids = {str(method["id"]) for method in methods}
    field_ids = {str(field["id"]) for field in fields}
    if any(item["original"] not in internal_classes for item in aliases["classes"]):
        raise SystemExit("Canonical semantic aliases reference a missing class")
    if any(item["original"] not in method_ids for item in aliases["methods"]):
        raise SystemExit("Canonical semantic aliases reference a missing method")
    if any(item["original"] not in field_ids for item in aliases["fields"]):
        raise SystemExit("Canonical semantic aliases reference a missing field")

    summary = {
        "analysis_mode": "static-only",
        "game_execution_performed": False,
        "class_count": len(classes),
        "method_count": len(methods),
        "field_count": len(fields),
        "instruction_count": instructions,
        "call_site_count": len(calls),
        "internal_call_site_count": sum(call["scope"] == "internal" for call in calls),
        "external_call_site_count": sum(call["scope"] == "external" for call in calls),
        "field_access_count": len(accesses),
        "external_class_reference_count": len(dependencies),
        "string_constant_count": len(string_constants),
        "most_called_targets": [
            {"target": target, "call_sites": count}
            for target, count in call_targets.most_common(30)
        ],
        "methods_with_most_calls": [
            {"method": method, "call_sites": count}
            for method, count in callers.most_common(30)
        ],
        "largest_methods_by_instruction_count": [
            {"method": method["id"], "instructions": method["instruction_count"]}
            for method in sorted(
                methods,
                key=lambda item: int(item["instruction_count"]),
                reverse=True,
            )[:30]
        ],
    }
    output_dir.parent.mkdir(parents=True, exist_ok=True)
    if output_dir.exists() and not output_dir.is_dir():
        raise SystemExit(f"Inventory output must be a regular directory: {output_dir}")
    stage = Path(
        tempfile.mkdtemp(prefix=f".{output_dir.name}.stage-", dir=output_dir.parent)
    )
    published = False
    try:
        write_json(stage / "summary.json", summary)
        write_json(stage / "methods.json", methods)
        write_json(stage / "fields.json", fields)
        write_json(stage / "calls.json", calls)
        write_json(stage / "field-accesses.json", accesses)
        write_json(stage / "dependencies.json", dependencies)
        write_json(stage / "string-constants.json", string_constants)
        write_json(stage / "semantic-aliases.json", aliases)
        if {path.name for path in stage.iterdir()} != MANAGED_OUTPUT_NAMES:
            raise SystemExit("Staged inventory file set differs from its schema")
        stage.chmod(0o755)
        publication_warning = publish_directory(stage, output_dir)
        published = True
        if publication_warning is not None:
            print(f"warning: {publication_warning}", file=sys.stderr)
    finally:
        if not published and stage.exists():
            shutil.rmtree(stage)
    print(json.dumps(summary, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
