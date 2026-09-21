package com.acrebuild.core

/**
 * Deferred commands a committed tick hands to backend adapters. Core owns the
 * decision (slot, when); adapters only execute. Nothing here may feed back
 * into gameplay within the same tick.
 */
sealed interface Command {
    data class PlaySfx(val slot: Int) : Command
    data class RequestSave(val snapshot: SaveSnapshot) : Command
}
