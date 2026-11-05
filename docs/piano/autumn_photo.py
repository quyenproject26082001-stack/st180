"""Co May tutorial practice arrangement, video nA2WweiqOw8.

Melody from IMG_9192/IMG_9193; left-hand patterns checked against the local video.
75 BPM is a practice tempo. The unrelated IMG_8648 ending is not appended.
Timing is quantized; this is not a capture of the performance's rubato/pedal.
"""
import json
from pathlib import Path
from co_may_fingering import annotate

SCORE = [
    ('F', 'r:.5 A4:.5 A4:.5 Bb4:.5 C5:1 Bb4:.5 A4:.5'),
    ('C', '^A4:.5 G4:.5 G4:.5 G4:.5 A4:1 G4:.5 F4:.5'),
    ('Dm', '^F4:1 F4:.5 F4:.5 F4:.5 F5:1 E5:.5'),
    ('Am', '^E5:1 C5:.5 D5:1 C5:1.5'),
    ('Bb', '^C5:1 D5:.5 D5:.5 D5:.5 D5:.5 E5:.5 D5:.5'),
    ('F', '^D5:.5 C5:.5 C5:.5 D5:.5 A4:1 G4:.5 F4:.5'),
    ('Gm', '^F4:1 G4:.5 G4:.5 G4:.5 F4:.5 D4:.5 C4:.5'),
    ('Bb/C', 'A4:.5 Bb4:1 G4:2 ^G4:.5'),
    ('F', 'r:.5 A4:.5 A4:.5 Bb4:.5 C5:1 Bb4:.5 A4:.5'),
    ('C', '^A4:.5 G4:.5 G4:.5 G4:.5 A4:1 G4:.5 F4:.5'),
    ('Dm', '^F4:1 F4:.5 F4:.5 F4:.5 F5:1 E5:.5'),
    ('Am', '^E5:1 C5:.5 D5:1 C5:1.5'),
    ('Bb', '^C5:1 D5:.5 D5:.5 D5:.5 D5:.5 E5:.5 D5:.5'),
    ('F', '^D5:.5 C5:.5 C5:.5 D5:.5 A4:1 G4:.5 F4:.5'),
    ('Gm', '^F4:1 G4:.5 G4:.5 G4:.5 A4:1 Bb4:.5'),
    ('Bb', '^Bb4:1 Bb4:.5 C4:.5 A4:.5 G4:.5 F4:1'),
    ('C', 'G4:4'),
    ('C', 'G4:2 C5:.5 D5:.5 F5:.5 G5:.5'),
    ('F', 'A5:2 C5:.5 D5:.5 F5:.5 G5:.5'),
    ('C', 'C5:2 A4:.5 C5:.5 C5:.5 D5:.5'),
    ('Dm', 'F5:1 D5:.5 C5:.5 C5:.5 C5:.5 D5:1'),
    ('Am', 'A4:2 A4:.5 G4:.5 G4:.5 F4:.5'),
    ('Bb', 'F4:1 ^F4:.5 F5:.5 D5:.5 D5:.5 C5:1'),
    ('F', 'A4:2 C4:.5 D4:.5 F4:.5 G4:.5'),
    ('Gm', 'A4:1 G4:.5 F4:.5 D4:.5 F4:.5 A4:1'),
    ('C', 'G4:2 C5:.5 D5:.5 F5:.5 G5:.5'),
    ('F', 'A5:2 C5:.5 D5:.5 F5:.5 G5:.5'),
    ('C', 'C5:2 A4:.5 C5:.5 C5:.5 D5:.5'),
    ('Dm', 'F5:1 D5:.5 C5:.5 C5:.5 C5:.5 D5:1'),
    ('Am', 'A4:2 A4:.5 G4:.5 G4:.5 F4:.5'),
    ('Bb', 'F4:2 F5:.5 D5:.5 D5:.5 C5:.5'),
    ('F', 'A4:2 C4:.5 D4:.5 F4:.5 G4:.5'),
    ('Gm/C', 'A4:1 G4:.5 F4:.5 D4:.5 F4:.5 G4:1'),
    ('F', 'F4:4'),
]
# Video nA2WweiqOw8: 09:50-10:40 (verse), 14:40-15:20 (chorus).
# Root / fifth / octave / tenth / twelfth; these are successive attacks.
VOICINGS = {
    'F': 'F2 C3 F3 A3 C4', 'C': 'C2 G2 C3 E3 G3',
    'Dm': 'D2 A2 D3 F3 A3', 'Am': 'A2 E3 A3 C4 E4',
    'Bb': 'Bb2 F3 Bb3 D4 F4', 'Gm': 'G2 D3 G3 Bb3 D4',
}


