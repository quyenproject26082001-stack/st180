"""Conservative suggested fingering for the advanced Autumn score.

Overlapping notes (including rolled chords/grace notes) share one pose. Groups
too wide for the octave-sized guide are deliberately left unassigned.
No pitches, hands, durations or ordering are changed.
"""
from itertools import combinations
from pathlib import Path
import json


def annotate(notes):
    for n in notes:
        n.pop('finger', None)
        n.pop('pose', None)
    for hand in (0, 1):
        groups = []
        end = -1
        for n in sorted((n for n in notes if n['hand'] == hand), key=lambda n: n['start']):
            if n['start'] >= end:
                groups.append([])
            groups[-1].append(n)
            end = max(end, n['end'])
        previous = None
        for group in groups:
            required = {n['midi'] for n in group}
            if len(required) > 5 or max(required) - min(required) > 12:
                previous = None
                continue
            if previous is not None and required.issubset(previous):
                pose = previous
            else:
                pool = set(range(max(28, min(required)-7), min(93, max(required)+7)+1))
                # Resting fingers favor the F-major collection; accidentals in
                # the score are always retained as required targets.
                pool = {m for m in pool if m % 12 in (0,2,4,5,7,9,10)} | required
                candidates = []
                for extra in combinations(sorted(pool-required), 5-len(required)):
                    p = sorted(required | set(extra), reverse=hand == 1)
                    if max(p)-min(p) > 12:
                        continue
                    gaps = [abs(a-b) for a,b in zip(p,p[1:])]
                    cost = sum(max(0, gap-3)**2 for gap in gaps)
                    cost += .15 * abs((max(p)-min(p))-7)
                    if previous:
                        cost += .5 * sum(abs(a-b) for a,b in zip(p, previous))
                    if p[0] % 12 in (1,3,6,8,10):
                        cost += 2
                    candidates.append((cost,p))
                if not candidates:
                    previous = None
                    continue
                pose = min(candidates, key=lambda x:x[0])[1]
            for n in group:
                n.update(finger=pose.index(n['midi'])+1, pose=pose)
            previous = pose
    return notes


if __name__ == '__main__':
    path=Path(__file__).resolve().parents[2]/'app/src/main/assets/autumn_notes.json'
    notes=annotate(json.loads(path.read_text()))
    path.write_text(json.dumps(notes,indent=2)+'\n')
    missing=[n for n in notes if 'finger' not in n]
    print(f'{len(notes)-len(missing)}/{len(notes)} notes have suggested fingering')
    print('Unassigned wide/overlapping groups:', [(n['hand'],n['start'],n['midi']) for n in missing])
