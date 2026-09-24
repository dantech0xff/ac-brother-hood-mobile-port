---
title: "Slice 200 — am() L5d standing-in-wall climb arm + iface provenance sweep"
phase: port
status: done
---

# Slice 200

- **`am()` L5d arm** (g.java:687-712, proven): `ag==0 && aR==19` with an
  open side (`aV==0 → av=0`, `aW==0 → av=1`, aW wins when both) → snap
  `ak` to the open side's grid edge (`ak/20*20 + av?20:0`) → shared tail
  `aj=ah=ag=0; a(63,16385)` climb-up. Call-site DOWN gate (`k.u(33024)`
  L1893) already faithful — the arm completes `am()`.
- **Provenance sweep** — four stale labels corrected to their wired
  impls: iface `jRand`/`jNextInt` (Level0World already wires the
  Java-LCG `rng`; `j.a(lo,hi)` verbatim incl. the MIN_VALUE quirk in
  `DeterministicRandom.nextRange`), iface `kBk` → `NpcFsm.decorClip`
  (Level0World:964), bannerK `Y()`/`Z()` decoded (k.java:19825/19851:
  Y() = `bA[15]==1||bA[14]>0` = `menuHasSave` verbatim; Z() = `f.a()`
  IGP catalog → no-shop port correctly returns false).

## Gates

verifier `ok:true`; `:core:test` green incl. Slice200Test (2): DOWN-held
climb-up inside cell-19 (S63 + grid snap + ah=0), no-DOWN gate blocks.
