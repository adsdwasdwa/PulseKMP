package app.pulsefit.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrackerEngineTest {
    @Test
    fun streakAllowsTwoSkippedDaysBetweenHits() {
        val today = LocalDate(2026, 6, 9)
        val status = TrackerEngine.streakFor(
            hitDates = listOf(
                LocalDate(2026, 6, 1),
                LocalDate(2026, 6, 4),
                LocalDate(2026, 6, 7),
                LocalDate(2026, 6, 9)
            ),
            today = today,
            maxSkips = 2
        )

        assertTrue(status.isActive)
        assertEquals(4, status.count)
        assertEquals(2, status.skipBufferRemaining)
    }

    @Test
    fun streakResetsAfterThirdMissedDay() {
        val today = LocalDate(2026, 6, 9)
        val status = TrackerEngine.streakFor(
            hitDates = listOf(LocalDate(2026, 6, 5)),
            today = today,
            maxSkips = 2
        )

        assertFalse(status.isActive)
        assertEquals(0, status.count)
        assertEquals(0, status.skipBufferRemaining)
    }
}
