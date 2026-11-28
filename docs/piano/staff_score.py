"""Generate practice assets for two-hand scores in 4/4 at a fixed tempo."""

import json
from pathlib import Path


_NATURAL_PITCHES = {"C": 0, "D": 2, "E": 4, "F": 5, "G": 7, "A": 9, "B": 11}
_EIGHTHS_PER_BAR = 8


def generate_score(score, play_order, tempo, sharps=""):
    def milliseconds(eighths):
        return round(eighths * 30_000 / tempo)

    notes = []
    previous_notes = ({}, {})
    timeline = []
    for index, bar in enumerate(play_order):
        start = index * _EIGHTHS_PER_BAR
        end = start + _EIGHTHS_PER_BAR
        timeline.append({"bar": bar, "startMs": milliseconds(start), "endMs": milliseconds(end)})
        for hand, staff in enumerate(score[bar]):
            position = start
            for token in staff.split():
                pitches, _, length = token.partition(":")
                next_position = position + int(length or "1")
                if next_position <= position:
                    raise ValueError(f"Invalid duration in measure {bar}, hand {hand}: {token}")
                if pitches != "r":
                    for pitch in pitches.split("+"):
                        letter, octave = pitch.removeprefix("~")
                        midi = 12 * (int(octave) + 1) + _NATURAL_PITCHES[letter] + (letter in sharps)
                        if pitch.startswith("~"):
                            previous = previous_notes[hand].get(midi)
                            if previous is None or previous["end"] != milliseconds(position):
                                raise ValueError(f"Unmatched tie in measure {bar}, hand {hand}: {token}")
                            previous["end"] = milliseconds(next_position)
                        else:
                            note = {
                                "midi": midi,
                                "start": milliseconds(position),
                                "end": milliseconds(next_position),
                                "hand": hand,
                            }
                            notes.append(note)
                            previous_notes[hand][midi] = note
                position = next_position
            if position != end:
                raise ValueError(f"Measure {bar}, hand {hand} does not fill 4/4")

    notes.sort(key=lambda note: (note["start"], note["hand"], note["midi"]))
    return notes, timeline


def write_lesson(song_id, notes, timeline):
    assets = Path(__file__).resolve().parents[2] / "app/src/main/assets"
    for suffix, rows in (("notes", notes), ("timeline", timeline)):
        contents = ",\n".join(f"  {json.dumps(row)}" for row in rows)
        (assets / f"{song_id}_{suffix}.json").write_text(f"[\n{contents}\n]\n", encoding="utf-8")
