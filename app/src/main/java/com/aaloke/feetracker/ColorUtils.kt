package com.aaloke.feetracker

import android.graphics.Color

object ColorUtils {
    private val colors = listOf(
        "#FFDDC1", "#FFABAB", "#FFC3A0", "#FFCCB6", "#C7CEEA",
        "#BEEBE9", "#F7DAD9", "#B5EAD7", "#E2F0CB", "#FFD3B6"
    )
    private var lastColorIndex = -1

    fun getNextColor(): Int {
        lastColorIndex = (lastColorIndex + 1) % colors.size
        return Color.parseColor(colors[lastColorIndex])
    }
}