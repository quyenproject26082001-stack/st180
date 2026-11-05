# Lãng Quên Chiều Thu • Synthesia cô Mây

Separate song ID `co_may_synthesia`, selected from either Autumn Home button.
The existing `autumn_photo` song, assets, fingering and saved settings are unchanged.

Source: `/home/lequyen/Downloads/piano_co_may.mp4` (1280×720, 23:07).
SHA-256: `1252a89f331bfe2e8246e8b4630501309faea9667f662b2363ca0a1ebe3cb8d8`.
Only the full Synthesia/SeeMusic performance at 04:07–08:10 is captured;
the later spoken tutorial and earlier camera performance are excluded.

The 30 fps capture contains 1,052 visible key presses from 247.383 to 489.217
seconds. After shifting the first note to zero, the lesson lasts 241.834 seconds.
It includes the intro, both verses, both chorus passes, both ascending C runs,
the high-octave variation and the extended ending. MIDI range: 29–108 (F1–C8).
The eight navigation markers are named sections, not claimed measure boundaries.
Speed controls scale the captured performance timing; no fixed 75 BPM is claimed.

## Reproduce

```sh
python3 docs/piano/extract_co_may_synthesia.py /home/lequyen/Downloads/piano_co_may.mp4
python3 docs/piano/co_may_synthesia.py
python3 docs/piano/co_may_synthesia_samples.py
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s docs/piano -p 'test_co_may_synthesia.py'
```

The capture needs PyGObject, GStreamer H.264 decoding and Pillow. Packaging uses
only the standard library. Sample extension uses Python 3.12 audioop; keys 94–108
are resampled from existing key 93, retaining the existing FluidR3 attribution.
Existing samples are not overwritten.

## Accuracy boundary

This is an automated transcription of illuminated video keys, not original MIDI.
Key centers are calibrated against an empty frame to avoid false accidentals.
Timing resolution is about 33 ms; presses shorter than 55 ms may be omitted.
Key illumination supplies release times; pedal and original dynamics are unavailable.
All displayed keys share one color: left/right assignment is an estimated split
with an accompaniment-continuation rule. Use both hands/demo to hear the entire
capture. Hand identity and fingering have not been certified; no synthetic fingers
are attached. Final listening and device/MIDI validation remain necessary.

## Verification (2026-11-05)

All 10 Python piano-data tests passed, including full capture preservation,
both previously missing C runs, the high-octave repeat, sample availability,
section ordering and reproducible packaged assets. Android debug assembly and
all 24 JVM unit tests passed using the local Linux SDK. No device was operated.
