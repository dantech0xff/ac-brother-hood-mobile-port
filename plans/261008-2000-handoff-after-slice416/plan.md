---
title: "Handoff after slice 416 — rules, tools, trap catalogue and backlog for continuing the raw-bytecode parity audit"
phase: "port"
status: "in_progress"
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - AGENTS.md
  - plans/261008-1800-slice416-parallel-audit-findings/plan.md
  - plans/261003-0700-parity-gap-closure-android-hardening/phase-01-parity-gap-closure.md (progress table = the slice ledger)
  - reconstructed-project/bytecode/*.javap.txt (the authority), reconstructed-project/inventory/*.json (verified against the JAR)
  - scripts/rawm.py, scripts/bcdec.py, scripts/xref.py, scripts/mutation-check.py, scripts/mutants/slice416.py
---

# Handoff: continue the parity audit without introducing errors

For whoever picks the work up next (Devin or anyone). Read sections 0-2 completely before touching anything; use the rest
as a reference while you work. Every command below was run on this checkout.

## 0. Read this first

`rewrite/` is a **remake** of the J2ME *Assassin's Creed: Brotherhood* (320×240) in Kotlin + LibGDX, not a demo. It plays
all eight missions. Since slice 346 the work is a **raw-bytecode parity audit**: take one method family of the original,
prove every divergence of the port in the instructions, fix it with a test that fails on the old code. Slices 346-416 did
this and every slice found real bugs — a port that *looks* right is not faithful. Do it the same way, at the same standard.

The six rules that prevent most wrong work:

1. **The `javap` text is the authority.** Decompiled sources (`reconstructed-project/src/{structured,simple,fallback}`)
   are hints: they invert conditions, flatten `else if` chains and hide overloads. Read the raw instructions.
2. **A name can mean two things.** Overloads, subclass re-declarations, a static and an instance field with the same name.
   Resolve the class and descriptor at every site, and run `scripts/xref.py` on every field you port or change.
3. **A Kotlin variable nobody writes — or nobody reads — is a bug until proven otherwise.** (Four such dead copies were
   found in slice 416 alone.)
4. **Never weaken a test.** A test that encoded a misread is corrected to the bytes, and the plan says so. Capstone bots
   change *route, timing, input* only — never enemies, state, assertions, or whether a test runs.
5. **One method family per slice. Failing test first. Mutation check. Gates. Commit.** Then stop or continue as the user says.
6. **Never run the original JAR. Never open a PR, merge, or delete a branch unless asked.**

**Stop conditions.** When the user says to pause / validate: finish the slice in hand, commit, push, (open the PR if
asked), report, and **stop**. Do not start the next slice. The user validates on a device and then decides.

## 1. Where things stand

- Branch `claude/audit-init-script-vm` → **PR #386** (slices 389-416) against `main`; slice 416 is commit `91384919`, CI green on it
  (static / jvm / android, about 3 minutes each — a cheap second opinion after every push). If #386 is not merged when you
  start, do **not** branch from `main` (it lacks slices 389-416):
  `git fetch origin claude/audit-init-script-vm && git checkout -b <new> origin/claude/audit-init-script-vm`.
  If it is merged: `git fetch origin main && git checkout -b <new> origin/main`. Unsure → ask. Never rebase, amend or force-push
  a branch someone else may have checked out.
- Gates at hand-off: verifier `"ok": true`; `python3 -m unittest discover -s tests` 57 OK; `:core:test` 2265 tests, 0
  failures; `:gdx:test` 18; `:android:assembleDebug` builds. CI (`.github/workflows/ci.yml`: static / jvm / android) runs the
  same gates.
- Plan state (`plans/261003-0700-parity-gap-closure-android-hardening/`): Phase 1 done as a rolling ledger (progress table in
  `phase-01-parity-gap-closure.md`, one row per slice); Phase 2 in progress (capstones green; step 3 "connect the stitched
  legs" and the device runs open); Phase 3 done; Phases 4-5 in progress — everything that needs no device is done (bar optional
  items); what is left is device measurement and cleanup that needs the user's approval.
- Evidence of how thorough the previous work was: `plans/261008-1800-slice416-parallel-audit-findings/plan.md` (bytes,
  was / now tables, tests, mutants, verified-equal list, parked items). Copy that shape.
- **Your first 15 minutes:** read `AGENTS.md` and sections 0-2 here; `rewrite/tools/bootstrap-dev-env.sh`; run the three gate groups
  of section 2 once on the *untouched* checkout (they must be green — if not, you have an environment problem, not a code
  problem); run the first three commands of 3.4 and compare with what is printed there. Only then pick a slice.

## 2. Ground rules, and how to comply with each

| rule | how to comply |
|---|---|
| **Static-only for the original JAR** (`assassins_creed_-_br_320x240_136711.jar`) | Never `java -jar`, never an emulator / simulator / J2ME runtime, never load its classes. Reading `javap` text, the JSON inventory and extracted resources, and running `scripts/*`, is fine. Only `rewrite/` is built and run. Slice 416 used a throwaway interpreter over the `javap` text as a differential oracle for pure helper methods (the user was told); it is **not** in the repo — do not rebuild or run anything like it without asking. |
| **Provenance + confidence** | Every claim about original behaviour carries `proven` / `high-confidence` / `inferred` / `unknown` and a source: `g.javap.txt e() @4440-4473` (byte offsets) or `file:line` in `reconstructed-project/src/`. In code comments, tests, plans and docs. `unknown` is a valid answer; a guess labelled `proven` is the worst outcome. |
| **No silent approximation** | If a mechanic is not mined in `docs/gameplay-mining/`, mine it first (read the bytes, write it down), then port. No stubs, no "close enough", no invented behaviour — the `universal collideSides` of slices 372/405 and the "player ticks first" frame order of G12 were exactly that. |
| **Raw bytes beat prior plans** | Earlier plans (including "verified equal" lines and this handoff) are hypotheses. A sub-agent's hand-back is a hypothesis too. Re-prove in the raw instructions before changing a line. |
| **Gates** | Before every push: `LANG=C.UTF-8 python3 scripts/verify-static-reconstruction.py assassins_creed_-_br_320x240_136711.jar reconstructed-project` → `"ok": true`; `python3 -m unittest discover -s tests`; `cd rewrite && ./gradlew :core:test :gdx:test :android:assembleDebug` (JDK 17; `rewrite/tools/bootstrap-dev-env.sh` sets the box up; keep `LANG=C.UTF-8`, test names contain non-ASCII). To force a re-run of unchanged tests use `:core:cleanTest`. |
| **Docs** | Repo docs (`docs/`) are Vietnamese. Files under `plans/` are English with YAML frontmatter (`title`, `phase`, `status`, ...). Slice plans: `plans/YYMMDD-HHMM-sliceNNN-slug/plan.md`, plus a row in the phase-01 progress table. |
| **Commits** | One commit per slice (`fix(port): slice N — <what, in one line>`), body = the byte offsets and labels, ending with a `Tests:` line (counts, mutants killed). Stage **by path** — never `git add -A` / `git add .` (scratch files). `git status --short` before committing; `git diff --stat` after any mutation run. Keep your tool's own attribution trailer if it adds one. |
| **PR / merge / branches** | Open a PR only when asked (follow `.github/pull_request_template.md` if one exists; none does today). Never merge, close, or delete branches (local or remote, including the old `devin/*` ones) without an explicit instruction. |
| **Working with the user** | They write Vietnamese, want frequent commits, validate on a device, and say "tạm dừng để tôi validate" to mean *stop after the PR*. When asked for an estimate, give an honest one with its assumptions. |

## 3. Toolbox (all static; nothing loads or runs the JAR)

The `javap` text lives in `reconstructed-project/bytecode/<class>.javap.txt` (`javap -c -p -s -constants -l -verbose`; the
verifier byte-compares it with a fresh disassembly). Classes: `g` (the player, `extends i`), `i` (entity base **and** every
object FSM, ax 0-80, dispatched by `i.I()`), `k` (`extends j`: world, frame loop, HUD, camera, spawn / reload), `j`
(`extends Canvas`: the 62 ms loop, screen state `j.c`, math, Bezier, pack reader), `GloftASBR` (MIDlet), `a b c d e f h`
(sprite animation player, sprite / bitmap font, waypoint store, enemy tables, audio, IGP promo, audio durations — roles and
confidence in `docs/symbol-map.md`).

### 3.1 How to read this `javap` text (checked on all 12 classes)

- Offsets are **byte offsets inside the method's `Code`**. `@4440-4473` in plans and comments means those instruction offsets;
  a jump operand (`goto 13629`) is an offset too, not a line number.
- The method header shows source-style types (`a(i)`), the real descriptor is the line below it (`descriptor: (Li;)V`).
  Overloads are everywhere: `g.a(int)`, `g.a()`, `g.a(i)` ... Always pick by descriptor.
- **Owner rule (proven: all 25,548 field accesses and 5,843 non-constructor calls that target an original class obey it).** In a comment,
  `Field i.ah:I` / `Method k.v:(I)Z` name the class that **declares** the member; a **bare** `Field v:Z` / `Method d:(I)V`
  belongs to the file's **own** class. So in `g.javap.txt`, `Field v:Z` is `g.v` and `Field i.P:I` is `i.P`. A subclass that
  re-declares a name makes a different member: `g.L` ≠ `i.L`, `g.n()` ≠ `g.e()`.
- `if_xx N` jumps to `N` when the condition **holds**; what follows it runs when it does not. `invokespecial` is a private
  method / constructor / `super` call (the private setter `i.a(Li;)V` is called that way), `invokevirtual` the rest.
- `tableswitch` / `lookupswitch` print `key: target` rows; `scripts/rawm.py` shows them as `key -> target`.

### 3.2 `scripts/rawm.py` — the raw instructions of one method

```
python3 scripts/rawm.py reconstructed-project/bytecode/g.javap.txt 'void e()' 4580 4583
== public final void e();    ()V
 4580 sipush 999
 4583 invokestatic #244 // Method d:(I)V          <- bare name: g.d(I)V, the static (S50 / S241 drain)
python3 scripts/rawm.py reconstructed-project/bytecode/i.javap.txt ' a(i)' --desc '(Li;)V'     # whole method
```
`<header>` is a substring of the header line; `--desc` picks one overload; `<from> <to>` are inclusive offsets (omit for the
whole method). It warns when several methods match.

### 3.3 `scripts/bcdec.py` — a pseudo-decompile to read the structure (a reading aid, not an oracle)

```
python3 scripts/bcdec.py reconstructed-project/bytecode/i.javap.txt ' a(i)' --desc '(Li;)V'
  if (ac == null) goto L27 / ac.P = (ac.P & -257) / ac = null / L27: if (r1 == null) goto L48 / ac = r1 / r1.P = (r1.P | 256)
```
Conditions are printed as the bytecode states them: `if (A op B) goto L` = the jump is taken when the condition holds. A join
label can print a ternary hint; `--from` should start at a statement boundary (earlier operands print as `<?>`). When a line
looks odd, read the raw instructions.

### 3.4 `scripts/xref.py` — every reader / writer / caller of a member, with the enclosing method and offset

```
python3 scripts/xref.py i.ac --writes          # 6 stores: g.a(I)V @37, g.e()V @12688, g.as()V @248, g.au()V @1431 are RAW
                                               # (no `P & 256` bookkeeping); i.a(Li;)V @24/@33 is the setter itself
python3 scripts/xref.py i.a --desc '(Li;)V'    # the 14 callers of that setter (all invokespecial)
python3 scripts/xref.py k.u --writes           # the dialog kind: only k.b(IIII)Z @8 and k.ag()V @87
python3 scripts/xref.py g.L ; python3 scripts/xref.py i.L     # two different members: gauge point (instance) vs touch anchor (static)
```
It reads `reconstructed-project/inventory/{field-accesses,calls}.json`, which the verifier regenerates from the JAR and
compares. If your output for the first three commands differs from the lines above, your checkout or the tools are broken —
stop and find out why before trusting anything else. `methods.json` / `fields.json` in the same directory list every method
(descriptor, instruction count, javap line) and field.

### 3.5 `scripts/mutation-check.py` — prove the tests would have caught the bug

Put each OLD (wrong) behaviour back, one at a time, and run the suite: every mutant must be **killed** (a testcase fails).
A survivor is a fix without a test. Write `scripts/mutants/sliceNNN.py` (format and the 36 mutants of slice 416 in
`scripts/mutants/slice416.py`), then:

```
python3 scripts/mutation-check.py scripts/mutants/sliceNNN.py --check                   # anchors exist exactly once (no build)
python3 scripts/mutation-check.py scripts/mutants/sliceNNN.py --tests com.acrebuild.core.SliceNNNTest   # fast iteration
python3 scripts/mutation-check.py scripts/mutants/sliceNNN.py                           # FULL :core:test — what the plan records
```
It edits sources **in place** and restores them byte-for-byte (journal in the temp dir, also on Ctrl-C / SIGTERM; after a
hard kill run `python3 scripts/mutation-check.py --restore`; it refuses to start over a stale journal). **Never commit while it runs**
and check `git diff --stat` afterwards. Kills are attributed from the JUnit XML — a red build with no failing testcase
(compile error, crash) is reported as `ERROR`, never as a kill. (Slice 416's 36 mutants were re-verified this way: 35 killed
by named `Slice416Test` testcases, one — G2, the S184 drag side — by `Slice135Test`, an older class.) A narrow `--tests` filter
therefore shows false survivors; the number you record in a plan is the full-suite one.

## 4. The slice loop — in this order

1. **Scope.** One method family or one FSM arm group. First check it is really unaudited:
   `grep -rln '<method>' plans/*/plan.md` and read its `sources:` / "Verified equal" lines. The "Not done (parked)" lists in
   the slice 402-415 plans are not consistent with each other — treat anything not named in a `sources:` header or a
   verified-equal list as unaudited.
2. **Read the Kotlin first**, the function and its helpers, so you know what the port does before you read the bytes.
3. **Dump the bytes** (`rawm.py`), read the structure (`bcdec.py`). For each statement note: offsets, what it reads and writes
   (class-qualified), where each branch goes.
4. **Cross-reference every member** it touches with `xref.py`. Check that the Kotlin has a writer **and** a reader wherever the
   original does, and nothing else. (This found the S375 sign family, `i.bn`, `g.E`, `k.at`, `i.L/M`.)
5. **Write the was / now / bytes table** in the plan before you code, with a label per row.
6. **Failing test first** (`SliceNNNTest.kt`, section 7). The test name states the behaviour; assert exact values and cite the
   offsets; confirm it fails on the old code.
7. **Fix minimally**, keep the offset comments, delete dead duplicates instead of leaving two sources of truth.
8. **Run the full `:core:test`** and classify every failure: (a) an old test encoded the misread → correct it to the bytes and
   say so in the plan; (b) a capstone bot leaned on the bug → re-route it (section 8); (c) your fix is wrong → re-prove from the
   bytes. Never adjust until green.
9. **Mutation check** (3.5) with the full suite.
10. **Docs.** Plan (English), the row in the phase-01 table, and the Vietnamese mechanic docs
    (`docs/gameplay-mining/*.md`, e.g. `player-mechanics.md`) when the fix changes a documented mechanic.
11. **Gates** (section 2), then **commit by path**, push. Report (section 10).

## 5. Trap catalogue — what went wrong before, and how to catch it

| # | trap | example (all `proven`) | how to catch it |
|---|---|---|---|
| T1 | A decompiled view lies about control flow | `a()` @7644-7657 is reached from BOTH `aA` branches, the structured view nested it in one (alerted soldiers were not solid); ax61 `aR()` @335-551 — the two `av` stores join at @445 so the hit lands on both sides; `ap()` knife arm needs `S != 79` | Follow every branch target offset in the raw text; never reason from indentation or from a decompiled `else` |
| T2 | Same name, different method | `g.n()` (flying player) is NOT `g.e()` — none of the `e()` head runs for it; `k.n(I)V` is the shake overload, `k.n()V` the wall release; the private `i.o()Z` vs `g.o()Z`; `g.b()/b(int)/c(int)/a()` families | `--desc` on every dump; `xref.py` on the call target |
| T3 | Same name, different storage | `g.L/g.M` (instance, the gauge point) vs `i.L/i.M` (static, the touch anchor): the port had four disjoint copies and the anchor zone was dead; `i.bn` (static); `k.u` (dialog kind) vs the pad words `k.bB/k.bC` | `xref.py <Class.field>` for BOTH classes; read each site's class prefix |
| T4 | Dead copies in the port | `PlayerFsm.bn`, `world.eFlag`, `kAt`, `Entity.bq` were written by nobody (or read by nobody): `ap()` ran in counter-binds, S199 was unreachable, the weapon cycle worked once per level | For every field you touch, grep the Kotlin for its writers and readers; compare with `xref.py` |
| T5 | An early exit skips the tail | S38's arm ENDS @11282-11306 (`goto 13629`), the port ran the DOWN handlers after it; the canyon L36 corner arm @278 returns without the trailing `t()` | Follow the jump target; list what lies between the branch and its target |
| T6 | Order of statements | `i.f(this)` is the LAST step of the air family (@8296); `i.bh--` comes after the dead-drag `return` in `g.n()` @47-61; the wall clamp runs after the anim-end fall | Keep byte order; never reorder for readability |
| T7 | `else` vs sequential `if`; fall-through; shared tails | `bu()` S10 falls into the S6/S8 body (no `goto` after `bt()`); a join reached from two arms | Read the instruction before each target: is there a `goto`? |
| T8 | Number semantics | Java `int` division truncates toward zero, `>>` vs `>>>`, `i2b` / `i2s` sign extension, 8.8 fixed-point, `java.util.Random` LCG (`DeterministicRandom`) | Check every cast and shift opcode; `Short`/`Byte` conversions in Kotlin are explicit |
| T9 | Snapshot vs live reads | Door `bv()` reads the LAST frame's `W`; `i.v()` reads a snapshot of the previous tick's camera rect (parked) | Look at *when* the read happens relative to the write |
| T10 | One behaviour, two byte sequences | The wall-grab snap: air site @7886-7938 `((W0+20)/20)*20+1`, fall arm @8881-8927 `(W0/20)*20+1` — one cell apart | Never unify two sites without proving them equal |
| T11 | Setter with side effects vs a raw store | `i.a(Li;)V` keeps `P & 256` (the freeze flag `k.I()` skips); four raw `putfield ac` sites do not | `xref.py i.ac --writes` |
| T12 | Building for a path no data reaches | ax64 `bl()`, ax76 `bO()`, applyHit ops 39/41 have no shipped record; of 85 ax5 S8 records all but one have `Z[2] = -1` (the odd one has no link); 184 type-55 rows, all within byte range | Census the data first (`docs/level-record-formats.md`, `rewrite/generated/level*/`), label the finding `latent` |
| T13 | Trusting a report | Slice 416's auditors returned claims; each was re-proven in the raw instructions first, and an `unknown` they raised (signed-byte waypoint fields) was settled by a data census, not by argument. Earlier plans' "verified equal" lines are claims too | Re-prove in the raw bytes before editing |
| T14 | A test encodes the misread | `Slice130Test` S375 sign, `Slice135Test` S184 side, `Slice195Test` S92 | When a proven fix fails an old test, read the test against the bytes before touching the fix |
| T15 | Static state leaks between worlds / tests | `Entity.gE`, `Entity.at`, `grabLatch`, `gq`, `gf`, `icu`, `entBq`, `L/M` live in the `Entity` companion | Reset in test setup (`resetStatics()` in `Slice369Test`, `world()` in `Slice1Test`); a new static in the port needs a reset point mirroring `D()` |
| T16 | Fresh object vs persistent object | The original builds a fresh `g` on `D()`; the port reuses one `player` — every field the constructor sets needs an explicit reset (parked, item P1 below) | Diff the constructors' `putfield`s against `resetPlayerToSpawn`: `rawm.py reconstructed-project/bytecode/g.javap.txt 'g();' --desc '()V'` and `… 'g(short[])'` (constructors have no modifiers in the header) |

## 6. Port map

| original | Kotlin (`rewrite/core/src/main/kotlin/com/acrebuild/core/`) |
|---|---|
| `g` — player; `g.e()` ground tick, `g.n()` flying tick | `PlayerFsm.kt` (`tickBody` → `dispatch` / `postTail`; `flightTick` for `g.n()`), player-specific helpers on `Entity` (`flingAirborne`, `contextDispatch` = `g.ap()`, `grabLunge`, ...) |
| `i` — entity base: `i(int)` = `setAnim`, `t()`, `a(...)` push / overlap, ctor `i(short[])` | `Entity.kt` (+ `LevelCellSource`, the interface through which entities reach the world) |
| `i.I()` and the ax FSMs (`aq()` ax5, `aR()` ax61, `aj()` ax4, ...) | `NpcFsm.kt` (~10.8k lines) |
| `k` — world, frame loop `k.a()` / `k.I()`, HUD, camera, spawn / reload, `j.c` screen state | `Level0World.kt` (`jC` = `j.c`) |
| `k` pad words `eK eL bB bC eM` | `Pad.kt` |
| `j` math: trig tables (archive `/16`; `j.b` is the cosine lookup), the quadratic Bezier `j.a(6-arg)` | `Trig.kt` (`Trig.bezier`, slice 409) |
| clips, level / mission packs, script tables, fonts | `Clip.kt`, `LevelPack.kt`, `ScriptTables.kt`, `FontClip.kt`; assets in `rewrite/generated/` |

Naming: entity instance fields keep the original names (`ak al ag ai ah av aA S T U W[] x1`). Original **statics** mostly appear
as `<class letter><Name>`: `world.iBn` = `i.bn`, `world.kAw` = `k.aw`, `world.jC` = `j.c`, `Entity.gE` = `g.E`, `Entity.at` =
`i.at`, `Entity.L/M` = `i.L/i.M` (touch anchor). Exceptions: `gQL/gQM` = `g.L/g.M` (the player's gauge point), `dlgU` = `k.u`
(dialog kind), `entBq` = `i.bq`. Declarations carry a `// k.aw` style comment — **trust it only after `xref.py` agrees**.
`LevelCellSource` gives many hooks a default **no-op getter / setter** (`var iBn: Boolean get() = false; set(_) {}`): a hook that
`Level0World` or a test world does not override silently reads 0 and swallows writes. Make it a real `var` in `Level0World`
and override it in the test worlds that need it.

