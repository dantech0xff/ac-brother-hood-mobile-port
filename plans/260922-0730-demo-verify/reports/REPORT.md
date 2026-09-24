---
title: Golden-path verification — devin/land (10a200ec → eb6516f5)
phase: demo-verify
status: slice-222-223-render-regression-PASS-identical-output
build: rewrite/android-debug.apk @ 10a200ec (slice 204, TEMP fix) · eb6516f5 (slice 208, stock) · 9ad6b723 (slice 210, stock) · 9584967a (slice 213, stock) · f9b486d7 (slice 214, stock) · 31289deb (slice 222+223, stock)
device: emulator-5554 (AVD `spike`, API 36, swiftshader_indirect, 2400×1080 landscape, scale=4 offset=(400,60))
date: 2026-09-24
---

# Run 2 — devin/land @ eb6516f5 (slice 207 boot fix + slice 208 g.a unification) — STOCK build

Boot is clean now — `level0: 637 records, 627x55 cells, world=12540x1100px,
npcs=601`, no FATAL. The slice-207 tileset-path fix works on stock; no
workaround needed. All behavior below is **unpatched**.

## Verified working (touch + jdb-verified)

- ✅ Boot → jC23 → left-footer → jC18 title → strip → jC2 menu → row0 LEVEL →
  jC9 loading card ("ROME/COLOSSEUM/KILL WOLFMEN") → jC21 u9 dialog → SKIP
  strip (display ~1908,944) → jC8 play. All on-screen.
- ✅ Traversal east by touch: run-right holds (cell5, display 780,780) carry
  the player x85→1379; camera tracks both axes.
- ✅ **Crate/ledge climbing works (slice-208 g.a verified):** from the shaft
  floor (y959) a jump makes the player grab the ledge/crate — `player.g` binds
  an Entity, S=37 hang, then a right-hold shimmies him EAST along the '5'
  platform edge x1682→2075, and a jump mantles him onto a beam at
  (2149–2169,906). The crates at (1607,793)/(1620,795) are the grab anchors.
- ✅ **Guard combat verified:** ax11 npcs[104] at (2187,964) stays aA=0 while
  the player is on the beam (y906); once the player drops onto the same floor
  (y959, x2163) the guard alerts aA=0→1, strikes S=12, staggers the player
  S=9, drains x1 30→0 in ~2 s → fail banner. Guard STAYS armed (aA=1, S=12)
  after reload — teleports next to it = instant KO.
- ✅ ax44 gap-poles are grabbable: teleporting to (2383,640) bound `player.g`
  to Entity@d791562 — the pole/swing prop engages g.a. (Fail still fired —
  camY kill-line artifact of the teleport, not the grab.)
- ✅ Fail→reload loop ×4: jC12 banner → row-tap (display ~1200,580) → respawn
  at checkpoint. Checkpoint in the shaft = (1606,959); x1 respawns at 30.
- ✅ ax14 not a contact-hazard confirmed: the player stood at x2163–2169 on
  the floor alive (ax14s sit at 2180/2322/2343/2426) — the earlier x2147
  insta-fail was the pit kill-line, matching the corrected semantics.

## Where traversal still breaks (touch-only east route)

1. 🔴 **x2200–2340 wall block — no ground passage.** Corridor floor at y960
   dead-ends at a solid wall face x2200 (cols110–117, solid r40–52, no door
   cells). The '5' platform ends at x2100 (y800); a run+jump toward the wall
   top (also y800) covers ~70 px then drops the player onto the floor at
   x2169 — the ~100 px gap + 30 px shortfall can't be crossed at that height,
   and the wall face did not bind a climb (g=null on contact).
2. 🔴 **Beam perch at (2149–2169,906) is a soft-lock pocket** — S=89 with all
   dpad directions, jump, attack, and down-hold producing zero movement;
   only west input escapes (drops back to shaft floor). The dormant-guard's
   floor is 54 px below but nothing drops the player off the beam tip.
3. ⚠️ **Upper route is real but teleport-inconclusive.** The shaft continues
   up: balcony floor at y580 (x1480–1700) with a gap at x1720–1820 directly
   over the '5' corridor; ax5 zone at (1385,349)-(1505,553), ax10 trigger at
   (2176,468), ax11@2213 at y482, ax44 crossing poles at x2383/2432/2479
   y657 — all upper-level infrastructure. Every ~200 px vertical teleport
   dies on the `al > camY+240` kill-line before the camera can lerp down, so
   these were verified structurally (collision map + live entity fields) but
   not traversed. Wall-jump chaining in the shaft (x1580–1840 interior is
   open y800→y580 through the balcony gap) is the likely intended climb —
   needs a touch-driven attempt.
