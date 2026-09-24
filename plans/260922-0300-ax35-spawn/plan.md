---
title: "Slice 173 — ax35 ENTITY_CLIP entry (bi[35]=62); last unmapped level-0 record type"
phase: port
status: done
---

# Slice 173 — ax35 record spawn

## What

`ENTITY_CLIP` was missing `35` — the same class of spawn-skip bug as the
ax42 win fuse (slice 171). `bi[35]=62` (k.java:266), clip62 already
converted in `rewrite/generated/clips/`, `initAx35`/`tickAx35` fully
ported (slice 44). One line restores the record.

## Faithful behavior verified

The level-0 ax35 record (ak=5816, al=165, S=6, P=32, W=[5832,161,5842,169])
is a right-moving grab-wave marker parked off-camera. On its first tick:

1. S6-9 arm (`i.java:19776-19822`): player-overlap grab check — no hit
   (player at x~85).
2. L160 sweep: `overlapStrict` against ax{17,11,23,47,50,73} — 0 hits.
3. L181 march: `aG=0` → nx=ak, ny=al+20.
4. L186 cull: `av=false && ak(5816) > kO(0)+420` → `k.c(this)` —
   **the original removes it identically on tick 1**.

So spawn → sweep → cull = exactly the original sequence; the record
exists for one tick. Slice173Test pins: spawn with non-null clip,
self-cull on tick 1, sweep kills nothing.

## Record coverage after this slice

All level-0 record types now spawn or are intentionally handled:
types 2/37 (record-driven via fireCheckpoints/fireScrollTriggers),
42 (clipless win fuse), 67 (decorClip), 0 (player) — everything else
resolved through ENTITY_CLIP.

## Gates

verifier `ok:true` · unittests 57 · `:core:test` · `:android:assembleDebug`
· `:gdx:build` — all green.
