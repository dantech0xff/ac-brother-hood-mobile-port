---
name: android-emulator-testing
description: Run/verify the LibGDX AC Brotherhood port (com.acrebuild.spike) on the local Android emulator — boot, input maps, state inspection via jdb, common blockers.
---

# Android emulator testing — AC Brotherhood port

## Environment
- `adb` = `~/Android/Sdk/platform-tools/adb` (not on PATH). AVD `spike` (API 36) = emulator-5554.
- Build: `cd rewrite && ./gradlew :android:assembleDebug` → `android/build/outputs/apk/debug/android-debug.apk`; `adb install -r`; launch via `monkey -p com.acrebuild.spike -c android.intent.category.LAUNCHER 1` or `am start` (use `am force-stop`+`pm clear` for a cold boot — `am start` on a running app only resumes it).
- Game renders 400×240 landscape letterboxed at scale 4, offset (400,60) on a 2400×1080 landscape surface.
- **Display orientation gotcha (lost ~30 min to this):** `AndroidManifest` uses `sensorLandscape` — the app gets its surface orientation from the ACCELEROMETER, not `user_rotation`. If the app renders in a tiny ~200×210 box at bottom-left, the WM gave it a portrait surface. Reliable fix: `settings put system accelerometer_rotation 1` (the orientation listener only runs with auto-rotate ON — with it off, sensorLandscape falls back to portrait) + give the sensor a landscape gravity vector: `emu sensor set acceleration 9.8:0:0` (or `emu rotate` — but it cycles through 4 orientations). Then `am force-stop` + relaunch. `wm size 1200x540` + `user_rotation 1` WITHOUT a landscape sensor = broken; ALSO `pm clear` RESETS `user_rotation` to 0. Safest verified combo: `wm size reset; wm density reset; accelerometer_rotation 1; sensor accel 9.8:0:0` → surface 2400×1080, scale=4, offset (400,60). Verify by reading `renderer.scale/offsetX/offsetY` live via jdb (Level0Game.render bp) — don't guess.
- `dumpsys SurfaceFlinger | grep -A3 'SurfaceView\[com.acrebuild'` shows the app's surface bounds (want landscape WxH>W).
- `adb shell screenrecord --time-limit N --size 1600x720 --bit-rate 6000000 /sdcard/x.mp4` records the framebuffer directly → clean landscape video (pull with `adb pull`). Prefer this over desktop capture when the window is small. `--time-limit` max is ~300 s — record multiple takes and concat (`ffmpeg -f concat -c copy`).
- **Perf note:** `-gpu swiftshader_indirect` (no host GPU). Post slice-170 the renderer packs all modules into one PixmapPacker atlas → GLThread ~10% CPU, ~16 unique fps = 1 frame per 62 ms sim tick (renderer no longer the bottleneck). Pre-170 it saturated a core (~97%, stack in `copyJni`+`glBufferData` per module) and heavy scenes collapsed to ~0.2 fps. `adb reboot` the guest if it's been up ~days (guest swap accumulates → even slower).
- **Measuring render fps — DON'T trust these:** gfxinfo `GPU p50` saturates at 4950 ms on this app (measures end-to-end GPU pipeline latency, not display rate) — useless. `ffmpeg mpdecimate` counts H.264 encoder noise as "distinct" — useless. Per-second frame diffs measure GAME SPEED (sim runs at 62 ms regardless of render rate) — useless for render fps.
- **Measuring render fps — DO:** `screenrecord` 15-60 s → pull → extract ALL frames → count consecutive-unique via `compare -metric AE prev cur null:` with threshold >4000 px (encoder noise floor ≈8-11 k px at 1200×540; <500 = true dup). Unique/s ≈ real render rate; ~16/s = keeps pace with the sim tick. Cross-check: `top -b -n 2 -d 1 -H -p <pid>` → GLThread %, and `dumpsys gfxinfo … framestats` PROFILEDATA rows (Vsync→FrameCompleted deltas = real ms/frame).
- **jdb attach slows it** (ART deopt): on a busy build a `Level0Game.render` breakpoint may take 15–90 s to hit — feed commands through a fifo script that WAITS for "Breakpoint hit" before `dump`/`eval`/`set` (see `/tmp/jdbdump.sh` pattern: `mkfifo`, jdb < fifo > out, `echo cmd >&3`, poll `grep 'Breakpoint hit'`). On the fast atlas build `render` bp may not hit inside 90 s — try `Level0World.tick` or plain `suspend` (all threads) + `thread <id>`/`where`.

