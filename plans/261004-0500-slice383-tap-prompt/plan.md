---
title: "Slice 383 — k.N, the tap prompt: one object, ticked and drawn"
phase: "port"
status: "done"
slice: 383
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:694-726
  - reconstructed-project/src/structured/k.java:2596-2620
  - reconstructed-project/src/structured/k.java:3077-3079
  - reconstructed-project/src/structured/i.java:293-331
  - reconstructed-project/src/structured/i.java:1482-1590
---

# Slice 383 — `k.N`

Reported by slice 371 ("`k.N` has two ports and neither ever draws").

## The original (proven)

- `k.c(x,y,uid)` (k.java:694-710) creates `k.N` once (ax14, clip `r(9)`,
  S54, az 302, `cq = uid`) and repositions it on every call; `k.k(uid)`
  (k.java:712-719) clears it when `cq == uid || uid == -1`. Nothing else
  writes `N` — it outlives a reload.
- Callers: `i.k()`, the stealth-kill driver (i.java:1482-1590), shows it
  above a target the player can backstab or grab-kill and kills on
  `v(65568)`; the ax51 crate context zone.
- Its tick in `k.I()` (k.java:2601-2619), after the player slot and its
  links: `N.s()`; S54 and a release inside the 50×50 box `k(H,I)`
  (k.java:721-726) or `v(32)` → `i(55)`, `P &= -65`, and the tap fires
  `E(32)` — `i.k()` reads it as `v(65568)`: tapping the prompt is the
  touch way to assassinate; S55 at `T == len-2` → `k(-1)`; finger held
  over it `k(J,K)` → `P |= 64; q()`; otherwise back to S54.
- `b()` calls `N.F()` after the entity loop (k.java:3077-3079).

## The port (before)

Two copies (`marker`/`markerTag` via `setMarker`/`clearMarker`, and
`kN`/`kCq` via `showPrompt`/`clearPrompt`), so two prompts could stand at
once; the reload reset one of them; neither was ticked or drawn — the
prompt could not be seen or tapped.

## Changes

One `kN`/`kCq`; `promptTick()` in `simI()` after the player links;
`kN.drawStyleF()` in `bPass` after the bubbles; the renderer draws it
after the entity loop. The reload no longer clears it.

## Tests

`Slice383Test` (4): a tap plays S55 and fires `E(32)`; S55 removes the
prompt; a held finger shows the held frame; only `k.k()` clears it.
The ax51 crate test reads `kN`.
