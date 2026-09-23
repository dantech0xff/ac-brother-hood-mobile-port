---
title: Slice 139 — ax10 S55 claim-QTE zone tail (i.java:9795-10423)
phase: port
status: done
---

## What this slice ports

The full `aV()` S55 arm (Lf4→La66, i.java:9795-10423) — the trigger-zone
that drives the game's 4-lane sequence QTEs (e.g. "press ← → ✓ in order"
walls). Slice 137 ported the L611-L651 overlap head; this ports the entire
claim/QTE tail behind it.

## Mined semantics (all `proven`, i.java:9795-10423)

**Lifecycle head** (L5ea-L651):
- `i.be` set → `k.c(this)` (zone removed while camera X-lock runs)
- `ab()` (claim-bound) → `aa()` (claim-script tick) and return
- `P & 8192` → `k.c(this)` — consumed zones self-remove
- `P|=128` while idle; overlap → release stale `k.C`, `P&=~128`,
  `g.E=true`, `Z[0]!=0 → i.cu`

**Consumed reset** (L66e): `aB!=0 && n!=0` (one-shot, `n` is script-set)
→ `P|=8192`, clears `aB/n/X/bA[]`, lifts `g.E`, `Z[0]!=0 → i.cu=0`; then
`aA>0` → `h(k.s(aA))` claim bind (`P|=512|16`, `k.C=this`, double `h()`
verbatim, `k(k.s(aA))`, `P|=128`); else `bh==3 → k.c(this)`; ends
`bh==3 → k.y(65568)+k.p()+O()` else `k.v()`, return.

**Idle pass** (L737-L77a): `n!=0 → return`; `bh==3 && (aH || !am)` →
`b(2)+k.o()` (slow-mo + input lock); `aB==0` → pad flush + `P&=~128,P|=16`,
`aA=Z[4]`, `az=300`, lanes `X[r9] = (Z[1] >> ((3-r9)<<2)) & 15`, spawn
`bA[r9]` cards (skip only while `r9!=3 && m==0 && X==0`), pick first live
lane `p=X[j]`, `m=j-1`, `aE=m`.

**Lane scan** (L8a5-La66): skip if `aB>=Z[2] || m>=10 || aA==Z[3]` →
La1f auto-pass card; else `aB++`, touch-hover card state, hit =
`k.v(cs[p])` or pointer-press on card → pressed art
(`ct[X[m]]+1` key / `-1` touch), `j>3 → aA=Z[3], k.A(25)` else
`m=j; p=X[j]; j++`. Stray input (`k.t()`/`k.j()` on another lane)
→ miss art (`+2`/`-1`) + `m += 10` (resolved marker).

## Field mapping

- `i.p`/`i.n` → existing `pv`/`nl` (r8[13]/r8[8] — same JADX fields).
- `X` (int[]) is the zone's repurposed hitbox array: `X=null` → port
  zeroes the fixed `IntArray(4)` (refilled from Z[1] each armed pass).
- `i.bA[]` → `Entity.scriptPrompts` (shared 4-slot QTE card pool).
- `i.cs[]` (i.java:184) ported: `{1,2,16388,8,4112,65568,8256,128,33024,512}`.
- `tickTrigger` gains a `pad` param (k.v/k.y/k.t are pad-level).

## Gates

verifier ok:true; 57 unittests; :core:test green (10 new tests); android
APK + gdx build clean.

## Honest flags

- `k.k()` (= `w.mounted`, cm==1) picks clip9 key cards vs clip74 touch
  cards — both paths ported; the level-0 fixture drives `mounted=false`.
- `k.s(aA)`/`k.by`/`k.bz` stubs (`-1` index) mean `bindScript(-1)` is a
  faithful no-op until the claim-script table lands.
- `X` field reuse: the original nulls then re-allocs; port reuses the
  fixed array — behaviorally identical since lanes refill each pass.
