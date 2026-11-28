"""Generate the Instant Eternity intermediate lesson from a manual transcription.

Zhao Haiyang, transcribed by coomt. Sheet: https://www.qinpu123.com/qiyue/gangqin/580794.html
The score is in B major. Note names below inherit its key signature; an explicit
`n` cancels it. Durations are in quarter-note beats with an eighth note as the
default. `~` continues a tie, `+` joins a chord and `r` is a rest. Grace notes
take 60 ms before their main note. Rolled chords are grouped for chord practice.
"""

import json
import re
from fractions import Fraction
from pathlib import Path


# Printed measure, right hand, left hand. Start at the pickup in measure 1.
SCORE = (
    (1, "B5 C6", "r:1"),
    (2, "D6:1.5 B5 F6:1.5 B5", "B3 F4 B4:1 A3 F4 A4:1"),
    (3, "B6:1.5 A6 F6:1.5 B5", "G3 D4 G4:1 F3 C4 F4:1"),
    (4, "G6:1 F6 E6 F6:1 B5:.75 G5:.25", "E3 B3 E4:1 D3 B3 D4:1"),
    (5, "E6 D6 C6 B5 C6:1 B5 C6", "C3 G3 B3:1 F2 C3 A3:1"),
    (6, "D6:1.5 B5 F6:1.5 B5", "B3 F4 B4:1 A3 F4 A4:1"),
    (7, "B6:1.5 A6 F6:1.5 B5", "G3 D4 B4:1 F3 D4 F4:1"),
    (8, "G6 F6 E6 D6 D6:1 C6:1", "E3 B3 E4:1 D3 A3 C4:1"),
    (9, "D5+F5+B5:3 ~D5+F5+B5 F4", "B2 F3 B3 C4 D4:2"),
    (10, "D5:1.5 B4 F5:1.5 B4", "B2 F3 D4:1 A2 F3 C4:1"),
    (11, "B5:1.5 A5 F5:1.5 B4", "G2 D3 B3:1 F2 D3 F3 A3"),
    (12, "G5 B4 E5 G5 F5 B4 D5 F5", "E2 B2 G3:1 D2 B2 D3 F3"),
    (13, "E5:1 D5:1 C5:1.5 F4", "C3 G3 B3:1 F2 C3 A3:1"),
    (14, "D5:1.5 B4 F5:1.5 B4", "B2 F3 D4:1 A2 F3 C4:1"),
    (15, "B5:1 A5:1 F5:2", "G2 D3 B3:1 F2 D3 F3 A3"),
    (16, "G5 B4 E5 G5 F5 B4 D5 F5", "E2 B2 G3:1 D2 B2 F3:1"),
    (17, "D5:1 C5:1", "F2 C3 A3:1"),
    (18, "B4:3 B5 C6", "B2 F3 B3 C4 D4:2"),
    (19, "C6:1 F5 D6 C6:1 F6:1", "B3 F4 B4:1 A3 F4 A4:1"),
    (20, "B5:1 D5 B5 A5:1 D6:1", "G3 D4 G4:1 F3 D4 F4:1"),
    (21, "G5:1 E5 G5 F5 B5 D6 F6", "E3 B3 E4 B3 D3 B3 D4 B3"),
    (22, "E6:1 D6:1 C6:2", "C3 G3 E4 C4 F3 C4 E4 A4"),
    (23, "F5+B5+D6:1 F5 D6 C6:1 F6:1", "B3 F4 B4 F4 A3 F4 A4 F4"),
    (24, "D5+G5+B5:1 D5 B5 A5:1 D6:1", "G3 D4 B4 D4 F3 D4 F4 A4"),
    (25, "G5 B5 E6 G6 F6:1.5 A5", "E3 B3 E4 B3 F3 C4 E4:1"),
    (26, "B5:3 B4:1", "B2 F3 B3 C4 D4:1 C4 B3"),
    (27, "G5 G5 G5 B4 A4:1 A4 G5", "E2 E3 G3 E3 E2 C3 A3 F3"),
    (28, "F5 F5 F5 C5 B4:2", "D2 D3 F3 A3 G2 D3 F3 B3"),
    (29, "G4+B4+E5 E5 E5 B4 A4:1 F4 A4", "C3 G3 B3 G3 F2 C3 A3 F3"),
    (30, "C5 B4 B4 C5 D5:1.5 B4", "B2 F3 B3 D4 B2 F3 An3 F3"),
    (31, "G5 G5 G5 B4 A4:1 A5 G5", "E2 E3 G3 E3 E2 C3 A3 F3"),
    (32, "F5 F5 F5 D5 G5:1.5 C5:0 D5", "D2 D3 F3 A3 G2 D3 B3 G3"),
    (33, "C5 D5:1 G5 F5:1 C5:0 D5 C5", "C3 G3 B3 G3 F2 C3 A3:1"),
    (34, "~C5 B4:1.5 ~B4:2", "B2 F3 B3 C4 D4:1 F4:1"),
    (35, "G6 G6 G6 D6 C6:2", "E3 B3 G4:1 E3 C4 A4:1"),
    (36, "F6 F6 F6 C6 B5:2", "D3 A3 F4:1 G3 D4 F4 B4"),
    (37, "G5+B5+E6 D6 C6 B5 A5:1 F5 C6", "C4 G4 B4 G4 F3 C4 A4:1"),
    (38, "F5+B5:1 F5+B5 C6 D6:1.5 B5", "B2 F3 B3 D4 B2 F3 An3 F3"),
    (39, "G6 G6 G6 B5 A5:1 A6 G6", "E2 E3 G3 B3 E2 C3 F3 A3"),
    (40, "F6:1 F6 C6 B5:2", "D2 D3 F3 A3 G2 D3 B3 G3"),
    (41, "G5+B5+E6 E6 E6 G5 B5:1 A5:1", "C3 G3 B3 E4 F3 C4 E4:1"),
    (42, "D5+F5+B5:4", "B2 F3 B3 C4 D4:1 F4:1"),
    (43, "D5 B4 B4 D5 C5:2", "E2 B2 E3 G3 E2 C3 A3 F3"),
    (44, "C5 F5 F5 C5 D5:2", "D2 D3 F3 A3 G2 D3 B3 A3"),
    (45, "D5 B4 B4 D5 C5:1 F4 C5", "E2 E3 G3 B3 F2 C3 A3:1"),
    (46, "F4+C5:.75 F4+D5:.25 ~F4+D5 D5 ~D5:1 B4:1", "B2 F3 B3 C4 B2 F3 An3 F3"),
    (47, "G5 G5 G5 B4 A4:1 A5 G5", "E2 E3 G3 B3 E2 C3 A3 F3"),
    (48, "F5 F5 F5 C5 B4:2", "D2 D3 F3 A3 G2 D3 A3 B3"),
    (49, "G4+B4+E5 D5 C5 G4 B4:1 A4:1", "C3 G3 B3:1 F2 C3 A3:1"),
    (50, "D4+F4+B4:4", "B2 F3 B3 C4 D4:1 F4:1"),
    (51, "G5 G5 G5 B5 A5:2", "E3 B3 G4:1 E3 C4 A4:1"),
    (52, "A5 A5 A5 C6 B5:2", "D3 A3 C4 F4 G3 D4 F4:1"),
    (53, "G5:1.5 B5 B5:1 A5:1", "C3 G3 B3:1 F3 C4 E4:1"),
    (54, "D5+F5+B5:4", "B3:4"),
)

