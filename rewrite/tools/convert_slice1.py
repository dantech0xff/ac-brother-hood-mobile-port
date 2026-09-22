#!/usr/bin/env python3
"""Port-slice-1 asset converter.

Reads the verified decoded artifacts under
``reconstructed-project/resources/`` and emits runtime packs consumed by the
Kotlin ``core`` module:

* ``generated/clips/<name>/clip.acpk`` — binary clip pack (modules, frame
  pool, anim table, per-anim rect pool) ported 1:1 from the ``b`` sprite
  format documented in ``docs/resource-formats.md``.
* ``generated/clips/<name>/modules/*.png`` — palette-00 module bitmaps.
* ``generated/level0/level0.aclv`` — pack-6 tile layers (collision ``et`` +
  visual ``ep``/``eu``/``er`` with their 2-bit flag planes ``eq``/``ev``/``es``)
  and the entity descriptor records (verbatim s16 field arrays).
* ``generated/level0/tileset-<n>/`` — pack-15 tileset module bitmaps.

Binary layouts (little-endian, documented in ``core`` readers):

ACPK:  magic 'ACPK' u32 | u8 version=1 |
        u16 moduleCount { u16 w, u16 h, u8 nameLen, name bytes }* |
        u16 animCount   { u16 frameStart, u16 frameCount }* |
        u32 frameCount  { u16 module, u8 durationTicks, i16 dx, i16 dy,
                          u8 flags }* |
        u16 objectCount { u16 rectStart, u16 rectCount,
                          u16 placeStart, u16 placeCount }*    // ai_aj_ao
                                                             // object space
        u32 rectCount   { i16 x, i16 y, i16 w, i16 h }*        // am_or_an
        u32 placeCount  { u16 module, u8 flags, i16 x, i16 y }*// ap_aq_ar_as

`ao` (rect spans) and `ai`/`aj` (placement spans) share one 0..N-1 drawable-
object index space — the same space used by `av` module indices in the
8-arg entity draw (`b.java:907`) and by `Z[7]` composite draws.
Rect select: `W = rects[ao[obj] + 0]` (hitbox), `X = +1` (attackbox).

ACLV:  magic 'ACLV' u32 | u8 version=1 |
        u16 cols | u16 rows | u8 cellPx |
        u8 layerCount { u8 layerId, u16 tilesetClip, u8 hasFlags,
                        u16 cols, u16 rows,
                        u8 cells[cols*rows], u8 flags[cols*rows]? }* |
        u16 entityCount { u16 fieldCount, i16 fields[fieldCount] }*

Static-only: reads decoded artifacts, never executes the JAR.
"""
import json
import shutil
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "reconstructed-project" / "resources"
SPR = RES / "sprites-decoded"
OUT = ROOT / "rewrite" / "generated"

# Level 0 = pack-6; visual layers ep/eu/er map to pack-15 tileset clips via
# k.ej[0..3] = {11,10,12,10}: slot order in k.G() is [ep-clip, eu-clip,
# er-clip, ?] — verified assignment below follows the G(8) read order:
#   entries 4/8/11 = ep/eu/er, clips ej[0],ej[1],ej[2] = 11,10,12 (ej[3]
#   repeats the eu tileset for an unused 4th layer slot).
# Confidence: high-confidence (draw order); flag-plane pairing is proven
# (each visual slot is followed by dims+flags entries).
LEVEL_PACK = 6
TILESETS = {"ep": 11, "eu": 10, "er": 12}
LAYER_ENTRIES = {"et": 1, "ep": 4, "eu": 8, "er": 11}
FLAG_ENTRIES = {"et": 3, "ep": 6, "eu": 10, "er": 13}
DIM_ENTRIES = {"et": 2, "ep": 5, "eu": 9, "er": 12}
VISUAL_LAYERS = ["ep", "eu", "er"]

