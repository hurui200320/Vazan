package info.skyblond.vazan.ui.composable

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridMenuItem(
    icon: ImageVector,
    text: String,
    onClick: Context.() -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(10.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            onClick = { onClick(context) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(0.07f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, contentDescription = text,
                    modifier = Modifier.size(150.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = text, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

data class MenuItem(
    val icon: ImageVector,
    val action: String,
    val callback: Context.() -> Unit
)

@Composable
fun GridMenu(
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    style: GridCells = GridCells.Adaptive(180.dp),
) {
    LazyVerticalGrid(
        columns = style, modifier = modifier
    ) {
        items(menuItems) { item ->
            GridMenuItem(
                icon = item.icon,
                text = item.action,
                onClick = item.callback
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGridMenuItem() {
    val list = listOf(
        MenuItem(Icons.Outlined.Print, "Print label") {},
        MenuItem(Icons.Outlined.Search, "Search") {},
        MenuItem(Icons.Outlined.QrCodeScanner, "Scan QR code") {},
        MenuItem(Icons.Outlined.Backup, "Backup") {},
        MenuItem(Icons.Outlined.Settings, "Settings") {},
    )
    GridMenu(menuItems = list)
}