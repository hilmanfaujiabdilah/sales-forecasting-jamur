package com.example.salesforecastingapps.utils

import android.graphics.Color
import com.example.salesforecastingapps.data.model.AgregasiBulanan
import com.example.salesforecastingapps.data.model.Prediksi
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import android.util.Log

object ChartHelper {

    fun setupLineChart(chart: LineChart, agregasi: List<AgregasiBulanan>, prediksiList: List<Prediksi>) {

        if (agregasi.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return  // ← keluar jika tidak ada data
        }

        val aktualPeriodes = agregasi.map { it.periode.substring(0, 7) }
        val prediksiPeriodes = prediksiList.mapNotNull { it.periodePrediksi?.substring(0, 7) }
        val allPeriodes = (aktualPeriodes + prediksiPeriodes).distinct().sorted()

        val aktualEntries = agregasi.map {a ->
            val idx = allPeriodes.indexOf(a.periode.substring(0, 7)).toFloat()
            Entry(idx, a.totalPenjualan)
        }

        val prediksiEntries = prediksiList.mapNotNull {pred ->
            val periode = pred.periodePrediksi?.substring(0, 7) ?: return@mapNotNull null
            val idx = allPeriodes.indexOf(periode).toFloat()
            Entry(idx, pred.predPeriode1)
        }

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

        if (prediksiEntries.isNotEmpty()){
            val predSet = LineDataSet(prediksiEntries, "Prediksi").apply {
                color = Color.parseColor("#FF6F00")
                setCircleColor(Color.parseColor("#FF6F00"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            datasets.add(predSet)
        }

        chart.apply {
            data = LineData(datasets)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(allPeriodes)
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                granularity = 1f
                setDrawGridLines(false)
                textSize = 8f
                setLabelCount(allPeriodes.size, false)
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                textSize = 9f
                axisMinimum = 0f
            }
            description.isEnabled = false
//            legend.isEnabled = false
            legend.textSize = 10f
            setExtraOffsets(0f, 0f, 0f, 23f)
            setTouchEnabled(true)
            setPinchZoom(false)
            animateX(800)
            invalidate()
        }
    }

    //    Bar chart untuk menu penjualan/produksi
    fun setupBarChart(chart: BarChart, agregasi: List<AgregasiBulanan>){
        val label = agregasi.map { it.periode.substring(0,7) }
        val entries = agregasi.mapIndexed { i, a ->
//            BarEntry(i.toFloat(), a.totalPenjualan)
            val nilai = if (a.totalProduksi > 0f) a.totalProduksi else a.totalPenjualan
            BarEntry(i.toFloat(), nilai)
        }

        val barSet = BarDataSet(entries, "Penjualan (kg)").apply {
            color = Color.parseColor("#4CAF50")
            setDrawValues(false)
        }

        chart.apply {
            data = BarData(barSet).also { it.barWidth = 0.6f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(label)
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                granularity = 1f
                setDrawGridLines(false)
                textSize = 8f
                setLabelCount(label.size, false)
                isGranularityEnabled = true
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                textSize = 9f
                axisMinimum = 0f
            }
            description.isEnabled = false
            setExtraOffsets(0f, 0f, 0f, 18f)
            legend.isEnabled = false
            setTouchEnabled(false)
            setPinchZoom(false)
            setScaleEnabled(true)
            animateY(600)
            invalidate()
        }
    }
}