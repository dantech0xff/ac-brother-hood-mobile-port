package com.acrebuild.core

/**
 * J2ME pad-word semantics for the port — now the verbatim `k.java`
 * word model (slice 79, proven from k.java:553-574 / :5585-5621 /
 * :1594-1609):
 *
 *   `eK` = this-frame accumulated edge word (`E(i)` writes it)
 *   `eL` = sticky edges awaiting the pointer-release flush
 *   `eN` = edges flushed by `pointerReleased` (release latch)
 *   `bB` = committed edge word — `v(mask)` = `(bB & mask) != 0`
 *   `bC` = sticky held word — `u(mask)` = `(bC & mask) != 0`
 *   `eM` = committed release latch — `w(mask)` = `(eM & mask) != 0`
 *   `eO/eP` = last nonzero `bB` + frames since — `x()` double-press
 *   `bD` = hold-duration counter (`bC != eL` resets it)
 *
 * `E(i)` (k.java:553): `v(); eK |= i; <bh3 remap>; bC |= eK; eL |= eK`
 * — every call wipes all six words, so the LAST `E()` in a frame wins.
 * Frame tail (k.java:1594-1608): `if (bB!=0){eO=bB;eP=0} eP++;
 * if (bC!=eL)bD=-1; if (bC!=0)bD++; bC=eL; bB=eK; eM=eN; eK=eN=0`.
 *
 * `bC`/`eL` are STICKY — `u()` keeps reporting the last edge until a
 * release flush (`eN=eL;eL=0` on pointerReleased, k.java:500-505) or a
 * `v()`/`y()` clear. There is no polled "held" bit: one `E()` per frame
 * is the whole model.
 *
 * Mask values (proven): wheel cells `2<<iJ` iJ∈0..8 → 2..512
 * (k.java:489/:511); soft/menu/context literals E(4112)/E(8256)/
 * E(131072)/E(262144)/E(65568); read masks are composites so either
 * the cell bit or the soft-key bit satisfies them:
 *   4112  = LEFT (bit12 + ML cell16)     8256  = RIGHT (bit13 + MR cell64)
 *   16388 = UP (bit14 + TC cell4)        33024 = DOWN (bit15 + BC cell256)
 *   2/8   = TL/TR tap edges              32    = MC context cell
 *   65568 = context (bit16 + cell32)     131072= cycle soft-key
 *   262144= pause/menu soft-key          94324 = any-direction group
 */
class Pad {
    // -- the six verbatim words + trackers (k.java:119-125 statics) ------
    var eK = 0            // k.eK
    var eL = 0            // k.eL
    var eN = 0            // k.eN
    var bB = 0            // k.bB — v() source
    var bC = 0            // k.bC — u() source
    var eM = 0            // k.eM — w() source
    var eO = 0            // k.eO — last nonzero bB
    var eP = 99           // k.eP — frames since eO
    var bD = 0            // k.bD — hold-duration counter

    /** `k.v()` (k.java:5609, proven): clear all six words. */
    fun clearLatches() { eL = 0; bC = 0; bB = 0; eM = 0; eK = 0; eN = 0; pendingPress = 0 }

    /** `k.E(int)` (k.java:553, proven). `bh3Remap` applies the autoscroll
     *  wheel remap {2→20, 8→68, 128→272, 512→320} gated by
     *  `j.c==8 && bh[aj]==3 && !k()`. */
    fun e(mask: Int, bh3Remap: Boolean = false) {
        clearLatches()
        eK = mask
        if (bh3Remap) eK = when (mask) { 2 -> 20; 8 -> 68; 128 -> 272; 512 -> 320; else -> eK }
        bC = bC or eK
        eL = eL or eK
    }

    private var pendingPress = 0
    /** Legacy queue — OR'd into the next `commit(nextHeld)` edge (same as
     *  `E()` for the pointer path's purposes: an injected press). World
     *  input uses `e()` directly; this stays for test/FSM injection. */
    fun queuePress(mask: Int) { pendingPress = pendingPress or mask }

    /** `pointerReleased` tail (k.java:501-505, proven): flush `eL`→`eN`. */
    fun releaseFlush() { if (eL != 0) { eN = eL; eL = 0 } }

    /** Frame commit (k.java:1594-1608, proven): fold eK/eL/eN into the
     *  visible words; tracks eO/eP (double-press) and bD (hold). */
    fun commit() {
        if (pendingPress != 0) { e(pendingPress); pendingPress = 0 }
        if (bB != 0) { eO = bB; eP = 0 }
        eP++
        if (bC != eL) bD = -1
        if (bC != 0) bD++
        bC = eL; bB = eK; eM = eN
        eK = 0; eN = 0
    }

    /** Legacy `commit(nextHeld)` (pre-slice-79 driver/test API): fold a
     *  raw "held mask this tick" into the words — bB = newly-down bits,
     *  eM = released bits, bC/eL = mask. Real input flows through
     *  `e()`/`releaseFlush()`/`commit()`; this keeps direct FSM tests
     *  expressing press/hold/release without synthesizing pointer events. */
    fun commit(nextHeld: Int) {
        if (bB != 0) { eO = bB; eP = 0 }
        eP++
        bB = (nextHeld and bC.inv()) or pendingPress
        pendingPress = 0
        eM = bC and nextHeld.inv()
        eL = nextHeld
        if (bC != eL) bD = -1
        if (bC != 0) bD++
        bC = eL
        eK = 0; eN = 0
    }

    /** `k.y(int)` (k.java:5620, proven): consume bits from both words. */
    fun y(mask: Int) { bC = bC and mask.inv(); bB = bB and mask.inv() }

    fun u(mask: Int): Boolean = bC and mask != 0            // k.u()
    fun v(mask: Int): Boolean = bB and mask != 0            // k.v(int)
    fun w(mask: Int): Boolean = eM and mask != 0            // k.w(int)
    fun x(mask: Int): Boolean =
        bB and mask != 0 && bB == eO && eP < 5              // k.x()

    /** `k.u()` (k.java:5589→:5605, proven): any press that isn't a menu key. */
    fun anyPress(): Boolean = bB != 0 && bB and M_CYCLE == 0 && bB and M_PAUSE == 0

    // -- compat views (old field names → the backing words) --------------
    var edge: Int get() = bB; set(v) { bB = v }
    var held: Int get() = bC; set(v) { bC = v }
    var released: Int get() = eM; set(v) { eM = v }
    /** double-press window remainder for g.S25 checks (`aA`, inferred). */
    val aA: Int get() = if (eP < 5) 5 - eP else 0
    val tap: Int get() = if (eP < 5 && bB == eO) bB else 0

    companion object {
        const val M_LEFT = 4112
        const val M_LEFT_ALT = 128
        const val M_RIGHT = 8256
        const val M_RIGHT_ALT = 512
        const val M_UP = 16388
        const val M_DOWN = 33024
        const val M_TAP_L = 2
        const val M_TAP_R = 8
        const val M_ACTION_FAMILY = 16398
        const val M_ANY_DIR = 94324
        /** Context/attack key (proven: `v(65568)` drives combos + assassinate). */
        const val M_CONTEXT = 65568
        /** Weapon-cycle edge (`ao()` at g.java:3792). */
        const val M_CYCLE = 131072
        /** Pause/menu soft-key (`E(262144)` at k.java:1054/:2288/:2607). */
        const val M_PAUSE = 262144
    }
}
