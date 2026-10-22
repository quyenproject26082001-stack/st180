"""Render the app's own piano samples into a fuller practice arrangement.

The melody is an approximate educational arrangement inspired by the public
song score and the linked cover. It is not the cover's original recording.
"""

from pathlib import Path
import re
import subprocess
import wave

import numpy as np


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/xylophone/learning/music/activity_app/main/AutumnPianoActivity.kt"
OUTPUT = ROOT / "app/src/main/assets/lang_quen_chieu_thu_arrangement.ogg"
SAMPLES = ROOT / "app/src/main/res/raw"
RATE = 22050
BPM = 91
BEAT = 60 / BPM
BAR = 4 * BEAT

# Four beats per bar; a dash sustains the preceding note. Notes are in the
# high piano range, while the lower line follows the displayed chords.
MELODY = [
    "G4 A4 C5 D5", "E5 - D5 B4", "C5 - A4 G4", "A4 C5 D5 B4",
    "C5 B4 A4 G4", "D5 C5 B4 A4", "G4 A4 C5 B4", "G4 - - -",
    "G4 A4 C5 D5", "E5 - D5 B4", "C5 B4 A4 G4", "B4 - A4 G4",
    "A4 C5 D5 C5", "B4 A4 G4 E4", "F4 A4 C5 A4", "B4 - G4 -",
    "C5 D5 E5 G5", "E5 - D5 C5", "C5 D5 E5 C5", "B4 - A4 G4",
    "A4 C5 D5 C5", "E5 D5 C5 G4", "A4 C5 D5 F5", "E5 - D5 -",
    "C5 D5 E5 G5", "E5 - D5 C5", "C5 D5 E5 C5", "B4 - A4 G4",
    "A4 C5 D5 C5", "E5 D5 C5 G4", "A4 C5 D5 F5", "E5 D5 C5 -",
]

SEMITONES = {"C": 0, "C#": 1, "D": 2, "D#": 3, "E": 4, "F": 5,
             "F#": 6, "G": 7, "G#": 8, "A": 9, "A#": 10, "B": 11}
CHORDS = {
    "C": ("C3", ["C4", "E4", "G4"]),
    "G/B": ("B2", ["B3", "D4", "G4"]),
    "Am": ("A2", ["A3", "C4", "E4"]),
    "F": ("F2", ["A3", "C4", "F4"]),
    "Em": ("E2", ["G3", "B3", "E4"]),
    "Dm": ("D2", ["A3", "D4", "F4"]),
    "G": ("G2", ["G3", "B3", "D4"]),
}


def midi(note):
    match = re.fullmatch(r"([A-G]#?)([2-5])", note)
    assert match, note
    return (int(match.group(2)) + 1) * 12 + SEMITONES[match.group(1)]


def load_sample(name):
    with wave.open(str(SAMPLES / name), "rb") as file:
        assert file.getsampwidth() == 2
        raw = np.frombuffer(file.readframes(file.getnframes()), dtype="<i2")
        raw = raw.reshape(-1, file.getnchannels()).mean(axis=1) / 32768
        source_rate = file.getframerate()
    index = np.arange(int(len(raw) * RATE / source_rate)) * source_rate / RATE
    return np.interp(index, np.arange(len(raw)), raw).astype(np.float32)


BASE_C3 = load_sample("samples_piano_c3.wav")
BASE_C4 = load_sample("samples_piano_c4.wav")
CACHE = {}


def pitched(note):
    if note not in CACHE:
        key = midi(note)
        base, root = (BASE_C3, midi("C3")) if key < midi("C4") else (BASE_C4, midi("C4"))
        ratio = 2 ** ((key - root) / 12)
        length = int(min(len(base) / ratio, RATE * 4.3))
        sample = np.interp(np.arange(length) * ratio, np.arange(len(base)), base)
        CACHE[note] = sample.astype(np.float32)
    return CACHE[note]


def add_note(mix, note, time, duration, gain, pan=0):
    sample = pitched(note)
    frames = min(len(sample), int((duration + 0.55) * RATE))
    if frames <= 0:
        return
    sound = sample[:frames].copy()
    attack = min(180, frames)
    sound[:attack] *= np.linspace(0, 1, attack)
    fade = np.minimum(1, np.maximum(0, (duration + 0.4 - np.arange(frames) / RATE) / 0.35))
    sound *= fade
    start = int(time * RATE)
    end = min(start + frames, len(mix))
    sound = sound[:end - start] * gain
    mix[start:end, 0] += sound * (1 - pan * .3)
    mix[start:end, 1] += sound * (1 + pan * .3)


def main():
    pattern = r'Measure\("(Intro|Verse|Chorus)", "([A-G][^\"]*)", listOf\(([^)]*)\)\)'
    measures = re.findall(pattern, SOURCE.read_text())
    assert len(measures) == len(MELODY) == 32
    mix = np.zeros((int((len(measures) * BAR + 3) * RATE), 2), dtype=np.float32)
    for bar_index, (_, chord, _) in enumerate(measures):
        start = bar_index * BAR
        bass, harmony = CHORDS[chord]
        add_note(mix, bass, start, 2.2 * BEAT, .35, -.6)
        add_note(mix, bass, start + 2 * BEAT, 1.6 * BEAT, .18, -.6)
        for beat, note in enumerate([harmony[0], harmony[1], harmony[2], harmony[1]]):
            add_note(mix, note, start + beat * BEAT + .03, .85 * BEAT, .15, -.25)
        # Sustained chord cushion, quiet enough to leave room for the melody.
        for note in harmony:
            add_note(mix, note, start + .04, 3.3 * BEAT, .045, -.15)
        melody = MELODY[bar_index].split()
        assert len(melody) == 4
        for beat, note in enumerate(melody):
            if note == "-":
                continue
            length = 1
            while beat + length < 4 and melody[beat + length] == "-":
                length += 1
            add_note(mix, note, start + beat * BEAT + .07,
                     (length - .08) * BEAT, .42, .55)

    # Short, quiet stereo echoes add room without washing out the note attacks.
    dry = mix.copy()
    for delay, gain in [(0.12, .11), (0.25, .08), (0.43, .06)]:
        offset = int(delay * RATE)
        mix[offset:, 0] += dry[:-offset, 1] * gain
        mix[offset:, 1] += dry[:-offset, 0] * gain
    peak = max(np.max(np.abs(mix)), .01)
    mix = np.tanh(mix / peak * 1.4) * .82
    wav = OUTPUT.with_suffix(".wav")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(wav), "wb") as file:
        file.setnchannels(2)
        file.setsampwidth(2)
        file.setframerate(RATE)
        file.writeframes((mix * 32767).astype("<i2").tobytes())
    subprocess.run([
        "gst-launch-1.0", "-q", "filesrc", f"location={wav}", "!", "wavparse",
        "!", "audioconvert", "!", "vorbisenc", "quality=0.5", "!",
        "oggmux", "!", "filesink", f"location={OUTPUT}"
    ], check=True)
    wav.unlink()
    print(OUTPUT)


if __name__ == "__main__":
    main()
