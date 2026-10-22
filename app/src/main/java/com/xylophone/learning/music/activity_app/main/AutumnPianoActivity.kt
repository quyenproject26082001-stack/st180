package com.xylophone.learning.music.activity_app.main

import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.xylophone.learning.music.R
import com.xylophone.learning.music.core.helper.SoundHelper
import com.xylophone.learning.music.databinding.ActivityAutumnPianoBinding
import java.io.File

/** A self-contained chord practice flow for the Trí Nhật piano cover. */
class AutumnPianoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAutumnPianoBinding
    private val handler = Handler(Looper.getMainLooper())
    private var measureIndex = 0
    private var isPlaying = false
    private var arrangementPlayer: MediaPlayer? = null
    private val noteButtons by lazy {
        listOf(binding.keyC, binding.keyD, binding.keyE, binding.keyF,
            binding.keyG, binding.keyA, binding.keyB, binding.keyC4)
    }
    private val noteSounds = listOf(
        R.raw.samples_piano_c3, R.raw.samples_piano_d3, R.raw.samples_piano_e3,
        R.raw.samples_piano_f3, R.raw.samples_piano_g3, R.raw.samples_piano_a3,
        R.raw.samples_piano_b3, R.raw.samples_piano_c4
    )

    private data class Measure(val section: String, val chord: String, val notes: List<Int>)

    // Indexes follow the C3-C4 keyboard below. Each measure is one four-beat arpeggio.
    private val measures = listOf(
        Measure("Intro", "C", listOf(0, 2, 4, 7)),
        Measure("Intro", "G/B", listOf(6, 1, 4, 6)),
        Measure("Intro", "Am", listOf(5, 0, 2, 5)),
        Measure("Intro", "F", listOf(3, 5, 0, 3)),
        Measure("Intro", "C", listOf(0, 2, 4, 7)),
        Measure("Intro", "G/B", listOf(6, 1, 4, 6)),
        Measure("Intro", "Am", listOf(5, 0, 2, 5)),
        Measure("Intro", "G", listOf(4, 6, 1, 4)),
        Measure("Verse", "C", listOf(0, 2, 4, 7)),
        Measure("Verse", "Em", listOf(2, 4, 6, 2)),
        Measure("Verse", "Am", listOf(5, 0, 2, 5)),
        Measure("Verse", "Em", listOf(2, 4, 6, 2)),
        Measure("Verse", "F", listOf(3, 5, 0, 3)),
        Measure("Verse", "Em", listOf(2, 4, 6, 2)),
        Measure("Verse", "Dm", listOf(1, 3, 5, 1)),
        Measure("Verse", "G", listOf(4, 6, 1, 4)),
        Measure("Chorus", "C", listOf(0, 2, 4, 7)),
        Measure("Chorus", "Em", listOf(2, 4, 6, 2)),
        Measure("Chorus", "Am", listOf(5, 0, 2, 5)),
        Measure("Chorus", "Em", listOf(2, 4, 6, 2)),
        Measure("Chorus", "F", listOf(3, 5, 0, 3)),
        Measure("Chorus", "C", listOf(0, 2, 4, 7)),
        Measure("Chorus", "Dm", listOf(1, 3, 5, 1)),
        Measure("Chorus", "G", listOf(4, 6, 1, 4)),
        Measure("Chorus", "C", listOf(0, 2, 4, 7)),
        Measure("Chorus", "Em", listOf(2, 4, 6, 2)),
        Measure("Chorus", "Am", listOf(5, 0, 2, 5)),
        Measure("Chorus", "Em", listOf(2, 4, 6, 2)),
        Measure("Chorus", "F", listOf(3, 5, 0, 3)),
        Measure("Chorus", "C", listOf(0, 2, 4, 7)),
        Measure("Chorus", "Dm", listOf(1, 3, 5, 1)),
        Measure("Chorus", "G", listOf(4, 6, 1, 4))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutumnPianoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noteSounds.forEach { if (!SoundHelper.isSoundNotNull(it)) SoundHelper.loadSound(this, it) }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSheet.setOnClickListener { openPracticeSheet() }
        binding.btnReferenceSheet.setOnClickListener { openUrl(SHEET_URL) }
        binding.btnVideo.setOnClickListener { openUrl(VIDEO_URL) }
        binding.btnPrevious.setOnClickListener { stopPlayback(); showMeasure((measureIndex - 1 + measures.size) % measures.size) }
        binding.btnNext.setOnClickListener { stopPlayback(); showMeasure((measureIndex + 1) % measures.size) }
        binding.btnPlayPattern.setOnClickListener {
            if (isPlaying) stopPlayback() else startPlayback()
        }
        noteButtons.forEachIndexed { index, button ->
            button.setOnClickListener { SoundHelper.playSound(noteSounds[index]); markNote(index) }
        }
        showMeasure(savedInstanceState?.getInt(STATE_MEASURE) ?: 0)
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun openPracticeSheet() {
        val file = File(cacheDir, "lang_quen_chieu_thu_practice.pdf")
        assets.open(file.name).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.autumn_no_pdf_viewer, Toast.LENGTH_LONG).show()
        }
    }

    private fun showMeasure(index: Int) {
        measureIndex = index
        val measure = measures[index]
        binding.tvSection.text = getString(R.string.autumn_section_progress,
            measure.section, index + 1, measures.size)
        binding.tvChord.text = measure.chord
        binding.tvNotes.text = measure.notes.joinToString("  •  ") {
            listOf("C3", "D3", "E3", "F3", "G3", "A3", "B3", "C4")[it]
        }
        markNote(-1)
    }

    private fun markNote(index: Int) {
        val chordNotes = measures[measureIndex].notes.toSet()
        noteButtons.forEachIndexed { i, button ->
            val color = when {
                i == index -> R.color.autumn_key_active
                i in chordNotes -> R.color.autumn_key_hint
                else -> android.R.color.white
            }
            button.setBackgroundColor(ContextCompat.getColor(this, color))
        }
    }

    private fun startPlayback() {
        val player = MediaPlayer()
        try {
            val file = File(cacheDir, ARRANGEMENT_FILE)
            if (!file.exists()) {
                assets.open(ARRANGEMENT_FILE).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.seekTo((measureIndex * BAR_MS).toInt())
            player.setOnCompletionListener { stopPlayback() }
            arrangementPlayer = player
            isPlaying = true
            binding.btnPlayPattern.setText(R.string.autumn_stop)
            player.start()
            followPlayback()
        } catch (_: Exception) {
            player.release()
            arrangementPlayer = null
            isPlaying = false
            Toast.makeText(this, R.string.autumn_audio_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun followPlayback() {
        if (!isPlaying) return
        val position = arrangementPlayer?.currentPosition ?: return
        val bar = (position / BAR_MS).toInt().coerceIn(measures.indices)
        if (bar != measureIndex) showMeasure(bar)
        handler.postDelayed({ followPlayback() }, 100L)
    }

    private fun stopPlayback() {
        isPlaying = false
        handler.removeCallbacksAndMessages(null)
        arrangementPlayer?.release()
        arrangementPlayer = null
        if (::binding.isInitialized) {
            binding.btnPlayPattern.setText(R.string.autumn_play_pattern)
            markNote(-1)
        }
    }

    override fun onStop() {
        stopPlayback()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_MEASURE, measureIndex)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_MEASURE = "measure"
        private const val BAR_MS = 2637L // Four beats at 91 BPM
        private const val ARRANGEMENT_FILE = "lang_quen_chieu_thu_arrangement.ogg"
        private const val VIDEO_URL = "https://www.youtube.com/watch?v=rQRMNIoT-KE"
        private const val SHEET_URL = "https://truongca.com/nhac/34354-lang-quen-chieu-thu-piano"
    }
}
