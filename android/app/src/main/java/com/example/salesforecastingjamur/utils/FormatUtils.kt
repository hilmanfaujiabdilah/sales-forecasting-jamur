package com.example.salesforecastingjamur.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {

    fun formatKg(value: Float): String =
        String.format(Locale("id"), "%.1f kg", value)

    fun formatAngka(value: Float): String =
        String.format(Locale("id"), "%.2f", value)

    fun formatPersen(value: Float): String =
        String.format(Locale("id"), "%.2f%%", value)

    fun formatBaglog(value: Int): String =
        NumberFormat.getNumberInstance(Locale("id")).format(value) + "baglog"
}