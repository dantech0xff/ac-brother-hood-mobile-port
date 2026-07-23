#!/usr/bin/env python3
"""Clean-room executable contracts for proven legacy gameplay invariants.

This module is an offline specification, not recovered source and not a game
runtime.  It models deliberately small slices observed in static bytecode:
entity materialization and storage, lookup precedence, render/interaction
ordering, fixed-point integration, and the scheduling boundary of the timeline
executor.  It never opens the JAR, loads target classes, or invokes MIDlet code.
"""

from __future__ import annotations

from collections.abc import Mapping
from dataclasses import dataclass, replace
from typing import Sequence


JAVA_INT_MIN = -(1 << 31)
JAVA_INT_MAX = (1 << 31) - 1
JAVA_SHORT_MIN = -(1 << 15)
JAVA_SHORT_MAX = (1 << 15) - 1
MIN_ENTITY_FIELDS = 7
MAX_ENTITY_FIELDS = 25
LEGACY_ENTITY_CAPACITY = 1000
SNAPSHOT_NOT_TRACKED = -98
SNAPSHOT_DELETED = -99


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


def _require_bool(value: object, label: str) -> bool:
    if not isinstance(value, bool):
        raise ValueError(f"{label} must be a boolean")
    return value


def java_i32(value: int) -> int:
    """Wrap an integer exactly as a JVM 32-bit ``int`` operation does."""

    return ((value + (1 << 31)) & 0xFFFF_FFFF) - (1 << 31)


def java_i16(value: int) -> int:
    """Wrap an integer exactly as a JVM ``i2s`` conversion does."""

    return ((value + (1 << 15)) & 0xFFFF) - (1 << 15)


def java_i8(value: int) -> int:
    """Wrap an integer exactly as a JVM signed byte value."""

    return ((value + (1 << 7)) & 0xFF) - (1 << 7)


def java_divide(dividend: int, divisor: int) -> int:
    """Execute JVM signed ``int`` division without using floating point."""

    dividend = _require_java_int(dividend, "dividend")
    divisor = _require_java_int(divisor, "divisor")
    if divisor == 0:
        raise ZeroDivisionError("Java int division by zero")
    if dividend == JAVA_INT_MIN and divisor == -1:
        return JAVA_INT_MIN
    quotient = abs(dividend) // abs(divisor)
    if (dividend < 0) != (divisor < 0):
        quotient = -quotient
    return java_i32(quotient)


def java_remainder(dividend: int, divisor: int) -> int:
    """Execute JVM signed ``int`` remainder, including ``MIN_VALUE % -1``."""

    dividend = _require_java_int(dividend, "dividend")
    divisor = _require_java_int(divisor, "divisor")
    if divisor == 0:
        raise ZeroDivisionError("Java int remainder by zero")
    if dividend == JAVA_INT_MIN and divisor == -1:
        return 0
    return java_i32(dividend - (java_divide(dividend, divisor) * divisor))


def _java_add(left: int, right: int) -> int:
    return java_i32(left + right)


def _java_subtract(left: int, right: int) -> int:
    return java_i32(left - right)


def _java_shift_left(value: int, distance: int) -> int:
    return java_i32(value << (distance & 0x1F))


def remap_runtime_type(raw_type: int, subtype: int) -> int:
    """Apply the two proven post-sprite-selection entity type rewrites."""

    raw_type = _require_java_short(raw_type, "raw_type")
    subtype = _require_java_short(subtype, "subtype")
    if raw_type == 11 and subtype in (80, 93):
        return 47
    if raw_type == 17 and subtype == 120:
        return 50
    return raw_type


@dataclass(frozen=True)
class MaterializedEntity:
    raw_type: int
    runtime_type: int
    legacy_id: int
    pixel_x: int
    pixel_y: int
    fixed_x: int
    fixed_y: int
    subtype: int
    flags: int
    facing_bit_set: bool
    constructor_symbol: str
    world_destination: str
    sprite_selector_raw_type: int | None
    sprite_selector_registry: str | None
    sprite_selector_value: int | None
    sprite_selector_field_index: int | None
    player_fsm_symbol: str | None
    remap_applied: bool


