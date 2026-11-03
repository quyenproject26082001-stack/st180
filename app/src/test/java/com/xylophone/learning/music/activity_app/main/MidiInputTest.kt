package com.xylophone.learning.music.activity_app.main

import org.junit.Assert.*
import org.junit.Test

class MidiInputTest {
    private fun bytes(vararg values: Int) = values.map { it.toByte() }.toByteArray()

    @Test fun fragmentedRunningStatusAndRealtimePreserveMessages() {
        val messages = mutableListOf<List<Int>>()
        val parser = MidiStreamParser { status, data, value -> messages.add(listOf(status, data, value)) }
        parser.receive(bytes(0, 0x92, 60), 1, 2)
        parser.receive(bytes(0xF8, 100, 64, 90, 60, 0, 0x82))
        parser.receive(bytes(64, 30))
        assertEquals(listOf(listOf(0x92,60,100), listOf(0x92,64,90), listOf(0x92,60,0), listOf(0x82,64,30)), messages)
    }

    @Test fun systemExclusiveAndSystemCommonCancelRunningStatus() {
        val messages = mutableListOf<List<Int>>()
        val parser = MidiStreamParser { status, data, value -> messages.add(listOf(status,data,value)) }
        parser.receive(bytes(0x90,60,100,0xF0,1,2,0xF8,3,0xF7,64,100,0xF2,1,2,65,100,0x90,67,90))
        assertEquals(listOf(listOf(0x90,60,100),listOf(0x90,67,90)), messages)
    }

    @Test fun oneByteMessagesDoNotConsumeTheNextStatus() {
        val messages = mutableListOf<List<Int>>()
        val parser = MidiStreamParser { status, data, value -> messages.add(listOf(status,data,value)) }
        parser.receive(bytes(0xC0,1,2,0xD0,40,0xB0,64,127,0xFF,60,100))
        assertEquals(listOf(listOf(0xC0,1,0),listOf(0xC0,2,0),listOf(0xD0,40,0),listOf(0xB0,64,127),listOf(0xFF,0,0)), messages)
    }

    @Test fun channelsVelocityZeroPedalAndDisconnectReleaseKeys() {
        val presses = mutableListOf<Int>()
        val releases = mutableListOf<Int>()
        var pedal = false
        val state = MidiPianoState({ midi, _ -> presses.add(midi) }, releases::add, { pedal=it })
        state.message(0x90,60,100)
        state.message(0x91,60,80)
        state.message(0x90,60,0)
        assertTrue(releases.isEmpty())
        state.message(0xB1,64,127)
        assertTrue(pedal)
        state.message(0x81,60,20)
        assertEquals(listOf(60), releases)
        state.message(0x90,64,100)
        state.reset()
        assertFalse(pedal)
        assertEquals(listOf(60,64), releases)
        assertEquals(listOf(60,60,64), presses)
    }

    @Test fun allNotesOffIsLimitedToItsChannel() {
        val released = mutableListOf<Int>()
        val state = MidiPianoState({ _, _ -> }, released::add, {})
        state.message(0x90,60,100)
        state.message(0x91,64,100)
        state.message(0xB0,123,0)
        assertEquals(listOf(60), released)
        state.message(0xB1,120,0)
        assertEquals(listOf(60,64), released)
    }

    @Test fun realMidiPressesAdvanceWaitLessonButHoldingDoesNotRepeat() {
        val lesson = PianoLesson(listOf(PianoLesson.Note(60,0.0,400.0,0), PianoLesson.Note(60,500.0,900.0,0)),true,1.0)
        lesson.seek(0.0,1000.0)
        val state = MidiPianoState({ midi, _ -> lesson.press(midi) }, {}, {})
        val parser = MidiStreamParser(state::message)
        parser.receive(bytes(0x90,61,100))
        lesson.advance(1000.0)
        assertEquals(0.0,lesson.position,0.0)
        parser.receive(bytes(60,100))
        lesson.advance(1000.0)
        assertEquals(500.0,lesson.position,0.0)
        assertEquals(1,lesson.hits)
        parser.receive(bytes(60,0,60,100))
        lesson.advance(1000.0)
        assertTrue(lesson.finished)
        assertEquals(2,lesson.hits)
    }
}
