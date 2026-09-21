# Deep mining pass 3 — report

## Results
- **ax21 `bD()` = mission director**: waypoint linked-list pursuit (`cB.g`),
  5-entity kill bitmask `l&62`, phase router `cC`→chains `Z[5+cC]`, escorts
  `Z[13/14]`, finale player-launch + milestone. Drives chase/escort missions.
- **ax29 `aP()` = boss duel**: counter-chance ramp `cn` (+100/stagger, cap
  800), grab-QTE S7 (16388 tap counter vs ±38400 projectile throw), barrage
  S14/35 (1 or 3 param-curve projectiles), finisher S17 (player→370),
  phases `by` 1/3 (Romulus/Cesare), arena camera clamp.
- **ax61 `aR()` = boss multi-tool**: aura S1, trap S15 (`g.d(g.u[au])`,
  `g.u={5,10,15}`), param-lerp projectile S8, grab-QTE overlay S12
  (wiggle 4112/8256 → boss stagger i(28)).
- **Player arms** decoded: 8 grabbed, 89 knockdown, 183/184/205 assassinate
  drives `aN` victim, 216/217 heavy lunge, 270/271 hostage carry, 287
  struggle, 293 air-assassin recover, 297 lever/crank, 310-312 mount QTE,
  370/371 boss-grabbed, 374/375/376 trap release.
- Level-script opcode semantics: ALREADY complete —
  `level-record-formats.md` + `timeline-opcode-contracts.json` (15 fixtures,
  480 extended occurrences). No new mining needed; marked done.

## Remaining unknowns (final)
- `V/M/P/t/v/ai/u/O/aa/bI` helper internals (signature-level only).
- `aJ`/`aL` tail arms; ax21 `bG()` follower internals.
- Difficulty `au`→`bu/bw/u` indexing contract.
