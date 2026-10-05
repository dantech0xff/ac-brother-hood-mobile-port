package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Shared helpers extracted from Slice1Test.kt (mechanical split) —
 *  private file-scope fns promoted to internal so sibling test files in the
 *  same package+module can call them. Semantics unchanged. */

internal fun asset(path: String): ByteArray =
    java.io.File("../generated/$path").readBytes()

internal fun charmap(): ByteArray = asset("fonts/charmap.bin")

fun world(charmap: ByteArray? = null, aj: Int = 0):
    Level0World {
    Entity.grabLatch = false                      // static latch — reset per world
    Entity.gq = false                             // g.q wall-run latch
    Entity.gf = null                              // g.f marker FX ref
    Entity.gE = false                             // g.E jump-tail suppress
    Entity.icu = false                            // i.cu static
    // mirror of the game's conversion of decoded assets
    val level = LevelPack.load(asset("level$aj/level$aj.aclv"))
        val clips = mutableMapOf(
            0 to Clip.load(asset("clips/clip0/clip.acpk")),
            1 to Clip.load(asset("clips/clip1/clip.acpk")),
            3 to Clip.load(asset("clips/clip3/clip.acpk")),
            7 to Clip.load(asset("clips/clip7/clip.acpk")),
            9 to Clip.load(asset("clips/clip9/clip.acpk")),
            32 to Clip.load(asset("clips/clip32/clip.acpk")),
            54 to Clip.load(asset("clips/clip54/clip.acpk")),
            64 to Clip.load(asset("clips/clip64/clip.acpk")),
            26 to Clip.load(asset("clips/clip26/clip.acpk")),
            27 to Clip.load(asset("clips/clip27/clip.acpk")),
            35 to Clip.load(asset("clips/clip35/clip.acpk")),
            10 to Clip.load(asset("clips/clip10/clip.acpk")),
            48 to Clip.load(asset("clips/clip48/clip.acpk")),
            45 to Clip.load(asset("clips/clip45/clip.acpk")),
            47 to Clip.load(asset("clips/clip47/clip.acpk")),
            31 to Clip.load(asset("clips/clip31/clip.acpk")),
            4 to Clip.load(asset("clips/clip4/clip.acpk")),
            11 to Clip.load(asset("clips/clip11/clip.acpk")),
            62 to Clip.load(asset("clips/clip62/clip.acpk")),
            25 to Clip.load(asset("clips/clip25/clip.acpk")),
            29 to Clip.load(asset("clips/clip29/clip.acpk")),
            60 to Clip.load(asset("clips/clip60/clip.acpk")),
            51 to Clip.load(asset("clips/clip51/clip.acpk")),
            63 to Clip.load(asset("clips/clip63/clip.acpk")),
            19 to Clip.load(asset("clips/clip19/clip.acpk")),
            36 to Clip.load(asset("clips/clip36/clip.acpk")),
            40 to Clip.load(asset("clips/clip40/clip.acpk")),
            20 to Clip.load(asset("clips/clip20/clip.acpk")),
            21 to Clip.load(asset("clips/clip21/clip.acpk")),
            38 to Clip.load(asset("clips/clip38/clip.acpk")),
            42 to Clip.load(asset("clips/clip42/clip.acpk")),
            46 to Clip.load(asset("clips/clip46/clip.acpk")),
            92 to Clip.load(asset("clips/clip92/clip.acpk")),
            14 to Clip.load(asset("clips/clip14/clip.acpk")),   // bi[22] ax22 zone
            15 to Clip.load(asset("clips/clip15/clip.acpk")),   // bi[26] glider
            16 to Clip.load(asset("clips/clip16/clip.acpk")),   // bi[25] ax25 player
            12 to Clip.load(asset("clips/clip94/clip.acpk")),   // z[12] = entry-012
            23 to Clip.load(asset("clips/clip23/clip.acpk")),   // ax66 platforms (bi[66]=23)
            61 to Clip.load(asset("clips/clip61/clip.acpk")),   // ax13 ropes (bi[13]=61)
            6 to Clip.load(asset("clips/clip6/clip.acpk")),     // ax10 zones (bi[10]=6, zero-pixel)
            28 to Clip.load(asset("clips/clip28/clip.acpk")),   // ax51 crates (bi[51]=28)
            44 to Clip.load(asset("clips/clip44/clip.acpk")),   // ax31 (bi[31]=44)
            13 to Clip.load(asset("clips/clip13/clip.acpk")),   // ax21 director + ax48 child (bi=13)
            52 to Clip.load(asset("clips/clip52/clip.acpk")),   // ax29 boss (bi[29]=52)
            30 to Clip.load(asset("clips/clip30/clip.acpk")),   // ax41 knockable (bi[41]=30)
            71 to Clip.load(asset("clips/clip71/clip.acpk")),   // ax61 multi-tool (bi[61]=71)
            // slice 390 — per-record clip tables (k.bk ax67 decor, k.bm[1]
            // ax7, k.bn[1] ax9): the pack-3 entries the shipped levels reach
            24 to Clip.load(asset("clips/clip24/clip.acpk")),
            34 to Clip.load(asset("clips/clip34/clip.acpk")),
            37 to Clip.load(asset("clips/clip37/clip.acpk")),
            41 to Clip.load(asset("clips/clip41/clip.acpk")),
            65 to Clip.load(asset("clips/clip65/clip.acpk")),
            66 to Clip.load(asset("clips/clip66/clip.acpk")),
            67 to Clip.load(asset("clips/clip67/clip.acpk")),
            69 to Clip.load(asset("clips/clip69/clip.acpk")),
            72 to Clip.load(asset("clips/clip72/clip.acpk")),
        )
        if (aj == 0) {
            clips[-10] = Clip.load(asset("level0/tileset-10/clip.acpk"))
            clips[-11] = Clip.load(asset("level0/tileset-11/clip.acpk"))
            clips[-12] = Clip.load(asset("level0/tileset-12/clip.acpk"))
        }
    // pack-14 entry-<aj+1> level-string table (line-delimited emit)
    // j.g(i) slot fidelity (same fix as the gdx loader, k.d→j.g): empty
    // pack slots are real table entries — filtering shifts every later
    // index (slice-316 root cause of the invisible bubbles).
    val levelStrings = java.io.File("../generated/level$aj/strings-${aj + 1}.txt")
        .readText().split("\n")
        .let { if (it.last().isEmpty()) it.dropLast(1) else it }
        .map { it.replace("\\n", "\n") }
    return Level0World(level, clips, DeterministicRandom(1L),
        levelStrings = levelStrings, charmap = charmap,
        scripts = ScriptTables.load(asset("level$aj/scripts.bin")),
        aj = aj, packLoader = { a -> missionPackFor(a) })
        .also {
            // the intro claim script's op105 dialogs (k.l(21)) suspend the
            // sim until a dismiss press — emulate an instantly-tapping player
            it.autoDismissDialog = true
        }
}

