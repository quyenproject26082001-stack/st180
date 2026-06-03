package com.xylophone.learning.music.activity_app.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.rateApp
import com.xylophone.learning.music.core.extensions.select
import com.xylophone.learning.music.core.extensions.startIntentRightToLeft
import com.xylophone.learning.music.core.extensions.visible
import com.xylophone.learning.music.core.helper.LanguageHelper
import com.xylophone.learning.music.core.helper.MediaHelper
import com.xylophone.learning.music.core.utils.key.ValueKey
import com.xylophone.learning.music.core.utils.state.RateState
import com.xylophone.learning.music.databinding.ActivityHomeBinding
import com.xylophone.learning.music.activity_app.SettingsActivity

import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.setGradientTextHeightColor
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.extensions.strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        deleteTempFolder()
        setupTextStroke()
    }

    private fun setupTextStroke() {
        // Enable marquee effect for long text
        binding.tv1.isSelected = true
        binding.tv2.isSelected = true
        binding.tv3.isSelected = true
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.setOnSingleClick { startIntentRightToLeft(SettingsActivity::class.java) }

            btnPlayHome.setOnSingleClick {
                Log.d("MainActivity", "btnPlayHome clicked - Opening PlayActivity")
                showInterAll {
                    startIntentRightToLeft(PlayActivity::class.java)
                }
            }

            btnMyRecordHome.setOnSingleClick {
                Log.d("MainActivity", "btnMyRecordHome clicked - Opening MyRecordActivity")
                showInterAll {
                    startIntentRightToLeft(MyRecordActivity::class.java)
                }
            }
            btnMoreInstrument.setOnSingleClick {
                startIntentRightToLeft(ChooseInstrumentActivity::class.java)
            }


        }
    }

    override fun initText() {
        super.initText()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarRight.setImageResource(R.drawable.ic_settings)
            btnActionBarRight.visible()
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 != 0) {
            rateApp(sharePreference) { state ->
                when (state) {
                    RateState.LESS3 -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            finishAffinity()
                        }
                    }

                    RateState.GREATER3 -> {
                        // Thoát app sau khi rate
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            finishAffinity()
                        }
                    }

                    RateState.CANCEL -> {
                        lifecycleScope.launch {
                            sharePreference.setCountBack(sharePreference.getCountBack() + 1)
                            withContext(Dispatchers.Main) {
                                delay(1000)
                                finishAffinity()
                            }
                        }
                    }
                }
            }
        } else {
            sharePreference.setCountBack(sharePreference.getCountBack() + 1)
            finishAffinity()
        }
    }

    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataTemp =
                MediaHelper.getImageInternal(this@MainActivity, ValueKey.DOWNLOAD_ALBUM_BACKGROUND)
            if (dataTemp.isNotEmpty()) {
                dataTemp.forEach {
                    val file = File(it)
                    file.delete()
                }
            }
        }
    }

    private fun updateText() {
        binding.apply {

            tv1.setText(R.string.play)
            tv2.setText(R.string.my_record)
            tv3.setText(R.string.more_instrument)
            // Re-enable marquee after text update
            tv1.isSelected = true
            tv2.isSelected = true
        }
    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        updateText()
        initNativeCollab()

    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_home),
            binding.flNativeCollab
        )
    }

    override fun initAds() {
        initNativeCollab()
        Admob.getInstance().loadInterAll(this, getString(R.string.inter_all))
    }


}