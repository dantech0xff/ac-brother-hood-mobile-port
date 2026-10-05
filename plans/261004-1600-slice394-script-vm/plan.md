---
title: "Slice 394 — the claim-script VM aa() and its big-op decoder follow the bytecode"
phase: "port"
status: "done"
slice: 394
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (aa() 64769 @0-3010; a(I[BIII)I @0-3335; bI @19346; bJ; h(I); k(I); b(I); O(); ab(); ac(); ai(); a(IIIZIII))
  - reconstructed-project/bytecode/k.javap.txt (static{} eI/ct/cs; j() pointer test)
---

# Slice 394

`i.aa()` (1 340 instructions) is the interpreter every mission script runs
through; `i.a(I[BIII)I` (1 757) decodes ops ≥ 100. Both were ported from the
simple decompile. This slice re-reads them op by op from the raw `javap -c`
(the pseudo-decompiler gained `iload v; iinc v,k` post-increment handling —
the earlier listing printed `r15 = r9[r8]` *after* the increment).

## Verified equal

Head (skip latch `k.v(131072)` → `cd[1]`, `k.m(k.ad)`, `k.A(23)`); the step
tick (`cd[1] || aH==0 || j.g % aI == 0`); block types 0/1/2 and the
`ac() && r6 == 0 → this` target rule; group header (`key u16, count u8, ops`);
the 4-byte move/camera ops 11/12/21/25/31 (incl. the `cM/cN` latch, the
-200/-120 offsets, the `k.br-400`/`k.bs-240` clamps); op13; ops 22/32 (+256
fix-up, dead for u16), 23/24 (the `P`-mask formula and the `av` rule); the
34-39 arg-op decoder and the 26 + 12 sub-switch arms (except the one below);
the group-consume rule (`key <= step`), the `cd[5]` done latch, the camera and
entity lerps (slow-mo scaling, the `P&512 || ab()` carry check for ax51/43,
the `bb[]` follower scan, the aw205 S34/35 exemption, the ax11 `Z` resync,
`t(); v()`), `cO` mount-align and the `bI()` tail, the `k.C==this && cK>0`
velocity clamp; ops 101-107, 109-112, 114 and the op-length table `eI`
(`[6,2,4,2,2,4,9,2,4,8,4,9,6,4,4]`, evaluated from `k`'s `static{}`); the
helpers `bI/bJ/h/k/b/O/ab/ac/ai` and `a(IIIZIII)`. Static scan of the 8 shipped
scripts: op13 is always the last op of its group (the one op whose operands
the VM skips when `step != key`), and none of the ops the VM ignores
(14-20, 26-30, 33, 40-44) occurs.

## Divergences fixed (`proven`)

| where | bytecode | port had |
|---|---|---|
| op37 arg 1 (9 shipped uses) | `k.l(15); if (k.aj != 7) skip; k.o(0)` @1414-1431 | `if (kAj != 7) kCount(0)` — the extra `ap[0]` tick landed on every mission except the final one |
| op22/32 anim 139 on an ax11 | `k.e(0, aw)` with `aload_0 getfield aw` @883-888 — the holder's uid | the target's uid |
| op100 sub 0 (**38 shipped ops**: z-order values 50-300, -5, -1) | @256-260: `arg == 0 → return; else az = arg` (joins sub 5 @748) | ran the sub-1 arg switch: `(0,1)`/`(0,5)` toggled `P` bits, the rest did nothing — no script ever set an actor's draw order |
| op100 sub 4 | `k.ab = true` only (@741 returns) | also `az = arg` |
| op108 / op113 (the QTE prompts) | accept = `k.v(cb[0])` or (touch) `k.c(rect 70x70)`; reject = mounted `k.v(1020)` or (touch) **`k.j()`** — a press elsewhere in the play area @1652-1853, @2862-3105 | `k.j()` sat in the accept set: in touch mode any tap in the play area passed the prompt, and no tap could fail it. The unmounted reject repaints `bA[r8]` (the current prompt) |
| op109 | coordinates read without `(short)` | `i16` |

## Tests

`Slice394Test` (11): op37 arg1 on all 8 missions, op22's uid, op100 sub 0/4, the
op108/op113 accept/reject matrix (mounted key, wrong key, tap inside / outside /
on a soft key, decides once). Stale units corrected (Slice43c sub0 / sub4).
Mutation-checked: 11 reverts, 11 killed.