## Touch input model (Level0World)
- All input is view-coordinate taps/holds. DOWN events → `resolvePadZone` (live for jC==8 and jC21 dlgU∈{8,10}); UP events → `lastTouchX/Y` → `pointerStrip()` (true when tap-UP lands view-y 0..204, or outside the 36..364 x-guard). View→screen for the letterbox: `sx = offsetX + x·scale`, `sy = offsetY + y·scale` — **read scale/offsetX/offsetY LIVE via jdb** (bp on `Level0Game.render`, `dump this.renderer.scale` etc.) — they depend on the surface size (2400×1080 → scale 4, offset (400,60); a portrait surface gives a broken corner render, see orientation gotcha).
- **jC=8 (play) — mounted pad (CORRECTED slice-204 session):** `resolvePadZone` (Level0World.kt:4059+) — the `mounted` branch is ACTIVE, so the wheel is a FIXED screen rect, not player-relative: `insideRect(-5,124,116,116)` = view x[−5,111], y[124,240] (display 380..844 × 556..1020 at scale4/offset400,60). `wheelCell` splits it into a 3×3 grid (~39×39 px cells): cell5 = run-right (tap view 95,180 → display 780,780), cell3 = run-left (view 25,180), cell2 = vault-up-right (view 95,140 → display 780,620). TWO separate radials sit to the right: `insideRadial(305,200,35)` → cell4 → `padE(32)` = M_CONTEXT attack/interact (display ~1620,860); `insideRadial(355,145,35)` → cell1 → `padE(4)` = jump (display ~1820,640). Taps outside all three zones → −1 (the player-relative wheel only exists for non-mounted state — tapping ON the player does NOTHING while mounted). A tap CLEARS held bits — re-issue RIGHT-holds after every tap. Jump mask on pad = 16398.
- **Menus:** footer zones (left view −5..65 → M_PAUSE, right 385−kCf..405 → M_CYCLE), `pointerStrip`. `pad.v(mask)` = `bB and mask != 0` → ANY bit of a composite satisfies it. **Row taps are same-tick only:** `menuRowAt(pressY)` hit-tests `pointerDownIn(rect)` against `lastTouchX/Y` (the UP coords) and `pressY` comes from `events.firstOrNull{DOWN}?.y` — a row tap commits ONLY when the DOWN and UP land in the same 62 ms tick's event list; slow taps (>1 tick) select nothing. jC=2 menuPanelRect=(93,45,214): row0=(93,55,214,35) → display ~1200,348; row1=(93,106,214,30) → ~1200,544; row2=(206,55,214,30) — row0 = "LEVEL" = the level-0 pack. Row rects sit ABOVE their visual buttons (row0's rect is y55–90 while the button draws lower) — tap by rect, not by visual.
- **jC=21 dialogs (post slice-165):** `pointerStrip→padE(M_CONTEXT)` still armed; **NEW bottom-right SKIP strip `insideRect(349,198,56,47)` → `padE(M_CYCLE)`** — the hardware right soft key — dismisses u9 script dialogs (visible SKIP button draws). Pause icon `insideRect(354,0,46,37)` armed on jC∈{8,21}.
- **jC=12/13 fail/win prompt:** menuQ rows — `menuRowRects` for jC12 = two side-by-side rows at `view x93..307 & x206..420, y117..147` (≈ screen x359..1095, y263..331 at scale 2.25/off150). Two-stage confirm: first press sets `kBw=0`, second fires RESTART → `reloadCheckpoint`. The "LEVEL/LEVEL 2" strip visible is NOT the hit zone.

