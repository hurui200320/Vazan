package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.MaterialColors
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.MoveToViewModel
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

@AndroidEntryPoint
class MoveToActivity : VazanActivity() {
    private val viewModel: MoveToViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    private fun startScan() {
        viewModel.scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.also {
                    viewModel.processLabel(it) { thread { startScan() } }
                } ?: showToast("Invalid barcode")
            }
            .addOnCanceledListener {
                showToast("Scan canceled")
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Scan failed: ${e.message}")
                finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.targetEntityId = intent.getStringExtra("entity_id") ?: kotlin.run {
            showToast("entity_id not found"); finish(); return
        }
        viewModel.targetType = intent.getStringExtra("target_type") ?: kotlin.run {
            showToast("target_type not found"); finish(); return
        }
        intent.getStringExtra("target_name")?.let { viewModel.targetName = it }

        when (viewModel.targetType) {
            "location", "box" -> {}
            else -> {
                showToast("entry_type invalid: location or box, got ${viewModel.targetType}")
                finish()
                return
            }
        }
        viewModel.scanner = GmsBarcodeScanning.getClient(
            this, GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_CODE_128
                ).build()
        )
        thread { startScan() }
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
                        Text(
                            text = "Move to: ${viewModel.targetType}",
                            fontSize = 25.sp, lineHeight = 25.sp
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        OneLineText(text = "Target: ${viewModel.targetName}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        OneLineText(text = "Target: ${viewModel.targetEntityId}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        Text(
                            text = "Current: ${viewModel.currentLabel}",
                            fontSize = 25.sp, lineHeight = 25.sp
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Text(
                            text = viewModel.status, fontSize = 30.sp, lineHeight = 30.sp,
                            color = when {
                                viewModel.status.startsWith("OK") -> MaterialColors.Green600
                                viewModel.status.startsWith("ERROR") -> MaterialColors.Orange600
                                else -> MaterialColors.Indigo600
                            }
                        )
                    }
                }
            }
        }
    }
}
