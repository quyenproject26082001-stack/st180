package com.xylophone.learning.music.activity_app.main

class MidiPianoState(
    private val onDown: (Int, Int) -> Unit,
    private val onUp: (Int) -> Unit,
    private val onPedal: (Boolean) -> Unit
) {
    private val keys = mutableSetOf<Pair<Int, Int>>()
    private val pedals = mutableSetOf<Int>()

    fun reset() {
        val notes = keys.map { it.second }.toSet()
        keys.clear()
        pedals.clear()
        onPedal(false)
        notes.forEach(onUp)
    }

    fun message(status: Int, data: Int, value: Int) {
        if (status == 255) { reset(); return }
        val channel = status and 15
        when (status and 240) {
            144 -> if (value > 0) {
                keys.add(channel to data)
                onDown(data, value)
            } else release(channel, data)
            128 -> release(channel, data)
            176 -> when (data) {
                64 -> {
                    if (value >= 64) pedals.add(channel) else pedals.remove(channel)
                    onPedal(pedals.isNotEmpty())
                }
                120, 123 -> {
                    pedals.remove(channel)
                    onPedal(pedals.isNotEmpty())
                    keys.filter { it.first == channel }.map { it.second }.forEach { release(channel, it) }
                }
                121 -> {
                    pedals.remove(channel)
                    onPedal(pedals.isNotEmpty())
                }
            }
        }
    }

    private fun release(channel: Int, midi: Int) {
        if (keys.remove(channel to midi) && keys.none { it.second == midi }) onUp(midi)
    }
}