def left_hand(bar, chord):
    """Beat offsets/durations, quantized for practice; no pedal baked into notes."""
    if bar == 34:
        return [('F2', 0, 4), ('C3', 0, 4), ('F3', 0, 4)]
    if chord in ('Bb/C', 'Gm/C'):
        # Short root-fifth-octave cells on each side of the chord change.
        split = 1.5 if chord == 'Bb/C' else 2.0
        result = []
        for symbol, start, end in [(chord.split('/')[0], 0, split), ('C', split, 4)]:
            for i, note in enumerate(VOICINGS[symbol].split()[:3]):
                onset = start + i * .5
                result.append((note, onset, .5 if i < 2 else end - onset))
        return result
    notes = VOICINGS[chord].split()
    if bar <= 17:
        offsets = [0, .5, 1, 1.5, 2]
        durations = [.5, .5, .5, .5, 2]
    else:
        notes = [notes[i] for i in [0, 1, 2, 4, 3, 4]]
        offsets = [0, .5, 1, 2, 2.5, 3]
        durations = [.5, .5, 1, .5, .5, 1]
    return list(zip(notes, offsets, durations))



def midi(name):
    return (int(name[-1]) + 1) * 12 + {'C': 0, 'D': 2, 'E': 4, 'F': 5, 'G': 7, 'A': 9, 'B': 11}[name[0]] + (1 if '#' in name else -1 if 'b' in name else 0)


def build():
    events, timeline = [], []
    now = 0.0
    previous = None
    for bar, (chord, melody) in enumerate(SCORE, 1):
        beats = 0.0
        timeline.append(dict(bar=bar, startMs=now))
        for token in melody.split():
            name, duration = token.split(':')
            duration = float(duration)
            start, end = now + beats * 800, now + (beats + duration) * 800
            if name == 'r':
                previous = None
            elif name.startswith('^'):
                assert previous and previous['midi'] == midi(name[1:]) and previous['end'] == start, (bar, token)
                previous['end'] = end
            else:
                previous = dict(midi=midi(name), start=start, end=end, hand=0)
                events.append(previous)
            beats += duration
        assert beats == 4, (bar, beats)
        for name, offset, length in left_hand(bar, chord):
            events.append(dict(midi=midi(name), start=now + offset * 800,
                               end=now + (offset + length) * 800, hand=1))
        now += beats * 800
    events = sorted(events, key=lambda event: event['start'])
    annotate(events, SCORE, VOICINGS, midi)
    return events, timeline


if __name__ == '__main__':
    events, timeline = build()
    assert len(timeline) == 34
    assert all(28 <= event['midi'] <= 93 and event['end'] > event['start'] for event in events)
    assets = Path(__file__).resolve().parents[2] / 'app/src/main/assets'
    for suffix, data in [('notes', events), ('timeline', timeline)]:
        (assets / f'autumn_photo_{suffix}.json').write_text(json.dumps(data, indent=2) + '\n')
    print(f'{len(events)} notes; {len(timeline)} bars; {max(event["end"] for event in events) / 1000:.1f}s')
