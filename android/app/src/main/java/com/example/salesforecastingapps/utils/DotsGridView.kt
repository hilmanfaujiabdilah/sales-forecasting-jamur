package com.example.tiracast.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * DotsGridView — tampilan grid titik-titik dekoratif
 * Sesuai elemen SVG: grid dots kiri atas dengan opacity 0.15
 */
class DotsGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40916C")
        style = Paint.Style.FILL
    }

    private val dotRadius = 2.5f * resources.displayMetrics.density  // 2.5dp
    private val spacing   = 40f  * resources.displayMetrics.density  // 40dp antar titik

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cols = ((width  / spacing) + 1).toInt()
        val rows = ((height / spacing) + 1).toInt()

        for (row in 0..rows) {
            for (col in 0..cols) {
                val x = col * spacing
                val y = row * spacing
                if (x <= width && y <= height) {
                    canvas.drawCircle(x, y, dotRadius, paint)
                }
            }
        }
    }
}