4. ⚠️ Guards remain dormant when approached from above (aA=0 on the beam at
   40 px horizontal distance); engagement requires same-floor proximity —
   consistent with a floor-gated alert, not necessarily a missing trigger.

## Artifacts (this run)

- `eb-take1.mp4` (180 s) — boot → sound prompt → title → menu → loading card
  → u9 dialog → SKIP → play → east run to the pocket/wall
- `eb-take2.mp4` (~210 s) — shaft climb → '5' platform shimmy → beam →
  corridor floor → guard alert/strike/KO → fail/reload → east-probes
- `eb-take3.mp4` (120 s) — clean climb sequence (grab→shimmy→mantle→beam)
  → teleport-assisted floor fight KO → row-tap reload
- PNGs: `eb-run` (intro w/ "MY BROTHER, LET'S MAKE THESE WOLVES HOWL!" +
  wolfman + SKIP), `eb-play` (in-game spawn), `eb-load` (loading card),
  `eb-wall` (pinned at x1379 pocket edge), `eb-shaft`/`eb-shaft2` (shaft
  floor + hang), `eb-climb` (S=37 ledge hang under '5' platform),
  `eb-beam`/`eb-corridor`/`eb-zoom` (beam walk + dormant guard below),
  `eb-floor`/`eb-fight`/`eb-fight2` (guard engage→KO), `eb-fail`/`eb-gko`
  (fail banners), `eb-pole` (ax44 grab attempt frame)

## Collision/route map (verified via `level.collisionCell(c,r)`, 20 px cells)

```
x1400–1580 (cols70-78): solid r38–47  — the ~200 px tower/building
x1580–1900 (cols79-95): r40 = '5' one-way platform (y800); r41–47 open
                        (shaft interior); r48+ solid (shaft floor y960)
x2000–2100:             '5' platform at y800 continues
x2200–2340 (cols110-117): solid r40–52 — wall/fill block, top y800
x2360–2540 (cols118-127): open r40–54 → full-height VOID gap
                        (ax44 poles at x2383/2432/2479, y657)
x2560+ (cols128+):      solid r40–41 only (y800–820 ceiling lip)
Balcony: x1480–1700 floor at y580–600 (r29–30), gap at x1720–1820;
         tower x1400–1460 rises to y360; east wall x1840+ r17–30.
```

---

# Run 1 — devin/land @ 10a200ec (slice 204) — TEMP-patched boot

## Headline finding — BOOT CRASH on HEAD (slice-177 regression)

Stock HEAD **fails to boot into level-0** — `Level0Game.kt:129-133` tileset-clip
loop has two bugs introduced by `8e67cb29` (slice 177; not present at `8ccca05`):

```kotlin
for (ts in level.layers.map { it.tilesetClip }.toSet()) {
    clips[-ts] = Clip.load(
        Gdx.files.internal(
            "level${firstPackTilesetDir}/tileset-$ts/clip.acpk")  // firstPackTilesetDir="level0" → "levellevel0/..."
            .readBytes())
}
```

1. `firstPackTilesetDir` already equals `"level0"` (line 31) → path is doubled.
2. Layer-0 (`et`, `tileset:null` in `generated/level0/meta.json`) yields
   `tilesetClip=0` → requests `level0/tileset-0/clip.acpk` which does not exist
   (only `tileset-10/11/12/` are generated). `GdxRuntimeException` → FATAL.

TEMP workaround used for this run (REVERTED after — tree is clean):
`"${firstPackTilesetDir}/tileset-$ts/clip.acpk"` and `toSet() - 0`.

Everything below was verified on the TEMP-fixed build — i.e. it validates
post-boot behavior on HEAD but required the workaround to boot.

## Verified working (touch-driven)

- ✅ Cold boot → jC23 sound prompt (left-footer) → jC18 title → jC2 menu →
  row0 "LEVEL" → jC9 loading card ("ROME / COLOSSEUM / KILL WOLFMEN") →
  jC21 u9 intro dialog → **SKIP strip (display ~1908,944) dismisses → jC8 play**.
  Slice-165 SKIP fix re-verified — 1–2 taps.
