---
report: baseline-audit
date: 2026-10-03
status: complete
head: 309c7987
author: claude
---

# Baseline status audit — `main` @ `309c7987` (2026-10-03)

Read-only audit that opened this plan. No repository file was changed and the
original JAR was never executed. **proven** = re-checked by hand against the
bytecode (`reconstructed-project/bytecode/*.javap.txt`) or the decompiled views
(`reconstructed-project/src/{structured,simple}/`); **inferred** = a gameplay
consequence that still needs a unit test or a device run.

## 1. Gates

| Gate | Result |
|---|---|
| `python3 scripts/verify-static-reconstruction.py assassins_creed_-_br_320x240_136711.jar reconstructed-project` | `ok: true` (~44 s) |
| `python3 -m unittest discover -s tests` | 57/57 pass |
| `./gradlew :core:test` | 1,598/1,598 pass, 0 skipped |
| `./gradlew :gdx:test` | 9/9 pass |
| `./gradlew :android:assembleDebug` | success, `android-debug.apk` 12.8 MB |
| GitHub | 0 open PRs, 0 open issues; 125 remote branches — 67 are ancestors of `main`, the other 58 are not (the ones spot-checked were squash-merged) |

Environment notes (cloud container, not the emulator host):

- Only JDK 21 was present; the Gradle toolchain needs JDK 17
  (`openjdk-17-jdk-headless` installed ad hoc).
- No Android SDK. After installing cmdline-tools, AGP 8.13.2 auto-installed
  `platforms;android-36` and `build-tools;35.0.0` — not the
  `android-36.1` / `36.0.0` pins listed in `rewrite/README.md`.
- No `/dev/kvm`: the emulator cannot run here. Device evidence must come from
  an emulator host (`.agents/skills/android-emulator-testing/SKILL.md`).

## 2. Position against the migration plan

`docs/modern-mobile-technical-design.md` §23:

| Design phase | State on 2026-10-03 |
|---|---|
| 0–2 evidence / toolchain / content | Android done. iOS half of gate item 1 still pending (`plans/260921-0830-libgdx-toolchain-spike/reports/gate-results.md:14`). `generated/provenance.json` lists 34 of the 13,192 shipped files. |
| 3 deterministic vertical slice | Done except a state hash / replay for the real game — hashing exists only on the spike path (`TickEngine.kt`, `DeterministicHashTest.kt`). |
| 4 gameplay parity systems | Mostly done: `i.aV()` 40/40 live states, every script opcode present in the 8 packs has an arm, flying/chase/boss directors ported. Gaps in §3 below. |
| 5 full content integration | 8/8 missions have a capstone win test. m0/m1/m4 run continuously; m2/m3/m5/m6/m7 are stitched legs with teleports between them; the m7 boss leg tops up player HP (`Slice1Test.kt:28929`). Device evidence in `plans/260922-0730-demo-verify/reports/REPORT.md` ends at Run-30 (slice 330); slices 337–343 were verified only per commit messages ("Run-31" is cited in `Slice337Test.kt:9` but not recorded). |
| 6 platform / release hardening | Largely not started (§4). |

## 3. Gameplay-fidelity gaps

