package com.example.skillforge.util

import java.util.Locale

fun formatDurationMinutes(minutes: Int): String {
    if (minutes < 60) return "${minutes}m"
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (remaining == 0) "${hours}h" else "${hours}h ${remaining}m"
}

fun formatDurationHoursShort(hours: Double): String {
    val whole = hours.toInt()
    val fraction = hours - whole
    return if (fraction == 0.0) "${whole}h" else String.format(Locale.US, "%.1fh", hours)
}

fun formatCourseHours(hours: Double): String = "${formatDurationHoursShort(hours)} total"

fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun formatStudentCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
