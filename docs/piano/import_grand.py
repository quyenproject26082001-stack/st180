"""Import FluidR3 Acoustic Grand Piano (CC BY 3.0) from MIDI.js Soundfonts.
Run: PYTHONPATH=<numpy/scipy/soundfile directory> python3 docs/piano/import_grand.py
Then run docs/render_autumn_audio.py with the same dependencies.
"""
from pathlib import Path
import base64, io, re, urllib.request, hashlib
import soundfile as sf
ROOT=Path(__file__).resolve().parents[2]
URL='https://gleitz.github.io/midi-js-soundfonts/FluidR3_GM/acoustic_grand_piano-mp3.js'
data=urllib.request.urlopen(URL,timeout=60).read()
folder=ROOT/'docs/piano/grand_source'
folder.mkdir(exist_ok=True)
for name,encoded in re.findall(r'"([A-G][b#]?\d)": "data:audio/mp3;base64,([^"]+)"',data.decode()):
    m=re.fullmatch(r'([A-G])([b#]?)(\d)',name)
    key=(int(m[3])+1)*12+dict(C=0,D=2,E=4,F=5,G=7,A=9,B=11)[m[1]]+{'':0,'b':-1,'#':1}[m[2]]
    if 28<=key<=93:
        audio,rate=sf.read(io.BytesIO(base64.b64decode(encoded)),always_2d=True)
        sf.write(folder/f'{key}.wav',audio.mean(axis=1),rate,subtype='PCM_16')
assert len(list(folder.glob('*.wav')))==66
print('Source SHA256:',hashlib.sha256(data).hexdigest())
