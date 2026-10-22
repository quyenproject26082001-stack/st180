"""Generate the printable two-hand practice sheet from the app arrangement.

This is an approximate educational arrangement, not the exact cover score.
"""

from pathlib import Path
import re

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/xylophone/learning/music/activity_app/main/AutumnPianoActivity.kt"
MELODY_SOURCE = ROOT / "docs/render_autumn_audio.py"
OUTPUT = ROOT / "app/src/main/assets/lang_quen_chieu_thu_practice.pdf"
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
REGULAR = ImageFont.truetype(FONT, 24)
SMALL = ImageFont.truetype(FONT, 20)
TITLE = ImageFont.truetype(FONT, 42)
BASS_CLEF = ImageFont.truetype("/usr/share/fonts/truetype/noto/NotoMusic-Regular.ttf", 56)
TREBLE_CLEF = ImageFont.truetype("/usr/share/fonts/truetype/noto/NotoMusic-Regular.ttf", 64)
NOTES = ["C3", "D3", "E3", "F3", "G3", "A3", "B3", "C4"]
DIATONIC_BASS = {"C3": 3, "D3": 4, "E3": 5, "F3": 6,
                 "G3": 7, "A3": 8, "B3": 9, "C4": 10}
DIATONIC_TREBLE = {"E4": 0, "F4": 1, "G4": 2, "A4": 3,
                   "B4": 4, "C5": 5, "D5": 6, "E5": 7,
                   "F5": 8, "G5": 9}


def measures():
    pattern = r'Measure\("(Intro|Verse|Chorus)", "([A-G][^\"]*)", listOf\(([^)]*)\)\)'
    rows = []
    for section, chord, values in re.findall(pattern, SOURCE.read_text()):
        indexes = [int(value.strip()) for value in values.split(",")]
        assert len(indexes) == 4 and all(0 <= index < len(NOTES) for index in indexes)
        rows.append((section, chord, [NOTES[index] for index in indexes]))
    melody_block = MELODY_SOURCE.read_text().split("MELODY = [", 1)[1].split("]", 1)[0]
    melody = [line.split() for line in re.findall(r'"([^"]+)"', melody_block)]
    assert len(rows) == len(melody) == 32
    rows = [(section, chord, bass, melody[index])
            for index, (section, chord, bass) in enumerate(rows)]
    return rows


def draw_system(draw, rows, first_bar, y):
    x0, bar_width, treble_bottom, bass_bottom = 98, 260, y + 72, y + 185
    draw.text((x0, y - 54), f"{rows[0][0]}  |  bars {first_bar + 1}-{first_bar + 4}",
              font=REGULAR, fill="#76523E")
    for bottom in (treble_bottom, bass_bottom):
        for line in range(5):
            line_y = bottom - line * 12
            draw.line((x0, line_y, x0 + 4 * bar_width, line_y), fill="#4B3E38", width=2)
    draw.text((x0 - 40, y + 1), "𝄞", font=TREBLE_CLEF, fill="#4B3E38")
    draw.text((x0 - 36, y + 121), "𝄢", font=BASS_CLEF, fill="#4B3E38")
    for bar_index, (_, chord, notes, melody) in enumerate(rows):
        left = x0 + bar_index * bar_width
        draw.text((left + 15, y - 18), chord, font=REGULAR, fill="#A2643F")
        draw.line((left + bar_width, treble_bottom - 48, left + bar_width, treble_bottom),
                  fill="#4B3E38", width=3)
        draw.line((left + bar_width, bass_bottom - 48, left + bar_width, bass_bottom),
                  fill="#4B3E38", width=3)
        for beat, note in enumerate(melody):
            if note == "-":
                continue
            x = left + 37 + beat * 56
            note_y = treble_bottom - DIATONIC_TREBLE[note] * 6
            draw.ellipse((x - 10, note_y - 7, x + 10, note_y + 7), fill="#362C29")
            draw.line((x + 9, note_y, x + 9, note_y - 35), fill="#362C29", width=2)
            draw.text((x - 15, treble_bottom + 14), note, font=SMALL, fill="#5F524C")
        for beat, note in enumerate(notes):
            x = left + 37 + beat * 56
            note_y = bass_bottom - DIATONIC_BASS[note] * 6
            if note == "C4":
                draw.line((x - 16, note_y, x + 17, note_y), fill="#4B3E38", width=2)
            draw.ellipse((x - 10, note_y - 7, x + 10, note_y + 7), fill="#362C29")
            draw.line((x + 9, note_y, x + 9, note_y - 39), fill="#362C29", width=2)
            draw.text((x - 15, bass_bottom + 19), note, font=SMALL, fill="#5F524C")


def make_page(rows, page_index):
    image = Image.new("RGB", (1240, 1754), "#FFFFFF")
    draw = ImageDraw.Draw(image)
    draw.text((90, 60), "Lãng Quên Chiều Thu", font=TITLE, fill="#3F302A")
    draw.text((90, 122), "Piano arrangement  |  C major  |  4/4  |  91 BPM",
              font=REGULAR, fill="#76523E")
    draw.text((90, 165), "Right hand: melody. Left hand: simplified arpeggio and bass.",
              font=SMALL, fill="#5F524C")
    for system in range(4):
        offset = system * 4
        draw_system(draw, rows[offset:offset + 4], page_index * 16 + offset,
                    300 + system * 330)
    draw.text((90, 1670),
              f"Practice arrangement | Not the exact Trí Nhật cover | Page {page_index + 1}/2",
              font=SMALL, fill="#76523E")
    return image


if __name__ == "__main__":
    song = measures()
    pages = [make_page(song[i * 16:(i + 1) * 16], i) for i in range(2)]
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    pages[0].save(OUTPUT, "PDF", resolution=150.0, save_all=True,
                  append_images=pages[1:])
    print(OUTPUT)
