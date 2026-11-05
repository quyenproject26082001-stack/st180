package com.xylophone.learning.music.activity_app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Path
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View

/** Notes and keys share exactly the same geometry, including black-key hit regions. */
class FallingPianoView(context: Context) : View(context) {
    var lesson: PianoLesson? = null
    var onDown: (Int) -> Unit = {}
    var onUp: (Int) -> Unit = {}
    var showHandGuide=false
        set(value) { field=value; guidePoses.clear(); invalidate() }
    private val guidePoses=mutableMapOf<Int,FloatArray>()
    private var guideTime=0L
    private var guidePosition=Double.NaN
    var externalHeldNotes: Set<Int> = emptySet()
        set(value) { field = value; invalidate() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fingers = mutableMapOf<Int, Int>()
    private var whites = (60..93).filter { white(it) }
    private var range = 36..93
    private fun white(m: Int) = m % 12 !in listOf(1,3,6,8,10)
    fun configure(notes: List<PianoLesson.Note>) {
        releaseKeys()
        guidePoses.clear()
        val displayed=notes.flatMap { listOf(it.midi)+it.pose }
        var low = displayed.min(); var high = displayed.max()
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
        if(showHandGuide) drawHands(c,model)
    }
    private fun drawHands(c: Canvas, model: PianoLesson) {
        val now=SystemClock.uptimeMillis()
        val dt=(now-guideTime).coerceIn(0,64).toFloat()
        guideTime=now
        if(guidePosition.isNaN() || kotlin.math.abs(model.position-guidePosition)>650) guidePoses.clear()
        guidePosition=model.position
        val keyWidth=width.toFloat()/whites.size
        val keyHeight=height-line
        val radius=(keyWidth*.22f).coerceAtLeast(4f)
        var moving=false
        c.save(); c.clipRect(0f,line,width.toFloat(),height.toFloat())
        for(hand in 0..1) {
            val targets=PianoHandGuide.targets(model,hand)
            if(targets.isEmpty()) { guidePoses.remove(hand); continue }
            val focus=model.notes[targets.last()]
            val desired=FloatArray(10)
            for(f in 0..4) {
                val r=rect(focus.pose[f])
                desired[f*2]=r.centerX()
                desired[f*2+1]=if(white(focus.pose[f])) line+keyHeight*(if(f==0) .67f else .40f) else r.bottom-radius*1.4f
            }
            val current=guidePoses.getOrPut(hand) { desired.copyOf() }
            var distance=0f
            for(i in current.indices) distance=maxOf(distance,kotlin.math.abs(current[i]-desired[i]))
            val lifting=distance>keyWidth*.3f
            for(i in current.indices) {
                current[i]+=(desired[i]-current[i])*(dt/110f).coerceIn(.01f,1f)
                if(kotlin.math.abs(current[i]-desired[i])<.3f) current[i]=desired[i] else moving=true
            }
            val color=if(hand==0) Color.rgb(80,173,244) else Color.rgb(237,173,86)
            val center=(current[2]+current[4]+current[6])/3
            val palmY=line+keyHeight*.83f
            val half=keyWidth*1.25f
            paint.style=Paint.Style.FILL; paint.color=color; paint.alpha=75
            c.drawRoundRect(center-half,palmY-keyHeight*.15f,center+half,palmY+keyHeight*.07f,radius*2,radius*2,paint)
            c.drawRoundRect(center-half*.55f,palmY,center+half*.55f,height.toFloat()+radius,radius,radius,paint)
            for(f in 0..4) {
                val index=targets.firstOrNull { model.notes[it].finger==f+1 }
                val n=index?.let { model.notes[it] }
                val pressed=index!=null && n!=null && n.start<=model.position && model.position<n.end &&
                    ((model.demonstration && model.results[index]==4) ||
                        (model.results[index]==1 && (n.midi in externalHeldNotes || fingers.containsValue(n.midi))))
                val x=current[f*2]
                val y=current[f*2+1] - if(lifting) radius else if(pressed) -radius*.4f else radius*.45f
                val direction=if(hand==0) 1 else -1
                val baseX=center+(f-2)*half*.43f*direction
                val path=Path().apply {
                    moveTo(baseX,palmY)
                    cubicTo(baseX,palmY-keyHeight*.1f,x,y+keyHeight*.15f,x,y)
                }
                paint.style=Paint.Style.STROKE; paint.strokeCap=Paint.Cap.ROUND
                paint.strokeWidth=radius*1.8f; paint.color=color; paint.alpha=if(n!=null) 170 else 85
                c.drawPath(path,paint)
                paint.style=Paint.Style.FILL
                paint.color=if(pressed) Color.rgb(81,191,127) else if(n!=null) Color.rgb(244,194,93) else color
                paint.alpha=if(n!=null) 240 else 145
                c.drawCircle(x,y,if(pressed) radius*1.15f else radius,paint)
                paint.color=Color.rgb(19,27,40); paint.alpha=255
                paint.textAlign=Paint.Align.CENTER; paint.textSize=radius*1.5f
                c.drawText("${f+1}",x,y-(paint.ascent()+paint.descent())/2,paint)
            }
        }
        c.restore()
        paint.alpha=255; paint.style=Paint.Style.FILL; paint.textAlign=Paint.Align.LEFT; paint.strokeCap=Paint.Cap.BUTT
        paint.color=Color.WHITE; paint.textSize=11*resources.displayMetrics.scaledDensity
        val unassigned=model.notes.any { it.finger==0 && it.start<=model.position && model.position<it.end }
        c.drawText(if(unassigned) "Đoạn rộng: xem sheet • Tay ảo tạm ẩn ở tay chưa có gợi ý"
            else "Ngón gợi ý • 1 cái · 2 trỏ · 3 giữa · 4 áp út · 5 út",8f,line-10f,paint)
        if(moving) postInvalidateOnAnimation()
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