def materialize_entity(fields: Sequence[int]) -> MaterializedEntity:
    """Project a decoded ``short[]`` record through the proven load order.

    The sprite-selector value intentionally retains the raw discriminator.  The
    runtime remap happens only afterward, matching ``i.<init>(short[])``.
    Registry lookup itself is outside this first contract slice, but the exact
    registry/value selection is retained because several raw types select it
    from a type-specific record field.
    """

    if isinstance(fields, (str, bytes, bytearray)):
        raise ValueError("fields must be a sequence of signed Java shorts")
    values = tuple(fields)
    if len(values) < MIN_ENTITY_FIELDS or len(values) > MAX_ENTITY_FIELDS:
        raise ValueError(
            f"fields must contain {MIN_ENTITY_FIELDS}..{MAX_ENTITY_FIELDS} values"
        )
    values = tuple(
        _require_java_short(value, f"fields[{index}]")
        for index, value in enumerate(values)
    )

    raw_type = values[0]
    legacy_id = values[1]
    pixel_x = values[2]
    pixel_y = values[3]
    subtype = values[5]
    flags = values[6]

    if raw_type in (0, 25):
        constructor_symbol = "g.<init>:([S)V"
        world_destination = "player-singleton-if-empty"
    elif raw_type == 55:
        constructor_symbol = "c.a:([S)V"
        world_destination = "waypoint-registry"
    else:
        constructor_symbol = "i.<init>:([S)V"
        world_destination = "entity-slots"

    special_sprite_selectors = {
        67: (7, "bk"),
        46: (10, "bl"),
        7: (8, "bm"),
        56: (7, "bj"),
        9: (8, "bn"),
    }
    if raw_type == 55:
        sprite_selector_raw_type = None
        sprite_selector_registry = None
        sprite_selector_value = None
        sprite_selector_field_index = None
    elif raw_type in special_sprite_selectors:
        sprite_selector_field_index, sprite_selector_registry = (
            special_sprite_selectors[raw_type]
        )
        if len(values) <= sprite_selector_field_index:
            raise ValueError(
                f"raw type {raw_type} requires fields[{sprite_selector_field_index}]"
            )
        sprite_selector_raw_type = raw_type
        sprite_selector_value = values[sprite_selector_field_index]
    else:
        sprite_selector_raw_type = raw_type
        sprite_selector_registry = "bi"
        sprite_selector_value = raw_type
        sprite_selector_field_index = None

    # In the legacy constructor this assignment occurs only after sprite
    # selection, so keep the order visible in this executable specification.
    runtime_type = remap_runtime_type(raw_type, subtype)
    player_fsm_symbol = {
        0: "g.e:()V",
        25: "g.n:()V",
    }.get(runtime_type)

    return MaterializedEntity(
        raw_type=raw_type,
        runtime_type=runtime_type,
        legacy_id=legacy_id,
        pixel_x=pixel_x,
        pixel_y=pixel_y,
        fixed_x=_java_shift_left(pixel_x, 8),
        fixed_y=_java_shift_left(pixel_y, 8),
        subtype=subtype,
        flags=flags,
        facing_bit_set=(flags & 1) != 0,
        constructor_symbol=constructor_symbol,
        world_destination=world_destination,
        sprite_selector_raw_type=sprite_selector_raw_type,
        sprite_selector_registry=sprite_selector_registry,
        sprite_selector_value=sprite_selector_value,
        sprite_selector_field_index=sprite_selector_field_index,
        player_fsm_symbol=player_fsm_symbol,
        remap_applied=runtime_type != raw_type,
    )


@dataclass(frozen=True)
class LegacyEntityRef:
    token: str
    legacy_id: int

    def __post_init__(self) -> None:
        if not isinstance(self.token, str) or not self.token:
            raise ValueError("token must be a nonempty string")
        _require_java_int(self.legacy_id, "legacy_id")


