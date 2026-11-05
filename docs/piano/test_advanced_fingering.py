import json, unittest, subprocess
from pathlib import Path
from advanced_fingering import annotate

class AdvancedFingeringTest(unittest.TestCase):
 def test_asset_preserves_original_score(self):
  path='app/src/main/assets/autumn_notes.json'
  old=json.loads(subprocess.check_output(['git','show','HEAD:'+path]))
  new=json.loads(Path(path).read_text())
  strip=lambda rows:[{k:n[k] for k in ('midi','start','end','hand')} for n in rows]
  self.assertEqual(strip(old),strip(new))
  self.assertEqual(annotate(json.loads(Path(path).read_text())),new)
  self.assertEqual(sum('finger' in n for n in new),470)
  for n in new:
   if 'finger' not in n: continue
   self.assertEqual(n['pose'][n['finger']-1],n['midi'])
   self.assertEqual(n['pose'],sorted(n['pose'],reverse=n['hand']==1))
   self.assertLessEqual(max(n['pose'])-min(n['pose']),12)
   for other in new:
    if n is other or n['hand']!=other['hand']: continue
    if max(n['start'],other['start'])<min(n['end'],other['end']):
     self.assertEqual(n['pose'],other['pose'])
     if n['midi']!=other['midi']: self.assertNotEqual(n['finger'],other['finger'])

if __name__=='__main__': unittest.main()
