package com.xylophone.learning.music.activity_app.main

/** The guide is advisory only: it never scores or synthesizes input. */
object PianoHandGuide {
    fun targets(model: PianoLesson, hand: Int): List<Int> {
        val candidates=model.notes.indices.filter {
            val n=model.notes[it]
            n.hand==hand && (model.results[it]==0 ||
                (model.demonstration && model.results[it]==4)) &&
                n.start<model.rangeEnd && n.end>model.position
        }
        val active=candidates.filter { model.notes[it].start<=model.position }
        fun valid(indices: List<Int>)=indices.all {
            val n=model.notes[it]
            n.finger in 1..5 && n.pose.size==5 && n.pose[n.finger-1]==n.midi
        }
        if(active.isNotEmpty()) return if(valid(active)) active else emptyList()
        val next=candidates.minOfOrNull { model.notes[it].start } ?: return emptyList()
        val upcoming=candidates.filter { model.notes[it].start==next }
        return if(valid(upcoming)) upcoming else emptyList()
    }
}
