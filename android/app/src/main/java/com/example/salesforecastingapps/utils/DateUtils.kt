package com.example.salesforecastingapps.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatTanggal(raw: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
        val output = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
        output.format(input.parse(raw)!!)
    } catch (e: Exception) {
        raw
    }

    fun formatPeriode(raw: String?): String {  // ← tambahkan ? pada parameter
        if (raw == null) return "-"  // ← return default jika null
        return try {
            val input  = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
            val output = SimpleDateFormat("MMMM yyyy", Locale("id"))
            output.format(input.parse(raw.substring(0, 10))!!)
        } catch (e: Exception) { raw }
    }

    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale("id")).format(Date())

    fun bulanDepan(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        return SimpleDateFormat("yyyy-MM", Locale("id")).format(cal.time)
    }

    fun namaBulan(bulan: Int): String {
        val bulanList = listOf(
            "Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"
        )
        return if (bulan in 1..12) bulanList[bulan -1] else "-"
    }
}