"""Suggested practice fingering, NOT a claim of frame-exact teacher fingering.

LH poses explicitly lift/reposition after the root-fifth-octave cell.
RH uses a small set of phrase positions in F major; a dynamic program selects
positions across a phrase instead of assigning fingers by pitch class.
"""

RIGHT_POSES = [
    [60, 62, 64, 65, 67], [65, 67, 69, 70, 72],
    [67, 69, 70, 72, 74], [69, 70, 72, 74, 76],
    [72, 74, 76, 77, 79], [72, 74, 77, 79, 81],
]


def annotate(events, score, voicings, midi):
    for bar, (chord, _) in enumerate(score, 1):
        start = (bar - 1) * 3200
        left = [n for n in events if n['hand'] == 1 and start <= n['start'] < start + 3200]
        for i, n in enumerate(left):
            symbol = chord
            cell_index = i
            if '/' in chord:
                symbol = chord.split('/')[0] if i < 3 else 'C'
                cell_index = i % 3
            root, fifth, octave, third_hi, fifth_hi = map(midi, voicings[symbol].split())
            # Finger order 1..5, descending pitches for the left hand.
            low = [octave, fifth, root + 5, third_hi - 12, root]
            high = [fifth_hi, third_hi, octave,
                    octave - (1 if third_hi - octave == 4 else 2), fifth]
            if bar == 34:
                finger, pose = [5, 2, 1][i], low
            elif cell_index < 3:
                finger, pose = [5, 2, 1][cell_index], low
            elif bar <= 17:
                finger, pose = [2, 1][cell_index - 3], high
            else:
                finger, pose = [1, 2, 1][cell_index - 3], high
            assert pose[finger - 1] == n['midi']
            n.update(finger=finger, pose=pose)

    right = [n for n in events if n['hand'] == 0]
    # Costs favor stable phrase positions, then small hand relocations.
    states = {None: (0.0, [])}
    for n in right:
        options = [i for i, p in enumerate(RIGHT_POSES) if n['midi'] in p]
        assert options, n
        following = {}
        for i in options:
            candidates = []
            for old, (cost, path) in states.items():
                move = 0 if old is None or old == i else 4 + abs(RIGHT_POSES[old][0] - RIGHT_POSES[i][0]) * .15
                candidates.append((cost + move, path + [i]))
            following[i] = min(candidates, key=lambda x: x[0])
        states = following
    chosen = min(states.values(), key=lambda x: x[0])[1]
    for n, i in zip(right, chosen):
        pose = RIGHT_POSES[i]
        n.update(finger=pose.index(n['midi']) + 1, pose=pose)
