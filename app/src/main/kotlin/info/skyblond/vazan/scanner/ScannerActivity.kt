package info.skyblond.vazan.scanner

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import info.skyblond.vazan.VazanActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ScannerActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = mapOf(
        Manifest.permission.CAMERA to "scanning barcodes"
    )

    private val resultCounter = ResultCounter()

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private val barcodeAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC,
                        Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_DATA_MATRIX
                    )
                    .build()
            )
            scanner.process(image)
                .addOnCompleteListener { imageProxy.close() }
                .addOnSuccessListener { barcodes ->
                    if(resultCounter.countResult(barcodes) >= 10) {
                        val intent = resultCounter.getResult(8)
                        setResult(RESULT_OK, intent)
                        finish()
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
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(this) }
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                            val executor = ContextCompat.getMainExecutor(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val cameraSelector = CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                    .build()

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .apply {
                                        setAnalyzer(executor, barcodeAnalyzer)
                                    }

                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    imageAnalysis,
                                    preview
                                )
                                if (camera.cameraInfo.hasFlashUnit())
                                    camera.cameraControl.enableTorch(true)
                            }, executor)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
