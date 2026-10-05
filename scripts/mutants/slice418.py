# Slice 418 mutants — each restores one pre-fix behaviour the slice proved wrong.
# Anchor rule: `old` occurs exactly once in the named file; `new` is the bug back.

BASE = "rewrite/core/src/main/kotlin/com/acrebuild/core/"

MUTANTS = [
    # camRect derived live again — the whole stale-snapshot bug back
    ("A live camRect", "Level0World.kt",
     [("    override val camRect: IntArray get() = ac",
       "    override val camRect: IntArray get() =\n        intArrayOf(camX, camY, camX + VIEW_W, camY + VIEW_H)")]),
    # kO/kP setters rebuild ac — mid-tick writes move the cull edge
    ("B setters rebuild ac", "Level0World.kt",
     [("    override var kO: Int get() = camX; set(v) { camX = v }\n"
      "    /** `k.P` — camera top edge (u()'s view-center operand; writable). */\n"
      "    override var kP: Int get() = camY; set(v) { camY = v }",
       "    override var kO: Int get() = camX; set(v) { camX = v; rebuildCamRect() }\n"
       "    /** `k.P` — camera top edge (u()'s view-center operand; writable). */\n"
       "    override var kP: Int get() = camY; set(v) { camY = v; rebuildCamRect() }")]),
    # m(I) tail drops the rebuild — ac stays stale forever after kM
    ("C kM no rebuild", "Level0World.kt",
     [("        rebuildCamRect()                          // m(I) tail @2680-2719\n",
       "")]),
    # D() autoscroll tail drops the rebuild
    ("D kD tail no rebuild", "Level0World.kt",
     [("        rebuildCamRect()                  // D() tail @651-690 (proven)\n",
       "")]),
    # D() snap arm drops the rebuild (j.c==21 / claim-Z arm)
    ("E kD snap no rebuild", "Level0World.kt",
     [("            camA = camX; camB = camY                                     // snap\n"
      "            rebuildCamRect()                // D() snap arm @52-91 (proven)\n"
      "            return",
       "            camA = camX; camB = camY                                     // snap\n"
       "            return")]),
]
