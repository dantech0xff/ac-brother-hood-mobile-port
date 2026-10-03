package com.acrebuild.gdx

import com.acrebuild.core.SaveEnvelope
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Write protocol + recovery of [SaveStore] (ADR `docs/decisions/save-policy.md`). */
class SaveStoreTest {
    private val dir: File = Files.createTempDirectory("savestore").toFile()

    @AfterTest fun cleanup() { dir.deleteRecursively() }

    private fun payload(seed: Int) = ByteArray(320) { (it * 31 + seed).toByte() }
    private fun store(crash: (SaveStore.Step) -> Unit = {}) = SaveStore(dir, "asbr-save.bin", crash = crash)
    private fun tmpCount() = dir.listFiles()!!.count { it.name.endsWith(".tmp") }

    @Test fun `empty dir reads null`() {
        assertNull(store().read())
    }

    @Test fun `writes round-trip and the revision climbs`() {
        val s = store()
        s.write(payload(1)); s.write(payload(2))
        assertEquals(2, s.revision)
        val fresh = store()
        assertContentEquals(payload(2), fresh.read())
        assertEquals(2, fresh.revision)
        assertTrue(File(dir, "asbr-save.bin.bak").isFile, "previous save rotated to .bak")
        assertEquals(0, tmpCount())
    }

    @Test fun `a legacy raw record loads and is rewritten as v1`() {
        File(dir, "asbr-save.bin").writeBytes(payload(5))
        val s = store()
        assertContentEquals(payload(5), s.read())
        s.write(payload(6))
        val r = SaveEnvelope.decode(File(dir, "asbr-save.bin").readBytes())
        val d = (r as SaveEnvelope.Result.Ok).save
        assertEquals(false, d.legacy); assertEquals(1, d.revision)
        assertContentEquals(payload(6), d.payload)
    }

    @Test fun `a corrupt current file falls back to bak and is never rotated over it`() {
        val s = store(); s.write(payload(1)); s.write(payload(2))
        File(dir, "asbr-save.bin").writeBytes(ByteArray(77))          // corrupt main
        val r = store()
        assertContentEquals(payload(1), r.read(), "bak holds the previous save")
        r.write(payload(3))
        assertContentEquals(payload(3), store().read())
        val bak = SaveEnvelope.decode(File(dir, "asbr-save.bin.bak").readBytes())
        assertContentEquals(payload(1), (bak as SaveEnvelope.Result.Ok).save.payload,
            "the corrupt file was dropped, not rotated")
    }

    private class Crash : RuntimeException()

    @Test fun `a crash at any step leaves the old or the new save`() {
        for (step in SaveStore.Step.values()) {
            dir.listFiles()!!.forEach { it.delete() }
            store().write(payload(1))
            val s = store { if (it == step) throw Crash() }
            try { s.write(payload(2)) } catch (e: Crash) { }
            val got = store().read()
            assertTrue(got != null &&
                (got.contentEquals(payload(1)) || got.contentEquals(payload(2))),
                "after a crash at $step the save is intact")
            assertEquals(0, tmpCount(), "read() clears leftover temps ($step)")
            if (step >= SaveStore.Step.MAIN_ROTATED)
                assertContentEquals(payload(2), got, "a forced temp is committed on read ($step)")
        }
    }

    @Test fun `a torn temp is ignored`() {
        val s = store(); s.write(payload(1)); s.write(payload(2))      // main=2, bak=1
        File(dir, "asbr-save.bin.3.tmp").writeBytes(SaveEnvelope.encode(payload(3), 3).copyOf(100))
        File(dir, "asbr-save.bin").delete()
        assertContentEquals(payload(1), store().read(), "the torn temp fails its digest; bak wins")
        assertEquals(0, tmpCount())
    }
}
