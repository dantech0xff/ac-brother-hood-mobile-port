# Slice 417 mutants — each restores one pre-fix behaviour the slice proved wrong.
# Anchor rule: `old` occurs exactly once in the named file; `new` is the bug back.

BASE = "rewrite/core/src/main/kotlin/com/acrebuild/core/"

MUTANTS = [
    # snap arm syncs N/O (old: setPositionPx) — kills the desync quirk
    ("A snap syncs N/O", "Level0World.kt",
     [("            player.ak = s.ak\n            player.al = s.al",
       "            player.setPositionPx(s.ak, s.al)")]),
    # record flag word not loaded — stale P survives respawn
    ("B P not loaded", "Level0World.kt",
     [("            player.P = rec[6]", "            ")]),
    # record facing not loaded — stale av survives respawn
    ("C av not loaded", "Level0World.kt",
     [("            player.av = rec[6] and 1 != 0", "            ")]),
    # g.<init> tail cG latch missing — stale grab-facing survives
    ("D cG not latched", "Level0World.kt",
     [("        player.cG = player.av", "        ")]),
    # E() skipped on the checkpoint path (old s==null guard)
    ("E settle gated by snap", "Level0World.kt",
     [("        if (rec == null || rec[0] != 25) player.eSettle(this)",
       "        if (checkpointSnap == null && (rec == null || rec[0] != 25)) player.eSettle(this)")]),
    # no ctor reset at all — the whole stale-field bug back
    ("F no ctor reset", "Level0World.kt",
     [("        player.resetToFreshSpawn()", "        ")]),
    # g.f cleared on spawn (old over-reset — the original keeps it)
    ("G gf cleared", "Level0World.kt",
     [("        playerLinkB = null                          //   g.b — the second",
       "        playerLinkB = null\n        Entity.gf = null")]),
    # g.t/g.n/g.o/g.B over-reset back (original persists them)
    ("H persist statics cleared", "Level0World.kt",
     [("        player.resetToFreshSpawn()",
       "        player.resetToFreshSpawn()\n        player.gt = 0; player.gn = 0; player.go = 0; player.gB = false")]),
    # g.b's world-side link copy not swept (g.g/g.F/gg live on the Entity —
    # the ctor reset covers them; playerLinkB is a Level0World field)
    ("I playerLinkB kept", "Level0World.kt",
     [("        playerLinkB = null                          //   g.b — the second",
       "        ")]),
    # i.bN ctor value wrong (1, not 0)
    ("J bN zero", "Entity.kt",
     [("        bN = 1", "        bN = 0")]),
    # record position not loaded — player keeps stale ak/al/N/O
    ("K pos not loaded", "Level0World.kt",
     [("        if (rec != null && rec.size > 3) player.setPositionPx(rec[2], rec[3])\n        else player.setPositionPx(100, 200)",
       "        if (rec == null) player.setPositionPx(100, 200)")]),
    # clip not rebound — stale clip slot survives respawn
    ("L clip not rebound", "Level0World.kt",
     [("        player.clip = clips[entityClipIndex(rec?.get(0) ?: 0, rec ?: intArrayOf()) ?: 0]",
       "        ")]),
]
