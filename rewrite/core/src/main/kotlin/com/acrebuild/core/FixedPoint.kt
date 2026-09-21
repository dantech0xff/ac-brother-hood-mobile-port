package com.acrebuild.core

/**
 * Integer 8.8 fixed-point helpers. The recovered engine keeps position and
 * velocity in 8.8 fixed point (docs/modern-mobile-technical-design.md section
 * 8 "Numeric model"); floats stay outside the simulation.
 */
object FixedPoint {
    const val FRACTION_BITS = 8
    const val SCALE = 1 shl FRACTION_BITS

    fun toFixed(pixels: Int): Int = pixels shl FRACTION_BITS
    fun toPixels(fixed: Int): Int = fixed shr FRACTION_BITS

    fun mulFixed(a: Int, b: Int): Int = ((a.toLong() * b.toLong()) shr FRACTION_BITS).toInt()
}
