"""Build a Fa-major practice arrangement from the two ST180 lesson captures.

The Minh Khang transcription provides the main score. After the first two beats
of measure 30, the second Synthesia cô Mây lead-in replaces the last two beats.
One additional 4/4 measure fits the borrowed run and a breath before the original
downbeat of measure 31. The borrowed notes keep their captured timing and pitch.

Sources: score.py, render_autumn_audio.py and CO_MAY_SYNTHESIA.md.
"""

import json
from pathlib import Path


ASSETS = Path(__file__).resolve().parents[2] / "app/src/main/assets"
SONG_ID = "autumn_co_may_mix"
SYNTHESIA_BASS_START_MS = 180_900


def read_asset(name):
    return json.loads((ASSETS / f"{name}.json").read_text(encoding="utf-8"))


def generate():
    original = read_asset("autumn_notes")
    original_timeline = read_asset("lang_quen_chieu_thu_timeline")
    synthesia = read_asset("co_may_synthesia_notes")
    synthesia_timeline = read_asset("co_may_synthesia_timeline")

    bar_30 = next(row["startMs"] for row in original_timeline if row["bar"] == 30)
    bar_31 = next(row["startMs"] for row in original_timeline if row["bar"] == 31)
    synthesia_chorus = next(row["startMs"] for row in synthesia_timeline if row["bar"] == 7)
    added_measure_ms = bar_31 - bar_30
    splice_start = bar_30 + added_measure_ms // 2
    chorus_start = bar_31 + added_measure_ms

    pickup = [note for note in synthesia if SYNTHESIA_BASS_START_MS <= note["start"] < synthesia_chorus]
    assert min(note["start"] for note in pickup) == SYNTHESIA_BASS_START_MS
    assert max(note["end"] for note in pickup) - SYNTHESIA_BASS_START_MS < chorus_start - splice_start

    def note_at(note, offset=0, end_limit=None):
        end = note["end"] + offset
        return {
            "midi": note["midi"],
            "start": note["start"] + offset,
            "end": min(end, end_limit) if end_limit is not None else end,
            "hand": note["hand"],
        }

    notes = [note_at(note, end_limit=splice_start) for note in original if note["start"] < splice_start]
    notes.extend(note_at(note, splice_start - SYNTHESIA_BASS_START_MS) for note in pickup)
    notes.extend(note_at(note, added_measure_ms) for note in original if note["start"] >= bar_31)
    notes.sort(key=lambda note: (note["start"], note["hand"], note["midi"]))

    # Keep the original printed bar numbers and give the playable pickup its own seek marker.
    pickup_marker = max(row["bar"] for row in original_timeline) + 1
    timeline = [{"bar": row["bar"], "startMs": row["startMs"]}
                for row in original_timeline if row["startMs"] < bar_31]
    timeline.append({"bar": pickup_marker, "startMs": splice_start, "label": "Chạy đà cô Mây"})
    timeline.extend({"bar": row["bar"], "startMs": row["startMs"] + added_measure_ms}
                    for row in original_timeline if row["startMs"] >= bar_31)
    return notes, timeline


if __name__ == "__main__":
    notes, timeline = generate()
    for suffix, rows in (("notes", notes), ("timeline", timeline)):
        contents = ",\n".join(f"  {json.dumps(row, ensure_ascii=False)}" for row in rows)
        (ASSETS / f"{SONG_ID}_{suffix}.json").write_text(f"[\n{contents}\n]\n", encoding="utf-8")