- ✅ Traversal east by touch: mounted-pad cell5 (view 95,180 → display 780,780)
  runs the player right; camera tracks horizontally and vertically.
- ✅ Combat machinery real: an ARMED guard pair (aA=1) alerted on approach,
  chased, wound up S23 → struck S12 → KO'd the player (gp4.mp4). `player.g`
  bound Entity during the fight (interact-lock CAN engage).
- ✅ Fail loop ×5: pit-falls/hazards/combat KO → jC12 "DO YOU WANT TO RESTART?"
  → two-stage row-tap (display ~1200,580) → respawn at checkpoint
  (jC=8, ak=1600, x1=30 post-reload — note x1 respawns at 30, not 90).
- ✅ ~1 frame per 62 ms tick (~16 unique fps) — the slice-170 atlas fix holds;
  gameplay is visibly smooth in the recordings.

## Hard blockers found on the golden path (with jdb evidence)

1. 🔴 **Boot crash (above)** — gates everything on stock HEAD.
2. 🔴 **x≈1379–1400: ~100 px sealed wall.** Collision cols 68–79 rows 38–42 are
   solid (20) flush on the ledge at rows 43–44, plus a `5` one-way strip at
   row 40 overlapping the east edge. Vaults (cell2/cell1) bounce the player
   backward; jumps dead. Touch-only traversal ends here.
   (gp2.mp4 shows the struggle at this wall.)
3. 🔴 **x2147–2180: ax14 hazard zone** — contact → `world.failed=true`
   (verified: jC12, x1=0, deaths incremented). Teleport-assisted crossing at
   (1650,890) still died on it during the east run (gp5.mp4).
4. 🔴 **x7683: ax4 hazard-crates** (entities at 7688,883–910, r8[5]=9→i=2) —
   block the street corridor.
5. 🔴 **ax4 crates never bind `p.g` by touch** — `interactScan` skips ax4/ax58
   unless `p.gI==8` (gI=1 in normal play) → `interactAction` runs with g=null →
   `setAnim(0)`. Sword smashes on crates unreachable; `damageIntake73` combat
   damage additionally requires `p.gI∈{1,2} && p.S∈ATTACK_ANIMS` + overlap.
6. ⚠️ **Guards dormant unless a trigger arms them** — the street pairs
   (ax11 at 6916/6980/7361/7401/7470, npc116 at 7702) sit `aA=0` and never
   alert on approach alone; only the trigger-zone pair went live.
7. ⚠️ **No player-kill landed via touch** — consistent with every prior run:
   engage-lock (`g`) bound once but the weaken→finisher chain didn't complete
   within the ~2 s KO window (soldier strikes are very fast).

## Artifacts

Device-recorded via `adb screenrecord` (framebuffer only — no overlay possible):

| file | contents |
|---|---|
| `gp1.mp4` (170 s) | boot → sound prompt → title → menu rows → mission browser |
| `gp2.mp4` (164 s) | level-0 play start → east run → x1379 wall struggle |
| `gp3.mp4` (169 s) | street corridor → combat KO → fail prompt |
| `gp4.mp4` (167 s) | armed-guard chase/strike/KO at ledge → fail → reload |
| `gp5.mp4` (142 s) | teleport 1650,890 → east street run → x2147 hazard KO → jC12 → row-tap reload → respawn (ak=1600, x1=30) |
| `demo-highlight.mp4` (140 s) | gp1 menu→loading→play + gp5 run→KO→reload |

Screenshots: `/home/ubuntu/screenshots/gp5-streetrun.png`,
`gp4-fight.png` (fail prompt over fight scene), `gp1-play.png` (menu rows),
`dh-frame.png` (fail prompt in highlight).

## jdb state evidence

- Fail/reload verified: `this.world.jC = 8`, `player.ak = 1600`,
  `player.x1 = 30` after row-tap reload (was jC=12, x1=0).
- Touch decode verified live: `resolvePadZone` mounted branch; dpad rect
  `insideRect(-5,124,116,116)`, radials `(305,200,r35)→cell4→padE(32)`,
  `(355,145,r35)→cell1→padE(4)`; taps outside → −1.
