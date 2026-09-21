---
title: NPC/Enemy FSM and Animation Map Mining
description: >-
  Deep-pass the shared ~170-state NPC/enemy FSM (ax 11/17/23/47/50/73) inside
  i.I(), plus i.i()/aa.b(S) state→animation mapping and remaining per-type
  handler semantics. Static-only.
status: in_progress
priority: P1
effort: large
branch: devin/1789984451-npc-fsm-mining
tags: [reverse-engineering, documentation, rewrite]
created: '2026-09-21'
createdBy: 'devin'
source: user
---

# NPC/Enemy FSM and Animation Map Mining

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [NPC FSM states](./phase-01-npc-fsm-states.md) | Completed |
| 2 | [State→animation map](./phase-02-state-animation-map.md) | Completed |
| 3 | [Docs + PR](./phase-03-docs-pr.md) | Completed |