def find_entity_by_legacy_id(
    legacy_id: int,
    player: LegacyEntityRef | None,
    slots: Sequence[LegacyEntityRef | None],
    high_water: int,
) -> LegacyEntityRef | None:
    """Model ``k.q(int)``: ``-1`` guard, player first, then live slots."""

    legacy_id = _require_java_int(legacy_id, "legacy_id")
    if legacy_id == -1:
        return None
    if player is not None:
        if not isinstance(player, LegacyEntityRef):
            raise ValueError("player must be a LegacyEntityRef or None")
        if player.legacy_id == legacy_id:
            return player

    high_water = _require_plain_int(high_water, "high_water")
    if high_water < 0 or high_water > len(slots):
        raise ValueError("high_water must be within the slot sequence")
    for index in range(high_water):
        entity = slots[index]
        if entity is not None and not isinstance(entity, LegacyEntityRef):
            raise ValueError(f"slots[{index}] must be a LegacyEntityRef or None")
        if entity is not None and entity.legacy_id == legacy_id:
            return entity
    return None


@dataclass(eq=False)
class StoredEntityRef:
    """Mutable host reference for the fields touched by ``k.b``/``k.c``."""

    token: str
    legacy_id: int
    snapshot_index: int = SNAPSHOT_NOT_TRACKED

    def __post_init__(self) -> None:
        if not isinstance(self.token, str) or not self.token:
            raise ValueError("token must be a nonempty string")
        _require_java_int(self.legacy_id, "legacy_id")
        _require_java_int(self.snapshot_index, "snapshot_index")


@dataclass(frozen=True)
class EntityStoreState:
    """Valid-state projection of ``ba/bb/bc/ea/eb/bg`` and two globals."""

    capacity: int
    slots: tuple[StoredEntityRef | None, ...]
    high_water: int
    free_slots: tuple[int, ...]
    snapshot_status: tuple[int, ...]
    active_tracking_entity: StoredEntityRef | None = None
    focus_entity: StoredEntityRef | None = None
    tracking_counters: tuple[int, int, int, int] = (0, 0, 0, 0)

    def __post_init__(self) -> None:
        capacity = _require_plain_int(self.capacity, "capacity")
        if capacity <= 0:
            raise ValueError("capacity must be positive")
        object.__setattr__(self, "slots", tuple(self.slots))
        object.__setattr__(self, "free_slots", tuple(self.free_slots))
        object.__setattr__(self, "snapshot_status", tuple(self.snapshot_status))
        object.__setattr__(self, "tracking_counters", tuple(self.tracking_counters))
        if len(self.slots) != capacity:
            raise ValueError("slots length must equal capacity")
        if len(self.snapshot_status) != capacity:
            raise ValueError("snapshot_status length must equal capacity")
        high_water = _require_plain_int(self.high_water, "high_water")
        if high_water < 0 or high_water > capacity:
            raise ValueError("high_water must be within capacity")
        if any(slot is not None for slot in self.slots[high_water:]):
            raise ValueError("slots above high_water must be empty")
        for index, slot in enumerate(self.slots[:high_water]):
            if slot is not None and not isinstance(slot, StoredEntityRef):
                raise ValueError(
                    f"slots[{index}] must be a StoredEntityRef or None"
                )
        free_slots: list[int] = []
        for stack_index, slot_index in enumerate(self.free_slots):
            slot_index = _require_plain_int(
                slot_index, f"free_slots[{stack_index}]"
            )
            if slot_index < 0 or slot_index >= high_water:
                raise ValueError("free slot must be below high_water")
            if self.slots[slot_index] is not None:
                raise ValueError("free slot must reference an empty slot")
            free_slots.append(slot_index)
        if len(free_slots) != len(set(free_slots)):
            raise ValueError("free_slots must not contain duplicates")
        holes = {
            index
            for index, slot in enumerate(self.slots[:high_water])
            if slot is None
        }
        if set(free_slots) != holes:
            raise ValueError("free_slots must describe every hole below high_water")
        for index, value in enumerate(self.snapshot_status):
            value = _require_plain_int(value, f"snapshot_status[{index}]")
            if value < -128 or value > 127:
                raise ValueError("snapshot_status values must fit a Java byte")
        for label, entity in (
            ("active_tracking_entity", self.active_tracking_entity),
            ("focus_entity", self.focus_entity),
        ):
            if entity is not None and not isinstance(entity, StoredEntityRef):
                raise ValueError(
                    f"{label} must be a StoredEntityRef or None"
                )
        if len(self.tracking_counters) != 4:
            raise ValueError("tracking_counters must contain R/S/T/U")
        for index, value in enumerate(self.tracking_counters):
            _require_java_int(value, f"tracking_counters[{index}]")


