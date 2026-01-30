package com.xylophone.learning.music.activity_app.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.animateScaleEffect
import com.xylophone.learning.music.core.extensions.goHome
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.invisible
import com.xylophone.learning.music.core.extensions.loadNativeCollabAds
import com.xylophone.learning.music.core.extensions.select
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.shakeViewEffect
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.extensions.visible
import com.xylophone.learning.music.core.helper.NoteIconManager
import com.xylophone.learning.music.core.helper.RecordingManager
import com.xylophone.learning.music.core.helper.SharePreferenceHelper
import com.xylophone.learning.music.core.helper.SoundHelper
import com.xylophone.learning.music.data.model.Instrument
import com.xylophone.learning.music.data.model.Recording
import com.xylophone.learning.music.data.model.Song
import com.xylophone.learning.music.data.model.SongLibrary
import com.xylophone.learning.music.databinding.ActivityPlayBinding


class PlayActivity : BaseActivity<ActivityPlayBinding>() {

    // Track button đã phát cho từng ngón tay (pointer)
    private val lastPlayedButtonMap = mutableMapOf<Int, ImageView?>()
    private val buttonSoundMap = mutableMapOf<ImageView, Int>()
    private val buttonNameMap = mutableMapOf<ImageView, String>()




    private var isLearningComplete : Boolean = false

    private var sunJumpAnimator: android.animation.ValueAnimator? = null

    // Note icon overlay manager
    private lateinit var noteIconManager: NoteIconManager

    // Recording
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    // Playback
    private var isPlaybackMode = false
    private var recordingId: String? = null
    private var playbackStartTime = 0L
    private var playbackTimerRunnable: Runnable? = null

    // Learning Mode
    private var isLearningMode = false
    private var currentSong: Song? = null
    private var currentNoteIndex = 0
    private var highlightedButton: ImageView? = null

    // Instrument Selection
    private lateinit var preferenceHelper: SharePreferenceHelper
    private var currentInstrument: Instrument = Instrument.XYLOPHONE

    private val playbackHandler = Handler(Looper.getMainLooper())
    private val playbackRunnables = mutableListOf<Runnable>()
    private var isPlaybackStopped = false

