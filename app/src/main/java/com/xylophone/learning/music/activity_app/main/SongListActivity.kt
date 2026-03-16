package com.xylophone.learning.music.activity_app.main

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lvt.ads.util.Admob
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.base.BaseActivity
import com.xylophone.learning.music.core.custom.text.DoubleStrokeTextView
import com.xylophone.learning.music.core.extensions.gone
import com.xylophone.learning.music.core.extensions.handleBackLeftToRight
import com.xylophone.learning.music.core.extensions.invisible
import com.xylophone.learning.music.core.extensions.setOnSingleClick
import com.xylophone.learning.music.core.extensions.showInterAll
import com.xylophone.learning.music.core.extensions.visible
import com.xylophone.learning.music.core.helper.RecordingManager
import com.xylophone.learning.music.data.model.Recording
import com.xylophone.learning.music.data.model.Song
import com.xylophone.learning.music.data.model.SongLibrary
import com.xylophone.learning.music.databinding.ActivitySongListBinding

class SongListActivity : BaseActivity<ActivitySongListBinding>() {


    private fun dp(dp: Float): Float = dp * resources.displayMetrics.density

    private var selectedSong: Song? = null
    private var selectedRecording: Recording? =null
    private lateinit var songAdapter: SongAdapter

    private lateinit var recordingAdapter: RecordingAdapter

    private lateinit var preferenceHelper: com.xylophone.learning.music.core.helper.SharePreferenceHelper

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
       // Toast.makeText(this, "SongListActivity onCreate called", Toast.LENGTH_SHORT).show()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //Toast.makeText(this, "SongListActivity onResume", Toast.LENGTH_SHORT).show()

        // Check nếu có flag clear_focus (từ Home/normal mode) → clear
        val shouldClearFocus = intent.getBooleanExtra("clear_focus", false)
        if (shouldClearFocus) {
            preferenceHelper.clearLastPlayedSongId()
            // Remove flag để lần sau không clear nữa
            intent.removeExtra("clear_focus")
        }