CLIPS = {
    "clip0": ("pack-3", "entry-000-marker-130"),   # player (z[0])
    "clip1": ("pack-3", "entry-001-marker-003"),   # ax5 mission logic (bi[5]=1)
    "clip48": ("pack-3", "entry-048-marker-003"),  # ax27 fuse/message (bi[27]=48)
    "clip45": ("pack-3", "entry-045-marker-003"),  # ax40 zipline gondola (bi[40]=45)
    "clip62": ("pack-3", "entry-062-marker-003"),  # ax35 multi-tool (bi[35]=62)
    "clip3": ("pack-3", "entry-003-marker-003"),   # ax4 destructibles (bi[4]=3)
    "clip7": ("pack-3", "entry-007-marker-130"),   # shared NPC family
    "clip9": ("pack-3", "entry-009-marker-003"),   # k.c marker popup (ax14 S54)
    "clip32": ("pack-3", "entry-032-marker-003"),  # ax44 door/gate (bi[44]=32)
    "clip54": ("pack-3", "entry-054-marker-003"),  # ax74 wisp (m(-1) bursts)
    "clip64": ("pack-3", "entry-064-marker-003"),  # ax67 kind-9 decor props (bk[9]=64)
    "clip26": ("pack-3", "entry-026-marker-003"),  # generic a(ax) small-item clip
    "clip10": ("pack-3", "entry-010-marker-003"),  # ax16 request markers (bi[16]=10)
    "clip27": ("pack-3", "entry-027-marker-003"),  # ax67 springboard (bk{1,2,3}=27)
    "clip35": ("pack-3", "entry-035-marker-003"),  # ax67 kind-5 interactives (bk[5]=35)
    "clip47": ("pack-3", "entry-047-marker-003"),  # ax9 bM() push/contact (bi[9]=47)
    "clip31": ("pack-3", "entry-031-marker-003"),  # ax9 ad-child spawn (bM :21198)
    "clip4": ("pack-3", "entry-004-marker-003"),   # ax6 trigger marker (bi[6]=4)
    "clip11": ("pack-3", "entry-011-marker-003"),  # ax19 pickup (bi[19]=11)
}

# pack-15 per-level tilesets are the same `b` clip format — cells index
# their composite-object space (7-arg draw, k.java:4505-4530).
TILESET_CLIPS = {10: "pack-15", 11: "pack-15", 12: "pack-15"}


def u8(v):
    return v & 0xFF


