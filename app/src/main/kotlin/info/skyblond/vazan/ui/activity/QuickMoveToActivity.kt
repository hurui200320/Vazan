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
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.MaterialColors
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.QuickMoveToViewModel
import kotlin.concurrent.thread

@AndroidEntryPoint
class QuickMoveToActivity : VazanActivity() {
    private val viewModel: QuickMoveToViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    companion object {
        const val INTENT_STRING_EXTRA_ENTRY_ID = "entry_id"
    }

    private fun startScan() {
        scanBarcode(
            onSuccess = { viewModel.processLabel(it) { thread { startScan() } } },
            onFailed = { showToast("Scan cancelled/failed"); finish() }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetEntityId = intent.getStringExtra(INTENT_STRING_EXTRA_ENTRY_ID) ?: kotlin.run {
            showToast("$INTENT_STRING_EXTRA_ENTRY_ID not found"); finish(); return
        }

        viewModel.checkEntry(targetEntityId, { thread { startScan() } }) {
            showToast(it)
            finish()
        }

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
                            text = "Move to: ${viewModel.targetEntry.type}",
                            fontSize = 25.sp, lineHeight = 25.sp
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        OneLineText(text = "Target: ${viewModel.targetEntry.name}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        OneLineText(text = "Target: ${viewModel.targetEntry.entryId}")
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
