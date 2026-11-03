package com.xylophone.learning.music.activity_app.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AutumnPhotoSheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(Button(this).apply { text = "‹ Trở về bài học"; setOnClickListener { finish() } })
        listOf("9192", "9193", "8648").forEachIndexed { index, number ->
            content.addView(TextView(this).apply { text = "${index + 1}/3 • IMG_$number • ${if (index == 2) "Đoạn kết có luyến" else "Sheet giai điệu"}" })
            val bitmap = assets.open("autumn_photo/IMG_$number.jpg").use { BitmapFactory.decodeStream(it) }
            content.addView(ImageView(this).apply {
                adjustViewBounds = true
                contentDescription = "Sheet Lãng Quên Chiều Thu, ảnh $number"
                setImageBitmap(bitmap)
            }, LinearLayout.LayoutParams(-1, -2))
        }
        setContentView(ScrollView(this).apply { addView(content) })
    }
}