/** `I(aj)` provider for tests — the same `level<aj>` asset triplet the
 *  gdx launcher assembles in `missionPack`. */
internal fun missionPackFor(aj: Int): MissionPack {
    val strings = java.io.File("../generated/level$aj/strings-${aj + 1}.txt")
        .readText().split("\n")
        .let { if (it.last().isEmpty()) it.dropLast(1) else it }
        .map { it.replace("\\n", "\n") }
    return MissionPack(
        LevelPack.load(asset("level$aj/level$aj.aclv")), strings,
        ScriptTables.load(asset("level$aj/scripts.bin")))
}

/** `P|16` — the director's keep-live flag: the `k.I()` eligibility gate
 *  (k.java L25f/L26d) ticks the entity even when `au>=2` off-camera.
 *  Tests pin it on entities they exercise directly so the verbatim
 *  au/park gate doesn't freeze them while the camera sits at spawn —
 *  the same mechanism the original claim scripts use to keep actors live. */
internal fun keepLive(e: Entity) { e.P = e.P or 16 }

/** Teleport the player onto an ax2 checkpoint record and arm the
 *  entity's `P|16` force-tick so its `aY()` arm runs this tick —
 *  without it, the `k.I()` au/park gate freezes the checkpoint until
 *  the (unmoved) camera arrives (k.java L25f eligibility). In real
 *  play the camera delivers the same tick on approach. */
