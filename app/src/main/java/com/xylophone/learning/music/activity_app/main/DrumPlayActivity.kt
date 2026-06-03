package com.xylophone.learning.music.activity_app.main

import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.hideNavigation
import com.xylophone.learning.music.core.extensions.hideSystemBars
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.helper.SoundHelper
import com.xylophone.learning.music.core.helper.SoundHelper.loadSound
import com.xylophone.learning.music.databinding.ActivityDrumBinding

class DrumPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrumBinding

    private val buttonSoundMap = mutableMapOf<ImageView, Int>()

    private val lastPlayedButtonMap = mutableMapOf<Int, ImageView?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadSounds()
        setUpButtonMap()
        setupTouchListener()
        hideNavigation(false)
        hideSystemBars()
        Admob.getInstance().setTimeCountdownNativeCollab(60000)
        initNativeCollab()
    }

    private fun setupTouchListener() {
        binding.btnBack.setOnClickListener {
           showInterAll {   handleBackLeftToRight()}
        }

        binding.root.setOnTouchListener { _, event ->
            val offsetX = event.rawX - event.x
            val offsetY = event.rawY - event.y
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    handleTouchEvent(event.rawX, event.rawY, event.getPointerId(0))
                    true
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    val idx = event.actionIndex
                    handleTouchEvent(
                        event.getX(idx) + offsetX,
                        event.getY(idx) + offsetY,
                        event.getPointerId(idx)
                    )
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        handleTouchEvent(
                            event.getX(i) + offsetX,
                            event.getY(i) + offsetY,
                            event.getPointerId(i)
                        )
                    }
                    true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    lastPlayedButtonMap.remove(event.getPointerId(event.actionIndex))
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    lastPlayedButtonMap.clear()
                    false
                }

                else -> false
            }
        }
    }

    private fun handleTouchEvent(x: Float, y: Float, pointerId: Int) {

        var found = buttonSoundMap.keys.firstOrNull { isTouching(it, x, y) }

        val soundId = found?.let { buttonSoundMap[it] }

        if (found != null && soundId != null && lastPlayedButtonMap[pointerId] != found) {
            SoundHelper.playSound(soundId)
            animateKey(found)
            lastPlayedButtonMap[pointerId] = found
        }

    }

    private fun animateKey(button: android.widget.ImageView) {

        try {
            button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_pulse))

        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    private fun isTouching(button: ImageView, x: Float, y: Float): Boolean {
        val loc = IntArray(2)
        button.getLocationOnScreen(loc)

        // For scaleX=-1 parent, getLocationOnScreen returns the visual right edge (not left),
        // because the button's local (0,0) maps to its visual top-right after the flip.
        val parent = button.parent as? android.view.View
        val left = if (parent?.scaleX == -1f) loc[0] - button.width else loc[0]

        return x >= left && x <= left + button.width &&
                y >= loc[1] && y <= loc[1] + button.height
    }

    private fun setUpButtonMap() {
        binding.apply {
            buttonSoundMap[btnLeftSnare] = R.raw.drum_snare
            buttonSoundMap[btnLeftHihat] = R.raw.drum_hihat
            buttonSoundMap[btnLeftCrash] = R.raw.drum_crash
            buttonSoundMap[btnLeftKick] = R.raw.drum_kick

            buttonSoundMap[btnRightSnare] = R.raw.drum_snare
            buttonSoundMap[btnRightHihat] = R.raw.drum_hihat
            buttonSoundMap[btnRightCrash] = R.raw.drum_crash
            buttonSoundMap[btnRightKick] = R.raw.drum_kick


        }
    }

    private fun loadSounds() {
        listOf(
            R.raw.drum_kick,
            R.raw.drum_snare,
            R.raw.drum_hihat,
            R.raw.drum_crash
        ).forEach { resId ->
            if (!SoundHelper.isSoundNotNull(resId)) {
                SoundHelper.loadSound(this, resId)
            }
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
            getString(R.string.native_cl_drum),
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