| # | Finding | Evidence | Status |
|---|---|---|---|
| G1 | **ax47 and ax50 run each other's FSM.** Original: `case 47 → aL()` (sentinel, S0/80–84/93/94), `case 50 → aK()` (pouncer, S119–130). Port: `tickAx47` holds the `aK` body, `tickAx50` the `aL` body. | `bytecode/i.javap.txt:20024-20025` (47→7680 `aL`, 50→7687 `aK`), `:22688/:22691`; `simple/i.java:5181-5194`, `:9910` (`aK`), `:10008` (`aL`); port `Level0World.kt:842-846` (retype), `:5432-5433` (dispatch), `NpcFsm.kt:9219-9332` | proven |
| G1a | Impact: 12 sentinels (ax11 records with r8[5]∈{80,93}: m0 4, m2 2, m3 3, m5 3) and 8 pouncers (ax17 records with r8[5]=120: m3 3, m5 5) never grab or pounce. `Slice64Test` builds ax47 at S120, which hides the swap. | `levels-decoded/pack-{6,8,9,11}/records.json` `retype_histogram`; `Slice1Test.kt:7505-7580` | proven (data) / inferred (gameplay) |
| G2 | **`applyHit` has no op 40.** The ax67 springboard calls `applyHit(40, …)`; the original case 40 sets `g.b`, `aB=3`, `i(3)`, faces and pushes ±512. | port `NpcFsm.kt:3591`, `Entity.kt:3493+` (arms 4/18/20/21/26/29/34/32/11/24/38 only); original `structured/i.java:3615-3624` | proven |
| G3 | **Player S18 has no arm.** In `g.e()` the switch sends 18/19/23/36 to offset 7396 = `L1ce4` (`cv=1`, then the `L1ce8` air tail with `aj=1536`). The port routes 19/36 (`PlayerFsm.kt:266`) and 23 (`:1000`, cv via `airFamily` `:1922`) there — its own comment at `:1919` lists 18 — but no arm dispatches 18. S18, entered from the S17 wall-kick (`:995`), falls into the default fling arm: no gravity, no air tail. | `bytecode/g.javap.txt:2537-2555` (18/19/23/36 → 7396), `:5344-5346`; `simple/g.java:632-650`; default arm `PlayerFsm.kt:1477-1488` | proven |
| G4 | **Player S371 has no arm.** Bytecode: `371 → 2223: goto 13629` — the same bare `goto L353d` (0x353d = 13629) as 59/65/164/211/297, which the port mirrors with empty arms (`PlayerFsm.kt:381`, `:940`). S370 (Cesare grab wind-up) enters S371 (`:1213-1215`) and the default arm can fling on anim end. Consequence: the ax61 grab may be escapable without the QTE (`NpcFsm.kt:5510-5516`). | `bytecode/g.javap.txt:2890`, `:2924`; targets of 59/65/164/211/297 at `:6471`, `:7021-7022`, `:7484` | proven (dispatch) / inferred (QTE escape) |
| G5 | **ax11 `aC()` attack scheduler is a subset.** Only a chase-timeout fragment is ported; nothing sets an NPC to S11, so the S11 wind-up arm is unreachable. ax73 has a full port (`attackScheduler73`). 176 ax11 records (m0 49, m2 41, m3 27, m5 27, m6 28, m7 4). | port `NpcFsm.kt:1076-1085`, `:371`, `:8541+`; original call `simple/i.java:5557-5560`, body `i.aC()` (`simple/i.java:8893-9144`); mining summary `docs/gameplay-mining/npc-fsm.md:33` | proven (gap) / inferred (combat impact) |
| G6 | **Ghost YES/NO rows** on the jC=28 "GAME DATA HAS BEEN DELETED" screen: `panelVisible` is true for every jC=28 while the `eC==121` arm of `ae()` draws title + back only. | port `Level0World.kt:3095-3097`, `:3554-3563`; original `k.java:6204-6228`; device `REPORT.md:1507-1508` | probable — confirm against the original draw path |
| G7 | Deviations, low impact: ax37 scroll triggers run from a separate record list that ignores P&32/P&256 and script removals; ax23 is routed through ax11 arms (unreachable with current data); `Trig` sin table is generated and `atan2` uses `StrictMath` instead of the original `V[]` binary search. | port `Level0World.kt:592-614`, `:5275`; `NpcFsm.kt:264`; `Trig.kt:7-20`, `:40-45`; original `structured/j.java:371-417` | proven (code) / inferred (impact) |

Stale code comments: `NpcFsm.kt:29-31` (S12 "deferred", ported at `:342`),
`PlayerFsm.kt:2197` (assassination shortcut "omitted", ported at `:2274-2282`),
`NpcFsm.kt:9219` (`aK` labelled "ledge-sentinel"),
`Level0Game.kt:209-212` (MIDI "log-skip"; `AudioBridge` plays the OGG renders).

## 4. Platform and hardening gaps