internal fun overlapCheckpoint(w: Level0World, cp: Level0World.Checkpoint) {
    // A teleport between frames must leave the boxes a finished frame
    // would: the entity loop now ticks before the player (G12), so stale
    // spawn boxes would bind the intro claim on the next tick.
    w.player.setPositionPx(cp.ak, cp.al + 5); w.player.refreshBoxes()
    w.npcs.firstOrNull { it.ax == 2 && it.aw == cp.aw }?.let(::keepLive)
}

/** Slice 379: the death screen opens when the death anim ends (S50
 *  `r()` → `k.l(12)`, g.java:2200-2219), not the frame the meter hits 0 —
 *  and a running claim keeps the player's head (and so `i(50)`) off. */
fun tickUntilFailed(w: Level0World, limit: Int = 300) {
    var t = 0
    while (!w.failed && t++ < limit) w.tick(emptyList())
}

/** Slice 385: the sim's neighbour scans walk `k.bd` — the list the last
 *  `b()` pass drew — not the `bb[]` pool. A unit test that places the
 *  neighbours by hand "paints" them: the last pass drew exactly these. */
fun Level0World.paint(vararg es: Entity) {
    drawCount = 0
    for (e in es) drawList[drawCount++] = e
}

/** Slice 388: `az()` scans `k.bd` — paint the hand-staged neighbours (every
 *  `npcs` entry, in list order) as the last pass's draw list, then scan. */
fun Level0World.scanInteract(p: Entity) {
    paint(*npcs.toTypedArray())
    playerFsm.interactScan(p)
}

fun settleIntro(w: Level0World) {
    // the spawn-intro claim script binds `k.C` in phases (~70 ticks each)
    // even with auto-dismiss dialogs; the `I()` L108 gate suspends
    // non-exempt entities while a claim is `ab()`. Fast-forward until the
    // claim stays released so tests see the post-intro play state they
    // were written against.
    var t = 0; var quiet = 0
    while (t++ < 400 && quiet < 40) {
        w.tick(emptyList())
        quiet = if (w.kC == null) quiet + 1 else 0
    }
}

/** L777's `!h(ak/20,al/20) && !h(ak/20,al/20+1) && s==null → i(25)`
 *  fall arm (i.java:6219) — drop spawned entities onto real ground the
 *  way the original's record placement does, else test subjects
 *  suspended mid-air correctly fall into S25. */
internal fun standOn(w: Level0World, e: Entity) {
    val cx = e.ak / 20
    if (w.collisionCell(cx, e.al / 20) >= 5 ||
        w.collisionCell(cx, e.al / 20 + 1) >= 5) return
    var cy = e.al / 20 + 1
    while (cy < w.level.worldH / 20 + 4) {
        if (w.collisionCell(cx, cy) >= 5) {
            e.al = cy * 20 - 1; e.O = e.al shl 8; e.refreshBoxes(); return
        }
        cy++
    }
}

/** `[key u16][cnt u8][ops]` — one script step-group. */
internal fun scriptGroup(key: Int, vararg ops: ByteArray): ByteArray {
    val g = ByteArray(3 + ops.sumOf { it.size })
    var p = 0
    u16le(key).copyInto(g, p); p += 2
    g[p++] = ops.size.toByte()
    for (o in ops) { o.copyInto(g, p); p += o.size }
    return g
}

/** Raw block: `[type][pad][uid iff 2/3][gcnt u16]` + groups + `u16(hdr)`. */
internal fun scriptBlock(type: Int, uid: Int, vararg groups: ByteArray): ByteArray {
    val hdr = if (type == 2 || type == 3) 6 else 4
    val b = ByteArray(hdr + groups.sumOf { it.size } + 2)
    var p = 0
    b[p++] = type.toByte(); b[p++] = 0
    if (type == 2 || type == 3) { u16le(uid).copyInto(b, p); p += 2 }
    u16le(groups.size).copyInto(b, p); p += 2
    for (g in groups) { g.copyInto(b, p); p += g.size }
    u16le(hdr).copyInto(b, p)
    return b
}

internal fun opAnim(a: Int) = byteArrayOf(22) + u16le(a)