    override fun setViewBinding(): ActivityPlayBinding {
        return ActivityPlayBinding.inflate(LayoutInflater.from(this))
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun initView() {

        Admob.getInstance().setTimeCountdownNativeCollab(60000)

        binding.idMusic.select()
        // Initialize preference helper
        preferenceHelper = SharePreferenceHelper(this)

        // Load instrument based on mode
        loadInstrumentBasedOnMode()

        if(!isLearningMode&&!isPlaybackMode)
        {
            binding.idMusic.setText(R.string.music)
        }

        binding.tvRecord.select()

        // Setup instrument selectors
        setupInstrumentSelectors()
        updateInstrumentUI()

        loadSounds()
        setupButtonMap()
        setupSwipeListener()
        setupRecordingUI()
        checkPlaybackMode()
        checkLearningMode()

        // Initialize note icon overlay manager
        noteIconManager = NoteIconManager(this, binding.overlayContainer)
    }

    private fun loadSounds() {
        // Load các file âm thanh cho instrument hiện tại
        val noteNames = listOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si", "Do2")

        noteNames.forEach { noteName ->
            val resId = currentInstrument.getSoundResource(noteName)
            if (resId != 0) {
                try {
                    if (!SoundHelper.isSoundNotNull(resId)) {
                        SoundHelper.loadSound(this, resId)
                    }
                } catch (e: Exception) {
                    // Bỏ qua nếu file âm thanh không tồn tại
                    e.printStackTrace()
                }
            }
        }
    }// tôi rất ngạc nhiên vì bạn tìm một tính năng gì đó đều rất nhanh, không biết bạn có bị quyết gì không vậy, chỉ cho tôi, để tôi có thể tìm nhanh như bạn

    private fun jumpSunToButton(targetButton: ImageView) {
        val sun = binding.imgSunGuide

        // Nếu chưa hiện thì hiện lên (learning mode)
        if (!sun.isVisible) sun.visible()

        // Đợi sun được layout xong (có width/height) trước khi tính toán
        if (sun.width == 0 || sun.height == 0) {
            sun.post {
                jumpSunToButton(targetButton) // Gọi lại sau khi layout xong
            }
            return
        }

        // Tính vị trí button so với overlayContainer
        val buttonLocation = IntArray(2)
        val containerLocation = IntArray(2)
        targetButton.getLocationOnScreen(buttonLocation)
        binding.overlayContainer.getLocationOnScreen(containerLocation)

        val relativeX = buttonLocation[0] - containerLocation[0]
        val relativeY = buttonLocation[1] - containerLocation[1]

        // Target: đặt sun ở gần "đầu" nốt (trên một chút cho dễ nhìn)
        val targetX = relativeX + (targetButton.width / 2f) - (sun.width / 2f)
        val targetY = relativeY - (sun.height * 0.35f) // nhích lên trên cho giống đang chỉ vào nốt

        // Cancel animation cũ (để nhảy được liên tục, kể cả nốt lặp lại)
        sun.animate().cancel()

        // Nếu có animation đang chạy, force finish để lấy vị trí đích chính xác
        sunJumpAnimator?.let { animator ->
            if (animator.isRunning) {
                animator.end() // Force kết thúc animation để sun về đúng vị trí đích
            }
        }
        sunJumpAnimator?.cancel()

        // Nếu lần đầu (chưa có vị trí), đặt thẳng tới target để khỏi bay từ (0,0)
        val startX: Float
        val startY: Float

        if (sun.x == 0f && sun.y == 0f) {
            // Lần đầu: bắt đầu từ vị trí target luôn (không bay từ góc màn hình)
            startX = targetX
            startY = targetY
            sun.x = targetX
            sun.y = targetY
            return // Không cần animation lần đầu
        } else {
            startX = sun.x
            startY = sun.y
        }
        val dx = targetX - startX
        val dy = targetY - startY

        // Jump height (px) - trẻ con nhìn rõ
        val jumpHeight = 70f * resources.displayMetrics.density

        // Nếu target gần như trùng (nốt lặp lại), vẫn nhảy tại chỗ
        val needOnlyJump = kotlin.math.abs(dx) < 2f && kotlin.math.abs(dy) < 2f

        // Giảm duration để kịp hoàn thành trước khi moveToNextNote (300ms)
        val duration = 250L

        // Tạo "nhảy theo parabol": y = linear - 4h*t(1-t)
        val anim = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            // Dùng OvershootInterpolator để animation nhanh hơn nhưng vẫn mượt
            interpolator = android.view.animation.OvershootInterpolator(0.5f)

            addUpdateListener { va ->
                val t = va.animatedValue as Float

                val newX = if (needOnlyJump) startX else (startX + dx * t)
                val newY = (startY + (if (needOnlyJump) 0f else dy) * t) - (4f * jumpHeight * t * (1f - t))

                sun.x = newX
                sun.y = newY

                // Tí “nhún” scale cho dễ thương
                val s = 1f + 0.12f * kotlin.math.sin(t * Math.PI).toFloat()
                sun.scaleX = s
                sun.scaleY = s
            }

            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Đặt đúng vào đích (tránh sai số)
                    sun.x = targetX
                    sun.y = targetY

                    // “Bounce” nhẹ lúc đáp
                    sun.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(180)
                        .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                        .start()
                }
            })
        }

        sunJumpAnimator = anim
        anim.start()
    }


    private fun setupButtonMap() {
        binding.apply {
            // Map sounds based on current instrument
            buttonSoundMap[btnDo] = currentInstrument.getSoundResource("Do")
            buttonSoundMap[btnRe] = currentInstrument.getSoundResource("Re")
            buttonSoundMap[btnMi] = currentInstrument.getSoundResource("Mi")
            buttonSoundMap[btnFa] = currentInstrument.getSoundResource("Fa")
            buttonSoundMap[btnSol] = currentInstrument.getSoundResource("Sol")
            buttonSoundMap[btnLa] = currentInstrument.getSoundResource("La")
            buttonSoundMap[btnSi] = currentInstrument.getSoundResource("Si")
            buttonSoundMap[btnDo2] = currentInstrument.getSoundResource("Do2")

            buttonNameMap[btnDo] = "Do"
            buttonNameMap[btnRe] = "Re"
            buttonNameMap[btnMi] = "Mi"
            buttonNameMap[btnFa] = "Fa"
            buttonNameMap[btnSol] = "Sol"
            buttonNameMap[btnLa] = "La"
            buttonNameMap[btnSi] = "Si"
            buttonNameMap[btnDo2] = "Do2"
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeListener() {
        // Tạo touch listener hỗ trợ multi-touch
        val touchListener = { view: android.view.View, event: MotionEvent ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // Ngón tay đầu tiên chạm xuống
                    val pointerId = event.getPointerId(0)
                    handleTouchEvent(event.rawX, event.rawY, pointerId)
                    true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Ngón tay thứ 2, 3, 4... chạm xuống
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)
                    // Tính raw coordinates cho pointer này
                    val x = event.getX(pointerIndex) + (event.rawX - event.x)
                    val y = event.getY(pointerIndex) + (event.rawY - event.y)
                    handleTouchEvent(x, y, pointerId)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Xử lý tất cả các ngón tay đang di chuyển
                    for (i in 0 until event.pointerCount) {
                        val pointerId = event.getPointerId(i)
                        // Tính raw coordinates cho mỗi pointer
                        val x = event.getX(i) + (event.rawX - event.x)
                        val y = event.getY(i) + (event.rawY - event.y)
                        handleTouchEvent(x, y, pointerId)
                    }
                    true
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // Một ngón tay được nhấc lên (còn ngón khác)
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)

                    lastPlayedButtonMap.remove(pointerId)
                    true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    // Ngón tay cuối cùng nhấc lên hoặc bị cancel

                    lastPlayedButtonMap.clear()
                    false // Cho phép click event hoạt động
                }
                else -> false
            }
        }

        // Gắn touch listener cho từng button
        binding.apply {
            btnDo.setOnTouchListener(touchListener)
            btnRe.setOnTouchListener(touchListener)
            btnMi.setOnTouchListener(touchListener)
            btnFa.setOnTouchListener(touchListener)
            btnSol.setOnTouchListener(touchListener)
            btnLa.setOnTouchListener(touchListener)
            btnSi.setOnTouchListener(touchListener)
            btnDo2.setOnTouchListener(touchListener)

            // Cũng gắn cho container để bắt swipe giữa các nút
            frameMusic.setOnTouchListener(touchListener)
        }
    }

    private fun handleTouchEvent(x: Float, y: Float, pointerId: Int) {

        if(isPlaybackMode) return // Không cho chơi khi đang playback

        // Tìm button đang được chạm
        var foundButton: ImageView? = null
        var foundSoundId: Int? = null

        buttonSoundMap.forEach { (button, soundId) ->
            if (isTouchingButton(button, x, y)) {
                foundButton = button
                foundSoundId = soundId
                return@forEach
            }
        }

        // Nếu tìm thấy button và button khác với button trước đó
        if (foundButton != null && foundSoundId != null) {
            if (lastPlayedButtonMap[pointerId] != foundButton) {
                var shouldShowIcon = true

                // Learning Mode: validate correct note
                if (isLearningMode) {
                    shouldShowIcon = handleLearningModeTouch(foundButton!!)
                } else {
                    // Normal Mode: chỉ phát khi chuyển sang button mới
                    playNoteSound(foundSoundId!!)
                    animateButton(foundButton!!)
                }

                // Show note icon overlay only if correct note (or not in learning mode)
                if (shouldShowIcon) {
                    noteIconManager.showIcon(pointerId, foundButton!!)
                }

                lastPlayedButtonMap[pointerId] = foundButton
            }
            // Nếu cùng button → không phát lại (giữ nguyên)
        } else {


            // Thay vì reset ngay -> kiểm tra xem có "ra xa hẳn" khỏi phím cuối cùng chưa
            val lastButton = lastPlayedButtonMap[pointerId]

            if (lastButton != null) {
                val location = IntArray(2)
                lastButton.getLocationOnScreen(location)
                val bx = location[0]
                val by = location[1]
                val bw = lastButton.width
                val bh = lastButton.height

                // "Vùng an toàn" quanh phím, ví dụ 16dp
                val extra = 16 * resources.displayMetrics.density

                val stillNear =
                    x >= bx - extra && x <= bx + bw + extra &&
                            y >= by - extra && y <= by + bh + extra

                // Chỉ khi thật sự ra xa khỏi vùng này mới reset
                if (!stillNear) {
                    lastPlayedButtonMap[pointerId] = null
                }
            }
            // FIX: Nếu swipe ra ngoài (không chạm button nào), reset tracking
            // Điều này cho phép user swipe lại vào button cũ và vẫn phát âm

           // lastPlayedButtonMap[pointerId] = null
        }
    }

    private fun isTouchingButton(button: ImageView, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        button.getLocationOnScreen(location)
        val buttonX = location[0]
        val buttonY = location[1]
        val buttonWidth = button.width
        val buttonHeight = button.height

        return x >= buttonX && x <= buttonX + buttonWidth &&
                y >= buttonY && y <= buttonY + buttonHeight
    }

    override fun viewListener() {
        binding.btnHome.setOnSingleClick {
            if (RecordingManager.isRecording()) {
                Toast.makeText(this,
                    getString(R.string.the_recording_was_not_saved), Toast.LENGTH_SHORT).show()
                showInterAll { goHome() }
            } else {
                showInterAll { goHome() }
            }
        }

        binding.apply {
            btnRecord.setOnSingleClick {
                startRecording()
            }

            btnStop.setOnSingleClick {
                when {
                    isLearningMode -> stopLearningMode()
                    isPlaybackMode -> stopPlayback()
                    RecordingManager.isRecording() -> stopRecording()

                }
            }

            btnMusic.setOnSingleClick {
              //  Toast.makeText(this@PlayActivity, "Music button clicked!", Toast.LENGTH_SHORT).show()

                try {
                    val intent = Intent(this@PlayActivity, SongListActivity::class.java)
                    if (!isLearningMode && !isPlaybackMode) {
                        intent.putExtra("clear_focus", true)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                 //   Toast.makeText(this@PlayActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun playNoteSound(resId: Int) {
        try {
            // Play sound với volume boost từ instrument hiện tại
            SoundHelper.playSound(resId, currentInstrument.volumeBoost)

            // Nếu đang recording, ghi lại nốt nhạc
            if (RecordingManager.isRecording() && !isPlaybackMode) {
                val button = buttonSoundMap.entries.find { it.value == resId }?.key
                val noteName = button?.let { buttonNameMap[it] } ?: "Unknown"
                RecordingManager.recordNote(resId, noteName)
            }
        } catch (e: Exception) {
            // Bỏ qua nếu âm thanh không phát được
            e.printStackTrace()
        }
    }

    // Animate button khi phát âm thanh
    private fun animateButton(button: ImageView) {
        try {
            // Load và start pulse animation
            val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.button_pulse)
            button.startAnimation(pulseAnim)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Setup UI cho recording
    private fun setupRecordingUI() {
        binding.apply {
            btnStop.invisible()
            btnCount.invisible()
            btnRecord.visible()
            btnMusic.visible()
            tvCount.text = "00:00"
        }
    }

    // Kiểm tra có phải playback mode không
    private fun checkPlaybackMode() {
        recordingId = intent.getStringExtra("recording_id")
        if (recordingId != null) {
            isPlaybackMode = true
            playbackRecording()
        }
    }

    // Bắt đầu recording tôi muốn lần đầu vào play thì focus xy
    private fun startRecording() {
        // Clear playback mode khi bắt đầu recording mới
        isPlaybackMode = false
        recordingId = null
        intent.removeExtra("recording_id")

        // Pass instrument hiện tại vào RecordingManager
        RecordingManager.startRecording(currentInstrument.name)

        binding.apply {
            btnRecord.invisible()
            btnMusic.invisible()
            btnStop.visible()
            btnCount.visible()

            // DISABLE instrument switching khi đang recording
            img1.isEnabled = false
            img2.isEnabled = false
            img3.isEnabled = false
            img1.alpha = 0.3f
            img2.alpha = 0.3f
            img3.alpha = 0.3f
        }

        startTimer()
    }

    // Dừng recording
    private fun stopRecording() {
        val pendingRecording = RecordingManager.stopRecording()  // Dừng nhưng CHƯA lưu
        stopTimer()

        binding.apply {
            btnRecord.isVisible = true
            btnMusic.isVisible = true
            btnStop.isVisible = false
            btnCount.isVisible = false
            tvCount.text = "00:00"
            // RE-ENABLE instrument switching sau khi stop recording
            img1.isEnabled = true
            img2.isEnabled = true
            img3.isEnabled = true
            // Restore alpha dựa trên instrument hiện tại
            updateInstrumentUI()
        }

        // Show dialog để user nhập tên
        pendingRecording?.let { recording ->
            showRecordingNameDialog(recording)
        }
    }

    private fun stopPlayback(){
        isPlaybackStopped = true

        playbackRunnables.forEach { playbackHandler.removeCallbacks(it) }
        playbackRunnables.clear()

        // Stop playback timer
        stopPlaybackTimer()

        setupRecordingUI()
        isPlaybackMode = false

        // Clear recordingId để không tự động playback lại khi activity recreate
        recordingId = null
        intent.removeExtra("recording_id")

        // Re-enable instrument switching
        binding.apply {
            img1.isEnabled = true
            img2.isEnabled = true
            img3.isEnabled = true
            updateInstrumentUI()
            idMusic.setText(R.string.music)
        }
    }
    private fun stopLearningMode() {
        isLearningMode = false
        currentSong = null
        currentNoteIndex = 0

        // Clear lastPlayedSongId vì đã stop → reset focus khi quay lại
        preferenceHelper.clearLastPlayedSongId()

        highlightedButton?.apply {
            clearAnimation()
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
        highlightedButton = null

        // bật lại instrument
        binding.apply {
            img1.isEnabled = true
            img2.isEnabled = true
            img3.isEnabled = true
            updateInstrumentUI()
            idMusic.setText(R.string.music)
        }
        binding.imgSunGuide.gone()
        sunJumpAnimator?.cancel()
        sunJumpAnimator = null


        setupRecordingUI()
    }


    /**
     * Show dialog để nhập tên cho recording
     */
    private fun showRecordingNameDialog(recording: Recording) {
        val dialog = com.xylophone.learning.music.dialog.RecordingNameDialog(this, recording)

        // Callback khi user bấm Save
        dialog.onSaveClick = { name ->
            // Lưu recording với tên user đã nhập
            showInterAll {   RecordingManager.savePendingRecording(this, name) }
            Toast.makeText(
                this,
                getString(R.string.recording_saved, name),
                Toast.LENGTH_SHORT
            ).show()        }

        // Callback khi user bấm Cancel
        dialog.onCancelClick = {
            // Discard recording (không lưu)
            RecordingManager.discardPendingRecording()
            Toast.makeText(
                this,
                getString(R.string.recording_discarded),
                Toast.LENGTH_SHORT
            ).show()
        }

        dialog.show()
        Admob.getInstance().loadNativeAd(
            this,
            getString(R.string.native_dialog),
            dialog.binding.nativeAds,
            R.layout.ads_native_medium_btn_bottom_2_dialog
        )
    }

    // Start timer
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                val duration = RecordingManager.getCurrentDuration()
                binding.tvCount.text = formatDuration(duration)
                timerHandler.postDelayed(this, 100)
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    // Stop timer
    private fun stopTimer() {
        timerRunnable?.let {
            timerHandler.removeCallbacks(it)
        }
        timerRunnable = null
    }

    // Format duration to mm:ss
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Start playback timer
    private fun startPlaybackTimer(duration: Long) {
        playbackStartTime = System.currentTimeMillis()
        playbackTimerRunnable = object : Runnable {
            override fun run() {
                if (isPlaybackStopped || isDestroyed || isFinishing) return

                val elapsed = System.currentTimeMillis() - playbackStartTime
                if (elapsed <= duration) {
                    binding.tvCount.text = formatDuration(elapsed)
                    timerHandler.postDelayed(this, 100)
                }
            }
        }
        timerHandler.post(playbackTimerRunnable!!)
    }

    // Stop playback timer
    private fun stopPlaybackTimer() {
        playbackTimerRunnable?.let {
            timerHandler.removeCallbacks(it)
        }
        playbackTimerRunnable = null
    }

    // Playback recording
    @SuppressLint("SuspiciousIndentation")
    private fun playbackRecording() {
        val recording = recordingId?.let { RecordingManager.getRecording(this, it) } ?: return

        // AUTO-LOAD instrument từ recording
        val recordedInstrument = Instrument.fromName(recording.instrumentName)
        if (currentInstrument != recordedInstrument) {
            // Switch sang instrument đúng (không cần save preference vì đây là playback mode)
            currentInstrument = recordedInstrument
            SoundHelper.releaseAllSounds()
            loadSounds()
            setupButtonMap()
            updateInstrumentUI()
        }

        isPlaybackStopped = false
        playbackRunnables.forEach { playbackHandler.removeCallbacks(it) }
        playbackRunnables.clear()

        binding.apply {
            btnRecord.invisible()
            btnMusic.invisible()
            btnStop.visible()
            btnCount.visible()

            // DISABLE instrument switching khi đang playback
            img1.isEnabled = false
            img2.isEnabled = false
            img3.isEnabled = false
            img1.alpha = 0.3f
            img2.alpha = 0.3f
            img3.alpha = 0.3f
        }

        // Start playback timer để update thời gian liên tục
        startPlaybackTimer(recording.duration)

        // Play notes với timing
        recording.notes.forEach { note ->


            val r = Runnable {
                // Kiểm tra Activity còn active không trước khi truy cập binding
                if(isPlaybackStopped || isDestroyed || isFinishing) return@Runnable


                    // Play sound với volume boost
                    SoundHelper.playSound(note.noteId, currentInstrument.volumeBoost)

                    // Tìm button tương ứng và chạy animation
                    val button = buttonSoundMap.entries.find { it.value == note.noteId }?.key
                    button?.let { animateButton(it) }

            }

            playbackRunnables.add(r)
            playbackHandler.postDelayed(r,note.timestamp)
        }

        // Khi xong, reset UI và re-enable instrument switching


        val endR = Runnable {
            // Kiểm tra Activity còn active không trước khi truy cập binding
            if(isPlaybackStopped || isDestroyed || isFinishing) return@Runnable

            // Stop playback timer
            stopPlaybackTimer()

            setupRecordingUI()
            isPlaybackMode = false

            // Clear recordingId để không tự động playback lại khi activity recreate
            recordingId = null
            intent.removeExtra("recording_id")

            // RE-ENABLE instrument switching sau khi playback xong
            binding.apply {
                img1.isEnabled = true
                img2.isEnabled = true
                img3.isEnabled = true
                updateInstrumentUI()
            }

        }
         playbackRunnables.add(endR)
        playbackHandler.postDelayed(endR,recording.duration +500)

    }

    // ==================== LEARNING MODE FUNCTIONS ====================

    // Kiểm tra có phải learning mode không
    private fun checkLearningMode() {
        val mode = intent.getStringExtra("mode")
        val songId = intent.getStringExtra("song_id")

        if (mode == "LEARNING_MODE" && songId != null) {
            isLearningMode = true
            currentSong = SongLibrary.getSongById(songId)
            currentNoteIndex = 0

            // Lưu songId để focus lại khi quay về SongListActivity
            preferenceHelper.setLastPlayedSongId(songId)

            // Setup UI cho learning mode
            binding.apply {
                btnRecord.invisible()
                btnMusic.visible()
                btnStop.visible()
                btnCount.invisible()

                // DISABLE instrument switching trong learning mode
                img1.isEnabled = false
                img2.isEnabled = false
                img3.isEnabled = false
                img1.alpha = 0.3f
                img2.alpha = 0.3f
                img3.alpha = 0.3f
                idMusic.text = currentSong?.name?:"Music"
            }

            // Hiển thị progress
            updateLearningProgress()
            // Highlight nốt đầu tiên
            highlightCurrentNote()
        }
    }

    // Xử lý touch event trong learning mode
    // Phát âm thanh với mọi nốt, nhưng chỉ chuyển tiếp khi bấm đúng
    // Return true để luôn hiển thị icon
    private fun handleLearningModeTouch(clickedButton: ImageView): Boolean {
        val song = currentSong ?: return false

        // Check bounds để tránh crash
        if (currentNoteIndex >= song.notes.size) {
            isLearningComplete = true
            return false // Bài hát đã hoàn thành, ignore click
        }

        // Luôn phát âm thanh của nốt được bấm (dù đúng hay sai)
        val soundId = buttonSoundMap[clickedButton]
        soundId?.let { playNoteSound(it) }

        // Kiểm tra có đúng nốt không
        val clickedNoteName = buttonNameMap[clickedButton]
        val correctNoteName = song.notes[currentNoteIndex]

        if (clickedNoteName == correctNoteName) {

            // Dừng mọi animation nhấn để không “đánh nhau” với highlightCurrentNote
            clickedButton.clearAnimation()
            clickedButton.animate().cancel()
            // ✅ ĐÚNG - Move to next note ngay lập tức (không delay)
            moveToNextNote()
        }
        else{
            // ❌ SAI – chỉ lúc sai mới dùng hiệu ứng chạm
            clickedButton.animateScaleEffect(0.8f, 150)
            animateButton(clickedButton)
        }
        // ❌ SAI - Không làm gì, giữ nguyên highlight nốt hiện tại

        return true // Luôn hiển thị icon (dù đúng hay sai)
    }


    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
        // Nếu muốn “chắc” hơn thì dùng systemBars():
        // controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    // Highlight nốt hiện tại
    private fun highlightCurrentNote() {
        val song = currentSong ?: return
        if (currentNoteIndex >= song.notes.size) return

        Log.d("LEARN", "Highlight note index=$currentNoteIndex, name=${song.notes[currentNoteIndex]}")


        // Tìm button cần highlight
        val correctNoteName = song.notes[currentNoteIndex]
        val buttonToHighlight = buttonNameMap.entries.find { it.value == correctNoteName }?.key

        buttonToHighlight?.let { button ->
            // Reset button cũ nếu khác button
            if (highlightedButton != button) {
                highlightedButton?.apply {
                    clearAnimation()
                    alpha = 1.0f
                    scaleX = 1.0f
                    scaleY = 1.0f
                }
            }

            highlightedButton = button

            // Clear animation cũ trước
            button.clearAnimation()
            button.animate().cancel()

            // Reset về bình thường trước
            button.scaleX = 1.0f
            button.scaleY = 1.0f
            button.alpha = 1.0f

            // Sau đó phóng to lên để tạo hiệu ứng bounce (kể cả khi nốt lặp lại)
            button.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(100)
                .withEndAction {
                    // Start pulse animation sau khi scale xong
                    startPulseAnimation(button)
                }
                .start()

            // ✅ Sun nhảy tới đúng nốt (kể cả nốt lặp lại)
            binding.overlayContainer.post {
                jumpSunToButton(button)
            }
        }

    }

    // Animation pulse liên tục cho button được highlight
    private fun startPulseAnimation(button: ImageView) {
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.button_pulse)
        pulseAnim.repeatCount = android.view.animation.Animation.INFINITE
        button.startAnimation(pulseAnim)
    }

    // Chuyển sang nốt tiếp theo
    private fun moveToNextNote() {
        if(isLearningComplete) return
        val song = currentSong ?: return
        currentNoteIndex++

        if (currentNoteIndex >= song.notes.size) {
            // Hoàn thành bài hát
            showCompletionDialog()
        } else {
            // Highlight nốt tiếp theo
            updateLearningProgress()
            highlightCurrentNote()
        }
    }

    // Cập nhật progress
    private fun updateLearningProgress() {
        val song = currentSong ?: return
        binding.tvCount.text = "${currentNoteIndex + 1} / ${song.notes.size}"
    }

    // Hiển thị dialog hoàn thành
    private fun showCompletionDialog() {

        if(isLearningComplete) return
        isLearningComplete = true

        // Clear lastPlayedSongId vì đã hoàn thành → reset focus khi quay lại
        preferenceHelper.clearLastPlayedSongId()

        // Reset highlight
        highlightedButton?.apply {
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
            clearAnimation()
        }

        // RE-ENABLE instrument switching khi hoàn thành
        binding.apply {
            img1.isEnabled = true
            img2.isEnabled = true
            img3.isEnabled = true
            updateInstrumentUI()
        }
        binding.imgSunGuide.gone()
        sunJumpAnimator?.cancel()
        sunJumpAnimator = null


        // Launch SuccessActivity
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra("song_id", currentSong?.id)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    // ==================== END LEARNING MODE FUNCTIONS ====================

    // ==================== INSTRUMENT SWITCHING FUNCTIONS ====================

    // Load instrument based on mode (from Home or from SongList)
    private fun loadInstrumentBasedOnMode() {
        val mode = intent.getStringExtra("mode")

        if (mode == "LEARNING_MODE" || mode == "PLAYBACK_MODE") {
            // Từ SongList hoặc MyRecord → Load instrument đã lưu
            val savedInstrument = preferenceHelper.getSelectedInstrument()
            currentInstrument = Instrument.fromName(savedInstrument)
        } else {
            // Từ Home → Reset về Xylophone (mặc định)
            currentInstrument = Instrument.XYLOPHONE
            preferenceHelper.setSelectedInstrument(Instrument.XYLOPHONE.name)
        }
    }

    // Setup instrument selector click listeners
    private fun setupInstrumentSelectors() {
        binding.apply {
            img1.setOnSingleClick {
                switchInstrument(Instrument.XYLOPHONE)
            }

            img2.setOnSingleClick {
                switchInstrument(Instrument.PIANO)
            }

            img3.setOnSingleClick {
                switchInstrument(Instrument.GUITAR)
            }
        }
    }

    // Switch to a new instrument
    private fun switchInstrument(newInstrument: Instrument) {
        if (currentInstrument == newInstrument) return

        // Không cho switch instrument khi đang recording
        if (RecordingManager.isRecording()) {
            Toast.makeText(this,
                getString(R.string.cannot_switch_instrument_while_recording), Toast.LENGTH_SHORT).show()
            return
        }

        currentInstrument = newInstrument

        // Save preference
        preferenceHelper.setSelectedInstrument(newInstrument.name)

        // Update UI
        updateInstrumentUI()

        // Reload sounds and button map
        SoundHelper.releaseAllSounds()
        loadSounds()
        setupButtonMap()

        // Show feedback
       // Toast.makeText(this, "Switched to ${newInstrument.displayName}", Toast.LENGTH_SHORT).show()
    }

    // Update UI to show selected instrument
    private fun updateInstrumentUI() {
        binding.apply {
            // Default alpha (unselected)
            img1.alpha = 1f
            img2.alpha = 1f
            img3.alpha = 1f
            // Reset all to unselected state (opacity)
            img1.setImageResource(R.drawable.xylo_unselected)
            img2.setImageResource(R.drawable.piano_unselected)
            img3.setImageResource(R.drawable.guitar_unselected)

            // Highlight selected instrument
            when (currentInstrument) {
                Instrument.XYLOPHONE -> {
                    img1.setImageResource(R.drawable.xylo_selected)
                    img1.alpha = 1.0f
                }
                Instrument.PIANO -> {
                    img2.setImageResource(R.drawable.piano_selected)
                    img2.alpha = 1.0f
                }
                Instrument.GUITAR -> {
                    img3.setImageResource(R.drawable.guitar_selected)
                    img3.alpha = 1.0f
                }
            }
        }
    }

    // ==================== END INSTRUMENT SWITCHING FUNCTIONS ====================

    override fun initText() {
        // Không cần text đặc biệt cho màn hình này
    }

    override fun initActionBar() {
        // PlayActivity không sử dụng action bar layout
    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_play),
            binding.flNativeCollab
        )
    }
    override fun initAds() {
        initNativeCollab()
    }

    override fun onRestart() {
        super.onRestart()
        // Set lại 60000 khi quay lại màn Play (sau cuộc gọi, sau khi ấn Home, etc.)
        Admob.getInstance().setTimeCountdownNativeCollab(60000)
        initNativeCollab()
    }



    override fun onBackPressed() {
        if (RecordingManager.isRecording()) {
            Toast.makeText(this, getString(R.string.the_recording_was_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        // Nếu đến từ SuccessActivity → về Home
        val fromSuccess = intent.getBooleanExtra("from_success", false)
        if (fromSuccess) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        // Reset về giá trị mặc định khi rời khỏi PlayActivity (chuyển sang màn khác)
        Admob.getInstance().setTimeCountdownNativeCollab(15000)
    }

    override fun onDestroy() {
        // Stop recording nếu đang recording (tránh memory leak)
        if (RecordingManager.isRecording()) {
            RecordingManager.stopRecording()
            RecordingManager.discardPendingRecording() // Hủy recording chưa lưu
            stopTimer() // Stop timer
        }

        // Stop timer nếu đang chạy
        stopTimer()

        // Stop playback timer nếu đang chạy
        stopPlaybackTimer()

        // Hủy tất cả pending runnables để tránh crash khi Activity bị destroy
        playbackRunnables.forEach { playbackHandler.removeCallbacks(it) }
        playbackRunnables.clear()

        // Cancel sun animation
        sunJumpAnimator?.cancel()
        sunJumpAnimator = null

        Admob.getInstance().setTimeCountdownNativeCollab(15000)
        super.onDestroy()
        // SoundHelper sẽ được release khi app đóng
    }
}
