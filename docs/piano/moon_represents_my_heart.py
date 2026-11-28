"""Generate the 41-measure lesson from the three-page Dinh Quang Duc arrangement.

Source: https://pianosnap.com/sheet/25509-anh-trang-noi-ho-long-toi-the-moon-represents-my-heart-piano
PDF: https://truongca.com/images/sheet/pdf/anh/anh-trang-noi-ho-long-toi-the-moon-represents-my-heart-piano-dinh-quang-duc-truongca.com.pdf

C major, 4/4, quarter note = 80. Each token is one eighth note unless
followed by an eighth-note count. r is a rest, + joins a chord. Measures
36–41 use sounding pitches wherever the treble staff has an 8va marking.
Rolled chords are grouped for practice. The PDF has pedal and dynamic markings.
"""

from staff_score import generate_score, write_lesson


# Printed measure, right hand, left hand.
SCORE = {
    1: ("C5+E5+G5:4 r E5 D5 C5", "C3 G3 C4 D4 E4:4"),
    2: ("A4+C5+E5:8", "A2 E3 A3 B3 C4:2 B3:2"),
    3: ("C5+F5+A5:4 r F5 D5 C5", "F2 C3 F3 G3 A3:4"),
    4: ("B4:3 A4 G4:3 G4", "G2 D3 G3 A3 B3:2 G3:2"),
    5: ("C5:3 E5 G5:3 C5", "C3 G3 C4 D4 E4:2 C4:2"),
    6: ("B4:3 E5 G5:3 G5", "E2 B2 E3 G3 B3:2 G3:2"),
    7: ("A5:3 B5 C6:3 A5", "F2 C3 F3 G3 A3:2 F3:2"),
    8: ("G5:6 E5 D5", "C3 G3 C4 D4 E4:2 B3:2"),
    9: ("C5:3 C5 C5:2 E5 D5", "A2 E3 A3 B3 C4:2 A3:2"),
    10: ("C5:3 C5 C5:2 D5 E5", "F2 C3 F3 G3 A3:2 F3:2"),
    11: ("D5:3 C5 A4:2 E5:2", "D3 A3 D4 E4 F4:2 D4:2"),
    12: ("D5:6 r G4", "G2 D3 G3 C4 B3:2 G3:2"),
    13: ("C5:3 E5 G5:3 C5", "C3 G3 C4 D4 E4:2 C4:2"),
    14: ("B4:3 E5 G5:3 G5", "E2 B2 E3 G3 B3:2 G3:2"),
    15: ("A5:3 B5 C6:3 A5", "F2 C3 F3 G3 A3:2 F3:2"),
    16: ("G5:6 E5 D5", "C3 G3 C4 D4 E4:2 B3:2"),
    17: ("C5:3 C5 C5:2 E5 D5", "A2 E3 A3 B3 C4:2 A3:2"),
    18: ("C5:3 C5 C5:2 D5 E5", "F2 C3 F3 G3 A3:2 F3:2"),
    19: ("D5:3 A4 B4:2 C5 D5", "G2 D3 G3 A3 B3:2 G3:2"),
    20: ("C5:6 E5 G5", "C3 G3 C4 D4 E4:2 C4:2"),
    21: ("E5:3 D5 C5:2 G5:2", "C3 G3 C4 G3 E4 G3 C4 G3"),
    22: ("B4:6 A4 B4", "E2 B2 E3 B2 G3 B2 E3 B2"),
    23: ("A4:3 B4 A4:2 G4:2", "F2 C3 F3 C3 A3 C3 F3 C3"),
    24: ("E5:6 G5:2", "C3 G3 C4 D4 E4:2 D4:2"),
    25: ("E5:3 D5 C5:2 G5:2", "C3 G3 C4 G3 E4 G3 C4 G3"),
    26: ("B4:6 A4 B4", "E2 B2 E3 B2 G3 B2 E3 B2"),
    27: ("C5:3 C5 C5:2 D5 E5", "F2 C3 F3 C3 A3 C3 F3 C3"),
    28: ("D5:6 r G4", "G2 D3 G3 D3 B3 D3 G3 D3"),
    29: ("C5:3 E5 G5:3 C5", "C3 G3 C4:2 E4:2 C4:2"),
    30: ("B4:3 E5 G5:3 G5", "E2 B2 E3:2 G3:2 E3:2"),
    31: ("A5:3 B5 C6:3 A5", "F2 C3 F3:2 A3:2 F3:2"),
    32: ("G5:6 C5+E5 B4+D5", "C3 G3 C4 D4 E4:2 B3:2"),
    33: ("A4+C5:3 A4+C5 A4+C5:2 C5+E5 B4+D5", "A2 E3 A3 B3 C4:2 A3:2"),
    34: ("A4+C5:3 A4+C5 A4+C5:2 B4+D5 C5+E5", "F2 C3 F3 G3 A3:2 F3:2"),
    35: ("B4+D5:3 A4 G4+B4:2 A4+C5 B4+D5", "G2 D3 G3 A3 B3:2 G3:2"),
    36: ("A4+C5:6 E6 D6", "C3 G3 C4 D4 E4:2 B3:2"),
    37: ("E5+A5+C6:3 C6 C6:2 E6 D6", "A2 E3 A3:2 E3:4"),
    38: ("F5+A5+C6:3 C6 C6:2 D6 E6", "F2 C3 F3:2 C3:4"),
    39: ("G5+B5+D6:3 A5 B5:2 C6 D6", "G2 D3 G3:2 D3:4"),
    40: ("E5+G5+C6:8", "C3 G3 C4 D4 E4:2 G4:2"),
    41: ("E5+G5+C6:8", "C3+C4:8"),
}


if __name__ == "__main__":
    write_lesson("moon_represents_my_heart", *generate_score(SCORE, SCORE, tempo=80))
