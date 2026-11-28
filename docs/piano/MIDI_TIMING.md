# Đo thời gian xử lý MIDI

Bản debuggable ghi kết quả theo nhóm 32 nốt hoặc khi ngắt đàn với tag `PianoMidiTiming`. Các mốc dùng đồng hồ đơn điệu.

- `main_queue` là từ callback MIDI đến khi app bắt đầu xử lý trên main thread.
- `event_age` chỉ tính khi gói có timestamp hợp lệ và cách callback tối đa một giây. Đây là tuổi sự kiện được báo bởi nguồn MIDI.
- `audio_request` kết thúc khi app đã gọi hàm phát sample. Bật **Âm app** để đo. Độ trễ đến lúc loa phát âm cần phép đo phần cứng riêng.
- `score` kết thúc sau khi hàm chấm điểm trả về. Bắt đầu bài và đợi hết đếm ngược để đo.

Dùng cùng một chuỗi nốt, tốc độ, bài và thiết bị. So sánh USB với BLE và thu riêng từng lượt trong màn chờ và lúc nốt đang rơi. Ghi lại số mẫu, p50, p95, chế độ và loại kết nối.
