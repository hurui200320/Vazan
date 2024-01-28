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
import info.skyblond.vazan.ui.composable.ConfigTextItem
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.startActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.SettingsViewModel

@AndroidEntryPoint
class SettingsActivity : VazanActivity() {
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    Surface(modifier = Modifier.padding(scaffoldPadding)) {
                        LazyColumn {
                            // jim api
                            item {
                                TextConfig(settingKey = SettingsKey.JIM_HOST) {
                                    showToast("Changes need 5s to apply...")
                                }
                            }
                            item {
                                TextConfig(settingKey = SettingsKey.JIM_API_PASSWORD) {
                                    showToast("Changes need 5s to apply...")
                                }
                            }
                            item { Divider() }
                            item { TextConfig(settingKey = SettingsKey.APP_LAST_PRINTER_ADDRESS) }
                            item { TextConfig(settingKey = SettingsKey.APP_LAST_PRINTER_PAPER) }
                            item { TextConfig(settingKey = SettingsKey.APP_LAST_PRINTER_REPEAT) }
                        }
                    }
                }
            }
        }
    }
}