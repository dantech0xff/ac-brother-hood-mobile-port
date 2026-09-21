package com.acrebuild.core

import kotlin.test.Test

private fun assetDbg(path: String): ByteArray =
    java.io.File("../generated/$path").readBytes()

class DebugSlice2 {
    @Test fun `trace player + soldier`() {
        val level = LevelPack.load(assetDbg("level0/level0.aclv"))
        val clips = mapOf(
            0 to Clip.load(assetDbg("clips/clip0/clip.acpk")),
            7 to Clip.load(assetDbg("clips/clip7/clip.acpk")),
        )
        val w = Level0World(level, clips, DeterministicRandom(1L))
        val s = w.npcs.firstOrNull { it.ax == 11 }
        val q = InputQueue()
        q.post(InputQueue.Type.DOWN, 300, 120)
        for (i in 0 until 30) {
            w.tick(q.drainTo(q.headSequence()))
            val p = w.player
            println("t$i p S=${p.S} ak=${p.ak} al=${p.al} ag=${p.ag} ah=${p.ah} " +
                "aO=${p.aO} aR=${p.aR} aZ=${p.aZ} cq=${p.cq} held=${w.pad.held} edge=${w.pad.edge} " +
                (s?.let { "| npc S=${it.S} ak=${it.ak} ag=${it.ag} k=${it.k} aC=${it.aC}" } ?: ""))
        }
        println("npcs=${w.npcs.size} soldiers=${w.npcs.count { it.ax == 11 }}")
    }
}
