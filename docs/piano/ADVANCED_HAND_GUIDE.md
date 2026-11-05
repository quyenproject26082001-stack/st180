# Advanced Autumn virtual hands

Enabled for song ID `autumn` (the default falling-note lesson). The same toggle
and renderer as Co May are used, with separate per-song preferences.

Run `python docs/piano/advanced_fingering.py` after editing the advanced JSON.
`render_autumn_audio.py` also calls this annotator when rebuilding the score.
It adds metadata only, preserving every original pitch, onset, release and hand.

These are algorithmic **suggested** fingerings, not an authoritative performance
transcription. Temporal overlap groups share a pose so a rolled chord or grace
note cannot assign two different pitches to one finger while both are held.
Poses favor the F-major collection, include score accidentals, stay within one
octave and prefer retaining the previous position when possible.

470 of 474 notes have guides. The left-hand rolled G3–D4–G4–Bb4 near
109.52 seconds spans a tenth; its four notes are deliberately unassigned.
The left hand disappears there and an on-screen message refers to the sheet.
No later note is shown as if it were the current instruction. This passage
needs an individually reviewed rolled/released fingering before adding a guide.

Checks: score identity, deterministic regeneration, pose orientation, target
alignment, octave span and finger uniqueness for all temporal overlaps.
Android checks additionally cover waiting, seeking, missing guides and chords.
Physical-device visual verification has not yet been performed.