def pack_clip(pack, entry_dir, out_dir):
    meta_path = SPR / pack / entry_dir / "metadata.json"
    meta = json.loads(meta_path.read_text())
    secs = {s["id"]: s for s in meta["sections"]}

    mods = secs["modules"]["records"]
    module_pngs = []
    # b.java:2436 `l(int)` = per-entity palette slot `aH`; export every
    # variant the decoder produced, keyed palette-00 in the blob so the
    # renderer substitutes by filename suffix.
    all_pngs = []
    for m in mods:
        pngs = sorted((SPR / pack / entry_dir).glob(
            f"module-{m['index']:04d}-palette-*-*.png"))
        if not pngs:
            # aU==2 = non-pixel module (vector/marker group): the decoder
            # emits no PNG by design. Keep an empty-name slot so module
            # indexing stays aligned; renderers skip empty names.
            assert m.get("runtime_type_aU") == 2, (
                f"missing pngs for module {m['index']} in {entry_dir}")
            module_pngs.append(("", m["ae_width"], m["af_height"]))
            continue
        p0 = [p for p in pngs if "-palette-00-" in p.name]
        assert p0, f"missing palette-00 png for module {m['index']}"
        module_pngs.append((p0[0].name, m["ae_width"], m["af_height"]))
        all_pngs.extend(p.name for p in pngs)

    frames = secs["av_aw_ax_ay_i_records"]["records"]
    anims = secs["au_h_records"]["records"]
    # Per-anim rect pool: ai_aj_ao carries ao_start/ao_end (rect span) and
    # am_or_an holds the rect quads in x,y,w,h order (u8 or i16 storage,
    # normalized here to i16).
    rects_meta = secs.get("ai_aj_ao_and_ak_or_al_records", {}).get("records", [])
    rect_pool = [r["runtime_values"] for r in
                 secs.get("am_or_an_records", {}).get("records", [])]
    # ak_or_al = per-object bounds quads (i.java t() Y fill, b.java:868-895
    # g/h/e/f readers). Decoded as runtime_values [x,y,w,h] i16-le.
    bounds_quads = [r["runtime_values"] for r in
                    secs.get("ai_aj_ao_and_ak_or_al_records", {}).get(
                        "ak_or_al_records", [])]

    blob = bytearray()
    blob += struct.pack("<4sB", b"ACPK", 1)
    blob += struct.pack("<H", len(module_pngs))
    for name, w, h in module_pngs:
        blob += struct.pack("<HHB", w, h, len(name)) + name.encode()
    blob += struct.pack("<H", len(anims))
    for a in anims:
        blob += struct.pack("<HH", a["h"]["java_i16"], a["au"]["raw_u8"])
    blob += struct.pack("<I", len(frames))
    for f in frames:
        blob += struct.pack("<HBhhB", f["av"]["raw_u8"],
                            f["aw"]["raw_u8"], f["ax"]["runtime_signed"],
                            f["ay"]["runtime_signed"], f["i"]["raw_u8"])
    blob += struct.pack("<H", len(rects_meta))
    for r in rects_meta:
        r0 = r.get("ao_start", 0)
        r1 = r.get("ao_end_exclusive", r0)
        blob += struct.pack("<HHHH", r0, r1 - r0,
                            r["aj"]["java_i16"], r["ai"]["raw_u8"])
    blob += struct.pack("<I", len(rect_pool))
    for x, y, w, h in rect_pool:
        blob += struct.pack("<hhhh", x, y, w, h)
    blob += struct.pack("<I", len(bounds_quads))
    for x, y, w, h in bounds_quads:
        blob += struct.pack("<hhhh", x, y, w, h)
    placements = secs.get("ap_ar_as_aq_records", {}).get("records", [])
    blob += struct.pack("<I", len(placements))
    for p in placements:
        # ar/as storage width varies by clip flags (u8 vs u16-le);
        # runtime value identical either way.
        ar = p["ar"].get("java_i16", p["ar"].get("java_i8"))
        as_ = p["as"].get("java_i16", p["as"].get("java_i8"))
        blob += struct.pack("<HBhh", p["ap"]["raw_u8"],
                            p["aq"]["raw_u8"], ar, as_)

    (out_dir / "modules").mkdir(parents=True, exist_ok=True)
    for name in all_pngs:
        shutil.copy2(SPR / pack / entry_dir / name, out_dir / "modules" / name)
    (out_dir / "clip.acpk").write_bytes(bytes(blob))
    (out_dir / "meta.json").write_text(json.dumps({
        "source": f"{pack}/{entry_dir}",
        "modules": len(module_pngs), "anims": len(anims),
        "frames": len(frames), "rects": len(rect_pool),
        "bounds": len(bounds_quads)}, indent=1))
    print(f"clip {entry_dir}: {len(anims)} anims {len(frames)} frames "
          f"{len(module_pngs)} modules {len(rect_pool)} rects "
          f"{len(bounds_quads)} bounds")


