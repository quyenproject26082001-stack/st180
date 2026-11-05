"""Package the separate, full Synthesia capture without changing autumn_photo.

Video timing is retained, including rubato. Hand assignment is an estimate:
the source uses one color for both hands and contains no finger information.
"""
import json
from pathlib import Path

HERE = Path(__file__).resolve().parent
ASSETS = HERE.parents[1] / 'app/src/main/assets'
SECTIONS = [
    (246.0, 'Dạo đầu'),
    (280.7, 'Verse 1'),
    (329.95, 'Chạy nối 1'),
    (333.3, 'Điệp khúc 1'),
    (380.3, 'Verse 2 • Quãng tám cao'),
    (429.317, 'Chạy nối 2'),
    (432.3, 'Điệp khúc 2'),
    (477.4, 'Kết mở rộng'),
]


def build():
    captured = json.loads((HERE / 'co_may_synthesia_capture.json').read_text())
    origin = captured[0]['start']
    assert 246 <= origin < 249
    events = []
    last_left = None
    for note in captured:
        # Low accompaniment can reach C4/E4. Follow its ascending cell where
        # possible; the same-color video cannot certify individual hands.
        pitch, start = note['midi'], note['start']
        is_left = pitch < 60 or (
            pitch < 65 and last_left is not None
            and 0 < start - last_left['start'] < .6
            and 0 < pitch - last_left['midi'] <= 12)
        if is_left:
            last_left = note
        events.append(dict(midi=pitch, start=round((start - origin) * 1000),
                           end=round((note['end'] - origin) * 1000),
                           hand=1 if is_left else 0))
    timeline = [dict(bar=i, startMs=max(0, round((time - origin) * 1000)), label=label)
                for i, (time, label) in enumerate(SECTIONS, 1)]
    return events, timeline


if __name__ == '__main__':
    events, timeline = build()
    assert all(21 <= n['midi'] <= 108 and n['end'] > n['start'] for n in events)
    for suffix, data in [('notes', events), ('timeline', timeline)]:
        (ASSETS / f'co_may_synthesia_{suffix}.json').write_text(json.dumps(data, indent=2) + '\n')
    print(f'{len(events)} notes, {max(n["end"] for n in events) / 1000:.3f}s, '
          f'MIDI {min(n["midi"] for n in events)}–{max(n["midi"] for n in events)}')
