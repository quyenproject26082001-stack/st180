package com.xylophone.learning.music

import android.app.Application
import com.lvt.ads.util.AdsApplication
import com.lvt.ads.util.AppOpenManager
import com.xylophone.learning.music.activity_app.splash.SplashActivity

class App : AdsApplication() {


    override fun onCreate() {
        super.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }

    override fun enableAdsResume(): Boolean {
        return false
    }

    override fun getListTestDeviceId(): MutableList<String>? {
        return null
    }

    override fun getResumeAdId(): String {
        return getString(R.string.open_resume)
    }

    override fun buildDebug(): Boolean {
        return true
    }

}