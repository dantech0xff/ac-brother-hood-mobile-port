---
title: "Slice 415 — ax5 mission watcher, ax61 boss hand and the ax11 script-claim head re-read from the raw bytes"
phase: "port"
status: "done"
slice: 415
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (static j(i)Z @0-71; aq()V @0-902; ap()V; ao()V; aR()V @0-1406; n()V @0-558; I() @1700-1951; aY/aX/aO/aN/al/bB/bN/G/P)
  - reconstructed-project/bytecode/k.javap.txt (l(I)V @0-1171 incl. @84-99; q(I)Li; @0-69; a(i); n())
---

# Slice 415

Slice 414 parked a list of entity arms that no raw-byte pass had touched (`ax41 n()`, `ax5 aq()`, `ax61 aR()` except
S8, the ax11 script-claim head @1700-1951 ...). This slice walks that list with `bcdec.py` / `rawm.py` over the javap text.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **ax61 `aR()` shared harm arm** (the boss's hand: S2 / S4 / S5 / S17) | @335-551: the two `av` stores (`aS.ak < aU.ak` → false, else true) join at @445, where `aS.a(4,0,0,this)` runs for BOTH sides; S2 **and S17** then `i(375)`, zero the velocities, `al = aU.al` | read from the jadx labels: a player LEFT of the boss "escaped unharmed" (no hit at all), and S17 got no grab snap | the hit lands on both sides, S2 and S17 snap into the grab |
| **ax11 script-claim head** (`ca != -1`) | @1700-1951: while the claim script is bound the soldier runs ONLY `aa()` (`P \|= 16`) and `goto 7660` skips every arm; it is released (`bI()`, `ca = -1`, `i(2)`, `aA = 0`) when `l()` sees the player, the player is within 40 × 50 px (no `aA & 8`, no `i.bn`) or his attack box `X` reaches `W` — only while `cd[7]` and the player is not in S268 / S267 / S291 | no head at all: `i.d` (`scriptBound`) was written at init and never read | the two shipped script-bound soldiers (mission 6, record `Z[13]` = 97 / 112; both scripts have **no blocks**) stand still as sentries until the player nears, attacks or is seen — they used to patrol like any guard |
| **ax5 `aq()` S8 watcher: the static `i.j(i)Z` polarity** | `j` @0-71: `null → true`; `ax ∈ {11,17,29,27} && r.P()` → true — and `P()` is the DEAD check (`aB <= 0 → G(); true`). The Z[1] = 0 / 3 / 10 / 13 / 17 arms @260-278, @407-433, @486-508, @539-572, @631-675 all use it | the port's private `iJ` answered "gone or ALIVE" (`!deadRelease()`), the exact inverse; `Entity.isDeadCheck` (used by the ax37 `al()` arm) already had it right | the five sites call `Entity.isDeadCheck`; `iJ` deleted. *Latent*: a census of all 8 missions finds 85 ax5-S8 records, every one with `Z[2] = -1` (straight to the bind) except one whose link is absent |
| **`k.l(int)` case 13 with a stats text** (`bx >= 0`) | @84-99: `this = 31; goto 0` re-enters the method as screen 31, which has no arm — the banner block (`eC = 25; K(3); eB = 59`) and the fail/win sting `z(7)` @102-120 are skipped | set `i = 31` and fell through into the banner block: `PlaySfx(7)` played on the way to the stats screen | `continue` (re-entry) |
| ax41 `n()` S6 | @404-441: a MOVING prop touching the player settles; a resting one falls out to the `k.bd` scan, where the same `moving` gate keeps it inert | `if (overlap) { if (moving) i(4); return }` | the literal shape — unobservable, kept for the next reader |

## Tests

`Slice415Test` (+13): the five `j(i)` polarity sites (alive / dead × finished / running); the ax61 arm on both sides
for S2 / S17 / S4 / S5, the degenerate `X` box and the exempt player states; the script head (stands still, released by
proximity / attack box, kept in S268 / S267 / S291 and when hidden); `l(13)` → 31 without the sting (with a bx < 0
control). `Slice1Test`: the "S2 harm skips a player left of the boss" test encoded the misread — corrected to the bytes.
Mutation-checked: 8 mutants (`mut415.py`) — 8 killed. No capstone changed: all core tests pass unchanged.

## Verified equal (no change)

ax2 `aY()` (the gate incl. the `bh==3` `k.ak == 0 && al >= aS.al` arm, the bA serializer, stamps / tombstones),
ax14 `aX()`, ax19 `aO()` (+ the static `g.c(I)Z` set {0,1,7,11,12,26,79}), ax22 `aN()` (both ternaries), ax37 `al()`
(links 0-5, holder claim, mask bits; `k.a(i)` / `k.n()` / `i.b(int[],int[])` containment), ax67 `bB()` (springboard S
bank, S28 / S12 arms), ax74 `bN()`, ax5 `aq()` (every arm but the polarity), `ap()` / `ao()`, ax61 `aR()` S1 / S8-13 / S18-19 and
the QTE helpers `f(II)Z` / `c(II)V` / `d(II)V` / `U()` / `V()` / `T()` (+ `k.v(I)Z`), `i.P()` / `G()`, `k.q(I)`,
`k.q()`, `k.I()` structure + the L142 tail, `k.l(int)` every other case.

## Not done (parked)

`k.a()` frame procs of the menu / UI screens, `k.b(Z)` draw, ax64 `bl()` / ax76 `bO()` (no shipped record), the
player `g.e()` arms (see the next slice).
