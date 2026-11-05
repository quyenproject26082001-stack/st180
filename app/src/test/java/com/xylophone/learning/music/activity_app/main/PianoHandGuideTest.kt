package com.xylophone.learning.music.activity_app.main

import org.junit.Assert.*
import org.junit.Test

class PianoHandGuideTest {
    @Test fun unassignedPassageDoesNotPointAtALaterNote() {
        val lesson=PianoLesson(listOf(PianoLesson.Note(41,0.0,400.0,1),
            note(48,400.0,800.0,2)),true,1.0,false)
        assertTrue(PianoHandGuide.targets(lesson,1).isEmpty())
        lesson.seek(0.0)
        assertTrue(PianoHandGuide.targets(lesson,1).isEmpty())
        lesson.seek(400.0)
        assertEquals(listOf(1),PianoHandGuide.targets(lesson,1))
    }
    private val low=listOf(53,48,46,45,41)
    private val high=listOf(60,57,53,48,45)
    private fun note(midi:Int, start:Double, end:Double, finger:Int, pose:List<Int> = low)=
        PianoLesson.Note(midi,start,end,1,finger,pose)

    @Test fun waitingAtAttackKeepsTheRequiredFingerWithoutScoring() {
        val lesson=PianoLesson(listOf(note(41,0.0,400.0,5),note(48,400.0,800.0,2)),true,1.0,false)
        lesson.seek(0.0)
        lesson.advance(2000.0)
        assertEquals(listOf(0),PianoHandGuide.targets(lesson,1))
        assertEquals(0,lesson.hits)
        assertEquals(0.0,lesson.position,0.0)
        assertTrue(PianoHandGuide.targets(lesson,0).isEmpty())
    }

    @Test fun seekAndLoopExcludeSkippedNotesAndPreviewNewPosition() {
        val lesson=PianoLesson(listOf(note(41,0.0,400.0,5),note(57,1200.0,1600.0,2,high)),true,1.0,false)
        lesson.seek(800.0,1600.0)
        assertEquals(listOf(1),PianoHandGuide.targets(lesson,1))
        lesson.seek(0.0,400.0)
        assertEquals(listOf(0),PianoHandGuide.targets(lesson,1))
    }

    @Test fun simultaneousFinalChordShowsAllThreeFingersThenDisappears() {
        val lesson=PianoLesson(listOf(note(41,0.0,1000.0,5),note(48,0.0,1000.0,2),note(53,0.0,1000.0,1)),false,1.0,true)
        lesson.seek(0.0)
        lesson.advance(10.0)
        assertEquals(listOf(0,1,2),PianoHandGuide.targets(lesson,1))
        lesson.advance(1000.0)
        assertTrue(PianoHandGuide.targets(lesson,1).isEmpty())
    }

    @Test fun otherSongsAndInvalidFingeringNeverProduceInventedHands() {
        val lesson=PianoLesson(listOf(PianoLesson.Note(60,0.0,1000.0,0),note(41,0.0,1000.0,1)),true,1.0,false)
        lesson.seek(0.0)
        assertTrue(PianoHandGuide.targets(lesson,0).isEmpty())
        assertTrue(PianoHandGuide.targets(lesson,1).isEmpty())
    }
}
