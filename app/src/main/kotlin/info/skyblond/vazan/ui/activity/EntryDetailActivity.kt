package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.intent
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.startActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.EntryDetailViewModel

@AndroidEntryPoint
class EntryDetailActivity : VazanActivity() {
    private val viewModel: EntryDetailViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

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
                        modifier = Modifier.fillMaxHeight(0.5f)
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

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra("entry_id")?.let {
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
                            viewModel.updateEntry("parent_id", actualValue) {
                                showToast("Failed to update parent id")
                            }
                        }
                        Divider()

                        Field(
                            name = "Children", value = viewModel.entry.childrenCount.toString(),
                            modifier = Modifier.clickable {
                                startActivity(
                                    intent(BrowseActivity::class).apply {
                                        putExtra("parent_id", viewModel.entry.entryId)
                                    }
                                )
                            }
                        )
                        Divider()

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
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "Move to (scan)")
                            }
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "Delete")
                            }
                            if (viewModel.confirmDelete) {
                                // TODO Another button to confirm delete, with timeout
                                Button(onClick = { /*TODO*/ }) {
                                    Text(text = "Really delete?")
                                }
                            }

                        }

                        // TODO Metadata CRUD?
                        Divider()
                        Spacer(modifier = Modifier.fillMaxHeight(0.01f))
                        Text(text = "Meta here?")

                        // val metaList: List<JimMeta>


                        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        Text(
                            text = "TODO", fontSize = 30.sp, lineHeight = 30.sp
                        )
                    }
                }
            }
        }
    }
}