@dataclass(frozen=True)
class EntityStoreOperationResult:
    """Observable result for a legacy ``void`` mutation."""

    state: EntityStoreState
    entity: StoredEntityRef | None
    outcome: str
    slot_index: int | None
    trace: tuple[str, ...]


def add_entity(
    state: EntityStoreState,
    entity: StoredEntityRef,
) -> EntityStoreOperationResult:
    """Model ``k.b(i)`` including LIFO reuse and the silent full-store drop."""

    if not isinstance(state, EntityStoreState):
        raise ValueError("state must be an EntityStoreState")
    if not isinstance(entity, StoredEntityRef):
        raise TypeError("legacy add dereferences a non-null entity")

    entity.snapshot_index = SNAPSHOT_NOT_TRACKED
    stored_entity = entity
    trace = [f"set-snapshot-index:{stored_entity.token}={SNAPSHOT_NOT_TRACKED}"]
    slots = list(state.slots)
    if state.free_slots:
        slot_index = state.free_slots[-1]
        trace.append(f"pop-free-slot:{slot_index}")
        slots[slot_index] = stored_entity
        trace.append(f"write-slot:{slot_index}")
        next_state = replace(
            state,
            slots=tuple(slots),
            free_slots=state.free_slots[:-1],
        )
        return EntityStoreOperationResult(
            state=next_state,
            entity=stored_entity,
            outcome="reused-free-slot",
            slot_index=slot_index,
            trace=tuple(trace),
        )

    if state.high_water >= state.capacity:
        trace.append("drop-full-store")
        return EntityStoreOperationResult(
            state=state,
            entity=stored_entity,
            outcome="dropped-full",
            slot_index=None,
            trace=tuple(trace),
        )

    slot_index = state.high_water
    trace.append(f"reserve-high-water:{slot_index}")
    slots[slot_index] = stored_entity
    trace.append(f"write-slot:{slot_index}")
    next_state = replace(
        state,
        slots=tuple(slots),
        high_water=slot_index + 1,
    )
    return EntityStoreOperationResult(
        state=next_state,
        entity=stored_entity,
        outcome="appended",
        slot_index=slot_index,
        trace=tuple(trace),
    )


def remove_entity(
    state: EntityStoreState,
    entity: StoredEntityRef | None,
) -> EntityStoreOperationResult:
    """Model ``k.c(i)`` through the normal-return boundary of ``i.p()``.

    The cleanup implementation is intentionally not reproduced.  Its exact
    call position appears in ``trace``; this contract assumes it returns
    normally before the slot is cleared and pushed onto the free stack.
    """

    if not isinstance(state, EntityStoreState):
        raise ValueError("state must be an EntityStoreState")
    if entity is None:
        return EntityStoreOperationResult(
            state=state,
            entity=None,
            outcome="null-noop",
            slot_index=None,
            trace=("return-null",),
        )
    if not isinstance(entity, StoredEntityRef):
        raise ValueError("entity must be a StoredEntityRef or None")

    active_tracking_entity = state.active_tracking_entity
    focus_entity = state.focus_entity
    tracking_counters = state.tracking_counters
    trace: list[str] = []
    if active_tracking_entity is entity:
        active_tracking_entity = None
        tracking_counters = (0, 0, 0, 0)
        trace.append(f"reset-active-tracking:{entity.token}")
    if focus_entity is entity:
        focus_entity = None
        trace.append(f"clear-focus:{entity.token}")

    slots = list(state.slots)
    slot_index = next(
        (
            index
            for index in range(state.high_water)
            if slots[index] is entity
        ),
        None,
    )
    if slot_index is None:
        next_state = replace(
            state,
            active_tracking_entity=active_tracking_entity,
            focus_entity=focus_entity,
            tracking_counters=tracking_counters,
        )
        trace.append("return-not-found")
        return EntityStoreOperationResult(
            state=next_state,
            entity=entity,
            outcome="not-found",
            slot_index=None,
            trace=tuple(trace),
        )

    snapshot_status = list(state.snapshot_status)
    if entity.snapshot_index != SNAPSHOT_NOT_TRACKED:
        if (
            entity.snapshot_index < 0
            or entity.snapshot_index >= len(snapshot_status)
        ):
            raise IndexError("legacy snapshot tombstone index is out of bounds")
        snapshot_status[entity.snapshot_index] = SNAPSHOT_DELETED
        trace.append(
            f"write-tombstone:{entity.snapshot_index}={SNAPSHOT_DELETED}"
        )
    trace.append(f"invoke-cleanup:i.p:()V:{entity.token}")
    slots[slot_index] = None
    trace.append(f"clear-slot:{slot_index}")
    free_slots = state.free_slots + (slot_index,)
    trace.append(f"push-free-slot:{slot_index}")
    next_state = EntityStoreState(
        capacity=state.capacity,
        slots=tuple(slots),
        high_water=state.high_water,
        free_slots=free_slots,
        snapshot_status=tuple(snapshot_status),
        active_tracking_entity=active_tracking_entity,
        focus_entity=focus_entity,
        tracking_counters=tracking_counters,
    )
    return EntityStoreOperationResult(
        state=next_state,
        entity=entity,
        outcome="removed",
        slot_index=slot_index,
        trace=tuple(trace),
    )


