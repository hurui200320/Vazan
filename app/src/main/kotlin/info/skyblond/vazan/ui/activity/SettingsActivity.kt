package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.ui.composable.ConfigSelectItem
import info.skyblond.vazan.ui.composable.ConfigTextItem
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.startActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SettingsActivity : VazanActivity(), CoroutineScope {
    override val permissionExplanation: Map<String, String> = emptyMap()
    private val viewModel: SettingsViewModel by viewModels()

    @Composable
    private fun TextConfig(settingKey: SettingsKey, action: () -> Unit = {}) {
        ConfigTextItem(
            key = settingKey.key,
            valueProvider = { viewModel.getConfigByKey(settingKey).value },
            validator = settingKey.validator,
            onValueChange = { viewModel.updateConfigByKey(settingKey, it); action() },
            singleLine = settingKey.singleLine
        )
    }

    @Composable
    private fun MementoLibraryConfig(settingKey: SettingsKey, action: () -> Unit = {}) {
        ConfigSelectItem(
            key = settingKey.key, valueProvider = { viewModel.getConfigByKey(settingKey).value },
            items = { viewModel.getLibraryList() },
            onValueChange = { viewModel.updateConfigByKey(settingKey, it.id); action() },
            itemToString = { it.name + "(${it.owner})" }
        )
    }

    @Composable
    private fun MementoFieldsConfig(settingKey: SettingsKey, lib: SettingsKey) {
        ConfigSelectItem(
            key = settingKey.key, valueProvider = { viewModel.getConfigByKey(settingKey).value },
            items = { viewModel.getLibraryFields(lib) },
            onValueChange = { viewModel.updateConfigByKey(settingKey, it.id.toString()) },
            itemToString = { it.name + "(${it.type})" }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showToast = { showToast(it) }
        setContent {
            VazanTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Settings") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "go back"
                                    )
                                }
                            }
                        )
                    },
                ) { scaffoldPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(scaffoldPadding)
                    ) {
                        LazyColumn {
                            // api
                            item {
                                TextConfig(settingKey = SettingsKey.MEMENTO_API_KEY) {
                                    showToast("Restarting to apply new API key...")
                                    finish()
                                    startActivity(SettingsActivity::class)
                                }
                            }
                            item { Divider() }
                            // location
                            item { MementoLibraryConfig(settingKey = SettingsKey.MEMENTO_LOCATION_LIBRARY_ID) }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_LOCATION_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_LOCATION_LIBRARY_ID
                                )
                            }
                            item { Divider() }
                            // box
                            item { MementoLibraryConfig(settingKey = SettingsKey.MEMENTO_BOX_LIBRARY_ID) }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_BOX_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_BOX_LIBRARY_ID
                                )
                            }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_BOX_PARENT_LOCATION_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_BOX_LIBRARY_ID
                                )
                            }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_BOX_PARENT_BOX_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_BOX_LIBRARY_ID
                                )
                            }
                            item { Divider() }
                            // item
                            item { MementoLibraryConfig(settingKey = SettingsKey.MEMENTO_ITEM_LIBRARY_ID) }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_ITEM_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_ITEM_LIBRARY_ID
                                )
                            }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_ITEM_PARENT_LOCATION_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_ITEM_LIBRARY_ID
                                )
                            }
                            item {
                                MementoFieldsConfig(
                                    settingKey = SettingsKey.MEMENTO_ITEM_PARENT_BOX_FIELD_ID,
                                    lib = SettingsKey.MEMENTO_ITEM_LIBRARY_ID
                                )
                            }
                            item { Divider() }
                            item { TextConfig(settingKey = SettingsKey.MEMENTO_SYNC_VERSION) }
                        }
                    }
                }
            }
        }
    }

    private val job = Job() + Dispatchers.Main
    override val coroutineContext: CoroutineContext = job
}