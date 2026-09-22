# Port slice 11 — ledge vault `a(257,8)` + wall-climb `am()`→S63

## Mining — proven

- `i.a(S, flags)` (i.java:853): full 12-bit re-center cascade — đã có
  `enterStateMasked` (slice 3+). `a(257,8)` = i(257) + `ak -= ak%20-10`
  (snap grid-center); `a(63,16385)` = deferred i(63) +
  `al = ((W[3]+10)/20)*20 - 1`.
- `am()` (g.java:301): `ag>0 && aV==19 && aR==0` → `av=true`,
  `ak=(ak/20)*20`; `ag<0 && aW==19 && aR==0` → `av=false`,
  `ak=(ak/20)*20+20`; rồi `aj=ah=ag=0; a(63,16385)` — wall-climb-up.
  (ag==0 ledge variant + `k.u(33024)` tie-in unmined — tail L19+.)
- `a(257,8)` call sites (g.java:2874, 5165): DOWN **edge** `v(33024)` +
  `al+=20;x();al-=20` shifted probe → `aQ∈{20,5}` support, `aR==0`, cell
  `(W[0]/20-2 | W[2]/20+2, W[3]/20+1)` non-solid → vault-drop S257.
  Placement `inferred` (grounded tail).
- S257 arm (g.java:3317 L1770): `r()` → `al += aa.c(S,T)+10`,
  `ak += av? -aa.b(S,T) : +aa.b(S,T)`, `a(0)` — clip frame dx/dy exit.
- DOWN-held→`am()` call site (g.java:2886): `u(33024)&&am()→ab=null`
  (platform link `ab` unported — drop op omitted).

## Port (PlayerFsm.kt)

- `wallClimb()` = am() ag≠0 half; `ledgeDrop257()` = L692 verbatim
  (shift-probe via `probeCells`); `ledgeDropArm` = S257 exit; `63` arm
  end→i(0) **inferred** (L390 generic tail shared with many states).
- groundedTail order: down-edge→wallClimb, down-held→ledgeDrop257,
  rồi attack entry như cũ.

## Kiểm chứng

- 42 `:core:test` xanh (không có test ledge mới — cần scenario cell;
  geometry giống sites gốc).

## Gaps

- `am()` ag==0 half (standing ledge + `aR==19` + down-tap → climb-down),
  `ab` link drop, S258/259/261 hang-shimmy chain, `cu` flag, S62 post-63.
