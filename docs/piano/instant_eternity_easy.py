"""Generate a two-hand beginner arrangement of Zhao Haiyang's Instant Eternity.

Uses the manual coomt transcription in instant_eternity.py. Moves the song to
C major at 68 BPM, keeps the upper melody, and brings high phrases down an octave.
Grace notes are omitted and dotted eighth–sixteenth pairs become even eighths.
The left hand plays one bass key at a time, with up to two attacks per measure.
"""

from fractions import Fraction

from instant_eternity import SCORE, bar_length, generate, midi, part_tokens, write_lesson


def melody(bar, part):
    notes = []
    lower_phrase = bar in range(1, 9) or bar in range(19, 26) or bar in range(35, 43) or bar in range(51, 55)
    for pitches, duration in part_tokens(part):
        if duration == 0:
            continue
        if duration in (Fraction(1, 4), Fraction(3, 4)):
            duration = Fraction(1, 2)
        if pitches == "r":
            notes.append(f"r:{duration}")
            continue

        pitch = max(pitches.removeprefix("~").split("+"), key=midi)
        # Keep the pickup and cadence in the same register as their neighboring phrases.
        if lower_phrase or (bar in (9, 18, 26) and midi(pitch) >= midi("B5")):
            pitch = pitch[:-1] + str(int(pitch[-1]) - 1)
        tie = "~" if pitches.startswith("~") else ""
        notes.append(f"{tie}{pitch}:{duration}")
    return " ".join(notes)


def bass(bar, part):
    beats = bar_length(bar)
    cursor = 0
    attacks = {}
    for pitches, duration in part_tokens(part):
        if cursor in (0, 2) and pitches != "r":
            pitch = min(pitches.removeprefix("~").split("+"), key=midi)
            # After transposition, all bass notes lie between C3 and B3.
            attacks[cursor] = pitch[:-1] + ("2" if pitch[0] == "B" else "3")
        cursor += duration

    first = attacks.get(0, "r")
    second = attacks.get(2, first)
    if first == second:
        return f"{first}:{beats}"
    return f"{first}:2 {second}:{beats - 2}"


def generate_easy():
    score = [(bar, melody(bar, right), bass(bar, left)) for bar, right, left in SCORE]
    return generate(score, beat_ms=Fraction(60000, 68), pitch_offset=1)


if __name__ == "__main__":
    write_lesson("instant_eternity_easy", *generate_easy())
