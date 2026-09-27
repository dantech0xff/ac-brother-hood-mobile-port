---
title: "Slice 276 — x8990-wall duel solved (face-foe swings + stall-jump suppression); weakened-guard S277 mount-freeze characterized"
phase: port
status: complete
confidence: proven
---

# Slice 276 — posted-guard duel + mount-freeze diagnosis

## What shipped (bot-harness input discipline only — engine untouched)

- **Attack gate faces the foe** (`Slice1Test.kt` ~:24068): when the nearest
  live foe is within `|dx|≤80, |dy|<50`, the bot presses
  `(foe.ak < p.ak ? LEFT : RIGHT) | CONTEXT`. Pure CONTEXT swings toward
  the last facing and hits air once the guard passes behind (the x9000
  S9-death mode); `held or CONTEXT` kept RIGHT held and walked the bot
  into the guard's strike between swings.
- **Stall-jump suppressed near foes** (`~:24041`): the `aZ && ag∈-256..256
  → UP` arm now requires `foe == null`. The guard's body stalls `ag`, and
  hopping over it landed the bot facing east with the guard behind.
- **S277/S293 top-dispatch `continue` removed**: the attack gate already
  emits CONTEXT when the victim is in range, and the `continue` bypassed
  the stall detector — it hid a 137000-tick freeze. Stall marks now print
  `at=`/`aN=`/`g=` alongside the existing fields.

## Result

`bot completes mission-0 end to end`: deaths **301 → 2**, `maxAk = 8999`
(assert `> 8900` still green). The posted ax11 @8850 patrol duel is won;
the bot reaches the '20'-cell wall face x9000–9060/y680–840 alive.

## The new blocker (characterized, unfixed)

The weakened guard grabs the player: the weaken claim sets `Z[19]=1` →
`g.g` binds (au() gate `g.g.ax==11 && Z[19]==1 && !P()`, g.java:10030) →
S277 mount orbit. The ax11-mount arm (g.java:10830, L662) applies a
**constant** drag `ag=-(cF>>8)·sin(cy), ah=(cF>>8)·sin(n-cy)` — `cy` is
never updated on this path, so a horizontal approach gives `ag=ah=0` and
the player is parked at the bind point forever.

The intended release chain (victim-side, i.java):

1. victim `aA==0` + `aS.W` overlaps victim `W` → `aA=1` + `aS.i(297)`
   (carry pose) — i.java:36480.
2. `aS.S==297` + victim within 8px + `k.u(65568)` → victim `i(2)` +
   `aS.i(298)` + `i.at=victim` — i.java:17063 (the throw).
3. `S==298` → victim `i(168)` — g.java:9768 (thrown flight/death).

The mount's frozen drag never reaches W-overlap, so step 1 never fires.
Stall evidence: `aN=null` (the weakened victim never claimed the
finisher lock), `g=11@8975`, `at=null`, `ag=ah=0`, victim `S183`.

## Next-slice question

Does the original `az()` scan bind a **weakened** ax11 (Z[19]==1 but
`i.aN`-unclaimed) to `g.g`? If not, the bind gate is over-broad in the
port; if yes, find the original release for a zero-velocity ax11 mount
(the `drop` arm at PlayerFsm:2295 needs facing/distance the mount can't
produce).