## BLOCKERS — verified @eb6516f5 (slice 208)
- ✅ Boot on eb6516f5 is clean (slice-207 fix works stock; the slice-177 `levellevel0/` + tilesetClip=0 crash is gone).
- ✅ u9 intro dialog SKIP strip re-verified (display ~1908,944).
- ✅ **ax4 crate/ledge climb works (slice-208):** from the shaft floor y959, jump radial (display 1820,640) → `player.g` binds the crate/edge → S=37 hang → right-hold shimmies EAST x1682→2075 along the '5' platform edge → jump mantles onto the beam (x2149–2169, y906). Climb sequence works end-to-end by touch.
- ✅ **Guards engage on same-floor proximity** — npcs[104] (ax11@2187) stays aA=0 while the player is on the beam 40 px away (y906 vs floor y960); drops to the floor → aA=1, strike S=12, stagger player S=9, KO (x1 30→0 ≈2 s). Guards stay armed across reloads (aA=1 persists). Dormant≠dead — they're floor-gated, not missing triggers.
- 🔴 **East corridor dead-end at x2200–2340** — solid wall block (cols110–117, r40–52, no door cells). '5' platform ends x2100; run+jump toward the wall top covers ~70 px and lands on the floor at x2169 (~30 px short); wall face doesn't bind a climb on contact. Beam perch at (2149–2169,906) = soft-lock pocket (S=89, no input moves; west drops back to shaft floor).
- ⚠️ **Upper route exists, teleport-unverifiable** — balcony floor y580 (x1480–1700, gap at x1720–1820 over the shaft), ax5 zone (1385,349)-(1505,553), ax10@2176 y468, ax11@2213 y482, ax44 crossing poles x2383/2432/2479 y657 (grab binds g.a). Any teleport >~150 px vertically fails on `al > camY+240` before cam lerps — probe these only via real traversal.
- ⚠️ ax14 NOT a contact hazard — player stands at x2163–2169 alive with ax14s at 2180+. Earlier "x2147 kill" was the pit line.
- Collision map for the region (20 px cells): x1400–1580 tower solid y760–960 (its top at y760); shaft interior x1580–1900 open y800–960 floored at y960; pocket below the west ledge x1200–1440 is open to VOID at r53+ — drop = death.
- Collision values: 20=solid, 5=one-way platform top, 0/255=void, 21 seen at (1720,520-540) = unknown cell type in the upper shaft.

## jdb on-device inspection/injection (debuggable debug APK)
```
PID=$(adb shell "ps -A | grep acrebuild | awk '{print \$2}'" | tr -d '\r')
adb forward tcp:8888 jdwp:$PID
{ echo 'stop in com.acrebuild.gdx.Level0Game.render'; sleep 5;
  echo 'dump this.world';            # all world fields labeled
  echo 'eval this.world.pad.e(131072, false)';   # inject M_CYCLE (Kotlin default args → pass both)
  echo 'set this.world.player.ak = 7550';        # mutate fields (teleports — move camX/camY too: al>camY+240 = instant fail)
  sleep 1; echo 'clear com.acrebuild.gdx.Level0Game.render'; echo 'resume';
} | timeout 30 jdb -attach localhost:8888
```
Notes: the breakpoint auto-selects GLThread with `this` in scope; `dump` output is reliable (single `print`/`eval` results get async-echo-scrambled — use `dump` or read one eval per session). `jdb` thread ids by number often fail — breakpoint selection avoids the issue. Useful fields: `world.jC` (screen id), `jG`, `kBv`, `dlgU/dlgV/dlgW`, `player.ak/al/S/x1/g/av`, `pad.bB/bC`, `kC.cd[]` (script flags), `camX/camY`.

## State machine quick map (jC) — verified flow @10a200ec
23 sound-prompt (left-footer M_PAUSE →18) · 18 title (strip tap →2) · 2 menu (3 rows: row0 "LEVEL"=level-0 pack at (93,55,214,35), row1, row2 left-bottom) · 30 mission browser (af(); row-tap fixed per slice-165) · 19 mission select · 20 story/briefing (NEXT=right-footer M_CYCLE, SKIP=left-footer M_PAUSE — mid-screen taps dead) · 9 load card "ROME/COLOSSEUM/KILL WOLFMEN" (strip tap once jG>164 →21) · 21 dialog (dlgU=9 intro script → SKIP strip 349,198,56,47 → 8) · 8 play · 12/13 fail/win prompt (two-stage row taps at view y117–147 → display ~1200,580) · −1 dead ghost.

