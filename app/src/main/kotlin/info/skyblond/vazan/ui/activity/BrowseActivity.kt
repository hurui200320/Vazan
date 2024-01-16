package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.BrowseViewModel

@AndroidEntryPoint
class BrowseActivity : VazanActivity() {
    private val viewModel: BrowseViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // try load parent id, otherwise start with root (null)
        viewModel.currentParentId = intent.getStringExtra("parent_id")
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
                        // fixed head bar
                        Row(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entries in ${viewModel.currentParentId ?: "root"}",
                                fontSize = 25.sp,
                                modifier = Modifier.clickable { viewModel.refresh() }
                            )
                            Spacer(modifier = Modifier.fillMaxWidth(0.03f))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create entry",
                                modifier = Modifier.size(25.dp)
                                    .clickable { showToast("huh?") }
                            )
                        }
                        // scrollable content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            if (viewModel.loading) {
                                Text(text = "Loading...", fontSize = 25.sp, color = Color.Red)
                            } else {
                                viewModel.entryIds.forEach {
                                    // TODO: Entry card and jump to that page
                                    // TODO: Create new entry under this parent
                                    Text(
                                        text = it.toString(),
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                    Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