        // Focus lại song đã chơi gần đây (nếu chưa bị clear)
        focusLastPlayedSong()
    }

    override fun onPause() {
        super.onPause()
        //Toast.makeText(this, "SongListActivity onPause", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        //Toast.makeText(this, "SongListActivity onDestroy", Toast.LENGTH_SHORT).show()
    }

    override fun setViewBinding(): ActivitySongListBinding {
        return ActivitySongListBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
       // Toast.makeText(this, "SongListActivity starting", Toast.LENGTH_SHORT).show()
        try {
            // Initialize preference helper
            preferenceHelper = com.xylophone.learning.music.core.helper.SharePreferenceHelper(this)

            setupRecyclerView()
            loadSongs()
            setupFilters()

            //Toast.makeText(this, "SongListActivity initialized", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error in initView: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onSongClick = { song ->
                selectedSong = song
                if(song !=null){
                    binding.actionBar.btnActionBarRight.visible()
                }else{
                    binding.actionBar.btnActionBarRight.invisible()
                }
            }
        )

        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@SongListActivity)
            adapter = songAdapter
        }
    }

    private fun setupRecordingRecyclerView(){
        recordingAdapter = RecordingAdapter(recordings = mutableListOf(),
            onItemClick = {recording ->
                selectedRecording = recording
                if(recording !=null){
                    binding.actionBar.btnActionBarRight.visible()
                }else{
                    binding.actionBar.btnActionBarRight.gone()
                }

            }
        )

        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@SongListActivity)
            adapter = recordingAdapter
        }

    }

    private fun loadRecordings(){
        val recordings = RecordingManager.getAllRecordings(this)

        if(recordings.isEmpty()) {
            binding.layoutNoItem.visible()
            binding.recyclerViewSongs.isVisible = false
        }else{
            binding.layoutNoItem.gone()
            binding.recyclerViewSongs.isVisible = true
            recordingAdapter.updateRecordings(recordings.toMutableList())
        }

    }

    private fun loadSongs() {
        val songs = SongLibrary.getallSongs()

        if (songs.isEmpty()) {
            binding.recyclerViewSongs.isVisible = false
        } else {
            binding.layoutNoItem.gone()
            binding.recyclerViewSongs.isVisible = true
            songAdapter.updateSongs(songs)
        }
    }


    private fun setTopMargin(view: android.view.View, topDp: Int) {
        val lp = view.layoutParams as? LinearLayout.LayoutParams ?: return
        lp.topMargin = (topDp * resources.displayMetrics.density).toInt()
        view.layoutParams = lp
    }


    private fun setupFilters() {
        binding.apply {
//            allSong.setOnSingleClick {
//                selecTab(true)
//            }
//
//            myRecord.setOnSingleClick {
//                selecTab(false)
//            }

        }
        selecTab(true)
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick {
               showInterAll {   handleBackLeftToRight() }
            }
            actionBar.btnActionBarRight.setOnSingleClick {
                if(binding.recyclerViewSongs.adapter == songAdapter){
                selectedSong?.let { song ->
                    val intent = Intent(this@SongListActivity, PlayActivity::class.java)
                    intent.putExtra("mode", "LEARNING_MODE")
                    intent.putExtra("song_id", song.id)
                   showInterAll {   startActivity(intent) }
                }
                }
                else{
                    selectedRecording?.let{ recording ->
                        val intent =Intent(this@SongListActivity, PlayActivity::class.java)
                        intent.putExtra("mode","PLAYBACK_MODE")
                        intent.putExtra("recording_id",recording.id)
                        startActivity(intent)

                    }
                }
            }
        }
    }

    override fun initText() {
        // Text already set in layout
    }

    private fun selecTab(isSongTab: Boolean){

        binding.actionBar.btnActionBarRight.invisible()

//        if(isSongTab) {
//            binding.tabMode.setBackgroundResource(R.drawable.tab_selected_song)
//            applySelectedTab(binding.allSong)
//            applyUnselectedTab(binding.myRecord)
//            binding.recyclerViewSongs.adapter = songAdapter
//            loadSongs()
//        }else{
//            binding.tabMode.setBackgroundResource(R.drawable.tab_selected_record)
//            applySelectedTab(binding.myRecord)
//            applyUnselectedTab(binding.allSong)
//            setupRecordingRecyclerView()
//            loadRecordings()
//
//        }
    }

    private fun applySelectedTab(tab: DoubleStrokeTextView) {
        // text style
        tab.setTextColor(getColor(R.color.white))
        tab.textSize = 24f

        // weight
        (tab.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.weight = 1.3f
            tab.layoutParams = it
        }

        setTopMargin(tab,0)

        // stroke
        tab.setDoubleStroke(
            outerColor = Color.parseColor("#E47E17"),
            outerWidth = dp(4f),                  // 4dp
            innerColor = Color.TRANSPARENT,
            innerWidth = 0f,
            join = Paint.Join.ROUND,
            miter = 10f
        )

        tab.requestLayout()
    }

    private fun applyUnselectedTab(tab: DoubleStrokeTextView) {

        tab.setTextColor(Color.parseColor("#808080"))
        tab.textSize = 16f

        (tab.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.weight = 0.8f
            tab.layoutParams = it
        }

        setTopMargin(tab, 6) // unselected: marginTop 6dp như XML


        tab.setDoubleStroke(
            outerColor = Color.TRANSPARENT,
            outerWidth = 0f,
            innerColor = Color.TRANSPARENT,
            innerWidth = 0f,
            join = Paint.Join.ROUND,
            miter = 10f
        )

        tab.requestLayout()
    }


    override fun initActionBar() {
        binding.actionBar.apply {
            tvCenter.text = getString(R.string.choose_sound)
            tvCenter.visible()
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
            btnActionBarRightText.gone()
            btnActionBarRight.setImageResource(R.drawable.ic_save)

        }
    }

    // Focus lại song đã chơi gần đây
    private fun focusLastPlayedSong() {
        // Chỉ focus khi đang hiển thị song list (không phải recording list)
        if (binding.recyclerViewSongs.adapter != songAdapter) return

        val lastPlayedSongId = preferenceHelper.getLastPlayedSongId() ?: return

        // Select song trong adapter
        val position = songAdapter.selectSongById(lastPlayedSongId)

        // Scroll tới vị trí đó
        if (position != RecyclerView.NO_POSITION) {
            binding.recyclerViewSongs.post {
                binding.recyclerViewSongs.smoothScrollToPosition(position)
            }
        }
    }
    override fun initAds() {
        initNativeCollab()
        Admob.getInstance().loadNativeAd(
            this,
            getString(R.string.native_chooseSound),
            binding.nativeAds,
            R.layout.ads_native_banner
        )
    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_chooseSound), binding.flNativeCollab)
    }
    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

}
