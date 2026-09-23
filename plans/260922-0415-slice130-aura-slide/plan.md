---
title: "Slice 130 — S375-377 ax61 aura knockback slide + i.f tails"
phase: port
status: done
---

## Scope

Ported the ax61 aura knockback slide trio (g.java:4245-4309, proven):
`i(375)` is written by `aR()` S15/S2+S17 catch arms (i.java:8768/:8947 —
already firing in our `tickAx61`), but the state's own per-tick logic
was unported — entities settled via the generic else-arm instead of
skidding.

- `S375`: `ag = av ? -1280 : 1280`, `r() → i(376)`, `i.f(this)` tail.
- `S376`: `ah=0; ag=0`, `r() → i(377)`, `i.f(this)`.
- `S377`: `r() → a(0)` (`enterFall`), `i.f(this)`.

The trailing JADX-empty blocks (`S!=9`, `aR==2`, `aO!=6`, `z2=false`,
`J&4`, `o()`, `aA` checks, `!A`) are the shared post-switch chain and
evaluate to no-ops — omitted verbatim.

## Verification finding (slice-129 follow-through)

`bm()` (ax66, i.java:15308) is already fully ported: S6-10/24-28
board/ride/sink/reset, S11-13 grab arms (`br()`/`bn()`/`bq()`/`m()`/`n()`
ported as `platformGrabCheck`/`platformSpawnAid`/`grabZone`), S14-22
countdown/kill-link/attack-grab arms. The earlier "unported bm() S0/1/2"
note was stale — ax66's bm() switch has no S0/1/2 cases (those live in
bs() for ax51 only, ported in slice 128).

## Gate results

- verifier: `ok:true`
- `python3 -m unittest`: 57 pass
- `:core:test`: 889 pass (3 new Slice130 tests)
- `:android:assembleDebug`, `:gdx:build`: green
