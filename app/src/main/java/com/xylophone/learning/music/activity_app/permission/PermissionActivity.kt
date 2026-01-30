package com.xylophone.learning.music.activity_app.permission

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R

import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.checkPermissions
import com.xylophone.learning.music.core.extensions.goToSettings
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.requestPermission
import com.xylophone.learning.music.core.extensions.select
import com.xylophone.learning.music.core.extensions.showToast
import com.xylophone.learning.music.core.extensions.startIntentRightToLeft
import com.xylophone.learning.music.core.extensions.visible
import com.xylophone.learning.music.core.helper.StringHelper
import com.xylophone.learning.music.core.utils.key.RequestKey
import com.xylophone.learning.music.databinding.ActivityPermissionBinding
import com.xylophone.learning.music.activity_app.main.MainActivity
import com.xylophone.learning.music.core.extensions.setGradientTextHeightColor
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import kotlinx.coroutines.launch
import kotlin.jvm.java

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    private var inter: InterstitialAd? = null


    override fun setViewBinding() = ActivityPermissionBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.btnStorage.visible()
            binding.btnNotification.gone()
        } else {
            binding.btnNotification.visible()
            binding.btnStorage.gone()
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
        setGradientTextHeightColor(
            binding.tvContinue,
            "#80F4FF".toColorInt(),
            "#80F4FF".toColorInt()
        )
        val textRes =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.string.to_access_13 else R.string.to_access

        binding.txtPer.text = TextUtils.concat(
            createColoredText(R.string.allow, R.color.white),
            " ",
            createColoredText(R.string.app_name, R.color.white),
            " ",
            createColoredText(textRes, R.color.white)
        )
    }

    override fun viewListener() {
        binding.swPermission.setOnSingleClick { handlePermissionRequest(isStorage = true) }
        binding.swNotification.setOnSingleClick { handlePermissionRequest(isStorage = false) }
        binding.tvContinue.setOnSingleClick(1500) { handleContinue() }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.storageGranted.collect { granted ->
                    updatePermissionUI(granted, true)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationGranted.collect { granted ->
                    updatePermissionUI(granted, false)
                }
            }
        }
    }

    private fun handlePermissionRequest(isStorage: Boolean) {
        val perms =
            if (isStorage) viewModel.getStoragePermissions() else viewModel.getNotificationPermissions()
        if (checkPermissions(perms)) {
            showToast(if (isStorage) R.string.granted_storage else R.string.granted_notification)
        } else if (viewModel.needGoToSettings(sharePreference, isStorage)) {
            goToSettings()
        } else {
            val requestCode =
                if (isStorage) RequestKey.STORAGE_PERMISSION_CODE else RequestKey.NOTIFICATION_PERMISSION_CODE
            requestPermission(perms, requestCode)
        }
    }

    private fun updatePermissionUI(granted: Boolean, isStorage: Boolean) {
        val imageView = if (isStorage) binding.swPermission else binding.swNotification
        imageView.setImageResource(if (granted) R.drawable.ic_sw_on else R.drawable.ic_sw_off)

        // Thay đổi kích thước trực tiếp (đơn vị dp)
        val params = imageView.layoutParams
        val density = resources.displayMetrics.density
        if (granted) {
            params.width = (40 * density).toInt()  // 60dp
            params.height = (24 * density).toInt() // 30dp
        } else {
            params.width = (40 * density).toInt()  // 50dp
            params.height = (24 * density).toInt() // 25dp
        }
        imageView.layoutParams = params
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted =
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> viewModel.updateStorageGranted(
                sharePreference,
                granted
            )

            RequestKey.NOTIFICATION_PERMISSION_CODE -> viewModel.updateNotificationGranted(
                sharePreference,
                granted
            )
        }
        if (granted) {
            showToast(if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) R.string.granted_storage else R.string.granted_notification)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateStorageGranted(
            sharePreference, checkPermissions(viewModel.getStoragePermissions())
        )
        viewModel.updateNotificationGranted(
            sharePreference, checkPermissions(viewModel.getNotificationPermissions())
        )
    }


    override fun initActionBar() {
        binding.actionBar.tvCenter.apply {
            text = getString(R.string.permission)
            visible()
        }
    }


    fun Activity.startIntentRightToLeft(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        val option =
            ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        startActivity(intent, option.toBundle())
    }

    override fun initAds() {
        Admob.getInstance().loadInterAds(
            this@PermissionActivity, getString(R.string.inter_per), object : InterCallback() {
                override fun onAdLoadSuccess(interstitialAd: InterstitialAd?) {
                    super.onAdLoadSuccess(interstitialAd)
                    inter = interstitialAd
                }
            })

        Admob.getInstance().loadNativeAd(
            this@PermissionActivity,
            getString(R.string.native_per),
            binding.nativeAds,
            R.layout.ads_native_big_btn_top
        )
    }

    private fun createColoredText(
        @androidx.annotation.StringRes textRes: Int,
        @androidx.annotation.ColorRes colorRes: Int,
        font: Int = R.font.chewy_regular
    ) = StringHelper.changeColor(this, getString(textRes), colorRes, font)

    private fun handleContinue() {
        Admob.getInstance().showInterAds(this@PermissionActivity, inter, object : InterCallback() {

            override fun onNextAction() {
                super.onNextAction()
                sharePreference.setIsFirstPermission(false)
                startIntentRightToLeft(MainActivity::class.java)
                finishAffinity()
            }
        }
        )

    }
}