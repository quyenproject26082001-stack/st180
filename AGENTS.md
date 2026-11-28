# Hướng dẫn làm việc với ST180

ST180 là app Android Kotlin. Module chính là `app`, package `com.xylophone.learning.music`. Tài liệu về nguồn nhạc và script sinh dữ liệu nằm trong [`docs/piano`](docs/piano).

## Quy ước làm việc

- Chạy `git status --short` trước khi sửa. Giữ các thay đổi đang có của người dùng, kể cả thay đổi cùng file. Tránh đưa cấu hình máy, cache Gradle và file build vào phần thay đổi của task.
- Bám đúng phạm vi yêu cầu. Giữ các luồng Home, điều khiển, dữ liệu đã lưu và asset của các bài khác khi thêm hoặc sửa một bài.
- Mặc định kiểm tra mã nguồn, diff và dữ liệu. Chỉ build Debug, đóng gói APK, cài hoặc chạy trên emulator hay thiết bị khi người dùng yêu cầu. Báo riêng kết quả kiểm tra mã nguồn, test đã chạy và hành vi đã thử trên thiết bị.

## Luồng luyện piano

- [`MainActivity.kt`](app/src/main/java/com/xylophone/learning/music/activity_app/main/MainActivity.kt) nhận thao tác từ Home và truyền `piano_song` vào [`FallingPianoActivity.kt`](app/src/main/java/com/xylophone/learning/music/activity_app/main/FallingPianoActivity.kt). Khi thêm nút Home, cập nhật cả [`layout/activity_home.xml`](app/src/main/res/layout/activity_home.xml) và [`layout-v31/activity_home.xml`](app/src/main/res/layout-v31/activity_home.xml) với cùng ID để View Binding dùng được trên cả hai biến thể.
- `FallingPianoActivity` tải nốt và mốc ô nhịp, quản lý tốc độ, tay tập, chơi mẫu và lặp A–B. [`PianoLesson.kt`](app/src/main/java/com/xylophone/learning/music/activity_app/main/PianoLesson.kt) chấm điểm. [`FallingPianoView.kt`](app/src/main/java/com/xylophone/learning/music/activity_app/main/FallingPianoView.kt) vẽ phím và nốt rơi. Giữ bài luyện tách khỏi luồng `FreePianoActivity`.
- [`PianoMidiInput.kt`](app/src/main/java/com/xylophone/learning/music/activity_app/main/PianoMidiInput.kt) nhận BLE-MIDI và USB-MIDI. Phím trên màn hình và đàn thật đều đi qua `PianoLesson` để chấm điểm. Giữ vòng đời kết nối, trạng thái phím và pedal khi thay đổi luồng này.

## Thêm một bản nhạc

1. Xác định đúng bản phối, giọng, tempo và nguồn sheet, MIDI hoặc video. Ghi nguồn cạnh script tạo dữ liệu và ghi rõ các chỗ chép tay, đổi nhịp hoặc giản lược cần thiết cho bài tập.
2. Dùng ID bài riêng và tên `SharedPreferences` riêng cho điểm A–B. Nối ID với tiêu đề, dữ liệu nốt và timeline trong `FallingPianoActivity`. Giữ nguyên ID và tên asset của các bài hiện có.
3. Lưu dữ liệu trong [`app/src/main/assets`](app/src/main/assets). Mỗi nốt có `midi`, `start`, `end` và `hand`. `start` và `end` tính bằng mili giây, `hand` bằng `0` cho tay phải và `1` cho tay trái. Timeline có `bar` và `startMs` theo thứ tự phát. Dùng mốc khớp bản chép để tua và lặp A–B chọn đúng đoạn.
4. Kiểm tra thời lượng nốt dương, mốc tăng dần và đủ file `piano_keys/<midi>.wav` cho dải mà Activity nạp. Với bài hai tay, kiểm tra dữ liệu của từng tay. Kiểm tra chế độ chờ đúng nốt, theo nhịp, chơi mẫu và tua vào giữa bài khi phạm vi sửa liên quan.

### Các bản dễ nhầm

- **Lãng Quên Chiều Thu • Bản nâng cao** dùng ID `autumn`, dữ liệu `autumn_notes.json` và `lang_quen_chieu_thu_timeline.json`. [`score.py`](docs/piano/score.py) chép hai trang nhạc từ PDF Trường Ca ghi tên Minh Khang. PDF nguồn nằm ở `app/src/main/assets/lang_quen_chieu_thu_practice.pdf`. Bản cô Mây và bản Synthesia dùng dữ liệu riêng. [Bản cô Mây](docs/piano/AUTUMN_PHOTO.md) · [Synthesia cô Mây](docs/piano/CO_MAY_SYNTHESIA.md)
- **Lãng Quên Chiều Thu • Bản phối Minh Khang × Synthesia cô Mây** dùng ID `autumn_co_may_mix`. Chạy `python3 docs/piano/autumn_co_may_mix.py` để tạo hai JSON cho bản phối từ dữ liệu hai bản nguồn. Điểm **Chạy đà cô Mây** nằm giữa ô 30 và 31, dẫn từ Đô trầm lên Đô8 rồi trở lại điệp khúc Minh Khang. Bản phối có điểm A–B riêng. [Cách ghép bản phối](docs/piano/autumn_co_may_mix.py)
- **Instant Eternity • Bản trung cấp** dùng ID `instant_eternity`, giọng Si trưởng ở 80 BPM. [`instant_eternity.py`](docs/piano/instant_eternity.py) chép bản đủ hai tay của coomt và sinh `instant_eternity_notes.json` cùng `instant_eternity_timeline.json`. Bản 82 BPM đã tham khảo là một bản chép khác. Mục xem sheet trong bài mở trang nguồn qua trình duyệt. [Sheet của coomt](https://www.qinpu123.com/qiyue/gangqin/580794.html)
- **Instant Eternity • Bản dễ** dùng ID `instant_eternity_easy`. [`instant_eternity_easy.py`](docs/piano/instant_eternity_easy.py) chuyển bản chép của coomt sang Đô trưởng ở 68 BPM, giữ một bè giai điệu và tối giản tay trái. Bản này có asset `instant_eternity_easy_notes.json` và `instant_eternity_easy_timeline.json` cùng điểm A–B riêng.

## Lệnh kiểm tra

Chạy từ gốc repo. Chỉ sinh lại asset của bài đang sửa. So sánh diff của dữ liệu sinh ra trước khi kết thúc.

```sh
git diff --check
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s docs/piano -p 'test_*.py'
```

Khi cần tái tạo dữ liệu Instant Eternity, chạy `python3 docs/piano/instant_eternity.py` cho bản trung cấp hoặc `python3 -B docs/piano/instant_eternity_easy.py` cho bản dễ. Các test Kotlin hiện có nằm trong [`app/src/test`](app/src/test).