BEAT_MS = 750  # 80 BPM
PITCHES = {"C": 1, "D": 3, "E": 4, "F": 6, "G": 8, "A": 10, "B": 11}


def midi(pitch):
    letter, accidental, octave = re.fullmatch(r"([A-G])(n?)([0-8])", pitch).groups()
    return (int(octave) + 1) * 12 + PITCHES[letter] - bool(accidental)


def part_tokens(part):
    for token in part.split():
        pitches, separator, length = token.partition(":")
        yield pitches, Fraction(length if separator else ".5")


def bar_length(bar):
    return {1: 1, 17: 2}.get(bar, 4)


def generate(score=SCORE, beat_ms=BEAT_MS, pitch_offset=0):
    notes = []
    timeline = []
    previous = [{}, {}]
    now = Fraction(0)

    for bar, right, left in score:
        beats = bar_length(bar)
        timeline.append({"bar": bar, "startMs": round(now * beat_ms)})
        for hand, part in enumerate((right, left)):
            cursor = now
            for pitches, duration in part_tokens(part):
                start = round(cursor * beat_ms)
                end = round((cursor + duration) * beat_ms)
                if duration == 0:
                    start -= 60
                if pitches != "r":
                    tied = pitches.startswith("~")
                    for pitch in pitches.removeprefix("~").split("+"):
                        key = midi(pitch) + pitch_offset
                        if tied:
                            note = previous[hand][key]
                            assert note["end"] == start, (bar, hand, pitches)
                            note["end"] = end
                        else:
                            note = {"midi": key, "start": start, "end": end, "hand": hand}
                            notes.append(note)
                            previous[hand][key] = note
                cursor += duration
            assert cursor == now + beats, (bar, hand, cursor - now)
        now += beats

    return sorted(notes, key=lambda note: (note["start"], note["hand"], note["midi"])), timeline


def write_lesson(song_id, notes, timeline):
    assets = Path(__file__).resolve().parents[2] / "app/src/main/assets"
    for suffix, rows in (("notes", notes), ("timeline", timeline)):
        contents = ",\n".join(f"  {json.dumps(row)}" for row in rows)
        (assets / f"{song_id}_{suffix}.json").write_text(f"[\n{contents}\n]\n", encoding="utf-8")


if __name__ == "__main__":
    write_lesson("instant_eternity", *generate())
