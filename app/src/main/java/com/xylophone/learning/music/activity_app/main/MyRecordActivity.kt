package com.xylophone.learning.music.activity_app.main

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.SwipeToDeleteCallback
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.visible
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.invisible
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.data.model.Recording
import com.xylophone.learning.music.core.helper.RecordingManager
import com.xylophone.learning.music.databinding.ActivityMyRecordBinding

class MyRecordActivity : BaseActivity<ActivityMyRecordBinding>() {

    private lateinit var recordingAdapter: RecordingAdapter
    private var swipeCallback: SwipeToDeleteCallback? = null
    private var selectedRecording: Recording? = null

    override fun setViewBinding(): ActivityMyRecordBinding {
        return ActivityMyRecordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupRecyclerView()
        setupSwipeToDelete()
        loadRecordings()
    }

    private fun setupSwipeToDelete() {
        swipeCallback = object : SwipeToDeleteCallback() {}

        val itemTouchHelper = ItemTouchHelper(swipeCallback!!)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewRecordings)

        binding.recyclerViewRecordings.addOnItemTouchListener(
            object : RecyclerView.SimpleOnItemTouchListener() {
                private var downX = 0f
                private var downY = 0f

                override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                    val swipedPos = swipeCallback?.getSwipedPosition() ?: RecyclerView.NO_POSITION

                    when (e.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            downX = e.x
                            downY = e.y
                            Log.d("MyRecordActivity", "ACTION_DOWN: swipedPos=$swipedPos")
                        }
                    }

                    // Only intercept if there's a swiped item
                    val shouldIntercept = swipedPos != RecyclerView.NO_POSITION
                    Log.d("MyRecordActivity", "onInterceptTouchEvent: action=${e.action}, swipedPos=$swipedPos, shouldIntercept=$shouldIntercept")
                    return shouldIntercept
                }

                override fun onTouchEvent(rv: RecyclerView, e: android.view.MotionEvent) {
                    when (e.action) {
                        android.view.MotionEvent.ACTION_UP -> {
                            Log.d("MyRecordActivity", "ACTION_UP at (${e.x}, ${e.y}), rawX=${e.rawX}, rawY=${e.rawY}")

                            val swipedPos = swipeCallback?.getSwipedPosition() ?: RecyclerView.NO_POSITION
                            if (swipedPos == RecyclerView.NO_POSITION) {
                                Log.d("MyRecordActivity", "No swiped position")
                                return
                            }

                            // Find the view holder at the swiped position
                            val viewHolder = rv.findViewHolderForAdapterPosition(swipedPos)
                            if (viewHolder == null) {
                                Log.d("MyRecordActivity", "ViewHolder not found for position $swipedPos")
                                return
                            }

                            val child = viewHolder.itemView
                            Log.d("MyRecordActivity", "Found swiped item at position $swipedPos")

                            // This item is swiped open, check if tapping delete button
                            val bounds = swipeCallback?.getDeleteButtonBounds()
                            Log.d("MyRecordActivity", "Bounds: $bounds")

                            if (bounds != null) {
                                // Get RecyclerView location on screen
                                val rvLoc = IntArray(2)
                                rv.getLocationOnScreen(rvLoc)

                                // Calculate tap coordinates relative to RecyclerView (bounds are in RV coords)
                                val tapXInRv = e.rawX.toInt() - rvLoc[0]
                                val tapYInRv = e.rawY.toInt() - rvLoc[1]

                                Log.d("MyRecordActivity", "RV location: (${rvLoc[0]}, ${rvLoc[1]})")
                                Log.d("MyRecordActivity", "Raw tap: (${e.rawX.toInt()}, ${e.rawY.toInt()})")
                                Log.d("MyRecordActivity", "Tap in RV coords: ($tapXInRv, $tapYInRv)")
                                Log.d("MyRecordActivity", "Bounds: $bounds")

                                if (bounds.contains(tapXInRv, tapYInRv)) {
                                    // Tap hit delete icon → delete the item immediately
                                    Log.d("MyRecordActivity", "DELETE ICON TAPPED!")
                                    val item = recordingAdapter.getItemAt(swipedPos)
                                    if (item != null) {
                                        RecordingManager.deleteRecording(this@MyRecordActivity, item.id)
                                        loadRecordings()
                                    }
                                } else {
                                    // Tap missed delete icon → close the swipe
                                    Log.d("MyRecordActivity", "Tap missed delete, closing swipe")
                                    swipeCallback?.closeSwipe(rv)
                                }
                            }
                        }
                    }
                }
            }
        )
    }


    private fun setupRecyclerView() {
        recordingAdapter = RecordingAdapter(
            recordings = mutableListOf(),
            onItemClick = { recording ->
                selectedRecording = recording
                if (recording != null) {
                    binding.actionBar.btnActionBarRight.visible()
                } else {
                    binding.actionBar.btnActionBarRight.invisible()
                }
            }
        )

        binding.recyclerViewRecordings.apply {
            layoutManager = LinearLayoutManager(this@MyRecordActivity)
            adapter = recordingAdapter
        }
    }

    private fun loadRecordings() {
        // Close any open swipe before reloading
        swipeCallback?.closeSwipe(binding.recyclerViewRecordings)

        // Reset selection and hide Done button
        selectedRecording = null
        binding.actionBar.btnActionBarRight.invisible()

        val recordings = RecordingManager.getAllRecordings(this)

        if (recordings.isEmpty()) {
            binding.layoutNoItem.isVisible = true
            binding.recyclerViewRecordings.isVisible = false
        } else {
            binding.layoutNoItem.isVisible = false
            binding.recyclerViewRecordings.isVisible = true
            recordingAdapter.updateRecordings(recordings.toMutableList())
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick {
               showInterAll {   handleBackLeftToRight()}
            }
            actionBar.btnActionBarRight.setOnSingleClick {
                selectedRecording?.let { recording ->
                    val intent = Intent(this@MyRecordActivity, PlayActivity::class.java)
                    intent.putExtra("mode", "PLAYBACK_MODE")
                    intent.putExtra("recording_id", recording.id)
                    showInterAll {  startActivity(intent)}
                }
            }
        }
    }

    override fun initText() {
        // Text already set in layout
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            tvCenter.text = getString(R.string.my_record_title)
            tvCenter.visible()
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_save)
            btnActionBarRight.invisible()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload recordings khi quay lại activity
        loadRecordings()
    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_myRecord), binding.flNativeCollab)
    }
    override fun initAds() {
        initNativeCollab()

    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }
}
