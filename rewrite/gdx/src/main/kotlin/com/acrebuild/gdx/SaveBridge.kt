package com.acrebuild.gdx

import com.acrebuild.core.SavePort
import com.badlogic.gdx.Gdx

/**
 * save-runtime adapter: writes core-owned bytes into the app sandbox via an
 * atomic temp-file rename.
 */
class SaveBridge(private val fileName: String = "spike-save.bin") : SavePort {

    override fun write(bytes: ByteArray) {
        val target = Gdx.files.local(fileName)
        val tmp = Gdx.files.local("$fileName.tmp")
        tmp.writeBytes(bytes, false)
        if (target.exists()) target.delete()
        tmp.moveTo(target)
    }

    override fun read(): ByteArray? {
        val f = Gdx.files.local(fileName)
        return if (f.exists()) f.readBytes() else null
    }
}
