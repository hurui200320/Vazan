package info.skyblond.vazan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.pdf417.encoder.Dimensions
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import info.skyblond.paperang.PaperangP2
import info.skyblond.vazan.database.VazanDatabase
import info.skyblond.vazan.scanner.ScannerActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class PrintLabelActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = mutableMapOf<String, String>().apply {
        put(Manifest.permission.BLUETOOTH, "communicating with printer")
        put(Manifest.permission.BLUETOOTH_ADMIN, "enabling bluetooth automatically")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            put(Manifest.permission.BLUETOOTH_CONNECT, "communicating with printer")
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun parsePrinterAddress(str: String): String? {
        val rawMac = if (str.contains("deviceId=")) str.split("deviceId=")[1] else str
        val mac = if (rawMac.length == 12) rawMac.chunked(2).joinToString(":") else rawMac
        return if (mac.length == 17) mac else null
    }

    private val scanPrinterAddressLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val mac = result.data!!.getBarcodes()
                    .map { String(it.second, Charsets.ISO_8859_1) }
                    .firstNotNullOfOrNull { parsePrinterAddress(it) }
                if (mac == null) {
                    showToast("Invalid barcode")
                } else {
                    printerAddressState.value = mac
                }
            }
        }

    private val printerAddressState = mutableStateOf("None")

    @SuppressLint("MissingPermission")
    @Composable
    private fun PrinterAddress() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            OneLineText(text = "Printer Address: ${printerAddressState.value}")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {// scan or select
                Button(onClick = {
                    scanPrinterAddressLauncher.launch(
                        Intent(this@PrintLabelActivity, ScannerActivity::class.java)
                    )
                }) { Text(text = "Scan") }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = {
                    val devices = bluetoothAdapter!!.bondedDevices.toList()
                    AlertDialog.Builder(this@PrintLabelActivity)
                        .setTitle("Select printer")
                        .setSingleChoiceItems(
                            devices.map { "${it.address} (${it.name})" }
                                .toTypedArray(), 0
                        ) { dialog: DialogInterface, choice: Int ->
                            dialog.dismiss()
                            printerAddressState.value = devices[choice].address
                        }.create().show()
                }) { Text(text = "Select") }
            }
        }
    }

    private val scanPrintedUUIDLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uuid = result.data!!.getBarcodes()
                    .map { String(it.second, Charsets.ISO_8859_1) }
                    .onEach { println("Result: $it") }
                    .firstNotNullOfOrNull {
                        try {
                            UUID.fromString(it)
                        } catch (_: Throwable) {
                            null
                        }
                    }
                if (uuid == null) {
                    showToast("Invalid UUID")
                } else {
                    uuidToPrintStates.value = uuid
                }
            }
        }

    private fun generateUUID(): UUID {
        while (true) {
            val uuid = UUID.randomUUID()
            // ensure no conflict
            if (VazanDatabase.useDatabase {
                    it.noteDao().countNotesByUUID(uuid)
                } == 0) return uuid
        }
    }

    private val uuidToPrintStates = mutableStateOf(generateUUID())

    @SuppressLint("MissingPermission")
    @Composable
    private fun UUIDToPrint() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            UUIDText(uuid = uuidToPrintStates.value)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {// regen or scan
                Button(onClick = {
                    uuidToPrintStates.value = generateUUID()
                }) { Text(text = "Random") }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = {
                    scanPrintedUUIDLauncher.launch(
                        Intent(
                            this@PrintLabelActivity,
                            ScannerActivity::class.java
                        )
                    )
                }) { Text(text = "Scan") }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = {
                    val input = createSingleLineEditText()
                    AlertDialog.Builder(this@PrintLabelActivity)
                        .setTitle("Input UUID manually")
                        .setView(input)
                        .setCancelable(false) // prevent miss touch
                        .setPositiveButton("OK") { _, _ ->
                            val str = input.text.toString()
                            try {
                                uuidToPrintStates.value = UUID.fromString(str)
                            } catch (_: Throwable) {
                                showToast("Invalid UUID")
                            }
                        }
                        .setCancelButton()
                        .create().show()
                }) { Text(text = "Manual") }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun PrintButtons() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            OneLineText(text = "Print barcode")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Button(
                    onClick = { printQR() },
                    enabled = printerAddressState.value.length == 17
                ) {
                    Text(text = "QR code")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    onClick = { printAztec() },
                    enabled = printerAddressState.value.length == 17
                ) {
                    Text(text = "Aztec")
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Button(
                    onClick = { printPDF417() },
                    enabled = printerAddressState.value.length == 17
                ) {
                    Text(text = "PDF417")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    onClick = { printDataMatrix() },
                    enabled = printerAddressState.value.length == 17
                ) {
                    Text(text = "Data Matrix")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // printer address
                        PrinterAddress()
                        Spacer(modifier = Modifier.height(30.dp))
                        // uuid to print
                        UUIDToPrint()
                        Spacer(modifier = Modifier.height(30.dp))
                        // print buttons
                        PrintButtons()
                    }
                }
            }
        }
    }

    private val pendingEnableBTFlag = AtomicBoolean(false)

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pendingEnableBTFlag.set(false)
            if (result.resultCode != Activity.RESULT_OK) {
                AlertDialog.Builder(this)
                    .setTitle("Failed to enable Bluetooth")
                    .setMessage("You have to enable the bluetooth so that the app can communicate with the printer.")
                    .setCancelable(false)
                    .setNeutralButton("Fine") { _: DialogInterface, _: Int ->
                        finish()
                    }
                    .create()
                    .show()
                finish()
            }
        }


    private var p2Cache: PaperangP2? = null

    @SuppressLint("MissingPermission")
    @Synchronized
    fun getP2Instance(): PaperangP2 {
        bluetoothAdapter!!.cancelDiscovery()
        if (p2Cache != null && p2Cache!!.isConnected()) return p2Cache!!
        if (p2Cache != null) { // cached, but closed
            p2Cache!!.close()
            p2Cache = null
        } // create a new one
        ensureBluetoothOpen()
        val btDevice = bluetoothAdapter!!.getRemoteDevice(printerAddressState.value)
        bluetoothAdapter!!.cancelDiscovery()
        p2Cache = PaperangP2(btDevice)
        return p2Cache!!
    }

    private fun printBitMatrix(bitMatrix: BitMatrix) {
        uuidToPrintStates.value.toPrintableByteArrays()
        val dialog = AlertDialog.Builder(this)
            .setMessage("Printing...")
            .setCancelable(false)
            .create()
        dialog.show()
        thread {
            try {
                val p2 = getP2Instance()
                p2.setPaperType()
                p2.setHeatDensity(100u)
                p2.feedToHeadLine(30)
                p2.sendPrintData(bitMatrix.toByteArrays())
                p2.feedSpaceLine(30)
                p2.sendPrintData(uuidToPrintStates.value.toPrintableByteArrays())
                p2.feedSpaceLine(350)
                Thread.sleep(5_000)
                runOnUiThread {
                    showToast("Print succeed")
                }
            } catch (t: Throwable) {
                runOnUiThread {
                    showToast("Failed to print")
                }
                Log.e(this::class.java.canonicalName, t.message ?: "???")
            } finally {
                dialog.dismiss()
            }
        }
    }

    private fun printQR() {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            uuidToPrintStates.value.toString(), BarcodeFormat.QR_CODE,
            PaperangP2.PRINT_BIT_PER_LINE, PaperangP2.PRINT_BIT_PER_LINE,
            mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H
            )
        )
        printBitMatrix(bitMatrix)
    }

    private fun printAztec() {
        val writer = AztecWriter()
        val bitMatrix = writer.encode(
            uuidToPrintStates.value.toString(), BarcodeFormat.AZTEC,
            PaperangP2.PRINT_BIT_PER_LINE, PaperangP2.PRINT_BIT_PER_LINE,
            mapOf(
                EncodeHintType.ERROR_CORRECTION to "40"
            )
        )
        printBitMatrix(bitMatrix)
    }

    private fun printPDF417() {
        val writer = PDF417Writer()
        val bitMatrix = writer.encode(
            uuidToPrintStates.value.toString(), BarcodeFormat.PDF_417,
            PaperangP2.PRINT_BIT_PER_LINE, Int.MAX_VALUE,
            mapOf(
                EncodeHintType.MARGIN to "0",
                EncodeHintType.ERROR_CORRECTION to 5,
                EncodeHintType.PDF417_DIMENSIONS to Dimensions(2, 4, 2, 35)
            )
        )
        printBitMatrix(bitMatrix)
    }

    private fun printDataMatrix() {
        val writer = DataMatrixWriter()
        val bitMatrix = writer.encode(
            uuidToPrintStates.value.toString(), BarcodeFormat.DATA_MATRIX,
            PaperangP2.PRINT_BIT_PER_LINE, PaperangP2.PRINT_BIT_PER_LINE
        )
        printBitMatrix(bitMatrix)
    }

    /**
     * When return, the bluetooth is enabled.
     * */
    private fun ensureBluetoothOpen() {
        if (bluetoothAdapter == null) {
            AlertDialog.Builder(this)
                .setTitle("No Bluetooth adapter")
                .setMessage("This device has no bluetooth adapter, thus no way to communicate with printer.")
                .setCancelable(false)
                .setNeutralButton("Fine") { dialog: DialogInterface, _: Int ->
                    finish()
                    dialog.dismiss()
                }
                .create()
                .show()
            finish()
        }

        while (!bluetoothAdapter!!.isEnabled) {
            if (pendingEnableBTFlag.get()) continue // waiting for current result
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            pendingEnableBTFlag.set(true)
        }
    }
}
