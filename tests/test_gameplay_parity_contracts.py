from __future__ import annotations

import hashlib
import importlib.util
import json
import struct
import sys
import unittest
from collections import Counter
from dataclasses import asdict
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
SCRIPTS_ROOT = REPOSITORY_ROOT / "scripts"
FIXTURE_PATH = Path(__file__).parent / "fixtures" / "gameplay-parity-contracts.json"
LEVELS_ROOT = REPOSITORY_ROOT / "reconstructed-project" / "resources" / "levels-decoded"
DECODED_ROOT = REPOSITORY_ROOT / "reconstructed-project" / "resources" / "decoded"
CONFIDENCE_VALUES = {"proven", "high-confidence", "inferred", "unknown"}
BASIS_VALUES = {
    "corpus",
    "synthetic-derived-from-corpus",
    "synthetic-source-contract",
}


def load_script_module(module_name: str, filename: str):
    path = SCRIPTS_ROOT / filename
    spec = importlib.util.spec_from_file_location(module_name, path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot import {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module


contracts = load_script_module(
    "gameplay_parity_contracts", "gameplay_parity_contracts.py"
)
level_decoder = load_script_module(
    "decode_gameloft_level_records", "decode-gameloft-level-records.py"
)


def fixture_manifest() -> dict:
    return json.loads(FIXTURE_PATH.read_text(encoding="utf-8"))


def record_values(record: dict) -> list[int]:
    return [field["java_i16"] for field in record["fields"]]


def encode_record(fields: list[int]) -> str:
    return (bytes([len(fields)]) + struct.pack(f"<{len(fields)}h", *fields)).hex()


def jsonable(value):
    """Normalize tuple-bearing dataclass output for JSON fixture comparison."""

    return json.loads(json.dumps(value))


class FixtureManifestTests(unittest.TestCase):
    def test_manifest_schema_and_evidence_boundaries(self) -> None:
        manifest = fixture_manifest()
        self.assertEqual(manifest["schema_version"], 1)
        self.assertEqual(manifest["analysis_mode"], "static-only")
        self.assertIs(manifest["target_code_execution"], False)
        self.assertEqual(set(manifest["allowed_basis"]), BASIS_VALUES)
        self.assertEqual(
            manifest["harness_parameters"],
            {
                "legacy_entity_capacity": 1000,
                "fixture_entity_capacities": [2, 4],
                "fixture_render_capacities": [3, 10],
                "note": (
                    "Reduced capacities expose ordering and overflow "
                    "deterministically; they do not replace the legacy "
                    "1000-slot constant."
                ),
            },
        )

        fixtures = manifest["fixtures"]
        self.assertEqual(len(fixtures), 30)
        self.assertEqual(
            Counter(fixture["basis"] for fixture in fixtures),
            Counter({
                "corpus": 12,
                "synthetic-derived-from-corpus": 1,
                "synthetic-source-contract": 17,
            }),
        )
        self.assertEqual(
            Counter(fixture["kind"] for fixture in fixtures),
            Counter({
                "entity-materialization": 11,
                "entity-lookup": 4,
                "coordinate-integration": 3,
                "entity-store-lifecycle": 4,
                "render-interaction-order": 2,
                "script-group-lookup": 3,
                "timeline-activity": 1,
                "timeline-scheduling": 2,
            }),
        )

        method_ids = {
            method["id"]
            for method in json.loads(
                (
                    REPOSITORY_ROOT
                    / "reconstructed-project"
                    / "inventory"
                    / "methods.json"
                ).read_text(encoding="utf-8")
            )
        }
        fixture_ids: set[str] = set()
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                self.assertNotIn(fixture["id"], fixture_ids)
                fixture_ids.add(fixture["id"])
                self.assertIn(fixture["basis"], BASIS_VALUES)
                self.assertIn(fixture["confidence"], CONFIDENCE_VALUES)
                self.assertIn(fixture["kind"], {
                    "entity-materialization",
                    "entity-lookup",
                    "coordinate-integration",
                    "entity-store-lifecycle",
                    "render-interaction-order",
                    "script-group-lookup",
                    "timeline-activity",
                    "timeline-scheduling",
                })
                self.assertIn("source", fixture)
                self.assertIn("input", fixture)
                self.assertIn("expected", fixture)
                if fixture["basis"] == "synthetic-source-contract":
                    evidence_path = (
                        REPOSITORY_ROOT / fixture["source"]["evidence"]
                    ).resolve()
                    evidence_path.relative_to(REPOSITORY_ROOT.resolve())
                    self.assertTrue(evidence_path.is_file())
                    source = fixture["source"]
                    symbols = source.get("symbols")
                    if symbols is None:
                        symbols = [source["symbol"]]
                    self.assertTrue(symbols)
                    for symbol in symbols:
                        self.assertIn(symbol, method_ids)

    def test_corpus_and_derived_materialization_sources_are_pinned(self) -> None:
        cache: dict[Path, dict] = {}
        for fixture in fixture_manifest()["fixtures"]:
            if fixture["kind"] != "entity-materialization":
                continue
            source = fixture["source"]
            source_path = (REPOSITORY_ROOT / source["path"]).resolve()
            source_path.relative_to(REPOSITORY_ROOT.resolve())
            self.assertEqual(
                hashlib.sha256(source_path.read_bytes()).hexdigest(),
                source["sha256"],
                fixture["id"],
            )
            if source_path not in cache:
                cache[source_path] = json.loads(
                    source_path.read_text(encoding="utf-8")
                )
            decoded = cache[source_path]
            self.assertEqual(
                decoded["sources"]["slot_0_entities"]["sha256_actual"],
                source["slot_0_sha256"],
                fixture["id"],
            )
            record = decoded["entities"]["records"][source["record_index"]]
            fields = record_values(record)
            if fixture["basis"] == "synthetic-derived-from-corpus":
                for mutation in source["mutations"]:
                    index = mutation["field_index"]
                    self.assertEqual(fields[index], mutation["from"])
                    fields[index] = mutation["to"]
            else:
                self.assertEqual(record["raw_hex"], source["raw_hex"])
            self.assertEqual(fields, fixture["input"]["fields"], fixture["id"])
            self.assertEqual(encode_record(fields), source["raw_hex"], fixture["id"])

    def test_corpus_script_sources_are_pinned(self) -> None:
        for fixture in fixture_manifest()["fixtures"]:
            if fixture["kind"] not in {
                "script-group-lookup",
                "timeline-scheduling",
            } or fixture["basis"] != "corpus":
                continue
            with self.subTest(fixture=fixture["id"]):
                source = fixture["source"]
                source_path = (REPOSITORY_ROOT / source["path"]).resolve()
                source_path.relative_to(REPOSITORY_ROOT.resolve())
                self.assertEqual(
                    hashlib.sha256(source_path.read_bytes()).hexdigest(),
                    source["sha256"],
                )
                decoded = json.loads(source_path.read_text(encoding="utf-8"))
                self.assertEqual(
                    decoded["sources"]["slot_7_scripts"]["sha256_actual"],
                    source["slot_7_sha256"],
                )
                groups = decoded["scripts"]["groups"]
                group = groups[source["group_index"]]
                if fixture["kind"] == "script-group-lookup":
                    self.assertEqual(
                        [
                            item["script_id"]["java_i16"]
                            for item in groups
                        ],
                        fixture["input"]["script_ids"],
                    )
                    self.assertEqual(
                        group["script_id"]["java_i16"],
                        fixture["input"]["script_id"],
                    )
                else:
                    self.assertEqual(group["raw_hex"], source["group_raw_hex"])
                    lane = group["lanes"][source["lane_index"]]
                    event = lane["events"][source["event_index"]]
                    self.assertEqual(event["raw_hex"], source["event_raw_hex"])
                    normalized_lanes = [
                        [
                            {
                                "tick": item["tick"]["java_i16"],
                                "opcodes": [
                                    instruction["opcode"]["raw_u8"]
                                    for instruction in item["instructions"]
                                ],
                            }
                            for item in decoded_lane["events"]
                        ]
                        for decoded_lane in group["lanes"]
                    ]
                    self.assertEqual(
                        normalized_lanes,
                        fixture["input"]["lanes"],
                    )


class EntityMaterializationContractTests(unittest.TestCase):
    def test_materialization_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "entity-materialization"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                actual = contracts.materialize_entity(fixture["input"]["fields"])
                self.assertEqual(asdict(actual), fixture["expected"])

    def test_all_4286_corpus_records_are_consistent_with_decoder_annotations(self) -> None:
        remaps: Counter[int] = Counter()
        record_count = 0
        special_selectors = {
            67: (7, "bk"),
            46: (10, "bl"),
            7: (8, "bm"),
            56: (7, "bj"),
            9: (8, "bn"),
        }
        for pack in level_decoder.PACK_IDS:
            delivered = json.loads(
                (LEVELS_ROOT / f"pack-{pack}" / "records.json").read_text(
                    encoding="utf-8"
                )
            )
            for record in delivered["entities"]["records"]:
                record_count += 1
                interpreted = record["interpreted"]
                actual = contracts.materialize_entity(record_values(record))
                self.assertEqual(actual.raw_type, interpreted["raw_discriminator"])
                self.assertEqual(actual.runtime_type, interpreted["runtime_type"])
                self.assertEqual(actual.legacy_id, interpreted["uid"])
                self.assertEqual(actual.pixel_x, interpreted["x"])
                self.assertEqual(actual.pixel_y, interpreted["y"])
                self.assertEqual(actual.subtype, interpreted["subtype_or_variant"])
                self.assertEqual(actual.flags, interpreted["flags"])
                self.assertEqual(actual.facing_bit_set, interpreted["flag_bit_0_av"])
                if actual.raw_type == 55:
                    self.assertIsNone(actual.sprite_selector_raw_type)
                    self.assertIsNone(actual.sprite_selector_registry)
                    self.assertIsNone(actual.sprite_selector_value)
                    self.assertIsNone(actual.sprite_selector_field_index)
                elif actual.raw_type in special_selectors:
                    field_index, registry = special_selectors[actual.raw_type]
                    self.assertEqual(actual.sprite_selector_raw_type, actual.raw_type)
                    self.assertEqual(actual.sprite_selector_registry, registry)
                    self.assertEqual(actual.sprite_selector_value, record_values(record)[field_index])
                    self.assertEqual(actual.sprite_selector_field_index, field_index)
                    self.assertEqual(
                        interpreted["semantic_annotations"]["sprite_selector"],
                        {
                            "source": "record field",
                            "field_index": field_index,
                            "value": record_values(record)[field_index],
                            "runtime_registry": registry,
                            "confidence": "proven",
                        },
                    )
                else:
                    self.assertEqual(actual.sprite_selector_raw_type, actual.raw_type)
                    self.assertEqual(actual.sprite_selector_registry, "bi")
                    self.assertEqual(actual.sprite_selector_value, actual.raw_type)
                    self.assertIsNone(actual.sprite_selector_field_index)
                expected_constructor = {
                    "g(short[]) / player constructor": "g.<init>:([S)V",
                    "c.a(short[]) / waypoint registry": "c.a:([S)V",
                    "i(short[]) / actor entity": "i.<init>:([S)V",
                }[interpreted["dispatch"]]
                self.assertEqual(actual.constructor_symbol, expected_constructor)
                expected_destination = (
                    "player-singleton-if-empty"
                    if actual.raw_type in (0, 25)
                    else "waypoint-registry"
                    if actual.raw_type == 55
                    else "entity-slots"
                )
                self.assertEqual(actual.world_destination, expected_destination)
                if actual.remap_applied:
                    remaps[actual.runtime_type] += 1

        self.assertEqual(record_count, 4286)
        self.assertEqual(remaps, Counter({47: 12, 50: 8}))

    def test_rejects_non_short_or_truncated_records(self) -> None:
        for fields in (
            [0, 1, 2],
            [0, 1, 2, 3, 4, 5, True],
            [0, 1, 40000, 3, 4, 5, 0],
            [46, 1, 2, 3, 4, 5, 0],
        ):
            with self.subTest(fields=fields):
                with self.assertRaises(ValueError):
                    contracts.materialize_entity(fields)


class EntityLookupContractTests(unittest.TestCase):
    @staticmethod
    def entity(value: dict | None):
        return None if value is None else contracts.LegacyEntityRef(**value)

    def test_lookup_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "entity-lookup"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                inputs = fixture["input"]
                actual = contracts.find_entity_by_legacy_id(
                    inputs["legacy_id"],
                    self.entity(inputs["player"]),
                    [self.entity(slot) for slot in inputs["slots"]],
                    inputs["high_water"],
                )
                self.assertEqual(
                    None if actual is None else asdict(actual), fixture["expected"]
                )

    def test_rejects_invalid_high_water(self) -> None:
        with self.assertRaises(ValueError):
            contracts.find_entity_by_legacy_id(1, None, [], 1)
        with self.assertRaises(ValueError):
            contracts.find_entity_by_legacy_id(1, None, [], -1)

    def test_lookup_short_circuits_before_unrelated_state(self) -> None:
        player = contracts.LegacyEntityRef(token="player", legacy_id=42)
        self.assertIsNone(
            contracts.find_entity_by_legacy_id(-1, object(), [object()], 99)
        )
        self.assertIs(
            contracts.find_entity_by_legacy_id(42, player, [object()], 99),
            player,
        )


class EntityStoreLifecycleContractTests(unittest.TestCase):
    @staticmethod
    def state_json(state) -> dict:
        return {
            "capacity": state.capacity,
            "slots": [
                None if entity is None else entity.token
                for entity in state.slots
            ],
            "high_water": state.high_water,
            "free_slots": list(state.free_slots),
            "snapshot_status": list(state.snapshot_status),
            "active_tracking_token": (
                None
                if state.active_tracking_entity is None
                else state.active_tracking_entity.token
            ),
            "focus_token": (
                None
                if state.focus_entity is None
                else state.focus_entity.token
            ),
            "tracking_counters": list(state.tracking_counters),
        }

    @staticmethod
    def build_state(raw: dict, entities: dict):
        return contracts.EntityStoreState(
            capacity=raw["capacity"],
            slots=tuple(
                None if token is None else entities[token]
                for token in raw["slots"]
            ),
            high_water=raw["high_water"],
            free_slots=tuple(raw["free_slots"]),
            snapshot_status=tuple(raw["snapshot_status"]),
            active_tracking_entity=(
                None
                if raw["active_tracking_token"] is None
                else entities[raw["active_tracking_token"]]
            ),
            focus_entity=(
                None
                if raw["focus_token"] is None
                else entities[raw["focus_token"]]
            ),
            tracking_counters=tuple(raw["tracking_counters"]),
        )

    def test_lifecycle_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "entity-store-lifecycle"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                inputs = fixture["input"]
                entities = {
                    token: contracts.StoredEntityRef(**raw)
                    for token, raw in inputs["entities"].items()
                }
                state = self.build_state(inputs["state"], entities)
                outcomes = []
                for operation in inputs["operations"]:
                    token = operation["token"]
                    if operation["operation"] == "add":
                        result = contracts.add_entity(state, entities[token])
                    else:
                        result = contracts.remove_entity(
                            state, entities[token]
                        )
                    state = result.state
                    if result.entity is not None:
                        entities[token] = result.entity
                    outcomes.append({
                        "operation": operation["operation"],
                        "token": token,
                        "outcome": result.outcome,
                        "slot_index": result.slot_index,
                        "entity_snapshot_index": (
                            None
                            if result.entity is None
                            else result.entity.snapshot_index
                        ),
                        "trace": list(result.trace),
                    })
                self.assertEqual(
                    {
                        "state": self.state_json(state),
                        "outcomes": outcomes,
                    },
                    fixture["expected"],
                )

    def test_null_duplicate_and_tombstone_edges(self) -> None:
        entity = contracts.StoredEntityRef(
            token="same",
            legacy_id=7,
            snapshot_index=1,
        )
        state = contracts.EntityStoreState(
            capacity=3,
            slots=(entity, entity, None),
            high_water=2,
            free_slots=(),
            snapshot_status=(0, 0, 0),
        )
        null_result = contracts.remove_entity(state, None)
        self.assertIs(null_result.state, state)
        self.assertEqual(null_result.outcome, "null-noop")

        removed = contracts.remove_entity(state, entity)
        self.assertIsNone(removed.state.slots[0])
        self.assertEqual(removed.state.slots[1], entity)
        self.assertEqual(removed.state.snapshot_status[1], -99)
        reused = contracts.add_entity(
            removed.state,
            contracts.StoredEntityRef("replacement", 8, 2),
        )
        self.assertEqual(reused.slot_index, 0)
        self.assertEqual(reused.state.snapshot_status[1], -99)

    def test_state_rejects_free_stack_that_does_not_match_holes(self) -> None:
        with self.assertRaises(ValueError):
            contracts.EntityStoreState(
                capacity=2,
                slots=(None, None),
                high_water=1,
                free_slots=(),
                snapshot_status=(0, 0),
            )

    def test_tracked_remove_preserves_legacy_tombstone_bounds_failure(self) -> None:
        entity = contracts.StoredEntityRef(
            token="tracked",
            legacy_id=1,
            snapshot_index=2,
        )
        state = contracts.EntityStoreState(
            capacity=1,
            slots=(entity,),
            high_water=1,
            free_slots=(),
            snapshot_status=(0,),
        )
        with self.assertRaises(IndexError):
            contracts.remove_entity(state, entity)

    def test_reference_identity_is_not_token_or_uid_equality(self) -> None:
        stored = contracts.StoredEntityRef(
            token="collision",
            legacy_id=7,
            snapshot_index=0,
        )
        lookalike = contracts.StoredEntityRef(
            token="collision",
            legacy_id=7,
            snapshot_index=0,
        )
        state = contracts.EntityStoreState(
            capacity=1,
            slots=(stored,),
            high_water=1,
            free_slots=(),
            snapshot_status=(0,),
            active_tracking_entity=stored,
            focus_entity=stored,
            tracking_counters=(1, 2, 3, 4),
        )
        result = contracts.remove_entity(state, lookalike)
        self.assertEqual(result.outcome, "not-found")
        self.assertIs(result.state.slots[0], stored)
        self.assertIs(result.state.active_tracking_entity, stored)
        self.assertIs(result.state.focus_entity, stored)
        self.assertEqual(result.state.snapshot_status, (0,))
        self.assertEqual(result.state.tracking_counters, (1, 2, 3, 4))

        already_stored = contracts.StoredEntityRef(
            token="already-stored",
            legacy_id=8,
            snapshot_index=3,
        )
        full_state = contracts.EntityStoreState(
            capacity=1,
            slots=(already_stored,),
            high_water=1,
            free_slots=(),
            snapshot_status=(0,),
        )
        dropped = contracts.add_entity(full_state, already_stored)
        self.assertEqual(dropped.outcome, "dropped-full")
        self.assertIs(dropped.entity, already_stored)
        self.assertIs(dropped.state.slots[0], already_stored)
        self.assertEqual(already_stored.snapshot_index, -98)


class RenderInteractionOrderContractTests(unittest.TestCase):
    def test_render_order_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "render-interaction-order"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                candidates = [
                    contracts.RenderInteractionRef(**candidate)
                    for candidate in fixture["input"]["candidates"]
                ]
                expected = fixture["expected"]
                if expected.get("error") == "IndexError":
                    with self.assertRaises(IndexError):
                        contracts.rebuild_render_interaction_list(
                            candidates,
                            fixture["input"]["capacity"],
                        )
                    continue
                actual = contracts.rebuild_render_interaction_list(
                    candidates,
                    fixture["input"]["capacity"],
                )
                self.assertEqual(
                    {
                        "tokens": [entry.token for entry in actual],
                        "keys": [
                            [entry.depth, entry.world_y]
                            for entry in actual
                        ],
                    },
                    expected,
                )

    def test_rebuild_starts_empty_and_keeps_duplicate_identity(self) -> None:
        duplicate = contracts.RenderInteractionRef("same", 1, 1)
        self.assertEqual(
            contracts.rebuild_render_interaction_list([duplicate, duplicate]),
            (duplicate, duplicate),
        )
        self.assertEqual(
            contracts.rebuild_render_interaction_list([]),
            (),
        )

    def test_render_list_rejects_invalid_host_contract_inputs(self) -> None:
        entity = contracts.RenderInteractionRef("valid", 0, 0)
        with self.assertRaises(ValueError):
            contracts.insert_render_interaction((), entity, -1)
        with self.assertRaises(ValueError):
            contracts.insert_render_interaction((object(),), entity)
        with self.assertRaises(ValueError):
            contracts.rebuild_render_interaction_list("not-a-sequence")


class CoordinateIntegrationContractTests(unittest.TestCase):
    def test_coordinate_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "coordinate-integration"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                inputs = dict(fixture["input"])
                slow_divisor = inputs.pop("slow_divisor", None)
                state = contracts.CoordinateState(**inputs)
                if fixture["expected"].get("error") == "ZeroDivisionError":
                    with self.assertRaises(ZeroDivisionError):
                        contracts.reconcile_and_integrate_coordinates(
                            state, slow_divisor
                        )
                    continue
                actual = contracts.reconcile_and_integrate_coordinates(
                    state, slow_divisor
                )
                self.assertEqual(asdict(actual), fixture["expected"])

    def test_java_int_overflow_and_arithmetic_shift_are_preserved(self) -> None:
        state = contracts.CoordinateState(
            fixed_x=2_147_483_647,
            fixed_y=-257,
            pixel_x=8_388_607,
            pixel_y=-2,
            velocity_x=1,
            velocity_y=-1,
            acceleration_x=1,
            acceleration_y=-1,
        )
        actual = contracts.reconcile_and_integrate_coordinates(state)
        self.assertEqual(actual.fixed_x, -2_147_483_648)
        self.assertEqual(actual.pixel_x, -8_388_608)
        self.assertEqual(actual.fixed_y, -258)
        self.assertEqual(actual.pixel_y, -2)
        self.assertEqual(actual.velocity_x, 2)
        self.assertEqual(actual.velocity_y, -2)

    def test_java_division_and_remainder_edges(self) -> None:
        self.assertEqual(contracts.java_divide(-7, 3), -2)
        self.assertEqual(contracts.java_divide(7, -3), -2)
        self.assertEqual(
            contracts.java_divide(contracts.JAVA_INT_MIN, -1),
            contracts.JAVA_INT_MIN,
        )
        self.assertEqual(contracts.java_remainder(-7, 3), -1)
        self.assertEqual(
            contracts.java_remainder(contracts.JAVA_INT_MIN, -1),
            0,
        )
        with self.assertRaises(ZeroDivisionError):
            contracts.java_divide(1, 0)
        with self.assertRaises(ZeroDivisionError):
            contracts.java_remainder(1, 0)


class ScriptGroupLookupContractTests(unittest.TestCase):
    def test_lookup_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "script-group-lookup"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                inputs = fixture["input"]
                self.assertEqual(
                    contracts.find_script_group_index(
                        inputs["script_id"], inputs["script_ids"]
                    ),
                    fixture["expected"],
                )

    def test_script_ids_remain_signed_shorts(self) -> None:
        with self.assertRaises(ValueError):
            contracts.find_script_group_index(1, [40000])
        with self.assertRaises(ValueError):
            contracts.find_script_group_index(1, [True])
        self.assertEqual(
            contracts.find_script_group_index(40000, [-25536]),
            -1,
        )


class TimelineActivityContractTests(unittest.TestCase):
    def test_activity_truth_table_fixture(self) -> None:
        fixture = next(
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "timeline-activity"
        )
        actual = [
            contracts.is_timeline_script_active(**case)
            for case in fixture["input"]["cases"]
        ]
        self.assertEqual(actual, fixture["expected"])

    def test_activity_rejects_non_boolean_pause_flag(self) -> None:
        with self.assertRaises(ValueError):
            contracts.is_timeline_script_active(0, 0, 0)


class TimelineSchedulingContractTests(unittest.TestCase):
    @staticmethod
    def build_lanes(raw_lanes):
        return tuple(
            tuple(
                contracts.TimelineEvent(
                    tick=event["tick"],
                    opcodes=tuple(event["opcodes"]),
                )
                for event in lane
            )
            for lane in raw_lanes
        )

    def test_scheduling_fixtures(self) -> None:
        fixtures = [
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["kind"] == "timeline-scheduling"
        ]
        for fixture in fixtures:
            with self.subTest(fixture=fixture["id"]):
                inputs = fixture["input"]
                state = contracts.TimelineCursorState(
                    **inputs["state"]
                )
                actual = contracts.step_timeline_script(
                    state,
                    self.build_lanes(inputs["lanes"]),
                    slow_time_enabled=inputs["slow_time_enabled"],
                    slow_divisor=inputs["slow_divisor"],
                    frame_counter=inputs["frame_counter"],
                )
                self.assertEqual(
                    jsonable(asdict(actual)),
                    fixture["expected"],
                )

    def test_paused_and_negative_tick_short_circuit(self) -> None:
        paused = contracts.TimelineCursorState(
            active_group_index=-1,
            paused=True,
            current_tick=0,
            advance_latched=False,
            lane_event_indexes=(),
        )
        paused_result = contracts.step_timeline_script(
            paused,
            (),
            slow_time_enabled=False,
            slow_divisor=0,
            frame_counter=0,
        )
        self.assertEqual(paused_result.trace, ("return-paused",))
        self.assertIsNone(paused_result.evaluated_tick)

        negative = contracts.TimelineCursorState(
            active_group_index=-1,
            paused=False,
            current_tick=-1,
            advance_latched=False,
            lane_event_indexes=(),
        )
        negative_result = contracts.step_timeline_script(
            negative,
            (),
            slow_time_enabled=True,
            slow_divisor=0,
            frame_counter=0,
        )
        self.assertEqual(negative_result.trace, ("skip-negative-tick",))
        self.assertIsNone(negative_result.evaluated_tick)

    def test_latch_short_circuits_zero_divisor_and_tick_wraps_as_short(self) -> None:
        state = contracts.TimelineCursorState(
            active_group_index=0,
            paused=False,
            current_tick=32767,
            advance_latched=True,
            lane_event_indexes=(0,),
        )
        result = contracts.step_timeline_script(
            state,
            ((contracts.TimelineEvent(32767, (99, 100)),),),
            slow_time_enabled=True,
            slow_divisor=0,
            frame_counter=0,
        )
        self.assertEqual(result.state.current_tick, -32768)
        self.assertEqual(
            [dispatch.dispatch_path for dispatch in result.dispatches],
            ["inline-low", "extended"],
        )
        self.assertIn("advance-latched", result.trace)

    def test_slow_gate_advances_on_java_remainder_zero(self) -> None:
        state = contracts.TimelineCursorState(
            active_group_index=0,
            paused=False,
            current_tick=2,
            advance_latched=False,
            lane_event_indexes=(),
        )
        result = contracts.step_timeline_script(
            state,
            (),
            slow_time_enabled=True,
            slow_divisor=-3,
            frame_counter=-6,
        )
        self.assertEqual(result.state.current_tick, 3)
        self.assertIn("advance-slow-gate", result.trace)

    def test_timeline_host_contract_validation_edges(self) -> None:
        with self.assertRaises(ValueError):
            contracts.TimelineEvent(0, (256,))
        self.assertEqual(
            [
                contracts.classify_timeline_opcode(opcode)
                for opcode in (99, 100, 127, 128, 255)
            ],
            [
                "inline-low",
                "extended",
                "extended",
                "inline-low",
                "inline-low",
            ],
        )
        with self.assertRaises(ValueError):
            contracts.TimelineCursorState(
                active_group_index=0,
                paused=False,
                current_tick=0,
                advance_latched=False,
                lane_event_indexes=(-1,),
            )

        state = contracts.TimelineCursorState(
            active_group_index=0,
            paused=False,
            current_tick=0,
            advance_latched=False,
            lane_event_indexes=(0,),
        )
        with self.assertRaises(ValueError):
            contracts.step_timeline_script(
                state,
                (),
                slow_time_enabled=False,
                slow_divisor=1,
                frame_counter=0,
            )
        with self.assertRaises(ValueError):
            contracts.step_timeline_script(
                state,
                ((object(),),),
                slow_time_enabled=False,
                slow_divisor=1,
                frame_counter=0,
            )

        abort_capable_state = contracts.TimelineCursorState(
            active_group_index=0,
            paused=False,
            current_tick=0,
            advance_latched=False,
            lane_event_indexes=(0,),
        )
        for abort_opcode in (108, 113):
            abort_lanes = (
                (contracts.TimelineEvent(0, (abort_opcode, 37)),),
            )
            with self.assertRaises(ValueError):
                contracts.step_timeline_script(
                    abort_capable_state,
                    abort_lanes,
                    slow_time_enabled=False,
                    slow_divisor=1,
                    frame_counter=0,
                )
            aborted = contracts.step_timeline_script(
                abort_capable_state,
                abort_lanes,
                slow_time_enabled=False,
                slow_divisor=1,
                frame_counter=0,
                extended_dispatch_results={(0, 0, 0): -1},
            )
            self.assertIs(aborted.execution_aborted, True)
            self.assertEqual(aborted.state.current_tick, 1)
            self.assertEqual(aborted.state.lane_event_indexes, (0,))
            self.assertEqual(
                [item.opcode for item in aborted.dispatches],
                [abort_opcode],
            )
            self.assertEqual(
                aborted.trace[-1],
                "abort-extended-dispatch:0:0:0",
            )

        abort_capable_lanes = (
            (contracts.TimelineEvent(0, (108, 37)),),
        )
        continued = contracts.step_timeline_script(
            abort_capable_state,
            abort_capable_lanes,
            slow_time_enabled=False,
            slow_divisor=1,
            frame_counter=0,
            extended_dispatch_results={(0, 0, 0): 4},
        )
        self.assertIs(continued.execution_aborted, False)
        self.assertEqual(continued.state.lane_event_indexes, (1,))
        self.assertEqual(
            [item.opcode for item in continued.dispatches],
            [108, 37],
        )

        future_abort_capable_lanes = (
            (contracts.TimelineEvent(1, (108,)),),
        )
        future = contracts.step_timeline_script(
            abort_capable_state,
            future_abort_capable_lanes,
            slow_time_enabled=False,
            slow_divisor=1,
            frame_counter=0,
        )
        self.assertIs(future.execution_aborted, False)
        self.assertEqual(future.state.lane_event_indexes, (0,))
        with self.assertRaises(ValueError):
            contracts.step_timeline_script(
                abort_capable_state,
                future_abort_capable_lanes,
                slow_time_enabled=False,
                slow_divisor=1,
                frame_counter=0,
                extended_dispatch_results={(0, 0, 0): -1},
            )


class FullCorpusOracleTests(unittest.TestCase):
    def test_redecode_all_packs_in_memory_matches_delivered_tree(self) -> None:
        level_decoder.validate_existing_output_root(LEVELS_ROOT)
        payloads = level_decoder.discover_payloads(DECODED_ROOT)
        fresh_results = []
        for pack in level_decoder.PACK_IDS:
            fresh = level_decoder.build_pack_result(pack, payloads[pack])
            delivered = json.loads(
                (LEVELS_ROOT / f"pack-{pack}" / "records.json").read_text(
                    encoding="utf-8"
                )
            )
            self.assertEqual(fresh, delivered, f"pack-{pack}")
            fresh_results.append(fresh)

        fresh_summary = level_decoder.build_summary(fresh_results)
        delivered_summary = json.loads(
            (LEVELS_ROOT / "summary.json").read_text(encoding="utf-8")
        )
        self.assertEqual(fresh_summary, delivered_summary)
        self.assertEqual(fresh_summary["inventory"]["entities"], 4286)
        self.assertEqual(fresh_summary["inventory"]["payloads_exact_eof"], 16)
        self.assertEqual(fresh_summary["histograms"]["entity_retype"], {"47": 12, "50": 8})
        self.assertIs(
            fresh_summary["evidence_scope"]["target_code_execution"], False
        )


if __name__ == "__main__":
    unittest.main()
