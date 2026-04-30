package com.example.salesforecastingjamur.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

//    Mengecek apakah ini adalah tanggal terakhir bulan berjalan
    fun isAkhirBulan(): Boolean {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun formatTanggal(raw: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
        val output = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
        output.format(input.parse(raw)!!)
    } catch (e: Exception) {
        raw
    }

    fun formatPeriode(raw: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
        val output = SimpleDateFormat("MMMM yyyy", Locale("id"))
        output.format(input.parse(raw.substring(0, 10))!!)
    } catch (e: Exception) {
        raw
    }

    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale("id")).format(Date())

    fun namaBulan(bulan: Int): String {
        val bulanList = listOf(
            "Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"
        )
        return if (bulan in 1..12) bulanList[bulan -1] else "-"
    }
}