## 7. Writing tests here

- New slice → new `rewrite/core/src/test/kotlin/com/acrebuild/core/SliceNNNTest.kt`. `Slice1Test.kt` is the legacy 30k-line file
  (190 classes): the shared helpers `world(charmap, aj)` (a real `Level0World` for mission `aj`; resets the static latches) and
  `settleIntro(w)` (ticks until the spawn-intro claim has released), plus the capstone bots. Do not add to it unless you must.
- Entity-level tests: `Slice369Test` has `synthClip(...)` / `synth` (a synthetic 400-anim clip) and `HeadWorld`. A **clipless**
  entity's `animFinished()` is true — give it a clip when the arm tests it. `Slice416Test` is a good template (29 tests, each
  citing offsets).
- Reset statics in setup (T15). Edit collision with `w.level.layers.first { it.id == 0 }.cells`. If a member is `private`, reach
  the behaviour through a public hook (`w.tutorialHint(0)`), do not widen visibility for a test.
- Run one class: `cd rewrite && LANG=C.UTF-8 ./gradlew :core:test --tests 'com.acrebuild.core.SliceNNNTest'`. Debug / scratch
  tests live outside the repo (or are deleted before `git add`).

## 8. Capstone bots (missions 0-7)

Bots in `Slice1Test.kt` drive the real world through each mission: `Slice277Test` (the chimney crossing at x9000),
`Slice280Test` m1, `Slice281Test` m2, `Slice282Test` m3, `Slice288Test` m4, `Slice289Test` (+ `chaseMask289`) the corridor capstone
(mission 5; the class also holds mission-6 pieces), `Slice291Test` m6, and for m7 `Slice297Test` `298` `300`-`304` `306`-`309`.
They prove the game is *completable*.

