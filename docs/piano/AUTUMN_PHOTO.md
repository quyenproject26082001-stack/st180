# Lãng Quên Chiều Thu — version from three photos

Home entry: **Lãng Quên Chiều Thu • Bản sheet 3 ảnh**. Song ID and independent
practice preferences: `autumn_photo` / `autumn_photo_piano_practice`.
Existing Autumn and Nơi này arrangement assets are unchanged; their falling-note
screens also gain the shared MIDI input controls.

Sources bundled unchanged: `assets/autumn_photo/IMG_9192.jpg`, `IMG_9193.jpg`,
`IMG_8648.jpg`. The lesson opens the original images without an external PDF app.

`python3 docs/piano/autumn_photo.py` regenerates the two lesson JSON files.
Manual melody transcription; key signature B-flat, 4/4. Default practice tempo
is quarter = 75 BPM because no tempo is printed. The existing 50/75/100% speed
control scales this tempo. There are 45 practice measures and 142.4 seconds
before the playback tail. Source order is 9192 → 9193 → 8648 (embellished ending).
This join is a practice arrangement, not a verified original performance order.
The last photo's two-beat pickup is numbered as practice bar 35.

Important fidelity limits: the source mainly provides melody and handwritten
chord names, not a complete two-staff piano arrangement. The left-hand sustained
chords are suggested realizations of those symbols. Small grace notes in the
last photo and its lower ending voicings are simplified; the original images
remain the reference. Ties extend previous notes rather than creating attacks.
Listening comparison and correction against the user's playing remain necessary.

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
