package com.example.salesforecastingjamur.utils

import android.graphics.Color
import com.example.salesforecastingjamur.data.model.AgregasiBulanan
import com.example.salesforecastingjamur.data.model.Prediksi
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

object ChartHelper {

    fun setupLineChart(chart: LineChart, agregasi: List<AgregasiBulanan>, prediksi: Prediksi?) {

        val labels = agregasi.map { it.periode.substring(0, 7) }.toMutableList()
        val aktualEntries = agregasi.mapIndexed { i, a -> Entry(i.toFloat(), a.totalPenjualan) }.toMutableList()
        val datasets = mutableListOf<ILineDataSet>()

//        Garis aktual
        val aktualSet = LineDataSet(aktualEntries, "Aktual").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        datasets.add(aktualSet)

//        Garis prediksi (jika ada)
        prediksi?.let {
            val lastIdx = agregasi.size.toFloat()
            val predEntries = listOf(
                Entry(lastIdx, it.predPeriode1),
                Entry(lastIdx + 1f, it.predPeriode2)
            )
            labels.add(it.periodePred1.substring(0,7))
            labels.add(it.periodePred2.substring(0,7))

            val predSet = LineDataSet(predEntries, "Prediksi").apply {
                color = Color.parseColor("#FF6F00")
                setCircleColor(Color.parseColor("#FF6F00"))
                lineWidth = 2f
                circleRadius = 3f
                enableDashedLine(10f, 5f, 0f)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            datasets.add(predSet)
        }

        chart.apply {
            data = LineData(datasets)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -30f
                granularity = 1f
                setDrawGridLines(false)
                textSize = 9f
            }
            axisRight.isEnabled = false
            axisLeft.textSize = 9f
            description.isEnabled = false
            legend.textSize = 10f
            setTouchEnabled(true)
            setPinchZoom(false)
            animateX(800)
            invalidate()
        }
    }

//    Bar chart untuk menu penjualan/produksi
    fun setupBarChart(chart: BarChart, agregasi: List<AgregasiBulanan>){
        val label = agregasi.map { DateUtils.formatPeriode(it.periode) }
        val entries = agregasi.mapIndexed { i, a -> BarEntry(i.toFloat(), a.totalPenjualan)}

        val barSet = BarDataSet(entries, "Penjualan (kg)").apply {
            color = Color.parseColor("#4CAF50")
            setDrawValues(false)
        }

        chart.apply {
            data = BarData(barSet).also { it.barWidth = 0.6f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(label)
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -30f
                granularity = 1f
                setDrawGridLines(false)
                textSize = 9f
            }
            axisRight.isEnabled = false
            axisLeft.textSize = 9f
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            animateY(600)
            invalidate()
        }
    }
}