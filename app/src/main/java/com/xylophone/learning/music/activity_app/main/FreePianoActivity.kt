package com.xylophone.learning.music.activity_app.main

import android.os.Bundle
import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xylophone.learning.music.R

/** Standalone instrument: no lesson, accompaniment, scoring or transport. */
class FreePianoActivity : AppCompatActivity() {
    private lateinit var keyboard: PlayablePianoKeyboard
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(24,29,38))
        }
        val controls=LinearLayout(this)
        controls.addView(Button(this).apply {
            setText(R.string.autumn_back); setOnClickListener { finish() }
        })
        val title=TextView(this).apply {
            setText(R.string.free_piano_home); setTextColor(Color.WHITE); textSize=20f
            gravity=android.view.Gravity.CENTER
        }
        controls.addView(title,LinearLayout.LayoutParams(0,-1,1f))
        keyboard=PlayablePianoKeyboard(this)
        controls.addView(Button(this).apply {
            setText(R.string.autumn_sustain_off)
            setOnClickListener {
                keyboard.sustain=!keyboard.sustain
                setText(if(keyboard.sustain) R.string.autumn_sustain_on else R.string.autumn_sustain_off)
            }
        })
        root.addView(controls)
        root.addView(TextView(this).apply {
            text="FluidR3 Grand Piano • F2–C6 • CC BY 3.0 US / Frank Wen (MIDI.js)"
            setTextColor(Color.LTGRAY); setPadding(16,8,16,8)
            setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(this@FreePianoActivity)
                    .setTitle("Grand piano")
                    .setMessage(assets.open("GRAND_PIANO_LICENSE.txt").bufferedReader().use { it.readText() })
                    .setPositiveButton(android.R.string.ok,null).show()
            }
        })
        root.addView(keyboard,LinearLayout.LayoutParams(-1,0,1f))
        keyboard.onNote={ title.text=getString(R.string.free_piano_home)+" • "+it }
        setContentView(root)
    }
    override fun onStop() { keyboard.stopAll(); super.onStop() }
}