| Area | Finding | Evidence |
|---|---|---|
| Save format | Game writes a raw 320 B `asbr-save.bin` (`kBA` = 160 LE shorts): no magic, version, length or digest; `saveLoad` accepts any file ≥ 2 bytes. The 49-byte versioned ACRS format is used only by `SpikeGame`. The legacy `bA` record is 512 bytes (`docs/save-format.md:21`). | `Level0Game.kt:55`; `Level0World.kt:1419`, `:2932-2957`; `SaveSnapshot.kt` |
| Atomic write | `SaveBridge` writes the temp file, **deletes the target, then renames** — a crash in between loses the save; no fsync, no backup, leftover `.tmp` ignored. | `SaveBridge.kt:15-23` |
| Tick failure | Exceptions in `world.tick` are logged as "quarantining" but the sim keeps ticking and draining commands (incl. `PersistBA`). Design §8 requires abort + permanent quarantine. | `Level0Game.kt:203-207` |
| Back key | No `setCatchKey`/key handler; `v(131072)` back has "no zone emitter yet", so system BACK finishes the Activity. | `Level0InputBridge.kt:43-66`; `Level0World.kt:2993` |
| Suspend | `pause()` only stashes the music slot; no flush. (Losing a mid-mission checkpoint on process death matches the original — Run-26.) | `Level0Game.kt:243-253` |
| Manifest | No `android:appCategory="game"` (Android 16 large-screen orientation override with targetSdk 36), `configChanges` lacks `smallestScreenSize\|density\|uiMode`, no cutout mode. | `android/src/main/AndroidManifest.xml:11-12` |
| Logging | Per-sound and per-save logs are always on; `AudioBridge` logs under `SpikeGame.TAG` ("AcSpike"). | `AudioBridge.kt:43-78`; `Level0Game.kt:187-244` |
| CI | None — no `.github/` directory. | repo root |
| iOS | Never built; `IOSLauncher` boots `SpikeGame`; no RoboVM plugin in `ios/build.gradle.kts`; module only joins on macOS. | `IOSLauncher.kt:18`; `settings.gradle.kts:33-36` |
| Provenance | 34 manifest entries vs 13,192 shipped files; `convert_spike_assets.py` overwrites the manifest with 3 entries. | `generated/provenance.json`; `tools/convert_spike_assets.py:23-39,71-73` |
| Rights | APK ships the original art/audio/levels plus Ubisoft/Gameloft strings. Out of scope: release is internal only (decision 2026-10-03). | `Level0World.kt:1503-1525` |

## 5. Docs and repo hygiene

- Stale: `README.md:175-179`, `rewrite/README.md` (still "spike, not the game
  port"; `AcSpike` tag; 49-byte save), `docs/project-roadmap.md` (no port
  track; malformed tables L64-70/L82), `docs/project-overview-pdr.md:17,32`,
  `docs/codebase-summary.md:4-21`, `docs/system-architecture.md:72,84-91`,
  `docs/modern-mobile-technical-design.md:3,14-16,167`,
  `docs/gameplay-design-document.md:112,119-122`,
  `docs/gameplay-mining/level-atlas.md` (no ax47/ax50 retypes),
  `docs/gameplay-mining/entity-type-catalog.md:39` (ax37 is the camera scroll
  trigger, not a pickup).
- `AGENTS.md:32-34` pins "≤4 catch-up" and "save 49-byte ACRS"; the game loop
  runs at most one self-clocked tick per frame since slice 336
  (`Level0Game.kt:15-25`, `j.java:189-219`) and saves the 320 B record.
- Plans: 17 `plan.md` files have no `status`; `plans/260921-0939-npc-fsm-mining/plan.md:7`
  says `in_progress` although all three phases are completed; slices 330–343
  have no plan directory; `REPORT.md` frontmatter is frozen at Run-14.
- Repo: `rewrite/a.out` (empty ELF, tracked); `DebugSlice2.kt` is a test with
  no assertions; `Slice1Test.kt` is 29,107 lines / 190 classes; spike-only
  `splash.png`/`actor.png`/`sfx.wav` ship in the APK.
