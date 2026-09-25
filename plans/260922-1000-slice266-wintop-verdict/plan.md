---
title: "Slice 266 — mission-1 top claim-QTE zone binds script-1 and wins (verdict)"
phase: port-slices
status: done
---

# Slice 266 — can level-1 (flying canyon) be completed? VERDICT: YES

## Question

`devin/land` shipped the canyon bot legs but the win condition of mission-1
was unproven end-to-end. This slice adjudicates: **does a completable win
chain exist in level-1, and does the port reproduce it?**

## Verdict — YES, via a scripted QTE at the canyon top (proven + executed)

The complete chain (all steps proven in source AND observed executing in
the port):

1. **ax10-uid7, S31** at record (456,481), `W=[456,481,764,705]` — the
   claim-QTE zone at the canyon top.
2. Player overlap → lane arm: `Z[1]=2` → nibble pack `{0,0,0,2}` → one
   live lane, `CS[2]=16388` (UP) (`NpcFsm.kt:1826-1867`, proven).
3. UP press → `e.aA = Z[3] = 8` + `nl=1` (bh3 draw-side latch,
   `NpcFsm.kt:10524-10525`).
4. `aB!=0 && nl!=0 → P|=8192`; consumed arm
   `bindScript(kSIndex(8)=1)` + `P|512+16` + `kC=e`.
5. `scripts.bin` **script-1** (eH uid8, index 1) — fully re-decoded with
   the verbatim `smallOpLen`/`EI` arg tables (`ScriptTables.kt:52-64`):
   - blk0 (type 2, target uid1 = the player): `op22[8]` ride pose, then
     `op21` waypoints — a straight column x≈580-583:
     `(583,511) → (580,504) → (580,329) → (580,320) → (580,300) →
     (580,289) → (580,-60)` — a scripted ascent to the top.
   - blk1 (type 0): key0 `op37[10]` goal blit; key45 `op37[6]+op37[7]`;
     **key62 `op37[1]` → `screenL(15)`** — mission complete
     (`Entity.kt:2304-2369`, proven).
   - blk2 (type 1): camera `op11[460,330]` / `op11[460,59]`.
6. `screenL(15)` → stats arm → `missionWon=true`; redirects to `i=22`
   (medal screen) or `i=10` (level select) per the stamped conditions —
   jC never *holds* 15 (proven, `Level0World.kt:2341-2355`).

## Port bug fixed

- **S31 consumed arm used `w.kS`** — the `Entity.kS(i)` stub that always
  returns -1 — so `bindScript(-1)` never fired. Fixed to `w.kSIndex`
  (`k.s(uid)` = `kEh.indexOf(uid)`, k.java:3885) at the 3 arm sites.
  Without this the win chain is unreachable; with it the script binds
  (`ca=1`, `P|512+16`, `kC=e`) and `scriptStep` ticks 0→62.

## Faithful findings (not bugs)

- **`i.be` static death-slide latch** (`i.java:99`, proven): set by the
  canyon cell-21 kill (`aT==21||aU==21`, `i.B()` L1d4 → `i.java:1380`)
  and by the S10 out-of-band perch kill (`i.java:12265`); cleared only
  by the `bB`-reset (`i.java:2566`). Any cell-21 death removes EVERY S31
  zone via `be → k.c(this)` (`i.java:12350`) — the zone consumption is
  faithful, and it permanently freezes the bound script (script lifetime
  = entity lifetime; only the entity's own `I()` drives `aa()`).
- **Stamped-21 walls are dynamic**: for bh==3, `collisionCell` reads
  `et[dL[cx%21][cy%13]]` — a 21×13 ring of indices into the tile plane,
  stamped from the visual layer as the camera ring rolls. A pre-tick
  dump can show air where a stamped 21 lands mid-ride.

## Test

`mission-1 top claim-QTE zone binds script-1 and wins` (Slice1Test.kt):
teleport into the zone box, drive `pad.e(16388)` every tick, pin the
player inside the cy26-28 air pocket (the ~70-step ride crosses a
21-stamped band whose ring stamps churn — the ride path itself stays
`inferred`), assert `w.missionWon`. Camera pinned `kP=al-120, kO=ak-200`
(follow — `k.m()` cannot leash to teleports, `au` would park at ~94).

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test` → all pass
- `:android:assembleDebug` + `:gdx:build` → green
