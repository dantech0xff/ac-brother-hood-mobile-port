---
title: "Port slice 122 — ax10 aU() case-34 slope rail (ride + rail-line draw)"
phase: rewrite-port
status: done
---

# Slice 122 — ax10 `aU()` case-34 (slope rail)

## Scope

Port the ax10 trigger's `aU()` arms — the branch of the trigger FSM that
runs in the entity's per-frame path (`i.a(Graphics)`, i.java:3047 →
:8989-9143). Split per the original's own structure:

- **tick arm** (`NpcFsm.kt` `tickTrigger` case `34`): the rail bind/ride/
  release logic — runs at tick frequency in the original's per-frame
  draw call.
- **draw arm** (`Level0Renderer.kt`): ax10 early-outs of `drawEntity`
  before any clip checks (zones carry no drawable clip); S34 renders
  its rail line(s) in `ROPE_COL` (`-3584205`), split at the player's
  screen x while the player is inside the rail x-range. ax10 S31's
  prompt-row arm (`i.bA` panel bank) is dormant on level 0 — no S31
  records — noted, not ported.

## Mined semantics (i.java:8989-9143, proven)

- `P |= 16`.
- `fwd = Z[0] == 0` (r8[11]).
- `free = aS.af == null || aS.af == this`.
- `near = aS.S != 9 && ((aS.af == this && aS.ak inside rail-x) ||
  a(W, W) non-strict overlap)` (i.java:540).
- Ride arm: `inside x` + player top edge `W[1] ∈ [ry-20, ry+30]` where
  `slope = ((W3-W1)<<8)/(W2-W0)`, `ry = W1 + (fwd ? (slope*(ak-W0))>>8
  : (slope*(W2-ak))>>8)` → bind: `af=e`, `al=ry+10+65`, `ah=(slope*1024)>>8`,
  `av=!fwd`, `ag=±2560`; `S50||g.t!=0 → al+=40` else `i(164)`.
- Jump-off (`v(33024)`): `af=null`, `al-=40`, `i(157)`, `ag=fwd?8192:-8192`,
  `ah=-2560`, `x()` probe, `E()` settle.
- Attack-off (`v(16388)||fwd&&v(8)||!fwd&&v(2)`): same arc without the
  `al-=40` and `x()`/`E()` probes (i.java:9094-9105).
- Release (`near` false): while `af===e && S==164` → `Z[1]==1`
  (dismount-jump flag r8[13]) → `al-=40`, `i(157)`, `ag=±8192`,
  `ah=-2560`; else plain `i(43)`, `ag=ah=0`; `af=null`.
- Draw: `j.a` line in `-3584205`; near-player split at `aS.ak`.

Level-0 carries 4 S34 records (`Z0=r8[11]∈{0,1}`, `Z1=r8[13]∈{0,1}`).

## Tests

`Slice122Test` — binds at slope height (`af`, `al=ry+75`, `ah`, `ag`,
`S164`), releases with `S43` for `Z[1]!=1`, and the `Z[1]==1` arm
produces the `157`/`±8192`/`-2560` dismount arc.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest` → 57 OK
- `:core:test`, `:gdx:build`, `:android:assembleDebug` → green
