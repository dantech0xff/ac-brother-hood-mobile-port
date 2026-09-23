---
title: "port slice 82 — e(z2) /ASBR record persistence"
phase: port-slice-82
status: done
---

# Slice 82 — `e(true)`/`e(false)` RMS save/load

## Original facts (proven)

- `bA` = `byte[512]` (k.java:291); `a(bA,i,s)` writes shorts
  little-endian at byte offset (k.java:5352), `a(bA,i)` reads them back
  (k.java:5372), `j.a(bA,i,byte)` writes single bytes (j.java:970).
- `e(z2)` (k.java:5557): `/ASBR` record 1 — `e(true)` →
  `addRecord`/`setRecord(1,bA,0,512)`; `e(false)` → `getRecord(1,bA,0)`,
  nop when `getNumRecords()<=0`; exceptions swallowed
  ("ERROR during RecordStore save|load").
- Boot load (k.java:4045-4052): `e(false)` → `eJ = bA[10]!=0`,
  `au = bA[8]`, `au %= 3`, `au==2 && bA[69]==0 → au=0`.

## Ported

- `saveFlush()` (`e(true)`): serializes `kBA` as little-endian shorts
  (2 bytes per slot — our model holds each slot's full value in one
  element, so LE shorts round-trip losslessly; orig byte-exactness is
  moot since the J2ME record is never shared) → emits
  `Command.PersistBA(record)` + `hasSaveRecord = true`.
- `saveLoad(record?)` (`e(false)`): decodes into `kBA`, applies the
  boot arm `eJ`/`au` verbatim; null/short record → nop.
- `hasSaveRecord` flag = `getNumRecords()>0`; `menuSlotReady()` now
  reads it (`Z()` save-slot gate, `inferred` consumer).
- `Level0Game`: `SaveBridge("asbr-save.bin")` — `save.read()` →
  `world.saveLoad()` at boot; `PersistBA` drain → `save.write`.
- `TickEngine` hash covers `PersistBA` (byte-content digest).

## Tests (Slice82Test, 5)

LE encoding, round-trip + `eJ`/`au` arms, `au%3` clamp + `bA[69]`
zero-arm, `e(false)` nop-on-empty defaults + `menuSlotReady` flag.

## Gates

`:core:test` 667+ green, `:gdx` + `:android:assembleDebug` BUILD
SUCCESSFUL, verifier `ok:true`, unittests 57 OK (carried — JAR side
untouched).
