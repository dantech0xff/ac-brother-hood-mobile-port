package com.acrebuild.core

/**
 * Storage boundary for save-runtime. Core owns the byte format; platform
 * adapters implement atomic write/read inside the app sandbox.
 */
interface SavePort {
    fun write(bytes: ByteArray)
    fun read(): ByteArray?
}
