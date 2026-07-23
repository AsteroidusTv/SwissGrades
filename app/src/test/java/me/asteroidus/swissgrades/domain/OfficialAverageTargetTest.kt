package me.asteroidus.swissgrades.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfficialAverageTargetTest {

    @Test
    fun parse_acceptsWholeAndHalfGradeTargetsWithEitherDecimalSeparator() {
        assertEquals(1.0, OfficialAverageTarget.parse("1")!!, 0.0)
        assertEquals(4.0, OfficialAverageTarget.parse("4.0")!!, 0.0)
        assertEquals(4.5, OfficialAverageTarget.parse("4,5")!!, 0.0)
        assertEquals(6.0, OfficialAverageTarget.parse("6.0")!!, 0.0)
    }

    @Test
    fun parse_rejectsNonOfficialMalformedAndOutOfRangeTargets() {
        assertNull(OfficialAverageTarget.parse("5.25"))
        assertNull(OfficialAverageTarget.parse("5.74"))
        assertNull(OfficialAverageTarget.parse("5.99"))
        assertNull(OfficialAverageTarget.parse("target"))
        assertNull(OfficialAverageTarget.parse("6.5"))
    }

    @Test
    fun normalizeLegacy_usesOfficialHalfGradeRounding() {
        assertEquals(5.5, OfficialAverageTarget.normalizeLegacy(5.25)!!, 0.0)
        assertEquals(5.5, OfficialAverageTarget.normalizeLegacy(5.74)!!, 0.0)
        assertEquals(6.0, OfficialAverageTarget.normalizeLegacy(5.75)!!, 0.0)
        assertNull(OfficialAverageTarget.normalizeLegacy(6.5))
    }
}
