package com.acrebuild.gdx

import com.acrebuild.core.SaveEnvelope
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Durable files for one save slot (ADR `docs/decisions/save-policy.md`):
 * `<name>` (current), `<name>.bak` (previous), `<name>.<rev>.tmp` (in flight).
 *
 * Write: encode the [SaveEnvelope] with the next revision → write the temp
 * file, flush, `force(true)` → rotate a valid current file to `.bak` (a
 * corrupt one is dropped, never rotated over a good backup) → rename the temp
 * onto the current name. `File.renameTo` is POSIX `rename(2)` on Android and
 * Linux — an atomic replace; `java.nio.file` is not used because minSdk 24
 * predates it. Where rename refuses to replace (Windows desktop) the target is
 * deleted first (non-atomic, desktop only).
 *
 * Read: a valid current file wins; otherwise the highest-revision valid
 * candidate among complete temps and `.bak` (a temp only exists complete
 * after its `force`, and the digest rejects a torn one). A chosen temp is
 * renamed into place before leftover temps are removed. No valid candidate →
 * `null` (defaults).
 *
 * [crash] is a test hook: it runs after each [Step] and may throw to stop the
 * protocol there.
 */
class SaveStore(
    private val dir: File,
    private val name: String,
    private val schema: Int = SaveEnvelope.SCHEMA_KBA_V1,
    private val crash: (Step) -> Unit = {},
) {
    enum class Step { TEMP_WRITTEN, TEMP_FORCED, MAIN_ROTATED, MAIN_REPLACED }

    /** Highest revision seen on disk or written; -1 before the first read. */
    var revision = -1L
        private set

    private val main get() = File(dir, name)
    private val bak get() = File(dir, "$name.bak")

    private class Candidate(val file: File, val save: SaveEnvelope.Decoded)

    private fun load(f: File): Candidate? {
        if (!f.isFile) return null
        val bytes = try { f.readBytes() } catch (e: IOException) { return null }
        val r = SaveEnvelope.decode(bytes, schema)
        return if (r is SaveEnvelope.Result.Ok) Candidate(f, r.save) else null
    }

    private fun temps(): List<File> =
        dir.listFiles { f -> f.isFile && f.name.startsWith("$name.") && f.name.endsWith(".tmp") }
            ?.toList() ?: emptyList()

    fun read(): ByteArray? {
        val cur = load(main)
        val others = (temps().mapNotNull { load(it) } + listOfNotNull(load(bak)))
        val best = cur ?: others.maxByOrNull { it.save.revision }
        revision = maxOf(revision, (listOfNotNull(cur) + others).maxOfOrNull { it.save.revision } ?: 0L)
        if (best != null && best.file != main && best.file != bak) {
            // a complete temp of an interrupted write: finish its commit
            if (main.exists()) main.delete()
            move(best.file, main)
        }
        for (t in temps()) t.delete()
        return best?.save?.payload
    }

    fun write(payload: ByteArray) {
        if (revision < 0) read()
        val rev = revision + 1
        val bytes = SaveEnvelope.encode(payload, rev, schema)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("cannot create $dir")
        val tmp = File(dir, "$name.$rev.tmp")
        FileOutputStream(tmp).use { out ->
            out.write(bytes)
            out.flush()
            crash(Step.TEMP_WRITTEN)
            out.channel.force(true)
        }
        crash(Step.TEMP_FORCED)
        if (main.exists()) {
            if (load(main) != null) move(main, bak) else main.delete()
        }
        crash(Step.MAIN_ROTATED)
        move(tmp, main)
        revision = rev
        crash(Step.MAIN_REPLACED)
    }

    private fun move(src: File, dst: File) {
        if (src.renameTo(dst)) return
        if (dst.exists() && dst.delete() && src.renameTo(dst)) return
        throw IOException("cannot move ${src.name} → ${dst.name}")
    }
}
