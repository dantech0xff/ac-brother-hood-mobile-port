---
title: "Slice 63 — port ax73 aJ() heavy-guard FSM"
phase: gameplay-port
status: done
date: 2026-09-22
slice: 63
entity: ax73 (heavy guard, clip-7 family)
source:
  aJ: i.java:9331-9905
  init: i.java:2727 (case 73 → L120), :3038-3088 (L120 full / L123 → L134 minimal)
  helpers: d() :1466, e() :1539, b() :1546, l() :2255, h() :1735, i() :1767,
    j() :1803, C() :1951, g() :1678, aB() :8845, aC() :8893, aE() :9144,
    aF() :9192, aG() :9220, am() :7167, v() :730, h(int,int) :7219,
    n() :9792, i.a(int×3) :9840, i.d(int,int) :9861, i.T() :9878,
    i.U() :9890, i.V() :9902, i.g(int,int) :6881
  k: k.l :804, k.a :816, k.m :863, k.h :6839, k.a(×5) :627, k.u :7203, k.v :7211
  g: g.b :346, g.g :3939, g.h :3945, g.g(int) :5265, g.h(int) :5267,
    g.l :4968, g.j :15, g.y :34, g.z :41
  tables: bu/bw/I/J/H/i.bu (i.java:22315)
  records: packs 8 (uid 301,74), 9 (uid 647), 11 (uid 27,147,148,227),
    12 (uid 733,52,112) — all f[4]=0, f[10]=0 → L134 minimal
confidence:
  aJ body: proven (full verbatim transcription)
  L134 minimal-init on IntArray(22): inferred reconciliation — the J2ME
    original uses Z=int[1] for ax73 and would AIOOBE on the Z[14] reads in
    the S152 ambush arm; the shipped game cannot crash, so minimal records
    must never arm those paths. The port's fixed IntArray(22) reads 0 →
    matches (ambush Z[14]∈{1,6,7} never true, sight rect degenerate →
    awareness is contact-only).
  r10/r11 tail plumbing: proven
ports:
  - rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt
      (+ initAx73, tickAx73, and private helpers: awareness73, sightRect73,
      sightCheck73, canEngage73, counterWindow73, counterStrike73,
      damageIntake73, reactOrEnrage73, hitKnockback73, strikePlayer73,
      attackScheduler73, ceilingAmbush73, crateEdge73, edgeAhead73,
      ceilingProbe73, onscreen73, markerSpawn74, markerMove74,
      markerIdle74, markerTapped74, doorScan73, pressGauge73, kDist73,
      reachRect73, heavyDead73; file-scope BU73/BW73/IH73 tables)
  - rewrite/core/src/main/kotlin/com/acrebuild/core/Entity.kt
      (LevelCellSource: iBf, iBx, kAA, gZ, iL, iM, grabResolve defaults)
  - rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt
      (overrides + init/tick dispatch type==73 / ax==73)
  - rewrite/core/src/main/kotlin/com/acrebuild/core/PlayerFsm.kt
      (l() made non-private for the grab-release call)
  - rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt
      (Slice73Test, 15 cases)
gates:
  verifier: "ok:true (57 unittests + manifest/extraction byte-exact)"
  core-tests: "Slice73Test 15/15 + full suite green (400+)"
  android: ":android:assembleDebug OK"
notes:
  - Armor semantics discovered verbatim and kept: r11 (player NOT facing)
    gates j() → the guard can only be hurt from behind; S152 idle with
    cq==true clears r11 → fully armored while idle.
  - C() enrage: any surviving hit on a Z0==0 record (aB<=bu) upgrades the
    guard to Z0==3 (leap archetype, i(155)+aq retreat). At Z0==3 the tail
    skips both h() and j() → enraged guards are sword-immune; kills are
    grab-QTE escape (S147→i(164)+aB=0) or finisher while still Z0==0.
  - S171 L134→L209 textual fall-through into the S148 arm is preserved
    (ledge + visible player → i(151) throw).
  - S165 abort chain: overshot (facing-side ≥140 past player) → abort;
    else wander ≥140 off the am latch or aG() ledge → abort; am()==false
    keeps walking; am()==true re-arms ag via the shared L159 (+2560
    regardless of facing — verbatim quirk).
  - Grab gate (S165): opposite-facing + X-overlap + aZ + held 16388 +
    |Δal|<10 + Z[8]==0 + kDist≥60, or tap on the clip-74 marker (V()).
    Escape: press-gauge 65568 to bl≥80 → i(164)+aB=0+player i(287);
    timeout bl→0 → i(149)+aS.l()+G().
  - Not ported (verified unused by aJ): n() door-corridor scan, S()/m()
    wisp burst, b(x,y) marker variant.
