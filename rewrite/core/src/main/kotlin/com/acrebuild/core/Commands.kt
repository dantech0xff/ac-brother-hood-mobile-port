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

    /** `e.b()` (e.java:87, proven) — stop the single MMAPI `Player`
     *  channel. The original nulls/stops/closes `b` and sets `e=-1`. */
    object StopAudio : Command

    /** `A.notifyDestroyed()` (j.java:218, proven semantic) — reached via
     *  `j.c==11 → j.c=-1`, the EXIT menu path. `inferred` adaptation:
     *  the gdx launcher exits the app on drain (core can't kill the
     *  process itself). */
    object QuitApp : Command

    /** The `I(aj)`/`G(i)` pack swap (k.java:5244/:4740, proven
     *  semantic): a different mission's pack was just loaded — adapters
     *  must refresh mission-bound resources (tileset clips, level-bound
     *  caches). Emitted only when the pack actually changed. */
    data class MissionLoaded(val aj: Int) : Command
}
