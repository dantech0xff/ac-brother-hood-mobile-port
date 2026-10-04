package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Save container v1 (ADR `docs/decisions/save-policy.md`). */
class SaveEnvelopeTest {
    private fun kba(): ByteArray = ByteArray(320) { (it * 7 + 3).toByte() }

    private fun ok(r: SaveEnvelope.Result): SaveEnvelope.Decoded =
        assertIs<SaveEnvelope.Result.Ok>(r).save

    private fun rejected(r: SaveEnvelope.Result): String =
        assertIs<SaveEnvelope.Result.Rejected>(r).reason

    @Test fun `v1 round-trips payload and revision`() {
        val p = kba()
        val d = ok(SaveEnvelope.decode(SaveEnvelope.encode(p, 41)))
        assertContentEquals(p, d.payload)
        assertEquals(41, d.revision)
        assertEquals(false, d.legacy)
    }

    @Test fun `the layout is header + payload + sha256`() {
        val e = SaveEnvelope.encode(kba(), 2)
        assertEquals(SaveEnvelope.HEADER_SIZE + 320 + SaveEnvelope.DIGEST_SIZE, e.size)
        assertEquals("ACBA", String(e, 0, 4, Charsets.US_ASCII))
        assertEquals(1, e[4].toInt()); assertEquals(0, e[5].toInt())        // version LE
        assertEquals(320 and 0xFF, e[16].toInt() and 0xFF)                  // length LE
        assertEquals(320 ushr 8, e[17].toInt())
    }

    @Test fun `a headerless 320-byte record is legacy v0`() {
        val raw = kba()
        val d = ok(SaveEnvelope.decode(raw))
        assertTrue(d.legacy)
        assertEquals(0, d.revision)
        assertContentEquals(raw, d.payload)
    }

    @Test fun `legacy raw only exists for the kBA schema`() {
        rejected(SaveEnvelope.decode(kba(), SaveEnvelope.SCHEMA_SPIKE_ACRS))
    }

    @Test fun `truncated envelopes are rejected`() {
        val e = SaveEnvelope.encode(kba(), 1)
        for (n in listOf(4, 19, 51, e.size - 1)) rejected(SaveEnvelope.decode(e.copyOf(n)))
    }

    @Test fun `wrong magic and other sizes are rejected`() {
        val e = SaveEnvelope.encode(kba(), 1)
        e[0] = 'X'.code.toByte()
        rejected(SaveEnvelope.decode(e))
        rejected(SaveEnvelope.decode(ByteArray(0)))
        rejected(SaveEnvelope.decode(ByteArray(319)))
        rejected(SaveEnvelope.decode(ByteArray(321)))
    }

    @Test fun `an oversized declared length is rejected before allocation`() {
        val e = SaveEnvelope.encode(kba(), 1)
        e[16] = 0xFF.toByte(); e[17] = 0xFF.toByte(); e[18] = 0xFF.toByte(); e[19] = 0x7F
        assertTrue("length" in rejected(SaveEnvelope.decode(e)))
    }

    @Test fun `a flipped header, payload or digest byte is rejected`() {
        val base = SaveEnvelope.encode(kba(), 9)
        for (i in listOf(8, 9, SaveEnvelope.HEADER_SIZE, SaveEnvelope.HEADER_SIZE + 200,
                         base.size - 1)) {
            val e = base.copyOf()
            e[i] = (e[i].toInt() xor 0x01).toByte()
            rejected(SaveEnvelope.decode(e))
        }
    }

    @Test fun `version and schema mismatches are rejected`() {
        val e = SaveEnvelope.encode(kba(), 1)
        rejected(SaveEnvelope.decode(e, SaveEnvelope.SCHEMA_SPIKE_ACRS))
        val v2 = e.copyOf(); v2[4] = 2
        assertTrue("v2" in rejected(SaveEnvelope.decode(v2)))
    }

    @Test fun `encode refuses oversized payloads and negative revisions`() {
        assertFailsWith<IllegalArgumentException> {
            SaveEnvelope.encode(ByteArray(SaveEnvelope.MAX_PAYLOAD + 1), 0)
        }
        assertFailsWith<IllegalArgumentException> { SaveEnvelope.encode(kba(), -1) }
    }

    @Test fun `the world's kBA record fits the legacy size`() {
        val w = world()
        w.saveFlush()
        val rec = w.drainCommands().filterIsInstance<Command.PersistBA>().last().record
        assertEquals(SaveEnvelope.LEGACY_KBA_SIZE, rec.size)
    }
}