- Menu row-tap: `menuRowAt` requires DOWN+UP in same tick's event list
  (`pressY` from `events.firstOrNull{DOWN}?.y`, hit-test on `lastTouchX/Y`
  from the UP) — fast taps required.

## Notes for next run

- Re-check boot on the NEXT HEAD — fix for the slice-177 regression
  (path should be `"${firstPackTilesetDir}/tileset-$ts/..."` and skip
  `tilesetClip==0` or guard `fileHandle.exists()`).
- A real east-end playthrough needs: a way past the x1379 wall (bigger jump
  arc? alternate route overhead?), hazard-zone timing at x2147, and trigger
  zones to arm the street guards. The x7683 hazard-crates may be passable
  once `p.g` binds (gI==8 state needed).
- `adb shell screenrecord --time-limit` did NOT always fire (~142–170 s takes
  overshot; one needed a manual `kill -2` to finalize the moov atom — verify
  the file after pull with `ffprobe` before extracting frames).

---

# Run 3 — devin/land @ 9ad6b723 (slice 210) — shaft/kick traversal attempt — STOCK build

Focused session: reach east past x2340 via the shaft wall-kick chain (x1820 face)
or the balcony route, per lead intel. Findings below are all **real-input play**
(jdb used only for reads + explicit position assists noted as harness actions).

## Headline blocker — '5' one-way platforms cannot be mounted from below

Every approach to a climb face from corridor level is pre-empted by '5'/'05'
ledge lips, and **no input combination mounts a '5' top from a hang**:

- Pure vertical jump under '5' (x1600, aO=0 overhead): rises to y810 → auto
  ledge-hang S37 on the lip — **no pass-through**; the lip always catches first.
- UP-vault from the hang (u(16388), cell1 press): refused at every x tried
  (1594, 1657, 1775, 1805, 2098) — probe reads **aO=5** (the one-way cell above
  the lip counts as overhead-blocked; aO reads cell-type: 20 seen at x1565).
- Shimmy works along lips (RIGHT cell bit6 — up-right bit3 does NOT shimmy),
  x1594→1829, but lip corners do NOT roll the player onto the top.
- Drop-from-hang (u33024 via cell7 live sliver x≥60,y≥207 — view x≤36,y≥207 is
  a soft-key dead zone) → returns to corridor floor.
- Result: corridor y960 under '5' is a one-way trap — the designed route
  (strip top → face kicks → roofs) is unreachable once you're below. The
  intended entry is from ABOVE (balcony drop / ax2 balcony checkpoint), which
  itself is gated behind the climb. **Whether original '5' allows
  jump-through/mount is a provenance question — this build treats the lip as
  hang-only.**

## Kick-face attempts (zone-2 hold verified arming aF=1)

- Floor jump + east hold at x1820 region → '5' lip catch (x1837,810) S37,
  cv=false — never touched the face (face bottom ~y820–840 sits below lip
  catch height ~y810... the lip line is hit first).
- East-face approach at B (x2120): jump from floor x2071 → '05' lip catch
  x2098,y810 S37 — same pre-emption.
- The x1740–1820 shaft interior above '5' cannot be entered from below
  ('5' spans the shaft bottom — mounts blocked as above).

## Lethal corridor patrol (blocks floor staging)

ax11 npcs[106] @2172,964 patrols x2142–2180 AND chases ~400px west (KO'd the
player at x2179 AND x1760). At checkpoint-health x1=30 the player dies in ~2s
of contact → any floor approach needs the guard dead or moved. For this run I
used a **harness assist**: jdb `set npcs.get(106).ak=2600` (guard alive,
patrolling, out of chase range) — noted, not gameplay.

## Environment/harness finding — jdb can leave threads suspended → ANR

A jdb session that quits without a completed `resume` leaves ART threads
suspended → main thread stalls in `dispatchVsync→CheckJNI WaitHoldingLocks`
→ **ANR dialog steals focus and eats ALL input** (this session's earlier
"dead input" episodes were this, not touch bugs). Trace saved
`/tmp/anr.txt` (Input dispatching timed out, main Native in CheckJNI wait).
Recovery: attach jdb + `resume`, or force-stop+relaunch. ANRs also appear at
boot under load without any debugger — always `dumpsys window|grep
mCurrentFocus` before interpreting a dead tap.

## Other evidence captured this run

