---
title: "Slice 218 — ax4 crate 'needs gI==8' blocker is misplaced (verdict + tick-path test)"
phase: port
status: done
---

# Slice 218 — ax4 crate gate verdict: no `gI==8` needed to break

## Reported blocker

Golden-path demo: ax4 crates "need `p.gI==8` bind, sword smash
unreachable."

## Verdict: misplaced — two different paths

`p.gI != 8` only gates the **interact-scan** (PlayerFsm L200,
`p.gI != 8 && (ax==4 || ax==58) → skip`, proven) — the context-prompt
target picker. And bit-8 **can never exist on level 0**: `missionF`
init sets `gJ = kF0Do[aj]` = all-5s (k.java:23861, proven), so the
equip wheel holds bits 1+4 only; no ax16-S38 record grants bit-8.

Crates break via the **attack path** (`tickDestructible` S5/7,
i.java:6733, proven): player mid-attack body overlap or `player.X`
hitbox overlap → `i(S+1)` → S6/8 wisp burst + remove. No equip state
involved.

## Proof through the real path

`Slice218Test`: teleport player beside the real uid16 crate
(1607,795, S5), pin camera (au-gate stays live), send a context tap →
`65568` → `ap()` `I==1` → `i(67)` sword swing → crate arms S5→S6,
wisp counter `kAp[5] ≥ 2`, entity removed — all inside `w.tick`,
no direct FSM calls.

Level-0 ax4 bank (records): S5×2 + S7×3 breakable (uid 9/10 @ ~528,
uid 15/16 @ ~1610, uid 83/84 @ ~3726); S9×14 + S21×1 inert props that
hit the proven no-op default arm — crates that visibly do nothing when
hit are faithful, not broken.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` 0 failures;
  `:android:assembleDebug`; `:gdx:build` — all green.
