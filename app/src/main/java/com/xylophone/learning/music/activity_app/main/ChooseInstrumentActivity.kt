package com.xylophone.learning.music.activity_app.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.hideNavigation
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.extensions.startIntentRightToLeft
import com.xylophone.learning.music.core.helper.LanguageHelper
import com.xylophone.learning.music.databinding.ActivityChooseInstrumentBinding

class ChooseInstrumentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseInstrumentBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.setLocale(this)

        binding = ActivityChooseInstrumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGuitar.setOnSingleClick {
            showInterAll { startActivity(Intent(this, GuitarPlayActivity::class.java))}
        }
        binding.btnDrum.setOnSingleClick {

            showInterAll {  startActivity(Intent(this, DrumPlayActivity::class.java))}

            }

        binding.btnPiano.setOnSingleClick {
            showInterAll {  startActivity(Intent(this, PianoPlayActivity::class.java))}
        }

        binding.actionBar.btnActionBarLeft.apply {
            setImageResource(R.drawable.ic_back)
            visibility = android.view.View.VISIBLE
            setOnSingleClick { showInterAll {  handleBackLeftToRight() }}
        }
        hideNavigation(true)

        initNativeCollab()

    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        initNativeCollab()

    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_moreInstrument),
            binding.flNativeCollab
        )
    }

    fun initAds() {
        initNativeCollab()
    }


}