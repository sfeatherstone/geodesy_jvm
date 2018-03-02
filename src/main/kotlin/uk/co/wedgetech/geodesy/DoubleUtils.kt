package uk.co.wedgetech.geodesy

import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.round

/***
 * [https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/toFixed] for Kotlin
 */
inline fun Double.toFixed(s : Int) =
        BigDecimal(this).setScale(s, BigDecimal.ROUND_HALF_UP).toDouble()


/***
 * Faster but less accurate
 */
fun Double.toFixed2(s : Int): Double {
    if (s==0) return round(this)
    val power = (10.0).pow(s)
    return round(this * power)/power
}


fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)?:""

inline fun Double.toRadians() : Double = Math.toRadians(this)
inline fun Double.toDegrees() : Double = Math.toDegrees(this)