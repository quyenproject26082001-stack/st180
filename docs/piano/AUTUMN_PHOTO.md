# Lãng Quên Chiều Thu — bản cô Mây

Song ID: `autumn_photo`. Home entry: **Lãng Quên Chiều Thu • Bản cô Mây**.
The separate difficult Autumn arrangement is unchanged.

Source: user-provided local video `nA2WweiqOw8` (23:07), plus the melody
in IMG_9192/IMG_9193. Original images remain available in the sheet viewer;
IMG_8648 is a reference variant, not an appended part of this lesson anymore.

Run `python docs/piano/autumn_photo.py` to regenerate both JSON assets.
34 bars, 108.8 seconds at the nominal practice tempo of 75 BPM.
The speed buttons further scale this tempo. Melody ties remain single attacks.

## Video comparison

- Around 10:00: Dm = Re–La–Re–Fa–La.
- Around 10:05: Am = La–Mi–La–Do–Mi.
- Around 10:20: F = Fa–Do–Fa–La–Do.
- Around 10:35: shortened Bb = Sib–Fa–Sib before the change to C.
- Around 14:45: chorus F = Fa–Do–Fa–Do–La–Do.
- Around 15:00: chorus Am = La–Mi–La–Mi–Do–Mi.
- Around 18:00: taught cadence Gm / C → F.

The generated left hand uses these open-position, successive-note patterns,
not the previous simultaneous sustained triads. Verse: bars 1–17; chorus:
18–33; closing F bass/octave voicing: bar 34. Two-chord bars use shortened
root/fifth/octave cells and preserve the melody's chord-change positions.

Fidelity boundary: this is a quantized practice arrangement of the taught
patterns, not a note-for-note MIDI extraction of every demonstration. The
exact release times, pedal, rubato and ending voicing are practice choices;
the standalone embellished third-photo ending is excluded. Right-hand melody
is retained from the first two photo transcriptions and has not been fully
retranscribed from audio. Do not describe this as an exact performance copy.

## Physical piano input

## Virtual hands (Co May only)

The `Tay ảo` button is available even when the main controls are collapsed.
Long-press it for the finger legend and limitations. Its preference is stored
only for `autumn_photo`. Other songs keep their existing rendering.

`co_may_fingering.py` adds `finger` (1 thumb through 5 little finger) and a
five-pitch `pose` to each note. These are **suggested fingerings**, not a
frame-exact transcription of May's fingers. Additional close frames at
10:20.5, 10:21.1 and 10:21.7 show the hand relocating after the low cell;
motion blur and occlusion do not support certifying every finger identity.

The proposed left-hand verse fingering is 5–2–1 / 2–1; chorus is
5–2–1 / 1–2–1, with a lifted relocation at the slash. The final octave
voicing uses 5/2/1. RH uses six explicit F-major phrase positions with a
minimum-movement selection across the melody. This RH selection is algorithmic
and has not been certified by a piano teacher or claimed to match the video.
Every pose spans at most an octave; simultaneous notes use distinct fingers.
Individual hand size may require a different fingering.

The Canvas overlay shares the keyboard's key geometry and leaves touch input
unchanged. It shows all five fingers, colors the target fingertips and eases
the whole pose between positions. Green requires demo playback or an accepted
note whose key is still held. Waiting does not auto-play or award points.
Changing lesson/hand resets the poses; seeking excludes skipped events;
after the last note hands disappear. MIDI still cannot identify real fingers.

Verification: Python checks cover every note-to-finger match, hand orientation,
pose span, chord finger uniqueness and left-hand relocation. JVM tests cover
waiting, seeking/loop bounds, final chords and absent/invalid fingering.
Device visual QA remains required when a phone/emulator is connected.

## Physical piano input

Every FallingPianoActivity lesson has **Đàn thật • Bluetooth / USB**. It supports
standard BLE-MIDI discovery and Android-registered USB-MIDI output ports. Choose
the piano, start the lesson and play. Note-on, note-off, velocity-zero note-off,
all channels, running status, split packets, sustain CC64, channel resets and
disconnect cleanup are handled. Incoming physical presses use the same scoring
and wait-mode engine as touch. The touch-only held-note assistance never
auto-accepts repeated physical notes. External keys highlight on the keyboard.

Bluetooth audio alone is not MIDI. Pianos without BLE-MIDI can use USB-MIDI via
OTG or an appropriate external MIDI interface. Permissions are requested only
when Bluetooth discovery is selected: Nearby devices on Android 12+, location
on Android 6–11. On old Android, location services may also need enabling.
The app does not derive physical location. There is no microphone pitch detection.

**Âm app** defaults off for physical input so the piano's local sound is not
doubled. Enable it for a silent controller; CC64 then sustains app echo.
Automatic accompaniment/demo retain their existing app audio behavior.
The connection is screen-scoped: leaving/backgrounding the lesson closes scans,
ports and the device, cancels pending opens and pauses practice. Reconnect on return.

## Verification

Run the generator and `:app:testDebugUnitTest :app:assembleDebug`.
Verified locally on 2026-11-03: debug APK built successfully, all 18 unit tests
passed (6 MIDI input, 11 lesson engine, 1 pre-existing example).
MIDI unit tests cover byte framing, running status/realtime/system messages,
channel cleanup, sustain state and real-note scoring with repeated attacks.
Compile/tests are not Bluetooth, USB, timing or transcription listening evidence.
No physical piano or Android device has been operated for this change.

Hardware checks: permission grant/denial, Bluetooth disabled, no MIDI device,
connect and play wrong/right notes, play a chord, repeat a held pitch, release,
pedal, unplug/turn off the piano, leave the screen during discovery or connection,
return/reconnect, compare the displayed original sheets with demo playback.

Android API references:
- https://developer.android.com/reference/android/media/midi/package-summary
- https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- https://developer.android.com/reference/android/media/midi/MidiReceiver
