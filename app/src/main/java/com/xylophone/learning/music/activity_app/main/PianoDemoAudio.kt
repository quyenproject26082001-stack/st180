package com.xylophone.learning.music.activity_app.main

import android.media.SoundPool
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToLong

/** Plays the demo on the score clock, even when the display misses a frame. */
internal class PianoDemoAudio(
    private val pool: SoundPool,
    private val samples: Map<Int, Int>,
    private val notes: List<PianoLesson.Note>
) {
    private val thread = HandlerThread("PianoDemoAudio").apply { start() }
    private val handler = Handler(thread.looper)
    private val generation = AtomicInteger()
    private val streams = mutableSetOf<Int>()

    fun start(position: Double, rangeStart: Double, rangeEnd: Double, speed: Double, startUptime: Long) {
        val session = cancelPending()
        fun dueAt(scoreTime: Double) = startUptime + ((scoreTime - position) / speed).roundToLong()

        handler.post {
            if (session != generation.get()) return@post
            stopStreams()
            val first = notes.indexOfFirst { it.start >= position }.let { if (it < 0) notes.size else it }
            var next = first
            val attacks = object : Runnable {
                override fun run() {
                    if (session != generation.get()) return
                    val now = SystemClock.uptimeMillis()
                    while (next < notes.size) {
                        val note = notes[next]
                        if (note.start >= rangeEnd) break
                        val due = dueAt(note.start)
                        if (due > now) {
                            handler.postAtTime(this, due)
                            return
                        }
                        val release = dueAt(minOf(note.end, rangeEnd))
                        if (release > now - LATE_ATTACK_TOLERANCE_MS) play(note, release, session)
                        next++
                    }
                }
            }
            handler.postAtTime({
                if (session == generation.get()) {
                    // A pause can happen during a sustained note. A seek still excludes previous notes.
                    for (index in 0 until first) {
                        val note = notes[index]
                        if (note.start >= rangeStart && note.end > position) {
                            play(note, dueAt(minOf(note.end, rangeEnd)), session)
                        }
                    }
                    attacks.run()
                }
            }, startUptime)
        }
    }

    fun stop() {
        cancelPending()
        handler.post(::stopStreams)
    }

    fun close() {
        cancelPending()
        handler.post {
            stopStreams()
            pool.release()
            thread.quitSafely()
        }
    }

    private fun cancelPending(): Int {
        val session = generation.incrementAndGet()
        handler.removeCallbacksAndMessages(null)
        return session
    }

    private fun stopStreams() {
        streams.forEach(pool::stop)
        streams.clear()
    }

    private fun play(note: PianoLesson.Note, releaseAt: Long, session: Int) {
        val sample = samples[note.midi] ?: return
        val volume = if (note.hand == 0) .45f else .325f
        val stream = pool.play(sample, volume, volume, 0, 0, 1f)
        if (stream == 0) return
        streams.add(stream)
        val release = maxOf(releaseAt, SystemClock.uptimeMillis() + MIN_ATTACK_MS)
        for (step in 1..FADE_STEPS) {
            handler.postAtTime({
                if (session == generation.get() && stream in streams) {
                    if (step == FADE_STEPS) {
                        pool.stop(stream)
                        streams.remove(stream)
                    } else {
                        val remaining = 1f - step / FADE_STEPS.toFloat()
                        val level = volume * remaining * remaining
                        pool.setVolume(stream, level, level)
                    }
                }
            }, release + step * FADE_INTERVAL_MS)
        }
    }

    private companion object {
        const val FADE_STEPS = 10
        const val FADE_INTERVAL_MS = 20L
        const val MIN_ATTACK_MS = 20L
        const val LATE_ATTACK_TOLERANCE_MS = 35L
    }
}
