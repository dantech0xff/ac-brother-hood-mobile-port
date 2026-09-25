---
title: "Slice 243 — i.C() live-react arm verbatim fix"
phase: port
status: done
confidence: proven (i.java:5757-5797)
---

# Slice 243 — `i.C()` / `hitReact` live arm correction

## The misread (pre-fix)

`hitReact`'s ax11/73 live arm (`Z[0]!=2`, `aB>0`) diverged from
`i.C()`'s Ld5→L112 chain in three ways:

- `p.S == 67` skipped `g()` — source: `{67,68,69,286,287}` ALL run
  `g()` (the victim push-out).
- `p.S == 287` re-ran the **dead** arm (`ab=null;i(0)` / `i(164)` zero)
  on a LIVE victim — source sends 287 through `g()` + shared react.
- Unlisted player anims no-op'd — source's `!=287 → L112` compare
  drops EVERY swing into `c(6,156,-1,-1)` + `k.A(13)`.

## Verbatim fix (Entity.kt:479)

```kotlin
if (Z[0] == 2) { setAnim(6); return true }          // Lc2
if (p.S == 67 || p.S == 68 || p.S == 69 ||
    p.S == 286 || p.S == 287) resolvePush(w)        // L10e `g()`
hitAnimByType(6, 156)                               // L112 `c(6,156,-1,-1)`
w.sfx(13)                                         // `k.A(13)`
return true
```

`resolvePush`=`i.g()` (i.java:4909), `hitAnimByType`=`i.c` (i.java:9053),
`w.sfx(13)`=`k.A(13)` — all previously ported procs.

## Tests (Slice243Test, 4)

287 on live ax11 → S6 react (was death-arm S0); 287 on live ax73 →
S156 (was S164); unlisted S112 → shared S6 react; `Z[0]==2` → S6
short-circuit.

## Gates

:core:test all green; :android:assembleDebug + :gdx:build clean.
