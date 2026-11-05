package com.xylophone.learning.music.activity_app.main

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
class PianoMidiInput(
    private val activity: AppCompatActivity,
    private val onStatus: (String, Boolean) -> Unit,
    private val onMessage: (Int, Int, Int) -> Unit,
    private val onDisconnected: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val manager = activity.getSystemService(MidiManager::class.java)
    private var device: MidiDevice? = null
    private var port: MidiOutputPort? = null
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var dialog: AlertDialog? = null
    private var generation = 0
    private var active = true
    private var disposed = false
    private val permissions = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (active && !disposed) {
            if (result.values.all { it }) showBluetooth()
            else onStatus("Chưa cấp quyền tìm đàn. Bấm kết nối để thử lại hoặc cấp quyền trong Cài đặt.", device != null)
        }
    }
    private val enableBluetooth = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (active && !disposed) {
            if (activity.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true) showBluetooth()
            else onStatus("Bluetooth chưa bật", device != null)
        }
    }
    private val enableLocation = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (active && !disposed) requestBluetooth()
    }
    private val deviceCallback = object : MidiManager.DeviceCallback() {
        override fun onDeviceRemoved(info: MidiDeviceInfo) {
            if (device?.info?.id == info.id) disconnect("Đàn đã ngắt kết nối • Bài học tạm dừng")
        }
    }

    init {
        manager?.registerDeviceCallback(deviceCallback, handler)
    }

    fun start() { if (!disposed) active = true }

    fun showChooser() {
        if (!active || disposed) return
        if (manager == null || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            onStatus("Thiết bị Android này không hỗ trợ MIDI", false)
            return
        }
        val options = arrayOf("Tìm đàn Bluetooth MIDI", "Đàn USB / MIDI đã kết nối", "Ngắt kết nối", "Hướng dẫn đàn thật")
        dialog?.dismiss()
        dialog = AlertDialog.Builder(activity).setTitle("Kết nối đàn thật")
            .setItems(options) { _, index ->
                when (index) {
                    0 -> requestBluetooth()
                    1 -> showRegisteredDevices()
                    2 -> disconnect("Đã ngắt đàn • Có thể tiếp tục bằng phím cảm ứng")
                    3 -> showHelp()
                }
            }.setNegativeButton("Đóng", null).show()
    }

    private fun showHelp() {
        dialog = AlertDialog.Builder(activity).setTitle("Học bằng đàn thật")
            .setMessage("1. Bật chế độ Bluetooth MIDI trên đàn, rồi chọn Tìm đàn Bluetooth MIDI. Bluetooth chỉ phát nhạc/audio không truyền được phím đàn.\n\n2. Nếu không có Bluetooth MIDI: nối cổng USB-MIDI của đàn qua USB OTG, rồi chọn Đàn USB / MIDI đã kết nối.\n\n3. Chọn tay và Chờ đánh đúng, bấm Bắt đầu rồi đánh phím trên đàn. Nốt sai không cho bài chạy tiếp. Nhấc và đánh lại mỗi nốt lặp.\n\nÂm đàn thật phát từ đàn; Âm app mặc định tắt để tránh vang đôi. Pedal CC64 được nhận nếu bật Âm app. Android 6–11 có thể cần bật dịch vụ Vị trí để quét BLE.\n\nKết nối chỉ dùng trong màn học và ngắt khi rời màn. Chưa có nhận diện âm thanh qua micro.")
            .setPositiveButton("Đã hiểu", null).show()
    }

    private fun requestBluetooth() {
        val required = if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val missing = required.filter { ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) showBluetooth() else permissions.launch(missing.toTypedArray())
    }

    private fun showBluetooth() {
        if (!active || disposed) return
        val adapter = activity.getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            onStatus("Điện thoại không hỗ trợ Bluetooth LE • Hãy dùng USB-MIDI", device != null)
            return
        }
        try {
            if (!adapter.isEnabled) {
                enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                return
            }
            // Android 7–11 requires location services as well as the runtime permission for BLE scans.
            if (Build.VERSION.SDK_INT < 31) {
                val location = activity.getSystemService(LocationManager::class.java)
                val enabled = location != null && (location.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    location.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                if (!enabled) {
                    dialog = AlertDialog.Builder(activity).setTitle("Bật Vị trí để tìm đàn")
                        .setMessage("Android 7–11 cần bật dịch vụ Vị trí để quét Bluetooth MIDI. Ứng dụng không đọc tọa độ của bạn.")
                        .setPositiveButton("Mở cài đặt") { _, _ -> enableLocation.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                        .setNegativeButton("Đóng", null).show()
                    return
                }
            }
            stopScan()
            val devices = mutableListOf<BluetoothDevice>()
            val labels = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1)
            val list = ListView(activity).apply { this.adapter = labels }
            dialog?.dismiss()
            val scanDialog = AlertDialog.Builder(activity).setTitle("Đang tìm đàn Bluetooth MIDI…")
                .setView(list).setNegativeButton("Đóng", null)
                .setNeutralButton("Tìm lại") { _, _ -> requestBluetooth() }
                .setPositiveButton("Hướng dẫn") { _, _ -> showHelp() }.create()
            dialog = scanDialog
            scanDialog.setOnDismissListener { stopScan() }
            list.setOnItemClickListener { _, _, index, _ ->
                val selected = devices[index]
                scanDialog.dismiss()
                connectBluetooth(selected)
            }
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    handler.post {
                        if (!active || scanCallback !== this || !scanDialog.isShowing) return@post
                        if (devices.none { it.address == result.device.address }) {
                            devices.add(result.device)
                            labels.add("${result.scanRecord?.deviceName ?: result.device.name ?: "Đàn MIDI"}\n${result.device.address}")
                        }
                    }
                }
                override fun onScanFailed(errorCode: Int) {
                    handler.post {
                        if (scanCallback !== this) return@post
                        stopScan()
                        scanDialog.setTitle("Không quét được MIDI (mã $errorCode) • Thử lại")
                    }
                }
            }
            scanner = adapter.bluetoothLeScanner
            if (scanner == null) { onStatus("Bluetooth chưa sẵn sàng • Hãy thử lại", device != null); return }
            scanCallback = callback
            scanDialog.show()
            scanner?.startScan(listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")).build()),
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), callback)
            handler.postDelayed({
                if (scanCallback === callback) {
                    stopScan()
                    if (scanDialog.isShowing) scanDialog.setTitle(if (devices.isEmpty()) "Không thấy đàn • Bật MIDI trên đàn rồi thử lại" else "Chọn đàn Bluetooth MIDI")
                }
            }, 12000)
        } catch (error: Exception) {
            stopScan()
            dialog?.dismiss()
            onStatus("Không quét được Bluetooth: ${error.localizedMessage}", device != null)
        }
    }

    private fun showRegisteredDevices() {
        val devices = manager?.devices.orEmpty().filter { it.outputPortCount > 0 && it.type != MidiDeviceInfo.TYPE_VIRTUAL }
        val choices = devices.flatMap { info -> info.ports.filter { it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT }.map { info to it } }
        if (choices.isEmpty()) {
            onStatus("Chưa thấy đàn MIDI • Nối USB OTG hoặc tìm Bluetooth MIDI trước", device != null)
            return
        }
        dialog = AlertDialog.Builder(activity).setTitle("Chọn cổng nhận phím đàn")
            .setItems(choices.map { (info, output) -> "${name(info)} • ${output.name.ifEmpty { "Cổng ${output.portNumber + 1}" }}" }.toTypedArray()) { _, index ->
                val (info, output) = choices[index]
                open(name(info), output.portNumber) { listener -> manager?.openDevice(info, listener, handler) }
            }.setNegativeButton("Đóng", null).show()
    }

    private fun connectBluetooth(selected: BluetoothDevice) {
        val label = runCatching { selected.name }.getOrNull() ?: "Đàn Bluetooth MIDI"
        open(label, null) { listener -> manager?.openBluetoothDevice(selected, listener, handler) }
    }

    private fun name(info: MidiDeviceInfo) = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Đàn MIDI ${info.id}"

    private fun open(label: String, portNumber: Int?, action: (MidiManager.OnDeviceOpenedListener) -> Unit) {
        disconnect("Đang kết nối $label…")
        val request = generation
        handler.postDelayed({
            if (active && request == generation && device == null) disconnect("Kết nối quá thời gian • Kiểm tra đàn rồi thử lại")
        }, 15000)
        try {
            action(MidiManager.OnDeviceOpenedListener { opened ->
                if (!active || disposed || generation != request) { runCatching { opened?.close() }; return@OnDeviceOpenedListener }
                if (opened == null) { disconnect("Không mở được đàn • Kiểm tra MIDI trên đàn rồi thử lại"); return@OnDeviceOpenedListener }
                device = opened
                try {
                    val outputNumber = portNumber ?: opened.info.ports.firstOrNull { it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT }?.portNumber
                    val output = outputNumber?.let { opened.openOutputPort(it) }
                    if (output == null) { disconnect("Đàn không có cổng MIDI gửi nốt khả dụng"); return@OnDeviceOpenedListener }
                    port = output
                    val parser = MidiStreamParser { status, data, value ->
                        handler.post { if (active && generation == request) onMessage(status, data, value) }
                    }
                    output.connect(object : MidiReceiver() {
                        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
                            synchronized(parser) { parser.receive(msg, offset, count) }
                        }
                    })
                    onStatus("Đã kết nối ${name(opened.info)} • Đánh phím để học", true)
                } catch (error: Exception) {
                    disconnect("Không nhận được MIDI: ${error.localizedMessage}")
                }
            })
        } catch (error: Exception) {
            disconnect("Kết nối thất bại: ${error.localizedMessage}")
        }
    }

    private fun stopScan() {
        val callback = scanCallback
        scanCallback = null
        if (callback != null) runCatching { scanner?.stopScan(callback) }
        scanner = null
    }

    fun disconnect(message: String = "Đàn thật: chưa kết nối") {
        generation++
        stopScan()
        val oldPort = port
        val oldDevice = device
        port = null
        device = null
        runCatching { oldPort?.close() }
        runCatching { oldDevice?.close() }
        onDisconnected()
        onStatus(message, false)
    }

    fun stop() {
        active = false
        dialog?.dismiss()
        dialog = null
        disconnect()
        handler.removeCallbacksAndMessages(null)
    }

    fun close() {
        stop()
        disposed = true
        manager?.unregisterDeviceCallback(deviceCallback)
    }
}
