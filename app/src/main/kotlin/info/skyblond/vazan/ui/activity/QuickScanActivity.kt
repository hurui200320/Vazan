package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
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
                    val showParentEntryIdDialog = remember { mutableStateOf(false) }
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
                            },
                            MenuItem(icon = Icons.Outlined.Inventory2, action = "Quick Move (M)") {
                                showParentEntryIdDialog.value = true
                            },
                            MenuItem(icon = Icons.Outlined.Search, action = "Quick View") {
                                scanBarcode(
                                    onSuccess = {
                                        startActivity(intent(EntryDetailActivity::class).apply {
                                            putExtra(
                                                EntryDetailActivity.INTENT_STRING_EXTRA_ENTRY_ID,
                                                it
                                            )
                                        })
                                    },
                                    onFailed = { showToast("Scan cancelled/failed") }
                                )
                            }
                        )
                    )
                    if (showParentEntryIdDialog.value) {
                            var textFieldStatus by rememberSaveable { mutableStateOf("") }
                            AlertDialog(
                                modifier = Modifier.imePadding(),
                                properties = DialogProperties(
                                    dismissOnClickOutside = false,
                                    decorFitsSystemWindows = true
                                ),
                                onDismissRequest = { showParentEntryIdDialog.value = false },
                                title = {
                                    Text(text = "Input target entry id")
                                },
                                text = {
                                    TextField(
                                        value = textFieldStatus,
                                        onValueChange = { textFieldStatus = it.uppercase() },
                                        singleLine = true,
                                        isError = textFieldStatus.isBlank(),
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        enabled = textFieldStatus.isNotBlank(),
                                        onClick = {
                                            showParentEntryIdDialog.value = false
                                            viewModel.requireNonItem(textFieldStatus, "move") {
                                                startActivity(intent(QuickMoveToActivity::class).apply {
                                                    putExtra(
                                                        QuickMoveToActivity.INTENT_STRING_EXTRA_ENTRY_ID,
                                                        textFieldStatus
                                                    )
                                                })
                                            }
                                        }) {
                                        Text("Go")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            showParentEntryIdDialog.value = false
                                        }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                    }
                }
            }
        }
    }
}
