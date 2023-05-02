package info.skyblond.vazan.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.theme.VazanTheme

@AndroidEntryPoint
class ScannerActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = buildMap {
        put(Manifest.permission.CAMERA, "scanning barcode")
    }

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_128, Barcode.FORMAT_DATA_MATRIX)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setup camera controller
        val cameraController = LifecycleCameraController(this)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraController.isTapToFocusEnabled = true
        cameraController.isPinchToZoomEnabled = true

        // setup camera preview
        val previewView = PreviewView(this)
        previewView.controller = cameraController
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

        // setup the scanner
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result ->
                val centerX = previewView.width / 2
                val centerY = previewView.height / 2
                // select the center one
                val list = result.getValue(barcodeScanner)?.sortedBy {
                    val dx = centerX - (it.boundingBox?.centerX() ?: 0)
                    val dy = centerY - (it.boundingBox?.centerY() ?: 0)
                    dx * dx + dy * dy
                } ?: emptyList()
                val barcode = list.firstNotNullOfOrNull { it.rawValue }
                if (barcode != null) {
                    setResult(RESULT_OK, Intent().apply { putExtra("barcode", barcode) })
                    finish()
                }
            }
        )

        // render
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        factory = { previewView }, modifier = Modifier.fillMaxSize(),
                    )
                    var torchStatus by remember { mutableStateOf(false) }
                    var fillStatus by remember { mutableStateOf(true) }

                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (torchStatus) {
                                IconButton(onClick = {
                                    cameraController.enableTorch(false)
                                    torchStatus = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "turn off torch"
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    cameraController.enableTorch(true)
                                    torchStatus = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOff,
                                        contentDescription = "turn on torch"
                                    )
                                }
                            }
                            if (fillStatus) {
                                IconButton(onClick = {
                                    previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
                                    fillStatus = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomInMap,
                                        contentDescription = "switch to fill"
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                                    fillStatus = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomOutMap,
                                        contentDescription = "switch to fit"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
