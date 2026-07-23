from __future__ import annotations

import hashlib
import importlib.util
import json
import sys
import unittest
from collections import Counter
from dataclasses import replace
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
SCRIPTS_ROOT = REPOSITORY_ROOT / "scripts"
FIXTURE_PATH = (
    Path(__file__).parent / "fixtures" / "timeline-opcode-contracts.json"
)
LEGACY_FIXTURE_PATH = (
    Path(__file__).parent / "fixtures" / "gameplay-parity-contracts.json"
)
LEVELS_ROOT = (
    REPOSITORY_ROOT
    / "reconstructed-project"
    / "resources"
    / "levels-decoded"
)
OBSERVED_EXTENDED_OPCODES = (
    *range(100, 109),
    *range(110, 115),
)
ALL_EXTENDED_OPCODES = tuple(range(100, 115))
EXPECTED_WIDTHS = (6, 2, 4, 2, 2, 4, 9, 2, 4, 8, 4, 9, 6, 4, 4)
EXPECTED_TOTAL_COUNTS = (
    204,
    1,
    90,
    7,
    5,
    59,
    16,
    30,
    30,
    0,
    9,
    25,
    1,
    1,
    2,
)
EXPECTED_PER_PACK_COUNTS = (
    (13, 0, 14, 0, 1, 11, 4, 5, 5, 0, 2, 1, 0, 0, 1),
    (6, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    (47, 1, 11, 1, 1, 12, 0, 3, 3, 0, 0, 9, 1, 1, 0),
    (47, 0, 24, 1, 0, 10, 10, 9, 9, 0, 4, 1, 0, 0, 0),
    (1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    (38, 0, 8, 1, 0, 6, 2, 4, 4, 0, 3, 1, 0, 0, 0),
    (22, 0, 10, 4, 3, 10, 0, 3, 3, 0, 0, 1, 0, 0, 1),
    (30, 0, 23, 0, 0, 7, 0, 6, 6, 0, 0, 12, 0, 0, 0),
)


def load_script_module(module_name: str, filename: str):
    path = SCRIPTS_ROOT / filename
    spec = importlib.util.spec_from_file_location(module_name, path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot import {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module


level_decoder = load_script_module(
    "timeline_opcode_level_decoder",
    "decode-gameloft-level-records.py",
)
timeline_contracts = load_script_module(
    "timeline_opcode_contracts",
    "timeline_opcode_contracts.py",
)
legacy_contracts = load_script_module(
    "timeline_opcode_legacy_scheduler",
    "gameplay_parity_contracts.py",
)


def fixture_manifest() -> dict:
    return json.loads(FIXTURE_PATH.read_text(encoding="utf-8"))


def instruction_at(decoded: dict, source: dict) -> dict:
    group = decoded["scripts"]["groups"][source["group_index"]]
    lane = group["lanes"][source["lane_index"]]
    event = lane["events"][source["event_index"]]
    return event["instructions"][source["instruction_index"]]


def flags_with(**values: bool) -> tuple[bool, ...]:
    flags = [False] * 10
    for index, value in values.items():
        flags[int(index.removeprefix("flag_"))] = value
    return tuple(flags)


def entity_for_reference(entities, reference):
    return next(
        entity
        for entity in entities
        if entity.reference is reference
    )


def words(*values: int) -> bytes:
    return b"".join(
        (value & 0xFFFF).to_bytes(2, "little")
        for value in values
    )


class TimelineOpcodeFixtureManifestTests(unittest.TestCase):
    def test_manifest_schema_and_evidence_boundaries(self) -> None:
        manifest = fixture_manifest()
        self.assertEqual(manifest["schema_version"], 1)
        self.assertEqual(manifest["analysis_mode"], "static-only")
        self.assertIs(manifest["static_only"], True)
        self.assertIs(manifest["target_code_execution"], False)
        self.assertEqual(
            set(manifest["allowed_basis"]),
            {"corpus", "synthetic-source-contract"},
        )

        fixtures = manifest["fixtures"]
        ids = [fixture["id"] for fixture in fixtures]
        self.assertEqual(len(fixtures), 15)
        self.assertEqual(len(ids), len(set(ids)))
        self.assertEqual(
            Counter(fixture["basis"] for fixture in fixtures),
            Counter({"corpus": 14, "synthetic-source-contract": 1}),
        )
        self.assertEqual(
            {fixture["input"]["opcode"] for fixture in fixtures},
            set(ALL_EXTENDED_OPCODES),
        )
        self.assertTrue(
            all(
                fixture["kind"]
                == "extended-opcode-decode-effect-family"
                for fixture in fixtures
            )
        )
        self.assertTrue(
            all(fixture["confidence"] == "proven" for fixture in fixtures)
        )
        self.assertTrue(
            all(fixture["expected"]["effect_family"] for fixture in fixtures)
        )

        opcode_109 = next(
            fixture
            for fixture in fixtures
            if fixture["input"]["opcode"] == 109
        )
        self.assertEqual(
            opcode_109["id"],
            "opcode-109-synthetic-source-contract",
        )
        self.assertEqual(opcode_109["basis"], "synthetic-source-contract")
        self.assertEqual(opcode_109["source"]["corpus_occurrences"], 0)
        self.assertNotIn("path", opcode_109["source"])
        self.assertEqual(
            {
                fixture["input"]["opcode"]
                for fixture in fixtures
                if fixture["basis"] == "corpus"
            },
            set(OBSERVED_EXTENDED_OPCODES),
        )

    def test_separate_manifest_preserves_legacy_30_fixture_set(self) -> None:
        self.assertNotEqual(FIXTURE_PATH, LEGACY_FIXTURE_PATH)
        self.assertEqual(
            hashlib.sha256(LEGACY_FIXTURE_PATH.read_bytes()).hexdigest(),
            "b555aca67ae6553ccafc19bddf2350c83b73846487d39780ee048b14118ef01c",
        )
        legacy = json.loads(LEGACY_FIXTURE_PATH.read_text(encoding="utf-8"))
        self.assertEqual(len(legacy["fixtures"]), 30)

    def test_corpus_fixture_sources_are_exactly_pinned(self) -> None:
        decoded_cache: dict[Path, dict] = {}
        for fixture in fixture_manifest()["fixtures"]:
            if fixture["basis"] != "corpus":
                continue
            with self.subTest(fixture=fixture["id"]):
                source = fixture["source"]
                source_path = (REPOSITORY_ROOT / source["path"]).resolve()
                source_path.relative_to(REPOSITORY_ROOT.resolve())
                self.assertTrue(source_path.is_file())
                self.assertEqual(
                    hashlib.sha256(source_path.read_bytes()).hexdigest(),
                    source["sha256"],
                )
                if source_path not in decoded_cache:
                    decoded_cache[source_path] = json.loads(
                        source_path.read_text(encoding="utf-8")
                    )
                decoded = decoded_cache[source_path]
                self.assertEqual(decoded["pack"], source["pack"])
                self.assertEqual(
                    decoded["sources"]["slot_7_scripts"]["sha256_actual"],
                    source["slot_7_sha256"],
                )
                instruction = instruction_at(decoded, source)
                self.assertEqual(
                    instruction["opcode"]["raw_u8"],
                    fixture["input"]["opcode"],
                )
                self.assertEqual(
                    instruction["offsets"]["start"],
                    source["offset"],
                )
                self.assertEqual(instruction["raw_hex"], source["raw_hex"])
                self.assertEqual(
                    instruction["operands"]["raw_hex"],
                    source["operands_raw_hex"],
                )
                self.assertEqual(
                    instruction["operands"]["raw_hex"],
                    fixture["input"]["operands_hex"],
                )
                self.assertEqual(
                    instruction["operand_width"],
                    fixture["expected"]["width"],
                )
                self.assertEqual(
                    instruction["operand_layout"],
                    fixture["expected"]["operand_layout"],
                )
                interpreted_operands = []
                for part in instruction["operands"]["interpreted_parts"]:
                    if part["storage"] == "u8":
                        value = part["raw_u8"]
                    elif part["storage"] == "s16le":
                        value = part["java_i16"]
                    elif part["storage"] == "u16le":
                        value = part["raw_u16"]
                    else:
                        value = part["raw_hex"]
                    interpreted_operands.append(
                        [part["name"], part["storage"], value]
                    )
                self.assertEqual(
                    interpreted_operands,
                    fixture["expected"]["operands"],
                )

    def test_synthetic_opcode_109_source_is_bytecode_pinned(self) -> None:
        fixture = next(
            fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["input"]["opcode"] == 109
        )
        source = fixture["source"]
        evidence_path = (REPOSITORY_ROOT / source["evidence"]).resolve()
        evidence_path.relative_to(REPOSITORY_ROOT.resolve())
        evidence = evidence_path.read_text(encoding="utf-8")
        self.assertEqual(
            hashlib.sha256(evidence_path.read_bytes()).hexdigest(),
            source["sha256"],
        )
        self.assertIn("descriptor: (I[BIII)I", evidence)
        self.assertIn("109: 1939", evidence)
        self.assertEqual(
            bytes.fromhex(source["raw_hex"])[0],
            fixture["input"]["opcode"],
        )
        self.assertEqual(
            source["raw_hex"][2:],
            fixture["input"]["operands_hex"],
        )


class TimelineOpcodeSchemaTests(unittest.TestCase):
    def test_fixture_decodes_match_typed_operands_and_effect_families(
        self,
    ) -> None:
        for fixture in fixture_manifest()["fixtures"]:
            with self.subTest(fixture=fixture["id"]):
                raw_operands = bytes.fromhex(
                    fixture["input"]["operands_hex"]
                )
                decoded = (
                    timeline_contracts.decode_extended_timeline_opcode(
                        fixture["input"]["opcode"],
                        raw_operands,
                    )
                )
                expected = fixture["expected"]
                self.assertEqual(decoded.opcode, fixture["input"]["opcode"])
                self.assertEqual(decoded.width, expected["width"])
                self.assertEqual(
                    decoded.operand_layout,
                    expected["operand_layout"],
                )
                self.assertEqual(
                    decoded.effect_family,
                    expected["effect_family"],
                )
                self.assertIsInstance(
                    decoded.typed_operands,
                    getattr(
                        timeline_contracts,
                        f"Op{decoded.opcode}Operands",
                    ),
                )
                self.assertEqual(decoded.raw_operands, raw_operands)
                self.assertEqual(
                    [
                        [operand.name, operand.storage, operand.value]
                        for operand in decoded.operands
                    ],
                    expected["operands"],
                )
                self.assertEqual(
                    b"".join(operand.raw for operand in decoded.operands),
                    raw_operands,
                )
                for operand in decoded.operands:
                    self.assertEqual(
                        decoded.value(operand.name),
                        operand.value,
                    )

    def test_contract_layouts_equal_canonical_decoder(self) -> None:
        self.assertEqual(
            set(timeline_contracts.EXTENDED_OPCODE_WIDTHS),
            set(ALL_EXTENDED_OPCODES),
        )
        self.assertEqual(
            set(timeline_contracts.EXTENDED_OPCODE_EFFECT_FAMILIES),
            set(ALL_EXTENDED_OPCODES),
        )
        fixtures_by_opcode = {
            fixture["input"]["opcode"]: fixture
            for fixture in fixture_manifest()["fixtures"]
        }
        for opcode in ALL_EXTENDED_OPCODES:
            with self.subTest(opcode=opcode):
                layout = level_decoder.OPCODE_LAYOUTS[opcode]
                self.assertEqual(
                    timeline_contracts.EXTENDED_OPCODE_WIDTHS[opcode],
                    layout.width,
                )
                self.assertEqual(
                    timeline_contracts.EXTENDED_OPCODE_EFFECT_FAMILIES[
                        opcode
                    ],
                    fixtures_by_opcode[opcode]["expected"]["effect_family"],
                )

    def test_signed_and_high_bit_unsigned_operands(self) -> None:
        signed = timeline_contracts.decode_extended_timeline_opcode(
            100,
            bytes.fromhex("ff7f0080ffff"),
        )
        self.assertEqual(
            [operand.value for operand in signed.operands],
            [32767, -32768, -1],
        )

        high_u16 = timeline_contracts.decode_extended_timeline_opcode(
            110,
            bytes.fromhex("ffff0080"),
        )
        self.assertEqual(
            [operand.value for operand in high_u16.operands],
            [65535, 32768],
        )

        high_u8 = timeline_contracts.decode_extended_timeline_opcode(
            105,
            bytes.fromhex("ff0080ff"),
        )
        self.assertEqual(
            [operand.value for operand in high_u8.operands],
            [255, 32768, 255],
        )

    def test_decoder_rejects_wrong_width_invalid_opcode_and_booleans(
        self,
    ) -> None:
        for opcode, width in zip(ALL_EXTENDED_OPCODES, EXPECTED_WIDTHS):
            with self.subTest(opcode=opcode, case="short"):
                with self.assertRaises(ValueError):
                    timeline_contracts.decode_extended_timeline_opcode(
                        opcode,
                        bytes(max(0, width - 1)),
                    )
            with self.subTest(opcode=opcode, case="long"):
                with self.assertRaises(ValueError):
                    timeline_contracts.decode_extended_timeline_opcode(
                        opcode,
                        bytes(width + 1),
                    )

        for opcode in (99, 115, -1, 256, True):
            with self.subTest(opcode=opcode):
                with self.assertRaises(ValueError):
                    timeline_contracts.decode_extended_timeline_opcode(
                        opcode,
                        b"",
                    )
        for invalid_raw in (None, [0, 0], True):
            with self.subTest(raw=invalid_raw):
                with self.assertRaises(ValueError):
                    timeline_contracts.decode_extended_timeline_opcode(
                        101,
                        invalid_raw,
                    )

    def test_decoder_accepts_bytes_like_and_rejects_missing_names(
        self,
    ) -> None:
        decoded_bytearray = (
            timeline_contracts.decode_extended_timeline_opcode(
                101,
                bytearray.fromhex("ffff"),
            )
        )
        decoded_memoryview = (
            timeline_contracts.decode_extended_timeline_opcode(
                101,
                memoryview(bytes.fromhex("ffff")),
            )
        )
        self.assertEqual(decoded_bytearray.raw_operands, b"\xff\xff")
        self.assertEqual(decoded_memoryview.value("script_id"), -1)
        with self.assertRaises(ValueError):
            decoded_memoryview.value("missing")


class TimelineCompletionContractTests(unittest.TestCase):
    def test_selected_cleanup_uses_identity_and_precedes_type5_focus(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("owner")
        player_ref = timeline_contracts.TimelineObjectRef("player")
        secondary_ref = timeline_contracts.TimelineObjectRef("secondary")
        linked_bm_ref = timeline_contracts.TimelineObjectRef("linked-bm")
        animation_af_ref = timeline_contracts.TimelineObjectRef(
            "animation-af"
        )
        owner = timeline_contracts.TimelineEntityState(
            owner_ref,
            runtime_type=5,
            timeline_flags=flags_with(flag_2=True),
        )
        player = timeline_contracts.TimelineEntityState(
            player_ref,
            aux_flags=127,
            linked_bm_ref=linked_bm_ref,
            animation_af_ref=animation_af_ref,
        )
        secondary = timeline_contracts.TimelineEntityState(
            secondary_ref,
            runtime_type=43,
        )
        linked_bm = timeline_contracts.TimelineEntityState(
            linked_bm_ref,
            runtime_type=13,
            aux_flags=91,
            linked_bm_ref=owner_ref,
            animation_af_ref=animation_af_ref,
        )
        animation_af = timeline_contracts.TimelineEntityState(
            animation_af_ref,
            runtime_type=73,
        )
        state = timeline_contracts.TimelineCompletionState(
            owner_ref=owner_ref,
            player_ref=player_ref,
            entities=(owner, player, secondary, linked_bm, animation_af),
            selected_owner_ref=owner_ref,
            focus_ref=secondary_ref,
            secondary_ref=secondary_ref,
            k_z=True,
            k_aa=True,
            k_ab=True,
            global_aw=23,
        )
        result = timeline_contracts.complete_timeline_script(
            state,
            timeline_contracts.TimelineCompletionContext(ai_result=False),
        )

        next_player = entity_for_reference(result.state.entities, player_ref)
        next_linked_bm = entity_for_reference(
            result.state.entities,
            linked_bm_ref,
        )
        next_owner = entity_for_reference(result.state.entities, owner_ref)
        self.assertIsNone(result.state.secondary_ref)
        self.assertIs(result.state.focus_ref, player_ref)
        self.assertIsNone(next_player.linked_bm_ref)
        self.assertIs(next_player.animation_af_ref, animation_af_ref)
        self.assertEqual(next_player.aux_flags, 63)
        self.assertIsNone(next_linked_bm.linked_bm_ref)
        self.assertIs(next_linked_bm.animation_af_ref, animation_af_ref)
        self.assertEqual(next_linked_bm.aux_flags, 0)
        self.assertIs(
            entity_for_reference(
                result.state.entities,
                animation_af_ref,
            ),
            animation_af,
        )
        self.assertIs(result.state.completion_latched, True)
        self.assertIs(result.state.k_z, False)
        self.assertIs(result.state.k_aa, False)
        self.assertIs(result.state.k_ab, False)
        self.assertEqual(next_owner.current_tick, -2)
        self.assertIs(next_owner.timeline_flags[2], False)
        self.assertEqual(result.state.global_aw, 0)
        self.assertEqual(
            [intention.symbol for intention in result.intentions],
            ["k.n:()V", "i.ai:()Z", "k.c:(Li;)V"],
        )
        self.assertEqual(
            [
                intention.arguments
                for intention in result.intentions
                if intention.symbol == "i.ai:()Z"
            ],
            [()],
        )
        self.assertLess(
            result.trace.index("write:g.a=null"),
            result.trace.index("intend:i.ai:()Z=false"),
        )
        self.assertEqual(
            result.boundary,
            "removal-intended-return-pc-344",
        )

        equal_token_not_identity = timeline_contracts.TimelineObjectRef(
            "owner"
        )
        identity_state = timeline_contracts.TimelineCompletionState(
            owner_ref=owner_ref,
            player_ref=owner_ref,
            entities=(timeline_contracts.TimelineEntityState(owner_ref),),
            selected_owner_ref=equal_token_not_identity,
            k_z=True,
            k_aa=True,
            k_ab=True,
            global_aw=9,
        )
        identity_result = timeline_contracts.complete_timeline_script(
            identity_state,
            timeline_contracts.TimelineCompletionContext(),
        )
        self.assertEqual(
            identity_result.boundary,
            "non-selected-return-pc-344",
        )
        self.assertIs(identity_result.state.k_z, True)
        self.assertIs(identity_result.state.k_aa, True)
        self.assertIs(identity_result.state.k_ab, True)
        self.assertIs(identity_result.state.completion_latched, False)
        self.assertEqual(identity_result.intentions, ())

    def test_repeat_returns_before_global_aw_and_normal_completion(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("repeat-owner")
        owner = timeline_contracts.TimelineEntityState(
            owner_ref,
            current_tick=31,
            timeline_flags=flags_with(flag_2=True, flag_3=True),
            lane_event_indexes=(4, 7),
        )
        state = timeline_contracts.TimelineCompletionState(
            owner_ref=owner_ref,
            player_ref=owner_ref,
            entities=(owner,),
            selected_owner_ref=owner_ref,
            k_z=True,
            k_aa=True,
            k_ab=True,
            global_aw=77,
        )
        result = timeline_contracts.complete_timeline_script(
            state,
            timeline_contracts.TimelineCompletionContext(),
        )
        next_owner = entity_for_reference(result.state.entities, owner_ref)
        self.assertEqual(result.boundary, "repeat-return-pc-131")
        self.assertEqual(next_owner.current_tick, 0)
        self.assertIsNone(next_owner.lane_event_indexes)
        self.assertIs(next_owner.timeline_flags[2], False)
        self.assertEqual(result.state.global_aw, 77)
        self.assertIs(result.state.selected_owner_ref, owner_ref)
        self.assertEqual(
            [intention.symbol for intention in result.intentions],
            ["k.n:()V", "i.bJ:()V"],
        )
        self.assertNotIn("write:k.aw=0", result.trace)
        self.assertNotIn("intend:k.c:(Li;)V", result.trace)

    def test_chained_script_duplicates_lookup_and_returns_early(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("chain-owner")
        owner = timeline_contracts.TimelineEntityState(
            owner_ref,
            chained_script_id=344,
        )
        state = timeline_contracts.TimelineCompletionState(
            owner_ref=owner_ref,
            player_ref=owner_ref,
            entities=(owner,),
            selected_owner_ref=owner_ref,
            global_aw=45,
        )
        result = timeline_contracts.complete_timeline_script(
            state,
            timeline_contracts.TimelineCompletionContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(344, 12),
                )
            ),
        )
        next_owner = entity_for_reference(result.state.entities, owner_ref)
        self.assertEqual(
            result.boundary,
            "chained-script-return-pc-270",
        )
        self.assertEqual(next_owner.chained_script_id, -1)
        self.assertEqual(next_owner.current_tick, -2)
        self.assertEqual(result.state.global_aw, 0)
        self.assertIs(result.state.selected_owner_ref, owner_ref)
        self.assertEqual(
            [intention.symbol for intention in result.intentions],
            [
                "k.n:()V",
                "k.s:(I)I",
                "i.h:(I)V",
                "k.s:(I)I",
                "i.k:(I)V",
            ],
        )
        self.assertEqual(
            [
                intention.observed_result
                for intention in result.intentions
                if intention.symbol == "k.s:(I)I"
            ],
            [12, 12],
        )
        self.assertNotIn("write:k.C=null", result.trace)
        with self.assertRaises(ValueError):
            timeline_contracts.complete_timeline_script(
                state,
                timeline_contracts.TimelineCompletionContext(),
            )

    def test_selected_endings_reset_retain_clear_flag_or_remove(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("ending-owner")

        cases = (
            (
                "reset",
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    timeline_flags=flags_with(flag_4=True),
                ),
                "reset-return-pc-288",
                ["k.n:()V", "i.bJ:()V"],
                None,
            ),
            (
                "retain-type-58",
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    runtime_type=58,
                ),
                "type-58-retained-return-pc-344",
                ["k.n:()V"],
                None,
            ),
            (
                "clear-type-5-flag-20",
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    runtime_type=5,
                    flags=0x1FF,
                    variables=(0, 20) + (0,) * 19,
                ),
                "type-5-flag-return-pc-339",
                ["k.n:()V"],
                0x1EF,
            ),
            (
                "clear-type-5-flag-21",
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    runtime_type=5,
                    flags=0x1FF,
                    variables=(0, 21) + (0,) * 19,
                ),
                "type-5-flag-return-pc-339",
                ["k.n:()V"],
                0x1EF,
            ),
            (
                "remove",
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    runtime_type=4,
                ),
                "removal-intended-return-pc-344",
                ["k.n:()V", "k.c:(Li;)V"],
                None,
            ),
        )
        for name, owner, boundary, symbols, expected_flags in cases:
            with self.subTest(case=name):
                state = timeline_contracts.TimelineCompletionState(
                    owner_ref=owner_ref,
                    player_ref=owner_ref,
                    entities=(owner,),
                    selected_owner_ref=owner_ref,
                    focus_ref=owner_ref,
                )
                result = timeline_contracts.complete_timeline_script(
                    state,
                    timeline_contracts.TimelineCompletionContext(),
                )
                self.assertEqual(result.boundary, boundary)
                self.assertIsNone(result.state.selected_owner_ref)
                self.assertEqual(
                    [
                        intention.symbol
                        for intention in result.intentions
                    ],
                    symbols,
                )
                if expected_flags is not None:
                    next_owner = entity_for_reference(
                        result.state.entities,
                        owner_ref,
                    )
                    self.assertEqual(next_owner.flags, expected_flags)

    def test_type5_focus_and_world_y_branches_are_ordered(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("type-5-owner")
        player_ref = timeline_contracts.TimelineObjectRef("type-5-player")
        secondary_ref = timeline_contracts.TimelineObjectRef(
            "type-43-secondary"
        )
        unrelated_focus = timeline_contracts.TimelineObjectRef("focus")
        owner = timeline_contracts.TimelineEntityState(
            owner_ref,
            runtime_type=5,
        )
        player = timeline_contracts.TimelineEntityState(
            player_ref,
            world_y=100,
        )
        secondary = timeline_contracts.TimelineEntityState(
            secondary_ref,
            runtime_type=43,
        )
        state = timeline_contracts.TimelineCompletionState(
            owner_ref=owner_ref,
            player_ref=player_ref,
            entities=(owner, player, secondary),
            focus_ref=unrelated_focus,
            secondary_ref=secondary_ref,
            level_mode=3,
            world_y_offset=7,
        )
        result = timeline_contracts.complete_timeline_script(
            state,
            timeline_contracts.TimelineCompletionContext(ai_result=False),
        )
        next_player = entity_for_reference(result.state.entities, player_ref)
        self.assertIs(result.state.focus_ref, secondary_ref)
        self.assertEqual(next_player.world_y, 93)
        self.assertEqual(result.boundary, "non-selected-return-pc-344")
        self.assertEqual(
            [intention.symbol for intention in result.intentions],
            ["i.ai:()Z"],
        )
        self.assertEqual(result.intentions[0].arguments, ())
        self.assertLess(
            result.trace.index("write:k.aw=0"),
            result.trace.index("intend:i.ai:()Z=false"),
        )
        self.assertLess(
            result.trace.index("write:k.ae=g.a"),
            result.trace.index("write:k.aS.al-=k.X"),
        )

        ai_true = timeline_contracts.complete_timeline_script(
            state,
            timeline_contracts.TimelineCompletionContext(ai_result=True),
        )
        self.assertIs(ai_true.state.focus_ref, unrelated_focus)
        k_z_state = replace(state, k_z=True)
        k_z_result = timeline_contracts.complete_timeline_script(
            k_z_state,
            timeline_contracts.TimelineCompletionContext(ai_result=True),
        )
        self.assertEqual(
            entity_for_reference(
                k_z_result.state.entities,
                player_ref,
            ).world_y,
            100,
        )
        with self.assertRaises(ValueError):
            timeline_contracts.complete_timeline_script(
                state,
                timeline_contracts.TimelineCompletionContext(),
            )


class ExtendedTimelineOpcodeContractTests(unittest.TestCase):
    @staticmethod
    def state_with_owner(
        owner,
        *extra_entities,
        player_ref=None,
        **state_values,
    ):
        return timeline_contracts.TimelineOpcodeState(
            owner_ref=owner.reference,
            entities=(owner, *extra_entities),
            player_ref=player_ref,
            **state_values,
        )

    @staticmethod
    def execute_exact(opcode, raw_operands, state, context=None):
        return timeline_contracts.execute_extended_timeline_opcode(
            opcode,
            raw_operands,
            7,
            7,
            state,
            (
                timeline_contracts.TimelineOpcodeContext()
                if context is None
                else context
            ),
        )

    def test_common_non_exact_guard_covers_every_non_prompt_opcode(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("guard-owner")
        state = self.state_with_owner(
            timeline_contracts.TimelineEntityState(owner_ref)
        )
        for opcode in (
            *range(100, 108),
            *range(109, 113),
            114,
        ):
            with self.subTest(opcode=opcode):
                result = timeline_contracts.execute_extended_timeline_opcode(
                    opcode,
                    bytes(
                        timeline_contracts.EXTENDED_OPCODE_WIDTHS[opcode]
                    ),
                    6,
                    7,
                    state,
                    timeline_contracts.TimelineOpcodeContext(),
                )
                self.assertIs(result.state, state)
                self.assertEqual(result.intentions, ())
                self.assertEqual(
                    result.return_value,
                    timeline_contracts.EXTENDED_OPCODE_WIDTHS[opcode],
                )
                self.assertEqual(result.boundary, "non-exact-guard")
                self.assertEqual(
                    result.trace[-1],
                    "return:common-non-exact-guard",
                )

        malformed_payload = (
            timeline_contracts.execute_extended_timeline_opcode(
                100,
                b"",
                6,
                7,
                state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        )
        self.assertIs(malformed_payload.state, state)
        self.assertIsNone(malformed_payload.operands)
        self.assertEqual(malformed_payload.return_value, 6)

    def test_opcode_100_outer_actions_do_not_fall_through(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("action-owner")

        def run(action, value):
            owner = timeline_contracts.TimelineEntityState(
                owner_ref,
                action=11,
                interaction_value=19,
                interaction_elapsed=23,
            )
            state = self.state_with_owner(owner)
            return self.execute_exact(
                100,
                words(0, action, value),
                state,
            )

        action_zero_noop = run(0, 0)
        self.assertEqual(
            entity_for_reference(
                action_zero_noop.state.entities,
                owner_ref,
            ).action,
            11,
        )
        action_zero_write = run(0, 7)
        self.assertEqual(
            entity_for_reference(
                action_zero_write.state.entities,
                owner_ref,
            ).action,
            7,
        )

        remove = run(2, 7)
        removed_owner = entity_for_reference(remove.state.entities, owner_ref)
        self.assertEqual(removed_owner.action, 11)
        self.assertEqual(removed_owner.interaction_value, 19)
        self.assertEqual(
            [intention.symbol for intention in remove.intentions],
            ["k.c:(Li;)V"],
        )

        interaction = run(3, -9)
        interaction_owner = entity_for_reference(
            interaction.state.entities,
            owner_ref,
        )
        self.assertEqual(interaction_owner.interaction_value, -9)
        self.assertEqual(interaction_owner.interaction_elapsed, 0)
        self.assertEqual(interaction_owner.action, 11)

        global_event = run(4, 99)
        self.assertIs(global_event.state.global_event_latched, True)
        self.assertEqual(
            entity_for_reference(
                global_event.state.entities,
                owner_ref,
            ).action,
            11,
        )

        action_five = run(5, -12)
        self.assertEqual(
            entity_for_reference(
                action_five.state.entities,
                owner_ref,
            ).action,
            -12,
        )
        for action in (6, 99):
            with self.subTest(action=action):
                no_op = run(action, 44)
                no_op_owner = entity_for_reference(
                    no_op.state.entities,
                    owner_ref,
                )
                self.assertEqual(no_op_owner.action, 11)
                self.assertEqual(no_op_owner.interaction_value, 19)
                self.assertEqual(no_op.intentions, ())
        for result in (
            action_zero_noop,
            action_zero_write,
            remove,
            interaction,
            global_event,
            action_five,
        ):
            self.assertEqual(result.return_value, 6)
            self.assertEqual(result.boundary, "exact-entity-control")

    def test_opcode_100_subaction_matrix_and_target_resolution(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("subaction-owner")

        def run(value, owner=None, extras=(), player_ref=None):
            current_owner = (
                timeline_contracts.TimelineEntityState(
                    owner_ref,
                    flags=16 | 32 | 128 | 512,
                    aux_flags=0,
                    velocity_ai=1,
                    velocity_ag=2,
                    velocity_aj=3,
                    velocity_ah=4,
                )
                if owner is None
                else owner
            )
            return self.execute_exact(
                100,
                words(0, 1, value),
                self.state_with_owner(
                    current_owner,
                    *extras,
                    player_ref=player_ref,
                ),
            )

        sub0 = run(0)
        self.assertEqual(
            entity_for_reference(sub0.state.entities, owner_ref).flags,
            32 | 128 | 512,
        )
        self.assertEqual(
            [item.symbol for item in sub0.intentions],
            ["i.G:()V"],
        )
        sub1 = run(1)
        self.assertEqual(
            entity_for_reference(sub1.state.entities, owner_ref).flags,
            16 | 512,
        )
        sub2 = run(
            2,
            timeline_contracts.TimelineEntityState(owner_ref),
        )
        self.assertEqual(
            entity_for_reference(sub2.state.entities, owner_ref).flags,
            16,
        )
        sub3 = run(
            3,
            timeline_contracts.TimelineEntityState(owner_ref, flags=32),
        )
        self.assertEqual(
            entity_for_reference(sub3.state.entities, owner_ref).flags,
            512,
        )
        sub4 = run(4)
        sub4_owner = entity_for_reference(sub4.state.entities, owner_ref)
        self.assertEqual(sub4_owner.flags & 512, 0)
        self.assertEqual(
            (
                sub4_owner.velocity_ai,
                sub4_owner.velocity_ag,
                sub4_owner.velocity_aj,
                sub4_owner.velocity_ah,
            ),
            (0, 0, 0, 0),
        )
        sub5_set = run(
            5,
            timeline_contracts.TimelineEntityState(owner_ref),
        )
        self.assertEqual(
            entity_for_reference(
                sub5_set.state.entities,
                owner_ref,
            ).flags,
            1024,
        )
        sub5_clear = run(
            5,
            timeline_contracts.TimelineEntityState(
                owner_ref,
                flags=1024,
            ),
        )
        self.assertEqual(
            entity_for_reference(
                sub5_clear.state.entities,
                owner_ref,
            ).flags,
            0,
        )
        sub6 = run(
            6,
            timeline_contracts.TimelineEntityState(owner_ref),
        )
        self.assertIs(
            entity_for_reference(
                sub6.state.entities,
                owner_ref,
            ).timeline_flags[7],
            True,
        )

        sub7 = run(
            7,
            timeline_contracts.TimelineEntityState(
                owner_ref,
                runtime_type=0,
            ),
            player_ref=owner_ref,
        )
        self.assertEqual(
            entity_for_reference(
                sub7.state.entities,
                owner_ref,
            ).aux_flags,
            256,
        )
        variables = [0] * 21
        sub8 = run(
            8,
            timeline_contracts.TimelineEntityState(
                owner_ref,
                runtime_type=11,
                variables=tuple(variables),
            ),
        )
        self.assertEqual(
            entity_for_reference(
                sub8.state.entities,
                owner_ref,
            ).variables[20],
            40,
        )
        sub9 = run(
            9,
            timeline_contracts.TimelineEntityState(
                owner_ref,
                flags=7,
            ),
        )
        self.assertEqual(
            entity_for_reference(
                sub9.state.entities,
                owner_ref,
            ).flags,
            7,
        )

        linked_bm_ref = timeline_contracts.TimelineObjectRef(
            "subaction-linked-bm"
        )
        animation_af_ref = timeline_contracts.TimelineObjectRef(
            "subaction-animation-af"
        )
        sub10 = run(
            10,
            timeline_contracts.TimelineEntityState(
                owner_ref,
                runtime_type=35,
                flags=7,
                linked_bm_ref=linked_bm_ref,
                animation_af_ref=animation_af_ref,
            ),
            extras=(
                timeline_contracts.TimelineEntityState(
                    linked_bm_ref,
                    runtime_type=73,
                ),
                timeline_contracts.TimelineEntityState(
                    animation_af_ref,
                    runtime_type=73,
                ),
            ),
        )
        self.assertEqual(
            entity_for_reference(
                sub10.state.entities,
                owner_ref,
            ).flags,
            32,
        )
        self.assertEqual(
            [
                item.arguments
                for item in sub10.intentions
                if item.symbol == "i.i:(I)V"
            ],
            [(owner_ref, 0), (animation_af_ref, 152)],
        )
        sub10_owner = entity_for_reference(
            sub10.state.entities,
            owner_ref,
        )
        self.assertIs(sub10_owner.linked_bm_ref, linked_bm_ref)
        self.assertIs(sub10_owner.animation_af_ref, animation_af_ref)
        sub11 = run(
            11,
            timeline_contracts.TimelineEntityState(owner_ref),
        )
        sub12 = run(
            12,
            timeline_contracts.TimelineEntityState(owner_ref),
        )
        self.assertEqual(
            entity_for_reference(
                sub11.state.entities,
                owner_ref,
            ).flags,
            32,
        )
        self.assertEqual(
            entity_for_reference(
                sub12.state.entities,
                owner_ref,
            ).flags,
            64,
        )

        target_ref = timeline_contracts.TimelineObjectRef(
            "resolved-target"
        )
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        target = timeline_contracts.TimelineEntityState(target_ref)
        state = self.state_with_owner(owner, target)
        resolved = self.execute_exact(
            100,
            words(42, 5, 9),
            state,
            timeline_contracts.TimelineOpcodeContext(
                entity_lookups=(
                    timeline_contracts.EntityLookupObservation(
                        42,
                        target_ref,
                    ),
                )
            ),
        )
        self.assertEqual(
            entity_for_reference(
                resolved.state.entities,
                target_ref,
            ).action,
            9,
        )
        self.assertEqual(
            [item.symbol for item in resolved.intentions],
            ["k.q:(I)Li;"],
        )
        null_target = self.execute_exact(
            100,
            words(42, 5, 9),
            state,
            timeline_contracts.TimelineOpcodeContext(
                entity_lookups=(
                    timeline_contracts.EntityLookupObservation(42, None),
                )
            ),
        )
        self.assertEqual(null_target.boundary, "exact-null-target")
        self.assertIs(null_target.state, state)
        with self.assertRaises(ValueError):
            self.execute_exact(100, words(42, 5, 9), state)

    def test_opcodes_101_through_104_direct_effects(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("simple-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(owner)

        chained = self.execute_exact(101, words(-2), state)
        self.assertEqual(
            entity_for_reference(
                chained.state.entities,
                owner_ref,
            ).chained_script_id,
            -2,
        )
        self.assertEqual(chained.return_value, 2)

        screen_on = self.execute_exact(102, words(-9, 1), state)
        screen_off = self.execute_exact(102, words(-9, 0), state)
        self.assertEqual(
            [item.symbol for item in screen_on.intentions],
            ["k.z:(I)V"],
        )
        self.assertEqual(screen_on.intentions[0].arguments, (-9,))
        self.assertEqual(
            [item.symbol for item in screen_off.intentions],
            ["k.A:(I)V"],
        )
        self.assertEqual(screen_off.intentions[0].arguments, (-9,))

        opaque = self.execute_exact(103, bytes.fromhex("80ff"), state)
        self.assertIs(opaque.state, state)
        self.assertEqual(opaque.intentions, ())
        self.assertEqual(opaque.boundary, "exact-opaque-no-op")
        self.assertIn("opaque-no-op:80ff", opaque.trace)

        audio = self.execute_exact(104, words(-5), state)
        self.assertEqual(
            [item.symbol for item in audio.intentions],
            ["k.n:(I)V"],
        )
        self.assertEqual(audio.intentions[0].arguments, (-5,))

    def test_opcode_105_media_paths_and_required_observation(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("media-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(
            owner,
            media_position=3,
            media_end=99,
        )
        succeeded = self.execute_exact(
            105,
            bytes.fromhex("05ff7f01"),
            state,
            timeline_contracts.TimelineOpcodeContext(
                media_start_succeeded=True,
                advance_latched_after_media=True,
            ),
        )
        succeeded_owner = entity_for_reference(
            succeeded.state.entities,
            owner_ref,
        )
        self.assertIs(succeeded_owner.timeline_flags[0], True)
        self.assertIs(succeeded_owner.timeline_flags[1], True)
        self.assertEqual(succeeded.state.media_position, 99)
        self.assertEqual(
            [item.symbol for item in succeeded.intentions],
            ["k.b:(III)Z", "k.l:(I)V"],
        )
        self.assertEqual(
            succeeded.intentions[0].arguments,
            (5, 32767, 1),
        )
        self.assertIs(succeeded.intentions[0].observed_result, True)

        failed = self.execute_exact(
            105,
            bytes.fromhex("05ff7f01"),
            state,
            timeline_contracts.TimelineOpcodeContext(
                media_start_succeeded=False,
                advance_latched_after_media=False,
            ),
        )
        self.assertEqual(
            [item.symbol for item in failed.intentions],
            ["k.b:(III)Z"],
        )
        self.assertEqual(failed.state.media_position, 3)
        already_latched_owner = replace(
            owner,
            timeline_flags=flags_with(flag_1=True),
        )
        already_latched = self.execute_exact(
            105,
            bytes.fromhex("05ff7f01"),
            self.state_with_owner(
                already_latched_owner,
                media_position=3,
                media_end=99,
            ),
        )
        self.assertEqual(already_latched.intentions, ())
        self.assertEqual(already_latched.state.media_position, 99)
        with self.assertRaises(ValueError):
            self.execute_exact(
                105,
                bytes.fromhex("05ff7f01"),
                state,
            )
        with self.assertRaises(ValueError):
            self.execute_exact(
                105,
                bytes.fromhex("05ff7f01"),
                state,
                timeline_contracts.TimelineOpcodeContext(
                    media_start_succeeded=True
                ),
            )

    def test_opcodes_106_and_107_state_setup(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("setup-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(owner)
        self_message = self.execute_exact(
            106,
            words(0, 22, 23, 24) + bytes([3]),
            state,
        )
        self.assertIs(self_message.state.message_target_ref, owner_ref)
        message_owner = entity_for_reference(
            self_message.state.entities,
            owner_ref,
        )
        self.assertEqual(
            tuple(
                message_owner.message_slots[index]
                for index in (2, 3, 5, 6, 7, 8, 9)
            ),
            (-1, 24, 22, 23, -1, 1, 1),
        )
        sticky_slots = [-1] * 10
        sticky_slots[7] = 77
        sticky_slots[9] = 99
        sticky_owner = replace(
            owner,
            message_slots=tuple(sticky_slots),
        )
        sticky_message = self.execute_exact(
            106,
            words(0, 40, 41, 42) + bytes([1]),
            self.state_with_owner(sticky_owner),
        )
        sticky_result_slots = entity_for_reference(
            sticky_message.state.entities,
            owner_ref,
        ).message_slots
        self.assertEqual(
            tuple(
                sticky_result_slots[index]
                for index in (3, 5, 6, 7, 8, 9)
            ),
            (42, 40, 41, 77, 1, 99),
        )

        target_ref = timeline_contracts.TimelineObjectRef("message-target")
        target = timeline_contracts.TimelineEntityState(target_ref)
        target_state = self.state_with_owner(owner, target)
        target_message = self.execute_exact(
            106,
            words(7, 30, 31, 32) + bytes([1]),
            target_state,
            timeline_contracts.TimelineOpcodeContext(
                entity_lookups=(
                    timeline_contracts.EntityLookupObservation(
                        7,
                        target_ref,
                    ),
                )
            ),
        )
        target_slots = entity_for_reference(
            target_message.state.entities,
            target_ref,
        ).message_slots
        self.assertIs(target_message.state.message_target_ref, target_ref)
        self.assertEqual(
            tuple(target_slots[index] for index in (3, 5, 6, 7, 8, 9)),
            (32, 30, 31, 1, 1, -1),
        )

        prompt_existing = replace(
            state,
            single_prompt=timeline_contracts.SinglePromptState(1, 0),
            prompt_response=2,
        )
        prompt = self.execute_exact(
            107,
            words(32),
            prompt_existing,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_widget_presence=(True,),
            ),
        )
        self.assertEqual(
            prompt.state.single_prompt,
            timeline_contracts.SinglePromptState(65568, 5),
        )
        self.assertEqual(prompt.state.prompt_response, 0)
        self.assertEqual(
            [item.kind for item in prompt.intentions],
            ["write", "construct", "write", "call", "call"],
        )
        self.assertEqual(
            [item.symbol for item in prompt.intentions],
            [
                "i.bA:[La;",
                "a.<init>:()V",
                "i.bA:[La;",
                "a.a:(Lb;)V",
                "a.a:(II)V",
            ],
        )
        self.assertEqual(prompt.intentions[0].arguments, (0, None))
        self.assertEqual(prompt.intentions[1].arguments, ())
        self.assertEqual(
            prompt.intentions[2].arguments,
            (0, "new-a-instance"),
        )
        pointer_prompt = self.execute_exact(
            107,
            words(4),
            state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=False,
                prompt_widget_presence=(False,),
            ),
        )
        self.assertEqual(
            pointer_prompt.state.single_prompt,
            timeline_contracts.SinglePromptState(16388, 2),
        )
        self.assertEqual(
            pointer_prompt.intentions[-1].arguments,
            (0, 0, -1),
        )
        with self.assertRaises(ValueError):
            self.execute_exact(107, words(32), state)
        with self.assertRaisesRegex(
            ValueError,
            "prompt_widget_presence",
        ):
            self.execute_exact(
                107,
                words(32),
                state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True
                ),
            )

    def test_opcode_107_to_108_confirm_cancel_and_exact_branches(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("single-owner")
        state = self.state_with_owner(
            timeline_contracts.TimelineEntityState(owner_ref)
        )
        setup = self.execute_exact(
            107,
            words(32),
            state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_widget_presence=(False,),
            ),
        )
        widget = timeline_contracts.PromptWidgetObservation(10, 20)

        confirmed = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            6,
            7,
            setup.state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_poll=timeline_contracts.PromptPollObservation(
                    masked_input_active=True,
                    cancel_input_active=False,
                    pointer_cancelled=False,
                    widgets=(widget,),
                ),
            ),
        )
        self.assertEqual(confirmed.state.prompt_response, 1)
        self.assertEqual(
            [item.symbol for item in confirmed.intentions],
            ["a.a:(II)V", "i.O:()V", "k.p:()V"],
        )
        self.assertEqual(
            confirmed.boundary,
            "future-single-prompt-poll",
        )
        self.assertEqual(confirmed.return_value, 4)

        cancelled = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            6,
            7,
            setup.state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_poll=timeline_contracts.PromptPollObservation(
                    masked_input_active=False,
                    cancel_input_active=True,
                    pointer_cancelled=False,
                    widgets=(widget,),
                ),
            ),
        )
        self.assertEqual(cancelled.state.prompt_response, 2)
        self.assertEqual(
            [item.symbol for item in cancelled.intentions],
            ["a.a:(II)V", "i.O:()V", "k.p:()V"],
        )

        unchanged = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            6,
            7,
            setup.state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_poll=timeline_contracts.PromptPollObservation(
                    masked_input_active=False,
                    cancel_input_active=False,
                    pointer_cancelled=False,
                    widgets=(widget,),
                ),
            ),
        )
        self.assertEqual(unchanged.state.prompt_response, 0)
        self.assertEqual(unchanged.intentions, ())

        keypad_null_unchanged = (
            timeline_contracts.execute_extended_timeline_opcode(
                108,
                words(12, 13),
                6,
                7,
                setup.state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True,
                    prompt_poll=(
                        timeline_contracts.PromptPollObservation(
                            masked_input_active=False,
                            cancel_input_active=False,
                            pointer_cancelled=False,
                            widgets=(None,),
                        )
                    ),
                ),
            )
        )
        self.assertEqual(keypad_null_unchanged.state.prompt_response, 0)
        self.assertEqual(keypad_null_unchanged.intentions, ())

        success_branch = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            7,
            7,
            confirmed.state,
            timeline_contracts.TimelineOpcodeContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(12, 3),
                )
            ),
        )
        self.assertEqual(success_branch.return_value, -1)
        self.assertEqual(
            success_branch.boundary,
            "exact-single-prompt-branch",
        )
        self.assertEqual(
            [item.symbol for item in success_branch.intentions],
            [
                "i.O:()V",
                "k.p:()V",
                "k.s:(I)I",
                "i.h:(I)V",
                "k.s:(I)I",
                "i.k:(I)V",
            ],
        )
        self.assertEqual(success_branch.intentions[0].arguments, ())
        self.assertEqual(
            [
                item.observed_result
                for item in success_branch.intentions
                if item.symbol == "k.s:(I)I"
            ],
            [3, 3],
        )

        failure_branch = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            7,
            7,
            cancelled.state,
            timeline_contracts.TimelineOpcodeContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(13, 4),
                )
            ),
        )
        self.assertEqual(failure_branch.return_value, -1)
        self.assertEqual(
            [
                item.arguments[0]
                for item in failure_branch.intentions
                if item.symbol == "k.s:(I)I"
            ],
            [13, 13],
        )

        exact_no_branch = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(0, 0),
            7,
            7,
            setup.state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertEqual(exact_no_branch.return_value, 4)
        self.assertEqual(
            exact_no_branch.boundary,
            "exact-single-prompt-no-branch",
        )
        self.assertEqual(
            [item.symbol for item in exact_no_branch.intentions],
            ["i.O:()V", "k.p:()V"],
        )

        past = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            8,
            7,
            state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertIs(past.state, state)
        self.assertEqual(past.intentions, ())
        self.assertEqual(past.boundary, "past-single-prompt-no-op")
        with self.assertRaisesRegex(ValueError, "single_prompt"):
            timeline_contracts.execute_extended_timeline_opcode(
                108,
                words(12, 13),
                7,
                7,
                state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                108,
                words(12, 13),
                6,
                7,
                state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True,
                    prompt_poll=timeline_contracts.PromptPollObservation(
                        False,
                        False,
                        False,
                        (widget,),
                    ),
                ),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                108,
                words(12, 13),
                6,
                7,
                setup.state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True
                ),
            )

    def test_opcodes_109_and_110_exact_state_and_unreachable_cleanup(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("camera-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(owner)
        vector = self.execute_exact(
            109,
            words(0, 1, 2, 3),
            state,
            timeline_contracts.TimelineOpcodeContext(
                camera_angle_result=91
            ),
        )
        vector_owner = entity_for_reference(
            vector.state.entities,
            owner_ref,
        )
        self.assertEqual(vector.state.camera_vector, (0, 1, 2, 3, 91))
        self.assertIs(vector_owner.timeline_flags[9], True)
        self.assertEqual(
            [item.symbol for item in vector.intentions],
            ["j.b:(II)I"],
        )
        self.assertEqual(vector.intentions[0].arguments, (-2, 2))
        self.assertEqual(vector.intentions[0].observed_result, 91)

        zero_delta = self.execute_exact(
            109,
            words(5, 7, 5, 10),
            state,
            timeline_contracts.TimelineOpcodeContext(
                camera_angle_result=12
            ),
        )
        self.assertEqual(zero_delta.intentions[0].arguments, (-1, 3))
        guarded_after_event = (
            timeline_contracts.execute_extended_timeline_opcode(
                109,
                words(9, 9, 9, 9),
                8,
                7,
                vector.state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        )
        self.assertIs(guarded_after_event.state, vector.state)
        self.assertEqual(
            guarded_after_event.state.camera_vector,
            (0, 1, 2, 3, 91),
        )
        self.assertIs(
            entity_for_reference(
                guarded_after_event.state.entities,
                owner_ref,
            ).timeline_flags[9],
            True,
        )
        self.assertEqual(
            guarded_after_event.boundary,
            "non-exact-guard",
        )
        with self.assertRaises(ValueError):
            self.execute_exact(109, words(0, 1, 2, 3), state)

        second_ref = timeline_contracts.TimelineObjectRef(
            "camera-second"
        )
        pair_state = self.state_with_owner(
            owner,
            timeline_contracts.TimelineEntityState(second_ref),
        )
        pair = self.execute_exact(
            110,
            words(5, 249),
            pair_state,
            timeline_contracts.TimelineOpcodeContext(
                entity_lookups=(
                    timeline_contracts.EntityLookupObservation(
                        5,
                        owner_ref,
                    ),
                    timeline_contracts.EntityLookupObservation(
                        249,
                        second_ref,
                    ),
                )
            ),
        )
        self.assertEqual(
            pair.state.camera_entity_refs,
            (owner_ref, second_ref),
        )
        self.assertIs(
            entity_for_reference(
                pair.state.entities,
                owner_ref,
            ).timeline_flags[9],
            True,
        )
        self.assertEqual(
            [item.symbol for item in pair.intentions],
            ["k.q:(I)Li;", "k.q:(I)Li;"],
        )
        with self.assertRaises(ValueError):
            self.execute_exact(
                110,
                words(5, 249),
                pair_state,
                timeline_contracts.TimelineOpcodeContext(
                    entity_lookups=(
                        timeline_contracts.EntityLookupObservation(
                            5,
                            owner_ref,
                        ),
                    )
                ),
            )

    def test_opcodes_111_and_112_feature_gate_and_option_filtering(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("spawn-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(owner)
        spawn_raw = words(4, 700, 800) + bytes([1]) + words(900)
        disabled = self.execute_exact(
            111,
            spawn_raw,
            state,
            timeline_contracts.TimelineOpcodeContext(
                special_entity_creation_enabled=False
            ),
        )
        self.assertIs(disabled.state, state)
        self.assertEqual(disabled.intentions, ())
        self.assertEqual(
            disabled.boundary,
            "exact-feature-gated-no-op",
        )

        created_ref = timeline_contracts.TimelineObjectRef("created")
        created = timeline_contracts.TimelineEntityState(
            created_ref,
            flags=1,
        )
        enabled = self.execute_exact(
            111,
            spawn_raw,
            state,
            timeline_contracts.TimelineOpcodeContext(
                special_entity_creation_enabled=True,
                created_entity=created,
            ),
        )
        self.assertEqual(len(enabled.state.entities), 2)
        self.assertEqual(
            entity_for_reference(
                enabled.state.entities,
                created_ref,
            ).flags,
            513,
        )
        self.assertEqual(
            [item.symbol for item in enabled.intentions],
            ["i.a:(IIIZIII)Li;"],
        )
        self.assertEqual(
            enabled.intentions[0].arguments,
            (8, 59, 4, True, 700, 800, 900),
        )
        with self.assertRaises(ValueError):
            self.execute_exact(111, spawn_raw, state)
        with self.assertRaises(ValueError):
            self.execute_exact(
                111,
                spawn_raw,
                state,
                timeline_contracts.TimelineOpcodeContext(
                    special_entity_creation_enabled=True
                ),
            )
        with self.assertRaises(ValueError):
            self.execute_exact(
                111,
                spawn_raw,
                state,
                timeline_contracts.TimelineOpcodeContext(
                    special_entity_creation_enabled=True,
                    created_entity=owner,
                ),
            )

        filtered = self.execute_exact(
            112,
            words(6, 10, 2),
            state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=False,
                prompt_widget_presence=(True, False),
            ),
        )
        self.assertEqual(
            filtered.state.multiple_prompt,
            timeline_contracts.MultiplePromptState((6, 2), 0),
        )
        self.assertEqual(filtered.state.prompt_response, 0)
        self.assertEqual(
            [item.kind for item in filtered.intentions],
            ["call", "call", "construct", "write", "call", "call"],
        )
        self.assertEqual(
            [
                item.arguments
                for item in filtered.intentions
                if item.symbol == "a.<init>:()V"
            ],
            [()],
        )
        self.assertEqual(
            [
                item.arguments
                for item in filtered.intentions
                if item.symbol == "i.bA:[La;"
            ],
            [(1, "new-a-instance")],
        )
        with self.assertRaises(ValueError):
            self.execute_exact(112, words(6, 10, 2), state)
        with self.assertRaisesRegex(
            ValueError,
            "prompt_widget_presence",
        ):
            self.execute_exact(
                112,
                words(6, 10, 2),
                state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=False
                ),
            )
        empty = self.execute_exact(
            112,
            words(10, 11, 65535),
            state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertEqual(
            empty.state.multiple_prompt,
            timeline_contracts.MultiplePromptState((), 0),
        )
        self.assertEqual(empty.intentions, ())

    def test_opcode_112_to_113_sequence_success_cancel_and_branches(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("sequence-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        state = self.state_with_owner(
            owner,
            multiple_prompt=timeline_contracts.MultiplePromptState(
                (1, 2),
                0,
            ),
        )
        widget_0 = timeline_contracts.PromptWidgetObservation(10, 20)
        widget_1 = timeline_contracts.PromptWidgetObservation(30, 40)
        confirm_poll = timeline_contracts.PromptPollObservation(
            masked_input_active=True,
            cancel_input_active=False,
            pointer_cancelled=False,
            widgets=(widget_0, widget_1),
        )
        first = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            6,
            7,
            state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_poll=confirm_poll,
            ),
        )
        self.assertEqual(first.state.multiple_prompt.progress, 1)
        self.assertEqual(
            [item.symbol for item in first.intentions],
            ["a.a:(II)V"],
        )
        self.assertEqual(first.intentions[0].arguments[0], 0)
        self.assertNotIn("intend:i.O:()V", first.trace)

        keypad_null_confirmed = (
            timeline_contracts.execute_extended_timeline_opcode(
                113,
                words(20, 21),
                6,
                7,
                state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True,
                    prompt_poll=(
                        timeline_contracts.PromptPollObservation(
                            masked_input_active=True,
                            cancel_input_active=False,
                            pointer_cancelled=False,
                            widgets=(None, widget_1),
                        )
                    ),
                ),
            )
        )
        self.assertEqual(
            keypad_null_confirmed.state.multiple_prompt.progress,
            1,
        )
        self.assertEqual(keypad_null_confirmed.intentions, ())

        second = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            6,
            7,
            first.state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=True,
                prompt_poll=confirm_poll,
            ),
        )
        self.assertEqual(second.state.multiple_prompt.progress, 2)
        self.assertEqual(
            [item.symbol for item in second.intentions],
            ["a.a:(II)V", "i.O:()V", "k.p:()V"],
        )
        self.assertEqual(second.intentions[0].arguments[0], 1)

        cancel_poll = timeline_contracts.PromptPollObservation(
            masked_input_active=False,
            cancel_input_active=False,
            pointer_cancelled=True,
            widgets=(widget_0, widget_1),
        )
        cancelled = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            6,
            7,
            state,
            timeline_contracts.TimelineOpcodeContext(
                keypad_mode=False,
                prompt_poll=cancel_poll,
            ),
        )
        self.assertEqual(cancelled.state.multiple_prompt.progress, -1)
        touch_updates = [
            item.arguments
            for item in cancelled.intentions
            if item.symbol == "a.a:(II)V"
        ]
        self.assertEqual(touch_updates, [(0, -1, 1), (0, -1, 1)])
        self.assertEqual(
            [item.symbol for item in cancelled.intentions[-2:]],
            ["i.O:()V", "k.p:()V"],
        )
        inactive = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            6,
            7,
            cancelled.state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertEqual(inactive.intentions, ())
        self.assertIn("sequence-prompt:inactive-progress", inactive.trace)

        success_branch = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            7,
            7,
            second.state,
            timeline_contracts.TimelineOpcodeContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(20, 6),
                )
            ),
        )
        self.assertEqual(success_branch.return_value, -1)
        self.assertIsNone(success_branch.state.multiple_prompt)
        self.assertEqual(
            [item.symbol for item in success_branch.intentions],
            [
                "i.O:()V",
                "k.p:()V",
                "k.s:(I)I",
                "i.h:(I)V",
                "k.s:(I)I",
                "i.k:(I)V",
            ],
        )
        self.assertEqual(success_branch.intentions[0].arguments, ())
        self.assertEqual(
            [
                item.arguments[0]
                for item in success_branch.intentions
                if item.symbol == "k.s:(I)I"
            ],
            [20, 20],
        )

        failure_branch = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            7,
            7,
            first.state,
            timeline_contracts.TimelineOpcodeContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(21, 7),
                )
            ),
        )
        self.assertEqual(failure_branch.return_value, -1)
        self.assertIsNone(failure_branch.state.multiple_prompt)
        self.assertEqual(
            [
                item.arguments[0]
                for item in failure_branch.intentions
                if item.symbol == "k.s:(I)I"
            ],
            [21, 21],
        )

        no_branch = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(0, 0),
            7,
            7,
            state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertEqual(no_branch.return_value, 4)
        self.assertIsNone(no_branch.state.multiple_prompt)
        self.assertEqual(
            [item.symbol for item in no_branch.intentions],
            ["i.O:()V", "k.p:()V"],
        )
        past = timeline_contracts.execute_extended_timeline_opcode(
            113,
            words(20, 21),
            8,
            7,
            state,
            timeline_contracts.TimelineOpcodeContext(),
        )
        self.assertIs(past.state, state)
        self.assertEqual(past.intentions, ())
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                113,
                words(20, 21),
                6,
                7,
                self.state_with_owner(owner),
                timeline_contracts.TimelineOpcodeContext(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                113,
                words(20, 21),
                6,
                7,
                state,
                timeline_contracts.TimelineOpcodeContext(
                    keypad_mode=True
                ),
            )

    def test_opcode_114_and_executor_validation(self) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("text-owner")
        state = self.state_with_owner(
            timeline_contracts.TimelineEntityState(owner_ref)
        )
        timed = self.execute_exact(
            114,
            words(11, 4000),
            state,
            timeline_contracts.TimelineOpcodeContext(
                level_index=2,
                localized_text=(
                    timeline_contracts.LocalizedTextObservation(
                        "localized text"
                    )
                ),
            ),
        )
        self.assertEqual(
            timed.state.timed_text,
            timeline_contracts.TimedTextState(
                "localized text",
                4000,
            ),
        )
        self.assertEqual(
            [item.symbol for item in timed.intentions],
            ["k.d:(II)Ljava/lang/String;"],
        )
        self.assertEqual(timed.intentions[0].arguments, (3, 11))
        self.assertEqual(
            timed.intentions[0].observed_result,
            "localized text",
        )
        wrapped_level = self.execute_exact(
            114,
            words(11, 4000),
            state,
            timeline_contracts.TimelineOpcodeContext(
                level_index=timeline_contracts.JAVA_INT_MAX,
                localized_text=(
                    timeline_contracts.LocalizedTextObservation(
                        "wrapped level"
                    )
                ),
            ),
        )
        self.assertEqual(
            wrapped_level.intentions[0].arguments[0],
            timeline_contracts.JAVA_INT_MIN,
        )
        null_text = self.execute_exact(
            114,
            words(11, 4000),
            state,
            timeline_contracts.TimelineOpcodeContext(
                level_index=2,
                localized_text=(
                    timeline_contracts.LocalizedTextObservation(None)
                ),
            ),
        )
        self.assertEqual(
            null_text.state.timed_text,
            timeline_contracts.TimedTextState(None, 4000),
        )
        self.assertIsNone(null_text.intentions[0].observed_result)
        for context in (
            timeline_contracts.TimelineOpcodeContext(
                localized_text=(
                    timeline_contracts.LocalizedTextObservation(
                        "localized text"
                    )
                )
            ),
            timeline_contracts.TimelineOpcodeContext(level_index=2),
        ):
            with self.subTest(context=context):
                with self.assertRaises(ValueError):
                    self.execute_exact(114, words(11, 4000), state, context)

        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                101,
                words(1),
                True,
                0,
                state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                101,
                words(1),
                0,
                False,
                state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                101,
                words(1),
                0,
                0,
                object(),
                timeline_contracts.TimelineOpcodeContext(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.execute_extended_timeline_opcode(
                101,
                words(1),
                0,
                0,
                state,
                object(),
            )
        with self.assertRaises(ValueError):
            timeline_contracts.TimelineOpcodeContext(keypad_mode=1)

    def test_executor_return_composes_with_legacy_scheduler_abort_map(
        self,
    ) -> None:
        owner_ref = timeline_contracts.TimelineObjectRef("compose-owner")
        owner = timeline_contracts.TimelineEntityState(owner_ref)
        single_state = self.state_with_owner(
            owner,
            single_prompt=timeline_contracts.SinglePromptState(1, 0),
            prompt_response=1,
        )
        single_result = timeline_contracts.execute_extended_timeline_opcode(
            108,
            words(12, 13),
            0,
            0,
            single_state,
            timeline_contracts.TimelineOpcodeContext(
                script_lookups=(
                    timeline_contracts.ScriptLookupObservation(12, 3),
                )
            ),
        )
        multiple_state = self.state_with_owner(
            owner,
            multiple_prompt=timeline_contracts.MultiplePromptState((1,), 1),
        )
        multiple_result = (
            timeline_contracts.execute_extended_timeline_opcode(
                113,
                words(20, 21),
                0,
                0,
                multiple_state,
                timeline_contracts.TimelineOpcodeContext(
                    script_lookups=(
                        timeline_contracts.ScriptLookupObservation(20, 4),
                    )
                ),
            )
        )
        self.assertEqual(single_result.return_value, -1)
        self.assertEqual(multiple_result.return_value, -1)

        cursor = legacy_contracts.TimelineCursorState(
            active_group_index=0,
            paused=False,
            current_tick=0,
            advance_latched=False,
            lane_event_indexes=(0,),
        )
        for opcode, executor_result in (
            (108, single_result),
            (113, multiple_result),
        ):
            with self.subTest(opcode=opcode):
                lanes = (
                    (
                        legacy_contracts.TimelineEvent(
                            0,
                            (opcode, 37),
                        ),
                    ),
                )
                scheduled = legacy_contracts.step_timeline_script(
                    cursor,
                    lanes,
                    slow_time_enabled=False,
                    slow_divisor=1,
                    frame_counter=0,
                    extended_dispatch_results={
                        (0, 0, 0): executor_result.return_value
                    },
                )
                self.assertIs(scheduled.execution_aborted, True)
                self.assertEqual(
                    [dispatch.opcode for dispatch in scheduled.dispatches],
                    [opcode],
                )
                self.assertEqual(
                    scheduled.trace[-1],
                    "abort-extended-dispatch:0:0:0",
                )

        continued_executor = (
            timeline_contracts.execute_extended_timeline_opcode(
                108,
                words(0, 0),
                0,
                0,
                single_state,
                timeline_contracts.TimelineOpcodeContext(),
            )
        )
        continued = legacy_contracts.step_timeline_script(
            cursor,
            (
                (
                    legacy_contracts.TimelineEvent(0, (108, 37)),
                ),
            ),
            slow_time_enabled=False,
            slow_divisor=1,
            frame_counter=0,
            extended_dispatch_results={
                (0, 0, 0): continued_executor.return_value
            },
        )
        self.assertIs(continued.execution_aborted, False)
        self.assertEqual(
            [dispatch.opcode for dispatch in continued.dispatches],
            [108, 37],
        )


class TimelineOpcodeCorpusOracleTests(unittest.TestCase):
    def test_exhaustive_instruction_layout_and_extended_count_oracle(
        self,
    ) -> None:
        total_instructions = 0
        opcode_counts: Counter[int] = Counter()
        per_pack_counts: list[tuple[int, ...]] = []
        maximum_opcode = -1
        opcodes_above_127 = 0
        first_extended_positions: dict[
            int, tuple[int, int, int, int, int, int]
        ] = {}

        for pack in level_decoder.PACK_IDS:
            decoded = json.loads(
                (LEVELS_ROOT / f"pack-{pack}" / "records.json").read_text(
                    encoding="utf-8"
                )
            )
            pack_counts: Counter[int] = Counter()
            for group_index, group in enumerate(
                decoded["scripts"]["groups"]
            ):
                for lane_index, lane in enumerate(group["lanes"]):
                    for event_index, event in enumerate(lane["events"]):
                        for instruction_index, instruction in enumerate(
                            event["instructions"]
                        ):
                            opcode = instruction["opcode"]["raw_u8"]
                            layout = level_decoder.OPCODE_LAYOUTS[opcode]
                            total_instructions += 1
                            opcode_counts[opcode] += 1
                            pack_counts[opcode] += 1
                            if (
                                opcode in OBSERVED_EXTENDED_OPCODES
                                and opcode not in first_extended_positions
                            ):
                                first_extended_positions[opcode] = (
                                    pack,
                                    group_index,
                                    lane_index,
                                    event_index,
                                    instruction_index,
                                    instruction["offsets"]["start"],
                                )
                            maximum_opcode = max(maximum_opcode, opcode)
                            opcodes_above_127 += int(opcode > 127)
                            self.assertEqual(
                                instruction["operand_width"],
                                layout.width,
                            )
                            self.assertEqual(
                                instruction["operand_layout"],
                                layout.label,
                            )
                            self.assertEqual(
                                len(bytes.fromhex(instruction["raw_hex"])),
                                layout.width + 1,
                            )
                            self.assertEqual(
                                len(
                                    bytes.fromhex(
                                        instruction["operands"]["raw_hex"]
                                    )
                                ),
                                layout.width,
                            )
                            if opcode in ALL_EXTENDED_OPCODES:
                                raw_operands = bytes.fromhex(
                                    instruction["operands"]["raw_hex"]
                                )
                                contract_decode = (
                                    timeline_contracts
                                    .decode_extended_timeline_opcode(
                                        opcode,
                                        raw_operands,
                                    )
                                )
                                self.assertEqual(
                                    contract_decode.width,
                                    instruction["operand_width"],
                                )
                                self.assertEqual(
                                    contract_decode.operand_layout,
                                    instruction["operand_layout"],
                                )
                                self.assertEqual(
                                    contract_decode.effect_family,
                                    timeline_contracts
                                    .EXTENDED_OPCODE_EFFECT_FAMILIES[
                                        opcode
                                    ],
                                )
                                self.assertEqual(
                                    contract_decode.raw_operands,
                                    raw_operands,
                                )
                                decoder_values = []
                                for part in instruction["operands"][
                                    "interpreted_parts"
                                ]:
                                    if part["storage"] == "u8":
                                        value = part["raw_u8"]
                                    elif part["storage"] == "s16le":
                                        value = part["java_i16"]
                                    elif part["storage"] == "u16le":
                                        value = part["raw_u16"]
                                    else:
                                        value = part["raw_hex"]
                                    decoder_values.append(
                                        (
                                            part["name"],
                                            part["storage"],
                                            value,
                                        )
                                    )
                                self.assertEqual(
                                    tuple(
                                        (
                                            operand.name,
                                            operand.storage,
                                            operand.value,
                                        )
                                        for operand in (
                                            contract_decode.operands
                                        )
                                    ),
                                    tuple(decoder_values),
                                )
            per_pack_counts.append(
                tuple(pack_counts[opcode] for opcode in ALL_EXTENDED_OPCODES)
            )

        total_counts = tuple(
            opcode_counts[opcode] for opcode in ALL_EXTENDED_OPCODES
        )
        self.assertEqual(total_instructions, 3705)
        self.assertEqual(sum(total_counts), 480)
        self.assertEqual(maximum_opcode, 114)
        self.assertEqual(opcodes_above_127, 0)
        self.assertEqual(
            tuple(
                level_decoder.OPCODE_LAYOUTS[opcode].width
                for opcode in ALL_EXTENDED_OPCODES
            ),
            EXPECTED_WIDTHS,
        )
        self.assertEqual(total_counts, EXPECTED_TOTAL_COUNTS)
        self.assertEqual(tuple(per_pack_counts), EXPECTED_PER_PACK_COUNTS)
        corpus_fixtures = {
            fixture["input"]["opcode"]: fixture
            for fixture in fixture_manifest()["fixtures"]
            if fixture["basis"] == "corpus"
        }
        self.assertEqual(
            set(first_extended_positions),
            set(OBSERVED_EXTENDED_OPCODES),
        )
        for opcode, position in first_extended_positions.items():
            source = corpus_fixtures[opcode]["source"]
            self.assertEqual(
                position,
                (
                    source["pack"],
                    source["group_index"],
                    source["lane_index"],
                    source["event_index"],
                    source["instruction_index"],
                    source["offset"],
                ),
            )

        oracle = fixture_manifest()["corpus_oracle"]
        self.assertEqual(oracle["pack_order"], list(level_decoder.PACK_IDS))
        self.assertEqual(oracle["opcode_order"], list(ALL_EXTENDED_OPCODES))
        self.assertEqual(oracle["operand_widths"], list(EXPECTED_WIDTHS))
        self.assertEqual(
            oracle["total_counts"],
            list(EXPECTED_TOTAL_COUNTS),
        )
        self.assertEqual(
            oracle["per_pack_counts"],
            [list(row) for row in EXPECTED_PER_PACK_COUNTS],
        )
        self.assertEqual(oracle["instruction_count"], total_instructions)
        self.assertEqual(
            oracle["extended_instruction_count"],
            sum(total_counts),
        )
        self.assertEqual(oracle["maximum_opcode"], maximum_opcode)
        self.assertEqual(oracle["opcodes_above_127"], opcodes_above_127)


if __name__ == "__main__":
    unittest.main()
