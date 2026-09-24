---
title: "slice 202 — e() head flag-clear fidelity + S12 L17c9 fallthrough + air-landing inversions"
phase: port
status: done
---

# Slice 202 — systemic flag-model fix

## What changed

Slice 168's traversal-unblock work introduced a **deviation**: the port armed
`cp/cq/z` universally at every tick head (comment claimed "`g.z` is sticky via
`l()`"). Label-span mining of fallback `g.java` this session proved the real
model:

- **e() head (g.java:1285-1301, proven)**: `cp=cq=cr=cs=z=ct=cu=cv=cw=0` — all
  nine latch flags cleared EVERY tick.
- **l() head (g.java:11608-11617, proven)**: `bM=0; aF=0; cp=1; cq=1; z=1` —
  arms only on `l()` callsites: the grounded tail, and per-state arms
  (cases 5, 1/7/11/26/79, 34, 8, 1(second table), 102, 332, 311/312, and the
  L351e default arm).
- **Per-state sets**: case 33 → `cp`, `ct` conditional; air/fall families →
  `cv` (+`cp/ct/cw`, `I==4→z`); case 60 → `cu`; cases 263/264 → `cw`;
  case 38 grapple-boost → `cq`. Ported accordingly.
- **S12 run arm (L16c0) arms NOTHING** while `ag!=0 && aO==0` — the
  `ag==0||aO!=0 → L17c9` exit (`i.O()` + the shared L17cc grounded block) is
  what re-arms flags on stopped/blocked ticks.

Port now: tick head clears all flags; `l()`/`groundedTail`/per-state arms set
their own. Removed 28 no-op workaround clear lines; kept the real `cq=0;z=0`
exit pairs (`a()` fling, `l()` air branch, grab sites). Deleted the `zz`
shadow field.

## Bugs found by the restage (real fidelity bugs, fixed here)

1. **S12 arm missing the `ag==0||aO!=0 → L17c9` fallthrough** — a runner
   pinned at a crate (ag=0) never re-entered `l()`, so `z` never armed and
   `ap()` starved — the crate could never be broken. Now calls
   `timewarpOff` + `groundedTail` (L17c9→L17cc equivalent).
2. **air-family landing guard inverted** — original `ah<=0 → skip` (lands
   while FALLING); port had `ah <= 0` → land checks (lands only while
   RISING). Jump arcs never landed: S22/233 fell forever. Fixed to `ah>0`
   + the L1fd9 list (`aR>=12||aS>=12||aR==5||aS==5`) with unconditional
   `d(0)` — the `aR==4/aS==4` variant arg lives only in the fall arm
   (L2136), not here.
3. **S22→S23 apex check doubly inverted** — original `ah<0 && ah+aj>=0`
   (still rising but next gravity step hits apex); port had
   `ah>=0 && ah+aj<0` (impossible — `aj=1536>0`). Apex pose never fired.

## Test restages (deviation-encoded → faithful)

- `tap top third jumps` — press only after the player lands (`aZ && S==0`);
  the `cq && v(action)` gate can only fire while an l()-family arm re-arms
  cq. Also: `aZ` jumps pick the S233 support-variant — expected set updated.
- `z clears during S12 run` — asserts the faithful two-sided contract:
  `z` cleared on active-run ticks (`ag!=0 && aO==0`), re-armed on
  `ag==0||aO!=0 → L17c9` ticks.
- `z clears when leaving the ground` — setup now arms `z` via the real
  settled-ground `l()` path instead of the removed universal set.
- `S102 cling up-edge outside E-zone` — pinned `aZ` doesn't survive
  `collideSides`; restaged with feet cell 12 so `aZ` holds → `l()` →
  `cq` → postTail `i(233)`.
- `p.cq=true` pins removed (tick-head clear makes them moot): entBq,
  S102/S332/S317 setups now ride real `aZ`/`l()` paths.

## Mining receipts

- e() head clear: `reconstructed-project/src/fallback/g.java:1285-1301`.
- l() head: `g.java:11608-11617`; callsites :3053, :3846, :4041, :5707,
  :5996, :6046 (`if(aZ)`), :6153 (`if(aZ)`), :7470, :7561 (L351e default).
- S12 arm `ag==0||aO!=0 → L17c9`: `g.java:3569-3572` + L17c9/L17cc
  `g.java:3706-3730`.
- Air land block `ah>0` gate: `g.java:4767-4810` (L1fb3); fall arm's
  `d(aR==4||aS==4)`: `g.java:L2136-L2184`.
- S22 apex `ah<0 && ah+aj>=0 → i(23)`: `g.java:4831-4845` (L2046-L2068).

## Gates

- verifier: `ok:true` · unittest: 57 OK
- `:core:test`: 1335 pass · `:android:assembleDebug` · `:gdx:build` — all green.
