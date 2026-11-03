"""Manual transcription of user-provided Mai Trong Quy easy piano PDF.
Quarter-note beats; ^ ties into the previous event. Printed pickup is bar 0.
Run with standard Python to regenerate lesson JSON. Tempo: quarter = 75.
"""
from pathlib import Path
import json
SCORE=[
('E5:.5 F5:.25 G5:.25','r:1'),
('^G5:1 E5:.5 F5:.25 G5:.25 ^G5:1 E5:.5 F5:.25 G5:.25','F3+A3+C4:2 G3+B3+D4:2'),
('^G5:.5 G5:.5 E5:.5 A5:.25 G5:.25 ^G5:.5 G5:.5 E5:.25 D5:.25 C5:.5','E3+G3+B3:2 A3+C4+E4:2'),
('D5:.75 A4:.25 E5:.25 D5:.25 C5:.5 D5:.5 C5:.25 D5:.25 ^D5:.25 G5:.75','F3+A3+C4:2 G3+B3+D4:2'),
('E5:3 E5:.5 F5:.25 G5:.25','C3:.5 G3:.25 C4:.25 ^C4:.5 F4:.5 E4:1 r:1'),
('D5:.75 A4:.25 E5:.25 D5:.25 C5:.5 D5:.5 E5:.5 D5:.5 C5:.5','F3+A3+C4:2 G3+B3+D4:2'),
('C5:3 A4:.75 G4:.25','C3:.5 G3:.25 C4:.25 ^C4:.5 F4:.5 E4:1 r:1'),
('A4:.5 C5:.5 D5:.5 E5:.5 E5:.5 D5:.5 D5:.5 B4:.5','F2:.5 C3:.5 F3:1 G2:.5 D3:.5 G3:1'),
('B4:.5 C5:.5 C5:2 A4:.75 G4:.25','A2:.5 E3:.5 A3:.5 E3:.5 C4:1 r:1'),
('A4:.5 C5:.5 D5:.5 E5:.5 E5:.5 D5:.5 D5:.5 B4:.5','F2:.5 C3:.5 F3:1 G2:.5 D3:.5 G3:1'),
('B4:.5 C5:.5 C5:2 C5:.5 B4:.5','A2:.5 E3:.5 A3:.5 E3:.5 C4:1 r:1'),
('A4:2 D5:1.5 B4:.5','F2:.5 C3:.5 A3:1 G2:.5 D3:.5 B3:1'),
('B4:.5 C5:.5 D5:.5 G4:.5 ^G4:1 C5:.5 B4:.5','E2:.5 B2:.5 G3:1 A2+C3:2'),
('A4:.5 E5:.25 E5:.25 ^E5:1 A4:.5 D5:.25 D5:.25 ^D5:.5 C5:.5','F2:.5 C3:.5 A3:1 G2:.5 D3:.5 B3:1'),
('C5:3 A4:.75 G4:.25','C3:.5 G3:.5 C4:.5 F4:.5 E4:2'),
('C5:3 E5:.5 F5:.25 G5:.25','C3:.5 G3:.5 C4:.5 F4:.5 E4:2'),
('^G5:1 E5:.5 F5:.25 G5:.25 ^G5:1 E5:.5 F5:.25 G5:.25','F3+A3+C4:1 F3+A3+C4:1 G3+B3+D4:1 G3+B3+D4:1'),
('^G5:.5 G5:.5 E5:.5 A5:.25 G5:.25 ^G5:.5 G5:.5 E5:.25 D5:.25 C5:.5','E3+G3+B3:1 E3+G3+B3:1 A3+C4+E4:1 A3+C4+E4:1'),
('D5:.75 A4:.25 E5:.25 D5:.25 C5:.5 D5:.5 C5:.25 D5:.25 ^D5:.25 G5:.75','F3+A3+C4:1 F3+A3+C4:1 G3+B3+D4:1 G3+B3+D4:1'),
('E5:3 E5:.5 F5:.25 G5:.25','C3:.5 G3:.5 C4:.5 F4:.5 E4:2'),
('D5:.75 A4:.25 E5:.25 D5:.25 C5:.5 D5:.5 E5:.5 D5:.5 C5:.5','F3+A3+C4:1 F3+A3+C4:1 G3+B3+D4:1 G3+B3+D4:1'),
('C5:2 r:.5 C5:.5 D5:.5 E5:.5','C3:.5 G3:.5 C4:.5 F4:.5 E4:2'),
('A4:2 D5:1.5 D5:.5','F3+A3+C4:2 G3+B3+D4:2'),
('D5:.5 E5:.5 G5:.5 E5:.5 E5:.5 C5:.5 D5:.5 E5:.5','E3:.5 G3:.5 C4:1 A2:.5 C3:.5 A3:1'),
('A4:2 D5:1.5 B4:.5','F3+A3+C4:2 G3+B3+D4:2'),
('B4:.5 C5:.5 C5:3','C3:.5 G3:.5 C4:.5 F4:.5 E4:2'),
]
ORDER=list(range(5))+[1,2,5,6]+list(range(7,15))+list(range(7,14))+list(range(15,26))
def midi(n): return (int(n[-1])+1)*12+dict(C=0,D=2,E=4,F=5,G=7,A=9,B=11)[n[0]]
def main():
    events=[]; timeline=[]; now=0; previous=[{},{}]
    for bar in ORDER:
        timeline.append(dict(bar=bar,startMs=now))
        for hand,line in enumerate(SCORE[bar]):
            beat=0
            for token in line.split():
                names,d=token.split(':');d=float(d);start=now+beat*800;end=start+d*800
                active={}
                for n in names.lstrip('^').split('+'):
                    if n=='r': continue
                    old=previous[hand].get(n)
                    if names.startswith('^') and old and old['end']==start:
                        old['end']=end;active[n]=old
                    else:
                        e=dict(midi=midi(n),start=start,end=end,hand=hand);events.append(e);active[n]=e
                previous[hand]=active;beat+=d
            assert beat==(1 if bar==0 else 4),(bar,hand,beat)
        now+=(1 if bar==0 else 4)*800
    assets=Path(__file__).resolve().parents[2]/'app/src/main/assets'
    (assets/'noi_nay_notes.json').write_text(json.dumps(sorted(events,key=lambda e:e['start']),indent=2)+'\n')
    (assets/'noi_nay_timeline.json').write_text(json.dumps(timeline,indent=2)+'\n')
    assert all(36<=e['midi']<=93 and e['end']>e['start'] for e in events)
    print(len(events),'notes;',len(timeline),'played bars;',now/1000,'seconds')
if __name__=='__main__':main()