## Logcat
Tag `AcLevel0`: `level0: N records … npcs=M` on boot; `audio: play track=N` on z() calls (23=UI beep, 30=back, 0=softkey, 5=mission music). No npc FSM logging in stock builds.

## Run-3 additions (@9ad6b723, slice 210)
- **ANR / jdb suspended-thread residue (BIG time-sink):** if a jdb session exits without a completed `resume` (or `quit` races it), ART threads stay suspended → main thread wedges inside `dispatchVsync→CheckJNI WaitHoldingLocks` → ANR dialog steals focus and eats ALL touch input (looks exactly like a dead input pipeline — `lastMoveX=-1`, `pointerDown=false`, `pad.eL=0` during real holds). Diagnose via `dumpsys window|grep mCurrentFocus` (shows `Application Not Responding:` window) + `/data/anr/anr_*` trace (needs `adb root` on emulator). Fix: attach jdb + `resume` (sometimes unrecoverable → `am force-stop`+relaunch). ANRs also fire on boot under load with NO debugger — always check focus before trusting a dead tap.
- **`set` is a raw jdb command, NOT an eval** — `eval set x=y` returns null. Scripts must send `set this.world.npcs.get(i).ak = V` unwrapped (see /tmp/tp.sh, /tmp/npcmv.sh patterns). Player `ak`/`al` sets stick; npc positions get rewritten by their FSM next tick (a set may not persist — re-check after).
- **aO/aU/aT are collision-CELL probes** (values = cell types: 0=clear, 5=one-way '5'/'05', 20=solid) — not timers/countdowns.
- **'5'/'05' one-way platforms are hang-only from below in this build:** pure vertical jumps under them catch the lip (S37/38) — no pass-through; UP-vault (u16388) refused wherever aO=5; lips shimmy only via cell5-bit6 holds (up-right bit3 does nothing); corners don't roll up. Corridor y960 under '5' = one-way trap (entry into upper route is from above — balcony/checkpoint drop).
- **cell7 bottom-center dead sliver:** view x≤36 && y≥207 is a soft-key exclusion zone → the down cell is only live at view x∈[60,72] (display ~664,940).
- **Run latches hard:** ≥120 ms holds move ~100–580 px — can't stop precisely; stage positions with `set ak`.
- **Guard chase range is long:** corridor ax11@2172 pursued ~400 px west to x1760 — relocate it (`/tmp/npcmv.sh 106 2600 959`) before staging shaft tests, and remember respawned-health is x1=30 (≈2 hits).
- **screenrecord auto-caps at 180 s** (default limit) even without --time-limit — fine, but a mid-recording ANR dialog burns in as a system overlay: keep jdb sessions short during takes.
- jC=12 pit-fail variant: falling into an unvisited pit (west of crates ~x1585, y1029+) returned to **jC=2 menu** — a full mission reset, not checkpoint reload.

