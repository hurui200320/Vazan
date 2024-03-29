package info.skyblond.vazan.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.GridMenu
import info.skyblond.vazan.ui.composable.MenuItem
import info.skyblond.vazan.ui.startActivity
import info.skyblond.vazan.ui.theme.VazanTheme


@AndroidEntryPoint
class MainActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = emptyMap()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GridMenu(
                        menuItems = listOf(
                            MenuItem(
                                icon = Icons.Outlined.EventNote,
                                action = "Browse"
                            ) { startActivity(BrowseActivity::class) },
                            MenuItem(
                                icon = Icons.Outlined.Print,
                                action = "Print"
                            ) { startActivity(PreparePrintActivity::class) },
                            MenuItem(
                                icon = Icons.Outlined.Search,
                                action = "Search"
                            ) { startActivity(KeywordSearchActivity::class) },
                            MenuItem(
                                icon = Icons.Outlined.QrCodeScanner,
                                action = "Quick scan",
                            ) { startActivity(QuickScanActivity::class) },
                            MenuItem(
                                icon = Icons.Outlined.Settings,
                                action = "Settings",
                            ) { startActivity(SettingsActivity::class) },
                            MenuItem(
                                icon = Icons.Outlined.Backup,
                                action = "Backup",
                            ) { startActivity(BackupActivity::class) }
                        )
                    )
                }
            }
        }
    }
}
