package com.xylophone.learning.music.activity_app.main

class MidiStreamParser(private val onMessage: (Int, Int, Int) -> Unit) {
    private var status = 0
    private var firstData = -1
    private var systemExclusive = false

    fun reset() {
        status = 0
        firstData = -1
        systemExclusive = false
    }

    fun receive(bytes: ByteArray, offset: Int = 0, count: Int = bytes.size - offset) {
        for (index in offset until offset + count) {
            val value = bytes[index].toInt() and 255
            if (value >= 248) {
                if (value == 255) { reset(); onMessage(value, 0, 0) }
                continue
            }
            if (value >= 128) {
                firstData = -1
                if (value >= 240) {
                    status = 0
                    systemExclusive = value == 240
                } else {
                    status = value
                    systemExclusive = false
                }
                continue
            }
            if (systemExclusive || status == 0) continue
            if (status and 240 in listOf(192, 208)) {
                onMessage(status, value, 0)
            } else if (firstData < 0) {
                firstData = value
            } else {
                onMessage(status, firstData, value)
                firstData = -1
            }
        }
    }
}
