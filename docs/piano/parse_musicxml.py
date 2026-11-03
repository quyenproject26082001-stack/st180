import zipfile,xml.etree.ElementTree as ET,json
from pathlib import Path
root=Path('/home/lequyen/LVT/ST180_Xylophone-Learning-Music');mxl=Path('/tmp/noi-omr/noi+nay+co+anh truongca.com.mxl');z=zipfile.ZipFile(mxl);xml=ET.fromstring(z.read([n for n in z.namelist() if n.endswith('.xml') and not n.startswith('META')][0]))
part=xml.find('part'); div=4; events=[]; timeline=[]; elapsed=0
step=60000/92
for measure in part.findall('measure'):
 num=int(measure.get('number','0')); attrs=measure.find('attributes');
 if attrs is not None and attrs.find('divisions') is not None: div=int(attrs.findtext('divisions'))
 cursor=0; staff_cursor={1:0,2:0}; maxcur=0
 timeline.append({'bar':num,'startMs':round(elapsed)})
 for child in measure:
  tag=child.tag
  if tag=='backup': cursor-=int(child.findtext('duration','0')); continue
  if tag=='forward': cursor+=int(child.findtext('duration','0')); maxcur=max(maxcur,cursor); continue
  if tag!='note': continue
  dur=int(child.findtext('duration','0')); voice=int(child.findtext('voice','1')); staff=int(child.findtext('staff','1')); ischord=child.find('chord') is not None
  if not ischord:
   start=cursor
  else: start=cursor-dur
  pitch=child.find('pitch')
  if pitch is not None:
   midi=(int(pitch.findtext('octave'))+1)*12+{'C':0,'D':2,'E':4,'F':5,'G':7,'A':9,'B':11}[pitch.findtext('step')]+{'-1':-1,'0':0,'1':1}.get(pitch.findtext('alter','0'),0)
   tie=child.find('tie[@type="stop"]') is not None and child.find('tie[@type="start"]') is None
   if not tie: events.append({'midi':midi,'start':round(elapsed+start/div*step,3),'end':round(elapsed+(start+dur)/div*step,3),'hand':0 if staff==1 else 1,'voice':voice})
  if not ischord: cursor+=dur; maxcur=max(maxcur,cursor)
 elapsed += maxcur/div*step
out=root/'app/src/main/assets';out.mkdir(exist_ok=True)
(events.sort(key=lambda e:(e['start'],e['hand'],e['midi'])));(out/'noi_nay_advanced_notes.json').write_text(json.dumps(events,indent=2));(out/'noi_nay_advanced_timeline.json').write_text(json.dumps(timeline,indent=2));print(len(events),len(timeline),elapsed/1000,min(e['midi'] for e in events),max(e['midi'] for e in events))
