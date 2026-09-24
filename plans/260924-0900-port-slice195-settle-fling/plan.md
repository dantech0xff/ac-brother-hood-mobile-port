---
title: "Port slice 195 — settle/fling/inert sweep (22 states)"
phase: port
status: done
---

# Slice 195 — the remaining small `g.e()` arms, verbatim

Full-table sweep: extracted every `case N: goto LABEL` from
`fallback/g.java`'s dispatch and diffed against the port's `when`.
This batch covers all the decoded "settle / fling / inert" arms; the
remaining undispatched labels (216/217 attack-jump, 209, 242/243,
258–287 parkour+hostage, 291/295/303/313) go to the next slice.

## Mining results

- **S19/S36** (L1ce4): `cv=1` → fall into L1ce8's shared air tail —
  the same body the port's `airFamily` already models.
- **S24/S157** (L1ce8): the air tail without `cv`.
- **L1ce8 fidelity gap**: the original tail arms `z=1` when `g.I==4`
  — `airFamily` lacked it (fallArm had it); added.
- **S49** (L3078): `T>9 → ag=ah=0` frame-9 stop; `r() → i(0); E()`;
  `return`.
- **S74** (L169c): `r() → ak±40; a(0)` — leap-dash fling; `return`.
- **S82-85/S326** (L2989→L2de0): `r() → P|=64` — the empty L2989
  label falls into L2de0's corpse-sleep arm (not the default arm).
- **S91** (L30a9): zero all four velocity fields; `r() → i(0)`;
  `return`.
- **S92/S101** (L1a46): zero all; `r() → av=!av; ag=∓2048 (new
  facing); aO==20 → ah=0 else ah=-5120; a(36,36)` wall re-entry.
- **S122** (L11c2): `r() → a(53,1032)`.
- **S148** (L2eb9): `ah=-5120`; `r() → i(149)` — knockback launch.
- **S149** (L2ed1): `ag = g.l ? g.l : ∓1024`; `aj=1536`; `r() →
  g.l=0; ag=0; i(150)` — fling carry → the shared fall arm's S150.
- **S152** (L2f42): `ag=∓1024`; `r() → i(0)`; `return`.
- **S156** (L2f13): zero all; `r() → ag=g.l; ah=0; i(157)` — the
  other end of S149's `g.l` carry; `return`.
- **S204** (L2596): `r() → i(203)`; `return` — the victim-dump
  loop-back.
- **S214** (L305d): `ah=-768`; `r() → ah=0; i(215)`; `return`.
- **S225/S244/S250** (L2f66/L311a/L311b): bare `return` — fully
  inert states.
- **S282** (L2b6c): `r() → i(38)`; `return`.
- **S283** (L309c): `r() → i(0)`; `return`.
- Confirmed bare `goto L353d` (default arm is verbatim — no port
  arm needed): S59/S65 (L2b66/L2b69), S164/S211 (L2f63).

## Changes

- Dispatch arms for 19/36 (`cv=1` + `airFamily`), 24/157 (`airFamily`),
  49, 74, 82-85/326, 91, 92/101, 122, 148, 149, 152, 156, 204, 214,
  225/244/250, 282, 283 — 22 states.
- `airFamily` gains the `I==4 → z` arm (L1ce8 parity with fallArm).
- `Slice195Test` (21 tests).

## Gates

- `:core:test` full rerun → green
- verifier → `ok:true`; unittests → 57; `:android:assembleDebug`,
  `:gdx:build` → ok
