package com.xylophone.learning.music.activity_app.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.extensions.loadNativeCollabAds

import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.initNetworkMonitor
import com.xylophone.learning.music.databinding.ActivitySplashBinding
import com.xylophone.learning.music.activity_app.intro.IntroActivity
import com.xylophone.learning.music.activity_app.language.LanguageActivity
import com.xylophone.learning.music.activity_app.main.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var intentActivity: Intent? = null
    private val dataViewModel: DataViewModel by viewModels()

    var interCallBack: InterCallback? = null

    private val nativeAdsDuration = 5000L // 5 seconds để hiện native ads
    private val minSplashDuration = 3000L // 3 seconds tối thiểu nếu không có ads
    private val maxSplashDuration = 10000L // 10 seconds tối đa (safety timeout)
    private var hasNavigated = false // Flag để tránh navigate nhiều lần

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {

        Admob.getInstance().setTimeCountdownNativeCollab(15000)

        Admob.getInstance().setTimeLimitShowAds(3000)

        // Set flag để control ads (true = show ads, false = không show ads)
        val shouldShowAds = true // Đổi thành true nếu muốn show ads
        Admob.getInstance().setOpenShowAllAds(shouldShowAds)

        // Xác định màn hình tiếp theo
        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }

        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                // Chỉ navigate một lần
                if (!hasNavigated) {
                    hasNavigated = true
                    startActivity(intentActivity)
                    finishAffinity()
                }
            }
        }


        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }


        initNetworkMonitor()


        // Start loading data
        dataViewModel.ensureData(this)

        // Check xem có show ads không

        if (shouldShowAds) {
            // Nếu có ads: Load inter ads và show sau 5 giây
            Admob.getInstance().loadSplashInterAds(
                this@SplashActivity,
                getString(R.string.inter_splash),
                30000,
                2000,
                interCallBack
            )

            // Sau 5 giây hiện native ads, show inter ads
            binding.root.postDelayed({
                if (!isFinishing && !hasNavigated) {
                    // Show inter ads sau 5 giây
                    Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 0)
                }
            }, nativeAdsDuration)
        } else {
            // Nếu không có ads (openAds = false hoặc không có mạng): Navigate sau 3 giây tối thiểu
            binding.root.postDelayed({
                if (!isFinishing && !hasNavigated) {
                    hasNavigated = true
                    startActivity(intentActivity)
                    finishAffinity()
                }
            }, minSplashDuration)
        }

        // Safety timeout: Đảm bảo luôn navigate sau 10 giây tối đa (kể cả khi ads fail)
        binding.root.postDelayed({
            if (!isFinishing && !hasNavigated) {
                hasNavigated = true
                startActivity(intentActivity)
                finishAffinity()
            }
        }, maxSplashDuration)

    }

    override fun dataObservable() {

        lifecycleScope.launch { delay(7000)



        }


        // Data will be loaded in background, but we won't navigate until 3 seconds pass
    }

    override fun viewListener() {
    }

    fun initNativeCollab() {
        loadNativeCollabAds(R.string.native_splash, binding.flNativeCollab)
    }

    override fun initAds() {
        initNativeCollab()
        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))

    }

    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }

    override fun initText() {}

    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}
}