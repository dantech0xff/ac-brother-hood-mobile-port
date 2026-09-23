---
title: "Slice 134 — g.j grab latch + default e() arm + S146/S147 wall-sequence"
phase: port-slice-134
status: done
---

# Slice 134 — `g.j` latch, `e()` default arm, S146/S147 transition

Verbatim transcriptions this slice (all `proven` unless noted):

## `g.j` — static grab/pass latch → `Entity.grabLatch`

- `public static boolean j` (g.java:60). Set `true` by the S146 and S147
  arms (g.java:2865, :2898). Cleared by `i.D()` entity-pool init
  (i.java:1817, and again inside the ax0 arm :1977) → reset in
  `Level0World.teardown()`.
- Read by the default `e()` arm (`r() && !j && !l() && !j`,
  g.java:1146) and by overlap/mount gates at i.java:7180 and :15564
  (readers still unported — the write side is now correct).

## `default:` arm — g.java:1145-1147

Replaces the slice-2 inference stub ("attack anims settle to 0/43").
The real arm is one line:

```java
if (r() && !j && !l() && !j) { a(0); }
```

- Covers the ~150-state fallthrough family (g.java:932-1144 — attack
  anims, hit-reacts, decor states without their own case).
- `a(0)` = `g.a(int)` = `flingAirborne(0)` (S43 + `al+=10`, `ah=0`,
  `aj=1536`, `a`/`ac` cleared).
- The doubled `!j` is verbatim — `l()` runs between the two reads.
- Semantics note: `!l()` only passes when the grounded-input helper
  leaves the state untouched — for non-79 states that is `ax()`'s
  airborne-no-support return (`!aZ && standingOn==null`, direction
  held). Grounded + no input → `l()`'s L120 fold lands `i(11)` and
  returns true, so no fling — matches the original ordering.

## `case 146` — g.java:2859-2884

```java
this.ag = this.av ? -2048 : 2048;
if (r() || ((this.av && this.aT != 3) || (!this.av && this.aU != 3))) {
    this.al += 20; i(147); j = true;
}
```

Wall/pass drive: walks at ±2048 until the anim ends OR the facing
side-strip leaves wall cell 3, then drops a row and enters S147.

## `case 147` — g.java:2885-2918 — the transition-landing arm

```java
this.aj = 0; this.ai = 0; this.ah = 0; this.ag = 0;
if (r()) {
    if (this.S == 145) this.al += 20;   // dead conjunct: S is 147
    j = true;
    k.bG = k.x() ? k.bH : -1;           // music slot restore
    k.A(18);                            // sfx
    k.a(true);                          // level reset (:5139)
    k.az = k.a(k.bA, 32);               // streak = short read bA[32]
}
```

- `k.x()` (k.java:5711) → `e.a()` (e.java:32 — track index set AND
  inside its `h.a[e]` duration). Mapped to `musicActive()` =
  `audioTrack >= 0` (`inferred` — our audio has no real-time expiry).
- `k.a(true)` → `resetLevel(true)` → `reloadCheckpoint` → `reload()`
  (`inferred` mapping, same as the fail-screen reload path).
- `k.a(bA, 32)` = the 2-byte LE short reader (k.java:5372) → `kBA[32]`.
- Dead conjunct `S==145` kept verbatim — S is 147 here, it can never
  fire (same proven-dead class as the case-0 flag block).

## Plumbing added to `LevelCellSource`

`kBg`, `kBH` (new field on Level0World, k.java:311), `kBA`,
`musicActive()`, `resetLevel(full)` — all with safe defaults so
test worlds need nothing new.

## Tests — `Slice134Test` (7)

- airborne + held-dir + no support → `a(0)` fling (S43, `al+10`, `ah=0`)
- grounded + no input → `l()` folds to S11, latch suppresses fling
- `grabLatch` blocks the fling
- S146: `ag=±2048`, `r()` handoff → `al+20`, S147, latch
- S146: `aT!=3`/`aU!=3` facing-strip handoff
- S147: vel0, latch, `kBg=kBH`, sfx 18, `resetLevel(true)`,
  `kAz=kBA[32]`; `musicActive=false` → `kBg=-1`

## Gates

verifier `ok:true`; unittests 57; `:core:test` all green (+7);
`:android:assembleDebug`; `:gdx:build`.

## Backlog touched

`g.j` readers at i.java:7180/:15564 still unported — flagged above.
`i(146)` producer at i.java:7361 still unported (the arm exists now;
the entry doesn't).