- Pit exists in the corridor floor west of the crates (~x1585): tp to
  (1590,959) fell to y1029 → kill-line → **jC=2 menu** (full mission reset,
  not checkpoint reload — pit falls lose more progress than guard KOs).
- Teleport while in hang states (S37/38) skips one-way catches → falls
  through '5' to kill-line. Teleports to mid-air lips → S38 grabs.
- Run latches: any ≥120 ms directional hold commits ~100–580 px — precise
  floor positioning by touch is not possible; use jdb `set ak` for staging.
- Guard relocate script: /tmp/npcmv.sh (raw `set`, not `eval set`).
- Carrier mount (prior session): real drop into [1734,600,1770,679] → S315→
  S318 climb-out works; the S318 release needs padHeld(33024) — untested
  post-ANR this session.

## Recording / artifacts (this dir)

- `k3-shaft-block.mp4` (180 s, 1600×720 device screenrecord, clean — no ANR
  overlays in its window): corridor combat KOs, crate/lip hangs, shimmying.
- `k2-anr.mp4` (173 s) — earlier take containing the ANR dialog episode
  (diagnostic only, do NOT ship as gameplay evidence).
- Frames/stills: `k3-f60.png`, `k3-f120.png` (corridor guard fight),
  `k3-f170.png`; `k-hang1775.png`, `k-hang1657.png` (lip hangs with platform
  guard overhead), `k-shaft-floor.png`, `k-pit-fail.png`, `k-anr.png`.

---

# Run 5 — devin/land @ 9584967a (slice 213 ax22-W fix) — STOCK build

Re-verify of the ax22 hopscotch route after the W-rect fix (I() preamble +
L1f35 tail now run for claimed-proc entities; initAx22 ports the Le87 Z-fill).
Mixed real input + documented jdb staging assists (position sets only — all
captures, vaults, climbs, combat observed are real mechanics).

## RESULT: ax22 zones now capture AND vault — every zone tested works

Zone index map for THIS launch (indices shift per launch):
- npcs[338]=(1214,636), [339]=(1316,568) — wall-hop chain
- npcs[335]=(2064,695), [336]=(1975,605), [337]=(2104,546) — B-lift zigzag
- npcs[340-344]=(7628,640),(7518,573),(9874,514),(10298,516),(10414,482)

Verified LIVE this session:

| zone | capture | vault exit | result |
|---|---|---|---|
| 338 (1214,636) | ✅ S65 snap | mask8 hold fires | vault arc → intercepted by ax7 (see below) |
| 335 (2064,695) | ✅ S65 snap | up-LEFT edge (westward hop) | → caught by 336 |
| 336 (1975,605) | ✅ S65 (landed 1982,605) | up-RIGHT edge | → caught by 337 |
| 337 (2104,546) | ✅ S65 snap | fresh up-RIGHT edge | → **lands roof-B top (2200,479), edge grab S203** |

- W rects now populated, e.g. npcs[344] (far zone) W=[10398,469,10432,502].
- S65 captured-pose snaps exactly to the zone anchor; directional hold picks
  the exit; the vault needs a FRESH directional EDGE (`pad.v`, not `u`) —
  a hold continued from the previous zone does not fire.
- After the third vault the player auto-grabs the roof edge (S203 ledge-hang);
  a fresh directional edge climbs up onto the roof top. Standing S0 at
  (2210,479) — **on building B's roof**.

## East traversal progress — x1379 → x2940

- Ran east along roof B top → passed x2340 (the old dead-end wall) →
  KO'd at **x2940** by the 3-guard pack (ax11 @ x2773/2798/2825), S=50 crush/
  hurt state, x1=0 → fail prompt.
- **Checkpoint ax2 (2594,485) ARMED + verified**: the KO respawned him at
  (2580,519) — NOT a full mission restart. Real checkpoint behavior.
- Farthest point reached: **x2940** (prior best x1379; the whole roof route
  x2210→2940 was continuous real input with no teleports).

## NEW BLOCKER — claimed-proc entities never tick their anim clocks

The wall-hop chain's designed mid-step is broken:

- The vault from zone-338 (1214,636) arcs NE; its apex passes through ax7
  npcs[84] at (1397,506), W=[1318,456,1334,472] — **the ax7 mouth-plant is
  placed exactly at the vault apex**. It swallows the player mid-arc
  (W∩playerW → `p.S=313`, `P|=64`, center-snap) — the DESIGNED catch: on
  release it would throw him `ag=+2048` east onto the wall top
  (x1400–1450, y~400–500).
