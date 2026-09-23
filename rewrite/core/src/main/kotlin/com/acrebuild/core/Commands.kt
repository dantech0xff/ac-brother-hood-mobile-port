package com.acrebuild.core

/**
 * Deferred commands a committed tick hands to backend adapters. Core owns the
 * decision (slot, when); adapters only execute. Nothing here may feed back
 * into gameplay within the same tick.
 */
sealed interface Command {
    data class PlaySfx(val slot: Int) : Command
    data class RequestSave(val snapshot: SaveSnapshot) : Command

    /** `e(true)` (k.java:5557) — persist the `bA` record: the original
     *  writes the raw 512-byte `bA` into RMS record 1 verbatim. The
     *  port carries `kBA` little-endian shorts (2 bytes per slot). */
    data class PersistBA(val record: ByteArray) : Command
}
