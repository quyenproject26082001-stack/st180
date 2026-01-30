package com.xylophone.learning.music.activity_app.main

import android.content.Intent
import android.view.LayoutInflater
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.loadNativeCollabAds
import com.xylophone.learning.music.core.extensions.select
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.helper.LanguageHelper
import com.xylophone.learning.music.databinding.ActivitySuccessBinding

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {

    private var songId: String? = null

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
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
    override fun initView() {
        binding.play.select()
        // Get song ID from intent if passed
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        songId = intent.getStringExtra("song_id")
    }

    override fun viewListener() {
        binding.apply {
            // Play button - go back to PlayActivity (normal mode)
            btnBgPlay.setOnSingleClick {
                // Go back to PlayActivity (normal mode, not learning)
                val intent = Intent(this@SuccessActivity, PlayActivity::class.java)
                // Flag để PlayActivity biết đến từ Success → bấm back sẽ về Home
                intent.putExtra("from_success", true)
                showInterAll {
                    startActivity(intent)
                    finish()
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        }
    }

    override fun initText() {
        // Text already set in layout
    }
    override fun initActionBar() {
        // No action bar for success screen
    }

    override fun onBackPressed() {
        // Go back to PlayActivity (normal mode)
        val intent = Intent(this@SuccessActivity, PlayActivity::class.java)
        // Flag để PlayActivity biết đến từ Success → bấm back sẽ về Home
        intent.putExtra("from_success", true)
        startActivity(intent)
        finish()
    }
    override fun initAds() {
        initNativeCollab()
    }


    fun initNativeCollab() {

        loadNativeCollabAds(R.string.native_cl_success, binding.flNativeCollab)


    }
    override fun onRestart() {
        super.onRestart()

    }


}
