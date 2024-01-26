package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.EntryTag
import info.skyblond.vazan.ui.composable.EntryTagFlow
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.intent
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.EntryDetailViewModel
import kotlinx.coroutines.delay

@AndroidEntryPoint
class EntryDetailActivity : VazanActivity() {
    private val viewModel: EntryDetailViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    companion object {
        const val INTENT_STRING_EXTRA_ENTRY_ID = "entry_id"
    }

    @Composable
    private fun Field(modifier: Modifier = Modifier, name: String, value: String) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
            Text(
                text = value, modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 5.dp),
                fontSize = 20.sp, fontWeight = FontWeight.Light,
                lineHeight = 22.sp
            )
        }
    }

    @Composable
    private fun EditableField(
        modifier: Modifier = Modifier,
        name: String, value: String,
        title: String,
        maxLines: Int,
        validator: (String) -> Boolean = { true },
        applyChanges: (String) -> Unit
    ) {
        val showAlertDialog = rememberSaveable { mutableStateOf(false) }
        Field(
            modifier = modifier.clickable { showAlertDialog.value = true },
            name = name,
            value = value
        )
        if (showAlertDialog.value) {
            var textFieldStatus by rememberSaveable { mutableStateOf(value) }
            AlertDialog(
                modifier = Modifier.imePadding(),
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    decorFitsSystemWindows = true
                ),
                onDismissRequest = { showAlertDialog.value = false },
                title = {
                    Text(text = title)
                },
                text = {
                    TextField(
                        value = textFieldStatus,
                        onValueChange = { textFieldStatus = it },
                        maxLines = maxLines,
                        singleLine = maxLines == 1,
                        isError = !validator(textFieldStatus),
                        modifier = if (maxLines > 1) Modifier.fillMaxHeight(0.5f) else Modifier
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAlertDialog.value = false
                            applyChanges(textFieldStatus)
                        }) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    Button(
                        enabled = validator(textFieldStatus),
                        onClick = {
                            showAlertDialog.value = false
                        }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SubTypeSelector(
        selected: MutableState<String>,
        options: List<String>
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selected.value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it) },
                        onClick = {
                            expanded = false
                            selected.value = it
                        })
                }
            }
        }
    }

    @Composable
    private fun CreateNewMetadataDialog(showCreateDialog: MutableState<Boolean>) {
        val typeOptions = listOf("TAG", "TEXT")
        val newMetaType = remember {
            mutableStateOf(typeOptions.firstOrNull() ?: "")
        }
        var newEntryName by remember { mutableStateOf("") }

        AlertDialog(
            modifier = Modifier.imePadding(),
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = true
            ),
            onDismissRequest = { showCreateDialog.value = false },
            title = {
                Text(text = "Create new Meta")
            },
            text = {
                Column {
                    TextField(
                        value = newEntryName,
                        onValueChange = {
                            newEntryName = it.lowercase().replace(" ", "_")
                        },
                        singleLine = true,
                        isError = newEntryName.isBlank(),
                        label = {
                            Text(text = "Meta name")
                        },
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 7.dp)
                    )
                    SubTypeSelector(newMetaType, typeOptions)
                }
            },
            confirmButton = {
                Button(
                    enabled = newEntryName.isNotBlank() && newMetaType.value.isNotBlank(),
                    onClick = {
                        showCreateDialog.value = false
                        viewModel.createMeta(newEntryName, newMetaType.value, { }) {
                            playToneErr()
                            showToast("Failed to create meta")
                        }
                    }) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showCreateDialog.value = false
                    }) {
                    Text("Cancel")
                }
            }
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(INTENT_STRING_EXTRA_ENTRY_ID)?.let {
            viewModel.getEntry(it) {
                showToast("Entry $it not found")
                finish()
            }
        } ?: run { showToast("Missing entry id"); finish(); return }

        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        OneLineText(
                            text = "${viewModel.entry.type} ${viewModel.entry.entryId}",
                            initialFontSize = 30.sp,
                        )
                        Divider()

                        EditableField(
                            name = "Name", value = viewModel.entry.name,
                            title = "New name",
                            maxLines = 1
                        ) {
                            viewModel.updateEntry("name", it.trim()) {
                                showToast("Failed to update name")
                            }
                        }
                        Divider()

                        EditableField(
                            name = "Parent",
                            value = viewModel.entry.parentId ?: "root",
                            title = "New parent id",
                            maxLines = 1,
                        ) {
                            val actualValue = if (it.isBlank()) null else it.uppercase()
                            viewModel.updateEntry(
                                BrowseActivity.INTENT_STRING_EXTRA_PARENT_ID,
                                actualValue
                            ) {
                                showToast("Failed to update parent id")
                            }
                        }
                        Divider()

                        if (viewModel.entry.type != "ITEM") {
                            Field(
                                name = "Children", value = viewModel.entry.childrenCount.toString(),
                                modifier = Modifier.clickable {
                                    startActivity(
                                        intent(BrowseActivity::class).apply {
                                            putExtra(
                                                BrowseActivity.INTENT_STRING_EXTRA_PARENT_ID,
                                                viewModel.entry.entryId
                                            )
                                        }
                                    )
                                }
                            )
                            Divider()
                        }


                        EditableField(
                            name = "Note", value = viewModel.entry.note,
                            title = "New note",
                            maxLines = Int.MAX_VALUE
                        ) {
                            viewModel.updateEntry("note", it.trim()) {
                                showToast("Failed to update note")
                            }
                        }
                        Divider()

                        FlowRow {
                            if (viewModel.confirmDeleteEntry) {
                                // when confirm delete, temporarily hide all buttons
                                LaunchedEffect(key1 = Unit) {
                                    delay(3000)
                                    viewModel.confirmDeleteEntry = false
                                }
                                Button(
                                    onClick = {
                                        viewModel.deleteEntry(
                                            { viewModel.confirmDeleteEntry = false; finish() }
                                        ) { showToast("Failed to delete entry") }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.padding(5.dp, 2.dp)
                                ) {
                                    Text(text = "Confirm delete")
                                }
                            } else {
                                // normally we display buttons for move (scan) and delete
                                Button(
                                    onClick = {
                                        scanBarcode({
                                            viewModel.updateEntry(
                                                "parent_id",
                                                it, { playToneOk() }) {
                                                playToneErr()
                                                showToast("Failed to move entry")
                                            }
                                        }) { showToast("Scan failed/canceled") }
                                    },
                                    modifier = Modifier.padding(5.dp, 2.dp)
                                ) {
                                    Text(text = "Move to (scan)")
                                }
                                Button(
                                    onClick = {
                                        if (!viewModel.confirmDeleteEntry) viewModel.confirmDeleteEntry =
                                            true
                                    },
                                    modifier = Modifier.padding(5.dp, 2.dp)
                                ) {
                                    Text(text = "Delete")
                                }
                                Button(
                                    onClick = {
                                        startActivity(intent(PrinterActivity::class).also {
                                            it.putExtra(
                                                PrinterActivity.INTENT_STRING_EXTRA_LABEL,
                                                viewModel.entry.entryId
                                            )
                                        })
                                    },
                                    modifier = Modifier.padding(5.dp, 2.dp)
                                ) {
                                    Text(text = "Print")
                                }
                            }
                        }

                        // Metadata list
                        Divider()
                        val showCreateDialog = remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Metadata",
                                fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create metadata",
                                modifier = Modifier
                                    .size(30.dp)
                                    .aspectRatio(1f)
                                    .clickable { showCreateDialog.value = true }
                            )
                        }

                        // TAG
                        if (viewModel.confirmDeleteMetadata != null) {
                            // when confirm delete, temporarily hide all buttons
                            LaunchedEffect(key1 = Unit) {
                                delay(3000)
                                viewModel.confirmDeleteMetadata = null
                            }
                            viewModel.confirmDeleteMetadata?.let { tag ->
                                // apply the same padding, simulating the flow row
                                Box(Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)) {
                                    EntryTag(
                                        tag = tag,
                                        cardContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        cardContentColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.clickable {
                                            viewModel.deleteMetadata(tag, { playToneOk() }) {
                                                playToneErr()
                                                showToast("Failed to delete metadata")
                                            }
                                            viewModel.confirmDeleteEntry = false
                                        }
                                    )
                                }
                            }
                        } else {
                            EntryTagFlow(viewModel.entry.metaList.filter { it.type == "TAG" }) {
                                showToast("Tap again to delete")
                                viewModel.confirmDeleteMetadata = it
                            }
                        }

                        // TEXT
                        viewModel.entry.metaList.filter { it.type == "TEXT" }.forEach { meta ->
                            Box(modifier = Modifier.padding(20.dp, 0.dp, 0.dp, 0.dp)) { Divider() }
                            Spacer(modifier = Modifier.width(10.dp))
                            EditableField(
                                name = "TEXT ${meta.name}",
                                value = meta.value,
                                title = "Update value of ${meta.name}",
                                maxLines = Int.MAX_VALUE,
                                modifier = Modifier.padding(20.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                viewModel.updateMeta(meta.name, "value", it.trim()) {
                                    showToast("Failed to update value of ${meta.name}")
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        // other than tag and text, everything else
                        viewModel.entry.metaList.filterNot {
                            it.type == "TEXT" || it.type == "TAG"
                        }.forEach {
                            Field(
                                name = "${it.type} ${it.name}",
                                value = if (it.needValue) it.value else ""
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                        }

                        if (showCreateDialog.value) {
                            CreateNewMetadataDialog(showCreateDialog)
                        }
                    }
                }
            }
        }
    }
}
