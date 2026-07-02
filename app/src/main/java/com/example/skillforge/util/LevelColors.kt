package com.example.skillforge.util

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.skillforge.R

/** Background/foreground color pair for a course level (e.g. chip fill + text color). */
fun levelColors(level: String, context: Context): Pair<Int, Int> {
    val tealBg = ContextCompat.getColor(context, R.color.teal_surface)
    val tealFg = ContextCompat.getColor(context, R.color.teal)
    val amberFg = ContextCompat.getColor(context, R.color.amber_rating)
    val amberBg = ColorUtils.setAlphaComponent(amberFg, 40)
    return when (level.lowercase()) {
        "intermediate", "advanced" -> amberBg to amberFg
        else -> tealBg to tealFg
    }
}

/** Just the accent (foreground) color for a course level, e.g. for plain colored text. */
fun levelAccentColor(level: String, context: Context): Int = levelColors(level, context).second
