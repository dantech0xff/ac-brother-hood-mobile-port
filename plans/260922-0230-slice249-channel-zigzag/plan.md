---
title: "Slice 249 — channel zigzag → pillar mantle → posted-guard pin (verdict)"
phase: port
status: done
---

# Goal

Prove the chimney-channel wall-kick zigzag + pillar mantle + the
posted-guard encounter run verbatim on live level-0 geometry.

# Result (measured)

Bot parked at the pillar top (1753,519). Full chain observed:

- `11→43` fall → corridor floor → run east → face kicks alternating
  (1799,749) → (1761,683) → (1799,617) — the x1740/x1820 channel zigzag
  rises ~66px/cycle verbatim.
- Kick launch → `43→60@1740,519` ledge-grab on the pillar lip → `62`
  mantle → `0@1750,519` stands on the pillar top.
- Walk east, falls off → `43→315@1734,614` ax10-S36 bound zone catch →
  release at 699 → `43→89@1759,749` **S89 killTouch pin by the ax11
  guard posted at ~x1759** — the tutorial's "MOVE CLOSE TO YOUR
  ENEMY" encounter. Combat, not a dead-end.

## Cell-verified geometry (aclv layer 1)

- Channel x1740–1820 open y200–800; pillar face x1740 spans y520–680;
  wall-B west face x1820 spans y400–800 (zigzag overlap band y520–680).
- Step staircase above: x1820-1860 tops y400, x1860-1880 tops y320,
  x1880+ tops y200 — next leg after the guard.
- Pillar = post y520–680 (open below to corridor floor y800).

# Notes

- S89 pin persisted >7,900 ticks — the exit needs the pinning entity to
  be ax11 AND reach S24. Worth a real-world check whether the posted
  guard cooperates; a non-ax11 pinner would be a verbatim possible
  softlock, same class as other skill gates.
- Test asserts the verbatim chain only; ascent beyond the guard is a
  skill/encounter gate.

# Gates

verifier `ok:true`; unittest 57; `:core:test`, `:gdx:build`,
`:android:assembleDebug` green.