/** World carrying a synthetic one-script table (`eH=[7]`, ca=0). */
internal fun scriptedWorld(vararg blocks: ByteArray): Level0World {
    val base = world()
    val tables = ScriptTables(
        eH = intArrayOf(7),
        by = arrayOf(arrayOf(*blocks)),
        bz = arrayOf(IntArray(blocks.size) { i ->
            val t = blocks[i][0].toInt() and 0xFF
            if (t == 2 || t == 3) 6 else 4
        }),
    )
    return Level0World(base.level, base.clips, DeterministicRandom(1L),
        scripts = tables).also { it.npcs.clear() }   // uid-space is the test's
}

/** ax5 claimer bound to script index 0 and stepping (`scriptStep=0`). */
internal fun claimer(w: Level0World): Entity {
    val e = Entity(5, null)
    e.aw = 700
    w.npcs.add(e)
    e.bindScript(0, w)
    e.scriptKeyStep(0, w)
    return e
}

internal fun op100(uid: Int, sub: Int, arg: Int) =
    byteArrayOf(100) + u16le(uid) + u16le(sub) + u16le(arg)

internal fun op105(r9: Int, str: Int, r11: Int) =
    byteArrayOf(105, r9.toByte()) + u16le(str) + byteArrayOf(r11.toByte())

internal fun op106(uid: Int, a: Int, b: Int, c: Int, d: Int) =
    byteArrayOf(106) + u16le(uid) + u16le(a) + u16le(b) + u16le(c) +
        byteArrayOf(d.toByte())

internal fun op107(mask: Int) = byteArrayOf(107) + u16le(mask)

internal fun op108(pass: Int, fail: Int) =
    byteArrayOf(108) + u16le(pass) + u16le(fail)

/**
 * Slice 411: `g.c(i)`'s lunge anim pick is `g.b(S)` (g.javap `c(Li;)V` @138) — an airborne / hanging
 * player takes S292 (no W/X rects: the swing starts at the press point and the CURRENT distance to the
 * wheel is its orbit radius); only a grounded one takes the 272–275 slope arcs. The capstone bots
 * therefore press CONTEXT for the counterweight only once it is within reach, otherwise the orbit
 * sweeps through the lift-row platforms and the mount drops (S277 → S0).
 */
internal fun wheelInReach(wheel: Entity?, p: Entity, reach: Double = 140.0): Boolean =
    wheel != null && Math.hypot((wheel.ak - p.ak).toDouble(), (wheel.al - p.al).toDouble()) <= reach

internal fun chaseMask297(p: Entity, w: Level0World): Int {
    // ax29 S7 grab-QTE escape (i.java:L314): `pad.v(16388)` while the
    // boss's T<=6 arms `iCj` → the T==7 `applyHit(4,…)` never lands.
    // Highest priority — the grab is what kills an idle/attacking bot.
    val boss = w.npcs.firstOrNull { it.ax == 29 }
    if (boss != null && boss.S == 7 && boss.T <= 6) return Pad.M_UP
    var mask = Pad.M_RIGHT
    when (p.S) {
        65 -> mask = Pad.M_UP + Pad.M_TAP_R
        228, 358 -> mask = Pad.M_RIGHT + Pad.M_UP
        297, 89, 90 -> mask = Pad.M_CONTEXT
        5, 79, 235, 236, 237, 238, 239, 240, 241, 242, 243,
        258, 259, 260, 261, 262, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
        60, 61, 203 -> mask = Pad.M_UP
        33 -> mask = if (p.av) Pad.M_LEFT else Pad.M_RIGHT
        22, 23, 43 -> mask =
            (if (p.av) Pad.M_LEFT + Pad.M_TAP_L else Pad.M_RIGHT + Pad.M_TAP_R)
        34 -> mask = Pad.M_RIGHT + Pad.M_UP
        else -> mask = Pad.M_RIGHT + Pad.M_UP
    }
    return mask
}

/**
 * Slice 410: the boss's two ranged attacks hurt a bot that stands and trades blows, and — since
 * `i.a(IIILi;)V`'s head upgrades a hit to the knock-down only for a player who is OFF the ground
 * (`g.b(aS.S)`, the air/hang set) — a grounded bot that is hit mid-combo no longer gets thrown
 * clear of the follow-up; it flinches in place. A human plays this fight by walking out of the
 * telegraphed spots, so the bot does the same (input only): (1) the S33 aura pulse that opens the
 * barrage harms only a player on the boss's right (`ax61HarmArm`, `p.ak >= aU.ak`) inside
 * ±60 px, so step away from the boss while it plays; (2) every barrage knife (ax61 S8) is aimed at
 * where the player stood when it was thrown and lands in the S10 shell there — leave the landing
 * spot, and the shell, while it is still falling or burning.
 * Returns the override pad mask, or null when nothing threatens.
 */
