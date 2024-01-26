package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.GridMenu
import info.skyblond.vazan.ui.composable.MenuItem
import info.skyblond.vazan.ui.intent
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.QuickScanViewModel

@AndroidEntryPoint
class QuickScanActivity : VazanActivity() {
    private val viewModel: QuickScanViewModel by viewModels()
    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showErr = {
            playToneErr()
            showToast(it)
        }
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GridMenu(
                        menuItems = listOf(
                            MenuItem(icon = Icons.Outlined.Home, action = "Quick Add") {
                                startActivity(intent(QuickAddActivity::class))
                            },
                            MenuItem(icon = Icons.Outlined.Inventory2, action = "Quick Move") {
                                scanBarcode(
                                    onSuccess = {
                                        viewModel.requireNonItem(it, "move") {
                                            startActivity(intent(QuickMoveToActivity::class).apply {
                                                putExtra(
                                                    QuickMoveToActivity.INTENT_STRING_EXTRA_ENTRY_ID,
                                                    it
                                                )
                                            })
                                        }
                                    },
                                    onFailed = { showToast("Scan cancelled/failed") }
                                )
                            }
                        )
                    )
                }
            }
        }
    }
}
