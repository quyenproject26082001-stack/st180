"""Extend existing grand piano samples above A6 for the full video runs.

Uses Python 3.12's audioop resampler. Derived from the existing FluidR3
samples under the same attribution/license; never rewrites existing keys.
"""
import audioop
from pathlib import Path
import wave

FOLDER = Path(__file__).resolve().parents[2] / 'app/src/main/assets/piano_keys'


def extend():
    with wave.open(str(FOLDER / '93.wav'), 'rb') as source:
        channels, width, rate = source.getnchannels(), source.getsampwidth(), source.getframerate()
        frames = source.readframes(source.getnframes())
    for midi in range(94, 109):
        target = FOLDER / f'{midi}.wav'
        if target.exists():
            continue
        # Keep the WAV playback rate fixed: shortening the waveform raises pitch.
        resampled, _ = audioop.ratecv(frames, width, channels, rate,
                                     round(rate / 2 ** ((midi - 93) / 12)), None)
        with wave.open(str(target), 'wb') as output:
            output.setnchannels(channels)
            output.setsampwidth(width)
            output.setframerate(rate)
            output.writeframes(resampled)


if __name__ == '__main__':
    extend()
