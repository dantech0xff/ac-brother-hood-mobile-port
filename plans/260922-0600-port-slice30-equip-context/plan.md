---
title: "port-slice30 — equip/context-action system (ao/ap/aq + k.ar/as/at/q)"
phase: rewrite-port
status: done
---

# Slice 30 — the equip/context-action system

Ports the full weapon/context subsystem mined this session: the `k.ar[]`
equip list + `k.at` action lock + `k.q()` rebuild, the `ao()` weapon cycle,
the `ap()` 65568 context dispatcher, the `aq()` mounted action, the
`S295` mounted-gauge loop arm, the `S304-306` reach-anim arm, and the
`i.y()` edge query. `bb()`'s collect arms (ax16 S30/38/39) were already
ported in slice 23/25 as `tickRequestMarker`.

## Proven originals → port

| Original | Where | Port |
|---|---|---|
| `g.g(r3)` `J \|= r3` + `k.q()` | g.java:5265 | `Entity.requestAction(mask, w)` → `w.rebuildEquip()` |
| `k.q()` rebuild `ar[5]`/`as` from `J` bits, mask-4 skipped | k.java:~4600 | `Level0World.rebuildEquip()` |
| `k.p(r3)` lowest-set-bit index | k.java:4631 | `equipList.indexOf` (sorted-dense ≡) |
| `g.h(r4)` + `k.at=1` | g.java:5270 | `Entity.requestH(r4, w)` → `w.actionLock = 1` |
| `g.o()` grounded/mounted gate | g.java:6369 | `Entity.groundOrVehicle()` (`aZ`/`standingOn.ax∈{51,15,43}`; `g.a` static → `standingOn` inferred) |
| `g.ao()` weapon cycle 131072/button → `h(ar[(p(I)+1)%as])` + A(23) | g.java:3792 | `Entity.cycleEquip` (`Pad.M_CYCLE` + `w.touchRect(355,197,30,26)`) |
| `g.ap()` I-dispatch (1→S67/81, 8→i303, 2→i286+A29; head `I==4&&aA<2→h(1)`) | g.java:3817 | `Entity.contextDispatch` |
| `g.aq()` mounted action (W-box reach 306/305/304, `K=6`, flat `bu[au]` dmg, knife `M+30`) | g.java:3958 | `Entity.mountedInteractAction` |
| `i.y()` directional edge flag | i.java:1211 | `Entity.edgeFlag` |
| S295 arm: `J&8→h(8)`, A(19), `I==8→aB()`+`v(65568)&&g→aq()`, `y()→a=null;a(0);i=false;k.ae=this` | g.java:2541 (L204) | PlayerFsm `295 ->` arm |
| S304-306 arm: `r()→K=0;cN=0;i(295)` | g.java:2560 (L218) | PlayerFsm `304,305,306 ->` arm |
| S297 + S296/276/278-281/285/288-290/307-309/314/316 → shared L1917/L1926 tail | g.java:3455+ | `297 -> {}` falls to `postTail` |
| L2048 `o()→ao()`; L2051-54 `aA` bookkeeping; L2057 `z&&!bn&&!E→ap()` | g.java:~3711 | `postTail` tail arms |
| `k.c(x,y,w,h)` view touch-rect | k.java (ao() button) | `w.touchRect` — lastTouch coords, `inferred` |
| `g.a`/`k.ae`/`g.i`/`g.E`/`k.C` statics | g/i/k statics | `w.vehicle`/`w.aeRef`/`w.iFlag`/`w.eFlag`/`w.cEntity` |

## Design decisions

- **The I==1 sword arm moved**: slice 5 inlined `ap()`'s `I==1` arm inside
  `groundedTail`; the real call site is the L2057 shared-tail arm gated by
  `z && i.bn==false && E==false`. `p.z` is now per-arm state — set true
  inside `groundedTail` (the locomotion arms), cleared at tick head, so
  `ap()` only fires from grounded control and can no longer stomp the
  combo `R=183/184` finisher write (regression caught by the slice-6
  finisher test).
- **`g.a` vs `i.a`**: `o()`/`aq()`'s `a` is the g-static vehicle link;
  the port approximates it with `standingOn` (same vehicle/mount role)
  and a world-level `vehicle` field for the L204 `a=null` drop.
- **`k.E.K()`** (held-entity release inside the I==1 arm) is flagged
  unported — `k.E` is the entity the player carries; no producer exists
  yet.
- `ap()`'s `r6` flag = `ac?.ax != 10` (decompiler-jumbled; inferred — the
  context press is blocked while riding an ax10 zipline).
- Bug found by tests: Kotlin `shr`/`shl` bind *below* `+`/`-`, so
  `(W0+W2) shr 1 - mid` parsed as `(W0+W2) shr (1-mid)` — r05 degenerate;
  fixed with parens (other call sites audited — all parenthesized).

## Gates

- `./gradlew :core:test` — **131 green** (+6 slice-30 tests)
- `verify-static-reconstruction.py` — `ok:true`
- `python3 -m unittest discover -s tests` — 57 OK
- Emulator: `level0: 637 records, npcs=454`, boot clean

## Not yet ported (flagged)

- `k.E`/`E.K()` held-entity carry/release (no producer).
- `k.c(x,y,w,h)` on real view buttons — mapped to `lastTouchX/Y` view-space;
  the 355,197,30,26 weapon-cycle button isn't drawn yet (touch works via
  `touchRect` once the HUD button is ported).
- `i.aG()` (aF mirror, i.java:9218) and `bb()`'s L21 marker-state switch
  (S15-33 non-collect arms) — separate slice.
- `k.au` weapon slot is still `0` — `bu[]` reads the 300 column until the
  weapon-index producer is mined.
