---
title: Slice 278 — x9000 wall crossing proven + capstone wall leg
phase: capstone-route
status: done
---

# Slice 278 — x9000 wall crossing proven end-to-end

## Verdict

The x9000 wall is mechanically crossable with the shipped arms — proven
end-to-end on real level-0 geometry with real input events (no state
pinning). Route decoded this session:

1. Floor (y800) → fall/run into the chimney x8940-9000 → `S101` face grab
2. Kick auto-bounce (S92/101 `r()→av=!av; ag=∓2048; ah=-5120`), ~70px
   rise per leg between the 60px faces — alternating corner-tap holds
   (cells 0/2) or M_UP arm the `aF` grab latch
3. West-drift apex under the LOW '5' strip (row 19, y380-400, x8740-8900)
   → `cw&&aO==5 → S280` ceiling grab (y390)
4. `S280→S38` hang → hold cell 5 (M_RIGHT) → `S37` shimmy east
5. At the strip's east end (x>8890) tap UP → `S38` vault-out arm
   `u(M_UP)→probe→a(54,8)` (PlayerFsm.kt:813-870, proven L1502)
6. Rise reaches under the BRIDGE '5' strip (row 13, y260-280, x8940-9160)
   → `S280` grab (y270)
7. Shimmy east → drop → descend onto the '02' one-way plateau top (y420)
   — one-way landing x~9000-9300, over the 200px pit (floor '20' row 55+)

## New geometry decoded (cell dumps)

- Wall mass x9000-9080 '20' SOLID rows 22-47 — no street under the plateau
- '02' plateau row 21 (top y420) x9000-9550 floats over a ~200px void pit
  — `isOneWay(v)=v==2||3||5||18` (LevelPack.kt:94), can't be lip-grabbed
  (needs cell ≥19)
- BRIDGE '5' strip row 13 (y260-280) x8940-9160 — second monkey-bar,
  key discovery closing the route
- ax14 marker-zone chain marks the interact region: @8957,305 bounds
  x8857-9007 y255-385 bridges low-strip end → bridge strip
- ax74 wisp trail: zigzag legs then y302 aerial line matching the route

## Test evidence

`chimney kick-zigzag + strip ceiling-grab + shimmy chain proven` —
grabs=48, ceilingGrab, shimmy, vaultOut, bridgeGrab, wallLand all true,
minAl=166, plateau landing at (9293,388).

## Capstone

- Route policy: new `p.ak in 8890..9560 && p.al < 850` arm driving the
  proven chain (velocity-matched holds + UP, shimmy east, vault-out at
  low-strip end, plateau run).
- Attack gate: `foeBlocking = foe.S == 144` suppression — NpcFsm.kt:632-657
  (i.java:6048-6066 proven) RECOVERS weakened guards AND back-counters
  attackers; swinging at the block just feeds the counter.
- Result: `maxAk=9212` (plateau lip crossed, was 8999), `minAl=270`.
  Assert raised to `maxAk > 9100`.
- Remaining blocker: posted ax11 @8850 (aB=300, alert x8690-9010) guards
  the chimney mouth + respawn point — the duel kills the bot (247 deaths).
  That's the next frontier (aB=300 elite = intended skill gate).

## Files

- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  Slice277Test rewritten to the full-route verdict (dumps removed),
  capstone wall leg + foeBlocking suppression, frontier docs/assert.
