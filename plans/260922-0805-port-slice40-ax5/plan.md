---
title: "Port slice 40 — ax5 aq() mission-logic FSM"
phase: port-slice-40
status: done
---

# Slice 40 — ax5 `aq()` (i.java:7343-7605, proven transcription)

The mission-logic/script-event entity: invisible clip (`bi[5]=1` =
`entry-001-marker-003`, 12 anims, 1 module — converts clean). 10 records
in level 0. Hosts the countdown-event, kill-zone, watcher, and
mission-fail arms plus the `k.C` claim-script binding machinery.

## S arms (aq() switch)

- **S3** — countdown event: player overlap → `b(n)` arm (`i.aH`, `aI=n`,
  `k.aw=0`; bh==3 saves/scales `k.X`), `aC = aF·n`; countdown → `O()`
  disarm + `P|32` + `k.c` removal.
- **S4** — kill zone: `aS.S!=50 && !g.g() && overlap` → `k.l(15)`
  mission-complete.
- **S8** — `Z[1]`-mode watcher on linked entity `k.q(Z[2])`: 20
  cases of P-bit/mode checks routed to the L169 resolution tail
  (`!ab → ao()` bind, `ab → aa()` step) or `ap()` script-forward.
- **S9** — `Z[1]==16` claim stepper: overlap + `k.q(Z[2])==k.C && ab`
  → `bI()` release.
- **S10** — `k.l(12)` mission-fail zone.
- **5/6/7/default** — no-op.

## Record init (i.java:3162, L180 arm via `case 5 → L180` at :2659)

`az=300`, `aE=r8[4]`, `aF=r8[7]`, `n=r8[8]` (`eventN` in the port —
`var n` clashed with the 8.8 x-pos field), `aG=r8[9]`, `aD=r8[10]`,
`m=r8[11]`, `P|128`, `Z[0..3]=r8[14..17]`; `Z[3]!=-1 → P|16`; the S8
pre-bind `h(k.s(aG)) + P|512` when `aG!=-1 && Z[1]<16`; `S==9 → P|512`.
Generic tail `i(r8[5])` (L395) then the **ax5-specific W fill**
`W=[ak, al, ak+r8[12], al+r8[13]]` (L414, i.java:3719-3727) — NOT the
ax10/37/42 `r8[7..10]` arm and not `t()` clip rects (ax∈{14,37,10,5,42}
early-return in `t()`).

## Claim machinery pulled in (all i.java proven)

`j(i)` (:7318), `ao()` (:7249, overlap + aG!=-1 + Z[0] gate — Z0==1
requires the 65568 edge — + `aS.av → P` facing bit + `N()` body),
`N()` (:7284, `k.C` slot claim with prior-holder release+remove),
`ap()` (:7300, `k.s(Z[3])` script forward via `cd[5]` gate), `h()`
(:19300 `bindScript`), `k()` (:19325 `scriptKeyStep`), `bI()` (:19346
`releaseClaim` — full ~60-line port incl. ax13 unlink arm, ax5 `k.ae`
rebind via `i.ai()` (:22284), bh==3 `aS.al-=k.X`, `cP` pending handoff,
`ax==58`/`Z[1]∈{20,21}` tails), `bJ()` (:20024 `reloadScriptOps` —
`k.bz[ca]` copy, table unported → null), `ac()` (:20577
`claimPositionType`), `b()`/`O()` (:7604/:7623 `eventArm`/`eventDisarm`),
`P()` (:7699 `deadRelease` — already ported).

Cross-class field collisions resolved: `i.cM/cN` (-1 latch) vs `g.cM/cN`
(0 orbit) → `claimLatchX/claimLatchY`; `i.cL` (int[] script ops) vs
`g.cL` (int gauge) → `scriptOps`. `i.n` → `eventN`. `cb` = IntArray
script buffer. `cd` widened to 10 (h() writes `cd[7]`, ap() reads
`cd[5]`).

## Still unported (flagged)

- `aa()`/`runClaimScript` — the `k.by` op-table interpreter (scripts
  themselves unmined); stub left with the unmined marker.
- `k.eH`/`k.bz` table loading — `kSIndex`/`claimOps` read empty tables;
  `h/k(k.s(x))` no-ops on -1 as the original does.

## Fix found this slice

`kEh` (`k.eH` IntArray) declared below the `init` block — Kotlin runs
initializers in order, so `spawnEntities` read a null backing field
during ax5 record init. Hoisted above `init`.

## Gates

- `:core:test` — 231 tests (8 new ax5), all green.
- verifier — `ok:true`; `python3 -m unittest` — 57 green.
- Emulator boot — clean (see android build log).
