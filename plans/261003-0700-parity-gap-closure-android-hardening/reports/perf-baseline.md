---
title: "Performance baseline — container side (device numbers pending)"
phase: 5
status: partial
date: 2026-10-03
---

# Performance baseline

The device half of Phase 5 step 6 (GLThread CPU, heap on device, tick rate
on the emulator host) needs an emulator host; this report records what the
cloud container can measure, so the device run has a reference.

## APK (`claude/phase2-faithful-ai`, 2026-10-03)

| Build | Size | Notes |
|---|---:|---|
| `android-debug.apk` | 12,838,007 B | debuggable, info logging |
| `android-release-unsigned.apk` | 11,773,168 B | R8 off, errors-only logging; 13,289 entries, 13,194 under `assets/` |

Almost all of the size is the decoded asset tree (`rewrite/generated/`) and
the four ABI copies of `libgdx.so`.

## Core tick cost (JVM, not a device)

Host: cloud container, 4 vCPU, OpenJDK 21.0.11. Method: a throwaway test
(not committed) boots each mission world through the shared `world(aj)`
helper, runs 300 warm-up ticks with no input, then times 3,000 ticks of
`Level0World.tick(emptyList())` with `System.nanoTime()`.

| Mission | Entities | Boot | Tick avg | p50 | p99 | max |
|---|---:|---:|---:|---:|---:|---:|
| m0 | 636 | 133 ms (first, cold JIT) | 54 µs | 26 µs | 295 µs | 4.9 ms |
| m2 | 566 | 11 ms | 84 µs | 53 µs | 436 µs | 6.6 ms |
| m3 | 691 | 12 ms | 107 µs | 79 µs | 471 µs | 2.5 ms |
| m5 | 739 | 6 ms | 113 µs | 90 µs | 446 µs | 6.4 ms |
| m6 | 848 | 7 ms | 58 µs | 40 µs | 168 µs | 1.9 ms |
| m7 | 318 | 5 ms | 97 µs | 91 µs | 299 µs | 3.8 ms |

- The flying missions (m1, m4) open on a dialog and, idle, end in a
  knockout, so an idle run does not measure their play loop; time them with
  the capstone bots on the device run.
- Against the 62 ms tick budget the core logic costs well under 1 % on this
  host; the max column is GC/JIT noise. Rendering (`Level0Renderer`) is not
  covered — that is the device measurement.
- Clips load once in `Level0Game.create()`; a world boot adds only a few MB
  of heap on the JVM. Lazy clip loading is not warranted unless the device
  run shows memory pressure.

## Pending (emulator host / real device)

Tick rate (`jG` per second) during play, GLThread CPU, heap after boot and
after each mission load, frame-time histogram, cold-start time.