- When a proven fix breaks a bot, the bot was relying on the old behaviour. Re-route it: **path, timing, inputs only**.
  Never weaken, disable or reposition enemies, patch state, skip a test, or loosen an assertion.
- Trace the bot with a compressed state-run log (`(tick, ak, al, S)` runs) and pick the input a human would plausibly press.
  Slice 416's chimney / m3 leg A / m5 leg B re-routes (in its plan) are worked examples.
- A bot that now fails because an enemy engages is a finding to classify, not a test to bend.

## 9. Backlog, in the order I would do it

**P0 — support the user's validation of PR #386.** If your environment has an emulator (earlier Devin commits record
"verified on emulator"), run the device checklist from the PR body: the flying missions (`g.n()` head change), a cart / gondola
ride (raw `ac` stores), the weapon cycle (press twice), the mission-0 tutorial hints after a retry. Procedure and report
location: `rewrite/README.md` (emulator section) and `plans/260922-0730-demo-verify/reports/REPORT.md` (Run-32/33 are queued
there). The cloud container has no `/dev/kvm`.

**P1 — parked by slice 416 (both `proven` / `inferred` as labelled in its plan).**
- *P1a Persistent player vs fresh `g`* (T16, highest value): the original builds a fresh `g` on `D()`, the port reuses `player`.
  `g` declares 71 fields, `i` many more. For each field written by `g.<init>()V` / `g.<init>([S)V` / `i.<init>` / `i.i(short[])`
  (`xref.py <field> --writes`), check `resetPlayerToSpawn` / `spawnEntities` re-initialises it. Fix what differs, with tests.
