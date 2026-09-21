# Slice 3 — combat loop: alert → chase → windup → strike → player damage

Track: `260921-1515-port-slice3-combat` on branch `devin/1789997650-port-slice3`.

Goal: close the loop that makes the ax11 soldiers dangerous — chase reaches
melee range → windup → contact applies the `i.a(op,…,attacker)` effect
dispatcher on the player → knockdown/hit-react.

## Ported semantics (source: `reconstructed-project/src/simple/{g,i}.java`)

### `i.a(int op, int arg, int arg2, i attacker)` — `Entity.applyHit`
The shared effect/damage dispatcher (i.java:4446+). Ops transcribed:
- **4** melee-contact query: if the target is mid-attack (`g.b(S)` table →
  `PlayerFsm.isAttackState`) it rewrites to op 18. Blocking (`g.a()`) would
  counter via `c(attacker)` — **deferred** (block input + `c()` QTE path are
  unmined). Passive targets fall through to the `k.A(18)` hurt-mark,
  surfaced as `hitsTaken++` (instrumentation, `inferred`).
- **18 / 20** hit-interrupt / knockdown → `i(43)` (tumble-fall arm from
  slice 2).
- **26** launch: `av=attacker.av; ag=±4096; ah=-4096; aj=1536; a(43,32)`.
- **29** stumble: `av=attacker.av; i(10); ag=∓1536`.
- **34** damage-mark: zero velocity + `a(8,5,14,…)` floatie + `k.A(11)` sfx
  hooks — recorded as `hitsTaken++` (spawners deferred).

### `aB()` melee application (i.java, subset) — in `NpcFsm.tick`
Every tick: if the NPC's attackbox `X` is non-degenerate (`X[0]!=X[2]`) and
overlaps the player's hitbox `W`, issue `k.aS.a(player.S==43 ? 20 : 4, l, 0,
this)` — the airborne-vs-grounded op split is verbatim.

### ax11 attack completion
- S23 = windup-approach (`ag=∓512`, `Q()` facing) → `r()` → `i(12)`
  (contact arm entry).
- S12 = L478 contact/counter arm subset: on anim end `i(23); aC=10`
  re-approach (the player-side counter window `a(34,…)` requires
  `player.S==6` — unreachable until the player attack input is mined).
- L451 chase already drops into `i(23)` on `W`-overlap with `aC=3`.

## Deferred (explicit, honest)
- Player attack input: the swing-entry key inside `g.e()` is unmined
  (`v(65568)` is the *context/assassinate* trigger → `c(g)` lock-on arc, not
  the standing slash). Next mining target.
- Player HP field: still unlocated (`g.x[]` is a stats array — op21 writes
  `g.x[1]`; hp index unconfirmed) → no death/respawn yet.
- `g.a()` block check, `c(i)` counter/QTE arc, `j()` NPC damage intake
  (needed for player→NPC hits), difficulty tables `bu[]/J[]`.
- Alert propagation `k.aA=60` broadcast, `aC()` chase-timeout mode `j`.

## Verification
- `:core:test` 35 green — new contract test: soldier alerts → chases →
  strikes the player (`hitsTaken>0` / S43 knockdown observed).
- Verifier `ok:true`; `unittest` 57 green.
- Emulator boot + hold-right still clean (`reports/slice3.png`).