def pack_level():
    pack_dir = RES / "decoded" / f"pack-{LEVEL_PACK}"
    dims = {k: json.loads((pack_dir /
            f"entry-{DIM_ENTRIES[k]:03d}-dimensions.json").read_text())
            for k in LAYER_ENTRIES}
    cols, rows = dims["et"]["width"], dims["et"]["height"]

    records = json.loads(
        (RES / "levels-decoded" / f"pack-{LEVEL_PACK}" / "records.json")
        .read_text())["entities"]["records"]
    entities = [[f["java_i16"] for f in r["fields"]] for r in records]

    blob = bytearray()
    blob += struct.pack("<4sB", b"ACLV", 1)
    blob += struct.pack("<HHB", cols, rows, 20)

    # Cells are u8 tile indices. Each visual layer carries its own dims
    # (eu is a small 21x13 backdrop grid) and a packed 2-bit flag plane.
    # layerId: 0=et(collision), 1=ep, 2=eu, 3=er
    layers = [("et", 0, 0, True)] + [
        (k, i + 1, TILESETS[k], True) for i, k in enumerate(VISUAL_LAYERS)]
    blob += struct.pack("<B", len(layers))
    for key, lid, tileset, has_flags in layers:
        lw, lh = dims[key]["width"], dims[key]["height"]
        cells = (pack_dir /
                 f"entry-{LAYER_ENTRIES[key]:03d}-marker-003.bin").read_bytes()
        assert len(cells) == lw * lh, (key, len(cells), lw * lh)
        blob += struct.pack("<BH BHH".replace(" ", ""), lid, tileset,
                            1 if has_flags else 0, lw, lh)
        blob += bytes(cells)
        if has_flags:
            flags = (pack_dir /
                     f"entry-{FLAG_ENTRIES[key]:03d}-marker-003.bin").read_bytes()
            # 2-bit packed flags, row-major; expand to u8 per cell.
            # 2-bit flags packed MSB-first per byte:
            # dX = (flags[i>>2] >> ((3 - (i&3)) << 1)) & 3  (k.java:4476).
            expanded = bytearray()
            for i in range(lw * lh):
                expanded.append((flags[i >> 2] >> ((3 - (i & 3)) << 1)) & 3)
            blob += bytes(expanded)

    blob += struct.pack("<H", len(entities))
    for fields in entities:
        blob += struct.pack("<H", len(fields))
        blob += struct.pack(f"<{len(fields)}h", *fields)

    out = OUT / "level0"
    out.mkdir(parents=True, exist_ok=True)
    (out / "level0.aclv").write_bytes(bytes(blob))
    # per-level string table — `k.d(1+k.aj, idx)` (k.java:486) resolves
    # through j.g to pack-14 entry-<level>; level 0 → entry-001.
    strings_src = (RES / "decoded" / "pack-14" / "entry-001-strings.json")
    (out / "strings-1.json").write_text(strings_src.read_text())
    # line-delimited variant for the gdx loader (no JSON dep): inner
    # newlines escaped as \n so one string = one line.
    _strings = json.loads(strings_src.read_text())
    (out / "strings-1.txt").write_text(
        "\n".join(s.replace("\n", "\\n") for s in _strings) + "\n")
    (out / "meta.json").write_text(json.dumps({
        "source": f"pack-{LEVEL_PACK}", "cols": cols, "rows": rows,
        "worldPx": [cols * 20, rows * 20], "entities": len(entities),
        "layers": {k: {"entry": LAYER_ENTRIES[k], "tileset": TILESETS.get(k),
                       "dims": [dims[k]["width"], dims[k]["height"]]}
                   for k in LAYER_ENTRIES},
        "strings": "pack-14/entry-001-strings.json",
        "scripts": "pack-6/entry-007 (k.by/bz/eH, k.java:6196)"}, indent=1))
    # `j.e(7)` of the mission pack → k.by/bz/eH script tables
    # (k.java:6190-6320) — carried raw; ScriptTables.load parses it.
    (out / "scripts.bin").write_bytes(
        (pack_dir / "entry-007-marker-003.bin").read_bytes())

    for name, clip in TILESETS.items():
        src_dir = next((SPR / "pack-15").glob(f"entry-{clip:03d}-*"))
        # full clip pack (cells index the composite-object space)
        pack_clip("pack-15", src_dir.name, out / f"tileset-{clip}")
    print(f"level0: {cols}x{rows} cells, {len(entities)} entities, "
          f"tilesets {sorted(set(TILESETS.values()))}")


def main():
    for name, (pack, entry) in CLIPS.items():
        pack_clip(pack, entry, OUT / "clips" / name)
    pack_level()
    print("convert_slice1: ok")


if __name__ == "__main__":
    sys.exit(main())
