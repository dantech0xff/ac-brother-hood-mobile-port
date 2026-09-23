---
title: "Slice 98 — b(z2) overlay tail: fades, vignette, letterbox, flicker, QTE bar"
phase: port-slice-98
status: done
---

# Slice 98 — `b(z2)` overlay tail

Ports the remainder of the `b(boolean z2)` HUD/overlay draw — the five tail
blocks after the claim footer (k.java:3164, done in slice 95). All
mutations live in `overlayTailStep()` at the end of `hudStep()` (the
original mutates inside the draw once per frame; the 62ms tick is the
same clock). The renderer reads post-step values — same provenance
discipline as slices 96-97.

## Semantics mined (all `proven` unless noted)

### `an`/`ao` stripe fades (k.java:3166-3188 + `aa()` :5715-5736)

- `an` (fade-IN): `bI<0→0`; while `bI <= 255-fk` → `bI += fk` + `aa()`
  grow arm: `fn++` clamp `fl=9`, draw bars `fm*fn` top + mirrored bottom.
  When `bI` exceeds → one solid-black frame, `an=false`. So the "fade"
  IS the stripe letterbox closing — `fl=9` stripes of `fm=13`px
  (`fm = (240/fl)/2`, k.java:315-317).
- `ao` (fade-OUT): `bI>255→255`; while `bI >= fk` → `bI -= fk` + `aa()`
  `!an` arm: `fn--` then draw shrink bars `120-((fl-fn)*fm)` top and
  `120+((fl-fn-1)*fm)` h=`120-((fl-fn-1)*fm)` bottom; `fn` may reach -1
  (undrawn) before `bI` drains — verbatim quirk kept.
- Arms: `k.B(i)` = `an=true,ao=false,bI=0,fk=26` (:5738);
  `k.C(i)` = `ao=true,an=false,bI=255,fk=26` (:5745). Int arg ignored.
  Callers: `i.bh()` door-exit (i.java:14434) and `i.bi()` door-arrival
  (:14448) — inside the ax44 `bv()` FSM; those arms are **unported**
  (flagged — `fadeIn()`/`fadeOut()` exist for the follow-up).
- `fk` init 4 (k.java:314); `bI` init 0.

### `i.bh` damage vignette (k.java:3190-3202)

- `i.bh > 0 && j.c == 8` → `fs -= 10; fs<=0 → fs=80`; alpha
  `(255*fs)/100`, color `0xff0000`, four 5px edge rects
  (top `5,0,390,5` / bottom `5,235` / left `0,0,5,240` / right `395,0`).
- `fs` init 80 (:323); `i.bh` is the ported hit-lock (decrements in
  `g.e()` tail — tests must give it >8 to watch a full fs cycle).

### `av`/`aw`/`dz` cinematic letterbox (k.java:3203-3218)

- `j.c != 14` gate. `av` → `dz += 20` cap 120; else dz chases `aw` by
  ±20, snapping when `|diff| < 20`. Bars of height `dz` top + bottom.
- `dz=120; aw=0` at mission init (:1079 `eC` arm — opening iris out).
  `kDz`/`kAw`/`kAv` fields already existed (slice-70 era labels
  corrected by this mining).

### `i.bJ` flicker (k.java:3219-3238 + k.javap.txt:16541-16599)

- `i.bJ > 0`: `i.bJ == i.bH (=1)` → if `i.bL >= 0` → `i.bJ = i.bI (=2)`;
  `i.bJ == i.bI` → if `i.bL <= 20` → `i.bJ = 0` + **early `return`**
  (skips the aU bar that frame — `tailSkipFrame` carries it).
- **Verbatim no-op**: `i.bL` is `x=x` — bytecode shows
  `getstatic; dup; putstatic` with no arithmetic (obfuscation residue or
  vestigial counter). Kept as a no-op read/test, documented.
- `y.l(0); y.a(cd, null, wrap(y,null,320), 200,50, 0,4,17,-1)` — a
  null-string wrapped draw: `inferred` no visible output, not drawn.
- Producer arm unported (i.javap.txt:10535 + :74799-74803 triple store).

### `aU` grab-QTE meter (k.java:3241-3253)

- `aU != null && (aU.P & 32) == 0 && i.by > 0` → white outline
  `j.c(120,215,125,11)` + fill `j.b(121,215, (125*aU.aB)/800 - 1, 10)`
  — red `aU.aB > 300 || j.g % 3 == 0` else amber `16763904` (0xFFBF00).
- `k.aU`/`i.by` already bound by the claim-script op-10 arm (slice 43c).

### `k.aQ` blit (k.java:3140-3141)

- `drawImage(aQ, 198 - aQ.getWidth(), 5)` — proven site. The `aQ` Image
  is the ax35 volume-paint surface (already ported as `volPaintRect` —
  the `i.a(IIIIZ)` tile/entity compositor itself remains deferred, see
  notes). Drawn as a yellow-bordered mini-rect (`-256` = the
  compositor's own border color, i.java:20159) — `inferred` body.

## Files

- `core/.../Level0World.kt` — fields `iBJ/iBH/iBI/iBL` overrides,
  `kBI/kFk/kFn/kFl/kFm/kFs/fadeSolidFrame/tailSkipFrame`;
  `overlayTailStep()` tail of `hudStep()`; `fadeIn()`/`fadeOut()`.
- `core/.../Entity.kt` — `iBJ/iBH/iBI/iBL` hooks-interface decls.
- `gdx/.../Level0Renderer.kt` — overlay-tail draw block after the claim
  footer: aQ blit, an/ao stripes + black frame, vignette edges, dz
  letterbox, aU meter.

## Gates

- verifier `ok:true`; unittests 57 pass; `:core:test` 768 pass
  (9 new `Slice98Test`); `:android:assembleDebug` clean.

## Left (flagged)

- `i.bh()`/`i.bi()` door-transition arms inside ax44 `bv()` (i.java:
  14356-14450) — they call `fadeIn()`/`fadeOut()`; the arms themselves
  (player vel zero + center-snap + `k.q(o)` bind + `i(284/285)` +
  `k.m(ad)` camera) are unmined/unported.
- `i.a(IIIIZ)` world-rect compositor into `k.aQ` (i.java:20129 — tiles
  via `k.a`/`k.b` painters + `bb[]` entity stamp + `-256` border) —
  heavy; deferred to its own slice.
- `i.bJ` producer arm; `av`/`aw` script producers.
