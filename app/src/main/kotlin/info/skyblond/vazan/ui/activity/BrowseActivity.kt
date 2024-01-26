package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.EntryCard
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.intent
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.BrowseViewModel

@AndroidEntryPoint
class BrowseActivity : VazanActivity() {
    private val viewModel: BrowseViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    companion object {
        const val INTENT_STRING_EXTRA_PARENT_ID = "parent_id"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SubTypeSelector(
        selected: MutableState<String>
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
                trailingIcon = { TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.getSubTypeOptions().forEach {
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
    private fun CreateNewEntryDialog(showCreateDialog: MutableState<Boolean>) {
        val newEntryType = remember {
            mutableStateOf(viewModel.getSubTypeOptions().firstOrNull() ?: "")
        }
        var newEntryId by remember { mutableStateOf("") }

        fun updateType() {
            newEntryType.value = viewModel.guessType(newEntryId, newEntryType.value)
        }

        AlertDialog(
            modifier = Modifier.imePadding(),
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = true
            ),
            onDismissRequest = { showCreateDialog.value = false },
            title = {
                Text(text = "Create new Entry")
            },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 7.dp)
                    ) {
                        TextField(
                            value = newEntryId,
                            onValueChange = {
                                newEntryId = it.uppercase()
                                updateType()
                            },
                            singleLine = true,
                            isError = newEntryId.isBlank(),
                            label = {
                                Text(text = "Entry Id")
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan entry",
                            modifier = Modifier
                                .size(60.dp)
                                .aspectRatio(1f)
                                .padding(0.dp, 0.dp, 20.dp, 0.dp)
                                .clickable {
                                    scanBarcode({
                                        playToneOk()
                                        newEntryId = it.uppercase()
                                        updateType()
                                    }) {
                                        playToneErr()
                                        showToast("Scan Failed/Canceled")
                                    }
                                }
                        )
                    }
                    SubTypeSelector(newEntryType)
                }

            },
            confirmButton = {
                Button(
                    enabled = newEntryId.isNotBlank() && newEntryType.value.isNotBlank(),
                    onClick = {
                        showCreateDialog.value = false
                        viewModel.createEntry(newEntryId, newEntryType.value, {
                            playToneOk()
                            startActivity(intent(EntryDetailActivity::class).apply {
                                putExtra(
                                    EntryDetailActivity.INTENT_STRING_EXTRA_ENTRY_ID,
                                    it.entryId
                                )
                            })
                        }) {
                            playToneErr()
                            showToast("Failed to create entry")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // try load parent id, otherwise start with root (null)
        viewModel.currentParentId = intent.getStringExtra(INTENT_STRING_EXTRA_PARENT_ID)
        viewModel.refresh()
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val showCreateDialog = remember { mutableStateOf(false) }
                        // fixed head bar
                        Row(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OneLineText(
                                text = "In ${viewModel.currentParentId ?: "root"}",
                                initialFontSize = 25.sp
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create entry",
                                modifier = Modifier
                                    .size(30.dp)
                                    .aspectRatio(1f)
                                    .clickable { showCreateDialog.value = true }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            items(
                                viewModel.entryIds,
                                key = { it.entryId }
                            ) {
                                EntryCard(entry = it)
                            }
                        }

                        if (showCreateDialog.value) {
                            CreateNewEntryDialog(showCreateDialog)
                        }
                    }
                    if (viewModel.loading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.fillMaxHeight(0.5f))
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        // refresh data in case user moved items
        viewModel.refresh()
    }
}
