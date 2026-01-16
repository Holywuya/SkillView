package com.skillview.expansion

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt


/**
 * 保留一位小数，四舍五入
 * @param value 原始浮点数
 */
fun Double.keepOneDecimal(): Double =
    (this * 10).roundToInt() / 10.0

fun Double.round1(): Double =
    BigDecimal(this).setScale(1, RoundingMode.HALF_UP).toDouble()

/**
 * 转换公式: (x / 100 + 1) 并保留一位小数
 * 专门用于将百分比增量（如 20 代表 +20%）转换为倍率（1.2）
 */
fun Double.toMultiplier(): Double {
    return (this / 100.0 + 1.0).keepOneDecimal()
}
/**
 * 转换公式: (x / 100 ) 并保留一位小数
 * 专门用于将百分比
 */
fun Double.toPercent(): Double {
    return (this / 100.0).keepOneDecimal()
}