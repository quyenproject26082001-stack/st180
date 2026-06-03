package com.xylophone.learning.music.activity_app.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.hideNavigation
import com.xylophone.learning.music.core.extensions.hideSystemBars
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.helper.SoundHelper
import com.xylophone.learning.music.databinding.ActivityGuitarBinding

class GuitarPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuitarBinding
    private val lastPlayedMap = mutableMapOf<Int, Pair<Int, Int>?>()

    companion object {
        private const val DEBUG_OVERLAY = false

        // --- CHỈNH Ở ĐÂY ---
        private const val BODY_RATIO = 0.41f       // body kết thúc tại x%
        private const val NECK_END_RATIO = 0.88f   // headstock bắt đầu tại x%

        // Chỉ cần chỉnh 4 giá trị này — các dây giữa tự chia đều
        private const val STR_TOP_BODY   = 0.33f  // dây 1 (E4) tại body
        private const val STR_BOTTOM_BODY = 0.67f  // dây 6 (E2) tại body
        private const val STR_TOP_HEAD   = 0.39f  // dây 1 (E4) tại headstock
        private const val STR_BOTTOM_HEAD = 0.61f  // dây 6 (E2) tại headstock

        private val STRING_Y_BODY = FloatArray(6) { i -> STR_TOP_BODY + i * (STR_BOTTOM_BODY - STR_TOP_BODY) / 5f }
        private val STRING_Y_HEAD = FloatArray(6) { i -> STR_TOP_HEAD + i * (STR_BOTTOM_HEAD - STR_TOP_HEAD) / 5f }
    }

    // 6 strings × 12 frets — chuẩn tuning E A D G B E
    private val stringNotes = arrayOf(
        // String 1 (E4) — trên cùng
        arrayOf(R.raw.guitar_e4, R.raw.guitar_f4, R.raw.guitar_f_sharp_4, R.raw.guitar_g4, R.raw.guitar_g_sharp_3, R.raw.guitar_a4, R.raw.guitar_a_sharp_4, R.raw.guitar_b4, R.raw.guitar_c5, R.raw.guitar_c_sharp_5, R.raw.guitar_d5, R.raw.guitar_d_sharp_4),
        // String 2 (B3)
        arrayOf(R.raw.guitar_b3, R.raw.guitar_c4, R.raw.guitar_c_sharp_4, R.raw.guitar_d4, R.raw.guitar_d_sharp_4, R.raw.guitar_e4, R.raw.guitar_f4, R.raw.guitar_f_sharp_4, R.raw.guitar_g4, R.raw.guitar_g_sharp_3, R.raw.guitar_a4, R.raw.guitar_a_sharp_4),
        // String 3 (G3)
        arrayOf(R.raw.guitar_g3, R.raw.guitar_g_sharp_3, R.raw.guitar_a3, R.raw.guitar_a_sharp_3, R.raw.guitar_b3, R.raw.guitar_c4, R.raw.guitar_c_sharp_4, R.raw.guitar_d4, R.raw.guitar_d_sharp_4, R.raw.guitar_e4, R.raw.guitar_f4, R.raw.guitar_f_sharp_4),
        // String 4 (D3)
        arrayOf(R.raw.guitar_d3, R.raw.guitar_d_sharp_3, R.raw.guitar_e3, R.raw.guitar_f3, R.raw.guitar_f_sharp_3, R.raw.guitar_g3, R.raw.guitar_g_sharp_3, R.raw.guitar_a3, R.raw.guitar_a_sharp_3, R.raw.guitar_b3, R.raw.guitar_c4, R.raw.guitar_c_sharp_4),
        // String 5 (A2)
        arrayOf(R.raw.guitar_a2, R.raw.guitar_a_sharp_2, R.raw.guitar_b2, R.raw.guitar_c3, R.raw.guitar_c_sharp_3, R.raw.guitar_d3, R.raw.guitar_d_sharp_3, R.raw.guitar_e3, R.raw.guitar_f3, R.raw.guitar_f_sharp_3, R.raw.guitar_g3, R.raw.guitar_g_sharp_3),
        // String 6 (E2) — dưới cùng
        arrayOf(R.raw.guitar_e2, R.raw.guitar_f2, R.raw.guitar_f_sharp_2, R.raw.guitar_g2, R.raw.guitar_g_sharp_2, R.raw.guitar_a2, R.raw.guitar_a_sharp_2, R.raw.guitar_b2, R.raw.guitar_c3, R.raw.guitar_c_sharp_3, R.raw.guitar_d3, R.raw.guitar_d_sharp_3)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuitarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadSounds()
        setupListeners()
        binding.fretboardOverlay.visibility = if (DEBUG_OVERLAY) android.view.View.VISIBLE else android.view.View.GONE
        binding.fretboard.post {
            if (DEBUG_OVERLAY) {
                binding.fretboardOverlay.apply {
                    bodyRatio = BODY_RATIO
                    neckEndRatio = NECK_END_RATIO
                    stringYBodyRatios = STRING_Y_BODY
                    stringYHeadRatios = STRING_Y_HEAD
                    invalidate()
                }
            }
            binding.guitarStringsAnim.apply {
                bodyRatio = BODY_RATIO
                neckEndRatio = NECK_END_RATIO
                stringYBodyRatios = STRING_Y_BODY
                stringYHeadRatios = STRING_Y_HEAD
            }
        }
        hideNavigation(false)
        hideSystemBars()
        Admob.getInstance().setTimeCountdownNativeCollab(60000)
        initNativeCollab()
    }

    private fun loadSounds() {
        stringNotes.flatten().distinct().forEach { resId ->
            if (!SoundHelper.isSoundNotNull(resId)) {
                SoundHelper.loadSound(this, resId)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.btnBack.setOnClickListener { showInterAll {  handleBackLeftToRight() }}

        binding.scrollFretboard.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    handleFretTouch(event.x, event.y, event.getPointerId(0))
                    false
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    val idx = event.actionIndex
                    handleFretTouch(event.getX(idx), event.getY(idx), event.getPointerId(idx))
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        handleFretTouch(event.getX(i), event.getY(i), event.getPointerId(i))
                    }
                    false
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    lastPlayedMap.remove(event.getPointerId(event.actionIndex))
                    false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    lastPlayedMap.clear()
                    false
                }
                else -> false
            }
        }
    }

    private fun handleFretTouch(x: Float, y: Float, pointerId: Int) {
        val fretboardW = binding.fretboard.width.toFloat()
        val fretboardH = binding.fretboard.height.toFloat()
        if (fretboardW == 0f || fretboardH == 0f) return

        val absoluteX = x + binding.scrollFretboard.scrollX
        val neckStart = fretboardW * BODY_RATIO
        val neckEnd = fretboardW * NECK_END_RATIO
        val neckX = absoluteX - neckStart
        if (neckX < 0 || absoluteX > neckEnd) return

        val neckWidth = neckEnd - neckStart
        val neckProgress = neckX / neckWidth  // 0 = body end, 1 = headstock end

        // Y của từng dây tại vị trí X đang chạm (interpolate body→head)
        fun stringYAt(i: Int) = fretboardH * (STRING_Y_BODY[i] + (STRING_Y_HEAD[i] - STRING_Y_BODY[i]) * neckProgress)

        val topY = stringYAt(0) - fretboardH * 0.03f
        val bottomY = stringYAt(5) + fretboardH * 0.03f
        if (y < topY || y > bottomY) return

        val fret = ((neckX / neckWidth) * 12).toInt().coerceIn(0, 11)
        val string = STRING_Y_BODY.indices.minByOrNull { kotlin.math.abs(stringYAt(it) - y) } ?: 0

        val current = Pair(string, fret)
        if (lastPlayedMap[pointerId] != current) {
            SoundHelper.playSound(stringNotes[string][fret])
            binding.guitarStringsAnim.pluckString(string, fret)
            lastPlayedMap[pointerId] = current
        }
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
        Admob.getInstance().setTimeCountdownNativeCollab(60000)

    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_guitar),
            binding.flNativeCollab
        )
    }

    fun initAds() {
        initNativeCollab()
    }

    override fun onStop() {
        super.onStop()
        // Reset về giá trị mặc định khi rời khỏi PlayActivity (chuyển sang màn khác)
        Admob.getInstance().setTimeCountdownNativeCollab(15000)
    }

    override fun onDestroy() {
        super.onDestroy()
        Admob.getInstance().setTimeCountdownNativeCollab(15000)

    }
}
