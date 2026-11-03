"""Practice transcription from IMG_9192, IMG_9193 and IMG_8648.

Quarter = 75 is a practice tempo, not a tempo printed on the source.
The last photo supplies the embellished ending; small grace notes are omitted.
Left-hand triads realize handwritten chord symbols and are not a transcribed bass.
"""
import json
from pathlib import Path

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
    ('-', 'C5:.5 D5:.5 F5:.5 G5:.5'),
    ('F', 'A5:3 C6:.5 Bb5:.5'),
    ('C/E', '^Bb5:1.5 C5:.5 ^C5:1 G5:.5 F5:.5'),
    ('Dm', '^F5:2 D5:.5 F5:.5 D5:.5 C5:.5'),
    ('C', '^C5:1.5 F4:.5 G4:.5 A4:.5 C5:.5 F5:.5'),
    ('Bb', 'D5:3 D5:.5 C5:.5'),
    ('Am', '^C5:2 F4:.5 G4:.5 ^G4:.5 A4:.5'),
    ('Gm', '^A4:2 F4:.5 G4:.5 ^G4:.5 A4:.5'),
    ('C', '^A4:2 F4:.5 G4:.5 ^G4:.5 F4:.5'),
    ('Bbm', '^F4:2 Bb3:1 Db4:1'),
    ('F6', 'F4:4'),
]
CHORDS = {
    'F': 'F3 A3 C4', 'C': 'C3 E3 G3', 'Dm': 'D3 F3 A3',
    'Am': 'A2 C3 E3', 'Bb': 'Bb2 D3 F3', 'Gm': 'G2 Bb2 D3',
    'C/E': 'E3 G3 C4', 'Bbm': 'Bb2 Db3 F3', 'F6': 'F3 A3 C4 D4',
}


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
        assert beats == (2 if bar == 35 else 4), (bar, beats)
        harmony = [(chord, 0, beats)]
        if chord in ('Bb/C', 'Gm/C'):
            split = 1.5 if chord == 'Bb/C' else 2
            harmony = [(chord.split('/')[0], 0, split), ('C', split, beats - split)]
        for symbol, offset, length in harmony:
            for name in CHORDS.get(symbol, '').split():
                events.append(dict(midi=midi(name), start=now + offset * 800, end=now + (offset + length) * 800, hand=1))
        now += beats * 800
    return sorted(events, key=lambda event: event['start']), timeline


if __name__ == '__main__':
    events, timeline = build()
    assert len(timeline) == 45
    assert all(28 <= event['midi'] <= 93 and event['end'] > event['start'] for event in events)
    assets = Path(__file__).resolve().parents[2] / 'app/src/main/assets'
    for suffix, data in [('notes', events), ('timeline', timeline)]:
        (assets / f'autumn_photo_{suffix}.json').write_text(json.dumps(data, indent=2) + '\n')
    print(f'{len(events)} notes; {len(timeline)} bars; {max(event["end"] for event in events) / 1000:.1f}s')
