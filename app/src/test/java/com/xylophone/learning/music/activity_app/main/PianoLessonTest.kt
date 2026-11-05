package com.xylophone.learning.music.activity_app.main

import org.junit.Assert.*
import org.junit.Test

class PianoLessonTest {
    @Test fun retryChordResetsPartiallyPlayedChordOnWrongNote() {
        val lesson=PianoLesson(listOf(note(60),note(64),note(67,600.0)),true,1.0,false,true)
        lesson.seek(0.0)
        lesson.advance(0.0)
        lesson.press(60)
        lesson.press(61)
        assertEquals(0, lesson.hits)
        lesson.press(60); lesson.press(64); lesson.advance(1000.0)
        assertEquals(600.0, lesson.position, 0.0)
    }
    private fun note(m:Int,t:Double=0.0)=PianoLesson.Note(m,t,t+500,0)
    @Test fun waitModeRequiresEveryChordNote() {
        val lesson=PianoLesson(listOf(note(60),note(64),note(67,600.0)),true,1.0)
        lesson.advance(5000.0)
        assertEquals(0.0,lesson.position,0.0)
        assertEquals("Sai nốt",lesson.press(61))
        lesson.press(60); lesson.advance(500.0)
        assertEquals(0.0,lesson.position,0.0)
        lesson.press(64); lesson.advance(1000.0)
        assertEquals(600.0,lesson.position,0.0)
        assertEquals(2,lesson.hits)
    }
    @Test fun continuousModeMissesOnlyOnceAndFinishes() {
        val lesson=PianoLesson(listOf(note(60)),false,1.0)
        lesson.advance(2800.0); lesson.advance(2000.0)
        assertEquals(1,lesson.misses)
        assertTrue(lesson.finished)
    }
    @Test fun timingWindowUsesRealTimeAtSlowSpeed() {
        val lesson=PianoLesson(listOf(note(60),note(60,500.0)),false,.5)
        lesson.advance(4800.0) // -100 score ms = 200 real ms early
        assertEquals("Quá sớm",lesson.press(60))
        lesson.advance(120.0)
        assertEquals("Sớm",lesson.press(60))
        assertEquals(1,lesson.hits)
        assertEquals("Quá sớm",lesson.press(60))
    }
    @Test fun repeatedPitchNeedsAnotherPress() {
        val lesson=PianoLesson(listOf(note(60),note(60,500.0)),true,1.0)
        lesson.advance(2500.0); lesson.press(60); lesson.advance(900.0)
        assertEquals(500.0,lesson.position,0.0)
        assertEquals(1,lesson.hits)
        lesson.press(60); lesson.advance(2000.0)
        assertTrue(lesson.finished)
    }
    @Test fun seekSkipsEarlierNotesWithoutCountingMisses() {
        val lesson=PianoLesson(listOf(note(60),note(64,500.0)),true,1.0)
        lesson.seek(500.0,1000.0)
        lesson.advance(5000.0)
        assertEquals(500.0,lesson.position,0.0)
        assertEquals(1,lesson.targetCount)
        assertEquals(0,lesson.misses)
        assertEquals("Sai nốt",lesson.press(60))
        lesson.press(64); lesson.advance(500.0)
        assertTrue(lesson.finished)
    }
    @Test fun loopEndIsExclusiveAndRestartClearsResults() {
        val lesson=PianoLesson(listOf(note(60),note(64,500.0)),false,1.0)
        lesson.seek(0.0,500.0)
        lesson.press(60); lesson.advance(1000.0)
        assertTrue(lesson.finished)
        assertEquals(1,lesson.targetCount)
        assertEquals(0,lesson.misses)
        lesson.seek(0.0,500.0)
        assertEquals(0,lesson.hits)
        assertEquals(0,lesson.results[0])
        assertEquals(3,lesson.results[1])
    }
    @Test fun demonstrationPlaysThroughWaitModeWithoutScoring() {
        val lesson=PianoLesson(listOf(note(60),note(64,500.0)),true,1.0,true)
        lesson.seek(0.0,1000.0)
        lesson.advance(600.0)
        assertEquals(600.0,lesson.position,0.0)
        assertEquals(4,lesson.results[0])
        assertEquals(4,lesson.results[1])
        lesson.press(60)
        assertEquals(0,lesson.hits)
        assertEquals(0,lesson.misses)
        lesson.advance(500.0)
        assertTrue(lesson.finished)
    }
    @Test fun demonstrationRespectsSelectedRangeAndSpeed() {
        val lesson=PianoLesson(listOf(note(60),note(64,500.0),note(67,1000.0)),false,.5,true)
        lesson.seek(500.0,1000.0)
        lesson.advance(400.0)
        assertEquals(700.0,lesson.position,0.0)
        assertEquals(3,lesson.results[0])
        assertEquals(4,lesson.results[1])
        assertEquals(3,lesson.results[2])
        lesson.seek(500.0,1000.0)
        assertEquals(0,lesson.results[1])
    }
    @Test fun correctNoteFilterRejectsWrongEarlyAndAlreadyPlayedNotes() {
        val lesson=PianoLesson(listOf(note(60),note(64),note(67,500.0)),true,.5)
        lesson.seek(0.0,1000.0)
        assertTrue(lesson.accepts(60))
        assertTrue(lesson.accepts(64))
        assertFalse(lesson.accepts(61))
        assertFalse(lesson.accepts(67))
        assertEquals(0,lesson.hits)
        lesson.press(60)
        assertFalse(lesson.accepts(60))
        assertTrue(lesson.accepts(64))
        lesson.press(64); lesson.advance(1000.0)
        assertTrue(lesson.accepts(67))
    }
    @Test fun heldNoteAutomaticallyAcceptsRepeatedOccurrences() {
        val lesson=PianoLesson(listOf(note(60),note(60,500.0),note(64,1000.0)),true,.5)
        lesson.seek(0.0,1500.0)
        lesson.advance(0.0)
        assertEquals(listOf(60),lesson.autoAcceptHeld(setOf(60)))
        lesson.advance(1000.0)
        assertEquals(1,lesson.hits)
        assertEquals(listOf(60),lesson.autoAcceptHeld(setOf(60)))
        assertEquals(2,lesson.hits)
        assertEquals(emptyList<Int>(),lesson.autoAcceptHeld(setOf(64)))
    }
    @Test fun heldSupportDoesNotAutoAcceptInRhythmMode() {
        val lesson=PianoLesson(listOf(note(60)),false,1.0)
        lesson.seek(0.0,1000.0); lesson.advance(1000.0)
        assertEquals(emptyList<Int>(),lesson.autoAcceptHeld(setOf(60)))
        assertEquals(0,lesson.hits)
    }
}