@dataclass(frozen=True)
class RenderInteractionRef:
    """The two ordering keys read by ``k.d(i)`` plus an identity token."""

    token: str
    depth: int
    world_y: int

    def __post_init__(self) -> None:
        if not isinstance(self.token, str) or not self.token:
            raise ValueError("token must be a nonempty string")
        _require_java_int(self.depth, "depth")
        _require_java_int(self.world_y, "world_y")


def insert_render_interaction(
    entries: Sequence[RenderInteractionRef],
    entity: RenderInteractionRef,
    capacity: int = LEGACY_ENTITY_CAPACITY,
) -> tuple[RenderInteractionRef, ...]:
    """Insert using ``az`` then ``al``; exact ties place the new item first."""

    if isinstance(entries, (str, bytes, bytearray)):
        raise ValueError("entries must be a sequence of render references")
    current = tuple(entries)
    for index, entry in enumerate(current):
        if not isinstance(entry, RenderInteractionRef):
            raise ValueError(
                f"entries[{index}] must be a RenderInteractionRef"
            )
    if not isinstance(entity, RenderInteractionRef):
        raise ValueError("entity must be a RenderInteractionRef")
    capacity = _require_plain_int(capacity, "capacity")
    if capacity < 0:
        raise ValueError("capacity must not be negative")
    if len(current) >= capacity:
        raise IndexError("legacy render interaction list capacity exceeded")

    insertion_index = 0
    while (
        insertion_index < len(current)
        and current[insertion_index].depth < entity.depth
    ):
        insertion_index += 1
    while (
        insertion_index < len(current)
        and current[insertion_index].depth == entity.depth
        and entity.world_y > current[insertion_index].world_y
    ):
        insertion_index += 1
    return (
        current[:insertion_index]
        + (entity,)
        + current[insertion_index:]
    )


def rebuild_render_interaction_list(
    candidates: Sequence[RenderInteractionRef],
    capacity: int = LEGACY_ENTITY_CAPACITY,
) -> tuple[RenderInteractionRef, ...]:
    """Model the per-frame ``be = 0`` rebuild followed by ordered inserts."""

    if isinstance(candidates, (str, bytes, bytearray)):
        raise ValueError("candidates must be a sequence of render references")
    entries: tuple[RenderInteractionRef, ...] = ()
    for candidate in candidates:
        entries = insert_render_interaction(entries, candidate, capacity)
    return entries


@dataclass(frozen=True)
class CoordinateState:
    fixed_x: int
    fixed_y: int
    pixel_x: int
    pixel_y: int
    velocity_x: int
    velocity_y: int
    acceleration_x: int
    acceleration_y: int

    def __post_init__(self) -> None:
        for field_name in self.__dataclass_fields__:
            _require_java_int(getattr(self, field_name), field_name)


