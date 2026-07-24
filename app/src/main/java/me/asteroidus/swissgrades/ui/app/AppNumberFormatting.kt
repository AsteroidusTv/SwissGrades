package me.asteroidus.swissgrades.ui.app

import java.util.Locale
import kotlin.math.abs

internal fun AppLanguage.formatOneOrTwoDecimals(value: Double): String {
    val locale = numberLocale()
    return if (value % 1.0 == 0.0) {
        "%.1f".format(locale, value)
    } else {
        "%.2f".format(locale, value).trimEnd('0')
    }
}

internal fun AppLanguage.formatTwoDecimals(value: Double): String {
    return "%.2f".format(numberLocale(), value)
}

internal fun AppLanguage.formatSignedOneOrTwoDecimals(value: Double): String {
    val normalized = if (abs(value) < 1e-9) 0.0 else value
    val prefix = if (normalized > 0.0) "+" else ""
    return prefix + formatOneOrTwoDecimals(normalized)
}

private fun AppLanguage.numberLocale(): Locale {
    return when (this) {
        AppLanguage.ENGLISH -> Locale.US
        AppLanguage.FRENCH -> Locale.FRANCE
    }
}
