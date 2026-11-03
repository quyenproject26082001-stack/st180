package com.xylophone.learning.music.core.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundHelper {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)  // Tăng từ 5 lên 10 để hỗ trợ multi-touch tốt hơn
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundMap = mutableMapOf<Int, Int>()

    fun isSoundNotNull(resId: Int) : Boolean {
        return soundMap[resId] != null
    }

    fun loadSound(context: Context, resId: Int) {
        if (soundMap[resId] == null) {
            soundMap[resId] = soundPool.load(context, resId, 1)
        }
    }

    fun playSound(resId: Int, volume: Float = 1f) {
        soundMap[resId]?.let { id ->
            // Clamp volume giữa 0 và 1
            val normalizedVolume = volume.coerceIn(0f, 1f)
            soundPool.play(id, normalizedVolume, normalizedVolume, 0, 0, 1f)
        }
    }

    fun playSound(resId: Int, volume: Float, playbackRate: Float) {
        soundMap[resId]?.let { id ->
            val v = volume.coerceIn(0f, 1f)
            soundPool.play(id, v, v, 1, 0, playbackRate.coerceIn(.5f, 2f))
        }
    }

    fun release() {
        soundPool.release()
    }

    fun releaseAllSounds() {
        soundMap.forEach { (_, soundId) ->
            soundPool.unload(soundId)
        }
        soundMap.clear()
    }
}