def reconcile_and_integrate_coordinates(
    state: CoordinateState,
    slow_divisor: int | None = None,
) -> CoordinateState:
    """Execute the proven normal or slow 8.8 fixed-point block from ``i.I()``.

    Order is significant: reconcile integer coordinates into fixed position,
    apply the old velocity contribution, accumulate the acceleration
    contribution into next-tick velocity, clear acceleration, then project fixed
    position back to integer pixels.  ``slow_divisor=None`` preserves the
    original public normal-path call.  An integer divisor selects the bytecode
    branch that applies ``idiv`` separately to velocity and acceleration.
    """

    if not isinstance(state, CoordinateState):
        raise ValueError("state must be a CoordinateState")
    if slow_divisor is not None:
        slow_divisor = _require_java_int(slow_divisor, "slow_divisor")

    fixed_x = _java_add(
        state.fixed_x,
        _java_shift_left(
            _java_subtract(state.pixel_x, state.fixed_x >> 8),
            8,
        ),
    )
    fixed_y = _java_add(
        state.fixed_y,
        _java_shift_left(
            _java_subtract(state.pixel_y, state.fixed_y >> 8),
            8,
        ),
    )
    if slow_divisor is None:
        velocity_contribution_x = state.velocity_x
        velocity_contribution_y = state.velocity_y
        acceleration_contribution_x = state.acceleration_x
        acceleration_contribution_y = state.acceleration_y
    else:
        velocity_contribution_x = java_divide(
            state.velocity_x, slow_divisor
        )
        velocity_contribution_y = java_divide(
            state.velocity_y, slow_divisor
        )
        acceleration_contribution_x = java_divide(
            state.acceleration_x, slow_divisor
        )
        acceleration_contribution_y = java_divide(
            state.acceleration_y, slow_divisor
        )
    fixed_x = _java_add(fixed_x, velocity_contribution_x)
    fixed_y = _java_add(fixed_y, velocity_contribution_y)
    velocity_x = _java_add(state.velocity_x, acceleration_contribution_x)
    velocity_y = _java_add(state.velocity_y, acceleration_contribution_y)

    return CoordinateState(
        fixed_x=fixed_x,
        fixed_y=fixed_y,
        pixel_x=fixed_x >> 8,
        pixel_y=fixed_y >> 8,
        velocity_x=velocity_x,
        velocity_y=velocity_y,
        acceleration_x=0,
        acceleration_y=0,
    )


def find_script_group_index(
    script_id: int,
    script_ids: Sequence[int],
) -> int:
    """Model ``k.s(int)`` as a first-match linear scan over ``short[] eH``."""

    script_id = _require_java_int(script_id, "script_id")
    if isinstance(script_ids, (str, bytes, bytearray)):
        raise ValueError("script_ids must be a sequence of Java shorts")
    for index, candidate in enumerate(script_ids):
        candidate = _require_java_short(candidate, f"script_ids[{index}]")
        if candidate == script_id:
            return index
    return -1


def is_timeline_script_active(
    active_group_index: int,
    paused: bool,
    current_tick: int,
) -> bool:
    """Model ``i.ab()`` exactly: group present, unpaused, nonnegative tick."""

    active_group_index = _require_java_int(
        active_group_index, "active_group_index"
    )
    paused = _require_bool(paused, "paused")
    current_tick = _require_java_short(current_tick, "current_tick")
    return active_group_index >= 0 and not paused and current_tick >= 0


@dataclass(frozen=True)
class TimelineEvent:
    """Normalized decoded event used only for scheduler-boundary parity."""

    tick: int
    opcodes: tuple[int, ...]

    def __post_init__(self) -> None:
        _require_java_short(self.tick, "tick")
        object.__setattr__(self, "opcodes", tuple(self.opcodes))
        for index, opcode in enumerate(self.opcodes):
            opcode = _require_plain_int(opcode, f"opcodes[{index}]")
            if opcode < 0 or opcode > 255:
                raise ValueError("timeline opcodes must fit an unsigned byte")


