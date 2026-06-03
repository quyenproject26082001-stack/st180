package com.xylophone.learning.music.activity_app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class GuitarStringsAnimView @JvmOverloads constructor(
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

    private data class StringAnim(val startTime: Long, val maxAmplitude: Float)
    private data class NoteAnim(val startTime: Long, val x: Float, val y: Float, val symbol: String, val color: Int)

    private val activeAnims = mutableMapOf<Int, StringAnim>()
    private val noteAnims = mutableListOf<NoteAnim>()
    private val path = Path()

    private val NOTE_SYMBOLS = listOf("♩", "♪", "♫", "♬")
    private val NOTE_DURATION_MS = 2400L
    private val NOTE_FLOAT_DISTANCE = 280f

    private val stringColors = intArrayOf(
        Color.rgb(220, 220, 220),
        Color.rgb(210, 210, 180),
        Color.rgb(200, 190, 150),
        Color.rgb(200, 175, 100),
        Color.rgb(190, 160, 80),
        Color.rgb(180, 140, 60)
    )
    private val strokeWidths = floatArrayOf(2f, 2.5f, 3f, 3.5f, 4f, 5f)
    private val visualFreqs = floatArrayOf(8.0f, 6.5f, 5.0f, 4.0f, 3.0f, 2.5f)
    private val maxAmplitudes = floatArrayOf(6f, 7f, 8f, 9f, 10f, 11f)

    private val ANIM_DURATION_MS = 1200L
    private val DAMPING = 3.5f

    private val stringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private fun getStringY(idx: Int, h: Float, neckProgress: Float = 0.5f): Float {
        val bodyR = stringYBodyRatios
        val headR = stringYHeadRatios
        if (bodyR != null && headR != null && idx < bodyR.size) {
            return h * (bodyR[idx] + (headR[idx] - bodyR[idx]) * neckProgress)
        }
        val customRatios = stringYRatios
        return if (customRatios != null && idx < customRatios.size) {
            h * customRatios[idx]
        } else {
            val stringTop = h * stringTopRatio
            val stringBottom = h * stringBottomRatio
            stringTop + (idx + 0.5f) / 6f * (stringBottom - stringTop)
        }
    }

    fun pluckString(stringIndex: Int, fretIndex: Int) {
        val idx = stringIndex.coerceIn(0, 5)
        val now = System.currentTimeMillis()

        activeAnims[idx] = StringAnim(now, maxAmplitudes[idx])

        val w = width.toFloat()
        val h = height.toFloat()
        val neckStart = w * bodyRatio
        val neckEnd = w * neckEndRatio
        val neckWidth = neckEnd - neckStart
        val stringTop = h * stringTopRatio
        val stringBottom = h * stringBottomRatio
        val zoneHeight = stringBottom - stringTop

        val noteX = neckStart + (fretIndex + 0.5f) / 12f * neckWidth
        val noteY = getStringY(idx, h)
        val symbol = NOTE_SYMBOLS[(idx + fretIndex) % NOTE_SYMBOLS.size]

        noteAnims.add(NoteAnim(now, noteX, noteY, symbol, stringColors[idx]))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis()
        val w = width.toFloat()
        val h = height.toFloat()
        val neckStart = w * bodyRatio
        val neckEnd = w * neckEndRatio
        val neckWidth = neckEnd - neckStart
        val stringTop = h * stringTopRatio
        val stringBottom = h * stringBottomRatio
        val zoneHeight = stringBottom - stringTop

        // String wave animations
        val toRemoveStrings = mutableListOf<Int>()
        for ((idx, anim) in activeAnims) {
            val elapsed = now - anim.startTime
            if (elapsed >= ANIM_DURATION_MS) { toRemoveStrings.add(idx); continue }

            val t = elapsed / 1000.0
            val stringY = getStringY(idx, h)
            val amplitude = anim.maxAmplitude * exp(-DAMPING * t)
            val freq = visualFreqs[idx]

            path.reset()
            var first = true
            var x = neckStart
            while (x <= neckEnd) {
                val nx = (x - neckStart) / neckWidth
                val baseY = getStringY(idx, h, nx)
                val dy = (amplitude * sin(PI * nx) * sin(2 * PI * freq * t)).toFloat()
                if (first) { path.moveTo(x, baseY + dy); first = false }
                else path.lineTo(x, baseY + dy)
                x += 3f
            }
            stringPaint.color = stringColors[idx]
            stringPaint.strokeWidth = strokeWidths[idx]
            canvas.drawPath(path, stringPaint)
        }
        toRemoveStrings.forEach { activeAnims.remove(it) }

        // Floating note icon animations
        val toRemoveNotes = mutableListOf<NoteAnim>()
        for (note in noteAnims) {
            val elapsed = now - note.startTime
            if (elapsed >= NOTE_DURATION_MS) { toRemoveNotes.add(note); continue }

            val progress = elapsed.toFloat() / NOTE_DURATION_MS
            val floatY = note.y - NOTE_FLOAT_DISTANCE * progress
            val alpha = ((1f - progress) * 255).toInt()
            val scale = 1f + progress * 0.5f

            notePaint.color = note.color
            notePaint.alpha = alpha
            notePaint.textSize = 88f * scale

            canvas.drawText(note.symbol, note.x, floatY, notePaint)
        }
        noteAnims.removeAll(toRemoveNotes)

        if (activeAnims.isNotEmpty() || noteAnims.isNotEmpty()) postInvalidateOnAnimation()
    }
}
