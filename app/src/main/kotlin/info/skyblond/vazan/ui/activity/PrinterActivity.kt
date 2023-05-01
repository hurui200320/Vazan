package info.skyblond.vazan.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.domain.PaperSize
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.PrinterViewModel

@AndroidEntryPoint
class PrinterActivity : VazanActivity() {
    private val viewModel: PrinterViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = mutableMapOf<String, String>().apply {
        put(Manifest.permission.BLUETOOTH, "communicating with printer")
        put(Manifest.permission.BLUETOOTH_ADMIN, "enabling bluetooth automatically")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            put(Manifest.permission.BLUETOOTH_SCAN, "communicating with printer")
            put(Manifest.permission.BLUETOOTH_CONNECT, "communicating with printer")
        }
    }

    private fun cancel(str: String) {
        showToast(str)
        setResult(RESULT_CANCELED)
        finish()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
                ?: kotlin.run {
                    cancel("Bluetooth adapter not found")
                    return
                }
        viewModel.label = intent.getStringExtra("label") ?: kotlin.run {
            cancel("Invalid label")
            return
        }
        viewModel.showToast = { showToast(it) }
        viewModel.loadLastPrinterParam()
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card {
                            Image(
                                bitmap = viewModel.getPaperSize().generatePreview(viewModel.label)
                                    .asImageBitmap(),
                                contentDescription = "label preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.3f)
                                    .padding(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                        Text(text = "Label: ${viewModel.label}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        OneLineText(text = "Printer Address: ${viewModel.printerAddress}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = {
                                val devices = viewModel.bluetoothAdapter.bondedDevices.toList()
                                AlertDialog.Builder(this@PrinterActivity)
                                    .setTitle("Select printer")
                                    .setSingleChoiceItems(
                                        devices.map { "${it.address} (${it.name})" }
                                            .toTypedArray(), 0
                                    ) { dialog: DialogInterface, choice: Int ->
                                        dialog.dismiss()
                                        viewModel.printerAddress = devices[choice].address
                                    }.create().show()
                            }) {
                                Text(text = "Select")
                            }
                            Button(onClick = { // call system setting to pair
                                startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                            }) { Text(text = "Pair") }
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Text(
                            text = "Paper: ${viewModel.getPaperSize().let { it.displayName + ", gap " + it.gap + " mm" }}",
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable {
                                    AlertDialog
                                        .Builder(this@PrinterActivity)
                                        .setTitle("Select paper size")
                                        .setSingleChoiceItems(
                                            PaperSize.values()
                                                .map { "${it.displayName}, gap ${it.gap} mm" }
                                                .toTypedArray(), viewModel.paperSelection
                                        ) { dialog: DialogInterface, choice: Int ->
                                            dialog.dismiss()
                                            viewModel.paperSelection = choice
                                        }
                                        .create()
                                        .show()
                                }
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { viewModel.repeat-- },
                                enabled = viewModel.repeat > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = "Decrease"
                                )
                            }
                            Text(text = "Repeat: ${viewModel.repeat}")
                            IconButton(
                                onClick = { viewModel.repeat++ },
                                enabled = viewModel.repeat < 100
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Increase"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Button(
                            onClick = {
                                viewModel.printing = true
                                viewModel.print{
                                    setResult(RESULT_OK, Intent().apply {
                                        putExtra("label", viewModel.label)
                                    })
                                }
                            },
                            enabled = viewModel.printerAddress.length == 17 && !viewModel.printing
                        ) {
                            Text(text = "Print")
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                    }
                }
            }
        }
    }
}
