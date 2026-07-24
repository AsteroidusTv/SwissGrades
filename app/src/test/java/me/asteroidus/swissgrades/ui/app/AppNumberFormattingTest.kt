package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNumberFormattingTest {

    @Test
    fun englishFormatting_usesDecimalPoint() {
        assertEquals("5.0", AppLanguage.ENGLISH.formatOneOrTwoDecimals(5.0))
        assertEquals("5.25", AppLanguage.ENGLISH.formatOneOrTwoDecimals(5.25))
        assertEquals("5.00", AppLanguage.ENGLISH.formatTwoDecimals(5.0))
        assertEquals("+1.5", AppLanguage.ENGLISH.formatSignedOneOrTwoDecimals(1.5))
    }

    @Test
    fun frenchFormatting_usesDecimalComma() {
        assertEquals("5,0", AppLanguage.FRENCH.formatOneOrTwoDecimals(5.0))
        assertEquals("5,25", AppLanguage.FRENCH.formatOneOrTwoDecimals(5.25))
        assertEquals("5,00", AppLanguage.FRENCH.formatTwoDecimals(5.0))
        assertEquals("+1,5", AppLanguage.FRENCH.formatSignedOneOrTwoDecimals(1.5))
    }

    @Test
    fun signedFormatting_normalizesNegativeZero() {
        assertEquals("0.0", AppLanguage.ENGLISH.formatSignedOneOrTwoDecimals(-0.0))
        assertEquals("0,0", AppLanguage.FRENCH.formatSignedOneOrTwoDecimals(-0.0))
    }
}
