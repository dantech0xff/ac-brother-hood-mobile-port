# Port slice 13 — corpse-drop `au()` (Z[21] linked-entity activation)

## Mining — proven (i.java:7738, k.java:5890 `q()`)

- `au()`: ax ∈ {11,17,73} && `aB<=0` && `Z[21] != -1` → `k.q(Z[21])`
  resolves the linked entity by `aw` uid → `r0.ak=ak; r0.al=al;
  r0.P &= -33` (clear bit5 = inactive flag) → `Z[21]=-1` (one-shot).
- `k.q(uid)` scans `bb[]` (entity slots) + `aS` for `aw==uid`.
- ax11 init: `Z[21] = r8[19]` (i.java:3061). Level-0 records all carry
  f19=-1 → dead code locally, correct forward-port for drop levels.

## Port

- `initSoldier`: `Z[21] = rf(19)`.
- `NpcFsm.corpseDrop(e)` = `au()`; fired at the `aB<=0 → i(139)` corpse
  transition (all death paths funnel there).
- `LevelCellSource.npcs` added (`k.q` source); `Level0World.npcs`
  now `override`.

## Kiểm chứng

- 43 `:core:test` xanh. No new contract test — level-0 has no drops
  (all f19=-1); behavior exercised once a drop-bearing level loads.

## Gaps

- Pickup entities (ax37/19) still don't spawn (ENTITY_CLIP filter) —
  drops will teleport an entity that isn't drawn/simmed yet.