- **The release never fires**: tickAx7's S1 branch waits `e.animFinished()`,
  but the entity's anim clock is frozen — npcs[84] sits S=1, T=0, U=0, V=0,
  P=0 forever. Verified permanent: player pinned at (1326,464) S=313
  indefinitely (screenshot r5-ax7-swallow.png shows him held in the plant's
  mouth).
- Same freeze on ax46 spring-traps: npcs[85] S=327 T=0, npcs[86] S=328 T=0
  (S328 = post-hit hold waiting `animFinished` → eternal S330 player pin).
- Guards (ax11) tick normally (T=2 observed) — the freeze is specific to
  claimed-proc types.

**Root cause (source-verified):** the original `i.I()` preamble runs
`s()` (anim advance) at i.java:3872-3874 for **every** entity — before the
ax-dispatch — gated only by `!cu && S>=0 && slow-mo`, inside the
`!k.al || aa==z[12]` block (which also does the `y` countdown + `m()` call).
In the port, `Level0World.tickNpc` (Level0World.kt:4642-4694) runs only
`n.b=true` + the dispatch + `if (claimed) defaultArm` — and `defaultArm`
(NpcFsm.kt:869-873) is just refreshBoxes+facing+push, **no `advanceAnim`**.
The soldier family is unaffected because its `tick()` tail calls
`e.advanceAnim()` at NpcFsm.kt:299-302. So every claimed proc that gates
behavior on `animFinished()`/`r()` can deadlock: ax7 release, ax46 release,
and likely others (ax13, ax44, ax66 ride states worth auditing).
Also missing from the preamble port: the `y` byte countdown and the `m()`
call (i.java:3875-3877) — worth checking their roles too.

## Harness notes this run

- **`adb input` injection can silently die after an ANR** — the input
  dispatcher blocks injected events to the app while real hardware-path
  input still works. Symptom: `kCj/kCk=-1`, `lastTouchX/Y=-1` during holds.
  Recovery/workaround: real mouse clicks via the emulator window (window-
  calibrated: wx=110+vx·1.087, wy=33+vy·1.102 for the shrunken window).
- jC=14 pause menu = LEVEL-row variant; RESUME is a menu item; poking
  `set jC=8` via jdb is a clean harness exit.
