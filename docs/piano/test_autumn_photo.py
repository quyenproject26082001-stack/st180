"""Regression checks for the Co May practice arrangement. Run with unittest."""
import json
from pathlib import Path
import unittest
from autumn_photo import build, left_hand


class CoMayLessonTest(unittest.TestCase):
    def test_fingers_land_on_their_notes_and_chords_have_distinct_fingers(self):
        notes, _ = build()
        groups = {}
        for n in notes:
            self.assertIn(n['finger'], range(1, 6))
            self.assertEqual(len(n['pose']), 5)
            self.assertEqual(n['pose'][n['finger'] - 1], n['midi'])
            self.assertEqual(n['pose'], sorted(n['pose'], reverse=n['hand'] == 1))
            self.assertLessEqual(max(n['pose']) - min(n['pose']), 12)
            key = (n['hand'], n['start'])
            self.assertNotIn(n['finger'], groups.setdefault(key, []))
            groups[key].append(n['finger'])

    def test_left_hand_repositions_instead_of_spanning_a_twelfth(self):
        notes, _ = build()
        left = [n for n in notes if n['hand'] == 1 and n['start'] < 3200]
        self.assertEqual([n['finger'] for n in left], [5, 2, 1, 2, 1])
        self.assertEqual(left[0]['pose'], left[2]['pose'])
        self.assertNotEqual(left[2]['pose'], left[3]['pose'])

    def test_verse_f_is_open_arpeggio_not_block_chord(self):
        self.assertEqual(left_hand(1, 'F'), [
            ('F2', 0, .5), ('C3', .5, .5), ('F3', 1, .5),
            ('A3', 1.5, .5), ('C4', 2, 2)])

    def test_chorus_am_uses_returning_fifth(self):
        self.assertEqual([n for n, _, _ in left_hand(22, 'Am')],
                         ['A2', 'E3', 'A3', 'E4', 'C4', 'E4'])

    def test_chord_changes_do_not_leave_previous_bass_sounding(self):
        for chord, split in [('Bb/C', 1.5), ('Gm/C', 2)]:
            notes = left_hand(8, chord)
            self.assertTrue(all(t + d <= split for _, t, d in notes[:3]))
            self.assertEqual(notes[3], ('C2', split, .5))
            self.assertTrue(all(t + d <= 4 for _, t, d in notes))

    def test_packaged_assets_match_generator_and_have_no_appended_ending(self):
        notes, timeline = build()
        assets = Path(__file__).resolve().parents[2] / 'app/src/main/assets'
        self.assertEqual(json.loads((assets / 'autumn_photo_notes.json').read_text()), notes)
        self.assertEqual(json.loads((assets / 'autumn_photo_timeline.json').read_text()), timeline)
        self.assertEqual(len(timeline), 34)
        self.assertEqual(max(n['end'] for n in notes), 108800)
        self.assertTrue(all(n['end'] > n['start'] and n['hand'] in (0, 1) for n in notes))
        self.assertEqual(len([n for n in notes if n['hand'] == 1 and n['start'] == 0]), 1)


if __name__ == '__main__':
    unittest.main()
