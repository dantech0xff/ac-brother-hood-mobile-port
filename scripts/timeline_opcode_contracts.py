#!/usr/bin/env python3
"""Static-only contracts for timeline completion and extended opcodes.

This module is a clean-room, host-side specification derived from repository
bytecode.  It does not open or execute the target JAR, load MIDlet classes, or
invoke an emulator, simulator, or device.  Directly proven field writes are
represented as immutable state transitions; calls into legacy ``i``, ``k``,
``g``, UI, and audio helpers are retained as ordered intentions.
"""

from __future__ import annotations

import importlib.util
import sys
from dataclasses import dataclass, replace
from pathlib import Path
from typing import TypeAlias


JAVA_INT_MIN = -(1 << 31)
JAVA_INT_MAX = (1 << 31) - 1
JAVA_SHORT_MIN = -(1 << 15)
JAVA_SHORT_MAX = (1 << 15) - 1
JAVA_U16_MAX = (1 << 16) - 1
JAVA_U8_MAX = (1 << 8) - 1


def _load_canonical_decoder():
    """Load only static syntax data from the sibling level decoder."""

    module_name = "_timeline_contract_level_decoder"
    loaded = sys.modules.get(module_name)
    if loaded is not None:
        return loaded
    path = Path(__file__).with_name("decode-gameloft-level-records.py")
    spec = importlib.util.spec_from_file_location(module_name, path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot import canonical opcode layouts from {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module


_CANONICAL_DECODER = _load_canonical_decoder()
EXTENDED_OPCODE_WIDTHS = {
    opcode: _CANONICAL_DECODER.OPCODE_LAYOUTS[opcode].width
    for opcode in range(100, 115)
}
EXTENDED_OPCODE_WIDTH_SEQUENCE = tuple(
    EXTENDED_OPCODE_WIDTHS[opcode] for opcode in range(100, 115)
)
EXTENDED_OPCODE_EFFECT_FAMILIES = {
    100: "entity-control",
    101: "chained-script",
    102: "screen-state",
    103: "opaque-no-op",
    104: "audio",
    105: "blocking-ui",
    106: "dialogue-state",
    107: "single-prompt-setup",
    108: "single-prompt-poll-branch",
    109: "synthetic-vector-state",
    110: "entity-link",
    111: "feature-gated-spawn",
    112: "multi-prompt-setup",
    113: "multi-prompt-poll-branch",
    114: "timed-text",
}


def _require_plain_int(value: object, label: str) -> int:
    if not isinstance(value, int) or isinstance(value, bool):
        raise ValueError(f"{label} must be an integer")
    return value


def _require_java_int(value: object, label: str) -> int:
    integer = _require_plain_int(value, label)
    if integer < JAVA_INT_MIN or integer > JAVA_INT_MAX:
        raise ValueError(f"{label} must fit a signed Java int")
    return integer


def _require_java_short(value: object, label: str) -> int:
    integer = _require_plain_int(value, label)
    if integer < JAVA_SHORT_MIN or integer > JAVA_SHORT_MAX:
        raise ValueError(f"{label} must fit a signed Java short")
    return integer


def _require_u16(value: object, label: str) -> int:
    integer = _require_plain_int(value, label)
    if integer < 0 or integer > JAVA_U16_MAX:
        raise ValueError(f"{label} must fit an unsigned 16-bit word")
    return integer


def _require_u8(value: object, label: str) -> int:
    integer = _require_plain_int(value, label)
    if integer < 0 or integer > JAVA_U8_MAX:
        raise ValueError(f"{label} must fit an unsigned byte")
    return integer


def _require_bool(value: object, label: str) -> bool:
    if not isinstance(value, bool):
        raise ValueError(f"{label} must be a boolean")
    return value


def _java_i32(value: int) -> int:
    return ((value + (1 << 31)) & 0xFFFF_FFFF) - (1 << 31)


def _normalize_opcode(opcode: object) -> int:
    opcode = _require_plain_int(opcode, "opcode")
    if opcode not in EXTENDED_OPCODE_WIDTHS:
        raise ValueError("opcode must be in the extended range 100..114")
    return opcode


def _normalize_raw_operands(
    opcode: int, raw_operands: object
) -> bytes:
    if not isinstance(raw_operands, (bytes, bytearray, memoryview)):
        raise ValueError("raw_operands must be bytes-like")
    raw = bytes(raw_operands)
    expected = EXTENDED_OPCODE_WIDTHS[opcode]
    if len(raw) != expected:
        raise ValueError(
            f"opcode {opcode} requires exactly {expected} operand byte(s)"
        )
    return raw


@dataclass(frozen=True)
class Op100Operands:
    target: int
    action: int
    value: int

    def __post_init__(self) -> None:
        _require_java_short(self.target, "target")
        _require_java_short(self.action, "action")
        _require_java_short(self.value, "value")


@dataclass(frozen=True)
class Op101Operands:
    script_id: int

    def __post_init__(self) -> None:
        _require_java_short(self.script_id, "script_id")


@dataclass(frozen=True)
class Op102Operands:
    value_0: int
    value_1: int

    def __post_init__(self) -> None:
        _require_java_short(self.value_0, "value_0")
        _require_java_short(self.value_1, "value_1")


@dataclass(frozen=True)
class Op103Operands:
    opaque_hex: str

    def __post_init__(self) -> None:
        if (
            not isinstance(self.opaque_hex, str)
            or len(self.opaque_hex) != 4
            or any(character not in "0123456789abcdef" for character in self.opaque_hex)
        ):
            raise ValueError("opaque_hex must be four lowercase hexadecimal characters")


@dataclass(frozen=True)
class Op104Operands:
    value: int

    def __post_init__(self) -> None:
        _require_java_short(self.value, "value")


@dataclass(frozen=True)
class Op105Operands:
    byte_0: int
    word_0: int
    byte_1: int

    def __post_init__(self) -> None:
        _require_u8(self.byte_0, "byte_0")
        _require_u16(self.word_0, "word_0")
        _require_u8(self.byte_1, "byte_1")


@dataclass(frozen=True)
class Op106Operands:
    word_0: int
    word_1: int
    word_2: int
    word_3: int
    byte: int

    def __post_init__(self) -> None:
        _require_u16(self.word_0, "word_0")
        _require_u16(self.word_1, "word_1")
        _require_u16(self.word_2, "word_2")
        _require_u16(self.word_3, "word_3")
        _require_u8(self.byte, "byte")


@dataclass(frozen=True)
class Op107Operands:
    input_mask: int

    def __post_init__(self) -> None:
        _require_u16(self.input_mask, "input_mask")


@dataclass(frozen=True)
class Op108Operands:
    branch_id_0: int
    branch_id_1: int

    def __post_init__(self) -> None:
        _require_u16(self.branch_id_0, "branch_id_0")
        _require_u16(self.branch_id_1, "branch_id_1")


@dataclass(frozen=True)
class Op109Operands:
    value_0: int
    value_1: int
    value_2: int
    value_3: int

    def __post_init__(self) -> None:
        _require_u16(self.value_0, "value_0")
        _require_u16(self.value_1, "value_1")
        _require_u16(self.value_2, "value_2")
        _require_u16(self.value_3, "value_3")


@dataclass(frozen=True)
class Op110Operands:
    entity_uid_0: int
    entity_uid_1: int

    def __post_init__(self) -> None:
        _require_u16(self.entity_uid_0, "entity_uid_0")
        _require_u16(self.entity_uid_1, "entity_uid_1")


@dataclass(frozen=True)
class Op111Operands:
    word_0: int
    word_1: int
    word_2: int
    byte: int
    word_3: int

    def __post_init__(self) -> None:
        _require_u16(self.word_0, "word_0")
        _require_u16(self.word_1, "word_1")
        _require_u16(self.word_2, "word_2")
        _require_u8(self.byte, "byte")
        _require_u16(self.word_3, "word_3")


@dataclass(frozen=True)
class Op112Operands:
    value_0: int
    value_1: int
    value_2: int

    def __post_init__(self) -> None:
        _require_u16(self.value_0, "value_0")
        _require_u16(self.value_1, "value_1")
        _require_u16(self.value_2, "value_2")


@dataclass(frozen=True)
class Op113Operands:
    branch_id_0: int
    branch_id_1: int

    def __post_init__(self) -> None:
        _require_u16(self.branch_id_0, "branch_id_0")
        _require_u16(self.branch_id_1, "branch_id_1")


@dataclass(frozen=True)
class Op114Operands:
    value_0: int
    value_1: int

    def __post_init__(self) -> None:
        _require_u16(self.value_0, "value_0")
        _require_u16(self.value_1, "value_1")


ExtendedOpcodeOperands: TypeAlias = (
    Op100Operands
    | Op101Operands
    | Op102Operands
    | Op103Operands
    | Op104Operands
    | Op105Operands
    | Op106Operands
    | Op107Operands
    | Op108Operands
    | Op109Operands
    | Op110Operands
    | Op111Operands
    | Op112Operands
    | Op113Operands
    | Op114Operands
)

EXTENDED_OPCODE_OPERAND_TYPES = {
    100: Op100Operands,
    101: Op101Operands,
    102: Op102Operands,
    103: Op103Operands,
    104: Op104Operands,
    105: Op105Operands,
    106: Op106Operands,
    107: Op107Operands,
    108: Op108Operands,
    109: Op109Operands,
    110: Op110Operands,
    111: Op111Operands,
    112: Op112Operands,
    113: Op113Operands,
    114: Op114Operands,
}


@dataclass(frozen=True)
class DecodedOperand:
    """One canonical layout part plus its exact source bytes."""

    name: str
    storage: str
    value: int | str
    raw: bytes

    def __post_init__(self) -> None:
        if not isinstance(self.name, str) or not self.name:
            raise ValueError("operand name must be a nonempty string")
        if self.storage not in {"u8", "u16le", "s16le", "opaque"}:
            raise ValueError(f"unsupported operand storage {self.storage!r}")
        object.__setattr__(self, "raw", bytes(self.raw))


@dataclass(frozen=True)
class DecodedExtendedTimelineOpcode:
    opcode: int
    width: int
    operand_layout: str
    effect_family: str
    typed_operands: ExtendedOpcodeOperands
    operands: tuple[DecodedOperand, ...]
    raw_operands: bytes

    def __post_init__(self) -> None:
        opcode = _normalize_opcode(self.opcode)
        object.__setattr__(self, "operands", tuple(self.operands))
        object.__setattr__(self, "raw_operands", bytes(self.raw_operands))
        layout = _CANONICAL_DECODER.OPCODE_LAYOUTS[opcode]
        if self.width != layout.width or self.width != len(self.raw_operands):
            raise ValueError("width must match canonical layout and raw bytes")
        if self.operand_layout != layout.label:
            raise ValueError("operand_layout must match the canonical decoder")
        if self.effect_family != EXTENDED_OPCODE_EFFECT_FAMILIES[opcode]:
            raise ValueError("effect_family must match the opcode")
        if not isinstance(
            self.typed_operands, EXTENDED_OPCODE_OPERAND_TYPES[opcode]
        ):
            raise ValueError("typed_operands class must match the opcode")
        if len(self.operands) != len(layout.parts):
            raise ValueError("operands must match the canonical layout parts")
        for operand, part in zip(self.operands, layout.parts):
            if operand.name != part.name or operand.storage != part.storage:
                raise ValueError(
                    "operand names/storage must match the canonical layout"
                )
        if b"".join(operand.raw for operand in self.operands) != self.raw_operands:
            raise ValueError("operand raw bytes must reconstruct raw_operands")

    @property
    def raw_hex(self) -> str:
        return self.raw_operands.hex()

    def value(self, name: str) -> int | str:
        values = [
            operand.value for operand in self.operands if operand.name == name
        ]
        if len(values) != 1:
            raise ValueError(f"expected exactly one operand named {name!r}")
        return values[0]


def _decode_canonical_operands(
    opcode: int, raw: bytes
) -> tuple[DecodedOperand, ...]:
    layout = _CANONICAL_DECODER.OPCODE_LAYOUTS[opcode]
    offset = 0
    decoded: list[DecodedOperand] = []
    for part in layout.parts:
        part_raw = raw[offset : offset + part.size]
        if part.storage == "u8":
            value: int | str = part_raw[0]
        elif part.storage == "u16le":
            value = int.from_bytes(part_raw, "little", signed=False)
        elif part.storage == "s16le":
            value = int.from_bytes(part_raw, "little", signed=True)
        elif part.storage == "opaque":
            value = part_raw.hex()
        else:
            raise ValueError(
                f"canonical decoder exposed unsupported storage "
                f"{part.storage!r}"
            )
        decoded.append(
            DecodedOperand(
                name=part.name,
                storage=part.storage,
                value=value,
                raw=part_raw,
            )
        )
        offset += part.size
    return tuple(decoded)


def decode_extended_timeline_opcode(
    opcode: int, raw_operands: bytes | bytearray | memoryview
) -> DecodedExtendedTimelineOpcode:
    """Decode one exact-width payload using the canonical syntax table."""

    opcode = _normalize_opcode(opcode)
    raw = _normalize_raw_operands(opcode, raw_operands)
    decoded_operands = _decode_canonical_operands(opcode, raw)
    values = tuple(operand.value for operand in decoded_operands)
    if opcode == 100:
        operands: ExtendedOpcodeOperands = Op100Operands(*values)
    elif opcode == 101:
        operands = Op101Operands(*values)
    elif opcode == 102:
        operands = Op102Operands(*values)
    elif opcode == 103:
        operands = Op103Operands(*values)
    elif opcode == 104:
        operands = Op104Operands(*values)
    elif opcode == 105:
        operands = Op105Operands(*values)
    elif opcode == 106:
        operands = Op106Operands(*values)
    elif opcode == 107:
        operands = Op107Operands(*values)
    elif opcode == 108:
        operands = Op108Operands(*values)
    elif opcode == 109:
        operands = Op109Operands(*values)
    elif opcode == 110:
        operands = Op110Operands(*values)
    elif opcode == 111:
        operands = Op111Operands(*values)
    elif opcode == 112:
        operands = Op112Operands(*values)
    elif opcode == 113:
        operands = Op113Operands(*values)
    else:
        operands = Op114Operands(*values)
    layout = _CANONICAL_DECODER.OPCODE_LAYOUTS[opcode]
    return DecodedExtendedTimelineOpcode(
        opcode=opcode,
        width=layout.width,
        operand_layout=layout.label,
        effect_family=EXTENDED_OPCODE_EFFECT_FAMILIES[opcode],
        typed_operands=operands,
        operands=decoded_operands,
        raw_operands=raw,
    )


@dataclass(frozen=True, eq=False)
class TimelineObjectRef:
    """Identity-bearing host reference; comparisons deliberately use ``is``."""

    token: str

    def __post_init__(self) -> None:
        if not isinstance(self.token, str) or not self.token:
            raise ValueError("token must be a nonempty string")


DEFAULT_ENTITY_VARIABLES = (0,) * 21
DEFAULT_TIMELINE_FLAGS = (False,) * 10


@dataclass(frozen=True)
class TimelineEntityState:
    reference: TimelineObjectRef
    runtime_type: int = 0
    flags: int = 0
    aux_flags: int = 0
    action: int = 0
    variables: tuple[int, ...] = DEFAULT_ENTITY_VARIABLES
    timeline_flags: tuple[bool, ...] = DEFAULT_TIMELINE_FLAGS
    lane_event_indexes: tuple[int, ...] | None = None
    current_tick: int = -1
    chained_script_id: int = -1
    world_y: int = 0
    velocity_ai: int = 0
    velocity_ag: int = 0
    velocity_aj: int = 0
    velocity_ah: int = 0
    interaction_value: int = 0
    interaction_elapsed: int = 0
    linked_bm_ref: TimelineObjectRef | None = None
    animation_af_ref: TimelineObjectRef | None = None
    message_slots: tuple[int, ...] | None = None

    def __post_init__(self) -> None:
        if not isinstance(self.reference, TimelineObjectRef):
            raise ValueError("reference must be a TimelineObjectRef")
        for name in (
            "runtime_type",
            "flags",
            "aux_flags",
            "action",
            "world_y",
            "velocity_ai",
            "velocity_ag",
            "velocity_aj",
            "velocity_ah",
            "interaction_value",
            "interaction_elapsed",
        ):
            _require_java_int(getattr(self, name), name)
        _require_java_short(self.chained_script_id, "chained_script_id")
        _require_java_short(self.current_tick, "current_tick")
        variables = tuple(self.variables)
        if len(variables) < 21:
            raise ValueError("variables must contain at least 21 Java ints")
        for index, value in enumerate(variables):
            _require_java_int(value, f"variables[{index}]")
        object.__setattr__(self, "variables", variables)
        timeline_flags = tuple(self.timeline_flags)
        if len(timeline_flags) != 10:
            raise ValueError("timeline_flags must contain exactly 10 booleans")
        for index, value in enumerate(timeline_flags):
            _require_bool(value, f"timeline_flags[{index}]")
        object.__setattr__(self, "timeline_flags", timeline_flags)
        if self.lane_event_indexes is not None:
            lane_indexes = tuple(self.lane_event_indexes)
            for index, value in enumerate(lane_indexes):
                _require_java_int(value, f"lane_event_indexes[{index}]")
            object.__setattr__(self, "lane_event_indexes", lane_indexes)
        for name in ("linked_bm_ref", "animation_af_ref"):
            reference = getattr(self, name)
            if reference is not None and not isinstance(
                reference, TimelineObjectRef
            ):
                raise ValueError(
                    f"{name} must be a TimelineObjectRef or None"
                )
        if self.message_slots is not None:
            message_slots = tuple(self.message_slots)
            if len(message_slots) != 10:
                raise ValueError("message_slots must contain exactly 10 Java ints")
            for index, value in enumerate(message_slots):
                _require_java_int(value, f"message_slots[{index}]")
            object.__setattr__(self, "message_slots", message_slots)


@dataclass(frozen=True)
class SinglePromptState:
    input_mask: int
    input_index: int

    def __post_init__(self) -> None:
        _require_java_int(self.input_mask, "input_mask")
        input_index = _require_plain_int(self.input_index, "input_index")
        if input_index < 0 or input_index > 15:
            raise ValueError("input_index must be in range 0..15")


@dataclass(frozen=True)
class MultiplePromptState:
    options: tuple[int, ...]
    progress: int

    def __post_init__(self) -> None:
        options = tuple(self.options)
        if len(options) > 3:
            raise ValueError("options must contain at most three prompt indexes")
        for index, option in enumerate(options):
            option = _require_plain_int(option, f"options[{index}]")
            if option < 0 or option >= 10:
                raise ValueError("prompt indexes must be in range 0..9")
        object.__setattr__(self, "options", options)
        progress = _require_java_int(self.progress, "progress")
        if progress < -1 or progress > len(options):
            raise ValueError("progress must be in range -1..len(options)")


@dataclass(frozen=True)
class TimedTextState:
    text: str | None
    duration: int

    def __post_init__(self) -> None:
        if self.text is not None and not isinstance(self.text, str):
            raise ValueError("text must be a string or None")
        _require_u16(self.duration, "duration")


@dataclass(frozen=True)
class LocalizedTextObservation:
    """Presence-bearing wrapper because the legacy lookup may return null."""

    value: str | None

    def __post_init__(self) -> None:
        if self.value is not None and not isinstance(self.value, str):
            raise ValueError("value must be a string or None")


@dataclass(frozen=True)
class EntityLookupObservation:
    legacy_id: int
    result: TimelineObjectRef | None

    def __post_init__(self) -> None:
        _require_java_int(self.legacy_id, "legacy_id")
        if self.result is not None and not isinstance(
            self.result, TimelineObjectRef
        ):
            raise ValueError("result must be a TimelineObjectRef or None")


@dataclass(frozen=True)
class ScriptLookupObservation:
    script_id: int
    group_index: int

    def __post_init__(self) -> None:
        _require_java_int(self.script_id, "script_id")
        _require_java_int(self.group_index, "group_index")


@dataclass(frozen=True)
class PromptWidgetObservation:
    x: int
    y: int
    active_marker: int = -1
    hovered: bool = False
    activated: bool = False

    def __post_init__(self) -> None:
        _require_java_int(self.x, "x")
        _require_java_int(self.y, "y")
        _require_java_int(self.active_marker, "active_marker")
        _require_bool(self.hovered, "hovered")
        _require_bool(self.activated, "activated")


@dataclass(frozen=True)
class PromptPollObservation:
    masked_input_active: bool
    cancel_input_active: bool
    pointer_cancelled: bool
    widgets: tuple[PromptWidgetObservation | None, ...]

    def __post_init__(self) -> None:
        _require_bool(self.masked_input_active, "masked_input_active")
        _require_bool(self.cancel_input_active, "cancel_input_active")
        _require_bool(self.pointer_cancelled, "pointer_cancelled")
        widgets = tuple(self.widgets)
        for index, widget in enumerate(widgets):
            if widget is not None and not isinstance(
                widget, PromptWidgetObservation
            ):
                raise ValueError(
                    f"widgets[{index}] must be a PromptWidgetObservation or None"
                )
        object.__setattr__(self, "widgets", widgets)


@dataclass(frozen=True)
class EffectIntention:
    """One ordered, non-executed legacy call or external write."""

    kind: str
    symbol: str
    arguments: tuple[object, ...] = ()
    observed_result: object | None = None

    def __post_init__(self) -> None:
        if self.kind not in ("call", "construct", "write"):
            raise ValueError("kind must be call, construct, or write")
        if not isinstance(self.symbol, str) or not self.symbol:
            raise ValueError("symbol must be a nonempty string")
        object.__setattr__(self, "arguments", tuple(self.arguments))


@dataclass(frozen=True)
class TimelineOpcodeState:
    owner_ref: TimelineObjectRef
    entities: tuple[TimelineEntityState, ...]
    player_ref: TimelineObjectRef | None = None
    global_event_latched: bool = False
    media_position: int = 0
    media_end: int = 0
    message_target_ref: TimelineObjectRef | None = None
    single_prompt: SinglePromptState | None = None
    multiple_prompt: MultiplePromptState | None = None
    prompt_response: int = 0
    camera_vector: tuple[int, int, int, int, int] | None = None
    camera_entity_refs: tuple[
        TimelineObjectRef | None, TimelineObjectRef | None
    ] = (None, None)
    timed_text: TimedTextState | None = None

    def __post_init__(self) -> None:
        if not isinstance(self.owner_ref, TimelineObjectRef):
            raise ValueError("owner_ref must be a TimelineObjectRef")
        entities = tuple(self.entities)
        for index, entity in enumerate(entities):
            if not isinstance(entity, TimelineEntityState):
                raise ValueError(
                    f"entities[{index}] must be a TimelineEntityState"
                )
        if len({id(entity.reference) for entity in entities}) != len(entities):
            raise ValueError("entities must not repeat a reference identity")
        object.__setattr__(self, "entities", entities)
        _find_entity(entities, self.owner_ref, "owner_ref")
        if self.player_ref is not None:
            if not isinstance(self.player_ref, TimelineObjectRef):
                raise ValueError("player_ref must be a TimelineObjectRef or None")
            _find_entity(entities, self.player_ref, "player_ref")
        _require_bool(self.global_event_latched, "global_event_latched")
        _require_java_int(self.media_position, "media_position")
        _require_java_int(self.media_end, "media_end")
        if self.message_target_ref is not None and not isinstance(
            self.message_target_ref, TimelineObjectRef
        ):
            raise ValueError(
                "message_target_ref must be a TimelineObjectRef or None"
            )
        if self.single_prompt is not None and not isinstance(
            self.single_prompt, SinglePromptState
        ):
            raise ValueError("single_prompt must be a SinglePromptState or None")
        if self.multiple_prompt is not None and not isinstance(
            self.multiple_prompt, MultiplePromptState
        ):
            raise ValueError(
                "multiple_prompt must be a MultiplePromptState or None"
            )
        _require_java_int(self.prompt_response, "prompt_response")
        if self.camera_vector is not None:
            camera_vector = tuple(self.camera_vector)
            if len(camera_vector) != 5:
                raise ValueError("camera_vector must contain exactly five ints")
            for index, value in enumerate(camera_vector):
                _require_java_int(value, f"camera_vector[{index}]")
            object.__setattr__(self, "camera_vector", camera_vector)
        camera_refs = tuple(self.camera_entity_refs)
        if len(camera_refs) != 2:
            raise ValueError("camera_entity_refs must contain exactly two refs")
        for index, reference in enumerate(camera_refs):
            if reference is not None and not isinstance(
                reference, TimelineObjectRef
            ):
                raise ValueError(
                    f"camera_entity_refs[{index}] must be a reference or None"
                )
        object.__setattr__(self, "camera_entity_refs", camera_refs)
        if self.timed_text is not None and not isinstance(
            self.timed_text, TimedTextState
        ):
            raise ValueError("timed_text must be a TimedTextState or None")


@dataclass(frozen=True)
class TimelineOpcodeContext:
    entity_lookups: tuple[EntityLookupObservation, ...] = ()
    script_lookups: tuple[ScriptLookupObservation, ...] = ()
    keypad_mode: bool | None = None
    prompt_widget_presence: tuple[bool, ...] | None = None
    prompt_poll: PromptPollObservation | None = None
    media_start_succeeded: bool | None = None
    advance_latched_after_media: bool | None = None
    camera_angle_result: int | None = None
    special_entity_creation_enabled: bool | None = None
    created_entity: TimelineEntityState | None = None
    level_index: int | None = None
    localized_text: LocalizedTextObservation | None = None

    def __post_init__(self) -> None:
        entity_lookups = tuple(self.entity_lookups)
        script_lookups = tuple(self.script_lookups)
        for index, observation in enumerate(entity_lookups):
            if not isinstance(observation, EntityLookupObservation):
                raise ValueError(
                    f"entity_lookups[{index}] must be an EntityLookupObservation"
                )
        for index, observation in enumerate(script_lookups):
            if not isinstance(observation, ScriptLookupObservation):
                raise ValueError(
                    f"script_lookups[{index}] must be a ScriptLookupObservation"
                )
        if len({item.legacy_id for item in entity_lookups}) != len(
            entity_lookups
        ):
            raise ValueError("entity_lookups must not repeat a legacy_id")
        if len({item.script_id for item in script_lookups}) != len(
            script_lookups
        ):
            raise ValueError("script_lookups must not repeat a script_id")
        object.__setattr__(self, "entity_lookups", entity_lookups)
        object.__setattr__(self, "script_lookups", script_lookups)
        for name in (
            "keypad_mode",
            "media_start_succeeded",
            "advance_latched_after_media",
            "special_entity_creation_enabled",
        ):
            value = getattr(self, name)
            if value is not None:
                _require_bool(value, name)
        if self.prompt_poll is not None and not isinstance(
            self.prompt_poll, PromptPollObservation
        ):
            raise ValueError(
                "prompt_poll must be a PromptPollObservation or None"
            )
        if self.prompt_widget_presence is not None:
            presence = tuple(self.prompt_widget_presence)
            for index, value in enumerate(presence):
                _require_bool(value, f"prompt_widget_presence[{index}]")
            object.__setattr__(
                self, "prompt_widget_presence", presence
            )
        if self.camera_angle_result is not None:
            _require_java_int(
                self.camera_angle_result, "camera_angle_result"
            )
        if self.created_entity is not None and not isinstance(
            self.created_entity, TimelineEntityState
        ):
            raise ValueError(
                "created_entity must be a TimelineEntityState or None"
            )
        if self.level_index is not None:
            _require_java_int(self.level_index, "level_index")
        if self.localized_text is not None and not isinstance(
            self.localized_text, LocalizedTextObservation
        ):
            raise ValueError(
                "localized_text must be a LocalizedTextObservation or None"
            )


@dataclass(frozen=True)
class TimelineOpcodeResult:
    state: TimelineOpcodeState
    operands: DecodedExtendedTimelineOpcode | None
    intentions: tuple[EffectIntention, ...]
    return_value: int
    boundary: str
    trace: tuple[str, ...]


@dataclass(frozen=True)
class TimelineCompletionState:
    owner_ref: TimelineObjectRef
    player_ref: TimelineObjectRef
    entities: tuple[TimelineEntityState, ...]
    selected_owner_ref: TimelineObjectRef | None = None
    focus_ref: TimelineObjectRef | None = None
    secondary_ref: TimelineObjectRef | None = None
    k_z: bool = False
    k_aa: bool = False
    k_ab: bool = False
    completion_latched: bool = False
    global_aw: int = 0
    level_mode: int = 0
    world_y_offset: int = 0

    def __post_init__(self) -> None:
        if not isinstance(self.owner_ref, TimelineObjectRef):
            raise ValueError("owner_ref must be a TimelineObjectRef")
        if not isinstance(self.player_ref, TimelineObjectRef):
            raise ValueError("player_ref must be a TimelineObjectRef")
        entities = tuple(self.entities)
        for index, entity in enumerate(entities):
            if not isinstance(entity, TimelineEntityState):
                raise ValueError(
                    f"entities[{index}] must be a TimelineEntityState"
                )
        if len({id(entity.reference) for entity in entities}) != len(entities):
            raise ValueError("entities must not repeat a reference identity")
        object.__setattr__(self, "entities", entities)
        _find_entity(entities, self.owner_ref, "owner_ref")
        _find_entity(entities, self.player_ref, "player_ref")
        for name in ("selected_owner_ref", "focus_ref", "secondary_ref"):
            reference = getattr(self, name)
            if reference is not None and not isinstance(
                reference, TimelineObjectRef
            ):
                raise ValueError(f"{name} must be a TimelineObjectRef or None")
        for name in ("k_z", "k_aa", "k_ab", "completion_latched"):
            _require_bool(getattr(self, name), name)
        _require_java_int(self.global_aw, "global_aw")
        _require_java_int(self.level_mode, "level_mode")
        _require_java_int(self.world_y_offset, "world_y_offset")


@dataclass(frozen=True)
class TimelineCompletionContext:
    ai_result: bool | None = None
    script_lookups: tuple[ScriptLookupObservation, ...] = ()

    def __post_init__(self) -> None:
        if self.ai_result is not None:
            _require_bool(self.ai_result, "ai_result")
        script_lookups = tuple(self.script_lookups)
        for index, observation in enumerate(script_lookups):
            if not isinstance(observation, ScriptLookupObservation):
                raise ValueError(
                    f"script_lookups[{index}] must be a ScriptLookupObservation"
                )
        if len({item.script_id for item in script_lookups}) != len(
            script_lookups
        ):
            raise ValueError("script_lookups must not repeat a script_id")
        object.__setattr__(self, "script_lookups", script_lookups)


@dataclass(frozen=True)
class TimelineCompletionResult:
    state: TimelineCompletionState
    intentions: tuple[EffectIntention, ...]
    boundary: str
    trace: tuple[str, ...]


def _find_entity(
    entities: tuple[TimelineEntityState, ...],
    reference: TimelineObjectRef,
    label: str,
) -> TimelineEntityState:
    for entity in entities:
        if entity.reference is reference:
            return entity
    raise ValueError(f"{label} does not identify an entity in state")


def _replace_entity(
    entities: tuple[TimelineEntityState, ...],
    updated: TimelineEntityState,
) -> tuple[TimelineEntityState, ...]:
    replaced = False
    result: list[TimelineEntityState] = []
    for entity in entities:
        if entity.reference is updated.reference:
            result.append(updated)
            replaced = True
        else:
            result.append(entity)
    if not replaced:
        raise ValueError("updated entity reference is not present in state")
    return tuple(result)


def _set_timeline_flag(
    entity: TimelineEntityState, index: int, value: bool
) -> TimelineEntityState:
    flags = list(entity.timeline_flags)
    flags[index] = value
    return replace(entity, timeline_flags=tuple(flags))


def _script_group_index(
    observations: tuple[ScriptLookupObservation, ...], script_id: int
) -> int:
    for observation in observations:
        if observation.script_id == script_id:
            return observation.group_index
    raise ValueError(
        f"missing ScriptLookupObservation for script id {script_id}"
    )


def _call(
    symbol: str, *arguments: object, result: object | None = None
) -> EffectIntention:
    return EffectIntention(
        kind="call",
        symbol=symbol,
        arguments=tuple(arguments),
        observed_result=result,
    )


def complete_timeline_script(
    state: TimelineCompletionState,
    context: TimelineCompletionContext,
) -> TimelineCompletionResult:
    """Model ``i.bI:()V`` through its exact direct-write and return order.

    Helper calls are not executed.  In particular, ``bJ()``, ``h(int)``,
    ``k(int)``, and ``k.c(i)`` remain intentions, so this projection records
    only writes performed directly by ``bI`` around those call boundaries.
    Reference comparisons use Python object identity to preserve JVM
    ``if_acmp*`` behavior even when two references carry the same token.
    """

    if not isinstance(state, TimelineCompletionState):
        raise ValueError("state must be a TimelineCompletionState")
    if not isinstance(context, TimelineCompletionContext):
        raise ValueError("context must be a TimelineCompletionContext")

    next_state = state
    entities = state.entities
    intentions: list[EffectIntention] = []
    trace: list[str] = []
    owner = _find_entity(entities, state.owner_ref, "owner_ref")
    player = _find_entity(entities, state.player_ref, "player_ref")
    selected_owner = state.selected_owner_ref is state.owner_ref

    if selected_owner:
        next_state = replace(next_state, k_z=False, k_aa=False, k_ab=False)
        trace.extend(("write:k.Z=false", "write:k.aa=false", "write:k.ab=false"))

        if next_state.secondary_ref is not None:
            next_state = replace(next_state, secondary_ref=None)
            trace.append("write:g.a=null")

        if player.linked_bm_ref is not None:
            attachment = _find_entity(
                entities, player.linked_bm_ref, "player linked_bm_ref"
            )
            if attachment.runtime_type == 13:
                attachment = replace(
                    attachment, aux_flags=0, linked_bm_ref=None
                )
                entities = _replace_entity(entities, attachment)
                trace.extend(
                    (
                        "write:k.aS.bM.aA=0",
                        "write:k.aS.bM.bM=null",
                    )
                )
                player = _find_entity(entities, state.player_ref, "player_ref")
                player = replace(
                    player,
                    linked_bm_ref=None,
                    aux_flags=_java_i32(player.aux_flags & -65),
                )
                entities = _replace_entity(entities, player)
                trace.extend(
                    (
                        "write:k.aS.bM=null",
                        "write:k.aS.aA&=-65",
                    )
                )

        intentions.append(_call("k.n:()V"))
        trace.append("intend:k.n:()V")
        next_state = replace(
            next_state,
            entities=entities,
            completion_latched=True,
        )
        trace.append("write:i.z=true")

    owner = _find_entity(next_state.entities, state.owner_ref, "owner_ref")
    owner = _set_timeline_flag(owner, 2, False)
    entities = _replace_entity(next_state.entities, owner)
    next_state = replace(next_state, entities=entities)
    trace.append("write:this.cd[2]=false")

    if owner.timeline_flags[3]:
        owner = replace(owner, lane_event_indexes=None)
        entities = _replace_entity(next_state.entities, owner)
        intentions.append(_call("i.bJ:()V", state.owner_ref))
        trace.extend(("write:this.cL=null", "intend:i.bJ:()V"))
        owner = replace(owner, current_tick=0)
        entities = _replace_entity(entities, owner)
        next_state = replace(next_state, entities=entities)
        trace.append("write:this.cK=0")
        return TimelineCompletionResult(
            state=next_state,
            intentions=tuple(intentions),
            boundary="repeat-return-pc-131",
            trace=tuple(trace),
        )

    owner = replace(owner, current_tick=-2)
    entities = _replace_entity(next_state.entities, owner)
    next_state = replace(next_state, entities=entities, global_aw=0)
    trace.extend(("write:this.cK=-2", "write:k.aw=0"))

    if owner.runtime_type == 5:
        if next_state.focus_ref is not state.player_ref:
            if context.ai_result is None:
                raise ValueError(
                    "ai_result is required when focus is not the player"
                )
            intentions.append(
                _call("i.ai:()Z", result=context.ai_result)
            )
            trace.append(f"intend:i.ai:()Z={str(context.ai_result).lower()}")
            if not context.ai_result:
                secondary_ref = next_state.secondary_ref
                if secondary_ref is None:
                    next_state = replace(next_state, focus_ref=state.player_ref)
                    trace.append("write:k.ae=k.aS")
                else:
                    secondary = _find_entity(
                        next_state.entities, secondary_ref, "secondary_ref"
                    )
                    if secondary.runtime_type == 43:
                        next_state = replace(
                            next_state, focus_ref=secondary_ref
                        )
                        trace.append("write:k.ae=g.a")
                    else:
                        next_state = replace(
                            next_state, focus_ref=state.player_ref
                        )
                        trace.append("write:k.ae=k.aS")

        if next_state.level_mode == 3 and not next_state.k_z:
            player = _find_entity(
                next_state.entities, state.player_ref, "player_ref"
            )
            player = replace(
                player,
                world_y=_java_i32(
                    player.world_y - next_state.world_y_offset
                ),
            )
            entities = _replace_entity(next_state.entities, player)
            next_state = replace(next_state, entities=entities)
            trace.append("write:k.aS.al-=k.X")

    if selected_owner:
        owner = _find_entity(next_state.entities, state.owner_ref, "owner_ref")
        if owner.chained_script_id != -1:
            script_id = owner.chained_script_id
            group_index = _script_group_index(
                context.script_lookups, script_id
            )
            intentions.extend(
                (
                    _call("k.s:(I)I", script_id, result=group_index),
                    _call("i.h:(I)V", state.owner_ref, group_index),
                    _call("k.s:(I)I", script_id, result=group_index),
                    _call("i.k:(I)V", state.owner_ref, group_index),
                )
            )
            trace.extend(
                (
                    f"intend:k.s:{script_id}->{group_index}",
                    f"intend:i.h:{group_index}",
                    f"intend:k.s:{script_id}->{group_index}",
                    f"intend:i.k:{group_index}",
                )
            )
            owner = replace(owner, chained_script_id=-1)
            entities = _replace_entity(next_state.entities, owner)
            next_state = replace(next_state, entities=entities)
            trace.append("write:this.cP=-1")
            return TimelineCompletionResult(
                state=next_state,
                intentions=tuple(intentions),
                boundary="chained-script-return-pc-270",
                trace=tuple(trace),
            )

        next_state = replace(next_state, selected_owner_ref=None)
        trace.append("write:k.C=null")

        if owner.timeline_flags[4]:
            intentions.append(_call("i.bJ:()V", state.owner_ref))
            trace.append("intend:i.bJ:()V")
            return TimelineCompletionResult(
                state=next_state,
                intentions=tuple(intentions),
                boundary="reset-return-pc-288",
                trace=tuple(trace),
            )

        if owner.runtime_type != 58:
            if (
                owner.runtime_type == 5
                and owner.variables[1] in (20, 21)
            ):
                owner = replace(
                    owner, flags=_java_i32(owner.flags & -17)
                )
                entities = _replace_entity(next_state.entities, owner)
                next_state = replace(next_state, entities=entities)
                trace.append("write:this.P&=-17")
                return TimelineCompletionResult(
                    state=next_state,
                    intentions=tuple(intentions),
                    boundary="type-5-flag-return-pc-339",
                    trace=tuple(trace),
                )

            intentions.append(_call("k.c:(Li;)V", state.owner_ref))
            trace.append("intend:k.c:(Li;)V")
            boundary = "removal-intended-return-pc-344"
        else:
            boundary = "type-58-retained-return-pc-344"
    else:
        boundary = "non-selected-return-pc-344"

    return TimelineCompletionResult(
        state=next_state,
        intentions=tuple(intentions),
        boundary=boundary,
        trace=tuple(trace),
    )


def execute_extended_timeline_opcode(
    opcode: int,
    raw_operands: bytes | bytearray | memoryview,
    current_tick: int,
    event_tick: int,
    state: TimelineOpcodeState,
    context: TimelineOpcodeContext,
) -> TimelineOpcodeResult:
    """Apply one proven extended-opcode transition.

    ``current_tick`` is the tick evaluated by ``i.aa()`` and ``event_tick`` is
    the event timestamp.  The legacy common guard suppresses every non-exact
    opcode except 108 and 113.  Those two prompt handlers poll while the event
    is still in the future (``current_tick < event_tick``), branch at equality,
    and do nothing once the current tick has passed the event.
    """

    if not isinstance(state, TimelineOpcodeState):
        raise ValueError("state must be a TimelineOpcodeState")
    if not isinstance(context, TimelineOpcodeContext):
        raise ValueError("context must be a TimelineOpcodeContext")
    current_tick = _require_java_short(current_tick, "current_tick")
    event_tick = _require_java_short(event_tick, "event_tick")
    opcode = _normalize_opcode(opcode)
    width = EXTENDED_OPCODE_WIDTHS[opcode]
    if current_tick != event_tick and opcode not in (108, 113):
        return TimelineOpcodeResult(
            state=state,
            operands=None,
            intentions=(),
            return_value=width,
            boundary="non-exact-guard",
            trace=(
                f"width:{opcode}:{width}",
                f"ticks:{current_tick}:{event_tick}",
                "return:common-non-exact-guard",
            ),
        )

    decoded = decode_extended_timeline_opcode(opcode, raw_operands)
    operands = decoded.typed_operands
    intentions: list[EffectIntention] = []
    trace: list[str] = [
        f"decode:{decoded.opcode}:{decoded.width}",
        f"ticks:{current_tick}:{event_tick}",
    ]

    def finish(
        next_state: TimelineOpcodeState,
        *,
        boundary: str,
        return_value: int | None = None,
    ) -> TimelineOpcodeResult:
        return TimelineOpcodeResult(
            state=next_state,
            operands=decoded,
            intentions=tuple(intentions),
            return_value=(
                decoded.width if return_value is None else return_value
            ),
            boundary=boundary,
            trace=tuple(trace),
        )

    def observed_entity_ref(legacy_id: int) -> TimelineObjectRef | None:
        matches = [
            observation
            for observation in context.entity_lookups
            if observation.legacy_id == legacy_id
        ]
        if len(matches) != 1:
            raise ValueError(
                "missing EntityLookupObservation for legacy id "
                f"{legacy_id}"
            )
        observation = matches[0]
        intentions.append(
            _call(
                "k.q:(I)Li;",
                legacy_id,
                result=observation.result,
            )
        )
        trace.append(
            "intend:k.q:"
            f"{legacy_id}->{getattr(observation.result, 'token', None)}"
        )
        return observation.result

    def require_keypad_mode() -> bool:
        if context.keypad_mode is None:
            raise ValueError("keypad_mode observation is required")
        return context.keypad_mode

    def require_widget_presence(count: int) -> tuple[bool, ...]:
        if count == 0:
            return ()
        if context.prompt_widget_presence is None:
            raise ValueError(
                "prompt_widget_presence observation is required"
            )
        if len(context.prompt_widget_presence) < count:
            raise ValueError(
                "prompt_widget_presence is shorter than the prompt count"
            )
        return context.prompt_widget_presence

    def append_cleanup() -> None:
        intentions.extend(
            (
                _call("i.O:()V"),
                _call("k.p:()V"),
            )
        )
        trace.extend(("intend:i.O:()V", "intend:k.p:()V"))

    def append_script_switch(script_id: int) -> None:
        group_index = _script_group_index(
            context.script_lookups, script_id
        )
        intentions.extend(
            (
                _call("k.s:(I)I", script_id, result=group_index),
                _call("i.h:(I)V", state.owner_ref, group_index),
                _call("k.s:(I)I", script_id, result=group_index),
                _call("i.k:(I)V", state.owner_ref, group_index),
            )
        )
        trace.extend(
            (
                f"intend:k.s:{script_id}->{group_index}",
                f"intend:i.h:{group_index}",
                f"intend:k.s:{script_id}->{group_index}",
                f"intend:i.k:{group_index}",
            )
        )

    next_state = state
    owner = _find_entity(state.entities, state.owner_ref, "owner_ref")

    if decoded.opcode == 100:
        assert isinstance(operands, Op100Operands)
        target_ref = state.owner_ref
        if operands.target != 0:
            target_ref = observed_entity_ref(operands.target)
        if target_ref is None:
            trace.append("entity-control:null-target")
            return finish(next_state, boundary="exact-null-target")
        target = _find_entity(
            next_state.entities, target_ref, "entity-control target"
        )
        action = operands.action
        value = operands.value

        if action == 0:
            if value != 0:
                target = replace(target, action=value)
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append(f"write:target.az={value}")
        elif action == 1:
            if value == 0:
                target = replace(
                    target,
                    flags=_java_i32((target.flags | 32 | 128) & -17),
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                intentions.append(_call("i.G:()V", target_ref))
                trace.extend(
                    (
                        "write:target.P|=32|128;target.P&=-17",
                        "intend:i.G:()V",
                    )
                )
            elif value == 1:
                target = replace(
                    target,
                    flags=_java_i32(target.flags & -33 & -129),
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P&=-33&-129")
            elif value == 2:
                target = replace(
                    target, flags=_java_i32(target.flags | 16)
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P|=16")
            elif value == 3:
                target = replace(
                    target,
                    flags=_java_i32((target.flags & -33) | 512),
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P&=-33;target.P|=512")
            elif value == 4:
                target = replace(
                    target,
                    flags=_java_i32(target.flags & -513),
                    velocity_ai=0,
                    velocity_ag=0,
                    velocity_aj=0,
                    velocity_ah=0,
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P&=-513;velocity-slots=0")
            elif value == 5:
                target = replace(
                    target, flags=_java_i32(target.flags ^ 1024)
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P^=1024")
            elif value == 6:
                target = _set_timeline_flag(
                    target, 7, not target.timeline_flags[7]
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.cd[7]=!target.cd[7]")
            elif value == 7 and target.runtime_type == 0:
                if next_state.player_ref is None:
                    raise ValueError(
                        "player_ref is required for entity-control value 7"
                    )
                player = _find_entity(
                    next_state.entities,
                    next_state.player_ref,
                    "player_ref",
                )
                player = replace(
                    player,
                    aux_flags=_java_i32(player.aux_flags ^ 256),
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, player),
                )
                trace.append("write:k.aS.aA^=256")
            elif value == 8 and target.runtime_type == 11:
                variables = list(target.variables)
                if variables[19] == 0:
                    variables[20] = 40
                    target = replace(target, variables=tuple(variables))
                    next_state = replace(
                        next_state,
                        entities=_replace_entity(
                            next_state.entities, target
                        ),
                    )
                    trace.append("write:target.Z[20]=40")
            elif value == 10:
                target = replace(target, flags=32)
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P=32")
                if target.runtime_type == 35:
                    intentions.append(_call("i.i:(I)V", target_ref, 0))
                    trace.append("intend:target.i(0)")
                    if target.animation_af_ref is not None:
                        attachment = _find_entity(
                            next_state.entities,
                            target.animation_af_ref,
                            "entity-control animation_af_ref",
                        )
                        if attachment.runtime_type == 73:
                            intentions.append(
                                _call(
                                    "i.i:(I)V",
                                    target.animation_af_ref,
                                    152,
                                )
                            )
                            trace.append("intend:target.af.i(152)")
            elif value == 11:
                target = replace(
                    target, flags=_java_i32(target.flags | 32)
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P|=32")
            elif value == 12:
                target = replace(
                    target, flags=_java_i32(target.flags | 64)
                )
                next_state = replace(
                    next_state,
                    entities=_replace_entity(next_state.entities, target),
                )
                trace.append("write:target.P|=64")
            else:
                trace.append(f"entity-control:subaction-no-op:{value}")
        elif action == 2:
            intentions.append(_call("k.c:(Li;)V", target_ref))
            trace.append("intend:k.c:(Li;)V")
        elif action == 3:
            target = replace(
                target,
                interaction_value=value,
                interaction_elapsed=0,
            )
            next_state = replace(
                next_state,
                entities=_replace_entity(next_state.entities, target),
            )
            trace.append(
                f"write:target.cz={value};target.cA=0"
            )
        elif action == 4:
            next_state = replace(next_state, global_event_latched=True)
            trace.append("write:k.ab=true")
        elif action == 5:
            target = replace(target, action=value)
            next_state = replace(
                next_state,
                entities=_replace_entity(next_state.entities, target),
            )
            trace.append(f"write:target.az={value}")
        else:
            trace.append(f"entity-control:action-no-op:{action}")
        return finish(next_state, boundary="exact-entity-control")

    if decoded.opcode == 101:
        assert isinstance(operands, Op101Operands)
        owner = replace(owner, chained_script_id=operands.script_id)
        next_state = replace(
            next_state,
            entities=_replace_entity(next_state.entities, owner),
        )
        trace.append(f"write:this.cP={operands.script_id}")
        return finish(next_state, boundary="exact-chained-script")

    if decoded.opcode == 102:
        assert isinstance(operands, Op102Operands)
        if operands.value_1 != 0:
            intentions.append(_call("k.z:(I)V", operands.value_0))
            trace.append(f"intend:k.z:{operands.value_0}")
        else:
            intentions.append(_call("k.A:(I)V", operands.value_0))
            trace.append(f"intend:k.A:{operands.value_0}")
        return finish(next_state, boundary="exact-screen-state")

    if decoded.opcode == 103:
        assert isinstance(operands, Op103Operands)
        trace.append(f"opaque-no-op:{operands.opaque_hex}")
        return finish(next_state, boundary="exact-opaque-no-op")

    if decoded.opcode == 104:
        assert isinstance(operands, Op104Operands)
        intentions.append(_call("k.n:(I)V", operands.value))
        trace.append(f"intend:k.n:{operands.value}")
        return finish(next_state, boundary="exact-audio")

    if decoded.opcode == 105:
        assert isinstance(operands, Op105Operands)
        if not owner.timeline_flags[1]:
            owner = _set_timeline_flag(owner, 0, True)
            if context.media_start_succeeded is None:
                raise ValueError(
                    "media_start_succeeded observation is required"
                )
            intentions.append(
                _call(
                    "k.b:(III)Z",
                    operands.byte_0,
                    operands.word_0,
                    operands.byte_1,
                    result=context.media_start_succeeded,
                )
            )
            trace.extend(
                (
                    "write:this.cd[0]=true",
                    "intend:k.b:(III)Z",
                )
            )
            if context.media_start_succeeded:
                intentions.append(_call("k.l:(I)V", 21))
                trace.append("intend:k.l:21")
            if context.advance_latched_after_media is None:
                raise ValueError(
                    "advance_latched_after_media observation is required"
                )
            owner = _set_timeline_flag(
                owner,
                1,
                context.advance_latched_after_media,
            )
            next_state = replace(
                next_state,
                entities=_replace_entity(next_state.entities, owner),
            )
        if owner.timeline_flags[1]:
            next_state = replace(
                next_state, media_position=next_state.media_end
            )
            trace.append("write:k.v=k.w")
        return finish(next_state, boundary="exact-blocking-ui")

    if decoded.opcode == 106:
        assert isinstance(operands, Op106Operands)
        target_ref: TimelineObjectRef | None
        if operands.word_0 <= 0:
            target_ref = state.owner_ref
        else:
            target_ref = observed_entity_ref(operands.word_0)
        next_state = replace(next_state, message_target_ref=target_ref)
        trace.append(
            "write:this.cT="
            f"{getattr(target_ref, 'token', None)}"
        )
        if target_ref is not None:
            target = _find_entity(
                next_state.entities, target_ref, "message target"
            )
            slots = (
                [-1] * 10
                if target.message_slots is None
                else list(target.message_slots)
            )
            slots[5] = operands.word_1
            slots[6] = operands.word_2
            slots[2] = -1
            slots[3] = operands.word_3
            slots[8] = (
                operands.byte - 2
                if operands.byte > 1
                else operands.byte
            )
            if operands.word_0 > 0:
                slots[7] = 1
            if operands.byte > 1:
                slots[9] = 1
            target = replace(target, message_slots=tuple(slots))
            next_state = replace(
                next_state,
                entities=_replace_entity(next_state.entities, target),
            )
            trace.append("write:target.cQ[2,3,5,6,8];conditional[7,9]")
        return finish(next_state, boundary="exact-dialogue-state")

    if decoded.opcode == 107:
        assert isinstance(operands, Op107Operands)
        keypad_mode = require_keypad_mode()
        widget_presence = require_widget_presence(1)
        input_index = 0
        while (operands.input_mask >> input_index) > 1:
            input_index += 1
        remapped_mask = {
            32: 65568,
            4: 16388,
            16: 4112,
            64: 8256,
            256: 33024,
        }.get(operands.input_mask, operands.input_mask)
        if widget_presence[0]:
            intentions.append(
                EffectIntention("write", "i.bA:[La;", (0, None))
            )
            trace.append("intend:i.bA[0]=null")
        intentions.append(
            EffectIntention("construct", "a.<init>:()V")
        )
        intentions.append(
            EffectIntention(
                "write", "i.bA:[La;", (0, "new-a-instance")
            )
        )
        if keypad_mode:
            intentions.extend(
                (
                    _call("a.a:(Lb;)V", 0, "k.z[9]"),
                    _call("a.a:(II)V", 0, f"ct[{input_index}]", -1),
                )
            )
        else:
            intentions.extend(
                (
                    _call("a.a:(Lb;)V", 0, "k.z[74]"),
                    _call("a.a:(II)V", 0, 0, -1),
                )
            )
        next_state = replace(
            next_state,
            single_prompt=SinglePromptState(
                remapped_mask, input_index
            ),
            prompt_response=0,
        )
        trace.extend(
            (
                f"write:this.cb[0]={remapped_mask}",
                f"write:this.cb[2]={input_index}",
                "write:this.cb[1]=0",
            )
        )
        return finish(next_state, boundary="exact-single-prompt-setup")

    if decoded.opcode == 108:
        assert isinstance(operands, Op108Operands)
        if current_tick < event_tick:
            keypad_mode = require_keypad_mode()
            if next_state.single_prompt is None:
                raise ValueError(
                    "single_prompt state is required while polling opcode 108"
                )
            if context.prompt_poll is None:
                raise ValueError(
                    "prompt_poll observation is required while polling"
                )
            poll = context.prompt_poll
            widget = poll.widgets[0] if poll.widgets else None
            if not keypad_mode and widget is None:
                raise ValueError(
                    "touch-mode opcode 108 requires prompt widget 0"
                )
            if (
                not keypad_mode
                and widget is not None
                and widget.active_marker != -1
                and widget.hovered
            ):
                intentions.append(_call("a.a:(II)V", 0, 1, 1))
                trace.append("intend:single-widget-hover")
            confirmed = poll.masked_input_active or (
                not keypad_mode
                and widget is not None
                and widget.activated
            )
            cancelled = (
                keypad_mode and poll.cancel_input_active
            ) or (not keypad_mode and poll.pointer_cancelled)
            if confirmed and next_state.prompt_response == 0:
                if widget is None:
                    raise ValueError(
                        "opcode 108 confirmation would dereference "
                        "a null widget"
                    )
                next_state = replace(next_state, prompt_response=1)
                frame: object = (
                    f"ct[{next_state.single_prompt.input_index}]+1"
                    if keypad_mode
                    else -1
                )
                intentions.append(_call("a.a:(II)V", 0, frame, 1))
                trace.append("write:this.cb[1]=1")
                append_cleanup()
            elif cancelled and next_state.prompt_response == 0:
                if widget is None:
                    raise ValueError(
                        "opcode 108 cancellation would dereference "
                        "a null widget"
                    )
                next_state = replace(next_state, prompt_response=2)
                frame = (
                    f"ct[{next_state.single_prompt.input_index}]+2"
                    if keypad_mode
                    else -1
                )
                intentions.append(_call("a.a:(II)V", 0, frame, 1))
                trace.append("write:this.cb[1]=2")
                append_cleanup()
            else:
                trace.append("single-prompt:poll-no-transition")
            return finish(
                next_state, boundary="future-single-prompt-poll"
            )
        if current_tick == event_tick:
            if next_state.single_prompt is None:
                raise ValueError(
                    "single_prompt state is required at opcode 108 branch"
                )
            append_cleanup()
            branch_id = (
                operands.branch_id_0
                if next_state.prompt_response == 1
                else operands.branch_id_1
            )
            if branch_id > 0:
                append_script_switch(branch_id)
                trace.append("return:-1")
                return finish(
                    next_state,
                    boundary="exact-single-prompt-branch",
                    return_value=-1,
                )
            trace.append("single-prompt:exact-no-branch")
            return finish(
                next_state, boundary="exact-single-prompt-no-branch"
            )
        trace.append("single-prompt:past-no-op")
        return finish(next_state, boundary="past-single-prompt-no-op")

    if decoded.opcode == 109:
        assert isinstance(operands, Op109Operands)
        if context.camera_angle_result is None:
            raise ValueError("camera_angle_result observation is required")
        delta_x = operands.value_2 - operands.value_0
        delta_y = operands.value_3 - operands.value_1
        if delta_x == 0:
            delta_x = 1
        intentions.append(
            _call(
                "j.b:(II)I",
                -delta_x,
                delta_y,
                result=context.camera_angle_result,
            )
        )
        owner = _set_timeline_flag(owner, 9, True)
        next_state = replace(
            next_state,
            entities=_replace_entity(next_state.entities, owner),
            camera_vector=(
                operands.value_0,
                operands.value_1,
                operands.value_2,
                operands.value_3,
                context.camera_angle_result,
            ),
        )
        trace.extend(
            (
                "write:this.cf[0..4]",
                "write:this.cd[9]=true",
            )
        )
        return finish(next_state, boundary="exact-camera-vector")

    if decoded.opcode == 110:
        assert isinstance(operands, Op110Operands)
        first_ref = observed_entity_ref(operands.entity_uid_0)
        second_ref = observed_entity_ref(operands.entity_uid_1)
        owner = _set_timeline_flag(owner, 9, True)
        next_state = replace(
            next_state,
            entities=_replace_entity(next_state.entities, owner),
            camera_entity_refs=(first_ref, second_ref),
        )
        trace.extend(
            (
                "write:i.cg/i.ch",
                "write:this.cd[9]=true",
            )
        )
        return finish(next_state, boundary="exact-entity-link")

    if decoded.opcode == 111:
        assert isinstance(operands, Op111Operands)
        if context.special_entity_creation_enabled is None:
            raise ValueError(
                "special_entity_creation_enabled observation is required"
            )
        if not context.special_entity_creation_enabled:
            trace.append("feature-gated-spawn:disabled")
            return finish(
                next_state, boundary="exact-feature-gated-no-op"
            )
        if context.created_entity is None:
            raise ValueError(
                "created_entity observation is required when spawning"
            )
        if any(
            entity.reference is context.created_entity.reference
            for entity in next_state.entities
        ):
            raise ValueError(
                "created_entity reference must be new to opcode state"
            )
        intentions.append(
            _call(
                "i.a:(IIIZIII)Li;",
                8,
                59,
                operands.word_0,
                operands.byte > 0,
                operands.word_1,
                operands.word_2,
                operands.word_3,
                result=context.created_entity.reference,
            )
        )
        created = replace(
            context.created_entity,
            flags=_java_i32(context.created_entity.flags | 512),
        )
        next_state = replace(
            next_state, entities=next_state.entities + (created,)
        )
        trace.extend(
            (
                "intend:i.a:type=8:subtype=59",
                "write:created.P|=512",
            )
        )
        return finish(next_state, boundary="exact-feature-gated-spawn")

    if decoded.opcode == 112:
        assert isinstance(operands, Op112Operands)
        options = tuple(
            value
            for value in (
                operands.value_0,
                operands.value_1,
                operands.value_2,
            )
            if value < 10
        )
        keypad_mode = require_keypad_mode() if options else False
        widget_presence = require_widget_presence(len(options))
        for index, option in enumerate(options):
            if not widget_presence[index]:
                intentions.append(
                    EffectIntention("construct", "a.<init>:()V")
                )
                intentions.append(
                    EffectIntention(
                        "write",
                        "i.bA:[La;",
                        (index, "new-a-instance"),
                    )
                )
            if keypad_mode:
                intentions.extend(
                    (
                        _call(
                            "a.a:(Lb;)V",
                            index,
                            "k.z[9]",
                        ),
                        _call(
                            "a.a:(II)V",
                            index,
                            f"ct[{option}]",
                            -1,
                        ),
                    )
                )
            else:
                intentions.extend(
                    (
                        _call(
                            "a.a:(Lb;)V",
                            index,
                            "k.z[74]",
                        ),
                        _call("a.a:(II)V", index, 0, -1),
                    )
                )
        next_state = replace(
            next_state,
            multiple_prompt=MultiplePromptState(options, 0),
            prompt_response=0,
        )
        trace.extend(
            (
                f"write:this.cc[0]={len(options)}",
                "write:this.cc[4]=0",
                "write:this.cb[1]=0",
            )
        )
        return finish(next_state, boundary="exact-multi-prompt-setup")

    if decoded.opcode == 113:
        assert isinstance(operands, Op113Operands)
        if current_tick > event_tick:
            trace.append("sequence-prompt:past-no-op")
            return finish(
                next_state, boundary="past-multi-prompt-no-op"
            )
        if next_state.multiple_prompt is None:
            raise ValueError(
                "multiple_prompt state is required for opcode 113"
            )
        prompt = next_state.multiple_prompt
        if current_tick < event_tick:
            if 0 <= prompt.progress < len(prompt.options):
                keypad_mode = require_keypad_mode()
                if context.prompt_poll is None:
                    raise ValueError(
                        "prompt_poll observation is required while polling"
                    )
                poll = context.prompt_poll
                current_index = prompt.progress
                widget = (
                    poll.widgets[current_index]
                    if len(poll.widgets) > current_index
                    else None
                )
                if not keypad_mode and widget is None:
                    raise ValueError(
                        "touch-mode opcode 113 requires the current "
                        "prompt widget"
                    )
                if (
                    not keypad_mode
                    and widget is not None
                    and widget.active_marker != -1
                    and widget.hovered
                ):
                    intentions.append(
                        _call(
                            "a.a:(II)V",
                            current_index,
                            1,
                            1,
                        )
                    )
                    trace.append("intend:sequence-widget-hover")
                input_mask = 1 << prompt.options[current_index]
                confirmed = poll.masked_input_active or (
                    not keypad_mode
                    and widget is not None
                    and widget.activated
                )
                cancelled = (
                    keypad_mode and poll.cancel_input_active
                ) or (not keypad_mode and poll.pointer_cancelled)
                if confirmed:
                    if (
                        len(poll.widgets) <= current_index
                        and keypad_mode
                    ):
                        raise ValueError(
                            "keypad confirmation requires current-widget "
                            "presence observation"
                        )
                    if widget is not None:
                        frame: object = (
                            f"ct[{prompt.options[current_index]}]+1"
                            if keypad_mode
                            else -1
                        )
                        intentions.append(
                            _call(
                                "a.a:(II)V",
                                current_index,
                                frame,
                                1,
                            )
                        )
                    progress = prompt.progress + 1
                    prompt = replace(prompt, progress=progress)
                    next_state = replace(
                        next_state, multiple_prompt=prompt
                    )
                    trace.extend(
                        (
                            f"observe:input-mask={input_mask}",
                            f"write:this.cc[4]={progress}",
                        )
                    )
                    if progress == len(prompt.options):
                        append_cleanup()
                elif cancelled:
                    if len(poll.widgets) < len(prompt.options):
                        raise ValueError(
                            "sequence cancellation requires widget "
                            "presence observations for every option"
                        )
                    for index in range(len(prompt.options)):
                        if (
                            index < len(poll.widgets)
                            and poll.widgets[index] is not None
                        ):
                            if keypad_mode:
                                frame = (
                                    f"ct[{prompt.options[index]}]+2"
                                )
                                intentions.append(
                                    _call(
                                        "a.a:(II)V",
                                        index,
                                        frame,
                                        -1,
                                    )
                                )
                            else:
                                # This repeated current-index update is the
                                # exact bytecode quirk, not a normalization.
                                intentions.append(
                                    _call(
                                        "a.a:(II)V",
                                        current_index,
                                        -1,
                                        1,
                                    )
                                )
                    prompt = replace(prompt, progress=-1)
                    next_state = replace(
                        next_state, multiple_prompt=prompt
                    )
                    trace.append("write:this.cc[4]=-1")
                    append_cleanup()
                else:
                    trace.append("sequence-prompt:poll-no-transition")
            else:
                trace.append("sequence-prompt:inactive-progress")
            return finish(
                next_state, boundary="future-multi-prompt-poll"
            )
        if current_tick == event_tick:
            append_cleanup()
            if prompt.progress == len(prompt.options):
                branch_id = operands.branch_id_0
            elif prompt.progress < len(prompt.options):
                branch_id = operands.branch_id_1
            else:
                branch_id = 0
            if branch_id > 0:
                append_script_switch(branch_id)
                next_state = replace(next_state, multiple_prompt=None)
                trace.append("write:this.cc=null")
                trace.append("return:-1")
                return finish(
                    next_state,
                    boundary="exact-multi-prompt-branch",
                    return_value=-1,
                )
            next_state = replace(next_state, multiple_prompt=None)
            trace.append("write:this.cc=null")
            trace.append("sequence-prompt:exact-no-branch")
            return finish(
                next_state, boundary="exact-multi-prompt-no-branch"
            )
        raise AssertionError("opcode 113 tick partition is exhaustive")

    assert decoded.opcode == 114
    assert isinstance(operands, Op114Operands)
    if context.level_index is None:
        raise ValueError("level_index observation is required")
    if context.localized_text is None:
        raise ValueError("localized_text observation is required")
    localized_text = context.localized_text.value
    intentions.append(
        _call(
            "k.d:(II)Ljava/lang/String;",
            _java_i32(1 + context.level_index),
            operands.value_0,
            result=localized_text,
        )
    )
    next_state = replace(
        next_state,
        timed_text=TimedTextState(
            localized_text,
            operands.value_1,
        ),
    )
    trace.extend(("write:k.aP=localized-text", "write:k.aO=duration"))
    return finish(next_state, boundary="exact-timed-text")
