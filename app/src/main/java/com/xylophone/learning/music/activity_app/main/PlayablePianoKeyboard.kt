package com.xylophone.learning.music.activity_app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.helper.SoundHelper
import kotlin.math.pow

/** A compact multi-touch keyboard for practising the supplied score. */
class PlayablePianoKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val firstMidi = 41 // F2
    private val lastMidi = 84 // C6
    private val whiteMidis = (firstMidi..lastMidi).filter { isWhite(it) }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fingers = mutableMapOf<Int, Int>()
    private val voices=mutableMapOf<Int,Int>()
    private val releases=android.os.Handler(android.os.Looper.getMainLooper())
    private val fading=mutableSetOf<Int>()
    var sustain = false
        set(value) {
            field=value
            if(!value) voices.keys.toList().filter { it !in fingers.values }.forEach { releaseNote(it) }
        }
    fun stopAll() {
        releases.removeCallbacksAndMessages(null)
        (voices.values.toList()+fading).forEach { pianoPool?.stop(it) }
        voices.clear(); fading.clear(); fingers.clear(); invalidate()
    }
    private fun releaseNote(midi:Int) {
        val stream=voices.remove(midi) ?: return
        fading.add(stream)
        for(step in 1..8) releases.postDelayed({
            val v=.62f*(1-step/8f)*(1-step/8f)
            pianoPool?.setVolume(stream,v,v)
            if(step==8) { pianoPool?.stop(stream); fading.remove(stream) }
        },step*25L)
    }
    var onNote: ((String) -> Unit)? = null
    private var pianoPool: android.media.SoundPool? = null
    private val pianoSounds = mutableMapOf<Int,Int>()
    private val loadedSounds = mutableSetOf<Int>()
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val pool=android.media.SoundPool.Builder().setMaxStreams(16).build()
        pianoPool=pool
        pool.setOnLoadCompleteListener { _, id, status -> if(status==0) loadedSounds.add(id) }
        for(midi in firstMidi..lastMidi) context.assets.openFd("piano_keys/$midi.wav").use {
            pianoSounds[midi]=pool.load(it,1)
        }
    }
    override fun onDetachedFromWindow() {
        stopAll()
        pianoPool?.release(); pianoPool=null
        pianoSounds.clear(); loadedSounds.clear()
        super.onDetachedFromWindow()
    }

    private fun isWhite(midi: Int) = midi % 12 !in intArrayOf(1, 3, 6, 8, 10)
    private fun name(midi: Int): String = "C C# D D# E F F# G G# A A# B".split(" ")[midi % 12] + (midi / 12 - 1)
    private fun play(midi: Int) {
        pianoSounds[midi]?.takeIf { it in loadedSounds }?.let {
            releaseNote(midi)
            pianoPool?.play(it,.62f,.62f,1,0,1f)?.let { stream -> voices[midi]=stream }
        }
        onNote?.invoke(name(midi))
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val white = width.toFloat() / whiteMidis.size
        paint.style = Paint.Style.FILL
        whiteMidis.forEachIndexed { i, midi ->
            paint.color = if (fingers.values.contains(midi)) Color.rgb(225, 177, 105) else Color.WHITE
            canvas.drawRect(i * white, 0f, (i + 1) * white - 1, height.toFloat(), paint)
            paint.color = Color.rgb(90, 70, 60); paint.style = Paint.Style.STROKE
            canvas.drawRect(i * white, 0f, (i + 1) * white, height.toFloat(), paint)
            paint.style = Paint.Style.FILL
            paint.textSize = 12f; paint.color = Color.DKGRAY
            if (midi % 12 == 0 || midi == firstMidi) canvas.drawText(name(midi), i * white + 4, height - 8f, paint)
        }
        val blackWidth = white * .58f
        paint.color = Color.rgb(45, 38, 38)
        for (midi in firstMidi..lastMidi) if (!isWhite(midi)) {
            paint.color=if(midi in fingers.values) Color.rgb(225,177,105) else Color.rgb(45,38,38)
            val leftWhite = whiteMidis.indexOfFirst { it > midi } - 1
            if (leftWhite >= 0) canvas.drawRect((leftWhite + 1) * white - blackWidth / 2, 0f,
                (leftWhite + 1) * white + blackWidth / 2, height * .62f, paint)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        when (event.actionMasked) {
            MotionEvent.ACTION_CANCEL -> stopAll()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                fingers.remove(event.getPointerId(event.actionIndex))?.let {
                    if(!sustain && it !in fingers.values) releaseNote(it)
                }
                if(event.actionMasked==MotionEvent.ACTION_UP) performClick()
            }
            else -> for(p in 0 until event.pointerCount) {
                val id=event.getPointerId(p)
                val midi=if(event.getX(p) in 0f..width.toFloat() && event.getY(p) in 0f..height.toFloat()) midiAt(event.getX(p),event.getY(p)) else null
                val old=fingers[id]
                if(old!=midi) {
                    fingers.remove(id)
                    if(old!=null && !sustain && old !in fingers.values) releaseNote(old)
                    if(midi!=null) { fingers[id]=midi; play(midi) }
                }
            }
        }
        invalidate()
        return true
    }
    override fun performClick():Boolean { super.performClick(); return true }
    private fun midiAt(x: Float, y: Float): Int {
        val white = width.toFloat() / whiteMidis.size
        val index = (x / white).toInt().coerceIn(0, whiteMidis.lastIndex)
        val candidate = whiteMidis[index]
        if (y < height * .62f) for (midi in candidate - 1..candidate + 1)
            if (!isWhite(midi) && midi in firstMidi..lastMidi && x > (whiteMidis.indexOfFirst { it > midi }) * white - white * .29f && x < (whiteMidis.indexOfFirst { it > midi }) * white + white * .29f) return midi
        return candidate
    }
}
