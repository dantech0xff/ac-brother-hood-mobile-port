package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Slice-121 `az[]` object-remap contract (b.java:657/713/926, proven). */
class Slice121Test {
    private fun asset(path: String): ByteArray =
        java.io.File("../generated/$path").readBytes()
    private val clip0 by lazy { Clip.load(asset("clips/clip0/clip.acpk")) }
    private val clip7 by lazy { Clip.load(asset("clips/clip7/clip.acpk")) }

    @Test fun `clip0 carries exactly four remap tables`() {
        // k.eh={0}, k.ei={4} at k.java:5047-5053 → az[0..3] on clip 0 only.
        assertEquals(4, clip0.remapTables.size)
    }

    @Test fun `pack-4 tables blank-hide objects to obj 253`() {
        // Every decoded pack-4 pair remaps into object 253 (the blank
        // module); unlisted indices stay identity.
        val t = clip0.remapTables[0]
        assertEquals(253, clip0.remap(0, 141))
        assertEquals(253, clip0.remap(0, 282))
        assertEquals(0, clip0.remap(0, 0))      // 0 absent from pairs
        assertEquals(140, clip0.remap(0, 140))  // identity outside pairs
    }

    @Test fun `remap is identity on unset or out-of-range table`() {
        // `aa.a(n)` on a clip with no table n = no-op (b.java:714-720).
        assertEquals(55, clip7.remap(0, 55))
        assertEquals(55, clip0.remap(-1, 55))
        assertEquals(55, clip0.remap(4, 55))
    }
}