@dataclass(frozen=True)
class TimelineCursorState:
    """Provable subset of ``ca``, ``cd[0]``, ``cK``, ``cd[1]``, and ``cL``."""

    active_group_index: int
    paused: bool
    current_tick: int
    advance_latched: bool
    lane_event_indexes: tuple[int, ...]

    def __post_init__(self) -> None:
        _require_java_int(self.active_group_index, "active_group_index")
        _require_bool(self.paused, "paused")
        _require_java_short(self.current_tick, "current_tick")
        _require_bool(self.advance_latched, "advance_latched")
        object.__setattr__(
            self, "lane_event_indexes", tuple(self.lane_event_indexes)
        )
        for index, event_index in enumerate(self.lane_event_indexes):
            event_index = _require_plain_int(
                event_index, f"lane_event_indexes[{index}]"
            )
            if event_index < 0:
                raise ValueError("lane event indexes must not be negative")


@dataclass(frozen=True)
class TimelineDispatch:
    lane_index: int
    event_index: int
    tick: int
    opcode: int
    dispatch_path: str


@dataclass(frozen=True)
class TimelineStepResult:
    """State after scheduling, before the broad ``i.bI()`` completion effects."""

    state: TimelineCursorState
    evaluated_tick: int | None
    dispatches: tuple[TimelineDispatch, ...]
    completion_requested: bool
    execution_aborted: bool
    trace: tuple[str, ...]


def classify_timeline_opcode(opcode: int) -> str:
    """Expose only the proven inline-low versus extended-executor boundary."""

    opcode = _require_plain_int(opcode, "opcode")
    if opcode < 0 or opcode > 255:
        raise ValueError("opcode must fit an unsigned byte")
    return "inline-low" if java_i8(opcode) < 100 else "extended"


