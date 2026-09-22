---
title: "Slice 64 — port ax47 aK() ledge sentinel + ax50 aL() pouncer + shared k() kill driver + j() intake"
phase: gameplay-port
status: done
date: 2026-09-22
slice: 64
entity: ax47 (ledge sentinel) + ax50 (pouncer) — clip-7 family
source:
  retype: i.java:2640-2651 (ax11+r8[5]∈{80,93}→ax47; ax17+r8[5]==120→ax50)
  init: i.java:3082 (ax47→L134: az=r8[17], aB=bu[au], Z[0]=r8[4], i(r8[5])),
    i.java:3017 (ax50→L114: az=r8[13])
  aK: i.java:9910-10007 (S120 snap-grab / 3x3 quadrant pounce pick / S121-128
    pounce with T1 sfx16 + T3 hit / S130 despawn; every arm falls through to
    L62 → k();j())
  aL: i.java:10008-10099 (S94 claim-hold release+despawn, S0 idle terminal,
    S81 post-decrement countdown perch, S82 re-perch, S83 air-walk
    floor probe, S84 →S0, S93 ceiling-grab variant into S80 + claim bind,
    else → L53 k();j())
  k: i.java:2057-2255 (shared stealth-kill driver — head exits, range gates
    r9=5/20, facing release, L79/L81 backstab window + prompt k.c/k.k,
    op6 anims {40<r10<80→49, r10<40→283, r10==40→skip}, L129 ax11 weakened
    finisher, L151 ax47 grab-kill, L160 ax50 grab-kill, tail anim r11)
  j: i.java:1803-1955 (damage intake: P() head, g.b() attack gate, L24
    proximity latch aN/bf, L50 X-overlap →Q()→ damage S∈{183,184,216,217}
    J[au]=100 else ax!=11 H[au]=50, L135 C())
  C: i.java:1956-2053 (ax47/50 arms: survive→no anim; dead→ax50 i129,
    ax47 none)
  l: i.java:2255-2400 (ax47→L83 degenerate point-pierce sight;
    ax50→L77 bn gate + W⊆cam-rect containment; shared LOS + S∉{284,285})
  op6 victim arm: i.java:4586 L142 (49→vel0+ag lerp+k.e/o(3)/A20;
    283→attacker±10 snap; else/90→ak=snapX + k.bK→a(8,59,1,av) fx;
    anim==90→ax47 attacker→k.e + always k.o(3); anim!=90→al=attacker.al)
  b(int[],int[]): i.java:666 — W⊆ac inclusive containment (NOT overlap)
  M()/h(): i.java:7198/7207 — facing-adjacent floor probe e(ak/20±1, al/20)≥5
  i.g(i): i.java:7758 — (o.ak<ak)==av — identical to port's faces()
  k.c(x,y,aw): k.java:870 — k.N prompt entity (ax14/clip9/S54/az302) +
    reposition; k.k(aw): k.java:888 — release when cq==aw
  k.A(n)=z(n) sfx :7372; k.e(0,aw)=w.kStatE; k.o(n)=apStats[n]++;
  S()=i.java:9253 wisp×3; g.p=g.java:21 kill bonus
confidence:
  aK/aL/k/j/C bodies: proven (verbatim transcription arm-for-arm)
  retype head: proven — ax47/50 have no authored records; they are retyped
    ax11/ax17 records (pack-6 carries exactly 4 ax11+S∈{80,93} → 4 ax47s)
  Entity.Z fixed IntArray(22): inferred (J2ME minimal-init reads → 0)
fixtures: 502 :core tests (30 Slice64Test); verifier ok:true; 57 unittests;
  :android:assembleDebug green
---

# What this slice ports

Completes the clip-7 NPC family: ax47 `aK()` (ledge sentinel — hangs under a
ledge, drop-grabs a player who walks over it into the `i(89)` grab anim,
then 3×3-quadrant pounces with anims 121-128), ax50 `aL()` (pouncer —
perch/air-walk/countdown states plus its own ceiling-grab variant into the
claim-bound S80 kill window), and the two shared drivers they fall through
to: `k()` (the whole stealth-kill state machine — backstab window, prompt
entity `k.N`, op6 victim anims, per-ax finishers) and `j()` (damage intake).

# Verbatim quirks preserved

- **Retype-before-init** (`initNpcs`, `proven`): `ax==11 && r8[5]∈{80,93} →
  ax=47`; `ax==17 && r8[5]==120 → ax=50` — no authored ax47/50 records exist.
- **aK() L62 fall-through** (`proven`): every arm — S120's pick, the
  121-128 pounce, even S130's `k.c(this)` despawn — runs `k();j()` after.
  Ported as an unconditional tail in `tickAx47`.
- **S81 post-decrement** (`proven`): the original `r1=aC; aC=r1-1; r1>=0→L53`
  fires `i(82)` only when the OLD value is already <0 — `e.aC-- < 0` in
  Kotlin, i.e. the transition lands one tick after the counter goes negative.
- **Pounce-hit counteredBy** (`proven`): T==3 `a(4,…)` runs BEFORE the `r()`
  check; a damageable player's op4 arm `counteredBy`s the sentinel to S9,
  which re-arms `r()` — the `i(120)` revert is skipped. All clip-7 pounce
  anims end at T==3, so the last frame always coincides with the hit arm.
- **L151/L160 grab-kills keep the marker** (`proven`): no `k.k(aw)` — the
  `k.N` prompt survives with `i(55)` success anim.
- **ax47/50 get no backstab anim** (`proven`): the L81 kill's `i(r11)` tail
  only runs for ax11 (and ax17's `i(68)`); ax47/50 play nothing.
- **op6 `al=attacker.al` gate** (`proven`): vertical sync applies ONLY to
  non-90 anims (L158); anim 90 does `k.e(0,aw)` (ax47 attacker) + `k.o(3)`.
- **ax50's `l()` = W⊆camera-rect containment** (`proven`, `b(int[],int[])`
  at i.java:666) — off-camera pouncers are inert; ax47's `l()` degenerates
  to a point-pierce on `al` (r11/r12/r13 never set → zero sight rect).
- **S94/S93/S84/S0 return without k();j()** (`proven`) — aL() exits early.

# Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt` — `HDM47`,
  `DROP_KILL`, `KILL_EXIT`, `KILL_SKIP`, `KILL_ANIM`, `KILL_HOLD` tables;
  `initAx47`/`initAx50`; `seen47`/`seen50`; `floorAheadM`; `wispBurst`;
  `contextK` (full k() transcription); `damageIntakeSentinel` (j()+C());
  `tickAx47`/`tickAx50`.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Entity.kt` —
  `applyHit6` (i.a(6,…) victim arm); `LevelCellSource` gains `kN`, `kCq`,
  `gP`, `showPrompt`, `clearPrompt`.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt` — retype
  `when` pre-init; init/tick dispatch; `kN`/`kCq`/`gP`/`showPrompt`/
  `clearPrompt` overrides (prompt joins `pendingInsert`).
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  `Slice64Test` (30 tests); palette test now asserts the 4 ax47 retypes.

# Gates

- `python3 scripts/verify-static-reconstruction.py …` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `./gradlew :core:test` → 502 tests, 0 failures
- `./gradlew :android:assembleDebug` → green
