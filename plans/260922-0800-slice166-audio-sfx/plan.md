---
title: "slice 166 — SFX playback (sfx→z wire-up + pack-17 WAV set + AudioBridge)"
slice: 166
status: done
---

# Slice 166 — SFX playback

## Goal

Make `k.A(id)` actually play sound: wire the `sfx()` stub into `z()`, ship
the WAV-decoded pack-17 SFX set under `generated/audio/`, and give the
LibGDX layer a per-slot `Sound` map.

## Sources

- `k.A(i) = z(i)` — `reconstructed-project/src/structured/k.java:5703`
  (proven). `z(n)`: bounds `n<0||n>=34` nop; `!kBE && n<10` skip;
  `!kBF && n>=10` skip; `audioTrack=n`; `pendingCommands += PlaySfx(n)`.
- Slot→asset map — `docs/gameplay-mining/audio-sprite-usage.md` (proven):
  pack-17 `entry-0NN` ↔ slot N. WAV slots 10–16,18–20,23–25,29–33 (18
  files, PCM u8 8kHz mono — LibGDX-compatible). MIDI slots 0–9 + 17,21,28
  (music/stingers); empty slots 22,26,27 legitimate.

## Port

- `Level0World.sfx(id)` → `sfxLog += id; z(id)` (sfxLog kept as test seam —
  records pre-gate requests verbatim).
- `generated/audio/sfx-NN.wav` — 18 copies, `copy-v1` provenance entries.
- `AudioBridge`: `wavSlots` per-slot `Sound` map, `audio/sfx-N.wav`;
  MIDI/empty slots log-skip (the `PlaySfx` command still issues — verbatim
  `e.a(n,false)`); spike's `sfx.wav` slot-0/1 mapping removed (slot 0/1 are
  MIDI tracks now).
- `Level0Game`: `audio.create()` at boot; command list drained once, fed to
  `audio.execute(...)` then the existing PersistBA/QuitApp loop (drain
  ordering fixed — double-drain would lose the save/quit commands);
  `audio.dispose()` on exit.

## Adaptation (labelled)

- MIDI slots cannot be synthesized on this pipeline (no timidity/fluidsynth
  on the box) — they emit the same `PlaySfx` + `audioTrack` the original
  does, and the runtime logs the skip. A later slice converts MIDI → a
  playable form. Label: `inferred` gap, documented.

## Verification

- `Slice166Test` ×4: `sfx(12)` → `PlaySfx(12)` + `audioTrack==12` +
  `sfxLog`; `kBF=false` gates SFX slots; `kBE=false` gates music slots;
  out-of-range nop — all pass.
- Gates: verifier `ok:true`; `python3 -m unittest` 57/57; `:core:test`
  green; `:gdx:build` + `:android:assembleDebug` clean.
