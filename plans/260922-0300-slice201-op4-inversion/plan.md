---
title: "Slice 201 — op-4 victim-hit-react inversion fix + deferred-note sweep"
phase: port
status: done
slice: 201
---

# Slice 201 — `i.a` op-4 inversion fix + clear remaining deferred notes

## What

Four stale `unmined`/deferred notes cleared by mining, plus one real
behaviour bug found and fixed while clearing the last note.

### 1. `k.j()` pointerStrip margins (proven)

`k.j()` (k.java:925-953) gates gameplay taps through the footer soft-key
strip. Exact semantics:

- `H==-1 && I==-1` → `false` (no touch pending).
- footer button span `ce <= H <= 400-cf` on strip row `240-cg < I < 240`
  → `true` (the tap IS a footer soft-key hit).
- else → `0 <= I <= 240-cg`.

`ce`/`cf` are the soft-key label widths written by `a(str,str2)`
(k.java:2271-2296): `b.d+30` when the label is `d(0,16)`/`d(0,18)`,
else 36 — and they **initialise to 60/60 and persist between draws**
(k.java:23630). `cg` is 37 and never reassigned (k.java:23635).

Port now reads live `kCe`/`kCf`/`kCg` instead of hardcoded 36/204, and
`footerQ` no longer resets `kCe`/`kCf` to -1 after each footer draw —
matching the original's persistence.

### 2. `menuPanelRect` case-2 + `panelVisible` (proven)

The `j.c` render switch (k.java:1955-1975) routes case 2 → L918 at
k.java:2906, whose arm calls `d(93,45,214)` at k.java:2950. Added
`2 -> intArrayOf(93, 45, 214)` and `jC == 2` to `panelVisible`.

### 3. `leaveDialog` exit state (proven)

The u-machine's exits are verbatim per dlgU: u9→`l(8)`, u3→15/24, u1→8,
teardown→2, u7→2, u5→15, else 8. `leaveDialog` (only reached from the
test harness `autoDismissDialog` path) now `stateL(8)` + `kAl=false`
instead of `jC = kCy`.

### 4. ax64 spawn sites (proven-dead)

`bi[64] = -1`; zero `new i` with `ax=64` in the bytecode — the only
spawn sites are `i.a(ax, clip, …)` aK spawns and the `cr[][]` pool
respawns at i.java:34240-34840, none carrying ax64 — and zero ax64
records in any pack 6-13 `records.json`. `aa = null` is faithful; the
NpcFsm comment updated to say so.

### 5. `i.a` op-4 victim-hit-react inversion (proven bugfix)

While clearing the "block input unmined" note at Entity.kt:1465 the
original arm was re-read at i.java:14722-14772:

```
k.E.P |= 128;
if (aS.S == 284 || aS.S == 285 || aS.S == 50) return;
if (r13 != null && r13.ax == 61 && g.a(r13)) r9.c(r13);
if (r9.S != 9 && g.a() && r13 != null && r13.ax != 17
        && r13.ax != 50 && r13.ax != 61) r9.c(r13);
L423: k.A(18);
```

`r9` = `this` (the victim), `r13` = attacker — so the original makes
**the victim hit-react on the attacker** (`r9.c(r13)` = `this.c(attacker)`).
The port called `attacker.counteredBy(this)` — the attacker staggered
itself on every landed hit, so soldiers self-staggered instead of the
player flinching when struck.

Fixed both arms to `counteredBy(attacker)` (= `this.c(attacker)`),
added the `k.E.P |= 128` head (kE null-guarded), and refreshed the
`:1465` doc block + the arm comment with the correct citation
(i.java:14722-14772 — the earlier comment pointed at the disassembler's
source-page line 4540-4590 which is a different region).

Tests restaged to the faithful direction (no assertion weakening — the
old assertions encoded the bug):

- `npc strike on a metered player counters the attacker` → renamed to
  `… pays meter and staggers the player`; now asserts `player.S == 9`
  (the victim hit-reacts) + `x1 < 90` (meter pay).
- `S121-128 pounce …` asserts `player.S == 9` on the landed hit and the
  sentinel's own `r()` → S120 tail — the sentinel no longer self-staggers.

## Gates

- verifier `ok:true`
- `python3 -m unittest discover -s tests` — 57 pass
- `:core:test --rerun-tasks` — all green
- `:android:assembleDebug` — green
- `:gdx:build` — green
