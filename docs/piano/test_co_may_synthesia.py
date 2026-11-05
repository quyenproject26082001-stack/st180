"""Coverage and packaging checks for the full video lesson (no device needed)."""
import json
import unittest
import wave

from co_may_synthesia import ASSETS, HERE, build


class CoMaySynthesiaTest(unittest.TestCase):
    def test_full_source_is_retained_with_valid_sections_and_audio(self):
        notes, timeline = build()
        captured = json.loads((HERE / 'co_may_synthesia_capture.json').read_text())
        self.assertEqual(len(notes), len(captured))
        self.assertGreater(max(n['end'] for n in notes), 240000)
        self.assertEqual(notes[0]['start'], 0)
        self.assertEqual(len(timeline), 8)
        self.assertEqual(timeline[0]['startMs'], 0)
        self.assertEqual([s['startMs'] for s in timeline], sorted(s['startMs'] for s in timeline))
        origin = captured[0]['start']
        for note, source in zip(notes, captured):
            self.assertEqual(note['midi'], source['midi'])
            self.assertLessEqual(abs(note['start'] - (source['start'] - origin) * 1000), .51)
            self.assertLessEqual(abs(note['end'] - (source['end'] - origin) * 1000), .51)
            self.assertGreater(note['end'], note['start'])
            self.assertIn(note['hand'], (0, 1))
        for start, end in zip(timeline, timeline[1:] + [{'startMs': 250000}]):
            self.assertTrue(any(start['startMs'] <= n['start'] < end['startMs'] for n in notes))
        for midi in range(min(28, min(n['midi'] for n in notes)), max(n['midi'] for n in notes) + 1):
            with wave.open(str(ASSETS / f'piano_keys/{midi}.wav')) as sample:
                self.assertGreater(sample.getnframes(), 1000)
                self.assertGreater(sample.getframerate(), 8000)

    def test_both_missing_runs_and_octave_repeat_are_present(self):
        notes, _ = build()
        origin = json.loads((HERE / 'co_may_synthesia_capture.json').read_text())[0]['start']
        for lo, hi in [(329.9, 332), (429.3, 431.5)]:
            pitches = {n['midi'] for n in notes if lo <= n['start'] / 1000 + origin < hi}
            self.assertTrue({60, 64, 67, 72, 76, 79, 84, 88, 91, 96, 108} <= pitches)
        repeat = [n for n in notes if 381.7 <= n['start'] / 1000 + origin < 384.5]
        self.assertTrue({81, 82, 84} <= {n['midi'] for n in repeat})

    def test_packaged_assets_are_reproducible(self):
        notes, timeline = build()
        self.assertEqual(json.loads((ASSETS / 'co_may_synthesia_notes.json').read_text()), notes)
        self.assertEqual(json.loads((ASSETS / 'co_may_synthesia_timeline.json').read_text()), timeline)


if __name__ == '__main__':
    unittest.main()
