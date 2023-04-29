package info.skyblond.vazan.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.data.room.Label
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.SyncViewModel

@AndroidEntryPoint
class SyncActivity : VazanActivity() {
    private val viewModel: SyncViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showToast = { showToast(it) }
        setContent {
            // keep screen on
            DisposableEffect(key1 = Unit) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
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
                            text = "Currently syncing: ${viewModel.isCurrentlySyncing}",
                            color = if (viewModel.isCurrentlySyncing) Color.Red else Color.Unspecified
                        )
                        Text(text = "Last sync version: ${viewModel.lastSyncTimestamp}")
                        Text(text = "Current sync version: ${viewModel.currentSyncingVersion}")
                        Text(text = "Synced box count: ${viewModel.syncedBoxCount}")
                        Text(text = "Synced item count: ${viewModel.syncedItemCount}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Button(
                            onClick = { viewModel.sync() },
                            enabled = !viewModel.isCurrentlySyncing
                        ) {
                            Text(text = "Sync")
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                        Button(
                            onClick = { viewModel.clearPrintedLabels(Label.Status.PRINTED) },
                            enabled = !viewModel.isCurrentlySyncing,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Text(text = "Clear printed labels")
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                        Button(
                            onClick = { viewModel.clearPrintedLabels(Label.Status.IN_USE) },
                            enabled = !viewModel.isCurrentlySyncing,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Text(text = "Clear in_use labels")
                        }
                    }
                    // show warning when syncing
                    if (viewModel.isCurrentlySyncing) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Do not exit this page while syncing!",
                                color = Color.Red,
                                lineHeight = 60.sp,
                                fontSize = 60.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