## Run-5 additions (@9584967a, slice 213) — ax22 zones LIVE + claimed-proc anim freeze
- **ax22 zones work end-to-end** (post-slice-213): run/jump into the W rect → S65 snap to anchor; a directional-hold EDGE vaults out — each zone's exit follows the HELD direction (up-right→east vault, up-left→west vault; the B-lift is a zigzag 335→336 W, →337 E). The vault needs a FRESH edge (`pad.v` per-capture — re-press after each snap; a carried-over hold doesn't re-fire). Landing on a ledge auto-grabs S203 (shared ledge-hang) → another fresh directional edge climbs up (S62). Verified: strip-top→zone(2064,695)→(1975,605)→(2104,546)→roof-B top (2200,479) → east run past x2340 → checkpoint ax2 (2594,485) armed → KO respawn at x2580.
- **Checkpoint respawn**: KO → restart prompt → row tap → respawn AT the last armed checkpoint (x2580), x1=30 — not a full mission reload (pit-falls to jC=2 ARE full resets though).
- **BUG — claimed-proc entities never advanceAnim**: `Level0World.tickNpc` runs `n.b=true` + dispatch + `if(claimed) defaultArm`; `defaultArm` (NpcFsm.kt:869) lacks `advanceAnim` — the original I() preamble runs `s()` for EVERY entity at i.java:3872-3874. Result: ax7/ax46 (and likely ax13/ax44/ax66/…) hold states that wait on `animFinished()`/`r()` deadlock forever — ax7 npcs[84] swallows the vault apex and pins the player eternally (S313); ax46 npcs[85/86] S327/328 T=0 pin via S330. Guards unaffected (soldier tail advances at NpcFsm.kt:301). Symptom probe: `e.S` stuck + `e.T==0`/`e.U==0` frozen while `e.P/e.V` clear.
- **`adb input` silently dies post-ANR** even after the dialog is dismissed (input dispatcher keeps injected events blocked): `kCj/kCk`/`lastTouchX/Y` stay −1 during real holds. REAL mouse input through the emulator window still works — calibrate window→view mapping (this session's shrunken window: `wx=110+viewX·1.087`, `wy=33+viewY·1.102`; game view is the 400×240 space). Use computer-tool `left_mouse_down/up` for holds.
- **Teleport wedging**: setting `ak/al` inside SOLID geometry (aO≥12 && aR≥12, i.e. head+below-feet cells both solid) forces S79 every tick — a frozen "embedded" pose that ignores input. Teleports must land in open air above a real floor (aR=5/20 after settle). Also `set S`/`set T` get overwritten by the FSM next tick — set position LAST.
- jC=14 pause menu: rows are LEVEL-n selects; RESUME is a menu item — easiest harness exit is `set jC=8`.

## Run-4 additions — entity/zone introspection
- **Zone rect = entity `W` (int[4])** — populated for working zones (ax10 carrier `npcs[151].W=[1734,600,1770,679]` — the VERIFIED capture rect) but **all 10 ax22 hopscotch zones load W=[0,0,0,0]** (X/Y too) at npcs[344-353] → they never capture; position/params (ak/al/Z) load fine. Compare W against a known-good zone before blaming input. [FIXED by slice 213 — see Run-5.]
- Finding ax types fast: `npcs` order is by level record — `ax74` wisps ~200-278, `ax14` ~279-329, `ax67` breadcrumbs ~330-380, **`ax22` zones at 344-353**, `ax10` carriers ~151-165, `ax11` guards ~103-110, `ax13` ~166-169, `ax5` ~170+. Indices SHIFT between sessions — re-scan `.ax` before referencing.
- `dump`/`set` are raw jdb commands — send them unwrapped (never `eval dump`/`eval set`, both → null). `/tmp/jraw.sh` sends raw commands; `/tmp/jdbc2.sh` evals; `/tmp/npcmv.sh i ak al` repositions npcs (FSM may rewrite ak next tick — verify after).
- Teleporting ONTO a capture anchor (1214,636) gives ~1 tick of overlap — enough to disprove proximity-capture when nothing fires.
- `Entity` fields seen live: `W/X/Y`=rect int[4]s, `Z`=record params int[22], `CS/CT` are class-statics (not instance), `markerLx/Ly`, `v` (visible), `aL` linked entity.

## Run-6 additions (@f9b486d7, slice 214) — claimed-proc anim fix verified + landing wedge
- **Slice-214 preamble works**: claimed procs now get `advanceAnim` via `tickNpc`'s preamble — ax7 npcs[84] S1 swallowed the player then RELEASED (`animFinished()` fired, `e.S`→0, `flingAirborne(0)`+`ag=+2048`); ax46 npcs[85/86] T/U advance (T=0→2 over ~3 s reads — take two reads apart to distinguish "cycling through 0" from "frozen at 0").
- **ax7 throw landing = deterministic wedge** (2/2 identical): from mouth-center (1326,464) `ag=+2048` `ah=0` arcs east and lands embedded at **(1470,499)** — straddling the wall's top-east corner (solid cols 70-73 rows 22-25, open x1480+ rows 22-26, lower structure row 30). `aO=20 && aR=20` → forced S79 input-immune; `bd=false` (embedded-resolve never arms). Same S79 signature as teleport-into-solid, but reached by pure flight physics — flag: next provenance question is whether the original's resolve ejects him (wall top y440 vs x1480+ gap continuation).
- **`adb input` returns on a FRESH app process** — the post-ANR input-dispatcher block is per-process (relaunch restores injected events; no need for the mouse-calibration fallback unless ANR recurs).
- Entity scan this launch (pid 20172): ax7 npcs[84]@(1397,506) W=[1318,456,1334,472]; also npcs[90]@(4801,674), npcs[91]@(10125,499); ax46 npcs[85]@(2389,386) + npcs[86]@(1067,650); ax22 at 335-344.
- Deterministic test recipe: `/tmp/jtp5.sh 0 1214 620 1000 560` (teleport onto zone-338 anchor → S65) → `input swipe 760 632 760 632 900` (cell2 mask8 up-right hold → vault → swallow → throw). Read `npcs.get(84).S/T` + `player.ak/al/S/ag` after ~2 s.

## Run-7 additions (@31289deb, slice 222+223) — render-path regression
- **Direct letterbox render (no FBO) verified identical**: boot→menus→dialog→gameplay all render correctly post-FBO-removal; scissor-clipped panels (intro dialog text area jC21, pause-menu scrollable list jC14) clip at their bounds correctly.
- **fps measurement without jdb**: game tick counter `world.jG` over a jdb-free window is the fps measure — read jG, `sleep 15`, read jG again, divide by the free window (≈sleep + ~2.5s bp-wait). ~15.5 ticks/s = the 62ms/16fps baseline. gfxinfo + SurfaceFlinger `--latency` return nothing useful on this emulator's GL SurfaceView.
- **jdb corrupts fps timing**: each jdbc2.sh `stop in render()` suspends the whole game — never compute rates across jdb calls; measure between attach-detach gaps only.
- **jC map**: 23=splash(attract carousel), 18=title art, 2=level-select rows, 9=briefing card(jG>164 ready), 21=intro dialog(dlgU=9), 8=gameplay, 14=pause LEVEL-list (blue back-arrow ~display(1912,936) resumes to jC8).
- **Rotation**: `adb emu sensor set acceleration 9.8:0:0` (host-side `adb emu`, not shell `emu`) + `accelerometer_rotation=1`; display may report ROTATION_0 while the SurfaceView runs landscape — trust screencap aspect, not dumpsys rotation.
- **screenrecord launch**: `adb shell 'screenrecord ... &'` from an exec call can get killed when the shell exits — verify file growth (`ls -la` twice) before trusting it's recording.

## Run-8 additions (@8b2559cd, slices 217–233)

- **Ledge arms live but geometry-gated**: `ledgeLipGrab` fires only when a
  lip cell ≥19 ('5'=5 fails) sits at head+10 during airborne, plus open
  cells above/inside — level0's reachable walls are thick so spontaneous
  falls don't grab. `ledgeDrop257` needs aQ∈{20,5} (shifted support
  probe), aR=0 below (thin platform), open 2-cells-out 1-row-below in
  the facing dir + fresh DOWN edge. Positive on-device fire needs the
  exact scan coords the unit tests compute (Slice1Test `fall brushing a
  wall lip…`, `down at a thin platform edge…`) — ask for/log those.
