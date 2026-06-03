package com.xylophone.learning.music.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.core.graphics.createBitmap
import com.xylophone.learning.music.R
import com.xylophone.learning.music.activity_app.main.MainActivity
import com.xylophone.learning.music.core.helper.RateHelper
import com.xylophone.learning.music.core.helper.SharePreferenceHelper
import com.xylophone.learning.music.core.helper.SoundHelper
import com.xylophone.learning.music.core.utils.DataLocal
import com.xylophone.learning.music.core.utils.state.RateState

// ----------------------------
// Visibility extensions
// ----------------------------
fun View.visible() { visibility = View.VISIBLE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.select() { isSelected = true }

// ----------------------------
// Toast extensions
// ----------------------------
fun Activity.showToast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, ContextCompat.getString(this, message), duration).show()
}
fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ----------------------------
// Navigation / Transition
// ----------------------------
fun Activity.handleBackLeftToRight() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}
fun Activity.handleBackRightToLeft() {
    finish()
    overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
}
fun Context.handleBackFragmentFromRight() {
    if (this is FragmentActivity) {
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        supportFragmentManager.popBackStack()
    }
}


fun Activity.goHome() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish() // optional: để chắc chắn đóng màn hiện tại
}


fun Activity.hideNavigation(isBlack: Boolean = true) {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = if (isBlack) {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    } else {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

fun Activity.hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
// ----------------------------
// Click utils
// ----------------------------
fun View.setOnSingleClick(interval: Long = 200, action: (View) -> Unit) {
    setOnClickListener {
        if (System.currentTimeMillis() - DataLocal.lastClickTime >= interval) {
            action(it)
            DataLocal.lastClickTime = System.currentTimeMillis()
        }
    }
}
fun View.setOnSingleClickWithSound(interval: Long = 500, action: (View) -> Unit) {
    setOnClickListener {
        if (System.currentTimeMillis() - DataLocal.lastClickTime >= interval) {
            SoundHelper.playSound(R.raw.touch)
            action(it)
            DataLocal.lastClickTime = System.currentTimeMillis()
        }
    }
}

// ----------------------------
// UI Capture
// ----------------------------
@Throws(OutOfMemoryError::class)
fun createBitmapFromView(view: View): Bitmap {
    return try {
        val output = createBitmap(view.width, view.height)
        val canvas = Canvas(output)
        view.draw(canvas)
        output
    } catch (error: OutOfMemoryError) {
        throw error
    }
}

// ----------------------------
// TextView
// ----------------------------
fun TextView.setFont(@FontRes resId: Int) {
    typeface = ResourcesCompat.getFont(context, resId)
}

fun TextView.setTextContent(context: Context, resId: Int) {
    text = context.getString(resId)
}

fun Context.strings(resId: Int) : String {
    return getString(resId)
}
