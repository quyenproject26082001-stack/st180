"""Normalize mono PCM16 piano keys to -6 dBFS peak (polyphony headroom).

Preserves pitch, decay and duration. Run on the generated piano_keys directory.
"""
import array
import math
from pathlib import Path
import sys
import wave

TARGET = 10 ** (-6 / 20)


def normalize_pcm(path):
    with wave.open(str(path), 'rb') as reader:
        params = reader.getparams()
        assert params.sampwidth == 2 and params.nchannels == 1, path
        data = array.array('h', reader.readframes(params.nframes))
    if sys.byteorder != 'little':
        data.byteswap()
    peak = max(map(abs, data), default=0)
    assert peak > 0, path
    target = round(32767 * TARGET)
    gain = target / peak
    result = array.array('h', (round(v * gain) for v in data))
    assert max(map(abs, result)) <= target
    if sys.byteorder != 'little':
        result.byteswap()
    with wave.open(str(path), 'wb') as writer:
        writer.setparams(params)
        writer.writeframes(result.tobytes())
    return 20 * math.log10(gain)


if __name__ == '__main__':
    folder = Path(__file__).resolve().parents[2] / 'app/src/main/assets/piano_keys'
    gains = [normalize_pcm(folder / f'{m}.wav') for m in range(28, 94)]
    print(f'66 keys: applied gain {min(gains):.1f}..{max(gains):.1f} dB; peak -6 dBFS')
