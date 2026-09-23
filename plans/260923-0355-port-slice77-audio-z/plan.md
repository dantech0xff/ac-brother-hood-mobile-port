---
title: "Port slice 77 — z()/e.b() audio command port + j.g counter"
phase: rewrite-port
status: done
---

# Slice 77 — `z()`/`e.b()` audio + `ee[]` mission music + `j.g`

## What

Corrects a slice-76 mislabel and wires it for real: `z(n)` (k.java:7363)
was assumed "screen init" — it is the **audio track player**: `n∉[0,34)
→ nop` else `e.a(n,false)` which plays `a[n]` (loop count 1) gated by
the two channel flags `k.bE` (n<10) / `k.bF` (n≥10) (e.java:50-60,
proven). `e.b()` (e.java:87) = stop → `audioTrack = -1`.

## Ported

- `z(n)` → `Command.PlaySfx(n)` on a new `pendingCommands` queue +
  `audioTrack` (=`e.e`) observable; `drainCommands()` for the game loop.
- `B()` (k.java:2021) mission music: `aJ==1 → z(9)`, else `ee[aj]≠-1 →
  z(ee[aj])`; `ee[]={5,2,3,3,2,4,5,1}` (k.java:8436, proven).
- `stateL` arms now call the real `z()`: `z(7)` on fail/win, `z(6)` on
  complete (guarded `ex∉{10,22}`), `z(0)` on the quit arm, `z(ee[aj])`
  via `missionInit()` on `8|21 && jC==9`.
- `j.g` is now its own counter (`jG`): `l()` resets it to 0 on every
  state entry (k.java:2047) and the driver increments it with each tick
  — separate from `tickIndex` (`j.f`). Previously derived `jG=get()=
  tickIndex` which could never reset — semantics corrected.
- Level0Game drains commands and logs `audio: play track=n` — real
  samples for the 34 tracks are not decoded into the app (flagged).

## Flags

- `a[n]!=null` assumed always (tracks unavailable check unmodeled).
- `kBE`/`kBF` default true (settings-bound in original).
- `e.b()` emits no command — `audioTrack=-1` only (stop needs no
  Command variant since adapters have nothing playing to stop).

## Gates

verifier `ok:true` · 57 unittests · `:core:test` 641 green (+9
`Slice77Test`) · `:android:assembleDebug` OK
