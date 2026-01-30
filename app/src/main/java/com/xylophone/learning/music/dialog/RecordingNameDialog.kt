package com.xylophone.learning.music.dialog

import android.app.Activity
import android.view.inputmethod.EditorInfo
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseDialog
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.hideNavigation
import com.xylophone.learning.music.core.extensions.select
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.strings
import com.xylophone.learning.music.core.helper.RecordingManager
import com.xylophone.learning.music.data.model.Instrument
import com.xylophone.learning.music.data.model.Recording
import androidx.core.content.ContextCompat

import com.xylophone.learning.music.databinding.DialogRecordingNameBinding

/**
 * Custom dialog để nhập tên cho recording sau khi ghi xong
 *
 * @param context Activity context
 * @param recording Recording object chứa thông tin recording (duration, instrument, notes)
 */
class RecordingNameDialog(
    val context: Activity,
    private val recording: Recording
) : BaseDialog<DialogRecordingNameBinding>(context, maxWidth = true, maxHeight = true) {

    override val layoutId: Int = R.layout.dialog_recording_name
    override val isCancelOnTouchOutside: Boolean = false  // Không cho dismiss khi tap ngoài
    override val isCancelableByBack: Boolean = false      // Không cho dismiss khi bấm back

    // Callbacks
    var onSaveClick: ((String) -> Unit) = {}     // Callback khi user bấm Save
    var onCancelClick: (() -> Unit) = {}         // Callback khi user bấm Cancel

    override fun initView() {
        setupRecordingInfo()
        setupButtons()
        setupEditText()
        binding.tvTitle.select()
        context.hideNavigation()
    }



    /**
     * Setup thông tin recording (duration, instrument)
     */
    private fun setupRecordingInfo() {
        binding.apply {
            // Don't set default text - require user to input
            etRecordingName.setText("")
        }
    }

    /**
     * Setup buttons (Cancel / Save)
     */
    private fun setupButtons() {
        binding.flBottom.apply {
            // Ẩn icons, chỉ hiển thị text

            // Set text cho buttons
            tvBottomLeft.text = localizedContext.strings(R.string.cancel)
            tvBottomRight.text = localizedContext.strings(R.string.save)
            tvBottomRight.select()
            tvBottomLeft.select()
        }
    }

    /**
     * Setup EditText behavior
     */
    private fun setupEditText() {
        binding.etRecordingName.apply {
            // Khi user bấm Done trên keyboard → chỉ ẩn bàn phím
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Ẩn bàn phím
                    val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    clearFocus()
                    true
                } else {
                    false
                }
            }

            // Clear error when user starts typing
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    error = null // Clear error when typing
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            // Request focus và show keyboard
            requestFocus()
        }
    }

    override fun initAction() {
        binding.apply {
            // Cancel button
            flBottom.btnBottomLeft.setOnSingleClick {
                onCancelClick.invoke()
                dismiss()
            }

            // Save button
            flBottom.btnBottomRight.setOnSingleClick {
                handleSave()
            }

            // Không cho dismiss khi tap vào background
            flOutSide.setOnSingleClick {
                // Do nothing - force user to choose Cancel or Save
            }
        }
    }

    /**
     * Xử lý logic Save:
     * 1. Validate tên không rỗng
     * 2. Check trùng tên
     * 3. Gọi callback với tên đã nhập
     * 4. Dismiss dialog
     */
    private fun handleSave() {
        val name = binding.etRecordingName.text.toString().trim()

        if (name.isEmpty()) {
            // Nếu tên rỗng → shake EditText và không cho save
            binding.etRecordingName.apply {
//                // Shake animation to indicate error
//                val shake = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake_bounce)
//                startAnimation(shake)

                // Show error hint
                error = localizedContext.strings(R.string.please_enter_name)
                requestFocus()
            }
            return
        }

        // Check trùng tên với recordings hiện có
        val existingRecordings = RecordingManager.getAllRecordings(context)
        val isDuplicate = existingRecordings.any { it.name.equals(name, ignoreCase = true) }

        if (isDuplicate) {
            // Nếu trùng tên → shake EditText và hiển thị error
            binding.etRecordingName.apply {
//                // Shake animation to indicate error
//                val shake = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake_bounce)
//                startAnimation(shake)

                // Show error hint
                error = localizedContext.strings(R.string.name_already_exists)
                requestFocus()
            }
            return
        }

        // Only save if name is not empty and not duplicate
        onSaveClick.invoke(name)
        dismiss()
    }

    /**
     * Format duration từ milliseconds sang mm:ss
     */
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDismissListener() {
        // Cleanup nếu cần
    }
}
