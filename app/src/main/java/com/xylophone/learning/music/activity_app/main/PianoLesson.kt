package com.xylophone.learning.music.activity_app.main

import kotlin.math.abs

/** Score time is independent of frame rate and freezes at unanswered attacks in wait mode. */
class PianoLesson(val notes: List<Note>, val waitMode: Boolean, val speed: Double,
                  val demonstration: Boolean = false,
                  val retryChordOnMistake: Boolean = false) {
    data class Note(val midi: Int, val start: Double, val end: Double, val hand: Int,
                    val finger: Int = 0, val pose: List<Int> = emptyList())
    val results = IntArray(notes.size) // 0 pending, 1 hit, 2 missed
    var position = -2500.0
        private set
    var hits = 0
        private set
    var misses = 0
        private set
    var wrongPresses = 0
        private set
    var earlyHits = 0
        private set
    var lateHits = 0
        private set
    var points = 0
        private set
    private val notePoints = IntArray(notes.size)
    var rangeEnd = (notes.maxOfOrNull { it.end } ?: 0.0) + 500
        private set
    val targetCount get() = results.count { it != 3 }
    val maxPoints get() = targetCount * 100
    val scorePercent: Int
        get() = if (maxPoints == 0) 0 else (points * 100 / maxPoints).coerceIn(0, 100)
    fun seek(start: Double, end: Double = (notes.maxOfOrNull { it.end } ?: 0.0) + 500) {
        require(start >= 0 && end > start)
        position = start
        rangeEnd = end
        hits = 0; misses = 0; wrongPresses = 0; earlyHits = 0; lateHits = 0
        points = 0
        notePoints.fill(0)
        notes.forEachIndexed { i, note -> results[i] = if (note.start < start || note.start >= end) 3 else 0 }
    }
    val finished get() = position >= rangeEnd
    fun advance(elapsed: Double) {
        var next = position + elapsed * speed
        for (index in notes.indices) {
            if (results[index] != 0) continue
            val start = notes[index].start
            when {
                demonstration -> if (start <= next) results[index] = 4
                waitMode -> next = minOf(next, start)
                next - start > 180 * speed -> {
                    results[index] = 2
                    misses++
                }
            }
        }
        position = minOf(next, rangeEnd)
    }
    fun autoAcceptHeld(held: Set<Int>): List<Int> {
        if (demonstration || !waitMode || held.isEmpty()) return emptyList()
        val accepted = mutableListOf<Int>()
        for (index in notes.indices) {
            val note = notes[index]
            if (results[index] != 0 || note.midi !in held || note.start > position) continue
            results[index] = 1
            hits++
            notePoints[index] = 100
            points += 100
            accepted += note.midi
        }
        return accepted
    }
    fun accepts(midi: Int): Boolean = !demonstration && notes.indices.any {
        results[it] == 0 && notes[it].midi == midi && abs(position - notes[it].start) <= 180 * speed
    }
    fun press(midi: Int): String {
        if (demonstration) return "Đang chơi mẫu"
        val pendingStart=if(waitMode) notes.indices.filter { results[it]==0 }.minOfOrNull { notes[it].start } else null
        if (waitMode && retryChordOnMistake && pendingStart != null) {
            val belongs = notes.indices.any { notes[it].start == pendingStart && notes[it].midi == midi }
            if (!belongs) {
                wrongPresses++
                val reset = notes.indices.filter { notes[it].start == pendingStart && results[it] == 1 }
                reset.forEach { results[it] = 0; points -= notePoints[it]; notePoints[it] = 0 }
                hits -= reset.size
                return "Sai nÃ³t â€¢ ÄÃ¡nh láº¡i há»£p Ã¢m"
            }
        }
        val index = notes.indices.filter { results[it] == 0 && notes[it].midi == midi }
            .minByOrNull { abs(notes[it].start - position) } ?: run {
                wrongPresses++
                return "Sai nốt"
            }
        val delta = (position - notes[index].start) / speed
        if (abs(delta) > 180) {
            wrongPresses++
            return if (delta < 0) "Quá sớm" else "Quá muộn"
        }
        results[index] = 1; hits++
        val timingPoints = when {
            abs(delta) <= 70 -> 100
            abs(delta) <= 130 -> 75
            else -> 50
        }
        notePoints[index] = timingPoints
        points += timingPoints
        if (delta < -70) earlyHits++
        if (delta > 70) lateHits++
        return if (waitMode || abs(delta) <= 70) "Đúng" else if (delta < 0) "Sớm" else "Muộn"
    }
}
