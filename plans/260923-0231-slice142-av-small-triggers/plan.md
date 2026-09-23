---
title: "Slice 142 — ax10 small linked-entity triggers (S47/S48/S49/S54)"
phase: port-slice
status: done
---

# Slice 142 — four one-shot `aV()` arms

All four are the same `k.q(e.o)` + `a(aS.Y, e.W)` pattern (proven):

- **S47 = L219** (i.java:9423): `k.aQ = 0` every tick — clears the global
  claim-owner slot while the zone lives. NOTE: `k.aQ` here is the
  `i`-typed claim static, not `Image aQ` (k.java:79) — JADX letter
  collision; ported as `w.kAQ = null`.
- **S48 = L166** (i.java:9278): overlap → `k.q(o).aG = p` + `k.c` —
  "boss speed" setter (debug print `Set Boss Speed=` omitted, proven-dead
  dev trace). `p` = record f13 via the L111 default init.
- **S49 = L1bc** (i.java:9347): overlap → `k.q(o).i(20)` + `k.c`.
- **S54 = L136** (i.java:9254): overlap AND linked entity still at S29 →
  `i(30)` + `k.c` — one-shot mission-step advance.

`e.o`/`e.p` come from the L111 default init (`o=f12`, `p=f13`) — no
explicit init arms needed.

## Tests

`Slice142Test` (6): claim-slot clear, S48 copy + overlap gate, S49 advance,
S54 advance + S29 hold.

## Gates

verifier `ok:true`; 57 unittests; `:core:test` all pass (992 incl. 6 new);
`:android:assembleDebug` + `:gdx:build` green.

## Note

pack-6 ax10 zone histogram = {33:2, 34:4, 36:1, 43:2, 16:4, 53:2} — all
already ported, so level 0 is fully covered. The unported arms (S31
sequence-QTE ×9, S54, S48, S17, S30, S24, S14, S20) live in later packs.
