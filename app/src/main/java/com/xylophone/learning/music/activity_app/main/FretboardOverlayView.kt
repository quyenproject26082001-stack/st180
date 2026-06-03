package com.xylophone.learning.music.activity_app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class FretboardOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var bodyRatio = 0.41f
    var neckEndRatio = 0.88f
    var stringTopRatio = 0.35f
    var stringBottomRatio = 0.68f
    var stringYRatios: FloatArray? = null
    var stringYBodyRatios: FloatArray? = null
    var stringYHeadRatios: FloatArray? = null

    private val fretPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val stringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val boundaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        textSize = 26f
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val neckStart = w * bodyRatio
        val neckEnd = w * neckEndRatio
        val neckWidth = neckEnd - neckStart
        val stringTop = h * stringTopRatio
        val stringBottom = h * stringBottomRatio

        // Ranh giới body/cần đàn (xanh lá)
        canvas.drawLine(neckStart, 0f, neckStart, h, boundaryPaint)
        // Ranh giới cần đàn/headstock (xanh lá)
        canvas.drawLine(neckEnd, 0f, neckEnd, h, boundaryPaint)

        // Ranh giới dây trên/dưới (xanh lá đứt)
        boundaryPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
        canvas.drawLine(neckStart, stringTop, neckEnd, stringTop, boundaryPaint)
        canvas.drawLine(neckStart, stringBottom, neckEnd, stringBottom, boundaryPaint)
        boundaryPaint.pathEffect = null

        // 12 fret lines (đỏ)
        for (fret in 0..12) {
            val x = neckStart + (fret / 12f) * neckWidth
            canvas.drawLine(x, stringTop, x, stringBottom, fretPaint)
            canvas.drawText("$fret", x + 4f, stringTop - 4f, textPaint)
        }

        // 6 dây (cyan) — chéo nếu có tapered ratios
        val bodyR = stringYBodyRatios
        val headR = stringYHeadRatios
        if (bodyR != null && headR != null) {
            for (i in bodyR.indices) {
                val yBody = h * bodyR[i]
                val yHead = h * headR[i]
                canvas.drawLine(neckStart, yBody, neckEnd, yHead, stringPaint)
                canvas.drawText("${i + 1}", neckStart - 30f, yBody + 8f, textPaint)
            }
        } else {
            val ratios = stringYRatios
            if (ratios != null) {
                for (i in ratios.indices) {
                    val y = h * ratios[i]
                    canvas.drawLine(neckStart, y, neckEnd, y, stringPaint)
                    canvas.drawText("${i + 1}", neckStart - 30f, y + 8f, textPaint)
                }
            } else {
                for (string in 0..6) {
                    val y = stringTop + (string / 6f) * (stringBottom - stringTop)
                    canvas.drawLine(neckStart, y, neckEnd, y, stringPaint)
                }
            }
        }
    }
}
