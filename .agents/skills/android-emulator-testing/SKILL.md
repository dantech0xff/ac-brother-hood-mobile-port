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

## Run-11 additions — screenrecord finalize + input flakiness

- **Never pull an mp4 while screenrecord is still writing** — the moov
  atom lands at finalize; early pulls yield unplayable files (lost a full
  take this way). Check `ls -la` twice — pull only after growth stops.
- **adb input is flaky per-process**, not just post-ANR: it worked ~3min
  on a fresh process then froze with no ANR. Diagnose via `lastMoveX/Y`
  staying −1; switch to mouse input without restarting.
- Slice 310: KO→YES gave **full level reload** in the observed cases
  (checkpoint respawn not seen this run — may need the checkpoint armed
  first, or semantics changed).
- The B-lift chain is the reliable showcase: jtp5 to (2050,660) → S65 →
  fixed-pad up cell (window ~174,189) taps ×4 → roof-B → run east → KO.

## Run-12 additions — mission-complete + pad injection + m1 crash

- **Mission-0 win** = claim-script uid 116 (1 block: `op37 r12=1 →
  screenL(15)`). Showcase recipe: `set kAV.aG=116` →
  `eval kAV.bindContext(world)` → `set kAV.cd[1]=true` +
  `set kAV.cd[2]=true` → l142Tail ticks `runClaimScript` → jC=15.
- **Post-win routing**: stats `v(458784)` advance → `v(327712)` confirm →
  `kAj++`+`kBA[14]` persist → `stateL(30)` medal browse → context →
  `stateL(9)` next briefing. `M_CYCLE` back-out → `stateL(19/2)`.
- **Injecting pad edges via jdb**: `eval world.pad.e(mask,false)` — the
  real e() pipeline (eK|=mask → commit → bB). `set pad.bB` is useless —
  `commit()` rewrites bB from eK every frame.
- **`scripts.bin` decode** (see Report run-12 script): `by[s][b]` blocks,
  group key + op stream; `op37 r12` = runArgSub 26-case switch
  (1=win,2=fail,4/5=kAV arm,0=cd2 latch).
- **ax42 escape fuse**: kind-1 expiry → `l(13)`+`kBx=58` timeout-fail;
  collect (player∩W while aJ==2) just removes the fuse — NOT the win.
- 🔴 **mission-1 crashes on device**: `LevelPack.stampAt` unguarded
  negative `%` on parallax<0 → ArrayIndexOutOfBounds ~20s into m1
  gameplay. Reproduced clean ×2 — the m1+ missions can't currently run.

## Run-13 addition — m1 crash fix verified

- Slice-311 `stampAt` fix verified on-device: negative parallax
  (parallaxX=-1, parallaxY=-3015 live) no longer crashes — m1 runs 90s+
  through multiple KO→restart→glider-respawn cycles. Mission-1 content:
  canyon + wolf horde + wisps + ax25 glider respawn. The CONTINUE row's
  right cell reaches jC=30 browse; M_CONTEXT there → briefing → m1.

## Run-14 additions — mission unlock + per-mission smoke

- **Unlock all**: `set world.kBA[14]=7` then `set kDa=8` at jC=19 (the
  select's row bound). Select = `set kBw=N` (0..7) → `pad.e(327712)` →
  jC=30 browse → `pad.e(327712)` → briefing → tap → gameplay.
- **Mission map**: kAj 0-7 = L1 ROME / L2 ROME-escape / L3 FLORENCE /
  L4 FLORENCE / L5 ROME / L6 VENICE / L7 PANTHEON / L8 COLOSSEUM-boss.
- **First-load wedge**: missions 3/5/6 spawn S79-embedded on the first
  load (insta-fail); the KO→YES reload lands a proper spawn. m5's
  wedge can soft-lock (S79 above the kill-line → no fail fires).
- All 8 packs render crash-free @46329a09 — no other OOB signature.

### Run-15 — mission-entry spawn bug (menuJc9 skips resetPlayerToSpawn)

- **jC9 briefing → gameplay does NOT reposition the player** —
  `menuJc9` (Level0World.kt ~:3606) runs `spawnEntities(); postSpawn()`
  at `jG==164` but omits `resetPlayerToSpawn()`. The player keeps his
  previous coords: on a fresh boot that's level0's record spawn
  **(85,940)**; on sequential mission switches it's the previous
  mission's end position. Missions where (85,940) has no floor
  (m3, m6) insta-fail at (85,1399); where a floor exists (m5) he lands
  and plays. `statsReset()` is skipped too — counters carry over.
- KO→YES reload goes through `reload()` → `resetPlayerToSpawn()` →
  lands the pack's own `playerSpawn()` record (verified m3→21,699 /
  m6→17,740).
