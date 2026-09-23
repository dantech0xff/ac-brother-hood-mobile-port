---
title: "Slice 158 — k.bk[] clip table wired into kBk"
phase: port
status: done
confidence: proven
---

## What

`Level0World.kBk` was a stub returning 0 — the `k.bk[]` ax67 per-kind
clip table was never consulted. The table itself was already ported
verbatim as `NpcFsm.BK`/`decorClip` (k.java:268, i.java:1945
`aa = k.r(bk[r8[7]])`). Fix: `kBk` now delegates to `decorClip` in both
`Level0World` and the `LevelCellSource` default.

Un-stubs the ax67 offscreen-score arm (i.java:588-592):

```java
ax67 && bk[Z[0]]==49 → r6/400 + r7/240
ax67 && bk[Z[0]]==27 → r6/800 + r7/240
else                 → r6/400 + r7/120
```

which previously always took the else (bk==0).

## Tests (Slice158Test, 1)

- bk[12]=49 → `/400+/240`, bk[1]=27 → `/800+/240`, bk[0]=24 → `/400+/120`
  — all three score arms exercised via `offscreenScore`.

## Gates

verifier ok:true | 57 unittests | :core:test all green (+1) |
android+gdx builds OK
