package com.skillview.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round(decimals: Int = 1): Double =
    BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()

fun Double.toMultiplier(): Double =
    (this / 100.0 + 1.0).round()

fun Double.toPercent(): Double =
    (this / 100.0).round()