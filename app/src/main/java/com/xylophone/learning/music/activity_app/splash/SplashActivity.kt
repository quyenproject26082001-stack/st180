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

    private val splashDuration = 3000L // 3 seconds

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {

        Admob.getInstance().setTimeCountdownNativeCollab(15000)

        Admob.getInstance().setTimeLimitShowAds(3000)
        Admob.getInstance().setOpenShowAllAds(true)
        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(intentActivity)
                finishAffinity()
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

        Admob.getInstance().loadSplashInterAds(
            this@SplashActivity,
            getString(R.string.inter_splash),
            30000,
            2000,
            interCallBack
        )

        // Wait exactly 3 seconds before navigating
        binding.root.postDelayed({
            if (!isFinishing) {
                startActivity(intentActivity)
                finishAffinity()
            }
        }, splashDuration)

        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
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