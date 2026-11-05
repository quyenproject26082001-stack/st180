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
    var rangeEnd = (notes.maxOfOrNull { it.end } ?: 0.0) + 500
        private set
    val targetCount get() = results.count { it != 3 }
    fun seek(start: Double, end: Double = (notes.maxOfOrNull { it.end } ?: 0.0) + 500) {
        require(start >= 0 && end > start)
        position = start
        rangeEnd = end
        hits = 0; misses = 0
        notes.forEachIndexed { i, note -> results[i] = if (note.start < start || note.start >= end) 3 else 0 }
    }
    val finished get() = position >= rangeEnd
    fun advance(elapsed: Double) {
        var next = position + elapsed * speed
        if (demonstration) {
            notes.indices.filter { results[it] == 0 && notes[it].start <= next }.forEach { results[it] = 4 }
        } else if (waitMode) {
            val pending = notes.indices.filter { results[it] == 0 }.minOfOrNull { notes[it].start }
            if (pending != null) next = minOf(next, pending)
        } else {
            notes.indices.filter { results[it] == 0 && next - notes[it].start > 180 * speed }.forEach {
                results[it] = 2; misses++
            }
        }
        position = minOf(next, rangeEnd)
    }
    fun autoAcceptHeld(held: Set<Int>): List<Int> {
        if (demonstration || !waitMode) return emptyList()
        val accepted=mutableListOf<Int>()
        notes.indices.filter { results[it] == 0 && notes[it].midi in held && notes[it].start <= position }.forEach {
            results[it]=1; hits++; accepted += notes[it].midi
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
                val reset = notes.indices.filter { notes[it].start == pendingStart && results[it] == 1 }
                reset.forEach { results[it] = 0 }
                hits -= reset.size
                return "Sai nÃ³t â€¢ ÄÃ¡nh láº¡i há»£p Ã¢m"
            }
        }
        val index = notes.indices.filter { results[it] == 0 && notes[it].midi == midi }
            .minByOrNull { abs(notes[it].start - position) } ?: return "Sai nốt"
        val delta = (position - notes[index].start) / speed
        if (abs(delta) > 180) return if (delta < 0) "Quá sớm" else "Quá muộn"
        results[index] = 1; hits++
        return if (waitMode || abs(delta) <= 70) "Đúng" else if (delta < 0) "Sớm" else "Muộn"
    }
}
