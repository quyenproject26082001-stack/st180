package com.xylophone.learning.music.data.model

import com.xylophone.learning.music.R

enum class Instrument(
    val displayName: String,
    val soundPrefix: String,
    val icon: Int,
    val volumeBoost: Float  // Volume multiplier để normalize sounds
) {
    PIANO("Piano", "note_", R.drawable.piano_selected, 1.0f),
    XYLOPHONE("Xylophone", "_xylo", R.drawable.xylophone_selected, 1.0f),  // Boost 20%
    GUITAR("Guitar", "_guitar", R.drawable.guitar_selected, 1.0f),

    FLUTE("Flute","flute",R.drawable.flute_selected,1.0f)
    ;  // Boost 30%

    fun getSoundResource(noteName: String): Int {
        return when (this) {
            PIANO -> {
                when (noteName.lowercase()) {
                    "do" -> R.raw.samples_piano_c3
                    "re" -> R.raw.samples_piano_d3
                    "mi" -> R.raw.samples_piano_e3
                    "fa" -> R.raw.samples_piano_f3
                    "sol" -> R.raw.samples_piano_g3
                    "la" -> R.raw.samples_piano_a3
                    "si" -> R.raw.samples_piano_b3
                    "do2" -> R.raw.samples_piano_c4
                    else -> 0
                }
            }
            XYLOPHONE -> {
                when (noteName.lowercase()) {
                    "do" -> R.raw.do_xylo
                    "re" -> R.raw.re_xylo
                    "mi" -> R.raw.mi_xylo
                    "fa" -> R.raw.fa_xylo
                    "sol" -> R.raw.sol_xylo
                    "la" -> R.raw.la_xylo
                    "si" -> R.raw.si_xylo
                    "do2" -> R.raw.do2_xylo
                    else -> 0
                }
            }

            FLUTE -> {
                when(noteName.lowercase()) {
                    "do" -> R.raw.flute_do
                    "re" -> R.raw.flute_re
                    "mi" -> R.raw.flute_mi
                    "fa" -> R.raw.flute_fa
                    "sol" -> R.raw.flute_sol
                    "la" -> R.raw.flute_la
                    "si" -> R.raw.flute_si
                    "do2" -> R.raw.flute_do2
                    else -> 0
                }
            }

            GUITAR -> {
                when (noteName.lowercase()) {
                    "do" -> R.raw.do_guitar
                    "re" -> R.raw.re_guitar
                    "mi" -> R.raw.mi_guitar
                    "fa" -> R.raw.fa_guitar
                    "sol" -> R.raw.g3_guitar_booster
                    "la" -> R.raw.a3_guitar_booster
                    "si" -> R.raw.si_guitar
                    "do2" -> R.raw.do2_guitar
                    else -> 0
                }
            }



        }
    }

    companion object {
        fun fromName(name: String?): Instrument {
            return values().find { it.name == name } ?: PIANO
        }
    }
}
