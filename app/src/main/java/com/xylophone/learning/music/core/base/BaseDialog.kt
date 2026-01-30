package com.xylophone.learning.music.core.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.xylophone.learning.music.core.helper.LanguageHelper
import androidx.core.graphics.drawable.toDrawable

abstract class BaseDialog<VB : ViewBinding>(
    context: Context,
    private val gravity: Int = Gravity.CENTER,
    private val maxWidth: Boolean = false,
    private val maxHeight: Boolean = false
) : Dialog(context) {

    lateinit var binding: VB
    abstract val layoutId: Int
    abstract val isCancelOnTouchOutside: Boolean
    abstract val isCancelableByBack: Boolean

    // Wrapped context với locale đúng để sử dụng cho getString()
    protected val localizedContext: Context by lazy {
        LanguageHelper.wrapContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Sử dụng localizedContext để inflate layout với locale đúng
        binding = DataBindingUtil.inflate(LayoutInflater.from(localizedContext), layoutId, null, false)
        setContentView(binding.root)

        setCancelable(isCancelableByBack)
        setCanceledOnTouchOutside(isCancelOnTouchOutside)

        setupWindow()
        initView()
        initAction()
    }

    private fun setupWindow() {
        window?.apply {
            setGravity(gravity)

            val width = if (maxWidth) WindowManager.LayoutParams.MATCH_PARENT
            else WindowManager.LayoutParams.WRAP_CONTENT
            val height = if (maxHeight) WindowManager.LayoutParams.MATCH_PARENT
            else WindowManager.LayoutParams.WRAP_CONTENT
            setLayout(width, height)

            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener()
    }

    abstract fun initView()
    abstract fun initAction()
    abstract fun onDismissListener()
}