- **jdb during screenrecord freezes video**: every `stop in render` +
  eval holds the game ~7s; screenrecord captures the static frame —
  budget probe loops inside a take, or expect dead stills.
- **`screenrecord --time-limit` silently caps** (~170s); `ls -la` the file
  to detect early death, re-launch for multi-segment runs.
- **Teleport into solid geometry → forced S79 wedge** (input-immune);
  keep staging teleports in open air above platforms.
- KO→YES tap respawns at the armed checkpoint (strip = (1593,799));
  corridor patrol is a hard gate at x1=30 if it leashes.

## Run-9 additions — firing the ledge arms on-device (the full recipe)

- **camY kill-line trap**: teleporting with camY near the target puts a
  silent out-of-bounds plane at `al > camY + ~240` — falls die mid-window.
  Set camY ~200+ px BELOW the fall's deepest point (e.g. camY=200 for a
  fall ending ~y430), not near the lip.
- **Fire a fall-catch deterministically**: `set S=43,T=0,av=false,
  ak=3965,al=180,ag=0,ah=0,N/O matching, camX=3700, camY=200` then let the
  game tick — `ledgeHangGrab` fires at the thin lip (200,9) and snaps to
  S61@(4000,179). The i4 window is razor-thin (~2-3 ticks; per-frame box
  shapes + a ~+23px east drift move W[2] between the hang window
  [3960,3979] and lip window [3995,4014]) — expect ~1-in-3 catches; retry.
