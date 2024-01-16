package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.data.room.Label
import info.skyblond.vazan.domain.LabelEncoding
import info.skyblond.vazan.ui.intent
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.MaterialColors
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.PreparePrintActivity

@AndroidEntryPoint
class PreparePrintActivity : VazanActivity() {
    private val viewModel: PreparePrintActivity by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    private val printerActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            if (r.resultCode == RESULT_OK) {
                r.data?.getStringExtra("label")?.also {
                    viewModel.afterPrintLabel(it)
                } ?: showToast("Invalid print result")
            } else {
                showToast("Print cancelled")
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSelectTypeDialog by rememberSaveable { mutableStateOf(false) }
                    var showManualInputDialog by rememberSaveable { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Type: ${viewModel.labelType.name}",
                            modifier = Modifier
                                .clickable { showSelectTypeDialog = true }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Text(text = "Label: ${viewModel.labelValue}")
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        Text(
                            text = "Status: ${viewModel.labelStatus}",
                            color = when (viewModel.labelStatus) {
                                Label.Status.IN_USE.name -> MaterialColors.Red600
                                Label.Status.PRINTED.name -> MaterialColors.Orange600
                                "NEW" -> MaterialColors.Green600
                                else -> MaterialColors.Cyan600
                            }
                        )
                        Spacer(modifier = Modifier.fillMaxHeight(0.04f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = { viewModel.generateLabel() }) { Text(text = "Random") }

                            Button(
                                onClick = {
                                    scanBarcode(
                                        onSuccess = {
                                            if (!viewModel.setLabel(it)) showToast("Invalid label")
                                        },
                                        onFailed = { showToast("Scan cancelled/failed") }
                                    )
                                }
                            ) {
                                Text(text = "Scan")
                            }

                            Button(onClick = { showManualInputDialog = true }) {
                                Text(text = "Manual")
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.06f))
                        Button(
                            onClick = {
                                printerActivityLauncher.launch(
                                    intent(PrinterActivity::class).also {
                                        it.putExtra("label", viewModel.labelValue)
                                    }
                                )
                            }
                        ) {
                            Text(text = "Print")
                        }
                    }
                    if (showSelectTypeDialog) {
                        var expanded by rememberSaveable { mutableStateOf(false) }
                        var selected by remember { mutableStateOf(viewModel.labelType) }
                        AlertDialog(
                            properties = DialogProperties(dismissOnClickOutside = false),
                            onDismissRequest = { showSelectTypeDialog = false },
                            title = { Text(text = "Select label type") },
                            text = {
                                ExposedDropdownMenuBox(
                                    expanded = expanded, onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        value = selected.name,
                                        onValueChange = {}, readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded, onDismissRequest = { expanded = false }
                                    ) {
                                        LabelEncoding.LabelType.values().forEach {
                                            DropdownMenuItem(text = { Text(text = it.name) },
                                                onClick = { expanded = false; selected = it })
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.labelType = selected
                                    viewModel.generateLabel()
                                    showSelectTypeDialog = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showSelectTypeDialog = false
                                }) { Text("Cancel") }
                            }
                        )
                    }

                    if (showManualInputDialog) {
                        var textInput by rememberSaveable { mutableStateOf("") }
                        AlertDialog(
                            properties = DialogProperties(dismissOnClickOutside = false),
                            onDismissRequest = { showManualInputDialog = false },
                            title = { Text(text = "Type label manually") },
                            text = {
                                TextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    singleLine = true,
                                    isError = !viewModel.isValidLabel(textInput),
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (!viewModel.setLabel(textInput)) showToast("Invalid label")
                                    showManualInputDialog = false
                                }, enabled = viewModel.isValidLabel(textInput)) { Text("OK") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showManualInputDialog = false
                                }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}