internal fun bossDodge297(p: Entity, w: Level0World, boss: Entity): Int? {
    var danger = false
    var fx = 0
    for (n in w.npcs) if (n.ax == 61) {
        when (n.S) {
            8 -> {                                              // knife in flight → lands at Z[8], Z[9]
                val dx = n.Z[8] - p.ak
                if (kotlin.math.abs(dx) < 90 && kotlin.math.abs(n.Z[9] - p.al) < 90) { danger = true; fx += dx }
            }
            10 -> {                                             // landed shell burning
                val dx = n.ak - p.ak
                if (kotlin.math.abs(dx) < 90 && kotlin.math.abs(n.al - p.al) < 90) { danger = true; fx += dx }
            }
        }
    }
    if (boss.S == 33 && p.ak >= boss.ak && p.ak - boss.ak < 110) { danger = true; fx = boss.ak - p.ak }
    return if (danger && p.aZ) (if (fx >= 0) Pad.M_LEFT else Pad.M_RIGHT) else null
}

internal fun driveDuelWin300(w: Level0World, p: Entity) {
        for (t in 0..12000) {
            val boss = w.npcs.firstOrNull { it.ax == 29 }
            if (w.iBy == 2) return
            var mask = chaseMask297(p, w)
            if (boss != null && boss.aB > 0 && boss.S != 139 &&
                !(boss.S == 7 && boss.T <= 6)) {
                if (boss.S == 26) {
                    mask = (if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT) + Pad.M_CONTEXT
                } else if (w.kC != null && w.kC!!.aw == 280 &&
                    (boss.S == 23 || boss.S == 25 || boss.S == 27)) {
                    mask = Pad.M_LEFT + Pad.M_CONTEXT
                } else if (boss.S == 15 || boss.S == 16) {
                    // slice 395: `aQ()` now spawns the pick's aura `e(4/5, …)` —
                    // the S15/S16 slash covers ±50 px in front of the boss
                    // (X ≈ [bx-60, bx+47]); step out of it while it plays
                    mask = if (boss.ak < p.ak) Pad.M_RIGHT else Pad.M_LEFT
                } else if (kotlin.math.abs(boss.ak - p.ak) < 170 &&
                    kotlin.math.abs(boss.al - p.al) < 60) {
                    mask = if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                    if (kotlin.math.abs(boss.ak - p.ak) < 70) mask += Pad.M_CONTEXT
                }
                bossDodge297(p, w, boss)?.let { mask = it }       // slice 410
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (w.jC == 15) return
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                continue
            }
            if (w.jC != 8) return
        }
    }

/** Shared phase-2 driver — east-wall rebound → ax13 uid37 rope → top. */
internal fun driveRopeClimb300(w: Level0World, p: Entity) {
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        var jumpCd = 0
        for (t in 0..12000) {
            var mask = chaseMask297(p, w)
            when {
                p.bM != null -> mask = Pad.M_UP
                !p.aZ -> mask = if (p.ak < 1250) Pad.M_RIGHT + Pad.M_UP else Pad.M_UP
                else -> mask = when {
                    p.ak >= 1250 -> Pad.M_RIGHT
                    else -> Pad.M_LEFT + Pad.M_UP
                }
            }
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                56, 60, 61, 62 -> mask = Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
            }
            if (jumpCd <= 0 && stall > 0 && stall % 25 == 0) {
                mask = if (p.ak < 1150) 16390 or Pad.M_LEFT
                       else if (p.ak in 1380..1460) 16388
                       else 16398 or Pad.M_RIGHT
                jumpCd = 25
            }
            jumpCd--
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (w.jC == 15) return
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                continue
            }
            if (w.jC != 8) return
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            lastAk = p.ak; lastAl = p.al
            if (p.al < 1460) return
        }
    }
