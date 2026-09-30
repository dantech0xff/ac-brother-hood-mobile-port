---
title: Golden-path verification — devin/land (10a200ec → eb6516f5)
phase: demo-verify
status: 46329a09-all-8-missions-load-render-no-crash-m5-spawn-wedge
build: rewrite/android-debug.apk @ 10a200ec (slice 204, TEMP fix) · eb6516f5 (slice 208) · 9ad6b723 (210) · 9584967a (213) · f9b486d7 (214) · 31289deb (222+223) · 8b2559cd (217-233) · 290d62c2 (through 244) — all stock
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

## Run-8 — 2026-09-24 ~21:55–22:40 · golden-path re-verify @ 8b2559cd (slices 217–233)

Stock `android-debug.apk` from devin/land @ 8b2559cd, emulator-5554,
pid 17522. Focus: full-chain regression + the newly-live ledge family
arms (S60 auto-mantle / S61 hang / S257 vault-drop).

### Method
Real `adb input` taps/swipes for all gameplay; jdb for reads + explicitly
flagged `set ak/al` staging teleports. Three `screenrecord` takes
(g1/g2/g3 — device-side, no overlays). ~40 min of live traversal.

### Results — per handoff item

1. **Boot → menu → NEW GAME → briefing → intro dialog → SKIP → gameplay
   — PASS.** jC 23→18→2→9→21→8 with the mapped taps; no overlay covers
   the game at any point.
2. **Run/vault east traversal + camera — PASS.** Spawn run x85→499+ on
   real input; camera scrolls continuously (camX tracks). Assisted
   passes covered the corridor x~1600→2179 at corridor and upper levels.
