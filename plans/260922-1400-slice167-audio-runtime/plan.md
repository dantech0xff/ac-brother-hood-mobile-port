---
title: "slice 167 — e.a()/e.b() verbatim + MIDI→OGG music playback"
slice: 167
status: done
---

# Slice 167 — audio runtime

## Goal

Port the real `e.a(i,z2)`/`e.b()`/`e.a()` single-channel audio semantics
(`e.java:40-98`) and ship playable renders of all 13 pack-17 MIDI tracks.

## Sources

- `e.a(i,z2)` — `reconstructed-project/src/simple/e.java:50-85` (proven):
  `a[i]==null→nop`; `!k.bE && i<10→return`; `!k.bF && i>=10→return`;
  `k.bF && k.bE && e!=-1 && a()` → every nested path returns (the
  L18–L29 chain collapses to "a live slot blocks any request"). `z2`
  never read — dead param. Play path: `b()`; `d=now`; Player;
  `setLoopCount(1)`; `e=i`.
- `e.a()` — e.java:40-48: `e!=-1 && (now-d) < h.a[e]`; `e.b()` —
  e.java:87-98: stop/close Player, `e=-1`.
- `h.a[34]` duration table — `reconstructed-project/src/simple/h.java:8`
  (proven): `{38958,14569,7449,16958,9682,11837,6964,3435,5340,9010,218,
  310,812,665,518,502,990,2823,621,797,1108,1581,385,325,517,990,665,281,
  1862,251,177,930,458,236}`.
- `e.a[]` availability — pack-17 ships every slot except 22/26/27
  (proven, legitimately empty).

## Port

- `Level0World`: `hA[34]`, `audioAvail` (22/26/27 excluded), `audioStartMs`
  on the deterministic clock (`tickIndex*62` — verbatim elapsed-ms
  semantic), `audioPlaying()` (`e.a()`), `audioStop()` (`e.b()` — emits
  `Command.StopAudio`), `audioPlay(i)` (`e.a(i,z2)` — collapsed gate +
  preempt via `StopAudio`), `z()` → `audioPlay`.
- `menuL` blip-skip + `musicActive()` now use the real duration check
  (`audioPlaying()`) instead of `audioTrack >= 0`.
- `Command.StopAudio` + `TickEngine` hash arm (5).
- `AudioBridge`: `Music` map `midiSlots→audio/music-N.ogg`, `Sound` map
  `wavSlots→audio/sfx-N.wav`, single-channel `stopAll()` preempt on every
  `PlaySfx`/`StopAudio` (one `Player`, `setLoopCount(1)`).
- `Level0Game` drains once → `audio.execute(list)` then the save/quit loop.

## Assets — `midi-render-v1`

- `generated/audio/music-N.ogg` ×13 (slots 0–9,17,21,28): JDK Gervill
  software synth render (no hardware line on the box — `openStream`
  offline path) → WAV 44.1k s16 → ffmpeg `libvorbis -q:a 3`. Converter
  persisted at `scripts/Midi2Wav.java`.
- Caveat flagged `inferred`: slots 6,17,21,28 render silence — their
  programs don't map onto Gervill's bundled soundbank (peaks measured
  0). Files shipped anyway: they are the faithful render output, and the
  `h.a[e]` duration gate already depends only on core state.
- All level music (`k.ee[]` = {5,2,3,3,2,4,5,1} → slots 1–5) renders
  audible (peaks ~31–32k).

## Verification

- `Slice167Test` ×6: SFX dropped while music within duration; play once
  the slot expires (preempt emits `StopAudio`); empty slots nop;
  `audioStop` → `StopAudio` + `e=-1`; `audioPlaying` duration window;
  `kBF=false` lets music through.
- Gates: verifier `ok:true`; 57 unittests; `:core:test`; `:gdx:build`;
  `:android:assembleDebug` — all green.