- ACLV packs are **little-endian** (PackReader): `ACLV` + ver u8 +
  cols/rows u16 + cellpx u8 + nLayers u8 + per-layer
  (id u8, tileset u16, hasFlags u8, lw/lh u16, cells, flags) +
  entCount u16 + records (len u16 + i16 fields). `playerSpawn` = first
  record type 0/25, coords at rec[2]/rec[3].
- Restart-prompt YES via jdb: `pad.e(327712,false)` — sometimes needs
  two edges (first may land during the banner transition).

### Run-16 — slice-312 fix verified

- menuJc9 now calls `resetPlayerToSpawn()` + clears checkpointSnap on
  briefing entry — all missions land their pack record on clean entry.
  Re-verify recipe: fresh boot per mission (carryover positions pollute
  sequential entries), `stateL(19)` + `kDa=8` + `kBw=N`, two
  `pad.e(327712)` edges for select→browse→briefing.

### Run-17 — m2-m7 play-through @ slice 314 + jdb bp-context evals

- **jdb `this` context**: `stop in com.acrebuild.core.Level0World.tick` +
  `run` + sleep → bp fires on GLThread → `this` = the world instance —
  eval `this.jC`, `this.player.ak`, `this.pad.e(65568,false)` etc.
  Thread NAME "GLThread 42" has a space → jdb `thread`/`suspend` can't
  select it; the bp route is the reliable eval path. Ref ids in
  `threads` output shift as hwuiTask threads respawn — don't trust them.
