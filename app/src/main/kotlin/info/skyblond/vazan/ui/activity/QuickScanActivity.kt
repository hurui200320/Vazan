package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

    private fun startActivity(entityId: String, targetName: String, targetType: String) {
        startActivity(
            intent(MoveToActivity::class).apply {
                putExtra("entity_id", entityId)
                putExtra("target_name", targetName)
                putExtra("target_type", targetType)
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showToast = { showToast(it) }
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GridMenu(
                        menuItems = listOf(
                            MenuItem(icon = Icons.Outlined.Home, action = "Move to Loc") {
                                viewModel.listLocation()
                                viewModel.showSelectLocationDialog = true
                            },
                            MenuItem(icon = Icons.Outlined.Inventory2, action = "Move to Box") {
                                scanBarcode(
                                    onSuccess = {
                                        viewModel.processMoveToBox(it) { entityId ->
                                            startActivity(entityId, it, "box")
                                        }
                                    },
                                    onFailed = { showToast("Scan cancelled/failed") }
                                )
                            }
                        )
                    )
                    if (viewModel.showSelectLocationDialog) {
                        var expanded by rememberSaveable { mutableStateOf(false) }
                        var selected by remember { mutableStateOf(Pair("", "")) }
                        AlertDialog(
                            properties = DialogProperties(dismissOnClickOutside = false),
                            onDismissRequest = {
                                viewModel.showSelectLocationDialog = false
                                finish()
                            },
                            title = { Text(text = "Select a location") },
                            text = {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        value = selected.first,
                                        onValueChange = {}, readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        viewModel.locationList.forEach {
                                            DropdownMenuItem(text = { Text(text = it.first) },
                                                onClick = { expanded = false; selected = it })
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    enabled = selected.second.isNotBlank(),
                                    onClick = {
                                        viewModel.showSelectLocationDialog = false
                                        startActivity(selected.second, selected.first, "location")
                                    }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    viewModel.showSelectLocationDialog = false
                                    finish()
                                }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}
