---
title: Slice 215 — ax7 mouth-throw landing wedge is verbatim original behavior
status: done
confidence: proven
---

# Slice 215 — ax7 wedge verdict

## Question carried over

The demo-verify run (`plans/260922-0730-demo-verify`) found the ax7
mouth-plant (uid 12, record pos 1397,506) swallows the player at the
pocket (~1326,464) and releases him at ~(1470,499) **deep-embedded in the
wall's top-east corner** → forced S79, input-immune, `bd=false`. Open
question: port divergence or original behavior?

## Verdict: verbatim original behavior — a shipped softlock

Every link in the chain was traced tick-by-tick on the real level-0 grid
(`Slice215Test`) and matched against the fallback source:

1. **Throw direction**: the ax7 record carries `P=0` → `e.av=false` →
   release writes `aS.ag = 2048` east (i.java:15695-15710, proven).
2. **The ride**: the S1 arm re-snaps `p.ak/al` to the mouth's *current*
   frame-W centre each tick (i.java ~15640). The decoded clip swings the
   mouth W east as far as x1495 (T9, past the corner x1480), then back
   west; last frame T11 is W [1466,458,1470,467] → centre (1468,462).
3. **Release point**: `r()` (i.java:462) = `T == frameCount-1 &&
   U == dur-1` → fires at T11 → release anchor (1468,462). The player's
   own S313 last-frame W is a degenerate 0×0 point at (1480,459) —
   verbatim decoded clip data.
4. **`a(43,32)`** (i.java:2336): mask-32 arm `al += u - Wc_new` uses the
   *stale* `u` (last `a(true)` resolve centre = 459) vs the new S43 W
   centre (430) → +29px; plus `al += 10` → anchor (1468,501).
5. **One fall tick**: `ag=2048` → +8px east, wall-face resolve pushes −8
   west → lands (1468,499) with `aO=aR=aP=20` (fully embedded →
   `bd=false`, `collideSides` resolve dead — verbatim `probeCells`
   else-branch).
6. **`L17cc`** (g.java): deep embed → `i(79)` + goto L353d — the `l()`
   input arms (S79 crawl) and the `u(33024)` ledge-escape only run when
   `aO<=12 || aR<=12` (shallow). The original wedges identically —
   input-immune forever.

Reproduced identically for ax7 uid 30 (4801,674): swing east ~170px,
release → deep-embed S79 at (4872,659).

So the mouth-plant is a **trap** at these corners: the record-facing throw
(`ag=+2048`, always east) deposits the player into the wall where the
deep-embed state holds him permanently. In the original this is a
softlock — the player restarts the mission. Faithful = keep it.

## Change set

- `Slice1Test.kt` — `Slice215Test`: regression test pinning the verbatim
  chain (capture → S313 + P|64 → swing past x1480 → release ag=2048 at
  ~(1468,501) → deep-embed S79 with aO=aR=aP=20, bd=false).
- No production-code changes — the port was already 1:1.

## Noted for later

- Shallow-embed S79 exits exist (`l()` crawl arms + `M_DOWN` →
  `a(257,8)` ledge escape, already ported in `groundedTail`/`ledgeDrop257`)
  — they just never arm at deep embed.
- The demo route through this plant wedges in the original too; the
  intended traversal past x1480 is the wall-kick shaft / other route.
