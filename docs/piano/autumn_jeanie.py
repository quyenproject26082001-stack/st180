"""Generate the two-hand lesson from Jeanie's 22 printed measures.

Original listing: https://www.poppiano.org/en/sheet/?id=62769
PDF: https://truongca.com/images/sheet/pdf/zhou/zhou-hua-jian-qi-shi-bu-xiang-zou-piano-wakin-chau-truongca.com.pdf

The two music pages say E major, 4/4, quarter note = 110.
Pitches inherit the printed key signature. Each token is one eighth note
unless followed by an eighth-note count. r is a rest, ~ continues a tie.
"""

from staff_score import generate_score, write_lesson


# Printed measure, right hand, left hand.
SCORE = {
    1: ("r G4 G4 A4 B4:2 A4 G4", "E2 B2 E3:6"),
    2: ("r F4 F4 F4 F4 G4 F4 E4", "D3 F3 B3:6"),
    3: ("r:2 E4 E4 E4 E5:2 D5", "C3 G3 C4:6"),
    4: ("~D5 B4 B4 C5 B4 B4:3", "B2 G3 D4:6"),
    5: ("r C5 C5 C5 C5 D5 C5:2", "A2 E3 A3:6"),
    6: ("r B4 B4 C5 F4 G4 F4 E4", "G2 D3 G3:2 C3 G3 C4:2"),
    7: ("r:2 F4 F4 F4 F4 E4 B3", "F2 C3 F3:6"),
    8: ("G4:2 A4 F4 G4 A4 F4:2", "A2 E3 A3:2 B2 F3 B3:2"),
    9: ("r:2 F4 F4 ~F4 F4 G4 A4", "F2 C3 F3:6"),
    10: ("~A4:3 B3 G4:2 F4 E4", "A2 E3 A3:6"),
    11: ("~E4:2 F4:6", "B2 F3 B3 C4 D4:4"),
    12: ("r:4 B4 C5 E5 F5", "B2 F3 B3:6"),
    13: ("G5:4 B4 C5 E5 F5", "E2 B2 E3 G3 B3:2 E3:2"),
    14: ("C5 B4:3 G4 B4 B4 C5", "G2 D3 G3 B3 D4:2 G3:2"),
    15: ("E5:2 C5 B4 B4 B4 C5 G4", "C3 G3 C4 E4 G4:2 C4:2"),
    16: ("G4:4 G4 F4 F4 E4", "G2 D3 G3 B3 D4:2 G3:2"),
    17: ("E4:3 E5 C5 C5 B4 B4", "A2 E3 A3 C4 E4:2 A3:2"),
    18: ("G4 G4:3 B3 C4 E4 F4", "G2 D3 G3 B3 D4:2 G3:2"),
    19: ("G4:2 F4 E4 C4 E4 F4:2", "F2 C3 F3 A3 C4:2 F3:2"),
    20: ("F4:4 B4 C5 E5 F5", "B2 F3 B3 D4 F4:2 B3:2"),
    21: ("E4:8", "E2 B2 E3 F3 G3:4"),
    22: ("~E4:4 r:4", "~G3:4 r:4"),
}

# First ending covers measures 7–8 on the first repeat and 20 on the second.
PLAY_ORDER = (
    *range(1, 9), *range(1, 7), *range(9, 13),
    *range(13, 21), *range(13, 20), 21, 22,
)

if __name__ == "__main__":
    write_lesson("autumn_jeanie", *generate_score(SCORE, PLAY_ORDER, tempo=110, sharps="CDFG"))
