package com.xylophone.learning.music.activity_app.main

import android.R.attr.button
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
import com.xylophone.learning.music.databinding.ActivityPianoBinding

class PianoPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPianoBinding

    private val buttonSoundMap = mutableMapOf<ImageView, Int>()

    private val blackKeys = mutableListOf<ImageView>()

    private val lastPlayedButtonMap = mutableMapOf<Int, ImageView?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPianoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadSounds()
        setUpButtonMap()
        setupTouchListener()
        hideNavigation(false)
        hideSystemBars()
        Admob.getInstance().setTimeCountdownNativeCollab(60000)
        initNativeCollab()
    }




    private fun loadSounds() {
        listOf(
            R.raw.samples_piano_c3,
            R.raw.samples_piano_d3,
            R.raw.samples_piano_e3,
            R.raw.samples_piano_f3,
            R.raw.samples_piano_g3,
            R.raw.samples_piano_a3,
            R.raw.samples_piano_b3,
            R.raw.samples_piano_c4,
            R.raw.samples_piano_cs3,
            R.raw.samples_piano_ds3,
            R.raw.samples_piano_fs3,
            R.raw.samples_piano_gs3,
            R.raw.samples_piano_as3,
        ).forEach { resId ->
            if(!SoundHelper.isSoundNotNull(resId)){
                SoundHelper.loadSound(this,resId)
            }
        }

    }
    private fun setUpButtonMap() {
            binding.apply {
                buttonSoundMap[keyDo]  = R.raw.samples_piano_c3
                buttonSoundMap[keyRe]  = R.raw.samples_piano_d3
                buttonSoundMap[keyMi]  = R.raw.samples_piano_e3
                buttonSoundMap[keyFa]  = R.raw.samples_piano_f3
                buttonSoundMap[keySol] = R.raw.samples_piano_g3
                buttonSoundMap[keyLa]  = R.raw.samples_piano_a3
                buttonSoundMap[keySi]  = R.raw.samples_piano_b3
                buttonSoundMap[keyDo2] = R.raw.samples_piano_c4

                // Phím đen (thăng)
                buttonSoundMap[keyDoSharp]  = R.raw.samples_piano_cs3
                buttonSoundMap[keyReSharp]  = R.raw.samples_piano_ds3
                buttonSoundMap[keyFaSharp]  = R.raw.samples_piano_fs3
                buttonSoundMap[keySolSharp] = R.raw.samples_piano_gs3
                buttonSoundMap[keyLaSharp]  = R.raw.samples_piano_as3

                blackKeys.addAll(listOf(keyDoSharp, keyReSharp, keyFaSharp, keySolSharp, keyLaSharp))

            }
    }

    private fun setupTouchListener() {
        binding.btnBack.setOnClickListener {
             handleBackLeftToRight()}


        val touchListener = { _: android.view.View, event: MotionEvent ->
        when(event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            handleTouchEvent(event.rawX, event.rawY, event.getPointerId(0))
            true

        }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = event.actionIndex
                val x = event.getX(idx) + (event.rawX - event.x)
                val y = event.getY(idx) + (event.rawY - event.y)
                handleTouchEvent(x,y, event.getPointerId(idx))
                true
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount){
                    val x = event.getX(i) + (event.rawX - event.x)
                    val y = event.getY(i) + (event.rawY - event.y)
                    handleTouchEvent(x,y, event.getPointerId(i))
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
        binding.apply {
            listOf(keyDo,keyRe,keyMi,keyFa,keySol,keyLa,keySi,keyDo2,keyDoSharp,keyReSharp,keySolSharp,keyLaSharp,
                pianoFrame).forEach { it.setOnTouchListener(touchListener) }
        }
    }

    private fun handleTouchEvent(x: Float, y: Float, pointerId: Int) {

        var found: ImageView? = blackKeys.firstOrNull{ isTouching(it,x,y)}

        if(found == null){
            found = buttonSoundMap.keys.firstOrNull { isTouching(it,x,y) }
        }

        val soundId = found?.let {buttonSoundMap[it]}
        if(found != null && soundId != null && lastPlayedButtonMap[pointerId] != found) {
            SoundHelper.playSound(soundId)
            animateKey(found)
            lastPlayedButtonMap[pointerId] = found
        }

    }

    private fun animateKey(button: android.widget.ImageView) {

        try {
            button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_pulse))
        } catch (e : Exception){
            e.printStackTrace()
        }

    }

    fun isTouching(button: android.widget.ImageView, x: Float, y: Float): Boolean {
        val loc = IntArray(2)
        button.getLocationOnScreen(loc)
        return x>=loc[0] && x<=loc[0] + button.width &&
                y >=loc[1] && y<= loc[1] + button.height
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
        Admob.getInstance().setTimeCountdownNativeCollab(60000)

    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_piano),
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