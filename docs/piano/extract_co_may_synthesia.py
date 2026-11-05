"""Capture visible key presses from the user's local 1280x720 tutorial.

Requires system Python with PyGObject/GStreamer and Pillow. No audio inference.
Usage: python3 docs/piano/extract_co_may_synthesia.py /path/piano_co_may.mp4
"""
import json
from pathlib import Path
import sys

import gi
gi.require_version('Gst', '1.0')
from gi.repository import Gst
from PIL import Image


def runs(image, y, bright, minimum):
    result, start = [], None
    for x in range(39, 1241):
        value = sum(image.getpixel((x, y)))
        inside = value > 240 if bright else value < 160
        if inside and start is None:
            start = x
        elif not inside and start is not None:
            if x - start >= minimum:
                result.append((start + x - 1) / 2)
            start = None
    if start is not None and 1241 - start >= minimum:
        result.append((start + 1240) / 2)
    return result


def capture(video):
    Gst.init(None)
    pipeline = Gst.parse_launch(
        'uridecodebin name=decoder ! videoconvert ! videorate ! '
        'video/x-raw,format=RGB,framerate=30/1 ! '
        'appsink name=sink sync=false max-buffers=2')
    pipeline.get_by_name('decoder').set_property('uri', Path(video).resolve().as_uri())
    sink = pipeline.get_by_name('sink')
    pipeline.set_state(Gst.State.PAUSED)
    pipeline.get_state(30 * Gst.SECOND)
    flags = Gst.SeekFlags.FLUSH | Gst.SeekFlags.ACCURATE

    def pixels(sample):
        caps = sample.get_caps().get_structure(0)
        assert (caps.get_value('width'), caps.get_value('height')) == (1280, 720)
        buffer = sample.get_buffer()
        ok, mapped = buffer.map(Gst.MapFlags.READ)
        assert ok
        data = bytes(mapped.data)
        buffer.unmap(mapped)
        return data

    try:
        # Empty keyboard at the end gives actual black-key centers. Assuming
        # equal boundary positions creates false accidental notes beside whites.
        pipeline.seek_simple(Gst.Format.TIME, flags, 490 * Gst.SECOND)
        pipeline.get_state(20 * Gst.SECOND)
        image = Image.frombytes('RGB', (1280, 720), pixels(sink.emit('pull-preroll')))
        white = [m for m in range(21, 109) if m % 12 not in (1, 3, 6, 8, 10)]
        black = [m for m in range(21, 109) if m not in white]
        white_x, black_x = runs(image, 535, True, 8), runs(image, 450, False, 8)
        assert len(white_x) == 52 and len(black_x) == 36
        points = {m: (round(x), 535) for m, x in zip(white, white_x)}
        points.update({m: (round(x), 465) for m, x in zip(black, black_x)})
        pipeline.seek_simple(Gst.Format.TIME, flags, 246 * Gst.SECOND)
        pipeline.set_state(Gst.State.PLAYING)
        active, events = {}, []
        while True:
            sample = sink.emit('try-pull-sample', 30 * Gst.SECOND)
            if sample is None:
                if sink.get_property('eos'):
                    break
                raise RuntimeError('Timed out decoding video')
            time = sample.get_buffer().pts / Gst.SECOND
            if time >= 490:
                break
            data = pixels(sample)
            pressed = set()
            for midi, (x, y) in points.items():
                # Require a small horizontal majority to reject compression noise.
                hits = 0
                for dx in (-2, 0, 2):
                    offset = (y * 1280 + x + dx) * 3
                    r, g, b = data[offset:offset + 3]
                    hits += r > g + 24 and b > g + 24 and b > 100
                if hits >= 2:
                    pressed.add(midi)
            for midi in pressed - active.keys():
                active[midi] = time
            for midi in list(active):
                if midi not in pressed:
                    start = active.pop(midi)
                    if time - start >= .055:
                        events.append(dict(midi=midi, start=round(start, 3), end=round(time, 3)))
        assert not active, 'Video cut while a key was still lit'
        return sorted(events, key=lambda n: (n['start'], n['midi']))
    finally:
        pipeline.set_state(Gst.State.NULL)


if __name__ == '__main__':
    events = capture(sys.argv[1])
    output = Path(__file__).with_name('co_may_synthesia_capture.json')
    output.write_text(json.dumps(events, indent=2) + '\n')
    print(f'{len(events)} key presses, {events[0]["start"]}–{events[-1]["end"]}s')
