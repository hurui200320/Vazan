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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import info.skyblond.paperang.PaperangP2
import info.skyblond.vazan.ui.theme.VazanTheme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.random.Random

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

                        Button(
                            onClick = { printSomething() }
                        ) {
                            Text(text = "print")
                        }
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
                    .setNeutralButton("Fine") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
                    .show()
                finish()
            }
        }

    // TODO: input?
    private val printerAddressStats = mutableStateOf("00:15:83:D1:24:11")

    private var p2Cache: PaperangP2? = null

    @SuppressLint("MissingPermission")
    @Synchronized
    fun getP2Instance(): PaperangP2 {
        if (p2Cache != null && p2Cache!!.isConnected()) return p2Cache!!
        if (p2Cache != null) { // cached, but closed
            p2Cache!!.close()
            p2Cache = null
        } // create a new one
        ensureBluetoothOpen()
        val btDevice = bluetoothAdapter!!.getRemoteDevice(printerAddressStats.value)
        bluetoothAdapter!!.cancelDiscovery()
        p2Cache = PaperangP2(btDevice)
        return p2Cache!!
    }

    private fun printSomething() {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        dialog.show()
        thread {
            val p2 = getP2Instance()
            p2.setPaperType()
            p2.setHeatDensity(100u)
            // feed space
            p2.feedToHeadLine(20)
            p2.sendPrintData(
                (0..16)
                    .map { Random.nextBytes(1008) }
                    .flatMap { it.toList() }.toByteArray()
            )
            // feed space
            p2.feedSpaceLine(250)
            Thread.sleep(5_000)
            dialog.dismiss()
        }
    }

    private fun ensureBluetoothOpen() {
        if (bluetoothAdapter == null) {
            AlertDialog.Builder(this)
                .setTitle("No Bluetooth adapter")
                .setMessage("This device has no bluetooth adapter, thus no way to communicate with printer.")
                .setCancelable(false)
                .setNeutralButton("Fine") { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    finish()
                }
                .create()
                .show()
            finish()
        }

        while (!bluetoothAdapter!!.isEnabled) {
            if (pendingEnableBTFlag.get()) continue // waiting for current result
            // check permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (pendingPermission()) continue
                ensurePermissions(listOf(Manifest.permission.BLUETOOTH_ADMIN))
                continue // try next loop
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            pendingEnableBTFlag.set(true)
        }
    }
}