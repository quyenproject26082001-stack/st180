"""Render the manual PDF transcription. Run with numpy, scipy and soundfile.
The supplied score is a different arrangement from the linked YouTube cover.
"""
from pathlib import Path
import json
import re
import wave
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly
from fractions import Fraction
from piano.score import SCORE, ORDER

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
RATE = 44100

def midi(note):
    m = re.fullmatch(r'([A-G])([b#]?)([0-8])', note)
    assert m, note
    return (int(m[3])+1)*12 + dict(C=0,D=2,E=4,F=5,G=7,A=9,B=11)[m[1]] + {'':0,'b':-1,'#':1}[m[2]]

def tokens(line):
    for token in line.split():
        pitch, duration = token.split(':')
        yield pitch.lstrip('^!').split('+'), float(duration), pitch.startswith('^'), pitch.startswith('!')

def main():
    events, timeline = [], []
    previous = [{}, {}]
    now = 0.0
    for occurrence, bar in enumerate(ORDER):
        _, right, left = SCORE[bar]
        beats = 2 if bar == 0 else 4
        # Gradual ritardando from the printed marking; the main tempo is 100.
        beat = .6 if bar < 39 else {39:.68,40:.78,41:.9,42:1.05}[bar]
        timeline.append(dict(bar=bar, startMs=round(now*1000), right=right, left=left))
        for hand, line in enumerate((right,left)):
            cursor = 0.
            for notes, duration, tied, rolled in tokens(line):
                if notes == ['r']:
                    cursor += duration
                    previous[hand] = {}
                    continue
                start = now + cursor*beat
                length = duration*beat
                if duration == 0:
                    start = max(0, start-.065)
                    length = .06
                active = {}
                for n, note in enumerate(notes):
                    old = previous[hand].get(note)
                    if tied and old is not None and abs(old['end']-start) < .002:
                        old['end'] = start+length
                        active[note] = old
                        continue
                    # Melody is voiced above accompaniment; deterministic phrase shaping.
                    gain = (.58 if hand == 0 else .28) * (1+.10*np.sin(cursor*np.pi/2))
                    if bar >= 23: gain *= 1.08
                    if bar >= 39: gain *= max(.65,1-(bar-39)*.09)
                    event = dict(note=note, start=start+(n*.022 if rolled else 0),
                                 end=start+length, gain=gain/(len(notes)**.3), hand=hand,
                                 pedal=now+beats*beat)
                    events.append(event)
                    active[note] = event
                if duration > 0:
                    previous[hand] = active
                cursor += duration
            assert abs(cursor-beats)<1e-6, (bar,hand,cursor)
        now += beats*beat
    (ASSETS/'autumn_notes.json').write_text(json.dumps([
        dict(midi=midi(e['note']), start=round(e['start']*1000),
             end=round(e['end']*1000), hand=e['hand']) for e in events
    ], indent=2)+'\n')
    samples = {}
    raw_dir = ROOT/'docs/piano/grand_source'
    for key in range(28,94):
        data,sr = sf.read(raw_dir/f'{key}.wav', always_2d=True)
        ratio = Fraction(RATE,sr)
        samples[key] = resample_poly(data.mean(axis=1),ratio.numerator,ratio.denominator)
    cache = {}
    mix = np.zeros((int((now+5)*RATE),2),dtype=np.float32)
    for e in events:
        key=midi(e['note'])
        if key not in cache:
            root=min(samples,key=lambda x:abs(x-key))
            ratio=Fraction(2**((root-key)/12)).limit_denominator(1000)
            cache[key]=resample_poly(samples[root],ratio.numerator,ratio.denominator)
        sample=cache[key]
        # Release at pedal change, capped to avoid smearing the next harmony.
        hold=max(e['end'],min(e['pedal'],e['end']+.65))-e['start']
        frames=min(len(sample),int((hold+.32)*RATE))
        sound=sample[:frames].copy()
        envelope=np.clip((hold+.32-np.arange(frames)/RATE)/.32,0,1)
        sound*=envelope*e['gain']
        pan=np.clip((key-60)/36,-.65,.65)
        start=round(e['start']*RATE)
        mix[start:start+frames,0]+=sound*np.sqrt((1-pan)/2)
        mix[start:start+frames,1]+=sound*np.sqrt((1+pan)/2)
    keys = ASSETS/'piano_keys'
    keys.mkdir(exist_ok=True)
    for key in range(28, 94):
        if key not in cache:
            root=min(samples,key=lambda x:abs(x-key))
            ratio=Fraction(2**((root-key)/12)).limit_denominator(1000)
            cache[key]=resample_poly(samples[root],ratio.numerator,ratio.denominator)
        sample = cache[key]
        # Short mono samples remain below SoundPool decoded-sample size limit.
        sound = sample[:int(RATE*6.0)].copy()
        fade = min(len(sound), int(RATE*.45))
        sound[-fade:] *= np.linspace(1,0,fade)
        sf.write(keys/f'{key}.wav',sound,RATE,subtype='PCM_16')
    dry=mix.copy()
    for delay,gain in [(.037,.10),(.061,.08),(.109,.055),(.173,.035),(.257,.02)]:
        offset=round(delay*RATE)
        mix[offset:]+=dry[:-offset,::-1]*gain
    peak=float(np.abs(mix).max())
    assert np.isfinite(mix).all() and peak>0
    mix *= .88/peak
    ASSETS.mkdir(exist_ok=True)
    wav = ASSETS/'lang_quen_chieu_thu_arrangement.wav'
    sf.write(wav,mix,RATE,format='WAV',subtype='PCM_16')
    import subprocess
    subprocess.run(['gst-launch-1.0','-q','filesrc',f'location={wav}','!','wavparse','!','audioconvert','!','vorbisenc','quality=0.5','!','oggmux','!','filesink',f'location={ASSETS / "lang_quen_chieu_thu_arrangement.ogg"}'],check=True)
    wav.unlink()
    (ASSETS/'lang_quen_chieu_thu_timeline.json').write_text(json.dumps(timeline,ensure_ascii=False,indent=2)+'\n')
    sf.write('/tmp/lang_quen_chieu_thu_preview.wav',mix[:RATE*30],RATE,format='WAV',subtype='PCM_16')
    print(f'{len(timeline)} played measures; {len(events)} attacks; {now:.2f}s + tail; peak 0.88')

if __name__ == '__main__': main()