- New-box note: `/tmp/jdbc2.sh`/`jtp5.sh` helpers don't survive reboots —
  rebuild them (bp-context pattern in this file's Run-16/17 section).
- Emulator fresh-boot ritual: `emulator -avd spike -gpu swiftshader_indirect`,
  `adb wait-for-device`, then the WINDOW stays portrait until the toolbar
  rotate button (display is already ROTATION_90 — just click rotate).
- m4 flying mission: glider descends into a soldier horde on the bank —
  restart banner = legit gauntlet end, not a wedge.
- Briefing "TOUCH THE SCREEN" dismiss = `pointerStrip()` — real tap only
  (pad.e masks don't dismiss it).
- Multi-eval jdb reads race/interleave — take single-field reads or grep
  the last ` = N` per field; identical values across fields = leaked.

### Run-18 — headwt demo @ slice 315

- Worktree builds: `/tmp/headwt/rewrite` is the devin/land checkout —
  build from there when the lead says "worktree"; same package, install -r.
- jC=21 intro dialog: pad edges do NOT dismiss (dlgU=9; SKIP is the
  pointer pill at view 349,198,56,47). The "hint cards" are jC=9's own
  pages — advance via `pad.e(65568)` (M_CONTEXT); after the last one it
  enters jC=8. `pad.e(327712)` inside gameplay opens PAUSE (jC=14);
  `pad.e(131072)` (M_CYCLE) backs out.
- Restart-prompt YES: two `pad.e(327712)` edges, ~2s apart.
- NEW GAME path: level-select row-1 → EASY → EZIO card (confirm via
  pad.e(327712), the arrows are back/browse) → story card → briefing.
- Prop smash: attack radial works on vases — orb drops + score bump.

## Run-19 — desktop LWJGL3 jdb + the m0 claim/bubble site
- Desktop session: `cd /tmp/headwt/rewrite && ./gradlew :lwjgl3:run` —
  window "AC Rewrite Spike (dev)", JDWP **5005** (suspend=n). Breakpoint
  thread is "main" (not GLThread — desktop runs the world tick on main).
- jdb fd lifetime: `exec 3>fifo` dies with the exec call — the WHOLE bp→
  set→run sequence must live inside ONE exec call; poll jfoT for
  'Breakpoint hit'/'will be instance'. A second writer can inject into
  the same fifo while the first session holds fd3 open.
- Field watchpoints work: `watch com.acrebuild.core.Level0World.bubbleDraw`
  → suspends at setBubbleDraw() bci=2 BEFORE the store; `next` steps into
  `NpcFsmKt.tickBubble` (static — `this` dies, use locals `w`/`e`/`q`).
  `w.bubbleDraw.x` etc readable there; `q` = the entity's cQ int[10].
- m0 claim site (ax5 uid234, rec f=[5,234,3650,721,0,8,0,0,1,327,0,0,
  24,150,...]): W strip = pos+(f12,f13) = x3650-3674 y721-871 INSIDE the
  balcony shaft. Natural reach = fall off the balcony west edge; for
  verification just teleport the player to (3662,790) — claim fires.
  Fires script 327 → camera pan + Altaïr-vs-guard + SKIP strip; the
  player DIES right after (real melee, input-suspended during claim).
- jC=12 YES banner on desktop: mouse taps at the YES row work;
  pad.e(327712) worked once then went dead — prefer mouse; two taps
  ~1s apart. jC=21 intro card needs the SKIP strip tap (848,565), not
  pad edges.

## Run-20 — the jdb suspend-leak + emulator tap recipe
- **jdwp forward binds to a PID — after force-stop/relaunch re-run
  `adb forward tcp:8888 jdwp:<new pid>` or jdb attaches to a dead
  endpoint** ("Nothing suspended"/evals all null).
- **Suspend-leak**: every bp-hit suspends GLThread; if the jdb session
  dies mid-suspend the count PERSISTS in the VM (thread shows 'running'
  in `threads` but never ticks — bp never hits, taps queue forever,
  screen static). Fix inside ONE session: `threads` →
  `resume <GLThread-id>` ×4-6 → then `stop in` works again. Or
  force-stop. Keep every suspended window <2s — >5s triggers ANR
  "isn't responding" (shows on screenrecord — user-forbidden overlay).
- One-shot edge pattern that avoids the leak: bp→eval→clear→run all
  in one <2s window (`/tmp/edge.sh`).
- **Real taps DO work** once the thread is free — `input tap` on menu
  rows, `input swipe x y x y <ms>` as a hold for the D-pad
  (right=view~97,185; up=~77,118; attack btn=view~335,185;
  SKIP pill=view 349,198→dev 1870,891). Device coords = view*4.5 + x300.
- Long teleports kill via camera-lag OOB — hop ~350px per jdb set.

## Run-21 — m2 Florence combat (pad/menu notes)

- **jC=12 restart-confirm**: `pad.e(327712)` edges often get eaten —
  the first jC==12 frame after a level load, a checkpoint reload or an
  input lock is skipped (`jT!=0 → jT=0`, slice 376), and the confirm
  arm is `pad.v(M_CONTEXT)||rowTap` then kBw arming.
  Two edges usually work, but when pointer+edges are flaky, call the
  dispatcher directly: `eval this.menuItem(14)` (14=YES row id, 15=NO)
  in bp-context → `reloadCheckpoint(true)` immediately (jC 12→8).
- **Pointer dead during jC=12**: taps DO work when alive — YES row is
  dev ~(1025,590) on the banner. But pointer flakiness is per-process.
- **m2 fightable soldier**: spawn-adjacent guard ~x946 wall-clings —
  he perches on the vine strip cycling top/mid, never grounds for
  melee. The ROOF soldier at ~(968,1876) DOES fight: hop-teleport
  (753,1925)→(880,1900)→(968,1876), then real attack taps. Fight
  gives slash arcs, overhead health bar, orb drops, real damage both
  ways — soldier counters kill if you just spam.
- **Checkpoint-snap hazard**: crossing the floating C ring mid-roof
  apparently poisoned the respawn — later reloads insta-died back to
  jC=12. If a reload loop appears, suspect a bad checkpoint snap.
- **MenuItem row ids** (Level0World.kt :3140+ dispatch): 11=RESUME,
  12=RESTART, 14=YES, 15=NO — callable via jdb eval as a last resort
  when the edge chain won't confirm.

## Run-22 — bdf117d3 demo (jC=20 exit, combat notes)

- **jC=20 story intro exits via M_CYCLE (131072)**, or M_PAUSE once
  kCu==5 (the scrollPanel stage). Taps/65568 only rewind the
  typewriter — the intro loops otherwise and eats recording time.
- **jC=21 dlgU=9 SKIP pill**: dev(1870,891), pointer-only, two taps.
- **Roof terrace soldier (m0 ~x2150,y~770)**: clinch melee is lethal —
  he wins every blind attack-spam exchange (blocks/counters). x1=90
  health set still lost. For a scripted kill shot, pre-stage health or
  accept honest losses. He never leaves his ledge lip — S25 ledge-fall
  can't be triggered by push combat.
- **Restart replays jC=21 intro dialog** — expected; SKIP it again.
- **YES at jC=12**: `menuItem(14)` in bp-context (Run-21 note) is more
  reliable than taps/edges when pointer is flaky.
- Take budget: jC=20 auto-play + menu edges consumed ~100s of take 1 —
  start the gameplay take BEFORE the briefing completes.

## Run-23 — ax5 claim trigger without travel (entity-move trick)

- **CORRECTED (Run-24)**: the ax5's `W` rect is computed ONCE in
  `initMissionLogic` (`e.W[0]=e.ak; e.W[2]=W[0]+rf(12)` — NpcFsm.kt:2942)
  and is NOT re-derived per tick. The full working recipe needs BOTH:
  `ak`/`al` move the entity into the tick-active window (its
  `tickMissionLogic` is gated by position — at x3650 it never ticks near
  spawn, so eventBind never runs), AND `W[0..3]` provides the overlap
  rect `eventBind` checks. Set all six in one suspend:
  `ak=75, al=880, W=[70,880,110,1000]` over the spawn player (80,940) →
  `run` → claim fires within ~1s → script 327 stages + cQ bubble plays.
  Single-field scripts race the suspend landing — retry or send a
  `print` first so later `set`s land inside the suspended window.
- The m0 street is NOT continuous: a waterfall/canal kill-strip runs
  ~x3400-3700 at street level (y~1095) — teleport hops along the road
  die there. Approaching the balcony W zone needs the roofline or the
  entity-move trick.
- Claim re-arm is per-LOAD not per-reload: after the claim has played
  once, `reloadCheckpoint(true)` may restore a post-claim snap where
  the entity's zone is already spent — force a fresh mission entry
  (pm clear or level-select) to re-fire.
- Field races on multi-field jdb sets: `set e.X = v` lines inside one
  suspend can still race ("Thread not suspended" → value=null) —
  verify each field after (print), retry misses.

## Run-25 — pure-input menu navigation (no-jdb) + demo recording

- **Input bridge is touch-only** — `input keyevent` never reaches the game
  (no key handler in `Level0InputBridge`); keyevents 61/96/108 hit Android
  (task-switch/launcher). Never use keyevents to control the game.
- **jC=23 YES/NO sound prompt**: row taps do NOT confirm — tap the LEFT
  footer soft-key (pause-icon zone ~dev(490,940)) → posts M_PAUSE which IS
  a 327712 confirm → `stateL(18)`. (menuQ row-tap path exists but this
  screen's case-23 confirm is pad-edge-driven.)
- **Menu screens**: row taps work — the tap's UP position is the row hit
  (`lastTouchX/Y` = release point, one frame). Quick `input tap` is fine —
  DOWN+UP batch in one tick.
- **Story intro jC=20**: exits via the ↩ back-arrow footer icon
  (~dev1900,940) = M_CYCLE edge — works at any page, skips the auto-play.
- **Real view→device map**: renderer `sc=min(sw/400,sh/240)` integer math
  → scale=4, offsets (400,60) on 2400x1080 → devX=400+lx*4, devY=60+ly*4.
  (The old 4.5× note was wrong.)
- **Pointer death is per-process**: taps landing on a dead pipe produce
  zero screencap diff — verify pointer life with a probe tap+diff before
  committing a recording take; force-stop+relaunch fixes.
- **screenrecord encode lag on swiftshader**: menu/loading sections can
  stretch ~2.5-3× vs wall-clock (encoder falls behind); gameplay runs
  ~realtime. For store clips, cut the slow menu segments or re-take.
- Menu tap coords (2400x1080): NEW GAME (1240,543), EASY (1049,498),
  EZIO card (1240,345), jC=14 RESTART row then YES (1200,580).

## Run-26 — save/resume verification recipe
- **Persisted save**: `kBA` IntArray(160) → `world.saveFlush()` writes
  little-endian shorts via `Command.PersistBA` → `SaveBridge`
  `asbr-save.bin` (320B) in app files; `world.saveLoad()` runs on boot.
  jdb-inject: `set this.kBA[14] = N` then `print this.saveFlush()`
  (jdb has NO `call` verb — `print <method>()>` invokes it).
- **jC=2 row hit-test ≠ button art**: row0's rect starts at panel.y+10
  (view y55) — a tap on the visible button TOP (view ~y41) misses.
  Rows: CONTINUE dev(1200,350); row1 dev(1200,544); NEW GAME(row2)
  dev(1600,340) — col-2 rows start at x206 view.
- **Real KO without combat**: jdb-set `player.al` deep (e.g. 1400) →
  falls → cam-lag OOB → jC=12; YES tap dev(1200,580) respawns.
- **Checkpoint verify**: `checkpoints.elementData[i]` (aw,ak,al);
  `kBA[16]`=aw of last-fired; `checkpointSnap` Snapshot(ak,al,x1,gJ,gI)
  is the respawn basis — restore is exact (1600,579 observed).
- Verify dump of a persisted int: `run-as PKG od -A d -t u2 -j <2*i> -N 4
  files/asbr-save.bin` (index i → byte offset 2i).

## Run-27 — rare menus + hit-zone map
- Pause HUD button: top-right "II" → view(354,0,46,37) → dev(1905,138).
- Pause menu rows (panel 93,30): RESUME 280 / RESTART 412 / OPTIONS 544 /
  HELP 676 / MAIN MENU 808 / EXIT 940 (dev y, x=1200).
- MAIN MENU rows dev: CONTINUE(1200,350) / NEW-GAME-row1(1200,544) /
  SELECT-LEVEL-row2(1600,340). YES/NO confirm rows (jC=28): YES
  (1200,640), NO (1650,640).
- OPTIONS page is ONE column of 8 at y96+33n (jc14) — rows ≥5 render
  below the 240 canvas; input clamps to 239 → untappable (defect).
- jC=3/jC=6/jC=15/jC=22(options-mode) are touch dead-ends — no footer
  zone emits M_CYCLE/fire; jdb `print this.stateL(N)` escapes.
- jC=4 difficulty chevrons: view x110-160/240-290, y15-95 → dev
  (950/1450, 300). jC=5 page chevrons: view y=iK±15≈122 → dev(680/1720,560).
- Score slots are BYTE pairs: scoreAt(i)=kBA[i]&255 | kBA[i+1]&255<<8 —
  stamp high-byte in i+1 or values >255 truncate. jdb: `set this.kBA[81]=210`
  + `set this.kBA[82]=4` shows 1234.
- Medal viewer = jC=22 (kCc-driven, not kBA directly): options-mode needs
  kEx==3 + kCc[i]==2 for lit rows; win-mode shows kCc[i]==1 then taps
  through to jC=15 stats (itself trapped).

### Run-28 notes (slice-328 verify)
- jdb: `internal` Kotlin members aren't callable by name (name-mangled; even `foo$main` failed) — use the public surface (`set this.kBA[i]`, `print this.saveFlush()`) and verify semantics via file dump + `scoreAt(i)`.
- Options drag-scroll: `input swipe 1200 900 1200 500 600` scrolls the bv4 8-row page; the scroll-offset-aware hit-test means row y depends on scroll amount — screenshot first, compute row centers from the image (~preview_y/706*1080), then tap.
- jC=22 has TWO modes: kEx==3 → options-mode (BACK pill, M_CYCLE exit); kEx!=3 → win-mode viewer (tap-anywhere → next state). From pause-options you get win-mode (kEx=0) — tap advances to jC=15.
- jC=24 credits exit lands on jC=6 ABOUT (chain, not a bug).

### Run-29 notes (slice-329 audio verify)
- jC=21 dialog dismiss depends on dlgU: full-screen u∈{0,4,5,7} advance on ANY tap (pointerStrip→M_CONTEXT); script dialogs u==9 need the skip gate `v(131072)` = the ↩ M_CYCLE footer zone (dev ~1900,940) — body taps do nothing.
- `lastTouchX/Y == -1` between ticks is NORMAL (consume() resets each tick) — it is not proof of dead input; verify via a dispatch side-effect instead (jC/field change).
- Stale jdb: an attached session left running holds the VM suspended → Android ANR dialog in ~2min. Before re-attaching always `pkill -f 'jd[b] -attach'` — note the bracket pattern: a literal `pkill -f jdb` matches your own shell's command line and kills itself (exit -1).
- audioPlay "one Player" gate: kBF&&kBE && audioTrack inside hA[track]ms → every new z() dropped. Menu blips (z(23)) are silent while menu music is fresh — wait ~hA ms (virtual tickIndex*62) then re-tap.
- Audio evidence: `logcat -s AcLevel0 AcSpike` → "audio: play track=N (e.e=N)" / "audio: play slot=N" / "audio: e.b() stop channel" / "audio: missing audio/" / "empty/unloaded". jdb reads: audioTrack, audioPlaying(), kBE (music<10), kBF (sfx>=10).
- jC=23: row-tap only focuses (kBw); commit is the footer-left zone (pad.v(327712)=M_PAUSE|M_CONTEXT). kBw=-1 default commit → neither arm (flags keep init defaults).

### Run-30 notes (task-41 real stats-arm persistence)
- Real score entry without a win: `stateL(15)` + `set this.kAp[i]` seeds — kAp[0]=kills (×kDH[kAu]={100,200,300}), kAp[3]=silent-kills (×kDI), kAp[1]=deaths (−300 cap 4), kAp[4]/[5]=bonus/time — i4 recomputes each proc → `statsScore`. Persist needs jG>10 (reveal elapsed or one `v(458784)` tap to skip), then the NEXT/footer tap writes `baShortPut(81+(kAu<<4)+(kAj<<1), i4)` + kBA[14]=kAj+1 + PersistBA in the same arm.
- File check: `run-as PKG od -A d -t u1 -j 162 -N 4 files/asbr-save.bin` → slot 81 pair at byte 162 (=2×81); LE-16 → lo,hi. Logcat "save: e(true) → 320B /ASBR" confirms PersistBA drain.
- jC=15 footer: NEXT pill dev ~(566,940) = the v(458784) edge; arm also reads v(327712) confirm → routes stateL(30) when kEgFlags set.

### Run-31 notes (PR-388 menu/dialog verify)
- `input tap` can be dead at KERNEL level — diagnose with `getevent -lt`: zero EV_ABS/EV_SYN lines = dead for the whole boot; don't keep retrying taps. Worked around with real mouse clicks via the computer tool on the emulator window (virtio multi-touch works immediately).
- Real-mouse calibration on a 3200×2400 display: `wmctrl -r "Android Emulator - klokk_aosp" -e 0,60,100,2700,1215`; game canvas ≈ (160,54)-(724,400) in 1024×768 space → `viewX=(sx-160)/1.41, viewY=(sy-54)/1.41`. `left_mouse_down` + `adb shell screencap` + `left_mouse_up` captures pressed states (pointerMoveIn → zD).
- jC=23 YES/NO: a mid-screen tap (pointerStrip → M_CONTEXT) confirms — no footer needed; the left "OK" pill is a 19px sliver at view x -5..14.
- jc19 8-row recipe via real UI: at jc2 jdb `set this.kBA[14]=7` → tap SELECT LEVEL → menuItem(3) computes kDa=8 → 8 rows. (Set kDa directly only when bypassing the menu.)
- jc3 (bv=4) is jdb-only: `print this.stateL(3)` + `print this.bannerK(4)` in the same suspend → 8 rows two-column on the (14,47,180) panel; no footer exit (verbatim dead-end) — `stateL(2)` to escape.
- Boot legal pages for screencaps: splash ~3s → legal-1 ~3s → legal-2 ~5s → jC=23; grab at launch+4s and +8s.
- jc21 dlgU=9 chain: jc9 briefing after jG>164 → strip tap → jc21; mid-screen taps advance pages (and complete the typewriter first). SKIP pill at view(349,198,56,47) → dev(1870,891) → jc8.

### Run-32 notes (PR-389 jc5 HELP verify)


## jc5 has TWO render arms selected by `kCy` (origin state), not by the screen itself
- `helpScreen()` picks `kCy == 14` → framed translucent panel + black "HELP" title bar
  over live gameplay (f(true)); otherwise → full-screen `drawFrame(97,1,0,0,0,0)`
  light-gradient backdrop (f(false)). Both draw palette-1 BLACK wrapped text.
- `stateL(i)` sets `kCy = old jC`, so the arm is decided by where you came from.
- The ONLY real-UI entry is pause menu (jc14) HELP row → kCy=14 framed variant.
  The else/backdrop arm is reachable in the harness ONLY via jdb:
  breakpoint `Level0World.tick` → `print this.menuItem(6)` while jC==2 (or any
  non-14 menu) → jC=5, kCy=2. Verified working.
- Original JAR also had a hidden jc2 HELP button at view (10,167,36,27)
  (`bw=0; l(5)`) — NOT ported; no equivalent exists in Level0World.kt.

## HELP (jc5) navigation geometry — menuG() + footerQ()
- Page model: `kBw` = page group (mod 4), `kCY` = current 8-line screen within
  the page; counter text `"${globalScreen}/${kCZ}"` at view (200,220) — for
  controls help kCZ=6 ("1/6" … "6/6").
- Left chevron hit zone: view rect (45, iK-15, 50, 30); right chevron:
  (305, iK-15, 50, 30), where `iK = menuGIK() = 47+(kEe-linesHeight(1))/2` —
  in practice just click the visible cyan chevron art (~view 70,120 / 330,120).
- Right chevron wraps: at last screen of a page it advances `kBw` (mod 4) and
  resets `kCY=1` — the counter can go past N/N? No: counter tracks global
  screen, reaching "6/6" then wrapping to the next page group's first screen.
- Exit: footer for jC=5 is `Pair("", d0(17))` — no left pill, BACK pill
  bottom-right zone `pointerDownIn(395-kCf-10, 198, kCf+20, 47)` (≈ view
  330-400,198-245 → dev x 1720-1960, y 852-1040). M_CYCLE → `kCY=1;
  stateL(kCy)` returns to the ORIGIN state (jc2 for backdrop arm, jc14 for
  framed arm). The BACK pill is faint/invisible on the backdrop arm — the hit
  zone is still armed; click the bottom-right corner anyway.

## Real-UI chain to pause→HELP (framed variant)
jc2 QUICK PLAY row0 → jc9 briefing card (wait ~11s for jG>164) → mid-screen
strip tap → jc21 dialog → SKIP pill (dev 1870,891) → jc8 gameplay → blue
pause icon top-right (dev ~1905,138) → jc14 → HELP row (4th of 6 rows,
dev ~1200,676) → jc5 cy=14. Quit-to-menu from jc14 pops a YES/NO confirm —
YES is the top pill.

## Input note (still true)
`adb input tap` can be kernel-dead across emulator reboots (zero events in
`getevent -lt`). Real mouse clicks via the computer tool on the emulator
window are the reliable fallback — all jc5 chevron/footer taps worked that
way on the first try this run.

## Run-33 notes (context): draw-vs-hit-test divergence trap
- When menu DRAW geometry changes, the world-side hit-test mirror must
  change in lockstep: renderer stacks rows in `menuPanel` (`i9 += i4`
  contiguous) while world computes tap rects in `menuRowRects()` /
  `menuScrollMax()`. Dropping gaps draw-side only drifted every row
  below row 0 (drawn y(n) vs hit y(n)+13+3n on jc2): top ~15px of each
  drawn row was DEAD, top ~18px of the next row committed the PREVIOUS
  row (SELECT LEVEL tap → NEW GAME).
- Test taps at row TOP/BOTTOM edges, not just centers — drift shows at
  edges first. Confirm which row committed by the LANDING screen, not
  the press highlight: pressed style uses draw-side `pointerMoveIn`
  (drawn geometry) so a row can LOOK pressed yet commit wrong.
- `pm clear` + `am start` changes the app PID — `adb forward
  tcp:8888 jdwp:$PID` goes stale, jdb silently hangs. Re-resolve PID +
  re-forward every relaunch.
- Footer pills (BACK/OK) are hit-tested by `softKeys()` zones, NOT
  `menuRowRects` — they keep working when row taps break; use them to
  recover between probes.
- jdb `set this.kBA[14] = 7` at jc2 then SELECT LEVEL → kDa=8 (two
  bands); jc2 row0 flips to CONTINUE (save marker) after the set —
  expected.
