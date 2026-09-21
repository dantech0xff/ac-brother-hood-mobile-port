package com.acrebuild.core

/**
 * Port of the pack-6 level container (ACLV pack emitted by
 * `tools/convert_slice1.py`). Layer ids follow `docs/resource-formats.md`:
 * 0 = `et` collision grid (queried by `k.g`), 1/2/3 = visual `ep`/`eu`/`er`
 * each bound to a pack-15 tileset clip via `k.ej`, plus a 2-bit transform
 * flag per cell.
 */
class LevelPack private constructor(
    val cols: Int,
    val rows: Int,
    val cellPx: Int,
    val layers: List<Layer>,
    val entities: List<IntArray>,
) {

    class Layer(
        val id: Int,
        val tilesetClip: Int,
        val cols: Int,
        val rows: Int,
        val cells: IntArray,   // u8 tile/module index; 255-ish = empty
        val flags: IntArray?,  // 2-bit transform per cell
    ) {
        fun cell(cx: Int, cy: Int): Int {
            if (cx < 0 || cx >= cols || cy < 0 || cy >= rows) return -1
            return cells[cy * cols + cx]
        }
        fun flag(cx: Int, cy: Int): Int =
            if (flags == null || cx < 0 || cx >= cols || cy < 0 || cy >= rows) 0
            else flags[cy * cols + cx]
    }

    private val et: Layer = layers.first { it.id == 0 }

    /**
     * `k.g(x, y)` — collision query in *cell* coordinates. Out-of-bounds
     * returns 20 (solid border sentinel), exactly like the original
     * (`k.java:5284`; the `bh[aj]==3` remap is unused on level 0).
     */
    fun collisionCell(cx: Int, cy: Int): Int {
        if (cx < 0 || cx >= et.cols || cy >= et.rows) return 20
        if (cy < 0) return 20
        // original `k` load remaps 255 -> 0 (proven)
        val v = et.cells[cy * et.cols + cx]
        return if (v == 255) 0 else v
    }

    /** Pixel-space convenience: `k.g(x/20, y/20)`. */
    fun collisionAtPx(px: Int, py: Int): Int = collisionCell(px / cellPx, py / cellPx)

    /**
     * Solidity classification of an `et` cell value (per mined semantics:
     * 255 = air; >=12 and != 255 = solid/LOS-block; {2,3,5,18} = landing
     * markers, standable from above but pass-through otherwise).
     * Confidence: solid rule proven via dominant cell value 20 + OOB
     * sentinel; one-way list is high-confidence.
     */
    fun isSolid(v: Int): Boolean = v != 255 && v >= 12
    fun isOneWay(v: Int): Boolean = v == 2 || v == 3 || v == 5 || v == 18

    val worldW: Int get() = cols * cellPx
    val worldH: Int get() = rows * cellPx

    /** Player spawn: first record with raw type 0 (routed to `g(short[])`). */
    fun playerSpawn(): Pair<Int, Int>? =
        entities.firstOrNull { it.isNotEmpty() && (it[0] == 0 || it[0] == 25) }
            ?.let { it[2] to it[3] }

    companion object {
        fun load(data: ByteArray): LevelPack {
            val r = PackReader(data)
            r.magic("ACLV")
            require(r.u8() == 1) { "ACLV version" }
            val cols = r.u16(); val rows = r.u16(); val cellPx = r.u8()
            val layerCount = r.u8()
            val layers = ArrayList<Layer>(layerCount)
            repeat(layerCount) {
                val id = r.u8(); val tileset = r.u16(); val hasFlags = r.u8()
                val lw = r.u16(); val lh = r.u16()
                val cells = IntArray(lw * lh) { r.u8() }
                val flags = if (hasFlags != 0) IntArray(lw * lh) { r.u8() } else null
                layers += Layer(id, tileset, lw, lh, cells, flags)
            }
            val entCount = r.u16()
            val entities = ArrayList<IntArray>(entCount)
            repeat(entCount) {
                val n = r.u16()
                entities += IntArray(n) { r.i16() }
            }
            require(r.pos == data.size) { "trailing bytes in ACLV" }
            return LevelPack(cols, rows, cellPx, layers, entities)
        }
    }
}
