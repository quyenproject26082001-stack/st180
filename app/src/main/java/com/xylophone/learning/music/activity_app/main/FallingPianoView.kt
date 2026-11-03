package com.xylophone.learning.music.activity_app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

/** Notes and keys share exactly the same geometry, including black-key hit regions. */
class FallingPianoView(context: Context) : View(context) {
    var lesson: PianoLesson? = null
    var onDown: (Int) -> Unit = {}
    var onUp: (Int) -> Unit = {}
    var externalHeldNotes: Set<Int> = emptySet()
        set(value) { field = value; invalidate() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fingers = mutableMapOf<Int, Int>()
    private var whites = (60..93).filter { white(it) }
    private var range = 36..93
    private fun white(m: Int) = m % 12 !in listOf(1,3,6,8,10)
    fun configure(notes: List<PianoLesson.Note>) {
        releaseKeys()
        var low = notes.minOf { it.midi }; var high = notes.maxOf { it.midi }
        while (!white(low)) low--
        while (!white(high)) high++
        range = low..high; whites = range.filter { white(it) }
        invalidate()
    }
    private val line get() = height * .60f
    private fun rect(m: Int): RectF {
        val w = width.toFloat() / whites.size
        if (white(m)) { val x = whites.indexOf(m)*w; return RectF(x,line,x+w,height.toFloat()) }
        val x = whites.count { it < m }*w
        return RectF(x-w*.30f,line,x+w*.30f,line+(height-line)*.62f)
    }
    private fun name(m: Int) = arrayOf("C","C♯","D","E♭","E","F","F♯","G","A♭","A","B♭","B")[m%12]+(m/12-1)
    override fun onDraw(c: Canvas) {
        c.drawColor(Color.rgb(19,27,40))
        val model = lesson ?: return
        paint.strokeWidth = 1f
        whites.forEach { m ->
            paint.color=Color.rgb(42,53,67); val r=rect(m)
            c.drawLine(r.left,0f,r.left,line,paint)
        }
        c.save(); c.clipRect(0f,0f,width.toFloat(),line)
        model.notes.forEachIndexed { i,n ->
            if (model.results[i] == 3) return@forEachIndexed
            val bottom=line-((n.start-model.position)/2500*line).toFloat()
            val top=line-((n.end-model.position)/2500*line).toFloat()
            if (bottom >= 0 && top <= line) {
                val r=rect(n.midi)
                paint.color=when(model.results[i]) {1, 4 -> Color.rgb(81,191,127); 2 -> Color.rgb(220,90,90); else -> if(n.hand==0) Color.rgb(80,173,244) else Color.rgb(237,173,86)}
                c.drawRoundRect(r.left+2,top,r.right-2,maxOf(top+8,bottom),5f,5f,paint)
                paint.color=Color.WHITE; paint.textSize=11*resources.displayMetrics.scaledDensity
                c.drawText(name(n.midi),r.left+3,bottom-4,paint)
            }
        }
        c.restore()
        for (m in whites + range.filter { !white(it) }) {
            val r=rect(m)
            val due=model.notes.indices.any { model.results[it]==0 && model.notes[it].midi==m && kotlin.math.abs(model.notes[it].start-model.position)<90 }
            paint.color=when { m in externalHeldNotes || fingers.containsValue(m) || model.notes.indices.any {
                model.results[it]==4 && model.notes[it].midi==m &&
                    model.position>=model.notes[it].start && model.position<model.notes[it].end
            }->Color.rgb(81,191,127); due->Color.rgb(244,194,93); white(m)->Color.WHITE; else->Color.rgb(34,34,40) }
            c.drawRect(r.left+1,r.top+2,r.right-1,r.bottom,paint)
            paint.color=if(white(m)) Color.DKGRAY else Color.WHITE
            paint.textSize=10*resources.displayMetrics.scaledDensity
            c.drawText(name(m),r.left+2,r.bottom-10,paint)
        }
        paint.color=Color.rgb(244,194,93); paint.strokeWidth=4f
        c.drawLine(0f,line,width.toFloat(),line,paint)
    }
    private fun key(x:Float,y:Float): Int? {
        if(y<line || y>height || x<0 || x>=width) return null
        return (range.filter { !white(it) }+whites).firstOrNull { rect(it).contains(x,y) }
    }
    fun releaseKeys() { fingers.values.toSet().forEach(onUp); fingers.clear(); invalidate() }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when(e.actionMasked) {
            MotionEvent.ACTION_CANCEL -> releaseKeys()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                fingers.remove(e.getPointerId(e.actionIndex))?.let { if(!fingers.containsValue(it)) onUp(it) }
                if(e.actionMasked==MotionEvent.ACTION_UP) performClick()
            }
            else -> for(i in 0 until e.pointerCount) {
                val id=e.getPointerId(i); val next=key(e.getX(i),e.getY(i)); val old=fingers[id]
                if(old!=next) {
                    fingers.remove(id)
                    if(old!=null && !fingers.containsValue(old)) onUp(old)
                    if(next!=null) { fingers[id]=next; onDown(next) }
                }
            }
        }
        invalidate(); return true
    }
    override fun performClick():Boolean { super.performClick(); return true }
}