- Teleporting into solid geometry wedges the player into S79 (forced by
  groundedTail's `aO>12 && aR>12` arm — head+below-feet both embedded) —
  teleports must target open air above real floor.
- KO → "DO YOU WANT TO RESTART?" → row tap → **respawns at the last armed
  checkpoint** (verified x2580 after KO at 2940); a FULL mission restart
  only happens from menu paths.

## Recording / artifacts (this dir)

- `k9-blift-chain-full.mp4` (170 s) — **the money take**: strip staging →
  jump→335 capture (S65) → west vault → 336 → east vault → 337 → fresh
  edge → roof-B edge grab S203 → climb → roof east run → KO at x2940.
- `k7-ax22-verify.mp4` (180 s): the wall-hop verify sequence —
  teleport→S65 capture at zone-338, mask8 vault, ax7 swallow freeze.
- `k8-blift-roof-run.mp4` (118 s): checkpoint respawn x2580 → east run →
  x2940 KO.
- `k6-real-input-run.mp4` (180 s): clean real-input boot→menus→SKIP→run
  east→pot vault→block→slab→x1379 (this build, for comparison).
- Stills: `r5-ax7-swallow.png` (S313 pin in the plant's mouth),
  `r5-roof-b-landed.png` (roof-B landing (2200,479)),
  `r5-ko-2940.png` (KO at the guard pack), `r5-strip-top.png`,
  `r5-strip-scene.png`, `k7-f*.png`, `k8-f*.png`.

---

# Run 4 — devin/land @ 9ad6b723 (slice 210) — ax22 hopscotch capture zones — STOCK build

Lead decode: the designed east route uses ax22 capture zones ("hopscotch" lifts):
slab y860 near x1200 → zone (1214,636) → zone (1316,568) → wall top → drop onto
'5' at x1580 → strip east → zones (2064,695)/(1975,605)/(2104,546) → B roof →
'02' ledge/poles → checkpoint (2594,485). Confirmed-faithful context: '5' from-
below mount refusal, corridor trap, single faces not kick-climbable.

## RESULT: ax22 zones are dead in this build — W/X/Y rects load as [0,0,0,0]

- All 10 ax22 zone entities found at `npcs[344..353]`:
  - 347=(1214,636), 348=(1316,568) — wall-hop chain
  - 344=(2064,695), 345=(1975,605), 346=(2104,546) — B-face lift chain
  - 349=(7628,640), 350=(7518,573) — mid-level chain
  - 351=(9874,514), 352=(10298,516), 353=(10414,482) — far chain
- **Every one has `W=[0,0,0,0]`, `X=[0,0,0,0]`, `Y=[0,0,0,0]`** — the overlap
  rect is never populated (checked live twice, including while the player was
  inside the nominal zone). Z=[1,10,-1,-1,1,…] params load fine; `v=true`.
- **Contrast proof the loader CAN populate W:** ax10 carrier `npcs[151]` has
  `W=[1734,600,1770,679]` — exactly the verified-working carrier zone
  (it captured the player into S315→S318 in the prior session). So the
  record→W path works for ax10 but produces zeros for ax22.
- No capture under any entry: teleport drops at (1240,660), (1214,700),
  and **exactly on the anchor (1214,636)** — all fall through to floor y859,
  player stays S=0/11, `gk=-1`, no S65 snap, no `gd` bind. A real jump+east
  hold from the slab runs past the wall to x1388 — nothing.
- Anchor-proximity capture also ruled out: passing within ~0–64 px of the
  anchor for ≥1 tick never fires.

## Verify-target answers

- (a) Capture zones do NOT fire in real play — rect data missing (W=0).
- (b) Vault exit — untestable (capture never engages).
- (c) Checkpoint (2594,485) — unreachable; the whole east route needs the lifts.
- **This is a real port bug, not a missing-mechanic decode:** entities exist
  at the right positions with params, but their interaction rect is empty.

## Recording / artifacts (this dir)

- `k4-hopscotch-dead.mp4` (172 s, 1600×720): slab positioning, real jump at the
  zone wall, teleport drop-throughs at the anchor.
- Frames/stills: `k4-f30/90/150.png`, `r4-zone-drop.png` (player standing under
  the dead zone at x1214), `r4-slab-wedged.png`.

# Run 6 — devin/land @ f9b486d7 (slice 214 — I() preamble port) — STOCK build

Device `emulator-5554`, stock `android-debug.apk` built + installed from HEAD
(HEAD verified f9b486d7 via `git log`). Real `adb input` touch restored on the
fresh process (pid 20172). Recording `k10-ax7-release.mp4` (169 s, device
screenrecord 2400×1080 — no overlays captured).

## Verify targets — all three PASS

1. **ax7 capture works.** Teleport-staged the player into zone-338 anchor
   (1214,620→636) → S65 capture. mask8 (up-right cell-2 hold, display 760,632)
   → vault → mid-arc the ax7 (`npcs[84]`, W=[1318,456,1334,472]) swallows:
   `e.S=1`, `p.S=313`, `p.P|=64`, center-snap (1326,464). Same as before.
2. **Release + throw FIRES now — no freeze.** The S1 anim ticks under the new
   preamble (`e.T` advancing), `animFinished()` trips, `e.setAnim(0)` (observed
   `e.S=0`, `e.T=2` post-release), `p.flingAirborne(0)` + `p.ag=+2048` — the
   player is thrown east. Deterministically (2 attempts): release → arc →
   **lands at (1470,499)** — ~144 px east of the mouth, past the wall face
   x1440. Pre-slice-214 this froze eternally at S1/S313; now the whole
   capture→swallow→anim→release→fling chain completes.
3. **ax46 regression — clean.** `npcs[85]`/`[86]` (S=327) show `T` and `U`
   advancing across reads (T=0→2, U=2 over ~3 s) — cycling anims under the
   preamble `s()`. No freeze mid-cycle.

## New observation — deterministic landing wedge at (1470,499)

- The throw lands the player embedded in the wall's top-east corner: box
  [1460,461,1482,500], `aO=20` (head cell solid) + `aR=20` (below-feet solid)
  → `groundedTail`'s else-arm forces **S=79** every tick — input-immune wedge
  (jump/direction taps consumed, `eL=0`, no movement).
- Geometry map: solid mass cols 70–73 (x1400–1479) rows 22–25 — top surface at
  ~y440; open air cols 74+ (x1480+) rows 22–26 with a lower structure at row 30
  (y600+). The player lands ~40 px below the wall top, straddling the wall's
  last solid column — right 2 px in open col74.
- `bd=false` — the embedded-resolve arm doesn't fire, so nothing ejects him.
- `flingAirborne(0)` is proven-faithful (g.java:126 — `ah=r5=0`, +10 px down,
  `aj=1536`) and `ag=±2048` is proven (i.java ax7 proc) — so the trajectory is
  faithful; the wedge is a downstream landing/resolve question:
  either the original embeds identically (faithful) or its resolve ejects the
  player onto the wall top / into the x1480+ gap to continue the block-top
  route east. **Flagged as the next provenance check** — the chain still can't
  continue past this wedge by real input.
- Both attempts land at the exact same pixel — deterministic.

## Recording / artifacts (this dir)

- `k10-ax7-release.mp4` (169 s): teleport→zone-338 capture (player held in
  S65 pose at anchor ~t=55–87 s) → vault (~t=88) → swallow + release + throw
  (~t=88–89) → landing wedge (t≥89 through end; second deterministic attempt
  re-teleport+re-vault near end).
- Frames: `k10-f87-5.png` (zone-held, pre-vault), `k10-f89-5.png`/`k10-f91.png`
  (post-throw wedge crouch at the wall/beam east lip), `k10-f55.png`/`k10-f80.png`
  (staging), `r6-wedge-1470.png` (screencap of the S79 wedge state).

# Run 7 — devin/land @ 31289deb (slice 222 alloc-kill + slice 223 FBO-removal) — render regression, STOCK build

Device `emulator-5554`, fresh APK built 20:02 + installed 20:09, new pid 27953.
`adb input` live (fresh process). Recordings: `r7-boot-menus.mp4` (~15 s,
truncated) + `r8-render-regression.mp4` (119.5 s: gameplay→pause→resume).

## RESULT — direct letterbox render path produces identical output, no regressions found

- **Boot→menus→level-select→briefing all render** (jC23 carousel, jC18 title
  art — full AC Brotherhood splash, jC2 level rows, jC9 "ROME / A.D. 1486 /
  KILL WOLFMEN" briefing card). Landscape, right-side-up, correct colors,
  letterboxed.
- **Intro dialog bottom panel (scissor check) — PASS** (jC21): text clipped
  cleanly inside the panel bounds above the separator line, portrait sprite +
  "TOUCH THE SCREEN" render correctly.
- **Gameplay scene — PASS** (jC8): player sprite at spawn, Rome rooftop tiles,
  props (green cage-cylinder, door, golden pot), wisp flames — identical to
  pre-223 output; NOT black/upside-down/offset/clipped.
- **HUD — PASS**: hooded portrait, sync meter, score counter ("4/100"), blue
  pause button, D-pad + action radials all drawn.
- **Pause menu (scrollable-list scissor check) — PASS** (jC14): LEVEL 1-6 row
  list renders over gameplay, clipped correctly at the gray header/footer
  bars (top row and LEVEL 6 partially clipped at bounds). Blue back-arrow →
  jC=8 resume works.
- **Camera scroll — PASS**: real right-hold run ak 85→499, camX 21→367 —
  scene + HUD track correctly while scrolled.
- **No crash/ANR** during the whole session; s1/s2 screencaps differ
  (screen live throughout).
- **FPS ≈ baseline**: game tick counter `jG` advanced 279 ticks over a ~18 s
  jdb-free window ≈ **15.5 ticks/s** (±0.5) — matches the ~16 fps (62 ms tick)
  baseline. gfxinfo/SurfaceFlinger latency unavailable on this emulator for a
  GL SurfaceView — jG delta is the same measure the baseline used.

## Recording / artifacts (this dir)

- `r8-render-regression.mp4` (119.5 s) — gameplay run, pause-menu open/scroll
  attempt, resume.
- `r7-boot-menus.mp4` (~15 s) — boot→title→level-select (truncated take).
- Stills: `r7-title.png`, `r7-briefing.png`, `r7-dialog-panel.png`,
  `r7-gameplay.png`, `r7-gameplay-scrolled.png`, `r7-pause-list.png`.