def step_timeline_script(
    state: TimelineCursorState,
    lanes: Sequence[Sequence[TimelineEvent]],
    *,
    slow_time_enabled: bool,
    slow_divisor: int,
    frame_counter: int,
    extended_dispatch_results: Mapping[tuple[int, int, int], int] | None = None,
) -> TimelineStepResult:
    """Model the scheduler/cursor boundary at the front of ``i.aa()``.

    This function intentionally stops before gameplay side effects.  It
    identifies the old tick evaluated this call, dispatches the current event
    of each lane even when its timestamp is still in the future, and advances
    at most one due cursor per lane.  Individual opcode handlers may apply
    their own time guard; those effects remain outside this boundary contract.
    If every lane is exhausted it reports where legacy code invokes ``bI()``.
    Opcodes 108 and 113 can make the extended executor return ``-1`` at the
    exact event tick.  Their result must therefore be supplied by
    ``extended_dispatch_results[(lane, event, opcode_index)]``; a negative
    result reproduces the immediate return before that lane cursor advances.
    """

    if not isinstance(state, TimelineCursorState):
        raise ValueError("state must be a TimelineCursorState")
    if state.paused:
        return TimelineStepResult(
            state=state,
            evaluated_tick=None,
            dispatches=(),
            completion_requested=False,
            execution_aborted=False,
            trace=("return-paused",),
        )
    if state.current_tick < 0:
        return TimelineStepResult(
            state=state,
            evaluated_tick=None,
            dispatches=(),
            completion_requested=False,
            execution_aborted=False,
            trace=("skip-negative-tick",),
        )
    if state.active_group_index < 0:
        raise IndexError("legacy timeline group index is negative")

    slow_time_enabled = _require_bool(
        slow_time_enabled, "slow_time_enabled"
    )
    if extended_dispatch_results is None:
        extended_dispatch_results = {}
    if not isinstance(extended_dispatch_results, Mapping):
        raise ValueError("extended_dispatch_results must be a mapping")
    normalized_extended_results: dict[tuple[int, int, int], int] = {}
    for key, result in extended_dispatch_results.items():
        if (
            not isinstance(key, tuple)
            or len(key) != 3
            or any(
                not isinstance(part, int)
                or isinstance(part, bool)
                or part < 0
                for part in key
            )
        ):
            raise ValueError(
                "extended result keys must be nonnegative "
                "(lane, event, opcode_index) tuples"
            )
        normalized_extended_results[key] = _require_java_int(
            result, f"extended_dispatch_results[{key!r}]"
        )
    if isinstance(lanes, (str, bytes, bytearray)):
        raise ValueError("lanes must be a sequence of event sequences")
    normalized_lanes = tuple(tuple(lane) for lane in lanes)
    if len(normalized_lanes) != len(state.lane_event_indexes):
        raise ValueError("lane cursor count must match lane count")
    for lane_index, lane in enumerate(normalized_lanes):
        for event_index, event in enumerate(lane):
            if not isinstance(event, TimelineEvent):
                raise ValueError(
                    f"lanes[{lane_index}][{event_index}] must be a TimelineEvent"
                )
        if state.lane_event_indexes[lane_index] > len(lane):
            raise ValueError("lane event index exceeds event count")

    evaluated_tick = state.current_tick
    next_tick = state.current_tick
    trace: list[str] = [f"evaluate-tick:{evaluated_tick}"]
    should_advance = state.advance_latched or not slow_time_enabled
    if state.advance_latched:
        trace.append("advance-latched")
    elif not slow_time_enabled:
        trace.append("advance-normal")
    else:
        slow_divisor = _require_java_int(slow_divisor, "slow_divisor")
        frame_counter = _require_java_int(frame_counter, "frame_counter")
        if java_remainder(frame_counter, slow_divisor) == 0:
            should_advance = True
            trace.append("advance-slow-gate")
        else:
            trace.append("hold-slow-gate")
    if should_advance:
        next_tick = java_i16(state.current_tick + 1)

    lane_event_indexes = list(state.lane_event_indexes)
    dispatches: list[TimelineDispatch] = []
    for lane_index, lane in enumerate(normalized_lanes):
        event_index = lane_event_indexes[lane_index]
        if event_index >= len(lane):
            continue
        event = lane[event_index]
        trace.append(
            f"dispatch-event:{lane_index}:{event_index}@{event.tick}"
        )
        for opcode_index, opcode in enumerate(event.opcodes):
            dispatches.append(
                TimelineDispatch(
                    lane_index=lane_index,
                    event_index=event_index,
                    tick=event.tick,
                    opcode=opcode,
                    dispatch_path=classify_timeline_opcode(opcode),
                )
            )
            signed_opcode = java_i8(opcode)
            if signed_opcode >= 100:
                result_key = (lane_index, event_index, opcode_index)
                can_abort = (
                    signed_opcode in (108, 113)
                    and event.tick == evaluated_tick
                )
                if can_abort and result_key not in normalized_extended_results:
                    raise ValueError(
                        "exact-tick opcode 108/113 requires an "
                        "extended dispatch result"
                    )
                extended_result = normalized_extended_results.get(
                    result_key, 0
                )
                if extended_result < 0 and not can_abort:
                    raise ValueError(
                        "a negative extended result is only valid for "
                        "exact-tick opcode 108/113"
                    )
                if extended_result < 0:
                    trace.append(
                        "abort-extended-dispatch:"
                        f"{lane_index}:{event_index}:{opcode_index}"
                    )
                    return TimelineStepResult(
                        state=replace(
                            state,
                            current_tick=next_tick,
                            lane_event_indexes=tuple(lane_event_indexes),
                        ),
                        evaluated_tick=evaluated_tick,
                        dispatches=tuple(dispatches),
                        completion_requested=False,
                        execution_aborted=True,
                        trace=tuple(trace),
                    )
        if event.tick <= evaluated_tick:
            lane_event_indexes[lane_index] = event_index + 1
            trace.append(
                f"advance-cursor-due:{lane_index}:{event_index}@{event.tick}"
            )
        else:
            trace.append(
                f"hold-future-event:{lane_index}:{event_index}@{event.tick}"
            )

    completion_requested = all(
        lane_event_indexes[index] >= len(lane)
        for index, lane in enumerate(normalized_lanes)
    )
    if completion_requested:
        trace.append("request-completion:i.bI:()V")
    next_state = replace(
        state,
        current_tick=next_tick,
        lane_event_indexes=tuple(lane_event_indexes),
    )
    return TimelineStepResult(
        state=next_state,
        evaluated_tick=evaluated_tick,
        dispatches=tuple(dispatches),
        completion_requested=completion_requested,
        execution_aborted=False,
        trace=tuple(trace),
    )
