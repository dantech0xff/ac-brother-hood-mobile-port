# Slice 420 mutants — each restores one pre-fix behaviour the slice proved wrong.
# Anchor rule: `old` occurs exactly once in the named file; `new` is the bug back.

BASE = "rewrite/core/src/main/kotlin/com/acrebuild/core/"

MUTANTS = [
    # A — armed path releases the marker again (the 1-tick flicker back)
    ("A armed releases marker", "NpcFsm.kt",
     [("        // @683-714: unbound + pad EDGE on the latch mask → tether shot",
       "        if (e.ak < p.ak && (m?.S == 42 || m?.S == 60)) p.releaseAe()\n"
       "        else if (e.ak > p.ak && m?.S == 48) p.releaseAe()\n"
       "        // @683-714: unbound + pad EDGE on the latch mask → tether shot")]),
    # B — fail-path right side drops S66 (back to {48} only)
    ("B fail right misses 66", "NpcFsm.kt",
     [("            else if (e.ak > p.ak && (m.S == 48 || m.S == 66)) p.releaseAe()",
       "            else if (e.ak > p.ak && m.S == 48) p.releaseAe()")]),
    # C — fail-path left side drops S60 (only S42 released)
    ("C fail left misses 60", "NpcFsm.kt",
     [("            if (e.ak < p.ak && (m.S == 42 || m.S == 60)) p.releaseAe()",
       "            if (e.ak < p.ak && m.S == 42) p.releaseAe()")]),
    # D — non-S7 ticks never retract the planted marker (@747 gone)
    ("D no non-S7 retract", "NpcFsm.kt",
     [("    else ax64MarkerRetract(e, p)                     // @18 → @747",
       "")]),
    # E — pin dropped: marker stops following the harrier
    ("E no pin", "NpcFsm.kt",
     [("        p.ae?.let { it.ak = e.ak; it.al = e.al - 20 }",
       "")]),
    # F — tether never fires (G-gate inverted: bound harriers re-tether)
    ("F tether gated on bound", "NpcFsm.kt",
     [("        if (!e.runnerG && w.padHeld(e.bl)) {",
       "        if (e.runnerG && w.padHeld(e.bl)) {")]),
    # G — (dropped) "follow arm drops S66": unobservable — the @512-575
    # follow writes ae=(e.ak, e.al-40) and the @715 pin immediately
    # overwrites ae=(e.ak, e.al-20) on the same armed tick, so a lone
    # S60-arm produces the identical end state for an S66 marker.
    # H — @747 retracts on every side (equal-ak too)
    ("H retract equal-ak", "NpcFsm.kt",
     [("    if (e.ak < p.ak && m.S == 60) p.releaseAe()      // @756-780\n"
       "    else if (e.ak > p.ak && m.S == 66) p.releaseAe() // @783-807",
       "    if (e.ak <= p.ak && m.S == 60) p.releaseAe()     // @756-780\n"
       "    else if (e.ak >= p.ak && m.S == 66) p.releaseAe() // @783-807")]),
]