- **`Q==61` poisons ledgeLipGrab** across reloads (respawn restores fields,
  doesn't setAnim → Q stays latched; same-state setAnim(43) is a no-op).
  After any forced S61, reset with a live transition or `set player.Q=0`.
- **bp-verify the arm**: `stop at com.acrebuild.core.PlayerFsm:2059` hits
  every fall tick at the consumer (`ct` + corner-9 gate readable via
  `eval p.ct/aO/aP/aQ`); `stop in com.acrebuild.core.Entity.probeCells`
  confirms physics ticks; method-bps on the grab functions themselves
  work but the consumer line is more informative.
- **W[] is stale in frozen states** (KO/menu) — it refreshes via
  probeCells only when the world ticks; don't trust W reads at jC!=8.
- **S257 recipe**: stand ON the thin platform's edge cell (support aR
  solid 20, row below open), face TOWARD the open side (the vault drops
  in the facing direction — west edge needs av=true), DOWN tap → vault
  drop. ~40px horizontal + ~100px down displacement signature.
- **jdb `set` runs only with a suspended thread** — "No current thread"
  means your bp never hit; commands were swallowed silently.
- Getters need parens: `world.bh3` → `world.getBh3()` (Kotlin val).

## Run-10 additions — ax22 hopscotch input model + mouse fallback

- **Launcher class** is now `com.acrebuild.spike/.AndroidLauncher`
  (package restructure ~slice 240s; `am start -n com.acrebuild.spike/.AndroidLauncher`).
- **adb input can die mid-session** without an ANR (seen after a jdb
  suspend cycle): kCj/kCk/lastTouch freeze at −1, taps do nothing.
  Fallback = mouse through the emulator window — clicks AND holds work
  (`mouse_move` then `left_mouse_down`, hold, `left_mouse_up`).
- **Window→view calibration** (device 2400×1080 → emulator window box
  ~(110,28)–(655,310)): `viewX = (windowX − 108)/1.1`,
  `viewY = (windowY − 35)/1.1`. Empirically verified — lastMoveX/Y is
  ground truth (click → read them → solves the map).
- **TWO pad layouts** (`resolvePadZone`, Level0World.kt:4240):
  - `mounted` (S65 zone-capture, S203/S61 hangs, rides): FIXED wheel box
    view(−5..111 × 124..240), inner split x[33,72] y[162,201] —
    cell1-up=view(~60,140), cell5-right=view(~95,180).
  - not mounted: player-relative 3×3 — x±25px, head−10..feet+10.
  - Returns −1 outside the active box → touch updates lastMove only,
    bC/bB stays 0 (diagnostic signature).
- **ax22 vault semantics** (NpcFsm.tickZoneInteract): capture = overlap→
  S65 snap; `padHeld(16390)` (up|TL edge) → WEST vault when Z[2]==0;
  `padHeld(16396)` (up|TR edge) → EAST vault when Z[2]!=0; down edge
  (33024) → drop-through when Z[1]!=0. All are EDGE-word reads (bB) —
  holds work because the pipeline calls E() per frame while down.
- **S203/S61 exits**: up/toward-wall edge → S62 climb; down edge → release.
- **Chain verified** (290d62c2): (2064,695)→W→(1982,605)→E→(2104,546)→
  S203@(2200,479)→climb→S0@(2210,479) roof-B → run→KO@(2940,499)→
  checkpoint respawn (2590,519) x1=30.
- **KO YES row-tap** ≈ view(175,122) (button hitbox lags the banner —
  retry once if it bounces).
- **screenrecord trap**: launching it BEFORE the app is landscape records
  the portrait launcher surface — start after the game is fullscreen.
- **S79 wedge** recurs on teleport-into-solid (aO>12&&aR>12) — stage drops
  ~80px above ground, not inside geometry.
