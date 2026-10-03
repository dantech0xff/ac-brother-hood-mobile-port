package com.acrebuild.gdx

import com.acrebuild.core.SaveEnvelope
import com.acrebuild.core.SavePort
import com.badlogic.gdx.Gdx

/**
 * save-runtime adapter: core-owned payloads go into the app sandbox
 * (`Gdx.files.local`) through [SaveStore] — the [SaveEnvelope] container and
 * the temp → force → rotate → rename write protocol (ADR
 * `docs/decisions/save-policy.md`). The game passes the `kBA` schema; the
 * toolchain spike keeps its own file and schema.
 */
class SaveBridge(
    private val fileName: String = "spike-save.bin",
    private val schema: Int = SaveEnvelope.SCHEMA_SPIKE_ACRS,
) : SavePort {

    private val store by lazy { SaveStore(Gdx.files.local("").file(), fileName, schema) }

    override fun write(bytes: ByteArray) = store.write(bytes)

    override fun read(): ByteArray? = store.read()
}
