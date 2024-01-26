package info.skyblond.vazan.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * [key]: The database key.
 * [valueProvider]: Given the key, eval the current value.
 * */
@Composable
private fun ConfigItem(
    key: String,
    valueProvider: (String) -> String,
    dialogContent: @Composable (MutableState<Boolean>) -> Unit,
) {
    val showAlertDialog = rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { showAlertDialog.value = true }
    ) {
        Text(
            key,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(5.dp)
        )
        Text(
            valueProvider(key),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(10.dp)
        )

        if (showAlertDialog.value) {
            dialogContent(showAlertDialog)
        }
    }
}

@Composable
fun ConfigTextItem(
    key: String,
    valueProvider: (String) -> String,
    validator: (String) -> Boolean,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
) {
    ConfigItem(key = key, valueProvider = valueProvider) { showAlertDialog ->
        var textFieldStatus by rememberSaveable { mutableStateOf(valueProvider(key)) }
        AlertDialog(
            modifier = Modifier.imePadding(),
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = true
            ),
            onDismissRequest = { showAlertDialog.value = false },
            title = {
                Text(text = key)
            },
            text = {
                TextField(
                    value = textFieldStatus,
                    onValueChange = { textFieldStatus = it },
                    maxLines = 10,
                    singleLine = singleLine,
                    isError = !validator(textFieldStatus),
                )
            },
            confirmButton = {
                Button(
                    enabled = validator(textFieldStatus),
                    onClick = {
                        showAlertDialog.value = false
                        onValueChange(textFieldStatus)
                    }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
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
fun <T> ConfigSelectItem(
    key: String,
    valueProvider: (String) -> String,
    items: () -> List<T>,
    onValueChange: (T) -> Unit,
    itemToString: (T) -> String = { it.toString() }
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    // Do not use rememberSavable here since T might not be serializable
    // thus cause the "Check failed" crash
    var selected by remember { mutableStateOf<T?>(null) }
    ConfigItem(
        key = key,
        valueProvider = valueProvider
    ) { showAlertDialog ->
        AlertDialog(
            modifier = Modifier.imePadding(),
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = true
            ),
            onDismissRequest = { showAlertDialog.value = false },
            title = {
                Text(text = key)
            },
            text = {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selected?.let { itemToString(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items().forEach {
                            DropdownMenuItem(text = {
                                Text(text = itemToString(it))
                            }, onClick = {
                                expanded = false
                                selected = it
                            })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selected != null,
                    onClick = {
                        showAlertDialog.value = false
                        onValueChange(selected!!)
                    }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAlertDialog.value = false
                    }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewConfigItem() {
    ConfigTextItem(
        key = "memento.location.parent_box_field_id_something_super_long_that_use_a_signle_ling.something",
        valueProvider = { "Value12345\n67890".repeat(20) },
        validator = { true }, onValueChange = {}
    )
}