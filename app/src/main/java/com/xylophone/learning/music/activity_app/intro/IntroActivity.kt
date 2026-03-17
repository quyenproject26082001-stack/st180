package com.xylophone.learning.music.activity_app.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.utils.DataLocal
import com.xylophone.learning.music.databinding.ActivityIntroBinding
import com.xylophone.learning.music.activity_app.main.MainActivity
import com.xylophone.learning.music.activity_app.permission.PermissionActivity
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.visible
import kotlin.system.exitProcess
import kotlin.text.toInt

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private val introAdapter by lazy { IntroAdapter(this) }

    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initVpg()
    }

    override fun viewListener() {
        binding.btnNext.setOnSingleClick { handleNext() }

        binding.vpgTutorial.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val params = binding.btnNext.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

                if (position == 0) {
                    binding.nativeAds.gone()
                    // Set lại width về 100dp
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = (48 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (5 * resources.displayMetrics.density).toInt()
                    params.marginEnd = (5 * resources.displayMetrics.density).toInt()

                    // Chiều cao
                    binding.btnNext.layoutParams = params
                    binding.btnNext.setTextColor(resources.getColor(R.color.white, null))
                    binding.btnNext.setBackgroundResource(R.drawable.bg_btn_ads)
                } else {
                    binding.nativeAds.visible()
                    // Đổi sang wrap_content
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    params.marginEnd = (16 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (0 * resources.displayMetrics.density).toInt()
                    binding.btnNext.layoutParams = params
                    binding.btnNext.setTextColor(Color.parseColor("#80F4FF"))
                    binding.btnNext.background = null
                }
            }
        })

    }

    override fun initText() {}

    override fun initActionBar() {}

    private fun initVpg() {
        binding.apply {
            binding.vpgTutorial.adapter = introAdapter
            binding.dotsIndicator.attachTo(binding.vpgTutorial)
            introAdapter.submitList(DataLocal.itemIntroList)
        }
    }

    private fun handleNext() {
        binding.apply {
            val nextItem = binding.vpgTutorial.currentItem + 1
            if (nextItem < DataLocal.itemIntroList.size) {
                vpgTutorial.setCurrentItem(nextItem, true)
            } else {
                val intent =
                    if (sharePreference.getIsFirstPermission()) {
                        Intent(this@IntroActivity, PermissionActivity::class.java)
                    } else {
                        Intent(this@IntroActivity, MainActivity::class.java)
                    }
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    override fun initAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_intro), binding.nativeAds, R.layout.ads_native_medium_btn_bottom)
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() { exitProcess(0) }
}