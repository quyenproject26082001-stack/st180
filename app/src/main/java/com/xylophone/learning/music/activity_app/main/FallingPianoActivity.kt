package com.xylophone.learning.music.activity_app.main

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class FallingPianoActivity : AppCompatActivity() {
    private lateinit var roll: FallingPianoView
    private lateinit var status: TextView
    private lateinit var demo: Button
    private var demonstration=false
    private lateinit var play: Button
    private lateinit var pool: SoundPool
    private val handler=Handler(Looper.getMainLooper())
    private val envelopes=Handler(Looper.getMainLooper())
    private val voiceVolumes=mutableMapOf<Int,Float>()
    private val sounds=mutableMapOf<Int,Int>()
    private val streams=mutableMapOf<Int,Int>()
    private val held=mutableSetOf<Int>()
    private val midiHeld=mutableSetOf<Int>()
    private val midiStreams=mutableMapOf<Int,Int>()
    private lateinit var midiInput: PianoMidiInput
    private lateinit var midiState: MidiPianoState
    private var midiEcho=false
    private var midiPedal=false
    private val backing=mutableMapOf<Int,Int>()
    private val accompanied=mutableSetOf<Int>()
    private lateinit var score: List<PianoLesson.Note>
    private lateinit var lesson: PianoLesson
    private lateinit var seekBar: SeekBar
    private lateinit var loopButton: Button
    private lateinit var markers: TextView
    private lateinit var bars: List<Pair<Int, Double>>
    private var duration=0.0
    private val pianoSong get()=intent.getStringExtra("piano_song") ?: "autumn"
    private val isPhotoSheet get()=pianoSong=="autumn_photo"
    private val isEasyAutumn get()=pianoSong=="autumn_easy"
    private val isNoiNay by lazy { pianoSong=="noi_nay" || pianoSong=="noi_nay_advanced" }
    private val isAdvanced by lazy { pianoSong=="noi_nay_advanced" }
    private val songTitle get()=when(pianoSong) { "autumn_photo" -> "Lãng Quên Chiều Thu • Bản cô Mây • 75 BPM luyện tập"; "autumn_easy" -> "Lãng Quên Chiều Thu • Bản dễ • Tay trái đơn giản"; "noi_nay_advanced" -> "Nơi này có anh • Advanced • Jason Srey • 92 BPM"; "noi_nay" -> "Nơi này có anh • Bản dễ • 75 BPM"; else -> "Lãng Quên Chiều Thu • Bản nâng cao" }
    private val practicePreferences by lazy { getSharedPreferences(when(pianoSong) { "autumn_photo" -> "autumn_photo_piano_practice"; "noi_nay_advanced" -> "noi_nay_advanced_piano_practice"; "noi_nay" -> "noi_nay_piano_practice"; else -> "autumn_piano_practice" }, MODE_PRIVATE) }
    private var pointA=0.0
    private var pointB=0.0
    private var looping=false
    private var dragging=false
    private var resumeAfterSeek=false
    private var countdown=3000.0
    private var hand=0
    private var correctOnly=false
    private var holdSupport=false
    private var retryChordOnMistake=false
    private lateinit var holdButton: Button
    private lateinit var correctOnlyButton: Button
    private var waitMode=true
    private var speed=.75
    private var started=false
    private var running=false
    private var ready=false
    private var sustain=false
    private var lastFrame=0L
    private var feedback="Nhấn Bắt đầu • Xanh: tay phải • Cam: tay trái"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val json=JSONArray(assets.open(when(pianoSong) { "autumn_photo" -> "autumn_photo_notes.json"; "autumn_easy" -> "autumn_easy_notes.json"; "noi_nay_advanced" -> "noi_nay_advanced_notes.json"; "noi_nay" -> "noi_nay_notes.json"; else -> "autumn_notes.json" }).bufferedReader().use { it.readText() })
        score=(0 until json.length()).map { i -> json.getJSONObject(i).let {
            val pose=it.optJSONArray("pose")
            PianoLesson.Note(it.getInt("midi"),it.getDouble("start"),it.getDouble("end"),it.getInt("hand"),
                it.optInt("finger",0), if(pose==null) emptyList() else (0 until pose.length()).map { i -> pose.getInt(i) })
        }}.sortedBy { it.start }
        duration=score.maxOf { it.end } + 500
        val savedA=practicePreferences.getLong("loop_start_ms",0L).toDouble()
        val savedB=practicePreferences.getLong("loop_end_ms",duration.toLong()).toDouble()
        val validRange=savedA>=0 && savedA<savedB && savedB<=duration
        pointA=if(validRange) savedA else 0.0
        pointB=if(validRange) savedB else duration
        looping=validRange && practicePreferences.getBoolean("loop_enabled",false)
        val timeline=JSONArray(assets.open(when(pianoSong) { "autumn_photo" -> "autumn_photo_timeline.json"; "noi_nay_advanced" -> "noi_nay_advanced_timeline.json"; "noi_nay" -> "noi_nay_timeline.json"; else -> "lang_quen_chieu_thu_timeline.json" }).bufferedReader().use { it.readText() })
        bars=(0 until timeline.length()).map { timeline.getJSONObject(it).let { row -> row.getInt("bar") to row.getDouble("startMs") } }
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(19,27,40)); setPadding(8,0,8,0) }
        val controlPanel=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(controlPanel,LinearLayout.LayoutParams(-1,-2))
        val controls=LinearLayout(this)
        fun button(label:String, action:()->Unit):Button = Button(this).apply { text=label; textSize=12f; setOnClickListener { action() }; controls.addView(this,LinearLayout.LayoutParams(0,48.dp,1f)) }
        button("‹ Trở về") { finish() }
        val mode=button("Chờ đánh đúng") {}
        mode.setOnClickListener { waitMode=!waitMode; mode.text=if(waitMode) "Chờ đánh đúng" else "Theo nhịp"; changeSettings() }
        val hands=button("Tay phải") {}
        hands.setOnClickListener { hand=(hand+1)%3; hands.text=listOf("Tay phải","Tay trái","Cả hai tay")[hand]; changeSettings() }
        val tempo=button("75%") {}
        tempo.setOnClickListener { speed=when(speed) {.5->.75; .75->1.0; else->.5}; tempo.text="${(speed*100).toInt()}%"; changeSettings() }
        val pedal=button("Pedal: tắt") {}
        pedal.setOnClickListener { sustain=!sustain; pedal.text=if(sustain) "Pedal: bật" else "Pedal: tắt"; if(!sustain) streams.keys.toList().filter { it !in held }.forEach { release(it) } }
        play=button("Đang tải…") { if(running) pause() else if(ready) { if(lesson.finished) reset(); running=true; started=true; lastFrame=SystemClock.uptimeMillis(); updateStatus(); handler.post(frame) } }
        play.isEnabled=false
        button("Chơi lại") { reset() }
        controlPanel.addView(controls)
        if(isNoiNay || isPhotoSheet) controlPanel.addView(TextView(this).apply {
            text="$songTitle • Chạm để xem sheet" + if(isPhotoSheet) "\nTay trái rải theo hướng dẫn cô Mây • Nhịp luyện tập đều" else ""
            setTextColor(Color.LTGRAY); textSize=12f
            setOnClickListener {
                if(isPhotoSheet) startActivity(android.content.Intent(this@FallingPianoActivity, AutumnPhotoSheetActivity::class.java))
                else openNoiNaySheet()
            }
        })
        val midiControls=LinearLayout(this)
        midiControls.addView(Button(this).apply {
            text="Đàn thật • Bluetooth / USB"; textSize=11f
            setOnClickListener { midiInput.showChooser() }
        },LinearLayout.LayoutParams(-2,44.dp))
        val midiStatus=TextView(this).apply {
            text="Chưa kết nối • Chạm Đàn thật để chọn MIDI"
            setTextColor(Color.LTGRAY); textSize=11f
        }
        midiControls.addView(midiStatus,LinearLayout.LayoutParams(0,-2,1f).apply { gravity=android.view.Gravity.CENTER_VERTICAL })
        midiControls.addView(Button(this).apply {
            text="Âm app: tắt"; textSize=11f
            setOnClickListener {
                midiEcho=!midiEcho
                text=if(midiEcho) "Âm app: bật" else "Âm app: tắt"
                if(!midiEcho) { midiStreams.values.forEach { fadeOut(it) }; midiStreams.clear() }
            }
        },LinearLayout.LayoutParams(-2,44.dp))
        controlPanel.addView(midiControls)
        val navigation=LinearLayout(this)
        fun nav(label:String, action:()->Unit):Button = Button(this).apply {
            text=label; textSize=11f
            setOnClickListener { action() }
            navigation.addView(this,LinearLayout.LayoutParams(0,44.dp,1f))
        }
        demo=nav("▶ Chơi mẫu") {
            demonstration=!demonstration
            changeSettings()
        }
        demo.isEnabled=false
        correctOnlyButton=nav("Chỉ nốt đúng: tắt") {
            correctOnly=!correctOnly
            updateStatus()
        }
        lateinit var retryChordButton: Button
        retryChordButton=nav("Sai hợp âm: tắt") {
            retryChordOnMistake=!retryChordOnMistake
            retryChordButton.text=if(retryChordOnMistake) "Sai hợp âm: lặp lại" else "Sai hợp âm: tắt"
            changeSettings()
        }
        holdButton=nav("Giữ nốt: tắt") {
            holdSupport=!holdSupport
            updateStatus()
        }
        nav("Ô trước") { val i=currentBar(); seekTo(bars[(i-1).coerceAtLeast(0)].second, running) }
        nav("Ô sau") { val i=currentBar(); seekTo(bars[(i+1).coerceAtMost(bars.lastIndex)].second, running) }
        nav("Đặt A") {
            val candidate=bars[currentBar()].second
            if(candidate<pointB) { pointA=candidate; saveLoop(); updateMarkers(); if(looping) seekTo(pointA,running) }
            else Toast.makeText(this,"A phải nằm trước B",Toast.LENGTH_SHORT).show()
        }
        nav("Đặt B") {
            val candidate=bars.getOrNull(currentBar()+1)?.second ?: duration
            if(candidate>pointA) { pointB=candidate; saveLoop(); updateMarkers(); if(looping) seekTo(pointA,running) }
            else Toast.makeText(this,"B phải nằm sau A",Toast.LENGTH_SHORT).show()
        }
        loopButton=nav(if(looping) "Lặp: bật" else "Lặp: tắt") {
            looping=!looping
            saveLoop()
            loopButton.text=if(looping) "Lặp: bật" else "Lặp: tắt"
            seekTo(if(looping) pointA else lesson.position.coerceAtLeast(0.0),running)
        }
        controlPanel.addView(navigation)
        markers=TextView(this).apply { setTextColor(Color.LTGRAY); textSize=11f }
        controlPanel.addView(markers)
        seekBar=SeekBar(this).apply {
            max=duration.toInt()
            contentDescription="Tua vị trí luyện tập"
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(bar:SeekBar) { dragging=true; resumeAfterSeek=running; pause() }
                override fun onProgressChanged(bar:SeekBar, progress:Int, fromUser:Boolean) {
                    if(fromUser) markers.text="Tua đến ${time(progress.toDouble())} • thả tay để chọn ô nhịp"
                }
                override fun onStopTrackingTouch(bar:SeekBar) {
                    dragging=false
                    val target=bars.lastOrNull { it.second<=bar.progress }?.second ?: 0.0
                    seekTo(target,resumeAfterSeek)
                }
            })
        }
        controlPanel.addView(seekBar,LinearLayout.LayoutParams(-1,28.dp))
        status=TextView(this).apply { setTextColor(Color.WHITE); textSize=13f; setPadding(6,4,6,4) }
        val compactBar=LinearLayout(this).apply { gravity=android.view.Gravity.CENTER_VERTICAL }
        compactBar.addView(status,LinearLayout.LayoutParams(0,-2,1f))
        val displayPreferences=getSharedPreferences("piano_display",MODE_PRIVATE)
        var panelHidden=displayPreferences.getBoolean("controls_hidden",false)
        val panelToggle=Button(this).apply { textSize=11f; isAllCaps=false }
        val playControlIndex=controls.indexOfChild(play)
        val playControlLayout=play.layoutParams
        val loopControlIndex=navigation.indexOfChild(loopButton)
        val loopControlLayout=loopButton.layoutParams
        fun updatePanelVisibility() {
            val playParent=if(panelHidden) compactBar else controls
            if(play.parent!==playParent) {
                (play.parent as? android.view.ViewGroup)?.removeView(play)
                if(panelHidden) compactBar.addView(play,1,LinearLayout.LayoutParams(-2,48.dp))
                else controls.addView(play,playControlIndex,playControlLayout)
            }
            play.alpha=if(panelHidden) .5f else 1f
            val loopParent=if(panelHidden) compactBar else navigation
            if(loopButton.parent!==loopParent) {
                (loopButton.parent as? android.view.ViewGroup)?.removeView(loopButton)
                if(panelHidden) compactBar.addView(loopButton,2,LinearLayout.LayoutParams(-2,48.dp))
                else navigation.addView(loopButton,loopControlIndex,loopControlLayout)
            }
            controlPanel.visibility=if(panelHidden) android.view.View.GONE else android.view.View.VISIBLE
            panelToggle.text=if(panelHidden) "Hiện điều khiển ▾" else "Ẩn điều khiển ▴"
            panelToggle.contentDescription=if(panelHidden) "Hiện bảng điều khiển piano" else "Ẩn bảng điều khiển để mở rộng piano"
        }
        panelToggle.setOnClickListener {
            panelHidden=!panelHidden
            displayPreferences.edit().putBoolean("controls_hidden",panelHidden).apply()
            updatePanelVisibility()
        }
        updatePanelVisibility()
        compactBar.addView(panelToggle,LinearLayout.LayoutParams(-2,48.dp))
        root.addView(compactBar,LinearLayout.LayoutParams(-1,-2))
        roll=FallingPianoView(this)
        if(isPhotoSheet || pianoSong=="autumn") {
            roll.showHandGuide=practicePreferences.getBoolean("hand_guide",true)
            val guideButton=Button(this).apply {
                textSize=11f; isAllCaps=false
                fun refresh() { text=if(roll.showHandGuide) "Tay ảo: bật" else "Tay ảo: tắt" }
                refresh()
                setOnClickListener {
                    roll.showHandGuide=!roll.showHandGuide
                    practicePreferences.edit().putBoolean("hand_guide",roll.showHandGuide).apply()
                    refresh()
                }
                setOnLongClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(this@FallingPianoActivity)
                        .setTitle("Bàn tay ảo • Ngón gợi ý")
                        .setMessage("1: cái • 2: trỏ • 3: giữa • 4: áp út • 5: út.\n\nTay mờ chuẩn bị ở nốt tiếp theo; đầu ngón sáng khi phím được đánh. Đi theo chuyển động dịch tay, không cố căng tay giữ tất cả phím.\n\nĐây là cách đi ngón gợi ý, chưa được xác nhận bởi giáo viên. Đoạn quá rộng hoặc chưa có thế tay phù hợp sẽ ẩn bàn tay; hãy theo nốt và sheet. MIDI chỉ nhận phím, không nhận biết ngón thật. Chọn Tay trái / Tay phải để tập riêng; Chơi mẫu để xem chuyển động.")
                        .setPositiveButton("Đã hiểu",null).show()
                    true
                }
            }
            compactBar.addView(guideButton,LinearLayout.LayoutParams(-2,48.dp))
        }
        root.addView(roll,LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
        pool=SoundPool.Builder().setMaxStreams(32).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).build()
        var loaded=0
        var failed=false
        pool.setOnLoadCompleteListener { _,_,result ->
            if(result!=0) failed=true
            loaded++
            if(loaded==66) { ready=!failed; play.isEnabled=ready; demo.isEnabled=ready; play.text=if(ready) "▶ Bắt đầu" else "Lỗi âm thanh"; if(::lesson.isInitialized) updateStatus() }
        }
        for(m in 28..93) assets.openFd("piano_keys/$m.wav").use { sounds[m]=pool.load(it,1) }
        roll.onDown=playKey@{ m ->
            if(demonstration) return@playKey
            if(correctOnly && waitMode && !retryChordOnMistake && (!running || countdown>0 || !lesson.accepts(m))) return@playKey
            if(holdSupport && waitMode && (!running || countdown>0 || !lesson.accepts(m))) return@playKey
            held.add(m)
            streams.remove(m)?.let { fadeOut(it) }
            streams[m]=playSoft(m, playerVolume(m), 1)
            if(running && countdown<=0) feedback=lesson.press(m)
            updateStatus()
        }
        roll.onUp={ m -> held.remove(m); if(!sustain) release(m) }
        reset()
        midiState=MidiPianoState(
            onDown={ midi, velocity ->
                midiHeld.add(midi)
                roll.externalHeldNotes=midiHeld.toSet()
                if(!demonstration) {
                    if(midiEcho && ready) {
                        midiStreams.remove(midi)?.let { fadeOut(it) }
                        midiStreams[midi]=playSoft(midi,playerVolume(midi)*velocity/127f,1)
                    }
                    feedback=if(running && countdown<=0) lesson.press(midi) else "Đã nhận phím MIDI $midi • Bấm Bắt đầu để học"
                    updateStatus()
                    if(!running || countdown>0) midiStatus.text="Đã nhận phím MIDI $midi • Vận tốc $velocity"
                }
            },
            onUp={ midi ->
                midiHeld.remove(midi)
                roll.externalHeldNotes=midiHeld.toSet()
                if(!midiPedal) midiStreams.remove(midi)?.let { fadeOut(it) }
            },
            onPedal={ down ->
                midiPedal=down
                if(!down) midiStreams.keys.toList().filter { it !in midiHeld }.forEach { midiStreams.remove(it)?.let { stream -> fadeOut(stream) } }
            }
        )
        midiInput=PianoMidiInput(this,
            onStatus={ message, connected -> midiStatus.text=message; midiStatus.setTextColor(if(connected) Color.rgb(81,191,127) else Color.LTGRAY) },
            onMessage={ message, data, value -> midiState.message(message,data,value) },
            onDisconnected={ pause() }
        )
    }
    private val Int.dp get()=(this*resources.displayMetrics.density).toInt()
    private fun release(m:Int) { streams.remove(m)?.let { fadeOut(it) } }
    private fun playerVolume(midi:Int):Float {
        // The selected hand must remain clearly audible, including bass-only practice.
        return 1f
    }
    // Preserve the piano attack; samples already have headroom for polyphony.
    private fun playSoft(midi:Int, volume:Float, priority:Int):Int {
        val sound=sounds[midi] ?: return 0
        val initial=volume.coerceIn(0f,1f)
        val stream=pool.play(sound,initial,initial,priority,0,1f)
        if(stream==0) return 0
        voiceVolumes[stream]=initial
        // Keep the longer piano decay alive for pedal and soft release.
        envelopes.postAtTime({ voiceVolumes.remove(stream) },stream,SystemClock.uptimeMillis()+6200)
        return stream
    }
    private fun fadeOut(stream:Int) {
        val initial=voiceVolumes[stream] ?: return
        envelopes.removeCallbacksAndMessages(stream)
        val start=SystemClock.uptimeMillis()
        for(step in 1..10) envelopes.postAtTime({
            val remaining=1f-step/10f
            val volume=initial*remaining*remaining
            pool.setVolume(stream,volume,volume)
            if(step==10) { pool.stop(stream); voiceVolumes.remove(stream) }
        },stream,start+step*20L)
    }
    /** Rebuild the selected part without rewinding the transport or changing A–B. */
    private fun changeSettings() {
        val position=lesson.position.coerceAtLeast(0.0)
        val wasRunning=running
        val wasStarted=started
        val remainingCountdown=countdown
        pause()
        lesson=PianoLesson(score.filter { demonstration || hand==2 || it.hand==hand },waitMode,speed,demonstration,retryChordOnMistake)
        roll.lesson=lesson
        roll.configure(lesson.notes)
        seekTo(position,false)
        countdown=remainingCountdown
        started=wasStarted
        feedback=if(waitMode) "Đánh phím sáng để tiếp tục" else "Đánh khi nốt chạm vạch vàng"
        if(wasRunning && ready) {
            running=true
            lastFrame=SystemClock.uptimeMillis()
            handler.post(frame)
        }
        updateStatus()
    }
    private fun reset() {
        pause()
        accompanied.clear()
        lesson=PianoLesson(score.filter { demonstration || hand==2 || it.hand==hand },waitMode,speed,demonstration,retryChordOnMistake)
        roll.lesson=lesson; roll.configure(lesson.notes)
        feedback="${if(waitMode) "Đánh phím sáng để tiếp tục" else "Đánh khi nốt chạm vạch vàng"} • Giữ theo độ dài thanh"
        seekTo(if(looping) pointA else 0.0,false)
    }
    private fun currentBar() = bars.indexOfLast { it.second<=lesson.position.coerceAtLeast(0.0) }.coerceAtLeast(0)
    private fun time(ms:Double):String { val sec=ms.toInt()/1000; return "%d:%02d".format(sec/60,sec%60) }
    private fun updateMarkers() {
        markers.text="${time(lesson.position.coerceAtLeast(0.0))} / ${time(duration)} • Ô ${bars[currentBar()].first} (lượt ${currentBar()+1}/${bars.size}) • A ${time(pointA)} – B ${time(pointB)} • B: hết ô đang chọn"
    }
    private fun seekTo(target:Double, resume:Boolean) {
        pause()
        val start=if(looping) target.coerceIn(pointA,pointB-1) else target.coerceIn(0.0,duration-1)
        lesson.seek(start,if(looping) pointB else duration)
        accompanied.clear()
        score.forEachIndexed { i,n -> if(n.start<start) accompanied.add(i) }
        countdown=3000.0
        started=false
        roll.invalidate(); updateStatus()
        if(resume && ready) {
            running=true; started=true; lastFrame=SystemClock.uptimeMillis(); updateStatus(); handler.post(frame)
        }
    }
    private fun pause() {
        running=false; handler.removeCallbacksAndMessages(null)
        if(::midiState.isInitialized) midiState.reset()
        midiHeld.clear()
        midiStreams.values.forEach(pool::stop); midiStreams.clear()
        if(::roll.isInitialized) roll.releaseKeys()
        envelopes.removeCallbacksAndMessages(null)
        voiceVolumes.keys.toList().forEach(pool::stop)
        voiceVolumes.clear()
        streams.values.forEach(pool::stop); streams.clear(); held.clear()
        backing.keys.toList().forEach { accompanied.remove(it) }
        backing.values.forEach(pool::stop); backing.clear()
        if(::lesson.isInitialized) updateStatus()
    }
    private fun updateStatus() {
        if(!dragging) { seekBar.progress=lesson.position.toInt().coerceAtLeast(0); updateMarkers() }
        val waiting=running && countdown<=0 && lesson.notes.indices.any {
            lesson.results[it]==0 && lesson.notes[it].start<=lesson.position
        } && waitMode && !demonstration
        val state=when {
            !ready -> "Đang tải âm thanh"
            lesson.finished -> "Đã hoàn thành"
            !running && !started -> "Sẵn sàng • Bấm Bắt đầu để chơi"
            !running -> "Đã tạm dừng • Bấm Tiếp tục để chơi tiếp"
            countdown>0 -> "Chuẩn bị ${kotlin.math.ceil(countdown/1000).toInt()}"
            demonstration -> "Đang chơi mẫu • Cả hai tay"
            waiting -> "Đang chờ bạn đánh đúng nốt"
            else -> "Đang chơi"
        }
        if(ready) play.text=when {
            running -> "Ⅱ Tạm dừng"
            lesson.finished -> "↻ Chơi lại"
            started -> "▶ Tiếp tục"
            else -> "▶ Bắt đầu"
        }
        play.contentDescription=if(running) "Tạm dừng bài đang chơi" else play.text
        correctOnlyButton.isEnabled=waitMode && !demonstration
        correctOnlyButton.text=if(correctOnly) "Chỉ nốt đúng: bật" else "Chỉ nốt đúng: tắt"
        holdButton.isEnabled=waitMode && !demonstration
        holdButton.text=if(holdSupport) "Giữ nốt: bật" else "Giữ nốt: tắt"
        demo.text=if(demonstration) "↩ Tự chơi" else "▶ Chơi mẫu"
        val progress=if(demonstration) "Chơi mẫu • Không chấm điểm" else "Đúng ${lesson.hits}/${lesson.targetCount} • Bỏ lỡ ${lesson.misses}"
        status.text="$state  |  $progress  |  ${maxOf(0,lesson.position.toInt()/1000)}s" +
            if(running && countdown<=0 && !demonstration) "  |  $feedback" else ""

    }
    private val frame=object:Runnable {
        override fun run() {
            if(!running) return
            val now=SystemClock.uptimeMillis()
            var elapsed=(now-lastFrame).toDouble(); lastFrame=now
            if(countdown>0) {
                val consumed=minOf(elapsed,countdown)
                countdown-=consumed; elapsed-=consumed
                if(countdown>0) { updateStatus(); handler.postDelayed(this,16); return }
            }
            lesson.advance(elapsed)
            if(holdSupport && waitMode && !demonstration) {
                lesson.autoAcceptHeld(held).forEach { midi ->
                    // Re-trigger the sample for each repeated attack while the finger stays down.
                    streams.remove(midi)?.let { fadeOut(it) }
                    streams[midi]=playSoft(midi, playerVolume(midi), 1)
                }
            }
            if(demonstration || hand!=2) score.forEachIndexed { i,n ->
                if((demonstration || n.hand!=hand) && n.start<lesson.rangeEnd && n.start<=lesson.position && i !in accompanied) {
                    accompanied.add(i)
                    // Demo includes dense chords: reserve mix headroom after sample normalization.
                    if(n.end>lesson.position) run { backing[i]=playSoft(n.midi, if(demonstration) { if(n.hand==0) .45f else .325f } else .35f, 0) }
                }
            }
            backing.keys.toList().filter { score[it].end<=lesson.position }.forEach { backing.remove(it)?.let { stream -> fadeOut(stream) } }
            roll.invalidate(); updateStatus()
            if(lesson.finished && looping) { seekTo(pointA,true); return }
            if(lesson.finished) { pause(); feedback="Hoàn thành • Nhấn Chơi lại để luyện tiếp"; updateStatus() }
            else handler.postDelayed(this,16)
        }
    }
    private fun saveLoop() {
        practicePreferences.edit()
            .putLong("loop_start_ms",pointA.toLong())
            .putLong("loop_end_ms",pointB.toLong())
            .putBoolean("loop_enabled",looping)
            .apply()
    }
    private fun openNoiNaySheet() {
        val file=java.io.File(cacheDir,if(isAdvanced) "noi_nay_advanced.pdf" else "noi_nay_easy.pdf")
        assets.open(if(isAdvanced) "noi_nay_advanced.pdf" else if(isAdvanced) "noi_nay_advanced.pdf" else "noi_nay_easy.pdf").use { input -> file.outputStream().use { input.copyTo(it) } }
        val uri=androidx.core.content.FileProvider.getUriForFile(this,"$packageName.provider",file)
        try {
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri,"application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_:android.content.ActivityNotFoundException) {
            Toast.makeText(this,"Cần ứng dụng đọc PDF để mở sheet",Toast.LENGTH_LONG).show()
        }
    }
    override fun onStart() { super.onStart(); if(::midiInput.isInitialized) midiInput.start() }
    override fun onStop() { if(::midiInput.isInitialized) midiInput.stop(); pause(); saveLoop(); super.onStop() }
    override fun onDestroy() { if(::midiInput.isInitialized) midiInput.close(); pool.release(); super.onDestroy() }
}
