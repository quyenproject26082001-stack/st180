package com.xylophone.learning.music.activity_app.main

import java.util.Locale
import kotlin.math.ceil

data class MidiArrival(val callbackNanos: Long, val eventNanos: Long)

internal class MidiMetrics {
    private val queue = mutableListOf<Long>()
    private val source = mutableListOf<Long>()
    private val audio = mutableListOf<Long>()
    private val scoring = mutableListOf<Long>()

    fun record(
        arrival: MidiArrival,
        dispatchedAt: Long,
        audioRequestedAt: Long?,
        scoredAt: Long?,
    ): String? {
        queue.add((dispatchedAt - arrival.callbackNanos).coerceAtLeast(0))
        val eventAge = arrival.callbackNanos - arrival.eventNanos
        if (arrival.eventNanos > 0 && eventAge in 0..1_000_000_000L) source.add(eventAge)
        audioRequestedAt?.let { audio.add(it - arrival.callbackNanos) }
        scoredAt?.let { scoring.add(it - arrival.callbackNanos) }
        return if (queue.size >= 32) drain() else null
    }

    fun drain(): String? {
        if (queue.isEmpty()) return null
        val result = listOf(
            describe("main_queue", queue), describe("event_age", source),
            describe("audio_request", audio), describe("score", scoring),
        ).joinToString("  ")
        listOf(queue, source, audio, scoring).forEach { it.clear() }
        return result
    }

    private fun describe(label: String, values: List<Long>): String {
        if (values.isEmpty()) return "$label n=0"
        val sorted = values.sorted()
        fun percentile(fraction: Double): Double =
            sorted[ceil(sorted.size * fraction).toInt() - 1] / 1_000_000.0
        return String.format(
            Locale.ROOT, "%s n=%d p50=%.2fms p95=%.2fms",
            label, sorted.size, percentile(.5), percentile(.95),
        )
    }
}
