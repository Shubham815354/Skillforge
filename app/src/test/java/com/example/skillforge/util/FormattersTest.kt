package com.example.skillforge.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `formatDurationMinutes under an hour shows minutes only`() {
        assertEquals("8m", formatDurationMinutes(8))
    }

    @Test
    fun `formatDurationMinutes on exact hour hides minutes`() {
        assertEquals("2h", formatDurationMinutes(120))
    }

    @Test
    fun `formatDurationMinutes over an hour shows hours and minutes`() {
        assertEquals("1h 15m", formatDurationMinutes(75))
    }

    @Test
    fun `formatDurationHoursShort with whole number omits decimal`() {
        assertEquals("9h", formatDurationHoursShort(9.0))
    }

    @Test
    fun `formatDurationHoursShort with fraction keeps one decimal`() {
        assertEquals("6.5h", formatDurationHoursShort(6.5))
    }

    @Test
    fun `formatCourseHours with whole number omits decimal`() {
        assertEquals("9h total", formatCourseHours(9.0))
    }

    @Test
    fun `formatCourseHours with fraction keeps one decimal`() {
        assertEquals("6.5h total", formatCourseHours(6.5))
    }

    @Test
    fun `formatStudentCount below one thousand is unchanged`() {
        assertEquals("820", formatStudentCount(820))
    }

    @Test
    fun `formatStudentCount in thousands abbreviates`() {
        assertEquals("18.4k", formatStudentCount(18420))
    }

    @Test
    fun `formatStudentCount in millions abbreviates`() {
        assertEquals("1.2M", formatStudentCount(1_200_000))
    }
}
