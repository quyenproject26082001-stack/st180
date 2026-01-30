package com.xylophone.learning.music.activity_app.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
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
                if (position == 1) {
                    binding.nativeAds.gone()
                } else {
                    binding.nativeAds.visible()
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