- *P1b `i.v()` snapshot* (`inferred`): it reads the previous tick's camera rect `k.ac`, the port reads the live rect — a one-tick
  cull edge. Prove whether it is observable before spending a slice on it.

**P2 — original code with no raw-byte pass on record.** The plans' own lists disagree, so *verify each is still open* (step 1 of
the loop): ax78 / ax45 `bA()`, ax34 / ax75 `ak()`, ax42 `bz()`, ax58 `bg()`, ax7 (inline in `I()`), ax17 `aA()`; the `g` statics
`aA()` `aC()` `d(int)` `e(int)` `f(int)`; same-name `i` / `g` method groups with *different parameter lists* (slice 412 swept only
equal-parameter overloads); `k.a()` frame procs of the menu / UI screens, `k.b(Z)` draw, the `k` UI methods `ah M R Q G(int)`.

**P3 — latent / duplicate (low value).** ax64 `bl()` and ax76 `bO()` (no shipped record), applyHit ops 39 / 41 (dead),
`Entity.sideFree` vs `crateEdge73` (the same `aF()` twice, equal on every input).

**P4 — not parity.** Phase 2 step 3 (connect the stitched mission legs into continuous runs; the slices it waited for have
landed), Phase 2 steps 5-6 and Phase 5 device measurements (need an emulator host), the optional `Slice1Test.kt` split (only
when no bot work is in flight; purely mechanical), remote `devin/*` branch cleanup (**needs the user's explicit approval**).

**Out of scope unless the user opens it** (`plans/261003-0700-…/plan.md` §"Out of scope"): iOS, exact-resume save (needs an
ADR), 60 FPS interpolation, replay / state hash, the cheat sequence, store publication.

## 10. Definition of done, and what to report

A slice is done when: every claim has a label and offsets; every fix has a test that failed before; the mutation check (full
suite) killed every mutant; the three gate groups are green; the plan, phase-01 row and Vietnamese docs are updated; the commit
touches only intended paths. Report in this shape (the user reads Vietnamese; keep identifiers as they are):

1. what changed — one line per fix with offsets and label; 2. what was verified equal; 3. tests added / corrected (and why a
test was corrected); 4. gates and mutation result, copied from real output; 5. capstone bots touched (route / timing / input
only, say what); 6. what you did **not** do and why (parked items); 7. what you need from the user.

Commit message shape: `fix(port): slice N — <one line>` then a body of `- <what> @<offsets> (proven): was → now`, `Parked: …`,
`Tests: SliceNTest (+k), m mutants, m killed. Gates: …`.

## 11. Ask the user — do not decide these yourself

Merging a PR; deleting any branch; anything that changes an AGENTS.md invariant or a CI gate; running or building anything that
interprets the original's bytecode; adding a dependency; behaviour that stays `unknown` after reading the bytes (record it, ask);
an ADR-level choice (save format, tick cadence, frame pacing); store / rights / publication work; anything destructive.