3. **Ledge arms — no misfires observed; positive grab NOT produced.**
   - Across ~40 min of varied traversal — running/jumping past platform
     edges, falling past the '5' strip at multiple x, bouncing off walls
     (S33 rebounds), dropping off the strip's east/west lips — **zero
     unintended grabs**. The tight geometry gate (lip cell ≥19 +
     open-side pocket requirements, Entity.kt:1605-1643) explains it:
     level0's reachable walls are thick/tall, and '5'=5 fails the ≥19
     lip check so '5' edges correctly stay clean.
   - Positive S60/S61/S257 could not be produced interactively: the
     arms need an exact thin-lip cell (the unit tests compute the spot
     by scanning the 627×55 map — e.g. Slice1Test.kt:10616's
     open-side-wall-top scan and :10679's thin-platform-edge scan).
     Not reachable by reasonable probing; `jtp5` teleports into solid
     geometry wedge the player (S79). DOWN-at-edge taps at the '5'
     strip east lip produced walk-off drops (landing x2141,y959),
     S257 not confirmed in S-reads (drop is ~8 frames; reads race).
   - Unit coverage: slice-231/233 tests assert S60→S62 mantle settle
     and DOWN→S257 on real level0 geometry — green per the slices.
4. **Combat + fail → banner → tap-reload — PASS.** Corridor/upper-route
   guards alert+chase+strike for real (visible slash VFX); player KO'd
   twice (x1→0 → jC=12 "DO YOU WANT TO RESTART?"); YES-tap reloads to
   the '5'-strip checkpoint (1593,799, x1=30). Earlier in the run the
   strip guard also KO'd at x1608.
5. **Recording — PASS.** Three device `screenrecord` takes, no system
   dialogs/ANRs this run.

### Regressions vs Run-7 (31289deb)
None found — rendering, HUD, scissor panels, camera, input, fps cadence
(~15.5 ticks/s earlier baseline) all unchanged.

### New-arms verdict
The risky direction (grabbing when it shouldn't) is clean in play.
The positive direction (grabbing at a real thin lip) is unit-verified
but remains unconfirmed on-device — the designed lips aren't in the
spontaneously-reachable play space near the east-traversal route.
Follow-up: to fire S60/S61 interactively, teleport to the coordinates
the slice tests compute (map-scan results) rather than guessing cells —
or share those coords.

### Artifacts
- `g1-menus-traversal.mp4` (171 s) — boot chain + spawn-area traversal.
- `g2-ledge-tests.mp4` (170 s) — assisted corridor/strip-edge tests;
  NOTE ~30 s of jdb-frozen stills inside (eval suspends the game —
  screen repeats the last frame while halted).
- `g3-traversal-combat.mp4` (170 s) — run east, upper-route traversal,
  corridor-guard fight + KO + restart banner + reload.
- Stills: `g1-spawn.png` (spawn scene), `g1-run-x499.png` (real-input
  run mid-scroll), `g1-strip.png` (standing on '5'), `g3-guard-fight.png`
  (guard strike VFX mid-combat), `g3-ko.png` (restart banner),
  `g2-s257-t71.png` (player at '5' east lip pre-drop),
  `g2-t80.png`, `g3-f40.png` (mid-traversal).

## Run-9 — 2026-09-24 ~22:55–23:35 · ledge-arm positive-fire verification @ 8b2559cd

Follow-up to close the Run-8 gap — the lead supplied the unit tests' exact
scan coords: thin lip at cell **(200,9)** (wall face x4000, lip-top y180,
open pocket col199 rows 8-11) and thin platform edge **(489,10)**
(x9780-9899, row10 solid / row11 open, WEST edge at col489).

### Verified live on-device

- **S61 hang — PASS.** Falling teleport (3965,180)+camY=200 → live
  `ledgeHangGrab` fired: `S=61`, snapped to `(4000,179)` (= wy*20-1),
  `aC` grace counting (40→3). Auto-released to S43 when the grace lapsed.
- **S60/S62 mantle chain — PASS.** In a jdb per-tick consumer trace
  (`stop at PlayerFsm:2059`), the hang→mount chain ran end-to-end:
  S61 → S62 climb → **settles `S=0, aZ` standing at (4010,179) on the
  wall top** — exact predicted lip position.
- **S257 vault-drop — PASS (displacement signature).** Standing at
  (9790,199) facing WEST (the platform's west edge — av=true), DOWN tap
  → dropped ~40px west + ~108px down (9750,307) — ledgeDropArm's exit
  offsets — then NPC-pinned `S=89` by a grabber below (real entity
  interaction). S257's anim raced past the read latency; the displacement
  + drop destination match the arm's semantics. Facing EAST at the same
  spot does NOT vault (east is mid-platform — edge probe correctly fails).
- **No misfires — PASS.** Every clean fall/edge in Run-8 stayed clean;
  here too the arm fired only at the genuine thin lip.

### Why spontaneous catches are rare (harness findings, not bugs)

- **camY kill-line**: the camera's y sets an out-of-bounds plane —
  `al > camY + ~240` kills mid-fall. With camY=10 the death plane is
  y~250 — it was silently killing every fall right at the i4=9 window
  (al~232-251) before the arm's ticks could align. camY=200 fixes.
- **Razor-thin window**: i4=(W[1]+10)/20 must equal row9 (~al∈[232,251]
  for the narrow fall box W[1]=al-62) AND W[2] must land in the arm's
  column window — hang wants W[2]∈[3960,3979], lip wants W[2]+5∈[4000,4019].
  The fall anim cycles per-frame box shapes (~ak+11 vs ~ak+34 east reach)
  and a ~+23px east drift during descent moves W[2] between windows —
  catch odds per fall are a few ticks of coincidence, exactly why real
  play rarely grabs (the "can auto-grab" is deliberately narrow).
- **`Q==61` latch**: `ledgeLipGrab` early-returns on Q==61 (previous-anim
  61 = just-hung). A jdb-forced S61 latches Q=61 across KO→reload
  (respawn restores fields, doesn't setAnim → no Q write; same-state
  setAnim(43) is a no-op) — silently blocks lip-grab in all later runs.
  Reset via a live transition (jump/land) or `set Q=0` before the fall.
- **Consumer verified**: bp at PlayerFsm:2059 hits every fall tick with
  `ct=true`, corner-9 gate open — the arm is live in the tick path.

### Artifacts
- `g4-ledge-tests.mp4` (169 s), `g5-hang-ko.mp4` (150 s), `g6-ledge-retry.mp4`
  (89 s) — falls/drops/reloads; the S61 hang pose is too brief (3-8 ticks)
  to catch between jdb-frozen frames.
- `g5-hangpose.png` (staged S61 pose), `g4-mantle.png` (settled on wall top),
  `g6-t55.png` (checkpoint strip).
- jdb evidence (in this run's transcript): per-tick consumer trace showing
  ledgeHangGrab=true → S61@4000,179 → S62 → S0 settle at 4010,179.

# Run 10 — devin/land @ 290d62c2 (slices through 244) — STOCK build, full golden-path demo

`./gradlew :android:assembleDebug` @ 290d62c2 → installed on emulator-5554,
new pid 12591. **Launcher class moved** to `com.acrebuild.spike/.AndroidLauncher`
(package restructure — `am start -n com.acrebuild.spike/.AndroidLauncher`).

## Golden path (all on-screen, real input)

- ✅ Boot → legal text card → jC23 attract → footer → jC18 title art → strip →
  jC2 level-select rows → row tap → jC9 briefing ("ROME/COLOSSEUM/KILL
  WOLFMEN") → jC21 intro dialog → SKIP → jC8 play at spawn (85,940). Full
  chain re-verified on this HEAD — every screen renders correctly
  (title art, menu rows, briefing card, dialog panel).
- ✅ Real-input run east: spawn → right-hold cell5 → run S=11, ak 85→1379,
  camera tracks (camX 8→1247), score ticks up to 8/100 en route. Wall at
  x~1400 stops ground traversal (route up needs the slab/zones — unchanged).
- ✅ KO → "DO YOU WANT TO RESTART?" banner → YES → full level reload →
  hint card → jC21 → SKIP → spawn (verified pre-checkpoint-arming reload).
- ⚠️ **adb input died mid-run** (same post-ANR symptom — no ANR this time,
  died after a jdb suspend cycle): kCj/kCk/lastTouch frozen −1 while taps
  did nothing. Switched to **mouse input through the emulator window** —
  fully sufficient (clicks AND holds via left_mouse_down).
- ⚠️ First screenrecord take (d1) captured only the **portrait home screen**
  — screenrecord started before/while the app rotated to landscape caught
  the launcher surface. Start recording after the app is in landscape.

## ax22 hopscotch — full B-lift chain on camera (d4)

Teleport-staged above zone-1 (documented assist; all captures/vaults/climbs
are the game's own FSM + real touch edges):

- ✅ Zone-1 (2064,695): overlap → **S65 snap to anchor**, e.S=1.
- ✅ Up-cell edge → **vault WEST** (16390 arm) → captured by zone-2 at
  (1982,605) — S65 again.
- ✅ Up-cell edge → **vault EAST** (16396 arm) → captured by zone-3 at
  (2104,546).
- ✅ Up-cell edge → vault → **S203 edge-grab at (2200,479)** roof-B lip.
- ✅ Up-cell edge → S62 mantle → **standing S=0 at (2210,479)** on roof-B.
- ✅ Roof run east by real input → KO'd by the guard pack at **(2940,499)**
  → jC12 → YES → **checkpoint respawn at (2590,519) x1=30** (no reload).
  Repeated → same respawn ×3.
- ✅ Combat on d5: teleport to the strip → lone guard melee — attack radial
  (view 305,200) lands slashes (red alert border, score ticks 4→8/100),
  guard strikes back → KO. Real exchange, both directions of damage.

### NEW input-model findings (drives all future touch work)

- **Pad cells come in two flavors**: `mounted` (zone-capture S65, edge-grabs,
  rides) → fixed bottom-left wheel box view(−5..111 × 124..240), cells via
  inner split x[33,72] y[162,201]; **not mounted** → player-relative 3×3
  grid (±25px x, head−10..feet+10 y).
- **Vault masks** (NpcFsm.kt tickZoneInteract L25): `padHeld(16390)` = up or
  TL edge → WEST vault (Z[2]==0); `padHeld(16396)` = up or TR edge → EAST
  vault (Z[2]!=0). `padHeld(33024)` = down edge → drop-through (Z[1]!=0).
- **`padHeld` = `pad.v` = bB edge word** — fresh edges only; holds keep
  firing because the input pipeline calls E() each frame while the pointer
  is down. A mouse_hold at the right cell sustains the edge.
- S65 captured → up cell (view ~60,140 → fixed pad) vaults whichever way
  Z[2] points; same cell worked for all three chain vaults.
- S203/S61 shared ledge-hang tail: `pad.v(16388)` up or toward-wall dir →
  S62 climb; `pad.v(33024)` → release-drop.
- **`resolvePadZone` returns −1 outside the active pad box** — touches
  elsewhere update `lastMoveX/Y` but produce no pad edge (diagnostic:
  bC/bB stays 0).
- KO YES row-tap ≈ view(175,122); the dialog button hitbox lags the banner
  by ~1 tick — retry if the tap bounces.

### Window → view calibration (mouse input on emulator-5554 window)

`viewX = (windowX − 108) / 1.1`, `viewY = (windowY − 35) / 1.1` (device
2400×1080 rendered ~0.227/0.261 scale inside the emulator window at
window-box ~(110,28)–(655,310)). Verified: fixed-pad cell5 (95,180)→
window (213,233) runs; cell1-up (60,140)→(174,189) vaults/climbs;
attack radial (305,215)→(478,252) slashes; KO YES (175,122)→(301,169).

### Artifacts (`plans/260922-0730-demo-verify/reports/`)
- **`d4-hopscotch-roof-ko-respawn.mp4`** (161.8 s) — THE money take:
  zone-1 capture → 3 vaults → S203 → climb → roof run → KO → checkpoint
  respawn → second KO → respawn.
- `d5-fight-kos.mp4` (166 s) — respawn loop + lone-guard melee exchange
  (slashes land, alert border) + KOs.
- `d2-ko-reload.mp4` (169 s) — first KO → banner → reload → SKIP → spawn
  + early zone attempts.
- `d1-boot-traversal.mp4` — BROKEN take (portrait home screen only);
  boot/menu proof is the live screencaps instead.
- Key frames: `d4-zone1-capture.png` (S65 snap), `d4-vault-west.png` /
  `d4-vault-east.png` (mid-vault), `d4-roof-standing.png`,
  `d4-ko-roof.png`, `d4-respawn.png`, `d5-f38.png` (melee exchange),
  `d5-f34.png` (standing on '5' strip), menus `d1-title/lsel/brief/dlg/
  spawn/x499/x1379.png`.

# Run 11 — devin/land @ f9bf7230 (slice 310, all-8-missions win conditions) — STOCK build, showcase demo

`./gradlew :android:assembleDebug` @ f9bf7230 → installed, pid 4545.

## Verified on camera (f1-full-demo.mp4, 169.4s + e1 boot/menus)

- ✅ Boot chain (e1): legal card → jC23 attract → jC18 title → jC2 level-select
  → jC9 briefing → hint card → jC21 → SKIP → jC8 play.
- ✅ Run east real-input: spawn run cell5 hold → x499→1381, camera tracks,
  score ticks to 8/100; the x~1400 wall still dead-ends ground traversal.
- ✅ **ax22 B-lift chain on camera again** (teleport-staged entry; all
  captures/vaults/climbs are real game FSM + touch edges):
  (2064,695) S65 → up-cell → W vault (1982,605) → up-cell → E vault
  (2104,546) → up-cell → S203@(2200,479) → up-cell → S0@(2210,479) roof-B.
- ✅ Roof run east → KO by the guard pack → jC12 "DO YOU WANT TO RESTART?"
  → YES → **full level reload** (hint card → dialog → spawn) — in this run
  no checkpoint respawn was observed; the two roof KOs may have been
  pre-arming, or slice-310 changed respawn semantics — flag for the lead.
- ✅ Combat melee on the strip: attack radial lands slashes (red alert
  border, score 4/100), guard strikes back → KO → banner → YES → reload.
- ✅ Loop closes cleanly back at spawn, game still running, no crashes/ANRs.

## Harness notes

- **adb input died mid-session AGAIN on a fresh process** — no ANR this
  time either; it worked for the nav taps (~3min) then froze (lastMove=-1).
  It's flaky per-process, not just post-ANR. Mouse fallback used for all
  gameplay.
- **screenrecord moov corruption**: pulling while the recorder is still
  writing yields an unplayable mp4 (lost e2 this way; f1 recovered by
  waiting for the file size to stop growing BEFORE pulling).
- e1 take mostly a briefing stall (the dead-input gap) — boot/menus valid
  for ~90s; the gameplay showcase is entirely on f1.

## Artifacts
- **`f1-full-demo.mp4`** (169.4 s, 23.5 MB) — THE demo take: zone-1 capture
  → 3 vaults → S203 → climb → roof → KO → YES → reload → SKIP → spawn →
  strip melee → KO → YES → reload → spawn.
- `e1-demo-290.mp4` (169 s) — boot→menus→briefing (usable first ~90s).
- Frames: `f1-vault.png` (mid-vault), `f1-f96.png` (strip melee, alert
  border), `f1-fight.png`/`f1-ko.png` (restart banner), `f1-f75.png`
  (post-reload spawn), `e1-menu.png` (title), `e1-vault.png` (briefing).

# Run 12 — devin/land @ f9bf7230 — MISSION COMPLETE chain verified + m1 stampAt CRASH

## Verified end-to-end on camera (w2-winflow.mp4 + w3-m1-crash.mp4)

- ✅ **MISSION COMPLETE screen** — jC=15: "MISSION COMPLETE" title,
  stat rows ENEMIES KILLED 1 / SILENT KILLS 0 / RETRIES 0 / SOULS 0 /
  TIME 2:32 / SCORE 200, typewriter footer "NEXT next-mission".
- ✅ **Win script path** — mission-0's win = script uid 116
  (`op37 r12=1 → screenL(15)`). Staged: `set kAV.aG=116` +
  `kAV.bindContext(world)` + `cd[1]=cd[2]=true` → the claim tail runs
  `runClaimScript` → op fires → jC=15. All downstream 100% real code.
- ✅ **Post-win routing** — stats confirm (`v(327712)` inside
  `v(458784)`) → `kAj++` → `kBA[14]=1` persisted (saveFlush) →
  `stateL(30)` medal browse (EZIO card) → confirm → `stateL(9)`
  briefing → mission-1 pack loads (ROME/A.D.1486/ESCAPE) → gameplay.
- ✅ **Unlock persists across relaunch** — fresh boot: YES/NO save
  prompt → title → CONTINUE/SELECT LEVEL rows → kDa=2 unlocked.

## 🔴 NEW BUG — mission-1 gameplay crashes deterministically

`FATAL EXCEPTION: GLThread — ArrayIndexOutOfBoundsException: length=273;
index=-1` (first crash) / `index=-57` (clean repro):
```
LevelPack.stampAt(LevelPack.kt:79)   // dl[(cx % 21) * 13 + (cy % 13)]
Level0Renderer.render(Level0Renderer.kt:1174)   // bh3 tile stamp loop
```
`stampAt` uses Kotlin `%` (sign-preserving) — when `parallaxX`/`parallaxY`
go negative the `dl[]` index goes negative → OOB crash. Mission-0 never
hits it (parallax stays ≥0); mission-1's spawn drives parallax negative
within ~20s of load. Reproduced on a fully clean path (fresh boot →
CONTINUE → browse → briefing → tap → gameplay → crash). Mission 1 renders
a few frames (golden tileset + soldier horde) before dying.
**Blocks any mission-1+ gameplay verification.**

## Also decoded this run

- ax42 escape fuse at (11410,418): kind-1, binds aw531, 70s countdown;
  expiry → `screenL(13)` with `kBx=58` = "MISSION FAILED. DIDN'T REACH
  THE ESCAPE LOCATION IN TIME"; `kBx=56` = "DID NOT CATCH YOUR TARGET"
  (the iW==2 far-band arm). Both fail-stat variants land on jC=31.
- Stats-screen input = `v(458784)` advance → `v(327712)` confirm.
- **Pad injection recipe**: `eval world.pad.e(mask,false)` — writes the
  real eK edge → commit → bB → `v()` fires. `set pad.bB` does NOT work
  (commit overwrites bB from eK every frame).
- Win op table: `runArgSub` r013==1 r12==1 → `screenL(15)` (WIN);
  r12==4/5 arm/disarm `kAV.Z[0]` (the chase goal). Level0 scripts carry
  no direct win op — the mission win routes through script uid 116.

## Artifacts
- **`w2-winflow.mp4`** (124s): gameplay → escape-zone collect →
  MISSION COMPLETE stats → (injected) NEXT → browse → m1 loading.
- **`w3-m1-crash.mp4`** (150s): clean relaunch → save prompt → title →
  level select → CONTINUE → EZIO medal browse card.
- `w1-winflow.mp4` (149s): escape-fuse arm + collect + fail-stat screens.
- `w2-mission-complete.png` / `w2-win.png` — the win screen.
- `w3-m1-gameplay.png` — mission-1 renders (golden tileset + horde).
- `w3-m1-loading.png`, `w3-ezio-browse.png`, `w1-escapezone.png`.

# Run 13 — devin/land @ 46329a09 (slice 311) — m1 stampAt CRASH-FIX verified

`./gradlew :android:assembleDebug` → installed → pid 22650.

## Verified on camera (m1fix-verified.mp4 150s + m1fix2-gameplay.mp4 90s)

- ✅ Boot → YES/NO save prompt → title → level-select → CONTINUE →
  jC=30 EZIO browse → briefing (ROME/A.D.1486/ESCAPE) → TOUCH → m1 gameplay.
- ✅ **Mission-1 renders + stays alive 90s+** — canyon tiles, wisp flames,
  the wolf horde formation, HUD + score (0→1/222) — the scene that
  crashed at ~20s pre-fix now runs continuously.
- ✅ **The exact crash condition is live**: `parallaxX=-1, parallaxY=-3015`
  measured mid-run — `stampAt` wraps the negative rows via `cyMod+=13`
  (k.java:4434-4438) → **zero ArrayIndexOutOfBounds** in logcat all run.
- ✅ **Real m1 gameplay loop**: spawn → horde combat → KO → "DO YOU WANT
  TO RESTART?" ×3 → YES → checkpoint respawn **on the glider** (ax25
  flying player — m1's flying mechanic) → combat again. Score ticks
  0→1 (a kill registered).
- 🔴 pre-fix behavior (run-12): `ArrayIndexOutOfBoundsException
  length=273; index=-1/-57` at `LevelPack.stampAt:79` ~20s into m1.
- ✅ post-fix: **no exception at all** — m1+ missions are runnable again.

## Artifacts
- **`m1fix2-gameplay.mp4`** (90s) — m1 horde combat + glider respawn +
  KO→restart cycles.
- `m1fix-verified.mp4` (150s) — the full nav chain to m1 gameplay.
- `m1-gameplay-fixed.png` / `m1fix2-t45.png` — canyon + wolf horde live.
- `m1-restart-banner.png`, `m1-glider-respawn.png`.

# Run 14 — devin/land @ 46329a09 — all-8-mission load/run smoke test

Unlock staged: `kBA[14]=7` + `kDa=8` → jC=19 select shows all 8 cards
(LEVEL 1-8: ROME×3, FLORENCE×2, VENICE, PANTHEON, ROME/COLOSSEUM).
Each: select → jC=30 browse → briefing → load → tap → gameplay →
15-25s monitor + logcat FATAL/AIOOBE/NPE scan.

## Verdict table

| kAj | Mission | Type/scene | Verdict |
|-----|---------|------------|---------|
| 0 | L1 ROME — rescue/escape the chase target | rooftop | ✅ verified earlier (win chain, Run-12) |
| 1 | L2 ROME — ESCAPE | canyon + wolf horde + glider | ✅ 90s+, KO→restart→glider respawn |
| 2 | L3 FLORENCE — KILL LUCREZIA & RESCUE CATERINA | night + soldiers + wheel | ✅ 18s+ |
| 3 | L4 FLORENCE — KILL JUAN BORGIA | night + spinners | ✅ 18s+ (spawn wedge → reload lands right) |
| 4 | L5 ROME — ESCAPE | canyon + wolf horde | ✅ 18s+ |
| 5 | L6 VENICE — KILL OCTAVIEN | dark scene + spinners | ⚠️ loads+renders, no crash — staged-entry soft-lock (S79 wedge off-camera); play unverified |
| 6 | L7 ROME PANTHEON — KILL MICHELOTTO | cyan interior + wisps + spinners | ✅ 18s+ |
| 7 | L8 ROME COLOSSEUM — KILL BORGIA & APPLE (BOSS) | skull arena + lava + saws | ✅ 24s+ |

**All 8 mission packs load + render without crashing** — zero FATAL /
ArrayIndexOutOfBounds / NullPointer across the whole run (vs the
pre-fix m1 AIOOBE). The stampAt wrap fix holds across every level's
tile data.

## Notable findings

- **First-load spawn wedge on m3/m5/m6** — the first gameplay spawn
  wedges the player (S79) → insta-fail; the KO→YES reload lands him
  on a proper spawn and the mission plays. Possibly a real
  first-spawn placement bug (or staged-entry carryover from the prior
  mission's fall state — needs a clean-entry recheck to classify).
- **m5 soft-lock signature** — S79 wedge at (21,-1334) with camY=0:
  above the kill-line so no fail fires; gameplay freezes off-camera.
  The reload path cleared it on m3 but m5's YES click resumed play
  still wedged.
- Each mission has distinct art/objectives: m2-3 Florence night,
  m4-5 canyon+dark, m6 Pantheon cyan+wisps, m7 Colosseum skulls+lava.

## Artifacts
- `m7-boss.mp4` (89s) — Colosseum arena scene on take.
- `m2-florence.png` `m3-florence.png` `m4-rome.png` `m5-venice.png`
  `m6-pantheon.png` `m7-colosseum.png` `m7-t15.png` — per-mission scenes.

---

# Run-15 — spawn-wedge classification: REAL first-spawn bug (menuJc9 omits resetPlayerToSpawn)

APK @ `46329a09`, emulator-5554, three separate FRESH boots (force-stop
→ am start → YES/NO → title → jC2 → stateL(19) select → kDa=8 + kBw=N →
context → browse → briefing → touch → gameplay → 15s+ monitor).

## Per-mission clean-entry verdict

| kAj | Mission | Clean entry | Where he landed | Verdict |
|---|---|---|---|---|
| 3 | L4 FLORENCE | S=5 fall → x1=0 → jC=12 in ~3s | dies at **(85,1399)** camY=1160 | **REAL bug** — insta-fail |
| 5 | L6 VENICE | S=0 standing, x1=30, stable 15s+ | (85,1143) on a floor | **plays** — the sequential wedge (21,-1334) was m4 carryover |
| 6 | L7 PANTHEON | S=5 fall → x1=0 → jC=12 in ~3s | dies at **(85,1399)** camY=1160 — identical | **REAL bug** — insta-fail |

KO→YES reload lands the pack's own spawn record (m3→(21,699), m6→(17,740)
— the lead's headless coords exactly) and both missions then play stably.

## Root cause — menuJc9() never repositions the player

`Level0World.menuJc9` (the briefing LOADING→TOUCH screen tick, :3606-3628):
`jG==3 → loadPackI(kAj)` swaps the pack; `jG==164 → spawnEntities(); postSpawn()`
rebuilds NPCs — **but `resetPlayerToSpawn()` is never called** (reload()
:4067 calls `spawnEntities → statsReset → resetPlayerToSpawn → postSpawn`;
menuJc9 does only the middle two). The player therefore enters every
mission keeping his previous coordinates:

- Clean boot → constructor `init{}` placed him at level0's record spawn
  **(85,940)** → every briefing-entry mission starts at (85,940):
  - m3: (85,940) has no floor → falls to world bottom (1399 = worldH-1)
    → kill-line x1=0 → jC=12 insta-fail.
  - m5: (85,940) falls onto a floor → stands at (85,1143) → plays (luck).
  - m6: identical to m3 → insta-fail.
  - m0: (85,940) IS m0's own spawn → works by coincidence.
- Sequential entries carry the previous mission's end position (m5's
  (21,-1334) off-camera soft-lock was m4's glider/fall state; m2's
  (60,1840) was m1's canyon fall state).

The original J2ME `G(164)=d(false)` respawned ALL entities from records
including the player; the port's `spawnEntities` deliberately skips the
persistent player entity — so `resetPlayerToSpawn()` must be called
explicitly after it on this path (and `statsReset()` too — death/kill
counters currently carry into the new mission as well).

Verified: pack spawn records (aclv, little-endian) m3=(21,699) m5=(44,582)
m6=(17,740) have solid floor (layer0 tile 20) directly beneath — geometry
is fine; the bug is purely the missing spawn-position reset.

## Artifacts
- `m3-clean-insta-fail.png` `m6-clean-insta-fail.png` — restart banner on clean entry
- `m5-venice-plays.png` — clean entry playing Venice (carryover cleared)
- `m6-playing.png` — m6 playing post-reload at (17,740)

Status: `46329a09-spawn-wedge-CLASSIFIED-menuJc9-missing-resetPlayerToSpawn-m3-m6-real-m5-carryover`

---

# Run-16 — slice-312 spawn fix VERIFIED: clean entries land pack records

APK @ `e1c58157` (slice 312 — menuJc9 now calls resetPlayerToSpawn +
clears checkpointSnap), emulator-5554. Same fresh-boot clean-entry
procedure as Run-15.

## Per-mission verdict (clean entry → coords → plays)

| kAj | Mission | Clean-entry spawn | Pre-fix | Now |
|---|---|---|---|---|
| 3 | L4 FLORENCE | **(21,699)** S=0 x1=30 jC=8, stable 10s+ | insta-fail (85,940→1399) | ✅ lands record, plays |
| 5 | L6 VENICE | **(44,582)** S=0 x1=30 jC=8, stable 10s+ | carried→fell→(85,1143) | ✅ lands record (better than pre-fix fallback), plays |
| 6 | L7 PANTHEON | **(17,740)** S=0 x1=30 jC=8, stable 10s+ | insta-fail (85,940→1399) | ✅ lands record, plays |

All three clean entries now land the pack's own playerSpawn record on
first gameplay — no insta-fail, no wedge, no carryover. m5 even improved
on its pre-fix behavior (it used to fall from (85,940) and land wherever
a floor caught it at (85,1143); now it starts at the designed (44,582)
rooftop spawn). The spawn bug is dead.

## Artifacts
- `m3-fixed-spawn.png` `m5-fixed-spawn.png` `m6-fixed-spawn.png` —
  post-fix clean-entry gameplay at the record spawns.

Status: `e1c58157-slice312-spawn-fix-VERIFIED-m3-21x699-m5-44x582-m6-17x740`

---

# Run-17 — m2–m7 play-through verification @ f7e868da (slice 314)

APK @ `f7e868da` (slice 314 — text-panel + typewriter + medal icons,
render/dead-path only), emulator-5554, ONE process, sequential mission
entries via the real chain (stateL(19) → kBw=N → browse → briefing →
touch → gameplay) — post-slice-312 this lands the pack record spawn.
Device `screenrecord` per mission; jdb bp-context evals for state.

## Per-mission play-through verdict

| kAj | Mission | Boots? | Spawn | Plays? | Deaths | Crash |
|---|---|---|---|---|---|---|
| 2 | L3 FLORENCE — kill Lucrezia | ✅ | record | ✅ ran east + orb 4/100 + jump over wall | none (guard blocked x~946) | none |
| 3 | L4 FLORENCE — kill Juan Borgia | ✅ | (21,699) | ✅ runs/jumps/restart ×2 | spike pit off the east ledge ×2 (real hazard) | none |
| 4 | L5 ROME — escape (FLYING) | ✅ | ax25 glider | ✅ sustained canyon glide ~20s, steered | descent into soldier horde → KO (real) | none |
| 5 | L6 VENICE — kill Octavien | ✅ | (44,582) | ✅ ran east on roofs | canal fall → KO (real hazard) | none |
| 6 | L7 PANTHEON — kill Micheletto | ✅ | (17,740) | ✅ ran east + orb 5/100 | none (eagle-statue wall needs up-route) | none |
| 7 | L8 COLOSSEUM — kill Borgia (boss) | ✅ | (579,1740) | ✅ ran east ~1500px through aqueduct | none | none |

**All six missions boot into gameplay on their pack record spawn and
are playable** — zero insta-fails, zero FATAL/AIOOBE/NPE app crashes.
Every death observed was a real level hazard encountered while running
(spike pit, canal water, the canyon soldier horde) — the authentic
gauntlet design, not placement bugs. Mission-type coverage: grounded
(m2/m3/m5/m6), flying ax25 (m4), boss arena (m7).

Caveats: input died mid-m6 (the recurring per-process pointer flakiness —
pad.e()/mouse fallback used; not a game defect). The m4 record ends on
the restart banner so the final ~30s shows the KO loop, not more flight.
Manual traversal couldn't clear every early hazard on first tries —
routes exist (headless bot won all 8) but need real play.

## Artifacts
- `pt-m2.mp4` `pt-m3.mp4` `pt-m4.mp4` `pt-m5.mp4` `pt-m6.mp4` `pt-m7.mp4`
  (~140-150s each) — briefing→spawn→gameplay per mission.
- `pt-m2-roofs.png` `pt-m2-run.png` `pt-m3-spikes.png` `pt-m4-glider.png`
  `pt-m4-horde.png` `pt-m5-canal.png` `pt-m6-eagle.png` `pt-m7-run.png`

Status: `f7e868da-m2-m7-all-boot-play-no-crash-real-hazard-deaths-only`

---

# Run-18 — slice-315 demo @ 3344539d (headwt): golden path + prop smash + KO/restart

APK built from `/tmp/headwt/rewrite` @ `3344539d` (slice 315 — the
i.ad() speech-bubble draw path + all prior fixes), emulator-5554
(pid 19260). Device `screenrecord` ×2; real input where alive, pad.e()
edges for menus/dialog (labeled).

## Beats verified on camera
- ✅ Boot → YES/NO save prompt → title → level-select → NEW GAME →
  EASY/NORMAL difficulty → EZIO card → **story intro card**
  ("IN AN ATTACK ON THE AUDITORE FAMILY VILLA…" full text render) →
  briefing "ROME/COLOSSEUM/KILL WOLFMEN" → two hint pages → gameplay
- ✅ Run east — camera tracks, score 0→4/100; spawned at level0 record
- ✅ **Destructible prop smash** — amphora vase broken → orb drop →
  score 8/100 → 12/100
- ✅ Wall-terrace climbing — vine ledges (partial hopscotch area)
- ✅ **Pause menu** — RESUME/RESTART/OPTIONS/HELP/MAIN MENU/EXIT rows
  render over live gameplay (accidental pause, kept — bonus coverage)
- ✅ KO → "DO YOU WANT TO RESTART?" banner (YES/NO) → YES → reload →
  respawn at spawn, score reset — **loop closes clean**
- ℹ️ No speech bubble seen — expected on m0 (the new i.ad() path is
  armed but no bubble-guard fired it this run).

## Notes
- The KO was staged via a `jtp5` teleport into a kill volume (assist —
  labeled), not a guard strike; guard melee untested this run (pointer
  flakiness kept interrupting combat approach).
- Intro dialog dismiss quirk: jC=21 dlgU=9 — pad edges don't dismiss;
  the hint pages are jC=9's own load flow and advance on M_CONTEXT
  edges (65568). The 327712 mask opens PAUSE from gameplay (jC=14) —
  M_CYCLE (131072) backs out.
- Input died intermittently mid-run (the per-process pointer flakiness);
  recovered by waiting and re-tapping.

## Artifacts
- `head-m0.mp4` (150s) — boot→menus→spawn→run→vase-smash→climb→pause menu
- `head-m0b.mp4` (150s) — climbing→KO banner→restart→respawn
- `head-m0-spawn.png` `head-m0-vase-smash.png` `head-m0-restart.png`
  `head-m0-pause.png` `head-m0-banner-hd.png`

Status: `3344539d-slice315-demo-VERIFIED-goldenpath-propsmash-pause-ko-restart-nocrash`

# Run-19 — desktop LWJGL3 speech-bubble verify @ eff107e2 (slice 315)

Target: the NEW `BubbleDraw` render path — `Level0Renderer.drawBubble`
consuming `world.bubbleDraw`, emitted by `npcFsm.tickBubble`. Desktop
`:lwjgl3:run` session, JDWP 5005. Claim site: m0 ax5 uid 234 at (3650,721),
W strip x3650-3674 y721-871 in the balcony shaft (ACLV f12/f13=24x150).

## Verdict — RENDERS, with one visual defect

- ✅ **Real claim fired** — teleport player into the W strip (3662,790)
  → `bindContext` → script 327 → camera pan → scripted Altaïr-vs-guard
  encounter with SKIP strip → op106 arms guard uid 325's cQ →
  `tickBubble` → `setBubbleDraw`.
- ✅ **Bubble draws on screen** — white rounded panel + black outline,
  wedge tail pointing at the guard, 3-line wrapped centered text,
  typewriter crawl. Two pages captured on video:
  "FREE CLAUDIO / FROM THE / WOLFMEN. IF YOU" and
  "THEN PROVE YOU / ARE WORTH / HELPING. HELP US".
- ✅ **Descriptor fields dumped live** (jdb watch on `bubbleDraw`,
  `next` into `tickBubble` frame): `x=190 y=120 w=120 h=10 lines=0
  flip=true tailUp=false text="YES.\n"` — real values mid-page-write;
  watch fired null→BubbleDraw(1741)→null (write+consume lifecycle).
- 🔴 **Visual defect — first text line clipped by panel top edge**:
  on BOTH pages line 1 ("FREE CLAUDIO", "THEN PROVE YOU") sits half
  ABOVE the panel's top border — `textY=y+5` vs the panel rect looks
  ~1 line-height too low (baseline-vs-top-anchor). Reproducible.
- ⚠️ jdb cQ re-display trick did NOT hold the bubble — `q[3]=600`,
  `q[2]=-1` set OK but the sequence still completed → descriptor
  nulled (the trick likely needs the claim gate / cd[] context).
- ⚠️ Player dies right after the scripted encounter every pass — the
  claim releases into a real melee the (input-dead) player loses.
  `claimSuspendsPlayer` holds during the script, not after.
- ⚠️ pad.e(327712) on the jC=12 banner worked once then stopped;
  mouse taps worked. jC=21 intro needs the SKIP strip tap (848,565).

## Artifacts
- rec-3dbf568d…-edited.mp4 (40s) — claim sequences + combat passes
- rec-8e3a9d93…-edited.mp4 (34s) — watchpoint pass
- `dt-bubble-p1-crop.png` `dt-bubble-p2-crop.png` — THE bubble frames
- `dt-bubble-page1.png` `dt-bubble-page2.png` — raw 1600x1200 frames
- `dt-claim-scene.png` `dt-altair-vs-guard.png` — the encounter

Status: `eff107e2-slice315-BUBBLE-RENDERS-VERIFIED-x190y120w120-flip-firstLineClip-defect`

# Run-20 — showcase demo @ fa9fce4f on emulator-5554 (device screenrecord)

Device `screenrecord` takes (framebuffer-only, landscape user_rotation=1).
apk = headwt @ fa9fce4f (all fixes incl. speech-bubble path).

## Verdict — playable end-to-end on camera, fight beat not captured

- ✅ Cold boot → splash → YES/NO save prompt → title → NEW GAME →
  ROME/COLOSSEUM "KILL WOLFMEN" briefing → intro dialog (typewriter,
  Ezio portrait) → SKIP → LOWER COLOSSEUM AREA gameplay — full chain
  on real taps (one labeled jdb edge assist at the save prompt).
- ✅ Traversal — run east with camera track, ledge jump, vine/wall
  climb (multi-segment auto-grab), mid-air poses all draw clean.
- ✅ Destructible vases smashed ×3 → orb drops → score 4→8→11→12/100.
- ✅ Guard ALERT ("!!" icon + music note + numeric prompt) fired on
  the upper terrace — a soldier sprite visible on the roof edge.
- ✅ KO → "DO YOU WANT TO RESTART?" → YES → respawn at spawn —
  restart loop verified TWICE on camera.
- ⚠️ Guard FIGHT not captured — the alerted soldier patrols the roof
  line; blind D-pad inputs kept wall-hanging at the mid-terrace vine
  (loop: run east → fall → auto-grab → climb → repeat). Long teleports
  (6800,1080 street patrol) auto-fail via camera-lag OOB (al>camY+240
  before the camera catches up) — hop-wise staging needed ~350px steps
  but the jdb freeze (below) ate the attempts.
- ⚠️ jdb suspend-leak wedged the app 3×: each attach's bp-hit suspends
  GLThread; leftover suspend-count survives detach (sessions die
  mid-suspend) → sim freezes, input dies, "tick" bp never fires.
  Recovery: `resume <tid>` ×N in one session, or force-stop. The
  one-shot edge.sh helper (bp→eval→clear→run in ~1.5s) avoids it.
- 🔴 ANR dialog captured once in show2.mp4 (~t+95s) — a >5s held
  suspend while input queued. Dismissed with Wait; NOT a game defect —
  a jdb-usage artifact. Keep every suspended window <2s.

## Artifacts (reports/)
- `show1.mp4` (85s) — boot→splash→save-prompt stall (input-dead take,
  superseded)
- `show2.mp4` (207s) — YES/NO→title→menu→NEW GAME→briefing→dialog→
  gameplay start (contains the ANR beat ~t95s)
- `show3.mp4` (239s) — gameplay take: run/jump/climb/vase-smash/orbs/
  alerts — THE SHOWCASE TAKE
- `show4.mp4` (199s) — KO→restart→respawn + more traversal
- `showcase-vase-smash.png` `showcase-jump.png` `showcase-alert.png`
  `showcase-respawn.png` `showcase-strike.png`

Status: `fa9fce4f-showcase-TAP-VERIFIED-goldenpath-traversal-smash-KOrestart-nofight`

# Run-21 — m2 Florence guard-combat clip @ fa9fce4f (show5.mp4)

Follow-up to Run-20's missing fight beat. emulator-5554, same apk.
SELECT LEVEL unlocked via `kDa=8` + row `kBw=2` → jC=30 → briefing
(FLORENCE / A.D. 1486 / KILL LUCREZIA & RESCUE CATERINA) → gameplay.

## Verdict — real combat captured on video

- ✅ Real approach: spawn → run east through the arched gallery,
  orbs 0→4/100, checkpoint rings — then the spawn-adjacent soldier
  (~x600 patrol, earlier than the x946 estimate).
- ⚠️ The gallery's broken east edge drops into a wall pocket; the
  x946 wall-cling guard perches on the wall's vine strip and cycles
  top↔mid perches — never descends to ground for melee. His
  wall-perch FSM makes him effectively un-fightable at ground level.
- ⚠️ Positioning assist (labeled): two hop-wise jdb teleports
  (753,1925)→(880,1900)→(968,1876) placed the player on the upper
  roof beside the patrol soldier — same <350px cam-safe hops.
- ✅ REAL FIGHT on camera: soldier alerted (sword drawn), player
  sword strikes → HUGE blue slash arcs, enemy health bar overhead,
  hit-stagger, orb drops bursting (score 4→8/100), soldier striking
  BACK — two-direction damage, red screen flash on hits taken.
- ✅ KO ending: the soldier's counterattacks killed the player →
  "DO YOU WANT TO RESTART?" — real combat stakes, no staging.
- ⚠️ jC=12 YES is awkward: pad.e(327712) edges get eaten by the
  kJT held-bits flush / arm-then-confirm pattern; taps worked once
  at dev(1025,590)=YES row. Reliable confirm: `menuItem(14)` (YES
  row id) called directly in bp-context — jC 12→8 instantly.
- ⚠️ Post-KO respawn loop: after the fight death, reloadCheckpoint
  landed then immediately re-entered jC=12 with aB=0 — checkpoint
  snap may have captured the lethal roof position (checkpoint ring
  crossed mid-roof). Needs a look — respawn-after-fight-death may
  insta-refail if the last checkpoint snap is on a hazard. Repro:
  die to the roof soldier, YES, watch aB.
- ℹ️ take-1 (show5-approach.mp4) covers boot→unlock→briefing→spawn→
  first chase+wall-cling standoff; take-2 (show5-standoff.mp4) the
  perch cat-and-mouse. The decisive fight is take-3 = show5.mp4.

## Artifacts (reports/)
- `show5.mp4` (179s) — briefing→spawn→run→gap→teleport→ROOF FIGHT
  (slash arcs, health bar, orb drops, bidirectional damage)→KO→banner
- `show5-approach.mp4` (239s) — approach/chase/wall-cling standoff
- `show5-standoff.mp4` (198s) — perch standoff continued
- `show5-roof-alert.png` — roof landing beside the alerted soldier
- `show5-fight.png` / `show5-fight2.png` — slash-arc + toe-to-toe melee
- `show5-ko.png` — restart banner after losing the exchange

Status: `fa9fce4f-m2-COMBAT-ON-VIDEO-slasharc-healthbar-bidir-ko-teleportassist`

# Run-22 — golden-path demo @ bdf117d3 on emulator-5554 (device screenrecord)

Fresh `pm clear` boot on the slice-316..319 HEAD (tile map, aQ button
transforms, sync meter, string-table index, S25 fall arm, S152 posted-perch
verdict). Three device screenrecord takes, framebuffer-only landscape.

## Verdict — full chain + real combat on camera; player loses the clinch

- ✅ Cold boot → YES/NO → title → NEW GAME → EASY → EZIO card → story
  intro (jC=20, auto-typewriter pages — exits via M_CYCLE/M_PAUSE when
  kCu==5; my taps only rewind pages) → ROME/COLOSSEUM "KILL WOLFMEN"
  briefing → hint pages → jC=21 intro dialog → SKIP pill → gameplay.
- ✅ Traversal — run east w/ camera track, orbs 0→12/100, urns smashed
  (score bumps), red "!!" alert icon at ~x1176, promenade→wall.
- ⚠️ Wall/chain climb could not be completed blind — the east wall at
  ~x1180 bounced every jump; route over it needs real platforming.
- ⚠️ Labeled positioning assists: hop teleports (~300px steps, cam-safe)
  up to the hedge terrace where the alerted soldier patrols (~x2150).
- ✅ REAL COMBAT ×3 on camera: clinch melee vs the roof soldier —
  blue slash arcs, sword-clash spark bursts, orange slash trails,
  overhead enemy health bar, orb drop 1/100 mid-fight, red damage
  flash on hits taken. Soldier counterattacks kill the player every
  time — honest losses, no scripted outcome.
- ✅ KO→"DO YOU WANT TO RESTART?"→YES→respawn loop ×3 — restart
  replays the jC=21 intro dialog each time (authentic behavior);
  SKIP pill returns to gameplay. m0 reload does NOT hit the Run-21
  checkpoint-snap death loop (respawn = clean spawn record).
- ⚠️ x1=90 jdb health assist set before fight #3 — player still lost
  (soldier's clinch burst is lethal regardless; could not verify a
  kill or a ledge-fall — the soldier stays rooted at the ledge lip,
  S25 fall arm never triggered on camera).
- ⚠️ Speech bubble: none fired this run (expected — m0 guards don't
  speech-script; the claim-site bubble was verified in Run-19).
- ✅ No crashes, no ANRs, no frozen states this run.

## Artifacts (reports/)
- `gd1-boot.mp4` (238s) — boot→YES/NO→title→NEW GAME→EASY→story intro
- `gd2-goldenpath.mp4` (238s) — story→briefing→dialog→SKIP→spawn→run
  east→urns→alert→wall→terrace clinch→KO #1
- `gd3-combat-restart.mp4` (219s) — teleports→terrace fights #1-3→
  KOs→restart dialog→respawns — THE COMBAT TAKE
- `gd-spawn.png` `gd-alert.png` `gd-fight-arrival.png`
  `gd-soldier-slash.png` `gd-ko.png` `gd-respawn.png` `gd-introdlg.png`

Status: `bdf117d3-goldenpath-VERIFIED-combat3x-clinch-healthbar-slasharcs-KOx3-restartloop-nocrash`

# Run-23 — ax5 speech-bubble ON DEVICE @ 2ff4ca0d (emulator-5554)

Fresh pm-clear boot, mission 0 via SELECT LEVEL (kDa=8/kBw=0 unlock
assists). Goal: real device screenshot of the BubbleDraw panel.

## Verdict — BUBBLE RENDERED + CAPTURED on device

- ✅ ax5 claim fired: staged Altaïr-vs-guard encounter rendered at the
  spawn gallery — camera holds, SKIP strip live, guard + Altaïr sprites.
- ✅ Speech bubble draws ON DEVICE: white rounded panel + black outline,
  wedge tail pointing at the guard's head, wrapped centered text with
  typewriter crawl — three pages captured:
  p1 "WHO ARE YOU? YOU…" (q13), p2 "FROM ENTERING / THE UPPER" (q14),
  p3 "LEAVE HERE AT…" (q16/h12) — the guard's exact script-327 strings.
- ✅ On device video (bub2-claim-bubble.mp4 t~105-112): bubble visible
  mid-sequence, then post-claim melee + red damage flash.
- 🔴 Visual defect REPRODUCED on device (same as Run-19 desktop): the
  typewriter's in-progress line renders clipped at the panel's bottom
  edge — q14/bubble-video-frame shows "THE UPPER" half-buried under the
  panel border while typing. Panel rect vs type-line Y is off.
- ⚠️ Post-claim: the scripted release drops the player into melee with
  the staged guard — he dies every pass (consistent w/ Run-19).
- ⚠️ Trigger assist (labeled): the natural W zone [3650,721,3674,871]
  is a balcony-shaft strip — the street below is a water kill-zone
  (~x3400-3700, waterfall) — player teleport-hops died twice reaching
  it. Instead moved the ENTITY to the player: `findByAw(234).ak=75,
  al=880` — W re-derives from ak/al each tick (writing W[] directly is
  overwritten) — the zone covered the player → claim fired naturally.
- ⚠️ Checkpoint-snap: after the claim+death, reloadCheckpoint(true)
  respawned with the entity S=8 but the W-move/claim would NOT refire
  — the snap may have captured post-claim state (fresh process + full
  mission re-entry re-armed it).

## Artifacts (reports/)
- `bub2-claim-bubble.mp4` (148s) — spawn → W-move → claim → bubble →
  post-claim melee; bubble visible ~t105-112
- `bub1-entry.mp4` (238s) — boot→menus→entry→teleport deaths (context)
- `bubble-p1-who.png` — page 1 "WHO ARE YOU? YOU" typing
- `bubble-p2-from.png` — page 2, type-line clip defect visible
- `bubble-p3-leave.png` / `bubble-p3.png` — page 3 held
- `bubble-video-frame.png` — video frame at t110
- `bubble-cleared.png` / `bubble-postclaim-melee.png` — sequence end + melee

Status: `2ff4ca0d-BUBBLE-ON-DEVICE-VERIFIED-3pages-tailwedge-typewriter-clipDefectReproduced`

---

## Run-24 — slice-324 dialogAdvance clip-fix verification (emulator-5554) — 5cd6e090

Rebuild: `/tmp/headwt` checked out to `5cd6e090` (merge PR #364, includes
`6153a235 fix(port): slice 324 — dialogAdvance uses real y-font metric`)
→ `:android:assembleDebug` → `adb install -r` → `pm clear` cold boot.

Method: same claim as Run-23 — BUT the correct entity-move recipe is now
proven: `findByAw(234)` needs BOTH `ak/al` (activate the entity's tick —
it is gated by position; at ak=3650 it never ticks near spawn) AND
`W[0..3]` (the ax5's W is init-only in `initMissionLogic:2942` — NOT
re-derived per tick; `eventBind` overlap-checks `e.W`). One jdb session
set ak=75, al=880, W=[70,880,110,1000] over the spawn player (80,940) →
claim fired on `run` → script 327 staged the Altaïr-vs-guard scene →
guard's cQ bubble played 3 pages → released into melee → player KO'd.

VERDICT — FIX VERIFIED: all 3 pages render their text fully INSIDE the
white panel. Direct comparison on page 2 ("FROM ENTERING / THE UPPER"):
Run-23 `bubble-p2-from.png` shows "THE UPPER" half-buried under the
panel's bottom border; `fix324-p2-from.png` shows the same completed line
with clear padding below — the panel grew upward ~14px as designed
(`footerFont.linesHeight(n)` = n*J + (n-1)*K), wedge-tail anchor
unchanged at the panel's bottom-right. Pages: p1 "WHO ARE YOU? YOU",
p2 "FROM ENTERING / THE UPPER", p3 "LEAVE HERE AT" — all clean.

## Artifacts
- `fix324-claim-bubble.mp4` (149s) — full pass: gameplay → jdb move →
  staging → all 3 bubble pages (t~9.5-13) → release melee → KO
- `fix324-p1-who.png` / `fix324-p2-from.png` / `fix324-p3-leave.png` —
  the 3 pages on the fixed build (video frames, native 2400x1080→1568)
- `fix324-staging.png` — scripted encounter staging frame

Status: `5cd6e090-slice324-CLIPFIX-VERIFIED-all3pages-insidepanel-panelgrewup-tailanchor-same`

---

## Run-25 — pure-input store demo (emulator-5554) — ce8a600c

Build: ce8a600c (slice-325 merge) — code-identical to 5cd6e090 (the merge
adds only docs/report files; APK unchanged).

PURE INPUT — no jdb assists. Input bridge is touch-only (`Level0InputBridge`
has no key handler — `input keyevent` never reaches the game; keyevent 61/96
hit Android instead and switched to the launcher). Pure-input menu recipe,
proven live:
- boot legal screens: tap anywhere or wait the auto-advance timers
- **YES/NO sound prompt (jC=23)**: row taps do NOT confirm — the real input
  is the LEFT footer soft-key (pause-icon zone, `pointerDownIn(-5,198,
  kCe+20,47)` → `padE(M_PAUSE)` ⊂ 327712 → the case-23 else-branch →
  `stateL(18)`). Device tap ~dev(490,940).
- main menu / difficulty / character card: menuQ row taps work — NEW GAME
  dev(1240,543), EASY dev(1049,498), EZIO dev(1240,345)
- story intro jC=20: exits via M_CYCLE = the ↩ back-arrow footer icon
  (dev~1900,940) — any time, not just at end
- jC=9 hint pages: taps advance; jC=21 intro dlgU=9: SKIP pill dev(1870,891)
- pause menu RESTART → YES row dev(1200,580); restart replays jC=21

**Coord map correction**: view render is `sc = min(sw/400, sh/240)` with
INT math → scale=4, offsetX=400, offsetY=60 on 2400x1080 (not 4.5/300/0):
devX = 400 + lx*4, devY = 60 + ly*4.

Per-process pointer death: taps/keyevents can land on a dead input pipe
(0-diff screencaps) — force-stop + relaunch fixes; verify pointer life with
a probe tap + diff before committing a take.

**Encode lag**: the first take ran ~2.5-3× stretched during the menu
section on swiftshader (the YES/NO prompt held ~35s of video; the whole
menu chain consumed ~145s of a 173s take). Gameplay takes run ~realtime.
Deliverable built by cutting the boot-path segments from take-1 +
realtime gameplay segments from take-3 into `store-demo.mp4` (108s).

Content shown: legal → YES/NO → title → NEW GAME → EASY → EZIO →
story → ROME/COLOSSEUM "KILL WOLFMEN" briefing → intro dialog → SKIP →
spawn → run east w/ camera → orb chain → double-urn smash (orbs 4→8→12) →
ledge gap jump → wall-cling/grabs → upper terrace → sentry "!!" alert →
climb attempts at the guard's perch + sword swings. No jdb anywhere.

HONEST GAPS: no landed melee exchange (the posted sentry holds his lip —
blind input can't top the rail climb) and no guard bubble (claim site not
reachable by pure input). The posted-perch behavior is consistent with the
S152 verdict (fightable but doesn't leave his post).

## Artifacts
- `store-demo.mp4` (108s) — THE DELIVERABLE, tight cut, all screens + gameplay
- `store-demo-raw-full.mp4` (173s) — take-1 raw (full chain, menu section slow)
- `store-demo-raw-gameplay.mp4` (164s) — take-3 raw (realtime gameplay)
- `store-spawn.png` / `store-urn-approach.png` / `store-balcony.png` /
  `store-alert-climb.png` — marketing stills
