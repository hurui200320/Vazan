package info.skyblond.vazan.ui.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.domain.PaperSize
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val configRepo: ConfigRepository
) : ViewModel() {
    private val sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var label by mutableStateOf("")
    var printerAddress by mutableStateOf("none")
    var paperSelection by mutableStateOf(0)

    var repeat by mutableStateOf(1)
    var printing by mutableStateOf(false)

    lateinit var bluetoothAdapter: BluetoothAdapter

    lateinit var showToast: (String) -> Unit

    fun loadLastPrinterParam() = viewModelScope.launch {
        val addr = configRepo.getConfigByKey(SettingsKey.APP_LAST_PRINTER_ADDRESS.key)?.value
        if (addr != null) printerAddress = addr

        val paper =
            configRepo.getConfigByKey(SettingsKey.APP_LAST_PRINTER_PAPER.key)?.value?.toIntOrNull()
        if (paper != null) paperSelection = paper.coerceIn(0, PaperSize.values().size - 1)
    }

    fun getPaperSize() = PaperSize.values()[paperSelection]

    @SuppressLint("MissingPermission")
    fun print(onSuccess: () -> Unit) {
        if (!bluetoothAdapter.isEnabled) {
            viewModelScope.launch { showToast("Bluetooth is not enabled") }
            printing = false
        } else {
            viewModelScope.launch {
                try {
                    val data = getPaperSize().generatePrintData(label, repeat)
                    val btDevice = bluetoothAdapter.getRemoteDevice(printerAddress)
                    bluetoothAdapter.cancelDiscovery()
                    btDevice.createRfcommSocketToServiceRecord(sppUUID).use { socket ->
                        socket.connect()
                        socket.outputStream.write(data)
                        socket.outputStream.flush()
                        delay(2000)
                        loop@ while (true) {
                            // query status
                            socket.outputStream.write(byteArrayOf(126, 33, 84, 13, 10))
                            socket.outputStream.flush()
                            delay(1000)
                            var b: Int
                            do {
                                b = socket.inputStream.read()
                                if (b != -1 && b and 0x20 == 0) {
                                    // bit 5 is 0 -> print finished
                                    break@loop
                                }
                                delay(500)
                            } while (b == -1)
                        }
                        socket.close()
                        showToast("Print success")
                        configRepo.insertOrUpdateConfig(
                            Config(
                                SettingsKey.APP_LAST_PRINTER_ADDRESS.key, printerAddress
                            )
                        )
                        configRepo.insertOrUpdateConfig(
                            Config(
                                SettingsKey.APP_LAST_PRINTER_PAPER.key, paperSelection.toString()
                            )
                        )
                        onSuccess()
                    }
                } catch (t: Throwable) {
                    viewModelScope.launch { showToast("Print failed: ${t.message}") }
                } finally {
                    printing = false
                }
            }
        }
    }
}