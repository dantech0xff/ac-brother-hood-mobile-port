# NPC-FSM mining report

## Results

- Dispatch proven: outer `case 11/17/23/47/50/73` shares preamble; inner
  `switch(ax)` routes 11→inline ~95-arm FSM, 17→aA (civilian), 47→aL
  (sentinel), 50→aK (pouncer), 73→aJ (grab guard); **23 has no inner case —
  scripted NPC only**.
- Root cause of sharing: `bi[11]=bi[17]=bi[23]=bi[47]=bi[50]=bi[73]=7` —
  all six bind clip 7 (pack-3 entry-007, 201 anims). `i.S` IS the anim index
  (`i.i()` bounds-checks `aa.a()`); `r()`=anim-ended.
- Documented helpers: d() awareness tiers, b() engage, f() drop-assassinate
  marker, j() incoming-hit (J={100,100,100}), k() stealth-kill window,
  l() sight+LOS(Bresenham), aE() ceiling ambush, aI() stomp-bounce-kill,
  aD() crate-riding, au() corpse drop Z[21].
- Full labeled state table for ax11 (idle/patrol/chase/attack/counter 12-18/
  pin 24/fall 25/carry 133-145/air-assassin 168-169/mount-QTE 174-177/
  bounce 180-184) + per-type tables for 17/47/50/73.
- Clip tables: player clip0 = 393 anims/1544 frames; clip7 = 201/942; all 95
  used states resolve to real anims (0 missing). Frame counts tabulated in
  `docs/gameplay-mining/state-animation-map.md`.
- Hook list in `i.i()`: ax29 S27 spawns type-15 burst; ax43 S11 zeroes
  velocity; player S50 footstep, S43/148 ground-ref, S43+S61 ledge fix.

## Gaps (carried to catalog/GDD honestly)

- `V/M/P/t/v/ai/u/O/aa/bI` read by signature only; `aJ`/`aL` tail arms past
  the documented range not individually itemized.
- Player-side states (287/293/310-312 etc.) visible only as NPC references;
  full player FSM pass remains its own phase.
