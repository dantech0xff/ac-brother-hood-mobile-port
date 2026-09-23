---
title: "Slice 152 — bA[] checkpoint serializer completion"
phase: port
status: done
---

# Slice 152 — bA[] checkpoint serializer + restore completion

## Scope

The `k.bA` 512-byte save/checkpoint buffer (`k.java:291`, proven) is already
modeled as `Level0World.kBA` (`IntArray(160)`), but `writeIX` only stamped
`bA[15]=1` and the restore arm dropped `gJ`/`gI`/`kAp` and the mission
globals entirely.

## Verbatim source

- Write arm `i.java:13488-13500` (proven): `bA[28]=ax`, `[30]=ay`,
  `[32]=az`, `[34]=aN`, `[50]=aL`, `[52+aj*2]=ap[5]`, `[68]=aZ?1:0`,
  `[79]=bn?1:0`, `bA[76+i]=br[i]?1:0`. (Same arm at `i.java:17286`.)
- Read arm `k.a(boolean)` `k.java:5185-5203` (proven): `g.I=bA[26]`,
  `q()`, `ap[0,3,2,4,5]`, `ax..aN`, `aL`, `aZ=bA[68]!=0`,
  `i.bn=bA[79]!=0`, `br[i]=bA[76+i]!=0`, then `if (aL!=-1) ...`.

## Port

- `Snapshot` extended with `kAx/kAy/kAz/kAN/kAL/kAZ/iBn` — the globals the
  original serializes into bytes.
- `writeIX` now stamps `bA[28/30/32/34/50/52+aj*2/68/79]`; the `br[]` dead
  set stays on `checkpointDead` (equivalent model).
- `resetPlayerToSpawn` snapshot path now restores `player.gJ/gI`, `kAp`,
  `kAx/kAy/kAz/kAN/kAL`, `kAZ`, `iBn` — previously pos/facing/x1 only,
  so reload lost equip, flags, stats and mission globals.

## Tests — `Slice152Test` (3)

- write stamps bA bytes (incl. `[52+aj*2]` and `[68]/[79]` flags)
- reload restores globals+flags vs the captured snapshot
- aZ/bn=false stamp zeros

## Gates

verifier `ok:true`; unittest 57; `:core:test` green; `:android:assembleDebug`,
`:gdx:build` green.

## Notes

- `kAx`/`kAy`/`kDB`/`kDC` etc. remain the live vars — `kBA` is the wire
  buffer they get stamped into at save/write time, matching the original's
  architecture.
- `kAL==-1` sentinel path (k.java:5200+) not hit on level 0 — noted.
